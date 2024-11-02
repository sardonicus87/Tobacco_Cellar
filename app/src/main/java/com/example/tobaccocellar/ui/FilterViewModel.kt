package com.example.tobaccocellar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

// @OptIn(FlowPreview::class)
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


    /** Filtering **/
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

    val isFilterApplied: StateFlow<Boolean> = combine(
        sheetSelectedBrands,
        sheetSelectedTypes,
        sheetSelectedUnassigned,
        sheetSelectedFavorites,
        sheetSelectedDislikeds,
        sheetSelectedNeutral,
        sheetSelectedNonNeutral,
        sheetSelectedInStock,
        sheetSelectedOutOfStock
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

        brands.isNotEmpty() ||
            types.isNotEmpty() ||
            unassigned ||
            favorites ||
            dislikeds ||
            neutral ||
            nonNeutral ||
            inStock ||
            outOfStock
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )


    // Filter states //
    private val filterUpdateEvents = MutableSharedFlow<FilterUpdateEvent>()
    object FilterUpdateEvent
    private var inactivityTimerJob: Job? = null

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


    // filter selection update functions //
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedBrands.value += brand
        } else {
            sheetSelectedBrands.value -= brand
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun clearAllSelectedBrands() {
        sheetSelectedBrands.value = emptyList()
        _selectedBrands.value = emptyList()
    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        if (isSelected) {
            sheetSelectedUnassigned.value = false
            sheetSelectedTypes.value += type
        } else {
            sheetSelectedTypes.value -= type
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun updateSelectedUnassigned(isSelected: Boolean) {
        sheetSelectedUnassigned.value = isSelected
        if (isSelected) {
            sheetSelectedTypes.value = emptyList()
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        sheetSelectedFavorites.value = isSelected
        if (isSelected) {
            sheetSelectedDislikeds.value = false
            sheetSelectedNeutral.value = false
            sheetSelectedNonNeutral.value = false
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        sheetSelectedDislikeds.value = isSelected
        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedNeutral.value = false
            sheetSelectedNonNeutral.value = false
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun updateSelectedNeutral(isSelected: Boolean) {
        sheetSelectedNeutral.value = isSelected
        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedDislikeds.value = false
            sheetSelectedNonNeutral.value = false
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun updateSelectedNonNeutral(isSelected: Boolean) {
        sheetSelectedNonNeutral.value = isSelected
        if (isSelected) {
            sheetSelectedFavorites.value = false
            sheetSelectedDislikeds.value = false
            sheetSelectedNeutral.value = false
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun updateSelectedInStock(isSelected: Boolean) {
        sheetSelectedInStock.value = isSelected
        if (isSelected) {
            sheetSelectedOutOfStock.value = false
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        sheetSelectedOutOfStock.value = isSelected
        if (isSelected) {
            sheetSelectedInStock.value = false
        }
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_DURATION)
            filterUpdateEvents.emit(FilterUpdateEvent)
        }
    }

    init {
        viewModelScope.launch {
            filterUpdateEvents.collect {
                withTimeoutOrNull(INACTIVITY_DURATION) {
                    _selectedBrands.value = sheetSelectedBrands.value
                    _selectedTypes.value = sheetSelectedTypes.value
                    _selectedUnassigned.value = sheetSelectedUnassigned.value
                    _selectedFavorites.value = sheetSelectedFavorites.value
                    _selectedDislikeds.value = sheetSelectedDislikeds.value
                    _selectedNeutral.value = sheetSelectedNeutral.value
                    _selectedNonNeutral.value = sheetSelectedNonNeutral.value
                    _selectedInStock.value = sheetSelectedInStock.value
                    _selectedOutOfStock.value = sheetSelectedOutOfStock.value
                }
            }
        }
    }

    companion object {
        private const val INACTIVITY_DURATION = 600L
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

        _selectedBrands.value = emptyList()
        _selectedTypes.value = emptyList()
        _selectedUnassigned.value = false
        _selectedFavorites.value = false
        _selectedDislikeds.value = false
        _selectedNeutral.value = false
        _selectedNonNeutral.value = false
        _selectedInStock.value = false
        _selectedOutOfStock.value = false
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