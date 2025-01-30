package com.sardonicus.tobaccocellar.ui.csvimport

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat
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
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate(13)
        }
        val updatedFirstFullRecord = firstFullRecord.mapIndexed { index, value ->
            if (value.isBlank()) "[Column ${index + 1}]" else value.truncate(13)
        }

        _csvImportState.value = CsvImportState(
            header = updatedHeader.toList(),
            firstFullRecord = updatedFirstFullRecord.toList(),
            allRecords = allRecords.toList(),
            recordCount = recordCount
        )
    }

    private fun String.truncate(maxLength: Int): String {
        return if (length > maxLength) {
            substring(0, minOf(10, maxLength - 3)) + "..."
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

    fun updateHeaderOptions(hasHeader: Boolean) {
        mappingOptions = mappingOptions.copy(hasHeader = hasHeader)
    }

    fun updateCollateTinsOption(collateTins: Boolean) {
        mappingOptions = mappingOptions.copy(collateTins = collateTins)
    }

    fun updateDateFormat(dateFormat: String) {
        mappingOptions = mappingOptions.copy(dateFormat = dateFormat)
    }

    enum class CsvField {
        Brand,
        Blend,
        Type,
        Quantity,
        Favorite,
        Disliked,
        Notes,
        SubGenre,
        Cut,
        Production,
        Components,
        Container,
        TinQuantity,
        ManufactureDate,
        CellarDate,
        OpenDate,
    }

    fun updateMappingOptions(field: CsvField, selectedColumn: String) {
        mappingOptions = when (field) {
            CsvField.Brand -> mappingOptions.copy(brandColumn = selectedColumn.ifBlank { "" })
            CsvField.Blend -> mappingOptions.copy(blendColumn = selectedColumn.ifBlank { "" })
            CsvField.Type -> mappingOptions.copy(typeColumn = selectedColumn.ifBlank { "" })
            CsvField.Quantity -> mappingOptions.copy(quantityColumn = selectedColumn.ifBlank { "" })
            CsvField.Favorite -> mappingOptions.copy(favoriteColumn = selectedColumn.ifBlank { "" })
            CsvField.Disliked -> mappingOptions.copy(dislikedColumn = selectedColumn.ifBlank { "" })
            CsvField.Notes -> mappingOptions.copy(notesColumn = selectedColumn.ifBlank { "" })
            CsvField.SubGenre -> mappingOptions.copy(subGenreColumn = selectedColumn.ifBlank { "" })
            CsvField.Cut -> mappingOptions.copy(cutColumn = selectedColumn.ifBlank { "" })
            CsvField.Production -> mappingOptions.copy(productionColumn = selectedColumn.ifBlank { "" })
            CsvField.Components -> mappingOptions.copy(componentsColumn = selectedColumn.ifBlank { "" })
            CsvField.Container -> mappingOptions.copy(containerColumn = selectedColumn.ifBlank { "" })
            CsvField.TinQuantity -> mappingOptions.copy(tinQuantityColumn = selectedColumn.ifBlank { "" })
            CsvField.ManufactureDate -> mappingOptions.copy(manufactureDateColumn = selectedColumn.ifBlank { "" })
            CsvField.CellarDate -> mappingOptions.copy(cellarDateColumn = selectedColumn.ifBlank { "" })
            CsvField.OpenDate -> mappingOptions.copy(openDateColumn = selectedColumn.ifBlank { "" })

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
        _overwriteSelections.value = _overwriteSelections.value + (field to overwrite)
    }


    /** Confirm and import **/
    // components handling //
    private fun componentSplitter(components: String): List<String> {
        return components.split(",").map { it.trim() }
    }

    private suspend fun insertComponents(components: List<String>) {
        val normalizedComponents = components.map { it.lowercase() }
        val existingComps = withContext(Dispatchers.IO) { itemsRepository.getAllComponentsStream().first() }
        val normalizedExistingComps = existingComps.map { it.componentName.lowercase() }
        val newComponents = normalizedComponents.filter { csvComp ->
            normalizedExistingComps.none {
                it == csvComp
            }
        }

        if (newComponents.isNotEmpty()) {
            val componentsToAdd = newComponents.map { component ->
                Components(
                    componentName = components.find {
                        it.lowercase() == component } ?: component
                )
            }

            withContext(Dispatchers.IO) {
                itemsRepository.insertMultipleComponents(componentsToAdd)
            }
        }
    }

    private suspend fun insertComponentsCrossRef(components: List<String>, itemId: Int) {
        val componentIds = withContext(Dispatchers.IO) {
            itemsRepository.getComponentsByName(components.map { it.lowercase() }).first().map { it.componentId}
        }
        val crossReferences = componentIds.map {
            ItemsComponentsCrossRef(itemId, it)
        }

        withContext(Dispatchers.IO) {
            itemsRepository.insertMultipleComponentsCrossRef(crossReferences)
        }
    }


    // tin collation handling //
    private fun generateTinLabels(numTins: Int, startingLabelNumber: Int = 1): List<String> {
        return (startingLabelNumber until startingLabelNumber + numTins).map { "Lot $it" }
    }

    private fun String.parseTinQuantity(): Pair<Double, String> {
        val regex = Regex("""(\d+(?:\.\d+)?)\s?(.*)""")
        val matchResult = regex.find(this)

        return if (matchResult != null) {
            val quantity = matchResult.groupValues[1].toDoubleOrNull() ?: 0.0
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
        } catch (e: Exception) {
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
                manufactureDate = null,
                cellarDate = null,
                openDate = null
            )
        }
        withContext(Dispatchers.IO) {
            itemsRepository.insertMultipleTins(tinsToInsert)
        }
    }

    suspend fun calculateSyncTinsQuantity(tinDataList: List<TinData>): Int {
        val ozRate = preferencesRepo.getTinOzConversionRate()
        val gramsRate = preferencesRepo.getTinGramsConversionRate()

        val totalLbsTins = tinDataList.filter { it.unit == "lbs" }.sumOf {
            (it.quantity * 16) / ozRate }
        val totalOzTins = tinDataList.filter { it.unit == "oz" }.sumOf {
            it.quantity / ozRate }
        val totalGramsTins = tinDataList.filter { it.unit == "grams" }.sumOf {
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
            brandColumn.isNotBlank() && blendColumn.isNotBlank()
        }
    }

    fun confirmImport() = viewModelScope.launch {
        _importStatus.value = ImportStatus.Loading

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
                            columnIndices[CsvField.Container]!! in record.indices) {
                            record[columnIndices[CsvField.Container]!!].trim()
                        } else ""
                    val (quantity, unit) =
                        if(columnIndices[CsvField.TinQuantity] != null &&
                            columnIndices[CsvField.TinQuantity]!! in record.indices) {
                            record[columnIndices[CsvField.TinQuantity]!!].trim().parseTinQuantity()
                        } else { Pair(0.0, "") }
                    val manufactureDate =
                        if(columnIndices[CsvField.ManufactureDate] != null &&
                            columnIndices[CsvField.ManufactureDate]!! in record.indices) {
                            val dateString = record[columnIndices[CsvField.ManufactureDate]!!].trim()
                            val dateFormat = mappingOptions.dateFormat
                            parseDateString(dateString, dateFormat)
                        } else null
                    val cellarDate =
                        if(columnIndices[CsvField.CellarDate] != null &&
                            columnIndices[CsvField.CellarDate]!! in record.indices) {
                            val dateString = record[columnIndices[CsvField.CellarDate]!!].trim()
                            val dateFormat = mappingOptions.dateFormat
                            parseDateString(dateString, dateFormat)
                        } else null
                    val openDate =
                        if(columnIndices[CsvField.OpenDate] != null &&
                            columnIndices[CsvField.OpenDate]!! in record.indices) {
                            val dateString = record[columnIndices[CsvField.OpenDate]!!].trim()
                            val dateFormat = mappingOptions.dateFormat
                            parseDateString(dateString, dateFormat)
                        } else null


                    TinData(
                        label = tinLabels[index],
                        container = container,
                        quantity = quantity,
                        unit = unit,
                        manufactureDate = manufactureDate,
                        cellarDate = cellarDate,
                        openDate = openDate,
                    )
                }
            } else emptyList()
            tinDataMap.getOrPut(brandBlendKey) { mutableListOf() }.addAll(tinDataList)
        }

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

            val brandBlendKey = Pair(brand, blend)
            val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()

            if (brand.isBlank() || blend.isBlank()) {
                null
            } else {
                val existingItem = withContext(Dispatchers.IO) {
                    itemsRepository.getItemByIndex(brand, blend)
                }
                val componentsList = componentSplitter(components)

                if (existingItem != null) {
                    updatedConversions++
                    when (importOption) {
                        ImportOption.UPDATE -> {
                            updatedFlagSet = true
                            val brandBlendKey = Pair(existingItem.brand, existingItem.blend)
                            val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()

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
                                quantity = if (collateTins)
                                    calculateSyncTinsQuantity(tinDataList)
                                    else existingItem.quantity,
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
                                notes = if (existingItem.notes.isBlank() &&
                                    columnIndices[CsvField.Notes] != null &&
                                    columnIndices[CsvField.Notes]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Notes]!!]
                                else existingItem.notes,
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
                            )
                            val existingComponents = withContext(Dispatchers.IO) {
                                itemsRepository.getComponentsForItemStream(existingItem.id).first().map {
                                    it.componentName
                                }
                            }
                            if (existingComponents.isEmpty() && componentsList.isNotEmpty()) {
                                insertComponents(componentsList)
                                insertComponentsCrossRef(componentsList, existingItem.id)
                            }

                            val existingTins = withContext(Dispatchers.IO) {
                                itemsRepository.getTinsForItemStream(existingItem.id).first()
                            }

                            var tinsAddedToItem = false
                            if (collateTins) {
                                if (existingTins.isEmpty()) {
                                    if (tinDataList.isNotEmpty()) {
                                        tinsAddedToItem = true
                                        insertTins(existingItem.id, tinDataList)
                                        tinDataList.forEach { _ -> addedTins++ }
                                        preferencesRepo.setItemSyncState(existingItem.id, true)
                                    }
                                }
                            }

                            withContext(Dispatchers.IO) {
                                itemsRepository.updateItem(updatedItem)
                            }
                            if (existingItem != updatedItem || tinsAddedToItem) updatedCount++
                            tinsAddedToItem = false
                            null
                        }

                        ImportOption.OVERWRITE -> {
                            updatedFlagSet = true
                            val overwriteFields = overwriteSelections.value.filterValues { it }.keys
                            val brandBlendKey = Pair(existingItem.brand, existingItem.blend)
                            val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()

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
                                quantity = if (overwriteFields.contains(CsvField.Quantity) &&
                                    columnIndices[CsvField.Quantity] != null &&
                                    columnIndices[CsvField.Quantity]!! in record.indices
                                ) {
                                    if ((record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1) > 99
                                    ) 0
                                    else record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1
                                } else if (collateTins) {
                                    calculateSyncTinsQuantity(tinDataList)
                                }
                                else existingItem.quantity,
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
                                notes = if (overwriteFields.contains(CsvField.Notes) &&
                                    columnIndices[CsvField.Notes] != null &&
                                    columnIndices[CsvField.Notes]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Notes]!!]
                                else existingItem.notes,
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
                                inProduction = if (overwriteFields.contains(CsvField.Production) &&
                                    columnIndices[CsvField.Production] != null &&
                                    columnIndices[CsvField.Production]!! in record.indices
                                )
                                    record[columnIndices[CsvField.Production]!!].toBoolean()
                                else existingItem.inProduction,
                            )

                            if (overwriteFields.contains(CsvField.Components) &&
                                columnIndices[CsvField.Components] != null &&
                                columnIndices[CsvField.Components]!! in record.indices
                            ) {
                                withContext(Dispatchers.IO) {
                                    itemsRepository.deleteComponentsCrossRefByItemId(existingItem.id)
                                }
                                insertComponents(componentsList)
                                insertComponentsCrossRef(componentsList, existingItem.id)
                            }

                            val existingTins = withContext(Dispatchers.IO) {
                                itemsRepository.getTinsForItemStream(existingItem.id).first()
                            }

                            var tinsAddedToItem = false
                            if (collateTins) {
                                if (existingTins.isEmpty()) {
                                    if (tinDataList.isNotEmpty()) {
                                        tinsAddedToItem = true
                                        insertTins(existingItem.id, tinDataList)
                                        tinDataList.forEach { _ -> addedTins++ }
                                        preferencesRepo.setItemSyncState(existingItem.id, true)
                                    }
                                }
                            }

                            withContext(Dispatchers.IO) {
                                itemsRepository.updateItem(updatedItem)
                            }
                            if (existingItem != updatedItem || tinsAddedToItem) updatedCount++
                            tinsAddedToItem = false
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
                        quantity = if (columnIndices[CsvField.Quantity] != null &&
                            columnIndices[CsvField.Quantity]!! >= 0 &&
                            columnIndices[CsvField.Quantity]!! < record.size
                        ) {
                            if ((record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1) > 99) 0
                            else record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1
                        } else if (collateTins) {
                            calculateSyncTinsQuantity(tinDataList)
                        } else 1,
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
                        notes = if (columnIndices[CsvField.Notes] != null &&
                            columnIndices[CsvField.Notes]!! >= 0 &&
                            columnIndices[CsvField.Notes]!! < record.size
                        )
                            record[columnIndices[CsvField.Notes]!!] else "",
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
                        inProduction = if (columnIndices[CsvField.Production] != null &&
                            columnIndices[CsvField.Production]!! >= 0 &&
                            columnIndices[CsvField.Production]!! < record.size
                        )
                            record[columnIndices[CsvField.Production]!!].toBoolean() else true
                    )
                }
            }
        }
        try {
            val insertedIds = withContext(Dispatchers.IO) {
                itemsRepository.insertMultipleItems(itemsToImport) }

            withContext(Dispatchers.IO) {
                itemsToImport.forEachIndexed { index, items ->
                    val comps = if (
                        columnIndices[CsvField.Components] != null &&
                        columnIndices[CsvField.Components]!!
                        in csvImportState.value.allRecords[index].indices) {
                            csvImportState.value.allRecords[index][columnIndices[CsvField.Components]!!]
                        } else ""
                    val components = componentSplitter(comps)
                    insertComponents(components)

                    val itemId = insertedIds[index].toInt()
                    val brandBlendKey = Pair(items.brand, items.blend)
                    val tinDataList = tinDataMap[brandBlendKey] ?: emptyList()

                    if (insertedIds[index] != -1L) {
                        insertComponentsCrossRef(components, insertedIds[index].toInt())
                        insertTins(itemId, tinDataList)
                        tinDataList.forEach { _ -> addedTins++ }
                        preferencesRepo.setItemSyncState(insertedIds[index].toInt(), true)
                    }
                }
            }
            val successfulConversions = itemsToImport.size + updatedConversions
            val successfulInsertions = insertedIds.count { it != -1L }
            val successfulUpdates = updatedCount
            val successfulTins = addedTins
            val updateFlag = updatedFlagSet
            val tinFlag = tinsFlagSet
            val totalRecords =
                if (hasHeader) { csvImportState.value.recordCount - 1 }
                else { csvImportState.value.recordCount }

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
        } catch (e: Exception) {
            _importStatus.value = ImportStatus.Error(e)
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
            CsvField.Favorite to header.indexOf(mappingOptions.favoriteColumn),
            CsvField.Disliked to header.indexOf(mappingOptions.dislikedColumn),
            CsvField.Notes to header.indexOf(mappingOptions.notesColumn),
            CsvField.SubGenre to header.indexOf(mappingOptions.subGenreColumn),
            CsvField.Cut to header.indexOf(mappingOptions.cutColumn),
            CsvField.Production to header.indexOf(mappingOptions.productionColumn),
            CsvField.Components to header.indexOf(mappingOptions.componentsColumn),
            CsvField.Container to header.indexOf(mappingOptions.containerColumn),
            CsvField.TinQuantity to header.indexOf(mappingOptions.tinQuantityColumn),
            CsvField.ManufactureDate to header.indexOf(mappingOptions.manufactureDateColumn),
            CsvField.CellarDate to header.indexOf(mappingOptions.cellarDateColumn),
            CsvField.OpenDate to header.indexOf(mappingOptions.openDateColumn),
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
    val brandColumn: String = "",
    val blendColumn: String = "",
    val typeColumn: String = "",
    val quantityColumn: String = "",
    val favoriteColumn: String = "",
    val dislikedColumn: String = "",
    val notesColumn: String = "",
    val subGenreColumn: String = "",
    val cutColumn: String = "",
    val productionColumn: String = "",
    val componentsColumn: String = "",
    val containerColumn: String = "",
    val tinQuantityColumn: String = "",
    val dateFormat: String = "",
    val manufactureDateColumn: String = "",
    val cellarDateColumn: String = "",
    val openDateColumn: String = "",
)

data class TinData(
    val label: String,
    val container: String,
    val quantity: Double,
    val unit: String,
    val manufactureDate: Long? = null,
    val cellarDate: Long? = null,
    val openDate: Long? = null,
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

