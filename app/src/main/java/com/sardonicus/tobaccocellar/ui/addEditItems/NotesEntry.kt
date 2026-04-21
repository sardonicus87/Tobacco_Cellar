package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun NotesEntry(
    itemDetails: ItemDetails,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
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
                text = "Notes:",
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            TextField(
                value = itemDetails.notes,
                onValueChange = {
                    var updatedText = it
                    if (it.contains("\n")) {
                        val lines = it.lines()
                        if (lines.size > 1) {
                            val lastLine = lines[lines.size - 2]
                            val currentLine = lines.last()
                            val lastWord = lastLine.substringAfterLast(" ")
                            if (currentLine.startsWith(lastWord) && currentLine.length > 1) {
                                updatedText = if (currentLine.length == lastWord.length + 1) {
                                    it.dropLast(lastWord.length + 1)
                                } else {
                                    it.dropLast(lastWord.length)
                                }
                            }
                        }
                    }
                    onValueChange(itemDetails.copy(notes = updatedText))
                },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None,
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = LocalCustomColors.current.textField,
                    unfocusedContainerColor = LocalCustomColors.current.textField,
                    disabledContainerColor = LocalCustomColors.current.textField,
                ),
                shape = MaterialTheme.shapes.extraSmall,
                singleLine = false,
                maxLines = 8,
                minLines = 8,
            )
        }
    }
}