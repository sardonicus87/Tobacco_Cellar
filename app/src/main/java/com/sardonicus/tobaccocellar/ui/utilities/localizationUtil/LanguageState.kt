package com.sardonicus.tobaccocellar.ui.utilities.localizationUtil

import android.content.Context
import com.sardonicus.tobaccocellar.CellarApplication
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

object LanguageState {
    private val _code = MutableStateFlow("system")

    private val _localeFlow: StateFlow<Locale> = _code.map { rawCode ->
        val systemLocales = getSystemLocales(CellarApplication.instance)
        val code = updateLegacyLangCode(rawCode)

        if (code == "system") { systemLocales.firstOrNull() ?: Locale.getDefault() }
        else { systemLocales.find { updateLegacyLangCode(it.language).equals(code, true) } ?: Locale.forLanguageTag(code) }
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