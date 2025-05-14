package com.sardonicus.tobaccocellar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.home.SearchClearedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchPerformedEvent
import com.sardonicus.tobaccocellar.ui.items.ItemSavedEvent
import com.sardonicus.tobaccocellar.ui.items.ItemUpdatedEvent
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterViewModel (
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo
): ViewModel() {
    /** BottomSheet State **/
    private val _bottomSheetState = MutableStateFlow(BottomSheetState.CLOSED)
    val bottomSheetState: StateFlow<BottomSheetState> = _bottomSheetState.asStateFlow()

    fun openBottomSheet() {
        _bottomSheetState.value = BottomSheetState.OPENED
    }

    fun closeBottomSheet() {
        _bottomSheetState.value = BottomSheetState.CLOSED
    }


    /** HomeScreen HomeHeader blend search **/
    private val _searchIconOpacity = MutableStateFlow(0.5f)
    val searchIconOpacity: StateFlow<Float> = _searchIconOpacity.asStateFlow()

    fun updateSearchIconOpacity(opacity: Float) {
        _searchIconOpacity.value = opacity
    }

    fun saveSearchSetting(setting: String) {
        viewModelScope.launch {
            preferencesRepo.setSearchSetting(setting)
        }
    }

    private val _searchTextDisplay = MutableStateFlow("")
    val searchTextDisplay: StateFlow<String> = _searchTextDisplay

    fun updateSearchText(text: String) {
        _searchTextDisplay.value = text
    }

    private val _searchValue = MutableStateFlow("")
    val searchValue: StateFlow<String> = _searchValue

    fun onSearch(text: String) {
        _searchValue.value = text
    }

    private val _searchFocused = MutableStateFlow(false)
    val searchFocused: StateFlow<Boolean> = _searchFocused.asStateFlow()

    fun updateSearchFocused(focused: Boolean) {
        _searchFocused.value = focused
    }


    /** Sheet selection states **/
    // sheet selection states //
    val sheetSelectedBrands = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedTypes = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedUnassigned = MutableStateFlow(false)
    val sheetSelectedFavorites = MutableStateFlow(false)
    val sheetSelectedDislikeds = MutableStateFlow(false)
    val sheetSelectedNeutral = MutableStateFlow(false)
    val sheetSelectedNonNeutral = MutableStateFlow(false)
    val sheetSelectedInStock = MutableStateFlow(false)
    val sheetSelectedOutOfStock = MutableStateFlow(false)
    val sheetSelectedExcludeBrands = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedExcludeBrandSwitch = MutableStateFlow(false)
    val sheetSelectedExcludeLikes = MutableStateFlow(false)
    val sheetSelectedExcludeDislikes = MutableStateFlow(false)

    val sheetSelectedSubgenres = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedCuts = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedComponents = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedComponentMatching = MutableStateFlow<String>("Any")
    val sheetSelectedFlavorings = MutableStateFlow<List<String>>(emptyList())
    val sheetSelectedFlavoringMatching = MutableStateFlow<String>("Any")
    val sheetSelectedProduction = MutableStateFlow(false)
    val sheetSelectedOutOfProduction = MutableStateFlow(false)

    val sheetSelectedHasTins = MutableStateFlow(false)
    val sheetSelectedNoTins = MutableStateFlow(false)
    val sheetSelectedContainer = MutableStateFlow<List<String>>(emptyList())


    // inclusionary filter states //
    private val _selectedBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedBrands: StateFlow<List<String>> = _selectedBrands

    private val _selectedTypes = MutableStateFlow<List<String>>(emptyList())
    val selectedTypes: StateFlow<List<String>> = _selectedTypes

    private val _selectedUnassigned = MutableStateFlow(false)
    val selectedUnassigned: StateFlow<Boolean> = _selectedUnassigned

    private val _selectedFavorites = MutableStateFlow(false)
    val selectedFavorites: StateFlow<Boolean> = _selectedFavorites

    private val _selectedDislikeds = MutableStateFlow(false)
    val selectedDislikeds: StateFlow<Boolean> = _selectedDislikeds

    private val _selectedNeutral = MutableStateFlow(false)
    val selectedNeutral: StateFlow<Boolean> = _selectedNeutral

    private val _selectedNonNeutral = MutableStateFlow(false)
    val selectedNonNeutral: StateFlow<Boolean> = _selectedNonNeutral

    private val _selectedInStock = MutableStateFlow(false)
    val selectedInStock: StateFlow<Boolean> = _selectedInStock

    private val _selectedOutOfStock = MutableStateFlow(false)
    val selectedOutOfStock: StateFlow<Boolean> = _selectedOutOfStock

    private val _selectedSubgenre = MutableStateFlow<List<String>>(emptyList())
    val selectedSubgenre: StateFlow<List<String>> = _selectedSubgenre

    private val _selectedCut = MutableStateFlow<List<String>>(emptyList())
    val selectedCut: StateFlow<List<String>> = _selectedCut

    private val _selectedComponent = MutableStateFlow<List<String>>(emptyList())
    val selectedComponent: StateFlow<List<String>> = _selectedComponent

    private val _compMatching = MutableStateFlow<String>("Any")
    val compMatching: StateFlow<String> = _compMatching

    private val _selectedFlavoring = MutableStateFlow<List<String>>(emptyList())
    val selectedFlavoring: StateFlow<List<String>> = _selectedFlavoring

    private val _flavorMatching = MutableStateFlow<String>("Any")
    val flavorMatching: StateFlow<String> = _flavorMatching

    private val _selectedProduction = MutableStateFlow(false)
    val selectedProduction: StateFlow<Boolean> = _selectedProduction

    private val _selectedOutOfProduction = MutableStateFlow(false)
    val selectedOutOfProduction: StateFlow<Boolean> = _selectedOutOfProduction

    private val _selectedHasTins = MutableStateFlow(false)
    val selectedHasTins: StateFlow<Boolean> = _selectedHasTins

    private val _selectedNoTins = MutableStateFlow(false)
    val selectedNoTins: StateFlow<Boolean> = _selectedNoTins

    // tins switch and filtering
    val showTins: StateFlow<Boolean> = combine(
        sheetSelectedContainer
    ) {
        val container = it[0] as List<String>

        container.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )


    private val _selectedContainer = MutableStateFlow<List<String>>(emptyList())
    val selectedContainer: StateFlow<List<String>> = _selectedContainer


    // exclusionary filter states //
    private val _selectedExcludeBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedExcludeBrands: StateFlow<List<String>> = _selectedExcludeBrands

    private val _selectedExcludeLikes = MutableStateFlow(false)
    val selectedExcludeLikes: StateFlow<Boolean> = _selectedExcludeLikes

    private val _selectedExcludeDislikes = MutableStateFlow(false)
    val selectedExcludeDislikes: StateFlow<Boolean> = _selectedExcludeDislikes


    // filter applied state for clear all button //
    val isFilterApplied: StateFlow<Boolean> = combine(
        sheetSelectedBrands, sheetSelectedTypes, sheetSelectedUnassigned, sheetSelectedFavorites,
        sheetSelectedDislikeds, sheetSelectedNeutral, sheetSelectedNonNeutral, sheetSelectedInStock,
        sheetSelectedOutOfStock, sheetSelectedExcludeBrands, sheetSelectedExcludeLikes,
        sheetSelectedExcludeDislikes, sheetSelectedSubgenres, sheetSelectedCuts,
        sheetSelectedComponents, sheetSelectedFlavorings, sheetSelectedProduction,
        sheetSelectedOutOfProduction, sheetSelectedHasTins, sheetSelectedNoTins,
        sheetSelectedContainer
    ) {
        val brands = it[0] as List<String>
        val types = it[1] as List<String>
        val unassigned = it[2] as Boolean
        val favorites = it[3] as Boolean
        val dislikeds = it[4] as Boolean
        val neutral = it[5] as Boolean
        val nonNeutral = it[6] as Boolean
        val inStock = it[7] as Boolean
        val outOfStock = it[8] as Boolean
        val excludeBrands = it[9] as List<String>
        val excludeLikes = it[10] as Boolean
        val excludeDislikes = it[11] as Boolean
        val subgenres = it[12] as List<String>
        val cuts = it[13] as List<String>
        val components = it[14] as List<String>
        val flavorings = it[15] as List<String>
        val production = it[16] as Boolean
        val outOfProduction = it[17] as Boolean
        val hasTins = it[18] as Boolean
        val noTins = it[19] as Boolean
        val container = it[20] as List<String>

        brands.isNotEmpty() ||
            types.isNotEmpty() ||
            unassigned ||
            favorites ||
            dislikeds ||
            neutral ||
            nonNeutral ||
            inStock ||
            outOfStock ||
            excludeBrands.isNotEmpty() ||
            excludeLikes ||
            excludeDislikes ||
            subgenres.isNotEmpty() ||
            cuts.isNotEmpty() ||
            components.isNotEmpty() ||
            flavorings.isNotEmpty() ||
            production ||
            outOfProduction ||
            hasTins ||
            noTins ||
            container.isNotEmpty()
    }.stateIn(
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
    val searchCleared: StateFlow<Boolean> = _searchCleared.asStateFlow()

    private val _searchPerformed = MutableStateFlow(false)
    val searchPerformed: StateFlow<Boolean> = _searchPerformed.asStateFlow()

    // remember scroll position //
    private val _currentPosition = MutableStateFlow(mapOf(0 to 0, 1 to 0))
    val currentPosition: StateFlow<Map<Int, Int>> = _currentPosition.asStateFlow()

    fun updateScrollPosition(index: Int, offset: Int) {
        _currentPosition.value = mapOf(0 to index, 1 to offset)
    }

    fun getPositionTrigger() {
        _getPosition.value++
        _shouldReturn.value = true
    }

    fun resetScroll() {
        _shouldScrollUp.value = false
        _shouldReturn.value = false
        _getPosition.value = 0
        _searchCleared.value = false
        _searchPerformed.value = false
        _savedItemId.value = -1
        _currentPosition.value = mapOf(0 to 0, 1 to 0)
    }

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
                    _shouldScrollUp.value = true
                }
            }
        }
    }


    /** Filtering states **/
    // get available fields for filter //
    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands

    private val _availableSubgenres = MutableStateFlow<List<String>>(emptyList())
    val availableSubgenres: StateFlow<List<String>> = _availableSubgenres

    private val _availableCuts = MutableStateFlow<List<String>>(emptyList())
    val availableCuts: StateFlow<List<String>> = _availableCuts

    private val _availableComponents = MutableStateFlow<List<String>>(emptyList())
    val availableComponents: StateFlow<List<String>> = _availableComponents

    private val _availableFlavors = MutableStateFlow<List<String>>(emptyList())
    val availableFlavors: StateFlow<List<String>> = _availableFlavors

    private val _availableContainers = MutableStateFlow<List<String>>(emptyList())
    val availableContainers: StateFlow<List<String>> = _availableContainers


    private val _refresh = MutableSharedFlow<Unit>(replay = 0)
    private val refresh = _refresh.asSharedFlow()

    init {
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    _refresh.emit(Unit)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val everythingFlow: Flow<List<ItemsComponentsAndTins>> =
        refresh.onStart { emit(Unit) }.flatMapLatest {
            itemsRepository.getEverythingStream()
        }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                everythingFlow.collectLatest {
                    _availableBrands.value = it.map { it.items.brand }.distinct().sorted()
                    _availableSubgenres.value = it.map {
                        if (it.items.subGenre.isBlank()) {
                            "(Unassigned)"
                        } else {
                            it.items.subGenre
                        }
                    }.distinct().sortedWith(
                        compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                            .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                    )
                    _availableCuts.value = it.map {
                        if (it.items.cut.isBlank()) {
                            "(Unassigned)"
                        } else {
                            it.items.cut
                        }
                    }.distinct().sortedWith(
                        compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                            .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                    )
                    _availableComponents.value = it.flatMap {
                        if (it.components.isEmpty()) {
                            listOf("(None Assigned)")
                        } else {
                            it.components.map { it.componentName }
                        }
                    }.distinct().sortedWith(
                        compareBy<String>{ if (it == "(None Assigned)") 1 else 0 }
                            .thenBy { if (it != "(None Assigned)") it.lowercase() else "" }
                    )
                    _availableFlavors.value = it.flatMap {
                        if (it.flavoring.isEmpty()) {
                            listOf("(None Assigned)")
                        } else {
                            it.flavoring.map { it.flavoringName }
                        }
                    }.distinct().sortedWith(
                        compareBy<String>{ if (it == "(None Assigned)") 1 else 0 }
                            .thenBy { if (it != "(None Assigned)") it.lowercase() else "" }
                    )
                    _availableContainers.value = it.flatMap {
                        it.tins }.map {
                        if (it.container.isBlank()) {
                            "(Unassigned)"
                        } else {
                            it.container
                        }
                    }.distinct().sortedWith(
                        compareBy<String>{ if (it == "(Unassigned)") 1 else 0 }
                            .thenBy { if (it != "(Unassigned)") it.lowercase() else "" }
                    )
                }
            }
        }
    }


    // remove selected filters if the last item with that filter is removed or changed //
    init {
        viewModelScope.launch {
            _availableBrands.collectLatest {
                val invalidBrands = _selectedBrands.value.filter { it !in availableBrands.value }
                if (invalidBrands.isNotEmpty()) {
                    sheetSelectedBrands.value = sheetSelectedBrands.value.filter { it in availableBrands.value }
                    _selectedBrands.value = _selectedBrands.value.filter { it in availableBrands.value }
                }
            }
        }
        viewModelScope.launch {
            _availableSubgenres.collectLatest {
                val invalidSubgenres = _selectedSubgenre.value.filter { it !in availableSubgenres.value }
                if (invalidSubgenres.isNotEmpty()) {
                    sheetSelectedSubgenres.value = sheetSelectedSubgenres.value.filter { it in availableSubgenres.value }
                    _selectedSubgenre.value = _selectedSubgenre.value.filter { it in availableSubgenres.value }
                }
            }
        }
        viewModelScope.launch {
            _availableCuts.collectLatest {
                val invalidCuts = sheetSelectedCuts.value.filter { it !in availableCuts.value }
                if (invalidCuts.isNotEmpty()) {
                    sheetSelectedCuts.value = _selectedCut.value.filter { it in availableCuts.value }
                    _selectedCut.value = _selectedCut.value.filter { it in availableCuts.value }
                }
            }
        }
        viewModelScope.launch {
            _availableComponents.collectLatest {
                val invalidComponents = _selectedComponent.value.filter { it !in availableComponents.value }
                if (invalidComponents.isNotEmpty()) {
                    sheetSelectedComponents.value = sheetSelectedComponents.value.filter { it in availableComponents.value }
                    _selectedComponent.value = _selectedComponent.value.filter { it in availableComponents.value }
                }
            }
        }
        viewModelScope.launch {
            _availableFlavors.collectLatest {
                val invalidFlavors = _selectedFlavoring.value.filter { it !in availableFlavors.value }
                if (invalidFlavors.isNotEmpty()) {
                    sheetSelectedFlavorings.value = sheetSelectedFlavorings.value.filter { it in availableFlavors.value }
                    _selectedFlavoring.value = _selectedFlavoring.value.filter { it in availableFlavors.value }
                }
            }
        }
        viewModelScope.launch {
            _availableContainers.collectLatest {
                val invalidContainers =
                    sheetSelectedContainer.value.filter { it !in availableContainers.value }
                if (invalidContainers.isNotEmpty()) {
                    sheetSelectedContainer.value = sheetSelectedContainer.value.filter { it in availableContainers.value }
                    _selectedContainer.value = _selectedContainer.value.filter { it in availableContainers.value }
                }
            }
        }
    }

    fun overflowCheck(selected: List<String>, available: List<String>, shown: Int): Boolean {
        val overflowedItems = available.drop(shown)
        return selected.any { overflowedItems.contains(it) }
    }


    // filter selection update functions //
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
            _selectedComponent.value += component
        } else {
            sheetSelectedComponents.value -= component
            _selectedComponent.value -= component
        }

        _shouldScrollUp.value = true
    }

    fun updateCompMatching(option: String) {
        sheetSelectedComponentMatching.value = option
        _compMatching.value = option

        if (option == "All" || option == "Only") {
            if (sheetSelectedComponents.value.contains("(None Assigned)")) {
                sheetSelectedComponents.value -= "(None Assigned)"
                _selectedComponent.value -= "(None Assigned)"
            }
        }
    }

    fun updateSelectedFlavoring(flavoring: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedFlavorings.value += flavoring
            _selectedFlavoring.value += flavoring
        } else {
            sheetSelectedFlavorings.value -= flavoring
            _selectedFlavoring.value -= flavoring
        }
        _shouldScrollUp.value = true
    }

    fun updateFlavorMatching(option: String) {
        sheetSelectedFlavoringMatching.value = option
        _flavorMatching.value = option

        if (option == "All" || option == "Only") {
            if (sheetSelectedFlavorings.value.contains("(None Assigned)")) {
                sheetSelectedFlavorings.value -= "(None Assigned)"
                _selectedFlavoring.value -= "(None Assigned)"
            }
        }
    }

    fun updateSelectedProduction(isSelected: Boolean) {
        sheetSelectedProduction.value = isSelected
        _selectedProduction.value = isSelected

        if (isSelected) {
            sheetSelectedOutOfProduction.value = false
            _selectedOutOfProduction.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfProduction(isSelected: Boolean) {
        sheetSelectedOutOfProduction.value = isSelected
        _selectedOutOfProduction.value = isSelected

        if (isSelected) {
            sheetSelectedProduction.value = false
            _selectedProduction.value = false
        }

        _shouldScrollUp.value = true
    }

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

    fun updateSelectedUnassigned(isSelected: Boolean) {
        sheetSelectedUnassigned.value = isSelected
        _selectedUnassigned.value = isSelected

        _shouldScrollUp.value = true
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        sheetSelectedFavorites.value = isSelected
        _selectedFavorites.value = isSelected

        if (isSelected) {
            sheetSelectedDislikeds.value = false
            sheetSelectedNeutral.value = false
            sheetSelectedNonNeutral.value = false
            sheetSelectedExcludeLikes.value = false
            sheetSelectedExcludeDislikes.value = false

            _selectedDislikeds.value = false
            _selectedNeutral.value = false
            _selectedNonNeutral.value = false
            _selectedExcludeLikes.value = false
            _selectedExcludeDislikes.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeLikes(isSelected: Boolean) {
        sheetSelectedExcludeLikes.value = isSelected
        _selectedExcludeLikes.value = isSelected

        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedDislikeds.value = false
            sheetSelectedNeutral.value = false
            sheetSelectedNonNeutral.value = false
            sheetSelectedExcludeDislikes.value = false

            _selectedFavorites.value = false
            _selectedDislikeds.value = false
            _selectedNeutral.value = false
            _selectedNonNeutral.value = false
            _selectedExcludeDislikes.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        sheetSelectedDislikeds.value = isSelected
        _selectedDislikeds.value = isSelected

        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedNeutral.value = false
            sheetSelectedNonNeutral.value = false
            sheetSelectedExcludeLikes.value = false
            sheetSelectedExcludeDislikes.value = false

            _selectedFavorites.value = false
            _selectedNeutral.value = false
            _selectedNonNeutral.value = false
            _selectedExcludeLikes.value = false
            _selectedExcludeDislikes.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedExcludeDislikes(isSelected: Boolean) {
        sheetSelectedExcludeDislikes.value = isSelected
        _selectedExcludeDislikes.value = isSelected

        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedDislikeds.value = false
            sheetSelectedNeutral.value = false
            sheetSelectedNonNeutral.value = false
            sheetSelectedExcludeLikes.value = false

            _selectedFavorites.value = false
            _selectedDislikeds.value = false
            _selectedNeutral.value = false
            _selectedNonNeutral.value = false
            _selectedExcludeLikes.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedNeutral(isSelected: Boolean) {
        sheetSelectedNeutral.value = isSelected
        _selectedNeutral.value = isSelected

        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedDislikeds.value = false
            sheetSelectedNonNeutral.value = false
            sheetSelectedExcludeLikes.value = false
            sheetSelectedExcludeDislikes.value = false

            _selectedFavorites.value = false
            _selectedDislikeds.value = false
            _selectedNonNeutral.value = false
            _selectedExcludeLikes.value = false
            _selectedExcludeDislikes.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedNonNeutral(isSelected: Boolean) {
        sheetSelectedNonNeutral.value = isSelected
        _selectedNonNeutral.value = isSelected

        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedDislikeds.value = false
            sheetSelectedNeutral.value = false
            sheetSelectedExcludeLikes.value = false
            sheetSelectedExcludeDislikes.value = false

            _selectedFavorites.value = false
            _selectedDislikeds.value = false
            _selectedNeutral.value = false
            _selectedExcludeLikes.value = false
            _selectedExcludeDislikes.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedInStock(isSelected: Boolean) {
        sheetSelectedInStock.value = isSelected
        _selectedInStock.value = isSelected

        if (isSelected) {
            sheetSelectedOutOfStock.value = false
            _selectedOutOfStock.value = false
        }
        _shouldScrollUp.value = true
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        sheetSelectedOutOfStock.value = isSelected
        _selectedOutOfStock.value = isSelected

        if (isSelected) {
            sheetSelectedInStock.value = false
            _selectedInStock.value = false
        }

        _shouldScrollUp.value = true
    }


    // Tins filtering //
    fun updateSelectedHasTins(isSelected: Boolean) {
        sheetSelectedHasTins.value = isSelected
        _selectedHasTins.value = isSelected

        if (isSelected) {
            sheetSelectedNoTins.value = false
            _selectedNoTins.value = false
        }

        _shouldScrollUp.value = true
    }

    fun updateSelectedNoTins(isSelected: Boolean) {
        sheetSelectedNoTins.value = isSelected
        _selectedNoTins.value = isSelected

        if (isSelected) {
            sheetSelectedHasTins.value = false
            _selectedHasTins.value = false
        }

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
        sheetSelectedUnassigned.value = false
        sheetSelectedFavorites.value = false
        sheetSelectedDislikeds.value = false
        sheetSelectedNeutral.value = false
        sheetSelectedNonNeutral.value = false
        sheetSelectedInStock.value = false
        sheetSelectedOutOfStock.value = false
        sheetSelectedExcludeBrands.value = emptyList()
        sheetSelectedExcludeLikes.value = false
        sheetSelectedExcludeDislikes.value = false
        sheetSelectedSubgenres.value = emptyList()
        sheetSelectedCuts.value = emptyList()
        sheetSelectedComponents.value = emptyList()
        sheetSelectedComponentMatching.value = "Any"
        sheetSelectedFlavorings.value = emptyList()
        sheetSelectedFlavoringMatching.value = "Any"
        sheetSelectedProduction.value = false
        sheetSelectedOutOfProduction.value = false

        sheetSelectedHasTins.value = false
        sheetSelectedNoTins.value = false
        sheetSelectedContainer.value = emptyList()

        // filtering state //
        _selectedBrands.value = emptyList()
        _selectedTypes.value = emptyList()
        _selectedUnassigned.value = false
        _selectedFavorites.value = false
        _selectedDislikeds.value = false
        _selectedNeutral.value = false
        _selectedNonNeutral.value = false
        _selectedInStock.value = false
        _selectedOutOfStock.value = false
        _selectedExcludeBrands.value = emptyList()
        _selectedExcludeLikes.value = false
        _selectedExcludeDislikes.value = false
        _selectedSubgenre.value = emptyList()
        _selectedCut.value = emptyList()
        _selectedComponent.value = emptyList()
        _compMatching.value = "Any"
        _selectedFlavoring.value = emptyList()
        _flavorMatching.value = "Any"
        _selectedProduction.value = false
        _selectedOutOfProduction.value = false

        _selectedHasTins.value = false
        _selectedNoTins.value = false
        _selectedContainer.value = emptyList()

        _shouldScrollUp.value = true
    }

}

enum class BottomSheetState {
    OPENED, CLOSED
}