package com.sardonicus.tobaccocellar.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay

/** TextField with AutoComplete suggestions */
@Composable
fun AutoCompleteText(
    value: String,
    onValueChange: ((String) -> Unit)?,
    allItems: List<String>,
    onOptionSelected: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current,
    componentField: Boolean = false,
) {
    var suggestionsState by remember { mutableStateOf<List<String>>(emptyList()) }
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(value)) }
    var override by remember { mutableStateOf(false) }
    var expandedState by remember { mutableStateOf(false) }

    var fieldY by remember { mutableFloatStateOf(0f) }
    var menuY by remember { mutableFloatStateOf(0f) }
    var typeCount by remember { mutableIntStateOf(0) }

    var listCache by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(value) {
        expandedState = if (value.length >= 2 && typeCount >= 2) {
            suggestionsState.isNotEmpty() && !override
        } else {
            false
        }
    }

    LaunchedEffect(expandedState) {
        if (!expandedState) {
            delay(250)
            listCache = emptyList()
        }
    }

    LaunchedEffect(override) {
        if (override) {
            delay(250)
            override = false
            if (componentField) {
                suggestionsState = emptyList()
            }
        }
    }

    BackHandler(enabled = expandedState) { expandedState = false }

    Box(modifier = modifier) {
        TextField(
            value = textFieldValueState.copy(text = value),
            onValueChange = { textFieldValue ->
                textFieldValueState = textFieldValue
                val text = textFieldValue.text
                onValueChange?.invoke(text)

                val input =
                    if (componentField && text.contains(", ")) {
                        val cursorPosition = textFieldValueState.selection.start
                        val delimiterIndices = text.mapIndexedNotNull { index, char ->
                            when (char) {
                                ',' -> index
                                ' ' -> index
                                else -> null
                            }
                        }.toMutableList().apply {
                            add(-1)
                            add(text.length)
                        }.sorted()
                        val startIndex = delimiterIndices.last { it < cursorPosition }
                        val endIndex = delimiterIndices.first { it >= cursorPosition }

                        text.substring(startIndex + 1, endIndex).trim()
                    } else { text }

                if (input.isNotBlank()) typeCount++

                if (input.length >= 2 && typeCount >= 2) {
                    val startsWith = allItems.filter { it.startsWith(input, ignoreCase = true) }
                    val otherWordsStartsWith = allItems.filter { string ->
                        string.split(" ").drop(1)
                            .any {
                                it.startsWith(input, ignoreCase = true) }
                                && !startsWith.contains(string)
                    }
                    val contains = allItems.filter {
                        it.contains(input, ignoreCase = true)
                                && !startsWith.contains(it) && !otherWordsStartsWith.contains(it)
                    }

                    val selectedInput =
                        if (componentField) {
                            if (text.isNotBlank()) {
                                text.split(", ").map { it.trim() }.filter { it.isNotBlank() } }
                            else { emptyList() } }
                        else { listOf(input) }
                    val selected = allItems.filter {
                        if (componentField) { selectedInput.contains(it) }
                        else { it.equals(input, ignoreCase = false) } }.toSet()

                    val newSuggestions = (startsWith + otherWordsStartsWith + contains) - selected
                    suggestionsState = newSuggestions

                    if (newSuggestions.isNotEmpty()) {
                        listCache = newSuggestions
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .onFocusChanged {
                    if (!it.isFocused) {
                        expandedState = false
                        typeCount = 0
                    }
                }
                .onGloballyPositioned { fieldY = it.positionOnScreen().y },
            enabled = enabled,
            trailingIcon = trailingIcon,
            singleLine = true,
            placeholder = placeholder,
            keyboardOptions = keyboardOptions,
            textStyle = textStyle,
            colors = colors.copy(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.50f),
            ),
            shape = MaterialTheme.shapes.extraSmall,
            label = label,
            maxLines = maxLines,
            minLines = minLines,
            supportingText = supportingText,
        )

        val yOffset = if (fieldY > menuY) 8.dp else (-8).dp

        DropdownMenu(
            expanded = expandedState,
            onDismissRequest = { /**/ },
            modifier = Modifier
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                .height(intrinsicSize = IntrinsicSize.Max)
                .onGloballyPositioned { menuY = it.positionOnScreen().y },
            properties = PopupProperties(focusable = false),
            offset = DpOffset(32.dp, yOffset),
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            val displayed = if (expandedState) suggestionsState else listCache
            displayed.take(3).forEach { label ->
                CustomDropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            modifier = Modifier
                                .padding(0.dp)
                                .focusable(false),
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        val currentText = textFieldValueState.text
                        val cursorPosition = textFieldValueState.selection.start

                        val newValue =
                            if (componentField && currentText.contains(", ")) {
                                val delimiterIndices = currentText.mapIndexedNotNull { index, char ->
                                    when (char) {
                                        ',' -> index
                                        ' ' -> index
                                        else -> null
                                    }
                                }.toMutableList().apply {
                                    add(-1)
                                    add(currentText.length)
                                }.sorted()
                                val startIndex = delimiterIndices.last { it < cursorPosition }
                                val endIndex = delimiterIndices.first { it >= cursorPosition }

                                val updatedText = currentText.replaceRange(startIndex + 1, endIndex, " $label, ")
                                    .trimStart().replace("  ", " ").replace(", ,", ",")
                                val finalCursorPos = if (startIndex == -1) {
                                    label.length + 2
                                } else if (endIndex == currentText.length) {
                                    updatedText.length
                                }
                                else { (startIndex + 1) + label.length + 1 }

                                TextFieldValue(
                                    text = updatedText,
                                    selection = TextRange(finalCursorPos.coerceIn(0, updatedText.length))
                                )
                            } else {
                                val updatedText = if (componentField) { "$label, " } else { label }
                                TextFieldValue(
                                    text = updatedText,
                                    selection = TextRange(updatedText.length)
                                )
                            }

                        textFieldValueState = newValue

                        override = true
                        typeCount = 0
                        expandedState = false
                        onOptionSelected?.invoke(newValue.text)
                    },
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp)
                        .offset(0.dp, 0.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CustomDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .clickable(
                indication = LocalIndication.current,
                interactionSource = null
            ) { onClick() }
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
    ) {
        text()
    }
}
