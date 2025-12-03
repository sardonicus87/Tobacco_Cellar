package com.sardonicus.tobaccocellar.ui.details

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.items.ItemUpdatedEvent
import com.sardonicus.tobaccocellar.ui.items.formatMediumDate
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val itemsId: Int,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    private val _blendDetails = MutableStateFlow(BlendDetails())
    val blendDetails = _blendDetails.asStateFlow()

    private val _loadingFinished = MutableStateFlow(false)
    val loadingFinished = _loadingFinished.asStateFlow()

    private val _selectionFocused = MutableStateFlow(false)
    val selectionFocused = _selectionFocused.asStateFlow()

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
            _loadingFinished.value = false

            val quantityRemap = when (val quantityOption = preferencesRepo.quantityOption.first()) {
                QuantityOption.TINS -> {
                    val isMetric = isMetricLocale()
                    if (isMetric) QuantityOption.GRAMS else QuantityOption.OUNCES
                }
                else -> quantityOption
            }

            itemsRepository.getItemDetailsStream(itemsId)
                .filterNotNull()
                .collectLatest {
                    val details = BlendDetails(
                        id = it.items.id,
                        brand = it.items.brand,
                        blend = it.items.blend,
                        favDisIcon = if (it.items.favorite) R.drawable.heart_filled_24 else if (it.items.disliked) R.drawable.heartbroken_filled_24 else null,
                        itemDetails = setOfNotNull(
                            buildDetailsString("Type: ", it.items.type.ifBlank { "Unassigned" }),
                            buildDetailsString("Subgenre: ", it.items.subGenre),
                            buildDetailsString("Cut: ", it.items.cut),
                            buildDetailsString("Components: ", it.components.map { it.componentName }.sorted().joinToString(", ")),
                            buildDetailsString("Flavors: ", it.flavoring.map { it.flavoringName }.sorted().joinToString(", ")),
                            buildDetailsString("Production Status: ", if (it.items.inProduction) "in production" else "not in production"),
                            buildDetailsString("No. of Tins: ", it.items.quantity.toString())
                        ),
                        rating = it.items.rating,
                        notes = it.items.notes,
                        tinsDetails = it.tins.associateWith { tin ->
                            buildSet {
                                buildDetailsString("Container: ", tin.container)?.let { add(DetailLine(it)) }
                                buildDetailsString("Quantity: ", if (tin.unit.isNotBlank()) { formatDecimal(tin.tinQuantity) + " ${tin.unit}" } else "")
                                    ?.let { add(DetailLine(it)) }

                                if (tin.manufactureDate != null) {
                                    val primary = buildDetailsString("Manufacture Date: ", formatMediumDate(tin.manufactureDate))!!
                                    val secondary = buildDetailsString("", "(${calculateAge(tin.manufactureDate, " manufacture ")})", 12.sp)
                                    add(DetailLine(primary, secondary))
                                }

                                if (tin.cellarDate != null) {
                                    val primary = buildDetailsString("Cellar Date: ", formatMediumDate(tin.cellarDate))!!
                                    val secondary = buildDetailsString("", "(${calculateAge(tin.cellarDate, " cellar ")})", 12.sp)
                                    add(DetailLine(primary, secondary))
                                }

                                if (tin.openDate != null) {
                                    val primary = buildDetailsString("Open Date: ", formatMediumDate(tin.openDate))!!
                                    val secondary = if (!tin.finished) { buildDetailsString("", "(${calculateAge(tin.openDate, " open ")})", 12.sp) } else { buildDetailsString("", "finished", 12.sp)}
                                    add(DetailLine(primary, secondary))
                                }
                            }
                        },
                        tinsTotal = if (it.tins.any { it.tinQuantity > 0 && !it.finished }) calculateTotal(it.tins.filter { !it.finished }, quantityRemap) else ""
                    )
                    _blendDetails.value = details
                    _loadingFinished.value = true
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

    fun updateFocused(focused: Boolean) {
        _selectionFocused.update { focused }
    }

    fun buildDetailsString(title: String, value: String, fontSize: TextUnit = 14.sp): AnnotatedString? {
        if (value.isBlank()) return null
        val string = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize)) { append(title) }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontSize = fontSize)) { append(value) }
        }
        return string
    }

}

data class BlendDetails(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val favDisIcon: Int? = null,
    val itemDetails: Set<AnnotatedString> = setOf(),
    val rating: Double? = null,
    val notes: String = "",
    val tinsDetails: Map<Tins, Set<DetailLine>?> = emptyMap(),
    val tinsTotal: String = "",
)

data class DetailLine(
    val primary: AnnotatedString,
    val secondary: AnnotatedString? = null
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