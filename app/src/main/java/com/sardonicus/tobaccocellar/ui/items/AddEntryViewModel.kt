package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddEntryViewModel(
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
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
                autoBrands = brands.value
            )
    }


    /** tin conversion **/
    var tinConversion = mutableStateOf(TinConversion())
        private set

    fun updateTinConversion(tinConversion: TinConversion) {
        this.tinConversion.value = tinConversion
            .copy(isConversionValid = validateConversion(tinConversion))
    }

    private fun validateConversion(tinConversion: TinConversion = this.tinConversion.value): Boolean {
        return with(tinConversion) {
            amount.isNotBlank() && unit.isNotBlank()
        }
    }

    init {
        viewModelScope.launch {
            tinConversion.value = TinConversion(
                ozRate = preferencesRepo.getTinOzConversionRate(),
                gramsRate = preferencesRepo.getTinGramsConversionRate(),
            )
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

    /** autocomplete for brands**/
    private val _brands = MutableStateFlow<List<String>>(emptyList())
        private val brands: StateFlow<List<String>> = _brands

    init {
        viewModelScope.launch {
            itemsRepository.getAllBrandsStream().collect {
                _brands.value = it
            }.also {
                _brands.value = ItemUiState().autoBrands
            }
        }
    }

    /** save to or delete from database **/
    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            brand.isNotBlank() && blend.isNotBlank()
        }
    }

    suspend fun saveItem() {
        if (validateInput()) {
            val newItemId = itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        //    itemsRepository.insertItem(itemUiState.itemDetails.toItem())
            EventBus.emit(ItemAddedEvent(newItemId))
        }
    }

    suspend fun deleteItem() {
        itemsRepository.deleteItem(itemUiState.itemDetails.toItem())
    }

}

/** Exist check state **/
data class ExistState(
    val exists: Boolean = false,
    val transferId: Int = 0,
    var existCheck: Boolean = false,
)

data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
    val autoBrands: List<String> = listOf(),
)

data class ItemDetails(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val quantity: Int = 1,
    var disliked: Boolean = false,
    var favorite: Boolean = false,
    val squantity: String = "",
    val notes: String = "",
    var originalBrand: String = "",
    var originalBlend: String = "",
)

data class TinConversion(
    val amount: String = "",
    val unit: String = "",
    val ozRate: Double = 1.75,
    val gramsRate: Double = 50.0,
    val isConversionValid: Boolean = false,
)

data class ItemAddedEvent(
    val newItemId: Long
)

/** convert ItemDetails (class) to Items (Database Table entity) **/
fun ItemDetails.toItem(): Items = Items(
    id = id,
    brand = brand,
    blend = blend,
    type = type,
    quantity = quantity,
    disliked = disliked,
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
    disliked = disliked,
    favorite = favorite,
    squantity = quantity.toString(),
    notes = notes
)