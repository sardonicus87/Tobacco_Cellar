package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
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

/** Fade effect boxes. If you want a set height and scrolling effect, add scroll modifier to
 * content and set the height restrictions on the GlowBox itself. Use spacers in the content to
 * inset/pad from the edges (glow extends inward from the edge) if you want a fade effect on scroll.*/
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
        content.invoke()
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

