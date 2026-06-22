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
import java.text.NumberFormat
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
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate()
        }
        val updatedFirstFullRecord = firstFullRecord.mapIndexed { index, value ->
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate()
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

    private fun String.truncate(): String {
        return if (length > 11) {
            substring(0, minOf(8, 11 - 3)) + "..."
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

    fun updateMaxValue(maxValue: String) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        val symbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
        val decimalSeparator = symbols.decimalSeparator.toString()
        val ds = Regex.escape(decimalSeparator)
        val pattern = Regex("^(\\s*|(\\d*)?($ds\\d{0,2})?)$")

        val parsedDouble = if (maxValue.isNotBlank() && maxValue.matches(pattern)) {
            val preNumber =
                if (maxValue.startsWith(decimalSeparator)) {
                    "0$maxValue"
                } else maxValue

            val number = numberFormat.parse(preNumber)
            number?.toDouble()
        } else { null }?.takeIf { it > 0.0 }

        mappingOptions = mappingOptions.copy(
            maxValue = parsedDouble,
            maxValueString = maxValue
        )
        csvUiState = csvUiState.copy(isFormValid = validateForm())
    }


    fun updateFieldMapping(field: CsvField, selectedColumn: String) {
        mappingOptions = mappingOptions.copy(
            columnMap = mappingOptions.columnMap + (field to selectedColumn.ifBlank { "" })
        )
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
        val matchResult = regex.find(this.trim()) ?: return Pair(0.0, "")

        val rawNumber = matchResult.groupValues[1]
            .replace(",", ".")
            .replace(" ", "")
            .replace(" ", "")

        val cleanedNumber = if (rawNumber.count { it == '.'} > 1) { // remove decimals as separators
            val lastIndex = rawNumber.lastIndexOf('.')
            rawNumber.substring(0, lastIndex).replace(".", "") + rawNumber.substring(lastIndex)
        } else { rawNumber }

        val quantity = cleanedNumber.toDoubleOrNull()?.let { ((it * 100.0).roundToInt()) / 100.0 } ?: 0.0
        val unit = matchResult.groupValues[2].trim().lowercase()
        val mappedUnit = when {
            unit.startsWith("ou") || unit.startsWith("oz") -> "oz"
            unit.startsWith("lb") || unit.startsWith("po") -> "lbs"
            unit.startsWith("g") -> "grams"
            else -> ""
        }

        return Pair(quantity, mappedUnit)
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
                    val year = parts[1].handleYear()

                    val formattedDate = "$month/01/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "24/01 or 2024/01 (YY/MM)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    val year = parts[0].handleYear()
                    val month = parts[1].padStart(2, '0')

                    val formattedDate = "$month/01/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "01/27/24 or 01/27/2024 (MM/DD/YY)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    val month = parts[0].padStart(2, '0')
                    val day = parts[1].padStart(2, '0')
                    val year = parts[2].handleYear()

                    val formattedDate = "$month/$day/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "27/01/24 or 27/01/2024 (DD/MM/YY)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    val day = parts[0].padStart(2, '0')
                    val month = parts[1].padStart(2, '0')
                    val year = parts[2].handleYear()

                    val formattedDate = "$month/$day/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    formatter.parse(formattedDate)?.time
                }
                "24/01/01 or 2024/01/01 (YY/MM/DD)" -> {
                    val parts = trimmedDateString.split(delimiter)
                    val year = parts[0].handleYear()
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

    private fun String.handleYear(): String {
        if (this.length != 2) return this
        val yearInt = this.toIntOrNull() ?: return this
        val currentYearEnd = Calendar.getInstance().get(Calendar.YEAR) % 100
        return if (yearInt > currentYearEnd) { "19$this" } else { "20$this" }
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

    fun calculateSyncTinsQuantity(tinDataList: List<TinData>, ozRate: Double, gramsRate: Double): Int {
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
            val brandMapped = columnMap[CsvField.Brand]?.isNotBlank() == true
            val blendMapped = columnMap[CsvField.Blend]?.isNotBlank() == true
            val ratingValid = if (columnMap[CsvField.Rating]?.isNotBlank() == true)
                { maxValue != null } else { true }

            brandMapped && blendMapped && ratingValid
        }
    }


    fun confirmImport(contentResolver: ContentResolver) = viewModelScope.launch {
        _importStatus.value = ImportStatus.Loading
        SyncStateManager.schedulingPaused = true

        val ozRate = preferencesRepo.tinOzConversionRate.first()
        val gramsRate = preferencesRepo.tinGramsConversionRate.first()
        val compCache = itemsRepository.getAllComponentsStream().first()
            .associate { it.componentName.lowercase() to it.componentId }.toMutableMap()
        val flavorCache = itemsRepository.getAllFlavoringStream().first()
            .associate { it.flavoringName.lowercase() to it.flavoringId }.toMutableMap()

        val uri = csvImportState.value.uri ?: return@launch
        val hasHeader = mappingOptions.hasHeader
        val columnIndices = getSelectedColumnIndices()
        val importOption = importOption.value
        var successfulConversions = 0
        var updatedCount = 0
        var addedTins = 0
        var updateFlag = false

        withContext(Dispatchers.Default) {
            try {
                val existingItems = itemsRepository.getEverythingStream().first()
                    .associateBy { (it.items.brand to it.items.blend) }
                val existingIds = existingItems.values.associateBy { it.items.id }

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
                val processedKeys = mutableSetOf<Pair<String, String>>()

                contentResolver.openInputStream(uri)?.use { stream ->
                    val parser = CSVParser.parse(stream, Charset.defaultCharset(), CSVFormat.DEFAULT)
                    parser.forEachIndexed { index, record ->
                        if (hasHeader && index == 0) return@forEachIndexed

                        val brand = record.getMapped(CsvField.Brand, columnIndices)
                        val blend = record.getMapped(CsvField.Blend, columnIndices)
                        if (brand.isBlank() || blend.isBlank()) return@forEachIndexed

                        successfulConversions++

                        val key = brand to blend
                        if (key in processedKeys) return@forEachIndexed
                        processedKeys.add(key)

                        val existing = existingItems[key]?.items
                        val tins = tinDataMap[key] ?: emptyList()

                        if (existing != null) {
                            updateFlag = true
                            if (importOption == ImportOption.SKIP) return@forEachIndexed
                            val tinsToProcess = if (mappingOptions.collateTins) {
                                when (importOption) {
                                    ImportOption.OVERWRITE -> tins
                                    ImportOption.UPDATE -> { if (existingItems[key]!!.tins.isEmpty()) tins else emptyList() }
                                }
                            } else emptyList()
                            val metaFromCsv = extractMeta(record, columnIndices, tinsToProcess)
                            val updated = createUpdatedItem(existing, record, columnIndices, tinsToProcess, ozRate, gramsRate)

                            val itemChanged = updated.copy(lastModified = 0) != existing.copy(lastModified = 0)
                            val compsChanged = compareChange(existingItems[key]!!.components.map { it.componentName }, metaFromCsv.componentString, importOption)
                            val flavorsChanged = compareChange(existingItems[key]!!.flavoring.map { it.flavoringName }, metaFromCsv.flavoringString, importOption)
                            val tinsChanged = mappingOptions.collateTins && compareTins(existingItems[key]!!.tins, tins, importOption)

                            if (itemChanged || compsChanged || flavorsChanged || tinsChanged) {
                                updatedCount++
                                itemsToUpdate.add(updated)
                                updateMeta.add(existing.id to metaFromCsv)
                            }
                        } else {
                            val newItem = createNewItem(brand, blend, record, columnIndices, tins, ozRate, gramsRate)
                            itemsToImport.add(newItem)
                            insertMeta.add(extractMeta(record, columnIndices, tins))
                        }
                    }
                }

                if (itemsToUpdate.isNotEmpty()) { itemsRepository.updateMultipleItems(itemsToUpdate) }

                val insertedIds =
                    if (itemsToImport.isNotEmpty()) { itemsRepository.insertMultipleItems(itemsToImport) }
                    else emptyList()
                val insertions = insertedIds.count { it != -1L }

                updateMeta.forEach { (id, meta) ->
                    processRelations(id, meta, importOption == ImportOption.OVERWRITE, compCache, flavorCache)
                    val originalTinCount = if (importOption == ImportOption.OVERWRITE) {
                        existingIds[id]?.tins?.size ?: 0 } else 0

                    addedTins += (meta.tins.size - originalTinCount)
                }

                itemsToImport.forEachIndexed { index, _ ->
                    val newId = insertedIds[index].toInt()
                    if (newId != -1) {
                        val meta = insertMeta[index]
                        processRelations(newId, meta, false, compCache, flavorCache)
                        addedTins += meta.tins.size
                    }
                }

                SyncStateManager.schedulingPaused = false
                itemsRepository.triggerUploadWorker()

                val totalRecords = if (hasHeader) csvImportState.value.recordCount - 1 else csvImportState.value.recordCount
                val results = ImportResults(
                    totalRecords = totalRecords,
                    successfulConversions = successfulConversions,
                    successfulInsertions = insertions,
                    successfulUpdates = updatedCount,
                    successfulTins = addedTins,
                    updateFlag = updateFlag,
                    tinFlag = mappingOptions.collateTins
                )

                delay(1500.milliseconds)

                _importStatus.value = ImportStatus.Success
                _navigateToResults.emit(results)

            } catch (e: Exception) {
                _importStatus.value = ImportStatus.Error(e)
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

    private suspend fun processRelations(id: Int, meta: RecordMeta, clearExisting: Boolean, compCache: MutableMap<String, Int>, flavorCache: MutableMap<String, Int>) {
        if (clearExisting) {
            itemsRepository.deleteComponentsCrossRefByItemId(id)
            itemsRepository.deleteFlavoringCrossRefByItemId(id)
        }

        if (meta.componentString.isNotBlank()) {
            val components = meta.componentString.split(",").map { it.trim() }
            components.forEach { comp ->
                val normalized = comp.lowercase()
                val compId =
                    if (compCache.containsKey(normalized)) { compCache[normalized]!! }
                    else {
                        val newId = itemsRepository.insertComponent(Components(componentName = comp)).toInt()
                        compCache[normalized] = newId
                        newId
                    }
                itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = id, componentId = compId))
            }
        }
        if (meta.flavoringString.isNotBlank()) {
            val flavorings = meta.flavoringString.split(",").map { it.trim() }
            flavorings.forEach { flavor ->
                val normalized = flavor.lowercase()
                val flavorId =
                    if (flavorCache.containsKey(normalized)) { flavorCache[normalized]!! }
                    else {
                        val newId = itemsRepository.insertFlavoring(Flavoring(flavoringName = flavor)).toInt()
                        flavorCache[normalized] = newId
                        newId
                    }
                itemsRepository.insertFlavoringCrossRef(ItemsFlavoringCrossRef(itemId = id, flavoringId = flavorId))
            }
        }
        if (meta.tins.isNotEmpty()) { insertTins(id, meta.tins) }
    }

    private fun createNewItem(
        brand: String,
        blend: String,
        record: CSVRecord,
        indices: Map<CsvField, Int>,
        tins: List<TinData>,
        ozRate: Double,
        gramsRate: Double
    ): Items {
        val quantity = record.getMapped(CsvField.Quantity, indices).toIntOrNull() ?: 1
        val syncTins = mappingOptions.syncTins && tins.isNotEmpty()

        return Items(
            brand = brand,
            blend = blend,
            type = record.getMapped(CsvField.Type, indices).capitalizeType(),
            subGenre = record.getMapped(CsvField.SubGenre, indices),
            cut = record.getMapped(CsvField.Cut, indices),
            quantity = if (syncTins) calculateSyncTinsQuantity(tins, ozRate, gramsRate) else if (quantity > 99) 1 else quantity,
            syncTins = syncTins,
            rating = parseRating(record.getMapped(CsvField.Rating, indices), mappingOptions.maxValue),
            favorite = record.getMapped(CsvField.Favorite, indices).toBoolean(),
            disliked = record.getMapped(CsvField.Disliked, indices).toBoolean(),
            inProduction = record.getMapped(CsvField.Production, indices).toBoolean(),
            notes = record.getMapped(CsvField.Notes, indices),
            lastModified = System.currentTimeMillis()
        )
    }

    private fun createUpdatedItem(
        existing: Items,
        record: CSVRecord,
        indices: Map<CsvField, Int>,
        tins: List<TinData>,
        ozRate: Double,
        gramsRate: Double
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
                if (syncTins) { calculateSyncTinsQuantity(tins, ozRate, gramsRate) }
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

    private fun compareChange(existing: List<String>, csvString: String, option: ImportOption): Boolean {
        if (existing.isEmpty() && csvString.isBlank()) return false
        val newNames = csvString.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val existingNames = existing.map { it.lowercase() }

        return when (option) {
            ImportOption.OVERWRITE -> newNames.sorted() != existing.sorted()
            ImportOption.UPDATE -> {
                if (newNames.isEmpty()) false
                else newNames.any { it !in existingNames }
            }
            else -> false
        }
    }

    private fun compareTins(existing: List<Tins>, csvTins: List<TinData>, option: ImportOption): Boolean {
        return when (option) {
            ImportOption.OVERWRITE -> {
                if (existing.isEmpty() && csvTins.isEmpty()) false
                else existing.size != csvTins.size
            }
            ImportOption.UPDATE -> {
                existing.isEmpty() && csvTins.isNotEmpty()
            }
            else -> false
        }
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
            finished = record.getMapped(CsvField.Finished, indices).toBoolean(),
            lastModified = System.currentTimeMillis()
        )
    }

    // Stored CSV UI state to Database Table structure conversion function //
    private fun getSelectedColumnIndices(): Map<CsvField, Int> {
        val header = csvImportState.value.header
        return CsvField.entries.associateWith { field ->
            header.indexOf(mappingOptions.columnMap[field] ?: "")
        }
    }


    /** reset import state **/
    fun resetImportState() {
        _importStatus.value = ImportStatus.Idle
        _csvImportState.value = CsvImportState()
        csvUiState = CsvUiState()
        mappingOptions = MappingOptions()
    }
}


enum class CsvField {
    Brand, Blend, Type, Quantity, Rating, Favorite, Disliked, Notes, SubGenre, Cut, Production,
    Components, Flavoring, Container, TinQuantity, ManufactureDate, CellarDate, OpenDate, Finished
}

data class CsvImportState(
    val uri: Uri? = null,
    val header: List<String> = emptyList(),
    val testHeader: List<String> = emptyList(),
    val firstFullRecord: List<String> = emptyList(),
    val recordCount: Int = 0
)

data class CsvUiState(
    val columns: List<String> = emptyList(),
    val isFormValid: Boolean = false
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
    val dateFormat: String = "",
    val maxValue: Double? = null,
    val maxValueString: String = "",
    val columnMap: Map<CsvField, String> = emptyMap()
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
    val lastModified: Long = System.currentTimeMillis()
)

enum class ImportOption { SKIP, UPDATE, OVERWRITE }

sealed class ImportStatus {
    object Idle : ImportStatus()
    object Loading : ImportStatus()
    object Success : ImportStatus()
    data class Error(val exception: Throwable) : ImportStatus()
}

data class ImportResults(
    val totalRecords: Int,
    val successfulConversions: Int,
    val successfulInsertions: Int,
    val successfulUpdates: Int,
    val successfulTins: Int,
    val updateFlag: Boolean = false,
    val tinFlag: Boolean = false
)