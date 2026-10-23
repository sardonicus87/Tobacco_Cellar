package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import android.app.Activity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.TransitionCache
import com.sardonicus.tobaccocellar.ui.utilities.getInstalledLanguages
import com.sardonicus.tobaccocellar.ui.utilities.getSystemLanguage
import com.sardonicus.tobaccocellar.ui.utilities.getSystemLocales
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val activity = LocalActivity.current as Activity
    val context = LocalCellarApplication.current

    val deviceLocales = getSystemLocales(context)
    val installedLocales = getInstalledLanguages(context)
    val currentAppLocale = AppCompatDelegate.getApplicationLocales()
    val currentCode = if (currentAppLocale.isEmpty) "system" else currentAppLocale[0]?.language?.lowercase() ?: "system"

    val preferredCodes = buildSet {
        add(currentCode)
        for (locale in deviceLocales) { add(locale.language.lowercase()) }
        addAll(installedLocales)
    }.filter { it in SupportedLanguages.codes }.toSet()

    val preferredOptions = preferredCodes.mapNotNull { code ->
        SupportedLanguages.list.find { it.code == code } }.distinct()
    val remaining = SupportedLanguages.list.filter { lang -> lang.code !in preferredCodes }

    val systemCode = getSystemLanguage(context)
    val systemOverride = if (systemCode in SupportedLanguages.codes) systemCode else "en"

    val options = listOf(
        LanguageOption(stringResource(R.string.system_language, systemOverride), "system")) +
            (preferredOptions + remaining).map { LanguageOption(it.displayString, it.code) }

    val selected = options.find { it.code == currentCode } ?: options[0]

    var expanded by remember { mutableStateOf(false) }
    val startIndex = options.indexOfFirst { it.code == currentCode }.minus(1).coerceAtLeast(0)
    val listState = rememberLazyListState(startIndex)
    LaunchedEffect(Unit) { if (!expanded) { listState.scrollToItem(startIndex) } }

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

                                    val showDivider = preferredOptions.isNotEmpty() &&
                                            remaining.isNotEmpty() &&
                                            option.code == remaining.first().code
                                    if (showDivider) { HorizontalDivider(Modifier.padding(vertical = 2.dp)) }

                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clickable(null, LocalIndication.current) {
                                                expanded = false
                                                if (option.code != currentCode) {
                                                    onLanguageChange(option.code)
                                                    onDismiss()
                                                    changeLanguage(activity, option.code)
                                                }
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
        confirmButton = { TextButton({ onDismiss() }) { Text(stringResource(R.string.cancel)) } },
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

fun changeLanguage(activity: Activity, code: String) {
    val decorView = activity.window.decorView
    val screenshot = decorView.drawToBitmap()

    TransitionCache.screenshot = screenshot

    if (code == "system") {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }
}