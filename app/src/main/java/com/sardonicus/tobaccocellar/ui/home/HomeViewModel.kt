package com.sardonicus.tobaccocellar.ui.home

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.Items
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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

    private val _tableTableSorting = MutableStateFlow(TableSorting())
    val tableSorting: StateFlow<TableSorting> = _tableTableSorting.asStateFlow()

    private val _listSorting = MutableStateFlow(ListSorting())
    val listSorting = _listSorting.asStateFlow()

    private val _isTableView = MutableStateFlow(false)
    val isTableView: StateFlow<Boolean> = _isTableView.asStateFlow()

    private val _importantAlertState = MutableStateFlow(ImportantAlertState())
    val importantAlertState = _importantAlertState.asStateFlow()

    private val _resetLoading = MutableStateFlow(false)
    val resetLoading = _resetLoading.asStateFlow()

    private val _itemMenuShown = MutableStateFlow(false)
    val itemMenuShown = _itemMenuShown.asStateFlow()

    private val _activeMenuId = MutableStateFlow<Int?>(null)
    val activeMenuId = _activeMenuId.asStateFlow()

    private val _showColumnMenu = MutableStateFlow(false)
    val showColumnMenu = _showColumnMenu.asStateFlow()

    private val _listShadow = MutableStateFlow(0.dp)
    val listShadow: StateFlow<Dp> = _listShadow.asStateFlow()

    private val _tableShadow = MutableStateFlow(0f)
    val tableShadow: StateFlow<Float> = _tableShadow.asStateFlow()

    fun updateScrollShadow (canScroll: Boolean) {
        _listShadow.value = if (canScroll) 3.dp else 0.dp
        _tableShadow.value = if (canScroll) 0.15f else 0f
    }

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
                // everything flow
                launch {
                    filterViewModel.everythingFlow.collect { _allItems.value = it }
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
                // Table View
                launch {
                    preferencesRepo.isTableView.collect { _isTableView.value = it }
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
                        val enabled = enablement[option] ?: true
                        if (enabled) option else TypeGenreOption.TYPE
                    }.collectLatest {
                        preferencesRepo.saveTypeGenreOption(it.value)
                    }
                }
            }
        }
    }

    private var searchWasPerformed = false
    private var savedSearchText = ""

    @Suppress("UNCHECKED_CAST")
    val emptyMessage: StateFlow<String> = combine(
        filterViewModel.searchValue,
        filterViewModel.searchPerformed,
        filterViewModel.isFilterApplied,
        filterViewModel.emptyDatabase,
        filterViewModel.homeScreenFilteredItems,
        preferencesRepo.searchSetting
    ) { values ->
        val searchText = values[0] as String
        val searchPerformed = values[1] as Boolean
        val filteringApplied = values[2] as Boolean
        val emptyDatabase = values[3] as Boolean
        val filteredItems = values[4] as List<ItemsComponentsAndTins>
        val searchSetting = values[5] as SearchSetting

        val emptyList = filteredItems.isEmpty()

        if (searchPerformed && searchText.isNotBlank()) {
            savedSearchText = searchText
        }

        val emptyMessage =
            if (!emptyList) { "" }
            else if (searchPerformed) {
                "No entries found matching\n\"$savedSearchText\" in ${searchSetting.value}." }
            else if (filteringApplied) {
                "No entries found matching\nselected filters."
            }
            else if (emptyDatabase) {
                "No entries found in cellar.\nClick \"+\" to add items,\n" +
                        "or use options to import CSV." }
            else { "" }

        val displayedMessage =
            if (searchWasPerformed && !searchPerformed) {
                delay(50L)
                emptyMessage
            } else {
                emptyMessage
            }
        searchWasPerformed = searchPerformed

        displayedMessage
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ""
        )

    val itemsCount = filterViewModel.homeScreenFilteredItems.map { it.size }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = 0
        )

    val viewSelect = isTableView.map {
        ViewSelect(
            isTableView = it,
        )
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ViewSelect()
        )

    val listSortingMenuState = combine(
        isTableView,
        preferencesRepo.typeGenreOption,
        filterViewModel.typesExist,
        filterViewModel.subgenresExist,
        filterViewModel.ratingsExist,
        preferencesRepo.showRating,
        listSorting
    ) { values ->
        val isTableView = values[0] as Boolean
        val typeGenreOption = values[1] as TypeGenreOption
        val typesExist = values[2] as Boolean
        val subgenresExist = values[3] as Boolean
        val ratingsExist = values[4] as Boolean
        val showRating = values[5] as Boolean
        val listSorting = values[6] as ListSorting

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

        ListColumnMenuState(
            isTableView = isTableView,
            sortingOptions = SortingOptionsList(sortingOptions),
            listSorting = listSorting
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ListColumnMenuState()
        )

    val menuState = combine(
        itemMenuShown,
        activeMenuId,
    ) { shown, id ->
        MenuState(shown, id)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MenuState()
        )

    val sortQuantity = combine(
        filterViewModel.homeScreenFilteredItems,
        filterViewModel.homeScreenFilteredTins,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate
    ) { filteredItems, filteredTins, quantityOption, ozRate, gramsRate ->
        filteredItems.associate { items ->
            items.items.id to calculateTotalQuantity(items, items.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate)
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyMap()
        )

    val formattedQuantities = combine(
        filterViewModel.homeScreenFilteredItems,
        filterViewModel.homeScreenFilteredTins,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate
    ) { filteredItems, filteredTins, quantityOption, ozRate, gramsRate ->

        filteredItems.associate { items ->
            val totalQuantity =
                calculateTotalQuantity(items, items.tins.filter { it in filteredTins }, quantityOption, ozRate, gramsRate) // !it.finished &&
            val formattedQuantity =
                formatQuantity(totalQuantity, quantityOption, items.tins.filter { it in filteredTins }) // !it.finished &&

            items.items.id to formattedQuantity
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyMap()
        )

    @Suppress("UNCHECKED_CAST")
    val sortedItems = combine(
        filterViewModel.homeScreenFilteredItems,
        isTableView,
        tableSorting,
        listSorting,
        preferencesRepo.typeGenreOption,
        sortQuantity,
        formattedQuantities
    ) { array ->
        val filteredItems = array[0] as List<ItemsComponentsAndTins>
        val isTableView = array[1] as Boolean
        val tableSorting = array[2] as TableSorting
        val listSorting = array[3] as ListSorting
        val typeGenreOption = array[4] as TypeGenreOption
        val sortQuantity = array[5] as Map<Int, Double>

        sortItems(filteredItems, isTableView, tableSorting, listSorting, sortQuantity, typeGenreOption)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val filteredTins = filterViewModel.homeScreenFilteredTins.map { TinsList(it) }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TinsList()
        )

    @Suppress("UNCHECKED_CAST")
    val itemsListState: StateFlow<ItemsList> = combine(
        sortedItems,
        formattedQuantities,
        preferencesRepo.showRating,
        preferencesRepo.typeGenreOption,
        filteredTins,
        filterViewModel.showTins,
    ) { array ->
        val items = array[0] as List<ItemsComponentsAndTins>
        val quantities = array[1] as Map<Int, String>
        val showRating = array[2] as Boolean
        val typeOption = array[3] as TypeGenreOption
        val tins = array[4] as TinsList
        val showTins = array[5] as Boolean

        val list = items.map { item ->
            val quantity = quantities[item.items.id] ?: "--"
            val filteredTins = if (showTins) item.tins.filter { it in tins.tins } else emptyList()

            val showRating = showRating && item.items.rating != null

            ItemsListState(
                item = item,
                itemId = item.items.id,
                formattedQuantity = quantity,
                outOfStock = quantity.none { it in '1'..'9' },
                formattedTypeGenre = calculateTypeGenre(item.items, typeOption),
                tins = TinsList(filteredTins),
                rating = if (showRating) item.items.rating.toString() else "",
            )
        }

        ItemsList(list)
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ItemsList()
        )

    val tableLayoutData = combine(
        preferencesRepo.tableColumnsHidden,
        preferencesRepo.typeGenreOption
    ) { tableColumnsHidden, typeGenreOption ->
        val columnVisibility: Map<TableColumn, Boolean> = TableColumn.entries.associateWith { column ->
            column.name !in tableColumnsHidden
        }
        val columnOrder = listOf(
            TableColumn.BRAND,
            TableColumn.BLEND,
            TableColumn.TYPE,
            TableColumn.SUBGENRE,
            TableColumn.RATING,
            TableColumn.FAV_DIS,
            TableColumn.NOTE,
            TableColumn.QTY
        )
        val columnMinWidths = columnOrder.map {
            val visible = columnVisibility[it] ?: true
            if (visible) {
                when (it) {
                    TableColumn.BRAND -> 180.dp
                    TableColumn.BLEND -> 300.dp
                    TableColumn.TYPE -> 108.dp
                    TableColumn.SUBGENRE -> 120.dp
                    TableColumn.RATING -> 64.dp
                    TableColumn.FAV_DIS -> 64.dp
                    TableColumn.NOTE -> 64.dp
                    TableColumn.QTY -> 98.dp
                }
            } else {
                0.dp
            }
        }
        val totalWidth = columnMinWidths.sumOf { it.value.toDouble() }.dp

        val fallbackType = typeGenreOption == TypeGenreOption.TYPE_FALLBACK && columnVisibility[TableColumn.SUBGENRE] == false
        val fallbackGenre = typeGenreOption == TypeGenreOption.SUB_FALLBACK && columnVisibility[TableColumn.TYPE] == false
        val columnMapping = columnOrder.map {
            when (it) {
                TableColumn.BRAND -> { item: Items -> item.brand }
                TableColumn.BLEND -> { item: Items -> item.blend }
                TableColumn.TYPE -> { item: Items -> item.type.ifBlank { if (fallbackType) "(${item.subGenre})" else "" } }
                TableColumn.SUBGENRE -> { item: Items -> item.subGenre.ifBlank { if (fallbackGenre) "(${item.type})" else "" } }
                TableColumn.RATING -> { item: Items -> item.rating }
                TableColumn.FAV_DIS -> { item: Items ->
                    when {
                        item.favorite -> 1
                        item.disliked -> 2
                        else -> 0
                    }
                }

                TableColumn.NOTE -> { item: Items -> item.notes }
                TableColumn.QTY -> { item: Items -> item.id }
            }
        }
        val alignment = columnMinWidths.indices.map {
            when (it) {
                0 -> Alignment.CenterStart // brand
                1 -> Alignment.CenterStart // blend
                2 -> Alignment.Center // type
                3 -> Alignment.Center // subgenre
                4 -> Alignment.Center // rating
                5 -> Alignment.Center // fav/dis
                6 -> Alignment.Center // notes
                7 -> Alignment.Center // quantity
                else -> Alignment.CenterStart
            }
        }

        val headerText = columnMinWidths.indices.map {
            val width = columnMinWidths[it]
            if (width == 0.dp) "" else {
                when (it) {
                    0 -> "Brand"
                    1 -> "Blend"
                    2 -> "Type"
                    3 -> "Subgenre"
                    4 -> "" // rating
                    5 -> "" // favorite/dislike
                    6 -> "Note"
                    7 -> "Qty"
                    else -> ""
                }
            }
        }

        TableLayoutData(
            columnMinWidths = ColumnWidth(columnMinWidths),
            totalWidth = totalWidth,
            columnMapping = ColumnMapping(columnMapping),
            alignment = ColumnAlignment(alignment),
            headerText = HeaderText(headerText)
        )
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TableLayoutData()
        )


    @Suppress("UNCHECKED_CAST")
    val homeUiState = combine(
        sortedItems,
        isTableView,
        filterViewModel.emptyDatabase,
        emptyMessage,
        resetLoading,
        _isRendered,
        formattedQuantities,
    ) { array ->
        val sortedItems = array[0] as List<ItemsComponentsAndTins>
        val isTableView = array[1] as Boolean
        val emptyDatabase = array[2] as Boolean
        val emptyMessage = array[3] as String
        val resetLoading = array[4] as Boolean
        val isRendered = array[5] as Boolean
        val formattedQuantities = array[6] as Map<Int, String>


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
            isTableView = isTableView,
            emptyDatabase = emptyDatabase,
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
    fun onShowMenu(itemId: Int) {
        _itemMenuShown.value = true
        _activeMenuId.value = itemId
    }

    fun onDismissMenu() {
        _itemMenuShown.value = false
        _activeMenuId.value = null
    }


    /** Helper functions **/
    private fun sortItems(
        filteredItems: List<ItemsComponentsAndTins>,
        isTableView: Boolean,
        tableSorting: TableSorting,
        listSorting: ListSorting,
        sortQuantity: Map<Int, Double>,
        typeGenreOption: TypeGenreOption,
    ): List<ItemsComponentsAndTins> {
        return if (filteredItems.isNotEmpty()) {
            if (isTableView) {
                when (tableSorting.columnIndex) {
                    0 -> filteredItems.sortedBy { it.items.brand }
                    1 -> filteredItems.sortedBy { it.items.blend }
                    2 -> filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                    3 -> filteredItems.sortedBy { it.items.subGenre.ifBlank { "~" } }
                    4 -> filteredItems.sortedByDescending { it.items.rating ?: 0.0 }
                    7 -> filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                    else -> filteredItems
                }.let { sortedList ->
                    if (tableSorting.sortAscending) sortedList
                    else sortedList.let { toReverse ->
                        when (tableSorting.columnIndex) {
                            4 -> toReverse.sortedBy { item -> item.items.rating ?: 10.0 }
                            7 -> {
                                toReverse.sortedWith(
                                    compareBy<ItemsComponentsAndTins> {
                                        if (sortQuantity[it.items.id] == 0.0) 1 else 0
                                    }.thenBy { sortQuantity[it.items.id] }
                                )
                            }
                            else -> toReverse.reversed()
                        }
                    }
                }
            } else {
                when (listSorting.option) {
                    ListSortOption.DEFAULT -> filteredItems.sortedBy { it.items.id }
                    ListSortOption.BLEND -> filteredItems.sortedBy { it.items.blend }
                    ListSortOption.BRAND -> filteredItems.sortedBy { it.items.brand }
                    ListSortOption.TYPE -> {
                        if (typeGenreOption == TypeGenreOption.TYPE_FALLBACK) {
                            filteredItems.sortedBy { it.items.type.ifBlank { it.items.subGenre.ifBlank { "~" } } }
                        }
                        else filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                    }
                    ListSortOption.SUBGENRE -> {
                        if (typeGenreOption == TypeGenreOption.SUB_FALLBACK) {
                            filteredItems.sortedBy { it.items.subGenre.ifBlank { it.items.type.ifBlank { "~" } } }
                        }
                        else filteredItems.sortedBy { it.items.subGenre.ifBlank { "~" } }
                    }
                    ListSortOption.RATING -> filteredItems.sortedByDescending { it.items.rating ?: 0.0 }
                    ListSortOption.QUANTITY -> filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                }.let { sortedList ->
                    if (listSorting.listAscending) sortedList
                    else sortedList.let { toReverse ->
                        when (listSorting.option) {
                            ListSortOption.RATING -> toReverse.sortedBy { item -> item.items.rating ?: 10.0 }
                            ListSortOption.QUANTITY -> {
                                toReverse.sortedWith(
                                    compareBy<ItemsComponentsAndTins> { if (sortQuantity[it.items.id] == 0.0) 1 else 0 }
                                        .thenBy { sortQuantity[it.items.id] }
                                )
                            }
                            else -> toReverse.reversed()
                        }
                    }
                }
            }
        } else emptyList()
    }

    private fun calculateTypeGenre(item: Items, option: TypeGenreOption): String {
        return when (option) {
            TypeGenreOption.TYPE -> item.type
            TypeGenreOption.SUBGENRE -> item.subGenre
            TypeGenreOption.BOTH -> {
                item.type +
                        if (item.type.isNotEmpty() && item.subGenre.isNotEmpty()) { " - " } else { "" } +
                        item.subGenre
            }
            TypeGenreOption.TYPE_FALLBACK -> item.type.ifBlank { item.subGenre }
            TypeGenreOption.SUB_FALLBACK -> item.subGenre.ifBlank { item.type }
        }
    }


    /** UI functions **/
    fun selectView() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepo.saveViewPreference(!_isTableView.value)
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

        viewModelScope.launch {
            preferencesRepo.saveTableSortingPreferences(
                newTableSorting.columnIndex, newTableSorting.sortAscending
            )
        }
    }

    fun showColumnMenuToggle() { _showColumnMenu.value = !_showColumnMenu.value }

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

    val filteredItems = filterViewModel.homeScreenFilteredItems.value

    override fun onExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = if (allItems) _allItems.value else filteredItems
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
                createTinExportData(filteredItems, maxRating, rounding)
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

@Stable
data class ItemsList(val list: List<ItemsListState> = emptyList())

@Stable
data class ItemsListState(
    val item: ItemsComponentsAndTins,
    val itemId: Int,
    val formattedQuantity: String,
    val outOfStock: Boolean,
    val formattedTypeGenre: String,
    val tins: TinsList,
    val rating: String
)

@Stable
data class TinsList(val tins: List<Tins> = emptyList())

@Stable
data class SortingOptionsList(val options: List<ListSortOption> = emptyList())

@Stable
data class HomeUiState(
    val isTableView: Boolean = false,
    val emptyDatabase: Boolean = false,
    val isLoading: Boolean = false
)

@Stable
data class ViewSelect(
    val isTableView: Boolean = false,
    val toggleContentDescription: Int =
        if (isTableView) R.string.table_view_toggle else R.string.list_view_toggle,
    val toggleIcon: Int =
        if (isTableView) R.drawable.table_view else R.drawable.list_view,
)

@Stable
data class ListColumnMenuState(
    val isTableView: Boolean = false,
    val sortingOptions: SortingOptionsList = SortingOptionsList(),
    val listSorting: ListSorting = ListSorting()
)

@Stable
data class MenuState(
    val isMenuShown: Boolean = false,
    val activeMenuId: Int? = null
)

@Stable
data class ImportantAlertState(
    val show: Boolean = false,
    val alertToDisplay: OneTimeAlert? = null,
    val isCurrentAlert: Boolean = false
)

@Stable
data class TableSorting(
    val columnIndex: Int = -1,
    val sortAscending: Boolean = true,
    val sortIcon: Int =
        when (columnIndex) {
            4, 7 -> if (sortAscending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (sortAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
)

@Stable
data class TableLayoutData(
    val columnMinWidths: ColumnWidth = ColumnWidth(),
    val totalWidth: Dp = 0.dp,
    val columnMapping: ColumnMapping = ColumnMapping(),
    val alignment: ColumnAlignment = ColumnAlignment(),
    val headerText: HeaderText = HeaderText()
)

@Stable
data class ColumnWidth(val values: List<Dp> = emptyList())

@Stable
data class ColumnAlignment(val values: List<Alignment> = emptyList())

@Stable
data class ColumnMapping(val values: List<(Items) -> Any?> = emptyList())

@Stable
data class HeaderText(val values: List<String> = emptyList())

@Stable
data class ListSorting(
    val option: ListSortOption = ListSortOption.DEFAULT,
    val listAscending: Boolean = true,
    val listIcon: Int =
        when (option) {
            ListSortOption.RATING, ListSortOption.QUANTITY -> if (listAscending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (listAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
)

@Immutable
enum class ListSortOption(val value: String) {
    DEFAULT("Default"),
    BLEND("Blend"),
    BRAND("Brand"),
    TYPE("Type"),
    SUBGENRE("Subgenre"),
    RATING("Rating"),
    QUANTITY("Quantity")
}

@Immutable
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