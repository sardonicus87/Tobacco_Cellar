package com.example.tobaccocellar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val isBottomSheetOpen: Boolean
        get() = _bottomSheetState.value == BottomSheetState.OPENED

    fun openBottomSheet() {
        _bottomSheetState.value = BottomSheetState.OPENED
    }

    fun closeBottomSheet() {
        _bottomSheetState.value = BottomSheetState.CLOSED
    }


    /** HomeScreen HomeHeader blend search **/
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
    val sheetSelectedExcludeSwitch = MutableStateFlow(false)
    val sheetSelectedExcludeLikes = MutableStateFlow(false)
    val sheetSelectedExcludeDislikes = MutableStateFlow(false)


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
        sheetSelectedExcludeDislikes
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
            excludeDislikes
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _shouldScrollUp = MutableStateFlow(false)
    val shouldScrollUp: StateFlow<Boolean> = _shouldScrollUp

    fun resetScroll() {
        _shouldScrollUp.value = false
    }


    /** Filtering states **/
//    private val filterUpdateEvents = MutableSharedFlow<FilterUpdateEvent>()
//    object FilterUpdateEvent
//    private var inactivityTimerJob: Job? = null

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

    // exclusionary filter states //
    private val _selectedExcludeBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedExcludeBrands: StateFlow<List<String>> = _selectedExcludeBrands

    private val _selectedExcludeLikes = MutableStateFlow(false)
    val selectedExcludeLikes: StateFlow<Boolean> = _selectedExcludeLikes

    private val _selectedExcludeDislikes = MutableStateFlow(false)
    val selectedExcludeDislikes: StateFlow<Boolean> = _selectedExcludeDislikes


    // filter selection update functions //
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        _shouldScrollUp.value = true

        if (isSelected) {
            sheetSelectedBrands.value += brand
            _selectedBrands.value += brand
        } else {
            sheetSelectedBrands.value -= brand
            _selectedBrands.value -= brand
        }
//        inactivityTimerJob?.cancel()
//        inactivityTimerJob = viewModelScope.launch {
//            delay(INACTIVITY_DURATION)
//            filterUpdateEvents.emit(FilterUpdateEvent)
//        }
    }

    fun updateSelectedExcludedBrands(brand: String, isSelected: Boolean) {
        _shouldScrollUp.value = true

        if (isSelected) {
            sheetSelectedExcludeBrands.value += brand
            _selectedExcludeBrands.value += brand
        } else {
            sheetSelectedExcludeBrands.value -= brand
            _selectedExcludeBrands.value -= brand
        }
    }

    fun updateSelectedExcludeSwitch(isSelected: Boolean) {
        sheetSelectedExcludeSwitch.value = isSelected

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

    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        _shouldScrollUp.value = true

        if (isSelected) {
            sheetSelectedUnassigned.value = false
            sheetSelectedTypes.value += type

            _selectedUnassigned.value = false
            _selectedTypes.value += type
        } else {
            sheetSelectedTypes.value -= type
            _selectedTypes.value -= type
        }
    }

    fun updateSelectedUnassigned(isSelected: Boolean) {
        sheetSelectedUnassigned.value = isSelected
        _selectedUnassigned.value = isSelected

        _shouldScrollUp.value = true

        if (isSelected) {
            sheetSelectedTypes.value = emptyList()
            _selectedTypes.value = emptyList()
        }
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        sheetSelectedFavorites.value = isSelected
        _selectedFavorites.value = isSelected

        _shouldScrollUp.value = true

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
    }

    fun updateSelectedExcludeLikes(isSelected: Boolean) {
        sheetSelectedExcludeLikes.value = isSelected
        _selectedExcludeLikes.value = isSelected

        _shouldScrollUp.value = true

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
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        sheetSelectedDislikeds.value = isSelected
        _selectedDislikeds.value = isSelected

        _shouldScrollUp.value = true

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
    }

    fun updateSelectedExcludeDislikes(isSelected: Boolean) {
        sheetSelectedExcludeDislikes.value = isSelected
        _selectedExcludeDislikes.value = isSelected

        _shouldScrollUp.value = true

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
    }

    fun updateSelectedNeutral(isSelected: Boolean) {
        sheetSelectedNeutral.value = isSelected
        _selectedNeutral.value = isSelected

        _shouldScrollUp.value = true

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
    }

    fun updateSelectedNonNeutral(isSelected: Boolean) {
        sheetSelectedNonNeutral.value = isSelected
        _selectedNonNeutral.value = isSelected

        _shouldScrollUp.value = true

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
    }

    fun updateSelectedInStock(isSelected: Boolean) {
        sheetSelectedInStock.value = isSelected
        _selectedInStock.value = isSelected

        _shouldScrollUp.value = true

        if (isSelected) {
            sheetSelectedOutOfStock.value = false
            _selectedOutOfStock.value = false
        }
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        sheetSelectedOutOfStock.value = isSelected
        _selectedOutOfStock.value = isSelected

        _shouldScrollUp.value = true

        if (isSelected) {
            sheetSelectedInStock.value = false
            _selectedInStock.value = false
        }
    }


//    init {
//        viewModelScope.launch {
//            filterUpdateEvents.collect {
//                withTimeoutOrNull(INACTIVITY_DURATION) {
//                    _selectedBrands.value = sheetSelectedBrands.value
//                    _selectedTypes.value = sheetSelectedTypes.value
//                    _selectedUnassigned.value = sheetSelectedUnassigned.value
//                    _selectedFavorites.value = sheetSelectedFavorites.value
//                    _selectedDislikeds.value = sheetSelectedDislikeds.value
//                    _selectedNeutral.value = sheetSelectedNeutral.value
//                    _selectedNonNeutral.value = sheetSelectedNonNeutral.value
//                    _selectedInStock.value = sheetSelectedInStock.value
//                    _selectedOutOfStock.value = sheetSelectedOutOfStock.value
//                }
//            }
//        }
//    }
//
//    companion object {
//        private const val INACTIVITY_DURATION = 150L
//    }

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

        _shouldScrollUp.value = true
    }


    // get vals //
    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands

    init {
        viewModelScope.launch {
            _availableBrands.value = itemsRepository.getAllBrandsStream().first()
        }
    }
}

enum class BottomSheetState {
    OPENED, CLOSED
}