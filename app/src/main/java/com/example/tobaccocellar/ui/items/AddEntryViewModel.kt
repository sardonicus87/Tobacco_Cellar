package com.example.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository


class AddEntryViewModel(
    private val itemsRepository: ItemsRepository,
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

    /** check if Item already exists, display optional dialog if so **/
    var existState by mutableStateOf(ExistState())

    suspend fun checkItemExistsOnSave() {
        val currentItem = itemUiState.itemDetails
        if (itemsRepository.exists(currentItem.brand, currentItem.blend)) {
            existState =
                ExistState(
                    exists = true,
                    transferId = itemsRepository.getItemIdByIndex(
                        currentItem.brand, currentItem.blend),
                    existCheck = true
                )
        }
        else if (!itemsRepository.exists(currentItem.brand, currentItem.blend)) {
            existState =
                ExistState(
                    exists = false,
                    transferId = 0,
                    existCheck = false,
                )
        }
    }

    fun resetExistState() {
        existState =
            ExistState(
                exists = false,
                transferId = 0,
                existCheck = false,
            )
    }


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


data class ExistState(
    val exists: Boolean = false,
    val transferId: Int = 0,
    var existCheck: Boolean = false,
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
    val squantity: String = "",
    val notes: String = "",
)


/** convert ItemDetails (state/class) to Items (Database Table entity) **/
fun ItemDetails.toItem(): Items = Items(
    id = id,
    brand = brand,
    blend = blend,
    type = type,
    quantity = quantity,
    hated = hated,
    favorite = favorite,
    notes = notes,
)

/** convert Items (Database Table) to ItemUiState**/
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
    squantity = quantity.toString(),
    notes = notes
)