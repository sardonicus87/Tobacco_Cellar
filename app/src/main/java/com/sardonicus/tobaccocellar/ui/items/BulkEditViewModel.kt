package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BulkEditViewModel (
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
): ViewModel() {

    /** UI stuff */
    val bulkEditUiState: StateFlow<BulkEditUiState> =
        itemsRepository.getEverythingStream()
            .map {
                BulkEditUiState(
                    items = it,
                    autoGenres = it.map { it.items.subGenre }.distinct().sorted(),
                    autoCuts = it.map { it.items.cut }.distinct().sorted(),
                    autoComps = it.flatMap { it.components }.map { it.componentName }.distinct().sorted(),
                    loading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
                initialValue = BulkEditUiState(loading = true)
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
            ratingSelected = editing.ratingSelected,
            productionSelected = editing.productionSelected,
            syncTinsSelected = editing.syncTinsSelected,

            type = editing.type,
            disliked = editing.disliked,
            favorite = editing.favorite,
            subGenre = editing.subGenre,
            cut = editing.cut,
            compsString = editing.compsString,
            components = editing.components,
            compsAdd = editing.compsAdd,
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
                editingState.cutSelected || editingState.compsSelected || editingState.ratingSelected ||
                editingState.productionSelected || editingState.syncTinsSelected
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

    private fun calculateSyncTins(tins: List<Tins>): Int {
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

            val itemsToUpdate = editingState.selectedItems.map {
                val itemsId = it.items.id
                val existingComps = it.components.map { it.componentName }
                val editComps = editingState.toComps(existingComps)

                val updatedComps = if (editingState.compsSelected) {
                    when (editingState.compsAdd) {
                        true -> {
                            (it.components + editComps.filterNot {
                                it.componentName in existingComps
                            }).distinctBy { it.componentName }
                        }

                        false -> {
                            it.components.filterNot {
                                it.componentName in editComps.map { it.componentName }
                            }
                        }
                    }
                } else {
                    it.components
                }

                val compsToAdd = updatedComps.filter {
                    it.componentName !in existingComps
                }
                val compsToRemove = existingComps.filter {
                    it !in updatedComps.map { it.componentName }
                }

                compsToAdd.forEach {
                    var componentId = itemsRepository.getComponentIdByName(it.componentName)
                    if (componentId == null) {
                        componentId = itemsRepository.insertComponent(it).toInt()
                    }
                    itemsRepository.insertComponentsCrossRef(ItemsComponentsCrossRef(itemsId, componentId))
                }
                compsToRemove.forEach {
                    val componentId = itemsRepository.getComponentIdByName(it)
                    if (componentId != null) {
                        itemsRepository.deleteComponentsCrossRef(itemsId, componentId)
                    }
                }

                it.copy(
                    items = it.items.copy(
                        type = if (editingState.typeSelected) editingState.type else it.items.type,
                        subGenre = if (editingState.genreSelected) editingState.subGenre else it.items.subGenre,
                        cut = if (editingState.cutSelected) editingState.cut else it.items.cut,
                        disliked = if (editingState.ratingSelected) editingState.disliked else it.items.disliked,
                        favorite = if (editingState.ratingSelected) editingState.favorite else it.items.favorite,
                        inProduction = if (editingState.productionSelected) editingState.inProduction else it.items.inProduction,
                    ),
                    components = updatedComps,
                )
            }
            itemsRepository.updateMultipleItems(itemsToUpdate)

            if (editingState.syncTinsSelected) {
                editingState.selectedItems.forEach {
                    preferencesRepo.setItemSyncState(it.items.id, editingState.syncTins)

                    val tins = itemsRepository.getTinsForItemStream(it.items.id).first()
                    val syncedQuantity = calculateSyncTins(tins)
                    if (editingState.syncTins == true) {
                        itemsRepository.updateItem(
                            it.items.copy(
                                quantity = syncedQuantity,
                            )
                        )
                    }
                }
            }

            setLoadingState(false)
            resetEditingState()
            resetTabIndex()
            _showSnackbar.value = true
        }
    }
}


data class BulkEditUiState(
    val loading: Boolean = false,
    val items: List<ItemsComponentsAndTins> = listOf(),
    val autoGenres: List<String> = listOf(),
    val autoCuts: List<String> = listOf(),
    val autoComps: List<String> = listOf(),
)

data class EditingState(
    val selectedItems: List<ItemsComponentsAndTins> = listOf(),
    val id: Int = 0,

    val typeSelected: Boolean = false,
    val genreSelected: Boolean = false,
    val cutSelected: Boolean = false,
    val compsSelected: Boolean = false,
    val ratingSelected: Boolean = false,
    val productionSelected: Boolean = false,
    val syncTinsSelected: Boolean = false,

    var type: String = "",
    var disliked: Boolean = false,
    var favorite: Boolean = false,
    var subGenre: String = "",
    var cut: String = "",
    var compsString: String = "",
    var components: List<Components> = listOf(),
    var compsAdd: Boolean = true,
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