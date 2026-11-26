package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import com.sardonicus.tobaccocellar.ui.utilities.localizedToDouble
import java.text.DecimalFormatSymbols

@Composable
fun RatingPopup(
    onDismiss: () -> Unit,
    onRatingSelected: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    currentRating: Double? = null,
) {
    val currentString = rememberTextFieldState(formatDecimal(currentRating))
    val focusManager = LocalFocusManager.current
    DisposableEffect(Unit) { onDispose { focusManager.clearFocus() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.wrapContentHeight(),
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = stringResource(R.string.rating_v2),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                val symbols = remember { DecimalFormatSymbols.getInstance(LanguageState.locale) }
                val decimalSeparator = symbols.decimalSeparator.toString()
                val allowedPattern = remember(decimalSeparator) {
                    val ds = Regex.escape(decimalSeparator)
                    Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
                }
                Text(
                    text = stringResource(R.string.rating_desc_v2),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(6.dp))
                    TextField(
                        state = currentString,
                        inputTransformation = { if (!this.toString().matches(allowedPattern)) { revertAllChanges() } },
                        modifier = Modifier
                            .width(80.dp)
                            .padding(end = 8.dp),
                        enabled = true,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.End,
                            textDirection = TextDirection.Ltr
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField,
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    val alpha = if (currentString.text.isNotBlank()) .75f else 0.38f
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(null, LocalIndication.current, currentString.text.isNotBlank()) {
                                currentString.clearText() }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(alpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRatingSelected(currentString.text.toString().localizedToDouble(max = 5.0)) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.done_v2)) }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.cancel_v2)) }
        }
    )
}