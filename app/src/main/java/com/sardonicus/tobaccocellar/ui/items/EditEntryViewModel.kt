package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.roundToInt

class EditEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    /** current item state **/
    var itemUiState by mutableStateOf(ItemUiState())
        private set
    var componentList by mutableStateOf(ComponentList())
        private set
    var tinDetailsState by mutableStateOf(TinDetails())
        private set
    var tinDetailsList by mutableStateOf<List<TinDetails>>(emptyList())
        private set
    var tabErrorState by mutableStateOf(TabErrorState())
        private set
    var loading by mutableStateOf(true)

    private var _originalComponents = mutableStateOf("")
    val originalComponents = _originalComponents

    private val itemsId: Int = checkNotNull(savedStateHandle[EditEntryDestination.itemsIdArg])

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        var validDetails = true
        var validTins = true

        validDetails = uiState.brand.isNotBlank() && uiState.blend.isNotBlank()
        validTins = uiState.tinDetailsList.all {
            it.tinLabel.isNotBlank() &&
            tinDetailsList.map { it.tinLabel }.distinct().size == tinDetailsList.size &&
            tinDetailsList.all {
                (it.tinQuantityString.isNotBlank() && it.unit.isNotBlank()) ||
                    it.tinQuantityString.isBlank() } &&
                    tinDetailsList.all {
                        var valid = true
                        val (manufactureValid, cellarValid, openValid) =
                            validateDates(it.manufactureDate, it.cellarDate, it.openDate)
                        valid = manufactureValid && cellarValid && openValid
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
    }

    init {
        viewModelScope.launch {
            loading = true
            val itemDetails = itemsRepository.getItemStream(itemsId)
                .filterNotNull()
                .first()
                .toItemUiState(true)
                .also { copyOriginalDetails(it.itemDetails) }
                .let {
                    val savedSynced = preferencesRepo.getItemSyncState(itemsId).first()
                    it.copy(itemDetails = it.itemDetails.copy(isSynced = savedSynced))
                }

            val components = itemsRepository.getComponentsForItemStream(itemsId)
                .filterNotNull()
                .first()
                .toComponentList()
                .also {
                    _originalComponents = mutableStateOf(it.componentString)
                }

            val existingTins = itemsRepository.getTinsForItemStream(itemsId)
                .filterNotNull()
                .first()
                .map { it.toTinDetails() }

            val tins = existingTins.mapIndexed { index, tinDetails ->
                tinDetails.copy(tempTinId = index + 1)
            }
            tinDetailsList = tins
            componentList = components

            val updatedDetails = itemDetails.itemDetails.copy(tinDetailsList = tins)
            itemUiState = itemDetails.copy(itemDetails = updatedDetails, isEntryValid = validateInput(updatedDetails))
            loading = false
        }
    }


    /** autocomplete vals **/
    private val _brands = MutableStateFlow<List<String>>(emptyList())
    private val brands: StateFlow<List<String>> = _brands

    private val _subGenres = MutableStateFlow<List<String>>(emptyList())
    private val subGenres: StateFlow<List<String>> = _subGenres

    private val _cuts = MutableStateFlow<List<String>>(emptyList())
    private val cuts: StateFlow<List<String>> = _cuts

    private val _components = MutableStateFlow<List<String>>(emptyList())
    val components: StateFlow<List<String>> = _components

    private val _tinContainers = MutableStateFlow<List<String>>(emptyList())
    private val tinContainers: StateFlow<List<String>> = _tinContainers

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getEverythingStream().collectLatest {
                    _brands.value = it.map { it.items.brand }.distinct().sorted()
                    _subGenres.value = it
                        .map {
                            it.items.subGenre
                        }.distinct().sorted()
                    _cuts.value = it
                        .map {
                            it.items.cut
                        }.distinct().sorted()
                    _components.value = it.flatMap { it.components }
                        .map {
                            it.componentName
                        }.distinct().sorted()
                    _tinContainers.value = it.flatMap { it.tins }
                        .map {
                            it.container
                        }.distinct().sorted()
                }
            }
        }
    }


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
    }

    private val _labelInvalid = MutableStateFlow<Boolean>(false)
    val labelInvalid: StateFlow<Boolean> = _labelInvalid

    fun isTinLabelValid(tinLabel: String, tempTinId: Int): Boolean {
        val check = tinDetailsList.filter { it.tempTinId != tempTinId }.none { it.tinLabel == tinLabel }
        return !check
        _labelInvalid.value = !check
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
        val totalLbsTins = tinDetailsList.filter { it.unit == "lbs" }.sumOf {
            (it.tinQuantity * 16) / tinConversion.value.ozRate }
        val totalOzTins = tinDetailsList.filter { it.unit == "oz" }.sumOf {
            it.tinQuantity / tinConversion.value.ozRate }
        val totalGramsTins = tinDetailsList.filter { it.unit == "grams" }.sumOf {
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
        val syncedTins = calculateSyncTins()
        val tinDetailsList = tinDetailsList
        val updatedDetails = if (itemDetails.isSynced) {
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
                autoBrands = brands.value,
                autoGenres = subGenres.value,
                autoCuts = cuts.value,
                autoContainers = tinContainers.value,
            )
    }

    fun updateComponentList(componentString: String) {
        componentList =
            ComponentList(
                componentString = componentString,
                autoComps = components.value,
            )
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
                manufactureDateShort = tinDetails.manufactureDateShort,
                cellarDateShort = tinDetails.cellarDateShort,
                openDateShort = tinDetails.openDateShort,
                manufactureDateLong = tinDetails.manufactureDateLong,
                cellarDateLong = tinDetails.cellarDateLong,
                openDateLong = tinDetails.openDateLong,
                detailsExpanded = tinDetails.detailsExpanded,
                labelIsNotValid = labelInvalid.value
            )
    }

    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails)) {
            val components = componentList.toComponents(components.value)
            val previousComps = itemsRepository.getComponentsForItemStream(itemsId).first().map { it.componentName }
            val newComps = components.filter { !previousComps.contains(it.componentName) }
            val removedComps = previousComps.filter { !components.map { it.componentName }.contains(it) }
            val existingTinIds = itemsRepository.getTinsForItemStream(itemsId).first().map { it.tinId }

            itemsRepository.updateItem(itemUiState.itemDetails.toItem())
            preferencesRepo.setItemSyncState(itemsId, itemUiState.itemDetails.isSynced)
            newComps.forEach {
                var componentId = itemsRepository.getComponentIdByName(it.componentName)
                if (componentId == null) {
                    componentId = itemsRepository.insertComponent(it).toInt()
                }
                itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = itemsId, componentId = componentId))
            }
            removedComps.forEach {
                val componentId = itemsRepository.getComponentIdByName(it)
                if (componentId != null) {
                    itemsRepository.deleteComponentsCrossRef(itemsId, componentId)
                }
            }
            tinDetailsList.filter { !existingTinIds.contains(it.tinId) }.forEach {
                val tin = it.toTin(itemsId)
                itemsRepository.insertTin(tin)
            }
            tinDetailsList.filter { existingTinIds.contains(it.tinId) }.forEach {
                val tin = it.toTin(itemsId)
                itemsRepository.updateTin(tin)
            }
            existingTinIds.filter { !tinDetailsList.map { it.tinId }.contains(it) }.forEach {
                itemsRepository.deleteTin(it)
            }

            EventBus.emit(ItemUpdatedEvent())
        }
    }

    suspend fun deleteItem() {
        itemsRepository.deleteItem(itemUiState.itemDetails.toItem())
    }

}

data class ItemUpdatedEvent(
    val updatedEvent: Boolean = true,
)