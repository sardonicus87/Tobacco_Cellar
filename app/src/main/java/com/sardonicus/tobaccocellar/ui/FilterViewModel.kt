package com.sardonicus.tobaccocellar.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.ExportType
import com.sardonicus.tobaccocellar.MenuState
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.home.SearchClearedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchPerformedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchSetting
import com.sardonicus.tobaccocellar.ui.home.calculateTotalQuantity
import com.sardonicus.tobaccocellar.ui.items.ItemSavedEvent
import com.sardonicus.tobaccocellar.ui.items.ItemUpdatedEvent
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
        viewModelScope.launch(Dispatchers.IO) { preferencesRepo.setSearchSetting(setting) }
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


    /** Sheet UI control **/
    val brandSearchText = MutableStateFlow("")
    fun updateBrandSearchText(text: String) { brandSearchText.value = text }


    // sheet selection states //
    val sheetSelectedBrands = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedTypes = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedFavorites = MutableStateFlow(false)
    val sheetSelectedDislikeds = MutableStateFlow(false)
    val sheetSelectedUnrated = MutableStateFlow(false)
    val sheetSelectedRatingLow = MutableStateFlow<Double?>(null)
    val sheetSelectedRatingHigh = MutableStateFlow<Double?>(null)
    val sheetSelectedInStock = MutableStateFlow(false)
    val sheetSelectedOutOfStock = MutableStateFlow(false)
    val sheetSelectedExcludeBrands = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedExcludeBrandSwitch = MutableStateFlow(false)
    val sheetSelectedExcludeFavorites = MutableStateFlow(false)
    val sheetSelectedExcludeDislikeds = MutableStateFlow(false)

    val sheetSelectedSubgenres = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedCuts = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedComponents = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedCompMatching = MutableStateFlow(FlowMatchOption.ANY)
    val sheetSelectedFlavorings = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedFlavorMatching = MutableStateFlow(FlowMatchOption.ANY)
    val sheetSelectedProduction = MutableStateFlow(false)
    val sheetSelectedOutOfProduction = MutableStateFlow(false)

    val sheetSelectedHasTins = MutableStateFlow(false)
    val sheetSelectedNoTins = MutableStateFlow(false)
    val sheetSelectedContainer = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedOpened = MutableStateFlow(false)
    val sheetSelectedUnopened = MutableStateFlow(false)
    val sheetSelectedFinished = MutableStateFlow(false)
    val sheetSelectedUnfinished = MutableStateFlow(false)


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

    private val _selectedSubgenre = MutableStateFlow<List<String>>(emptyList())
    val selectedSubgenre: StateFlow<List<String>> = _selectedSubgenre

    private val _selectedCut = MutableStateFlow<List<String>>(emptyList())
    val selectedCut: StateFlow<List<String>> = _selectedCut

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

    // tins switch and filtering
    @Suppress("UNCHECKED_CAST")
    val showTins: StateFlow<Boolean> = combine(
        sheetSelectedContainer, sheetSelectedOpened, sheetSelectedUnopened, sheetSelectedFinished,
        sheetSelectedUnfinished, isTinSearch, searchPerformed
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


    // exclusionary filter states //
    private val _selectedExcludeBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedExcludeBrands: StateFlow<List<String>> = _selectedExcludeBrands

    private val _selectedExcludeFavorites = MutableStateFlow(false)
    val selectedExcludeFavorites: StateFlow<Boolean> = _selectedExcludeFavorites

    private val _selectedExcludeDislikeds = MutableStateFlow(false)
    val selectedExcludeDislikeds: StateFlow<Boolean> = _selectedExcludeDislikeds


    // filter applied state //
    private val listFiltersApplied = combine(
        sheetSelectedBrands, sheetSelectedTypes, sheetSelectedExcludeBrands, sheetSelectedSubgenres,
        sheetSelectedCuts, sheetSelectedComponents, sheetSelectedFlavorings, sheetSelectedContainer
    ) { flows ->
        flows.any { (it as? List<*>)?.isNotEmpty() == true }
    }
    private val booleanFiltersApplied = combine(
        sheetSelectedFavorites, sheetSelectedDislikeds, sheetSelectedUnrated, sheetSelectedInStock,
        sheetSelectedOutOfStock, sheetSelectedExcludeFavorites, sheetSelectedExcludeDislikeds,
        sheetSelectedProduction, sheetSelectedOutOfProduction, sheetSelectedHasTins,
        sheetSelectedNoTins, sheetSelectedOpened, sheetSelectedUnopened, sheetSelectedFinished,
        sheetSelectedUnfinished
    ) { flows ->
        flows.any { it }
    }
    private val ratingFiltersApplied = combine(
        sheetSelectedRatingLow, sheetSelectedRatingHigh
    ) { low, high ->
        low != null || high != null
    }

    val isFilterApplied: StateFlow<Boolean> = combine(
        listFiltersApplied, booleanFiltersApplied, ratingFiltersApplied
    ) { flows ->
        flows.any { it }
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
//    val searchCleared: StateFlow<Boolean> = _searchCleared.asStateFlow()

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
                    delay(25)
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

    private val lastSeenFlow: Flow<List<Int>> = preferencesRepo.datesSeen.map { datesString ->
        if (datesString.isBlank()) { emptyList() }
        else { datesString.split(",").mapNotNull { it.trim().toIntOrNull() } }
    }

    // setting available vals
    init {
        viewModelScope.launch(Dispatchers.Default) {
            everythingFlow.collectLatest { data ->
                _availableBrands.value = data.map { it.items.brand }
                    .distinct().sorted()
                _availableTypes.value = data.map { it.items.type.ifBlank { "(Unassigned)" } }
                    .distinct().sortedWith(
                        compareBy { typeOrder[it] ?: typeOrder.size }
                    )
                _availableSubgenres.value = data.map {
                    it.items.subGenre.ifBlank { "(Unassigned)" }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                        .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                )
                _availableCuts.value = data.map {
                    it.items.cut.ifBlank { "(Unassigned)" }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                        .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                )
                _availableComponents.value = data.flatMap { items ->
                    items.components.ifEmpty {
                        listOf(Components(componentName = "(None Assigned)"))
                    }.map { it.componentName }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(None Assigned)") 1 else 0 }
                        .thenBy { if (it != "(None Assigned)") it.lowercase() else "" }
                )
                _availableFlavorings.value = data.flatMap { items ->
                    items.flavoring.ifEmpty {
                        listOf(Flavoring(flavoringName = "(None Assigned)"))
                    }.map { it.flavoringName }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(None Assigned)") 1 else 0 }
                        .thenBy { if (it != "(None Assigned)") it.lowercase() else "" }
                )
                _availableContainers.value = data.flatMap {
                    it.tins }.map { it.container.ifBlank { "(Unassigned)" }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                        .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                )
                _typesExist.value = data.any { it.items.type.isNotBlank() }
                _subgenresExist.value = data.any { it.items.subGenre.isNotBlank() }
                _ratingsExist.value = data.any { it.items.rating != null }
                _favDisExist.value = data.any { it.items.favorite || it.items.disliked }
                _tinsExist.value = data.any { it.tins.isNotEmpty() }
                _notesExist.value = data.any { it.items.notes.isNotBlank() }
                _datesExist.value = data.flatMap { it.tins }.any {
                    it.manufactureDate != null || it.cellarDate != null || it.openDate != null
                }
                _emptyDatabase.value = data.isEmpty()
                _autoComplete.value = AutoCompleteData(
                    brands = _availableBrands.value,
                    subgenres = _availableSubgenres.value.filter { it != "(Unassigned)" },
                    cuts = _availableCuts.value.filter { it != "(Unassigned)" },
                    components = _availableComponents.value.filter { it != "(None Assigned)" },
                    flavorings = _availableFlavorings.value.filter { it != "(None Assigned)" },
                    tinContainers = _availableContainers.value.filter { it != "(Unassigned)" }
                )
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            combine (
                everythingFlow,
                lastSeenFlow
            ) { everything, lastSeen ->
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

    // remove selected filters if the last item with that filter is removed or changed
    init {
        viewModelScope.launch(Dispatchers.Default) {
            supervisorScope {
                launch {
                    _availableBrands.collectLatest {
                        val invalidBrands =
                            _selectedBrands.value.filter { it !in availableBrands.value }

                        if (invalidBrands.isNotEmpty()) {
                            sheetSelectedBrands.value =
                                sheetSelectedBrands.value.filter { it in availableBrands.value }
                            _selectedBrands.value =
                                _selectedBrands.value.filter { it in availableBrands.value }
                        }
                    }
                }
                launch {
                    _availableTypes.collectLatest {
                        val invalidTypes =
                            _selectedTypes.value.filter { it !in availableTypes.value }

                        if (invalidTypes.isNotEmpty()) {
                            sheetSelectedTypes.value =
                                sheetSelectedTypes.value.filter { it in availableTypes.value }
                            _selectedTypes.value =
                                _selectedTypes.value.filter { it in availableTypes.value }
                        }
                    }
                }
                launch {
                    _availableSubgenres.collectLatest {
                        val invalidSubgenres =
                            _selectedSubgenre.value.filter { it !in availableSubgenres.value }

                        if (invalidSubgenres.isNotEmpty()) {
                            sheetSelectedSubgenres.value =
                                sheetSelectedSubgenres.value.filter { it in availableSubgenres.value }
                            _selectedSubgenre.value =
                                _selectedSubgenre.value.filter { it in availableSubgenres.value }
                        }
                    }
                }
                launch {
                    _availableCuts.collectLatest {
                        val invalidCuts =
                            sheetSelectedCuts.value.filter { it !in availableCuts.value }

                        if (invalidCuts.isNotEmpty()) {
                            sheetSelectedCuts.value =
                                _selectedCut.value.filter { it in availableCuts.value }
                            _selectedCut.value =
                                _selectedCut.value.filter { it in availableCuts.value }
                        }
                    }
                }
                launch {
                    _availableComponents.collectLatest {
                        val invalidComponents =
                            _selectedComponents.value.filter { it !in availableComponents.value }

                        if (invalidComponents.isNotEmpty()) {
                            sheetSelectedComponents.value =
                                sheetSelectedComponents.value.filter { it in availableComponents.value }
                            _selectedComponents.value =
                                _selectedComponents.value.filter { it in availableComponents.value }
                        }
                    }
                }
                launch {
                    _availableFlavorings.collectLatest {
                        val invalidFlavors =
                            _selectedFlavorings.value.filter { it !in availableFlavorings.value }

                        if (invalidFlavors.isNotEmpty()) {
                            sheetSelectedFlavorings.value =
                                sheetSelectedFlavorings.value.filter { it in availableFlavorings.value }
                            _selectedFlavorings.value =
                                _selectedFlavorings.value.filter { it in availableFlavorings.value }
                        }
                    }
                }
                launch {
                    _availableContainers.collectLatest {
                        val invalidContainers =
                            sheetSelectedContainer.value.filter { it !in availableContainers.value }

                        if (invalidContainers.isNotEmpty()) {
                            sheetSelectedContainer.value =
                                sheetSelectedContainer.value.filter { it in availableContainers.value }
                            _selectedContainer.value =
                                _selectedContainer.value.filter { it in availableContainers.value }
                        }
                    }
                }
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
            selectedSubgenre,
            selectedCut,
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
        return allItems.filter { items ->
            val tinFiltering = allItems.flatMap { it.tins }.filter {
                (!opened || it.openDate != null && it.openDate < System.currentTimeMillis()) &&
                        (!unopened || ((it.openDate == null || it.openDate >= System.currentTimeMillis()) && !it.finished)) &&
                        (!finished || it.finished) &&
                        (!unfinished || !it.finished) &&
                        (container.isEmpty() || container.contains(it.container.ifBlank { "(Unassigned)" }))
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

            val quantityRemap = allItems.associate { items ->
                items.items.id to calculateTotalQuantity(items, items.tins.filter { it in tinFiltering }, quantityOption, ozRate, gramsRate)
            }
            val isInStock = quantityRemap[items.items.id] != null && quantityRemap[items.items.id]!! > 0.0

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

            var tinsFilterResult = true

            if (applyTinFilter) {
                tinsFilterResult =
                    (!hasTins || items.tins.isNotEmpty()) &&
                            (!noTins || items.tins.isEmpty()) &&
                            (!opened || items.tins.any { it in tinFiltering }) &&
                            (!unopened || items.tins.any { it in tinFiltering }) &&
                            (!finished || items.tins.any { it in tinFiltering }) &&
                            (!unfinished || items.tins.any { it in tinFiltering }) &&
                            (container.isEmpty() || items.tins.any { it in tinFiltering })
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
        return allItems.flatMap { it.tins }.filter {
            ((container.isEmpty() && !container.contains("(Unassigned)")) || ((container.contains("(Unassigned)") && it.container.isBlank())) || container.contains(it.container)) &&
                    (!opened || (it.openDate != null && it.openDate < System.currentTimeMillis())) &&
                    (!unopened || (!it.finished && (it.openDate == null || it.openDate >= System.currentTimeMillis()))) &&
                    (!finished || it.finished) &&
                    (!unfinished || !it.finished)
        }
    }


    /** Enable/Disable Specific Filters based on Existing filtering **/
    @Suppress("UNCHECKED_CAST")
    val sheetSelectionsFlow: StateFlow<SheetSelections> =
        combine(
            sheetSelectedBrands, sheetSelectedExcludeBrands, sheetSelectedTypes,
            sheetSelectedFavorites, sheetSelectedExcludeFavorites, sheetSelectedDislikeds,
            sheetSelectedExcludeDislikeds, sheetSelectedInStock, sheetSelectedOutOfStock,
            sheetSelectedSubgenres, sheetSelectedCuts, sheetSelectedComponents, compMatching,
            sheetSelectedFlavorings, flavorMatching, sheetSelectedHasTins, sheetSelectedNoTins,
            sheetSelectedOpened, sheetSelectedUnopened, sheetSelectedFinished,
            sheetSelectedUnfinished, sheetSelectedContainer, sheetSelectedProduction,
            sheetSelectedOutOfProduction, sheetSelectedUnrated, sheetSelectedRatingLow,
            sheetSelectedRatingHigh
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

    private fun contextFilterForEnable(
        item: ItemsComponentsAndTins,
        selections: SheetSelections,
        ignoreCategory: FilterCategory? = null
    ): Boolean {
        val applyTins = true

        val stockGroup = ignoreCategory in setOf(FilterCategory.IN_STOCK, FilterCategory.OUT_OF_STOCK)
        val favDisGroup = ignoreCategory in setOf(
            FilterCategory.FAVORITE, FilterCategory.EXCLUDE_FAVORITE, FilterCategory.DISLIKED,
            FilterCategory.EXCLUDE_DISLIKED)
        val ratingGroup = ignoreCategory in setOf(FilterCategory.UNRATED, FilterCategory.RATING_LOW, FilterCategory.RATING_HIGH)
        val tinCheck = ignoreCategory in setOf(FilterCategory.HAS_TINS, FilterCategory.NO_TINS)
        val openCheck = ignoreCategory in setOf(FilterCategory.OPENED, FilterCategory.UNOPENED)
        val finishedCheck = ignoreCategory in setOf(FilterCategory.FINISHED, FilterCategory.UNFINISHED)
        val productionCheck = ignoreCategory in setOf(FilterCategory.PRODUCTION, FilterCategory.OUT_OF_PRODUCTION)

        val brandMatch =
            if (ignoreCategory == FilterCategory.BRAND) true
            else selections.brands.isEmpty() || selections.brands.contains(item.items.brand)
        val excludeBrandMatch =
            if (ignoreCategory == FilterCategory.EXCLUDE_BRAND) true
            else selections.excludeBrands.isEmpty() || !selections.excludeBrands.contains(item.items.brand)
        val typeMatch =
            if (ignoreCategory == FilterCategory.TYPE) true
            else selections.types.isEmpty() || (selections.types.contains(item.items.type.ifBlank { "(Unassigned)" }))
        val favoriteMatch =
            if (!favDisExist.value) false
            else if (favDisGroup) true
            else !selections.favorites || (if (selections.dislikeds) (item.items.disliked || item.items.favorite) else item.items.favorite)
        val excludeLikeMatch =
            if (!favDisExist.value) false
            else if (favDisGroup) true
            else !selections.excludeFavorites || !item.items.favorite
        val dislikedMatch =
            if (!favDisExist.value) false
            else if (favDisGroup) true
            else !selections.dislikeds || (if (selections.favorites) (item.items.disliked || item.items.favorite) else item.items.disliked)
        val excludeDislikeMatch =
            if (!favDisExist.value) false
            else if (favDisGroup) true
            else !selections.excludeDislikeds || !item.items.disliked
        val ratingsMatch =
            if (ratingGroup) true
            else {
                val unratedSelected = selections.unrated
                val ratingLowSelected = selections.ratingLow
                val ratingHighSelected = selections.ratingHigh

                val itemUnrated = item.items.rating == null

                val unratedPassed = unratedSelected && itemUnrated

                val rangeActive = ratingLowSelected != null || ratingHighSelected != null
                val rangePassed = rangeActive && !itemUnrated &&
                        (ratingLowSelected == null || item.items.rating >= ratingLowSelected) &&
                        (ratingHighSelected == null || item.items.rating <= ratingHighSelected)

                if (!unratedSelected && !rangeActive) {
                    true
                } else (unratedPassed || rangePassed)
            }
        val inStockMatch =
            if (stockGroup) true
            else !selections.inStock || item.items.quantity > 0
        val outOfStockMatch =
            if (stockGroup) true
            else !selections.outOfStock || item.items.quantity == 0
        val subgenreMatch =
            if (ignoreCategory == FilterCategory.SUBGENRE) true
            else selections.subgenres.isEmpty() || selections.subgenres.contains(item.items.subGenre.ifBlank { "(Unassigned)" })
        val cutMatch =
            if (ignoreCategory == FilterCategory.CUT) true
            else selections.cuts.isEmpty() || selections.cuts.contains(item.items.cut.ifBlank { "(Unassigned)" })
        val componentMatch =
            if (ignoreCategory == FilterCategory.COMPONENT) true
            else when (selections.compMatching) {
                FlowMatchOption.ALL -> selections.components.isEmpty() || (selections.components == listOf("(None Assigned)") && item.components.isEmpty()) || item.components.map { it.componentName }.containsAll(selections.components)
                FlowMatchOption.ONLY -> selections.components.isEmpty() || (selections.components == listOf("(None Assigned)") && item.components.isEmpty()) || (item.components.map { it.componentName }.containsAll(selections.components) && item.components.size == selections.components.size)
                else -> selections.components.isEmpty() || ((selections.components.contains("(None Assigned)") && item.components.isEmpty()) || item.components.map { it.componentName }.any { selections.components.contains(it) })
            }
        val flavorMatch =
            if (ignoreCategory == FilterCategory.FLAVORING) true
            else when (selections.flavorMatching) {
                FlowMatchOption.ALL -> selections.flavorings.isEmpty() || (selections.flavorings == listOf("(None Assigned)") && item.flavoring.isEmpty()) || item.flavoring.map { it.flavoringName }.containsAll(selections.flavorings)
                FlowMatchOption.ONLY -> selections.flavorings.isEmpty() || (selections.flavorings == listOf("(None Assigned)") && item.flavoring.isEmpty()) || (item.flavoring.map { it.flavoringName }.containsAll(selections.flavorings) && item.flavoring.size == selections.flavorings.size)
                else -> selections.flavorings.isEmpty() || ((selections.flavorings.contains("(None Assigned)") && item.flavoring.isEmpty()) || item.flavoring.map { it.flavoringName }.any { selections.flavorings.contains(it) })
            }
        val hasTinsMatch =
            if (!tinsExist.value) false
            else if (tinCheck) true
            else !selections.hasTins || item.tins.isNotEmpty()
        val noTinsMatch =
            if (!tinsExist.value) false
            else if (tinCheck) true
            else !selections.noTins || item.tins.isEmpty()
        val openedMatch =
            if (!tinsExist.value) false
            else if (openCheck) true
            else !selections.opened || item.tins.any { it.openDate != null && it.openDate < System.currentTimeMillis() }
        val unopenedMatch =
            if (!tinsExist.value) false
            else if (openCheck) true
            else !selections.unopened || item.tins.any { (it.openDate == null || it.openDate >= System.currentTimeMillis()) && !it.finished }
        val finishedMatch =
            if (!tinsExist.value) false
            else if (finishedCheck) true
            else (!selections.finished) || item.tins.any { it.finished }
        val unfinishedMatch =
            if (!tinsExist.value) false
            else if (finishedCheck) true
            else !selections.unfinished || item.tins.any { !it.finished }
        val containerMatch =
            if (ignoreCategory == FilterCategory.CONTAINER) true
            else selections.container.isEmpty() || item.tins.map { it.container }.any { selections.container.contains(it.ifBlank { "(Unassigned)" }) }
        val productionMatch =
            if (productionCheck) true
            else !selections.production || item.items.inProduction
        val outOfProductionMatch =
            if (productionCheck) true
            else !selections.outOfProduction || !item.items.inProduction

        return brandMatch && excludeBrandMatch && typeMatch && favoriteMatch && excludeLikeMatch &&
                dislikedMatch && excludeDislikeMatch && ratingsMatch && inStockMatch &&
                outOfStockMatch && subgenreMatch && cutMatch && componentMatch && flavorMatch &&
                productionMatch && outOfProductionMatch &&
                if (applyTins) {
                    hasTinsMatch && noTinsMatch  && openedMatch && unopenedMatch && finishedMatch &&
                            unfinishedMatch && containerMatch
                } else true
    }

    private fun contextTinFiltersForEnable(
        item: ItemsComponentsAndTins,
        selections: SheetSelections,
        ignoreCategory: FilterCategory? = null
    ): List<Tins> {
        if (item.tins.isEmpty()) return emptyList()

        val openCheck = ignoreCategory in setOf(FilterCategory.OPENED, FilterCategory.UNOPENED)
        val finishedCheck = ignoreCategory in setOf(FilterCategory.FINISHED, FilterCategory.UNFINISHED)

        return item.tins.filter {
            val openMatch =
                if (openCheck) true
                else !selections.opened || it.openDate != null && it.openDate < System.currentTimeMillis()
            val unopenedMatch =
                if (openCheck) true
                else !selections.unopened || (it.openDate == null || it.openDate >= System.currentTimeMillis()) && !it.finished
            val finishedMatch =
                if (finishedCheck) true
                else !selections.finished || it.finished
            val unfinishedMatch =
                if (finishedCheck) true
                else !selections.unfinished || !it.finished
            val containerMatch =
                if (ignoreCategory == FilterCategory.CONTAINER) true
                else selections.container.isEmpty() || selections.container.contains(it.container.ifBlank { "(Unassigned)" })

            openMatch && unopenedMatch && finishedMatch && unfinishedMatch && containerMatch
        }
    }

    private fun createEnabledFlow(
        check: (item: ItemsComponentsAndTins) -> Boolean,
        category: FilterCategory,
        currentlySelected: (selections: SheetSelections) -> Boolean
    ): StateFlow<Boolean> {
        return combine(
            everythingFlow,
            sheetSelectionsFlow
        ) { allItems, selections ->
            val calculation = if (allItems.isEmpty()) {
                false
            } else {
                allItems.any {
                    val inContext = contextFilterForEnable(it, selections, category)
                    if (inContext) {
                        check(it)
                    } else {
                        false
                    }
                }
            }
            calculation || currentlySelected(selections)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(200L),
                initialValue = true
            )
    }

    private fun createEnabledDoubleFlow(
        category: FilterCategory,
        boundSelector: (List<Double>) -> Double?
    ): StateFlow<Double?> {
        return combine(
            everythingFlow, sheetSelectionsFlow
        ) { allItems, selections ->
            if (allItems.isEmpty()) {
                null
            } else {
                val ratings = allItems.mapNotNull {
                    val inContext = contextFilterForEnable(it, selections, category)
                    if (inContext) it.items.rating else null
                }
                boundSelector(ratings)
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(200L),
                initialValue = null
            )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createMapEnabledFlow(
        allOptions: StateFlow<List<String>>,
        check: (option: String) -> ((item: ItemsComponentsAndTins) -> Boolean),
        category: FilterCategory,
        currentlySelected: (it: SheetSelections, option: String) -> Boolean
    ): StateFlow<Map<String, Boolean>> {
        return allOptions.flatMapLatest { optionList ->
            if (optionList.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val individualOptions: List<StateFlow<Boolean>> = optionList.map {
                    createEnabledFlow(check(it), category) { selections-> currentlySelected(selections, it) }
                }

                combine(individualOptions) {
                    val map = mutableMapOf<String, Boolean>()
                    optionList.forEachIndexed { index, option ->
                        map[option] = it[index]
                    }
                    map
                }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(500L),
                initialValue = emptyMap()
            )
    }

    val brandsEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableBrands,
        { name -> { it.items.brand == name } },
        FilterCategory.BRAND,
        { it, option -> it.brands.contains(option) }
    )
    val excludeBrandsEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableBrands,
        { name -> { it.items.brand != name } },
        FilterCategory.EXCLUDE_BRAND,
        { it, option -> !it.excludeBrands.contains(option) }
    )
    val typesEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableTypes,
        { name -> { it.items.type.ifBlank { "(Unassigned)" } == name } },
        FilterCategory.TYPE,
        { it, option -> it.types.contains(option) }
    )
    val favoritesEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.items.favorite },
        FilterCategory.FAVORITE,
        { it.favorites }
    )
    val excludeFavoritesEnabled: StateFlow<Boolean> = createEnabledFlow(
        { !it.items.favorite },
        FilterCategory.EXCLUDE_FAVORITE,
        { it.excludeFavorites }
    )
    val dislikedsEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.items.disliked },
        FilterCategory.DISLIKED,
        { it.dislikeds }
    )
    val excludeDislikesEnabled: StateFlow<Boolean> = createEnabledFlow(
        { !it.items.disliked },
        FilterCategory.EXCLUDE_DISLIKED,
        { it.excludeDislikeds }
    )
    val unratedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.items.rating == null },
        FilterCategory.UNRATED,
        { it.unrated }
    )
    val ratingLowEnabled: StateFlow<Double?> = createEnabledDoubleFlow(
        FilterCategory.RATING_LOW
    ) { it.minOrNull() }
    val ratingHighEnabled: StateFlow<Double?> = createEnabledDoubleFlow(
        FilterCategory.RATING_HIGH
    ) { it.maxOrNull() }
    @Suppress("UNCHECKED_CAST")
    val inStockEnabled: StateFlow<Boolean> = combine(
            everythingFlow, sheetSelectionsFlow, preferencesRepo.quantityOption,
            preferencesRepo.tinOzConversionRate, preferencesRepo.tinGramsConversionRate,
            unifiedFilteredTins
        ) { values ->
            val allItems = values[0] as List<ItemsComponentsAndTins>
            val selections = values[1] as SheetSelections
            val quantityOption = values[2] as QuantityOption
            val ozRate = values[3] as Double
            val gramsRate = values[4] as Double
            val filteredTins = values[5] as List<Tins>

            val relevantItems: List<ItemsComponentsAndTins> = allItems.filter {
                contextFilterForEnable(it, selections, FilterCategory.IN_STOCK)
            }

            relevantItems.any { items ->
                val quantityRemap = calculateTotalQuantity(items, items.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate)
                quantityRemap > 0.0
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = true
            )
    @Suppress("UNCHECKED_CAST")
    val outOfStockEnabled: StateFlow<Boolean> = combine(
            everythingFlow, sheetSelectionsFlow, preferencesRepo.quantityOption,
            preferencesRepo.tinOzConversionRate, preferencesRepo.tinGramsConversionRate,
            unifiedFilteredTins
        ) { values ->
            val allItems = values[0] as List<ItemsComponentsAndTins>
            val selections = values[1] as SheetSelections
            val quantityOption = values[2] as QuantityOption
            val ozRate = values[3] as Double
            val gramsRate = values[4] as Double
            val filteredTins = values[5] as List<Tins>

            val relevantItems: List<ItemsComponentsAndTins> = allItems.filter {
                contextFilterForEnable(it, selections, FilterCategory.OUT_OF_STOCK)
            }

            relevantItems.any { item ->
                val quantityRemap = calculateTotalQuantity(item, item.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate)
                quantityRemap == 0.0
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = true
            )
    val subgenresEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableSubgenres,
        { name -> { it.items.subGenre.ifBlank { "(Unassigned)" } == name } },
        FilterCategory.SUBGENRE,
        { it, option -> it.subgenres.contains(option) }
    )
    val cutsEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableCuts,
        { name -> { it.items.cut.ifBlank { "(Unassigned)" } == name } },
        FilterCategory.CUT,
        { it, option -> it.cuts.contains(option) }
    )
    val componentsEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableComponents,
        { option ->
            { item ->
                val currentSelected = sheetSelectionsFlow.value.components
                val currentMatching = sheetSelectionsFlow.value.compMatching

                if (currentMatching != FlowMatchOption.ANY) {
                    if (option == "(None Assigned)") { // enable "(None Assigned)" only if nothing selected
                        currentSelected.none { it != "(None Assigned)" } && item.components.isEmpty()
                    } else { // options other than "(None Assigned)"
                        if (currentSelected.contains("(None Assigned)")) { // disable others if "(None Assigned)" selected
                            false
                        } else { // anything other than "(None Assigned)" selected
                            val hypothetical = (currentSelected + option).distinct()
                            if (currentMatching == FlowMatchOption.ALL) {
                                item.components.map { it.componentName }.containsAll(hypothetical)
                            } else { // "Only" branch
                                item.components.map { it.componentName }.containsAll(hypothetical) && item.components.size == hypothetical.size
                            }
                        }
                    }
                } else { // "Any" branch
                    if (option == "(None Assigned)") { item.components.isEmpty() }
                    else { item.components.any { it.componentName == option } }
                }
            }
        },
        FilterCategory.COMPONENT,
        { it, option -> it.components.contains(option) }
    )
    val compMatchingEnabled: StateFlow<Map<FlowMatchOption, Boolean>> = combine(
        everythingFlow, sheetSelectionsFlow
    ) { allItems, selections ->
        FlowMatchOption.entries.associateWith { option ->
            if (selections.components.isEmpty()) true
            else {
                allItems.any { item ->
                    val selectedComps = selections.components
                    val itemComps = item.components.map { it.componentName }
                    val match = when (option) {
                        FlowMatchOption.ANY -> (selectedComps.contains("(None Assigned)") && itemComps.isEmpty()) || itemComps.any { selectedComps.contains(it) }
                        FlowMatchOption.ALL -> (selectedComps == listOf("(None Assigned)") && itemComps.isEmpty()) || itemComps.containsAll(selectedComps)
                        FlowMatchOption.ONLY -> (selectedComps == listOf("(None Assigned)") && itemComps.isEmpty()) || (itemComps.containsAll(selectedComps) && itemComps.size == selectedComps.size)
                    }
                    match && contextFilterForEnable(item, selections, FilterCategory.COMPONENT)
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = FlowMatchOption.entries.associateWith { true }
        )
    val flavoringsEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableFlavorings,
        { option ->
            { item ->
                val currentSelected = sheetSelectionsFlow.value.flavorings
                val currentMatching = sheetSelectionsFlow.value.flavorMatching

                if (currentMatching != FlowMatchOption.ANY) {
                    if (option == "(None Assigned)") { // enable "(None Assigned)" only if nothing selected
                        currentSelected.none { it != "(None Assigned)" } && item.flavoring.isEmpty()
                    } else { // options other than "(None Assigned)"
                        if (currentSelected.contains("(None Assigned)")) { // disable others if "(None Assigned)" selected
                            false
                        } else { // anything selected other than "(None Assigned)"
                            val hypothetical = (currentSelected + option).distinct()
                            if (currentMatching == FlowMatchOption.ALL) {
                                item.flavoring.map { it.flavoringName }.containsAll(hypothetical)
                            } else { // "Only" branch
                                item.flavoring.map { it.flavoringName }.containsAll(hypothetical) && item.flavoring.size == hypothetical.size
                            }
                        }
                    }
                } else { // "Any" branch
                    if (option == "(None Assigned)") { item.flavoring.isEmpty() }
                    else { item.flavoring.any { it.flavoringName == option } }
                }
            }
        },
        FilterCategory.FLAVORING,
        { it, option -> it.flavorings.contains(option) }
    )
    val flavorMatchingEnabled: StateFlow<Map<FlowMatchOption, Boolean>> = combine(
        everythingFlow, sheetSelectionsFlow
    ) { allItems, selections ->
        FlowMatchOption.entries.associateWith { option ->
            if (selections.flavorings.isEmpty()) true
            else {
                allItems.any { item ->
                    val selectedFlavor = selections.flavorings
                    val itemFlavors = item.components.map { it.componentName }
                    val match = when (option) {
                        FlowMatchOption.ANY -> (selectedFlavor.contains("(None Assigned)") && itemFlavors.isEmpty()) || itemFlavors.any { selectedFlavor.contains(it) }
                        FlowMatchOption.ALL -> (selectedFlavor == listOf("(None Assigned)") && itemFlavors.isEmpty()) || itemFlavors.containsAll(selectedFlavor)
                        FlowMatchOption.ONLY -> (selectedFlavor == listOf("(None Assigned)") && itemFlavors.isEmpty()) || (itemFlavors.containsAll(selectedFlavor) && itemFlavors.size == selectedFlavor.size)
                    }
                    match && contextFilterForEnable(item, selections, FilterCategory.FLAVORING)
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = FlowMatchOption.entries.associateWith { true }
        )
    val hasTinsEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.tins.isNotEmpty() },
        FilterCategory.HAS_TINS,
        { it.hasTins }
    )
    val noTinsEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.tins.isEmpty() },
        FilterCategory.NO_TINS,
        { it.noTins }
    )
    val openedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { item -> contextTinFiltersForEnable(item, sheetSelectionsFlow.value, FilterCategory.OPENED).any { it.openDate != null && (it.openDate < System.currentTimeMillis()) } },
        FilterCategory.OPENED,
        { it.opened }
    )
    val unopenedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { item -> contextTinFiltersForEnable(item, sheetSelectionsFlow.value, FilterCategory.UNOPENED).any { (it.openDate == null || it.openDate >= System.currentTimeMillis()) && !it.finished } },
        FilterCategory.UNOPENED,
        { it.unopened }
    )
    val finishedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { item -> contextTinFiltersForEnable(item, sheetSelectionsFlow.value, FilterCategory.FINISHED).any { it.finished } },
        FilterCategory.FINISHED,
        { it.finished }
    )
    val unfinishedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { item -> contextTinFiltersForEnable(item, sheetSelectionsFlow.value, FilterCategory.UNFINISHED).any { !it.finished } },
        FilterCategory.UNFINISHED,
        { it.unfinished }
    )
    val containerEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableContainers,
        { name -> { tins -> contextTinFiltersForEnable(tins, sheetSelectionsFlow.value, FilterCategory.CONTAINER).any { it.container.ifBlank { "(Unassigned)" } == name } } },
        FilterCategory.CONTAINER,
        { it, option -> it.container.contains(option) }
    )
    val productionEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.items.inProduction },
        FilterCategory.PRODUCTION,
        { it.production }
    )
    val outOfProductionEnabled: StateFlow<Boolean> = createEnabledFlow(
        { !it.items.inProduction },
        FilterCategory.OUT_OF_PRODUCTION,
        { it.outOfProduction }
    )


    /** Track and remove applied filters if items change and result in no returns **/
    private val _selectionHistory = MutableStateFlow<List<Pair<FilterCategory, Any?>>>(emptyList())
    private fun trackSelection(category: FilterCategory, value: Any?, added: Boolean) {
        val singletonCats = setOf(
            FilterCategory.FAVORITE, FilterCategory.EXCLUDE_FAVORITE, FilterCategory.DISLIKED,
            FilterCategory.EXCLUDE_DISLIKED, FilterCategory.UNRATED, FilterCategory.RATING_LOW,
            FilterCategory.RATING_HIGH, FilterCategory.IN_STOCK, FilterCategory.OUT_OF_STOCK,
            FilterCategory.HAS_TINS, FilterCategory.NO_TINS, FilterCategory.OPENED,
            FilterCategory.UNOPENED, FilterCategory.FINISHED, FilterCategory.UNFINISHED,
            FilterCategory.PRODUCTION, FilterCategory.OUT_OF_PRODUCTION
        )

        if (category in singletonCats) {
            _selectionHistory.value = _selectionHistory.value.filterNot { it.first == category }
        } else {
            _selectionHistory.value = _selectionHistory.value.filterNot { it.first == category && it.second == value }
        }

        if (added) {
            _selectionHistory.value += (category to value)
        }
    }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            unifiedFilteredItems.collectLatest { items ->
                if (items.isEmpty() && isFilterApplied.value && !searchPerformed.value && !_emptyDatabase.value) {
                    delay(50)
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

        val selections = sheetSelectionsFlow.value

        val disabledSelections = history.filter { (category, value) ->
            isSelectionDisabled(category, value, selections)
        }

        val target = disabledSelections.lastOrNull() ?: history.lastOrNull() ?: return
        val (category, value) = target

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
    private fun isSelectionDisabled(category: FilterCategory, value: Any?, selections: SheetSelections): Boolean {
        return when (category) {
            FilterCategory.BRAND -> brandsEnabled.value[value as String] == false
            FilterCategory.EXCLUDE_BRAND -> excludeBrandsEnabled.value[value as String] == false
            FilterCategory.TYPE -> typesEnabled.value[value as String] == false
            FilterCategory.FAVORITE -> !favoritesEnabled.value
            FilterCategory.EXCLUDE_FAVORITE -> !excludeFavoritesEnabled.value
            FilterCategory.DISLIKED -> !dislikedsEnabled.value
            FilterCategory.EXCLUDE_DISLIKED -> !excludeDislikesEnabled.value
            FilterCategory.UNRATED -> !unratedEnabled.value
            FilterCategory.RATING_LOW -> {
                val max = ratingHighEnabled.value
                selections.ratingLow != null && (max == null || selections.ratingLow > max)
            }
            FilterCategory.RATING_HIGH -> {
                val min = ratingLowEnabled.value
                selections.ratingHigh != null && (min == null || selections.ratingHigh < min)
            }
            FilterCategory.IN_STOCK -> !inStockEnabled.value
            FilterCategory.OUT_OF_STOCK -> !outOfStockEnabled.value
            FilterCategory.SUBGENRE -> subgenresEnabled.value[value as String] == false
            FilterCategory.CUT -> cutsEnabled.value[value as String] == false
            FilterCategory.COMPONENT -> componentsEnabled.value[value as String] == false
            FilterCategory.FLAVORING -> flavoringsEnabled.value[value as String] == false
            FilterCategory.HAS_TINS -> !hasTinsEnabled.value
            FilterCategory.NO_TINS -> !noTinsEnabled.value
            FilterCategory.OPENED -> !openedEnabled.value
            FilterCategory.UNOPENED -> !unopenedEnabled.value
            FilterCategory.FINISHED -> !finishedEnabled.value
            FilterCategory.UNFINISHED -> !unfinishedEnabled.value
            FilterCategory.CONTAINER -> containerEnabled.value[value as String] == false
            FilterCategory.PRODUCTION -> !productionEnabled.value
            FilterCategory.OUT_OF_PRODUCTION -> !outOfProductionEnabled.value
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
        sheetSelectedExcludeBrandSwitch,
        sheetSelectedBrands,
        sheetSelectedExcludeBrands,
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
        sheetSelectedExcludeBrandSwitch,
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
        sheetSelectedExcludeBrandSwitch,
        sheetSelectedBrands,
        sheetSelectedExcludeBrands,
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
        return if (text.isBlank()) {
            allBrands
        } else {
            val startsWith = allBrands.filter { brand ->
                brand.startsWith(text, ignoreCase = true)
            }
            val otherWordsStartsWith = allBrands.filter { brand ->
                brand.split(" ").drop(1).any { word ->
                    word.startsWith(text, ignoreCase = true)
                } && !brand.startsWith(text, ignoreCase = true)
            }
            val contains = allBrands.filter { brand ->
                brand.contains(text, ignoreCase = true)
                        && !brand.startsWith(text, ignoreCase = true) &&
                        !otherWordsStartsWith.contains(brand)
            }
            startsWith + otherWordsStartsWith + contains
        }
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
        sheetSelectedFavorites,
        sheetSelectedExcludeFavorites
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
        sheetSelectedDislikeds,
        sheetSelectedExcludeDislikeds
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
                if (!enabled && sheetSelectedExcludeFavorites.value) {
                    updateSelectedExcludeFavorites(false)
                }
            }
        }
        viewModelScope.launch {
            dislikedsEnabled.collect { enabled ->
                if (!enabled && sheetSelectedExcludeDislikeds.value) {
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
        sheetSelectedRatingLow,
        sheetSelectedRatingHigh
    ) { low, high ->
        low == null && high == null
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = true
        )

    val rangeLowText = sheetSelectedRatingLow.map { formatDecimal(it, 1).ifBlank { "0" } }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = "0"
        )

    val rangeHighText = sheetSelectedRatingHigh.map { formatDecimal(it, 1).ifBlank { "5" } }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = "5"
        )

    val rangeLowTextAlpha = combine(
        sheetSelectedRatingLow,
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
        sheetSelectedRatingHigh,
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
        sheetSelectedRatingLow,
        sheetSelectedRatingHigh
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


    val hasContainer = sheetSelectedContainer.map { it.isNotEmpty() }

    val implicitHasTins = combine(
        sheetSelectedHasTins,
        sheetSelectedOpened,
        sheetSelectedUnopened,
        sheetSelectedFinished,
        sheetSelectedUnfinished,
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
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedBrands.value += brand
            _selectedBrands.value += brand
        } else {
            sheetSelectedBrands.value -= brand
            _selectedBrands.value -= brand
        }
        trackSelection(FilterCategory.BRAND, brand, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedExcludeBrands.value += brand
            _selectedExcludeBrands.value += brand
        } else {
            sheetSelectedExcludeBrands.value -= brand
            _selectedExcludeBrands.value -= brand
        }
        trackSelection(FilterCategory.EXCLUDE_BRAND, brand, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeBrandsSwitch() {
        sheetSelectedExcludeBrandSwitch.value = !sheetSelectedExcludeBrandSwitch.value

        if (sheetSelectedExcludeBrandSwitch.value) {
            if (sheetSelectedBrands.value.isNotEmpty()) {
                sheetSelectedExcludeBrands.value = sheetSelectedBrands.value
                _selectedExcludeBrands.value = _selectedBrands.value

                sheetSelectedBrands.value = emptyList()
                _selectedBrands.value = emptyList()

                _selectedExcludeBrands.value.forEach {
                    trackSelection(FilterCategory.EXCLUDE_BRAND, it, true)
                }
                _selectedBrands.value.forEach {
                    trackSelection(FilterCategory.BRAND, it, false)
                }
            }
        } else {
            if (sheetSelectedExcludeBrands.value.isNotEmpty()) {
                sheetSelectedBrands.value = _selectedExcludeBrands.value
                _selectedBrands.value = _selectedExcludeBrands.value

                sheetSelectedExcludeBrands.value = emptyList()
                _selectedExcludeBrands.value = emptyList()

                _selectedBrands.value.forEach {
                    trackSelection(FilterCategory.BRAND, it, true)
                }
                _selectedExcludeBrands.value.forEach {
                    trackSelection(FilterCategory.EXCLUDE_BRAND, it, false)
                }
            }
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedTypes.value += type
            _selectedTypes.value += type
        } else {
            sheetSelectedTypes.value -= type
            _selectedTypes.value -= type
        }
        trackSelection(FilterCategory.TYPE, type, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedExcludeFavorites.value = false
            _selectedExcludeFavorites.value = false
        }

        sheetSelectedFavorites.value = isSelected
        _selectedFavorites.value = isSelected

        trackSelection(FilterCategory.FAVORITE, null, isSelected)
        trackSelection(FilterCategory.EXCLUDE_FAVORITE, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeFavorites(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedFavorites.value = false
            _selectedFavorites.value = false
        }

        sheetSelectedExcludeFavorites.value = isSelected
        _selectedExcludeFavorites.value = isSelected

        trackSelection(FilterCategory.EXCLUDE_FAVORITE, null, isSelected)
        trackSelection(FilterCategory.FAVORITE, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedExcludeDislikeds.value = false
            _selectedExcludeDislikeds.value = false
        }

        sheetSelectedDislikeds.value = isSelected
        _selectedDislikeds.value = isSelected

        trackSelection(FilterCategory.DISLIKED, null, isSelected)
        trackSelection(FilterCategory.EXCLUDE_DISLIKED, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeDislikeds(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedDislikeds.value = false
            _selectedDislikeds.value = false
        }

        sheetSelectedExcludeDislikeds.value = isSelected
        _selectedExcludeDislikeds.value = isSelected

        trackSelection(FilterCategory.EXCLUDE_DISLIKED, null, isSelected)
        trackSelection(FilterCategory.DISLIKED, null, false)

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
        sheetSelectedUnrated.value = isSelected
        _selectedUnrated.value = isSelected

        trackSelection(FilterCategory.UNRATED, null, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedRatingRange(low: Double?, high: Double?) {
        sheetSelectedRatingLow.value = low
        _selectedRatingLow.value = low

        if (low != null) {
            trackSelection(FilterCategory.RATING_LOW, low, true)
        } else trackSelection(FilterCategory.RATING_LOW, null, false)

        sheetSelectedRatingHigh.value = high
        _selectedRatingHigh.value = high

        if (high != null) {
            trackSelection(FilterCategory.RATING_HIGH, high, true)
        } else trackSelection(FilterCategory.RATING_HIGH, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedInStock(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedOutOfStock.value = false
            _selectedOutOfStock.value = false
        }

        sheetSelectedInStock.value = isSelected
        _selectedInStock.value = isSelected

        trackSelection(FilterCategory.IN_STOCK, null, isSelected)
        trackSelection(FilterCategory.OUT_OF_STOCK, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedInStock.value = false
            _selectedInStock.value = false
        }

        sheetSelectedOutOfStock.value = isSelected
        _selectedOutOfStock.value = isSelected

        trackSelection(FilterCategory.OUT_OF_STOCK, null, isSelected)
        trackSelection(FilterCategory.IN_STOCK, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedSubgenre(subgenre: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedSubgenres.value += subgenre
            _selectedSubgenre.value += subgenre
        } else {
            sheetSelectedSubgenres.value -= subgenre
            _selectedSubgenre.value -= subgenre
        }

        trackSelection(FilterCategory.SUBGENRE, subgenre, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedCut(cut: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedCuts.value += cut
            _selectedCut.value += cut
        } else {
            sheetSelectedCuts.value -= cut
            _selectedCut.value -= cut
        }

        trackSelection(FilterCategory.CUT, cut, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateSelectedComponent(component: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedComponents.value += component
            _selectedComponents.value += component
        } else {
            sheetSelectedComponents.value -= component
            _selectedComponents.value -= component
        }

        trackSelection(FilterCategory.COMPONENT, component, isSelected)

        _shouldScrollUp.value = true
    }

    fun updateCompMatching(option: FlowMatchOption) {
        sheetSelectedCompMatching.value = option
        _compMatching.value = option

        if (option != FlowMatchOption.ANY) {
            val selected = sheetSelectedComponents.value
            val pruned = selected.filter { componentsEnabled.value[it] == true }
            if (pruned.size < selected.size) {
                val removed = selected - pruned.toSet()
                removed.forEach { trackSelection(FilterCategory.COMPONENT, it, false) }

                sheetSelectedComponents.value = pruned
                _selectedComponents.value = pruned
            }
        }
    }

    fun updateSelectedFlavoring(flavoring: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedFlavorings.value += flavoring
            _selectedFlavorings.value += flavoring
        } else {
            sheetSelectedFlavorings.value -= flavoring
            _selectedFlavorings.value -= flavoring
        }
        _shouldScrollUp.value = true

        trackSelection(FilterCategory.FLAVORING, flavoring, isSelected)
    }

    fun updateFlavorMatching(option: FlowMatchOption) {
        sheetSelectedFlavorMatching.value = option
        _flavorMatching.value = option

        if (option != FlowMatchOption.ANY) {
            val selected = sheetSelectedFlavorings.value
            val pruned = selected.filter { flavoringsEnabled.value[it] == true }
            if (pruned.size < selected.size) {
                val removed = selected - pruned.toSet()
                removed.forEach { trackSelection(FilterCategory.FLAVORING, it, false) }

                sheetSelectedFlavorings.value = pruned
                _selectedFlavorings.value = pruned
            }
        }
    }

    fun updateSelectedProduction(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedOutOfProduction.value = false
            _selectedOutOfProduction.value = false
        }

        sheetSelectedProduction.value = isSelected
        _selectedProduction.value = isSelected

        trackSelection(FilterCategory.PRODUCTION, null, isSelected)
        trackSelection(FilterCategory.OUT_OF_PRODUCTION, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfProduction(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedProduction.value = false
            _selectedProduction.value = false
        }

        sheetSelectedOutOfProduction.value = isSelected
        _selectedOutOfProduction.value = isSelected

        trackSelection(FilterCategory.OUT_OF_PRODUCTION, null, isSelected)
        trackSelection(FilterCategory.PRODUCTION, null, false)

        _shouldScrollUp.value = true
    }


    // Tins filtering //
    fun updateSelectedHasTins(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedNoTins.value = false
            _selectedNoTins.value = false
        }

        sheetSelectedHasTins.value = isSelected
        _selectedHasTins.value = isSelected

        trackSelection(FilterCategory.HAS_TINS, null, isSelected)
        trackSelection(FilterCategory.NO_TINS, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedNoTins(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedHasTins.value = false
            _selectedHasTins.value = false

            sheetSelectedOpened.value = false
            sheetSelectedUnopened.value = false
            sheetSelectedFinished.value = false
            sheetSelectedUnfinished.value = false
            _selectedOpened.value = false
            _selectedUnopened.value = false
            _selectedFinished.value = false
            _selectedUnfinished.value = false
        }

        sheetSelectedNoTins.value = isSelected
        _selectedNoTins.value = isSelected

        trackSelection(FilterCategory.NO_TINS, null, isSelected)
        trackSelection(FilterCategory.HAS_TINS, null, false)
        trackSelection(FilterCategory.OPENED, null, false)
        trackSelection(FilterCategory.UNOPENED, null, false)
        trackSelection(FilterCategory.FINISHED, null, false)
        trackSelection(FilterCategory.UNFINISHED, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedOpened(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedUnopened.value = false
            _selectedUnopened.value = false

            sheetSelectedNoTins.value = false
            _selectedNoTins.value = false
        }

        sheetSelectedOpened.value = isSelected
        _selectedOpened.value = isSelected

        trackSelection(FilterCategory.OPENED, null, isSelected)
        trackSelection(FilterCategory.UNOPENED, null, false)
        trackSelection(FilterCategory.NO_TINS, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedUnopened(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedOpened.value = false
            _selectedOpened.value = false

            sheetSelectedNoTins.value = false
            _selectedNoTins.value = false
        }

        sheetSelectedUnopened.value = isSelected
        _selectedUnopened.value = isSelected

        trackSelection(FilterCategory.UNOPENED, null, isSelected)
        trackSelection(FilterCategory.OPENED, null, false)
        trackSelection(FilterCategory.NO_TINS, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedFinished(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedUnfinished.value = false
            _selectedUnfinished.value = false

            sheetSelectedNoTins.value = false
            _selectedNoTins.value = false
        }

        sheetSelectedFinished.value = isSelected
        _selectedFinished.value = isSelected

        trackSelection(FilterCategory.FINISHED, null, isSelected)
        trackSelection(FilterCategory.UNFINISHED, null, false)
        trackSelection(FilterCategory.NO_TINS, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedUnfinished(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedFinished.value = false
            _selectedFinished.value = false

            sheetSelectedNoTins.value = false
            _selectedNoTins.value = false
        }

        sheetSelectedUnfinished.value = isSelected
        _selectedUnfinished.value = isSelected

        trackSelection(FilterCategory.UNFINISHED, null, isSelected)
        trackSelection(FilterCategory.FINISHED, null, false)
        trackSelection(FilterCategory.NO_TINS, null, false)

        _shouldScrollUp.value = true
    }

    fun updateSelectedContainer(container: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedContainer.value += container
            _selectedContainer.value += container
        } else {
            sheetSelectedContainer.value -= container
            _selectedContainer.value -= container
        }

        trackSelection(FilterCategory.CONTAINER, container, isSelected)


        _shouldScrollUp.value = true
    }

    // Filter overflow check and clearing //
    fun overflowCheck(selected: List<String>, available: List<String>, shown: Int): Boolean {
        val overflowedItems = available.drop(shown)
        return selected.any { overflowedItems.contains(it) }
    }

    fun clearAllSelected(field: ClearAll) {
        when (field) {
            ClearAll.SUBGENRE -> {
                sheetSelectedSubgenres.value = emptyList()
                _selectedSubgenre.value = emptyList()
                _selectedSubgenre.value.forEach {
                    trackSelection(FilterCategory.SUBGENRE, it, false)
                }
            }
            ClearAll.CUT -> {
                sheetSelectedCuts.value = emptyList()
                _selectedCut.value = emptyList()
                _selectedCut.value.forEach {
                    trackSelection(FilterCategory.CUT, it, false)
                }
            }
            ClearAll.COMPONENT -> {
                sheetSelectedComponents.value = emptyList()
                _selectedComponents.value = emptyList()
                _compMatching.value = FlowMatchOption.ANY
                sheetSelectedCompMatching.value = FlowMatchOption.ANY
                _selectedComponents.value.forEach {
                    trackSelection(FilterCategory.COMPONENT, it, false)
                }
            }
            ClearAll.FLAVORING -> {
                sheetSelectedFlavorings.value = emptyList()
                _selectedFlavorings.value = emptyList()
                _flavorMatching.value = FlowMatchOption.ANY
                sheetSelectedFlavorMatching.value = FlowMatchOption.ANY
                _selectedFlavorings.value.forEach {
                    trackSelection(FilterCategory.FLAVORING, it, false)
                }
            }
            ClearAll.CONTAINER -> {
                sheetSelectedContainer.value = emptyList()
                _selectedContainer.value = emptyList()
                _selectedContainer.value.forEach {
                    trackSelection(FilterCategory.CONTAINER, it, false)
                }
            }
        }
        _shouldScrollUp.value = true
    }

    fun clearAllSelectedBrands() {
        val historyToClear = _selectedBrands.value.ifEmpty { _selectedExcludeBrands.value }
        val category = if (_selectedBrands.value.isEmpty()) FilterCategory.EXCLUDE_BRAND else FilterCategory.BRAND
        historyToClear.forEach {
            trackSelection(category, it, false)
        }

        sheetSelectedBrands.value = emptyList()
        _selectedBrands.value = emptyList()

        sheetSelectedExcludeBrands.value = emptyList()
        _selectedExcludeBrands.value = emptyList()

        updateClearBrandTrigger()

        _shouldScrollUp.value = true
    }

    fun resetFilter() {
        // sheet state //
        sheetSelectedBrands.value = emptyList()
        sheetSelectedTypes.value = emptyList()
        sheetSelectedFavorites.value = false
        sheetSelectedDislikeds.value = false
        sheetSelectedInStock.value = false
        sheetSelectedOutOfStock.value = false
        sheetSelectedUnrated.value = false
        sheetSelectedRatingLow.value = null
        sheetSelectedRatingHigh.value = null
        sheetSelectedExcludeBrands.value = emptyList()
        sheetSelectedExcludeFavorites.value = false
        sheetSelectedExcludeDislikeds.value = false
        sheetSelectedSubgenres.value = emptyList()
        sheetSelectedCuts.value = emptyList()
        sheetSelectedComponents.value = emptyList()
        sheetSelectedCompMatching.value = FlowMatchOption.ANY
        sheetSelectedFlavorings.value = emptyList()
        sheetSelectedFlavorMatching.value = FlowMatchOption.ANY
        sheetSelectedProduction.value = false
        sheetSelectedOutOfProduction.value = false

        sheetSelectedHasTins.value = false
        sheetSelectedNoTins.value = false
        sheetSelectedContainer.value = emptyList()
        sheetSelectedOpened.value = false
        sheetSelectedUnopened.value = false
        sheetSelectedFinished.value = false
        sheetSelectedUnfinished.value = false

        // filtering state //
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
        _selectedSubgenre.value = emptyList()
        _selectedCut.value = emptyList()
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