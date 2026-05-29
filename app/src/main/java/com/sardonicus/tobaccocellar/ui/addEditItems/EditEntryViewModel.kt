package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class EditEntryViewModel(
    private val itemsId: Int,
    filterViewModel: FilterViewModel,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

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
        if (index < 2) {
            _currentLeftTab.value = index
        }
        updateUiState(itemUiState.itemDetails)
    }

    val loading = mutableStateOf(false)

    val originalItem = MutableStateFlow(OriginalItem())
    val originalTins = MutableStateFlow<List<OriginalTin>>(emptyList())
    val originalComponentList = MutableStateFlow<List<Components>>(emptyList())
    val originalFlavoringList = MutableStateFlow<List<Flavoring>>(emptyList())

    private val _uiEvents = MutableSharedFlow<ItemNotFoundEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            loading.value = true

            val initialDetails = withTimeoutOrNull(3000) {
                filterViewModel.everythingFlow
                    .mapNotNull { list -> list.find { it.items.id == itemsId } }
                    .first()
            }

            if (initialDetails == null) {
                loading.value = false
                _uiEvents.emit(ItemNotFoundEvent.ShowMessage("Error: item not found."))
                _uiEvents.emit(ItemNotFoundEvent.NavigateBack)
                return@launch
            }

            val components = initialDetails.components.map { it.componentName }.sorted().joinToString(", ")
            val flavoring = initialDetails.flavoring.map { it.flavoringName }.sorted().joinToString(", ")

            val itemDetails = initialDetails.items.toItemDetails(components, flavoring)
                .let { copyOriginalDetails(it) }

            val tins = initialDetails.tins
                .sortedBy { it.tinId }
                .mapIndexed { index, it ->
                    it.toTinDetails().copy(tempTinId = index + 1)
                }.also {
                    copyOriginalTins(it)
                }

            originalComponentList.value = initialDetails.components
            originalFlavoringList.value = initialDetails.flavoring

            val updatedDetails = itemDetails.copy(tinDetailsList = tins)

            itemUiState = itemUiState.copy(
                itemDetails = updatedDetails,
                isEntryValid = validateInput(updatedDetails, updatedDetails.tinDetailsList) { tabErrorState = it },
            )

            loading.value = false
        }
        viewModelScope.launch {
            combine(
                preferencesRepo.tinOzConversionRate,
                preferencesRepo.tinGramsConversionRate,
            ) { ozRate, gramsRate ->
                Pair(ozRate, gramsRate)
            }.first().let { (ozRate, gramsRate) ->
                tinConversion.value = TinConversion(
                    ozRate = ozRate,
                    gramsRate = gramsRate,
                )
            }
        }
    }

    private fun copyOriginalDetails(itemDetails: ItemDetails): ItemDetails {
        val original = itemDetails.toOriginalItem()

        originalItem.value = original

        return itemDetails.copy(originalItem = original)
    }

    private fun copyOriginalTins(tins: List<TinDetails>) {
        originalTins.value = tins.map { originalTin ->
            OriginalTin(
                tinId = originalTin.tinId,
                itemsId = originalTin.itemsId,
                tinLabel = originalTin.tinLabel,
                container = originalTin.container,
                tinQuantity = originalTin.tinQuantity,
                unit = originalTin.unit,
                manufactureDate = originalTin.manufactureDate,
                cellarDate = originalTin.cellarDate,
                openDate = originalTin.openDate,
                finished = originalTin.finished,
                lastModified = originalTin.lastModified
            )
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        val syncedTins = calculateSyncTins(itemDetails.tinDetailsList, tinConversion.value)

        val updatedDetails = itemDetails.copy(
            quantityString = if (itemDetails.syncTins) syncedTins.toString() else itemDetails.quantity.toString(),
            quantity = if (itemDetails.syncTins) syncedTins else itemDetails.quantity
        )

        itemUiState = ItemUiState(
            itemDetails = updatedDetails,
            isEntryValid = validateInput(updatedDetails, updatedDetails.tinDetailsList) { tabErrorState = it }
        )
    }

    fun updateTinDetails(tinDetails: TinDetails) {
        val newList = itemUiState.itemDetails.tinDetailsList.map {
            if (it.tempTinId == tinDetails.tempTinId) {
                tinDetails.copy(labelIsNotValid = isTinLabelValid(tinDetails.tinLabel, tinDetails.tempTinId))
            } else {
                it.copy(labelIsNotValid = isTinLabelValid(it.tinLabel, it.tempTinId))
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
        val tinDetailsList = itemUiState.itemDetails.tinDetailsList
        val existingIds = tinDetailsList.map { it.tinId }
        var newTempId = if (tinDetailsList.isEmpty()) 1 else tinDetailsList.maxOf { it.tempTinId } + 1
        while (existingIds.contains(newTempId)) {
            newTempId++
        }
        val newTin = TinDetails(tempTinId = newTempId, tinLabel = "")

       // tinDetailsList = tinDetailsList + newTin
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = tinDetailsList + newTin))
    }

    fun removeTin(index: Int) {
        val newList = itemUiState.itemDetails.tinDetailsList.toMutableList().apply { removeAt(index) }
        updateUiState(itemUiState.itemDetails.copy(tinDetailsList = newList))
    }

    fun isTinLabelValid(tinLabel: String, tempTinId: Int): Boolean {
        val check = itemUiState.itemDetails.tinDetailsList.filter { it.tempTinId != tempTinId }.none { it.tinLabel == tinLabel }
        return !check
    }


    /** check if Item already exists, display optional dialog if so **/
    val existState = mutableStateOf(ExistState())

    suspend fun checkItemExistsOnUpdate() {
        val currentItem = itemUiState.itemDetails

        if (currentItem.brand == originalItem.value.brand && currentItem.blend == originalItem.value.blend) {
            existState.value =
                ExistState(
                    exists = false,
                    transferId = 0,
                    existCheck = false,
                )
        } else {
            val existCheck = itemsRepository.exists(currentItem.brand, currentItem.blend)
            existState.value = if (existCheck) {
                ExistState(
                    exists = true,
                    transferId = 0,
                    existCheck = true
                )
            } else {
                ExistState(
                    exists = false,
                    transferId = 0,
                    existCheck = false,
                )
            }
        }
    }

    fun resetExistState() {
        existState.value =
            ExistState(
                exists = false,
                transferId = 0,
                existCheck = false,
            )
    }

    suspend fun updateItem() {
        val details = itemUiState.itemDetails
        if (validateInput(details, details.tinDetailsList) { }) {
            SyncStateManager.schedulingPaused = true

            val previousCompsSet = originalComponentList.first().map { it.componentName.lowercase() }.toSet()
            val editedComps = details.componentString.toComponents(autoCompleteData.components)
            val editedCompsSet = editedComps.map { it.componentName.lowercase() }.toSet()
            val compsToAdd = editedComps.filter { it.componentName.lowercase() !in previousCompsSet }
            val compsToRemove = originalComponentList.first().filter { it.componentName.lowercase() !in editedCompsSet }

            val previousFlavorsSet = originalFlavoringList.first().map { it.flavoringName.lowercase() }.toSet()
            val editedFlavoring = details.flavoringString.toFlavoring(autoCompleteData.flavorings)
            val editedFlavoringSet = editedFlavoring.map { it.flavoringName.lowercase() }.toSet()
            val flavorToAdd = editedFlavoring.filter { it.flavoringName.lowercase() !in previousFlavorsSet }
            val flavorToRemove = originalFlavoringList.first().filter { it.flavoringName.lowercase() !in editedFlavoringSet }

            val previousTins = originalTins.first()
            val existingTinIds = previousTins.map { it.tinId }
            val newTins = details.tinDetailsList.filter { !existingTinIds.contains(it.tinId) }
            val updatedTins = details.tinDetailsList.filter { existingTinIds.contains(it.tinId) }.filter {
                it.toOriginalTin() != previousTins.find { originalTin -> originalTin.tinId == it.tinId }
            }
            val conflictingTins = previousTins.filter { tin ->
                // check for label conflicts in the event of swapped tin labels
                details.tinDetailsList.any { it.tinId != tin.tinId && it.tinLabel == tin.tinLabel }
            }
            val tinsToDelete = existingTinIds.filter { tinId -> !details.tinDetailsList.map { it.tinId }.contains(tinId) }

            val actuallyUpdated = (details.toOriginalItem() != details.originalItem) ||
                    compsToAdd.isNotEmpty() || compsToRemove.isNotEmpty() ||
                    flavorToAdd.isNotEmpty() || flavorToRemove.isNotEmpty() ||
                    newTins.isNotEmpty() || updatedTins.isNotEmpty() || tinsToDelete.isNotEmpty()

            val time = System.currentTimeMillis()

            if (actuallyUpdated) {
                itemsRepository.updateItem(details.copy(lastModified = time).toItem())
            }

            compsToAdd.forEach {
                var componentId = itemsRepository.getComponentIdByName(it.componentName)
                if (componentId == null) {
                    componentId = itemsRepository.insertComponent(it).toInt()
                }
                itemsRepository.insertComponentsCrossRef(
                    ItemsComponentsCrossRef(itemsId, componentId)
                )
            }
            compsToRemove.forEach {
                val componentId = it.componentId
                itemsRepository.deleteComponentsCrossRef(
                    ItemsComponentsCrossRef(itemsId, componentId)
                )
            }

            flavorToAdd.forEach {
                var flavorId = itemsRepository.getFlavoringIdByName(it.flavoringName)
                if (flavorId == null) {
                    flavorId = itemsRepository.insertFlavoring(it).toInt()
                }
                itemsRepository.insertFlavoringCrossRef(
                    ItemsFlavoringCrossRef(itemsId, flavorId)
                )
            }
            flavorToRemove.forEach {
                val flavorId = it.flavoringId
                itemsRepository.deleteFlavoringCrossRef(
                    ItemsFlavoringCrossRef(itemsId, flavorId)
                )
            }

            tinsToDelete.forEach {
                itemsRepository.deleteTin(it)
            }
            delay(1)
            conflictingTins.forEach { blocker ->
                val tempTin = Tins(
                    tinId = blocker.tinId,
                    itemsId = blocker.itemsId,
                    tinLabel = "${blocker.tinLabel}_temp_${System.currentTimeMillis()}",
                    container = blocker.container,
                    tinQuantity = blocker.tinQuantity,
                    unit = blocker.unit,
                    manufactureDate = blocker.manufactureDate,
                    cellarDate = blocker.cellarDate,
                    openDate = blocker.openDate,
                    finished = blocker.finished,
                    lastModified = time
                )
                itemsRepository.updateTin(tempTin)
            }
            delay(1)
            updatedTins.forEach {
                val tin = it.copy(lastModified = time).toTin(itemsId)
                itemsRepository.updateTin(tin)
            }
            delay(1)
            newTins.forEach {
                val tin = it.copy(lastModified = time).toTin(itemsId)
                itemsRepository.insertTin(tin)
            }

            SyncStateManager.schedulingPaused = false
            if (actuallyUpdated) { itemsRepository.triggerUploadWorker() }
            EventBus.emit(ItemUpdatedEvent())
        }
    }

    suspend fun deleteItem() { itemsRepository.deleteItem(itemUiState.itemDetails.toItem()) }

}


data class ItemUpdatedEvent(
    val updatedEvent: Boolean = true,
)


fun ItemDetails.toOriginalItem(): OriginalItem {
    return OriginalItem(
        id = this.id,
        brand = this.brand,
        blend = this.blend,
        type = this.type,
        quantity = this.quantity,
        rating = this.rating,
        favorite = this.favorite,
        disliked = this.disliked,
        notes = this.notes,
        subGenre = this.subGenre,
        cut = this.cut,
        inProduction = this.inProduction,
        syncTins = this.syncTins,
        lastModified = this.lastModified
    )
}

fun TinDetails.toOriginalTin(): OriginalTin {
    return OriginalTin(
        tinId = this.tinId,
        itemsId = this.itemsId,
        tinLabel = this.tinLabel,
        container = this.container,
        tinQuantity = this.tinQuantity,
        unit = this.unit,
        manufactureDate = this.manufactureDate,
        cellarDate = this.cellarDate,
        openDate = this.openDate,
        finished = this.finished,
        lastModified = this.lastModified
    )
}

sealed class ItemNotFoundEvent {
    object NavigateBack: ItemNotFoundEvent()
    data class ShowMessage(val message: String): ItemNotFoundEvent()
}