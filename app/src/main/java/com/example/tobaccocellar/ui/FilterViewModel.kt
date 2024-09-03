package com.example.tobaccocellar.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        Log.d("FilterViewModel", "ViewModel initialized")
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


    /** Filtering **/
    // Filter states //
    private val _selectedBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedBrands: StateFlow<List<String>> = _selectedBrands

    private val _selectedTypes = MutableStateFlow<List<String>>(emptyList())
    val selectedTypes: StateFlow<List<String>> = _selectedTypes

    private val _selectedFavorites = MutableStateFlow(false)
    val selectedFavorites: StateFlow<Boolean> = _selectedFavorites

    private val _selectedDislikeds = MutableStateFlow(false)
    val selectedDislikeds: StateFlow<Boolean> = _selectedDislikeds

    private val _selectedOutOfStock = MutableStateFlow(false)
    val selectedOutOfStock: StateFlow<Boolean> = _selectedOutOfStock

    init {
        Log.d("FilterViewModel", "Initial selectedBrands: ${_selectedBrands.value}")
        Log.d("FilterViewModel", "Initial selectedTypes: ${_selectedTypes.value}")
        Log.d("FilterViewModel", "Initial selectedFavorites: ${_selectedFavorites.value}")
        Log.d("FilterViewModel", "Initial selectedDislikeds: ${_selectedDislikeds.value}")
        Log.d("FilterViewModel", "Initial selectedOutOfStock: ${_selectedOutOfStock.value}")
    }

    // Filtering update functions //
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) { _selectedBrands.value += brand }
        else { _selectedBrands.value -= brand }
        Log.d("FilterViewModel", "Selected brands: ${_selectedBrands.value}")
    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        if (isSelected) { _selectedTypes.value += type }
        else { _selectedTypes.value -= type }
        Log.d("FilterViewModel", "Selected types: ${_selectedTypes.value}")
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        _selectedFavorites.value = isSelected
        Log.d("FilterViewModel", "Selected favorites: ${_selectedFavorites.value}")
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        _selectedDislikeds.value = isSelected
        Log.d("FilterViewModel", "Selected dislikeds: ${_selectedDislikeds.value}")
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        _selectedOutOfStock.value = isSelected
        Log.d("FilterViewModel", "Selected out of stock: ${_selectedOutOfStock.value}")
    }

    // get vals //
    val availableBrands: StateFlow<List<String>> =
        itemsRepository.getAllBrandsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    val availableTypes: StateFlow<List<String>> =
        itemsRepository.getAllTypesStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    val availableFavorites: StateFlow<List<Boolean>> =
        itemsRepository.getAllFavoritesStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    val availableDislikeds: StateFlow<List<Boolean>> =
        itemsRepository.getAllDislikeStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    val availableOutOfStock: StateFlow<List<Boolean>> =
        itemsRepository.getAllZeroQuantityStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )





    /** Sorting states **/
    private val _sortByDefault = mutableStateListOf<String>()
    val sortByDefault: List<String> = _sortByDefault

    private val _sortByBrand = mutableStateListOf<String>()
    val sortByBrand: List<String> = _sortByBrand

    private val _sortByType = mutableStateListOf<String>()
    val sortByType: List<String> = _sortByType


    fun updateSortByDefault(default: String, isSelected: Boolean) {
        if (isSelected) {
            _sortByDefault.add(default)
        } else {
            _sortByDefault.remove(default)
        }
    }

    fun updateSortByBrand(brand: String, isSelected: Boolean) {
        if (isSelected) {
            _sortByBrand.add(brand)
        } else {
            _sortByBrand.remove(brand)
        }
    }

    fun updateSortByType(type: String, isSelected: Boolean) {
        if (isSelected) {
            _sortByType.add(type)
        } else {
            _sortByType.remove(type)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("FilterViewModel", "ViewModel cleared")
    }
}

enum class BottomSheetState {
    OPENED, CLOSED
}

data class SortOrderOption(
    val option: SortType,
    val ascending: Boolean
)
    enum class SortType {
        DEFAULT, BRAND, TYPE, QUANTITY
    }