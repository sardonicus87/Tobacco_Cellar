package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.sardonicus.tobaccocellar.data.Tins
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.format.FormatStyle
import kotlin.String
import kotlin.collections.List
import kotlin.math.roundToInt

class AddEntryViewModel(
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
): ViewModel() {

    /** current item state **/
    var itemUiState by mutableStateOf(ItemUiState())
        private set
    var componentList by mutableStateOf(ComponentList())
        private set
    var tinDetailsState by mutableStateOf(TinDetails())
        private set
    var tinDetailsList by mutableStateOf<List<TinDetails>>(emptyList())
        private set


    /** update item state **/
    fun updateUiState(itemDetails: ItemDetails) {
        val syncedTins = calculateSyncTins()
        val updatedDetails = if (itemDetails.isSynced) {
            itemDetails.copy(
                quantityString = syncedTins.toString(),
                quantity = syncedTins,
                syncedQuantity = syncedTins,
            ) } else {
            itemDetails.copy(
                syncedQuantity = syncedTins,
            )
        }

        itemUiState =
            ItemUiState(
                itemDetails = updatedDetails,
                isEntryValid = validateInput(itemDetails),
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
                tinDetails
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


    /** add/remove tins **/
    fun addTin() {
        val newTempId = if (tinDetailsList.isEmpty()) 1 else tinDetailsList.maxOf { it.tempTinId } + 1
        val newTin = TinDetails(tempTinId = newTempId, tinLabel = "")
        tinDetailsList = tinDetailsList + newTin
    }

    fun removeTin(tinIndex: Int) {
        tinDetailsList = tinDetailsList.toMutableList()
            .also {
                it.removeAt(tinIndex)
            }
    }

    val _labelInvalid = MutableStateFlow<Boolean>(false)
    val labelInvalid: StateFlow<Boolean> = _labelInvalid

    fun isTinLabelValid(tinLabel: String, tempTinId: Int): Boolean {
        val check = tinDetailsList.filter { it.tempTinId != tempTinId }.none { it.tinLabel == tinLabel }
        return !check
        _labelInvalid.value = !check
    }


    /** tin conversion and sync state **/
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


    /** autocomplete vals **/
    private val _brands = MutableStateFlow<List<String>>(emptyList())
    private val brands: StateFlow<List<String>> = _brands

    private val _subGenres = MutableStateFlow<List<String>>(emptyList())
    private val subGenres: StateFlow<List<String>> = _subGenres

    private val _cuts = MutableStateFlow<List<String>>(emptyList())
    private val cuts: StateFlow<List<String>> = _cuts

    private val _components = MutableStateFlow<List<String>>(emptyList())
    private val components: StateFlow<List<String>> = _components

    private val _tinContainers = MutableStateFlow<List<String>>(emptyList())
    private val tinContainers: StateFlow<List<String>> = _tinContainers

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllBrandsStream().collect {
                    _brands.value = it
                }.also {
                    _brands.value = ItemUiState().autoBrands
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllSubGenresStream().collect {
                    _subGenres.value = it
                }.also {
                    _subGenres.value = ItemUiState().autoGenres
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllCutsStream().collect {
                    _cuts.value = it
                }.also {
                    _cuts.value = ItemUiState().autoCuts
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllCompNamesStream().collect {
                    _components.value = it
                }.also {
                    _components.value = ComponentList().autoComps
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemsRepository.getAllTinContainersStream().collect {
                    _tinContainers.value = it
                }.also {
                    _tinContainers.value = ItemUiState().autoContainers
                }
            }
        }
    }


    /** save to database **/
    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            brand.isNotBlank() && blend.isNotBlank() &&
                (tinDetailsList.isEmpty() ||
                    (tinDetailsList.all { it.tinLabel.isNotBlank() } &&
                    tinDetailsList.map { it.tinLabel }.distinct().size == tinDetailsList.size)
                )
        }
    }

    suspend fun saveItem() {
        if (validateInput()) {
            val components = componentList.toComponents(components.value)

            val savedItemId = itemsRepository.insertItem(itemUiState.itemDetails.toItem())

            preferencesRepo.setItemSyncState(savedItemId.toInt(), itemUiState.itemDetails.isSynced)

            components.forEach {
                var componentId = itemsRepository.getComponentIdByName(it.componentName)
                if (componentId == null) {
                    componentId = itemsRepository.insertComponent(it).toInt()
                }
                itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemId = savedItemId.toInt(), componentId = componentId))
            }

            tinDetailsList.forEach {
                val tin = it.toTin(savedItemId.toInt())
                itemsRepository.insertTin(tin)
            }

            EventBus.emit(ItemSavedEvent(savedItemId))
        }
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
    val autoGenres: List<String> = listOf(),
    val autoCuts: List<String> = listOf(),
    val autoContainers: List<String> = listOf(),
)

data class ItemDetails(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val quantity: Int = 1,
    val quantityString: String = "",
    var disliked: Boolean = false,
    var favorite: Boolean = false,
    val notes: String = "",
    var originalBrand: String = "",
    var originalBlend: String = "",
    val subGenre: String = "",
    val cut: String = "",
    val inProduction: Boolean = true,
    val isSynced: Boolean = false,
    val syncedQuantity: Int = 0,
)

data class TinDetails(
    val tinId: Int = 0,
    val tempTinId: Int = 0,
    val itemsId: Int = 0,
    val tinLabel: String = "",
    val container: String = "",
    val tinQuantity: Double = 0.0,
    var tinQuantityString: String = "",
    val unit: String = "",
    val manufactureDate: Long? = null,
    val cellarDate: Long? = null,
    val openDate: Long? = null,
    var manufactureDateShort: String = "",
    var cellarDateShort: String = "",
    var openDateShort: String = "",
    var manufactureDateLong: String = "",
    var cellarDateLong: String = "",
    var openDateLong: String = "",
    var detailsExpanded: Boolean = false,
    var labelIsNotValid: Boolean = false
)

data class ComponentList(
    val componentString: String = "",
    val autoComps: List<String> = listOf(),
)

data class TinConversion(
    val amount: String = "",
    val unit: String = "",
    val ozRate: Double = 1.75,
    val gramsRate: Double = 50.0,
    val isConversionValid: Boolean = false,
)

data class ItemSavedEvent(
    val savedItemId: Long
)


/** copy data classes to database table entity **/
fun ItemDetails.toItem(): Items = Items(
    id = id,
    brand = brand,
    blend = blend,
    type = type,
    quantity = quantity,
    disliked = disliked,
    favorite = favorite,
    notes = notes,
    subGenre = subGenre,
    cut = cut,
    inProduction = inProduction,
)

fun TinDetails.toTin(itemsId: Int): Tins = Tins(
    tinId = tinId,
    itemsId = itemsId,
    tinLabel = tinLabel,
    container = container,
    tinQuantity = tinQuantity,
    unit = unit,
    manufactureDate = manufactureDate,
    cellarDate = cellarDate,
    openDate = openDate,
)

fun ComponentList.toComponents(existingComps: List<String>): List<Components> {
    return componentString
        .replace(Regex("\\s+"), " ")
        .split(",")
        .filter { it.isNotBlank() }.map { enteredComp ->
            val normalizedComp = enteredComp.trim().lowercase()
            val existingComp = existingComps.find { existingComp ->
                existingComp.lowercase() == normalizedComp
            }

            Components(componentName = existingComp ?: enteredComp.trim())
        }
}

//fun ComponentList.toComponents(): List<Components> {
//    return componentString.split(", ").filter { it.isNotBlank() }.map {
//        Components(componentName = it)
//    }
//}


/** convert Items (Database Table) to ItemUiState **/
fun Items.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid
)


/** copy database entities to data classes **/
fun Items.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    brand = brand,
    blend = blend,
    type = type,
    quantity = quantity,
    disliked = disliked,
    favorite = favorite,
    quantityString = quantity.toString(),
    notes = notes,
    subGenre = subGenre,
    cut = cut,
    inProduction = inProduction,
)

fun Tins.toTinDetails(): TinDetails = TinDetails(
    tinId = tinId,
    itemsId = itemsId,
    tinLabel = tinLabel,
    container = container,
    tinQuantity = tinQuantity,
    tinQuantityString = tinQuantity.toString(),
    unit = unit,
    manufactureDate = manufactureDate,
    cellarDate = cellarDate,
    openDate = openDate,
    manufactureDateShort = formatShortDate(manufactureDate),
    cellarDateShort = formatShortDate(cellarDate),
    openDateShort = formatShortDate(openDate),
    manufactureDateLong = formatLongDate(manufactureDate),
    cellarDateLong = formatLongDate(cellarDate),
    openDateLong = formatLongDate(openDate),
)

fun formatShortDate(millis: Long?): String {
    return if (millis != null) {
        val instant = Instant.ofEpochMilli(millis)
        val formatter = DateTimeFormatter.ofPattern("MM/yy").withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } else { "" }
}

fun formatLongDate(millis: Long?): String {
    return if (millis != null) {
        val instant = Instant.ofEpochMilli(millis)
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        formatter.format(localDate)
    } else { "" }
}

fun List<Components>.toComponentList(): ComponentList {
    val componentString = this.joinToString(", ") { it.componentName }
    return ComponentList(
        componentString = componentString
    )
}






