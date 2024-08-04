package com.example.tobaccocellar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import com.example.tobaccocellar.data.PreferencesRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class HomeViewModel(
    itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
): ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    val homeUiState: StateFlow<HomeUiState> =
        combine(
            itemsRepository.getAllItemsStream(),
            preferencesRepo.isTableView,
        ) { items, isTableView -> HomeUiState(items, isTableView) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )


    /** Toggle Cellar View **/
    fun selectView(isTableView: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveViewPreference(isTableView)
        }
    }


}

data class HomeUiState(
    val items: List<Items> = listOf(),
    val isTableView: Boolean = true,
    val toggleContentDescription: Int =
        if (isTableView) R.string.list_view_toggle else R.string.table_view_toggle,
    val toggleIcon: Int =
        if (isTableView) R.drawable.list_view else R.drawable.table_view,
)