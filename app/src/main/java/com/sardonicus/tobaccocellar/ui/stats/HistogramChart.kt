package com.sardonicus.tobaccocellar.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun HistogramChart(
    data: RatingsDistribution,
    modifier: Modifier = Modifier,
    showValues: Boolean = false,
) {
    val maxCount = (data.distribution.values.maxOrNull() ?: 0).coerceAtLeast(1)

    val density = LocalDensity.current
    var currentWidth by remember { mutableStateOf(0.dp) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                currentWidth = with(density) { it.size.width.toDp() }
            }
    ) {
        val width: Dp = (currentWidth) / 11
        val ratingSteps = List(11) { it / 2.0 }

        Box(
            modifier = Modifier
        ) {
            // chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.33f)
                    .padding(bottom = 16.dp)
            ) {
                ratingSteps.forEach {
                    val count = data.distribution.getOrDefault(it, 0)
                    val barHeight = count.toFloat() / maxCount

                    Column(
                        modifier = Modifier
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(width)
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (showValues && count == 0) {
                                Text(
                                    text = "(0)",
                                    color = LocalContentColor.current,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    autoSize = TextAutoSize.StepBased(
                                        minFontSize = 9.sp,
                                        maxFontSize = 12.sp,
                                        stepSize = .02.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(start = 4.dp, end = 4.dp, bottom = 1.dp)
                                )
                            }
                            // actual bar
                            val borderColor = colorScheme.background
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 2.dp)
                                    .fillMaxHeight(barHeight)
                                    .background(LocalCustomColors.current.pieOne)
                                    .drawBehind {
                                        val strokeWidth = Dp.Hairline.toPx()
                                        val xOffset = size.width - strokeWidth / 2

                                        drawLine(
                                            color = borderColor,
                                            start = Offset((strokeWidth / 2), 0f),
                                            end = Offset((strokeWidth / 2), size.height),
                                            strokeWidth = strokeWidth
                                        )

                                        drawLine(
                                            color = borderColor,
                                            start = Offset(xOffset, 0f),
                                            end = Offset(xOffset, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                contentAlignment = Alignment.TopCenter
                            ) {
                                val offset = with(density) { 6.sp.toDp() }
                                if (showValues && count > 0) {
                                    Text(
                                        text = "$count",
                                        color = LocalContentColor.current,
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .offset(y = -offset)
                                            .background(colorScheme.background.copy(alpha = 0.75f))
                                            .padding(start = 4.dp, end = 4.dp, bottom = 0.5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // null count
            if (data.unratedCount > 0) {
                Text(
                    text = "(Unrated: ${data.unratedCount})",
                    color = LocalContentColor.current,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(colorScheme.background.copy(alpha = 0.75f))
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                )
            }
            // count label
            Text(
                text = "Frequency",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .rotate(-90f)
                    .align(Alignment.CenterStart)
                    .offset(24.dp, -(width + 32.dp))
            )
        }
        // x-axis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
                .height(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = colorScheme.outline,
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    repeat(11) {
                        Box(
                            modifier = Modifier
                                .width(width),
                            contentAlignment = Alignment.TopStart
                        ) {
                            val fraction = if (it % 2 == 0) 1f else 0.5f
                            val thickness = if (it % 2 == 0) 2.dp else 0.75.dp
                            VerticalDivider(Modifier.fillMaxHeight(fraction), thickness, colorScheme.outline)
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = -(width / 2)),
            verticalAlignment = Alignment.Top
        ) {
            ratingSteps.forEachIndexed { index, it ->
                if (index % 2 == 0) {
                    Text(
                        text = formatDecimal(it, 0),
                        modifier = Modifier
                            .width(width),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    val offset = with(density) { 9.sp.toDp() }
                    Text(
                        text = formatDecimal(it, 1),
                        modifier = Modifier
                            .width(width)
                            .offset(y = -offset),
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rating (ranges)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}