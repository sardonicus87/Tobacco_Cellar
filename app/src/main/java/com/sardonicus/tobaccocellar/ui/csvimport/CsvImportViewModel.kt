package com.sardonicus.tobaccocellar.ui.csvimport

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.nio.charset.Charset
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class CsvImportViewModel(
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    private val _csvImportState = mutableStateOf(CsvImportState())
    val csvImportState: State<CsvImportState> = _csvImportState
    var csvUiState by mutableStateOf(CsvUiState())


    /** setting states from the CSV data **/
    fun onCsvLoaded(
        uri: Uri,
        header: List<String>,
        firstFullRecord: List<String>,
        recordCount: Int
    ) {
        val updatedHeader = header.mapIndexed { index, value ->
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate(11)
        }
        val updatedFirstFullRecord = firstFullRecord.mapIndexed { index, value ->
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate(11)
        }

        _csvImportState.value = CsvImportState(
            uri = uri,
            header = header.toList(),
            testHeader = updatedHeader.toList(),
            firstFullRecord = updatedFirstFullRecord.toList(),
            recordCount = recordCount
        )
    }

    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog = _showErrorDialog.asStateFlow()

    private val _csvErrorMessage = MutableStateFlow("")
    val csvErrorMessage = _csvErrorMessage.asStateFlow()

    fun onShowError (show: Boolean) {
        _showErrorDialog.value = show
    }

    fun onCsvError(message: String?) {
        _csvErrorMessage.value = message ?: ""
    }

    private fun String.truncate(maxLength: Int): String {
        return if (length > maxLength) {
            substring(0, minOf(8, maxLength - 3)) + "..."
        } else {
            this
        }
    }

    fun generateColumns(columnCount: Int): List<String> {
        val header = csvImportState.value.header
        return if (header.isNotEmpty() && header.size == columnCount) { header }
        else { (1..columnCount).map { "Column $it" } }
            .also {
                csvUiState = csvUiState.copy(columns = it)
            }
    }


    /** handling mapping options and UI state **/
    var mappingOptions by mutableStateOf(MappingOptions())

    fun updateSyncTinsOption(syncTins: Boolean) {
        mappingOptions = mappingOptions.copy(syncTins = syncTins)
        updateFieldMapping(CsvField.Quantity, "")
    }

    fun updateHeaderOption(hasHeader: Boolean) {
        mappingOptions = mappingOptions.copy(hasHeader = hasHeader)
    }

    fun updateCollateTinsOption(collateTins: Boolean) {
        mappingOptions = mappingOptions.copy(collateTins = collateTins)
    }

    fun updateDateFormat(dateFormat: String) {
        mappingOptions = mappingOptions.copy(dateFormat = dateFormat)
    }

    fun updateMaxValue(maxValue: String, maxValueDouble: Double?) {
        mappingOptions = mappingOptions.copy(
            maxValue = maxValueDouble,
            maxValueString = maxValue
        )
        csvUiState = csvUiState.copy(isFormValid = validateForm())
    }


    enum class CsvField {
        Brand, Blend, Type, Quantity, Rating, Favorite, Disliked, Notes, SubGenre, Cut, Production,
        Components, Flavoring, Container, TinQuantity, ManufactureDate, CellarDate, OpenDate, Finished
    }

    fun updateFieldMapping(field: CsvField, selectedColumn: String) {
        mappingOptions = when (field) {
            CsvField.Brand -> mappingOptions.copy(brandColumn = selectedColumn.ifBlank { "" })
            CsvField.Blend -> mappingOptions.copy(blendColumn = selectedColumn.ifBlank { "" })
            CsvField.Type -> mappingOptions.copy(typeColumn = selectedColumn.ifBlank { "" })
            CsvField.Quantity -> mappingOptions.copy(quantityColumn = selectedColumn.ifBlank { "" })
            CsvField.Rating -> mappingOptions.copy(ratingColumn = selectedColumn.ifBlank { "" })
            CsvField.Favorite -> mappingOptions.copy(favoriteColumn = selectedColumn.ifBlank { "" })
            CsvField.Disliked -> mappingOptions.copy(dislikedColumn = selectedColumn.ifBlank { "" })
            CsvField.Notes -> mappingOptions.copy(notesColumn = selectedColumn.ifBlank { "" })
            CsvField.SubGenre -> mappingOptions.copy(subGenreColumn = selectedColumn.ifBlank { "" })
            CsvField.Cut -> mappingOptions.copy(cutColumn = selectedColumn.ifBlank { "" })
            CsvField.Production -> mappingOptions.copy(productionColumn = selectedColumn.ifBlank { "" })
            CsvField.Components -> mappingOptions.copy(componentsColumn = selectedColumn.ifBlank { "" })
            CsvField.Flavoring -> mappingOptions.copy(flavoringColumn = selectedColumn.ifBlank { "" })
            CsvField.Container -> mappingOptions.copy(containerColumn = selectedColumn.ifBlank { "" })
            CsvField.TinQuantity -> mappingOptions.copy(tinQuantityColumn = selectedColumn.ifBlank { "" })
            CsvField.ManufactureDate -> mappingOptions.copy(manufactureDateColumn = selectedColumn.ifBlank { "" })
            CsvField.CellarDate -> mappingOptions.copy(cellarDateColumn = selectedColumn.ifBlank { "" })
            CsvField.OpenDate -> mappingOptions.copy(openDateColumn = selectedColumn.ifBlank { "" })
            CsvField.Finished -> mappingOptions.copy(finishedColumn = selectedColumn.ifBlank { "" })
        }
        csvUiState = csvUiState.copy(isFormValid = validateForm())
    }

    private val _importOption = MutableStateFlow(ImportOption.SKIP)
    val importOption = _importOption.asStateFlow()

    fun updateImportOption(option: ImportOption) {
        _importOption.value = option
    }

    private val _overwriteSelections = MutableStateFlow<Map<CsvField, Boolean>>(emptyMap())
    val overwriteSelections = _overwriteSelections.asStateFlow()

    fun updateOverwriteSelection(field: CsvField, overwrite: Boolean) {
        _overwriteSelections.value += (field to overwrite)
    }


    /** Confirm and import **/
    // components handling //
    private suspend fun componentSplitter(components: String): List<Components> {
        val componentsList = components.split(",").map { it.trim() }
        val existingComps = withContext(Dispatchers.Default) { itemsRepository.getAllComponentsStream().first() }
        return componentsList.toComponents(existingComps.map { it.componentName })
    }


    // Flavoring handling //
    private suspend fun flavorSplitter(flavoring: String): List<Flavoring> {
        val flavoringList = flavoring.split(",").map { it.trim() }
        val existingFlavors = withContext(Dispatchers.Default) { itemsRepository.getAllFlavoringStream().first() }
        return flavoringList.toFlavoring(existingFlavors.map { it.flavoringName })
    }


    // rating handling //
    private fun parseRating(rating: String, maxValue: Double?): Double? {
        if (rating.isBlank()) return null

        val symbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
        val decimalSeparator = symbols.decimalSeparator.toString()
        val ds = Regex.escape(decimalSeparator)
        val ratingRegex = Regex("(\\d*$ds?\\d+)")
        val result = ratingRegex.find(rating)

        return if (result != null && maxValue != null) {
            val (value) = result.destructured
            val scaling = 5.0 / maxValue
            val originalNumber = value.toDoubleOrNull() ?: return null

            val scaledNumber = originalNumber.times(scaling)
            val roundedNumber = (scaledNumber * 10).roundToInt() / 10.0

            roundedNumber.takeIf { it <= 5.0 } ?: 5.0

        } else {
            null
        }
    }


    // tin collation handling //
    private fun generateTinLabels(numTins: Int, startingLabelNumber: Int = 1): List<String> {
        return (startingLabelNumber until startingLabelNumber + numTins).map { "Lot $it" }
    }

    private fun String.parseTinQuantity(): Pair<Double, String> {
        val regex = Regex("""^([\d.,  ]*)\s*(.+)$""")
        val matchResult = regex.find(this.trim())

        return if (matchResult != null) {
            val preQuantity1 = matchResult.groupValues[1]
            val preQuantity2 = if (preQuantity1.startsWith(".") || preQuantity1.startsWith(",")) {
                "0$preQuantity1" } else { preQuantity1 }
            val preQuantity3a = preQuantity2.replace(',', '.')
            val preQuantity3b = preQuantity3a.replace(" ", "")
            val preQuantity3c = preQuantity3b.replace(" ", "")
            val lastDot = preQuantity3c.lastIndexOf('.')
            val preQuantity4 = if (lastDot != -1) {
                val integer = preQuantity3c.take(lastDot)
                val fractional = preQuantity3c.substring(lastDot)
                val cleaned = integer.replace(".", "")
                cleaned + fractional } else { preQuantity3c }
            val preQuantity5 = preQuantity4.toDoubleOrNull() ?: 0.0

            val quantity = if (preQuantity5 != 0.0) {
                ((preQuantity5 * 100.0).roundToInt()) / 100.0
            } else {
                0.0
            }

            val unit = matchResult.groupValues[2].trim().lowercase()

            val mappedUnit = when {
                unit.startsWith("ou") || unit.startsWith("oz") -> "oz"
                unit.startsWith("lb") || unit.startsWith("po") -> "lbs"
                unit.startsWith("g") -> "grams"
                else -> ""
            }
            Pair(quantity, mappedUnit)
        } else {
            Pair(0.0, "")
        }
    }

    private fun parseDateString(dateString: String, dateFormat: String): Long? {
        if (dateString.isBlank()) return null

        val trimmedDateString = dateString.trim()
        val delimiter = when {
            trimmedDateString.contains("/") -> "/"
            trimmedDateString.contains("-") -> "-"
            trimmedDateString.contains(".") -> "."
            else -> ""
        }

        return try {
            when (dateFormat) {
                "01/24 or 01/2024 (MM/YY)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    val month = parts[0].padStart(2, '0')
                    var year = parts[1]
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val currentYearEnd = currentYear % 100
                    if (year.length == 2) {
                        val yearInt = year.toInt()
                        year = if (yearInt > currentYearEnd) { "19$year" } else { "20$year" }
                    }

                    val formattedDate = "$month/01/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "24/01 or 2024/01 (YY/MM)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    var year = parts[0]
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val currentYearEnd = currentYear % 100
                    if (year.length == 2) {
                        val yearInt = year.toInt()
                        year = if (yearInt > currentYearEnd) { "19$year" } else { "20$year" }
                    }
                    val month = parts[1].padStart(2, '0')

                    val formattedDate = "$month/01/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "01/27/24 or 01/27/2024 (MM/DD/YY)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    val month = parts[0].padStart(2, '0')
                    val day = parts[1].padStart(2, '0')
                    var year = parts[2]
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val currentYearEnd = currentYear % 100
                    if (year.length == 2) {
                        val yearInt = year.toInt()
                        year = if (yearInt > currentYearEnd) { "19$year" } else { "20$year" }
                    }

                    val formattedDate = "$month/$day/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "27/01/24 or 27/01/2024 (DD/MM/YY)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    val day = parts[0].padStart(2, '0')
                    val month = parts[1].padStart(2, '0')
                    var year = parts[2]
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val currentYearEnd = currentYear % 100
                    if (year.length == 2) {
                        val yearInt = year.toInt()
                        year = if (yearInt > currentYearEnd) { "19$year" } else { "20$year" }
                    }

                    val formattedDate = "$month/$day/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "24/01/01 or 2024/01/01 (YY/MM/DD)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    var year = parts[0]
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val currentYearEnd = currentYear % 100
                    if (year.length == 2) {
                        val yearInt = year.toInt()
                        year = if (yearInt > currentYearEnd) { "19$year" } else { "20$year" }
                    }
                    val month = parts[1].padStart(2, '0')
                    val day = parts[2].padStart(2, '0')

                    val formattedDate = "$month/$day/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "January 27, 2024 or Jan 27, 2024" -> {
                    val separatorRegex = Regex("""([A-Za-z]{3,})\s+(\d{1,2}),\s+(\d{4})""")
                    val matchResult = separatorRegex.find(trimmedDateString)
                    if (matchResult != null) {
                        val (month, day, year) = matchResult.destructured

                        val formattedDate = "$month $day, $year"
                        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                        formatter.parse(formattedDate)?.time
                    } else null
                }
                "27 January, 2024 or 27 Jan, 2024" -> {
                    val separatorRegex = Regex("""(\d{1,2})\s+([A-Za-z]{3,}),\s+(\d{4})""")
                    val matchResult = separatorRegex.find(trimmedDateString)
                    if (matchResult != null) {
                        val (day, month, year) = matchResult.destructured

                        val formattedDate = "$month $day, $year"
                        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                        formatter.parse(formattedDate)?.time
                    } else null
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun insertTins(itemId: Int, tinDataList: List<TinData>) {
        val tinLabels = generateTinLabels(tinDataList.size)
        val tinsToInsert = tinDataList.mapIndexed { index, tinData ->
            Tins(
                itemsId = itemId,
                tinLabel = tinLabels[index],
                container = tinData.container,
                tinQuantity = tinData.quantity,
                unit = tinData.unit,
                manufactureDate = tinData.manufactureDate,
                cellarDate = tinData.cellarDate,
                openDate = tinData.openDate,
                finished = tinData.finished,
                lastModified = System.currentTimeMillis(),
            )
        }
        withContext(Dispatchers.Default) {
            itemsRepository.insertMultipleTins(tinsToInsert)
        }
    }

    suspend fun calculateSyncTinsQuantity(tinDataList: List<TinData>): Int {
        val ozRate = preferencesRepo.tinOzConversionRate.first()
        val gramsRate = preferencesRepo.tinGramsConversionRate.first()

        val tins = tinDataList.filter { !it.finished }

        val totalLbsTins = tins.filter { it.unit == "lbs" }.sumOf {
            (it.quantity * 16) / ozRate }
        val totalOzTins = tins.filter { it.unit == "oz" }.sumOf {
            it.quantity / ozRate }
        val totalGramsTins = tins.filter { it.unit == "grams" }.sumOf {
            it.quantity / gramsRate }

        return (totalLbsTins + totalOzTins + totalGramsTins).roundToInt()
    }


    // import function //
    private val _importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    val importStatus = _importStatus.asStateFlow()

    private val _navigateToResults = MutableSharedFlow<ImportResults>()
    val navigateToResults = _navigateToResults.asSharedFlow()

    private fun validateForm (csvUiState: MappingOptions = mappingOptions): Boolean {
        return with(csvUiState) {
            brandColumn.isNotBlank() && blendColumn.isNotBlank() &&
                    if (ratingColumn.isNotBlank()) { maxValue != null } else { true }
        }
    }


    fun confirmImport(contentResolver: ContentResolver) = viewModelScope.launch {
        _importStatus.value = ImportStatus.Loading
        SyncStateManager.schedulingPaused = true

        val uri = csvImportState.value.uri ?: return@launch
        val hasHeader = mappingOptions.hasHeader
        val columnIndices = getSelectedColumnIndices()
        val importOption = importOption.value
        var successfulConversions = 0
        var updatedCount = 0
        var insertions = 0
        var addedTins = 0

        withContext(Dispatchers.Default) {
            try {
                val existingItems = itemsRepository.getEverythingStream().first()
                    .associateBy { (it.items.brand to it.items.blend) }

                val tinDataMap = mutableMapOf<Pair<String, String>, MutableList<TinData>>()
                val clearedTins = mutableSetOf<Int>()

                // Pre-pass: collate tins
                contentResolver.openInputStream(uri)?.use { stream ->
                    val parser = CSVParser.parse(stream, Charset.defaultCharset(), CSVFormat.DEFAULT)
                    parser.forEachIndexed { index, record ->
                        if (index == 0 && hasHeader) return@forEachIndexed

                        val brand = record.getMapped(CsvField.Brand, columnIndices)
                        val blend = record.getMapped(CsvField.Blend, columnIndices)
                        if (brand.isBlank() || blend.isBlank()) return@forEachIndexed
                        val key = brand to blend

                        if (mappingOptions.collateTins) {
                            if (importOption == ImportOption.OVERWRITE) {
                                existingItems[key]?.items?.id?.let { id ->
                                    if (id !in clearedTins) {
                                        itemsRepository.deleteAllTinsForItem(id)
                                        clearedTins.add(id)
                                    }
                                }
                            }
                            val currentList = tinDataMap.getOrPut(key) { mutableListOf() }
                            currentList.add(parseTinData(record, columnIndices))
                        }
                    }
                }

                // Item Mapping
                val itemsToImport = mutableListOf<Items>()
                val itemsToUpdate = mutableListOf<Items>()
                val insertMeta = mutableListOf<RecordMeta>()
                val updateMeta = mutableListOf<Pair<Int, RecordMeta>>()

                contentResolver.openInputStream(uri)?.use { stream ->
                    val parser = CSVParser.parse(stream, Charset.defaultCharset(), CSVFormat.DEFAULT)
                    parser.forEachIndexed { index, record ->
                        if (hasHeader && index == 0) return@forEachIndexed

                        val brand = record.getMapped(CsvField.Brand, columnIndices)
                        val blend = record.getMapped(CsvField.Blend, columnIndices)
                        if (brand.isBlank() || blend.isBlank()) return@forEachIndexed

                        successfulConversions++

                        val key = brand to blend
                        val existing = existingItems[key]?.items
                        val metaFromCsv = extractMeta(record, columnIndices, tinDataMap[key] ?: emptyList())
                        val tins = tinDataMap[key] ?: emptyList()

                        if (existing != null) {
                            if (importOption == ImportOption.SKIP) return@forEachIndexed
                            val updated = createUpdatedItem(existing, record, columnIndices, tins)

                            val itemChanged = updated.copy(lastModified = 0) != existing.copy(lastModified = 0)
                            val compsChanged = compareChange(existingItems[key]!!.components.map { it.componentName }, metaFromCsv.componentString)
                            val flavorsChanged = compareChange(existingItems[key]!!.flavoring.map { it.flavoringName }, metaFromCsv.flavoringString)
                            val tinsChanged = mappingOptions.collateTins && compareTins(existingItems[key]!!.tins, tins)

                            if (itemChanged || compsChanged || flavorsChanged || tinsChanged) {
                                updatedCount++
                                itemsToUpdate.add(updated)
                                updateMeta.add(existing.id to metaFromCsv)
                            }
                        } else {
                            val newItem = createNewItem(brand, blend, record, columnIndices, tins)
                            itemsToImport.add(newItem)
                            insertMeta.add(extractMeta(record, columnIndices, tins))
                        }
                    }
                }

                if (itemsToUpdate.isNotEmpty()) { itemsRepository.updateMultipleItems(itemsToUpdate) }

                val insertedIds =
                    if (itemsToImport.isNotEmpty()) { itemsRepository.insertMultipleItems(itemsToImport) }
                    else emptyList()
                insertions = insertedIds.count { it != -1L }

                updateMeta.forEach { (id, meta) ->
                    processRelations(id, meta, importOption == ImportOption.OVERWRITE)
                    addedTins += meta.tins.size
                }

                itemsToImport.forEachIndexed { index, _ ->
                    val newId = insertedIds[index].toInt()
                    if (newId != -1) {
                        val meta = insertMeta[index]
                        processRelations(newId, meta, false)
                        addedTins += meta.tins.size
                    }
                }
            } catch (e: Exception) {
                _importStatus.value = ImportStatus.Error(e)
            } finally {
                SyncStateManager.schedulingPaused = false
                itemsRepository.triggerUploadWorker()

                val totalRecords = if (hasHeader) csvImportState.value.recordCount - 1 else csvImportState.value.recordCount
                val results = ImportResults(
                    totalRecords = totalRecords,
                    successfulConversions = successfulConversions,
                    successfulInsertions = insertions,
                    successfulUpdates = updatedCount,
                    successfulTins = addedTins,
                    updateFlag = updatedCount > 0,
                    tinFlag = mappingOptions.collateTins
                )

                delay(1500.milliseconds)

                _importStatus.value = ImportStatus.Success(
                    totalRecords = results.totalRecords,
                    successfulConversions = results.successfulConversions,
                    successfulInsertions = results.successfulInsertions,
                    successfulUpdates = results.successfulUpdates,
                    successfulTins = results.successfulTins,
                    updateFlag = results.updateFlag,
                    tinFlag = results.tinFlag
                )
                _navigateToResults.emit(results)
            }
        }
    }

    private fun CSVRecord.getMapped(field: CsvField, indices: Map<CsvField, Int>): String {
        val index = indices[field] ?: return ""
        return if (index >= 0 && index < size()) get(index).trim() else ""
    }

    private fun extractMeta(record: CSVRecord, indices: Map<CsvField, Int>, tins: List<TinData>): RecordMeta {
        return RecordMeta(
            componentString = record.getMapped(CsvField.Components, indices),
            flavoringString = record.getMapped(CsvField.Flavoring, indices),
            tins = tins
        )
    }

    private suspend fun processRelations(id: Int, meta: RecordMeta, clearExisting: Boolean) {
        if (clearExisting) {
            itemsRepository.deleteComponentsCrossRefByItemId(id)
            itemsRepository.deleteFlavoringCrossRefByItemId(id)
        }

        if (meta.componentString.isNotBlank()) {
            componentSplitter(meta.componentString).forEach { comp ->
                val compId = itemsRepository.getComponentIdByName(comp.componentName) ?: itemsRepository.insertComponent(comp).toInt()
                itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = id, componentId = compId))
            }
        }
        if (meta.flavoringString.isNotBlank()) {
            flavorSplitter(meta.flavoringString).forEach { flavor ->
                val flavorId = itemsRepository.getFlavoringIdByName(flavor.flavoringName) ?: itemsRepository.insertFlavoring(flavor).toInt()
                itemsRepository.insertFlavoringCrossRef(ItemsFlavoringCrossRef(itemId = id, flavoringId = flavorId))
            }
        }
        if (meta.tins.isNotEmpty()) { insertTins(id, meta.tins) }
    }

    private suspend fun createNewItem(
        brand: String,
        blend: String,
        record: CSVRecord,
        indices: Map<CsvField, Int>,
        tins: List<TinData>
    ): Items {
        val quantity = record.getMapped(CsvField.Quantity, indices).toIntOrNull() ?: 1
        val syncTins = mappingOptions.syncTins && tins.isNotEmpty()

        return Items(
            brand = brand,
            blend = blend,
            type = record.getMapped(CsvField.Type, indices).capitalizeType(),
            subGenre = record.getMapped(CsvField.SubGenre, indices),
            cut = record.getMapped(CsvField.Cut, indices),
            quantity = if (syncTins) calculateSyncTinsQuantity(tins) else if (quantity > 99) 1 else quantity,
            syncTins = syncTins,
            rating = parseRating(record.getMapped(CsvField.Rating, indices), mappingOptions.maxValue),
            favorite = record.getMapped(CsvField.Favorite, indices).toBoolean(),
            disliked = record.getMapped(CsvField.Disliked, indices).toBoolean(),
            inProduction = record.getMapped(CsvField.Production, indices).toBoolean(),
            notes = record.getMapped(CsvField.Notes, indices),
            lastModified = System.currentTimeMillis()
        )
    }

    private suspend fun createUpdatedItem(
        existing: Items,
        record: CSVRecord,
        indices: Map<CsvField, Int>,
        tins: List<TinData>
    ): Items {
        val overwrite = importOption.value == ImportOption.OVERWRITE
        val selections = overwriteSelections.value

        fun shouldUpdate(field: CsvField, currentValue: Any?): Boolean {
            return if (overwrite) { selections[field] == true } else {
                when (currentValue) {
                    is String -> currentValue.isBlank()
                    is Boolean -> !currentValue
                    null -> true
                    else -> false
                }
            }
        }

        val syncTins =
            if (overwrite) { mappingOptions.syncTins && tins.isNotEmpty() }
            else { existing.syncTins || (mappingOptions.syncTins && tins.isNotEmpty()) }

        return existing.copy(
            type = if (shouldUpdate(CsvField.Type, existing.type)) record.getMapped(CsvField.Type, indices).capitalizeType() else existing.type,
            subGenre = if (shouldUpdate(CsvField.SubGenre, existing.subGenre)) record.getMapped(CsvField.SubGenre, indices) else existing.subGenre,
            cut = if (shouldUpdate(CsvField.Cut, existing.cut)) record.getMapped(CsvField.Cut, indices) else existing.cut,
            quantity =
                if (syncTins) { calculateSyncTinsQuantity(tins) }
                else if (shouldUpdate(CsvField.Quantity, existing.quantity)) { record.getMapped(CsvField.Quantity, indices).toIntOrNull() ?: existing.quantity }
                else { existing.quantity },
            syncTins = syncTins,
            rating = if (shouldUpdate(CsvField.Rating, existing.rating)) parseRating(record.getMapped(CsvField.Rating, indices), mappingOptions.maxValue) else existing.rating,
            favorite = if (shouldUpdate(CsvField.Favorite, existing.favorite)) record.getMapped(CsvField.Favorite, indices).toBoolean() else existing.favorite,
            disliked = if (shouldUpdate(CsvField.Disliked, existing.disliked)) record.getMapped(CsvField.Disliked, indices).toBoolean() else existing.disliked,
            inProduction = if (shouldUpdate(CsvField.Production, existing.inProduction)) record.getMapped(CsvField.Production, indices).toBoolean() else existing.inProduction,
            notes = if (shouldUpdate(CsvField.Notes, existing.notes)) record.getMapped(CsvField.Notes, indices) else existing.notes,
            lastModified = System.currentTimeMillis()
        )
    }

    private fun compareChange(existing: List<String>, csvString: String): Boolean {
        if (existing.isEmpty() && csvString.isBlank()) return false
        val newNames = csvString.split(",").map { it.trim() }.filter { it.isNotBlank() }

        return existing.map { it.lowercase() }.sorted() != newNames.map { it.lowercase() }.sorted()
    }

    private fun compareTins(existing: List<Tins>, csvTins: List<TinData>): Boolean {
        if (existing.isEmpty() && csvTins.isEmpty()) return false
        if (existing.size != csvTins.size) return true

        val existingMap = existing.map {
            TinData(
                label = it.tinLabel,
                container = it.container,
                quantity = it.tinQuantity,
                unit = it.unit,
                manufactureDate = it.manufactureDate,
                cellarDate = it.cellarDate
            )
        }.sortedBy { it.label }

        return existingMap != csvTins.sortedBy { it.label }
    }

    private fun String.capitalizeType(): String {
        val upper = this.uppercase()
        return if (upper in listOf("AROMATIC", "ENGLISH", "BURLEY", "VIRGINIA", "OTHER")) {
            this.lowercase().replaceFirstChar { it.uppercase() }
        } else ""
    }

    private fun parseTinData(record: CSVRecord, indices: Map<CsvField, Int>): TinData {
        val (quantity, unit) = record.getMapped(CsvField.TinQuantity, indices).parseTinQuantity()
        val dateFormat = mappingOptions.dateFormat

        return TinData(
            label = "",
            container = record.getMapped(CsvField.Container, indices),
            quantity = quantity,
            unit = unit,
            manufactureDate = parseDateString(record.getMapped(CsvField.ManufactureDate, indices), dateFormat),
            cellarDate = parseDateString(record.getMapped(CsvField.CellarDate, indices), dateFormat),
            openDate = parseDateString(record.getMapped(CsvField.OpenDate, indices), dateFormat),
            finished = record.getMapped(CsvField.Finished, indices).toBoolean()
        )
    }

    // Stored CSV UI state to Database Table structure conversion function //
    private fun getSelectedColumnIndices(): Map<CsvField, Int> {
        val header = csvImportState.value.header
        return mapOf(
            CsvField.Brand to header.indexOf(mappingOptions.brandColumn),
            CsvField.Blend to header.indexOf(mappingOptions.blendColumn),
            CsvField.Type to header.indexOf(mappingOptions.typeColumn),
            CsvField.Quantity to header.indexOf(mappingOptions.quantityColumn),
            CsvField.Rating to header.indexOf(mappingOptions.ratingColumn),
            CsvField.Favorite to header.indexOf(mappingOptions.favoriteColumn),
            CsvField.Disliked to header.indexOf(mappingOptions.dislikedColumn),
            CsvField.Notes to header.indexOf(mappingOptions.notesColumn),
            CsvField.SubGenre to header.indexOf(mappingOptions.subGenreColumn),
            CsvField.Cut to header.indexOf(mappingOptions.cutColumn),
            CsvField.Production to header.indexOf(mappingOptions.productionColumn),
            CsvField.Components to header.indexOf(mappingOptions.componentsColumn),
            CsvField.Flavoring to header.indexOf(mappingOptions.flavoringColumn),
            CsvField.Container to header.indexOf(mappingOptions.containerColumn),
            CsvField.TinQuantity to header.indexOf(mappingOptions.tinQuantityColumn),
            CsvField.ManufactureDate to header.indexOf(mappingOptions.manufactureDateColumn),
            CsvField.CellarDate to header.indexOf(mappingOptions.cellarDateColumn),
            CsvField.OpenDate to header.indexOf(mappingOptions.openDateColumn),
            CsvField.Finished to header.indexOf(mappingOptions.finishedColumn),
        )
    }


    /** reset import state **/
    fun resetImportState() {
        _importStatus.value = ImportStatus.Idle
        _csvImportState.value = CsvImportState()
        csvUiState = CsvUiState()
        mappingOptions = MappingOptions()
    }
}


data class CsvImportState(
    val uri: Uri? = null,
    val header: List<String> = emptyList(),
    val testHeader: List<String> = emptyList(),
    val firstFullRecord: List<String> = emptyList(),
    val recordCount: Int = 0,
)

data class CsvUiState(
    val csvImportState: CsvImportState = CsvImportState(),
    val columns: List<String> = emptyList(),
    val isFormValid: Boolean = false,
)

data class RecordMeta(
    val componentString: String,
    val flavoringString: String,
    val tins: List<TinData>
)

data class MappingOptions(
    val hasHeader: Boolean = false,
    val collateTins: Boolean = false,
    val syncTins: Boolean = false,
    val brandColumn: String = "",
    val blendColumn: String = "",
    val typeColumn: String = "",
    val quantityColumn: String = "",
    val ratingColumn: String = "",
    val maxValue: Double? = null,
    val maxValueString: String = "",
    val favoriteColumn: String = "",
    val dislikedColumn: String = "",
    val notesColumn: String = "",
    val subGenreColumn: String = "",
    val cutColumn: String = "",
    val productionColumn: String = "",
    val componentsColumn: String = "",
    val flavoringColumn: String = "",
    val containerColumn: String = "",
    val tinQuantityColumn: String = "",
    val dateFormat: String = "",
    val manufactureDateColumn: String = "",
    val cellarDateColumn: String = "",
    val openDateColumn: String = "",
    val finishedColumn: String = "",
)

data class TinData(
    val label: String,
    val container: String,
    val quantity: Double,
    val unit: String,
    val manufactureDate: Long? = null,
    val cellarDate: Long? = null,
    val openDate: Long? = null,
    val finished: Boolean = false,
)

enum class ImportOption { SKIP, UPDATE, OVERWRITE }

sealed class ImportStatus {
    object Idle : ImportStatus()
    object Loading : ImportStatus()
    data class Success(
        val totalRecords: Int,
        val successfulConversions: Int,
        val successfulInsertions: Int,
        val successfulUpdates: Int,
        val successfulTins: Int,
        val updateFlag: Boolean = false,
        val tinFlag: Boolean = false,
    ) : ImportStatus()
    data class Error(val exception: Throwable) : ImportStatus()
}

data class ImportResults(
    val totalRecords: Int,
    val successfulConversions: Int,
    val successfulInsertions: Int,
    val successfulUpdates: Int,
    val successfulTins: Int,
    val updateFlag: Boolean = false,
    val tinFlag: Boolean = false,
)

fun List<String>.toComponents(existingComps: List<String>): List<Components> {
    return this
        .map { it }
        .filter { it.isNotBlank() }
        .map {  entered ->
            val normalizedComp = entered.trim().lowercase()
            val existingComp = existingComps.find { existing ->
                existing.lowercase() == normalizedComp
            }
            Components(componentName = existingComp ?: entered.trim())
        }
}

fun List<String>.toFlavoring(existingFlavor: List<String>): List<Flavoring> {
    return this
        .map { it }
        .filter { it.isNotBlank() }
        .map {  entered ->
            val normalizedFlavor = entered.trim().lowercase()
            val existingFlavor = existingFlavor.find { existing ->
                existing.lowercase() == normalizedFlavor
            }

            Flavoring(flavoringName = existingFlavor ?: entered.trim())
        }
}