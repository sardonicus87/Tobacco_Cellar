package com.sardonicus.tobaccocellar.ui.utilities

import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import java.text.NumberFormat
import java.text.ParseException

fun String.localizedToDouble(
    fallback: Double,
    min: Double? = null,
    max: Double? = null
): Double {
    if (this.isBlank()) return fallback

    val numberFormat = NumberFormat.getNumberInstance(LanguageState.locale)

    return try {
        val parsedNumber = numberFormat.parse("0$this")?.toDouble() ?: return fallback

        var result = parsedNumber
        if (min != null && result < min) result = min
        if (max != null && result > max) result = max
        result
    } catch (_: ParseException) { fallback }
}


fun String.localizedToDouble(
    min: Double? = null,
    max: Double? = null
): Double? {
    if (this.isBlank()) return null

    val numberFormat = NumberFormat.getNumberInstance(LanguageState.locale)

    return try {
        val parsedNumber = numberFormat.parse("0$this")?.toDouble() ?: return null

        var result = parsedNumber
        if (min != null && result < min) result = min
        if (max != null && result > max) result = max
        result
    } catch (_: ParseException) { null }
}