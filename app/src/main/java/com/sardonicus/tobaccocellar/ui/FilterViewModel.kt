package com.sardonicus.tobaccocellar.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.ExportType
import com.sardonicus.tobaccocellar.MenuState
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.addEditItems.ItemSavedEvent
import com.sardonicus.tobaccocellar.ui.addEditItems.ItemUpdatedEvent
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.home.SearchClearedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchPerformedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchSetting
import com.sardonicus.tobaccocellar.ui.home.calculateTotalQuantity
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.settings.ExportRating
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.SyncDownloadEvent
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

class FilterViewModel (
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo
): ViewModel() {

    /** BottomSheet State **/
    private val _bottomSheetState = MutableStateFlow(BottomSheetState.CLOSED)
    val bottomSheetState: StateFlow<BottomSheetState> = _bottomSheetState.asStateFlow()

    fun openBottomSheet() { _bottomSheetState.value = BottomSheetState.OPENED }
    fun closeBottomSheet() { _bottomSheetState.value = BottomSheetState.CLOSED }


    /** HomeScreen HomeHeader blend search **/
    private val _searchMenuExpanded = MutableStateFlow(false)
    val searchMenuExpanded: StateFlow<Boolean> = _searchMenuExpanded.asStateFlow()

    fun setSearchMenuExpanded(expanded: Boolean) { _searchMenuExpanded.value = expanded }

    fun saveSearchSetting(setting: String) {
        viewModelScope.launch(Dispatchers.Default) { preferencesRepo.setSearchSetting(setting) }
    }

    private val _searchTextDisplay = MutableStateFlow("")
    val searchTextDisplay: StateFlow<String> = _searchTextDisplay

    fun updateSearchText(text: String) { _searchTextDisplay.value = text }

    private val _searchPerformed = MutableStateFlow(false)
    val searchPerformed: StateFlow<Boolean> = _searchPerformed.asStateFlow()

    private val _searchValue = MutableStateFlow("")
    val searchValue: StateFlow<String> = _searchValue

    fun onSearch(text: String) { _searchValue.value = text }

    private val _searchFocused = MutableStateFlow(false)
    val searchFocused: StateFlow<Boolean> = _searchFocused.asStateFlow()

    fun updateSearchFocused(focused: Boolean) { _searchFocused.value = focused }

    private val _isTinSearch = MutableStateFlow(false)
    val isTinSearch: StateFlow<Boolean> = _isTinSearch

    init {
        viewModelScope.launch {
            preferencesRepo.searchSetting
                .flowOn(Dispatchers.Default)
                .collect {
                _isTinSearch.value = (it == SearchSetting.TinLabel)
            }
        }
    }


    /** Filter states **/
    val brandSearchText = MutableStateFlow("")
    fun updateBrandSearchText(text: String) { brandSearchText.value = text }


    // inclusionary filter states //
    private val _selectedBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedBrands: StateFlow<List<String>> = _selectedBrands

    private val _selectedTypes = MutableStateFlow<List<String>>(emptyList())
    val selectedTypes: StateFlow<List<String>> = _selectedTypes

    private val _selectedFavorites = MutableStateFlow(false)
    val selectedFavorites: StateFlow<Boolean> = _selectedFavorites

    private val _selectedDislikeds = MutableStateFlow(false)
    val selectedDislikeds: StateFlow<Boolean> = _selectedDislikeds

    private val _selectedUnrated = MutableStateFlow(false)
    val selectedUnrated: StateFlow<Boolean> = _selectedUnrated

    private val _selectedRatingLow = MutableStateFlow<Double?>(null)
    val selectedRatingLow: StateFlow<Double?> = _selectedRatingLow

    private val _selectedRatingHigh = MutableStateFlow<Double?>(null)
    val selectedRatingHigh: StateFlow<Double?> = _selectedRatingHigh

    private val _selectedInStock = MutableStateFlow(false)
    val selectedInStock: StateFlow<Boolean> = _selectedInStock

    private val _selectedOutOfStock = MutableStateFlow(false)
    val selectedOutOfStock: StateFlow<Boolean> = _selectedOutOfStock

    private val _selectedSubgenres = MutableStateFlow<List<String>>(emptyList())
    val selectedSubgenres: StateFlow<List<String>> = _selectedSubgenres

    private val _selectedCuts = MutableStateFlow<List<String>>(emptyList())
    val selectedCuts: StateFlow<List<String>> = _selectedCuts

    private val _selectedComponents = MutableStateFlow<List<String>>(emptyList())
    val selectedComponents: StateFlow<List<String>> = _selectedComponents

    private val _compMatching = MutableStateFlow(FlowMatchOption.ANY)
    val compMatching: StateFlow<FlowMatchOption> = _compMatching

    private val _selectedFlavorings = MutableStateFlow<List<String>>(emptyList())
    val selectedFlavorings: StateFlow<List<String>> = _selectedFlavorings

    private val _flavorMatching = MutableStateFlow(FlowMatchOption.ANY)
    val flavorMatching: StateFlow<FlowMatchOption> = _flavorMatching

    private val _selectedProduction = MutableStateFlow(false)
    val selectedProduction: StateFlow<Boolean> = _selectedProduction

    private val _selectedOutOfProduction = MutableStateFlow(false)
    val selectedOutOfProduction: StateFlow<Boolean> = _selectedOutOfProduction

    private val _selectedContainer = MutableStateFlow<List<String>>(emptyList())
    val selectedContainer: StateFlow<List<String>> = _selectedContainer

    private val _selectedHasTins = MutableStateFlow(false)
    val selectedHasTins: StateFlow<Boolean> = _selectedHasTins

    private val _selectedNoTins = MutableStateFlow(false)
    val selectedNoTins: StateFlow<Boolean> = _selectedNoTins

    private val _selectedOpened = MutableStateFlow(false)
    val selectedOpened: StateFlow<Boolean> = _selectedOpened

    private val _selectedUnopened = MutableStateFlow(false)
    val selectedUnopened: StateFlow<Boolean> = _selectedUnopened

    private val _selectedFinished = MutableStateFlow(false)
    val selectedFinished: StateFlow<Boolean> = _selectedFinished

    private val _selectedUnfinished = MutableStateFlow(false)
    val selectedUnfinished: StateFlow<Boolean> = _selectedUnfinished

    // tins switch and filtering
    @Suppress("UNCHECKED_CAST")
    val showTins: StateFlow<Boolean> = combine(
        selectedContainer, selectedOpened, selectedUnopened, selectedFinished, selectedUnfinished,
        isTinSearch, searchPerformed
    ) {
        val container = it[0] as List<String>
        val opened = it[1] as Boolean
        val unopened = it[2] as Boolean
        val finished = it[3] as Boolean
        val unfinished = it[4] as Boolean
        val isTinSearch = it[5] as Boolean
        val searchPerformed = it[6] as Boolean

        (container.isNotEmpty() || opened || unopened || finished || unfinished) || (isTinSearch && searchPerformed)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )


    // exclusionary filter states //
    val excludeBrandSwitch = MutableStateFlow(false)

    private val _selectedExcludeBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedExcludeBrands: StateFlow<List<String>> = _selectedExcludeBrands

    private val _selectedExcludeFavorites = MutableStateFlow(false)
    val selectedExcludeFavorites: StateFlow<Boolean> = _selectedExcludeFavorites

    private val _selectedExcludeDislikeds = MutableStateFlow(false)
    val selectedExcludeDislikeds: StateFlow<Boolean> = _selectedExcludeDislikeds

    @Suppress("UNCHECKED_CAST")
    val filterSelectionsFlow: StateFlow<SheetSelections> =
        combine(
            selectedBrands, selectedExcludeBrands, selectedTypes, selectedFavorites,
            selectedExcludeFavorites, selectedDislikeds, selectedExcludeDislikeds, selectedInStock,
            selectedOutOfStock, selectedSubgenres, selectedCuts, selectedComponents, compMatching,
            selectedFlavorings, flavorMatching, selectedHasTins, selectedNoTins, selectedOpened,
            selectedUnopened, selectedFinished, selectedUnfinished, selectedContainer,
            selectedProduction, selectedOutOfProduction, selectedUnrated, selectedRatingLow,
            selectedRatingHigh
        ) {
            SheetSelections(
                brands = it[0] as List<String>,
                excludeBrands = it[1] as List<String>,
                types = it[2] as List<String>,
                favorites = it[3] as Boolean,
                excludeFavorites = it[4] as Boolean,
                dislikeds = it[5] as Boolean,
                excludeDislikeds = it[6] as Boolean,
                inStock = it[7] as Boolean,
                outOfStock = it[8] as Boolean,
                subgenres = it[9] as List<String>,
                cuts = it[10] as List<String>,
                components = it[11] as List<String>,
                compMatching = it[12] as FlowMatchOption,
                flavorings = it[13] as List<String>,
                flavorMatching = it[14] as FlowMatchOption,
                hasTins = it[15] as Boolean,
                noTins = it[16] as Boolean,
                opened = it[17] as Boolean,
                unopened = it[18] as Boolean,
                finished = it[19] as Boolean,
                unfinished = it[20] as Boolean,
                container = it[21] as List<String>,
                production = it[22] as Boolean,
                outOfProduction = it[23] as Boolean,
                unrated = it[24] as Boolean,
                ratingLow = it[25] as Double?,
                ratingHigh = it[26] as Double?
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(50L),
                initialValue = SheetSelections()
            )

    val isFilterApplied: StateFlow<Boolean> = filterSelectionsFlow.map {
        it.brands.isNotEmpty() || it.excludeBrands.isNotEmpty() || it.types.isNotEmpty() ||
                it.favorites || it.excludeFavorites || it.dislikeds || it.excludeDislikeds
                || it.unrated || it.ratingLow != null || it.ratingHigh != null || it.inStock
                || it.outOfStock || it.subgenres.isNotEmpty() || it.cuts.isNotEmpty()
                || it.components.isNotEmpty() || it.flavorings.isNotEmpty() || it.hasTins
                || it.noTins || it.opened || it.unopened || it.finished || it.unfinished
                || it.container.isNotEmpty() || it.production || it.outOfProduction
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )


    /** Cellar screen scroll state **/
    private val _shouldScrollUp = MutableStateFlow(false)
    val shouldScrollUp: StateFlow<Boolean> = _shouldScrollUp.asStateFlow()

    private val _savedItemId = MutableStateFlow(-1)
    val savedItemId: StateFlow<Int> = _savedItemId.asStateFlow()

    private val _shouldReturn = MutableStateFlow(false)
    val shouldReturn: StateFlow<Boolean> = _shouldReturn.asStateFlow()

    private val _getPosition = MutableStateFlow(0)
    val getPosition: StateFlow<Int> = _getPosition.asStateFlow()

    private val _searchCleared = MutableStateFlow(false)

    // remember scroll position //
    private val _currentPosition = MutableStateFlow(mapOf(0 to 0, 1 to 0))
    val currentPosition: StateFlow<Map<Int, Int>> = _currentPosition.asStateFlow()

    fun updateScrollPosition(index: Int, offset: Int) { _currentPosition.value = mapOf(0 to index, 1 to offset) }

    fun getPositionTrigger() {
        _getPosition.value++
        _shouldReturn.value = true
    }

    fun shouldScrollUp() { _shouldScrollUp.value = true }

    fun resetScroll() {
        _shouldScrollUp.value = false
        _shouldReturn.value = false
        _getPosition.value = 0
        _searchCleared.value = false
        _searchPerformed.value = false
        _savedItemId.value = -1
        _currentPosition.value = mapOf(0 to 0, 1 to 0)
    }

    private val _refresh = MutableSharedFlow<Unit>(replay = 0)
    private val refresh = _refresh.asSharedFlow()


    /** Events from EventBus **/
    init {
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is ItemSavedEvent) {
                    resetFilter()
                    _shouldScrollUp.value = false
                    _shouldReturn.value = false
                    _savedItemId.value = it.savedItemId.toInt()
                }
                if (it is ItemUpdatedEvent) {
                    _shouldReturn.value = true
                }
                if (it is SearchClearedEvent) {
                    _searchPerformed.value = false
                    _searchCleared.value = true
                    _shouldReturn.value = true
                }
                if (it is SearchPerformedEvent) {
                    _searchPerformed.value = true
                }
                if (it is DatabaseRestoreEvent) {
                    resetFilter()
                    _refresh.emit(Unit)
                    delay(25.milliseconds)
                    _refresh.emit(Unit)
                    _shouldScrollUp.value = true
                }
                if (it is SyncDownloadEvent) {
                    _refresh.emit(Unit)
                }
            }
        }
    }


    /** Home Scroll State stuff **/
    @Suppress("UNCHECKED_CAST")
    val homeScrollState: StateFlow<HomeScrollState> = combine(
        currentPosition,
        shouldScrollUp,
        savedItemId,
        shouldReturn,
        getPosition
    ) { values ->
        val position = values[0] as Map<Int, Int>
        val scrollUp = values[1] as Boolean
        val savedItemId = values[2] as Int
        val doReturn = values[3] as Boolean
        val getPosition = values[4] as Int

        HomeScrollState(position, scrollUp, savedItemId, doReturn, getPosition)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeScrollState()
        )


    /** Filtering states **/
    // available fields for filter //
    private val _autoComplete = MutableStateFlow(AutoCompleteData())
    val autoComplete: StateFlow<AutoCompleteData> = _autoComplete

    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands

    private val _availableTypes = MutableStateFlow<List<String>>(emptyList())
    val availableTypes: StateFlow<List<String>> = _availableTypes

    private val _availableSubgenres = MutableStateFlow<List<String>>(emptyList())
    val availableSubgenres: StateFlow<List<String>> = _availableSubgenres

    private val _availableCuts = MutableStateFlow<List<String>>(emptyList())
    val availableCuts: StateFlow<List<String>> = _availableCuts

    private val _availableComponents = MutableStateFlow<List<String>>(emptyList())
    val availableComponents: StateFlow<List<String>> = _availableComponents

    private val _availableFlavorings = MutableStateFlow<List<String>>(emptyList())
    val availableFlavorings: StateFlow<List<String>> = _availableFlavorings

    private val _availableContainers = MutableStateFlow<List<String>>(emptyList())
    val availableContainers: StateFlow<List<String>> = _availableContainers

    private val _typesExist = MutableStateFlow(true)
    val typesExist: StateFlow<Boolean> = _typesExist

    private val _subgenresExist = MutableStateFlow(true)
    val subgenresExist: StateFlow<Boolean> = _subgenresExist

    private val _ratingsExist = MutableStateFlow(true)
    val ratingsExist: StateFlow<Boolean> = _ratingsExist

    private val _favDisExist = MutableStateFlow(true)
    val favDisExist: StateFlow<Boolean> = _favDisExist

    private val _tinsExist = MutableStateFlow(true)
    val tinsExist: StateFlow<Boolean> = _tinsExist

    private val _tinsReady = MutableStateFlow(false)
    val tinsReady: StateFlow<Boolean> = _tinsReady

    private val _notesExist = MutableStateFlow(true)
    val notesExist: StateFlow<Boolean> = _notesExist

    private val _datesExist = MutableStateFlow(true)
    val datesExist: StateFlow<Boolean> = _datesExist

    private val _emptyDatabase = MutableStateFlow(false)
    val emptyDatabase: StateFlow<Boolean> = _emptyDatabase

    // database refresh on restore
    @OptIn(ExperimentalCoroutinesApi::class)
    val everythingFlow: Flow<List<ItemsComponentsAndTins>> =
        refresh.onStart { emit(Unit) }.flatMapLatest {
            itemsRepository.getEverythingStream()
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)


    // setting available vals
    private val _selectionHistory = MutableStateFlow<List<Pair<FilterCategory, Any?>>>(emptyList())
    init {
        viewModelScope.launch(Dispatchers.Default) {
            everythingFlow.collectLatest { data ->
                if (data.isEmpty()) {
                    _availableBrands.value = emptyList()
                    _availableTypes.value = emptyList()
                    _availableSubgenres.value = emptyList()
                    _availableCuts.value = emptyList()
                    _availableComponents.value = emptyList()
                    _availableFlavorings.value = emptyList()
                    _availableContainers.value = emptyList()
                    _typesExist.value = false
                    _subgenresExist.value = false
                    _ratingsExist.value = false
                    _favDisExist.value = false
                    _tinsExist.value = false
                    _notesExist.value = false
                    _datesExist.value = false
                    _emptyDatabase.value = true
                    _autoComplete.value = AutoCompleteData()
                    return@collectLatest
                }

                val brands = mutableSetOf<String>()
                val types = mutableSetOf<String>()
                val subgenres = mutableSetOf<String>()
                val cuts = mutableSetOf<String>()
                val components = mutableSetOf<String>()
                val flavorings = mutableSetOf<String>()
                val containers = mutableSetOf<String>()

                var typesAny = false
                var subgenresAny = false
                var ratingsAny = false
                var favDisAny = false
                var tinsAny = false
                var notesAny = false
                var datesAny = false

                for (itemFull in data) {
                    val item = itemFull.items

                    brands.add(item.brand)
                    types.add(item.type.ifBlank { "(Unassigned)" })
                    subgenres.add(item.subGenre.ifBlank { "(Unassigned)" })
                    cuts.add(item.cut.ifBlank { "(Unassigned)" })

                    if (item.type.isNotBlank()) typesAny = true
                    if (item.subGenre.isNotBlank()) subgenresAny = true
                    if (item.rating != null) ratingsAny = true
                    if (item.favorite || item.disliked) favDisAny = true
                    if (item.notes.isNotBlank()) notesAny = true

                    if (itemFull.components.isEmpty()) { components.add("(None Assigned)") }
                    else {
                        for (comp in itemFull.components) { components.add(comp.componentName) }
                    }

                    if (itemFull.flavoring.isEmpty()) { flavorings.add("(None Assigned)") }
                    else {
                        for (flavor in itemFull.flavoring) { flavorings.add(flavor.flavoringName) }
                    }

                    if (itemFull.tins.isNotEmpty()) {
                        tinsAny = true
                        for (tin in itemFull.tins) {
                            containers.add(tin.container.ifBlank { "(Unassigned)" })
                            if (!datesAny && (tin.manufactureDate != null || tin.cellarDate != null || tin.openDate != null)) {
                                datesAny = true
                            }
                        }
                    }
                }

                val placeComparator = compareBy<String> {
                    if (it == "(Unassigned)" || it == "(None Assigned)") 1 else 0
                }.thenBy { it.lowercase() }

                _availableBrands.value = brands.sorted()
                _availableTypes.value = types.sortedWith(compareBy { typeOrder[it] ?: typeOrder.size })
                _availableSubgenres.value = subgenres.sortedWith(placeComparator)
                _availableCuts.value = cuts.sortedWith(placeComparator)
                _availableComponents.value = components.sortedWith(placeComparator)
                _availableFlavorings.value = flavorings.sortedWith(placeComparator)
                _availableContainers.value = containers.sortedWith(placeComparator)

                _typesExist.value = typesAny
                _subgenresExist.value = subgenresAny
                _ratingsExist.value = ratingsAny
                _favDisExist.value = favDisAny
                _tinsExist.value = tinsAny
                _notesExist.value = notesAny
                _datesExist.value = datesAny
                _emptyDatabase.value = false
                _autoComplete.value = AutoCompleteData(
                    brands = _availableBrands.value,
                    subgenres = _availableSubgenres.value.filter { it != "(Unassigned)" },
                    cuts = _availableCuts.value.filter { it != "(Unassigned)" },
                    components = _availableComponents.value.filter { it != "(None Assigned)" },
                    flavorings = _availableFlavorings.value.filter { it != "(None Assigned)" },
                    tinContainers = _availableContainers.value.filter { it != "(Unassigned)" }
                )

                val invalid = _selectionHistory.value.filter { (cat, value) ->
                    isSelectionInvalid(cat, value)
                }
                if (invalid.isNotEmpty()) {
                    invalid.forEach { (cat, value) -> removeFilter(cat, value) }
                }
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            combine (
                everythingFlow,
                preferencesRepo.datesSeen
            ) { everything, datesString ->
                val lastSeen = datesString.split(",").mapNotNull { it.trim().toIntOrNull() }
                everything.flatMap { it.tins }
                    .filter { tins ->
                        tins.openDate?.let {
                            Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate() in LocalDate.now()..LocalDate.now().plusDays(7)
                        } ?: false
                    }.map { it.tinId }.any { it !in lastSeen }
            }.collect {
                _tinsReady.value = it
            }
        }
    }


    /** Single Source Filtering **/
//    private val _activeScreen = MutableStateFlow(ActiveScreen.HOME)
//    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()
//
//    fun setActiveScreen(screen: ActiveScreen) { _activeScreen.value = screen }

    // Filtering function //
    @Suppress("UNCHECKED_CAST")
    val unifiedFilteredItems: StateFlow<List<ItemsComponentsAndTins>> =
        combine(
            everythingFlow,
            selectedBrands,
            selectedExcludeBrands,
            selectedTypes,
            selectedFavorites,
            selectedExcludeFavorites,
            selectedDislikeds,
            selectedExcludeDislikeds,
            selectedInStock,
            selectedOutOfStock,
            selectedSubgenres,
            selectedCuts,
            selectedComponents,
            compMatching,
            selectedFlavorings,
            flavorMatching,
            selectedProduction,
            selectedOutOfProduction,
            selectedHasTins,
            selectedNoTins,
            selectedContainer,
            selectedOpened,
            selectedUnopened,
            selectedFinished,
            selectedUnfinished,
            selectedUnrated,
            selectedRatingLow,
            selectedRatingHigh,
            preferencesRepo.quantityOption,
            preferencesRepo.tinOzConversionRate,
            preferencesRepo.tinGramsConversionRate
        ) { values ->
            val allItems = values[0] as List<ItemsComponentsAndTins>
            val brands = values[1] as List<String>
            val excludeBrands = values[2] as List<String>
            val types = values[3] as List<String>
            val favorites = values[4] as Boolean
            val excludeLikes = values[5] as Boolean
            val dislikeds = values[6] as Boolean
            val excludeDislikes = values[7] as Boolean
            val inStock = values[8] as Boolean
            val outOfStock = values[9] as Boolean
            val subgenres = values[10] as List<String>
            val cuts = values[11] as List<String>
            val components = values[12] as List<String>
            val compMatching = values[13] as FlowMatchOption
            val flavoring = values[14] as List<String>
            val flavorMatching = values[15] as FlowMatchOption
            val production = values[16] as Boolean
            val outOfProduction = values[17] as Boolean
            val hasTins = values[18] as Boolean
            val noTins = values[19] as Boolean
            val container = values[20] as List<String>
            val opened = values[21] as Boolean
            val unopened = values[22] as Boolean
            val finished = values[23] as Boolean
            val unfinished = values[24] as Boolean
            val unrated = values[25] as Boolean
            val ratingLow = values[26] as Double?
            val ratingHigh = values[27] as Double?
            val quantityOption = values[28] as QuantityOption
            val ozRate = values[29] as Double
            val gramsRate = values[30] as Double

            val applyTin = true

            generateFilteredItemsList(
                allItems, brands, excludeBrands, types, favorites, dislikeds, excludeLikes,
                excludeDislikes, inStock, outOfStock, subgenres, cuts, components, compMatching,
                flavoring, flavorMatching, production, outOfProduction, hasTins, noTins, container,
                opened, unopened, finished, unfinished, unrated, ratingLow, ratingHigh, quantityOption,
                ozRate, gramsRate, applyTin
            )
        }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @Suppress("UNCHECKED_CAST")
    val homeScreenFilteredItems: StateFlow<List<ItemsComponentsAndTins>> =
        combine(
            everythingFlow,
            unifiedFilteredItems,
            searchValue,
            preferencesRepo.searchSetting
        ) { values ->
            val allItems = values[0] as List<ItemsComponentsAndTins>
            val filteredItems = values[1] as List<ItemsComponentsAndTins>
            val currentSearchValue = values[2] as String
            val currentSearchSetting = values[3] as SearchSetting

            if (currentSearchValue.isNotBlank()) {
                allItems.filter { items ->
                    when (currentSearchSetting) {
                        SearchSetting.Blend -> items.items.blend.contains(currentSearchValue, true)
                        SearchSetting.Notes -> items.items.notes.contains(currentSearchValue, true)
                        SearchSetting.TinLabel -> items.tins.any { it.tinLabel.contains(currentSearchValue, true) }
                    }
                }
            }
            else filteredItems
        }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @Suppress("SameParameterValue")
    private fun generateFilteredItemsList(
        allItems: List<ItemsComponentsAndTins>,

        brands: List<String>, excludeBrands: List<String>, types: List<String>, favorites: Boolean,
        dislikeds: Boolean, excludeFavorites: Boolean, excludeDislikeds: Boolean, inStock: Boolean,
        outOfStock: Boolean, subgenres: List<String>, cuts: List<String>, components: List<String>,
        compMatching: FlowMatchOption, flavorings: List<String>, flavorMatching: FlowMatchOption, production: Boolean,
        outOfProduction: Boolean, hasTins: Boolean, noTins: Boolean, container: List<String>,
        opened: Boolean, unopened: Boolean, finished: Boolean, unfinished: Boolean, unrated: Boolean,
        ratingLow: Double?, ratingHigh: Double?, quantityOption: QuantityOption, ozRate: Double,
        gramsRate: Double,

        applyTinFilter: Boolean
    ): List<ItemsComponentsAndTins> {
        if (allItems.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()

        return allItems.filter { items ->
            val tinFiltering = if (!applyTinFilter) items.tins else items.tins.filter { tin ->
                (!opened || (tin.openDate != null && tin.openDate < now)) &&
                        (!unopened || ((tin.openDate == null || tin.openDate >= now) && !tin.finished)) &&
                        (!finished || tin.finished) &&
                        (!unfinished || !tin.finished) &&
                        (container.isEmpty() || container.contains(tin.container.ifBlank { "(Unassigned)" }))
            }

            val compMatch = when (compMatching) {
                FlowMatchOption.ALL -> (components.isEmpty() || (components == listOf("(None Assigned)") && items.components.isEmpty()) || items.components.map { it.componentName }.containsAll(components))
                FlowMatchOption.ONLY -> (components.isEmpty() || (components == listOf("(None Assigned)") && items.components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components) && items.components.size == components.size))
                else -> (components.isEmpty() || ((components.contains("(None Assigned)") && items.components.isEmpty()) || items.components.map { it.componentName }.any { components.contains(it) }))
            }
            val flavorMatch = when (flavorMatching) {
                FlowMatchOption.ALL -> (flavorings.isEmpty() || (flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || items.flavoring.map { it.flavoringName }.containsAll(flavorings))
                FlowMatchOption.ONLY -> (flavorings.isEmpty() || (flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.containsAll(flavorings) && items.flavoring.size == flavorings.size))
                else -> (flavorings.isEmpty() || ((flavorings.contains("(None Assigned)") && items.flavoring.isEmpty()) || items.flavoring.map { it.flavoringName }.any { flavorings.contains(it) }))
            }

            val quantity = calculateTotalQuantity(items, tinFiltering, quantityOption, ozRate, gramsRate)
            val isInStock = quantity > 0.0

            val ratingRangeLow = (ratingLow != null && (items.items.rating != null && (items.items.rating >= ratingLow)))
            val ratingRangeHigh = (ratingHigh != null && (items.items.rating != null && (items.items.rating <= ratingHigh)))

            val baseFilters =
                (brands.isEmpty() || brands.contains(items.items.brand)) &&
                        (excludeBrands.isEmpty() || !excludeBrands.contains(items.items.brand)) &&
                        (types.isEmpty() || types.contains(items.items.type.ifBlank { "(Unassigned)" })) &&
                        (!favorites || if (dislikeds) (items.items.disliked || items.items.favorite) else items.items.favorite) &&
                        (!excludeFavorites || !items.items.favorite) &&
                        (!dislikeds || if (favorites) (items.items.favorite || items.items.disliked) else items.items.disliked) &&
                        (!excludeDislikeds || !items.items.disliked) &&
                        (!inStock || isInStock) &&
                        (!outOfStock || !isInStock) &&
                        compMatch &&
                        flavorMatch &&
                        (subgenres.isEmpty() || subgenres.contains(items.items.subGenre.ifBlank { "(Unassigned)" })) &&
                        (cuts.isEmpty() || cuts.contains(items.items.cut.ifBlank { "(Unassigned)" })) &&
                        (!production || items.items.inProduction) &&
                        (!outOfProduction || !items.items.inProduction) &&
                        (!unrated || (items.items.rating == null || ratingRangeLow || ratingRangeHigh)) &&
                        (ratingLow == null || (if (unrated) (items.items.rating == null || ratingRangeLow) else ratingRangeLow)) &&
                        (ratingHigh == null || (if (unrated) (items.items.rating == null || ratingRangeHigh) else ratingRangeHigh))

            val tinsFilterResult = if (!applyTinFilter) true else {
                (!hasTins || items.tins.isNotEmpty()) &&
                        (!noTins || items.tins.isEmpty()) &&
                        (!opened || tinFiltering.isNotEmpty()) &&
                        (!unopened || tinFiltering.isNotEmpty()) &&
                        (!finished || tinFiltering.isNotEmpty()) &&
                        (!unfinished || tinFiltering.isNotEmpty()) &&
                        (container.isEmpty() || tinFiltering.isNotEmpty())
            }

            baseFilters && tinsFilterResult
        }
    }

    @Suppress("UNCHECKED_CAST")
    val unifiedFilteredTins: StateFlow<List<Tins>> =
        combine(
            everythingFlow,
            selectedContainer,
            selectedOpened,
            selectedUnopened,
            selectedFinished,
            selectedUnfinished,
        ) { values ->
            val items = values[0] as List<ItemsComponentsAndTins>
            val container = values[1] as List<String>
            val opened = values[2] as Boolean
            val unopened = values[3] as Boolean
            val finished = values[4] as Boolean
            val unfinished = values[5] as Boolean

            generateFilteredTinsList(items, container, opened, unopened, finished, unfinished)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = listOf()
            )

    @Suppress("UNCHECKED_CAST")
    val homeScreenFilteredTins: StateFlow<List<Tins>> =
        combine(
            everythingFlow,
            unifiedFilteredTins,
            searchValue,
            preferencesRepo.searchSetting
        ) { values ->
            val allItems = values[0] as List<ItemsComponentsAndTins>
            val filteredTins = values[1] as List<Tins>
            val currentSearchValue = values[2] as String
            val currentSearchSetting = values[3] as SearchSetting

            if (currentSearchValue.isBlank()) {
                filteredTins
            } else {
                if (currentSearchSetting == SearchSetting.TinLabel) {
                    allItems.flatMap { it.tins }.filter {
                        it.tinLabel.contains(currentSearchValue, true)
                    }
                } else {
                    emptyList()
                }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = listOf()
            )

    private fun generateFilteredTinsList(
        allItems: List<ItemsComponentsAndTins>, container: List<String>, opened: Boolean,
        unopened: Boolean, finished: Boolean, unfinished: Boolean,
    ): List<Tins> {
        val now = System.currentTimeMillis()
        val checkContainer = container.isNotEmpty()
        val hasUnassigned = if (checkContainer) container.contains("(Unassigned)") else false

        return allItems.flatMap { it.tins }.filter {
            val mContainer = !checkContainer || (hasUnassigned && it.container.isBlank()) || container.contains(it.container)
            val mOpen = (!opened || (it.openDate != null && it.openDate < now)) && (!unopened || (!it.finished && (it.openDate == null || it.openDate >= now)))
            val mFinish = (!finished || it.finished) && (!unfinished || !it.finished)

            mContainer && mOpen && mFinish
        }
    }


    /** Enable/Disable Specific Filters based on Existing filtering **/
    @Suppress("UNCHECKED_CAST")
    val enablementState: StateFlow<EnablementState> = combine (
        everythingFlow,
        filterSelectionsFlow,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate
    ) { array ->
        val allItems = array[0] as List<ItemsComponentsAndTins>
        val selections = array[1] as SheetSelections
        val quantityOption = array[2] as QuantityOption
        val ozRate = array[3] as Double
        val gramsRate = array[4] as Double

        if (allItems.isEmpty()) return@combine EnablementState()
        val now = System.currentTimeMillis()

        val brandsMap = mutableMapOf<String, Boolean>()
        val excludeBrandsMap = mutableMapOf<String, Boolean>()
        val typesMap = mutableMapOf<String, Boolean>()
        val subgenresMap = mutableMapOf<String, Boolean>()
        val cutsMap = mutableMapOf<String, Boolean>()
        val componentsMap = mutableMapOf<String, Boolean>()
        val flavoringsMap = mutableMapOf<String, Boolean>()
        val containersMap = mutableMapOf<String, Boolean>()
        val compMatchingMap = mutableMapOf<FlowMatchOption, Boolean>().apply {
            if (selections.components.isEmpty()) FlowMatchOption.entries.forEach { put(it, true) }
        }
        val flavorMatchingMap = mutableMapOf<FlowMatchOption, Boolean>().apply {
            if (selections.flavorings.isEmpty()) FlowMatchOption.entries.forEach { put(it, true) }
        }

        var favorites = false; var excludeFaves = false; var dislikes = false; var excludeDis = false
        var unrated = false; var inStock = false; var outOfStock = false
        var hasTins = false; var noTins = false; var opened = false; var unopened = false
        var finished = false; var unfinished = false; var prod = false; var outOfProd = false
        var minR: Double? = null; var maxR: Double? = null

        for (items in allItems) {
            val item = items.items

            val mBrand = selections.brands.isEmpty() || selections.brands.contains(item.brand)
            val mExBrand = selections.excludeBrands.isEmpty() || !selections.excludeBrands.contains(item.brand)
            val mType = selections.types.isEmpty() || (selections.types.contains(item.type.ifBlank { "(Unassigned)" }))
            val mProd = (!selections.production || item.inProduction) && (!selections.outOfProduction || !item.inProduction)
            val mSubgenre = selections.subgenres.isEmpty() || selections.subgenres.contains(item.subGenre.ifBlank { "(Unassigned)" })
            val mCut = selections.cuts.isEmpty() || selections.cuts.contains(item.cut.ifBlank { "(Unassigned)" })

            val mFavDis = run {
                val fav = !selections.favorites || (if (selections.dislikeds) (item.disliked || item.favorite) else item.favorite)
                val exFav = !selections.excludeFavorites || !item.favorite
                val dis = !selections.dislikeds || (if (selections.favorites) (item.favorite || item.disliked) else item.disliked)
                val exDis = !selections.excludeDislikeds || !item.disliked
                fav && exFav && dis && exDis
            }

            val mRating = run {
                val unrated = item.rating == null
                val unratedPassed = selections.unrated && unrated
                val rangeActive = selections.ratingLow != null || selections.ratingHigh != null
                val rangePassed = rangeActive && !unrated &&
                        (selections.ratingLow == null || item.rating >= selections.ratingLow) &&
                        (selections.ratingHigh == null || item.rating <= selections.ratingHigh)
                if (!selections.unrated && !rangeActive) true else (unratedPassed || rangePassed)
            }

            val itemComps = items.components.map { it.componentName }
            val mComp = when (selections.compMatching) {
                FlowMatchOption.ALL -> selections.components.isEmpty() || (selections.components == listOf("(None Assigned)") && items.components.isEmpty()) || itemComps.containsAll(selections.components)
                FlowMatchOption.ONLY -> selections.components.isEmpty() || (selections.components == listOf("(None Assigned)") && items.components.isEmpty()) || (itemComps.containsAll(selections.components) && items.components.size == selections.components.size)
                else -> selections.components.isEmpty() || ((selections.components.contains("(None Assigned)") && items.components.isEmpty()) || itemComps.any { selections.components.contains(it) })
            }

            val itemFlavor = items.flavoring.map { it.flavoringName }
            val mFlavor = when (selections.flavorMatching) {
                FlowMatchOption.ALL -> selections.flavorings.isEmpty() || (selections.flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || itemFlavor.containsAll(selections.flavorings)
                FlowMatchOption.ONLY -> selections.flavorings.isEmpty() || (selections.flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || (itemFlavor.containsAll(selections.flavorings) && itemFlavor.size == selections.flavorings.size)
                else -> selections.flavorings.isEmpty() || ((selections.flavorings.contains("(None Assigned)") && items.flavoring.isEmpty()) || itemFlavor.any { selections.flavorings.contains(it) })
            }

            val mTinExist = (!selections.hasTins || items.tins.isNotEmpty()) && (!selections.noTins || items.tins.isEmpty())

            val mTinsSatisfyAll = items.tins.any { tin ->
                (!selections.opened || (tin.openDate != null && tin.openDate < now) || tin.finished) &&
                        (!selections.unopened || ((tin.openDate == null || tin.openDate >= now) && !tin.finished)) &&
                        (!selections.finished || tin.finished) &&
                        (!selections.unfinished || !tin.finished) &&
                        (selections.container.isEmpty() || selections.container.contains(tin.container.ifBlank { "(Unassigned)" }))
            }

            val mOpen = if (!selections.opened && !selections.unopened) true else mTinsSatisfyAll
            val mFinish = if (!selections.finished && !selections.unfinished) true else mTinsSatisfyAll
            val mContainer = if (selections.container.isEmpty()) true else mTinsSatisfyAll

            val quantity = run {
                val tinFiltered = items.tins.filter { tin ->
                    (!selections.opened || (tin.openDate != null && tin.openDate < now)) &&
                            (!selections.unopened || ((tin.openDate == null || tin.openDate >= now) && !tin.finished)) &&
                            (!selections.finished || tin.finished) &&
                            (!selections.unfinished || !tin.finished) &&
                            (selections.container.isEmpty() || selections.container.contains(tin.container.ifBlank { "(Unassigned)" }))
                }
                calculateTotalQuantity(items, tinFiltered, quantityOption, ozRate, gramsRate)
            }
            val mStock = (!selections.inStock || quantity > 0.0) && (!selections.outOfStock || quantity == 0.0)

            val results = arrayOf(mBrand, mExBrand, mType, mFavDis, mRating, mStock, mSubgenre, mCut, mComp, mFlavor, mTinExist, mOpen, mFinish, mContainer, mProd)
            val failCount = results.count { !it }

            if (failCount > 1) continue

            fun passesAllOthers(index: Int) = failCount == 0 || (failCount == 1 && !results[index])

            if (passesAllOthers(0)) brandsMap[item.brand] = true
            if (passesAllOthers(1)) excludeBrandsMap[item.brand] = true
            if (passesAllOthers(2)) typesMap[item.type.ifBlank { "(Unassigned)" }] = true

            if (passesAllOthers(3)) {
                if (item.favorite) favorites = true
                if (!item.favorite) excludeFaves = true
                if (item.disliked) dislikes = true
                if (!item.disliked) excludeDis = true
            }

            if (passesAllOthers(4)) {
                if (item.rating == null) unrated = true
                item.rating?.let { r ->
                    minR = if (minR == null) r else minOf(minR, r)
                    maxR = if (maxR == null) r else maxOf(maxR, r)
                }
            }

            if (passesAllOthers(5)) {
                val tinFiltered = items.tins.filter { tin ->
                    (!selections.opened || (tin.openDate != null && tin.openDate < now)) &&
                            (!selections.unopened || ((tin.openDate == null || tin.openDate >= now) && !tin.finished)) &&
                            (!selections.finished || tin.finished) &&
                            (!selections.unfinished || !tin.finished) &&
                            (selections.container.isEmpty() || selections.container.contains(tin.container.ifBlank { "(Unassigned)" }))
                }
                val qty = calculateTotalQuantity(items, tinFiltered, quantityOption, ozRate, gramsRate)
                if (qty > 0.0) inStock = true
                if (qty == 0.0) outOfStock = true
            }

            if (passesAllOthers(6)) subgenresMap[item.subGenre.ifBlank { "(Unassigned)" }] = true
            if (passesAllOthers(7)) cutsMap[item.cut.ifBlank { "(Unassigned)" }] = true

            if (passesAllOthers(8)) {
                val noneMatch = items.components.isEmpty() && (selections.components.isEmpty() || selections.components == listOf("(None Assigned)"))
                if (noneMatch && (selections.compMatching == FlowMatchOption.ANY || items.components.isEmpty())) {
                    componentsMap["(None Assigned)"] = true
                }

                if (!selections.components.contains("(None Assigned)")) {
                    itemComps.forEach { comp ->
                        val potential = (selections.components + comp).distinct()
                        val isEnabled = when (selections.compMatching) {
                            FlowMatchOption.ANY -> true
                            FlowMatchOption.ALL -> itemComps.containsAll(potential)
                            FlowMatchOption.ONLY -> itemComps.containsAll(potential) && itemComps.size == potential.size
                        }
                        if (isEnabled) componentsMap[comp] = true
                    }
                }

                if (selections.components.isNotEmpty()) {
                    FlowMatchOption.entries.forEach { option ->
                        val match = when (option) {
                            FlowMatchOption.ANY -> (selections.components.contains("(None Assigned)") && items.components.isEmpty()) || itemComps.any { selections.components.contains(it) }
                            FlowMatchOption.ALL -> (selections.components == listOf("(None Assigned)") && items.components.isEmpty()) || itemComps.containsAll(selections.components)
                            FlowMatchOption.ONLY -> (selections.components == listOf("(None Assigned)") && items.components.isEmpty()) || (itemComps.containsAll(selections.components) && itemComps.size == selections.components.size)
                        }
                        if (match) compMatchingMap[option] = true
                    }
                }
            }

            if (passesAllOthers(9)) {
                val noneMatch = items.flavoring.isEmpty() && (selections.flavorings.isEmpty() || selections.flavorings == listOf("(None Assigned)"))
                if (noneMatch && (selections.flavorMatching == FlowMatchOption.ANY || items.flavoring.isEmpty())) {
                    flavoringsMap["(None Assigned)"] = true
                }

                if (!selections.flavorings.contains("(None Assigned)")) {
                    itemFlavor.forEach { flavor ->
                        val potential = (selections.flavorings + flavor).distinct()
                        val isEnabled = when (selections.flavorMatching) {
                            FlowMatchOption.ANY -> true
                            FlowMatchOption.ALL -> itemFlavor.containsAll(potential)
                            FlowMatchOption.ONLY -> itemFlavor.containsAll(potential) && itemFlavor.size == potential.size
                        }
                        if (isEnabled) flavoringsMap[flavor] = true
                    }
                }

                if (selections.flavorings.isNotEmpty()) {
                    FlowMatchOption.entries.forEach { option ->
                        val match = when (option) {
                            FlowMatchOption.ANY -> (selections.flavorings.contains("(None Assigned)") && items.flavoring.isEmpty()) || itemFlavor.any { selections.flavorings.contains(it) }
                            FlowMatchOption.ALL -> (selections.flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || itemFlavor.containsAll(selections.flavorings)
                            FlowMatchOption.ONLY -> (selections.flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || (itemFlavor.containsAll(selections.flavorings) && itemFlavor.size == selections.flavorings.size)
                        }
                        if (match) flavorMatchingMap[option] = true
                    }
                }
            }

            if (passesAllOthers(10)) {
                if (items.tins.isNotEmpty()) hasTins = true
                if (items.tins.isEmpty()) noTins = true
            }

            if (passesAllOthers(11)) {
                val vTins = items.tins.filter { tin ->
                    (!selections.finished || tin.finished) && (!selections.unfinished || !tin.finished) &&
                            (selections.container.isEmpty() || selections.container.contains(tin.container.ifBlank { "(Unassigned)" }))
                }
                vTins.forEach { tin ->
                    val isOpen = tin.openDate != null && tin.openDate < now
                    val isUnopened = (tin.openDate == null || tin.openDate >= now) && !tin.finished
                    if (isOpen) opened = true
                    if (isUnopened) unopened = true
                }
            }

            if (passesAllOthers(12)) {
                val vTins = items.tins.filter { tin ->
                    (!selections.opened || (tin.openDate != null && tin.openDate < now)) &&
                            (!selections.unopened || ((tin.openDate == null || tin.openDate >= now) && !tin.finished)) &&
                            (selections.container.isEmpty() || selections.container.contains(tin.container.ifBlank { "(Unassigned)" }))
                }
                vTins.forEach { tin ->
                    if (tin.finished) finished = true
                    if (!tin.finished) unfinished = true
                }
            }

            if (passesAllOthers(13)) {
                val vTins = items.tins.filter { tin ->
                    (!selections.opened || (tin.openDate != null && tin.openDate < now)) &&
                            (!selections.unopened || ((tin.openDate == null || tin.openDate >= now) && !tin.finished)) &&
                            (!selections.finished || tin.finished) &&
                            (!selections.unfinished || !tin.finished)
                }
                vTins.groupBy { it.container.ifBlank { "(Unassigned)" } }.forEach { (name, tins) ->
                    containersMap[name] = true
                }
            }

            if (passesAllOthers(14)) {
                if (item.inProduction) prod = true
                if (!item.inProduction) outOfProd = true
            }
        }

        EnablementState(
            brands = brandsMap, excludeBrands = excludeBrandsMap, types = typesMap,
            favorites = favorites, excludeFavorites = excludeFaves, dislikeds = dislikes,
            excludeDislikes = excludeDis, unrated = unrated, ratingLow = minR, ratingHigh = maxR,
            inStock = inStock, outOfStock = outOfStock, subgenres = subgenresMap, cuts = cutsMap,
            components = componentsMap, compMatching = compMatchingMap, flavorings = flavoringsMap,
            flavorMatching = flavorMatchingMap, hasTins = hasTins, noTins = noTins, opened = opened,
            unopened = unopened, finished = finished, unfinished = unfinished,
            container = containersMap, production = prod, outOfProduction = outOfProd
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EnablementState()
        )

    val brandsEnabled = enablementState.map { it.brands }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val excludeBrandsEnabled = enablementState.map { it.excludeBrands }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val typesEnabled = enablementState.map { it.types }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val favoritesEnabled = enablementState.map { it.favorites }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val excludeFavoritesEnabled = enablementState.map { it.excludeFavorites }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val dislikedsEnabled = enablementState.map { it.dislikeds }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val excludeDislikesEnabled = enablementState.map { it.excludeDislikes }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val unratedEnabled = enablementState.map { it.unrated }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val ratingLowEnabled = enablementState.map { it.ratingLow }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val ratingHighEnabled = enablementState.map { it.ratingHigh }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val inStockEnabled = enablementState.map { it.inStock }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val outOfStockEnabled = enablementState.map { it.outOfStock }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val subgenresEnabled = enablementState.map { it.subgenres }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val cutsEnabled = enablementState.map { it.cuts }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val componentsEnabled = enablementState.map { it.components }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val compMatchingEnabled = enablementState.map { it.compMatching }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val flavoringsEnabled = enablementState.map { it.flavorings }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val flavorMatchingEnabled = enablementState.map { it.flavorMatching }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val hasTinsEnabled = enablementState.map { it.hasTins }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val noTinsEnabled = enablementState.map { it.noTins }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val openedEnabled = enablementState.map { it.opened }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val unopenedEnabled = enablementState.map { it.unopened }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val finishedEnabled = enablementState.map { it.finished }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val unfinishedEnabled = enablementState.map { it.unfinished }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val containerEnabled = enablementState.map { it.container }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val productionEnabled = enablementState.map { it.production }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val outOfProductionEnabled = enablementState.map { it.outOfProduction }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)


    /** Track and remove applied filters if items change and result in no returns **/
    private val singletonCats = setOf(
        FilterCategory.FAVORITE, FilterCategory.EXCLUDE_FAVORITE, FilterCategory.DISLIKED,
        FilterCategory.EXCLUDE_DISLIKED, FilterCategory.UNRATED, FilterCategory.RATING_LOW,
        FilterCategory.RATING_HIGH, FilterCategory.IN_STOCK, FilterCategory.OUT_OF_STOCK,
        FilterCategory.HAS_TINS, FilterCategory.NO_TINS, FilterCategory.OPENED,
        FilterCategory.UNOPENED, FilterCategory.FINISHED, FilterCategory.UNFINISHED,
        FilterCategory.PRODUCTION, FilterCategory.OUT_OF_PRODUCTION
    )

    private fun trackSelection(category: FilterCategory, value: Any?, added: Boolean) {
        _selectionHistory.update { history ->
            val next = history.toMutableList()

            if (category in singletonCats) { next.removeAll { it.first == category } }
            else { next.removeAll { it.first == category && it.second == value } }

            if (added) next.add(category to value)
            next.toList()
        }
    }

    // recover from empty
    init {
        viewModelScope.launch(Dispatchers.Default) {
            unifiedFilteredItems.collectLatest { items ->
                if (items.isEmpty() && isFilterApplied.value && !searchPerformed.value && !_emptyDatabase.value) {
                    delay(50.milliseconds)
                    if (unifiedFilteredItems.value.isEmpty()) {
                        recoverFromEmpty()
                    }
                }
            }
        }
    }

    private fun recoverFromEmpty() {
        val history = _selectionHistory.value
        if (history.isEmpty()) {
            resetFilter()
            return
        }

        val invalidSelections = history.filter { (category, value) ->
            isSelectionInvalid(category, value)
        }

        if (invalidSelections.isNotEmpty()) {
            invalidSelections.forEach { (category, value) -> removeFilter(category, value) }
            return
        }

        val (category, value) = history.last()
        val selections = filterSelectionsFlow.value

        if (category == FilterCategory.COMPONENT) {
            if (selections.compMatching == FlowMatchOption.ONLY) {
                updateCompMatching(FlowMatchOption.ALL)
                return
            } else if (selections.compMatching == FlowMatchOption.ALL) {
                updateCompMatching(FlowMatchOption.ANY)
                return
            }
        } else if (category == FilterCategory.FLAVORING) {
            if (selections.flavorMatching == FlowMatchOption.ONLY) {
                updateFlavorMatching(FlowMatchOption.ALL)
                return
            } else if (selections.flavorMatching == FlowMatchOption.ALL) {
                updateFlavorMatching(FlowMatchOption.ANY)
                return
            }
        }

        removeFilter(category, value)
    }
    private fun isSelectionInvalid(category: FilterCategory, value: Any?): Boolean {
        return when (category) {
            FilterCategory.BRAND, FilterCategory.EXCLUDE_BRAND -> !availableBrands.value.contains(value as String)
            FilterCategory.TYPE -> !availableTypes.value.contains(value as String)
            FilterCategory.SUBGENRE -> !availableSubgenres.value.contains(value as String)
            FilterCategory.CUT -> !availableCuts.value.contains(value as String)
            FilterCategory.COMPONENT -> !availableComponents.value.contains(value as String)
            FilterCategory.FLAVORING -> !availableFlavorings.value.contains(value as String)
            FilterCategory.CONTAINER -> !availableContainers.value.contains(value as String)

            FilterCategory.FAVORITE, FilterCategory.EXCLUDE_FAVORITE,
            FilterCategory.DISLIKED, FilterCategory.EXCLUDE_DISLIKED -> !favDisExist.value

            FilterCategory.UNRATED, FilterCategory.RATING_LOW, FilterCategory.RATING_HIGH -> !ratingsExist.value

            FilterCategory.HAS_TINS, FilterCategory.NO_TINS, FilterCategory.OPENED,
            FilterCategory.UNOPENED, FilterCategory.FINISHED, FilterCategory.UNFINISHED -> !tinsExist.value

            else -> false
        }
    }
    private fun removeFilter(category: FilterCategory, value: Any?) {
        when (category) {
            FilterCategory.BRAND -> updateSelectedBrands(value as String, false)
            FilterCategory.EXCLUDE_BRAND -> updateSelectedExcludedBrands(value as String, false)
            FilterCategory.TYPE -> updateSelectedTypes(value as String, false)
            FilterCategory.FAVORITE -> updateSelectedFavorites(false)
            FilterCategory.EXCLUDE_FAVORITE -> updateSelectedExcludeFavorites(false)
            FilterCategory.DISLIKED -> updateSelectedDislikeds(false)
            FilterCategory.EXCLUDE_DISLIKED -> updateSelectedExcludeDislikeds(false)
            FilterCategory.UNRATED -> updateSelectedUnrated(false)
            FilterCategory.RATING_LOW -> updateSelectedRatingRange(null, _selectedRatingHigh.value)
            FilterCategory.RATING_HIGH -> updateSelectedRatingRange(_selectedRatingLow.value, null)
            FilterCategory.IN_STOCK -> updateSelectedInStock(false)
            FilterCategory.OUT_OF_STOCK -> updateSelectedOutOfStock(false)
            FilterCategory.SUBGENRE -> updateSelectedSubgenre(value as String, false)
            FilterCategory.CUT -> updateSelectedCut(value as String, false)
            FilterCategory.COMPONENT -> updateSelectedComponent(value as String, false)
            FilterCategory.FLAVORING -> updateSelectedFlavoring(value as String, false)
            FilterCategory.HAS_TINS -> updateSelectedHasTins(false)
            FilterCategory.NO_TINS -> updateSelectedNoTins(false)
            FilterCategory.OPENED -> updateSelectedOpened(false)
            FilterCategory.UNOPENED -> updateSelectedUnopened(false)
            FilterCategory.FINISHED -> updateSelectedFinished(false)
            FilterCategory.UNFINISHED -> updateSelectedUnfinished(false)
            FilterCategory.CONTAINER -> updateSelectedContainer(value as String, false)
            FilterCategory.PRODUCTION -> updateSelectedProduction(false)
            FilterCategory.OUT_OF_PRODUCTION -> updateSelectedOutOfProduction(false)
        }
    }


    /** Final UI States and hoisted states for other stuff **/
    // TwoPane stuff
    private val _secondPaneExpanded = MutableStateFlow(true)
    val secondPaneExpanded = _secondPaneExpanded.asStateFlow()
    fun toggleSecondPane() { _secondPaneExpanded.value = !_secondPaneExpanded.value }
    fun setSecondPaneExpansion(expanded: Boolean) { _secondPaneExpanded.value = expanded }

    // top app bar
    val menuExpanded = MutableStateFlow(false)
    val menuState = MutableStateFlow(MenuState.MAIN)
    val exportCsvPopup = MutableStateFlow(false)
    val exportType = MutableStateFlow<ExportType?>(null)
    val displayedExportRating = MutableStateFlow(Pair("", ""))
    val selectAllItems = MutableStateFlow(true)

    val topAppBarMenuState = combine(
        menuExpanded,
        menuState
    ) { values: Array<Any?> ->
        val menuExpanded = values[0] as Boolean
        val menuState = values[1] as MenuState

        TopAppBarMenuState(
            menuExpanded = menuExpanded,
            menuState = menuState,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500),
            initialValue = TopAppBarMenuState()
        )

    @Suppress("UNCHECKED_CAST")
    val exportCsvState = combine(
        preferencesRepo.exportRating,
        displayedExportRating,
        selectAllItems
    ) { values: Array<Any?> ->
        val exportRating = values[0] as ExportRating
        val displayedExportRating = values[1] as Pair<String, String>
        val selectAllItems = values[2] as Boolean

        val selectedIndex = if (selectAllItems) 0 else 1

        ExportCsvState(
            exportRating,
            displayedExportRating,
            selectAllItems,
            selectedIndex
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ExportCsvState()
        )

    fun toggleMenu() {
        menuExpanded.value = !menuExpanded.value
        getPositionTrigger()
    }
    fun showMenu(expanded: Boolean) { menuExpanded.value = expanded }
    fun changeMenuState(state: MenuState) { menuState.value = state }
    fun changeExportType(type: ExportType) { exportType.value = type }
    fun selectAll(all: Boolean) { selectAllItems.value = all }
    fun showExportCsv(show: Boolean) {
        if (show) {
            viewModelScope.launch {
                val current = preferencesRepo.exportRating.first()
                displayedExportRating.value = Pair(current.maxRating.toString(), current.rounding.toString())
                exportCsvPopup.value = true
            }
        } else {
            exportCsvPopup.value = false
            changeMenuState(MenuState.MAIN)
        }
    }
    fun updateExportRating(max: String, rounding: String) { displayedExportRating.value = Pair(max, rounding) }
    suspend fun saveExportRating(rating: String, rounding: String) {
        val max = rating.toIntOrNull() ?: 5
        val roundingInt = rounding.toIntOrNull() ?: 2
        preferencesRepo.saveExportRating(max, roundingInt)
    }

    // Bottom app bar
    private val _twoPaneState = MutableStateFlow(false)
    val twoPaneState = _twoPaneState.asStateFlow()
    fun updateTwoPaneState(state: Boolean) { _twoPaneState.value = state }

    private val _clickToAdd = MutableStateFlow(false)
    val clickToAdd = _clickToAdd.asStateFlow()
    fun updateClickToAdd(clicked: Boolean) { _clickToAdd.value = clicked }


    // HomeScreen stuff
    val searchState = combine(
        searchFocused,
        searchPerformed,
        isTinSearch,
        searchTextDisplay,
        preferencesRepo.searchSetting,
        tinsExist,
        notesExist,
        searchMenuExpanded,
        emptyDatabase
    ) { it: Array<Any?> ->
        val searchFocused = it[0] as Boolean
        val searchPerformed = it[1] as Boolean
        val isTinSearch = it[2] as Boolean
        val searchText = it[3] as String
        val searchSetting = it[4] as SearchSetting
        val tinsExist = it[5] as Boolean
        val notesExist = it[6] as Boolean
        val searchMenuExpanded = it[7] as Boolean
        val databaseEmpty = it[8] as Boolean

        val blendSearch = SearchSetting.Blend
        val notesSearch = if (notesExist) SearchSetting.Notes else null
        val tinsSearch = if (tinsExist) SearchSetting.TinLabel else null
        val settingsList = listOfNotNull(blendSearch, notesSearch, tinsSearch)
        val settingsEnabled = settingsList.size > 1

        val iconOpacity = if (searchPerformed) { 1f } else { if (searchMenuExpanded) 1f else 0.5f }

        if (!settingsEnabled && searchSetting != SearchSetting.Blend) { saveSearchSetting(SearchSetting.Blend.value) }

        SearchState(
            searchFocused = searchFocused,
            searchPerformed = searchPerformed,
            isTinSearch = isTinSearch,
            searchText = searchText,
            currentSetting = searchSetting,
            settingsList = SearchSettingList(settingsList),
            settingsEnabled = settingsEnabled,
            searchMenuExpanded = searchMenuExpanded,
            searchIconOpacity = iconOpacity,
            emptyDatabase = databaseEmpty
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SearchState()
        )


    // Final filter sections UI states //
    val filteredBrands = combine(
        brandSearchText,
        availableBrands
    ) { brandSearchText, allBrands ->
        updateFilterBrands(brandSearchText, allBrands)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    @Suppress("UNCHECKED_CAST")
    val unselectedBrands = combine(
        filteredBrands,
        excludeBrandSwitch,
        selectedBrands,
        selectedExcludeBrands,
        brandsEnabled,
        excludeBrandsEnabled
    ) { array ->
        val filteredBrands = array[0] as List<String>
        val excludeSwitch = array[1] as Boolean
        val selectedBrands = array[2] as List<String>
        val selectedExcludedBrands = array[3] as List<String>
        val includeEnabled = array[4] as Map<String, Boolean>
        val excludeEnabled = array[5] as Map<String, Boolean>

        val brandEnabled = if (excludeSwitch) excludeEnabled else includeEnabled

        updateUnselectedBrandRow(
            filteredBrands,
            excludeSwitch,
            selectedBrands,
            selectedExcludedBrands,
            brandEnabled
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val brandEnabled = combine(
        excludeBrandSwitch,
        brandsEnabled,
        excludeBrandsEnabled
    ) { switch, includeBrands, excludeBrands ->
        if (switch) excludeBrands else includeBrands
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyMap()
        )

    val selectedBrand = combine(
        excludeBrandSwitch,
        selectedBrands,
        selectedExcludeBrands,
    ) { switch, includeBrands, excludeBrands ->
        if (switch) excludeBrands else includeBrands
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _clearBrandTrigger = MutableStateFlow(0)
    val clearBrandTrigger = _clearBrandTrigger.asStateFlow()
    fun updateClearBrandTrigger() { _clearBrandTrigger.value++ }


    private val _chipBoxWidth = MutableStateFlow(0.dp)
    val chipBoxWidth = _chipBoxWidth.asStateFlow()
    fun updateChipBoxWidth(width: Dp) { _chipBoxWidth.value = width }
    val chipMaxWidth = chipBoxWidth.map {
        (it * 0.32f) - 4.dp
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.dp
        )

    private val _showBrandChipOverflow = MutableStateFlow(false)
    val showBrandChipOverflow = _showBrandChipOverflow.asStateFlow()
    fun showBrandOverflow() { _showBrandChipOverflow.value = !_showBrandChipOverflow.value }

    fun updateFilterBrands(text: String, allBrands: List<String>): List<String> {
        if (text.isBlank()) return allBrands

        return allBrands.mapNotNull { brand ->
            val score = when {
                brand.startsWith(text, ignoreCase = true) -> 0
                brand.split(" ").any { it.startsWith(text, ignoreCase = true) } -> 1
                brand.contains(text, ignoreCase = true) -> 2
                else -> null
            }

            if (score != null) score to brand else null
        }.sortedBy { it.first }.map { it.second }
    }

    fun updateUnselectedBrandRow(
        filteredBrands: List<String>,
        excluded: Boolean,
        selectedBrands: List<String>,
        selectedExcludedBrands: List<String>,
        brandEnabled: Map<String, Boolean>
    ): List<String> {
        val preSorted = filteredBrands.filterNot {
            if (!excluded) {
                selectedBrands.contains(it)
            } else {
                selectedExcludedBrands.contains(it)
            }
        }

        return reorderChips(preSorted, brandEnabled)
    }


    val favoriteSelection = combine(
        selectedFavorites,
        selectedExcludeFavorites
    ) { favorites, excludeFavorites ->
        val favoritesSelection = when {
            favorites -> ToggleableState.On
            excludeFavorites -> ToggleableState.Indeterminate
            else -> ToggleableState.Off
        }
        favoritesSelection
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ToggleableState.Off
        )

    val dislikedSelection = combine(
        selectedDislikeds,
        selectedExcludeDislikeds
        ) { dislikeds, excludeDislikeds ->
        val dislikedsSelection = when {
            dislikeds -> ToggleableState.On
            excludeDislikeds -> ToggleableState.Indeterminate
            else -> ToggleableState.Off
        }
        dislikedsSelection
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ToggleableState.Off
        )

    init {
        viewModelScope.launch {
            favoritesEnabled.collect { enabled ->
                if (!enabled && selectedExcludeFavorites.value) {
                    updateSelectedExcludeFavorites(false)
                }
            }
        }
        viewModelScope.launch {
            dislikedsEnabled.collect { enabled ->
                if (!enabled && selectedExcludeDislikeds.value) {
                    updateSelectedExcludeDislikeds(false)
                }
            }
        }
    }


    private val _showRatingPop = MutableStateFlow(false)
    val showRatingPop = _showRatingPop.asStateFlow()
    fun onShowRatingPop() { _showRatingPop.value = !_showRatingPop.value }


    val rangeEnabled = unifiedFilteredItems.map {
        it.any { item -> item.items.rating != null }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = true
        )

    val rangeUnchosen = combine(
        selectedRatingLow,
        selectedRatingHigh
    ) { low, high ->
        low == null && high == null
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = true
        )

    val rangeLowText = selectedRatingLow.map { formatDecimal(it, 1).ifBlank { "0" } }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = "0"
        )

    val rangeHighText = selectedRatingHigh.map { formatDecimal(it, 1).ifBlank { "5" } }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = "5"
        )

    val rangeLowTextAlpha = combine(
        selectedRatingLow,
        rangeUnchosen
    ) { ratingLow, unchosen ->
        if (unchosen || ratingLow == null) .7f else 1f
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = .7f
        )

    val rangeHighTextAlpha = combine(
        selectedRatingHigh,
        rangeUnchosen
    ) { ratingHigh, unchosen ->
        if (unchosen || ratingHigh == null) .7f else 1f
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = .7f
        )

    val ratingRowEmptyAlpha = combine(
        rangeUnchosen,
        selectedRatingLow,
        selectedRatingHigh
    ) { unchosen, ratingLow, ratingHigh ->
        if (unchosen || (ratingLow != null && ratingHigh == null)) 1f else .38f
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 1f
        )


    fun reorderChips(available: List<String>, enablement: Map<String, Boolean>): List<String> {
        if (available.isEmpty()) return emptyList()
        return available.sortedBy { if (enablement[it] == true) 0 else 1 }
    }

    // Flow section filtering data
    val subgenreAvailable = combine(
        availableSubgenres,
        subgenresEnabled
    ) { availableSubgenres, subgenresEnabled ->
        reorderChips(availableSubgenres, subgenresEnabled)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val cutAvailable = combine(
        availableCuts,
        cutsEnabled
    ) { availableCuts, cutsEnabled ->
        reorderChips(availableCuts, cutsEnabled)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val componentAvailable = combine(
        availableComponents,
        componentsEnabled
    ) { availableComponents, componentsEnabled ->
        reorderChips(availableComponents, componentsEnabled)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val flavoringAvailable = combine(
        availableFlavorings,
        flavoringsEnabled
    ) { availableFlavorings, flavoringsEnabled ->
        reorderChips(availableFlavorings, flavoringsEnabled)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val containerAvailable = combine(
        availableContainers,
        containerEnabled
    ) { availableContainers, containerEnabled ->
        reorderChips(availableContainers, containerEnabled)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val hasContainer = selectedContainer.map { it.isNotEmpty() }

    val implicitHasTins = combine(
        selectedHasTins,
        selectedOpened,
        selectedUnopened,
        selectedFinished,
        selectedUnfinished,
        hasContainer,
    ) { array ->
        val hasTins = array[0]
        val opened = array[1]
        val unopened = array[2]
        val finished = array[3]
        val unfinished = array[4]
        val hasContainer = array[5]

        !hasTins && (opened || unopened || finished || unfinished || hasContainer)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )


    // filter selection update functions //
    private fun updateHistoryBrandSwap(brands: List<String>, toExclude: Boolean) {
        _selectionHistory.update { history ->
            val newHistory = history.toMutableList()
            val oldCategory = if (toExclude) FilterCategory.BRAND else FilterCategory.EXCLUDE_BRAND
            val newCategory = if (toExclude) FilterCategory.EXCLUDE_BRAND else FilterCategory.BRAND
            brands.forEach { brand ->
                newHistory.removeAll { it.first == oldCategory && it.second == brand }
                newHistory.add(newCategory to brand)
            }
            newHistory.toList()
        }
    }

    private fun updateHistoryBooleanSwaps(category: FilterCategory, conflictingCat: List<FilterCategory>, isSelected: Boolean) {
        _selectionHistory.update { history ->
            val next = history.toMutableList()
            next.removeAll { it.first == category || it.first in conflictingCat }
            if (isSelected) next.add(category to null)
            next.toList()
        }
    }

    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) {
            _selectedBrands.value += brand
        } else {
            _selectedBrands.value -= brand
        }
        trackSelection(FilterCategory.BRAND, brand, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) {
            _selectedExcludeBrands.value += brand
        } else {
            _selectedExcludeBrands.value -= brand
        }
        trackSelection(FilterCategory.EXCLUDE_BRAND, brand, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeBrandsSwitch() {
        val wasExcluded = excludeBrandSwitch.value
        val included = _selectedBrands.value
        val excluded = _selectedExcludeBrands.value
        val targetExclude = !wasExcluded
        excludeBrandSwitch.value = targetExclude

        if (targetExclude) {
            if (included.isNotEmpty()) {
                _selectedBrands.value = emptyList()
                _selectedExcludeBrands.value = included
                updateHistoryBrandSwap(included, true)
            }
        } else {
            if (excluded.isNotEmpty()) {
                _selectedExcludeBrands.value = emptyList()
                _selectedBrands.value = excluded
                updateHistoryBrandSwap(excluded, false)
            }
        }
        _shouldScrollUp.value = true
    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        if (isSelected) { _selectedTypes.value += type }
        else { _selectedTypes.value -= type }

        trackSelection(FilterCategory.TYPE, type, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        if (isSelected) { _selectedExcludeFavorites.value = false }
        _selectedFavorites.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.FAVORITE, listOf(FilterCategory.EXCLUDE_FAVORITE), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeFavorites(isSelected: Boolean) {
        if (isSelected) { _selectedFavorites.value = false }
        _selectedExcludeFavorites.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.EXCLUDE_FAVORITE, listOf(FilterCategory.FAVORITE), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        if (isSelected) { _selectedExcludeDislikeds.value = false }
        _selectedDislikeds.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.DISLIKED, listOf(FilterCategory.EXCLUDE_DISLIKED), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeDislikeds(isSelected: Boolean) {
        if (isSelected) { _selectedDislikeds.value = false }
        _selectedExcludeDislikeds.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.EXCLUDE_DISLIKED, listOf(FilterCategory.DISLIKED), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateFavSelection() {
        when (favoriteSelection.value) {
            ToggleableState.Off -> {
                if (favoritesEnabled.value) updateSelectedFavorites(true)
                else if (excludeFavoritesEnabled.value) updateSelectedExcludeFavorites(true)
                else {
                    updateSelectedFavorites(false)
                    updateSelectedExcludeFavorites(false)
                }
            }
            ToggleableState.On ->
                if (excludeFavoritesEnabled.value) updateSelectedExcludeFavorites(true)
                else updateSelectedFavorites(false)
            ToggleableState.Indeterminate -> {
                updateSelectedFavorites(false)
                updateSelectedExcludeFavorites(false)
            }
        }
    }

    fun updateDisSelection() {
        when (dislikedSelection.value) {
            ToggleableState.Off -> {
                if (dislikedsEnabled.value) updateSelectedDislikeds(true)
                else if (excludeDislikesEnabled.value) updateSelectedExcludeDislikeds(true)
                else {
                    updateSelectedDislikeds(false)
                    updateSelectedExcludeDislikeds(false)
                }
            }
            ToggleableState.On ->
                if (excludeDislikesEnabled.value) updateSelectedExcludeDislikeds(true)
                else updateSelectedDislikeds(false)
            ToggleableState.Indeterminate -> {
                updateSelectedDislikeds(false)
                updateSelectedExcludeDislikeds(false)
            }
        }
    }

    fun updateSelectedUnrated(isSelected: Boolean) {
        _selectedUnrated.value = isSelected

        trackSelection(FilterCategory.UNRATED, null, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedRatingRange(low: Double?, high: Double?) {
        _selectedRatingLow.value = low
        _selectedRatingHigh.value = high

        _selectionHistory.update { history ->
            val next = history.toMutableList()
            next.removeAll { it.first == FilterCategory.RATING_LOW || it.first == FilterCategory.RATING_HIGH }
            if (low != null) next.add(FilterCategory.RATING_LOW to low)
            if (high != null) next.add(FilterCategory.RATING_HIGH to high)
            next.toList()
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedInStock(isSelected: Boolean) {
        if (isSelected) { _selectedOutOfStock.value = false }
        _selectedInStock.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.IN_STOCK, listOf(FilterCategory.OUT_OF_STOCK), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        if (isSelected) { _selectedInStock.value = false }
        _selectedOutOfStock.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.OUT_OF_STOCK, listOf(FilterCategory.IN_STOCK), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedSubgenre(subgenre: String, isSelected: Boolean) {
        if (isSelected) { _selectedSubgenres.value += subgenre }
        else { _selectedSubgenres.value -= subgenre }

        trackSelection(FilterCategory.SUBGENRE, subgenre, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedCut(cut: String, isSelected: Boolean) {
        if (isSelected) { _selectedCuts.value += cut }
        else { _selectedCuts.value -= cut }

        trackSelection(FilterCategory.CUT, cut, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedComponent(component: String, isSelected: Boolean) {
        if (isSelected) { _selectedComponents.value += component }
        else { _selectedComponents.value -= component }

        trackSelection(FilterCategory.COMPONENT, component, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateCompMatching(option: FlowMatchOption) {
        _compMatching.value = option

        if (option != FlowMatchOption.ANY) {
            val selected = selectedComponents.value
            val pruned = selected.filter { componentsEnabled.value[it] == true }
            if (pruned.size < selected.size) {
                val removed = selected - pruned.toSet()
                removed.forEach { trackSelection(FilterCategory.COMPONENT, it, false) }

                _selectedComponents.value = pruned
            }
        }
    }

    fun updateSelectedFlavoring(flavoring: String, isSelected: Boolean) {
        if (isSelected) { _selectedFlavorings.value += flavoring }
        else { _selectedFlavorings.value -= flavoring }

        _shouldScrollUp.value = true

        trackSelection(FilterCategory.FLAVORING, flavoring, isSelected)
    }

    fun updateFlavorMatching(option: FlowMatchOption) {
        _flavorMatching.value = option

        if (option != FlowMatchOption.ANY) {
            val selected = selectedFlavorings.value
            val pruned = selected.filter { flavoringsEnabled.value[it] == true }
            if (pruned.size < selected.size) {
                val removed = selected - pruned.toSet()
                removed.forEach { trackSelection(FilterCategory.FLAVORING, it, false) }

                _selectedFlavorings.value = pruned
            }
        }
    }

    fun updateSelectedProduction(isSelected: Boolean) {
        if (isSelected) { _selectedOutOfProduction.value = false }
        _selectedProduction.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.PRODUCTION, listOf(FilterCategory.OUT_OF_PRODUCTION), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfProduction(isSelected: Boolean) {
        if (isSelected) { _selectedProduction.value = false }
        _selectedOutOfProduction.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.OUT_OF_PRODUCTION, listOf(FilterCategory.PRODUCTION), isSelected)

        _shouldScrollUp.value = true
    }


    // Tins filtering //
    fun updateSelectedHasTins(isSelected: Boolean) {
        if (isSelected) { _selectedNoTins.value = false }
        _selectedHasTins.value = isSelected

        updateHistoryBooleanSwaps(FilterCategory.HAS_TINS, listOf(FilterCategory.NO_TINS), isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedNoTins(isSelected: Boolean) {
        if (isSelected) {
            _selectedHasTins.value = false
            _selectedOpened.value = false
            _selectedUnopened.value = false
            _selectedFinished.value = false
            _selectedUnfinished.value = false
        }
        _selectedNoTins.value = isSelected

        updateHistoryBooleanSwaps(
            FilterCategory.NO_TINS,
            listOf(
                FilterCategory.HAS_TINS, FilterCategory.OPENED, FilterCategory.UNOPENED,
                FilterCategory.FINISHED, FilterCategory.UNFINISHED
            ),
            isSelected
        )

        _shouldScrollUp.value = true
    }

    fun updateSelectedOpened(isSelected: Boolean) {
        if (isSelected) {
            _selectedUnopened.value = false
            _selectedNoTins.value = false
        }
        _selectedOpened.value = isSelected

        updateHistoryBooleanSwaps(
            FilterCategory.OPENED,
            listOf(FilterCategory.UNOPENED, FilterCategory.NO_TINS),
            isSelected
        )

        _shouldScrollUp.value = true
    }

    fun updateSelectedUnopened(isSelected: Boolean) {
        if (isSelected) {
            _selectedOpened.value = false
            _selectedNoTins.value = false
        }
        _selectedUnopened.value = isSelected

        updateHistoryBooleanSwaps(
            FilterCategory.UNOPENED,
            listOf(FilterCategory.OPENED, FilterCategory.NO_TINS),
            isSelected
        )

        _shouldScrollUp.value = true
    }

    fun updateSelectedFinished(isSelected: Boolean) {
        if (isSelected) {
            _selectedUnfinished.value = false
            _selectedNoTins.value = false
        }
        _selectedFinished.value = isSelected

        updateHistoryBooleanSwaps(
            FilterCategory.FINISHED,
            listOf(FilterCategory.UNFINISHED, FilterCategory.NO_TINS),
            isSelected
        )

        _shouldScrollUp.value = true
    }

    fun updateSelectedUnfinished(isSelected: Boolean) {
        if (isSelected) {
            _selectedFinished.value = false
            _selectedNoTins.value = false
        }
        _selectedUnfinished.value = isSelected

        updateHistoryBooleanSwaps(
            FilterCategory.UNFINISHED,
            listOf(FilterCategory.FINISHED, FilterCategory.NO_TINS),
            isSelected
        )

        _shouldScrollUp.value = true
    }

    fun updateSelectedContainer(container: String, isSelected: Boolean) {
        if (isSelected) { _selectedContainer.value += container }
        else { _selectedContainer.value -= container }

        trackSelection(FilterCategory.CONTAINER, container, isSelected)

        _shouldScrollUp.value = true
    }

    // Filter overflow check and clearing //
    fun overflowCheck(selected: List<String>, available: List<String>, shown: Int): Boolean {
        val overflowedItems = available.drop(shown)
        return selected.any { overflowedItems.contains(it) }
    }

    fun clearAllSelected(field: ClearAll) {
        _selectionHistory.update { history ->
            val nextHistory = history.toMutableList()

            when (field) {
                ClearAll.SUBGENRE -> {
                    val previous = _selectedSubgenres.value
                    _selectedSubgenres.value = emptyList()
                    previous.forEach { item ->
                        nextHistory.removeAll { it.first == FilterCategory.SUBGENRE && it.second == item }
                    }
                }

                ClearAll.CUT -> {
                    val previous = _selectedCuts.value
                    _selectedCuts.value = emptyList()
                    previous.forEach { item ->
                        nextHistory.removeAll { it.first == FilterCategory.CUT && it.second == item }
                    }
                }

                ClearAll.COMPONENT -> {
                    val previous = _selectedComponents.value
                    _selectedComponents.value = emptyList()
                    _compMatching.value = FlowMatchOption.ANY
                    previous.forEach { item ->
                        nextHistory.removeAll { it.first == FilterCategory.COMPONENT && it.second == item }
                    }
                }

                ClearAll.FLAVORING -> {
                    val previous = _selectedFlavorings.value
                    _selectedFlavorings.value = emptyList()
                    _flavorMatching.value = FlowMatchOption.ANY
                    previous.forEach { item ->
                        nextHistory.removeAll { it.first == FilterCategory.FLAVORING && it.second == item }
                    }
                }

                ClearAll.CONTAINER -> {
                    val previous = _selectedContainer.value
                    _selectedContainer.value = emptyList()
                    previous.forEach { item ->
                        nextHistory.removeAll { it.first == FilterCategory.CONTAINER && it.second == item }
                    }
                }
            }
            nextHistory.toList()
        }
        _shouldScrollUp.value = true
    }

    fun clearAllSelectedBrands() {
        val brandsToClear = _selectedBrands.value.ifEmpty { _selectedExcludeBrands.value }
        val category = if (_selectedBrands.value.isEmpty()) FilterCategory.EXCLUDE_BRAND else FilterCategory.BRAND

        _selectedBrands.value = emptyList()
        _selectedExcludeBrands.value = emptyList()

        _selectionHistory.update { history ->
            val next = history.toMutableList()
            brandsToClear.forEach { brand ->
                next.removeAll { it.first == category && it.second == brand }
            }
            next.toList()
        }

        updateClearBrandTrigger()
        _shouldScrollUp.value = true
    }

    fun resetFilter() {
        _selectedBrands.value = emptyList()
        _selectedTypes.value = emptyList()
        _selectedFavorites.value = false
        _selectedDislikeds.value = false
        _selectedUnrated.value = false
        _selectedRatingLow.value = null
        _selectedRatingHigh.value = null
        _selectedInStock.value = false
        _selectedOutOfStock.value = false
        _selectedExcludeBrands.value = emptyList()
        _selectedExcludeFavorites.value = false
        _selectedExcludeDislikeds.value = false
        _selectedSubgenres.value = emptyList()
        _selectedCuts.value = emptyList()
        _selectedComponents.value = emptyList()
        _compMatching.value = FlowMatchOption.ANY
        _selectedFlavorings.value = emptyList()
        _flavorMatching.value = FlowMatchOption.ANY
        _selectedProduction.value = false
        _selectedOutOfProduction.value = false

        _selectedHasTins.value = false
        _selectedNoTins.value = false
        _selectedContainer.value = emptyList()
        _selectedOpened.value = false
        _selectedUnopened.value = false
        _selectedFinished.value = false
        _selectedUnfinished.value = false

        _selectionHistory.value = emptyList()

        updateClearBrandTrigger()

        _shouldScrollUp.value = true
    }

}

enum class BottomSheetState { OPENED, CLOSED }

//enum class ActiveScreen { HOME, STATS, DATES, Other }

enum class FilterCategory {
    BRAND, EXCLUDE_BRAND, TYPE, FAVORITE, EXCLUDE_FAVORITE, DISLIKED, EXCLUDE_DISLIKED, UNRATED,
    RATING_LOW, RATING_HIGH, IN_STOCK, OUT_OF_STOCK, SUBGENRE, CUT, COMPONENT, FLAVORING, HAS_TINS,
    NO_TINS, OPENED, UNOPENED, FINISHED, UNFINISHED, CONTAINER, PRODUCTION, OUT_OF_PRODUCTION,
}

enum class FlowMatchOption(val value: String) { ANY("Any"), ALL("All"), ONLY("Only") }

enum class ClearAll { SUBGENRE, CUT, COMPONENT, FLAVORING, CONTAINER }

@Stable
data class SheetSelections(
    val brands: List<String> = emptyList(),
    val excludeBrands: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val favorites: Boolean = false,
    val excludeFavorites: Boolean = false,
    val dislikeds: Boolean = false,
    val excludeDislikeds: Boolean = false,
    val unrated: Boolean = false,
    val ratingLow: Double? = null,
    val ratingHigh: Double? = null,
    val inStock: Boolean = false,
    val outOfStock: Boolean = false,
    val subgenres: List<String> = emptyList(),
    val cuts: List<String> = emptyList(),
    val components: List<String> = emptyList(),
    val compMatching: FlowMatchOption = FlowMatchOption.ANY,
    val flavorings: List<String> = emptyList(),
    val flavorMatching: FlowMatchOption = FlowMatchOption.ANY,
    val hasTins: Boolean = false,
    val noTins: Boolean = false,
    val opened: Boolean = false,
    val unopened: Boolean = false,
    val finished: Boolean = false,
    val unfinished: Boolean = false,
    val container: List<String> = emptyList(),
    val production: Boolean = false,
    val outOfProduction: Boolean = false,
)

@Stable
data class EnablementState(
    val brands: Map<String, Boolean> = emptyMap(),
    val excludeBrands: Map<String, Boolean> = emptyMap(),
    val types: Map<String, Boolean> = emptyMap(),
    val favorites: Boolean = false,
    val excludeFavorites: Boolean = false,
    val dislikeds: Boolean = false,
    val excludeDislikes: Boolean = false,
    val unrated: Boolean = false,
    val ratingLow: Double? = null,
    val ratingHigh: Double? = null,
    val inStock: Boolean = false,
    val outOfStock: Boolean = false,
    val subgenres: Map<String, Boolean> = emptyMap(),
    val cuts: Map<String, Boolean> = emptyMap(),
    val components: Map<String, Boolean> = emptyMap(),
    val compMatching: Map<FlowMatchOption, Boolean> = emptyMap(),
    val flavorings: Map<String, Boolean> = emptyMap(),
    val flavorMatching: Map<FlowMatchOption, Boolean> = emptyMap(),
    val hasTins: Boolean = false,
    val noTins: Boolean = false,
    val opened: Boolean = false,
    val unopened: Boolean = false,
    val finished: Boolean = false,
    val unfinished: Boolean = false,
    val container: Map<String, Boolean> = emptyMap(),
    val production: Boolean = false,
    val outOfProduction: Boolean = false,
)

/** Stuff for stuff other than Filter Sheet **/
@Stable
data class TopAppBarMenuState(
    val menuExpanded: Boolean = false,
    val menuState: MenuState = MenuState.MAIN,
)

@Stable
data class ExportCsvState(
    val exportRating: ExportRating = ExportRating(),
    val exportRatingString: Pair<String, String> = Pair("", ""),
    val allItems: Boolean = true,
    val selectedIndex: Int = 0
)

@Stable
data class SearchSettingList(
    val settings: List<SearchSetting> = emptyList(),
)

@Stable
data class SearchState(
    val searchFocused: Boolean = false,
    val searchPerformed: Boolean = false,
    val isTinSearch: Boolean = false,
    val searchText: String = "",
    val currentSetting: SearchSetting = SearchSetting.Blend,
    val settingsList: SearchSettingList = SearchSettingList(),
    val settingsEnabled: Boolean = false,
    val searchMenuExpanded: Boolean = false,
    val searchIconOpacity: Float = 0.5f,
    val emptyDatabase: Boolean = false
)


@Stable
data class HomeScrollState(
    val currentPosition: Map<Int, Int> = mapOf(0 to 0, 1 to 0),
    val shouldScrollUp: Boolean = false,
    val savedItemId: Int = -1,
    val shouldReturn: Boolean = false,
    val getPosition: Int = 0
)

@Stable
data class AutoCompleteData(
    val brands: List<String> = emptyList(),
    val subgenres: List<String> = emptyList(),
    val cuts: List<String> = emptyList(),
    val components: List<String> = emptyList(),
    val flavorings: List<String> = emptyList(),
    val tinContainers: List<String> = emptyList()
)


val typeOrder = mapOf(
    "Aromatic" to 0,
    "English" to 1,
    "Burley" to 2,
    "Virginia" to 3,
    "Other" to 4,
    "(Unassigned)" to 5
)