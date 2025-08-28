package com.sardonicus.tobaccocellar.ui.home

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeViewModel(
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
    filterViewModel: FilterViewModel,
    private val csvHelper: CsvHelper,
    application: Application
): AndroidViewModel(application), ExportCsvHandler {

    private val _tableTableSorting = mutableStateOf(TableSorting())
    val tableSorting: State<TableSorting> = _tableTableSorting

    private val _quantityOption = MutableStateFlow(QuantityOption.TINS)
    val quantityOption = _quantityOption.asStateFlow()

    private val _resetLoading = MutableStateFlow(false)
    val resetLoading = _resetLoading.asStateFlow()

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
            preferencesRepo.quantityOption.collect {
                _quantityOption.value = it
            }
        }
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    _resetLoading.value = true
                }
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    val homeUiState = combine(
        filterViewModel.homeScreenFilteredItems,
        filterViewModel.homeScreenFilteredTins,
        preferencesRepo.quantityOption,
        preferencesRepo.isTableView,
        snapshotFlow { tableSorting.value },
        preferencesRepo.listSorting,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate,
    ) {
        val filteredItems = it[0] as List<ItemsComponentsAndTins>
        val filteredTins = it[1] as List<Tins>
        val quantityOption = it[2] as QuantityOption
        val isTableView = it[3] as Boolean
        val tableSorting = it[4] as TableSorting
        val listSorting = it[5] as String
        val ozRate = it[6] as Double
        val gramsRate = it[7] as Double

        val sortQuantity = filteredItems.associate {
            it.items.id to calculateTotalQuantity(it, it.tins.filter { !it.finished && it in filteredTins }, quantityOption, ozRate, gramsRate)
        }

        val sortedItems = if (filteredItems.isNotEmpty()) {
            if (isTableView) {
                when (tableSorting.columnIndex) {
                    0 -> filteredItems.sortedBy { it.items.brand }
                    1 -> filteredItems.sortedBy { it.items.blend }
                    2 -> filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                    5 -> filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                    else -> filteredItems
                }.let {
                    if (tableSorting.sortAscending) it else it.reversed()
                }
            } else {
                when (listSorting) {
                    "Default" -> filteredItems.sortedBy { it.items.id }
                    "Blend" -> filteredItems.sortedBy { it.items.blend }
                    "Brand" -> filteredItems.sortedBy { it.items.brand }
                    "Type" -> filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                    "Quantity" -> filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                    else -> filteredItems
                }
            }
        } else emptyList()

        val formattedQuantities = sortedItems.associate {
            val totalQuantity = calculateTotalQuantity(it, it.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate) // !it.finished &&
            val formattedQuantity = formatQuantity(totalQuantity, quantityOption, it.tins.filter { it in filteredTins }) // !it.finished &&
            it.items.id to formattedQuantity
        }

        if (formattedQuantities.isNotEmpty()) { _resetLoading.value = false }

        HomeUiState(
            sortedItems = sortedItems,
            filteredTins = filteredTins,
            formattedQuantities = formattedQuantities,
            isTableView = isTableView,
            tableSorting = tableSorting,
            listSorting = listSorting,
            isLoading = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeUiState(isLoading = true)
        )




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
    val emptyMessage: StateFlow<String> =
        combine(
            filterViewModel.searchValue,
            filterViewModel.searchPerformed,
            filterViewModel.isFilterApplied,
            filterViewModel.emptyDatabase,
            filterViewModel.homeScreenFilteredItems
        ) { searchText, searchPerformed, filteringApplied, emptyDatabase, filteredItems ->
            val emptyList = filteredItems.isEmpty()
            if (!emptyList) { "" }
            else if (searchPerformed) {
                "No entries found matching\n\"$searchText\"."
            }
            else if (filteringApplied) {
                "No entries found matching\nselected filters."
            }
            else if (emptyDatabase) {
                "No entries found in cellar.\nClick \"+\" to add items,\n" +
                        "or use options to import CSV."
            }
            else { "" }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = ""
            )


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
    private fun calculateTotalQuantity(
        items: ItemsComponentsAndTins,
        tins: List<Tins>,
        quantityOption: QuantityOption,
        ounceRate: Double,
        gramRate: Double
    ): Double {
        val tinQuantities = tins.map {
            when (it.finished) {
                true -> 0.0
                false -> it.tinQuantity
            }
        }
        val tinsRemap = tins.mapIndexed { index, it ->
            it.copy(tinQuantity = tinQuantities[index])
        }

        return if (tins.isEmpty() || tins.all { it.unit.isBlank() }) {
            when (quantityOption) {
                QuantityOption.TINS -> items.items.quantity.toDouble()
                QuantityOption.OUNCES -> items.items.quantity.toDouble() * ounceRate
                QuantityOption.GRAMS -> items.items.quantity.toDouble() * gramRate
                else -> 0.0
            }
        } else {
            when (quantityOption) {
                QuantityOption.TINS -> items.items.quantity.toDouble()
                QuantityOption.OUNCES -> calculateOunces(tinsRemap)
                QuantityOption.GRAMS -> calculateGrams(tinsRemap)
                else -> 0.0
            }
        }
    }

    private fun calculateOunces(tins: List<Tins>): Double {
        return tins.sumOf {
            if (it.tinQuantity > 0.0) {
                when (it.unit) {
                    "oz" -> it.tinQuantity
                    "lbs" -> it.tinQuantity * 16
                    "grams" -> it.tinQuantity / 28.3495
                    else -> 0.0
                }
            } else 0.0
        }
    }

    private fun calculateGrams(tins: List<Tins>): Double {
        return tins.sumOf {
            if (it.tinQuantity > 0.0) {
                when (it.unit) {
                    "oz" -> it.tinQuantity * 28.3495
                    "lbs" -> it.tinQuantity * 453.592
                    "grams" -> it.tinQuantity
                    else -> 0.0
                }
            } else 0.0
        }
    }

    private fun formatQuantity(quantity: Double, quantityOption: QuantityOption, tins: List<Tins>): String {
        return when (quantityOption) {
            QuantityOption.TINS -> "x${quantity.toInt()}"
            QuantityOption.OUNCES -> {
                val pounds = quantity / 16
                if (tins.isNotEmpty() && tins.all { it.unit.isNotBlank() }) {
                    if (quantity >= 16) {
                        formatDecimal(pounds) + " lbs"
                    } else {
                        formatDecimal(quantity) + " oz"
                    }
                } else {
                    if (quantity >= 16) {
                       "${formatDecimal(pounds)}* lbs"
                    } else
                        "${formatDecimal(quantity)}* oz"
                }
            }
            QuantityOption.GRAMS -> {
                if (tins.isNotEmpty() && tins.all { it.unit.isNotBlank() }) {
                    formatDecimal(quantity) + " g"
                } else {
                    "${formatDecimal(quantity)}* g"
                }
            }
            else -> { "--" }
        }
    }


    /** csvExport for TopAppBar **/
    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    fun snackbarShown() { _showSnackbar.value = false }

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
    val sortedItems: List<ItemsComponentsAndTins> = emptyList(),
    val filteredTins: List<Tins> = emptyList(),
    val formattedQuantities: Map<Int, String> = emptyMap(),
    val isTableView: Boolean = false,
    val tableSorting: TableSorting = TableSorting(),
    val listSorting: String = ListSorting.DEFAULT.value,
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
        val QUANTITY = ListSorting("Quantity")
    }
}

sealed class SearchSetting(val value: String) {
    data object Blend: SearchSetting("Blend")
    data object Notes: SearchSetting("Notes")
    data object TinLabel: SearchSetting("Tin Label")
}