package com.sardonicus.tobaccocellar.ui.settings

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.data.allMigrations
import com.sardonicus.tobaccocellar.data.multiDeviceSync.DownloadSyncWorker
import com.sardonicus.tobaccocellar.data.multiDeviceSync.GoogleDriveServiceHelper
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextPreset
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class SettingsViewModel(
    private val itemsRepository: ItemsRepository,
    val filterViewModel: FilterViewModel,
    val preferencesRepo: PreferencesRepo,
    private val networkMonitor: NetworkMonitor,
    private val application: Application
): ViewModel() {

    /** Display Settings */
    val themeSetting = preferencesRepo.themeSetting
    val showRatings = preferencesRepo.showRating.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val typeGenreOptionEnablement: StateFlow<Map<TypeGenreOption, Boolean>> = combine(
        filterViewModel.typesExist,
        filterViewModel.subgenresExist,
    ) { types, subgenres ->
        mapOf(
            TypeGenreOption.TYPE to (types || !subgenres),
            TypeGenreOption.SUBGENRE to subgenres,
            TypeGenreOption.BOTH to (types && subgenres),
            TypeGenreOption.TYPE_FALLBACK to types,
            TypeGenreOption.SUB_FALLBACK to subgenres,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = mapOf(
                TypeGenreOption.TYPE to true,
                TypeGenreOption.SUBGENRE to true,
                TypeGenreOption.BOTH to true,
                TypeGenreOption.TYPE_FALLBACK to true,
                TypeGenreOption.SUB_FALLBACK to true,
            )
        )

    val typeGenreOption = combine(
        preferencesRepo.typeGenreOption,
        typeGenreOptionEnablement
    ) { option, typeGenreEnablement ->
        val enabled = typeGenreEnablement[option] ?: true
        if (enabled) option else TypeGenreOption.TYPE
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TypeGenreOption.TYPE)

    val quantityOption = preferencesRepo.quantityOption.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuantityOption.TINS)

    val parseLinks = preferencesRepo.parseLinks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val globalTwoPane = preferencesRepo.globalTwoPane.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val landscapeTwoPane = preferencesRepo.landscapeTwoPane

    val twoColumnTabs = preferencesRepo.twoColumnTabs


    /** App & Database settings */
    val deviceSyncAcknowledgement = preferencesRepo.crossDeviceAcknowledged.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val crossDeviceSync = preferencesRepo.crossDeviceSync
        .onEach { if (it) _signingIn.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _signingIn = MutableStateFlow(false)
    val signingIn = _signingIn.asStateFlow()

    val allowMobileData = preferencesRepo.allowMobileData.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userEmail = preferencesRepo.signedInUserEmail.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hasScope = preferencesRepo.hasDriveScope.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val tinOzConversionRate = preferencesRepo.tinOzConversionRate.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TinConversionRates.DEFAULT.ozRate)

    val tinGramsConversionRate = preferencesRepo.tinGramsConversionRate.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TinConversionRates.DEFAULT.gramsRate)

    val defaultSyncOption = preferencesRepo.defaultSyncOption.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    /** General UI control **/
    private val _openDialog = MutableStateFlow<DialogType?>(null)
    val openDialog: StateFlow<DialogType?> = _openDialog.asStateFlow()

    private val _snackbarState = MutableStateFlow(SnackbarState())
    val snackbarState: StateFlow<SnackbarState> = _snackbarState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()


    init {
        viewModelScope.launch {
            EventBus.events.collect { event ->
                if (event is SignInCancelled) {
                    _signingIn.value = false
                }
                if (event is SignOutEvent) {
                    _signingIn.value = false
                }
            }
        }
    }

    val networkEnabled: StateFlow<Boolean> = combine(
        preferencesRepo.allowMobileData,
        networkMonitor.isConnected,
        networkMonitor.isWifi
    ) { allowMobile, isConnected, isWifi ->
        (allowMobile && isConnected) || isWifi
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )


    val displaySettings = combine(
        themeSetting,
        showRatings,
        typeGenreOption,
        quantityOption,
        parseLinks
    ) { theme, showRatings, typeGenre, quantity, parseLinks ->
        listOf(
            SettingsDialog("Theme", "Change the theme of the app.", theme.value, DialogType.Theme),
            SettingsDialog("Cellar Ratings Visibility", "Show/hide ratings in list view.", showRatings.let { if (it) "On" else "Off" }, DialogType.Ratings),
            SettingsDialog("Cellar Type/Genre Display", "Set type/genre display for Cellar screen.", typeGenre.value, DialogType.TypeGenre),
            SettingsDialog("Cellar Quantity Display", "Change quantity display on Cellar screen.", quantity.let {
                when (it) {
                    QuantityOption.TINS -> "Tins"
                    QuantityOption.OUNCES -> "Oz/lbs"
                    QuantityOption.GRAMS -> "Grams"
                }
            }, DialogType.QuantityDisplay),
            SettingsDialog("Parse Links in Notes", "Enable/disable link parsing in notes.", parseLinks.let { if (it) "On" else "Off" }, DialogType.ParseLinks),
            SettingsDialog("Large Screen Options", "Large screen adaptive layout options.", null, DialogType.GlobalTwoPane)
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val databaseSettings = combine (
        crossDeviceSync,
        allowMobileData,
        networkEnabled,
        tinOzConversionRate,
        tinGramsConversionRate,
        defaultSyncOption,
    ) { values: Array<Any> ->
        val crossDeviceSync = values[0] as Boolean
        val mobileData = values[1] as Boolean
        val connected = values[2] as Boolean
        val ozRate = values[3] as Double
        val gramsRate = values[4] as Double
        val defaultSync = values[5] as Boolean

        listOf(
            SettingsDialog("Multi-Device Sync", "Enable/disable cross-device sync.", crossDeviceSync.let {
                if (it) {
                    if (!connected) "Disconnected" else "On (${if (mobileData) "mobile" else "WiFi"})"
                } else "Off"
            },
                DialogType.DeviceSync),
            SettingsDialog("Tin Conversion Rates", "Change tin conversion rates.", "$ozRate oz/${formatDecimal(gramsRate)} g", DialogType.TinRates),
            SettingsDialog("Default \"Sync Tins?\" Option", "Set default tin sync option.", defaultSync.let { if (it) "On" else "Off" }, DialogType.TinSyncDefault),
            SettingsDialog("Backup/Restore", "Backup or restore database and/or settings.", null, DialogType.BackupRestore),
            SettingsDialog("Other Db Operations", "Fix sync quantities and optimize database.", null, DialogType.DbOperations),
            SettingsDialog("Delete Database", "Delete all entries.", null, DialogType.DeleteAll)
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun showDialog(dialog: DialogType) { _openDialog.value = dialog }

    fun dismissDialog() { _openDialog.value = null }

    fun showSnackbar(message: String) { _snackbarState.value = SnackbarState(true, message) }

    fun snackbarShown() { _snackbarState.value = SnackbarState(false, "") }

    fun setLoadingState(loading: Boolean) { _loading.value = loading }


    /** Display Settings **/

    fun saveThemeSetting(setting: String) {
        viewModelScope.launch {
            preferencesRepo.saveThemeSetting(setting)
        }
    }

    fun saveQuantityOption(option: String) {
        viewModelScope.launch {
            preferencesRepo.saveQuantityPreference(option)
        }
    }

    fun saveShowRatingOption(option: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveShowRatingOption(option)
        }
    }

    fun saveTypeGenreOption(option: String) {
        viewModelScope.launch {
            preferencesRepo.saveTypeGenreOption(option)
        }
    }

    fun saveParseLinksOption(option: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveParseLinksOption(option)
        }
    }

    fun saveGlobalTwoPane(option: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveGlobalTwoPane(option)
        }
    }

    fun saveLandscapeTwoPane(option: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveLandscapeTwoPane(option)
        }
    }

    fun saveTwoColumnTabs(option: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveTwoColumnTabs(option)
        }
    }


    /** Database Settings **/
    fun saveCrossDeviceAcknowledged() {
        viewModelScope.launch {
            preferencesRepo.saveCrossDeviceAcknowledged(true)
        }
    }

    fun saveCrossDeviceSync(enable: Boolean) {
        viewModelScope.launch {
            if (enable) {
                val email = userEmail.value
                if (!email.isNullOrEmpty()) {
                    (application as CellarApplication).periodicDownloadSetup()
                }
                _signingIn.value = userEmail.value.isNullOrEmpty()
                EventBus.emit(SignInEvent)
            } else {
                _signingIn.value = false
                preferencesRepo.saveCrossDeviceSync(false)
                stopWorkers()
            }
        }
    }

    fun saveAllowMobileData(enable: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveAllowMobileData(enable)
        }
    }

    fun manualSync() {
        viewModelScope.launch {
            if (SyncStateManager.isSyncing.first()) {
                showSnackbar("Sync already in progress.")
                return@launch
            }

            preferencesRepo.signedInUserEmail.first() ?: return@launch

            if (!networkEnabled.value) {
                val message = if (!networkMonitor.isWifi.first()) "allow mobile data is off"
                    else "check connection"
                showSnackbar("Failed: $message.")
                return@launch
            }

            val workManager = WorkManager.getInstance(application)

            val allowMobile = preferencesRepo.allowMobileData.first()
            val networkType = if (allowMobile) NetworkType.CONNECTED else NetworkType.UNMETERED

            itemsRepository.triggerUploadWorker()

            val workRequest = OneTimeWorkRequestBuilder<DownloadSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(networkType)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(
                "manual_download_sync",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            val timeoutJob = viewModelScope.launch {
                delay(5000.milliseconds)
                val workInfo = workManager.getWorkInfoById(workRequest.id).get()
                if (workInfo?.state == WorkInfo.State.ENQUEUED) {
                    workManager.cancelWorkById(workRequest.id)
                    setLoadingState(false)
                    showSnackbar("Sync failed (connection timeout).")
                }
            }

            workManager.getWorkInfoByIdFlow(workRequest.id)
                .collect { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.ENQUEUED -> {
                            setLoadingState(true)
                        }
                        WorkInfo.State.RUNNING -> {
                            timeoutJob.cancel()
                            setLoadingState(true)
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            timeoutJob.cancel()
                            setLoadingState(false)
                            val result = workInfo.outputData
                            when (result.getString(DownloadSyncWorker.RESULT_KEY)) {
                                DownloadSyncWorker.SYNC_COMPLETE -> {
                                    showSnackbar("Sync complete.")
                                }
                                DownloadSyncWorker.REMOTE_EMPTY -> {
                                    showSnackbar("Remote files not found.")
                                }
                                DownloadSyncWorker.UP_TO_DATE -> {
                                    showSnackbar("No new sync data available.")
                                }
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            timeoutJob.cancel()
                            setLoadingState(false)
                            showSnackbar("Sync failed (check connection).")
                        }
                        WorkInfo.State.CANCELLED -> {
                            timeoutJob.cancel()
                            setLoadingState(false)
                        }
                        else -> {
                            timeoutJob.cancel()
                            setLoadingState(false)
                        }
                    }
                }
        }
    }

    fun clearRemoteData() {
        viewModelScope.launch {
            if (SyncStateManager.isSyncing.first()) {
                showSnackbar("Sync in progress, please wait for it to finish.")
                return@launch
            }

            if (!networkEnabled.value) {
                val message = if (!networkMonitor.isWifi.first()) "allow mobile data is off"
                    else "check connection"
                showSnackbar("Failed: $message.")
                return@launch
            }

            setLoadingState(true)

            withContext(Dispatchers.IO) {
                val email = preferencesRepo.signedInUserEmail.first()
                if (email == null) {
                    setLoadingState(false)
                    return@withContext
                }

                try {
                    val driveService = GoogleDriveServiceHelper.getDriveService(application, email)

                    val files = driveService.files().list()
                        .setSpaces("appDataFolder")
                        .setFields("files(id, name, createdTime)")
                        .execute()

                    if (files.files.isNullOrEmpty()) {
                        setLoadingState(false)
                        showSnackbar("No data to delete.")
                        return@withContext
                    }

                    val batch = driveService.batch()
                    val callback = object : JsonBatchCallback<Void>() {
                        override fun onSuccess(
                            t: Void?,
                            responseHeaders: com.google.api.client.http.HttpHeaders?
                        ) { }

                        override fun onFailure(
                            e: com.google.api.client.googleapis.json.GoogleJsonError?,
                            responseHeaders: com.google.api.client.http.HttpHeaders?
                        ) {  }
                    }

                    for (file in files.files) {
                        driveService.files().delete(file.id).queue(batch, callback)
                    }

                    batch.execute()
                    showSnackbar("Remote data deleted.")

                } catch (_: Exception) {
                    showSnackbar("Error deleting remote data.")
                } finally {
                    setLoadingState(false)
                }
            }
        }
    }

    fun clearLoginState() {
        viewModelScope.launch {
            EventBus.emit(SignOutEvent)
        }
    }

    fun stopWorkers() {
        viewModelScope.launch { (application as CellarApplication).cancelPeriodicSync() }
    }

    fun setTinConversionRates(ozRate: Double, gramsRate: Double) {
        viewModelScope.launch {
            preferencesRepo.setTinOzConversionRate(ozRate)
            preferencesRepo.setTinGramsConversionRate(gramsRate)

            updateTinSync(ozRate, gramsRate)
        }
    }

    fun setDefaultSyncOption(option: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveDefaultSyncOption(option)
        }
    }

    fun updateTinSync(ozConversion: Double? = null, gramsConversion: Double? = null, runSilent: Boolean = false) {
        viewModelScope.launch {
            if (!runSilent) {
                setLoadingState(true)
            }
            SyncStateManager.schedulingPaused = true

            var message = ""
            val allItems = filterViewModel.everythingFlow.first()
            val allSyncItems = allItems.filter { it.items.syncTins }

            val ozRate = ozConversion ?: preferencesRepo.tinOzConversionRate.first()
            val gramsRate = gramsConversion ?: preferencesRepo.tinGramsConversionRate.first()

            try {
                allSyncItems.forEach { items ->
                    val tins = items.tins.filter { !it.finished }
                    val syncQuantity = calculateSyncTins(tins, ozRate, gramsRate)
                    itemsRepository.updateItem(
                        items.items.copy(
                            quantity = syncQuantity,
                        )
                    )
                }

                message = if (ozConversion != null || gramsConversion != null) {
                    "Conversion rates and synced entry quantities updated."
                } else { "Synced entry quantities updated." }
            } catch (_: Exception) {
                SyncStateManager.schedulingPaused = false
                message = "Error updating sync quantities."
            } finally {
                if (!runSilent) {
                    setLoadingState(false)
                    SyncStateManager.schedulingPaused = false
                    itemsRepository.triggerUploadWorker()
                    showSnackbar(message)
                }
            }
        }
    }

    fun optimizeDatabase() {
        viewModelScope.launch {
            setLoadingState(true)

            itemsRepository.optimizeDatabase()

            setLoadingState(false)
            showSnackbar("Optimization complete.")
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch(Dispatchers.Default) {
            itemsRepository.deleteAllItems()
            saveTypeGenreOption(TypeGenreOption.TYPE.value)
            showSnackbar("Database deleted!")
        }
    }

    private fun calculateSyncTins(tins: List<Tins>, ozRate: Double, gramsRate: Double): Int {
        val totalLbsTins = tins.filter { it.unit == "lbs" }.sumOf {
            (it.tinQuantity * 16) / ozRate
        }
        val totalOzTins = tins.filter { it.unit == "oz" }.sumOf {
            it.tinQuantity / ozRate
        }
        val totalGramsTins = tins.filter { it.unit == "grams" }.sumOf {
            it.tinQuantity / gramsRate
        }
        return (totalLbsTins + totalOzTins + totalGramsTins).roundToInt()
    }


    /** Backup/Restore **/
    // Backup //
    private val _backupState = MutableStateFlow(BackupState())
    var backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _restoreState = MutableStateFlow(RestoreState())
    var restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    init { updateSuggestedFilename() }

    fun onBackupOptionChanged(backupState: BackupState) {
        _backupState.value = _backupState.value.copy(
            databaseChecked = backupState.databaseChecked,
            settingsChecked = backupState.settingsChecked,
        )
        updateSuggestedFilename()
    }

    fun onRestoreOptionChanged(restoreState: RestoreState) {
        _restoreState.value = _restoreState.value.copy(
            databaseChecked = restoreState.databaseChecked,
            settingsChecked = restoreState.settingsChecked,
        )
    }

    private fun updateSuggestedFilename() {
        val databaseChecked = _backupState.value.databaseChecked
        val settingsChecked = _backupState.value.settingsChecked
        val shortDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
        val pattern: String = if (shortDate is SimpleDateFormat) { shortDate.toPattern() } else { "M/d/yy" }
        val modifiedDate = pattern.replace(Regex("[/.]"), "-")
        val dateFormatted = SimpleDateFormat(modifiedDate, Locale.getDefault()).format(Date())
        val baseFilename = "TC_"

        val filename = when {
            databaseChecked && settingsChecked -> "${baseFilename}complete_$dateFormatted.tcbu"
            databaseChecked -> "${baseFilename}db_$dateFormatted.tcbu"
            settingsChecked -> "${baseFilename}settings_$dateFormatted.tcbu"
            else -> ""
        }

        _backupState.value = _backupState.value.copy(suggestedFilename = filename)
    }

    private suspend fun createSettingsBytes(): ByteArray {
        val settingsText = createSettingsText(preferencesRepo)
        return settingsText.toByteArray(Charset.forName("UTF-8"))
    }

    fun createBackupBinary(uri: Uri, context: Context) {
        viewModelScope.launch {
            setLoadingState(true)
            var message = ""

            val tempDbZip = context.contentResolver.openFileDescriptor(uri, "w")?.use {
                File(context.cacheDir, "temp_db_backup.zip")
            } ?: throw IOException("Could not open file descriptor for backup")

            try {
                if (backupState.value.databaseChecked) { backupDatabase(context, tempDbZip) }

                val databaseBytes = if (backupState.value.databaseChecked) { tempDbZip.readBytes() } else { ByteArray(0) }
                val databaseLengthBytes = databaseBytes.size.toByteArray()

                val combinedDatabaseBytes = ByteArray(databaseLengthBytes.size + databaseBytes.size)

                databaseLengthBytes.copyInto(combinedDatabaseBytes)
                databaseBytes.copyInto(combinedDatabaseBytes, databaseLengthBytes.size)

                val settingsBytes = if (backupState.value.settingsChecked) createSettingsBytes() else ByteArray(0)

                val magicNumber = byteArrayOf(0x54, 0x43, 0x42, 0x55) // "TCBU"
                val version = byteArrayOf(0x04) // Version 3 item sync state now in database, version 4 settings backup data class change
                val settingsLength = settingsBytes.size.toByteArray()

                val header = magicNumber + version + settingsLength

                val combinedBytes = ByteArray(header.size + combinedDatabaseBytes.size + settingsBytes.size)

                header.copyInto(combinedBytes)
                combinedDatabaseBytes.copyInto(combinedBytes, header.size)
                settingsBytes.copyInto(combinedBytes, header.size + combinedDatabaseBytes.size)

                writeBytesToFile(uri, combinedBytes, context)
                message = "Backup complete."
            } catch (_: Exception) {
                try {
                    DocumentsContract.deleteDocument(context.contentResolver, uri)
                } catch (_: Exception) { }
                message = "Backup failed."
            } finally {
                deleteTempFile(tempDbZip)
                setLoadingState(false)
                showSnackbar(message)
            }
        }
    }

    fun saveBackup(context: Context, uri: Uri) {
        createBackupBinary(uri, context)
    }

    private fun deleteTempFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    // Restore //
    fun restoreBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            setLoadingState(true)

            val workManager = WorkManager.getInstance(context)
            SyncStateManager.loggingPaused = true
            SyncStateManager.schedulingPaused = true

            var message = ""

            val bytes = readBytesFromFile(uri, context)

            if (bytes == null) {
                message = "Invalid file."
            } else try {
                val fileContentState = validateBackupFile(bytes)
                val restoreState = _restoreState.value
                val (databaseBytes, itemSyncStateBytes, settingsBytes) = parseBackup(bytes)

                if (!fileContentState.magicNumberValid) {
                    message = "Restore failed: file is invalid."
                }
                else if (!fileContentState.versionValid) {
                    message = "Restore failed: file is for an unsupported version."
                }
                else if (!fileContentState.databasePresent && !fileContentState.settingsPresent) {
                    message = "Restore failed: file does not contain database or settings data."
                } else {
                    if (restoreState.databaseChecked && restoreState.settingsChecked) {
                        if (fileContentState.databasePresent && fileContentState.settingsPresent) {
                            try {
                                restoreDatabase(context, databaseBytes)
                                if (fileContentState.version == 2) {
                                    restoreItemSyncState(itemSyncStateBytes)
                                }
                                restoreSettings(settingsBytes, fileContentState.version)
                                updateTinSync(runSilent = true)
                                message = "Database and Settings restored."
                            } catch (_: Exception) {
                                message = "Restore failed."
                            }
                        } else {
                            if (!fileContentState.databasePresent) {
                                try {
                                    restoreSettings(settingsBytes, fileContentState.version)
                                    updateTinSync(runSilent = true)
                                    message = "File missing database data, settings restored."
                                } catch (_: Exception) {
                                    message = "Restore failed."
                                }
                            } else {
                                try {
                                    restoreDatabase(context, databaseBytes)
                                    if (fileContentState.version == 2) {
                                        restoreItemSyncState(itemSyncStateBytes)
                                    }
                                    updateTinSync(runSilent = true)
                                    message = "File missing settings data, database restored."
                                } catch (_: Exception) {
                                    message = "Restore failed."
                                }
                            }
                        }
                    }
                    else if (restoreState.databaseChecked) {
                        if (fileContentState.databasePresent) {
                            try {
                                restoreDatabase(context, databaseBytes)
                                if (fileContentState.version == 2) {
                                    restoreItemSyncState(itemSyncStateBytes)
                                }
                                message = "Database restored."
                            } catch (_: Exception) {
                                message = "Error restoring database."
                            }
                        } else { message = "Backup file does not contain database data." }
                    }
                    else if (restoreState.settingsChecked) {
                        if (fileContentState.settingsPresent) {
                            restoreSettings(settingsBytes, fileContentState.version)
                            updateTinSync(runSilent = true)
                            message = "Settings restored."
                        } else {
                            message = "Backup file does not contain settings data."
                        }
                    }
                }
            } finally {
                SyncStateManager.loggingPaused = false
                SyncStateManager.schedulingPaused = false
            }

            if (crossDeviceSync.value) {
                workManager.enqueue(OneTimeWorkRequestBuilder<DownloadSyncWorker>().build())
            }

            setLoadingState(false)
            showSnackbar(message)
        }
    }

    private fun validateBackupFile(bytes: ByteArray): FileContentState {
        val magicNumber = bytes.copyOfRange(0, 4)
        val isMagicNumberValid = magicNumber.contentEquals(byteArrayOf(0x54, 0x43, 0x42, 0x55))
        if (!isMagicNumberValid) return FileContentState(magicNumberValid = false, versionValid = false)

        val version = bytes[4]
        val isVersionValid = version.toInt() in 2..4
        if (!isVersionValid) return FileContentState(magicNumberValid = true, versionValid = false)

        val settingsLengthBytes = bytes.copyOfRange(5, 9)
        val settingsLength = byteArrayToInt(settingsLengthBytes)
        val settingsBytes = bytes.copyOfRange(bytes.size - settingsLength, bytes.size)
        val databaseBytes = bytes.copyOfRange(9, bytes.size - settingsLength)

        return FileContentState(
            databaseBytes.isNotEmpty(),
            settingsBytes.isNotEmpty(),
            versionValid = true,
            magicNumberValid = true,
            version = version.toInt()
        )
    }

    private fun parseBackup(bytes: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        val settingsLengthBytes = bytes.copyOfRange(5, 9)
        val settingsLength = byteArrayToInt(settingsLengthBytes)
        val settingsBytes = bytes.copyOfRange(bytes.size - settingsLength, bytes.size)

        val databaseAndSyncStateBytes = if (bytes.size > 9 + settingsLength)
            { bytes.copyOfRange(9, bytes.size - settingsLength) } else { ByteArray(0) }

        val databaseLengthBytes = if (databaseAndSyncStateBytes.size >= 4) {
            databaseAndSyncStateBytes.copyOfRange(0, 4)
        } else { byteArrayOf(0, 0, 0, 0) }
        val databaseLength = byteArrayToInt(databaseLengthBytes)

        val databaseBytes = if (databaseLength > 0 && databaseAndSyncStateBytes.size >= 4 + databaseLength) {
            databaseAndSyncStateBytes.copyOfRange(4, 4 + databaseLength)
        } else { ByteArray(0) }
        val itemSyncStateBytes = if (databaseLength > 0 && databaseAndSyncStateBytes.size >= 4 + databaseLength) {
            databaseAndSyncStateBytes.copyOfRange(4 + databaseLength, databaseAndSyncStateBytes.size)
        } else { ByteArray(0) }

        return Triple(databaseBytes, itemSyncStateBytes, settingsBytes)
    }

    suspend fun restoreDatabase(context: Context, databaseBytes: ByteArray) {
        val dbPath = getDatabaseFilePath(context)
        val dbFile = File(dbPath)
        val walFile = File("$dbPath-wal")
        val shmFile = File("$dbPath-shm")

        val backupDbFile = File("$dbPath.bak")
        val backupWalFile = File("$dbPath-wal.bak")
        val backupShmFile = File("$dbPath-shm.bak")

        val tempDir = File(context.filesDir, "temp_db_restore_dir")
        if (!tempDir.exists()) { tempDir.mkdirs() }
        var tempMigrationDir: File? = null

        val tempZipFile = File(tempDir, "temp_db_restore.zip")

        val existingDbVersion = TobaccoDatabase.getDatabaseVersion(context)
        val backupDbVersion = getBackupDbVersion(context, databaseBytes)

        try {
            copyFile(dbFile, backupDbFile)
            if (walFile.exists()) { copyFile(walFile, backupWalFile) }
            if (shmFile.exists()) { copyFile(shmFile, backupShmFile) }

            if (backupDbVersion == existingDbVersion) {
                tempZipFile.writeBytes(databaseBytes)
                unzipFile(tempZipFile, tempDir)

                copyFile(File(tempDir, "tobacco_database"), dbFile)
                if (File(tempDir, "tobacco_database-wal").exists()) { copyFile(File(tempDir, "tobacco_database-wal"), walFile) }
                if (File(tempDir, "tobacco_database-shm").exists()) { copyFile(File(tempDir, "tobacco_database-shm"), shmFile) }

            } else if (backupDbVersion < existingDbVersion) {
                tempMigrationDir = File(context.cacheDir, "temp_migration_dir")
                tempMigrationDir.mkdirs()

                tempZipFile.writeBytes(databaseBytes)
                unzipFile(tempZipFile, tempMigrationDir)

                val unzippedDb = File(tempMigrationDir, "tobacco_database")
                val migratedDb = buildAndMigrateDb(context, unzippedDb)

                copyMigratedDb(migratedDb, dbFile, walFile, shmFile)
                migratedDb.close()

            } else {
                throw Exception("Backup database version is invalid.")
            }

        } catch (e: Exception) {
            copyFile(backupDbFile, dbFile)
            if (File("$dbPath-wal.bak").exists()) { copyFile(File("$dbPath-wal.bak"), walFile) }
            if (File("$dbPath-shm.bak").exists()) { copyFile(File("$dbPath-shm.bak"), shmFile) }
            throw e
        } finally {
            backupDbFile.delete()
            backupWalFile.delete()
            backupShmFile.delete()
            tempDir.deleteRecursively()
            tempMigrationDir?.deleteRecursively()

            EventBus.emit(DatabaseRestoreEvent)
        }
    }

    private fun buildAndMigrateDb(context: Context, unzippedDb: File): RoomDatabase {
        return try {
            Room.databaseBuilder(
                context.applicationContext,
                TobaccoDatabase::class.java,
                unzippedDb.absolutePath
            )
                .createFromFile(unzippedDb)
                .addMigrations(*allMigrations)
                .build()
                .apply { openHelper.writableDatabase }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun copyMigratedDb(migratedDb: RoomDatabase, dbFile: File, walFile: File, shmFile: File) {
        val migratedPath = migratedDb.openHelper.readableDatabase.path
        val migratedWalFile = File("$migratedPath-wal")
        val migratedShmFile = File("$migratedPath-shm")

        try {
            copyFile(File(migratedPath!!), dbFile)
            if (migratedWalFile.exists()) { copyFile(migratedWalFile, walFile) }
            if (migratedShmFile.exists()) { copyFile(migratedShmFile, shmFile) }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun restoreItemSyncState(itemSyncStateBytes: ByteArray) {
        val syncStateString = String(itemSyncStateBytes, Charset.forName("UTF-8"))
        val lines = syncStateString.lines()

        val itemsToUpdate = mutableListOf<Items>()

        for (line in lines) {
            if (line.isBlank()) continue
            val parts = line.split(":")
            val itemId = parts.getOrNull(0)?.toIntOrNull()
            val isSynced = parts.getOrNull(1)?.toBoolean()

            if (itemId != null && isSynced != null) {
                val item = itemsRepository.getItemById(itemId)
                if (item != null) {
                    itemsToUpdate.add(item.copy(syncTins = isSynced))
                }
            }
        }

        for (item in itemsToUpdate) {
            itemsRepository.updateItem(item.copy(lastModified = System.currentTimeMillis()))
        }
    }

    private fun restoreSettings(settingsBytes: ByteArray, backupVersion: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val settingsText = String(settingsBytes, Charset.forName("UTF-8"))
            parseSettingsText(settingsText, preferencesRepo, backupVersion)
        }
    }

    fun getBackupDbVersion(context: Context, databaseBytes: ByteArray): Int {
        val tempDir = File(context.cacheDir, "temp_db_restore_dir")
        tempDir.mkdirs()
        val tempZipFile = File(tempDir, "temp_db_restore.zip")
        val dbFile = File(tempDir, "tobacco_database")

        return try {
            tempZipFile.writeBytes(databaseBytes)
            unzipFile(tempZipFile, tempDir)

            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.rawQuery("PRAGMA user_version", null)
            cursor.moveToFirst()
            val version = cursor.getInt(0)
            cursor.close()
            db.close()

            version

        } finally {
            tempDir.deleteRecursively()
        }
    }


}

@Stable
data class SettingsDialog(
    val title: String,
    val description: String,
    val currentSetting: String?,
    val dialogType: DialogType
)

@Stable
sealed class DialogType {
    object Theme : DialogType()
    object Ratings : DialogType()
    object TypeGenre : DialogType()
    object QuantityDisplay : DialogType()
    object ParseLinks : DialogType()
    object GlobalTwoPane: DialogType()

    object DeviceSync : DialogType()
    object BackupRestore: DialogType()
    object TinRates : DialogType()
    object TinSyncDefault : DialogType()
    object DbOperations : DialogType()
    object DeleteAll : DialogType()
}

enum class ThemeSetting(val value: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

enum class QuantityOption(val value: String) {
    TINS("\"No. of Tins\" (default)"),
    OUNCES("Ounces/Pounds"),
    GRAMS("Grams")
}

enum class TypeGenreOption(val value: String) {
    TYPE("Type"),
    SUBGENRE("Subgenre"),
    BOTH("Both"),
    TYPE_FALLBACK("Type (fallback)"),
    SUB_FALLBACK("Subgenre (fallback)")
}

data class TinConversionRates(
    val ozRate: Double,
    val gramsRate: Double
) {
    companion object {
        val DEFAULT = TinConversionRates(1.75, 50.0)
    }
}

@Serializable
data class ExportRating(
    val maxRating: Int = 5,
    val rounding: Int = 2
)

data class BackupState(
    val databaseChecked: Boolean = false,
    val settingsChecked: Boolean = false,
    val suggestedFilename: String = "",
)

@Serializable
data class SettingsBackup (
    val tableView: Boolean = false,
    val tableColumnsHidden: Set<String> = emptySet(),
    val quantityOption: String = QuantityOption.TINS.value,
    val themeSetting: String = ThemeSetting.SYSTEM.value,
    val tinOzConversionRate: Double = 1.75,
    val tinGramsConversionRate: Double = 50.0,
    val plaintextFormatString: String = "",
    val plaintextDelimiter: String = "",
    val plaintextPresets: List<PlaintextPreset> = emptyList(),
    val plaintextPrintFontSize: Float = 12f,
    val plaintextPrintMargin: Double = 1.0,
    val showRatingOption: Boolean = true,
    val typeGenreOption: String = TypeGenreOption.TYPE.value,
    val exportRating: ExportRating = ExportRating(),
    val defaultSyncTinsOption: Boolean = false,
    val columnVisibility: Set<String> = emptySet(),
    val parseLinksOption: Boolean = true,
    val syncAcknowledgement: Boolean = false,
    val processedSync: Set<String> = emptySet(),
    val datesLastSeen: String = "",
    val globalTwoPane: Boolean = true,
    val twoColumnTabs: Boolean = true,
    val landscapeTwoPane: Boolean = false
)

data class RestoreState(
    val databaseChecked: Boolean = false,
    val settingsChecked: Boolean = false,
)

data class FileContentState(
    val databasePresent: Boolean = false,
    val settingsPresent: Boolean = false,
    val versionValid: Boolean = false,
    val magicNumberValid: Boolean = false,
    val version: Int = 0
)

data class SnackbarState(
    val show: Boolean = false,
    val message: String = ""
)

data object DatabaseRestoreEvent
data object SyncDownloadEvent
data object SignInEvent
data object SignInCancelled
data object SignOutEvent


/** Extension functions */
// Create backup //
fun Int.toByteArray(): ByteArray {
    return byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}

fun byteArrayToInt(bytes: ByteArray): Int {
    require(bytes.size == 4) { "ByteArray must be of size 4" }
    return (bytes[0].toInt() shl 24) or
            (bytes[1].toInt() and 0xFF shl 16) or
            (bytes[2].toInt() and 0xFF shl 8) or
            (bytes[3].toInt() and 0xFF)
}

@Throws(IOException::class)
fun copyFile(src: File, dst: File) {
    FileInputStream(src).use { `in` ->
        FileOutputStream(dst).use { out ->
            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            var totalBytesRead = 0
            var totalBytesWritten = 0
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
                totalBytesRead += len
                totalBytesWritten += len
            }
        }
    }
}

fun zipFiles(files: List<File>, zipFile: File) {
    ZipOutputStream(FileOutputStream(zipFile)).use { outStream ->
        files.forEach { file ->
            FileInputStream(file).use { inStream ->
                val zipEntry = ZipEntry(file.name)
                outStream.putNextEntry(zipEntry)
                inStream.copyTo(outStream)
                outStream.closeEntry()
            }
        }
    }
}

fun unzipFile(zipFile: File, destinationDir: File) {
    if (!destinationDir.exists()) {
        destinationDir.mkdirs()
    }
    ZipInputStream(FileInputStream(zipFile)).use { zipStream ->
        var zipEntry = zipStream.nextEntry
        while (zipEntry != null) {
            val newFile = File(destinationDir, zipEntry.name)
            FileOutputStream(newFile).use { outStream ->
                zipStream.copyTo(outStream)
            }
            zipStream.closeEntry()
            zipEntry = zipStream.nextEntry
        }
    }
}

fun getDatabaseFilePath(context: Context): String {
    val dbPath: String? = TobaccoDatabase.getDatabase(context).openHelper.writableDatabase.path
    return dbPath ?: ""
}

fun backupDatabase(context: Context, backupFile: File) {
    try {
        val db = TobaccoDatabase.getDatabase(context)
        val dbPath = getDatabaseFilePath(context)
        val dbFile = File(dbPath)
        val walFile = File("$dbPath-wal")
        val shmFile = File("$dbPath-shm")

        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")

        val dbFiles = listOf(dbFile, walFile, shmFile)

        zipFiles(dbFiles, backupFile)

    } catch (e: Exception) {
        throw e
    }
}

suspend fun createSettingsText(preferencesRepo: PreferencesRepo): String {
    val backup = SettingsBackup(
        tableView = preferencesRepo.isTableView.first(),
        tableColumnsHidden = preferencesRepo.tableColumnsHidden.first(),
        quantityOption = preferencesRepo.quantityOption.first().value,
        themeSetting = preferencesRepo.themeSetting.first().value,
        tinOzConversionRate = preferencesRepo.tinOzConversionRate.first(),
        tinGramsConversionRate = preferencesRepo.tinGramsConversionRate.first(),
        plaintextFormatString = preferencesRepo.plaintextFormatString.first(),
        plaintextDelimiter = preferencesRepo.plaintextDelimiter.first(),
        plaintextPresets = preferencesRepo.plaintextPresetsFlow.first(),
        plaintextPrintFontSize = preferencesRepo.plaintextPrintFontSize.first(),
        plaintextPrintMargin = preferencesRepo.plaintextPrintMargin.first(),
        showRatingOption = preferencesRepo.showRating.first(),
        typeGenreOption = preferencesRepo.typeGenreOption.first().value,
        exportRating = preferencesRepo.exportRating.first(),
        defaultSyncTinsOption = preferencesRepo.defaultSyncOption.first(),
        columnVisibility = preferencesRepo.tableColumnsHidden.first(),
        parseLinksOption = preferencesRepo.parseLinks.first(),
        syncAcknowledgement = preferencesRepo.crossDeviceAcknowledged.first(),
        processedSync = preferencesRepo.processedSyncFiles.first(),
        datesLastSeen = preferencesRepo.datesSeen.first(),
        globalTwoPane = preferencesRepo.globalTwoPane.first(),
        twoColumnTabs = preferencesRepo.twoColumnTabs.first(),
        landscapeTwoPane = preferencesRepo.landscapeTwoPane.first()
    )

    return Json.encodeToString(backup)
}

fun writeBytesToFile(uri: Uri, bytes: ByteArray, context: Context) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(bytes)
        }
    } catch (_: Exception) { }
}

// Restore backup //
fun readBytesFromFile(uri: Uri, context: Context): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (_: IOException) {
        null
    }
}

suspend fun parseSettingsText(settingsText: String, preferencesRepo: PreferencesRepo, version: Int) {
    if (version >= 4) {
        val backup = Json.decodeFromString<SettingsBackup>(settingsText)
        with(preferencesRepo) {
            saveViewPreference(backup.tableView)
            saveTableColumnsHidden(backup.tableColumnsHidden)
            saveQuantityPreference(backup.quantityOption)
            saveThemeSetting(backup.themeSetting)
            setTinOzConversionRate(backup.tinOzConversionRate)
            setTinGramsConversionRate(backup.tinGramsConversionRate)
            setPlaintextFormatString(backup.plaintextFormatString)
            setPlaintextDelimiter(backup.plaintextDelimiter)
            backup.plaintextPresets.forEach {
                preferencesRepo.savePlaintextPreset(it.slot, it.formatString, it.delimiter)
            }
            setPlaintextPrintOptions(backup.plaintextPrintFontSize, backup.plaintextPrintMargin)
            saveShowRatingOption(backup.showRatingOption)
            saveTypeGenreOption(backup.typeGenreOption)
            saveExportRating(backup.exportRating.maxRating, backup.exportRating.rounding)
            saveDefaultSyncOption(backup.defaultSyncTinsOption)
            saveTableColumnsHidden(backup.columnVisibility)
            saveParseLinksOption(backup.parseLinksOption)
            saveCrossDeviceAcknowledged(backup.syncAcknowledgement)
            saveProcessedSyncFiles(backup.processedSync)
            setDatesSeen(backup.datesLastSeen)
            saveGlobalTwoPane(backup.globalTwoPane)
            saveTwoColumnTabs(backup.twoColumnTabs)
            saveLandscapeTwoPane(backup.landscapeTwoPane)
        }
    } else {
        val lines = settingsText.lines()
        for (line in lines) {
            val parts = line.split("=")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()

                when (key) {
                    "tableView" -> preferencesRepo.saveViewPreference(value.toBoolean())
                    "tableColumnsHidden" -> preferencesRepo.saveTableColumnsHidden(
                        Json.decodeFromString<Set<String>>(value)
                    )
                    "quantityOption" -> preferencesRepo.saveQuantityPreference(value)
                    "themeSetting" -> preferencesRepo.saveThemeSetting(value)
                    "tinOzConversionRate" -> preferencesRepo.setTinOzConversionRate(value.toDouble())
                    "tinGramsConversionRate" -> preferencesRepo.setTinGramsConversionRate(value.toDouble())
                    "plaintextFormatString" -> preferencesRepo.setPlaintextFormatString(value)
                    "plaintextDelimiter" -> preferencesRepo.setPlaintextDelimiter(value)
                    "plaintextPresets" -> {
                        val presets = Json.decodeFromString<List<PlaintextPreset>>(value)
                        presets.forEach {
                            preferencesRepo.savePlaintextPreset(
                                it.slot,
                                it.formatString,
                                it.delimiter
                            )
                        }
                    }
                    "plaintextPrintOptions" -> {
                        val options = value.split(", ")
                        val font = options.first().toFloat()
                        val margin = options.last().toDouble()
                        preferencesRepo.setPlaintextPrintOptions(font, margin)
                    }
                    "showRatingOption" -> preferencesRepo.saveShowRatingOption(value.toBoolean())
                    "typeGenreOption" -> preferencesRepo.saveTypeGenreOption(value)
                    "exportRating" -> {
                        val options = Json.decodeFromString<ExportRating>(value)
                        preferencesRepo.saveExportRating(options.maxRating, options.rounding)
                    }
                    "defaultSyncTinsOption" -> preferencesRepo.saveDefaultSyncOption(value.toBoolean())
                    "columnVisibility" -> {
                        val columns = value.split(", ").toSet()
                        preferencesRepo.saveTableColumnsHidden(columns)
                    }
                    "parseLinksOption" -> preferencesRepo.saveParseLinksOption(value.toBoolean())
                    "syncAcknowledgement" -> preferencesRepo.saveCrossDeviceAcknowledged(value.toBoolean())
                    "processedSync" -> {
                        val files = value.split(", ").toSet()
                        preferencesRepo.saveProcessedSyncFiles(files)
                    }
                    "datesLastSeen" -> preferencesRepo.setDatesSeen(value)
                    "globalTwoPane" -> preferencesRepo.saveGlobalTwoPane(value.toBoolean())
                    "twoColumnTabs" -> preferencesRepo.saveTwoColumnTabs(value.toBoolean())
                    "landscapeTwoPane" -> preferencesRepo.saveLandscapeTwoPane(value.toBoolean())
                }
            }
        }
    }
}

fun exportRatingString(rating: Double?, maxRating: Int, rounding: Int): String {
    val scaling = maxRating / 5.0

    if (rating == null) { return "" }

    val scaledRating = (rating * scaling)
    val places = rounding.takeIf { it <= 2 } ?: 2

    return formatDecimal(scaledRating, places)
}