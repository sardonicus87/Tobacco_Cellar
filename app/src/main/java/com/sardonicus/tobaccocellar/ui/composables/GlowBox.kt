package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fade effect boxes. Add scroll modifier to the content itself. Use spacers in the content
 * to inset/pad from the edges (glow extends inward from the edge) if you want a fade effect on
 * scroll.*/
@Composable
fun GlowBox(
    color: GlowColor,
    size: GlowSize,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        Column (
            modifier = Modifier,
            content = content
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawGlow(color, size)
                }
        )
    }
}

private fun DrawScope.drawGlow(glowColor: GlowColor, glowSize: GlowSize) {
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