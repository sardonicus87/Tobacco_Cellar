package com.sardonicus.tobaccocellar.ui.home

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.TinExportData
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor

class HomeViewModel(
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

    private val _importantAlertState = MutableStateFlow(ImportantAlertState())
    val importantAlertState = _importantAlertState.asStateFlow()

    private val _allItems = mutableStateOf<List<ItemsComponentsAndTins>>(emptyList())

    private val _isRendered = MutableStateFlow(false)
    fun updateListRendered(rendered: Boolean) { _isRendered.value = rendered }

    init {
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    _resetLoading.value = true
                }
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            supervisorScope {
                launch {
                    filterViewModel.everythingFlow.collect {
                        _allItems.value = it
                    }
                }
                // Table Sorting
                launch {
                    combine(
                        preferencesRepo.sortColumnIndex,
                        preferencesRepo.sortAscending
                    ) { columnIndex, sortAscending ->
                        TableSorting(columnIndex, sortAscending)
                    }.collect {
                        _tableTableSorting.value = it
                    }
                }
                // List Sorting
                launch {
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
                // Ensure Brand and Blend columns never get hidden, unhide them if so
                launch(Dispatchers.IO) {
                    preferencesRepo.tableColumnsHidden.collect {
                        if (it.contains(TableColumn.BRAND.name)) {
                            updateColumnVisibility(TableColumn.BRAND, true)
                        }
                        if (it.contains(TableColumn.BLEND.name)) {
                            updateColumnVisibility(TableColumn.BLEND, true)
                        }
                    }
                }
                // Type/Subgenre visibility, default to Type if an option becomes disabled
                launch(Dispatchers.IO) {
                    combine(
                        preferencesRepo.typeGenreOption,
                        filterViewModel.typesExist,
                        filterViewModel.subgenresExist
                    ) { option, types, subgenres ->
                        val enablement = mapOf(
                            TypeGenreOption.TYPE to (types || !subgenres),
                            TypeGenreOption.SUBGENRE to subgenres,
                            TypeGenreOption.BOTH to (types && subgenres),
                            TypeGenreOption.TYPE_FALLBACK to types,
                            TypeGenreOption.SUB_FALLBACK to subgenres,
                        )
                        val enabled = enablement[option] ?: false
                        if (enabled) option else TypeGenreOption.TYPE
                    }.collectLatest {
                        preferencesRepo.saveTypeGenreOption(it.value)
                    }
                }
                // Important alerts
                launch {
                    preferencesRepo.lastAlertFlow.collect { lastShown ->
                        val unseenAlerts = OneTimeAlerts.alerts
                            .filter { it.id in (lastShown + 1) ..OneTimeAlerts.CURRENT_ALERT_VERSION }
                            .sortedBy { it.id }

                        val alertToDisplay = unseenAlerts.firstOrNull()
                        val isCurrent = alertToDisplay?.id == OneTimeAlerts.CURRENT_ALERT_VERSION

                        // Safety check, should never happen
                        if (lastShown > OneTimeAlerts.CURRENT_ALERT_VERSION) {
                            preferencesRepo.saveAlertShown(OneTimeAlerts.CURRENT_ALERT_VERSION)
                        }

                        _importantAlertState.value = ImportantAlertState(
                            show = (alertToDisplay != null),
                            alertToDisplay = alertToDisplay,
                            isCurrentAlert = isCurrent
                        )
                    }
                }
            }
        }
    }

    val emptyMessage: StateFlow<String> = combine(
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
        filterViewModel.ratingsExist,
        filterViewModel.emptyDatabase,
        filterViewModel.showTins,
        emptyMessage,
        resetLoading,
        _isRendered
    ) { array ->
        val filteredItems = array[0] as List<ItemsComponentsAndTins>
        val filteredTins = array[1] as List<Tins>
        val quantityOption = array[2] as QuantityOption
        val isTableView = array[3] as Boolean
        val tableSorting = array[4] as TableSorting
        val listSorting = array[5] as ListSorting
        val ozRate = array[6] as Double
        val gramsRate = array[7] as Double
        val showRating = array[8] as Boolean
        val typeGenreOption = array[9] as TypeGenreOption
        val typesExist = array[10] as Boolean
        val subgenresExist = array[11] as Boolean
        val ratingsExist = array[12] as Boolean
        val emptyDatabase = array[13] as Boolean
        val showTins = array[14] as Boolean
        val emptyMessage = array[15] as String
        val resetLoading = array[16] as Boolean
        val isRendered = array[17] as Boolean

        val sortQuantity = filteredItems.associate { items ->
            items.items.id to calculateTotalQuantity(items, items.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate)
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

        val formattedQuantities = sortedItems.associate { items ->
            val totalQuantity = calculateTotalQuantity(items, items.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate) // !it.finished &&
            val formattedQuantity = formatQuantity(totalQuantity, quantityOption, items.tins.filter { it in filteredTins }) // !it.finished &&
            items.items.id to formattedQuantity
        }

        val formattedTypeGenre = sortedItems.associate { item ->
            val text = when (typeGenreOption) {
                TypeGenreOption.TYPE -> item.items.type
                TypeGenreOption.SUBGENRE -> item.items.subGenre
                TypeGenreOption.BOTH -> {
                    item.items.type +
                            if (item.items.type.isNotEmpty() && item.items.subGenre.isNotEmpty()) { " - " } else { "" } +
                            item.items.subGenre
                }
                TypeGenreOption.TYPE_FALLBACK -> item.items.type.ifBlank { item.items.subGenre }
                TypeGenreOption.SUB_FALLBACK -> item.items.subGenre.ifBlank { item.items.type }
            }
            item.items.id to text
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

        val dataLoading = if (resetLoading) {
            true
        } else {
            if (!emptyDatabase && sortedItems.isNotEmpty()) {
                !isRendered
            } else {
                if (emptyDatabase) false else emptyMessage.isBlank()
            }
        }

        HomeUiState(
            sortedItems = sortedItems,
            filteredTins = filteredTins,
            emptyMessage = emptyMessage,
            showRating = showRating,
            sortingOptions = sortingOptions,
            typeGenreOption = typeGenreOption,
            formattedTypeGenre = formattedTypeGenre,
            formattedQuantities = formattedQuantities,
            isTableView = isTableView,
            tableSorting = tableSorting,
            listSorting = listSorting,
            emptyDatabase = emptyDatabase,
            showTins = showTins,
            isLoading = dataLoading
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeUiState(isLoading = true)
        )


    /** One-Time Alerts **/
    fun saveAlertSeen(alertId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepo.saveAlertShown(alertId)
        }
    }


    /** Item menu overlay and expand details **/
    private val _itemMenuShown = mutableStateOf(false)
    val itemMenuShown: State<Boolean> = _itemMenuShown

    private val _activeMenuId = mutableStateOf<Int?>(null)
    val activeMenuId: State<Int?> = _activeMenuId

    fun onShowMenu(itemId: Int) {
        _itemMenuShown.value = true
        _activeMenuId.value = itemId
    }

    fun onDismissMenu() {
        _itemMenuShown.value = false
        _activeMenuId.value = null
    }


    /** Sorting and toggle view **/
    fun selectView(isTableView: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
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

        viewModelScope.launch(Dispatchers.IO) {
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

        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepo.saveTableSortingPreferences(
                newTableSorting.columnIndex, newTableSorting.sortAscending
            )
        }
    }

    private val _showColumnMenu = mutableStateOf(false)
    val showColumnMenu: State<Boolean> = _showColumnMenu

    fun showColumnMenuToggle() {
        _showColumnMenu.value = !_showColumnMenu.value
    }

    val tableColumnVisibility: StateFlow<Map<TableColumn, Boolean>> =
        preferencesRepo.tableColumnsHidden.map {
            TableColumn.entries.associateWith { column ->
                column.name !in it
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TableColumn.entries.associateWith { true }
        )

    fun updateColumnVisibility(column: TableColumn, visible: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentHidden = preferencesRepo.tableColumnsHidden.first()
            val newHidden = if (visible) {
                currentHidden - column.name
            } else {
                currentHidden + column.name
            }
            preferencesRepo.saveTableColumnsHidden(newHidden)
        }
    }

    val columnVisibilityEnablement: StateFlow<Map<TableColumn, Boolean>> =
        combine(
            filterViewModel.typesExist,
            filterViewModel.subgenresExist,
            filterViewModel.ratingsExist,
            filterViewModel.favDisExist,
            filterViewModel.notesExist,
        ) {
            val types = it[0]
            val subgenres = it[1]
            val ratings = it[2]
            val favDis = it[3]
            val notes = it[4]

            mapOf(
                TableColumn.BRAND to true,
                TableColumn.BLEND to true,
                TableColumn.TYPE to types,
                TableColumn.SUBGENRE to subgenres,
                TableColumn.RATING to ratings,
                TableColumn.FAV_DIS to favDis,
                TableColumn.NOTE to notes,
                TableColumn.QTY to true
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = mapOf(
                    TableColumn.BRAND to true,
                    TableColumn.BLEND to true,
                    TableColumn.TYPE to true,
                    TableColumn.SUBGENRE to true,
                    TableColumn.RATING to true,
                    TableColumn.FAV_DIS to true,
                    TableColumn.NOTE to true,
                    TableColumn.QTY to true
                )
            )

    // make columns not visible automatically if they become disabled
    init {
        viewModelScope.launch(Dispatchers.IO) {
            columnVisibilityEnablement.collect { visibilityMap ->
                val disabled = visibilityMap.filterValues { !it }.keys
                disabled.forEach {
                    updateColumnVisibility(it, false)
                }
            }
        }
    }


    /** csvExport for TopAppBar **/
    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    fun snackbarShown() { _showSnackbar.value = false }

    override fun onExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating) {
        viewModelScope.launch(Dispatchers.IO) {
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

    override fun onTinsExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating) {
        viewModelScope.launch(Dispatchers.IO) {
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
    val emptyMessage: String = "",
    val showRating: Boolean = false,
    val sortingOptions: List<ListSortOption> = emptyList(),
    val typeGenreOption: TypeGenreOption = TypeGenreOption.TYPE,
    val formattedTypeGenre: Map<Int, String> = emptyMap(),
    val formattedQuantities: Map<Int, String> = emptyMap(),
    val isTableView: Boolean = false,
    val tableSorting: TableSorting = TableSorting(),
    val listSorting: ListSorting = ListSorting(),
    val emptyDatabase: Boolean = false,
    val showTins: Boolean = false,
    val toggleContentDescription: Int =
        if (isTableView) R.string.table_view_toggle else R.string.list_view_toggle,
    val toggleIcon: Int =
        if (isTableView) R.drawable.table_view else R.drawable.list_view,
    val isLoading: Boolean = false
)

@Stable
data class ItemsIconData(
    val icon: Int,
    val color: Color,
    val size: Dp = 17.dp
)


data class ImportantAlertState(
    val show: Boolean = false,
    val alertToDisplay: OneTimeAlert? = null,
    val isCurrentAlert: Boolean = false
)

data class TableSorting(
    val columnIndex: Int = -1,
    val sortAscending: Boolean = true,
    val sortIcon: Int =
        when (columnIndex) {
            4, 7 -> if (sortAscending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (sortAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
)

data class ListSorting(
    val option: ListSortOption = ListSortOption.DEFAULT,
    val listAscending: Boolean = true,
    val listIcon: Int =
        when (option) {
            ListSortOption.RATING, ListSortOption.QUANTITY -> if (listAscending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (listAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
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