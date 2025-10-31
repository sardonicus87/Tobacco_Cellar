package com.sardonicus.tobaccocellar.ui.home

import android.content.res.Configuration
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.items.ItemUpdatedEvent
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round

class BlendDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    private val itemsId: Int = checkNotNull(savedStateHandle[BlendDetailsDestination.itemsIdArg])

    private val _blendDetails = MutableStateFlow(BlendDetails())
    val blendDetails = _blendDetails.asStateFlow()

    init {
        refreshData()
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is ItemUpdatedEvent) {
                    refreshData()
                }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            val details = itemsRepository.getItemDetailsStream(itemsId)
                .filterNotNull()
                .first()
                .toBlendDetails()
            val quantityRemap = when (val quantityOption = preferencesRepo.quantityOption.first()) {
                QuantityOption.TINS -> {
                    val isMetric = isMetricLocale()
                    if (isMetric) QuantityOption.GRAMS else QuantityOption.OUNCES
                }
                else -> quantityOption
            }

            _blendDetails.update {
                details.copy(
                    tinsTotal = calculateTotal(details.tins.filter { !it.finished }, quantityRemap)
                )
            }
        }
    }

    private fun calculateTotal(tins: List<Tins>, quantityOption: QuantityOption): String {
        val sum =
            when (quantityOption) {
                QuantityOption.OUNCES -> {
                    tins.sumOf{
                        when (it.unit) {
                            "oz" -> it.tinQuantity
                            "lbs" -> it.tinQuantity * 16
                            "grams" -> it.tinQuantity / 28.3495
                            else -> 0.0
                        }
                    }
                }
                QuantityOption.GRAMS -> {
                    tins.sumOf{
                        when (it.unit) {
                            "oz" -> it.tinQuantity * 28.3495
                            "lbs" -> it.tinQuantity * 453.592
                            "grams" -> it.tinQuantity
                            else -> 0.0
                        }
                    }
                }
                else -> null
            }

        val formattedSum =
            when (quantityOption) {
                QuantityOption.OUNCES -> {
                    if (sum != null) {
                        if (sum >= 16.00) {
                            val pounds = sum / 16
                            formatDecimal(pounds) + " lbs"
                        } else {
                            formatDecimal(sum) + " oz"
                        }
                    } else {
                        null
                    }
                }

                QuantityOption.GRAMS -> {
                    if (sum != null) {
                        formatDecimal(sum) + " g"
                    } else {
                        null
                    }
                }

                else -> {
                    null
                }
            }

        return formattedSum ?: ""
    }

}

data class BlendDetails(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val rating: Double? = null,
    val disliked: Boolean = false,
    val favorite: Boolean = false,
    val notes: String = "",
    val subGenre: String = "",
    val cut: String = "",
    val inProduction: Boolean = true,
    val componentList: String = "",
    val flavoring: String = "",
    val tins: List<Tins> = emptyList(),
    val tinsTotal: String = "",
)

fun ItemsComponentsAndTins.toBlendDetails(): BlendDetails = BlendDetails(
    id = items.id,
    brand = items.brand,
    blend = items.blend,
    type = items.type,
    quantity = items.quantity,
    rating = items.rating,
    disliked = items.disliked,
    favorite = items.favorite,
    notes = items.notes,
    subGenre = items.subGenre,
    cut = items.cut,
    inProduction = items.inProduction,
    componentList = components.joinToString(", ") { it.componentName },
    flavoring = flavoring.joinToString(", ") { it.flavoringName },
    tins = tins,
)

fun calculateAge(date: Long?, field: String): String {
    if (date == null) { return "" }

    val now = LocalDate.now()
    val then = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    val period = if (then < now) { Period.between(then, now) } else { Period.between(now, then) }

    val years = period.years
    val months = period.months
    val days = period.days

    val parts = mutableListOf<String>()

    if (years > 0) {
        parts.add("$years year${if (years > 1) "s" else ""}")
    }
    if (months > 0) {
        parts.add("$months month${if (months > 1) "s" else ""}")
    }
    if (days > 0) {
        parts.add("$days day${if (days > 1) "s" else ""}")
    }

    val end = when (field) {
        "manufacture" -> if (then < now) { " old" } else { " until made/available?" }
        "cellar" -> if (then < now) { " in cellar" } else { " until adding/available?" }
        "open" -> if (then < now) { " open" } else { " until opening" }
        else -> ""
    }

    return if (parts.isEmpty()) { "today" } else { parts.joinToString(", ") + end }
}

fun isMetricLocale(): Boolean {
    val config: Configuration = Resources.getSystem().configuration
    val locale: Locale = config.locales.get(0) ?: Locale.getDefault()

    return when (locale.country.uppercase()) {
        "US", "LR", "MM" -> false
        else -> true
    }
}

fun formatDecimal(number: Double?, places: Int = 2): String {
    if (number == null) return ""

    val multiplier = 10.0.pow(places)
    val rounded = round(number * multiplier) / multiplier
    val formatted = NumberFormat.getNumberInstance(Locale.getDefault())
    formatted.maximumFractionDigits = places
    formatted.minimumFractionDigits = places

    var formattedString = formatted.format(rounded)
    val decimalSeparator = (formatted as? DecimalFormat)?.decimalFormatSymbols?.decimalSeparator ?: '.'
    if (places > 0 && formattedString.contains(decimalSeparator)) {
        while (formattedString.endsWith("0") && formattedString.contains(decimalSeparator)) {
            formattedString = formattedString.dropLast(1)
        }
        if (formattedString.endsWith(decimalSeparator)) {
            formattedString = formattedString.dropLast(1)
        }
    }
    return formattedString
}