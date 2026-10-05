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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

@Composable
fun RatingPopup(
    onDismiss: () -> Unit,
    onRatingSelected: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    currentRating: Double? = null,
) {
    var currentString by remember { mutableStateOf(formatDecimal(currentRating)) }
    var parsedDouble by remember { mutableStateOf<Double?>(null) }

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
                text = stringResource(R.string.rating),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
                val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
                val decimalSeparator = symbols.decimalSeparator.toString()
                val allowedPattern = remember(decimalSeparator) {
                    val ds = Regex.escape(decimalSeparator)
                    Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
                }
                Text(
                    text = stringResource(R.string.rating_desc),
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
                        value = currentString,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                currentString = it

                                try {
                                    if (it.isNotBlank()) {
                                        val preNumber =
                                            if (it.startsWith(decimalSeparator)) { "0$it" }
                                            else it
                                        val number = numberFormat.parse(preNumber)
                                        parsedDouble = number?.toDouble() ?: 0.0
                                        if (parsedDouble!! > 5.0) { parsedDouble = 5.0 }
                                    } else { parsedDouble = null }
                                } catch (_: ParseException) { }
                            }
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .padding(end = 8.dp),
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
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
                    val alpha = if (currentString.isNotBlank()) .75f else 0.38f
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(null, LocalIndication.current, currentString.isNotBlank()) {
                                currentString = ""; parsedDouble = null }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(alpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRatingSelected(parsedDouble) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.done)) }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.cancel)) }
        }
    )
}