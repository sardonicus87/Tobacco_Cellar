package com.sardonicus.tobaccocellar.ui.addEditItems

import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.Tins
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

/** Validation Functions **/
fun validateInput(
    details: ItemDetails,
    tins: List<TinDetails>,
    onTabErrorUpdate: (TabErrorState) -> Unit
): Boolean {
    val validDetails = details.brand.isNotBlank() && details.blend.isNotBlank()

    val labels = tins.map { it.tinLabel }
    val uniqueLabels = labels.size == labels.distinct().size
    val validTins = tins.all { tin ->
        val (manuf, cellar, open) = validateDates(tin.manufactureDate, tin.cellarDate, tin.openDate)
        tin.tinLabel.isNotBlank() &&
                ((tin.tinQuantityString.isNotBlank() && tin.unit.isNotBlank()) ||
                tin.tinQuantityString.isBlank()) &&
                manuf && cellar && open
    } && uniqueLabels

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

    val manufCellarValid = if (manuf != null && cellar != null) manuf <= cellar else true
    val manufOpenValid = if (manuf != null && open != null) manuf <= open else true
    val cellarOpenValid = if (cellar != null && open != null) cellar <= open else true

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
    return tins.filter { it.tempTinId != tempTinId && it.tinLabel.isNotBlank() }.none { it.tinLabel == tinLabel }
}


fun calculateSyncTins(
    tinDetails: List<TinDetails>,
    conversion: TinConversion
): Int {
    val tins = tinDetails.filter { !it.finished }

    val totalLbs = tins.filter { it.unit == "lbs" }.sumOf { (it.tinQuantity * 16) / conversion.ozRate }
    val totalOz = tins.filter { it.unit == "oz" }.sumOf { it.tinQuantity / conversion.ozRate }
    val totalGrams = tins.filter { it.unit == "grams" }.sumOf { it.tinQuantity / conversion.gramsRate }

    return (totalLbs + totalOz + totalGrams).roundToInt()
}


/** Data Classes **/
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
)

data class ItemDetails(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val type: String = "",
    val subGenre: String = "",
    val cut: String = "",
    val componentString: String = "",
    val flavoringString: String = "",
    val quantity: Int = 1,
    val quantityString: String = "",
    val disliked: Boolean = false,
    val favorite: Boolean = false,
    val rating: Double? = null,
    val notes: String = "",
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
    val tinLabel: String = "",
    val container: String = "",
    val tinQuantity: Double = 0.0,
    val tinQuantityString: String = "",
    val unit: String = "",
    val manufactureDate: Long? = null,
    val cellarDate: Long? = null,
    val openDate: Long? = null,
    val finished: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val manufactureDateShort: String = "",
    val cellarDateShort: String = "",
    val openDateShort: String = "",
    val manufactureDateLong: String = "",
    val cellarDateLong: String = "",
    val openDateLong: String = "",
    val detailsExpanded: Boolean = true,
    val labelIsValid: Boolean = false,
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
    brand = brand,
    blend = blend,
    type = type,
    quantity = quantity,
    syncTins = syncTins,
    disliked = disliked,
    favorite = favorite,
    rating = rating,
    notes = notes,
    subGenre = subGenre,
    cut = cut,
    inProduction = inProduction,
    lastModified = lastModified
)

fun TinDetails.toTin(itemsId: Int): Tins = Tins(
    tinId = tinId,
    itemsId = itemsId,
    tinLabel = tinLabel,
    container = container,
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
        .splitToSequence(",")
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
        .splitToSequence(",")
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
    brand = brand,
    blend = blend,
    type = type,
    subGenre = subGenre,
    cut = cut,
    componentString = compString,
    flavoringString = flavorString,
    quantity = quantity,
    quantityString = quantity.toString(),
    syncTins = syncTins,
    rating = rating,
    disliked = disliked,
    favorite = favorite,
    notes = notes,
    inProduction = inProduction,
    lastModified = lastModified
)


fun Tins.toTinDetails(): TinDetails {
    val numberFormat = NumberFormat.getInstance(Locale.getDefault())
    val quantityString = if (unit.isNotBlank()) {
        if (tinQuantity == floor(tinQuantity)) {
            val integerFormatter = NumberFormat.getIntegerInstance(Locale.getDefault())
            integerFormatter.format(tinQuantity.toLong())
        } else {
            numberFormat.format(tinQuantity)
        }
    } else {
        ""
    }

    return TinDetails(
        tinId = tinId,
        itemsId = itemsId,
        tinLabel = tinLabel,
        container = container,
        unit = unit,
        tinQuantity = tinQuantity,
        tinQuantityString = quantityString,
        manufactureDate = manufactureDate,
        cellarDate = cellarDate,
        openDate = openDate,
        finished = finished,
        lastModified = lastModified,
        manufactureDateShort = formatShortDate(manufactureDate),
        cellarDateShort = formatShortDate(cellarDate),
        openDateShort = formatShortDate(openDate),
        manufactureDateLong = formatMediumDate(manufactureDate),
        cellarDateLong = formatMediumDate(cellarDate),
        openDateLong = formatMediumDate(openDate),
    )
}

/** Date functions **/
fun formatShortDate(millis: Long?): String {
    return millis?.let {
        val instant = Instant.ofEpochMilli(it)
        DateTimeFormatter.ofPattern("MM/yy").withZone(ZoneId.systemDefault()).format(instant)
    } ?: ""
}

fun formatMediumDate(millis: Long?): String {
    return millis?.let {
        val instant = Instant.ofEpochMilli(it)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(localDate)
    } ?: ""
}

//fun formatLongDate(millis: Long?): String {
//    return millis?.let {
//        val instant = Instant.ofEpochMilli(it)
//        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
//       DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(localDate)
//    } ?: ""
//}