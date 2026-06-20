package com.sardonicus.tobaccocellar.ui.home

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.BuildConfig
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.TinExportData
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.aboutChangelog.ChangelogEntryData
import com.sardonicus.tobaccocellar.ui.aboutChangelog.changelogEntries
import com.sardonicus.tobaccocellar.ui.addEditItems.ItemUpdatedEvent
import com.sardonicus.tobaccocellar.ui.addEditItems.formatMediumDate
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.settings.ExportRating
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.TypeGenreOption
import com.sardonicus.tobaccocellar.ui.settings.exportRatingString
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.ExportCsvHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(
    private val preferencesRepo: PreferencesRepo,
    private val itemsRepository: ItemsRepository,
    private val filterViewModel: FilterViewModel,
    private val csvHelper: CsvHelper,
    private val application: Application
): ViewModel(), ExportCsvHandler {

    private val _tableTableSorting = MutableStateFlow(TableSorting())
    val tableSorting: StateFlow<TableSorting> = _tableTableSorting.asStateFlow()

    private val _listSorting = MutableStateFlow(ListSorting())
    val listSorting = _listSorting.asStateFlow()

    private val _isTableView = MutableStateFlow(false)
    val isTableView: StateFlow<Boolean> = _isTableView.asStateFlow()

    private val _releaseNotesState = MutableStateFlow(ReleaseNotesState())
    val releaseNotesState = _releaseNotesState.asStateFlow()

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

                // Release notes
                launch{
                    val savedVersion = preferencesRepo.releaseNotesSeen.first()
                    if (savedVersion == null) {
                        saveReleaseNotesSeen()
                    } else {
                        val latestReleaseNotes = changelogEntries
                            .filter { it.versionNumber.isNotBlank() && it.releaseNotes.isNotEmpty() && it.versionCode > savedVersion }
                            .sortedByDescending { it.versionCode }
                            .take(3)

                        if (latestReleaseNotes.isNotEmpty()) {
                            _releaseNotesState.value = ReleaseNotesState(
                                show = true,
                                changelogData = latestReleaseNotes
                            )
                        } else {
                            _releaseNotesState.value = ReleaseNotesState()
                        }
                    }
                }

                // New User (may be used in the future)
                launch {
                    if (preferencesRepo.newUser.first() == null ) { preferencesRepo.updateToExistingUser() }
                }

                // Important alerts
                launch {
                    preferencesRepo.lastAlertFlow.takeWhile { lastShown ->
                        lastShown < OneTimeAlerts.CURRENT_ALERT_VERSION
                    }.collect { lastShown ->
                        val unseenAlerts = OneTimeAlerts.alerts
                            .filter { it.id in (lastShown + 1)..OneTimeAlerts.CURRENT_ALERT_VERSION }
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
                launch {
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
                launch {
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
                delay(50.milliseconds)
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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val viewSelect = isTableView.map { ViewSelect(isTableView = it) }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
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

        val alwaysOptions = listOf(ListSortOption.DEFAULT, ListSortOption.BLEND, ListSortOption.BRAND, ListSortOption.QUANTITY, ListSortOption.EDITED)
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

        ListSortingMenuState(
            isTableView = isTableView,
            sortingOptions = SortingOptionsList(sortingOptions),
            listSorting = listSorting
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListSortingMenuState()
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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MenuState()
        )

    private val quantityState = combine(
        filterViewModel.homeScreenFilteredItems,
        filterViewModel.homeScreenFilteredTins,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate
    ) { filteredItems, filteredTins, quantityOption, ozRate, gramsRate ->
        filteredItems.associate { items ->
            val itemTins = items.tins.filter { it in filteredTins }
            val raw = calculateTotalQuantity(items, itemTins, quantityOption, ozRate, gramsRate)
            val display = formatQuantity(raw, quantityOption, itemTins)

            items.items.id to ItemQuantity(raw, display)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    @Suppress("UNCHECKED_CAST")
    val sortedItems = combine(
        filterViewModel.homeScreenFilteredItems,
        isTableView,
        tableSorting,
        listSorting,
        preferencesRepo.typeGenreOption,
        quantityState
    ) { array ->
        val filteredItems = array[0] as List<ItemsComponentsAndTins>
        val isTableView = array[1] as Boolean
        val tableSorting = array[2] as TableSorting
        val listSorting = array[3] as ListSorting
        val typeGenreOption = array[4] as TypeGenreOption
        val sortState = array[5] as Map<Int, ItemQuantity>

        val sortQuantity = sortState.mapValues { it.value.raw }

        sortItems(filteredItems, isTableView, tableSorting, listSorting, sortQuantity, typeGenreOption)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredTins = filterViewModel.homeScreenFilteredTins.map { TinsList(it) }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TinsList()
        )

    @Suppress("UNCHECKED_CAST")
    val itemsListState: StateFlow<ItemsList> = combine(
        sortedItems,
        quantityState,
        preferencesRepo.showRating,
        preferencesRepo.typeGenreOption,
        filteredTins,
        filterViewModel.showTins,
    ) { array ->
        val items = array[0] as List<ItemsComponentsAndTins>
        val quantities = array[1] as Map<Int, ItemQuantity>
        val showRating = array[2] as Boolean
        val typeOption = array[3] as TypeGenreOption
        val tins = array[4] as TinsList
        val showTins = array[5] as Boolean

        val list = items.map { item ->
            val quantity = quantities[item.items.id]
            val formattedQuantity = quantity?.display ?: "--"
            val outOfStock = quantity?.raw == 0.0
            val filteredTins = if (showTins) item.tins.filter { it in tins.tins } else emptyList()

            val showRating = showRating && item.items.rating != null

            ItemsListState(
                item = item,
                itemId = item.items.id,
                formattedQuantity = formattedQuantity,
                outOfStock = outOfStock,
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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ItemsList()
        )

    val tableLayoutData = combine(
        preferencesRepo.tableColumnsHidden,
        preferencesRepo.typeGenreOption
    ) { tableColumnsHidden, typeGenreOption ->
        val columnVisibility: Map<TableColumn, Boolean> = TableColumn.entries.associateWith { column ->
            column.name !in tableColumnsHidden
        }
        val columnMinWidths = TableColumn.entries.map {
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
                    TableColumn.EDITED -> 108.dp
                }
            } else {
                0.dp
            }
        }
        val totalWidth = columnMinWidths.sumOf { it.value.toDouble() }.dp

        val fallbackType = typeGenreOption == TypeGenreOption.TYPE_FALLBACK && columnVisibility[TableColumn.SUBGENRE] == false
        val fallbackGenre = typeGenreOption == TypeGenreOption.SUB_FALLBACK && columnVisibility[TableColumn.TYPE] == false
        val columnMapping = TableColumn.entries.map { column ->
            when (column) {
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
                TableColumn.EDITED -> { item: Items -> item.lastModified.let { if (it == 0L) "n/a" else formatMediumDate(it, true) } }
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
                8 -> Alignment.Center // edited
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
                    8 -> "Modified"
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
            started = SharingStarted.WhileSubscribed(5000),
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
        quantityState
    ) { array ->
        val sortedItems = array[0] as List<ItemsComponentsAndTins>
        val isTableView = array[1] as Boolean
        val emptyDatabase = array[2] as Boolean
        val emptyMessage = array[3] as String
        val resetLoading = array[4] as Boolean
        val isRendered = array[5] as Boolean
        val qtyState = array[6] as Map<Int, ItemQuantity>

        val formatFinished = emptyDatabase || qtyState.isNotEmpty()

        if (formatFinished) { _resetLoading.value = false }

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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )


    /** Release Notes && One-Time Alerts **/
    fun saveReleaseNotesSeen() {
        viewModelScope.launch(Dispatchers.Default) {
            _releaseNotesState.value = _releaseNotesState.value.copy(show = false)
            preferencesRepo.saveReleaseNotesSeen(BuildConfig.VERSION_CODE)
        }
    }

    fun saveAlertSeen(alertId: Int) {
        viewModelScope.launch(Dispatchers.Default) { preferencesRepo.saveAlertShown(alertId) }
    }


    /** Item menu overlay and expand details **/
    private val _quickEditState = MutableStateFlow(QuickEditItem())
    val quickEditState: StateFlow<QuickEditItem> = _quickEditState.asStateFlow()

    private val _quickChanges = MutableStateFlow(QuickEditChanges())
    val quickChanges: StateFlow<QuickEditChanges> = _quickChanges.asStateFlow()

    private val _originalItem = MutableStateFlow<Items?>(null)
    val originalState: StateFlow<QuickEditItem> = _originalItem.map {
            QuickEditItem(
                rating = it?.rating,
                favorite = it?.favorite ?: false,
                disliked = it?.disliked ?: false,
                notes = it?.notes ?: "",
                quantity = it?.quantity ?: 0,
            )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuickEditItem()
        )


    private var dismissJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                _originalItem,
                _quickEditState,
            ) { original, pending ->
                QuickEditChanges (
                    rating = original?.rating != pending.rating,
                    favorite = original?.favorite != pending.favorite,
                    disliked = original?.disliked != pending.disliked,
                    notes = original?.notes != pending.notes,
                    quantity = original?.quantity != pending.quantity
                )
            }.collect {
                val anythingChanged = it.rating || it.favorite || it.disliked || it.notes || it.quantity
                _quickEditState.value = _quickEditState.value.copy(saveEnabled = anythingChanged)
                _quickChanges.value = it
            }
        }

        viewModelScope.launch {
            sortedItems.collect {
                val ids = it.map { item -> item.items.id }
                if (_activeMenuId.value != null && _activeMenuId.value !in ids) {
                    onDismissMenu()
                }
            }
        }
    }

    fun onShowMenu(itemId: Int) {
        dismissJob?.cancel()
        _itemMenuShown.value = true
        _activeMenuId.value = itemId
    }

    fun onDismissMenu() {
        _itemMenuShown.value = false
        _activeMenuId.value = null
        dismissJob = viewModelScope.launch {
            delay(150.milliseconds)
            setQuickEditItem(null)
        }
    }

    fun setQuickEditItem(itemId: Int?) {
        if (itemId != null) {
            val item = sortedItems.value.first { it.items.id == itemId }
            val active = QuickEditItem(
                rating = item.items.rating,
                favorite = item.items.favorite,
                disliked = item.items.disliked,
                notes = item.items.notes,
                quantity = item.items.quantity,
                syncTins = item.items.syncTins
            )
            _quickEditState.value = active
            _originalItem.value = item.items
        } else {
            _quickEditState.value = QuickEditItem()
            _originalItem.value = null
        }
    }

    fun updateQuickFavorite(fav: Boolean) {
        _quickEditState.value = _quickEditState.value.copy(
            favorite = fav,
            disliked = if (fav) false else _quickEditState.value.disliked
        )
    }

    fun updateQuickDislike(dis: Boolean) {
        _quickEditState.value = _quickEditState.value.copy(
            disliked = dis,
            favorite = if (dis) false else _quickEditState.value.favorite
        )
    }

    fun updateQuickRating(rating: Double?) {
        _quickEditState.value = _quickEditState.value.copy(
            rating = rating
        )
    }

    fun updateQuickNotes(notes: String) {
        _quickEditState.value = _quickEditState.value.copy(
            notes = notes
        )
    }

    fun updateQuickQuantity(quantity: Int) {
        _quickEditState.value = _quickEditState.value.copy(
            quantity = quantity
        )
    }

    fun saveQuickEdits() {
        _activeMenuId.value ?: return
        val pending = _quickEditState.value

        viewModelScope.launch(Dispatchers.Default) {
            val original = _originalItem.value ?: return@launch

            if (pending.saveEnabled) {
                val updatedItem = original.copy(
                    rating = pending.rating,
                    favorite = pending.favorite,
                    disliked = pending.disliked,
                    notes = pending.notes,
                    quantity = pending.quantity,
                    lastModified = System.currentTimeMillis()
                )
                itemsRepository.updateItem(updatedItem)
                EventBus.emit(ItemUpdatedEvent())
            }

            launch(Dispatchers.Main) {
                onDismissMenu()
            }
        }
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
                    0 ->
                        if (tableSorting.sortAscending) filteredItems.sortedBy { it.items.brand }
                        else filteredItems.sortedByDescending { it.items.brand }
                    1 ->
                        if (tableSorting.sortAscending) filteredItems.sortedBy { it.items.blend }
                        else filteredItems.sortedByDescending { it.items.blend }
                    2 ->
                        if (tableSorting.sortAscending) filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                        else filteredItems.sortedByDescending { it.items.type.ifBlank { "~" } }
                    3 ->
                        if (tableSorting.sortAscending) filteredItems.sortedBy { it.items.subGenre.ifBlank { "~" } }
                        else filteredItems.sortedByDescending { it.items.subGenre.ifBlank { "~" } }
                    4 ->
                        if (tableSorting.sortAscending) filteredItems.sortedByDescending { it.items.rating ?: 0.0 }
                        else filteredItems.sortedBy { it.items.rating ?: 10.0 }
                    7 ->
                        if (tableSorting.sortAscending) filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                        else filteredItems.sortedWith(
                            compareBy<ItemsComponentsAndTins> {
                                if (sortQuantity[it.items.id] == 0.0) 1 else 0
                            }.thenBy { sortQuantity[it.items.id] }
                        )
                    8 ->
                        if (tableSorting.sortAscending) filteredItems.sortedByDescending { it.items.lastModified }
                        else filteredItems.sortedBy { it.items.lastModified }
                    else -> filteredItems
                }
            } else {
                when (listSorting.option) {
                    ListSortOption.DEFAULT ->
                        if (listSorting.listAscending) filteredItems.sortedBy { it.items.id }
                        else filteredItems.sortedByDescending { it.items.id }
                    ListSortOption.BLEND ->
                        if (listSorting.listAscending) filteredItems.sortedBy { it.items.blend }
                        else filteredItems.sortedByDescending { it.items.blend }
                    ListSortOption.BRAND ->
                        if (listSorting.listAscending) filteredItems.sortedBy { it.items.brand }
                        else filteredItems.sortedByDescending { it.items.brand }
                    ListSortOption.TYPE ->
                        if (listSorting.listAscending) {
                            if (typeGenreOption == TypeGenreOption.TYPE_FALLBACK) {
                                filteredItems.sortedBy { it.items.type.ifBlank { it.items.subGenre.ifBlank { "~" } } }
                            } else filteredItems.sortedBy { it.items.type.ifBlank { "~" } }
                        }
                        else {
                            if (typeGenreOption == TypeGenreOption.TYPE_FALLBACK) {
                                filteredItems.sortedByDescending { it.items.type.ifBlank { it.items.subGenre.ifBlank { "~" } } }
                            } else filteredItems.sortedByDescending { it.items.type.ifBlank { "~" } }
                        }
                    ListSortOption.SUBGENRE ->
                        if (listSorting.listAscending) {
                            if (typeGenreOption == TypeGenreOption.SUB_FALLBACK) {
                                filteredItems.sortedBy { it.items.subGenre.ifBlank { it.items.type.ifBlank { "~" } } }
                            } else filteredItems.sortedBy { it.items.subGenre.ifBlank { "~" } }
                        }
                        else {
                            if (typeGenreOption == TypeGenreOption.SUB_FALLBACK) {
                                filteredItems.sortedByDescending { it.items.subGenre.ifBlank { it.items.type.ifBlank { "~" } } }
                            } else filteredItems.sortedByDescending { it.items.subGenre.ifBlank { "~" } }
                        }
                    ListSortOption.RATING ->
                        if (listSorting.listAscending) filteredItems.sortedByDescending { it.items.rating ?: 0.0 }
                        else filteredItems.sortedBy { item -> item.items.rating ?: 10.0 }
                    ListSortOption.QUANTITY ->
                        if (listSorting.listAscending) filteredItems.sortedByDescending { sortQuantity[it.items.id] }
                        else filteredItems.sortedWith(
                            compareBy<ItemsComponentsAndTins> { if (sortQuantity[it.items.id] == 0.0) 1 else 0 }
                                .thenBy { sortQuantity[it.items.id] }
                        )
                    ListSortOption.EDITED ->
                        if (listSorting.listAscending) filteredItems.sortedByDescending { it.items.lastModified }
                        else filteredItems.sortedBy { it.items.lastModified }
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
            TypeGenreOption.TYPE_FALLBACK -> item.type.ifBlank { "(${item.subGenre})" }
            TypeGenreOption.SUB_FALLBACK -> item.subGenre.ifBlank { "(${item.type})" }
        }
    }


    /** UI functions **/
    fun selectView() {
        viewModelScope.launch(Dispatchers.Default) {
            preferencesRepo.saveViewPreference(!_isTableView.value)
        }
    }

    fun saveListSorting(value: ListSortOption) {
        val currentSorting = _listSorting.value
        val newListSorting =
            if (currentSorting.option == value) {
                ListSorting(value, !currentSorting.listAscending)
            } else { ListSorting(value) }

        _listSorting.value = newListSorting

        viewModelScope.launch(Dispatchers.Default) {
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
            } else { TableSorting(columnIndex, true) }

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
            TableColumn.entries.associateWith { column -> column.name !in it }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TableColumn.entries.associateWith { true }
        )

    fun updateColumnVisibility(column: TableColumn, visible: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentHidden = preferencesRepo.tableColumnsHidden.first()
            val newHidden = if (visible) currentHidden - column.name else currentHidden + column.name

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
                TableColumn.QTY to true,
                TableColumn.EDITED to true
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = mapOf(
                    TableColumn.BRAND to true,
                    TableColumn.BLEND to true,
                    TableColumn.TYPE to true,
                    TableColumn.SUBGENRE to true,
                    TableColumn.RATING to true,
                    TableColumn.FAV_DIS to true,
                    TableColumn.NOTE to true,
                    TableColumn.QTY to true,
                    TableColumn.EDITED to true
                )
            )

    // make columns not visible automatically if they become disabled
    init {
        viewModelScope.launch(Dispatchers.Default) {
            columnVisibilityEnablement.collect { visibilityMap ->
                val disabled = visibilityMap.filterValues { !it }.keys
                disabled.forEach { updateColumnVisibility(it, false) }
            }
        }
    }


    /** csvExport for TopAppBar **/
    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar.asStateFlow()

    fun snackbarShown() { _showSnackbar.value = false }

    override fun onExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating) {
        viewModelScope.launch(Dispatchers.Default) {
            val data = if (allItems) filterViewModel.everythingFlow.first() else filterViewModel.homeScreenFilteredItems.first()
            val maxRating = exportRating.maxRating
            val rounding = exportRating.rounding

            val csvData = csvHelper.exportToCsv(data, maxRating, rounding)

            if (uri != null) {
                application.contentResolver.openOutputStream(uri)?.use { outputStream ->
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
        viewModelScope.launch(Dispatchers.Default) {
            val maxRating = exportRating.maxRating
            val rounding = exportRating.rounding
            val data: List<TinExportData> = if (allItems) {
                createTinExportData(filterViewModel.everythingFlow.first(), maxRating, rounding)
            } else {
                createTinExportData(filterViewModel.homeScreenFilteredItems.first(), maxRating, rounding)
            }

            val tinCsvData = csvHelper.exportTinsToCsv(data)

            if (uri != null) {
                application.contentResolver.openOutputStream(uri)?.use { outputStream ->
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
data class ItemQuantity(
    val raw: Double,
    val display: String
)

@Stable
data class MenuState(
    val isMenuShown: Boolean = false,
    val activeMenuId: Int? = null
)

@Stable
data class QuickEditItem(
    val rating: Double? = null,
    val favorite: Boolean = false,
    val disliked: Boolean = false,
    val notes: String = "",
    val quantity: Int = 0,
    val syncTins: Boolean = false,
    val saveEnabled: Boolean = false
)

@Stable
data class QuickEditChanges(
    val rating: Boolean = false,
    val favorite: Boolean = false,
    val disliked: Boolean = false,
    val notes: Boolean = false,
    val quantity: Boolean = false
)

@Stable
data class ListSortingMenuState(
    val isTableView: Boolean = false,
    val sortingOptions: SortingOptionsList = SortingOptionsList(),
    val listSorting: ListSorting = ListSorting()
)

@Stable
data class ListSorting(
    val option: ListSortOption = ListSortOption.DEFAULT,
    val listAscending: Boolean = true,
    val listIcon: Int =
        when (option) {
            ListSortOption.RATING, ListSortOption.QUANTITY, ListSortOption.EDITED -> if (listAscending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
            else -> if (listAscending) R.drawable.triangle_arrow_up else R.drawable.triangle_arrow_down
        }
)

@Stable
data class TableSorting(
    val columnIndex: Int = -1,
    val sortAscending: Boolean = true,
    val sortIcon: Int =
        when (columnIndex) {
            4, 7, 8 -> if (sortAscending) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up
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
data class ReleaseNotesState(
    val show: Boolean = false,
    val changelogData: List<ChangelogEntryData> = emptyList()
)

@Stable
data class ImportantAlertState(
    val show: Boolean = false,
    val alertToDisplay: OneTimeAlert? = null,
    val isCurrentAlert: Boolean = false
)

@Immutable
enum class ListSortOption(val value: String) {
    DEFAULT("Default"),
    BLEND("Blend"),
    BRAND("Brand"),
    TYPE("Type"),
    SUBGENRE("Subgenre"),
    RATING("Rating"),
    QUANTITY("Quantity"),
    EDITED("Modified")
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
    if (tins.isEmpty() || tins.all { it.unit.isBlank() }) {
        return when (quantityOption) {
            QuantityOption.TINS -> items.items.quantity.toDouble()
            QuantityOption.OUNCES -> items.items.quantity.toDouble() * ounceRate
            QuantityOption.GRAMS -> items.items.quantity.toDouble() * gramRate
        }
    }

    return when (quantityOption) {
        QuantityOption.TINS -> items.items.quantity.toDouble()
        QuantityOption.OUNCES -> calculateOunces(tins)
        QuantityOption.GRAMS -> calculateGrams(tins)
    }
}

fun calculateOunces(tins: List<Tins>): Double {
    return tins.sumOf {
        if (!it.finished && it.tinQuantity > 0.0) {
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
        if (!it.finished && it.tinQuantity > 0.0) {
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