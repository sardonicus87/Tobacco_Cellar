package com.sardonicus.tobaccocellar.ui.utilities

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

fun getSystemLanguage(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? LocaleManager
        val systemLocales = localeManager?.systemLocales
        if (systemLocales != null && !systemLocales.isEmpty) { systemLocales[0]?.language ?: "en" }
        else { Locale.getDefault().language }
    } else {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (appLocales.isEmpty) { Locale.getDefault().language }
        else "en"
    }
}