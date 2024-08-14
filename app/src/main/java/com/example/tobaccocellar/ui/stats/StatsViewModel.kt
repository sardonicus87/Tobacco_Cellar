package com.example.tobaccocellar.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val itemsRepository: ItemsRepository
): ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    val statsUiState: StateFlow<StatsUiState> =
        itemsRepository.getAllItemsStream().map { StatsUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = StatsUiState()
            )
}

data class StatsUiState(val items: List<Items> = listOf())