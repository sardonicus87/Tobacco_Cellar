package com.sardonicus.tobaccocellar.ui.csvimport

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
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.round
import kotlin.math.roundToInt

class CsvImportViewModel(
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    private val _csvImportState = mutableStateOf(CsvImportState())
    val csvImportState: State<CsvImportState> = _csvImportState
    var csvUiState by mutableStateOf(CsvUiState())


    /** setting states from the CSV data **/
    fun onCsvLoaded(
        header: List<String>,
        firstFullRecord: List<String>,
        allRecords: List<List<String>>,
        recordCount: Int
    ) {
        val updatedHeader = header.mapIndexed { index, value ->
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate(11)
        }
        val updatedFirstFullRecord = firstFullRecord.mapIndexed { index, value ->
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate(11)
        }

        _csvImportState.value = CsvImportState(
            header = header.toList(),
            testHeader = updatedHeader.toList(),
            firstFullRecord = updatedFirstFullRecord.toList(),
            allRecords = allRecords.toList(),
            recordCount = recordCount
        )
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
        val existingComps = withContext(Dispatchers.IO) { itemsRepository.getAllComponentsStream().first() }
        return componentsList.toComponents(existingComps.map { it.componentName })
    }


    // Flavoring handling //
    private suspend fun flavorSplitter(flavoring: String): List<Flavoring> {
        val flavoringList = flavoring.split(",").map { it.trim() }
        val existingFlavors = withContext(Dispatchers.IO) { itemsRepository.getAllFlavoringStream().first() }
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
            val roundedNumber = round(scaledNumber * 10) / 10

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
                (round(preQuantity5 * 100.0)) / 100.0
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
        withContext(Dispatchers.IO) {
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


    fun confirmImport() = viewModelScope.launch {
        _importStatus.value = ImportStatus.Loading
        SyncStateManager.schedulingPaused = true

        val hasHeader = mappingOptions.hasHeader
        val collateTins = mappingOptions.collateTins
        val importOption = importOption.value
        var updatedCount = 0
        var updatedConversions = 0
        var addedTins = 0
        var updatedFlagSet = false
        var tinsFlagSet = false

        val recordsToImport =
            if (hasHeader) csvImportState.value.allRecords.drop(1)
            else csvImportState.value.allRecords
        val columnIndices = getSelectedColumnIndices()

        val tinLabelMap = mutableMapOf<Pair<String, String>, Int>()
        val tinDataMap = mutableMapOf<Pair<String, String>, MutableList<TinData>>()

        recordsToImport.forEach { record ->
            val brand = record[columnIndices[CsvField.Brand]!!]
            val blend = record[columnIndices[CsvField.Blend]!!]
            val brandBlendKey = Pair(brand, blend)

            val numTins = 1
            val nextLabelNumber = tinLabelMap.getOrDefault(brandBlendKey, 1)
            val tinLabels = generateTinLabels(numTins, nextLabelNumber)
            tinLabelMap[brandBlendKey] = nextLabelNumber + numTins

            if (collateTins) {
                tinsFlagSet = true
                if (importOption == ImportOption.OVERWRITE) {
                    if (brand.isNotBlank() && blend.isNotBlank()) {
                        val existingItem = withContext(Dispatchers.IO) {
                            itemsRepository.getItemByIndex(brand, blend)
                        }
                        if (existingItem != null) {
                            itemsRepository.deleteAllTinsForItem(existingItem.id)
                        }
                    }
                }
            }

            val tinDataList = if (collateTins) {
                List(numTins) { index ->
                    val container =
                        if (columnIndices[CsvField.Container] != null &&
                            columnIndices[CsvField.Container]!! in record.indices
                        ) {
                            record[columnIndices[CsvField.Container]!!].trim()
                        } else ""
                    val (quantity, unit) =
                        if (columnIndices[CsvField.TinQuantity] != null &&
                            columnIndices[CsvField.TinQuantity]!! in record.indices
                        ) {
                            record[columnIndices[CsvField.TinQuantity]!!].trim().parseTinQuantity()
                        } else {
                            Pair(0.0, "")
                        }
                    val manufactureDate =
                        if (columnIndices[CsvField.ManufactureDate] != null &&
                            columnIndices[CsvField.ManufactureDate]!! in record.indices
                        ) {
                            val dateString = record[columnIndices[CsvField.ManufactureDate]!!].trim()
                            val dateFormat = mappingOptions.dateFormat
                            parseDateString(dateString, dateFormat)
                        } else null
                    val cellarDate =
                        if (columnIndices[CsvField.CellarDate] != null &&
                            columnIndices[CsvField.CellarDate]!! in record.indices
                        ) {
                            val dateString = record[columnIndices[CsvField.CellarDate]!!].trim()
                            val dateFormat = mappingOptions.dateFormat
                            parseDateString(dateString, dateFormat)
                        } else null
                    val openDate =
                        if (columnIndices[CsvField.OpenDate] != null &&
                            columnIndices[CsvField.OpenDate]!! in record.indices
                        ) {
                            val dateString = record[columnIndices[CsvField.OpenDate]!!].trim()
                            val dateFormat = mappingOptions.dateFormat
                            parseDateString(dateString, dateFormat)
                        } else null
                    val finished =
                        if (columnIndices[CsvField.Finished] != null &&
                            columnIndices[CsvField.Finished]!! in record.indices
                        ) {
                            record[columnIndices[CsvField.Finished]!!].toBoolean()
                        } else false


                    TinData(
                        label = tinLabels[index],
                        container = container,
                        quantity = quantity,
                        unit = unit,
                        manufactureDate = manufactureDate,
                        cellarDate = cellarDate,
                        openDate = openDate,
                        finished = finished
                    )
                }
            } else emptyList()

            tinDataMap.getOrPut(brandBlendKey) { mutableListOf() }.addAll(tinDataList)
        }

        @Suppress("SimplifyBooleanWithConstants")
        val itemsToImport = recordsToImport.mapNotNull { record ->
            val brand =
                if (columnIndices[CsvField.Brand] != null &&
                    columnIndices[CsvField.Brand]!! in record.indices)
                    record[columnIndices[CsvField.Brand]!!] else ""
            val blend =
                if (columnIndices[CsvField.Blend] != null &&
                    columnIndices[CsvField.Blend]!! in record.indices)
                    record[columnIndices[CsvField.Blend]!!] else ""
            val components =
                if (columnIndices[CsvField.Components] != null &&
                    columnIndices[CsvField.Components]!! in record.indices)
                    record[columnIndices[CsvField.Components]!!] else ""
            val flavoring =
                if (columnIndices[CsvField.Flavoring] != null &&
                    columnIndices[CsvField.Flavoring]!! in record.indices)
                    record[columnIndices[CsvField.Flavoring]!!] else ""

            val brandBlendKey = Pair(brand, blend)
            val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()

            if (brand.isBlank() || blend.isBlank()) {
                null
            } else {
                val existingItem = withContext(Dispatchers.IO) {
                    itemsRepository.getItemByIndex(brand, blend)
                }
                val componentsList = componentSplitter(components)
                val flavoringList = flavorSplitter(flavoring)

                if (existingItem != null) {
                    updatedConversions++
                    when (importOption) {

                        ImportOption.UPDATE -> {
                            updatedFlagSet = true
                            val brandBlendKey = Pair(existingItem.brand, existingItem.blend)
                            val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()
                            val existingTins = withContext(Dispatchers.IO) {
                                itemsRepository.getTinsForItemStream(existingItem.id).first()
                            }

                            val updatedItem = existingItem.copy(
                                type = if (existingItem.type.isBlank() &&
                                    columnIndices[CsvField.Type] != null &&
                                    columnIndices[CsvField.Type]!! in record.indices
                                ) {
                                    val typeFromCsv = record[columnIndices[CsvField.Type]!!]
                                    when (typeFromCsv.uppercase()) {
                                        "AROMATIC", "ENGLISH", "BURLEY", "VIRGINIA", "OTHER" -> {
                                            typeFromCsv.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        }
                                        else -> ""
                                    }
                                } else existingItem.type,
                                subGenre = if (existingItem.subGenre.isBlank() &&
                                    columnIndices[CsvField.SubGenre] != null &&
                                    columnIndices[CsvField.SubGenre]!! in record.indices
                                )
                                    record[columnIndices[CsvField.SubGenre]!!]
                                else existingItem.subGenre,
                                cut = if (existingItem.cut.isBlank() &&
                                    columnIndices[CsvField.Cut] != null &&
                                    columnIndices[CsvField.Cut]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Cut]!!]
                                else existingItem.cut,
                                quantity = if (mappingOptions.syncTins && tinDataList.isNotEmpty() && existingTins.isEmpty())
                                    calculateSyncTinsQuantity(tinDataList)
                                else existingItem.quantity,
                                syncTins = if (mappingOptions.syncTins && tinDataList.isNotEmpty() && existingTins.isEmpty()) true else existingItem.syncTins,
                                rating = if (existingItem.rating == null &&
                                    columnIndices[CsvField.Rating] != null &&
                                    mappingOptions.maxValue != null &&
                                    columnIndices[CsvField.Rating]!! in record.indices
                                )
                                    parseRating(record[columnIndices[CsvField.Rating]!!], mappingOptions.maxValue)
                                else existingItem.rating,
                                favorite = if (existingItem.favorite == false &&
                                    existingItem.disliked == false &&
                                    columnIndices[CsvField.Favorite] != null &&
                                    columnIndices[CsvField.Favorite]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Favorite]!!].toBoolean()
                                else existingItem.favorite,
                                disliked = if (existingItem.favorite == false &&
                                    existingItem.disliked == false &&
                                    columnIndices[CsvField.Disliked] != null &&
                                    columnIndices[CsvField.Disliked]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Disliked]!!].toBoolean()
                                else existingItem.disliked,
                                inProduction = if (existingItem.inProduction == false &&
                                    columnIndices[CsvField.Production] != null &&
                                    columnIndices[CsvField.Production]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Production]!!].toBoolean()
                                else existingItem.inProduction,
                                notes = if (existingItem.notes.isBlank() &&
                                    columnIndices[CsvField.Notes] != null &&
                                    columnIndices[CsvField.Notes]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Notes]!!]
                                else existingItem.notes,
                                lastModified = existingItem.lastModified
                            )

                            val existingComponents = withContext(Dispatchers.IO) {
                                itemsRepository.getComponentsForItemStream(existingItem.id).first().map {
                                    it.componentName
                                }
                            }
                            var compsAdded = false
                            if (existingComponents.isEmpty() && componentsList.isNotEmpty()) {
                                compsAdded = true
                                componentsList.forEach {
                                    var componentId = itemsRepository.getComponentIdByName(it.componentName)
                                    if (componentId == null) {
                                        componentId = itemsRepository.insertComponent(it).toInt()
                                    }
                                    itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = existingItem.id, componentId = componentId))
                                }
                            }

                            val existingFlavoring = withContext(Dispatchers.IO) {
                                itemsRepository.getFlavoringForItemStream(existingItem.id).first().map {
                                    it.flavoringName
                                }
                            }
                            var flavorAdded = false
                            if (existingFlavoring.isEmpty() && flavoringList.isNotEmpty()) {
                                flavorAdded = true
                                flavoringList.forEach {
                                    var flavoringId = itemsRepository.getFlavoringIdByName(it.flavoringName)
                                    if (flavoringId == null) {
                                        flavoringId = itemsRepository.insertFlavoring(it).toInt()
                                    }
                                    itemsRepository.insertFlavoringCrossRef(ItemsFlavoringCrossRef(itemId = existingItem.id, flavoringId = flavoringId))
                                }
                            }

                            var tinsAddedToItem = false
                            if (collateTins) {
                                if (existingTins.isEmpty()) {
                                    if (tinDataList.isNotEmpty()) {
                                        tinsAddedToItem = true
                                        insertTins(existingItem.id, tinDataList)
                                        tinDataList.forEach { _ -> addedTins++ }
                                    }
                                }
                            }

                            if (existingItem != updatedItem) {
                                updatedCount++
                                val updated = updatedItem.copy(lastModified = System.currentTimeMillis())
                                withContext(Dispatchers.IO) { itemsRepository.updateItem(updated) }
                            }
                            if (existingItem == updatedItem && (compsAdded || flavorAdded || tinsAddedToItem)) updatedCount++

                            null
                        }

                        ImportOption.OVERWRITE -> {
                            updatedFlagSet = true
                            val overwriteFields = overwriteSelections.value.filterValues { it }.keys
                            val brandBlendKey = Pair(existingItem.brand, existingItem.blend)
                            val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()
                            // Should be empty in overwrite, but double check
                            val existingTins = withContext(Dispatchers.IO) { itemsRepository.getTinsForItemStream(existingItem.id).first() }

                            val updatedItem = existingItem.copy(
                                type = if (overwriteFields.contains(CsvField.Type) &&
                                    columnIndices[CsvField.Type] != null &&
                                    columnIndices[CsvField.Type]!! in record.indices
                                ) {
                                    val typeFromCsv = record[columnIndices[CsvField.Type]!!]
                                    when (typeFromCsv.uppercase()) {
                                        "AROMATIC", "ENGLISH", "BURLEY", "VIRGINIA", "OTHER" -> {
                                            typeFromCsv.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        }
                                        else -> ""
                                    }
                                } else existingItem.type,
                                subGenre = if (overwriteFields.contains(CsvField.SubGenre) &&
                                    columnIndices[CsvField.SubGenre] != null &&
                                    columnIndices[CsvField.SubGenre]!! in record.indices
                                )
                                    record[columnIndices[CsvField.SubGenre]!!]
                                else existingItem.subGenre,
                                cut = if (overwriteFields.contains(CsvField.Cut) &&
                                    columnIndices[CsvField.Cut] != null &&
                                    columnIndices[CsvField.Cut]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Cut]!!]
                                else existingItem.cut,
                                quantity = if (mappingOptions.syncTins && tinDataList.isNotEmpty()) {
                                    calculateSyncTinsQuantity(tinDataList)
                                } else if (overwriteFields.contains(CsvField.Quantity) &&
                                    columnIndices[CsvField.Quantity] != null &&
                                    columnIndices[CsvField.Quantity]!! in record.indices
                                ) {
                                    if ((record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1) > 99) 0
                                    else record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1
                                } else existingItem.quantity,
                                syncTins = if (mappingOptions.syncTins && tinDataList.isNotEmpty()) true else existingItem.syncTins,
                                rating = if (overwriteFields.contains(CsvField.Rating) &&
                                    columnIndices[CsvField.Rating] != null &&
                                    mappingOptions.maxValue != null &&
                                    columnIndices[CsvField.Rating]!! in record.indices
                                )
                                    parseRating(record[columnIndices[CsvField.Rating]!!], mappingOptions.maxValue)
                                else existingItem.rating,
                                favorite = if (overwriteFields.contains(CsvField.Favorite) &&
                                    columnIndices[CsvField.Favorite] != null &&
                                    columnIndices[CsvField.Favorite]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Favorite]!!].toBoolean()
                                else existingItem.favorite,
                                disliked = if (overwriteFields.contains(CsvField.Disliked) &&
                                    columnIndices[CsvField.Disliked] != null &&
                                    columnIndices[CsvField.Disliked]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Disliked]!!].toBoolean()
                                else existingItem.disliked,
                                inProduction = if (overwriteFields.contains(CsvField.Production) &&
                                    columnIndices[CsvField.Production] != null &&
                                    columnIndices[CsvField.Production]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Production]!!].toBoolean()
                                else existingItem.inProduction,
                                notes = if (overwriteFields.contains(CsvField.Notes) &&
                                    columnIndices[CsvField.Notes] != null &&
                                    columnIndices[CsvField.Notes]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Notes]!!]
                                else existingItem.notes,
                                lastModified = existingItem.lastModified
                            )

                            val existingComponents = withContext(Dispatchers.IO) {
                                itemsRepository.getComponentsForItemStream(existingItem.id).first().map {
                                    it.componentName
                                }
                            }
                            var compsAdded = false
                            if (overwriteFields.contains(CsvField.Components) &&
                                columnIndices[CsvField.Components] != null &&
                                columnIndices[CsvField.Components]!! in record.indices
                            ) {
                                withContext(Dispatchers.IO) {
                                    itemsRepository.deleteComponentsCrossRefByItemId(existingItem.id)
                                }
                                componentsList.forEach {
                                    var componentId = itemsRepository.getComponentIdByName(it.componentName)
                                    if (componentId == null) {
                                        componentId = itemsRepository.insertComponent(it).toInt()
                                    }
                                    itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = existingItem.id, componentId = componentId))
                                }

                                val insertedComponents = withContext(Dispatchers.IO) {
                                    itemsRepository.getComponentsForItemStream(existingItem.id).first().map {
                                        it.componentName
                                    }
                                }
                                if (insertedComponents != existingComponents) {
                                    compsAdded = true
                                }
                            }

                            val existingFlavoring = withContext(Dispatchers.IO) {
                                itemsRepository.getFlavoringForItemStream(existingItem.id).first().map {
                                    it.flavoringName
                                }
                            }
                            var flavorAdded = false
                            if (overwriteFields.contains(CsvField.Flavoring) &&
                                columnIndices[CsvField.Flavoring] != null &&
                                columnIndices[CsvField.Flavoring]!! in record.indices
                            ) {
                                withContext(Dispatchers.IO) {
                                    itemsRepository.deleteFlavoringCrossRefByItemId(existingItem.id)
                                }
                                flavoringList.forEach {
                                    var flavoringId = itemsRepository.getFlavoringIdByName(it.flavoringName)
                                    if (flavoringId == null) {
                                        flavoringId = itemsRepository.insertFlavoring(it).toInt()
                                    }
                                    itemsRepository.insertFlavoringCrossRef(ItemsFlavoringCrossRef(itemId = existingItem.id, flavoringId = flavoringId))
                                }

                                val insertedFlavoring = withContext(Dispatchers.IO) {
                                    itemsRepository.getFlavoringForItemStream(existingItem.id)
                                        .first().map {
                                            it.flavoringName
                                        }
                                }
                                if (insertedFlavoring != existingFlavoring) {
                                    flavorAdded = true
                                }
                            }

                            if (collateTins) {
                                if (existingTins.isEmpty()) {
                                    if (tinDataList.isNotEmpty()) {
                                        insertTins(existingItem.id, tinDataList)
                                        tinDataList.forEach { _ -> addedTins++ }
                                    }
                                }
                            }

                            if (existingItem != updatedItem) {
                                updatedCount++
                                val updated = updatedItem.copy(lastModified = System.currentTimeMillis())
                                withContext(Dispatchers.IO) { itemsRepository.updateItem(updated) }
                            }
                            if (existingItem == updatedItem && (compsAdded || flavorAdded)) updatedCount++

                            null
                        }

                        else -> null
                    }
                } else { // Default Import Option SKIP and add new records for above options
                    Items(
                        brand = brand,
                        blend = blend,
                        type = if (columnIndices[CsvField.Type] != null &&
                            columnIndices[CsvField.Type]!! >= 0 &&
                            columnIndices[CsvField.Type]!! < record.size
                        ) {
                            val typeFromCsv = record[columnIndices[CsvField.Type]!!]
                            when (typeFromCsv.uppercase()) {
                                "AROMATIC", "ENGLISH", "BURLEY", "VIRGINIA", "OTHER" -> {
                                    typeFromCsv.lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                } else -> ""
                            }
                        } else "",
                        subGenre = if (columnIndices[CsvField.SubGenre] != null &&
                            columnIndices[CsvField.SubGenre]!! >= 0 &&
                            columnIndices[CsvField.SubGenre]!! < record.size
                        )
                            record[columnIndices[CsvField.SubGenre]!!] else "",
                        cut = if (columnIndices[CsvField.Cut] != null &&
                            columnIndices[CsvField.Cut]!! >= 0 &&
                            columnIndices[CsvField.Cut]!! < record.size
                        )
                            record[columnIndices[CsvField.Cut]!!] else "",
                        quantity = if (mappingOptions.syncTins && tinDataList.isNotEmpty()) {
                            calculateSyncTinsQuantity(tinDataList)
                        }
                            else if (columnIndices[CsvField.Quantity] != null &&
                            columnIndices[CsvField.Quantity]!! >= 0 &&
                            columnIndices[CsvField.Quantity]!! < record.size
                        ) {
                            if ((record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1) > 99) 0
                            else record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1
                        } else 1,
                        syncTins = mappingOptions.syncTins && tinDataList.isNotEmpty(),
                        rating = if (columnIndices[CsvField.Rating] != null &&
                            columnIndices[CsvField.Rating]!! >= 0 &&
                            mappingOptions.maxValue != null &&
                            columnIndices[CsvField.Rating]!! < record.size
                        )
                            parseRating(record[columnIndices[CsvField.Rating]!!], mappingOptions.maxValue) else null,
                        favorite = if (columnIndices[CsvField.Favorite] != null &&
                            columnIndices[CsvField.Favorite]!! >= 0 &&
                            columnIndices[CsvField.Favorite]!! < record.size
                        )
                            record[columnIndices[CsvField.Favorite]!!].toBoolean() else false,
                        disliked = if (columnIndices[CsvField.Disliked] != null &&
                            columnIndices[CsvField.Disliked]!! >= 0 &&
                            columnIndices[CsvField.Disliked]!! < record.size
                        )
                            record[columnIndices[CsvField.Disliked]!!].toBoolean() else false,
                        inProduction = if (columnIndices[CsvField.Production] != null &&
                            columnIndices[CsvField.Production]!! >= 0 &&
                            columnIndices[CsvField.Production]!! < record.size
                        )
                            record[columnIndices[CsvField.Production]!!].toBoolean() else true,
                        notes = if (columnIndices[CsvField.Notes] != null &&
                            columnIndices[CsvField.Notes]!! >= 0 &&
                            columnIndices[CsvField.Notes]!! < record.size
                        )
                            record[columnIndices[CsvField.Notes]!!] else "",
                        lastModified = System.currentTimeMillis()
                    )
                }
            }
        }

        var insertions = 0

        try {
            val insertedIds = withContext(Dispatchers.IO) { itemsRepository.insertMultipleItems(itemsToImport) }

            withContext(Dispatchers.IO) {
                itemsToImport.forEachIndexed { index, items ->
                    val itemId = insertedIds[index].toInt()

                    val comps = if (
                        columnIndices[CsvField.Components] != null &&
                        columnIndices[CsvField.Components]!!
                        in recordsToImport[index].indices
                    ) recordsToImport[index][columnIndices[CsvField.Components]!!] else ""
                    val components = componentSplitter(comps)

                    components.forEach {
                        var componentId = itemsRepository.getComponentIdByName(it.componentName)
                        if (componentId == null) {
                            componentId = itemsRepository.insertComponent(it).toInt()
                        }
                        if (insertedIds[index] != -1L) {
                            itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = itemId, componentId = componentId))
                        }
                    }

                    val flavor = if (
                        columnIndices[CsvField.Flavoring] != null &&
                        columnIndices[CsvField.Flavoring]!!
                        in recordsToImport[index].indices
                    ) recordsToImport[index][columnIndices[CsvField.Flavoring]!!] else ""
                    val flavoring = flavorSplitter(flavor)

                    flavoring.forEach {
                        var flavoringId = itemsRepository.getFlavoringIdByName(it.flavoringName)
                        if (flavoringId == null) {
                            flavoringId = itemsRepository.insertFlavoring(it).toInt()
                        }
                        if (insertedIds[index] != -1L) {
                            itemsRepository.insertFlavoringCrossRef(ItemsFlavoringCrossRef(itemId = itemId, flavoringId = flavoringId))
                        }
                    }

                    if (itemId != -1) { insertions++ }
                    val brandBlendKey = Pair(items.brand, items.blend)
                    val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()

                    if (insertedIds[index] != -1L) {
                        if (collateTins) {
                            insertTins(itemId, tinDataList)
                            tinDataList.forEach { _ -> addedTins++ }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _importStatus.value = ImportStatus.Error(e)
        } finally {
            SyncStateManager.schedulingPaused = false
            itemsRepository.triggerUploadWorker()

            val successfulConversions = itemsToImport.size + updatedConversions
            val successfulInsertions = insertions
            val successfulUpdates = updatedCount
            val successfulTins = addedTins
            val updateFlag = updatedFlagSet
            val tinFlag = tinsFlagSet
            val totalRecords =
                if (hasHeader) {
                    csvImportState.value.recordCount - 1
                } else {
                    csvImportState.value.recordCount
                }

            delay(1500)

            _importStatus.value = ImportStatus.Success(
                totalRecords = totalRecords,
                successfulConversions = successfulConversions,
                successfulInsertions = successfulInsertions,
                successfulUpdates = successfulUpdates,
                successfulTins = successfulTins,
                updateFlag = updateFlag,
                tinFlag = tinFlag
            )
            _navigateToResults.emit(
                ImportResults(
                    totalRecords = totalRecords,
                    successfulConversions = successfulConversions,
                    successfulInsertions = successfulInsertions,
                    successfulUpdates = successfulUpdates,
                    successfulTins = successfulTins,
                    updateFlag = updateFlag,
                    tinFlag = tinFlag
                )
            )
        }
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
    val header: List<String> = emptyList(),
    val testHeader: List<String> = emptyList(),
    val firstFullRecord: List<String> = emptyList(),
    val allRecords: List<List<String>> = emptyList(),
    val recordCount: Int = 0,
)

data class CsvUiState(
    val csvImportState: CsvImportState = CsvImportState(),
    val columns: List<String> = emptyList(),
    val isFormValid: Boolean = false,
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