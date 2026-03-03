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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
    showEmpty: Boolean = false,
    starColor: Color = LocalCustomColors.current.starRating,
    emptyColor: Color = LocalCustomColors.current.starRating,
    emptyAlpha: Float = 0.38f,
) {
    if (starSize != Dp.Unspecified) {
        RatingRowImpl(
            rating = { rating },
            starSize = { starSize },
            maxColor = { starColor },
            minColor = { starColor },
            emptyColor = { emptyColor },
            showEmpty = { showEmpty },
            showDivider = { false },
            dividerColor = { Color.Transparent },
            maxAlpha = { 1f },
            minAlpha = { 1f },
            emptyAlpha = { emptyAlpha },
            range = { Pair(null, null) },
            showRating = { true },
            showRange = { false },
            modifier = modifier
        )
    } else if (rating != null) {
        BoxWithConstraints(modifier = modifier) {
            val dynamicSize = if (maxHeight > 0.dp && maxWidth > 0.dp) {
                if (showEmpty) {
                    min(maxHeight, maxWidth / 5)
                } else {
                    min(maxHeight, maxWidth * rating.toFloat())
                }
            } else {
                0.dp
            }
            if (dynamicSize > 0.dp) {
                RatingRowImpl(
                    rating = { rating },
                    starSize = { dynamicSize },
                    maxColor = { starColor },
                    minColor = { starColor },
                    emptyColor = { emptyColor },
                    showEmpty = { showEmpty },
                    showDivider = { false },
                    dividerColor = { Color.Transparent },
                    maxAlpha = { 1f },
                    minAlpha = { 1f },
                    emptyAlpha = { emptyAlpha },
                    range = { Pair(null, null) },
                    showRating = { true },
                    showRange = { false },
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
    emptyAlpha: Float = 0.38f,
) {
    if (starSize != Dp.Unspecified) {
        RatingRowImpl(
            rating = { 5.0 },
            starSize = { starSize },
            maxColor = { maxColor },
            minColor = { minColor },
            emptyColor = { emptyColor },
            showEmpty = { true },
            showDivider = { showDivider },
            dividerColor = { dividerColor },
            maxAlpha = { maxAlpha },
            minAlpha = { minAlpha },
            emptyAlpha = { emptyAlpha },
            range = { range },
            showRating = { false },
            showRange = { true },
            modifier = modifier
        )
    } else {
        BoxWithConstraints(modifier = modifier) {
            val dynamicSize by remember(maxHeight, maxWidth) {
                derivedStateOf {
                    if (maxHeight > 0.dp && maxWidth > 0.dp) {
                        min(maxHeight, maxWidth / 5)
                    } else {
                        0.dp
                    }
                }
            }

            if (dynamicSize > 0.dp) {
                RatingRowImpl(
                    rating = { 5.0 },
                    starSize = { dynamicSize },
                    maxColor = { maxColor },
                    minColor = { minColor },
                    emptyColor = { emptyColor },
                    showEmpty = { true },
                    showDivider = { showDivider },
                    dividerColor = { dividerColor },
                    maxAlpha = { maxAlpha },
                    minAlpha = { minAlpha },
                    emptyAlpha = { emptyAlpha },
                    range = { range },
                    showRating = { false },
                    showRange = { true },
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun RatingRowImpl(
    rating: () -> Double?,
    starSize: () -> Dp,
    showEmpty: () -> Boolean,
    showDivider: () -> Boolean,
    dividerColor: () -> Color,
    maxColor: () -> Color,
    minColor: () -> Color,
    emptyColor: () -> Color,
    maxAlpha: () -> Float,
    minAlpha: () -> Float,
    emptyAlpha: () -> Float,
    range: () -> Pair<Double?, Double?>,
    showRating: () -> Boolean,
    showRange: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun star(color: Color, alignment: Alignment = Alignment.CenterStart, alpha: Float = 1f) {
        Image(
            painter = painterResource(id = R.drawable.star_filled),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier
                .height(starSize()),
            alignment = alignment,
            contentScale = ContentScale.FillHeight,
            alpha = alpha,
        )
    }

    @Composable
    fun fractionalStar(
        fractional: Double,
        startColor: Color = maxColor(),
        endColor: Color = emptyColor(),
        startAlpha: Float = 1f,
        endAlpha: Float = 1f
    ) {
        val startWidth = starSize() * fractional.toFloat()
        val endWidth = starSize() - startWidth
        Box(
            modifier = Modifier
                .height(starSize())
                .width(startWidth)
                .clip(RectangleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            star(startColor, alpha = startAlpha)
        }
        if (showEmpty()) {
            Box(
                modifier = Modifier
                    .height(starSize())
                    .width(endWidth)
                    .clip(RectangleShape),
                contentAlignment = Alignment.CenterStart
            ) {
                star(endColor, Alignment.CenterEnd, endAlpha)
            }
        }
    }

    Row(
        modifier = modifier
            .height(starSize()),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showRange()) {
            val nullRange = range().first == null && range().second == null

            if (nullRange) {
                repeat(5) {
                    star(emptyColor(), alpha = emptyAlpha())
                }
            } else {
                val min = range().first ?: 0.0
                val max = range().second ?: range().first ?: 5.0
                val minWhole = (0.0 + min).toInt()
                val fractionalMin = min - minWhole
                val entireMin = ceil(minWhole + fractionalMin).toInt()
                val maxWhole = (max - entireMin).toInt()
                val fractionalMax = if (max != min) (max - (maxWhole + entireMin)) else 0.0
                val emptyEnd = (5.0 - max).toInt()

                val fractionalMinAlphaRemap = if (range().second == null || range().second == range().first) emptyAlpha else maxAlpha
                val fractionalMinColorRemap = if (range().second == null || range().second == range().first) emptyColor else maxColor

                Box {
                    Row {
                        repeat(minWhole) {
                            star(minColor(), alpha = minAlpha())
                        }
                        if (fractionalMin > 0.0) {
                            fractionalStar(
                                fractionalMin,
                                minColor(),
                                fractionalMinColorRemap(),
                                minAlpha(),
                                fractionalMinAlphaRemap()
                            )
                        }
                        repeat(maxWhole) {
                            star(maxColor(), alpha = maxAlpha())
                        }
                        if (fractionalMax > 0.0) {
                            fractionalStar(
                                fractionalMax,
                                maxColor(),
                                emptyColor(),
                                maxAlpha(),
                                emptyAlpha()
                            )
                        }
                        repeat(emptyEnd) {
                            star(emptyColor(), alpha = emptyAlpha())
                        }
                    }
                    if (showDivider()) {
                        Box(
                            modifier = Modifier
                                .matchParentSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            range().toList().forEach {
                                if (it != null && (it != 0.0 && it != 5.0)) {
                                    val padding: Dp = starSize() * it.toFloat()

                                    Row {
                                        Spacer(modifier = Modifier.width(padding))
                                        VerticalDivider(thickness = 1.5.dp, color = dividerColor())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showRating()) {
            if (rating() != null) {
                val whole = rating()!!.toInt()
                val remainder = rating()!! - whole
                val empty = (5.0 - rating()!!).toInt()

                repeat(whole) {
                    star(maxColor())
                }
                if (remainder > 0.0) {
                    fractionalStar(
                        remainder,
                        maxColor(),
                        emptyColor(),
                        1f,
                        emptyAlpha()
                    )
                }
                if (showEmpty() && empty > 0) {
                    repeat(empty) {
                        star(emptyColor(), alpha = emptyAlpha())
                    }
                }
            } else {
                if (showEmpty()) {
                    repeat(5) {
                        star(emptyColor(), alpha = emptyAlpha())
                    }
                }
            }
        }
    }
}