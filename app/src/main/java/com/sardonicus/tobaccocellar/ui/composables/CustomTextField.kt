package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation

//@Composable
//fun CustomTextField(
//    value: String,
//    onValueChange: (String) -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    readOnly: Boolean = false,
//    textStyle:  TextStyle = TextStyle.Default,
//    placeholder: @Composable (() -> Unit)? = null,
//    leadingIcon: @Composable (() -> Unit)? = null,
//    trailingIcon: @Composable (() -> Unit)? = null,
//    prefix: @Composable (() -> Unit)? = null,
//    suffix: @Composable (() -> Unit)? = null,
//    supportingText: @Composable (() -> Unit)? = null,
//    isError: Boolean = false,
//    visualTransformation: VisualTransformation = VisualTransformation.None,
//    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
//    keyboardActions: KeyboardActions = KeyboardActions.Default,
//    singleLine: Boolean = false,
//    maxLines: Int = if (singleLine) 1 else Int. MAX_VALUE,
//    minLines: Int = 1,
//    interactionSource: MutableInteractionSource? = null,
//    shape: Shape = TextFieldDefaults.shape,
//    colors: TextFieldColors = TextFieldDefaults.colors(),
//) {
//    var showCursor by remember { mutableStateOf(false) }
//    var hasFocus by remember { mutableStateOf(false) }
//    val focusManager = LocalFocusManager.current
//
//    BasicTextField(
//        value = value,
//        onValueChange = onValueChange,
//        modifier = modifier
//            .onFocusChanged { focusState ->
//                hasFocus = focusState.hasFocus
//                showCursor = focusState.hasFocus
//                if (!focusState.hasFocus) {
//                    focusManager.clearFocus()
//                }
//            }
//            .background(colors.unfocusedContainerColor, shape),
//        enabled = enabled,
//        readOnly = readOnly,
//        textStyle = textStyle,
//        keyboardOptions = keyboardOptions,
//        keyboardActions = keyboardActions,
//        singleLine = singleLine,
//        maxLines = maxLines,
//        minLines = minLines,
//        visualTransformation = visualTransformation,
//        interactionSource = interactionSource,
//        cursorBrush =
//            if (showCursor) {
//                if (isError) SolidColor(colors.errorCursorColor)
//                else SolidColor(colors.cursorColor)
//            }
//            else { SolidColor(Color.Transparent) },
//        decorationBox = { innerTextField ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Start,
//            ) {
//                if (leadingIcon != null) leadingIcon()
//                if (prefix != null) prefix()
//                Box(
//                    modifier = Modifier
//                        .background(
//                            color =
//                            if (enabled) {
//                                if (hasFocus) colors.focusedContainerColor
//                                else if (isError) colors.errorContainerColor
//                                else colors.unfocusedContainerColor
//                            } else colors.disabledContainerColor,
//                            shape = shape
//                        )
//                        .weight(1f)
//                        .padding(horizontal = 4.dp),
//                    contentAlignment = Alignment.CenterStart
//                ) {
//                    if (value.isEmpty() && !hasFocus) {
//                        if (placeholder != null) placeholder()
//                    }
//                    innerTextField()
//                }
//                if (suffix != null) suffix()
//                if (trailingIcon != null) trailingIcon()
//                if (supportingText != null) supportingText()
//            }
//        }
//    )
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle:  TextStyle = LocalTextStyle.current,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int. MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding:  PaddingValues = contentPaddingWithoutLabel(),
) {
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle.copy(color = LocalContentColor.current),
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
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
            interactionSource = interactionSource,
            colors = colors,
            shape = shape,
            contentPadding = contentPadding,
        )
    }
}


//    focusedTextColor: Color = Color. Unspecified,
//    unfocusedTextColor: Color = Color. Unspecified,
//    disabledTextColor: Color = Color. Unspecified,
//    errorTextColor: Color = Color. Unspecified,
//

//    selectionColors: TextSelectionColors? = null,
//
//    focusedIndicatorColor: Color = Color. Unspecified,
//    unfocusedIndicatorColor: Color = Color. Unspecified,
//    disabledIndicatorColor: Color = Color. Unspecified,
//    errorIndicatorColor: Color = Color. Unspecified,
//
//    focusedLeadingIconColor: Color = Color. Unspecified,
//    unfocusedLeadingIconColor: Color = Color. Unspecified,
//    disabledLeadingIconColor: Color = Color. Unspecified,
//    errorLeadingIconColor: Color = Color. Unspecified,
//
//    focusedTrailingIconColor: Color = Color. Unspecified,
//    unfocusedTrailingIconColor: Color = Color. Unspecified,
//    disabledTrailingIconColor: Color = Color. Unspecified,
//    errorTrailingIconColor: Color = Color. Unspecified,
//
//    focusedLabelColor: Color = Color. Unspecified,
//    unfocusedLabelColor: Color = Color. Unspecified,
//    disabledLabelColor: Color = Color. Unspecified,
//    errorLabelColor: Color = Color. Unspecified,
//
//    focusedPlaceholderColor: Color = Color. Unspecified,
//    unfocusedPlaceholderColor: Color = Color. Unspecified,
//    disabledPlaceholderColor: Color = Color. Unspecified,
//    errorPlaceholderColor: Color = Color. Unspecified,
//
//    focusedSupportingTextColor: Color = Color. Unspecified,
//    unfocusedSupportingTextColor: Color = Color. Unspecified,
//    disabledSupportingTextColor: Color = Color. Unspecified,
//    errorSupportingTextColor: Color = Color. Unspecified,
//
//    focusedPrefixColor: Color = Color. Unspecified,
//    unfocusedPrefixColor: Color = Color. Unspecified,
//    disabledPrefixColor: Color = Color. Unspecified,
//    errorPrefixColor: Color = Color. Unspecified,
//
//    focusedSuffixColor: Color = Color. Unspecified,
//    unfocusedSuffixColor: Color = Color. Unspecified,
//    disabledSuffixColor: Color = Color. Unspecified,
//    errorSuffixColor: Color = Color. Unspecified
