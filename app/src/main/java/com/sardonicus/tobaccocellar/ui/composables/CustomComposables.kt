package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.sardonicus.tobaccocellar.ui.items.CustomDropdownMenuItem
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch

/** Fade effect boxes. If you want a set height and scrolling effect, add scroll modifier to
 * the content itself. Use spacers in the content to inset/pad from the edges (glow extends
 *  inward from the edge) if you want a fade effect on scroll.*/
@Composable
fun GlowBox(
    color: GlowColor,
    size: GlowSize,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        Column {
            content.invoke()
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawGlow(color, size)
                }
        )
    }
}

fun DrawScope.drawGlow(glowColor: GlowColor, glowSize: GlowSize) {
    val topSize: Dp
    val bottomSize: Dp
    val startSize: Dp
    val endSize: Dp

    val topColor: Color
    val bottomColor: Color
    val startColor: Color
    val endColor: Color

    when (glowSize) {
        is GlowSize.All -> {
            topSize = glowSize.size
            bottomSize = glowSize.size
            startSize = glowSize.size
            endSize = glowSize.size
        }
        is GlowSize.HorizontalVertical -> {
            topSize = glowSize.vertical
            bottomSize = glowSize.vertical
            startSize = glowSize.horizontal
            endSize = glowSize.horizontal
        }
        is GlowSize.Edges -> {
            topSize = glowSize.top
            bottomSize = glowSize.bottom
            startSize = glowSize.start
            endSize = glowSize.end
        }
    }

    when (glowColor) {
        is GlowColor.All -> {
            topColor = glowColor.color
            bottomColor = glowColor.color
            startColor = glowColor.color
            endColor = glowColor.color
        }
        is GlowColor.HorizontalVertical -> {
            topColor = glowColor.vertical
            bottomColor = glowColor.vertical
            startColor = glowColor.horizontal
            endColor = glowColor.horizontal
        }
        is GlowColor.Edges -> {
            topColor = glowColor.top
            bottomColor = glowColor.bottom
            startColor = glowColor.start
            endColor = glowColor.end
        }
    }

    val topFade = topSize > 0.dp
    val bottomFade = bottomSize > 0.dp
    val startFade = startSize > 0.dp
    val endFade = endSize > 0.dp

    val fadeOffsetX = size.width - endSize.toPx()
    val fadeOffsetY = size.height - bottomSize.toPx()

    if (topFade) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    topColor,
                    Color.Transparent,
                ),
                startY = 0f,
                endY = topSize.toPx(),
            ),
            topLeft = Offset(0f, 0f),
            size = Size(size.width, topSize.toPx())
        )
    }
    if (bottomFade) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    bottomColor,
                ),
                startY = fadeOffsetY,
                endY = size.height,
            ),
            topLeft = Offset(0f, fadeOffsetY),
            size = Size(size.width, bottomSize.toPx())
        )
    }
    if (startFade) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    startColor,
                    Color.Transparent,
                ),
                startX = 0f,
                endX = startSize.toPx(),
            ),
            topLeft = Offset(0f, 0f),
            size = Size(startSize.toPx(), size.height)
        )
    }
    if (endFade) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    endColor,
                ),
                startX = fadeOffsetX,
                endX = size.width,
            ),
            topLeft = Offset(fadeOffsetX, 0f),
            size = Size(endSize.toPx(), size.height)
        )
    }
}

sealed class GlowColor {
    data class All(
        val color: Color = Color.Transparent
    ) : GlowColor()

    data class HorizontalVertical(
        val horizontal: Color = Color.Transparent,
        val vertical: Color = Color.Transparent,
    ) : GlowColor()

    data class Edges(
        val top: Color = Color.Transparent,
        val bottom: Color = Color.Transparent,
        val start: Color = Color.Transparent,
        val end: Color = Color.Transparent,
    ) : GlowColor()

    companion object {
        operator fun invoke(
            color: Color = Color.Transparent
        ): All = All(color)

        operator fun invoke(
            horizontal: Color = Color.Transparent,
            vertical: Color = Color.Transparent,
        ): HorizontalVertical = HorizontalVertical(horizontal, vertical)

        operator fun invoke(
            top: Color = Color.Transparent,
            bottom: Color = Color.Transparent,
            start: Color = Color.Transparent,
            end: Color = Color.Transparent
        ): Edges = Edges(top, bottom, start, end)
    }
}

sealed class GlowSize {
    data class All(
        val size: Dp = 0.dp
    ) : GlowSize()

    data class HorizontalVertical(
        val horizontal: Dp = 0.dp,
        val vertical: Dp = 0.dp
    ) : GlowSize()

    data class Edges(
        val top: Dp = 0.dp,
        val bottom: Dp = 0.dp,
        val start: Dp = 0.dp,
        val end: Dp = 0.dp,
    ) : GlowSize()

    companion object {
        operator fun invoke(
            size: Dp = 0.dp
        ): All = All(size)

        operator fun invoke(
            horizontal: Dp = 0.dp,
            vertical: Dp = 0.dp
        ): HorizontalVertical = HorizontalVertical(horizontal, vertical)

        operator fun invoke(
            top: Dp = 0.dp,
            bottom: Dp = 0.dp,
            start: Dp = 0.dp,
            end: Dp = 0.dp
        ): Edges = Edges(top, bottom, start, end)
    }
}


/** Full screen loading indicator */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Transparent,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor)
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


/** TextField with AutoComplete suggestions */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteText(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: ((String) -> Unit)?,
    onOptionSelected: (String, String) -> Unit,
    suggestions: List<String> = emptyList(),
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int. MAX_VALUE,
    minLines: Int = 1,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current,
    componentField: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    var suggestions = suggestions
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(value)) }
    val focusRequester = remember { FocusRequester() }
    val focusState = remember { mutableStateOf(false) }

    LaunchedEffect(suggestions) {
        //    expanded = value.isNotEmpty() && suggestions.isNotEmpty() && focusState.value
        if (suggestions.isEmpty()) {
            expanded = false
        } else {
            expanded = value.isNotEmpty() && suggestions.isNotEmpty() && focusState.value
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && focusState.value && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
            .padding(0.dp)
    ) {
        TextField(
            value = textFieldValueState.copy(text = value),
            onValueChange = {
                textFieldValueState = it
                onValueChange?.invoke(it.text)
            },
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(0.dp)
                .onFocusChanged { focusState.value = it.isFocused }
                .focusRequester(focusRequester)
                .menuAnchor(MenuAnchorType.Companion.PrimaryEditable, true),
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
        DropdownMenu(
            expanded = expanded && focusState.value && suggestions.isNotEmpty(),
            onDismissRequest = { focusState.value },
            modifier = Modifier
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                .heightIn(max = 82.dp),
            properties = PopupProperties(focusable = false),
            offset = DpOffset(32.dp, (-12).dp),
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            suggestions.take(3).forEach { label ->
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
                        onOptionSelected(label, currentText)

                        val updatedText = if (currentText.contains(", ")) {
                            currentText.substringBeforeLast(", ", "") + ", " + label + ", "
                        } else {
                            if (componentField) {
                                label + ", "
                            } else {
                                label
                            }
                        }

                        textFieldValueState = TextFieldValue(
                            text = updatedText,
                            selection = TextRange(updatedText.length)
                        )
                        suggestions = emptyList()
                        expanded = false
                    },
                    enabled = true,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp)
                        .offset(0.dp, 0.dp)
                        .fillMaxWidth(),
                    colors = MenuDefaults.itemColors(),
                )
            }
        }
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


/** pager indicator **/
data class IndicatorSizes(val current: Dp, val other: Dp)

@Composable
fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    indicatorSize: IndicatorSizes = IndicatorSizes(current = 8.dp, other = 7.dp),
) {
    val animationScope = rememberCoroutineScope()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) {
            val color = if (pagerState.currentPage == it) {
                MaterialTheme.colorScheme.primary } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
            }
            val size = if (pagerState.currentPage == it) indicatorSize.current else indicatorSize.other

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(size)
                    .clickable {
                        animationScope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    }
            )
        }
    }
}


@Composable
fun CustomCheckBox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    checkedIcon: Int,
    uncheckedIcon: Int,
    modifier: Modifier = Modifier,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
    enabled: Boolean = true,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange?.invoke(it) },
        modifier = modifier
            .size(34.dp),
        colors = colors,
        enabled = enabled
    ) {
        Icon(
            imageVector = if (checked) {
                ImageVector.Companion.vectorResource(id = checkedIcon)
            } else ImageVector.Companion.vectorResource(id = uncheckedIcon),
            contentDescription = null,
            modifier = Modifier.Companion
        )
    }
}

