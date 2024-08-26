package com.example.tobaccocellar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FilterViewModel (
    private val itemsRepository: ItemsRepository
): ViewModel() {



    /** Filtering states **/
    private val _selectedBrands = mutableStateListOf<String>()
    val selectedBrands: List<String> = _selectedBrands

    private val _selectedTypes = mutableStateListOf<String>()
    val selectedTypes: List<String> = _selectedTypes

    private val _selectedFavorites = mutableStateListOf<String>()
    val selectedFavorites: List<String> = _selectedFavorites

    private val _selectedDislikeds = mutableStateListOf<String>()
    val selectedDislikeds: List<String> = _selectedDislikeds

//    private val _selectedOutOfStock = mutableStateListOf<String>()
//    val selectedOutOfStock: List<String> = _selectedOutOfStock

    /** Sorting states **/
    private val _sortByDefault = mutableStateListOf<String>()
    val sortByDefault: List<String> = _sortByDefault

    private val _sortByBrand = mutableStateListOf<String>()
    val sortByBrand: List<String> = _sortByBrand

    private val _sortByType = mutableStateListOf<String>()
    val sortByType: List<String> = _sortByType

    /** Filtering state flows **/
    val itemsFilter: StateFlow<ItemsFilter> = combine(
        itemsRepository.getItemsByBrand(brand = ""),
        itemsRepository.getItemsByType(type = ""),
        itemsRepository.getItemsByFavorite(),
        itemsRepository.getItemsByDisliked(),
//        itemsRepository.getItemsByOutOfStock()
    ) { brand, type, favorite, disliked -> ItemsFilter( brand, type, favorite, disliked) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ItemsFilter(
                brand = emptyList(),
                type = emptyList(),
                favorite = emptyList(),
                disliked = emptyList())
        )

//    val brands: StateFlow<List<ItemsFilter>> = itemsRepository.getItemsByBrand(brand = "").map { list ->
//        list.map { ItemsFilter(
//            it.brand,
//            type = "",
//            favorite = false,
//            disliked = false,
//            outOfStock = "")
//        }
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(),
//        initialValue = emptyList()
//    )
//
//    val types: StateFlow<List<ItemsFilter>> = itemsRepository.getItemsByType(type = "").map { list ->
//        list.map { ItemsFilter(brand = "", it.type,
//            favorite = false,
//            disliked = false,
//            outOfStock = ""
//        )}
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(),
//        initialValue = emptyList()
//    )
//
//    val favorites: StateFlow<List<ItemsFilter>> = itemsRepository.getItemsByFavorite().map { list ->
//        list.map { ItemsFilter("", "", it.favorite, "", "")}
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(),
//        initialValue = emptyList()
//    )
//
//    val dislikeds: StateFlow<List<ItemsFilter>> = itemsRepository.getItemsByDisliked().map { list ->
//        list.map { ItemsFilter("", "", "", it.disliked, "")}
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(),
//        initialValue = emptyList()
//    )

//    val outOfStock: StateFlow<List<ItemsFilter>> = itemsRepository.getItemsByOutOfStock().map { list ->
//        list.map { ItemsFilter(it.outOfStock)}
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(),
//        initialValue = emptyList()
//    )

    /** Setting filtering states **/
    fun updateSelectedBrands(brand: String, isSelected: Boolean) {
        if (isSelected) {
            _selectedBrands.add(brand)
        } else {
            _selectedBrands.remove(brand)
        }
    }

    fun updateSelectedTypes(type: String, isSelected: Boolean) {
        if (isSelected) {
            _selectedTypes.add(type)
        } else {
            _selectedTypes.remove(type)
        }
    }

    fun updateSelectedFavorites(favorite: String, isSelected: Boolean) {
        if (isSelected) {
            _selectedFavorites.add(favorite)
        } else {
            _selectedFavorites.remove(favorite)
        }
    }

    fun updateSelectedDislikeds(disliked: String, isSelected: Boolean) {
        if (isSelected) {
            _selectedDislikeds.add(disliked)
        } else {
            _selectedDislikeds.remove(disliked)
        }
    }

//    fun updateSelectedOutOfStock(outOfStock: String, isSelected: Boolean) {
//        if (isSelected) {
//            _selectedOutOfStock.add(outOfStock)
//        } else {
//            _selectedOutOfStock.remove(outOfStock)
//        }
//    }

    /** Setting sorting states **/
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

}

data class ItemsFilter(
    val brand: List<Items>,
    val type: List<Items>,
    val favorite: List<Items>,
    val disliked: List<Items>,
//    val outOfStock: String
)