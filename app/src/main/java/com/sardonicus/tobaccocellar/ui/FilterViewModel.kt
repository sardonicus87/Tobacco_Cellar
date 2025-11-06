package com.sardonicus.tobaccocellar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.home.SearchClearedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchPerformedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchSetting
import com.sardonicus.tobaccocellar.ui.items.ItemSavedEvent
import com.sardonicus.tobaccocellar.ui.items.ItemUpdatedEvent
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val _searchIconOpacity = MutableStateFlow(0.5f)
    val searchIconOpacity: StateFlow<Float> = _searchIconOpacity.asStateFlow()

    fun updateSearchIconOpacity(opacity: Float) { _searchIconOpacity.value = opacity }

    fun saveSearchSetting(setting: String) {
        viewModelScope.launch { preferencesRepo.setSearchSetting(setting) }
    }

    private val _searchTextDisplay = MutableStateFlow("")
    val searchTextDisplay: StateFlow<String> = _searchTextDisplay

    fun updateSearchText(text: String) { _searchTextDisplay.value = text }

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
            preferencesRepo.searchSetting.collect {
                _isTinSearch.value = (it == SearchSetting.TinLabel)
            }
        }
    }


    /** Sheet selection states **/
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
    val sheetSelectedCompMatching = MutableStateFlow("Any")
    val sheetSelectedFlavorings = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedFlavorMatching = MutableStateFlow("Any")
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

    private val _compMatching = MutableStateFlow("Any")
    val compMatching: StateFlow<String> = _compMatching

    private val _selectedFlavorings = MutableStateFlow<List<String>>(emptyList())
    val selectedFlavorings: StateFlow<List<String>> = _selectedFlavorings

    private val _flavorMatching = MutableStateFlow("Any")
    val flavorMatching: StateFlow<String> = _flavorMatching

    private val _selectedProduction = MutableStateFlow(false)
    val selectedProduction: StateFlow<Boolean> = _selectedProduction

    private val _selectedOutOfProduction = MutableStateFlow(false)
    val selectedOutOfProduction: StateFlow<Boolean> = _selectedOutOfProduction

    // tins switch and filtering
    @Suppress("UNCHECKED_CAST")
    val showTins: StateFlow<Boolean> = combine(
        sheetSelectedContainer, sheetSelectedOpened, sheetSelectedUnopened, sheetSelectedFinished,
        sheetSelectedUnfinished, isTinSearch
    ) {
        val container = it[0] as List<String>
        val opened = it[1] as Boolean
        val unopened = it[2] as Boolean
        val finished = it[3] as Boolean
        val unfinished = it[4] as Boolean

        container.isNotEmpty() || opened || unopened || finished || unfinished

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
    @Suppress("UNCHECKED_CAST")
    val isFilterApplied: StateFlow<Boolean> = combine(
        sheetSelectedBrands, sheetSelectedTypes, sheetSelectedFavorites,
        sheetSelectedDislikeds, sheetSelectedInStock, sheetSelectedOutOfStock,
        sheetSelectedExcludeBrands, sheetSelectedExcludeFavorites, sheetSelectedExcludeDislikeds,
        sheetSelectedSubgenres, sheetSelectedCuts, sheetSelectedComponents, sheetSelectedFlavorings,
        sheetSelectedProduction, sheetSelectedOutOfProduction, sheetSelectedHasTins,
        sheetSelectedNoTins, sheetSelectedContainer, sheetSelectedOpened, sheetSelectedUnopened,
        sheetSelectedFinished, sheetSelectedUnfinished, sheetSelectedUnrated, sheetSelectedRatingLow,
        sheetSelectedRatingHigh
    ) {
        val brands = it[0] as List<String>
        val types = it[1] as List<String>
        val favorites = it[2] as Boolean
        val dislikeds = it[3] as Boolean
        val inStock = it[4] as Boolean
        val outOfStock = it[5] as Boolean
        val excludeBrands = it[6] as List<String>
        val excludeFavorites = it[7] as Boolean
        val excludeDislikeds = it[8] as Boolean
        val subgenres = it[9] as List<String>
        val cuts = it[10] as List<String>
        val components = it[11] as List<String>
        val flavorings = it[12] as List<String>
        val production = it[13] as Boolean
        val outOfProduction = it[14] as Boolean
        val hasTins = it[15] as Boolean
        val noTins = it[16] as Boolean
        val container = it[17] as List<String>
        val opened = it[18] as Boolean
        val unopened = it[19] as Boolean
        val finished = it[20] as Boolean
        val unfinished = it[21] as Boolean
        val unrated = it[22] as Boolean
        val ratingLow = it[23] as Double?
        val ratingHigh = it[24] as Double?

        brands.isNotEmpty() ||
            types.isNotEmpty() || favorites || dislikeds || inStock || outOfStock ||
            excludeBrands.isNotEmpty() || excludeFavorites || excludeDislikeds ||
            subgenres.isNotEmpty() || cuts.isNotEmpty() || components.isNotEmpty() ||
            flavorings.isNotEmpty() || production || outOfProduction || hasTins || noTins ||
            container.isNotEmpty() || opened || unopened || finished || unfinished || unrated ||
            ratingLow != null || ratingHigh != null

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

    private val _searchPerformed = MutableStateFlow(false)
    val searchPerformed: StateFlow<Boolean> = _searchPerformed.asStateFlow()

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
                    _shouldScrollUp.value = true
                }
            }
        }
    }


    /** Filtering states **/
    // available fields for filter //
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

    private val _typesExist = MutableStateFlow(false)
    val typesExist: StateFlow<Boolean> = _typesExist

    private val _subgenresExist = MutableStateFlow(false)
    val subgenresExist: StateFlow<Boolean> = _subgenresExist

    private val _ratingsExist = MutableStateFlow(false)
    val ratingsExist: StateFlow<Boolean> = _ratingsExist

    private val _favDisExist = MutableStateFlow(false)
    val favDisExist: StateFlow<Boolean> = _favDisExist

    private val _tinsExist = MutableStateFlow(false)
    val tinsExist: StateFlow<Boolean> = _tinsExist

    private val _tinsReady = MutableStateFlow(false)
    val tinsReady: StateFlow<Boolean> = _tinsReady

    private val _notesExist = MutableStateFlow(false)
    val notesExist: StateFlow<Boolean> = _notesExist

    private val _datesExist = MutableStateFlow(false)
    val datesExist: StateFlow<Boolean> = _datesExist

    private val _emptyDatabase = MutableStateFlow(false)
    val emptyDatabase: StateFlow<Boolean> = _emptyDatabase

    // database refresh on restore
    @OptIn(ExperimentalCoroutinesApi::class)
    val everythingFlow: Flow<List<ItemsComponentsAndTins>> =
        refresh.onStart { emit(Unit) }.flatMapLatest {
            itemsRepository.getEverythingStream()
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val lastSeenFlow: Flow<List<Int>> = preferencesRepo.datesSeen.map {
        if (it.isBlank()) { emptyList() }
        else { it.split(",").mapNotNull { it.trim().toIntOrNull() } }
    }

    // setting available vals
    init {
        viewModelScope.launch {
            everythingFlow.collectLatest {
                _availableBrands.value = it.map { it.items.brand }
                    .distinct().sorted()
                _availableTypes.value = it.map { it.items.type.ifBlank { "(Unassigned)" } }
                    .distinct().sortedWith(
                        compareBy { typeOrder[it] ?: typeOrder.size }
                    )
                _availableSubgenres.value = it.map {
                    it.items.subGenre.ifBlank { "(Unassigned)" }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                        .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                )
                _availableCuts.value = it.map {
                    it.items.cut.ifBlank { "(Unassigned)" }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                        .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                )
                _availableComponents.value = it.flatMap {
                    it.components.ifEmpty {
                        listOf(Components(componentName = "(None Assigned)"))
                    }.map { it.componentName }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(None Assigned)") 1 else 0 }
                        .thenBy { if (it != "(None Assigned)") it.lowercase() else "" }
                )
                _availableFlavorings.value = it.flatMap {
                    it.flavoring.ifEmpty {
                        listOf(Flavoring(flavoringName = "(None Assigned)"))
                    }.map { it.flavoringName }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(None Assigned)") 1 else 0 }
                        .thenBy { if (it != "(None Assigned)") it.lowercase() else "" }
                )
                _availableContainers.value = it.flatMap {
                    it.tins }.map { it.container.ifBlank { "(Unassigned)" }
                }.distinct().sortedWith(
                    compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                        .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                )
                _typesExist.value = it.any { it.items.type.isNotBlank() }
                _subgenresExist.value = it.any { it.items.subGenre.isNotBlank() }
                _ratingsExist.value = it.any { it.items.rating != null }
                _favDisExist.value = it.any { it.items.favorite || it.items.disliked }
                _tinsExist.value = it.any { it.tins.isNotEmpty() }
                _notesExist.value = it.any { it.items.notes.isNotBlank() }
                _datesExist.value = it.flatMap { it.tins }.any {
                    it.manufactureDate != null || it.cellarDate != null || it.openDate != null
                }
                _emptyDatabase.value = it.isEmpty()
            }
        }
        viewModelScope.launch {
            combine (
                everythingFlow,
                lastSeenFlow
            ) { everything, lastSeen ->
                val currentReady = everything.flatMap { it.tins }
                    .filter {
                        it.openDate?.let {
                            Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate() in LocalDate.now()..LocalDate.now().plusDays(7)
                        } ?: false
                    }.map { it.tinId }
                _tinsReady.value = currentReady.any { it !in lastSeen }
            }.collect()
        }
    }

    // remove selected filters if the last item with that filter is removed or changed //
    init {
        viewModelScope.launch {
            launch {
                _availableBrands.collectLatest {
                    val invalidBrands = _selectedBrands.value.filter { it !in availableBrands.value }

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
                    val invalidTypes = _selectedTypes.value.filter { it !in availableTypes.value }

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
                    val invalidSubgenres = _selectedSubgenre.value.filter { it !in availableSubgenres.value }

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
                    val invalidCuts = sheetSelectedCuts.value.filter { it !in availableCuts.value }

                    if (invalidCuts.isNotEmpty()) {
                        sheetSelectedCuts.value =
                            _selectedCut.value.filter { it in availableCuts.value }
                        _selectedCut.value = _selectedCut.value.filter { it in availableCuts.value }
                    }
                }
            }
            launch {
                _availableComponents.collectLatest {
                    val invalidComponents = _selectedComponents.value.filter { it !in availableComponents.value }

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
                    val invalidFlavors = _selectedFlavorings.value.filter { it !in availableFlavorings.value }

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
                    val invalidContainers = sheetSelectedContainer.value.filter { it !in availableContainers.value }

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
            selectedRatingHigh
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
            val compMatching = values[13] as String
            val flavoring = values[14] as List<String>
            val flavorMatching = values[15] as String
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

            val applyTin = true

            generateFilteredItemsList(
                allItems, brands, excludeBrands, types, favorites, dislikeds, excludeLikes,
                excludeDislikes, inStock, outOfStock, subgenres, cuts, components, compMatching,
                flavoring, flavorMatching, production, outOfProduction, hasTins, noTins, container,
                opened, unopened, finished, unfinished, unrated, ratingLow, ratingHigh, applyTin
            )
        }
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
                allItems.filter {
                    when (currentSearchSetting) {
                        SearchSetting.Blend -> it.items.blend.contains(currentSearchValue, true)
                        SearchSetting.Notes -> it.items.notes.contains(currentSearchValue, true)
                        SearchSetting.TinLabel -> it.tins.any { it.tinLabel.contains(currentSearchValue, true) }
                    }
                }
            }
            else filteredItems
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private fun generateFilteredItemsList(
        allItems: List<ItemsComponentsAndTins>,

        brands: List<String>, excludeBrands: List<String>, types: List<String>, favorites: Boolean,
        dislikeds: Boolean, excludeFavorites: Boolean, excludeDislikeds: Boolean, inStock: Boolean,
        outOfStock: Boolean, subgenres: List<String>, cuts: List<String>, components: List<String>,
        compMatching: String, flavorings: List<String>, flavorMatching: String, production: Boolean,
        outOfProduction: Boolean, hasTins: Boolean, noTins: Boolean, container: List<String>,
        opened: Boolean, unopened: Boolean, finished: Boolean, unfinished: Boolean, unrated: Boolean,
        ratingLow: Double?, ratingHigh: Double?,

        applyTinFilter: Boolean
    ): List<ItemsComponentsAndTins> {
        return allItems.filter { items ->
            val compMatch = when (compMatching) {
                "All" -> (components.isEmpty() || (components == listOf("(None Assigned)") && items.components.isEmpty()) || items.components.map { it.componentName }.containsAll(components))
                "Only" -> (components.isEmpty() || (components == listOf("(None Assigned)") && items.components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components) && items.components.size == components.size))
                else -> (components.isEmpty() || ((components.contains("(None Assigned)") && items.components.isEmpty()) || items.components.map { it.componentName }.any { components.contains(it) }))
            }
            val flavorMatch = when (flavorMatching) {
                "All" -> (flavorings.isEmpty() || (flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || items.flavoring.map { it.flavoringName }.containsAll(flavorings))
                "Only" -> (flavorings.isEmpty() || (flavorings == listOf("(None Assigned)") && items.flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.containsAll(flavorings) && items.flavoring.size == flavorings.size))
                else -> (flavorings.isEmpty() || ((flavorings.contains("(None Assigned)") && items.flavoring.isEmpty()) || items.flavoring.map { it.flavoringName }.any { flavorings.contains(it) }))
            }
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
                        (!inStock || items.items.quantity > 0) &&
                        (!outOfStock || items.items.quantity == 0) &&
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

            val tinFiltering = allItems.flatMap { it.tins }.filter {
                (!opened || it.openDate != null && it.openDate < System.currentTimeMillis()) &&
                        (!unopened || ((it.openDate == null || it.openDate >= System.currentTimeMillis()) && !it.finished)) &&
                        (!finished || it.finished) &&
                        (!unfinished || !it.finished) &&
                        (container.isEmpty() || container.contains(it.container.ifBlank { "(Unassigned)" }))
            }

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
                compMatching = it[12] as String,
                flavorings = it[13] as List<String>,
                flavorMatching = it[14] as String,
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
        val applyTins = true // activeScreen.value != ActiveScreen.DATES

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
            if (favDisGroup) true
            else !selections.favorites || item.items.favorite
        val excludeLikeMatch =
            if (favDisGroup) true
            else !selections.excludeFavorites || !item.items.favorite
        val dislikedMatch =
            if (favDisGroup) true
            else !selections.dislikeds || item.items.disliked
        val excludeDislikeMatch =
            if (favDisGroup) true
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
                "All" -> selections.components.isEmpty() || (selections.components == listOf("(None Assigned)") && item.components.isEmpty()) || item.components.map { it.componentName }.containsAll(selections.components)
                "Only" -> selections.components.isEmpty() || (selections.components == listOf("(None Assigned)") && item.components.isEmpty()) || (item.components.map { it.componentName }.containsAll(selections.components) && item.components.size == selections.components.size)
                else -> selections.components.isEmpty() || ((selections.components.contains("(None Assigned)") && item.components.isEmpty()) || item.components.map { it.componentName }.any { selections.components.contains(it) })
            }
        val flavorMatch =
            if (ignoreCategory == FilterCategory.FLAVORING) true
            else when (selections.flavorMatching) {
                "All" -> selections.flavorings.isEmpty() || (selections.flavorings == listOf("(None Assigned)") && item.flavoring.isEmpty()) || item.flavoring.map { it.flavoringName }.containsAll(selections.flavorings)
                "Only" -> selections.flavorings.isEmpty() || (selections.flavorings == listOf("(None Assigned)") && item.flavoring.isEmpty()) || (item.flavoring.map { it.flavoringName }.containsAll(selections.flavorings) && item.flavoring.size == selections.flavorings.size)
                else -> selections.flavorings.isEmpty() || ((selections.flavorings.contains("(None Assigned)") && item.flavoring.isEmpty()) || item.flavoring.map { it.flavoringName }.any { selections.flavorings.contains(it) })
            }
        val hasTinsMatch =
            if (tinCheck) true
            else !selections.hasTins || item.tins.isNotEmpty()
        val noTinsMatch =
            if (tinCheck) true
            else !selections.noTins || item.tins.isEmpty()
        val openedMatch =
            if (openCheck) true
            else !selections.opened || item.tins.any { it.openDate != null && it.openDate < System.currentTimeMillis() }
        val unopenedMatch =
            if (openCheck) true
            else !selections.unopened || item.tins.any { (it.openDate == null || it.openDate >= System.currentTimeMillis()) && !it.finished }
        val finishedMatch =
            if (finishedCheck) true
            else (!selections.finished) || item.tins.any { it.finished }
        val unfinishedMatch =
            if (finishedCheck) true
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
    val inStockEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.items.quantity > 0 },
        FilterCategory.IN_STOCK,
        { it.inStock }
    )
    val outOfStockEnabled: StateFlow<Boolean> = createEnabledFlow(
        { it.items.quantity == 0 },
        FilterCategory.OUT_OF_STOCK,
        { it.outOfStock }
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

                if (currentMatching != "Any") {
                    if (option == "(None Assigned)") { // enable "(None Assigned)" only if nothing selected
                        currentSelected.none { it != "(None Assigned)" } && item.components.isEmpty()
                    } else { // options other than "(None Assigned)"
                        if (currentSelected.contains("(None Assigned)")) { // disable others if "(None Assigned)" selected
                            false
                        } else { // anything other than "(None Assigned)" selected
                            val hypothetical = (currentSelected + option).distinct()
                            if (currentMatching == "All") {
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
    val flavoringsEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableFlavorings,
        { option ->
            { item ->
                val currentSelected = sheetSelectionsFlow.value.flavorings
                val currentMatching = sheetSelectionsFlow.value.flavorMatching

                if (currentMatching != "Any") {
                    if (option == "(None Assigned)") { // enable "(None Assigned)" only if nothing selected
                        currentSelected.none { it != "(None Assigned)" } && item.flavoring.isEmpty()
                    } else { // options other than "(None Assigned)"
                        if (currentSelected.contains("(None Assigned)")) { // disable others if "(None Assigned)" selected
                            false
                        } else { // anything selected other than "(None Assigned)"
                            val hypothetical = (currentSelected + option).distinct()
                            if (currentMatching == "All") {
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
        { contextTinFiltersForEnable(it, sheetSelectionsFlow.value, FilterCategory.OPENED).any { it.openDate != null && (it.openDate < System.currentTimeMillis()) } },
        FilterCategory.OPENED,
        { it.opened }
    )
    val unopenedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { contextTinFiltersForEnable(it, sheetSelectionsFlow.value, FilterCategory.UNOPENED).any { (it.openDate == null || it.openDate >= System.currentTimeMillis()) && !it.finished } },
        FilterCategory.UNOPENED,
        { it.unopened }
    )
    val finishedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { contextTinFiltersForEnable(it, sheetSelectionsFlow.value, FilterCategory.FINISHED).any { it.finished } },
        FilterCategory.FINISHED,
        { it.finished }
    )
    val unfinishedEnabled: StateFlow<Boolean> = createEnabledFlow(
        { contextTinFiltersForEnable(it, sheetSelectionsFlow.value, FilterCategory.UNFINISHED).any { !it.finished } },
        FilterCategory.UNFINISHED,
        { it.unfinished }
    )
    val containerEnabled: StateFlow<Map<String, Boolean>> = createMapEnabledFlow(
        availableContainers,
        { name -> { contextTinFiltersForEnable(it, sheetSelectionsFlow.value, FilterCategory.CONTAINER).any { it.container.ifBlank { "(Unassigned)" } == name } } },
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

    fun reorderChips(available: List<String>, enablement: Map<String, Boolean>): List<String> {
        if (available.isEmpty()) return emptyList()
        return available.sortedBy { if (enablement[it] == true) 0 else 1 }
    }

    // Flow section filtering data
    @Suppress("UNCHECKED_CAST")
    val brandsData: StateFlow<BrandsSectionData> = combine(
        availableBrands,
        sheetSelectedBrands,
        sheetSelectedExcludeBrands,
        sheetSelectedExcludeBrandSwitch,
        brandsEnabled,
        excludeBrandsEnabled
    ) {
            val allBrands = it[0] as List<String>
            val selectedBrands = it[1] as List<String>
            val excludeBrands = it[2] as List<String>
            val excludeBrandSwitch = it[3] as Boolean
            val brandsEnabled = it[4] as Map<String, Boolean>
            val excludeBrandsEnabled = it[5] as Map<String, Boolean>
            BrandsSectionData(
                allBrands,
                selectedBrands,
                excludeBrands,
                excludeBrandSwitch,
                brandsEnabled,
                excludeBrandsEnabled
            )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = BrandsSectionData()
        )
    val typeData: StateFlow<FilterSectionData> = generateFilterSectionData(
        availableTypes,
        typesEnabled,
        sheetSelectedTypes,
        reorder = false
    )
    val otherData: StateFlow<OtherSectionData> = combine(
        sheetSelectedFavorites,
        favoritesEnabled,
        sheetSelectedExcludeFavorites,
        excludeFavoritesEnabled,
        sheetSelectedDislikeds,
        dislikedsEnabled,
        sheetSelectedExcludeDislikeds,
        excludeDislikesEnabled,
        sheetSelectedInStock,
        inStockEnabled,
        sheetSelectedOutOfStock,
        outOfStockEnabled,
        sheetSelectedUnrated,
        unratedEnabled,
        sheetSelectedRatingLow,
        ratingLowEnabled,
        sheetSelectedRatingHigh,
        ratingHighEnabled,
        ratingsExist
    ) { it: Array<Any?> ->
        val favorites = it[0] as Boolean
        val favoritesEnabled = it[1] as Boolean
        val excludeFavorites = it[2] as Boolean
        val excludeFavoritesEnabled = it[3] as Boolean
        val dislikeds = it[4] as Boolean
        val dislikedsEnabled = it[5] as Boolean
        val excludeDislikeds = it[6] as Boolean
        val excludeDislikedsEnabled = it[7] as Boolean
        val inStock = it[8] as Boolean
        val inStockEnabled = it[9] as Boolean
        val outOfStock = it[10] as Boolean
        val outOfStockEnabled = it[11] as Boolean
        val unrated = it[12] as Boolean
        val unratedEnabled = it[13] as Boolean
        val ratingLow = it[14] as Double?
        val ratingLowEnabled = it[15] as Double?
        val ratingHigh = it[16] as Double?
        val ratingHighEnabled = it[17] as Double?
        val ratingsExist = it[18] as Boolean

        OtherSectionData(
            favorites,
            excludeFavorites,
            dislikeds,
            excludeDislikeds,
            unrated,
            ratingLow,
            ratingHigh,
            inStock,
            outOfStock,
            favoritesEnabled,
            excludeFavoritesEnabled,
            dislikedsEnabled,
            excludeDislikedsEnabled,
            unratedEnabled,
            ratingLowEnabled,
            ratingHighEnabled,
            inStockEnabled,
            outOfStockEnabled,
            ratingsExist
        )
    }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500L),
        initialValue = OtherSectionData()
    )
    val subgenreData: StateFlow<FilterSectionData> = generateFilterSectionData(
        availableSubgenres,
        subgenresEnabled,
        sheetSelectedSubgenres
    )
    val cutData: StateFlow<FilterSectionData> = generateFilterSectionData(
            availableCuts,
            cutsEnabled,
            sheetSelectedCuts
        )
    val componentData: StateFlow<FilterSectionData> = generateFilterSectionData(
            availableComponents,
            componentsEnabled,
            sheetSelectedComponents,
            sheetSelectedCompMatching
        )
    val flavoringData: StateFlow<FilterSectionData> = generateFilterSectionData(
            availableFlavorings,
            flavoringsEnabled,
            sheetSelectedFlavorings,
            sheetSelectedFlavorMatching
        )
    val containerData: StateFlow<FilterSectionData> = generateFilterSectionData(
            availableContainers,
            containerEnabled,
            sheetSelectedContainer
        )
    val tinsFilterData: StateFlow<TinsFilterData> = combine(
        sheetSelectedHasTins,
        hasTinsEnabled,
        sheetSelectedNoTins,
        noTinsEnabled,
        sheetSelectedOpened,
        openedEnabled,
        sheetSelectedUnopened,
        unopenedEnabled,
        sheetSelectedFinished,
        finishedEnabled,
        sheetSelectedUnfinished,
        unfinishedEnabled
    ) {
        val hasTins = it[0]
        val hasEnabled = it[1]
        val noTins = it[2]
        val noEnabled = it[3]
        val opened = it[4]
        val openedEnabled = it[5]
        val unopened = it[6]
        val unopenedEnabled = it[7]
        val finished = it[8]
        val finishedEnabled = it[9]
        val unfinished = it[10]
        val unfinishedEnabled = it[11]

        TinsFilterData(
            hasTins,
            noTins,
            opened,
            unopened,
            finished,
            unfinished,
            hasEnabled,
            noEnabled,
            openedEnabled,
            unopenedEnabled,
            finishedEnabled,
            unfinishedEnabled
        )
    }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500L),
        initialValue = TinsFilterData()
    )
    val productionData: StateFlow<ProductionSectionData> = combine(
        sheetSelectedProduction,
        productionEnabled,
        sheetSelectedOutOfProduction,
        outOfProductionEnabled
    ) { production, productionEnabled, outOfProduction, outOfProductionEnabled ->
        ProductionSectionData(
            production,
            outOfProduction,
            productionEnabled,
            outOfProductionEnabled
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = ProductionSectionData()
        )

    private fun generateFilterSectionData(
        available: StateFlow<List<String>>,
        enabled: StateFlow<Map<String, Boolean>>,
        selected: StateFlow<List<String>>,
        matching: StateFlow<String>? = null,
        reorder: Boolean = true
    ): StateFlow<FilterSectionData> {
        return if (matching != null) {
            combine(
                available,
                enabled,
                selected,
                matching,
            ) { available, enabled, selected, matching ->
                FilterSectionData(
                    available = if (reorder) reorderChips(available, enabled) else available,
                    selected = selected,
                    enabled = enabled,
                    matching = matching
                )
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(500L),
                    initialValue = FilterSectionData(emptyList(), emptyList(), emptyMap(), null)
                )

        } else {
            combine(
                available,
                enabled,
                selected,
            ) { available, enabled, selected ->
                FilterSectionData(
                    available = if (reorder) reorderChips(available, enabled) else available,
                    selected = selected,
                    enabled = enabled,
                    matching = ""
                )
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(500L),
                    initialValue = FilterSectionData(emptyList(), emptyList(), emptyMap())
                )
        }
    }


    // filter selection update functions //
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedBrands.value += brand
            _selectedBrands.value += brand
        } else {
            sheetSelectedBrands.value -= brand
            _selectedBrands.value -= brand
        }

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

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeBrandsSwitch(isSelected: Boolean) {
        sheetSelectedExcludeBrandSwitch.value = isSelected

        if (isSelected) {
            if (sheetSelectedBrands.value.isNotEmpty()) {
                sheetSelectedExcludeBrands.value = sheetSelectedBrands.value
                _selectedExcludeBrands.value = _selectedBrands.value

                sheetSelectedBrands.value = emptyList()
                _selectedBrands.value = emptyList()
            }
        } else {
            if (sheetSelectedExcludeBrands.value.isNotEmpty()) {
                sheetSelectedBrands.value = _selectedExcludeBrands.value
                _selectedBrands.value = _selectedExcludeBrands.value

                sheetSelectedExcludeBrands.value = emptyList()
                _selectedExcludeBrands.value = emptyList()
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

        _shouldScrollUp.value = true
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedExcludeFavorites.value = false
            _selectedExcludeFavorites.value = false
        }

        sheetSelectedFavorites.value = isSelected
        _selectedFavorites.value = isSelected

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeFavorites(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedFavorites.value = false
            _selectedFavorites.value = false
        }

        sheetSelectedExcludeFavorites.value = isSelected
        _selectedExcludeFavorites.value = isSelected

        _shouldScrollUp.value = true
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedExcludeDislikeds.value = false
            _selectedExcludeDislikeds.value = false
        }

        sheetSelectedDislikeds.value = isSelected
        _selectedDislikeds.value = isSelected

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeDislikeds(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedDislikeds.value = false
            _selectedDislikeds.value = false
        }

        sheetSelectedExcludeDislikeds.value = isSelected
        _selectedExcludeDislikeds.value = isSelected

        _shouldScrollUp.value = true
    }

    fun updateSelectedUnrated(isSelected: Boolean) {
        sheetSelectedUnrated.value = isSelected
        _selectedUnrated.value = isSelected

        _shouldScrollUp.value = true
    }

    fun updateSelectedRatingRange(low: Double?, high: Double?) {
        sheetSelectedRatingLow.value = low
        _selectedRatingLow.value = low

        sheetSelectedRatingHigh.value = high
        _selectedRatingHigh.value = high

        _shouldScrollUp.value = true
    }

    fun updateSelectedInStock(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedOutOfStock.value = false
            _selectedOutOfStock.value = false
        }

        sheetSelectedInStock.value = isSelected
        _selectedInStock.value = isSelected

        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedInStock.value = false
            _selectedInStock.value = false
        }

        sheetSelectedOutOfStock.value = isSelected
        _selectedOutOfStock.value = isSelected

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

        _shouldScrollUp.value = true
    }

    fun updateCompMatching(option: String) {
        sheetSelectedCompMatching.value = option
        _compMatching.value = option

        if (option != "Any") {
            val selected = sheetSelectedComponents.value
            val pruned = selected.filter { componentsEnabled.value[it] == true }
            if (pruned.size < selected.size) {
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
    }

    fun updateFlavorMatching(option: String) {
        sheetSelectedFlavorMatching.value = option
        _flavorMatching.value = option

        if (option != "Any") {
            val selected = sheetSelectedFlavorings.value
            val pruned = selected.filter { flavoringsEnabled.value[it] == true }
            if (pruned.size < selected.size) {
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

        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfProduction(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedProduction.value = false
            _selectedProduction.value = false
        }

        sheetSelectedOutOfProduction.value = isSelected
        _selectedOutOfProduction.value = isSelected

        _shouldScrollUp.value = true
    }


    // Tins filtering //
    fun updateSelectedTins(string: String, it: Boolean) {
        when (string) {
            "has" -> updateSelectedHasTins(it)
            "no" -> updateSelectedNoTins(it)
            "opened" -> updateSelectedOpened(it)
            "unopened" -> updateSelectedUnopened(it)
            "finished" -> updateSelectedFinished(it)
            "unfinished" -> updateSelectedUnfinished(it)
        }
    }

    fun updateSelectedHasTins(isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedNoTins.value = false
            _selectedNoTins.value = false
        }

        sheetSelectedHasTins.value = isSelected
        _selectedHasTins.value = isSelected

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

        _shouldScrollUp.value = true
    }

    // Filter overflow check and clearing //
    fun overflowCheck(selected: List<String>, available: List<String>, shown: Int): Boolean {
        val overflowedItems = available.drop(shown)
        return selected.any { overflowedItems.contains(it) }
    }

    fun clearAllSelected(field: String) {
        when (field) {
            "Subgenre" -> {
                sheetSelectedSubgenres.value = emptyList()
                _selectedSubgenre.value = emptyList()
            }
            "Cut" -> {
                sheetSelectedCuts.value = emptyList()
                _selectedCut.value = emptyList()
            }
            "Components" -> {
                sheetSelectedComponents.value = emptyList()
                _selectedComponents.value = emptyList()
                _compMatching.value = "Any"
                sheetSelectedCompMatching.value = "Any"
            }
            "Flavorings" -> {
                sheetSelectedFlavorings.value = emptyList()
                _selectedFlavorings.value = emptyList()
                _flavorMatching.value = "Any"
                sheetSelectedFlavorMatching.value = "Any"
            }
            "Container" -> {
                sheetSelectedContainer.value = emptyList()
                _selectedContainer.value = emptyList()
            }
        }
        _shouldScrollUp.value = true
    }

    fun clearAllSelectedBrands() {
        sheetSelectedBrands.value = emptyList()
        _selectedBrands.value = emptyList()

        sheetSelectedExcludeBrands.value = emptyList()
        _selectedExcludeBrands.value = emptyList()

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
        sheetSelectedCompMatching.value = "Any"
        sheetSelectedFlavorings.value = emptyList()
        sheetSelectedFlavorMatching.value = "Any"
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
        _compMatching.value = "Any"
        _selectedFlavorings.value = emptyList()
        _flavorMatching.value = "Any"
        _selectedProduction.value = false
        _selectedOutOfProduction.value = false

        _selectedHasTins.value = false
        _selectedNoTins.value = false
        _selectedContainer.value = emptyList()
        _selectedOpened.value = false
        _selectedUnopened.value = false
        _selectedFinished.value = false
        _selectedUnfinished.value = false

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
    val compMatching: String = "Any",
    val flavorings: List<String> = emptyList(),
    val flavorMatching: String = "Any",
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

data class FilterSectionData(
    val available: List<String>,
    val selected: List<String>,
    val enabled: Map<String, Boolean>,
    val matching: String? = null,
)

data class BrandsSectionData(
    val allBrands: List<String> = emptyList(),
    val selectedBrands: List<String> = emptyList(),
    val excludeBrands: List<String> = emptyList(),
    val excludeBrandSwitch: Boolean = false,

    val includeBrandsEnabled: Map<String, Boolean> = emptyMap(),
    val excludeBrandsEnabled: Map<String, Boolean> = emptyMap(),
)

data class OtherSectionData(
    val favorites: Boolean = false,
    val excludeFavorites: Boolean = false,
    val dislikeds: Boolean = false,
    val excludeDislikeds: Boolean = false,
    val unrated: Boolean = false,
    val ratingLow: Double? = null,
    val ratingHigh: Double? = null,
    val inStock: Boolean = false,
    val outOfStock: Boolean = false,

    val favoritesEnabled: Boolean = false,
    val excludeFavoritesEnabled: Boolean = false,
    val dislikedsEnabled: Boolean = false,
    val excludeDislikedsEnabled: Boolean = false,
    val unratedEnabled: Boolean = false,
    val ratingLowEnabled: Double? = null,
    val ratingHighEnabled: Double? = null,
    val inStockEnabled: Boolean = false,
    val outOfStockEnabled: Boolean = false,

    val ratingsExist: Boolean = false,
)

data class TinsFilterData(
    val hasTins: Boolean = false,
    val noTins: Boolean = false,
    val opened: Boolean = false,
    val unopened: Boolean = false,
    val finished: Boolean = false,
    val unfinished: Boolean = false,

    val hasEnabled: Boolean = false,
    val noEnabled: Boolean = false,
    val openedEnabled: Boolean = false,
    val unopenedEnabled: Boolean = false,
    val finishedEnabled: Boolean = false,
    val unfinishedEnabled: Boolean = false,
)

data class ProductionSectionData(
    val production: Boolean = false,
    val outOfProduction: Boolean = false,

    val productionEnabled: Boolean = false,
    val outOfProductionEnabled: Boolean = false,
)


val typeOrder = mapOf(
    "Aromatic" to 0,
    "English" to 1,
    "Burley" to 2,
    "Virginia" to 3,
    "Other" to 4,
    "(Unassigned)" to 5
)