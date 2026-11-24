package com.sardonicus.tobaccocellar.ui.plaintext

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.BlendTypes
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.addEditItems.formatMediumDate
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.home.calculateTotalQuantity
import com.sardonicus.tobaccocellar.ui.home.formatQuantity
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.exportRatingString
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.collator
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.formatLocalizedList
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.getLocalizedContext
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.getLocalizedType
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.rtlIsolate
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.safeString
import com.sardonicus.tobaccocellar.ui.utilities.toCleanString
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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

class PlaintextViewModel (
    filterViewModel: FilterViewModel,
    val preferencesRepo: PreferencesRepo,
    private val app: CellarApplication
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)

    val formatStringState = TextFieldState()
    val delimiterState = TextFieldState()

    private val _listAsTins = MutableStateFlow(false)
    val listAsTins: StateFlow<Boolean> = _listAsTins

    private val _listAsTinsEnabled = MutableStateFlow(false)
    val listAsTinsEnabled: StateFlow<Boolean> = _listAsTinsEnabled

    private val _rtlDialogSeen = MutableStateFlow(false)
    val rtlDialogSeen = _rtlDialogSeen.asStateFlow()
    fun saveRtlDialogSeen() {
        _rtlDialogSeen.value = true
        viewModelScope.launch { preferencesRepo.saveRtlDialogSeen() }
    }

    private sealed class Template {
        data class Text(val content: String) : Template()
        data class Placeholder(val key: String) : Template()
        data class LineNumber(val length: Int) : Template()
        data class Conditional(val segments: List<Template>) : Template()
        data class TinSublist(val segments: List<Template>, val subDelimiter: String) : Template()
    }

    // Preview data

    private val previewItems = listOf(
        Items(
            id = 1,
            brand = getLocalizedContext(app).safeString(R.string.brand_a_v2),
            blend = getLocalizedContext(app).safeString(R.string.blend_1_v2),
            type = BlendTypes.VIRGINIA.name,
            subGenre = "VA/per",
            cut = getLocalizedContext(app).safeString(R.string.flake_cut_v2),
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
            brand = getLocalizedContext(app).safeString(R.string.brand_a_v2),
            blend = getLocalizedContext(app).safeString(R.string.blend_2_v2),
            type = BlendTypes.BURLEY.name,
            subGenre = "",
            cut = getLocalizedContext(app).safeString(R.string.ribbon_v2),
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
            brand = getLocalizedContext(app).safeString(R.string.brand_b_v2),
            blend = getLocalizedContext(app).safeString(R.string.blend_1_v2),
            type = BlendTypes.ENGLISH.name,
            subGenre = getLocalizedContext(app).safeString(R.string.balkan_v2),
            cut = getLocalizedContext(app).safeString(R.string.ribbon_v2),
            inProduction = false,
            quantity = 1,
            favorite = false,
            disliked = false,
            rating = 1.0,
            notes = getLocalizedContext(app).safeString(R.string.fake_note_v2),
            syncTins = false,
            lastModified = -1L
        )
    )
    private val previewTins = setOf(
        Tins(
            tinId = 1,
            itemsId = 1,
            tinLabel = getLocalizedContext(app).safeString(R.string.lot_1_v2),
            container = getLocalizedContext(app).safeString(R.string.jar_v2),
            tinQuantity = 1.75,
            unit = "oz",
            manufactureDate = 1704175200000,
            cellarDate = 1704261600000,
            openDate = 1704348000000,
            finished = true,
            lastModified = -1L
        ),
        Tins(
            tinId = 2,
            itemsId = 1,
            tinLabel = getLocalizedContext(app).safeString(R.string.lot_2_v2),
            container = getLocalizedContext(app).safeString(R.string.original_tin_v2),
            tinQuantity = 50.00,
            unit = "grams",
            manufactureDate = 1704175200000,
            cellarDate = 1704261600000,
            openDate = null,
            finished = false,
            lastModified = -1L
        ),
        Tins(
            tinId = 3,
            itemsId = 2,
            tinLabel = getLocalizedContext(app).safeString(R.string.lot_1_v2),
            container = "",
            tinQuantity = 0.00,
            unit = "",
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
            componentName = getLocalizedContext(app).safeString(R.string.virginia_lower_v2)
        ),
        Components(
            componentId = 2,
            componentName = getLocalizedContext(app).safeString(R.string.perique_v2)
        ),
        Components(
            componentId = 3,
            componentName = getLocalizedContext(app).safeString(R.string.burley_lower_v2)
        )
    )
    private val previewFlavoring = listOf(
        Flavoring(
            flavoringId = 1,
            flavoringName = getLocalizedContext(app).safeString(R.string.vanilla_v2)
        ),
        Flavoring(
            flavoringId = 2,
            flavoringName = getLocalizedContext(app).safeString(R.string.anise_v2)
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


    companion object {
        private val RATING_PLACEHOLDER = Regex("""@rating_(\d+)(?:_(\d))?""")
        private val PLACEHOLDER_SCAN = Regex("""@(rating_\d+(?:_\d)?|T_qty|[a-zA-Z0-9]+)""")
        private val SPECIAL_CHARACTERS = setOf('#', '[', ']', '{', '}', '\'', '~', '_')

        private val TIN_SORT_VALUES = setOf(
            PlaintextSortOption.TIN_DEFAULT,
            PlaintextSortOption.TIN_LABEL,
            PlaintextSortOption.TIN_CONTAINER,
            PlaintextSortOption.TIN_QUANTITY,
            PlaintextSortOption.TIN_MANUF,
            PlaintextSortOption.TIN_CELLAR,
            PlaintextSortOption.TIN_OPEN
        )

        private val fakeTin = Tins(-1, -1, "", "", 0.0, "", null, null, null, false, 0L)
    }

    init {
        viewModelScope.launch {
            launch { _rtlDialogSeen.value = preferencesRepo.ptRtlDialogSeen.first() }

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
                        formatStringState.edit { replace(0, length, format) }
                        delimiterState.edit { replace(0, length, delimiter) }
                        _listAsTins.value = listAsTins
                    }
                    delay(50.milliseconds)
                    _isInitialized.value = true
                }
            }

            // Save latest format
            launch {
                combine(
                    snapshotFlow { formatStringState.toCleanString() },
                    snapshotFlow { delimiterState.toCleanString() },
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
        preferencesRepo.plaintextMainSorting,
        preferencesRepo.plaintextSortAscending,
        preferencesRepo.plaintextSubSorting
    ) { mainSort, ascending, subSort ->
        PlaintextSorting(mainSort, ascending, subSort)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlaintextSorting())

    val sortMenuState: StateFlow<SortMenuState> = sortState.map {
        SortMenuState(it.mainSort, it.subSort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortMenuState())


    private val parsedTemplate: StateFlow<List<Template>> = snapshotFlow { formatStringState.toCleanString() }
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
        snapshotFlow { delimiterState.toCleanString() },
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
                    PlaintextSortOption.TIN_DEFAULT -> if (tinData.tinId == -1) Int.MAX_VALUE else tinData.tinId
                    PlaintextSortOption.TIN_LABEL -> tinData.tinLabel
                    PlaintextSortOption.TIN_CONTAINER -> tinData.container
                    PlaintextSortOption.TIN_QUANTITY -> {
                        val qty = tinQuantitySorting[tinData] ?: 0.0
                        if (qty == 0.0) Double.MAX_VALUE else -qty
                    }

                    PlaintextSortOption.TIN_MANUF -> tinData.manufactureDate ?: Long.MAX_VALUE
                    PlaintextSortOption.TIN_CELLAR -> tinData.cellarDate ?: Long.MAX_VALUE
                    PlaintextSortOption.TIN_OPEN -> tinData.openDate ?: Long.MAX_VALUE
                    PlaintextSortOption.DEFAULT -> itemData.items.id
                    PlaintextSortOption.BRAND -> itemData.items.brand
                    PlaintextSortOption.BLEND -> itemData.items.blend
                    PlaintextSortOption.TYPE -> getLocalizedType(itemData.items.type, false)
                    PlaintextSortOption.SUBGENRE -> itemData.items.subGenre
                    PlaintextSortOption.CUT -> itemData.items.cut
                    PlaintextSortOption.RATING -> {
                        val rating = itemData.items.rating
                        if (rating == null) Double.MAX_VALUE else -rating
                    }
                    else -> itemData.items.id
                }
            }
            else {
                when (sortState.subSort) {
                    PlaintextSortOption.DEFAULT -> itemData.items.id
                    PlaintextSortOption.BRAND -> itemData.items.brand
                    PlaintextSortOption.BLEND -> itemData.items.blend
                    PlaintextSortOption.TYPE -> getLocalizedType(itemData.items.type, false)
                    PlaintextSortOption.SUBGENRE -> itemData.items.subGenre
                    PlaintextSortOption.CUT -> itemData.items.cut
                    PlaintextSortOption.QUANTITY -> {
                        val quantity = quantitiesData[itemData.items.id]?.first ?: 0.0
                        if (quantity == 0.0) Double.MAX_VALUE else -quantity
                    }
                    PlaintextSortOption.RATING -> {
                        val rating = itemData.items.rating
                        if (rating == null) Double.MAX_VALUE else -rating
                    }
                    else -> itemData.items.id
                }
            }
        }

        val entryComparator: Comparator<Pair<ItemsComponentsAndTins, Tins?>> =
            when (sortState.mainSort) {
                PlaintextSortOption.DEFAULT -> if (asc) compareBy { it.first.items.id } else compareByDescending { it.first.items.id }
                PlaintextSortOption.BRAND -> if (asc) compareBy(collator) { it.first.items.brand } else compareByDescending(collator) { it.first.items.brand }
                PlaintextSortOption.BLEND -> if (asc) compareBy(collator) { it.first.items.blend } else compareByDescending(collator) { it.first.items.blend }
                PlaintextSortOption.TYPE -> if (asc) compareBy(collator) { getLocalizedType(it.first.items.type, false).ifBlank { "~" } } else compareByDescending(collator) { getLocalizedType(it.first.items.type, false).ifBlank { "~" } }
                PlaintextSortOption.SUBGENRE -> if (asc) compareBy(collator) { it.first.items.subGenre.ifBlank { "~" } } else compareByDescending(collator) { it.first.items.subGenre.ifBlank { "~" } }
                PlaintextSortOption.CUT -> if (asc) compareBy(collator) { it.first.items.cut.ifBlank { "~" } } else compareByDescending(collator) { it.first.items.cut.ifBlank { "~" } }
                PlaintextSortOption.QUANTITY -> compareBy<Pair<ItemsComponentsAndTins, Tins?>> { quantitiesData[it.first.items.id]?.first == 0.0 }
                    .thenBy {
                        val qty = quantitiesData[it.first.items.id]?.first ?: 0.0
                        if (asc) -qty else qty
                    }
                PlaintextSortOption.RATING -> compareBy<Pair<ItemsComponentsAndTins, Tins?>> { it.first.items.rating == null }
                    .thenBy {
                        val rating = it.first.items.rating ?: 0.0
                        if (asc) -rating else rating
                    }

                PlaintextSortOption.TIN_DEFAULT -> if (asc) compareBy { if (it.second?.tinId == -1) Int.MAX_VALUE else it.second?.tinId } else compareByDescending { it.second?.tinId }
                PlaintextSortOption.TIN_LABEL -> if (asc) compareBy(collator) { it.second?.tinLabel ?: "~" } else compareByDescending(collator) { it.second?.tinLabel ?: "1" }
                PlaintextSortOption.TIN_CONTAINER -> if (asc) compareBy(collator) { it.second?.container ?: "~" } else compareByDescending(collator) { it.second?.container ?: "1" }
                PlaintextSortOption.TIN_QUANTITY -> compareBy<Pair<ItemsComponentsAndTins, Tins?>> { (it.second?.let { t -> tinQuantitySorting[t] } ?: 0.0) == 0.0 }
                    .thenBy {
                        val qty = it.second?.let { tins -> tinQuantitySorting[tins] } ?: 0.0
                        if (asc) -qty else qty
                    }
                PlaintextSortOption.TIN_MANUF -> if (asc) compareBy { it.second?.manufactureDate ?: Long.MAX_VALUE } else compareByDescending { it.second?.manufactureDate ?: Long.MIN_VALUE }
                PlaintextSortOption.TIN_CELLAR -> if (asc) compareBy { it.second?.cellarDate ?: Long.MAX_VALUE } else compareByDescending { it.second?.cellarDate ?: Long.MIN_VALUE }
                PlaintextSortOption.TIN_OPEN -> if (asc) compareBy { it.second?.openDate ?: Long.MAX_VALUE } else compareByDescending { it.second?.openDate ?: Long.MIN_VALUE }
            }.thenBy { entrySubSort(it) }

        val sortedEntries = entries.sortedWith(entryComparator)

        generateListString(sortedEntries, filteredTinsSet, quantitiesData.mapValues { it.value.second }, template, delimiter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val loading: StateFlow<Boolean> = combine(
        plainList,
        snapshotFlow { formatStringState.toCleanString() },
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
        snapshotFlow { formatStringState.toCleanString() },
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

        for ((items, _, _, tins) in filteredItems) {
            if (items.type.isNotBlank()) typesExist = true
            if (items.subGenre.isNotBlank()) subgenresExist = true
            if (items.cut.isNotBlank()) cutsExist = true
            if (items.rating != null) ratingsExist = true
            if (tins.isNotEmpty()) tinsExist = true

            if (tinsExist) {
                if (tins.any { it.container.isNotBlank() }) containersExist = true
                if (tins.any { it.unit.isNotBlank() }) tinQuantityExist = true
                if (datesExist) {
                    if (tins.any { it.manufactureDate != null }) manufExist = true
                    if (tins.any { it.cellarDate != null }) cellarExist = true
                    if (tins.any { it.openDate != null }) openExist = true
                }
            }
        }

        if (formatString.isNotBlank()) {
            val options = mutableListOf<PlaintextSortOption>()
            val itemOptions = mutableListOf(
                PlaintextSortOption.DEFAULT,
                PlaintextSortOption.BRAND,
                PlaintextSortOption.BLEND,
                PlaintextSortOption.TYPE,
                PlaintextSortOption.SUBGENRE,
                PlaintextSortOption.CUT,
                PlaintextSortOption.QUANTITY,
                PlaintextSortOption.RATING
            )
            val tinOptions = mutableListOf(
                PlaintextSortOption.TIN_DEFAULT,
                PlaintextSortOption.TIN_LABEL,
                PlaintextSortOption.TIN_CONTAINER,
                PlaintextSortOption.TIN_QUANTITY,
                PlaintextSortOption.TIN_MANUF,
                PlaintextSortOption.TIN_CELLAR,
                PlaintextSortOption.TIN_OPEN
            )
            val removeMap = mapOf(
                PlaintextSortOption.TYPE to typesExist,
                PlaintextSortOption.SUBGENRE to subgenresExist,
                PlaintextSortOption.CUT to cutsExist,
                PlaintextSortOption.RATING to ratingsExist,
                PlaintextSortOption.TIN_DEFAULT to tinsExist,
                PlaintextSortOption.TIN_LABEL to tinsExist,
                PlaintextSortOption.TIN_CONTAINER to containersExist,
                PlaintextSortOption.TIN_QUANTITY to tinQuantityExist,
                PlaintextSortOption.TIN_MANUF to manufExist,
                PlaintextSortOption.TIN_CELLAR to cellarExist,
                PlaintextSortOption.TIN_OPEN to openExist
            )

            itemOptions.removeAll { removeMap[it] == false }
            tinOptions.removeAll { removeMap[it] == false }

            if (listAsTins) options.addAll(tinOptions + itemOptions - PlaintextSortOption.QUANTITY)
            else options.addAll(itemOptions)

            val noSubOptions = setOf(PlaintextSortOption.DEFAULT, PlaintextSortOption.BLEND, PlaintextSortOption.TIN_DEFAULT, PlaintextSortOption.TIN_LABEL)

            val subOptionMap = (options - noSubOptions).associateWith { mainOption ->
                when (mainOption) {
                    in tinOptions -> tinOptions + itemOptions - mainOption - PlaintextSortOption.QUANTITY
                    in itemOptions -> {
                        if (listAsTins && tinsExist) { tinOptions + itemOptions - mainOption - PlaintextSortOption.QUANTITY }
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
        snapshotFlow { delimiterState.toCleanString() }
    ) { values ->
        val listAsTins = values[0] as Boolean
        val quantityOption = values[1] as QuantityOption
        val ozRate = values[2] as Double
        val gramsRate = values[3] as Double
        val template = values[4] as List<Template>
        val delimiter = values[5] as String

        val previewFormattedQuantities = previewData.associate { items ->
            val relevantTins = items.tins.filter { it in previewTins }
            items.items.id to formatQuantity(calculateTotalQuantity(items, relevantTins, quantityOption, ozRate, gramsRate), quantityOption, relevantTins).rtlIsolate()
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
                is Template.Text -> result.append(segment.content.rtlIsolate())
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
        val context = getLocalizedContext(app)

        if (itemData != null && placeholder.startsWith("@rating_")) {
            val matchResult = RATING_PLACEHOLDER.find(placeholder)
            if (matchResult != null) {
                val max = matchResult.groupValues.getOrNull(1)?.toIntOrNull() ?: 5
                val rounding = matchResult.groupValues.getOrNull(2)?.toIntOrNull().takeIf { it in 0..2 } ?: 2
                return exportRatingString(itemData.items.rating, max, rounding).rtlIsolate()
            }
        }

        if (itemData != null) {
            when (placeholder) {
                "@brand" -> return itemData.items.brand.rtlIsolate()
                "@blend" -> return itemData.items.blend.rtlIsolate()
                "@type" -> return getLocalizedType(itemData.items.type, false).rtlIsolate()
                "@subgenre" -> return itemData.items.subGenre.rtlIsolate()
                "@cut" -> return itemData.items.cut.rtlIsolate()
                "@comps" -> return formatLocalizedList(itemData.components.map { it.componentName.rtlIsolate() }.sortedWith(collator))
                "@flavors" -> return formatLocalizedList(itemData.flavoring.map { it.flavoringName.rtlIsolate() }.sortedWith(collator))
                "@qty" -> return formattedQuantities[itemData.items.id]?.rtlIsolate() ?: ""
                "@prod" -> return if (itemData.items.inProduction) context.safeString(R.string.in_production_v2).rtlIsolate() else context.safeString(R.string.discontinued_v2).rtlIsolate()
            }
        }
        if (tinData != null) {
            when (placeholder) {
                "@label" -> return tinData.tinLabel.rtlIsolate()
                "@container" -> return tinData.container.rtlIsolate()
                "@T_qty" -> return tinQuantityString(tinData, context).rtlIsolate()
                "@manufacture" -> return formatMediumDate(tinData.manufactureDate)
                "@cellar" -> return formatMediumDate(tinData.cellarDate)
                "@open" -> return formatMediumDate(tinData.openDate)
                "@finished" -> return if (tinData.finished) context.safeString(R.string.finished_parentheses_v2).rtlIsolate() else ""
            }
        }
        return placeholder
    }

    private fun tinQuantityString (tin: Tins, context: Context): String {
        return if (tin.unit.isBlank() || tin.finished) ""
        else {
            val resource = when (tin.unit) {
                "oz" -> R.string.quantity_oz_v2
                "lbs" -> R.string.quantity_lbs_v2
                "g" -> R.string.quantity_g_v2
                else -> R.string.quantity_kg_v2
            }
            context.safeString(resource, formatDecimal(tin.tinQuantity))
        }
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
            "g" -> tin.tinQuantity
            "kg" -> tin.tinQuantity * 1000
            else -> 0.0
        }
    }

    fun updateSorting(mainOption: PlaintextSortOption? = null, subOption: PlaintextSortOption? = null) {
        viewModelScope.launch{
            val currentSort = sortState.value
            val options = sortOptions.value

            if (subOption != null) {
                if (currentSort.subSort == subOption) {
                    preferencesRepo.setPtSort(currentSort.mainSort, !currentSort.ascending) }
                else { preferencesRepo.setPtSubSort(subOption) } }
            else if (mainOption != null) {
                val newMain = currentSort.mainSort != mainOption
                val hasSub = options.subOptions.containsKey(mainOption)

                if (newMain) {
                    preferencesRepo.setPtSort(mainOption, true)
                    if (hasSub) {
                        val sub = if (mainOption in TIN_SORT_VALUES) PlaintextSortOption.TIN_DEFAULT else PlaintextSortOption.DEFAULT
                        preferencesRepo.setPtSubSort(sub)
                    }
                }
                else { if (!hasSub) preferencesRepo.setPtSort(mainOption, !currentSort.ascending) }
            }
        }
    }

    fun saveFormatting(format: String? = null, delimiter: String? = null, listAsTins: Boolean? = null) {
        val currentListAsTins = _listAsTins.value

        if (format != null) formatStringState.edit { replace(0, length, format) }
        if (delimiter != null) delimiterState.edit { replace(0, length, delimiter) }
        _listAsTins.value = listAsTins ?: _listAsTins.value

        if (listAsTins != null && currentListAsTins != listAsTins) {
            val mainOption = if (listAsTins) PlaintextSortOption.TIN_DEFAULT else PlaintextSortOption.DEFAULT
            if (mainOption != sortState.value.mainSort) updateSorting(mainOption = mainOption)
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
    val delimiter: String = ""
)

@Stable
data class PrintOptions(
    val font: Float = 12f,
    val margin: Double = 1.0,
)

@Immutable
enum class PlaintextSortOption(@StringRes val resId: Int) {
   DEFAULT(R.string.item_default_v2),
   BRAND(R.string.brand_v2),
   BLEND(R.string.blend_v2),
   TYPE(R.string.type_v2),
   SUBGENRE(R.string.subgenre_v2),
   CUT(R.string.cut_v2),
   QUANTITY(R.string.quantity_v2),
   RATING(R.string.rating_v2),
   TIN_DEFAULT(R.string.tin_default_v2),
   TIN_LABEL(R.string.tin_label_v2),
   TIN_CONTAINER(R.string.tin_container_v2),
   TIN_QUANTITY(R.string.tin_quantity_v2),
   TIN_MANUF(R.string.manufacture_date_v2),
   TIN_CELLAR(R.string.cellar_date_v2),
   TIN_OPEN(R.string.open_date_v2)
}

@Stable
data class PlaintextSorting(
    val mainSort: PlaintextSortOption = PlaintextSortOption.DEFAULT,
    val ascending: Boolean = true,
    val subSort: PlaintextSortOption = PlaintextSortOption.DEFAULT,
    val icon: Int =
        when (mainSort) {
            PlaintextSortOption.QUANTITY, PlaintextSortOption.RATING -> if (ascending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (ascending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
)

data class SortingOptions(
    val mainOptions: List<PlaintextSortOption> = emptyList(),
    val subOptions:  Map<PlaintextSortOption, List<PlaintextSortOption>> = emptyMap()
)

data class SortMenuState(
    val mainSelection: PlaintextSortOption = PlaintextSortOption.DEFAULT,
    val subSelection: PlaintextSortOption = PlaintextSortOption.DEFAULT
)