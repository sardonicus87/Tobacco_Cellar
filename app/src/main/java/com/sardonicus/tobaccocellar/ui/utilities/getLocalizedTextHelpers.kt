package com.sardonicus.tobaccocellar.ui.utilities

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.BlendTypes
import java.util.Locale

fun getLocalizedContext(context: Context): Context {
    val locales = AppCompatDelegate.getApplicationLocales()
    val target =
        if (locales.isEmpty) { Locale.forLanguageTag(getSystemLanguage(context)) }
        else { locales[0] ?: return context }

    val config = Configuration(Resources.getSystem().configuration)
    config.setLocale(target)
    return context.createConfigurationContext(config)
}

fun getLocalizedType(typeKey: String, app: CellarApplication, showUnassigned: Boolean = true): String {
    val context = getLocalizedContext(app)

    if (typeKey.isBlank()) { return if (showUnassigned) context.getString(R.string.unassigned) else "" }
    val blendType = runCatching { BlendTypes.valueOf(typeKey) }.getOrDefault(BlendTypes.UNASSIGNED)
    return context.getString(blendType.resId)
}