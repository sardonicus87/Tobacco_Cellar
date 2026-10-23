package com.sardonicus.tobaccocellar.ui.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.BlendTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

@SuppressLint("AppBundleLocaleChanges")
fun getLocalizedContext(context: Context): Context {
    val target =
        if (LanguageState.code == "system") { Locale.forLanguageTag(getSystemLanguage(context)) }
        else { Locale.forLanguageTag(LanguageState.code) }

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

fun getLocalizedType(typeKey: String, app: CellarApplication, showUnassigned: Boolean = true): String {
    val context = getLocalizedContext(app)

    if (typeKey.isBlank()) { return if (showUnassigned) context.safeString(R.string.unassigned) else "" }
    val blendType = runCatching { BlendTypes.valueOf(typeKey) }.getOrDefault(BlendTypes.UNASSIGNED)
    return context.safeString(blendType.resId)
}

object LanguageState {
    private val _code = MutableStateFlow("system")
    var codeFlow: StateFlow<String> = _code.asStateFlow()

    var code: String
        get() = _code.value
        set(value) { _code.value = value }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _code.value = prefs.getString("app_language", "system") ?: "system"
    }
}