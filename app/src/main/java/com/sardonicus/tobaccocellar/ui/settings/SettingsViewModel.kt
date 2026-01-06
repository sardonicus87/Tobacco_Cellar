package com.sardonicus.tobaccocellar.ui.settings

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.MIGRATION_1_2
import com.sardonicus.tobaccocellar.data.MIGRATION_2_3
import com.sardonicus.tobaccocellar.data.MIGRATION_3_4
import com.sardonicus.tobaccocellar.data.MIGRATION_4_5
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.data.multiDeviceSync.DownloadSyncWorker
import com.sardonicus.tobaccocellar.data.multiDeviceSync.GoogleDriveServiceHelper
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextPreset
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.NetworkMonitor
import com.sardonicus.tobaccocellar.ui.utilities.SignInRequestedEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

class SettingsViewModel(
    application: Application,
    private val itemsRepository: ItemsRepository,
    val filterViewModel: FilterViewModel,
    val preferencesRepo: PreferencesRepo,
): AndroidViewModel(application) {

    /** Display Settings */
    private val _themeSetting = MutableStateFlow(ThemeSetting.SYSTEM)
    val themeSetting: StateFlow<ThemeSetting> = _themeSetting.asStateFlow()

    private val _showRatings = MutableStateFlow(false)
    val showRatings: StateFlow<Boolean> = _showRatings.asStateFlow()

    private val _typeGenreOption = MutableStateFlow(TypeGenreOption.TYPE)
    val typeGenreOption: StateFlow<TypeGenreOption> = _typeGenreOption.asStateFlow()

    private val _quantityOption = MutableStateFlow(QuantityOption.TINS)
    val quantityOption: StateFlow<QuantityOption> = _quantityOption.asStateFlow()

    private val _parseLinks = MutableStateFlow(true)
    val parseLinks: StateFlow<Boolean> = _parseLinks.asStateFlow()


    /** App & Database settings */
    private val _deviceSyncAcknowledgement = MutableStateFlow(false)
    val deviceSyncAcknowledgement = _deviceSyncAcknowledgement.asStateFlow()

    private val _crossDeviceSync = MutableStateFlow(false)
    val crossDeviceSync = _crossDeviceSync.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail = _userEmail.asStateFlow()

    private val _hasScope = MutableStateFlow(false)
    val hasScope = _hasScope.asStateFlow()

    private val _allowMobileData = MutableStateFlow(false)
    val allowMobileData = _allowMobileData.asStateFlow()

    private val _tinOzConversionRate = MutableStateFlow(TinConversionRates.DEFAULT.ozRate)
    val tinOzConversionRate: StateFlow<Double> = _tinOzConversionRate.asStateFlow()

    private val _defaultSyncOption = MutableStateFlow(false)
    val defaultSyncOption = _defaultSyncOption.asStateFlow()


    /** General UI control **/
    private val _openDialog = MutableStateFlow<DialogType?>(null)
    val openDialog: StateFlow<DialogType?> = _openDialog.asStateFlow()

    private val _tinGramsConversionRate = MutableStateFlow(TinConversionRates.DEFAULT.gramsRate)
    val tinGramsConversionRate: StateFlow<Double> = _tinGramsConversionRate.asStateFlow()

    private val _snackbarState = MutableStateFlow(SnackbarState())
    val snackbarState: StateFlow<SnackbarState> = _snackbarState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val networkMonitor = NetworkMonitor(getApplication())


    val displaySettings = listOf(
        SettingsDialog("Theme", "Change the theme of the app", DialogType.Theme),
        SettingsDialog("Cellar Ratings Visibility", "Show/hide ratings in list", DialogType.Ratings),
        SettingsDialog("Cellar Type/Genre Display", "Set type/genre display in list", DialogType.TypeGenre),
        SettingsDialog("Cellar Quantity Display", "Change quantity display in list", DialogType.QuantityDisplay),
        SettingsDialog("Parse Links in Notes", "Enable/disable link parsing in notes", DialogType.ParseLinks)
    )

    val databaseSettings = listOf(
        SettingsDialog("Multi-Device Sync", "Enable/disable cross-device sync", DialogType.DeviceSync),
        SettingsDialog("Tin Conversion Rates", "Change tin conversion rates", DialogType.TinRates),
        SettingsDialog("Default Sync Tins Option", "Set default tin sync option", DialogType.TinSyncDefault),
        SettingsDialog("Database Operations", "Fix sync quantities and optimize database", DialogType.DbOperations),
        SettingsDialog("Backup/Restore", "Backup or restore database/settings", DialogType.BackupRestore),
        SettingsDialog("Delete Database", "Delete all items", DialogType.DeleteAll)
    )


    fun showDialog(dialog: DialogType) {
        _openDialog.value = dialog
    }

    fun dismissDialog() { _openDialog.value = null }

    fun showSnackbar(message: String) { _snackbarState.value = SnackbarState(true, message) }

    fun snackbarShown() { _snackbarState.value = SnackbarState(false, "") }

    fun setLoadingState(loading: Boolean) { _loading.value = loading }

    // option initializations //
    init {
        // Theme Setting
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.themeSetting.collect {
                    _themeSetting.value = it
                }
            }
        }
        // Ratings Visibility
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.showRating.collect {
                    _showRatings.value = it
                }
            }
        }
        // Type/Genre Display
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.typeGenreOption.collect {
                    _typeGenreOption.value = it
                }
            }
        }
        // Quantity Display
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.quantityOption.collect {
                    _quantityOption.value = it
                }
            }
        }
        //Parse Links
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.parseLinks.collect {
                    _parseLinks.value = it
                }
            }
        }

        // Device Sync
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.crossDeviceAcknowledged.collect {
                    _deviceSyncAcknowledgement.value = it
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.crossDeviceSync.collect {
                    _crossDeviceSync.value = it
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.signedInUserEmail.collect {
                    _userEmail.value = it
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.hasDriveScope.collect {
                    _hasScope.value = it
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.allowMobileData.collect {
                    _allowMobileData.value = it
                }
            }
        }
        // Tin Conversion Rates
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.tinOzConversionRate.collect {
                    _tinOzConversionRate.value = it
                }
            }
            withContext(Dispatchers.IO) {
                preferencesRepo.tinGramsConversionRate.collect {
                    _tinGramsConversionRate.value = it
                }
            }
        }
        // Default sync tins
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.defaultSyncOption.collect {
                    _defaultSyncOption.value = it
                }
            }
        }

    }


    /** Display Settings **/
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = mapOf(
            TypeGenreOption.TYPE to true,
            TypeGenreOption.SUBGENRE to false,
            TypeGenreOption.BOTH to false,
            TypeGenreOption.TYPE_FALLBACK to false,
            TypeGenreOption.SUB_FALLBACK to false,
        )
    )

    init {
        viewModelScope.launch {
            combine(
                preferencesRepo.typeGenreOption,
                typeGenreOptionEnablement
            ) { option, typeGenreEnablement ->
                val enabled = typeGenreEnablement[option] ?: false
                if (enabled) option else TypeGenreOption.TYPE
            }.collect {
                _typeGenreOption.value = it
                preferencesRepo.saveTypeGenreOption(it.value)
            }
        }
    }

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


    /** Database Settings **/
    fun saveCrossDeviceAcknowledged() {
        viewModelScope.launch {
            preferencesRepo.saveCrossDeviceAcknowledged(true)
        }
    }

    fun saveCrossDeviceSync(enable: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveCrossDeviceSync(enable)
            if (enable) {
                EventBus.emit(SignInRequestedEvent())
            } else {
                stopWorkers()
            }
        }
    }

    fun saveAllowMobileData(enable: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveAllowMobileData(enable)
        }
    }

    val manualSyncEnabled: StateFlow<Boolean> = combine(
        preferencesRepo.allowMobileData,
        networkMonitor.isConnected,
        networkMonitor.isWifi
    ) { allowMobile, isConnected, isWifi ->
        (allowMobile && isConnected) || isWifi
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun manualSync() {
        viewModelScope.launch {
            preferencesRepo.signedInUserEmail.first() ?: return@launch

            val context: Context = getApplication()
            val workManager = WorkManager.getInstance(context)

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
                delay(5000)
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
                                    EventBus.emit(SyncDownloadEvent)
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
            setLoadingState(true)

            withContext(Dispatchers.IO) {
                val email = preferencesRepo.signedInUserEmail.first()
                if (email == null) {
                    setLoadingState(false)
                    return@withContext
                }

                try {
                    val context: Context = getApplication()
                    val driveService = GoogleDriveServiceHelper.getDriveService(context, email)

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
                        ) { println("ClearRemoteData, error deleting file in batch: ${e?.message}") }
                    }

                    for (file in files.files) {
                        driveService.files().delete(file.id).queue(batch, callback)
                    }

                    batch.execute()
                    showSnackbar("Remote data deleted.")

                } catch (e: Exception) {
                    println("Exception: $e")
                    showSnackbar("Error deleting remote data.")
                } finally {
                    setLoadingState(false)
                }
            }
        }
    }

    fun clearLoginState() {
        viewModelScope.launch {
            preferencesRepo.clearLoginState()
        }
    }

    fun stopWorkers() {
        viewModelScope.launch {
            preferencesRepo.signedInUserEmail.first() ?: return@launch

            val context: Context = getApplication()
            val workManager = WorkManager.getInstance(context)

            workManager.cancelUniqueWork(("download_sync_work"))
        }
    }

    fun setTinConversionRates(ozRate: Double, gramsRate: Double) {
        viewModelScope.launch {
            preferencesRepo.setTinOzConversionRate(ozRate)
            preferencesRepo.setTinGramsConversionRate(gramsRate)
            _tinOzConversionRate.value = ozRate
            _tinGramsConversionRate.value = gramsRate

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
            } catch (e: Exception) {
                println("Update Tins Sync Exception: $e")
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

    suspend fun deleteAllItems() {
        itemsRepository.deleteAllItems()
        saveTypeGenreOption(TypeGenreOption.TYPE.value)
        showSnackbar("Database deleted!")
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
                if (backupState.value.databaseChecked) {
                    backupDatabase(context, tempDbZip)
                }

                val databaseBytes = if (backupState.value.databaseChecked) { tempDbZip.readBytes() } else { ByteArray(0) }
                val databaseLengthBytes = databaseBytes.size.toByteArray()

                val combinedDatabaseBytes = ByteArray(databaseLengthBytes.size + databaseBytes.size)

                databaseLengthBytes.copyInto(combinedDatabaseBytes)
                databaseBytes.copyInto(combinedDatabaseBytes, databaseLengthBytes.size)

                val settingsBytes = if (backupState.value.settingsChecked) createSettingsBytes() else ByteArray(0)

                val magicNumber = byteArrayOf(0x54, 0x43, 0x42, 0x55) // "TCBU"
                val version = byteArrayOf(0x03) // Version 3 item sync state now in database
                val settingsLength = settingsBytes.size.toByteArray()

                val header = magicNumber + version + settingsLength

                val combinedBytes = ByteArray(header.size + combinedDatabaseBytes.size + settingsBytes.size)

                header.copyInto(combinedBytes)
                combinedDatabaseBytes.copyInto(combinedBytes, header.size)
                settingsBytes.copyInto(combinedBytes, header.size + combinedDatabaseBytes.size)

                writeBytesToFile(uri, combinedBytes, context)
                message = "Backup complete."
            } catch (e: Exception) {
                println("Exception: $e")
                try {
                    val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
                    if (deleted) {
                        println("$uri deleted successfully.")
                    } else {
                        println("Failed to delete $uri.")
                    }
                } catch (e: Exception) {
                    println("Exception: $e")
                }
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
        viewModelScope.launch {
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
                                restoreSettings(settingsBytes)
                                restoreDatabase(context, databaseBytes)
                                if (fileContentState.version == 2) {
                                    restoreItemSyncState(itemSyncStateBytes)
                                }
                                updateTinSync(runSilent = true)
                                message = "Database and Settings restored."
                            } catch (e: Exception) {
                                println("Exception: $e")
                                message = "Restore failed. Error: ${e.message}."
                            }
                        } else {
                            if (!fileContentState.databasePresent) {
                                try {
                                    restoreSettings(settingsBytes)
                                    updateTinSync(runSilent = true)
                                    message = "File missing database data, settings restored."
                                } catch (e: Exception) {
                                    println("Exception: $e")
                                    message = "Restore failed."
                                }
                            } else {
                                try {
                                    restoreDatabase(context, databaseBytes)
                                    if (fileContentState.version == 2) {
                                        restoreItemSyncState(itemSyncStateBytes)
                                    }
                                    message = "File missing settings data, database restored."
                                } catch (e: Exception) {
                                    println("Exception: $e")
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
                            } catch (e: Exception) {
                                println("Exception: $e")
                                message = "Error restoring database."
                            }
                        } else { message = "Backup file does not contain database data." }
                    }
                    else if (restoreState.settingsChecked) {
                        if (fileContentState.settingsPresent) {
                            restoreSettings(settingsBytes)
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

            workManager.enqueue(OneTimeWorkRequestBuilder<DownloadSyncWorker>().build())

            setLoadingState(false)
            showSnackbar(message)
        }
    }

    private fun validateBackupFile(bytes: ByteArray): FileContentState {
        val magicNumber = bytes.copyOfRange(0, 4)
        val isMagicNumberValid = magicNumber.contentEquals(byteArrayOf(0x54, 0x43, 0x42, 0x55))
        if (!isMagicNumberValid) {
            return FileContentState(magicNumberValid = false, versionValid = false)
        }

        val version = bytes[4]
        val isVersionValid = (version == 0x02.toByte() || version == 0x03.toByte())
        if (!isVersionValid) {
            return FileContentState(magicNumberValid = true, versionValid = false)
        }

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
                val migrations = getMigrations()

                tempZipFile.writeBytes(databaseBytes)
                unzipFile(tempZipFile, tempMigrationDir)

                val unzippedDb = File(tempMigrationDir, "tobacco_database")
                val migratedDb = buildAndMigrateDb(context, unzippedDb, migrations)

                copyMigratedDb(migratedDb, dbFile, walFile, shmFile)
                migratedDb.close()

            } else {
                throw Exception("Backup database version is invalid.")
            }

        } catch (e: Exception) {
            println("Exception: $e")
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

    private fun getMigrations(): List<Migration> {
        val migrations = mutableListOf<Migration>()
        migrations.add(MIGRATION_1_2)
        migrations.add(MIGRATION_2_3)
        migrations.add(MIGRATION_3_4)
        migrations.add(MIGRATION_4_5)
        return migrations
    }

    private fun buildAndMigrateDb(context: Context, unzippedDb: File, migrations: List<Migration>): RoomDatabase {
        return try {
            Room.databaseBuilder(
                context.applicationContext,
                TobaccoDatabase::class.java,
                unzippedDb.absolutePath
            )
                .createFromFile(unzippedDb)
                .addMigrations(*migrations.toTypedArray())
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

    private fun restoreSettings(settingsBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val settingsText = String(settingsBytes, Charset.forName("UTF-8"))
            parseSettingsText(settingsText, preferencesRepo)
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


data class SettingsDialog(
    val title: String,
    val description: String,
    val dialogType: DialogType
)

sealed class DialogType {
    object Theme : DialogType()
    object Ratings : DialogType()
    object TypeGenre : DialogType()
    object QuantityDisplay : DialogType()
    object ParseLinks : DialogType()

    object DeviceSync : DialogType()
    object TinRates : DialogType()
    object TinSyncDefault : DialogType()
    object DbOperations : DialogType()
    object BackupRestore: DialogType()
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
    val tableView = preferencesRepo.isTableView.toString()
    val tableColumnsHidden = Json.encodeToString(preferencesRepo.tableColumnsHidden.first())
    val quantityOption = preferencesRepo.quantityOption.first().value
    val themeSetting = preferencesRepo.themeSetting.first()
    val tinOzConversionRate = preferencesRepo.tinOzConversionRate.first().toString()
    val tinGramsConversionRate = preferencesRepo.tinGramsConversionRate.first().toString()
    val plaintextFormatString = preferencesRepo.plaintextFormatString.first()
    val plaintextDelimiter = preferencesRepo.plaintextDelimiter.first()
    val plaintextPresets = Json.encodeToString(preferencesRepo.plaintextPresetsFlow.first())
    val plaintextPrintOptions = "${preferencesRepo.plaintextPrintFontSize.first()}, ${preferencesRepo.plaintextPrintMargin.first()}"
    val showRatingOption = preferencesRepo.showRating.first().toString()
    val typeGenreOption = preferencesRepo.typeGenreOption.first().value
    val exportRating = Json.encodeToString(preferencesRepo.exportRating.first())
    val defaultSyncTinsOption = preferencesRepo.defaultSyncOption.first().toString()
    val columnVisibility = preferencesRepo.tableColumnsHidden.first().joinToString(", ") { it }
    val parseLinksOption = preferencesRepo.parseLinks.first().toString()
    val syncAcknowledgement = preferencesRepo.crossDeviceAcknowledged.first().toString()
    val processedSync = preferencesRepo.processedSyncFiles.first().joinToString(", ") { it }
    val allowMobileSync = preferencesRepo.allowMobileData.first().toString()

    return """
            tableView=$tableView
            tableColumnsHidden=$tableColumnsHidden
            quantityOption=$quantityOption
            themeSetting=$themeSetting
            tinOzConversionRate=$tinOzConversionRate
            tinGramsConversionRate=$tinGramsConversionRate
            plaintextFormatString=$plaintextFormatString
            plaintextDelimiter=$plaintextDelimiter
            plaintextPresets=$plaintextPresets
            plaintextPrintOptions=$plaintextPrintOptions
            showRatingOption=$showRatingOption
            typeGenreOption=$typeGenreOption
            exportRating=$exportRating
            defaultSyncTinsOption=$defaultSyncTinsOption
            columnVisibility=$columnVisibility
            parseLinksOption=$parseLinksOption
            syncAcknowledgement=$syncAcknowledgement
            processedSync=$processedSync
            allowMobileSync=$allowMobileSync
        """.trimIndent()
}

fun writeBytesToFile(uri: Uri, bytes: ByteArray, context: Context) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(bytes)
        }
    } catch (e: Exception) {
        println("Exception: $e")
    }
}

// Restore backup //
fun readBytesFromFile(uri: Uri, context: Context): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: IOException) {
        println("Exception: $e")
        null
    }
}

suspend fun parseSettingsText(settingsText: String, preferencesRepo: PreferencesRepo) {
    val lines = settingsText.lines()
    for (line in lines) {
        val parts = line.split("=")
        if (parts.size == 2) {
            val key = parts[0].trim()
            val value = parts[1].trim()

            when (key) {
                "tableView" -> preferencesRepo.saveViewPreference(value.toBoolean())
                "tableColumnsHidden" -> preferencesRepo.saveTableColumnsHidden(Json.decodeFromString<Set<String>>(value))
                "quantityOption" -> preferencesRepo.saveQuantityPreference(value)
                "themeSetting" -> preferencesRepo.saveThemeSetting(value)
                "tinOzConversionRate" -> preferencesRepo.setTinOzConversionRate(value.toDouble())
                "tinGramsConversionRate" -> preferencesRepo.setTinGramsConversionRate(value.toDouble())
                "plaintextFormatString" -> preferencesRepo.setPlaintextFormatString(value)
                "plaintextDelimiter" -> preferencesRepo.setPlaintextDelimiter(value)
                "plaintextPresets" -> {
                    val presets = Json.decodeFromString<List<PlaintextPreset>>(value)
                    presets.forEach {
                        preferencesRepo.savePlaintextPreset(it.slot, it.formatString, it.delimiter)
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
                "allowMobileSync" -> preferencesRepo.saveAllowMobileData(value.toBoolean())
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