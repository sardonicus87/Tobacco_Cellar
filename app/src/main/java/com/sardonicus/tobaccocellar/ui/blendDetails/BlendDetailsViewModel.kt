package com.sardonicus.tobaccocellar.ui.blendDetails

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Stable
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
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.addEditItems.formatMediumDate
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.utilities.getLocalizedType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Locale

class BlendDetailsViewModel(
    itemsId: Int,
    filterViewModel: FilterViewModel,
    preferencesRepo: PreferencesRepo,
    private val app: CellarApplication
) : ViewModel() {

    private val _parseLinks = MutableStateFlow(false)
    val parseLinks = _parseLinks.asStateFlow()

    val blendDetails: StateFlow<BlendDetails> = combine(
        filterViewModel.everythingFlow,
        preferencesRepo.quantityOption,
        preferencesRepo.parseLinks
    ) { allItems, quantityOption, parseLinks ->
        val isMetric = isMetricLocale()
        val quantityRemap = when (quantityOption) {
            QuantityOption.TINS -> if (isMetric) QuantityOption.GRAMS else QuantityOption.OUNCES
            else -> quantityOption
        }

        _parseLinks.value = parseLinks

        val item = allItems.find { it.items.id == itemsId } ?: return@combine BlendDetails()

        BlendDetails(
            id = item.items.id,
            brand = item.items.brand,
            blend = item.items.blend,
            favDisIcon = if (item.items.favorite) R.drawable.heart_filled_24 else if (item.items.disliked) R.drawable.heartbroken_filled_24 else null,
            itemDetails = setOfNotNull(
                buildDetailsString(app.getString(R.string.type_label), getLocalizedType(item.items.type, app)),
                buildDetailsString(app.getString(R.string.subgenre_label), item.items.subGenre),
                buildDetailsString(app.getString(R.string.cut_label), item.items.cut),
                buildDetailsString(app.getString(R.string.component_label), item.components.map { it.componentName }.sorted().joinToString(", ")),
                buildDetailsString(app.getString(R.string.flavors_label), item.flavoring.map { it.flavoringName }.sorted().joinToString(", ")),
                buildDetailsString(app.getString(R.string.production_status), if (item.items.inProduction) app.getString(R.string.in_production) else app.getString(R.string.discontinued)),
                buildDetailsString(app.getString(R.string.no_of_tins_label), item.items.quantity.toString())
            ),
            rating = item.items.rating,
            notes = item.items.notes,
            tinsDetails = item.tins.sortedBy { it.tinId }.associateWith { tin ->
                buildSet {
                    buildDetailsString(app.getString(R.string.container_label), tin.container)?.let { add(DetailLine(it)) }
                    buildDetailsString(app.getString(R.string.quantity_label), if (tin.unit.isNotBlank()) { app.getString(R.string.tin_quantity_format, formatDecimal(tin.tinQuantity), tin.unit) } else "")?.let { add(DetailLine(it)) }
                    buildDetailsString(app.getString(R.string.manufacture_date_label), formatMediumDate(tin.manufactureDate))?.let {
                        val secondary = buildDetailsString("", "(${calculateAge(tin.manufactureDate, app, DateField.MANUFACTURE)})", 12.sp)
                        add(DetailLine(it, secondary))
                    }
                    buildDetailsString(app.getString(R.string.cellar_date_label), formatMediumDate(tin.cellarDate))?.let {
                        val secondary = buildDetailsString("", "(${calculateAge(tin.cellarDate, app, DateField.CELLAR)})", 12.sp)
                        add(DetailLine(it, secondary))
                    }
                    buildDetailsString(app.getString(R.string.open_date_label), formatMediumDate(tin.openDate))?.let {
                        val secondary =
                            if (!tin.finished) { buildDetailsString("", "(${calculateAge(tin.openDate, app, DateField.OPEN)})", 12.sp) }
                            else { buildDetailsString("", app.getString(R.string.finished_lower), 12.sp) }
                        add(DetailLine(it, secondary))
                    }
                }
            },
            tinsTotal = calculateTotal(item.tins.filter { !it.finished }, quantityRemap),
            lastModified = item.items.lastModified.let { if (it == 0L) "n/a" else formatMediumDate(it, true) },
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BlendDetails()
        )


    private fun buildDetailsString(title: String, value: String, fontSize: TextUnit = 14.sp): AnnotatedString? {
        if (value.isBlank()) return null
        val string = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize)) { append(title) }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontSize = fontSize)) { append(value) }
        }
        return string
    }

    private fun calculateTotal(tins: List<Tins>, quantityOption: QuantityOption): String {
        if (tins.isEmpty()) return ""
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

        return when (quantityOption) {
            QuantityOption.OUNCES -> {
                if (sum != null) {
                    if (sum >= 16.00) { app.getString(R.string.format_quantity, "", formatDecimal((sum / 16)), "lb") }
                    else { app.getString(R.string.format_quantity, "", formatDecimal(sum), "oz") }
                } else { null }
            }
            QuantityOption.GRAMS -> {
                if (sum != null) { app.getString(R.string.format_quantity, "", formatDecimal(sum), "g") } else { null }
            }
            else -> { null }
        } ?: ""
    }

    val urlRegex = Regex("""(https?://|www\.)[a-zA-Z0-9_./-]*[a-zA-Z0-9_/-]""")
    fun parseHyperlinks(text: String, color: Color, linkListener: LinkInteractionListener, parseLinks: Boolean): AnnotatedString {
        if (text.isBlank()) return AnnotatedString("")

        if (!parseLinks) return buildAnnotatedString { append(text) }

        val annotatedString = buildAnnotatedString {
            val matches = urlRegex.findAll(text)
            var lastIndex = 0

            for (match in matches) {
                if (match.range.first > lastIndex) {
                    append(text.substring(lastIndex, match.range.first))
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

                lastIndex = match.range.last + 1
            }

            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }

        return annotatedString
    }

}

@Stable
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
    val lastModified: String = ""
)

@Stable
data class DetailLine(
    val primary: AnnotatedString,
    val secondary: AnnotatedString? = null
)

fun calculateAge(date: Long?, app: CellarApplication, field: DateField? = null): String {
    if (date == null) { return "" }

    val now = LocalDate.now()
    val then = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    val period = if (then < now) { Period.between(then, now) } else { Period.between(now, then) }

    val parts = listOfNotNull(
        if (period.years > 0) { app.resources.getQuantityString(R.plurals.years, period.years, period.years) } else
            null,
        if (period.months > 0) { app.resources.getQuantityString(R.plurals.months, period.months, period.months) } else
            null,
        if (period.days > 0) { app.resources.getQuantityString(R.plurals.days, period.days, period.days) } else
            null
    )

    return if (parts.isEmpty()) { app.resources.getString(R.string.today_lower) } else { parts.joinToString(", ") + when (field) {
        DateField.MANUFACTURE -> if (then < now) { app.resources.getString(R.string.old) } else { app.resources.getString(R.string.until_available) }
        DateField.CELLAR -> if (then < now) { app.resources.getString(R.string.in_cellar) } else { app.resources.getString(R.string.until_available) }
        DateField.OPEN -> if (then < now) { app.resources.getString(R.string.open_age) } else { app.resources.getString(R.string.until_opening) }
        else -> "" }
    }
}

fun isMetricLocale(): Boolean {
    val config: Configuration = Resources.getSystem().configuration
    val locale: Locale = config.locales.get(0) ?: Locale.getDefault()

    return when (locale.country.uppercase()) {
        "US", "LR", "MM" -> false
        else -> true
    }
}

fun formatDecimal(number: Double?, places: Int = 2, drop: Boolean = true): String {
    if (number == null) return ""

    val formatted = NumberFormat.getNumberInstance(Locale.getDefault())
    formatted.minimumFractionDigits = if (drop) 0 else (places)
    formatted.maximumFractionDigits = places
    formatted.roundingMode = java.math.RoundingMode.HALF_UP

    return formatted.format(number)
}

enum class DateField { MANUFACTURE, CELLAR, OPEN }