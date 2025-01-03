package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
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

/** Dropdown menu item with inner-padding access */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMenuItem(
    text: @Composable (() -> Unit),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(
        textColor = LocalContentColor.current
    ),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
//    val contentColor = when {
//        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_DISABLED)
//        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_FULL)
//    }
//
//    CompositionLocalProvider(LocalContentColor provides contentColor) {
//    }

//    val textString = String()
//
//    Box(
//        modifier = modifier
//            .clickable(enabled) { onClick() }
//            .fillMaxWidth()
//            .padding(contentPadding),
//    ) {
//        text(
//        )
//        if (enabled) {
//            colors.copy(
//                textColor = colors.textColor
//            )
//        }
//        if (leadingIcon != null) {
//            leadingIcon()
//        }
//        if (trailingIcon != null) {
//            trailingIcon()
//        }
//    }

    Box(
        modifier = modifier
            .clickable(enabled) { onClick() }
            .fillMaxWidth()
            .padding(contentPadding),
    ) {
        text()
        if (enabled) {
            colors.copy(
                textColor = colors.textColor
            )
        }
    }

}

/** TextField with inner-padding access */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
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
        textStyle = textStyle,
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
            colors = colors.copy(
                cursorColor = MaterialTheme.colorScheme.primary
            ),
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
