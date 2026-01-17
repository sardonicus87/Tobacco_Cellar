package com.sardonicus.tobaccocellar.ui.details

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Locale

class BlendDetailsViewModel(
    private val itemsId: Int,
    private val itemsRepository: ItemsRepository,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    private val _blendDetails = MutableStateFlow(BlendDetails())
    val blendDetails = _blendDetails.asStateFlow()

    private val _loadingFinished = MutableStateFlow(false)
    val loadingFinished = _loadingFinished.asStateFlow()

    private val _selectionKey = MutableStateFlow(0)
    val selectionKey = _selectionKey.asStateFlow()

    private val _selectionFocused = MutableStateFlow(false)
    val selectionFocused = _selectionFocused.asStateFlow()

    private val _parseLinks = MutableStateFlow(false)
    val parseLinks = _parseLinks.asStateFlow()

    init {
        refreshData()
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is ItemUpdatedEvent) {
                    refreshData()
                }
            }
        }
        viewModelScope.launch {
            _parseLinks.value = preferencesRepo.parseLinks.first()
        }
    }

    fun resetSelection() {
        _selectionKey.update { it + 1 }
        updateFocused(false)
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
                .collectLatest { item ->
                    val details = BlendDetails(
                        id = item.items.id,
                        brand = item.items.brand,
                        blend = item.items.blend,
                        favDisIcon = if (item.items.favorite) R.drawable.heart_filled_24 else if (item.items.disliked) R.drawable.heartbroken_filled_24 else null,
                        itemDetails = setOfNotNull(
                            buildDetailsString("Type: ", item.items.type.ifBlank { "Unassigned" }),
                            buildDetailsString("Subgenre: ", item.items.subGenre),
                            buildDetailsString("Cut: ", item.items.cut),
                            buildDetailsString("Components: ", item.components.map { it.componentName }.sorted().joinToString(", ")),
                            buildDetailsString("Flavors: ", item.flavoring.map { it.flavoringName }.sorted().joinToString(", ")),
                            buildDetailsString("Production Status: ", if (item.items.inProduction) "in production" else "not in production"),
                            buildDetailsString("No. of Tins: ", item.items.quantity.toString())
                        ),
                        rating = item.items.rating,
                        notes = item.items.notes,
                        tinsDetails = item.tins.associateWith { tin ->
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
                        tinsTotal = if (item.tins.any { it.tinQuantity > 0 && !it.finished }) calculateTotal(item.tins.filter { !it.finished }, quantityRemap) else ""
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

    fun parseHyperlinks(text: String, color: Color, linkListener: LinkInteractionListener, parseLinks: Boolean): AnnotatedString {
        if (text.isBlank()) return AnnotatedString("")

        if (!parseLinks) return buildAnnotatedString { append(text) }

        val urlRegex = Regex("""(https?://|www\.)[a-zA-Z0-9_./-]*[a-zA-Z0-9_/-]""")
        val annotatedString = buildAnnotatedString {
            val matches = urlRegex.findAll(text)
            var lastIndex = 0

            for (match in matches) {
                val startIndex = match.range.first
                val endIndex = match.range.last + 1

                if (startIndex > lastIndex) {
                    append(text.substring(lastIndex, startIndex))
                }

                val url = match.value
                val annotatedUrl = if (url.startsWith("www.")) "https://$url" else url

                pushLink(
                    LinkAnnotation.Url(
                        url = annotatedUrl,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = color,
                                fontWeight = FontWeight.Normal,
                                textDecoration = TextDecoration.Underline
                            ),
                            focusedStyle = SpanStyle(
                                color = color,
                                fontWeight = FontWeight.Normal,
                                textDecoration = TextDecoration.Underline,
                                background = color.copy(alpha = 0.2f)
                            ),
                            hoveredStyle = SpanStyle(
                                color = color,
                                fontWeight = FontWeight.Normal,
                                textDecoration = TextDecoration.Underline,
                                background = color.copy(alpha = 0.2f)
                            ),
                            pressedStyle = SpanStyle(
                                color = color,
                                fontWeight = FontWeight.Normal,
                                textDecoration = TextDecoration.Underline,
                                background = color.copy(alpha = 0.2f)
                            ),
                        ),
                        linkInteractionListener = linkListener
                    )
                )
                append(url)
                pop()

                lastIndex = endIndex
            }

            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }

        return annotatedString
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

    val formatted = NumberFormat.getNumberInstance(Locale.getDefault())
    formatted.minimumFractionDigits = 0
    formatted.maximumFractionDigits = places
    formatted.roundingMode = java.math.RoundingMode.HALF_UP

    return formatted.format(number)
}