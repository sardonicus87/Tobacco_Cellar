package com.sardonicus.tobaccocellar.ui.home.plaintext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.home.calculateTotalQuantity
import com.sardonicus.tobaccocellar.ui.home.formatDecimal
import com.sardonicus.tobaccocellar.ui.home.formatQuantity
import com.sardonicus.tobaccocellar.ui.items.formatMediumDate
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaintextViewModel (
    filterViewModel: FilterViewModel,
    val preferencesRepo: PreferencesRepo
) : ViewModel() {

    private val _sortState = MutableStateFlow(PlaintextSortOption())
    val sortState: StateFlow<PlaintextSortOption> = _sortState.asStateFlow()

    private val _setTemplateView = MutableStateFlow(false)
    val setTemplateView: StateFlow<Boolean> = _setTemplateView.asStateFlow()

    private val _formatStringEntry = MutableStateFlow("")
    val formatStringEntry: StateFlow<String> = _formatStringEntry.asStateFlow()

    private val _delimiter = MutableStateFlow("")
    val delimiter: StateFlow<String> = _delimiter.asStateFlow()

    private val _subSortOption = MutableStateFlow("")
    val subSortOption: StateFlow<String> = _subSortOption.asStateFlow()

    private val _printOptions = MutableStateFlow(PrintOptions())
    val printOptions: StateFlow<PrintOptions> = _printOptions.asStateFlow()


    init {
        viewModelScope.launch {
            combine(
                preferencesRepo.plaintextSorting,
                preferencesRepo.plaintextSortAscending,
                preferencesRepo.plaintextSubSorting
            ) { sorting, ascending, subSorting ->
                PlaintextSortOption(
                    value = sorting,
                    ascending = ascending,
                    subSort = subSorting
                )
            }.collect {
                _sortState.value = it
            }
        }
        viewModelScope.launch {
            val format = preferencesRepo.plaintextFormatString.first()
            val delimiter = preferencesRepo.plaintextDelimiter.first()

            if (format.isNotBlank() || delimiter.isNotBlank()) {
                saveFormatString(format, delimiter)
            }
        }
        viewModelScope.launch {
            combine(
                preferencesRepo.plaintextPrintFontSize,
                preferencesRepo.plaintextPrintMargin
            ) { font, margin ->
                PrintOptions(font, margin)
            }.collect {
                _printOptions.value = it
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val uiState = combine(
        filterViewModel.unifiedFilteredItems,
        filterViewModel.unifiedFilteredTins,
        preferencesRepo.quantityOption,
        preferencesRepo.plaintextFormatString,
        preferencesRepo.plaintextDelimiter,
        sortState,
        subSortOption,
        preferencesRepo.plaintextPresetsFlow
    ) {
        val filteredItems = it[0] as List<ItemsComponentsAndTins>
        val filteredTins = it[1] as List<Tins>
        val quantityOption = it[2] as QuantityOption
        val formatString = it[3] as String
        val delimiter = it[4] as String
        val sortState = it[5] as PlaintextSortOption
        val subSortOption = it[6] as String
        val presets = it[7] as List<PlaintextPreset>

        val ozRate = preferencesRepo.tinOzConversionRate.first()
        val gramsRate = preferencesRepo.tinGramsConversionRate.first()

        val sortQuantity = filteredItems.associate {
            it.items.id to calculateTotalQuantity(it, it.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate)
        }
        val formattedQuantities = filteredItems.associate {
            val totalQuantity = calculateTotalQuantity(it, it.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate)
            val formattedQuantity = formatQuantity(totalQuantity, quantityOption, it.tins.filter { it in filteredTins })
            it.items.id to formattedQuantity
        }
        val tinSubSort: (Tins) -> Comparable<*> = {
            val parentItem = filteredItems.firstOrNull { item -> item.items.id == it.itemsId }
            if (parentItem != null) {
                when (subSortOption) {
                    PlaintextSortOption.DEFAULT.value -> parentItem.items.id
                    PlaintextSortOption.TIN_DEFAULT.value -> it.tinId
                    PlaintextSortOption.BRAND.value -> parentItem.items.brand
                    PlaintextSortOption.BLEND.value -> parentItem.items.blend
                    else -> parentItem.items.id
                }
            } else {
                it.tinId
            }
        }
        val tinQuantitySorting = filteredTins.associateWith { tinNormalizedWeight(it) }

//        val itemsSubSort: (ItemsComponentsAndTins) -> Comparable<*> = when (subSortOption) {
//            PlaintextSortOption.DEFAULT.value -> { it -> it.items.id }
//            PlaintextSortOption.BRAND.value -> { it -> it.items.brand }
//            PlaintextSortOption.BLEND.value -> { it -> it.items.blend }
//            else -> { it -> it.items.id }
//        }

        val sortedItems =  if (filteredItems.isNotEmpty()) {
            when (sortState.value) {
                PlaintextSortOption.DEFAULT.value -> filteredItems.sortedBy { it.items.id }
                PlaintextSortOption.BRAND.value -> filteredItems.sortedBy { it.items.brand }
                PlaintextSortOption.BLEND.value -> filteredItems.sortedBy { it.items.blend }
                PlaintextSortOption.TYPE.value -> filteredItems.sortedBy { it.items.type }
                PlaintextSortOption.SUBGENRE.value -> filteredItems.sortedBy { it.items.subGenre }
                PlaintextSortOption.CUT.value -> filteredItems.sortedBy { it.items.cut }
                PlaintextSortOption.QUANTITY.value -> filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                else -> filteredItems.sortedBy { it.items.id }
            }.let{
                if (sortState.ascending) { it } else { it.reversed() }
            }
        } else emptyList()
        val sortedTins = if (filteredTins.isNotEmpty()) {
            when (sortState.value) {
                PlaintextSortOption.TIN_LABEL.value -> filteredTins.sortedWith (
                    compareBy<Tins> { it.tinLabel }.thenBy { tinSubSort(it) }
                )
                PlaintextSortOption.TIN_CONTAINER.value -> filteredTins.sortedWith(
                    compareBy<Tins> { it.container.ifBlank { "~" } }.thenBy { tinSubSort(it) }
                )
                PlaintextSortOption.TIN_QUANTITY.value -> filteredTins.sortedWith(
                    compareByDescending<Tins> { tinQuantitySorting[it] }.thenBy { tinSubSort(it) }
                )
                else -> filteredTins.sortedBy { it.itemsId }
            }.let {
                if (sortState.ascending) { it } else { it.reversed() }
            }
        } else emptyList()

        val sortOptions = if (formatString.isNotBlank()) {
            val options = mutableListOf(PlaintextSortOption.DEFAULT)
            val itemOptionMap = mapOf(
                "@brand" to PlaintextSortOption.BRAND,
                "@blend" to PlaintextSortOption.BLEND,
                "@type" to PlaintextSortOption.TYPE,
                "@subgenre" to PlaintextSortOption.SUBGENRE,
                "@cut" to PlaintextSortOption.CUT,
                "@qty" to PlaintextSortOption.QUANTITY
            )
            val tinOptionMap = mapOf(
                "@label" to PlaintextSortOption.TIN_LABEL,
                "@container" to PlaintextSortOption.TIN_CONTAINER,
                "@T_qty" to PlaintextSortOption.TIN_QUANTITY
            )

            itemOptionMap.forEach { if (formatString.contains(it.key)) options.add(it.value) }

            val tinsSublist = Regex("""\{(.*?)\}""")
            val fsRemovedSublist = formatString.replace(tinsSublist, "")
            val validTinOptions = mutableListOf<PlaintextSortOption>()
            tinOptionMap.forEach {
                if (fsRemovedSublist.contains(it.key)) {
                    validTinOptions.add(it.value)
                }
            }
            if (validTinOptions.isNotEmpty()) {
                options.add(PlaintextSortOption.TIN_DEFAULT)
                options.addAll(validTinOptions)
            }

            options.distinctBy { it.value }
        } else emptyList()

        val formatGuide = mapOf(
            "Brand" to "@brand",
            "Blend" to "@blend",
            "Type" to "@type",
            "Subgenre" to "@subgenre",
            "Cut" to "@cut",
            "Components" to "@comps",
            "Flavoring" to "@flavors",
            "Quantity" to "@qty",
            "Production" to "@prod",
            "Tin Label" to "@label",
            "Tin Container" to "@container",
            "Tin Quantity" to "@T_qty",
            "Manufacture" to "@manufacture",
            "Cellar Date" to "@cellar",
            "Open Date" to "@open",
            "Finished" to "@finished",
            "New Line" to "_n_",
            "Number" to "#",
            "Escape char" to "'",
            "If any" to "[...]",
            "Tin sublist" to "{...}",
            "Sublist delim." to "~"
        )

        val previewItems = listOf(
            Items(
                id = 1,
                brand = "Brand A",
                blend = "Blend 1",
                type = "Virginia",
                subGenre = "VA/per",
                cut = "flake",
                inProduction = true,
                quantity = 2,
                favorite = false,
                disliked = false,
                notes = "",
            ),
            Items(
                id = 2,
                brand = "Brand A",
                blend = "Blend 2",
                type = "Burley",
                subGenre = "",
                cut = "ribbon",
                inProduction = true,
                quantity = 1,
                favorite = true,
                disliked = false,
                notes = "",
            ),
            Items(
                id = 3,
                brand = "Brand B",
                blend = "Blend 1",
                type = "English",
                subGenre = "Balkan",
                cut = "ribbon",
                inProduction = false,
                quantity = 1,
                favorite = false,
                disliked = false,
                notes = "note",
            )
        )
        val previewTins = listOf(
            Tins(
                tinId = 1,
                itemsId = 1,
                tinLabel = "Lot 1",
                container = "jar",
                tinQuantity = 1.75,
                unit = "oz", // grams
                manufactureDate = 1704175200000,
                cellarDate = 1704261600000,
                openDate = 1704348000000,
                finished = true
            ),
            Tins(
                tinId = 2,
                itemsId = 1,
                tinLabel = "Lot 2",
                container = "original tin",
                tinQuantity = 50.00,
                unit = "grams", // grams
                manufactureDate = 1704175200000,
                cellarDate = 1704261600000,
                openDate = null,
                finished = false
            ),
            Tins(
                tinId = 3,
                itemsId = 2,
                tinLabel = "Lot 1",
                container = "",
                tinQuantity = 0.00,
                unit = "", // grams
                manufactureDate = null,
                cellarDate = null,
                openDate = null,
                finished = false
            ),
        )
        val previewComponents = listOf(
            Components(
                componentId = 1,
                componentName = "virginia"
            ),
            Components(
                componentId = 2,
                componentName = "perique"
            ),
            Components(
                componentId = 3,
                componentName = "burley"
            )
        )
        val previewFlavoring = listOf(
            Flavoring(
                flavoringId = 1,
                flavoringName = "vanilla"
            ),
            Flavoring(
                flavoringId = 2,
                flavoringName = "anise"
            ),
        )
        val previewComponentCrossRef = listOf(
            ItemsComponentsCrossRef(
                itemId = 1,
                componentId = 1
            ),
            ItemsComponentsCrossRef(
                itemId = 1,
                componentId = 2
            ),
            ItemsComponentsCrossRef(
                itemId = 2,
                componentId = 3
            ),
        )
        val previewFlavoringCrossRef = listOf(
            ItemsFlavoringCrossRef(
                itemId = 3,
                flavoringId = 1
            ),
            ItemsFlavoringCrossRef(
                itemId = 2,
                flavoringId = 2
            ),
        )
        val previewData = previewItems.map { item ->
            val itemsComponents = previewComponents.filter {
                previewComponentCrossRef.any { ref -> ref.itemId == item.id && ref.componentId == it.componentId }
            }
            val itemsFlavoring = previewFlavoring.filter {
                previewFlavoringCrossRef.any { ref -> ref.itemId == item.id && ref.flavoringId == it.flavoringId }
            }
            ItemsComponentsAndTins(
                items = item,
                components = itemsComponents,
                flavoring = itemsFlavoring,
                tins = previewTins.filter { it.itemsId == item.id }
            )

        }
        val previewFormattedQuantities = previewData.associate {
            it.items.id to formatQuantity(calculateTotalQuantity(it, it.tins.filter { it in previewTins }, quantityOption, ozRate, gramsRate), quantityOption, it.tins.filter { it in previewTins })
        }

        val listString = generateListString(sortedItems, sortedTins, sortState, formattedQuantities, formatString, delimiter)
        val formatPreview = generateListString(previewData, previewTins, sortState, previewFormattedQuantities, formatString, delimiter)

        PlaintextUiState(
            formatString = formatString,
            delimiter = delimiter,
            preview = formatPreview,
            plainList = listString,
            formattedQuantities = formattedQuantities,
            sortState = sortState,
            sortOptions = sortOptions,
            formatGuide = formatGuide,
            presets = presets,
            loading = false,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PlaintextUiState(loading = true)
        )

    private fun tinNormalizedWeight(tin: Tins): Double {
        if (tin.finished) return 0.0
        if (tin.unit.isBlank()) return 0.0

        return when (tin.unit) {
            "oz" -> tin.tinQuantity * 28.3495
            "lbs" -> tin.tinQuantity * 453.592
            "grams" -> tin.tinQuantity
            else -> 0.0
        }
    }

    fun setTemplateView(set: Boolean) { _setTemplateView.value = set }

    fun updateSorting(option: String, reverseSwitch: Boolean) {
        viewModelScope.launch {
            val currentSort = _sortState.value
            val reverse = if (reverseSwitch) !currentSort.ascending else currentSort.ascending
            val newSort = if (currentSort.value == option) {
                PlaintextSortOption(value = option, reverse)
            } else {
                PlaintextSortOption(option)
            }
            preferencesRepo.setPlaintextSorting(newSort.value, newSort.ascending)
        }
    }

    fun updateSubSorting(option: String) {
        _subSortOption.value = option

        viewModelScope.launch {
            preferencesRepo.setPlaintextSubSorting(option)
        }
    }

    fun saveFormatString(format: String, delimiter: String = "") {
        _formatStringEntry.value = format
        _delimiter.value = delimiter

        viewModelScope.launch {
            preferencesRepo.setPlaintextFormatString(format)
            preferencesRepo.setPlaintextDelimiter(delimiter)
        }
    }

    fun savePreset(slot: Int, format: String, delimiter: String) {
        viewModelScope.launch {
            preferencesRepo.savePlaintextPreset(slot, format, delimiter)
        }
    }

    fun savePrintOptions(font: Float, margin: Double) {
        _printOptions.value = PrintOptions(font, margin)

        viewModelScope.launch {
            preferencesRepo.setPlaintextPrintOptions(font, margin)
        }
    }

    private fun generateListString(
        items: List<ItemsComponentsAndTins>,
        tins: List<Tins>,
        sortState: PlaintextSortOption,
        quantities: Map<Int, String>,
        formatString: String,
        delimiter: String,
    ): String {
        if (formatString.isBlank()) return ""

        val resultBuilder = StringBuilder()
        var entryCounter = 0
        val numPlaceholder = Regex("#+")

        val tinPlaceholders = listOf(
            "@label",
            "@container",
            "@T_qty",
            "@manufacture",
            "@cellar",
            "@open",
            "@finished"
        )
        val tinsSublistCheck = Regex("""\{(.*?)\}""")
        val fsRemovedSublist = tinsSublistCheck.replace(formatString, "")
        val containsTinCall = tinPlaceholders.any { fsRemovedSublist.contains(it) }

        val tinsPrimary = when (sortState.value) {
            PlaintextSortOption.TIN_LABEL.value,
            PlaintextSortOption.TIN_CONTAINER.value,
            PlaintextSortOption.TIN_QUANTITY.value -> true
            else -> false
        }

        if (!tinsPrimary) {
            // List by Items sorting
            for (item in items) {
                val itemTins = item.tins.filter { it in tins }

                if (containsTinCall) {
                    if (itemTins.isNotEmpty()) {
                        for (tin in itemTins) {
                            entryCounter++
                            resultBuilder.append(
                                processLine(
                                    formatString, delimiter, item, tin, tins, quantities, entryCounter, numPlaceholder
                                )
                            )
                        }
                    } else {
                        entryCounter++
                        resultBuilder.append(
                            processLine(
                                formatString, delimiter, item, null, tins, quantities, entryCounter, numPlaceholder
                            )
                        )
                    }
                } else {
                    entryCounter++
                    resultBuilder.append(
                        processLine(
                            formatString, delimiter, item, null, tins, quantities, entryCounter, numPlaceholder
                        )
                    )
                }
            }
        } else {
            // List by Tins sorting
            if (containsTinCall) {
                for (tin in tins) {
                    val tinItem = items.first { it.items.id == tin.itemsId }

                    entryCounter++
                    resultBuilder.append(
                        processLine(
                            formatString, delimiter, tinItem, tin, tins, quantities, entryCounter, numPlaceholder
                        )
                    )
                }
            } else {
                val uniqueItems = tins.mapNotNull { tin ->
                    items.firstOrNull { it.items.id == tin.itemsId } }.distinctBy { it.items.id }
                val remainingItems = items.filter { it !in uniqueItems }

                for (item in uniqueItems) {
                    entryCounter++

                    resultBuilder.append(
                        processLine(
                            formatString, delimiter, item, null, tins, quantities, entryCounter, numPlaceholder
                        )
                    )
                }
                for (item in remainingItems) {
                    entryCounter++

                    resultBuilder.append(
                        processLine(
                            formatString, delimiter, item, null, tins, quantities, entryCounter, numPlaceholder
                        )
                    )
                }
            }
        }

        val processedString = resultBuilder.toString()
        val processedDelimiter = delimiter.replace("_n_", "\n")

        return if (processedDelimiter.isNotBlank() && processedString.endsWith(processedDelimiter)) {
            processedString.removeSuffix(processedDelimiter)
        } else { processedString }
    }

    private fun processLine(
        format: String,
        delimiter: String,
        itemData: ItemsComponentsAndTins?,
        tinData: Tins?,
        filteredTins: List<Tins>,
        formattedQuantities: Map<Int, String>,
        currentLineNumber: Int,
        numPlaceholderRegex: Regex,
    ) : String {
        var processedLine = format

        // Pre-process escaped specials (replace with temp unique placeholder to prevent processing)
        val specialCharacters = listOf(
            '#',
            '[',
            ']',
            '{',
            '}',
            '\'',
            '~'
        )
        val escapeReplacements = mutableMapOf<String, String>()
        var tempEscapeString = ""
        var i = 0
        var placeholderIndex = 0

        while (i < processedLine.length) {
            if (processedLine[i] == '\'') {
                if (i + 1 < processedLine.length) {
                    val nextChar = processedLine[i + 1]
                    if (specialCharacters.contains(nextChar)) {
                        val placeholder = "%%ESC${placeholderIndex++}%%"
                        escapeReplacements[placeholder] = nextChar.toString()
                        tempEscapeString += placeholder
                        i += 2
                        continue
                    }
                }
            }
            tempEscapeString += processedLine[i]
            i++
        }

        processedLine = tempEscapeString


        // Conditional block processing
        val tinSublist = Regex("""\{(.*?)\}""")
        val tinSublistReplacements = mutableMapOf<String, String>()
        var tempId = 0
        var lineWithoutTins = processedLine
        val hasTinSublistPattern = processedLine.contains("{")
                && processedLine.contains("}")
                && listOf(
            "@label", "@container", "@T_qty", "@manufacture", "@cellar", "@open", "@finished"
        ).any { processedLine.contains(it) }

        lineWithoutTins = tinSublist.replace(lineWithoutTins) {
            val original = it.value
            val placeholder = "%%TIN${tempId++}%%"
            tinSublistReplacements[placeholder] = original
            placeholder
        }

        var conditionalsProcessed = conditionalProcessing(lineWithoutTins, itemData, tinData, formattedQuantities, hasTinSublistPattern)

        tinSublistReplacements.forEach { (placeholder, original) ->
            conditionalsProcessed = conditionalsProcessed.replace(placeholder, original)
        }

        processedLine = conditionalsProcessed


        // Tins as sublist processing
        // val tinSublist = Regex("""\{(.*?)\}""")
        processedLine = tinSublist.replace(processedLine) {
            val sublistTemplate = it.groupValues[1]
            val sublistOut = StringBuilder()
            val sublistDelimiter = sublistTemplate.substringAfterLast("~", "").substringBeforeLast("}")

            val tinsToProcess = itemData?.tins?.filter { it in filteredTins }

            if (tinsToProcess.isNullOrEmpty()) { return@replace "" }

            tinsToProcess.forEachIndexed { index, tin ->
                var tinLine = sublistTemplate.substringBeforeLast("~")

                if (index > 0 && sublistDelimiter.isNotBlank()) { sublistOut.append(sublistDelimiter) }

                tinLine = conditionalProcessing(tinLine, itemData, tin, formattedQuantities)

                tinLine = tinLine.replace("@label", tin.tinLabel)
                tinLine = tinLine.replace("@container", tin.container)
                tinLine = tinLine.replace("@T_qty", if (tin.unit.isNotBlank() && !tin.finished) "${formatDecimal(tin.tinQuantity)} ${tin.unit}" else "")
                tinLine = tinLine.replace("@manufacture", formatMediumDate(tin.manufactureDate))
                tinLine = tinLine.replace("@cellar", formatMediumDate(tin.cellarDate))
                tinLine = tinLine.replace("@open", formatMediumDate(tin.openDate))
                tinLine = tinLine.replace("@finished", if (tin.finished) "(Finished)" else "")

                sublistOut.append(tinLine)
            }
            if (sublistDelimiter.isNotBlank() && sublistOut.endsWith(sublistDelimiter)) {
                sublistOut.removeSuffix(sublistDelimiter)
            }

            sublistOut.toString()
        }


        // Number count processing
        if (processedLine.contains("#")) {
          //  val numberFormat = currentLineNumber.toString().padStart(numHashCt, '0')
            processedLine = numPlaceholderRegex.replace(processedLine) {
                currentLineNumber.toString().padStart(it.value.length, '0')
            }
        }


        // Main item processing
        if (itemData != null) {
            processedLine = processedLine.replace("@brand", itemData.items.brand)
            processedLine = processedLine.replace("@blend", itemData.items.blend)
            processedLine = processedLine.replace("@type", itemData.items.type)
            processedLine = processedLine.replace("@subgenre", itemData.items.subGenre)
            processedLine = processedLine.replace("@cut", itemData.items.cut)
            processedLine = processedLine.replace("@comps", itemData.components.joinToString(", ") { it.componentName })
            processedLine = processedLine.replace("@flavors", itemData.flavoring.joinToString(", ") { it.flavoringName })
            processedLine = processedLine.replace("@qty", formattedQuantities[itemData.items.id] ?: "")
            processedLine = processedLine.replace("@prod", if (itemData.items.inProduction) "In Production" else "Discontinued")
        } else {
            listOf("@brand", "@blend", "@type", "@subgenre", "@cut", "@comps", "@flavors", "@qty", "@prod").forEach{
                processedLine = processedLine.replace(it, "")
            }
        }

        if (tinData != null) {
            processedLine = processedLine.replace("@label", tinData.tinLabel)
            processedLine = processedLine.replace("@container", tinData.container)
            processedLine = processedLine.replace("@T_qty", if (tinData.unit.isNotBlank() && !tinData.finished) "${formatDecimal(tinData.tinQuantity)} ${tinData.unit}" else "")
            processedLine = processedLine.replace("@manufacture", formatMediumDate(tinData.manufactureDate))
            processedLine = processedLine.replace("@cellar", formatMediumDate(tinData.cellarDate))
            processedLine = processedLine.replace("@open", formatMediumDate(tinData.openDate))
            processedLine = processedLine.replace("@finished", if (tinData.finished) "(Finished)" else "")
        } else {
            listOf("@label", "@container", "@T_qty", "@manufacture", "@cellar", "@open", "@finished").forEach{
                processedLine = processedLine.replace(it, "")
            }
        }

        // Revert escaped characters
        escapeReplacements.forEach { (placeholder, original) ->
            processedLine = processedLine.replace(placeholder, original)
        }

        processedLine += delimiter
        processedLine = processedLine.replace("_n_", "\n")

        return processedLine
    }

    private fun conditionalProcessing (
        inputLine: String,
        itemData: ItemsComponentsAndTins?,
        tinData: Tins?,
        formattedQuantities: Map<Int, String>,
        hasTinSublist: Boolean = false,
    ): String {
        var processedLine = inputLine
        var lineBeforeThisPass: String
        val nestedConditional = Regex("""\[([^\[\]]*)]""")

        do {
            lineBeforeThisPass = processedLine
            processedLine = nestedConditional.replace(processedLine) {
                val innerContent = it.groupValues[1]
                val placeholderScan = Regex("""@\w+(?!\w)""")

                val allPlaceholders = placeholderScan.findAll(innerContent).toList()

                if (hasTinSublist) {
                    if (itemData != null && itemData.tins.isNotEmpty()) {
                        innerContent
                    } else { "" }
                } else {
                    if (allPlaceholders.isNotEmpty()) {
                        var content = innerContent
                        var anyResolved = false

                        allPlaceholders.forEach {
                            val placeholder = it.value
                            val resolved =
                                resolveSinglePlace(
                                    placeholder,
                                    itemData,
                                    tinData,
                                    formattedQuantities
                                )

                            if (resolved.isNotBlank() && resolved != placeholder) {
                                anyResolved = true
                            }
                            content = content.replace(placeholder, resolved)
                        }
                        if (anyResolved) {
                            content
                        } else { "" }
                    } else { "" }
                }
            }
        } while (processedLine != lineBeforeThisPass)
        return processedLine
    }

    private fun resolveSinglePlace(
        placeholder: String,
        itemData: ItemsComponentsAndTins?,
        tinData: Tins?,
        formattedQuantities: Map<Int, String>
    ): String {
        if (itemData != null) {
            when (placeholder) {
                "@brand" -> return itemData.items.brand
                "@blend" -> return itemData.items.blend
                "@type" -> return itemData.items.type
                "@subgenre" -> return itemData.items.subGenre
                "@cut" -> return itemData.items.cut
                "@comps" -> return itemData.components.joinToString(", ") { it.componentName }
                "@flavors" -> return itemData.flavoring.joinToString(", ") { it.flavoringName }
                "@qty" -> return formattedQuantities[itemData.items.id] ?: ""
                "@prod" -> return if (itemData.items.inProduction) "In Production" else "Discontinued"
            }
        }
        if (tinData != null) {
            when (placeholder) {
                "@label" -> return tinData.tinLabel
                "@container" -> return tinData.container
                "@T_qty" -> return if (tinData.unit.isNotBlank() && !tinData.finished) "${formatDecimal(tinData.tinQuantity)} ${tinData.unit}" else ""
                "@manufacture" -> return formatMediumDate(tinData.manufactureDate)
                "@cellar" -> return formatMediumDate(tinData.cellarDate)
                "@open" -> return formatMediumDate(tinData.openDate)
                "@finished" -> return if (tinData.finished) "(Finished)" else ""
            }
        }
        return ""
    }


}

data class PlaintextUiState(
    val formatString: String = "",
    val delimiter: String = "",
    val preview: String = "",
    val plainList: String = "",
    val formattedQuantities: Map<Int, String> = emptyMap(),
    val sortState: PlaintextSortOption = PlaintextSortOption(),
    val sortOptions: List<PlaintextSortOption> = emptyList(),
    val formatGuide: Map<String, String> = emptyMap(),
    val presets: List<PlaintextPreset> = emptyList(),
    val loading: Boolean = false,
)

data class PlaintextPreset(
    val slot: Int = 0,
    val formatString: String = "",
    val delimiter: String = "",
)

data class PrintOptions(
    val font: Float = 12f,
    val margin: Double = 1.0,
)

data class PlaintextSortOption(
    val value: String = "Default",
    val ascending: Boolean = true,
    val subSort: String = "",
    val icon: Int =
        if (ascending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
) {
    companion object {
        val DEFAULT = PlaintextSortOption("Item Default")
        val BRAND = PlaintextSortOption("Brand")
        val BLEND = PlaintextSortOption("Blend")
        val TYPE = PlaintextSortOption("Type")
        val SUBGENRE = PlaintextSortOption("Subgenre")
        val CUT = PlaintextSortOption("Cut")
        val QUANTITY = PlaintextSortOption("Quantity")
        val TIN_DEFAULT = PlaintextSortOption("Tin Default")
        val TIN_LABEL = PlaintextSortOption("Tin Label")
        val TIN_CONTAINER = PlaintextSortOption("Tin Container")
        val TIN_QUANTITY = PlaintextSortOption("Tin Quantity")
    }
}