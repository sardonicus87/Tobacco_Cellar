package com.sardonicus.tobaccocellar.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.getLocalizedComma
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.stripAccent
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.stripControlChar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

/** TextField with AutoComplete suggestions */
@Composable
fun AutoCompleteText(
    state: TextFieldState,
    allItems: List<String>,
    onOptionSelected: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Inside(),
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current,
    interactionSource: MutableInteractionSource? = null,
    componentField: Boolean = false,
) {
    var suggestionsState by remember { mutableStateOf<List<String>>(emptyList()) }
    var override by remember { mutableStateOf(false) }
    var focusInteractionCount by remember { mutableIntStateOf(0) }
    var expandedState by remember { mutableStateOf(false) }

    var fieldY by remember { mutableFloatStateOf(0f) }
    var menuY by remember { mutableFloatStateOf(0f) }

    var listCache by remember { mutableStateOf<List<String>>(emptyList()) }

    val input = remember(state.text.toString(), state.selection) {
        getInput(
            text = state.text.toString(),
            cursorPosition = state.selection.start,
            componentField = componentField
        )
    }

    LaunchedEffect(state, Dispatchers.Default) {
        snapshotFlow { state.text.toString() }.collectLatest {
            if (focusInteractionCount > 0) focusInteractionCount++
        }
    }

    SideEffect(input) {
        val newSuggestions = getSuggestions(input, state.text.toString(), allItems, componentField)
        suggestionsState = newSuggestions

        if (expandedState && newSuggestions.isNotEmpty()) { listCache = newSuggestions }
    }

    SideEffect(expandedState) {
        if (expandedState && suggestionsState.isNotEmpty()) { listCache = suggestionsState }
    }

    SideEffect(input, suggestionsState, override, state.text.toString(), focusInteractionCount) {
        expandedState = input.length >= 2 && focusInteractionCount > 2 && suggestionsState.isNotEmpty() && !override && state.text.toString().isNotBlank()
    }

    LaunchedEffect(override) {
        if (override) {
            delay(250.milliseconds)
            override = false
            if (componentField) { suggestionsState = emptyList() }
        }
    }

    BackHandler(expandedState) { expandedState = false }

    val focusManager = LocalFocusManager.current
    DisposableEffect(Unit) { onDispose { expandedState = false; focusManager.clearFocus() } }

    Box(modifier = modifier) {
        TextField(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .onFocusChanged {
                    if (it.isFocused) { if (focusInteractionCount == 0) focusInteractionCount = 1 }
                    else { expandedState = false; focusInteractionCount = 0 }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press) {
                                if (focusInteractionCount > 0) focusInteractionCount++
                            }
                        }
                    }
                }
                .onGloballyPositioned { fieldY = it.positionOnScreen().y },
            enabled = enabled,
            trailingIcon = trailingIcon,
            lineLimits = lineLimits,
            placeholder = placeholder,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            keyboardOptions = keyboardOptions,
            textStyle = textStyle,
            labelPosition = labelPosition,
            label = label,
            colors = colors.copy(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.50f),
            ),
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.extraSmall,
            supportingText = supportingText,
        )

        val yOffset = if (fieldY > menuY) 8.dp else (-8).dp

        DropdownMenu(
            expanded = expandedState,
            onDismissRequest = { },
            modifier = Modifier
                .padding(0.dp)
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
                        val currentText = state.text.toString()
                        val cursorPosition = state.selection.start

                        val separator = when {
                            currentText.contains('،') -> "، "
                            currentText.contains('、') -> "、"
                            currentText.contains(',') -> ", "
                            else -> getLocalizedComma(LanguageState.locale)
                        }

                        state.edit {
                            if (componentField && currentText.contains(Regex("[,،、]\\s*"))) {
                                val delimiterIndices =
                                    currentText.mapIndexedNotNull { index, char ->
                                        when (char) {
                                            ',', '،', '、', ' ' -> index
                                            else -> null
                                        }
                                    }.toMutableList().apply {
                                        add(-1)
                                        add(currentText.length)
                                    }.sorted()
                                val startIndex = delimiterIndices.last { it < cursorPosition }
                                val endIndex = delimiterIndices.first { it >= cursorPosition }

                                replace(startIndex +1, endIndex, "$label$separator")
                                val finalCursorPos =
                                    if (startIndex == -1) { label.length + separator.length }
                                    else if (endIndex == currentText.length) { length }
                                    else { (startIndex + 1) + label.length + 1 }

                                selection = TextRange(finalCursorPos.coerceIn(0, length))
                            }
                            else {
                                val replacement = if (componentField) "$label$separator" else label
                                replace(0, length, replacement)
                                placeCursorAtEnd()
                            }
                        }

                        override = true
                        expandedState = false
                        focusInteractionCount = if (componentField) focusInteractionCount else 1
                        onOptionSelected?.invoke(state.text.toString())
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
            .clickable(null, LocalIndication.current) { onClick() }
            .padding(0.dp)
    ) { text() }
}


private fun getInput(text: String, cursorPosition: Int, componentField: Boolean): String {
    if (!componentField) return text.stripControlChar()

    val delimiterIndices = text.stripControlChar().mapIndexedNotNull { index, char ->
        when (char) {
            ',', '،', '、' -> index
            else -> null
        }
    }.toMutableList().apply {
        add(-1)
        add(text.length)
    }.sorted()
    val startIndex = delimiterIndices.last { it < cursorPosition }
    val endIndex = delimiterIndices.first { it >= cursorPosition }

    return text.substring(startIndex + 1, endIndex).trim()
}

private fun getSuggestions(
    input: String,
    fullText: String,
    allItems: List<String>,
    componentField: Boolean
): List<String> {
    if (input.length < 2) return emptyList()

    val selectedInput = if (componentField && fullText.isNotBlank()) {
        fullText.split(Regex("[,،、]")).map { it.trim() }.filter { it.isNotBlank() }
    } else { emptyList() }

    val selected = allItems.filter {
        if (componentField) selectedInput.contains(it)
        else it.equals(input, ignoreCase = false)
    }.toSet()

    val cleanInput = input.stripAccent()

    val scoreResults = allItems.mapNotNull { brand ->
        val cleanBrand = brand.stripAccent()
        val exactSpecial = brand != cleanBrand
        val score = when {
            brand.startsWith(input, ignoreCase = true) && exactSpecial -> 0
            cleanBrand.startsWith(cleanInput, ignoreCase = true) -> 1
            cleanBrand.split(" ").any { it.startsWith(cleanInput, ignoreCase = true) } -> 2
            cleanBrand.contains(cleanInput, ignoreCase = true) -> 3
            else -> null
        }
        if (score != null) score to brand else null
    }.filter { it.second !in selected }

    val hasPrimaryMatches = scoreResults.any { it.first < 2 }

    return scoreResults.filter { (score, _) ->
        when (score) {
            0, 1 -> true
            2 -> hasPrimaryMatches || input.length > 3
            else -> false
        }
    }.sortedBy { it.first }.map { it.second } - selected
}