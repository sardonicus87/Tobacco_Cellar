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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

object LanguageState {
    private val _code = MutableStateFlow("system")

    private val _localeFlow: StateFlow<Locale> = _code.map {
        val tag = if (it == "system") {
            try { getSystemLanguage(CellarApplication.instance) }
            catch(_ : Exception) { Locale.getDefault().language }
        } else { it }
        Locale.forLanguageTag(tag)
    }.stateIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, Locale.ROOT)

    var codeFlow: StateFlow<String> = _code.asStateFlow()

    var code: String
        get() = _code.value
        set(value) { _code.value = value }

    val locale: Locale
        get() = _localeFlow.value

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _code.value = prefs.getString("app_language", "system") ?: "system"
    }
}