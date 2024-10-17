package com.example.tobaccocellar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _selectedNeutral = MutableStateFlow(false)
    val selectedNeutral: StateFlow<Boolean> = _selectedNeutral

    private val _selectedInStock = MutableStateFlow(false)
    val selectedInStock: StateFlow<Boolean> = _selectedInStock

    private val _selectedOutOfStock = MutableStateFlow(false)
    val selectedOutOfStock: StateFlow<Boolean> = _selectedOutOfStock


    // Filtering update functions //
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) { _selectedBrands.value += brand }
        else { _selectedBrands.value -= brand }
    }

    fun clearAllSelectedBrands() {
        _selectedBrands.value = emptyList()
    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        if (isSelected) { _selectedTypes.value += type }
        else { _selectedTypes.value -= type }
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        _selectedFavorites.value = isSelected
        if (isSelected) {
            _selectedDislikeds.value = false
            _selectedNeutral.value = false
        }
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        _selectedDislikeds.value = isSelected
        if (isSelected) {
            _selectedFavorites.value = false
            _selectedNeutral.value = false
        }
    }

    fun updateSelectedNeutral(isSelected: Boolean) {
        _selectedNeutral.value = isSelected
        if (isSelected) {
            _selectedFavorites.value = false
            _selectedDislikeds.value = false
        }
    }

    fun updateSelectedInStock(isSelected: Boolean) {
        _selectedInStock.value = isSelected
        if (isSelected) {
            _selectedOutOfStock.value = false
        }
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        _selectedOutOfStock.value = isSelected
        if (isSelected) {
            _selectedInStock.value = false
        }
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
