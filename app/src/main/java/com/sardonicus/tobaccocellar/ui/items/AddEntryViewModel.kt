package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.floor
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
    var flavoringList by mutableStateOf(FlavoringList())
        private set
    var tinDetailsState by mutableStateOf(TinDetails())
        private set
    var tinDetailsList by mutableStateOf<List<TinDetails>>(emptyList())
        private set
    var tabErrorState by mutableStateOf(TabErrorState())
        private set


    /** update item state **/
    fun updateUiState(itemDetails: ItemDetails) {
        val tinDetailsList = tinDetailsList
        val syncedTins = calculateSyncTins()
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
        updateUiState(itemUiState.itemDetails)
    }

    fun updateFlavoringList(flavoringString: String) {
        flavoringList =
            FlavoringList(
                flavoringString = flavoringString,
                autoFlavors = flavoring.value,
            )
        updateUiState(itemUiState.itemDetails)
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
                finished = tinDetails.finished,
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


    /** Popup and menu control **/
    private val _showRatingPop = mutableStateOf(false)
    val showRatingPop: State<Boolean> = _showRatingPop

    fun onShowRatingPop(show: Boolean) {
        _showRatingPop.value = show
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

    private val _flavoring = MutableStateFlow<List<String>>(emptyList())
    private val flavoring: StateFlow<List<String>> = _flavoring

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
                    _flavoring.value = it.flatMap { it.flavoring }
                        .map {
                            it.flavoringName
                        }.distinct().sorted()
                    _tinContainers.value = it.flatMap { it.tins }
                        .map {
                            it.container
                        }.distinct().sorted()
                }
            }
        }
    }


    /** save to database **/
    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        val validDetails = uiState.brand.isNotBlank() && uiState.blend.isNotBlank()
        val validTins = uiState.tinDetailsList.all {
            it.tinLabel.isNotBlank() &&
                    tinDetailsList.map { it.tinLabel }.distinct().size == tinDetailsList.size &&
                    tinDetailsList.all {
                        (it.tinQuantityString.isNotBlank() && it.unit.isNotBlank()) ||
                                it.tinQuantityString.isBlank() }
        }

        tabErrorState = tabErrorState.copy(
            detailsError = !validDetails,
            tinsError = !validTins
        )

        return validDetails && validTins

    }

    suspend fun saveItem() {
        if (validateInput()) {
            val components = componentList.toComponents(components.value)
            val flavoring = flavoringList.toFlavoring(flavoring.value)

            val savedItemId = itemsRepository.insertItem(itemUiState.itemDetails.toItem())

            preferencesRepo.setItemSyncState(savedItemId.toInt(), itemUiState.itemDetails.isSynced)

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
    val rating: Double? = null,
    val notes: String = "",
    var originalBrand: String = "",
    var originalBlend: String = "",
    val subGenre: String = "",
    val cut: String = "",
    val inProduction: Boolean = true,
    val isSynced: Boolean = false,
    val syncedQuantity: Int = 0,

    val tinDetailsList: List<TinDetails> = listOf(TinDetails()),
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
    val finished: Boolean = false,
    var manufactureDateShort: String = "",
    var cellarDateShort: String = "",
    var openDateShort: String = "",
    var manufactureDateLong: String = "",
    var cellarDateLong: String = "",
    var openDateLong: String = "",
    var detailsExpanded: Boolean = true,
    var labelIsNotValid: Boolean = false
)

data class ComponentList(
    val componentString: String = "",
    val autoComps: List<String> = listOf(),
)

data class FlavoringList(
    val flavoringString: String = "",
    val autoFlavors: List<String> = listOf(),
)

data class TinConversion(
    val amount: String = "",
    val unit: String = "",
    val ozRate: Double = 1.75,
    val gramsRate: Double = 50.0,
    val isConversionValid: Boolean = false,
)

data class TabErrorState(
    var detailsError: Boolean = false,
    var tinsError: Boolean = false,
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
    rating = rating,
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
    finished = finished
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

fun FlavoringList.toFlavoring(existingFlavors: List<String>): List<Flavoring> {
    return flavoringString
        .replace(Regex("\\s+"), " ")
        .split(",")
        .filter { it.isNotBlank() }.map { enteredFlavor ->
            val normalizedFlavor = enteredFlavor.trim().lowercase()
            val existingFlavor = existingFlavors.find { existingFlavor ->
                existingFlavor.lowercase() == normalizedFlavor
            }
            Flavoring(flavoringName = existingFlavor ?: enteredFlavor.trim())
        }
}


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
    rating = rating,
    quantityString = quantity.toString(),
    notes = notes,
    subGenre = subGenre,
    cut = cut,
    inProduction = inProduction,
)

fun List<Components>.toComponentList(): ComponentList {
    val componentString = this.joinToString(", ") { it.componentName }
    return ComponentList(
        componentString = componentString
    )
}

fun List<Flavoring>.toFlavoringList(): FlavoringList {
    val flavoringString = this.joinToString(", ") { it.flavoringName }
    return FlavoringList(
        flavoringString = flavoringString
    )
}

fun Tins.toTinDetails(): TinDetails {
    val numberFormat = NumberFormat.getInstance(Locale.getDefault())
    val quantityString = if (unit.isNotBlank()) {
        if (tinQuantity == floor(tinQuantity)) {
            val integerFormatter = NumberFormat.getIntegerInstance(Locale.getDefault())
            integerFormatter.format(tinQuantity.toLong())
        } else {
            numberFormat.format(tinQuantity)
        }
    } else {
        ""
    }

    return TinDetails(
        tinId = tinId,
        itemsId = itemsId,
        tinLabel = tinLabel,
        container = container,
        unit = unit,
        tinQuantity = tinQuantity,
        tinQuantityString = quantityString,
        manufactureDate = manufactureDate,
        cellarDate = cellarDate,
        openDate = openDate,
        finished = finished,
        manufactureDateShort = formatShortDate(manufactureDate),
        cellarDateShort = formatShortDate(cellarDate),
        openDateShort = formatShortDate(openDate),
        manufactureDateLong = formatMediumDate(manufactureDate),
        cellarDateLong = formatMediumDate(cellarDate),
        openDateLong = formatMediumDate(openDate),
    )
}


/** Date functions **/
fun formatShortDate(millis: Long?): String {
    return if (millis != null) {
        val instant = Instant.ofEpochMilli(millis)
        val formatter = DateTimeFormatter.ofPattern("MM/yy").withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } else { "" }
}

fun formatMediumDate(millis: Long?): String {
    return if (millis != null) {
        val instant = Instant.ofEpochMilli(millis)
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        formatter.format(localDate)
    } else { "" }
}

fun formatLongDate(millis: Long?): String {
    return if (millis != null) {
        val instant = Instant.ofEpochMilli(millis)
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        formatter.format(localDate)
    } else { "" }
}