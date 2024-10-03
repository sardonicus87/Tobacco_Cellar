package com.example.tobaccocellar.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _selectedOutOfStock = MutableStateFlow(false)
    val selectedOutOfStock: StateFlow<Boolean> = _selectedOutOfStock


    // Filtering update functions //
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) { _selectedBrands.value += brand }
        else { _selectedBrands.value -= brand }
    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        if (isSelected) { _selectedTypes.value += type }
        else { _selectedTypes.value -= type }
    }

    fun updateSelectedFavorites(isSelected: Boolean) {
        _selectedFavorites.value = isSelected
    }

    fun updateSelectedDislikeds(isSelected: Boolean) {
        _selectedDislikeds.value = isSelected
        Log.d("FilterViewModel", "Selected dislikeds: ${_selectedDislikeds.value}")
    }

    fun updateSelectedOutOfStock(isSelected: Boolean) {
        _selectedOutOfStock.value = isSelected
    }

    // get vals //
    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands

    init {
        viewModelScope.launch {
            _availableBrands.value = itemsRepository.getAllBrandsStream().first()
        }
    }

//    val availableBrands: StateFlow<List<String>> =
//        itemsRepository.getAllBrandsStream()
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(),
//                initialValue = emptyList()
//            )

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
}

enum class BottomSheetState {
    OPENED, CLOSED
}
