package com.sardonicus.tobaccocellar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.ui.home.SearchClearedEvent
import com.sardonicus.tobaccocellar.ui.home.SearchPerformedEvent
import com.sardonicus.tobaccocellar.ui.items.ItemSavedEvent
import com.sardonicus.tobaccocellar.ui.items.ItemUpdatedEvent
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterViewModel (
    private val itemsRepository: ItemsRepository
): ViewModel() {

    // this is just to get the ItemsRepo injection to be recognized as used and make sure it's working //
    init {
        viewModelScope.launch {
            val brands = itemsRepository.getAllBrandsStream()
        }
    }


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

    private val _blendSearchValue = MutableStateFlow("")
    val blendSearchValue: StateFlow<String> = _blendSearchValue

    fun onBlendSearch(text: String) {
        _blendSearchValue.value = text
    }

    private val _blendSearchText = MutableStateFlow("")
    val blendSearchText: StateFlow<String> = _blendSearchText

    fun updateSearchText(text: String) {
        _blendSearchText.value = text
    }

    private val _blendSearchFocused = MutableStateFlow(false)
    val blendSearchFocused: StateFlow<Boolean> = _blendSearchFocused.asStateFlow()

    fun updateSearchFocused(focused: Boolean) {
        _blendSearchFocused.value = focused
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
    val sheetSelectedComponentMatchAll = MutableStateFlow(false)
    val sheetSelectedProduction = MutableStateFlow(false)
    val sheetSelectedOutOfProduction = MutableStateFlow(false)


    // filter applied state for clear all button //
    val isFilterApplied: StateFlow<Boolean> = combine(
        sheetSelectedBrands,
        sheetSelectedTypes,
        sheetSelectedUnassigned,
        sheetSelectedFavorites,
        sheetSelectedDislikeds,
        sheetSelectedNeutral,
        sheetSelectedNonNeutral,
        sheetSelectedInStock,
        sheetSelectedOutOfStock,
        sheetSelectedExcludeBrands,
        sheetSelectedExcludeLikes,
        sheetSelectedExcludeDislikes,
        sheetSelectedSubgenres,
        sheetSelectedCuts,
        sheetSelectedComponents,
        sheetSelectedProduction,
        sheetSelectedOutOfProduction,
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
        val production = it[15] as Boolean
        val outOfProduction = it[16] as Boolean

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
            production ||
            outOfProduction
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

    fun returnScroll() {
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
                    _shouldReturn.value = false
                    _savedItemId.value = it.savedItemId.toInt()
                }
                if (it is ItemUpdatedEvent) {
                    _shouldReturn.value = true
                }
                if (it is SearchClearedEvent) {
                    _shouldReturn.value = false
                    _searchCleared.value = true
                    _searchPerformed.value = false
                }
                if (it is SearchPerformedEvent) {
                    _searchPerformed.value = true
                }
            }
        }
    }


    /** Filtering states **/
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

    private val _compMatchAll = MutableStateFlow(false)
    val compMatchAll: StateFlow<Boolean> = _compMatchAll

    private val _selectedProduction = MutableStateFlow(false)
    val selectedProduction: StateFlow<Boolean> = _selectedProduction

    private val _selectedOutOfProduction = MutableStateFlow(false)
    val selectedOutOfProduction: StateFlow<Boolean> = _selectedOutOfProduction

    // exclusionary filter states //
    private val _selectedExcludeBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedExcludeBrands: StateFlow<List<String>> = _selectedExcludeBrands

    private val _selectedExcludeLikes = MutableStateFlow(false)
    val selectedExcludeLikes: StateFlow<Boolean> = _selectedExcludeLikes

    private val _selectedExcludeDislikes = MutableStateFlow(false)
    val selectedExcludeDislikes: StateFlow<Boolean> = _selectedExcludeDislikes

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

    fun updateCompMatchAll(isSelected: Boolean) {
        sheetSelectedComponentMatchAll.value = isSelected
        _compMatchAll.value = isSelected

        if (isSelected) {
            if (sheetSelectedComponents.value.contains("(None Assigned)")) {
                sheetSelectedComponents.value -= "(None Assigned)"
                _selectedComponent.value -= "(None Assigned)"
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
        //    sheetSelectedUnassigned.value = false
            sheetSelectedTypes.value += type

        //    _selectedUnassigned.value = false
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

//        if (isSelected) {
//            sheetSelectedTypes.value = emptyList()
//            _selectedTypes.value = emptyList()
//        }
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


    fun clearAllSelectedBrands() {
        sheetSelectedBrands.value = emptyList()
        _selectedBrands.value = emptyList()

        sheetSelectedExcludeBrands.value = emptyList()
        _selectedExcludeBrands.value = emptyList()

        _shouldScrollUp.value = true
    }


    fun resetFilter() {
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
        sheetSelectedProduction.value = false
        sheetSelectedOutOfProduction.value = false

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
        _selectedProduction.value = false
        _selectedOutOfProduction.value = false

        _shouldScrollUp.value = true
    }


    // get available fields for filter //
    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands

    private val _availableSubgenres = MutableStateFlow<List<String>>(emptyList())
    val availableSubgenres: StateFlow<List<String>> = _availableSubgenres

    private val _availableCuts = MutableStateFlow<List<String>>(emptyList())
    val availableCuts: StateFlow<List<String>> = _availableCuts

    private val _availableComponents = MutableStateFlow<List<String>>(emptyList())
    val availableComponents: StateFlow<List<String>> = _availableComponents

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllBrandsStream().collectLatest { brands ->
                    _availableBrands.value = brands
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllSubgenresStream()
                    .map {
                        it.map {
                            if (it.isBlank()) {
                                "(Unassigned)"
                            } else {
                                it
                            }
                        }
                    }
                    .collectLatest { subgenres ->
                        _availableSubgenres.value = subgenres
                    }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllCutsStream()
                    .map {
                        it.map {
                            if (it.isBlank()) {
                                "(Unassigned)"
                            } else {
                                it
                            }
                        }
                    }
                    .collectLatest { cuts ->
                        _availableCuts.value = cuts
                    }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllComponentsStream()
                    .collectLatest { components ->
                        _availableComponents.value = components.map {
                            if (it.componentName.isBlank()) {
                                "(None Assigned)"
                            } else {
                                it.componentName
                            }
                        }
                    }
            }
        }
    }

}

enum class BottomSheetState {
    OPENED, CLOSED
}