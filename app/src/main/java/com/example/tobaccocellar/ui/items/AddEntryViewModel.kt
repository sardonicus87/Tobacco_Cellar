package com.example.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            brand.isNotBlank() && blend.isNotBlank()
        }
    }

    /** check if Item already exists **/



    /** save to or delete from database **/
    suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }
    suspend fun deleteItem() {
        if (validateInput()) {
            itemsRepository.deleteItem(itemUiState.itemDetails.toItem())
        }
    }
}


data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
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