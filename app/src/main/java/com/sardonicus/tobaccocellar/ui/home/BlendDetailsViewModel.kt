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
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val itemsId: Int = checkNotNull(savedStateHandle[BlendDetailsDestination.itemsIdArg])

    val blendDetails: StateFlow<BlendDetails> =
        combine(
            itemsRepository.getItemDetailsStream(itemsId),
            preferencesRepo.quantityOption
        ) { item, quantityOption ->
            val quantityRemap = when (quantityOption) {
                QuantityOption.TINS -> {
                    val isMetric = isMetricLocale()
                    if (isMetric) QuantityOption.GRAMS else QuantityOption.OUNCES
                }
                else -> quantityOption
            }

            val tinsTotal = calculateTotal(item!!.tins, quantityRemap)

                BlendDetails(
                    details = item,
                    tinsTotal = tinsTotal,
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

}

data class BlendDetails(
    val details: ItemsComponentsAndTins? = null,
    val tinsTotal: String? = null,
    val isLoading: Boolean = false
)