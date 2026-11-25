package com.sardonicus.tobaccocellar.ui.addEditItems

import android.text.format.DateFormat
import androidx.compose.foundation.text.input.TextFieldState
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import com.sardonicus.tobaccocellar.ui.utilities.toCleanString
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.floor
import kotlin.math.roundToInt

/** Validation Functions **/
fun validateInput(
    details: ItemDetails,
    tins: List<TinDetails>,
    onTabErrorUpdate: (TabErrorState) -> Unit
): Boolean {
    val validDetails = details.brand.toCleanString().isNotBlank() && details.blend.toCleanString().isNotBlank()

    val labels = tins.map { it.tinLabel.toCleanString() }
    val validTins = tins.all { tin ->
        val (manuf, cellar, open) = validateDates(tin.manufactureDate, tin.cellarDate, tin.openDate)
        tin.tinLabel.toCleanString().isNotBlank() &&
                ((tin.tinQuantityString.toCleanString().isNotBlank() && tin.unit.isNotBlank()) ||
                tin.tinQuantityString.toCleanString().isBlank()) &&
                manuf && cellar && open
    } && labels.size == labels.distinct().size

    onTabErrorUpdate(TabErrorState(!validDetails, !validTins))
    return validDetails && validTins
}

fun validateDates(
    manufactureDate: Long?,
    cellarDate: Long?,
    openDate: Long?,
): Triple<Boolean, Boolean, Boolean> {
    val manuf = normalizeDate(manufactureDate)
    val cellar = normalizeDate(cellarDate)
    val open = normalizeDate(openDate)

    val manufCellarValid = !(manuf != null && cellar != null) || manuf <= cellar
    val manufOpenValid = !(manuf != null && open != null) || manuf <= open
    val cellarOpenValid = !(cellar != null && open != null) || cellar <= open

    return Triple(manufCellarValid, manufOpenValid, cellarOpenValid)
}

private fun normalizeDate(millis: Long?): Long? {
    return millis?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
}


fun isTinLabelValid(tins: List<TinDetails>, tinLabel: String, tempTinId: Int): Boolean {
    return tinLabel.isBlank() || tins.filter { it.tempTinId != tempTinId }.none { it.tinLabel.toCleanString() == tinLabel }
}


fun calculateSyncTinsDetails(
    tinDetails: List<TinDetails>,
    conversion: TinConversion
): Int {
    return tinDetails.sumOf {
        if (it.finished) 0.0 else {
            when (it.unit) {
                "oz" -> it.tinQuantity / conversion.ozRate
                "lbs" -> (it.tinQuantity * 16) / conversion.ozRate
                "g" -> it.tinQuantity / conversion.gramsRate
                "kg" -> it.tinQuantity * 1000 / conversion.gramsRate
                else -> 0.0
            }
        }
    }.roundToInt()
}


/** Data Classes **/
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
)

data class ItemDetails(
    val id: Int = 0,
    val brand: TextFieldState = TextFieldState(""),
    val blend: TextFieldState = TextFieldState(""),
    val type: String = "",
    val subGenre: TextFieldState = TextFieldState(""),
    val cut: TextFieldState = TextFieldState(""),
    val componentString: TextFieldState = TextFieldState(""),
    val flavoringString: TextFieldState = TextFieldState(""),
    val quantity: Int = 1,
    val quantityString: TextFieldState = TextFieldState(""),
    val disliked: Boolean = false,
    val favorite: Boolean = false,
    val rating: Double? = null,
    val notes: TextFieldState = TextFieldState(""),
    val inProduction: Boolean = true,
    val syncTins: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),

    val tinDetailsList: List<TinDetails> = listOf(),
    val originalItem: OriginalItem = OriginalItem()
)

data class OriginalItem(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val rating: Double? = 0.0,
    val favorite: Boolean = false,
    val disliked: Boolean = false,
    val notes: String = "",
    val subGenre: String = "",
    val cut: String = "",
    val inProduction: Boolean = false,
    val syncTins: Boolean = false,
    val lastModified: Long = -1L
)

data class TinDetails(
    val tinId: Int = 0,
    val tempTinId: Int = 0,
    val itemsId: Int = 0,
    val tinLabel: TextFieldState = TextFieldState(""),
    val container: TextFieldState = TextFieldState(""),
    val tinQuantity: Double = 0.0,
    val tinQuantityString: TextFieldState = TextFieldState(""),
    val unit: String = "",
    val manufactureDate: Long? = null,
    val cellarDate: Long? = null,
    val openDate: Long? = null,
    val finished: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val manufactureDateShort: TextFieldState = TextFieldState(""),
    val cellarDateShort: TextFieldState = TextFieldState(""),
    val openDateShort: TextFieldState = TextFieldState(""),
    val manufactureDateLong: TextFieldState = TextFieldState(""),
    val cellarDateLong: TextFieldState = TextFieldState(""),
    val openDateLong: TextFieldState = TextFieldState(""),
    val detailsExpanded: Boolean = true,
    val labelIsValid: Boolean = true,
)

data class OriginalTin(
    val tinId: Int = 0,
    val itemsId: Int = 0,
    val tinLabel: String = "",
    val container: String = "",
    val tinQuantity: Double = 0.0,
    val unit: String = "",
    val manufactureDate: Long? = null,
    val cellarDate: Long? = null,
    val openDate: Long? = null,
    val finished: Boolean = false,
    val lastModified: Long = -1L
)

data class TinConversion(
    val amount: String = "",
    val unit: String = "",
    val ozRate: Double = 1.75,
    val gramsRate: Double = 50.0
)

data class TabErrorState(
    val detailsError: Boolean = false,
    val tinsError: Boolean = false,
)

data class ExistState(
    val exists: Boolean = false,
    val transferId: Int = 0
)

/** Extension converter functions **/
// data classes -> db table
fun ItemDetails.toItem(): Items = Items(
    id = id,
    brand = brand.toCleanString(),
    blend = blend.toCleanString(),
    type = type,
    quantity = quantity,
    syncTins = syncTins,
    disliked = disliked,
    favorite = favorite,
    rating = rating,
    notes = notes.toCleanString(),
    subGenre = subGenre.toCleanString(),
    cut = cut.toCleanString(),
    inProduction = inProduction,
    lastModified = lastModified
)

fun TinDetails.toTin(itemsId: Int): Tins = Tins(
    tinId = tinId,
    itemsId = itemsId,
    tinLabel = tinLabel.toCleanString(),
    container = container.toCleanString(),
    tinQuantity = tinQuantity,
    unit = unit,
    manufactureDate = manufactureDate,
    cellarDate = cellarDate,
    openDate = openDate,
    finished = finished,
    lastModified = lastModified
)

fun String.toComponents(existingComps: List<String>): List<Components> {
    if (this.isBlank()) return emptyList()

    val lookup = existingComps.associateBy { it.lowercase() }

    return this
        .splitToSequence(Regex("[,،、]"))
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.isNotBlank() }
        .map { entered ->
            val existing = lookup[entered.lowercase()]
            Components(componentName = existing ?: entered)
        }.toList()
}

fun String.toFlavoring(existingFlavors: List<String>): List<Flavoring> {
    if (this.isBlank()) return emptyList()

    val lookup = existingFlavors.associateBy { it.lowercase() }

    return this
        .splitToSequence(Regex("[,،、]"))
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.isNotBlank() }
        .map { entered ->
            val existing = lookup[entered.lowercase()]
            Flavoring(flavoringName = existing ?: entered.trim())
        }.toList()
}

// db tables -> data classes
fun Items.toItemDetails(compString: String, flavorString: String): ItemDetails = ItemDetails(
    id = id,
    brand = TextFieldState(brand),
    blend = TextFieldState(blend),
    type = type,
    subGenre = TextFieldState(subGenre),
    cut = TextFieldState(cut),
    componentString = TextFieldState(compString),
    flavoringString = TextFieldState(flavorString),
    quantity = quantity,
    quantityString = TextFieldState(quantity.toString()),
    syncTins = syncTins,
    rating = rating,
    disliked = disliked,
    favorite = favorite,
    notes = TextFieldState(notes),
    inProduction = inProduction,
    lastModified = lastModified
)


fun Tins.toTinDetails(): TinDetails {
    val quantityString = if (unit.isNotBlank()) {
        if (tinQuantity == floor(tinQuantity)) {
            NumberFormat.getIntegerInstance(LanguageState.locale).format(tinQuantity.toLong())
        } else {
            NumberFormat.getInstance(LanguageState.locale).format(tinQuantity)
        }
    } else { "" }

    return TinDetails(
        tinId = tinId,
        itemsId = itemsId,
        tinLabel = TextFieldState(tinLabel),
        container = TextFieldState(container),
        unit = unit,
        tinQuantity = tinQuantity,
        tinQuantityString = TextFieldState(quantityString),
        manufactureDate = manufactureDate,
        cellarDate = cellarDate,
        openDate = openDate,
        finished = finished,
        lastModified = lastModified,
        manufactureDateShort = TextFieldState(formatShortDate(manufactureDate)),
        cellarDateShort = TextFieldState(formatShortDate(cellarDate)),
        openDateShort = TextFieldState(formatShortDate(openDate)),
        manufactureDateLong = TextFieldState(formatMediumDate(manufactureDate)),
        cellarDateLong = TextFieldState(formatMediumDate(cellarDate)),
        openDateLong = TextFieldState(formatMediumDate(openDate)),
    )
}

/** Date functions **/
fun formatShortDate(millis: Long?): String {
    return millis?.let {
        val localDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        val pattern = DateFormat.getBestDateTimePattern(LanguageState.locale, "MM/yy")
        val formatter = DateTimeFormatter.ofPattern(pattern, LanguageState.locale)
        formatter.format(localDate)
    } ?: ""
}

fun formatMediumDate(millis: Long?, alternate: Boolean = false): String {
    return millis?.let {
        val localDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()

        val formatter = if (alternate) {
            val pattern = DateFormat.getBestDateTimePattern(LanguageState.locale, "MMddyy")
            DateTimeFormatter.ofPattern(pattern, LanguageState.locale)
        } else { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

        formatter.format(localDate)
    } ?: ""
}

//fun formatLongDate(millis: Long?): String {
//    return millis?.let {
//        val localDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
//        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
//        formatter.format(localDate)
//    } ?: ""
//}