package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()
    private val _currentLeftTab = MutableStateFlow(0)
    val currentLeftTab = _currentLeftTab.asStateFlow()

    fun updateSelectedTab(index: Int) {
        _selectedTabIndex.value = index
        if (index < 2) _currentLeftTab.value = index

        updateUiState(itemUiState.itemDetails)
    }

    init {
        viewModelScope.launch {
            combine(
                preferencesRepo.tinOzConversionRate,
                preferencesRepo.tinGramsConversionRate,
                preferencesRepo.defaultSyncOption
            ) { ozRate, gramsRate, defaultSync ->
                Triple(ozRate, gramsRate, defaultSync)
            }.first().let { (ozRate, gramsRate, defaultSync) ->
                tinConversion.value = TinConversion(
                    ozRate = ozRate,
                    gramsRate = gramsRate,
                )
                if (defaultSync) updateUiState(itemUiState.itemDetails.copy(syncTins = true))
            }
        }
    }

    /** update item state **/
    fun updateUiState(itemDetails: ItemDetails) {
        val updatedDetails = if (itemDetails.syncTins) {
            val syncedTins = calculateSyncTins(itemDetails.tinDetailsList, tinConversion.value)
            itemDetails.copy(
                quantityString = syncedTins.toString(),
                quantity = syncedTins)
        } else { itemDetails }


        itemUiState = ItemUiState(
            itemDetails = updatedDetails,
            isEntryValid = validateInput(updatedDetails, updatedDetails.tinDetailsList) { tabErrorState = it }
        )
    }

    fun updateTinDetails(tinDetails: TinDetails) {
        val newList = itemUiState.itemDetails.tinDetailsList.map {
            if (it.tempTinId == tinDetails.tempTinId) {
                tinDetails.copy(labelIsValid = isTinLabelValid(itemUiState.itemDetails.tinDetailsList, tinDetails.tinLabel, tinDetails.tempTinId))
            } else {
                it.copy(labelIsValid = isTinLabelValid(itemUiState.itemDetails.tinDetailsList, it.tinLabel, it.tempTinId))
            }
        }
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = newList))
    }


    /** Popup and menu control **/
    private val _showRatingPop = MutableStateFlow(false)
    val showRatingPop = _showRatingPop.asStateFlow()

    fun onShowRatingPop(show: Boolean) { _showRatingPop.value = show }


    /** add/remove tins **/
    fun addTin() {
        val current = itemUiState.itemDetails.tinDetailsList
        val newTempId = if (current.isEmpty()) 1 else current.maxOf { it.tempTinId } + 1
        val newTin = TinDetails(tempTinId = newTempId, tinLabel = "")
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = current + newTin))
    }

    fun removeTin(index: Int) {
        val newList = itemUiState.itemDetails.tinDetailsList.toMutableList().apply { removeAt(index) }
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = newList))
    }


    /** check if Item already exists, display optional dialog if so **/
    val existState = mutableStateOf(ExistState())

    suspend fun checkItemExistsOnSave() {
        val currentItem = itemUiState.itemDetails
        val exists = itemsRepository.exists(currentItem.brand, currentItem.blend)
        if (exists) {
            val transferId = itemsRepository.getItemIdByIndex(currentItem.brand, currentItem.blend)

            existState.value =
                ExistState(
                    exists = true,
                    transferId = transferId!!
                )
        } else {
            existState.value =
                ExistState(
                    exists = false,
                    transferId = 0
                )
        }
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

            val components = itemUiState.itemDetails.componentString.toComponents(autoCompleteData.components)
            val flavoring = itemUiState.itemDetails.flavoringString.toFlavoring(autoCompleteData.flavorings)

            val lastModified = System.currentTimeMillis()
            val itemDetails = itemUiState.itemDetails.copy(lastModified = lastModified)

            val savedItemId = itemsRepository.insertItem(itemDetails.toItem())

            components.forEach {
                var componentId = itemsRepository.getComponentIdByName(it.componentName)
                if (componentId == null) {
                    componentId = itemsRepository.insertComponent(it).toInt()
                }
                itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = savedItemId.toInt(), componentId = componentId))
            }
            flavoring.forEach {
                var flavoringId = itemsRepository.getFlavoringIdByName(it.flavoringName)
                if (flavoringId == null) {
                    flavoringId = itemsRepository.insertFlavoring(it).toInt()
                }
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