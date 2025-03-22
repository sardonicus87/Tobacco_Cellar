package com.sardonicus.tobaccocellar.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(
    private val itemsRepository: ItemsRepository,
    val preferencesRepo: PreferencesRepo,
): ViewModel() {

    /** Theme Settings */
    private val _themeSetting = MutableStateFlow(ThemeSetting.SYSTEM.value)
    private val _quantityOption = MutableStateFlow(QuantityOption.TINS)

    private val _tinOzConversionRate = MutableStateFlow(TinConversionRates.DEFAULT.ozRate)
    val tinOzConversionRate: StateFlow<Double> = _tinOzConversionRate.asStateFlow()

    private val _tinGramsConversionRate = MutableStateFlow(TinConversionRates.DEFAULT.gramsRate)
    val tinGramsConversionRate: StateFlow<Double> = _tinGramsConversionRate.asStateFlow()

    private val _snackbarState = MutableStateFlow(SnackbarState())
    val snackbarState: StateFlow<SnackbarState> = _snackbarState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun showSnackbar(message: String) {
        _snackbarState.value = SnackbarState(true, message)
    }

    fun snackbarShown() {
        _snackbarState.value = SnackbarState()
    }

    fun setLoadingState(loading: Boolean) {
        _loading.value = loading
    }

    // option initializations //
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.themeSetting.first().let {
                    _themeSetting.value = it
                    if (it == ThemeSetting.SYSTEM.value) {
                        preferencesRepo.saveThemeSetting(ThemeSetting.SYSTEM.value)
                    }
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.quantityOption.first().let {
                    _quantityOption.value = it
                    if (it == QuantityOption.TINS) {
                        preferencesRepo.saveQuantityPreference(QuantityOption.TINS.value)
                    }
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.tinOzConversionRate.first().let {
                    _tinOzConversionRate.value = it
                    if (it == TinConversionRates.DEFAULT.ozRate) {
                        preferencesRepo.setTinOzConversionRate(TinConversionRates.DEFAULT.ozRate)
                    }
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.tinGramsConversionRate.first().let {
                    _tinGramsConversionRate.value = it
                    if (it == TinConversionRates.DEFAULT.gramsRate) {
                        preferencesRepo.setTinGramsConversionRate(TinConversionRates.DEFAULT.gramsRate)
                    }
                }
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

    /** Database Settings */
    suspend fun deleteAllItems() {
        itemsRepository.deleteAllItems()
    }

    fun setTinConversionRates(ozRate: Double, gramsRate: Double) {
        viewModelScope.launch {
            preferencesRepo.setTinOzConversionRate(ozRate)
            preferencesRepo.setTinGramsConversionRate(gramsRate)
            _tinOzConversionRate.value = ozRate
            _tinGramsConversionRate.value = gramsRate
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


    /** Backup/Restore */
    // Backup //
    private val _backupState = MutableStateFlow(BackupState())
    var backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _restoreState = MutableStateFlow(RestoreState())
    var restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    init {
        updateSuggestedFilename()
    }

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

    fun saveBackup(context: Context) {
        viewModelScope.launch {
            val backupState = _backupState.value
            val suggestedFilename = _backupState.value.suggestedFilename

            if (suggestedFilename.isBlank()) {
                return@launch
            }

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_TITLE, suggestedFilename)
            }

            context.startActivity(intent)
        }
    }

    private fun updateSuggestedFilename() {
        val databaseChecked = _backupState.value.databaseChecked
        val settingsChecked = _backupState.value.settingsChecked
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val baseFilename = "TobaccoCellar_"

        val filename = when {
            databaseChecked && settingsChecked -> "${baseFilename}Complete_backup_$currentDate.tcbu"
            databaseChecked -> "${baseFilename}Database_backup_$currentDate.tcbu"
            settingsChecked -> "${baseFilename}Settings_backup_$currentDate.tcbu"
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

            val tempBackupFile = context.contentResolver.openFileDescriptor(uri, "w")?.use {
                File(context.cacheDir, "temp_db_backup.tcbu")
            } ?: throw IOException("Could not open file descriptor for backup")

            try {
                if (backupState.value.databaseChecked) {
                    backupDatabase(context, tempBackupFile)
                }

                val databaseBytes = if (backupState.value.databaseChecked) {
                    tempBackupFile.readBytes()
                } else { ByteArray(0) }


                val itemIds = withContext(Dispatchers.IO){ itemsRepository.getAllItemIds() }

                val itemSyncStateStringBuilder = StringBuilder()
                for (itemId in itemIds) {
                    val isSynced = preferencesRepo.getItemSyncStateString(itemId)
                    itemSyncStateStringBuilder.append("$itemId:$isSynced\n")
                }
                val itemSyncStateBytes = itemSyncStateStringBuilder.toString().toByteArray(Charset.forName("UTF-8"))

//                val dbVersion = TobaccoDatabase.getDatabaseVersion(context).toString().toByteArray()
                val databaseLengthBytes = databaseBytes.size.toByteArray()

                val combinedDatabaseBytes = ByteArray(databaseLengthBytes.size + databaseBytes.size + itemSyncStateBytes.size)
                databaseLengthBytes.copyInto(combinedDatabaseBytes)
                databaseBytes.copyInto(combinedDatabaseBytes, databaseLengthBytes.size)
                itemSyncStateBytes.copyInto(combinedDatabaseBytes, databaseBytes.size)

                val settingsBytes = if (backupState.value.settingsChecked) createSettingsBytes() else ByteArray(0)

                val magicNumber = byteArrayOf(0x54, 0x43, 0x42, 0x55) // "TCBU"
                val version = byteArrayOf(0x01) // Version 1
                val settingsLength = settingsBytes.size.toByteArray()

                val header = magicNumber + version + settingsLength

                val combinedBytes = ByteArray(header.size + combinedDatabaseBytes.size + settingsBytes.size)

                header.copyInto(combinedBytes)
                combinedDatabaseBytes.copyInto(combinedBytes, header.size)
                settingsBytes.copyInto(combinedBytes, header.size + combinedDatabaseBytes.size)

                writeBytesToFile(uri, combinedBytes, context)
                showSnackbar("Backup complete.")
            } catch (e: Exception) {
                showSnackbar("Backup failed.")
                throw e
            } finally {
                deleteTempFile(tempBackupFile)
                setLoadingState(false)
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
            var message = ""

            val bytes = readBytesFromFile(uri, context)
            if (bytes == null) {
                message = "Invalid file."
            } else {
                val fileContentState = validateBackupFile(bytes)
                val restoreState = _restoreState.value
                val (databaseBytes, itemSyncStateBytes, settingsBytes) = parseBackup(bytes)

                if (!fileContentState.databasePresent && !fileContentState.settingsPresent) {
                    message = "Invalid file."
                } else {
                    if (restoreState.databaseChecked && restoreState.settingsChecked) {
                        if (fileContentState.databasePresent && fileContentState.settingsPresent) {
                            try {
                                restoreDatabase(context, databaseBytes)
                            } catch (e: Exception) {
                                message = "Error restoring database."
                            }
                            if (message.isBlank()) {
                                restoreItemSyncState(itemSyncStateBytes)
                            }
                            restoreSettings(settingsBytes)
                            message = "Database and Settings restored."
                        } else {
                            if (!fileContentState.databasePresent) {
                                message = "Restore failed: file missing database data."
                            } else { message = "Restore failed: file missing settings data." }
                        }
                    }
                    else if (restoreState.databaseChecked) {
                        if (fileContentState.databasePresent) {
                            try {
                                restoreDatabase(context, databaseBytes)
                            } catch (e: Exception) {
                                message = "Error restoring database."
                            }
                            if (message.isBlank()) {
                                restoreItemSyncState(itemSyncStateBytes)
                                message = "Database restored."
                            }
                        } else { message = "Backup file does not contain database data." }
                    }
                    else if (restoreState.settingsChecked) {
                        if (fileContentState.settingsPresent) {
                            restoreSettings(settingsBytes)
                            message = "Settings restored."
                        } else {
                            message = "Backup file does not contain settings data."
                        }
                    }
                }
            }
            setLoadingState(false)
            showSnackbar(message)
        }
    }

    private fun validateBackupFile(bytes: ByteArray): FileContentState {
        val magicNumber = bytes.copyOfRange(0, 4)
        val version = bytes[4]

        if (!magicNumber.contentEquals(byteArrayOf(0x54, 0x43, 0x42, 0x55)) || version != 0x01.toByte()) {
            return FileContentState(false, false)
        }

        val settingsLengthBytes = bytes.copyOfRange(5, 9)
        val settingsLength = byteArrayToInt(settingsLengthBytes)
        val settingsBytes = bytes.copyOfRange(bytes.size - settingsLength, bytes.size)
        val databaseAndSyncStateBytes = bytes.copyOfRange(9, bytes.size - settingsLength)

        return FileContentState(databaseAndSyncStateBytes.isNotEmpty(), settingsBytes.isNotEmpty())
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
        val tempDir = File(context.cacheDir, "temp_db_restore_dir")
        tempDir.mkdirs()
        val tempDbFile = File(tempDir, "tobacco_database")
        val tempWalFile = File(tempDir, "tobacco_database-wal")
        val tempShmFile = File(tempDir, "tobacco_database-shm")

        try {
            copyFile(dbFile, backupDbFile)
            if (walFile.exists()) { copyFile(walFile, File("$dbPath-wal.bak")) }
            if (shmFile.exists()) { copyFile(shmFile, File("$dbPath-shm.bak")) }

            val dbLength = if (databaseBytes.isNotEmpty()) { databaseBytes.size / 3 } else { 0 }
            val dbBytes = databaseBytes.copyOfRange(0, dbLength)
            val walBytes = databaseBytes.copyOfRange(dbLength, dbLength * 2)
            val shmBytes = databaseBytes.copyOfRange(dbLength * 2, databaseBytes.size)

            tempDbFile.writeBytes(dbBytes)
            if (walBytes.isNotEmpty()) { tempWalFile.writeBytes(walBytes) }
            if (shmBytes.isNotEmpty()) { tempShmFile.writeBytes(shmBytes) }

            copyFile(tempDbFile, dbFile)
            if (walBytes.isNotEmpty()) { copyFile(tempWalFile, walFile) }
            if (shmBytes.isNotEmpty()) { copyFile(tempShmFile, shmFile) }

        } catch (e: Exception) {

            copyFile(backupDbFile, dbFile)
            if (File("$dbPath-wal.bak").exists()) { copyFile(File("$dbPath-wal.bak"), walFile) }
            if (File("$dbPath-shm.bak").exists()) { copyFile(File("$dbPath-shm.bak"), shmFile) }

            backupDbFile.delete()
            File("$dbPath-wal.bak").delete()
            File("$dbPath-shm.bak").delete()
            throw e
        } finally {
            tempDir.deleteRecursively()
            EventBus.emit(DatabaseRestoreEvent)
        }
    }

    private fun restoreItemSyncState(itemSyncStateBytes: ByteArray) {
        viewModelScope.launch {
            val itemSyncStateString = String(itemSyncStateBytes, Charset.forName("UTF-8"))
            val lines = itemSyncStateString.lines()
            for (line in lines) {
                val parts = line.split(":")
                if (parts.size == 2) {
                    val itemId = parts[0].toInt()
                    val isSynced = parts[1].toBooleanStrictOrNull()
                    if (isSynced != null) {
                        preferencesRepo.setItemSyncState(itemId, isSynced)
                    }
                }
            }
        }
    }

    private fun restoreSettings(settingsBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val settingsText = String(settingsBytes, Charset.forName("UTF-8"))
            parseSettingsText(settingsText, preferencesRepo)
        }
    }

}


data class ThemeSetting(
    val value: String
) {
    companion object {
        val LIGHT = ThemeSetting("Light")
        val DARK = ThemeSetting("Dark")
        val SYSTEM = ThemeSetting("System")
    }
}

data class QuantityOption(
    val value: String
) {
    companion object {
        val TINS = QuantityOption("\"No. of Tins\" (default)")
        val OUNCES = QuantityOption("Ounces/Pounds")
        val GRAMS = QuantityOption("Grams")
    }
}

data class TinConversionRates(
    val ozRate: Double,
    val gramsRate: Double
) {
    companion object {
        val DEFAULT = TinConversionRates(1.75, 50.0)
    }
}

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
)

data class SnackbarState(
    val show: Boolean = false,
    val message: String = ""
)

data object DatabaseRestoreEvent


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

fun getDatabaseFilePath(context: Context): String {
    val db = Room.databaseBuilder(
        context, TobaccoDatabase::class.java,
        "tobacco_database"
    ).build()
    val dbPath: String? = db.openHelper.writableDatabase.path
    db.close()

    return dbPath ?: ""
}

fun backupDatabase(context: Context, backupFile: File) {
    val dbPath = getDatabaseFilePath(context)
    val dbFile = File(dbPath)
    val walFile = File("$dbPath-wal")
    val shmFile = File("$dbPath-shm")

    val tempDir = File(context.cacheDir, "temp_db_backup_dir")
    tempDir.mkdirs()

    val tempDbFile = File(tempDir, "tobacco_database")
    val tempWalFile = File(tempDir, "tobacco_database-wal")
    val tempShmFile = File(tempDir, "tobacco_database-shm")

    try {

        copyFile(dbFile, tempDbFile)

        if (walFile.exists()) { copyFile(walFile, tempWalFile) }
        if (shmFile.exists()) { copyFile(shmFile, tempShmFile) }

        FileOutputStream(backupFile).use { outputStream ->
            if (tempDbFile.exists() && tempDbFile.length() > 0) {
                FileInputStream(tempDbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (tempWalFile.exists() && tempWalFile.length() > 0) {
                FileInputStream(tempWalFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (tempShmFile.exists() && tempShmFile.length() > 0) {
                FileInputStream(tempShmFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    } catch (e: Exception) {
        copyFile(tempDbFile, dbFile)
        if (tempWalFile.exists()) { copyFile(tempWalFile, walFile) }
        if (tempShmFile.exists()) { copyFile(tempShmFile, shmFile) }
        throw e
    } finally {
        tempDir.deleteRecursively()
    }
}

suspend fun createSettingsText(preferencesRepo: PreferencesRepo): String {
    val quantityOption = preferencesRepo.quantityOption.first().value
    val themeSetting = preferencesRepo.themeSetting.first()
    val tinOzConversionRate = preferencesRepo.tinOzConversionRate.first().toString()
    val tinGramsConversionRate = preferencesRepo.tinGramsConversionRate.first().toString()

    return """
            quantityOption=$quantityOption
            themeSetting=$themeSetting
            tinOzConversionRate=$tinOzConversionRate
            tinGramsConversionRate=$tinGramsConversionRate
        """.trimIndent()
}

fun writeBytesToFile(uri: Uri, bytes: ByteArray, context: Context) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(bytes)
        }
    } catch (e: Exception) {
        Log.e("SettingsViewModel", "Error writing bytes to file: ${e.message}")
        e.printStackTrace()
    }
}

// Restore backup //
fun readBytesFromFile(uri: Uri, context: Context): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: IOException) {
        Log.e("SettingsViewModel", "Exception caught read bytes: ${e.message}")
        e.printStackTrace()
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
                "quantityOption" -> preferencesRepo.saveQuantityPreference(value)
                "themeSetting" -> preferencesRepo.saveThemeSetting(value)
                "tinOzConversionRate" -> preferencesRepo.setTinOzConversionRate(value.toDouble())
                "tinGramsConversionRate" -> preferencesRepo.setTinGramsConversionRate(value.toDouble())
            }
        }
    }
}