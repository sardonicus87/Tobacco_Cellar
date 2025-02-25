package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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

    TextField(
        value = "",
        onValueChange = {},
        modifier = modifier
    )

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


/** Fade effect boxes. Parent must be a box and must add ".matchParentSize()" and ".align(Alignment.TopStart)" to modifier */
@Composable
fun VerticalFadeBox(
    fadeColor: Color,
    fadeHeight: Dp,
    modifier: Modifier = Modifier,
    topFade: Boolean = true,
    bottomFade: Boolean = true,
) {
    Box(
        modifier = modifier
            .then(
                Modifier.drawBehind {
                    val fadeOffsetY =
                        size.height - (fadeHeight.toPx())
                    if (topFade) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    fadeColor,
                                    Color.Transparent,
                                ),
                                startY = 0f,
                                endY = fadeHeight.toPx(),
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, fadeHeight.toPx())
                        )
                    }
                    if (bottomFade) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    fadeColor,
                                ),
                                startY = fadeOffsetY,
                                endY = size.height,
                            ),
                            topLeft = Offset(0f, fadeOffsetY),
                            size = Size(size.width, fadeHeight.toPx())
                        )
                    }
                }
            )
    )
}

/** Fade effect boxes. Parent must be a box and must add ".matchParentSize()" and ".align(Alignment.TopStart)" to modifier. */
@Composable
fun HorizontalFadeBox(
    fadeColor: Color,
    fadeWidth: Dp,
    modifier: Modifier = Modifier,
    startFade: Boolean = true,
    endFade: Boolean = true,
) {
    Box(
        modifier = modifier
            .then(
                Modifier.drawBehind {
                    val fadeOffsetX =
                        size.width - (fadeWidth.toPx())
                    if (startFade) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    fadeColor,
                                    Color.Transparent,
                                ),
                                startX = 0f,
                                endX = fadeWidth.toPx(),
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(fadeWidth.toPx(), size.height)
                        )
                    }
                    if (endFade) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    fadeColor,
                                ),
                                startX = fadeOffsetX,
                                endX = size.width,
                            ),
                            topLeft = Offset(fadeOffsetX, 0f),
                            size = Size(fadeWidth.toPx(), size.height)
                        )
                    }
                }
            )
    )
}


/** Full screen loading indicator */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    skrimColor: Color = Color.Transparent,
    skrimAlpha: Float = 0.5f
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(skrimColor.copy(alpha = skrimAlpha))
    ) {
        Spacer(
            modifier = Modifier
                .weight(1.5f)
        )
        CircularProgressIndicator(
            modifier = Modifier
                .padding(0.dp)
                .size(48.dp)
                .weight(0.5f),
        )
        Spacer(
            modifier = Modifier
                .weight(2f)
        )
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
    isError: Boolean = false,
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
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
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
            isError = isError,
            interactionSource = interactionSource,
            colors = colors.copy(
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = shape,
            contentPadding = contentPadding,
            leadingIcon = leadingIcon,
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
        )
    }
}


/** Auto-resizing Text() composable **/
@Composable
fun AutoSizeText(
    text: String,
    fontSize: TextUnit,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = LocalTextStyle.current.lineHeight,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    contentAlignment: Alignment = Alignment.Center
) {
    val coroutineScope = rememberCoroutineScope()
    var fontMultiplier by remember { mutableFloatStateOf(1f) }
    fun updateFontSize(multiplier: Float) {
        val newSize = fontSize * multiplier
        if (newSize > minFontSize) {
            fontMultiplier = multiplier
        }
    }

    Box (
        modifier = Modifier
            .width(width)
            .height(height),
        contentAlignment = contentAlignment
    ) {
        Text(
            text = text,
            modifier = modifier,
            color = color,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = {
                if (it.hasVisualOverflow) {
                    coroutineScope.launch {
                        updateFontSize(fontMultiplier * 0.98f)
                    }
                }
            },
            style = style.copy(
                fontSize = fontSize * fontMultiplier,
                lineHeight = lineHeight * fontMultiplier,
            )
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
