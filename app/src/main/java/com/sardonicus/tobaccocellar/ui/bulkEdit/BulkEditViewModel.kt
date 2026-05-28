package com.sardonicus.tobaccocellar.ui.bulkEdit

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.flow.SharingStarted
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

    var editingState by mutableStateOf(EditingState())

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
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = BulkEditUiState()
            )


    init {
        viewModelScope.launch {
            filterViewModel.unifiedFilteredItems.collect { filteredItems ->
                val filteredSet = filteredItems.map { it.items.id }.toSet()
                val updatedSelection = editingState.selectedItems.intersect(filteredSet)

                if (updatedSelection.size != editingState.selectedItems.size) {
                    editingState = editingState.copy(selectedItems = updatedSelection)
                }
            }
        }
    }

    private val _tabIndex = MutableStateFlow(0)
    val tabIndex = _tabIndex.asStateFlow()

    fun updateTabIndex(index: Int) { _tabIndex.value = index }

    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    fun snackbarShown() { _showSnackbar.value = false }

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

    fun updateSelection(itemId: Int) {
        val current = editingState.selectedItems
        editingState = editingState.copy(
            selectedItems = if (current.contains(itemId)) current - itemId else current + itemId
        )
    }

    val enableSave by derivedStateOf {
        editingState.selectedItems.isNotEmpty() && (
                editingState.typeSelected || editingState.genreSelected ||
                        editingState.cutSelected || editingState.compsSelected ||
                        editingState.flavorSelected || editingState.favoriteDisSelected ||
                        editingState.productionSelected || editingState.syncTinsSelected ||
                        editingState.ratingSelected
                )
    }

    private fun resetEditingState() { editingState = EditingState() }

    fun resetSelectedItems() { editingState = editingState.copy(selectedItems = emptySet()) }

    fun selectAll() { editingState = editingState.copy(
        selectedItems = bulkEditUiState.value.items.map { it.items.id }.toSet())
    }


    /** helper functions for tin sync */
    private fun calculateSyncTins(allTins: List<Tins>, ozRate: Double, gramsRate: Double): Int {
        val tins = allTins.filter { !it.finished }
        val totalLbsTins = tins.filter { it.unit == "lbs" }.sumOf { (it.tinQuantity * 16) / ozRate }
        val totalOzTins = tins.filter { it.unit == "oz" }.sumOf { it.tinQuantity / ozRate }
        val totalGramsTins = tins.filter { it.unit == "grams" }.sumOf { it.tinQuantity / gramsRate }

        return (totalLbsTins + totalOzTins + totalGramsTins).roundToInt()
    }

    private val _saveIndicator = MutableStateFlow(false)
    val saveIndicator: StateFlow<Boolean> = _saveIndicator.asStateFlow()

    fun setLoadingState(loading: Boolean) { _saveIndicator.value = loading }


    /** Save function */
    fun batchEditSave() {
        viewModelScope.launch {
            setLoadingState(true)
            SyncStateManager.schedulingPaused = true

            val ozRate = preferencesRepo.tinOzConversionRate.first()
            val gramsRate = preferencesRepo.tinGramsConversionRate.first()

            try {
                val lastModified = System.currentTimeMillis()
                val filteredItems = bulkEditUiState.value.items.associateBy { it.items.id }
                val selectedItems = editingState.selectedItems.mapNotNull { filteredItems[it] }

                val resolvedCompIds = mutableMapOf<String, Int>()
                if (editingState.compsSelected) {
                    val existingComps = bulkEditUiState.value.autoComps
                    val compNames = editingState.compsString.split(",")
                        .map { it.trim() }.filter { it.isNotBlank() }.map { input ->
                            existingComps.find { it.equals(input, ignoreCase = true) } ?: input
                        }

                    compNames.forEach { name ->
                        var id = itemsRepository.getComponentIdByName(name)
                        if (id == null && editingState.compsAdd) {
                            id = itemsRepository.insertComponent(Components(componentName = name)).toInt()
                        }
                        if (id != null) resolvedCompIds[name] = id
                    }
                }

                val resolvedFlavorIds = mutableMapOf<String, Int>()
                if (editingState.flavorSelected) {
                    val existingFlavors = bulkEditUiState.value.autoFlavor
                    val flavorNames = editingState.flavorString.split(",")
                        .map { it.trim() }.filter { it.isNotBlank() }.map { input ->
                            existingFlavors.find { it.equals(input, ignoreCase = true) } ?: input
                        }

                    flavorNames.forEach { name ->
                        var id = itemsRepository.getFlavoringIdByName(name)
                        if (id == null && editingState.flavorAdd) {
                            id = itemsRepository.insertFlavoring(Flavoring(flavoringName = name)).toInt()
                        }
                        if (id != null) resolvedFlavorIds[name] = id
                    }
                }

                val itemsToUpdate = selectedItems.map { items ->
                    val itemsId = items.items.id

                    if (editingState.compsSelected) {
                        resolvedCompIds.values.forEach { compId ->
                            val crossRef = ItemsComponentsCrossRef(itemsId, compId)
                            if (editingState.compsAdd) {
                                itemsRepository.insertComponentsCrossRef(crossRef)
                            } else {
                                itemsRepository.deleteComponentsCrossRef(crossRef)
                            }
                        }
                    }

                    if (editingState.flavorSelected) {
                        resolvedFlavorIds.values.forEach { flavorId ->
                            val crossRef = ItemsFlavoringCrossRef(itemsId, flavorId)
                            if (editingState.flavorAdd) {
                                itemsRepository.insertFlavoringCrossRef(crossRef)
                            } else {
                                itemsRepository.deleteFlavoringCrossRef(crossRef)
                            }
                        }
                    }

                    val newQuantity = if (editingState.syncTinsSelected && editingState.syncTins) {
                        calculateSyncTins(items.tins, ozRate, gramsRate)
                    } else {
                        items.items.quantity
                    }


                    items.items.copy(
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
                    )
                }

                itemsRepository.updateMultipleItems(itemsToUpdate)

            } finally {
                SyncStateManager.schedulingPaused = false
            }

            setLoadingState(false)
            itemsRepository.triggerUploadWorker()
            resetEditingState()
            updateTabIndex(0)
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
    val selectedItems: Set<Int> = emptySet(), // List<ItemsComponentsAndTins> = listOf()
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