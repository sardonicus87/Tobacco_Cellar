package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlin.math.ceil

@Composable
fun RatingRow(
    rating: Double?,
    modifier: Modifier = Modifier,
    starSize: Dp = Dp.Unspecified,
    starColor: Color = LocalCustomColors.current.starRating,
    emptyColor: Color = LocalCustomColors.current.starRating,
    showEmpty: Boolean = false,
    emptyAlpha: Float = 0.38f
) {
    if (starSize != Dp.Unspecified) {
        RatingRowImpl(
            rating = rating,
            starSize = starSize,
            maxColor = starColor,
            minColor = starColor,
            emptyColor = emptyColor,
            showEmpty = showEmpty,
            emptyAlpha = emptyAlpha,
            showRating = true,
            modifier = modifier
        )
    } else if (rating != null) {
        BoxWithConstraints(modifier = modifier) {
            val dynamicSize = remember(maxWidth, maxHeight, rating, showEmpty) {
                if (maxHeight > 0.dp && maxWidth > 0.dp) {
                    if (showEmpty) { min(maxHeight, maxWidth / 5) }
                    else { min(maxHeight, maxWidth * rating.toFloat()) }
                } else { 0.dp }
            }
            if (dynamicSize > 0.dp) {
                RatingRowImpl(
                    rating = rating,
                    starSize = dynamicSize,
                    maxColor = starColor,
                    minColor = starColor,
                    emptyColor = emptyColor,
                    showEmpty = showEmpty,
                    emptyAlpha = emptyAlpha,
                    showRating = true,
                    modifier = modifier
                )
            }
        }
    }
}

/** Ranged rating row that shows all 5 stars and a range from a low to high value with optional dividers. */
@Composable
fun RatingRow(
    range: Pair<Double?, Double?>,
    modifier: Modifier = Modifier,
    starSize: Dp = Dp.Unspecified,
    showDivider: Boolean = false,
    dividerColor: Color = MaterialTheme.colorScheme.onBackground,
    minColor: Color = LocalCustomColors.current.starRating,
    maxColor: Color = LocalCustomColors.current.starRating,
    emptyColor: Color = LocalContentColor.current,
    minAlpha: Float = 1f,
    maxAlpha: Float = 1f,
    emptyAlpha: Float = 0.38f
) {
    if (starSize != Dp.Unspecified) {
        RatingRowImpl(
            starSize = starSize,
            maxColor = maxColor,
            minColor = minColor,
            emptyColor = emptyColor,
            showEmpty = true,
            showDivider = showDivider,
            dividerColor = dividerColor,
            maxAlpha = maxAlpha,
            minAlpha = minAlpha,
            emptyAlpha = emptyAlpha,
            range = range,
            showRange = true,
            modifier = modifier
        )
    } else {
        BoxWithConstraints(modifier = modifier) {
            val dynamicSize = remember(maxHeight, maxWidth) {
                if (maxHeight > 0.dp && maxWidth > 0.dp) {
                    min(maxHeight, maxWidth / 5)
                } else { 0.dp }
            }

            if (dynamicSize > 0.dp) {
                RatingRowImpl(
                    starSize = dynamicSize,
                    maxColor = maxColor,
                    minColor = minColor,
                    emptyColor = emptyColor,
                    showEmpty = true,
                    showDivider = showDivider,
                    dividerColor = dividerColor,
                    maxAlpha = maxAlpha,
                    minAlpha = minAlpha,
                    emptyAlpha = emptyAlpha,
                    range = range,
                    showRange = true,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun RatingRowImpl(
    starSize: Dp,
    maxColor: Color,
    minColor: Color,
    emptyColor: Color,
    emptyAlpha: Float,
    modifier: Modifier = Modifier,
    showRating: Boolean = false,
    showRange: Boolean = false,
    rating: Double? = 5.0,
    range: Pair<Double?, Double?> = Pair(null, null),
    showEmpty: Boolean = false,
    showDivider: Boolean = false,
    dividerColor: Color = Color.Transparent,
    maxAlpha: Float = 1f,
    minAlpha: Float = 1f
) {
    @Composable
    fun Star(color: Color, alignment: Alignment = Alignment.CenterStart, alpha: Float = 1f) {
        Image(
            painter = painterResource(id = R.drawable.star_filled),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.height(starSize),
            alignment = alignment,
            contentScale = ContentScale.FillHeight,
            alpha = alpha
        )
    }

    @Composable
    fun FractionalStar(
        fractional: Double,
        startColor: Color = maxColor,
        endColor: Color = emptyColor,
        startAlpha: Float = 1f,
        endAlpha: Float = 1f
    ) {
        Box(
            modifier = Modifier
                .height(starSize)
                .width(starSize * fractional.toFloat())
                .clip(RectangleShape),
            contentAlignment = Alignment.CenterStart
        ) { Star(startColor, alpha = startAlpha) }

        if (showEmpty) {
            Box(
                modifier = Modifier
                    .height(starSize)
                    .width(starSize * (1f - fractional.toFloat()))
                    .clip(RectangleShape),
                contentAlignment = Alignment.CenterStart
            ) { Star(endColor, Alignment.CenterEnd, endAlpha) }
        }
    }

    Row(
        modifier = modifier.height(starSize),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showRange) {
            val nullRange = range.first == null && range.second == null

            if (nullRange) {
                repeat(5) { Star(emptyColor, alpha = emptyAlpha) }
            } else {
                val min = range.first ?: 0.0
                val max = range.second ?: range.first ?: 5.0
                val minWhole = min.toInt()
                val fractionalMin = (min - minWhole).coerceIn(0.0, 1.0)
                val entireMin = ceil(minWhole + fractionalMin).toInt().coerceAtLeast(0)
                val maxWhole = (max - entireMin).toInt()
                val fractionalMax = if (max != min) (max - (maxWhole + entireMin)).coerceIn(0.0, 1.0) else 0.0
                val emptyEnd = (5.0 - max).toInt().coerceAtLeast(0)

                val fMinColor = if (range.second == null || range.second == range.first) emptyColor else maxColor
                val fMinAlpha = if (range.second == null || range.second == range.first) emptyAlpha else maxAlpha

                Box {
                    Row {
                        repeat(minWhole) { Star(minColor, alpha = minAlpha) }

                        if (fractionalMin > 0.0) {
                            FractionalStar(fractionalMin, minColor, fMinColor, minAlpha, fMinAlpha)
                        }

                        repeat(maxWhole) { Star(maxColor, alpha = maxAlpha) }

                        if (fractionalMax > 0.0) {
                            FractionalStar(fractionalMax, maxColor, emptyColor, maxAlpha, emptyAlpha)
                        }

                        repeat(emptyEnd) { Star(emptyColor, alpha = emptyAlpha) }
                    }
                    if (showDivider) {
                        Box(
                            modifier = Modifier.matchParentSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            range.toList().forEach {
                                if (it != null && (it > 0.0 && it < 5.0)) {
                                    Row {
                                        Spacer(modifier = Modifier.width(starSize * it.toFloat()))
                                        VerticalDivider(thickness = 1.5.dp, color = dividerColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showRating) {
            if (rating != null) {
                val whole = rating.toInt()
                val remainder = rating - whole
                val empty = (5.0 - rating).toInt()

                repeat(whole) { Star(maxColor) }

                if (remainder > 0.0) { FractionalStar(remainder, maxColor, emptyColor, 1f, emptyAlpha) }
                if (showEmpty && empty > 0) { repeat(empty) { Star(emptyColor, alpha = emptyAlpha) } }

            } else {
                if (showEmpty) { repeat(5) { Star(emptyColor, alpha = emptyAlpha) } }
            }
        }
    }
}