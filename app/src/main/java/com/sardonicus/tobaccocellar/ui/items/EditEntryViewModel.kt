package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    /** current item state **/
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    var tinConversion = mutableStateOf(TinConversion())
        private set

    fun updateTinConversion(tinConversion: TinConversion) {
        this.tinConversion.value = tinConversion
    }

    private val itemsId: Int = checkNotNull(savedStateHandle[EditEntryDestination.itemsIdArg])

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            brand.isNotBlank() && blend.isNotBlank()
        }
    }

    private fun copyOriginalDetails(itemDetails: ItemDetails) {
        itemDetails.originalBrand = itemDetails.brand
        itemDetails.originalBlend = itemDetails.blend
    }

    init {
        viewModelScope.launch {
            itemUiState = itemsRepository.getItemStream(itemsId)
                .filterNotNull()
                .first()
                .toItemUiState(true)
                .also { copyOriginalDetails(it.itemDetails) }
        }
    }

    /** autocomplete for brands**/
    private val _brands = MutableStateFlow<List<String>>(emptyList())
    private val brands: StateFlow<List<String>> = _brands


    init {
        viewModelScope.launch {
            itemsRepository.getAllBrandsStream().collect {
                _brands.value = it
            }
        }
    }


    /** get tin conversion rates from preferences **/
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

    suspend fun checkItemExistsOnUpdate() {
        val currentItem = itemUiState.itemDetails

        if (currentItem.brand == itemUiState.itemDetails.originalBrand && currentItem.blend == itemUiState.itemDetails.originalBlend) {
            existState =
                ExistState(
                    exists = false,
                    transferId = 0,
                    existCheck = false,
                )
        } else {
            if (itemsRepository.exists(currentItem.brand, currentItem.blend)) {
                existState =
                    ExistState(
                        exists = true,
                        transferId = 0,
                        existCheck = true
                    )
            } else if (!itemsRepository.exists(currentItem.brand, currentItem.blend)) {
                existState =
                    ExistState(
                        exists = false,
                        transferId = 0,
                        existCheck = false,
                    )
            }
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

