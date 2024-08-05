package com.example.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.launch

class AddEntryViewModel(
    private val itemsRepository: ItemsRepository
): ViewModel() {

    /** current item state **/
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    /** update item state **/
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(
                itemDetails = itemDetails,
                isEntryValid = validateInput(itemDetails),
            )
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            brand.isNotBlank() && blend.isNotBlank()
        }
    }

    /** check if Item already exists, display snackbar if so **/
    private var _snackbarError = mutableStateOf("")
    val snackbarError: State<String> = _snackbarError

    fun checkItemExistsOnSave() {
        viewModelScope.launch {
            val currentItem = itemUiState.itemDetails
            if (itemsRepository.exists(currentItem.brand, currentItem.blend)) {
                _snackbarError.value = "Item already exists"
            } else {
                saveItem()
            }
        }
    }

    /** save to or delete from database **/
    private suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }

    suspend fun deleteItem() {
        if (validateInput()) {
            itemsRepository.deleteItem(itemUiState.itemDetails.toItem())
        }
    }

    fun clearSnackbarError() {
        _snackbarError.value = ""
    }
}

data class ItemExistsState(
    val exists: Boolean = false
)

data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
)

data class ItemDetails(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val quantity: Int = 1,
    val hated: Boolean = false,
    val favorite: Boolean = false,
    val squantity: String = ""
)


/** convert ItemDetails (class) to Items (Database Table) **/
fun ItemDetails.toItem(): Items = Items(
    id = id,
    brand = brand,
    blend = blend,
    type = type,
    quantity = quantity,
    hated = hated,
    favorite = favorite
)

/** convert Items to ItemUiState**/
fun Items.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid
)

/** convert Items (Database Table) to ItemDetails (class) **/
fun Items.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    brand = brand,
    blend = blend,
    type = type,
    quantity = quantity,
    hated = hated,
    favorite = favorite,
    squantity = quantity.toString()
)