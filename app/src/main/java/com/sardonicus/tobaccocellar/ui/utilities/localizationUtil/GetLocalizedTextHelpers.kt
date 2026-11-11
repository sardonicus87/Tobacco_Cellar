package com.sardonicus.tobaccocellar.ui.utilities.localizationUtil

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.ibm.icu.text.ListFormatter
import com.ibm.icu.util.ULocale
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.BlendTypes
import java.text.Collator
import java.text.Normalizer
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

@SuppressLint("AppBundleLocaleChanges")
fun getLocalizedContext(context: Context): Context {
    val target = LanguageState.locale
    val config = Configuration(Resources.getSystem().configuration)
    config.setLocale(target)
    return context.createConfigurationContext(config)
}

fun Context.safeString(@StringRes resId: Int, vararg formatArgs: Any): String {
    return try { if (formatArgs.isNotEmpty()) getString(resId, *formatArgs) else getString(resId) }
    catch (_: Exception) { // fallback to default base resources if the localized split isn't yet on disk
        try {
            if (formatArgs.isNotEmpty()) resources.getString(resId, *formatArgs)
            else resources.getString(resId)
        } catch (_: Exception) { "" }
    }
}


fun Context.safeQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
    return try { resources.getQuantityString(id, quantity, *formatArgs) }
    catch (_: Exception) {
        try { resources.getQuantityString(id, quantity, *formatArgs) } catch (_: Exception) { "" }
    }
}

fun getLocalizedType(typeKey: String, showUnassigned: Boolean = true): String {
    val context = getLocalizedContext(CellarApplication.instance)

    if (typeKey.isBlank()) { return if (showUnassigned) context.safeString(R.string.unassigned) else "" }
    val blendType = runCatching { BlendTypes.valueOf(typeKey) }.getOrDefault(BlendTypes.UNASSIGNED)
    return context.safeString(blendType.resId)
}

fun formatLocalizedList(items: List<String>, locale: Locale = LanguageState.locale): String {
    if (items.isEmpty()) return ""
    return try {
        val uLocale = ULocale.forLocale(locale)
        ListFormatter.getInstance(uLocale, ListFormatter.Type.UNITS, ListFormatter.Width.WIDE).format(items)
    } catch (_: Exception) { items.joinToString(", ") }
}

fun getLocalizedComma(locale: Locale = LanguageState.locale): String {
    return when (locale.language.lowercase()) {
        "ar", "fa", "ur" -> "، "
        "zh", "ja" -> "、"
        else -> ", "
    }
}

val collator: Collator = Collator.getInstance(LanguageState.locale).apply { strength = Collator.PRIMARY }

fun String.toLtr(): String { return if (this.isNotBlank()) "\u202A$this\u202C" else this }

fun String.rtlIsolate(): String {
    return if (this.isNotBlank() && LanguageState.isRtl) "\u2068$this\u2069" else this
}

fun String.stripControlChar(): String {
    return this.replace(Regex("[\u2067\u2068\u2069\u202A\u202C]"), "")
}

fun String.stripAccent(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "")
}

fun parseLocalizedDouble(text: String, locale: Locale = LanguageState.locale): Double? {
    if (text.isBlank()) return null
    return try {
        NumberFormat.getNumberInstance(locale).parse(text)?.toDouble()
    } catch (_: ParseException) { text.replace(',', '.').toDoubleOrNull()
    } catch (_: Exception) { null }
}