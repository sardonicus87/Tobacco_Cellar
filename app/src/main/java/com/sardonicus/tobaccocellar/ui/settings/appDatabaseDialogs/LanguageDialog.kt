package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Popup
import androidx.core.os.LocaleListCompat
import androidx.core.view.drawToBitmap
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.TransitionActivity
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.TransitionCache
import com.sardonicus.tobaccocellar.ui.utilities.TransitionCoverReady
import com.sardonicus.tobaccocellar.ui.utilities.getSystemLanguage
import com.sardonicus.tobaccocellar.ui.utilities.getSystemLocales
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val installedOptions = AppCompatDelegate.getApplicationLocales()
    val currentCode = if (installedOptions.isEmpty) "system" else installedOptions[0]?.language ?: "system"
    val context = LocalCellarApplication.current
    val activity = LocalActivity.current as Activity
    val scope = rememberCoroutineScope()

    val localeCode = getSystemLanguage(context)
    val systemCode = if (localeCode in SupportedLanguages.codes) localeCode else "en"

    val deviceLocales = getSystemLocales(context)
    val supportedDeviceLocales = deviceLocales.map { it.language.lowercase() }
        .filter { it in SupportedLanguages.codes }
        .mapNotNull { code -> SupportedLanguages.list.find { it.code == code } }

    val remaining = SupportedLanguages.list.filter { lang -> lang.code !in supportedDeviceLocales.map { it.code } }

    val options = listOf(
        LanguageOption(stringResource(R.string.system_language, systemCode), "system")) +
            (supportedDeviceLocales + remaining).map { LanguageOption(it.displayString, it.code) }

    val selected = options.find { it.code == currentCode } ?: options[0]
    var expanded by remember { mutableStateOf(false) }
    val startIndex = options.indexOfFirst { it.code == currentCode }.coerceAtLeast(0)

    val listState = rememberLazyListState(startIndex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.language_description))

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(null, null) { expanded = !expanded },
                    contentAlignment = Alignment.Center
                ) {
                    CustomTextField(
                        value = selected.label,
                        modifier = Modifier.width(175.dp),
                        readOnly = true,
                        enabled = false,
                        onValueChange = { },
                        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            disabledTrailingIconColor = LocalContentColor.current,
                            disabledTextColor = LocalContentColor.current,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField,
                        ),
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraSmall,
                        contentPadding = PaddingValues(16.dp, 4.dp, 12.dp, 4.dp)
                    )

                    if (expanded) {
                        Popup(
                            alignment = Alignment.Center,
                            onDismissRequest = { expanded = false }
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .width(175.dp)
                                    .heightIn(max = 168.dp)
                                    .background(
                                        LocalCustomColors.current.textField,
                                        MaterialTheme.shapes.extraSmall
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                items(options.size) {
                                    val option = options[it]

                                    val showDivider = supportedDeviceLocales.isNotEmpty() &&
                                            remaining.isNotEmpty() &&
                                            option.code == remaining.first().code
                                    if (showDivider) { HorizontalDivider(Modifier.padding(vertical = 2.dp)) }

                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clickable(null, LocalIndication.current) {
                                                if (option.code != currentCode) {
                                                    scope.launch { changeLanguage(activity, option.code) }
                                                    onLanguageChange(option.code)
                                                }
                                                expanded = false
                                            }
                                            .padding(horizontal = 16.dp),
                                        Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = option.label,
                                            lineHeight = 1.em,
                                            fontWeight = if (option.code == currentCode) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton({ onDismiss() }) { Text(stringResource(R.string.done)) } },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )

}

data class LanguageOption(val label: String, val code: String)

data class AppLanguage(val label: String, val code: String) {
    val displayString: String get() = "$label ($code)"
}

object SupportedLanguages {
    val codes = listOf(
        "ar", "bn", "bg", "ca", "zh-CN", "hr", "cs", "da", "nl", "en",
        "et", "fi", "fr", "de", "el", "gu", "he", "hi", "hu", "is",
        "id", "it", "ja", "kn", "ko", "lv", "lt", "ml", "mr", "no",
        "fa", "pl", "pt", "pa", "ro", "ru", "sk", "sl", "es", "sw",
        "sv", "ta", "te", "th", "tr", "uk", "ur", "vi", "zu"
    )

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

suspend fun changeLanguage(activity: Activity, code: String) {
    val decorView = activity.window.decorView
    val bitmap = decorView.drawToBitmap()

    TransitionCache.saveScreenshot(activity, bitmap)

    val intent = Intent(activity, TransitionActivity::class.java)
    val options = ActivityOptions.makeCustomAnimation(activity, 0, 0)
    activity.startActivity(intent, options.toBundle())

    EventBus.events.first { it is TransitionCoverReady }
    delay(25.milliseconds)
    if (code == "system") {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }
}