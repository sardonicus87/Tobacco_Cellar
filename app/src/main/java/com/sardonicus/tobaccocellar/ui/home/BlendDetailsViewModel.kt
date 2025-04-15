package com.sardonicus.tobaccocellar.ui.home

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Locale
import kotlin.math.round

class BlendDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    var blendDetails by mutableStateOf(BlendDetails())
        private set

    private val itemsId: Int = checkNotNull(savedStateHandle[BlendDetailsDestination.itemsIdArg])

    init {
        viewModelScope.launch {
            val details = itemsRepository.getItemDetailsStream(itemsId)
                .filterNotNull()
                .first()
                .toBlendDetails()
            val quantityOption = preferencesRepo.quantityOption.first()
            val quantityRemap = when (quantityOption) {
                QuantityOption.TINS -> {
                    val isMetric = isMetricLocale()
                    if (isMetric) QuantityOption.GRAMS else QuantityOption.OUNCES
                }
                else -> quantityOption
            }

            blendDetails = details.copy(
                tinsTotal = calculateTotal(details.tins, quantityRemap)
            )
        }
    }


    private fun calculateTotal(tins: List<Tins>, quantityOption: QuantityOption): String {
        val sum =
            when (quantityOption) {
                QuantityOption.OUNCES -> {
                    tins.sumOf{
                        when (it.unit) {
                            "oz" -> it.tinQuantity.toDouble()
                            "lbs" -> it.tinQuantity.toDouble() * 16
                            "grams" -> it.tinQuantity.toDouble() / 28.3495
                            else -> 0.0
                        }
                    }
                }
                QuantityOption.GRAMS -> {
                    tins.sumOf{
                        when (it.unit) {
                            "oz" -> it.tinQuantity.toDouble() * 28.3495
                            "lbs" -> it.tinQuantity.toDouble() * 453.592
                            "grams" -> it.tinQuantity.toDouble()
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
                            val rounded = round(pounds * 100) / 100
                            val decimal = String.format("%.2f", rounded)
                            when {
                                decimal.endsWith("00") -> {
                                    decimal.substringBefore(".")
                                }

                                decimal.endsWith("0") -> {
                                    decimal.substring(0, decimal.length - 1)
                                }

                                else -> decimal
                            } + " lbs"
                        } else {
                            val rounded = round(sum * 100) / 100
                            val decimal = String.format("%.2f", rounded)
                            when {
                                decimal.endsWith("00") -> {
                                    decimal.substringBefore(".")
                                }

                                decimal.endsWith("0") -> {
                                    decimal.substring(0, decimal.length - 1)
                                }

                                else -> decimal
                            } + " oz"
                        }
                    } else {
                        null
                    }
                }

                QuantityOption.GRAMS -> {
                    if (sum != null) {
                        val rounded = round(sum * 100) / 100
                        val decimal = String.format("%.2f", rounded)
                        when {
                            decimal.endsWith("00") -> {
                                decimal.substringBefore(".")
                            }

                            decimal.endsWith("0") -> {
                                decimal.substring(0, decimal.length - 1)
                            }

                            else -> decimal
                        } + " g"
                    } else {
                        null
                    }
                }

                else -> {
                    null
                }
            }

        return formattedSum?.toString() ?: ""
    }

    private fun isMetricLocale(): Boolean {
        val config: Configuration = Resources.getSystem().configuration
        val locale: Locale = config.locales.get(0) ?: Locale.getDefault()

        return when (locale.country.uppercase()) {
            "US", "LR", "MM" -> false
            else -> true
        }
    }

    fun formatDecimal(number: Double): String {
        val rounded = round(number * 100) / 100
        val formatted = String.format("%.2f", rounded)
        return when {
            formatted.endsWith("00") -> {
                formatted.substringBefore(".")
            }
            formatted.endsWith("0") -> {
                formatted.substring(0, formatted.length - 1)
            }
            else -> formatted
        }
    }

}

data class BlendDetails(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val disliked: Boolean = false,
    val favorite: Boolean = false,
    val notes: String = "",
    val subGenre: String = "",
    val cut: String = "",
    val inProduction: Boolean = true,
    val componentList: String = "",
    val tins: List<Tins> = emptyList(),
    val tinsTotal: String = "",
)

fun ItemsComponentsAndTins.toBlendDetails(): BlendDetails = BlendDetails(
    id = items.id,
    brand = items.brand,
    blend = items.blend,
    type = items.type,
    quantity = items.quantity,
    disliked = items.disliked,
    favorite = items.favorite,
    notes = items.notes,
    subGenre = items.subGenre,
    cut = items.cut,
    inProduction = items.inProduction,
    componentList = components.joinToString(", ") { it.componentName },
    tins = tins,
)

fun calculateAge(date: Long?, field: String): String {
    if (date == null) {
        return ""
    }

    val now = LocalDate.now()
    val then = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    val period = if (then < now) { Period.between(then, now) } else { Period.between(now, then) }

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

    val end = when (field) {
        "manufacture" -> {
            if (then < now) {
                " old"
            } else {
                " until made/available?"
            }
        }
        "cellar" -> {
            if (then < now) {
                " in cellar"
            } else {
                " until adding/available?"
            }
        }
        "open" -> {
            if (then < now) {
                " open"
            } else {
                " until opening?"
            }
        }
        else -> { "" }
    }

    return if (parts.isEmpty()) {
        "less than a day"
    } else {
        parts.joinToString(", ") + end
    }
}