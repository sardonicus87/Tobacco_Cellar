package com.sardonicus.tobaccocellar.ui.home

import android.app.Application
import android.net.Uri
import android.os.Environment
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    _resetLoading.value = true
                }
            }
        }
    }

    /** States and Flows **/
    val homeUiState: StateFlow<HomeUiState> =
        combine(
            preferencesRepo.isTableView,
            preferencesRepo.quantityOption,
            filterViewModel.everythingFlow,
            filterViewModel.unifiedFilteredItems,
            filterViewModel.unifiedFilteredTins
        ) { isTableView, quantityOption, allItems, filteredItems, filteredTins ->
            var formattedQuantities = mapOf<Int, String>()

            val sortQuantity = filteredItems.associate {
                it.items.id to calculateTotalQuantity(it, it.tins.filter { !it.finished && it in filteredTins }, quantityOption)
            }

            formattedQuantities = filteredItems.associate {
                val totalQuantity = calculateTotalQuantity(it, it.tins.filter { !it.finished && it in filteredTins }, quantityOption)
                val formattedQuantity = formatQuantity(totalQuantity, quantityOption, it.tins.filter { !it.finished && it in filteredTins })
                it.items.id to formattedQuantity
            }

            if (formattedQuantities.isNotEmpty()) { _resetLoading.value = false }

            HomeUiState(
                items = allItems,
                isTableView = isTableView,
                quantityDisplay = quantityOption,
                sortQuantity = sortQuantity,
                formattedQuantities = formattedQuantities,
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
    private suspend fun calculateTotalQuantity(items: ItemsComponentsAndTins, tins: List<Tins>, quantityOption: QuantityOption): Double {
        val ounceRate = preferencesRepo.tinOzConversionRate.first()
        val gramRate = preferencesRepo.tinGramsConversionRate.first()

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
                if (tins.isNotEmpty() && tins.all { it.unit.isNotBlank() }) {
                    if (quantity >= 16) {
                        val pounds = quantity / 16
                        formatDecimal(pounds) + " lbs"
                    } else {
                        formatDecimal(quantity) + " oz"
                    }
                } else {
                    if (quantity >= 16) {
                       val pounds = quantity / 16
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
    val items: List<ItemsComponentsAndTins> = listOf(),
    val isTableView: Boolean = false,
    val quantityDisplay: QuantityOption = QuantityOption.TINS,
    val sortQuantity: Map<Int, Double> = mapOf(),
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
        val QUANTITY = ListSorting("Quantity")
    }
}

sealed class SearchSetting(val value: String) {
    data object Blend: SearchSetting("Blend")
    data object Notes: SearchSetting("Notes")
    data object TinLabel: SearchSetting("Tin Label")
}