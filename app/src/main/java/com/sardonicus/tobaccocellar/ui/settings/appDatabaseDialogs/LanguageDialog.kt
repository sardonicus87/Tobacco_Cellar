package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

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
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.getSystemLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val installedOptions = AppCompatDelegate.getApplicationLocales()
    val currentCode = if (installedOptions.isEmpty) "system" else installedOptions[0]?.language ?: "system"
    val context = LocalCellarApplication.current

    val localeCode = getSystemLanguage(context)
    val systemCode = if (localeCode in SupportedLanguages.codes) localeCode else "en"

    val options = listOf(LanguageOption(stringResource(R.string.system_language, systemCode), "system")) +
            SupportedLanguages.list.map { LanguageOption(it.displayString, it.code) }

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
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .clickable(null, LocalIndication.current) {
                                                    if (option.code == "system") {
                                                        AppCompatDelegate.setApplicationLocales(
                                                            LocaleListCompat.getEmptyLocaleList()
                                                        )
                                                    } else {
                                                        AppCompatDelegate.setApplicationLocales(
                                                            LocaleListCompat.forLanguageTags(option.code)
                                                        )
                                                    }
                                                    onLanguageChange(option.code)
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
    val list = listOf(
        AppLanguage("English", "en"),
        AppLanguage("Español", "es"),
        AppLanguage("Deutsch", "de"),
        AppLanguage("Français", "fr"),
        AppLanguage("日本語", "ja")
    )
    val codes = list.map { it.code }.toSet()

    fun getDisplayString(code: String): String? {
        val lang = list.find { it.code == code }
        return lang?.displayString
    }
}