package com.sardonicus.tobaccocellar.ui.home

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.ExportCsvHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeViewModel(
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
    private val filterViewModel: FilterViewModel,
    private val csvHelper: CsvHelper,
    application: Application
): AndroidViewModel(application), ExportCsvHandler {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _tableTableSorting = mutableStateOf(TableSorting())
    val tableSorting: State<TableSorting> = _tableTableSorting

    private val _resetLoading = MutableStateFlow(false)
    val resetLoading = _resetLoading.asStateFlow()

    private val _refresh = MutableSharedFlow<Unit>(replay = 0)
    private val refresh = _refresh.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesRepo.sortColumnIndex,
                preferencesRepo.sortAscending
            ) { columnIndex, sortAscending ->
                TableSorting(columnIndex, sortAscending)
            }.collect {
                _tableTableSorting.value = it
            }
        }
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    _resetLoading.value = true
                    _refresh.emit(Unit)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val everythingFlow: Flow<List<ItemsComponentsAndTins>> =
        refresh.onStart { emit(Unit) }.flatMapLatest {
            itemsRepository.getEverythingStream()
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    /** States and Flows **/
    val homeUiState: StateFlow<HomeUiState> =
        combine(
            preferencesRepo.isTableView,
            preferencesRepo.quantityOption,
            everythingFlow,
        ) { isTableView, quantityOption, allItems ->
            val formattedQuantities = allItems.associate {
                val tins = itemsRepository.getTinsForItemStream(it.items.id).first()
                val totalQuantity = calculateTotalQuantity(it, quantityOption)
                val formattedQuantity = formatQuantity(totalQuantity, quantityOption, tins)
                it.items.id to formattedQuantity
            }

            if (formattedQuantities.isNotEmpty()) { _resetLoading.value = false }

            HomeUiState(
                items = allItems,
                isTableView = isTableView,
                quantityDisplay = quantityOption,
                formattedQuantities = formattedQuantities,
                isLoading = false
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState(isLoading = true)
            )

    // Filtering stuff //
    private val _filteredItems = MutableStateFlow<List<ItemsComponentsAndTins>>(listOf())
    val filteredItems: StateFlow<List<ItemsComponentsAndTins>> = _filteredItems.asStateFlow()

    private val _filteredTins = MutableStateFlow<List<Tins>>(listOf())
    val filteredTins: StateFlow<List<Tins>> = _filteredTins.asStateFlow()

    private fun filterParameters(): FilterParameters {
        return FilterParameters(
            filterViewModel.searchValue.value,
            filterViewModel.selectedBrands.value,
            filterViewModel.selectedTypes.value,
            filterViewModel.selectedUnassigned.value,
            filterViewModel.selectedFavorites.value,
            filterViewModel.selectedDislikeds.value,
            filterViewModel.selectedNeutral.value,
            filterViewModel.selectedNonNeutral.value,
            filterViewModel.selectedInStock.value,
            filterViewModel.selectedOutOfStock.value,
            filterViewModel.selectedExcludeBrands.value,
            filterViewModel.selectedExcludeLikes.value,
            filterViewModel.selectedExcludeDislikes.value,
            filterViewModel.selectedComponent.value,
            filterViewModel.compMatching.value,
            filterViewModel.selectedFlavoring.value,
            filterViewModel.flavorMatching.value,
            filterViewModel.selectedSubgenre.value,
            filterViewModel.selectedCut.value,
            filterViewModel.selectedProduction.value,
            filterViewModel.selectedOutOfProduction.value,
            filterViewModel.selectedHasTins.value,
            filterViewModel.selectedNoTins.value,
            filterViewModel.selectedContainer.value,
            filterViewModel.selectedOpened.value,
            filterViewModel.selectedUnopened.value,
            filterViewModel.selectedFinished.value,
            filterViewModel.selectedUnfinished.value,
        )
    }

    init {
        viewModelScope.launch {
            combine(
                everythingFlow,
                filterViewModel.searchValue,
                filterViewModel.selectedBrands,
                filterViewModel.selectedTypes,
                filterViewModel.selectedUnassigned,
                filterViewModel.selectedFavorites,
                filterViewModel.selectedDislikeds,
                filterViewModel.selectedNeutral,
                filterViewModel.selectedNonNeutral,
                filterViewModel.selectedInStock,
                filterViewModel.selectedOutOfStock,
                filterViewModel.selectedExcludeBrands,
                filterViewModel.selectedExcludeLikes,
                filterViewModel.selectedExcludeDislikes,
                filterViewModel.selectedComponent,
                filterViewModel.compMatching,
                filterViewModel.selectedFlavoring,
                filterViewModel.flavorMatching,
                filterViewModel.selectedSubgenre,
                filterViewModel.selectedCut,
                filterViewModel.selectedProduction,
                filterViewModel.selectedOutOfProduction,
                filterViewModel.selectedHasTins,
                filterViewModel.selectedNoTins,
                filterViewModel.selectedContainer,
                filterViewModel.selectedOpened,
                filterViewModel.selectedUnopened,
                filterViewModel.selectedFinished,
                filterViewModel.selectedUnfinished,
                preferencesRepo.searchSetting
            ) {
                filterItems(everythingFlow.first(), filterParameters(), preferencesRepo.searchSetting.first())
            }.collect { }
        }
    }

    private fun filterItems(allItems: List<ItemsComponentsAndTins>, filterParams: FilterParameters, searchSetting: SearchSetting) {
        viewModelScope.launch{
            val searchValue = filterParams.searchValue
            val brands = filterParams.selectedBrands
            val types = filterParams.selectedTypes
            val unassigned = filterParams.selectedUnassigned
            val favorites = filterParams.selectedFavorites
            val dislikeds = filterParams.selectedDislikeds
            val neutral = filterParams.selectedNeutral
            val nonNeutral = filterParams.selectedNonNeutral
            val inStock = filterParams.selectedInStock
            val outOfStock = filterParams.selectedOutOfStock
            val excludedBrands = filterParams.selectedExcludeBrands
            val excludedLikes = filterParams.selectedExcludeLikes
            val excludedDislikes = filterParams.selectedExcludeDislikes
            val components = filterParams.selectedComponent
            val compMatching = filterParams.compMatching
            val flavoring = filterParams.selectedFlavoring
            val flavorMatching = filterParams.flavorMatching
            val subgenres = filterParams.selectedSubgenre
            val cuts = filterParams.selectedCut
            val production = filterParams.selectedProduction
            val outOfProduction = filterParams.selectedOutOfProduction
            val hasTins = filterParams.selectedHasTins
            val noTins = filterParams.selectedNoTins
            val container = filterParams.selectedContainer
            val opened = filterParams.selectedOpened
            val unopened = filterParams.selectedUnopened
            val finished = filterParams.selectedFinished
            val unfinished = filterParams.selectedUnfinished

            val filteredItems =
                if (searchValue.isBlank()) {
                    allItems.filter { items ->
                        val componentMatching = when (compMatching) {
                            "All" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components)))
                            "Only" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components) && items.components.size == components.size))
                            else -> ((components.isEmpty() && !components.contains("(None Assigned)")) || ((components.contains("(None Assigned)") && items.components.isEmpty()) || (items.components.map { it.componentName }.any { components.contains(it) })))
                        }
                        val flavorMatching = when (flavorMatching) {
                            "All" -> ((flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.containsAll(flavoring)))
                            "Only" -> ((flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.containsAll(flavoring) && items.flavoring.size == flavoring.size))
                            else -> ((flavoring.isEmpty() && !flavoring.contains("(None Assigned)")) || ((flavoring.contains("(None Assigned)") && items.flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.any { flavoring.contains(it) })))
                        }

                        /** ( [filter not selected side] || [filter selected side] ) */
                        (brands.isEmpty() || brands.contains(items.items.brand)) &&
                                ((types.isEmpty() && !unassigned) || (types.contains(items.items.type) || (unassigned && items.items.type.isBlank()))) &&
                                (!favorites || items.items.favorite) &&
                                (!dislikeds || items.items.disliked) &&
                                (!neutral || (!items.items.favorite && !items.items.disliked)) &&
                                (!nonNeutral || (items.items.favorite || items.items.disliked)) &&
                                (!inStock || items.items.quantity > 0) &&
                                (!outOfStock || items.items.quantity == 0) &&
                                (excludedBrands.isEmpty() || !excludedBrands.contains(items.items.brand)) &&
                                (!excludedLikes || !items.items.favorite) &&
                                (!excludedDislikes || !items.items.disliked) &&
                                componentMatching &&
                                flavorMatching &&
                                ((subgenres.isEmpty() && !subgenres.contains("(Unassigned)")) || ((subgenres.contains("(Unassigned)") && items.items.subGenre.isBlank()) || subgenres.contains(items.items.subGenre))) &&
                                ((cuts.isEmpty() && !cuts.contains("(Unassigned)")) || ((cuts.contains("(Unassigned)") && items.items.cut.isBlank()) || cuts.contains(items.items.cut))) &&
                                (!production || items.items.inProduction) &&
                                (!outOfProduction || !items.items.inProduction) &&
                                (!hasTins || items.tins.isNotEmpty()) &&
                                (!noTins || items.tins.isEmpty()) &&
                                ((container.isEmpty() && !container.contains("(Unassigned)")) || ((container.contains("(Unassigned)") && items.tins.any { it.container.isBlank() }) || (items.tins.map { it.container }.any { container.contains(it) }) )) &&
                                (!opened || items.tins.any { it.openDate != null && (it.openDate < System.currentTimeMillis() && !it.finished) }) &&
                                (!unopened || items.tins.any { it.openDate == null || it.openDate > System.currentTimeMillis() }) &&
                                (!finished || items.tins.any { it.finished }) &&
                                (!unfinished || items.tins.any { !it.finished && it.openDate != null && it.openDate < System.currentTimeMillis() })
                    }
                } else {
                    when (searchSetting) {
                         SearchSetting.Blend -> {
                            allItems.filter {
                                it.items.blend.contains(searchValue, ignoreCase = true)
                            }
                        }
                        SearchSetting.Notes -> {
                            allItems.filter {
                                it.items.notes.contains(searchValue, ignoreCase = true)
                            }
                        }
                        SearchSetting.TinLabel -> {
                            allItems.filter {
                                it.tins.any { it.tinLabel.contains(searchValue, ignoreCase = true) }
                            }
                        }
                    }
                }

            val filteredTins =
                if (searchValue.isBlank()) {
                    allItems.flatMap { it.tins }.filter {
                        ((container.isEmpty() && !container.contains("(Unassigned)")) || ((container.contains("(Unassigned)") && it.container.isEmpty()) || container.contains( it.container ))) &&
                                (!opened || (it.openDate != null && (it.openDate < System.currentTimeMillis() && !it.finished))) &&
                                (!unopened || (it.openDate == null || it.openDate > System.currentTimeMillis())) &&
                                (!finished || it.finished) &&
                                (!unfinished || !it.finished && it.openDate != null && it.openDate < System.currentTimeMillis())
                    }
                } else {
                    if (searchSetting == SearchSetting.TinLabel) {
                        allItems.flatMap { it.tins }.filter {
                            it.tinLabel.contains(searchValue, ignoreCase = true)
                        }
                    } else {
                        emptyList()
                    }
                }

            _filteredItems.value = filteredItems
            _filteredTins.value = filteredTins
        }
    }


    /** List View item menu overlay and expand details **/
    private val _isMenuShown = mutableStateOf(false)
    val isMenuShown: State<Boolean> = _isMenuShown

    private val _activeMenuId = mutableStateOf<Int?>(null)
    val activeMenuId: State<Int?> = _activeMenuId

    fun onShowMenu(itemId: Int) {
        _isMenuShown.value = true
        _activeMenuId.value = itemId
    }

    fun onDismissMenu() {
        _isMenuShown.value = false
        _activeMenuId.value = null
    }


    /** Sorting and toggle view **/
    fun selectView(isTableView: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveViewPreference(isTableView)
        }
    }

    fun saveListSorting(value: String) {
        viewModelScope.launch {
            preferencesRepo.saveListSorting(value)
        }
    }

    fun updateSorting(columnIndex: Int) {
        val currentSorting = _tableTableSorting.value
        val newTableSorting =
            if (currentSorting.columnIndex == columnIndex) {
                when {
                    currentSorting.sortAscending -> currentSorting.copy(sortAscending = false)
                    else -> TableSorting()
                }
            } else {
                TableSorting(columnIndex, true)
            }

        _tableTableSorting.value = newTableSorting
        viewModelScope.launch {
            preferencesRepo.saveTableSortingPreferences(
                newTableSorting.columnIndex, newTableSorting.sortAscending
            )
        }
    }


    /** helper functions for quantity display **/
    private suspend fun calculateTotalQuantity(items: ItemsComponentsAndTins, quantityOption: QuantityOption): Double {
        val tins = itemsRepository.getTinsForItemStream(items.items.id).first()
        return if (tins.isEmpty()) {
            when (quantityOption) {
                QuantityOption.TINS -> items.items.quantity.toDouble()
                QuantityOption.OUNCES -> items.items.quantity.toDouble()
                QuantityOption.GRAMS -> items.items.quantity.toDouble()
                else -> 0.0
            }
        } else {
            when (quantityOption) {
                QuantityOption.TINS -> items.items.quantity.toDouble()
                QuantityOption.OUNCES -> calculateOunces(tins)
                QuantityOption.GRAMS -> calculateGrams(tins)
                else -> 0.0
            }
        }
    }

    private fun calculateOunces(tins: List<Tins>): Double {
        return tins.sumOf {
            when (it.unit) {
                "oz" -> it.tinQuantity
                "lbs" -> it.tinQuantity * 16
                "grams" -> it.tinQuantity / 28.3495
                else -> 0.0
            }
        }
    }

    private fun calculateGrams(tins: List<Tins>): Double {
        return tins.sumOf {
            when (it.unit) {
                "oz" -> it.tinQuantity * 28.3495
                "lbs" -> it.tinQuantity * 453.592
                "grams" -> it.tinQuantity
                else -> 0.0
            }
        }
    }

    private fun formatQuantity(quantity: Double, quantityOption: QuantityOption, tins: List<Tins>): String {
        return when (quantityOption) {
            QuantityOption.TINS -> "x${quantity.toInt()}"
            QuantityOption.OUNCES -> {
                if (tins.isNotEmpty()) {
                    if (quantity >= 16) {
                        val pounds = quantity / 16
                        formatDecimal(pounds) + " lbs"
                    } else {
                        formatDecimal(quantity) + " oz"
                    }
                } else {
                    "x${quantity.toInt()}"
                }
            }
            QuantityOption.GRAMS -> {
                if (tins.isNotEmpty()) {
                    formatDecimal(quantity) + " g"
                } else {
                    "x${quantity.toInt()}"
                }
            }
            else -> { "--" }
        }
    }


    /** csvExport for TopAppBar **/
    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    fun snackbarShown() {
        _showSnackbar.value = false
    }

    override fun onExportCsvClick(uri: Uri?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val itemsWithComponentsAndFlavoring = itemsRepository.getAllItemsWithComponentsAndFlavoring()
                val csvData = csvHelper.exportToCsv(itemsWithComponentsAndFlavoring)
                if (uri != null) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                            outputStream ->
                        outputStream.write(csvData.toByteArray())
                        _showSnackbar.value = true
                    }
                } else {
                    val documentsDirectory = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(documentsDirectory, "tobacco_cellar.csv")
                    file.writeText(csvData)
                }
            }
        }
    }

    override fun onTinsExportCsvClick(uri: Uri?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val tinExportData = itemsRepository.getTinExportData()
                val tinCsvData = csvHelper.exportTinsToCsv(tinExportData)
                if (uri != null) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                            outputStream ->
                        outputStream.write(tinCsvData.toByteArray())
                        _showSnackbar.value = true
                    }
                } else {
                    val documentsDirectory = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(documentsDirectory, "tobacco_cellar_as_tins.csv")
                    file.writeText(tinCsvData)
                }
            }
        }
    }

}

data class HomeUiState(
    val items: List<ItemsComponentsAndTins> = listOf(),
    val isTableView: Boolean = false,
    val quantityDisplay: QuantityOption = QuantityOption.TINS,
    val formattedQuantities: Map<Int, String> = mapOf(),
    val toggleContentDescription: Int =
        if (isTableView) R.string.table_view_toggle else R.string.list_view_toggle,
    val toggleIcon: Int =
        if (isTableView) R.drawable.table_view else R.drawable.list_view,
    val isLoading: Boolean = false
)

data class TableSorting(
    val columnIndex: Int = -1,
    val sortAscending: Boolean = true,
    val sortIcon: Int =
        if (sortAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
)

data class ListSorting(
    val value: String = "Default"
) {
    companion object {
        val DEFAULT = ListSorting("Default")
        val BLEND = ListSorting("Blend")
        val BRAND = ListSorting("Brand")
        val TYPE = ListSorting("Type")
    }
}

@Stable
data class FilterParameters(
    val searchValue: String,
    val selectedBrands: List<String>,
    val selectedTypes: List<String>,
    val selectedUnassigned: Boolean,
    val selectedFavorites: Boolean,
    val selectedDislikeds: Boolean,
    val selectedNeutral: Boolean,
    val selectedNonNeutral: Boolean,
    val selectedInStock: Boolean,
    val selectedOutOfStock: Boolean,
    val selectedExcludeBrands: List<String>,
    val selectedExcludeLikes: Boolean,
    val selectedExcludeDislikes: Boolean,
    val selectedComponent: List<String>,
    val compMatching: String,
    val selectedFlavoring: List<String>,
    val flavorMatching: String,
    val selectedSubgenre: List<String>,
    val selectedCut: List<String>,
    val selectedProduction: Boolean,
    val selectedOutOfProduction: Boolean,

    val selectedHasTins: Boolean,
    val selectedNoTins: Boolean,
    val selectedContainer: List<String>,
    val selectedOpened: Boolean,
    val selectedUnopened: Boolean,
    val selectedFinished: Boolean,
    val selectedUnfinished: Boolean,
)

sealed class SearchSetting(val value: String) {
    data object Blend: SearchSetting("Blend")
    data object Notes: SearchSetting("Notes")
    data object TinLabel: SearchSetting("Tin Label")
}