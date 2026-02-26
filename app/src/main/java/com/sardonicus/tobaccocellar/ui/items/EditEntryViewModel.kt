package com.sardonicus.tobaccocellar.ui.items

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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.roundToInt

class EditEntryViewModel(
    private val itemsId: Int,
    filterViewModel: FilterViewModel,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    /** current item state **/
    var itemUiState by mutableStateOf(ItemUiState())
        private set
    var componentList by mutableStateOf(ComponentList())
        private set
    var flavoringList by mutableStateOf(FlavoringList())
        private set
    var tinDetailsState by mutableStateOf(TinDetails())
        private set
    var tinDetailsList by mutableStateOf<List<TinDetails>>(emptyList())
        private set
    var tabErrorState by mutableStateOf(TabErrorState())
        private set
    var loading by mutableStateOf(false)

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()
    fun updateSelectedTab(index: Int) {
        _selectedTabIndex.value = index
        updateUiState(itemUiState.itemDetails)
    }

    var originalItem by mutableStateOf(OriginalItem())
    var originalTins by mutableStateOf<List<OriginalTin>>(emptyList())

    val autoCompleteData = filterViewModel.autoComplete.value

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        val validDetails = uiState.brand.isNotBlank() && uiState.blend.isNotBlank()
        val validTins = uiState.tinDetailsList.all { tinDetails ->
            tinDetails.tinLabel.isNotBlank() &&
            tinDetailsList.map { it.tinLabel }.distinct().size == tinDetailsList.size &&
            tinDetailsList.all {
                (it.tinQuantityString.isNotBlank() && it.unit.isNotBlank()) ||
                    it.tinQuantityString.isBlank() } &&
                    tinDetailsList.all {
                        val (manufactureValid, cellarValid, openValid) =
                            validateDates(it.manufactureDate, it.cellarDate, it.openDate)
                        val valid = manufactureValid && cellarValid && openValid
                        valid
                    }
        }

        tabErrorState = tabErrorState.copy(
            detailsError = !validDetails,
            tinsError = !validTins
        )

        return validDetails && validTins
    }

    fun validateDates(
        manufactureDate: Long?,
        cellarDate: Long?,
        openDate: Long?,
    ): Triple<Boolean, Boolean, Boolean> {
        var manufactureCellarValid = true
        var manufactureOpenValid = true
        var cellarOpenValid = true

        if (manufactureDate != null && cellarDate != null) {
            val manufacture = LocalDateTime.ofInstant(Instant.ofEpochMilli(manufactureDate), ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
            if (manufacture > cellarDate) {
                manufactureCellarValid = false
            }
        }
        if (manufactureDate != null && openDate != null) {
            val manufacture = LocalDateTime.ofInstant(Instant.ofEpochMilli(manufactureDate), ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
            if (manufacture > openDate) {
                manufactureOpenValid = false
            }
        }
        if (cellarDate != null && openDate != null) {
            val cellar = LocalDateTime.ofInstant(Instant.ofEpochMilli(cellarDate), ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
            if (cellar > openDate) {
                cellarOpenValid = false
            }
        }
        return Triple(manufactureCellarValid, manufactureOpenValid, cellarOpenValid)
    }

    private fun copyOriginalDetails(itemDetails: ItemDetails) {
        itemDetails.originalBrand = itemDetails.brand
        itemDetails.originalBlend = itemDetails.blend

        originalItem = originalItem.copy(
            id = itemDetails.id,
            brand = itemDetails.originalBrand,
            blend = itemDetails.originalBlend,
            type = itemDetails.type,
            quantity = itemDetails.quantity,
            rating = itemDetails.rating,
            favorite = itemDetails.favorite,
            disliked = itemDetails.disliked,
            notes = itemDetails.notes,
            subGenre = itemDetails.subGenre,
            cut = itemDetails.cut,
            inProduction = itemDetails.inProduction,
            syncTins = itemDetails.syncTins,
            lastModified = itemDetails.lastModified
        )
    }

    private fun copyOriginalTins(tins: List<TinDetails>) {
        originalTins = tins.map { originalTin ->
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

    init {
        viewModelScope.launch {
            loading = true

            val initialDetails = itemsRepository.getItemDetailsStream(itemsId)
                .filterNotNull()
                .first()

            val itemDetails = initialDetails.items.toItemDetails()
                .also { copyOriginalDetails(it) }
            val components = initialDetails.components.toComponentList()
            val flavoring = initialDetails.flavoring.toFlavoringList()
            val tins = initialDetails.tins.mapIndexed { index, it ->
                it.toTinDetails().copy(tempTinId = index + 1)
            }. also {
                copyOriginalTins(it)
            }

            componentList = components
            flavoringList = flavoring
            tinDetailsList = tins

            val updatedDetails = itemDetails.copy(tinDetailsList = tins)

            itemUiState = itemUiState.copy(
                itemDetails = updatedDetails,
                isEntryValid = validateInput(updatedDetails),
            )

            loading = false
        }
    }

    /** Popup and menu control **/
    private val _showRatingPop = MutableStateFlow(false)
    val showRatingPop = _showRatingPop.asStateFlow()

    fun onShowRatingPop(show: Boolean) { _showRatingPop.value = show }


    /** add/remove tins **/
    fun addTin() {
        val existingIds = tinDetailsList.map { it.tinId }
        var newTempId = if (tinDetailsList.isEmpty()) 1 else tinDetailsList.maxOf { it.tempTinId } + 1
        while (existingIds.contains(newTempId)) {
            newTempId++
        }
        val newTin = TinDetails(tempTinId = newTempId, tinLabel = "")

        tinDetailsList = tinDetailsList + newTin
    }

    fun removeTin(tinIndex: Int) {
        tinDetailsList = tinDetailsList.toMutableList()
            .also {
                it.removeAt(tinIndex)
            }
        updateUiState(itemUiState.itemDetails)
    }

    private val _labelInvalid = MutableStateFlow(false)
    val labelInvalid: StateFlow<Boolean> = _labelInvalid

    fun isTinLabelValid(tinLabel: String, tempTinId: Int): Boolean {
        val check = tinDetailsList.filter { it.tempTinId != tempTinId }.none { it.tinLabel == tinLabel }
        _labelInvalid.value = !check
        return !check
    }


    /** tin conversion and sync state **/
    var tinConversion = mutableStateOf(TinConversion())
        private set

    init {
        viewModelScope.launch {
            tinConversion.value = TinConversion(
                ozRate = preferencesRepo.tinOzConversionRate.first(),
                gramsRate = preferencesRepo.tinGramsConversionRate.first(),
            )
        }
    }

    fun calculateSyncTins(): Int {
        val tins = tinDetailsList.filter { !it.finished }

        val totalLbsTins = tins.filter { it.unit == "lbs" }.sumOf {
            (it.tinQuantity * 16) / tinConversion.value.ozRate }
        val totalOzTins = tins.filter { it.unit == "oz" }.sumOf {
            it.tinQuantity / tinConversion.value.ozRate }
        val totalGramsTins = tins.filter { it.unit == "grams" }.sumOf {
            it.tinQuantity / tinConversion.value.gramsRate }

        val syncedTotal =  (totalLbsTins + totalOzTins + totalGramsTins).roundToInt()

        itemUiState = itemUiState.copy(
            itemDetails = itemUiState.itemDetails.copy(
                syncedQuantity = syncedTotal
            )
        )
        return syncedTotal
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
            val existCheck = itemsRepository.exists(currentItem.brand, currentItem.blend)
            existState = if (existCheck) {
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
        existState =
            ExistState(
                exists = false,
                transferId = 0,
                existCheck = false,
            )
    }

    fun updateUiState(itemDetails: ItemDetails) {
        val syncedTins = calculateSyncTins()
        val tinDetailsList = tinDetailsList
        val updatedDetails = if (itemDetails.syncTins) {
            itemDetails.copy(
                quantityString = syncedTins.toString(),
                quantity = syncedTins,
                syncedQuantity = syncedTins,
                tinDetailsList = tinDetailsList
            ) } else {
            itemDetails.copy(
                syncedQuantity = syncedTins,
                tinDetailsList = tinDetailsList
            )
        }

        itemUiState =
            ItemUiState(
                itemDetails = updatedDetails,
                isEntryValid = validateInput(updatedDetails),
                autoBrands = autoCompleteData.brands,
                autoGenres = autoCompleteData.subgenres,
                autoCuts = autoCompleteData.cuts,
                autoContainers = autoCompleteData.tinContainers,
            )
    }

    fun updateComponentList(componentString: String) {
        componentList =
            ComponentList(
                componentString = componentString,
                autoComps = autoCompleteData.components,
            )
        updateUiState(itemUiState.itemDetails)
    }

    fun updateFlavoringList(flavoringString: String) {
        flavoringList =
            FlavoringList(
                flavoringString = flavoringString,
                autoFlavors = autoCompleteData.flavorings,
            )
        updateUiState(itemUiState.itemDetails)
    }

    fun updateTinDetails(tinDetails: TinDetails) {
        tinDetailsList = tinDetailsList.map {
            if (it.tempTinId == tinDetails.tempTinId) {
                tinDetails.copy(tempTinId = it.tempTinId)
            } else {
                it
            }
        }

        tinDetailsState =
            TinDetails(
                tinId = tinDetails.tinId,
                tempTinId = tinDetails.tempTinId,
                itemsId = tinDetails.itemsId,
                tinLabel = tinDetails.tinLabel,
                container = tinDetails.container,
                tinQuantity = tinDetails.tinQuantity,
                tinQuantityString = tinDetails.tinQuantityString,
                unit = tinDetails.unit,
                manufactureDate = tinDetails.manufactureDate,
                cellarDate = tinDetails.cellarDate,
                openDate = tinDetails.openDate,
                finished = tinDetails.finished,
                lastModified = tinDetails.lastModified,
                manufactureDateShort = tinDetails.manufactureDateShort,
                cellarDateShort = tinDetails.cellarDateShort,
                openDateShort = tinDetails.openDateShort,
                manufactureDateLong = tinDetails.manufactureDateLong,
                cellarDateLong = tinDetails.cellarDateLong,
                openDateLong = tinDetails.openDateLong,
                detailsExpanded = tinDetails.detailsExpanded,
                labelIsNotValid = labelInvalid.value
            )
        updateUiState(itemUiState.itemDetails)
    }

    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails)) {
            SyncStateManager.schedulingPaused = true

            val lastModified = System.currentTimeMillis()

            val previousComps = itemsRepository.getComponentsForItemStream(itemsId).first()
            val previousCompsSet = previousComps.map { it.componentName.lowercase() }
            val editedComps = componentList.toComponents(autoCompleteData.components)
            val editedCompsSet = editedComps.map { it.componentName.lowercase() }
            val compsToAdd = editedComps.filter { it.componentName.lowercase() !in previousCompsSet }
            val compsToRemove = previousComps.filter { it.componentName.lowercase() !in editedCompsSet }

            val previousFlavors = itemsRepository.getFlavoringForItemStream(itemsId).first()
            val previousFlavorsSet = previousFlavors.map { it.flavoringName.lowercase() }
            val editedFlavoring = flavoringList.toFlavoring(autoCompleteData.flavorings)
            val editedFlavoringSet = editedFlavoring.map { it.flavoringName.lowercase() }
            val flavorToAdd = editedFlavoring.filter { it.flavoringName.lowercase() !in previousFlavorsSet }
            val flavorToRemove = previousFlavors.filter { it.flavoringName.lowercase() !in editedFlavoringSet }

            val existingTinIds = originalTins.map { it.tinId }
            val newTins = tinDetailsList.filter { !existingTinIds.contains(it.tinId) }
            val updatedTins = tinDetailsList.filter { existingTinIds.contains(it.tinId) }.filter {
                it.toOriginalTin() != originalTins.find { originalTin -> originalTin.tinId == it.tinId }
            }
            val tinsToDelete = existingTinIds.filter { tinId -> !tinDetailsList.map { it.tinId }.contains(tinId) }

            val actuallyUpdated = (itemUiState.itemDetails.toOriginalItem() != originalItem) ||
                    compsToAdd.isNotEmpty() || compsToRemove.isNotEmpty() ||
                    flavorToAdd.isNotEmpty() || flavorToRemove.isNotEmpty() ||
                    newTins.isNotEmpty() || updatedTins.isNotEmpty() || tinsToDelete.isNotEmpty()

            val itemDetails = itemUiState.itemDetails.copy(lastModified = lastModified)

            if (actuallyUpdated) { itemsRepository.updateItem(itemDetails.toItem()) }

            compsToAdd.forEach {
                var componentId = itemsRepository.getComponentIdByName(it.componentName)
                if (componentId == null) {
                    componentId = itemsRepository.insertComponent(it).toInt()
                }
                itemsRepository.insertComponentsCrossRef(
                    ItemsComponentsCrossRef(itemsId, componentId)
                )
            }
            compsToRemove.forEach{
                val componentId = itemsRepository.getComponentIdByName(it.componentName)
                if (componentId != null) {
                    itemsRepository.deleteComponentsCrossRef(
                        ItemsComponentsCrossRef(itemsId, componentId)
                    )
                }
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
                val flavorId = itemsRepository.getFlavoringIdByName(it.flavoringName)
                if (flavorId != null) {
                    itemsRepository.deleteFlavoringCrossRef(
                        ItemsFlavoringCrossRef(itemsId, flavorId)
                    )
                }
            }

            newTins.forEach {
                val tin = it.copy(lastModified = lastModified).toTin(itemsId)
                itemsRepository.insertTin(tin)
            }
            updatedTins.forEach {
                val tin = it.copy(lastModified = lastModified).toTin(itemsId)
                itemsRepository.updateTin(tin)
            }
            tinsToDelete.forEach {
                itemsRepository.deleteTin(it)
            }

            SyncStateManager.schedulingPaused = false
            if (actuallyUpdated) { itemsRepository.triggerUploadWorker() }
            EventBus.emit(ItemUpdatedEvent())
        }
    }

    suspend fun deleteItem() { itemsRepository.deleteItem(itemUiState.itemDetails.toItem()) }

}

data class OriginalItem(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val rating: Double? = 0.0,
    val favorite: Boolean = false,
    val disliked: Boolean = false,
    val notes: String = "",
    val subGenre: String = "",
    val cut: String = "",
    val inProduction: Boolean = false,
    val syncTins: Boolean = false,
    val lastModified: Long = -1L
)

data class OriginalTin(
    val tinId: Int = 0,
    val itemsId: Int = 0,
    val tinLabel: String = "",
    val container: String = "",
    val tinQuantity: Double = 0.0,
    val unit: String = "",
    val manufactureDate: Long?,
    val cellarDate: Long?,
    val openDate: Long?,
    val finished: Boolean = false,
    val lastModified: Long = -1L
)

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