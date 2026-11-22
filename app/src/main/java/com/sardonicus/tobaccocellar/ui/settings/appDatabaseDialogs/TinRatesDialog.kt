package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import com.sardonicus.tobaccocellar.ui.utilities.toLocalizedDouble
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.text.DecimalFormatSymbols
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TinRatesDialog(
    onDismiss: () -> Unit,
    loading: Boolean,
    ozRate: Double,
    gramsRate: Double,
    onSave: (Double, Double) -> Unit
) {
    val ozState = rememberTextFieldState(formatDecimal(ozRate))
    val gramsState = rememberTextFieldState(formatDecimal(gramsRate))
    val focusManager = LocalFocusManager.current
    var saving by remember { mutableStateOf(false) }
    var debouncedLoading by remember { mutableStateOf(false) }

    LaunchedEffect(loading) {
        if (loading) {
            delay(50.milliseconds)
            debouncedLoading = true
        } else { debouncedLoading = false }
    }

    LaunchedEffect(saving, loading) {
        if (saving && !loading) {
            focusManager.clearFocus()
            delay(25.milliseconds)
            onDismiss()
        }
        else if (saving) {
            focusManager.clearFocus()
            snapshotFlow { loading }.first { !it }
            onDismiss()
        }
    }

    DisposableEffect(Unit) { onDispose { focusManager.clearFocus() } }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        text = {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                        .alpha(if (debouncedLoading) .38f else 1f),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .offset(x = (-8).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.tin_conversion_one_is_v2),
                            modifier = Modifier.padding(end = 8.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.Start
                        ) {
                            val symbols = remember { DecimalFormatSymbols.getInstance(LanguageState.locale) }
                            val decimalSeparator = symbols.decimalSeparator.toString()
                            val allowedPattern = remember(decimalSeparator) {
                                val ds = Regex.escape(decimalSeparator)
                                Regex("^(\\s*|\\d{0,4}?($ds\\d{0,2})?)$")
                            }
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                CustomTextField(
                                    state = ozState,
                                    inputTransformation = {
                                        if (!this.toString().matches(allowedPattern)) {
                                            revertAllChanges()
                                        }
                                    },
                                    modifier = Modifier.width(100.dp),
                                    textStyle = LocalTextStyle.current.copy(
                                        textDirection = TextDirection.Ltr,
                                        textAlign = TextAlign.End
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedContainerColor = LocalCustomColors.current.textField,
                                        unfocusedContainerColor = LocalCustomColors.current.textField,
                                        disabledContainerColor = LocalCustomColors.current.textField,
                                        cursorColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    enabled = !debouncedLoading,
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    contentPadding = PaddingValues(12.dp, 6.dp),
                                    placeholder = {
                                        Text(
                                            text = stringResource(R.string.parenthetical_v2, formatDecimal(ozRate)),
                                            modifier = Modifier.fillMaxWidth(),
                                            style = LocalTextStyle.current.copy(
                                                textAlign = TextAlign.End,
                                                fontSize = 14.sp,
                                                lineHeight = 1.em,
                                                color = LocalContentColor.current.copy(alpha = .38f),
                                            )
                                        )
                                    },
                                    suffix = {
                                        Text(
                                            text = "oz",
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(start = 2.dp),
                                            color = LocalContentColor.current.copy(alpha = .8f)
                                        )
                                    }
                                )
                            }
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                CustomTextField(
                                    state = gramsState,
                                    inputTransformation = {
                                        if (!this.toString().matches(allowedPattern)) {
                                            revertAllChanges()
                                        }
                                    },
                                    modifier = Modifier.width(100.dp),
                                    textStyle = LocalTextStyle.current.copy(
                                        textDirection = TextDirection.Ltr,
                                        textAlign = TextAlign.End
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedContainerColor = LocalCustomColors.current.textField,
                                        unfocusedContainerColor = LocalCustomColors.current.textField,
                                        disabledContainerColor = LocalCustomColors.current.textField,
                                        cursorColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    enabled = !debouncedLoading,
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    contentPadding = PaddingValues(12.dp, 6.dp),
                                    placeholder = {
                                        Text(
                                            text = stringResource(R.string.parenthetical_v2, formatDecimal(gramsRate)),
                                            modifier = Modifier.fillMaxWidth(),
                                            style = LocalTextStyle.current.copy(
                                                textAlign = TextAlign.End,
                                                fontSize = 14.sp,
                                                lineHeight = 1.em,
                                                color = LocalContentColor.current.copy(alpha = .38f),
                                            )
                                        )
                                    },
                                    suffix = {
                                        Text(
                                            text = "g",
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(start = 2.dp),
                                            color = LocalContentColor.current.copy(alpha = .8f)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                if (debouncedLoading) { LoadingIndicator(center = true, modifier = Modifier.matchParentSize()) }
            }
        },
        dismissButton = { TextButton({ onDismiss() }, enabled = !debouncedLoading) { Text(stringResource(R.string.cancel_v2)) } },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(ozState.text.toString().toLocalizedDouble() ?: ozRate, gramsState.text.toString().toLocalizedDouble() ?: gramsRate)
                    saving = true
                },
                enabled = !debouncedLoading
            ) { Text(stringResource(R.string.save_v2)) }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}