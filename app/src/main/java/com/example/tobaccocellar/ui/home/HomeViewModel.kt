package com.example.tobaccocellar.ui.home

import android.app.Application
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.CsvHelper
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import com.example.tobaccocellar.data.PreferencesRepo
import com.example.tobaccocellar.ui.FilterViewModel
import com.example.tobaccocellar.ui.interfaces.ExportCsvHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flattenMerge
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

    init {
        Log.d("HomeViewModel", "ViewModel initialized")
    }

    companion object {
        private const val TIMEOUT_MILLIS = 1_000L
    }

    val homeUiState: StateFlow<HomeUiState> =
        combine(
            filterViewModel.selectedBrands,
            filterViewModel.selectedTypes,
            filterViewModel.selectedFavorites,
            filterViewModel.selectedDislikeds,
            filterViewModel.selectedOutOfStock,
            preferencesRepo.isTableView,
        ) { values ->
            val brands = values[0] as List<String>
            val types = values[1] as List<String>
            val favorites = values[2] as Boolean
            val dislikeds = values[3] as Boolean
            val outOfStock = values[4] as Boolean
            val isTableView = values[5] as Boolean

            itemsRepository.getFilteredItems(
                brands = brands,
                types = types,
                favorites = favorites,
                dislikeds = dislikeds,
                outOfStock = outOfStock
            ).map { items ->
                HomeUiState(
                    items = items,
                    isTableView = isTableView
                )
            }
        }
            .flattenMerge()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState(isLoading = true)
            )




    /** Toggle Cellar View **/
    fun selectView(isTableView: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveViewPreference(isTableView)
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
                        Log.d("HomeViewModel", "CSV data exported to URI: $uri")
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
    val items: List<Items> = listOf(),
    val isTableView: Boolean = false,
    val toggleContentDescription: Int =
        if (isTableView) R.string.list_view_toggle else R.string.table_view_toggle,
    val toggleIcon: Int =
        if (isTableView) R.drawable.list_view else R.drawable.table_view,
    val isLoading: Boolean = false
)