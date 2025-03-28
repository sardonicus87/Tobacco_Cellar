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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.round

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

    private val _sorting = mutableStateOf(Sorting())
    val sorting: State<Sorting> = _sorting

    private val _refresh = MutableSharedFlow<Unit>(replay = 0)
    private val refresh = _refresh.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesRepo.sortColumnIndex,
                preferencesRepo.sortAscending
            ) { columnIndex, sortAscending ->
                Sorting(columnIndex, sortAscending)
            }.collect {
                _sorting.value = it
            }
        }
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    HomeUiState(isLoading = true)
                    _refresh.emit(Unit)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val everythingFlow: Flow<List<ItemsComponentsAndTins>> =
        refresh.onStart { emit(Unit) }.flatMapLatest {
            itemsRepository.getEverythingStream()
        }

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

    private val _filteredItems = MutableStateFlow<List<ItemsComponentsAndTins>>(listOf())
    val filteredItems: StateFlow<List<ItemsComponentsAndTins>> = _filteredItems.asStateFlow()

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
            filterViewModel.selectedSubgenre.value,
            filterViewModel.selectedCut.value,
            filterViewModel.selectedProduction.value,
            filterViewModel.selectedOutOfProduction.value
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
                filterViewModel.selectedSubgenre,
                filterViewModel.selectedCut,
                filterViewModel.selectedProduction,
                filterViewModel.selectedOutOfProduction,
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
            val matching = filterParams.compMatching
            val subgenres = filterParams.selectedSubgenre
            val cuts = filterParams.selectedCut
            val production = filterParams.selectedProduction
            val outOfProduction = filterParams.selectedOutOfProduction

            val filteredItems =
                if (searchValue.isBlank()) {
                    allItems.filter { items ->
                        val componentMatching = when (matching) {
                            "All" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components)))
                            "Only" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components) && items.components.size == components.size))
                            else -> ((components.isEmpty() && !components.contains("(None Assigned)")) || ((components.contains("(None Assigned)") && items.components.isEmpty()) || (items.components.map { it.componentName }.any { components.contains(it) })))
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
                                ((subgenres.isEmpty() && !subgenres.contains("(Unassigned)")) || ((subgenres.contains("(Unassigned)") && items.items.subGenre.isBlank()) || subgenres.contains(items.items.subGenre))) &&
                                ((cuts.isEmpty() && !cuts.contains("(Unassigned)")) || ((cuts.contains("(Unassigned)") && items.items.cut.isBlank()) || cuts.contains(items.items.cut))) &&
                                (!production || items.items.inProduction) &&
                                (!outOfProduction || !items.items.inProduction)
                    }
                } else {
                    when (searchSetting) {
                         SearchSetting.BLEND -> {
                            allItems.filter {
                                it.items.blend.contains(searchValue, ignoreCase = true)
                            }
                        }
                        SearchSetting.NOTES -> {
                            allItems.filter {
                                it.items.notes.contains(searchValue, ignoreCase = true)
                            }
                        }
                        SearchSetting.CONTAINER -> {
                            allItems.filter {
                                it.tins.any { it.container.contains(searchValue, ignoreCase = true) }
                            }
                        }
                    }
                }

            _filteredItems.value = filteredItems
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


    /** Toggle view **/
    fun selectView(isTableView: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveViewPreference(isTableView)
        }
    }


    /** Toggle Sorting **/
    fun updateSorting(columnIndex: Int) {
        val currentSorting = _sorting.value
        val newSorting =
            if (currentSorting.columnIndex == columnIndex) {
                when {
                    currentSorting.sortAscending -> currentSorting.copy(sortAscending = false)
                    else -> Sorting()
                }
            } else {
                Sorting(columnIndex, true)
            }

        _sorting.value = newSorting
        viewModelScope.launch {
            preferencesRepo.saveTableSortingPreferences(
                newSorting.columnIndex, newSorting.sortAscending
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
                "oz" -> it.tinQuantity.toDouble()
                "lbs" -> it.tinQuantity.toDouble() * 16
                "grams" -> it.tinQuantity.toDouble() / 28.3495
                else -> 0.0
            }
        }
    }

    private fun calculateGrams(tins: List<Tins>): Double {
        return tins.sumOf {
            when (it.unit) {
                "oz" -> it.tinQuantity.toDouble() * 28.3495
                "lbs" -> it.tinQuantity.toDouble() * 453.592
                "grams" -> it.tinQuantity.toDouble()
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

    private fun formatDecimal(number: Double): String {
        val rounded = round(number * 100) / 100
        val formatted = String.format("%.2f", rounded)
        return when {
            formatted.endsWith("00") -> {
                    formatted.substringBefore(".")
            }
            formatted.endsWith("0") -> {
                    formatted.substring(0, formatted.length - 1)
            }
            else -> formatted
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
                val itemsWithComponents = itemsRepository.getAllItemsWithComponents()
                val csvData = csvHelper.exportToCsv(itemsWithComponents)
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

data class Sorting(
    val columnIndex: Int = -1,
    val sortAscending: Boolean = true,
    val sortIcon: Int =
        if (sortAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
)

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
    val selectedSubgenre: List<String>,
    val selectedCut: List<String>,
    val selectedProduction: Boolean,
    val selectedOutOfProduction: Boolean,
)

sealed class SearchSetting(val value: String) {
    data object BLEND: SearchSetting("Blend")
    data object NOTES: SearchSetting("Notes")
    data object CONTAINER: SearchSetting("Container")
}