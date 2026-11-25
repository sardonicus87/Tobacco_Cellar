package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.delete
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState

@Composable
fun NotesEntry(
    itemDetails: ItemDetails,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Notes //
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.notes_v2),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            var lastTime = remember { 0L }
            TextField(
                state = itemDetails.notes,
                inputTransformation = {
                    val text = this.asCharSequence().toString()
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastTime < 50L) {
                        val lines = text.lines()
                        if (lines.size > 1) {
                            val lastLine = lines[lines.size - 2]
                            val currentLine = lines.last()
                            val lastWord = lastLine.substringAfterLast(" ")

                            if (currentLine.isNotEmpty() && currentLine == lastWord) {
                                delete(length - lastWord.length, length)
                            }
                        }
                    }
                    lastTime = currentTime
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None,
                ),
                lineLimits = TextFieldLineLimits.MultiLine(8, 8),
                textStyle = LocalTextStyle.current.copy(
                    textDirection = if (LanguageState.isRtl) TextDirection.Rtl else TextDirection.Ltr
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
        }
    }
}