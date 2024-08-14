package com.example.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    /** current item state **/
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val itemsId: Int = checkNotNull(savedStateHandle[EditEntryDestination.itemsIdArg])

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            brand.isNotBlank() && blend.isNotBlank()
        }
    }

    private fun copyOriginalDetails(itemDetails: ItemDetails) {
        itemDetails.originalBrand = "(" + itemDetails.brand + ")"
        itemDetails.originalBlend = "(" + itemDetails.blend + ")"
    }

    init {
        viewModelScope.launch {
            itemUiState = itemsRepository.getItemStream(itemsId)
                .filterNotNull()
                .first()
                .toItemUiState(true)
                .also {copyOriginalDetails(it.itemDetails)}
        }
    }

    /** autocomplete for brands**/
    private val _brands = MutableStateFlow<List<String>>(emptyList())
    private val brands: StateFlow<List<String>> = _brands


    init {
        viewModelScope.launch {
            itemsRepository.getAllBrands().collect {
                _brands.value = it
            }.also {
                _brands.value = ItemUiState().autoBrands
            }
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(
                itemDetails = itemDetails,
                isEntryValid = validateInput(itemDetails),
                autoBrands = brands.value
            )
    }

    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails)) {
            itemsRepository.updateItem(itemUiState.itemDetails.toItem())
        }
    }

    suspend fun deleteItem() {
        itemsRepository.deleteItem(itemUiState.itemDetails.toItem())
    }

}

