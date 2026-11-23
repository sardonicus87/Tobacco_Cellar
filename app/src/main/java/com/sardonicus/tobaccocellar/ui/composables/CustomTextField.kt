package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState

/** TextField with inner-padding access */
@Composable
fun CustomTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    inputTransformation: InputTransformation? = null,
    isError: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues = contentPaddingWithoutLabel(),
    interactionSource: MutableInteractionSource? = null,
    outputTransformation: OutputTransformation? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    overridePlaceholder: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
//    supportingText: @Composable (() -> Unit)? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

    val focused = enabled && !readOnly && interactionSource.collectIsFocusedAsState().value
    val focusManager = LocalFocusManager.current

    val textColor = textStyle.color.takeOrElse {
        when {
            !enabled -> colors.disabledTextColor
            isError -> colors.errorTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    DisposableEffect(Unit) { onDispose { focusManager.clearFocus() } }

    val maxLines = lineLimits.let {
        if (it is TextFieldLineLimits.MultiLine) { it.minHeightInLines }
        else 1
    }

    val color = colors.containerColor(enabled, isError, focused)
    if(!enabled || readOnly) {
        Box(modifier){
            CustomDecorationBox(
                state.text.toString(), shape, color, mergedTextStyle, maxLines, Modifier,
                contentPadding, prefix, suffix, leadingIcon, null, false, trailingIcon
            )
        }
        return
    }

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            state = state,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            inputTransformation = inputTransformation,
            textStyle = mergedTextStyle,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            outputTransformation = outputTransformation,
            decorator = { innerTextField ->
                CustomDecorationBox(
                    state.text.toString(), shape, color, mergedTextStyle, maxLines,
                    Modifier
                        .indicatorLine(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            textFieldShape = shape,
                            focusedIndicatorLineThickness = 2.dp,
                            unfocusedIndicatorLineThickness = 1.dp,
                        ),
                    contentPadding, prefix, suffix, leadingIcon, placeholder, !overridePlaceholder,
                    trailingIcon, innerTextField
                )
            }
        )
    }
}

@Composable
private fun CustomDecorationBox(
    value: String,
    shape: Shape,
    color: Color,
    style: TextStyle,
    maxLines: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = contentPaddingWithoutLabel(),
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    showPlaceholder: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    innerTextField: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clip(shape)
            .background(color, shape)
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        leadingIcon?.invoke()
        prefix?.invoke()
        val contentRtl = value.isNotEmpty() &&
                (value.first().code in 0x0590..0x05FF ||
                        value.first().code in 0x0600..0x06FF)

        val boxAlignment = when (style.textAlign) { // dynamic side-switch when input doesn't match layout direction
            TextAlign.Center -> Alignment.Center
            TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
            TextAlign.Start, TextAlign.Left -> Alignment.CenterStart
            else -> if (LanguageState.isRtl == contentRtl) Alignment.CenterStart else Alignment.CenterEnd
        }

        Box(Modifier.weight(1f).padding(horizontal = 2.dp), boxAlignment) {
            Box(modifier) { // passed indicator line
                innerTextField?.invoke() ?: Text(
                    text = value, style = style, maxLines = maxLines, overflow = TextOverflow.Clip
                )
                if (value.isBlank() && showPlaceholder) { placeholder?.invoke() }
            }
        }
        suffix?.invoke()
        trailingIcon?.invoke()
    }
}