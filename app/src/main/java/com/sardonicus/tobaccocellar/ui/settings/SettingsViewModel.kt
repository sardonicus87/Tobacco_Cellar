package com.sardonicus.tobaccocellar.ui.settings

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.MIGRATION_1_2
import com.sardonicus.tobaccocellar.data.MIGRATION_2_3
import com.sardonicus.tobaccocellar.data.MIGRATION_3_4
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.ui.home.formatDecimal
import com.sardonicus.tobaccocellar.ui.home.plaintext.PlaintextPreset
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

class SettingsViewModel(
    private val itemsRepository: ItemsRepository,
    val preferencesRepo: PreferencesRepo,
): ViewModel() {

    /** Theme Settings */
    private val _themeSetting = MutableStateFlow(ThemeSetting.SYSTEM.value)
    private val _showRatingsOption = MutableStateFlow(false)
    private val _typeGenreOption = MutableStateFlow(TypeGenreOption.TYPE)
    private val _quantityOption = MutableStateFlow(QuantityOption.TINS)

    private val _tinOzConversionRate = MutableStateFlow(TinConversionRates.DEFAULT.ozRate)
    val tinOzConversionRate: StateFlow<Double> = _tinOzConversionRate.asStateFlow()

    private val _tinGramsConversionRate = MutableStateFlow(TinConversionRates.DEFAULT.gramsRate)
    val tinGramsConversionRate: StateFlow<Double> = _tinGramsConversionRate.asStateFlow()

    private val _exportRating = MutableStateFlow(ExportRating())
    val exportRating: StateFlow<ExportRating> = _exportRating.asStateFlow()

    private val _snackbarState = MutableStateFlow(SnackbarState())
    val snackbarState: StateFlow<SnackbarState> = _snackbarState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun showSnackbar(message: String) { _snackbarState.value = SnackbarState(true, message) }

    fun snackbarShown() { _snackbarState.value = SnackbarState() }

    fun setLoadingState(loading: Boolean) { _loading.value = loading }

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
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.typeGenreOption.first().let {
                    _typeGenreOption.value = it
                    if (it == TypeGenreOption.TYPE) {
                        preferencesRepo.saveTypeGenreOption(TypeGenreOption.TYPE.value)
                    }
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.showRating.first().let {
                    _showRatingsOption.value = it
                    if (it) {
                        preferencesRepo.saveShowRatingOption(true)
                    }
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepo.exportRating.first().let {
                    _exportRating.value = it
                    if (it.maxRating == 5 && !it.rounding) {
                        preferencesRepo.saveExportRating(5, false)
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

    fun saveMaxRating(rating: Int, rounding: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveExportRating(rating, rounding)
            _exportRating.value = ExportRating(rating, rounding)
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

                // val dbVersion = TobaccoDatabase.getDatabaseVersion(context).toString().toByteArray()

                val databaseBytes = if (backupState.value.databaseChecked) {
                    tempDbZip.readBytes()
                } else { ByteArray(0) }
                val databaseLengthBytes = databaseBytes.size.toByteArray()

                val itemIds = withContext(Dispatchers.IO){ itemsRepository.getAllItemIds() }
                val itemSyncStateStringBuilder = StringBuilder()
                for (itemId in itemIds) {
                    val isSynced = preferencesRepo.getItemSyncStateString(itemId)
                    itemSyncStateStringBuilder.append("$itemId:$isSynced\n")
                }
                val itemSyncStateBytes = itemSyncStateStringBuilder.toString().toByteArray(Charset.forName("UTF-8"))

                val combinedDatabaseBytes = ByteArray(
                    databaseLengthBytes.size + databaseBytes.size + itemSyncStateBytes.size
                )

                databaseLengthBytes.copyInto(combinedDatabaseBytes)
                databaseBytes.copyInto(combinedDatabaseBytes, databaseLengthBytes.size)
                itemSyncStateBytes.copyInto(combinedDatabaseBytes, databaseBytes.size)

                val settingsBytes = if (backupState.value.settingsChecked) createSettingsBytes() else ByteArray(0)

                val magicNumber = byteArrayOf(0x54, 0x43, 0x42, 0x55) // "TCBU"
                val version = byteArrayOf(0x02) // Version 2 zipping db files
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
            var message = ""

            val bytes = readBytesFromFile(uri, context)
            if (bytes == null) {
                message = "Invalid file."
            } else {
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
                                restoreItemSyncState(itemSyncStateBytes)
                                restoreSettings(settingsBytes)
                                message = "Database and Settings restored."
                            } catch (e: Exception) {
                                println("Exception: $e")
                                restoreSettings(settingsBytes)
                                message = "Error restoring database, settings successfully restored."
                            }
                        } else {
                            message = if (!fileContentState.databasePresent) {
                                "Restore failed: file missing database data."
                            } else {
                                "Restore failed: file missing settings data."
                            }
                        }
                    }
                    else if (restoreState.databaseChecked) {
                        if (fileContentState.databasePresent) {
                            try {
                                restoreDatabase(context, databaseBytes)
                                restoreItemSyncState(itemSyncStateBytes)
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

        if (!magicNumber.contentEquals(byteArrayOf(0x54, 0x43, 0x42, 0x55)) || version != 0x02.toByte()) {
            return FileContentState(
                magicNumberValid = magicNumber.contentEquals(byteArrayOf(0x54, 0x43, 0x42, 0x55)),
                versionValid = version == 0x02.toByte(),
            )
        }

        val settingsLengthBytes = bytes.copyOfRange(5, 9)
        val settingsLength = byteArrayToInt(settingsLengthBytes)
        val settingsBytes = bytes.copyOfRange(bytes.size - settingsLength, bytes.size)
        val databaseAndSyncStateBytes = bytes.copyOfRange(9, bytes.size - settingsLength)

        return FileContentState(databaseAndSyncStateBytes.isNotEmpty(), settingsBytes.isNotEmpty(),
            versionValid = true,
            magicNumberValid = true
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

data class ExportRating(
    val maxRating: Int = 5,
    val rounding: Boolean = false
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


    return """
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
            }
        }
    }
}

fun exportRatingString(rating: Double?, maxRating: Int, rounding: Boolean): String {
    val scaling = maxRating / 5.0

    if (rating == null) { return "" }

    val scaledRating = (rating * scaling)
    val places = if (rounding) 0 else 2

    return formatDecimal(scaledRating, places)
}