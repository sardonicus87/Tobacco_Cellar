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
import com.sardonicus.tobaccocellar.data.TinExportData
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.items.formatMediumDate
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.settings.ExportRating
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.TypeGenreOption
import com.sardonicus.tobaccocellar.ui.settings.exportRatingString
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
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor

class HomeViewModel(
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
    filterViewModel: FilterViewModel,
    private val csvHelper: CsvHelper,
    application: Application
): AndroidViewModel(application), ExportCsvHandler {

    private val _tableTableSorting = mutableStateOf(TableSorting())
    val tableSorting: State<TableSorting> = _tableTableSorting

    private val _listSorting = mutableStateOf(ListSorting())
    val listSorting: State<ListSorting> = _listSorting

    private val _resetLoading = MutableStateFlow(false)
    val resetLoading = _resetLoading.asStateFlow()

    private val _allItems = mutableStateOf<List<ItemsComponentsAndTins>>(emptyList())

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
            combine(
                preferencesRepo.listSorting,
                preferencesRepo.listAscending
            ) { value, ascending ->
                val option = ListSortOption.entries.firstOrNull { it.value == value } ?: ListSortOption.DEFAULT
                ListSorting(option, ascending)
            }.collect {
                _listSorting.value = it
            }
        }
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    _resetLoading.value = true
                }
            }
        }
        viewModelScope.launch {
            filterViewModel.everythingFlow.collect {
                _allItems.value = it
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
        snapshotFlow { listSorting.value },
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate,
        preferencesRepo.showRating,
        preferencesRepo.typeGenreOption,
        filterViewModel.typesExist,
        filterViewModel.subgenresExist,
        filterViewModel.ratingsExist
    ) {
        val filteredItems = it[0] as List<ItemsComponentsAndTins>
        val filteredTins = it[1] as List<Tins>
        val quantityOption = it[2] as QuantityOption
        val isTableView = it[3] as Boolean
        val tableSorting = it[4] as TableSorting
        val listSorting = it[5] as ListSorting
        val ozRate = it[6] as Double
        val gramsRate = it[7] as Double
        val showRating = it[8] as Boolean
        val typeGenreOption = it[9] as TypeGenreOption
        val typesExist = it[10] as Boolean
        val subgenresExist = it[11] as Boolean
        val ratingsExist = it[12] as Boolean


        val sortQuantity = filteredItems.associate {
            it.items.id to calculateTotalQuantity(it, it.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate)
        }

        val sortedItems = if (filteredItems.isNotEmpty()) {
            if (isTableView) {
                when (tableSorting.columnIndex) {
                    0 -> filteredItems.sortedBy { it.items.brand }
                    1 -> filteredItems.sortedBy { it.items.blend }
                    2 -> filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                    3 -> filteredItems.sortedBy { it.items.subGenre.ifBlank { "~" } }
                    4 -> filteredItems.sortedByDescending { it.items.rating ?: 0.0 }
                    7 -> filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                    else -> filteredItems
                }.let {
                    if (tableSorting.sortAscending) it else it.reversed()
                }
            } else {
                when (listSorting.option.value) {
                    "Default" -> filteredItems.sortedBy { it.items.id }
                    "Blend" -> filteredItems.sortedBy { it.items.blend }
                    "Brand" -> filteredItems.sortedBy { it.items.brand }
                    "Type" -> {
                        if (typeGenreOption == TypeGenreOption.TYPE_FALLBACK) {
                            filteredItems.sortedBy { it.items.type.ifBlank { it.items.subGenre.ifBlank { "~" } } }
                        }
                        else filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                    }
                    "Subgenre" -> {
                        if (typeGenreOption == TypeGenreOption.SUB_FALLBACK) {
                            filteredItems.sortedBy { it.items.subGenre.ifBlank { it.items.type.ifBlank { "~" } } }
                        }
                        else filteredItems.sortedBy { it.items.subGenre.ifBlank { "~" } }
                    }
                    "Rating" -> filteredItems.sortedByDescending { it.items.rating ?: 0.0 }
                    "Quantity" -> filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                    else -> filteredItems
                }.let {
                    if (listSorting.listAscending) it else it.reversed()
                }
            }
        } else emptyList()

        val formattedQuantities = sortedItems.associate {
            val totalQuantity = calculateTotalQuantity(it, it.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate) // !it.finished &&
            val formattedQuantity = formatQuantity(totalQuantity, quantityOption, it.tins.filter { it in filteredTins }) // !it.finished &&
            it.items.id to formattedQuantity
        }

        val alwaysOptions = listOf(ListSortOption.DEFAULT, ListSortOption.BLEND, ListSortOption.BRAND, ListSortOption.QUANTITY)
        val subgenreOption = when (typeGenreOption) {
            TypeGenreOption.SUBGENRE -> true
            TypeGenreOption.BOTH -> true
            TypeGenreOption.SUB_FALLBACK -> true
            else -> false
        }
        val sortingOptions = ListSortOption.entries.filter {
            when (it) {
                in alwaysOptions -> true
                ListSortOption.TYPE -> typesExist
                ListSortOption.SUBGENRE -> subgenreOption && subgenresExist
                ListSortOption.RATING -> ratingsExist && showRating
                else -> false
            }
        }

        if (formattedQuantities.isNotEmpty()) { _resetLoading.value = false }

        HomeUiState(
            sortedItems = sortedItems,
            filteredTins = filteredTins,
            showRating = showRating,
            sortingOptions = sortingOptions,
            typeGenreOption = typeGenreOption,
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

    fun saveListSorting(value: ListSortOption) {
        val currentSorting = _listSorting.value
        val newListSorting =
            if (currentSorting.option == value) {
                ListSorting(value, !currentSorting.listAscending)
            } else {
                ListSorting(value)
            }

        _listSorting.value = newListSorting

        viewModelScope.launch {
            preferencesRepo.saveListSorting(newListSorting.option.value, newListSorting.listAscending)
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


    /** csvExport for TopAppBar **/
    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    fun snackbarShown() { _showSnackbar.value = false }

    override fun onExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = if (allItems) _allItems.value else homeUiState.value.sortedItems
                val maxRating = exportRating.maxRating
                val rounding = exportRating.rounding

                val csvData = csvHelper.exportToCsv(data, maxRating, rounding)

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

    override fun onTinsExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val maxRating = exportRating.maxRating
                val rounding = exportRating.rounding
                val data: List<TinExportData> = if (allItems) {
                    createTinExportData(_allItems.value, maxRating, rounding)
                } else {
                    createTinExportData(homeUiState.value.sortedItems, maxRating, rounding)
                }

                val tinCsvData = csvHelper.exportTinsToCsv(data)

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

    private fun createTinExportData(items: List<ItemsComponentsAndTins>, maxRating: Int, rounding: Int): List<TinExportData> {
        val tinExportData = mutableListOf<TinExportData>()
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        val integerFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

        for (item in items) {
            val components = item.components.joinToString(", ") { it.componentName }
            val flavoring = item.flavoring.joinToString(", ") { it.flavoringName }
            val ratingString = exportRatingString(item.items.rating, maxRating, rounding)

            val tins = item.tins


            if (tins.isNotEmpty()) {
                for (tin in tins) {
                    val quantity = if (tin.tinQuantity == floor(tin.tinQuantity)) {
                        integerFormat.format(tin.tinQuantity.toLong())
                    } else {
                        numberFormat.format(tin.tinQuantity)
                    }

                    val tinExport = TinExportData(
                        brand = item.items.brand,
                        blend = item.items.blend,
                        type = item.items.type,
                        subGenre = item.items.subGenre,
                        cut = item.items.cut,
                        components = components,
                        flavoring = flavoring,
                        quantity = item.items.quantity,
                        rating = ratingString,
                        favorite = item.items.favorite,
                        disliked = item.items.disliked,
                        inProduction = item.items.inProduction,
                        notes = item.items.notes,
                        container = tin.container,
                        tinQuantity = if (tin.unit.isNotBlank()) "$quantity ${tin.unit}" else "",
                        manufactureDate = formatMediumDate(tin.manufactureDate),
                        cellarDate = formatMediumDate(tin.cellarDate),
                        openDate = formatMediumDate(tin.openDate),
                        finished = tin.finished
                    )
                    tinExportData.add(tinExport)
                }
            } else {
                val tinExport = TinExportData(
                    brand = item.items.brand,
                    blend = item.items.blend,
                    type = item.items.type,
                    subGenre = item.items.subGenre,
                    cut = item.items.cut,
                    components = components,
                    flavoring = flavoring,
                    quantity = item.items.quantity,
                    rating = ratingString,
                    favorite = item.items.favorite,
                    disliked = item.items.disliked,
                    inProduction = item.items.inProduction,
                    notes = item.items.notes,
                    container = "",
                    tinQuantity = "",
                    manufactureDate = "",
                    cellarDate = "",
                    openDate = "",
                    finished = false
                )
                tinExportData.add(tinExport)
            }
        }
        return tinExportData
    }

}

data class HomeUiState(
    val sortedItems: List<ItemsComponentsAndTins> = emptyList(),
    val filteredTins: List<Tins> = emptyList(),
    val showRating: Boolean = false,
    val sortingOptions: List<ListSortOption> = emptyList(),
    val typeGenreOption: TypeGenreOption = TypeGenreOption.TYPE,
    val formattedQuantities: Map<Int, String> = emptyMap(),
    val isTableView: Boolean = false,
    val tableSorting: TableSorting = TableSorting(),
    val listSorting: ListSorting = ListSorting(),
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
    val option: ListSortOption = ListSortOption.DEFAULT,
    val listAscending: Boolean = true,
    val listIcon: Int =
        if (listAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
)

enum class ListSortOption(val value: String) {
    DEFAULT("Default"),
    BLEND("Blend"),
    BRAND("Brand"),
    TYPE("Type"),
    SUBGENRE("Subgenre"),
    RATING("Rating"),
    QUANTITY("Quantity")
}



sealed class SearchSetting(val value: String) {
    data object Blend: SearchSetting("Blend")
    data object Notes: SearchSetting("Notes")
    data object TinLabel: SearchSetting("Tin Label")
}

/** helper functions for quantity display **/
fun calculateTotalQuantity(
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
        }
    } else {
        when (quantityOption) {
            QuantityOption.TINS -> items.items.quantity.toDouble()
            QuantityOption.OUNCES -> calculateOunces(tinsRemap)
            QuantityOption.GRAMS -> calculateGrams(tinsRemap)
        }
    }
}

fun calculateOunces(tins: List<Tins>): Double {
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

fun calculateGrams(tins: List<Tins>): Double {
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

fun formatQuantity(quantity: Double, quantityOption: QuantityOption, tins: List<Tins>): String {
    return when (quantityOption) {
        QuantityOption.TINS -> "x${quantity.toInt()}"
        QuantityOption.OUNCES -> {
            val pounds = quantity / 16
            if (tins.isNotEmpty() && tins.all { it.unit.isNotBlank() }) {
                if (quantity >= 16) {
                    "${formatDecimal(pounds)} lbs"
                } else {
                    "${formatDecimal(quantity)} oz"
                }
            } else {
                if (quantity >= 16) {
                    "*${formatDecimal(pounds)} lbs"
                } else
                    "*${formatDecimal(quantity)} oz"
            }
        }
        QuantityOption.GRAMS -> {
            if (tins.isNotEmpty() && tins.all { it.unit.isNotBlank() }) {
                "${formatDecimal(quantity)} g"
            } else {
                "*${formatDecimal(quantity)} g"
            }
        }
    }
}