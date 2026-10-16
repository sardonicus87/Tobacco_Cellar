package com.sardonicus.tobaccocellar.ui.utilities

import android.app.LocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
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

fun getSystemLocales(context: Context): List<Locale> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? LocaleManager
        val systemLocales = localeManager?.systemLocales
        if (systemLocales != null && !systemLocales.isEmpty) {
            (0 until systemLocales.size()).mapNotNull { systemLocales[it] }
        } else { listOf(Locale.getDefault()) }
    } else {
        val configLocales = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
        if (!configLocales.isEmpty) {
            (0 until configLocales.size()).mapNotNull { configLocales.get(it) }
        } else { listOf(Locale.getDefault()) }
    }
}