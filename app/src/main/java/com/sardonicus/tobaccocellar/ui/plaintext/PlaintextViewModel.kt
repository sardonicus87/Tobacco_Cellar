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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

class PlaintextViewModel (
    filterViewModel: FilterViewModel,
    val preferencesRepo: PreferencesRepo
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)

    private val _formatStringEntry = MutableStateFlow("")
    val formatStringEntry: StateFlow<String> = _formatStringEntry

    private val _delimiter = MutableStateFlow("")
    val delimiter: StateFlow<String> = _delimiter

    private val _listAsTins = MutableStateFlow(false)
    val listAsTins: StateFlow<Boolean> = _listAsTins

    private val _listAsTinsEnabled = MutableStateFlow(false)
    val listAsTinsEnabled: StateFlow<Boolean> = _listAsTinsEnabled

    private sealed class Template {
        data class Text(val content: String) : Template()
        data class Placeholder(val key: String) : Template()
        data class LineNumber(val length: Int) : Template()
        data class Conditional(val segments: List<Template>) : Template()
        data class TinSublist(val segments: List<Template>, val subDelimiter: String) : Template()
    }

    companion object {
        private val RATING_PLACEHOLDER = Regex("""@rating_(\d+)(?:_(\d))?""")
        private val PLACEHOLDER_SCAN = Regex("""@(rating_\d+(?:_\d)?|T_qty|[a-zA-Z0-9]+)""")
        private val SPECIAL_CHARACTERS = setOf('#', '[', ']', '{', '}', '\'', '~', '_')

        private val TIN_SORT_VALUES = setOf(
            PlaintextSorting.TIN_DEFAULT.value,
            PlaintextSorting.TIN_LABEL.value,
            PlaintextSorting.TIN_CONTAINER.value,
            PlaintextSorting.TIN_QUANTITY.value,
            PlaintextSorting.TIN_MANUF.value,
            PlaintextSorting.TIN_CELLAR.value,
            PlaintextSorting.TIN_OPEN.value
        )

        private val fakeTin = Tins(-1, -1, "", "", 0.0, "", null, null, null, false, 0L)

        // Preview data
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
        private val previewTins = setOf(
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
            // initialized
            launch {
                combine(
                    preferencesRepo.plaintextFormatString,
                    preferencesRepo.plaintextDelimiter,
                    preferencesRepo.plaintextListAs
                ) { format, delimiter, listAsTins ->
                    Triple(format, delimiter, listAsTins)
                }.first().let { (format, delimiter, listAsTins) ->
                    if (format.isNotBlank() || delimiter.isNotBlank()) {
                        _formatStringEntry.value = format
                        _delimiter.value = delimiter
                        _listAsTins.value = listAsTins
                    }
                    delay(50.milliseconds)
                    _isInitialized.value = true
                }
            }

            // Save latest format
            launch {
                combine(
                    _formatStringEntry,
                    _delimiter,
                    _listAsTins
                ) { format, delimiter, listAs ->
                    Triple(format, delimiter, listAs)
                }.collectLatest { (format, delimiter, listAs) ->
                    if (!_isInitialized.value) return@collectLatest
                    delay(500.milliseconds)
                    preferencesRepo.setPtFormat(format)
                    preferencesRepo.setPtDelimiter(delimiter)
                    preferencesRepo.setPtListAs(listAs)
                }
            }

            // enable list as tins
            launch {
                filterViewModel.unifiedFilteredItems.collectLatest {
                    _listAsTinsEnabled.value = it.any { items -> items.tins.isNotEmpty() }
                }
            }
        }
    }

    val sortState: StateFlow<PlaintextSorting> = combine(
        preferencesRepo.plaintextSorting,
        preferencesRepo.plaintextSortAscending,
        preferencesRepo.plaintextSubSorting
    ) { sorting, ascending, subSorting ->
        PlaintextSorting(sorting, ascending, subSorting)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlaintextSorting())

    val sortMenuState: StateFlow<SortMenuState> = sortState.map {
        SortMenuState(PlaintextSorting(it.value), PlaintextSorting(it.subSort))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortMenuState())


    private val parsedTemplate: StateFlow<List<Template>> = _formatStringEntry
        .map { try { parseTemplate(it) } catch (_: Throwable) { emptyList() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(50000), emptyList())

    @Suppress("UNCHECKED_CAST")
    val plainList = combine(
        filterViewModel.unifiedFilteredItems,
        filterViewModel.unifiedFilteredTins,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate,
        parsedTemplate,
        _delimiter,
        _listAsTins,
        sortState
    ) { values ->
        val filteredItems = values[0] as List<ItemsComponentsAndTins>
        val filteredTins = values[1] as List<Tins>
        val quantityOption = values[2] as QuantityOption
        val ozRate = values[3] as Double
        val gramsRate = values[4] as Double
        val template = values[5] as List<Template>
        val delimiter = values[6] as String
        val listAsTins = values[7] as Boolean
        val sortState = values[8] as PlaintextSorting

        val filteredTinsSet = filteredTins.toSet()

        val quantitiesData = filteredItems.associate { items ->
            val relevantTins = items.tins.filter { it in filteredTinsSet }
            val totalQuantity = calculateTotalQuantity(items, relevantTins, quantityOption, ozRate, gramsRate)
            val formattedQuantity = formatQuantity(totalQuantity, quantityOption, relevantTins)
            items.items.id to (totalQuantity to formattedQuantity)
        }
        val tinQuantitySorting = filteredTinsSet.associateWith { tinNormalizedWeight(it) }

        val entries: List<Pair<ItemsComponentsAndTins, Tins?>> = filteredItems.flatMap { item ->
            if (!listAsTins) listOf(item to null)
            else {
                item.tins.filter { it in filteredTinsSet }.ifEmpty { listOf(fakeTin) }.map { item to it }
            }
        }

        val asc = sortState.ascending
        val entrySubSort: (Pair<ItemsComponentsAndTins, Tins?>) -> Comparable<*> = { (itemData, tinData) ->
            if (listAsTins && tinData != null) {
                when (sortState.subSort) {
                    PlaintextSorting.TIN_DEFAULT.value -> if (tinData.tinId == -1) Int.MAX_VALUE else tinData.tinId
                    PlaintextSorting.TIN_LABEL.value -> tinData.tinLabel
                    PlaintextSorting.TIN_CONTAINER.value -> tinData.container
                    PlaintextSorting.TIN_QUANTITY.value -> {
                        val qty = tinQuantitySorting[tinData] ?: 0.0
                        if (qty == 0.0) Double.MAX_VALUE else -qty
                    }
                    PlaintextSorting.TIN_MANUF.value -> tinData.manufactureDate ?: Long.MAX_VALUE
                    PlaintextSorting.TIN_CELLAR.value -> tinData.cellarDate ?: Long.MAX_VALUE
                    PlaintextSorting.TIN_OPEN.value -> tinData.openDate ?: Long.MAX_VALUE
                    PlaintextSorting.DEFAULT.value -> itemData.items.id
                    PlaintextSorting.BRAND.value -> itemData.items.brand
                    PlaintextSorting.BLEND.value -> itemData.items.blend
                    PlaintextSorting.TYPE.value -> itemData.items.type
                    PlaintextSorting.SUBGENRE.value -> itemData.items.subGenre
                    PlaintextSorting.CUT.value -> itemData.items.cut
                    PlaintextSorting.RATING.value -> {
                        val rating = itemData.items.rating
                        if (rating == null) Double.MAX_VALUE else -rating
                    }
                    else -> itemData.items.id
                }
            }
            else {
                when (sortState.subSort) {
                    PlaintextSorting.DEFAULT.value -> itemData.items.id
                    PlaintextSorting.BRAND.value -> itemData.items.brand
                    PlaintextSorting.BLEND.value -> itemData.items.blend
                    PlaintextSorting.TYPE.value -> itemData.items.type
                    PlaintextSorting.SUBGENRE.value -> itemData.items.subGenre
                    PlaintextSorting.CUT.value -> itemData.items.cut
                    PlaintextSorting.QUANTITY.value -> {
                        val quantity = quantitiesData[itemData.items.id]?.first ?: 0.0
                        if (quantity == 0.0) Double.MAX_VALUE else -quantity
                    }
                    PlaintextSorting.RATING.value -> {
                        val rating = itemData.items.rating
                        if (rating == null) Double.MAX_VALUE else -rating
                    }
                    else -> itemData.items.id
                }
            }
        }

        val entryComparator: Comparator<Pair<ItemsComponentsAndTins, Tins?>> =
            when (sortState.value) {
                PlaintextSorting.DEFAULT.value -> if (asc) compareBy { it.first.items.id } else compareByDescending { it.first.items.id }
                PlaintextSorting.BRAND.value -> if (asc) compareBy { it.first.items.brand } else compareByDescending { it.first.items.brand }
                PlaintextSorting.BLEND.value -> if (asc) compareBy { it.first.items.blend } else compareByDescending { it.first.items.blend }
                PlaintextSorting.TYPE.value -> if (asc) compareBy { it.first.items.type } else compareByDescending { it.first.items.type }
                PlaintextSorting.SUBGENRE.value -> if (asc) compareBy { it.first.items.subGenre } else compareByDescending { it.first.items.subGenre }
                PlaintextSorting.CUT.value -> if (asc) compareBy { it.first.items.cut } else compareByDescending { it.first.items.cut }
                PlaintextSorting.QUANTITY.value -> compareBy<Pair<ItemsComponentsAndTins, Tins?>> { quantitiesData[it.first.items.id]?.first == 0.0 }
                    .thenBy {
                        val qty = quantitiesData[it.first.items.id]?.first ?: 0.0
                        if (asc) -qty else qty
                    }
                PlaintextSorting.RATING.value -> compareBy<Pair<ItemsComponentsAndTins, Tins?>> { it.first.items.rating == null }
                    .thenBy {
                        val rating = it.first.items.rating ?: 0.0
                        if (asc) -rating else rating
                    }

                PlaintextSorting.TIN_DEFAULT.value -> if (asc) compareBy { if (it.second?.tinId == -1) Int.MAX_VALUE else it.second?.tinId } else compareByDescending { it.second?.tinId }
                PlaintextSorting.TIN_LABEL.value -> if (asc) compareBy { it.second?.tinLabel?.ifBlank { "~" } } else compareByDescending { it.second?.tinLabel }
                PlaintextSorting.TIN_CONTAINER.value -> if (asc) compareBy { it.second?.container?.ifBlank { "~" } } else compareByDescending { it.second?.container?.ifBlank { "" } }
                PlaintextSorting.TIN_QUANTITY.value -> compareBy<Pair<ItemsComponentsAndTins, Tins?>> { (it.second?.let { t -> tinQuantitySorting[t] } ?: 0.0) == 0.0 }
                    .thenBy {
                        val qty = it.second?.let { tins -> tinQuantitySorting[tins] } ?: 0.0
                        if (asc) -qty else qty
                    }
                PlaintextSorting.TIN_MANUF.value -> if (asc) compareBy { it.second?.manufactureDate ?: Long.MAX_VALUE } else compareByDescending { it.second?.manufactureDate ?: Long.MIN_VALUE }
                PlaintextSorting.TIN_CELLAR.value -> if (asc) compareBy { it.second?.cellarDate ?: Long.MAX_VALUE } else compareByDescending { it.second?.cellarDate ?: Long.MIN_VALUE }
                PlaintextSorting.TIN_OPEN.value -> if (asc) compareBy { it.second?.openDate ?: Long.MAX_VALUE } else compareByDescending { it.second?.openDate ?: Long.MIN_VALUE }

                else -> if (asc) compareBy { it.first.items.id } else compareByDescending { it.first.items.id }
            }.thenBy { entrySubSort(it) }

        val sortedEntries = entries.sortedWith(entryComparator)

        generateListString(sortedEntries, filteredTinsSet, quantitiesData.mapValues { it.value.second }, template, delimiter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val loading: StateFlow<Boolean> = combine(
        plainList,
        _formatStringEntry,
        _isInitialized
    ) { list, formatString, initialized ->
        when {
            !initialized -> true
            formatString.isBlank() -> false
            list.isNotBlank() -> { delay(25.milliseconds); false }
            else -> true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)


    @Suppress("UNCHECKED_CAST")
    val sortOptions = combine(
        _formatStringEntry,
        _listAsTins,
        filterViewModel.unifiedFilteredItems,
        filterViewModel.datesExist
    ) { values: Array<Any?> ->
        val formatString = values[0] as String
        val listAsTins = values[1] as Boolean
        val filteredItems = values[2] as List<ItemsComponentsAndTins>
        val datesExist = values[3] as Boolean

        var typesExist = false
        var subgenresExist = false
        var cutsExist = false
        var ratingsExist = false
        var tinsExist = false
        var containersExist = false
        var tinQuantityExist = false
        var manufExist = false
        var cellarExist = false
        var openExist = false

        for (item in filteredItems) {
            if (item.items.type.isNotBlank()) typesExist = true
            if (item.items.subGenre.isNotBlank()) subgenresExist = true
            if (item.items.cut.isNotBlank()) cutsExist = true
            if (item.items.rating != null) ratingsExist = true
            if (item.tins.isNotEmpty()) tinsExist = true

            if (tinsExist) {
                if (item.tins.any { it.container.isNotBlank() }) containersExist = true
                if (item.tins.any { it.unit.isNotBlank() }) tinQuantityExist = true
                if (datesExist) {
                    if (item.tins.any { it.manufactureDate != null }) manufExist = true
                    if (item.tins.any { it.cellarDate != null }) cellarExist = true
                    if (item.tins.any { it.openDate != null }) openExist = true
                }
            }
        }

        if (formatString.isNotBlank()) {
            val options = mutableListOf<PlaintextSorting>()
            val itemOptions = mutableListOf(
                PlaintextSorting.DEFAULT,
                PlaintextSorting.BRAND,
                PlaintextSorting.BLEND,
                PlaintextSorting.TYPE,
                PlaintextSorting.SUBGENRE,
                PlaintextSorting.CUT,
                PlaintextSorting.QUANTITY,
                PlaintextSorting.RATING
            )
            val tinOptions = mutableListOf(
                PlaintextSorting.TIN_DEFAULT,
                PlaintextSorting.TIN_LABEL,
                PlaintextSorting.TIN_CONTAINER,
                PlaintextSorting.TIN_QUANTITY,
                PlaintextSorting.TIN_MANUF,
                PlaintextSorting.TIN_CELLAR,
                PlaintextSorting.TIN_OPEN
            )
            val removeMap = mapOf(
                PlaintextSorting.TYPE to typesExist,
                PlaintextSorting.SUBGENRE to subgenresExist,
                PlaintextSorting.CUT to cutsExist,
                PlaintextSorting.RATING to ratingsExist,
                PlaintextSorting.TIN_DEFAULT to tinsExist,
                PlaintextSorting.TIN_LABEL to tinsExist,
                PlaintextSorting.TIN_CONTAINER to containersExist,
                PlaintextSorting.TIN_QUANTITY to tinQuantityExist,
                PlaintextSorting.TIN_MANUF to manufExist,
                PlaintextSorting.TIN_CELLAR to cellarExist,
                PlaintextSorting.TIN_OPEN to openExist
            )

            itemOptions.removeAll { removeMap[it] == false }
            tinOptions.removeAll { removeMap[it] == false }

            if (listAsTins) options.addAll(tinOptions + itemOptions - PlaintextSorting.QUANTITY)
            else options.addAll(itemOptions)

            val noSubOptions = setOf(PlaintextSorting.DEFAULT, PlaintextSorting.BLEND, PlaintextSorting.TIN_DEFAULT, PlaintextSorting.TIN_LABEL)

            val subOptionMap = (options - noSubOptions).associateWith { mainOption ->
                when (mainOption) {
                    in tinOptions -> tinOptions + itemOptions - mainOption - PlaintextSorting.QUANTITY
                    in itemOptions -> {
                        if (listAsTins && tinsExist) { tinOptions + itemOptions - mainOption - PlaintextSorting.QUANTITY }
                        else itemOptions - mainOption
                    }
                    else -> emptyList()
                }
            }

            SortingOptions(options, subOptionMap)
        } else SortingOptions()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortingOptions())

    val sortEnabled: StateFlow<Boolean> = combine(
        filterViewModel.unifiedFilteredItems,
        sortOptions
    ) { items, sortOptions ->
        items.size > 1 && sortOptions.mainOptions.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @Suppress("UNCHECKED_CAST")
    val formatPreview = combine(
        _listAsTins,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate,
        parsedTemplate,
        _delimiter
    ) { values ->
        val listAsTins = values[0] as Boolean
        val quantityOption = values[1] as QuantityOption
        val ozRate = values[2] as Double
        val gramsRate = values[3] as Double
        val template = values[4] as List<Template>
        val delimiter = values[5] as String

        val previewFormattedQuantities = previewData.associate { items ->
            val relevantTins = items.tins.filter { it in previewTins }
            items.items.id to formatQuantity(calculateTotalQuantity(items, relevantTins, quantityOption, ozRate, gramsRate), quantityOption, relevantTins)
        }

        val previewEntries = previewData.flatMap { items ->
            if (!listAsTins) listOf(items to null)
            else { items.tins.filter { it in previewTins }
                .ifEmpty { listOf(fakeTin) }.map { items to it } } }

        generateListString(previewEntries, previewTins, previewFormattedQuantities, template, delimiter)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val printOptions: StateFlow<PrintOptions> = combine(
        preferencesRepo.plaintextPrintFontSize,
        preferencesRepo.plaintextPrintMargin
    ) { font, margin ->
        PrintOptions(font, margin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrintOptions())

    val presets = preferencesRepo.plaintextPresetsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private suspend fun generateListString(
        sortedEntries: List<Pair<ItemsComponentsAndTins, Tins?>>,
        filteredTins: Set<Tins>,
        quantities: Map<Int, String>,
        template: List<Template>,
        delimiter: String,
    ): String = coroutineScope {
        if (template.isEmpty()) return@coroutineScope ""

        val processedDelimiter = delimiter.replace("_n_", "\n")
        val results = sortedEntries.mapIndexed { index, (itemData, tinData) ->
            async(Dispatchers.Default) {
                renderSegments(template, itemData, tinData, filteredTins, quantities, index + 1, processedDelimiter).first
            }
        }.awaitAll()

        val processedString = results.joinToString("")

        if (processedDelimiter.isNotEmpty() && processedString.endsWith(processedDelimiter)) {
            processedString.removeSuffix(processedDelimiter)
        } else { processedString }
    }

    private fun renderSegments(
        templates: List<Template>,
        itemData: ItemsComponentsAndTins?,
        tinData: Tins?,
        filteredTins: Set<Tins>,
        quantities: Map<Int, String>,
        currentLineNumber: Int,
        delimiter: String = ""
    ): Pair<String, Boolean> {
        var anyResolved = false
        val result = StringBuilder()

        for (segment in templates) {
            when (segment) {
                is Template.Text -> result.append(segment.content)
                is Template.Placeholder -> {
                    val resolved = resolveSinglePlace(segment.key, itemData, tinData, quantities)
                    if (resolved.isNotBlank()) anyResolved = true
                    result.append(resolved)
                }
                is Template.LineNumber -> {
                    result.append(currentLineNumber.toString().padStart(segment.length, '0'))
                }
                is Template.Conditional -> {
                    val (inner, resolved) = renderSegments(segment.segments, itemData, tinData, filteredTins, quantities, currentLineNumber, "")
                    if (resolved) {
                        anyResolved = true
                        result.append(inner)
                    }
                }
                is Template.TinSublist -> {
                    val itemTins = itemData?.tins?.filter { it in filteredTins } ?: emptyList()
                    if (itemTins.isNotEmpty()) {
                        val sublistResult = itemTins.joinToString(segment.subDelimiter) { tin ->
                            renderSegments(segment.segments, itemData, tin, filteredTins, quantities, currentLineNumber, "").first
                        }
                        if (sublistResult.isNotBlank()) {
                            anyResolved = true
                            result.append(sublistResult)
                        }
                    }
                }
            }
        }

        if (anyResolved || templates.any { it is Template.Text }) { result.append(delimiter) }

        return result.toString() to anyResolved
    }

    private fun resolveSinglePlace(
        placeholder: String,
        itemData: ItemsComponentsAndTins?,
        tinData: Tins?,
        formattedQuantities: Map<Int, String>
    ): String {
        if (itemData != null && placeholder.startsWith("@rating_")) {
            val matchResult = RATING_PLACEHOLDER.find(placeholder)
            if (matchResult != null) {
                val max = matchResult.groupValues.getOrNull(1)?.toIntOrNull() ?: 5
                val rounding = matchResult.groupValues.getOrNull(2)?.toIntOrNull().takeIf { it in 0..2 } ?: 2
                return exportRatingString(itemData.items.rating, max, rounding)
            }
        }

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
        return placeholder
    }

    private fun parseTemplate(input: String): List<Template> {
        val segments = mutableListOf<Template>()
        var i = 0
        while (i < input.length) {
            when (val char = input[i]) {
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
                    if (nextIndex != -1) {
                        segments.add(Template.Conditional(parseTemplate(inner))); i = nextIndex }
                    else { segments.add(Template.Text("[")); i++ }
                }
                '{' -> {
                    val (inner, nextIndex) = findClosing(input, i + 1, '{', '}')
                    val subDelimiter = inner.substringAfterLast("~", "").replace("_n_", "\n")
                    val subTemplate = if (inner.contains("~")) inner.substringBeforeLast("~") else inner

                    if (nextIndex != -1) {
                        segments.add(Template.TinSublist(parseTemplate(subTemplate), subDelimiter))
                        i = nextIndex
                    } else { segments.add(Template.Text("{")); i++ }
                }
                '_' -> {
                    if (input.startsWith("_n_", i)) { segments.add(Template.Text("\n")); i += 3 }
                    else { segments.add(Template.Text("_")); i++ }
                }
                else -> {
                    val start = i
                    if (isSpecial(char)) { segments.add(Template.Text(char.toString())); i++ }
                    else {
                        while (i < input.length && !isSpecial(input[i])) { i++ }
                        segments.add(Template.Text(input.substring(start, i))) }
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
        return "" to -1
    }

    private fun tinNormalizedWeight(tin: Tins): Double {
        if (tin.finished || tin.unit.isBlank()) return 0.0

        return when (tin.unit) {
            "oz" -> tin.tinQuantity * 28.3495
            "lbs" -> tin.tinQuantity * 453.592
            "grams" -> tin.tinQuantity
            else -> 0.0
        }
    }

    fun updateSorting(mainOption: PlaintextSorting? = null, subOption: PlaintextSorting? = null) {
        viewModelScope.launch{
            val currentSort = sortState.value
            val options = sortOptions.value

            if (subOption != null) {
                if (currentSort.subSort == subOption.value) {
                    preferencesRepo.setPtSort(currentSort.value, !currentSort.ascending) }
                else { preferencesRepo.setPtSubSort(subOption.value) } }
            else if (mainOption != null) {
                val newMain = currentSort.value != mainOption.value
                val hasSub = options.subOptions.containsKey(mainOption)

                if (newMain) {
                    preferencesRepo.setPtSort(mainOption.value, true)
                    if (hasSub) {
                        val sub = if (mainOption.value in TIN_SORT_VALUES) PlaintextSorting.TIN_DEFAULT.value else PlaintextSorting.DEFAULT.value
                        preferencesRepo.setPtSubSort(sub)
                    }
                }
                else { if (!hasSub) preferencesRepo.setPtSort(mainOption.value, !currentSort.ascending) }
            }
        }
    }

    fun saveFormatting(format: String? = null, delimiter: String? = null, listAsTins: Boolean? = null) {
        val currentListAsTins = _listAsTins.value

        _formatStringEntry.value = format ?: _formatStringEntry.value
        _delimiter.value = delimiter ?: _delimiter.value
        _listAsTins.value = listAsTins ?: _listAsTins.value

        if (listAsTins != null && currentListAsTins != listAsTins) {
            val mainOption = if (listAsTins) PlaintextSorting.TIN_DEFAULT else PlaintextSorting.DEFAULT
            if (mainOption.value != sortState.value.value) updateSorting(mainOption = mainOption)
        }
    }

    fun savePreset(slot: Int, format: String, delimiter: String) {
        viewModelScope.launch { preferencesRepo.savePtPreset(slot, format, delimiter) }
    }

    fun savePrintOptions(font: Float, margin: Double) {
        viewModelScope.launch { preferencesRepo.setPtPrintOptions(font, margin) }
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

@Stable
data class PlaintextSorting(
    val value: String = DEFAULT.value,
    val ascending: Boolean = true,
    val subSort: String = "",
    val icon: Int =
        when (value) {
            "Quantity", "Rating" -> if (ascending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (ascending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
) {
    companion object {
        val DEFAULT = PlaintextSorting("Item Default")
        val BRAND = PlaintextSorting("Brand")
        val BLEND = PlaintextSorting("Blend")
        val TYPE = PlaintextSorting("Type")
        val SUBGENRE = PlaintextSorting("Subgenre")
        val CUT = PlaintextSorting("Cut")
        val QUANTITY = PlaintextSorting("Quantity")
        val RATING = PlaintextSorting("Rating")
        val TIN_DEFAULT = PlaintextSorting("Tin Default")
        val TIN_LABEL = PlaintextSorting("Tin Label")
        val TIN_CONTAINER = PlaintextSorting("Tin Container")
        val TIN_QUANTITY = PlaintextSorting("Tin Quantity")
        val TIN_MANUF = PlaintextSorting("Manufacture Date")
        val TIN_CELLAR = PlaintextSorting("Cellar Date")
        val TIN_OPEN = PlaintextSorting("Open Date")
    }
}

data class SortingOptions(
    val mainOptions: List<PlaintextSorting> = emptyList(),
    val subOptions:  Map<PlaintextSorting, List<PlaintextSorting>> = emptyMap()
)

data class SortMenuState(
    val mainSelection: PlaintextSorting = PlaintextSorting.DEFAULT,
    val subSelection: PlaintextSorting = PlaintextSorting.DEFAULT
)