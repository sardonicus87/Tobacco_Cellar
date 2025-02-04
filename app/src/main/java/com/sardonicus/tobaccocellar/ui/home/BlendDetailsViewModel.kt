package com.sardonicus.tobaccocellar.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

class BlendDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    private val itemsId: Int = checkNotNull(savedStateHandle[BlendDetailsDestination.itemsIdArg])

    val blendDetails: StateFlow<BlendDetails> =
        itemsRepository.getItemDetailsStream(itemsId)
            .map {
                BlendDetails(
                    details = it,
                    isLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BlendDetails(isLoading = true)
            )

    fun calculateAge(date: Long?): String {
        if (date == null) {
            return ""
        }

        val now = LocalDate.now()
        val then = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        val period = Period.between(then, now)

        val years = period.years
        val months = period.months
        val days = period.days

        val parts = mutableListOf<String>()
        if (years > 0) {
            if (years > 1) {
                parts.add("$years years")
            } else {
                parts.add("$years year")
            }
        }
        if (months > 0) {
            if (months > 1) {
                parts.add("$months months")
            } else {
                parts.add("$months month")
            }
        }
        if (days > 0) {
            if (days > 1) {
                parts.add("$days days")
            } else {
                parts.add("$days day")
            }
        }

        return if (parts.isEmpty()) {
            "less than a day."
        } else {
            parts.joinToString(", ")
        }
    }


}

data class BlendDetails(
    val details: ItemsComponentsAndTins? = null,
    val isLoading: Boolean = false
)