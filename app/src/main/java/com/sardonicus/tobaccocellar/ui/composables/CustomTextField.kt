package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation

/** TextField with inner-padding access */
@Suppress("DEPRECATION")
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues = contentPaddingWithoutLabel(),
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
) {
    val interactionSourceImp = interactionSource ?: remember { MutableInteractionSource() }

    val textColor = textStyle.color.takeOrElse {
        val focused = interactionSourceImp.collectIsFocusedAsState().value
        when {
            !enabled -> colors.disabledTextColor
            isError -> colors.errorTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

//    val textFieldState = rememberTextFieldState(initialText = value)
//
//    LaunchedEffect(value) {
//        if (textFieldState.text.toString() != value) {
//            textFieldState.edit { replace(0, length, value) }
//        }
//    }
//
//    LaunchedEffect(textFieldState) {
//        snapshotFlow { textFieldState.text.toString() }
//            .collectLatest { if (it != value) onValueChange(it) }
//    }

//    val lineLimits =
//        if (singleLine) { TextFieldLineLimits.SingleLine }
//        else { TextFieldLineLimits.MultiLine(minHeightInLines = minLines, maxHeightInLines = maxLines) }
//
//    val actionScope = remember {
//        object : KeyboardActionScope {
//            override fun defaultKeyboardAction(imeAction: ImeAction) {}
//        }
//    }

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            visualTransformation = visualTransformation,
            interactionSource = interactionSourceImp,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
        ) { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSourceImp,
                colors = colors.copy(cursorColor = MaterialTheme.colorScheme.primary),
                shape = shape,
                contentPadding = contentPadding,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
            )
        }

//        BasicTextField(
//            state = textFieldState,
//            modifier = modifier,
//            enabled = enabled,
//            readOnly = readOnly,
//            textStyle = mergedTextStyle,
//            keyboardOptions = keyboardOptions,
//            onKeyboardAction = { action ->
//                action()
//                when (keyboardOptions.imeAction) {
//                    ImeAction.Done -> keyboardActions.onDone?.invoke(actionScope)
//                    ImeAction.Next -> keyboardActions.onNext?.invoke(actionScope)
//                    ImeAction.Search -> keyboardActions.onSearch?.invoke(actionScope)
//                    ImeAction.Send -> keyboardActions.onSend?.invoke(actionScope)
//                    ImeAction.Go -> keyboardActions.onGo?.invoke(actionScope)
//                    ImeAction.Previous -> keyboardActions.onPrevious?.invoke(actionScope)
//                    else -> {}
//                }
//            },
//            lineLimits = lineLimits,
//            interactionSource = interactionSource,
//            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
//       //     outputTransformation = outputTransformation,
//            decorator = { innerTextField ->
//                TextFieldDefaults.DecorationBox(
//                    value = value,
//                    innerTextField = innerTextField,
//                    enabled = enabled,
//                    singleLine = singleLine,
//                    visualTransformation = visualTransformation,
//                    interactionSource = interactionSourceImp,
//                    isError = isError,
//                    label = {},
//                    placeholder = placeholder,
//                    leadingIcon = leadingIcon,
//                    trailingIcon = trailingIcon,
//                    prefix = prefix,
//                    suffix = suffix,
//                    supportingText = supportingText,
//                    shape = shape,
//                    colors = colors.copy(cursorColor = MaterialTheme.colorScheme.primary),
//                    contentPadding = contentPadding
//                )
//            }
//        )
    }
}