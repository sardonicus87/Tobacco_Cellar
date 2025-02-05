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
import com.sardonicus.tobaccocellar.ui.FilterViewModel
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
    private val filterViewModel: FilterViewModel,
    private val csvHelper: CsvHelper,
    application: Application
): AndroidViewModel(application), ExportCsvHandler {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _sorting = mutableStateOf(Sorting())
    val sorting: State<Sorting> = _sorting

    init {
        viewModelScope.launch {
            filterViewModel

            combine(
                preferencesRepo.sortColumnIndex
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.Lazily,
                        initialValue = -1
                    ),
                preferencesRepo.sortAscending
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.Lazily,
                        initialValue = true
                    )
            ) { columnIndex, sortAscending ->
                Sorting(columnIndex, sortAscending)
            }.collect {
                _sorting.value = it
            }
        }
    }


    /** States and Flows **/
    val homeUiState: StateFlow<HomeUiState> =
        combine(
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
            filterViewModel.blendSearchValue,
            itemsRepository.getEverythingStream(),
            preferencesRepo.isTableView
        ) { values ->
            val brands = values[0] as List<String>
            val types = values[1] as List<String>
            val unassigned = values[2] as Boolean
            val favorites = values[3] as Boolean
            val dislikeds = values[4] as Boolean
            val neutral = values[5] as Boolean
            val nonNeutral = values[6] as Boolean
            val inStock = values[7] as Boolean
            val outOfStock = values[8] as Boolean
            val excludedBrands = values[9] as List<String>
            val excludedLikes = values[10] as Boolean
            val excludedDislikes = values[11] as Boolean
            val blendSearchValue = values[12] as String
            val allItems = values[13] as List<ItemsComponentsAndTins>
            val isTableView = values[14] as Boolean

            val filteredItems =
                if (blendSearchValue.isBlank()) {
                    allItems.filter { items ->
                        (brands.isEmpty() || brands.contains(items.items.brand)) &&
                                (types.isEmpty() || types.contains(items.items.type)) &&
                                (!unassigned || items.items.type.isBlank()) &&
                                (!favorites || items.items.favorite) &&
                                (!dislikeds || items.items.disliked) &&
                                (!neutral || (!items.items.favorite && !items.items.disliked)) &&
                                (!nonNeutral || (items.items.favorite || items.items.disliked)) &&
                                (!inStock || items.items.quantity > 0) &&
                                (!outOfStock || items.items.quantity == 0) &&
                                (excludedBrands.isEmpty() || !excludedBrands.contains(items.items.brand)) &&
                                (!excludedLikes || !items.items.favorite) &&
                                (!excludedDislikes || !items.items.disliked)
                    }
                } else {
                    allItems.filter { items ->
                        items.items.blend.contains(blendSearchValue, ignoreCase = true)
                    }
                }
            HomeUiState(
                items = filteredItems,
                isTableView = isTableView,
                isLoading = false
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
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


    /** Toggle Cellar View **/
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
        if (sortAscending) R.drawable.arrow_up else R.drawable.arrow_down
)
