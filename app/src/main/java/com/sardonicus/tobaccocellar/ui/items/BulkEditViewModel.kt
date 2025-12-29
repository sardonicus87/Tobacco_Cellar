package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BulkEditViewModel (
    filterViewModel: FilterViewModel,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
): ViewModel() {

    /** UI stuff */
    @Suppress("UNCHECKED_CAST")
    val bulkEditUiState: StateFlow<BulkEditUiState> =
        combine(
            filterViewModel.unifiedFilteredItems,
            filterViewModel.autoComplete,
        ) { items, autoComplete ->

            BulkEditUiState(
                items = items,
                autoGenres = autoComplete.subgenres,
                autoCuts = autoComplete.cuts,
                autoComps = autoComplete.components,
                autoFlavor = autoComplete.flavorings,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
                initialValue = BulkEditUiState()
            )

    var editingState by mutableStateOf(EditingState())
        private set

    var tabIndex by mutableIntStateOf(0)

    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    fun snackbarShown() {
        _showSnackbar.value = false
    }

    fun onValueChange(editing: EditingState) {
        editingState = EditingState(
            selectedItems = editing.selectedItems,
            id = editing.id,

            typeSelected = editing.typeSelected,
            genreSelected = editing.genreSelected,
            cutSelected = editing.cutSelected,
            compsSelected = editing.compsSelected,
            flavorSelected = editing.flavorSelected,
            ratingSelected = editing.ratingSelected,
            favoriteDisSelected = editing.favoriteDisSelected,
            productionSelected = editing.productionSelected,
            syncTinsSelected = editing.syncTinsSelected,

            type = editing.type,
            rating = editing.rating,
            disliked = editing.disliked,
            favorite = editing.favorite,
            subGenre = editing.subGenre,
            cut = editing.cut,
            compsString = editing.compsString,
            components = editing.components,
            compsAdd = editing.compsAdd,
            flavorString = editing.flavorString,
            flavoring = editing.flavoring,
            flavorAdd = editing.flavorAdd,
            inProduction = editing.inProduction,
            syncTins = editing.syncTins,
        )
    }

    fun updateSelection(item: ItemsComponentsAndTins) {
        editingState = editingState.copy(
            selectedItems = editingState.selectedItems.toMutableList().apply {
                if (editingState.selectedItems.contains(item)) {
                    remove(item)
                } else {
                    add(item)
                }
            }
        )
    }

    var anyFieldSelected by mutableStateOf(false)

    fun fieldSelected(): Boolean {
        anyFieldSelected = editingState.typeSelected || editingState.genreSelected ||
                editingState.cutSelected || editingState.compsSelected ||
                editingState.flavorSelected || editingState.favoriteDisSelected ||
                editingState.productionSelected || editingState.syncTinsSelected ||
                editingState.ratingSelected

        return anyFieldSelected
    }

    private fun resetEditingState() {
        editingState = EditingState()
    }

    fun resetSelectedItems() {
        editingState = editingState.copy(
            selectedItems = listOf()
        )
    }

    fun selectAll() {
        editingState = editingState.copy(
            selectedItems = bulkEditUiState.value.items
        )
    }

    private fun resetTabIndex() {
        tabIndex = 0
    }


    /** helper functions for tin sync */
    private var ozRate: Double = 0.0
    private var gramsRate: Double = 0.0

    init {
        viewModelScope.launch {
            ozRate = preferencesRepo.tinOzConversionRate.first()
            gramsRate = preferencesRepo.tinGramsConversionRate.first()
        }
    }

    private fun calculateSyncTins(allTins: List<Tins>): Int {
        val tins = allTins.filter { !it.finished }
        val totalLbsTins = tins.filter { it.unit == "lbs" }.sumOf {
            (it.tinQuantity * 16) / ozRate
        }
        val totalOzTins = tins.filter { it.unit == "oz" }.sumOf {
            it.tinQuantity / ozRate
        }
        val totalGramsTins = tins.filter { it.unit == "grams" }.sumOf {
            it.tinQuantity / gramsRate
        }
        return (totalLbsTins + totalOzTins + totalGramsTins).roundToInt()
    }

    private val _saveIndicator = MutableStateFlow(false)
    val saveIndicator: StateFlow<Boolean> = _saveIndicator.asStateFlow()

    fun setLoadingState(loading: Boolean) {
        _saveIndicator.value = loading
    }


    /** Save function */
    fun batchEditSave() {
        viewModelScope.launch {
            setLoadingState(true)

            SyncStateManager.schedulingPaused = true

            try {
                val lastModified = System.currentTimeMillis()

                val itemsToUpdate = editingState.selectedItems.map { items ->
                    val itemsId = items.items.id

                    val existingComps = items.components.map { it.componentName }
                    val editComps = editingState.toComps(existingComps)
                    val updatedComps = if (editingState.compsSelected) {
                        when (editingState.compsAdd) {
                            true -> {
                                (items.components + editComps.filterNot {
                                    it.componentName in existingComps
                                }).distinctBy { it.componentName }
                            }

                            false -> {
                                items.components.filterNot { component ->
                                    component.componentName in editComps.map { it.componentName }
                                }
                            }
                        }
                    } else {
                        items.components
                    }
                    val compsToAdd = updatedComps.filter { it.componentName !in existingComps }
                    val compsToRemove =
                        existingComps.filter { component -> component !in updatedComps.map { it.componentName } }

                    val existingFlavor = items.flavoring.map { it.flavoringName }
                    val editFlavor = editingState.toFlavor(existingFlavor)
                    val updatedFlavor = if (editingState.flavorSelected) {
                        when (editingState.flavorAdd) {
                            true -> {
                                (items.flavoring + editFlavor.filterNot {
                                    it.flavoringName in existingFlavor
                                }).distinctBy { it.flavoringName }
                            }

                            false -> {
                                items.flavoring.filterNot { flavoring ->
                                    flavoring.flavoringName in editFlavor.map { it.flavoringName }
                                }
                            }
                        }
                    } else {
                        items.flavoring
                    }
                    val flavorToAdd = updatedFlavor.filter { it.flavoringName !in existingFlavor }
                    val flavorToRemove =
                        existingFlavor.filter { flavoring -> flavoring !in updatedFlavor.map { it.flavoringName } }

                    compsToAdd.forEach {
                        var componentId = itemsRepository.getComponentIdByName(it.componentName)
                        if (componentId == null) {
                            componentId = itemsRepository.insertComponent(it).toInt()
                        }
                        itemsRepository.insertComponentsCrossRef(
                            ItemsComponentsCrossRef(
                                itemsId,
                                componentId
                            )
                        )
                    }
                    compsToRemove.forEach {
                        val componentId = itemsRepository.getComponentIdByName(it)
                        if (componentId != null) {
                            itemsRepository.deleteComponentsCrossRef(
                                ItemsComponentsCrossRef(
                                    itemsId,
                                    componentId
                                )
                            )
                        }
                    }

                    flavorToAdd.forEach {
                        var flavorId = itemsRepository.getFlavoringIdByName(it.flavoringName)
                        if (flavorId == null) {
                            flavorId = itemsRepository.insertFlavoring(it).toInt()
                        }
                        itemsRepository.insertFlavoringCrossRef(
                            ItemsFlavoringCrossRef(
                                itemsId,
                                flavorId
                            )
                        )
                    }
                    flavorToRemove.forEach {
                        val flavorId = itemsRepository.getFlavoringIdByName(it)
                        if (flavorId != null) {
                            itemsRepository.deleteFlavoringCrossRef(
                                ItemsFlavoringCrossRef(
                                    itemsId,
                                    flavorId
                                )
                            )
                        }
                    }

                    val newQuantity = if (editingState.syncTinsSelected && editingState.syncTins) {
                        calculateSyncTins(items.tins)
                    } else {
                        items.items.quantity
                    }


                    items.copy(
                        items = items.items.copy(
                            type = if (editingState.typeSelected) editingState.type else items.items.type,
                            subGenre = if (editingState.genreSelected) editingState.subGenre else items.items.subGenre,
                            cut = if (editingState.cutSelected) editingState.cut else items.items.cut,
                            rating = if (editingState.ratingSelected) editingState.rating else items.items.rating,
                            disliked = if (editingState.favoriteDisSelected) editingState.disliked else items.items.disliked,
                            favorite = if (editingState.favoriteDisSelected) editingState.favorite else items.items.favorite,
                            inProduction = if (editingState.productionSelected) editingState.inProduction else items.items.inProduction,
                            syncTins = if (editingState.syncTinsSelected) editingState.syncTins else items.items.syncTins,
                            quantity = newQuantity,
                            lastModified = lastModified
                        ),
                        components = updatedComps,
                        flavoring = updatedFlavor
                    )
                }

                itemsRepository.updateMultipleItems(itemsToUpdate)

            } finally {
                SyncStateManager.schedulingPaused = false
            }

            setLoadingState(false)
            itemsRepository.triggerUploadWorker()
            resetEditingState()
            resetTabIndex()
            _showSnackbar.value = true
        }
    }
}


data class BulkEditUiState(
    val items: List<ItemsComponentsAndTins> = listOf(),
    val autoGenres: List<String> = listOf(),
    val autoCuts: List<String> = listOf(),
    val autoComps: List<String> = listOf(),
    val autoFlavor: List<String> = listOf(),
)

data class EditingState(
    val selectedItems: List<ItemsComponentsAndTins> = listOf(),
    val id: Int = 0,

    val typeSelected: Boolean = false,
    val ratingSelected: Boolean = false,
    val genreSelected: Boolean = false,
    val cutSelected: Boolean = false,
    val compsSelected: Boolean = false,
    val flavorSelected: Boolean = false,
    val favoriteDisSelected: Boolean = false,
    val productionSelected: Boolean = false,
    val syncTinsSelected: Boolean = false,

    var type: String = "",
    var rating: Double? = null,
    var disliked: Boolean = false,
    var favorite: Boolean = false,
    var subGenre: String = "",
    var cut: String = "",
    var compsString: String = "",
    var components: List<Components> = listOf(),
    var compsAdd: Boolean = true,
    var flavorString: String = "",
    var flavoring: List<Flavoring> = listOf(),
    var flavorAdd: Boolean = true,
    var inProduction: Boolean = true,
    var syncTins: Boolean = false,
)

fun EditingState.toComps(existingComps: List<String>): List<Components> {
    return compsString
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

fun EditingState.toFlavor(existingFlavor: List<String>): List<Flavoring> {
    return flavorString
        .replace(Regex("\\s+"), " ")
        .split(",")
        .filter { it.isNotBlank() }.map { enteredFlavor ->
            val normalizedFlavor = enteredFlavor.trim().lowercase()
            val existingFlavor = existingFlavor.find { existingFlavor ->
                existingFlavor.lowercase() == normalizedFlavor
            }
            Flavoring(flavoringName = existingFlavor ?: enteredFlavor.trim())
        }
}