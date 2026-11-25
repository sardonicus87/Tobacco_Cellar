package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.toCleanString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEntryViewModel(
    filterViewModel: FilterViewModel,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
): ViewModel() {

    /** current item state **/
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    var tabErrorState by mutableStateOf(TabErrorState())
        private set

    val autoCompleteData = filterViewModel.autoComplete.value
    val tinConversion = mutableStateOf(TinConversion())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            launch {
                combine(
                    preferencesRepo.tinOzConversionRate,
                    preferencesRepo.tinGramsConversionRate,
                    preferencesRepo.defaultSyncOption
                ) { ozRate, gramsRate, defaultSync -> Triple(ozRate, gramsRate, defaultSync) }
                    .first().let { (ozRate, gramsRate, defaultSync) ->
                        tinConversion.value = TinConversion(ozRate = ozRate, gramsRate = gramsRate)
                        if (defaultSync) updateUiState(itemUiState.itemDetails.copy(syncTins = true))
                    }
            }
            launch {
                snapshotFlow { itemUiState.itemDetails.quantityString.toCleanString() }
                    .collectLatest {
                        val quantity =
                            if (!itemUiState.itemDetails.syncTins) it.toIntOrNull() ?: 1
                            else itemUiState.itemDetails.quantity
                        updateUiState(itemUiState.itemDetails.copy(quantity = quantity))
                    }
            }
            launch {
                combine(
                    snapshotFlow { itemUiState.itemDetails.brand.toCleanString() },
                    snapshotFlow { itemUiState.itemDetails.blend.toCleanString() }
                ) { itemUiState.itemDetails }.collectLatest { updateUiState(itemUiState.itemDetails) }
            }
        }
    }

    /** update item state **/
    fun updateUiState(itemDetails: ItemDetails) {
        val updatedDetails = if (itemDetails.syncTins) {
            val syncedTins = calculateSyncTinsDetails(itemDetails.tinDetailsList, tinConversion.value)
            itemUiState.itemDetails.quantityString.edit { replace(0, length, "$syncedTins") }
            itemDetails.copy(quantity = syncedTins)
        } else { itemDetails }

        itemUiState = ItemUiState(
            itemDetails = updatedDetails,
            isEntryValid = validateInput(updatedDetails, updatedDetails.tinDetailsList) { tabErrorState = it }
        )
    }

    fun updateTinDetails(tinDetails: TinDetails) {
        val newList = itemUiState.itemDetails.tinDetailsList.map {
            if (it.tempTinId == tinDetails.tempTinId) {
                tinDetails.copy(labelIsValid = isTinLabelValid(itemUiState.itemDetails.tinDetailsList, tinDetails.tinLabel.toCleanString(), tinDetails.tempTinId))
            } else {
                it.copy(labelIsValid = isTinLabelValid(itemUiState.itemDetails.tinDetailsList, it.tinLabel.toCleanString(), it.tempTinId))
            }
        }
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = newList))
    }


    /** add/remove tins **/
    fun addTin() {
        val current = itemUiState.itemDetails.tinDetailsList
        val newTempId = if (current.isEmpty()) 1 else current.maxOf { it.tempTinId } + 1
        val newTin = TinDetails(tempTinId = newTempId, tinLabel = TextFieldState(""))
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = current + newTin))
    }

    fun removeTin(index: Int) {
        val newList = itemUiState.itemDetails.tinDetailsList.toMutableList().apply { removeAt(index) }
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = newList))
    }


    /** check if Item already exists, display optional dialog if so **/
    val existState = mutableStateOf(ExistState())

    suspend fun checkItemExistsOnSave() {
        val transferId = itemsRepository.getItemIdByIndex(itemUiState.itemDetails.brand.toCleanString(), itemUiState.itemDetails.blend.toCleanString())
        existState.value =
            ExistState(
                exists = transferId != null,
                transferId = transferId ?: 0
            )
    }

    fun resetExistState() {
        existState.value =
            ExistState(
                exists = false,
                transferId = 0
            )
    }


    /** save to database **/
    suspend fun saveItem() {
        if (validateInput(itemUiState.itemDetails, itemUiState.itemDetails.tinDetailsList) { }) {
            SyncStateManager.schedulingPaused = true

            val components = itemUiState.itemDetails.componentString.toCleanString().toComponents(autoCompleteData.components)
            val flavoring = itemUiState.itemDetails.flavoringString.toCleanString().toFlavoring(autoCompleteData.flavorings)
            val lastModified = System.currentTimeMillis()
            val itemDetails = itemUiState.itemDetails.copy(lastModified = lastModified)

            val savedItemId = itemsRepository.insertItem(itemDetails.toItem())

            components.forEach {
                val componentId = itemsRepository.getComponentIdByName(it.componentName) ?: itemsRepository.insertComponent(it).toInt()
                itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = savedItemId.toInt(), componentId = componentId))
            }
            flavoring.forEach {
                val flavoringId = itemsRepository.getFlavoringIdByName(it.flavoringName) ?: itemsRepository.insertFlavoring(it).toInt()
                itemsRepository.insertFlavoringCrossRef(ItemsFlavoringCrossRef(itemId = savedItemId.toInt(), flavoringId = flavoringId))
            }

            itemUiState.itemDetails.tinDetailsList.forEach {
                val tin = it.copy(lastModified = lastModified).toTin(savedItemId.toInt())
                itemsRepository.insertTin(tin)
            }

            SyncStateManager.schedulingPaused = false
            itemsRepository.triggerUploadWorker()

            EventBus.emit(ItemSavedEvent(savedItemId))
        }
    }

}

data class ItemSavedEvent(val savedItemId: Long)