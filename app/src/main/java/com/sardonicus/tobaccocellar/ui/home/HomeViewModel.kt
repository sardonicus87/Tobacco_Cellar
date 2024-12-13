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
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.ExportCsvHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    init {
        viewModelScope.launch {
           filterViewModel
        }
    }


    /** States and Flows **/
    val homeUiState: StateFlow<HomeUiState> =
        preferencesRepo.isTableView
            .map { isTableView ->
                HomeUiState(isTableView)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )


    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsState: StateFlow<ItemsState> =
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
            itemsRepository.getAllItemsStream()
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
            val allItems = values[13] as List<Items>

            val filteredItems =
                if (blendSearchValue.isBlank()) {
                    allItems.filter { items ->
                        (brands.isEmpty() || brands.contains(items.brand)) &&
                                (types.isEmpty() || types.contains(items.type)) &&
                                (!unassigned || items.type.isBlank()) &&
                                (!favorites || items.favorite) &&
                                (!dislikeds || items.disliked) &&
                                (!neutral || (!items.favorite && !items.disliked)) &&
                                (!nonNeutral || (items.favorite || items.disliked)) &&
                                (!inStock || items.quantity > 0) &&
                                (!outOfStock || items.quantity == 0) &&
                                (excludedBrands.isEmpty() || !excludedBrands.contains(items.brand)) &&
                                (!excludedLikes || !items.favorite) &&
                                (!excludedDislikes || !items.disliked)
                    }
                } else {
                    allItems.filter { items ->
                        items.blend.contains(blendSearchValue, ignoreCase = true)
                    }
                }
            flow {emit(ItemsState(items = filteredItems))
            }
        }
            .flattenMerge()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ItemsState(isLoading = true)
            )


    private val _sorting = mutableStateOf(Sorting())
    val sorting: State<Sorting> = _sorting

    private val _isMenuShown = mutableStateOf(false)
    val isMenuShown: State<Boolean> = _isMenuShown

    private val _menuItemId = mutableStateOf<Int?>(null)
    val menuItemId: State<Int?> = _menuItemId


    /** List View item menu overlay **/
    fun onShowMenu(itemId: Int) {
        _isMenuShown.value = true
        _menuItemId.value = itemId
    }

    fun onDismissMenu() {
        _isMenuShown.value = false
        _menuItemId.value = null
    }


    /** Toggle Cellar View **/
    fun selectView(isTableView: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveViewPreference(isTableView)
        }
    }


    /** Toggle Sorting **/
    fun updateSorting(columnIndex: Int) {
        if (_sorting.value.columnIndex == columnIndex) {
            when {
                _sorting.value.sortAscending -> _sorting.value =
                    _sorting.value.copy(sortAscending = false)

                else -> _sorting.value = Sorting()
            }
        } else {
            _sorting.value = Sorting(columnIndex, true)
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
                val items = itemsRepository.getAllItemsExport()
                val csvData = csvHelper.exportToCsv(items)
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
}

data class HomeUiState(
//    val items: List<Items> = listOf(),
    val isTableView: Boolean = false,
    val toggleContentDescription: Int =
        if (isTableView) R.string.list_view_toggle else R.string.table_view_toggle,
    val toggleIcon: Int =
        if (isTableView) R.drawable.table_view else R.drawable.list_view,
//    val isLoading: Boolean = false
)

data class ItemsState(
    val items: List<Items> = listOf(),
    val isLoading: Boolean = false
)

data class Sorting(
    val columnIndex: Int = -1,
    val sortAscending: Boolean = true,
)

