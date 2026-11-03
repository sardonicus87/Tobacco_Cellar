package com.sardonicus.tobaccocellar.ui.utilities.localizationUtil

import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.AppLanguage
import java.util.Locale

object SupportedLanguages {
    val codes = listOf(
        "ar", "bn", "bg", "ca", "zh-CN", "hr", "cs", "da", "nl", "en",
        "et", "fi", "fr", "de", "el", "gu", "he", "hi", "hu", "is",
        "id", "it", "ja", "kn", "ko", "lv", "lt", "ml", "mr", "no",
        "fa", "pl", "pt", "pa", "ro", "ru", "sk", "sl", "es", "sw",
        "sv", "ta", "te", "th", "tr", "uk", "ur", "vi", "zu"
    ).sorted().toSet()

    val list: List<AppLanguage> = codes.map { code ->
        val locale = Locale.forLanguageTag(code)
        val nativeName = locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
        AppLanguage(nativeName, code)
    }

    fun getDisplayString(code: String): String? {
        val lang = list.find { it.code == code }
        return lang?.displayString
    }
}

fun updateLegacyLangCode(code: String): String {
    return when (code.lowercase()) {
        "iw" -> "he" // Hebrew
        "in" -> "id" // Indonesian
        "ji" -> "yi" // Yiddish
        else -> code
    }
}