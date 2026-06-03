package com.sardonicus.tobaccocellar.ui.plaintext

import androidx.compose.runtime.Stable
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
import com.sardonicus.tobaccocellar.ui.addEditItems.formatMediumDate
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.home.calculateTotalQuantity
import com.sardonicus.tobaccocellar.ui.home.formatQuantity
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.exportRatingString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

class PlaintextViewModel (
    filterViewModel: FilterViewModel,
    val preferencesRepo: PreferencesRepo
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _sortMenuState = MutableStateFlow(SortMenuState())
    val sortMenuState = _sortMenuState.asStateFlow()

    private val _sortState = MutableStateFlow(PlaintextSortOption())
    val sortState: StateFlow<PlaintextSortOption> = _sortState.asStateFlow()

    private val _subSortOption = MutableStateFlow("")
    val subSortOption: StateFlow<String> = _subSortOption.asStateFlow()

    private val _printDialog = MutableStateFlow(false)
    val printDialog = _printDialog.asStateFlow()

    private val _printOptions = MutableStateFlow(PrintOptions())
    val printOptions: StateFlow<PrintOptions> = _printOptions.asStateFlow()

    private val _formatStringEntry = MutableStateFlow("")
    val formatStringEntry: StateFlow<String> = _formatStringEntry.asStateFlow()

    private val _delimiter = MutableStateFlow("")
    val delimiter: StateFlow<String> = _delimiter.asStateFlow()

    private val _presets = MutableStateFlow(emptyList<PlaintextPreset>())
    val presets: StateFlow<List<PlaintextPreset>> = _presets.asStateFlow()

    private val _selectionFocused = MutableStateFlow(false)
    val selectionFocused = _selectionFocused.asStateFlow()

    private val _selectionKey = MutableStateFlow(0)
    val selectionKey = _selectionKey.asStateFlow()

    private val _tabIndex = MutableStateFlow(0)
    val tabIndex = _tabIndex.asStateFlow()

    private val _actionRowExpanded = MutableStateFlow(false)
    val actionRowExpanded = _actionRowExpanded.asStateFlow()

    fun updateTabIndex(index: Int) { _tabIndex.value = index }

    private sealed class Template {
        data class Text(val content: String) : Template()
        data class Placeholder(val key: String) : Template()
        data class LineNumber(val length: Int) : Template()
        data class Conditional(val segments: List<Template>) : Template()
        data class TinSublist(val segments: List<Template>, val subDelimiter: String) : Template()
    }

    companion object {
        private val TIN_SUBLIST = Regex("""\{(.*?)\}""")
        private val RATING_PLACEHOLDER = Regex("@rating_(\\d+)(?:_(\\d))?")
        private val PLACEHOLDER_SCAN = Regex("""@\w+(?!\w)""")

        private val TIN_PLACEHOLDERS = listOf("@label", "@container", "@T_qty", "@manufacture",
            "@cellar", "@open", "@finished")
        private val SPECIAL_CHARACTERS = setOf('#', '[', ']', '{', '}', '\'', '~')

        private val previewItems = listOf(
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
                rating = 4.5,
                notes = "",
                syncTins = false,
                lastModified = -1L
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
                rating = null,
                notes = "",
                syncTins = false,
                lastModified = -1L
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
                rating = 1.0,
                notes = "note",
                syncTins = false,
                lastModified = -1L
            )
        )
        private val previewTins = listOf(
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
                finished = true,
                lastModified = -1L
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
                finished = false,
                lastModified = -1L
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
                finished = false,
                lastModified = -1L
            ),
        )
        private val previewComponents = listOf(
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
        private val previewFlavoring = listOf(
            Flavoring(
                flavoringId = 1,
                flavoringName = "vanilla"
            ),
            Flavoring(
                flavoringId = 2,
                flavoringName = "anise"
            ),
        )
        private val previewComponentCrossRef = listOf(
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
        private val previewFlavoringCrossRef = listOf(
            ItemsFlavoringCrossRef(
                itemId = 3,
                flavoringId = 1
            ),
            ItemsFlavoringCrossRef(
                itemId = 2,
                flavoringId = 2
            ),
        )
        private val previewData = previewItems.map { item ->
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
    }

    init {
        viewModelScope.launch {
            // Sorting
            launch {
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
                }.collect { _sortState.value = it }
            }

            // Format string
            launch {
                combine(
                    preferencesRepo.plaintextFormatString,
                    preferencesRepo.plaintextDelimiter
                ) { format, delimiter ->
                    format to delimiter
                }.first().let { (format, delimiter) ->
                    if (format.isNotBlank() || delimiter.isNotBlank()) {
                        saveFormatString(format, delimiter)
                    }
                    _isInitialized.value = true
                }
            }

            // Save latest format
            launch {
                combine(
                    _formatStringEntry,
                    _delimiter
                ) { format, delimiter ->
                    format to delimiter
                }.collectLatest { (format, delimiter) ->
                    delay(500.milliseconds)
                    preferencesRepo.setPlaintextFormatString(format)
                    preferencesRepo.setPlaintextDelimiter(delimiter)
                }
            }

            // Print settings
            launch {
                combine(
                    preferencesRepo.plaintextPrintFontSize,
                    preferencesRepo.plaintextPrintMargin
                ) { font, margin ->
                    PrintOptions(font, margin)
                }.collect { _printOptions.value = it }
            }

            // Presets loading
            launch {
                preferencesRepo.plaintextPresetsFlow.collect { _presets.value = it }
            }
        }
    }

    private val parsedTemplate: StateFlow<List<Template>> = preferencesRepo.plaintextFormatString
        .map { parseTemplate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(50000), emptyList())

    @Suppress("UNCHECKED_CAST")
    val plainList = combine(
        filterViewModel.unifiedFilteredItems,
        filterViewModel.unifiedFilteredTins,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate,
        parsedTemplate,
        preferencesRepo.plaintextDelimiter,
        sortState,
        subSortOption
    ) { values ->
        val filteredItems = values[0] as List<ItemsComponentsAndTins>
        val filteredTins = values[1] as List<Tins>
        val quantityOption = values[2] as QuantityOption
        val ozRate = values[3] as Double
        val gramsRate = values[4] as Double
        val template = values[5] as List<Template>
        val delimiter = values[6] as String
        val sortState = values[7] as PlaintextSortOption
        val subSortOption = values[8] as String

        val itemsMap = filteredItems.associateBy { it.items.id }
        val filteredTinsSet = filteredTins.toSet()

        val quantitiesData = filteredItems.associate { items ->
            val relevantTins = items.tins.filter { it in filteredTinsSet }
            val totalQuantity = calculateTotalQuantity(items, relevantTins, quantityOption, ozRate, gramsRate)
            val formattedQuantity = formatQuantity(totalQuantity, quantityOption, relevantTins)
            items.items.id to (totalQuantity to formattedQuantity)
        }

        val tinSubSort: (Tins) -> Comparable<*> = {
            val parentItem = itemsMap[it.itemsId] // filteredItems.firstOrNull { item -> item.items.id == it.itemsId }
            if (parentItem != null) {
                when (subSortOption) {
                    PlaintextSortOption.DEFAULT.value -> parentItem.items.id
                    PlaintextSortOption.TIN_DEFAULT.value -> it.tinId
                    PlaintextSortOption.BRAND.value -> parentItem.items.brand
                    PlaintextSortOption.BLEND.value -> parentItem.items.blend
                    else -> parentItem.items.id
                }
            } else { it.tinId }
        }
        val tinQuantitySorting = filteredTinsSet.associateWith { tinNormalizedWeight(it) }

//        val itemsSubSort: (ItemsComponentsAndTins) -> Comparable<*> = when (subSortOption) {
//            PlaintextSortOption.DEFAULT.value -> { it -> it.items.id }
//            PlaintextSortOption.BRAND.value -> { it -> it.items.brand }
//            PlaintextSortOption.BLEND.value -> { it -> it.items.blend }
//            else -> { it -> it.items.id }
//        }

        val sortedItems =  if (filteredItems.isNotEmpty()) {
            val comparator: Comparator<ItemsComponentsAndTins> = when (sortState.value) {
                PlaintextSortOption.DEFAULT.value -> compareBy { it.items.id }
                PlaintextSortOption.BRAND.value -> compareBy { it.items.brand }
                PlaintextSortOption.BLEND.value -> compareBy { it.items.blend }
                PlaintextSortOption.TYPE.value -> compareBy { it.items.type }
                PlaintextSortOption.SUBGENRE.value -> compareBy { it.items.subGenre }
                PlaintextSortOption.CUT.value -> compareBy { it.items.cut }
                PlaintextSortOption.QUANTITY.value ->
                    compareBy<ItemsComponentsAndTins> { (quantitiesData[it.items.id]?.first ?: 0.0) == 0.0 }
                        .thenBy {
                            val weight = quantitiesData[it.items.id]?.first ?: 0.0
                            if (sortState.ascending) -weight else weight
                        }
                PlaintextSortOption.RATING.value ->
                    compareBy<ItemsComponentsAndTins> { it.items.rating == null }
                        .thenBy {
                            val rating = it.items.rating ?: 0.0
                            if (sortState.ascending ) -rating else rating
                        }
                else -> compareBy { it.items.id }
            }

            if (sortState.value == PlaintextSortOption.QUANTITY.value || sortState.value == PlaintextSortOption.RATING.value){
                filteredItems.sortedWith(comparator)
            } else filteredItems.sortedWith(if (sortState.ascending) comparator else comparator.reversed())
        } else emptyList()

        val sortedTins = if (filteredTinsSet.isNotEmpty()) {
            val comparator = when (sortState.value) {
                PlaintextSortOption.TIN_LABEL.value ->
                    compareBy<Tins> { it.tinLabel }.thenBy { tinSubSort(it) }
                PlaintextSortOption.TIN_CONTAINER.value ->
                    compareBy<Tins> { it.container.ifBlank { "~" } }.thenBy { tinSubSort(it) }
                PlaintextSortOption.TIN_QUANTITY.value ->
                    compareByDescending<Tins> { tinQuantitySorting[it] }.thenBy { tinSubSort(it) }
                else -> compareBy<Tins> { it.itemsId }.thenBy { tinSubSort(it) }
            }

            filteredTinsSet.sortedWith(if (sortState.ascending) comparator else comparator.reversed())
        } else emptyList()

        generateListString(sortedItems, sortedTins, filteredTins, sortState, quantitiesData.mapValues { it.value.second }, template, delimiter)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    init {
        viewModelScope.launch {
            combine(
                plainList,
                formatStringEntry,
                _isInitialized
            ) { list, format, initialized ->
                when {
                    !initialized -> true
                    format.isBlank() -> false
                    list.isNotBlank() -> {
                        delay(25.milliseconds)
                        false
                    }
                    else -> true
                }
            }.collect { _loading.value = it }
        }
    }

    val sortOptions = preferencesRepo.plaintextFormatString.map { formatString ->
        if (formatString.isNotBlank()) {
            val options = mutableListOf(PlaintextSortOption.DEFAULT)
            val itemOptionMap = mapOf(
                "@brand" to PlaintextSortOption.BRAND,
                "@blend" to PlaintextSortOption.BLEND,
                "@type" to PlaintextSortOption.TYPE,
                "@subgenre" to PlaintextSortOption.SUBGENRE,
                "@cut" to PlaintextSortOption.CUT,
                "@qty" to PlaintextSortOption.QUANTITY,
                "@rating_" to PlaintextSortOption.RATING
            )
            val tinOptionMap = mapOf(
                "@label" to PlaintextSortOption.TIN_LABEL,
                "@container" to PlaintextSortOption.TIN_CONTAINER,
                "@T_qty" to PlaintextSortOption.TIN_QUANTITY
            )

            itemOptionMap.forEach { if (formatString.contains(it.key)) options.add(it.value) }

            val fsRemovedSublist = formatString.replace(TIN_SUBLIST, "")
            val validTinOptions = mutableListOf<PlaintextSortOption>()

            tinOptionMap.forEach { if (fsRemovedSublist.contains(it.key)) validTinOptions.add(it.value) }

            if (validTinOptions.isNotEmpty()) {
                options.add(PlaintextSortOption.TIN_DEFAULT)
                options.addAll(validTinOptions)
            }

            options.distinctBy { it.value }
        } else emptyList()
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @Suppress("UNCHECKED_CAST")
    val formatPreview = combine(
        sortState,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate,
        parsedTemplate,
        preferencesRepo.plaintextDelimiter
    ) { values ->
        val sortState = values[0] as PlaintextSortOption
        val quantityOption = values[1] as QuantityOption
        val ozRate = values[2] as Double
        val gramsRate = values[3] as Double
        val template = values[4] as List<Template>
        val delimiter = values[5] as String

        val previewFormattedQuantities = previewData.associate { items ->
            val relevantTins = items.tins.filter { it in previewTins }
            items.items.id to formatQuantity(calculateTotalQuantity(items, relevantTins, quantityOption, ozRate, gramsRate), quantityOption, relevantTins)
        }

        generateListString(previewData, previewTins, previewTins, sortState, previewFormattedQuantities, template, delimiter)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    private suspend fun generateListString(
        items: List<ItemsComponentsAndTins>,
        tins: List<Tins>,
        filteredTins: List<Tins>,
        sortState: PlaintextSortOption,
        quantities: Map<Int, String>,
        template: List<Template>,
        delimiter: String,
    ): String = coroutineScope {
        if (template.isEmpty()) return@coroutineScope ""

        val fsRemovedSublist = TIN_SUBLIST.replace(_formatStringEntry.value, "")
        val containsTinCall = TIN_PLACEHOLDERS.any { fsRemovedSublist.contains(it) }
        val processedDelimiter = delimiter.replace("_n_", "\n")

        val tinsPrimary = when (sortState.value) {
            PlaintextSortOption.TIN_LABEL.value,
            PlaintextSortOption.TIN_CONTAINER.value,
            PlaintextSortOption.TIN_QUANTITY.value -> true
            else -> false
        }

        val tasks = mutableListOf<Pair<ItemsComponentsAndTins, Tins?>>()

        if (!tinsPrimary) {
            // List by Items sorting
            for (item in items) {
                val itemTins = item.tins.filter { it in tins }
                if (containsTinCall) {
                    if (itemTins.isNotEmpty()) {
                        itemTins.forEach { tasks.add(item to it) }
                    } else {
                        tasks.add(item to null)
                    }
                } else {
                    tasks.add(item to null)
                }
            }
        } else {
            // List by Tins sorting
            if (containsTinCall) {
                for (tin in tins) {
                    val tinItem = items.first { it.items.id == tin.itemsId }
                    tasks.add(tinItem to tin)
                }
            } else {
                val uniqueItems = tins.mapNotNull { tin ->
                    items.firstOrNull { it.items.id == tin.itemsId } }.distinctBy { it.items.id }
                val remainingItems = items.filter { it !in uniqueItems }
                (uniqueItems + remainingItems).forEach { tasks.add(it to null) }
            }
        }

        val results = tasks.mapIndexed { index, (item, tin) ->
            async(Dispatchers.Default) {
                renderSegments(template, item, tin, filteredTins, quantities, index + 1, processedDelimiter).first
            }
        }.awaitAll()

        val processedString = results.joinToString("") // resultBuilder.toString()

        if (processedDelimiter.isNotEmpty() && processedString.endsWith(processedDelimiter)) {
            processedString.removeSuffix(processedDelimiter)
        } else { processedString }
    }

    private fun renderSegments(
        segments: List<Template>,
        itemData: ItemsComponentsAndTins?,
        tinData: Tins?,
        filteredTins: List<Tins>,
        formattedQuantities: Map<Int, String>,
        currentLineNumber: Int,
        delimiter: String = ""
    ): Pair<String, Boolean> {
        var anyResolved = false
        val result = StringBuilder()

        for (segment in segments) {
            when (segment) {
                is Template.Text -> result.append(segment.content)
                is Template.Placeholder -> {
                    val resolved = resolveSinglePlace(segment.key, itemData, tinData, formattedQuantities)
                    if (resolved.isNotBlank()) anyResolved = true
                    result.append(resolved)
                }
                is Template.LineNumber -> {
                    result.append(currentLineNumber.toString().padStart(segment.length, '0'))
                }
                is Template.Conditional -> {
                    val (inner, resolved) = renderSegments(segment.segments, itemData, tinData, filteredTins, formattedQuantities, currentLineNumber, "")
                    if (resolved) {
                        anyResolved = true
                        result.append(inner)
                    }
                }
                is Template.TinSublist -> {
                    val itemTins = itemData?.tins?.filter { it in filteredTins } ?: emptyList()
                    if (itemTins.isNotEmpty()) {
                        val sublistResult = itemTins.joinToString(segment.subDelimiter) { tin ->
                            renderSegments(segment.segments, itemData, tin, filteredTins, formattedQuantities, currentLineNumber, "").first
                        }
                        if (sublistResult.isNotBlank()) {
                            anyResolved = true
                            result.append(sublistResult)
                        }
                    }
                }
            }
        }

        if (anyResolved || segments.any { it is Template.Text }) { result.append(delimiter) }

        return result.toString() to anyResolved
    }

    private fun resolveSinglePlace(
        placeholder: String,
        itemData: ItemsComponentsAndTins?,
        tinData: Tins?,
        formattedQuantities: Map<Int, String>
    ): String {
        if (itemData != null) {
            if (placeholder.startsWith("@rating_")) {
                val matchResult = RATING_PLACEHOLDER.find(placeholder)
                if (matchResult != null) {
                    val max = matchResult.groupValues[1].toIntOrNull() ?: 5
                    val rounding = matchResult.groupValues[2].toIntOrNull().takeIf { it in 0..2 } ?: 2
                    return exportRatingString(itemData.items.rating, max, rounding)
                }
            }

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

    private fun parseTemplate(input: String): List<Template> {
        val segments = mutableListOf<Template>()
        var i = 0
        while (i < input.length) {
            when (input[i]) {
                '\'' -> {
                    if (i + 1 < input.length && SPECIAL_CHARACTERS.contains(input[i + 1])) {
                        segments.add(Template.Text(input[i + 1].toString()))
                        i += 2
                    } else { segments.add(Template.Text("'")); i++ }
                }
                '@' -> {
                    val match = PLACEHOLDER_SCAN.find(input, i)
                    if (match != null && match.range.first == i) {
                        segments.add(Template.Placeholder(match.value))
                        i = match.range.last + 1
                    } else { segments.add(Template.Text("@")); i++ }
                }
                '#' -> {
                    var length = 0
                    while (i < input.length && input[i] == '#') { length++; i++ }
                    segments.add(Template.LineNumber(length))
                }
                '[' -> {
                    val (inner, nextIndex) = findClosing(input, i + 1, '[', ']')
                    segments.add(Template.Conditional(parseTemplate(inner)))
                    i = nextIndex
                }
                '{' -> {
                    val (inner, nextIndex) = findClosing(input, i + 1, '{', '}')
                    val subDelimiter = inner.substringAfterLast("~", "").replace("_n_", "\n")
                    val subTemplate = if (inner.contains("~")) inner.substringBeforeLast("~") else inner
                    segments.add(Template.TinSublist(parseTemplate(subTemplate), subDelimiter))
                    i = nextIndex
                }
                '_' -> {
                    if (input.startsWith("_n_", i)) {
                        segments.add(Template.Text("\n"))
                        i += 3
                    } else {
                        segments.add(Template.Text("_"))
                        i++
                    }
                }
                else -> {
                    val start = i
                    while (i < input.length && !isSpecial(input[i])) { i++ }
                    segments.add(Template.Text(input.substring(start, i)))
                }
            }
        }
        return segments
    }

    private fun isSpecial(char: Char) = SPECIAL_CHARACTERS.contains(char) || char == '@' || char == '#'

    private fun findClosing(input: String, start: Int, open: Char, close: Char): Pair<String, Int> {
        var depth = 1
        var i = start
        while (i < input.length) {
            if (input[i] == open) depth++
            else if (input[i] == close) depth--
            if (depth == 0) return input.substring(start, i) to i + 1
            i++
        }
        return input.substring(start) to input.length
    }


    fun resetSelection() {
        _selectionKey.update { it + 1 }
        updateFocused(false)
    }

    fun updateFocused(focused: Boolean) { _selectionFocused.update { focused } }

    private fun tinNormalizedWeight(tin: Tins): Double {
        if (tin.finished || tin.unit.isBlank()) return 0.0

        return when (tin.unit) {
            "oz" -> tin.tinQuantity * 28.3495
            "lbs" -> tin.tinQuantity * 453.592
            "grams" -> tin.tinQuantity
            else -> 0.0
        }
    }

    fun toggleActionRow() { _actionRowExpanded.update { !it } }

    fun updateSortMenuState(sortMenu: SortMenuState) {
        val subMenuOverride = if (!sortMenu.mainMenu) false else sortMenu.subMenu

        _sortMenuState.value = SortMenuState(
            mainMenu = sortMenu.mainMenu,
            subMenu = subMenuOverride,
            mainSelection = sortMenu.mainSelection,
            subSelection = sortMenu.subSelection
        )
    }

    fun updateSorting(option: String, reverseSwitch: Boolean) {
        viewModelScope.launch {
            val currentSort = _sortState.value
            val reverse = if (reverseSwitch) !currentSort.ascending else currentSort.ascending
            val newSort =
                if (currentSort.value == option) { PlaintextSortOption(value = option, reverse) }
                else { PlaintextSortOption(option) }
            preferencesRepo.setPlaintextSorting(newSort.value, newSort.ascending)
        }
    }

    fun updateSubSorting(option: String) {
        _subSortOption.value = option
        viewModelScope.launch { preferencesRepo.setPlaintextSubSorting(option) }
    }

    fun saveFormatString(format: String, delimiter: String = "") {
        _formatStringEntry.value = format
        _delimiter.value = delimiter
    }

    fun savePreset(slot: Int, format: String, delimiter: String) {
        viewModelScope.launch { preferencesRepo.savePlaintextPreset(slot, format, delimiter) }
    }

    fun showPrintDialog(show: Boolean) { _printDialog.value = show }

    fun savePrintOptions(font: Float, margin: Double) {
        _printOptions.value = PrintOptions(font, margin)
        viewModelScope.launch { preferencesRepo.setPlaintextPrintOptions(font, margin) }
    }

}


@Serializable
@Stable
data class PlaintextPreset(
    val slot: Int = 0,
    val formatString: String = "",
    val delimiter: String = "",
)

@Stable
data class PrintOptions(
    val font: Float = 12f,
    val margin: Double = 1.0,
)

data class PlaintextSortOption(
    val value: String = "Item Default",
    val ascending: Boolean = true,
    val subSort: String = "",
    val icon: Int =
        when (value) {
            "Quantity", "Rating" -> if (ascending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (ascending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
) {
    companion object {
        val DEFAULT = PlaintextSortOption("Item Default")
        val BRAND = PlaintextSortOption("Brand")
        val BLEND = PlaintextSortOption("Blend")
        val TYPE = PlaintextSortOption("Type")
        val SUBGENRE = PlaintextSortOption("Subgenre")
        val CUT = PlaintextSortOption("Cut")
        val QUANTITY = PlaintextSortOption("Quantity")
        val RATING = PlaintextSortOption("Rating")
        val TIN_DEFAULT = PlaintextSortOption("Tin Default")
        val TIN_LABEL = PlaintextSortOption("Tin Label")
        val TIN_CONTAINER = PlaintextSortOption("Tin Container")
        val TIN_QUANTITY = PlaintextSortOption("Tin Quantity")
    }
}

data class SortMenuState(
    val mainMenu: Boolean = false,
    val subMenu: Boolean = false,
    val mainSelection: String = "",
    val subSelection: String = ""
)