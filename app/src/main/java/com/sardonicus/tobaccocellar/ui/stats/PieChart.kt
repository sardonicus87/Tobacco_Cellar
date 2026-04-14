package com.sardonicus.tobaccocellar.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showPercentages: Boolean = true,
    showValues: Boolean = false,
    colors: List<Color> = listOf(
        LocalCustomColors.current.pieOne,
        LocalCustomColors.current.pieTwo,
        LocalCustomColors.current.pieThree,
        LocalCustomColors.current.pieFour,
        LocalCustomColors.current.pieFive,
        LocalCustomColors.current.pieSix,
        LocalCustomColors.current.pieSeven,
        LocalCustomColors.current.pieEight,
        LocalCustomColors.current.pieNine,
        LocalCustomColors.current.pieTen),
    onSliceLabelPosition: Float = .5f,
    outsideSliceLabelPosition: Float = 0.5f,
    outsideLabelThreshold: Float = 20f,
    rotationOffset: Float = 270f,
    textColor: Color = Color.Black,
    labelBackground: Color = Color.Unspecified,
    sortData: Boolean = false,
) {
    val total = data.values.sum()
    val sortedData = if (sortData) { data.toList().sortedByDescending { it.second }.toMap() } else { data }
    val textMeasurer = rememberTextMeasurer()

    val isLightTheme = LocalCustomColors.current.isLightTheme
    val background = colorScheme.background

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val insideLabel = min(centerX, centerY) * onSliceLabelPosition
        val outsideLabel = (min(centerX, centerY) * (1f)) + ((20.dp.toPx() * outsideSliceLabelPosition))

        drawSlices(
            sortedData, colors, total, rotationOffset
        )

        drawLabels(
            sortedData, total, rotationOffset, showLabels, showPercentages, showValues, textMeasurer, textColor, colors, labelBackground,
            centerX, centerY, insideLabel, outsideLabel, outsideLabelThreshold, isLightTheme, background
        )
    }
}

private fun DrawScope.drawSlices(
    data: Map<String, Int>,
    colors: List<Color>,
    total: Int,
    startAngle: Float,
) {
    var currentStartAngle = startAngle

    data.forEach { (label, value) ->
        val sweepAngle = (value.toFloat() / total) * 360f

        drawArc(
            color = colors[data.keys.indexOf(label) % colors.size],
            startAngle = currentStartAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            style = Fill,
        )
        currentStartAngle += sweepAngle
    }
}

private fun DrawScope.drawLabels(
    data: Map<String, Int>,
    total: Int,
    startAngle: Float,
    showLabels: Boolean,
    showPercentages: Boolean,
    showValues: Boolean,
    textMeasurer: TextMeasurer,
    textColor: Color,
    colors: List<Color>,
    backgroundColor: Color,
    centerX: Float,
    centerY: Float,
    insideRadius: Float,
    outsideRadius: Float,
    outsideLabelThreshold: Float,
    isLightTheme: Boolean,
    pageBackground: Color,
) {
    var currentStartAngle = startAngle
    var outsideLabelCount = 0
    val sliceCount = data.size
    val totalOutsideLabels = data.values.count { (it.toFloat() / total) * 360f < outsideLabelThreshold }
    val totalThinPercent = data.values.count {
        val normalizedMidpointAngle = (currentStartAngle + (it.toFloat() / total) * 360f) % 360f
        (it.toFloat() / total) * 360f < 10f && normalizedMidpointAngle > 245f
    }
    var thinCount = 0

    var firstOutsideLabelAngle = -1f
    var tempAngle = startAngle
    for ((_, value) in data) {
        val sweep = (value.toFloat() / total) * 360f
        if (sweep < outsideLabelThreshold) {
            firstOutsideLabelAngle = (tempAngle + sweep / 2f) % 360f
            break
        }
        tempAngle += sweep
    }

    val moveToBottom = firstOutsideLabelAngle in 180f..235f

    val thinPercentMeasures = mutableMapOf<String, Float>()
    var tempMeasureAngle = startAngle
    // measure percentage pre-processing loop
    data.forEach { (label, value) ->
        val sweep = (value.toFloat() / total) * 360f
        val midpoint = tempMeasureAngle + sweep / 2f
        val normalizedMidpoint = (midpoint + 360f) % 360f

        if (sweep < 10f && normalizedMidpoint > 245f) {
            val valuePad = if (showValues) "($value) " else ""
            val percent = (value.toDouble() / total) * 100
            val percentCal = " ${formatDecimal(percent)}% " + valuePad
            val measuredWidth = textMeasurer.measure(
                text = AnnotatedString(percentCal),
                style = TextStyle(fontSize = 11.sp)
            ).size.width.toFloat()
            thinPercentMeasures[label] = measuredWidth
        }
        tempMeasureAngle += sweep
    }
    val spacing = 3.dp.toPx()
    val totalThinWidth = thinPercentMeasures.values.sum() + (spacing * (thinPercentMeasures.size - 1).coerceAtLeast(0))
    val orderedThinLabels = data.keys.filter { it in thinPercentMeasures.keys }

    data.forEach { (label, value) ->
        val sweepAngle = (value.toFloat() / total) * 360f
        val midpointAngle = currentStartAngle + (sweepAngle / 2)
        val normalizedMidpointAngle = (midpointAngle) % 360f
        val isOther = label == "(Other)"
        if (sweepAngle < outsideLabelThreshold && !isOther) { outsideLabelCount++ }
        if (sweepAngle < 10f && normalizedMidpointAngle > 245f) { thinCount++ }

        val radius = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            outsideRadius
        } else {
            if (sliceCount > 5 && isOther) {
                insideRadius * .65f
            } else {
                insideRadius
            }
        }

        /** label coloring */
        val targetColor = listOf(colors[3], colors[4], colors[5], colors[6])

        val labelColor = if (showLabels) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    colors[data.keys.indexOf(label) % colors.size]
                } else {
                    textColor
                }
            } else {
                textColor
            }
        } else {
            Color.Transparent
        }
        val percentColor = if (showLabels && showPercentages) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    colors[data.keys.indexOf(label) % colors.size]
                } else {
                    textColor
                }
            } else {
                textColor
            }
        } else {
            Color.Transparent
        }
        val labelBg = if (showLabels) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    if (isLightTheme && labelColor in targetColor) {
                        Color.DarkGray.copy(alpha = 0.65f)
                    } else {
                        pageBackground.copy(alpha = 0.75f)
                    }
                } else {
                    backgroundColor
                }
            } else {
                backgroundColor
            }
        } else {
            Color.Transparent
        }
        val percentBg = if (showLabels && showPercentages) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    if (isLightTheme && percentColor in targetColor) {
                        Color.DarkGray.copy(alpha = 0.65f)
                    } else {
                        pageBackground.copy(alpha = 0.75f)
                    }
                }  else {
                    backgroundColor
                }
            } else {
                backgroundColor
            }
        } else {
            Color.Transparent
        }


        /** label text formatting */
        val labelPad = " $label "
        val valuePad = if (showValues) "($value) " else ""
        val percent = (value.toDouble() / total) * 100
        val percentageCal = " ${formatDecimal(percent)}% " + valuePad

        val textLabel = textMeasurer.measure(
            text = AnnotatedString(labelPad),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                background = labelBg,
                textAlign = TextAlign.Start,
            ),
            constraints = Constraints(maxWidth = 250),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        val percentageLabel = textMeasurer.measure(
            text = AnnotatedString(percentageCal),
            style = TextStyle(
                fontSize = 11.sp,
                background = percentBg,
                textAlign = TextAlign.Center,
            )
        )


        /** label positioning */
        val labelWidth = textLabel.size.width.toFloat()
        val labelHeight = textLabel.size.height.toFloat()
        val percentageWidth = percentageLabel.size.width.toFloat()
        val percentageHeight = percentageLabel.size.height.toFloat()
        val combinedHeight = if (showLabels && showPercentages) {
            labelHeight + percentageHeight // * .2f)
        } else {
            if (showLabels) {
                labelHeight
            } else {
                percentageHeight
            }
        }

        val labelX = centerX + radius * cos(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val labelY = centerY + radius * sin(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val percentageX = centerX + radius * cos(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val percentageY = centerY + radius * sin(Math.toRadians(midpointAngle.toDouble())).toFloat()

        val listSpacing = 3.dp.toPx()

        val labelList = (labelHeight + listSpacing) * (outsideLabelCount - 1)
        val listHeight = (labelHeight + listSpacing) * totalOutsideLabels
        val listX = (centerX - outsideRadius) - 125

        val listY = (centerY - outsideRadius) - (percentageHeight * 1.5f)


        /** additional adjustment based on quadrant */
        val xOffsetFactor = when (normalizedMidpointAngle) {
            in 0f..90f -> (normalizedMidpointAngle - 0f) / (180f) // 0 to 0.5
            in 90f..180f -> (normalizedMidpointAngle - 0f) / (180f) // 0.5 to 1
            in 180f..270f -> (360f - normalizedMidpointAngle) / (180f)  // 1 to 0.5
            in 270f..360f -> (360f - normalizedMidpointAngle) / (180f) // 0.5 to 0
            else -> 0f
        }
        val yOffsetFactor = when (normalizedMidpointAngle) {
            in 0f..90f -> (90f - normalizedMidpointAngle) / (180f) // 0.5 to 0
            in 90f..180f -> (normalizedMidpointAngle - 90f) / (180f) // 0 to 0.5
            in 180f..270f -> (normalizedMidpointAngle - 90f) / (180f)  // 0.5 to 1
            in 270f..360f -> (450f - normalizedMidpointAngle) / (180f) // 1 to 0.5
            else -> 0f
        }


        /** final adjustment */
        val adjustedLabelX = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            // list placement
            listX
        } else {
            // normal on slice labels
            labelX - (labelWidth * xOffsetFactor)
        }
        val adjustedLabelY = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            // list placement
            val listBase = if (moveToBottom) { size.height - listHeight } else { 0f }
            listBase + labelList
        } else {
            // normal on slice labels
            labelY - (combinedHeight * yOffsetFactor)
        }

        val adjustedPercentageX = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            // very thin slices extra adjustment
            if (sweepAngle < 10f && normalizedMidpointAngle > 245f && totalThinPercent > 0) {
                // list
                val start = centerY - (totalThinWidth / 2)
                val index = orderedThinLabels.indexOf(label)
                val precedingWidth = orderedThinLabels.take(index).sumOf {
                    thinPercentMeasures[it]?.toDouble() ?: 0.0
                }.toFloat()
                val precedingSpace = listSpacing * index

                start + precedingWidth + precedingSpace
//                if (totalThinPercent > 2) {
//                    val right = (totalThinPercent - thinCount)
//                    (percentageX - (percentageWidth * xOffsetFactor)) - ((right * (percentageHeight * 1.15f)) * cos(Math.toRadians(midpointAngle.toDouble())).toFloat())
//                } else {
//                    // alternating
//                    val additionalOffset = if (outsideLabelCount % 2 == 0) {
//                        // evens up
//                        (-1 * ((percentageHeight * 0.25f) + (alternatingOffsetMax * alternatingOffsetFactor)))
//                    } else {
//                        // odds down
//                        (percentageHeight * 0.75f) + ((alternatingOffsetMax * 3) * alternatingOffsetFactor)
//                    }
//                    (percentageX - (percentageWidth * xOffsetFactor)) - (additionalOffset * cos(Math.toRadians(midpointAngle.toDouble())).toFloat())
//                }
            } else {
                // normal outside slice placement
                percentageX - (percentageWidth * xOffsetFactor)
            }
        } else {
            // normal inside slice placement
            adjustedLabelX + (labelWidth - percentageWidth) / 2
        }
        val adjustedPercentageY = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            if (sweepAngle < 10f && normalizedMidpointAngle > 245f && totalThinPercent > 1) {
                // list placement
                listY

//                // very thin slices at the top of the chart
//                if (totalThinPercent > 2) {
//                    val down = (totalThinPercent - thinCount)
//                    ((centerY - radius) - percentageHeight) + (down * (percentageHeight * 1.15f))
//                }
//                else {
//                    // alternating
//                    val additionalOffset = if (outsideLabelCount % 2 == 0) {
//                        // evens up
//                        (-1 * ((percentageHeight * 0.25f) + (alternatingOffsetMax * alternatingOffsetFactor)))
//                    } else {
//                        // odds down
//                        (percentageHeight * 0.75f) + ((alternatingOffsetMax * 3) * alternatingOffsetFactor)
//                    }
//                    (percentageY - (percentageHeight * yOffsetFactor)) + (additionalOffset)
//                }
            } else {
                // outside slice placement
                percentageY - (percentageHeight * yOffsetFactor)
            }
        } else {
            // normal inside slice placement
            adjustedLabelY + labelHeight
        }

        drawText(
            textLayoutResult = textLabel,
            brush = SolidColor(labelColor),
            topLeft = Offset(
                x = adjustedLabelX,
                y = adjustedLabelY
            ),
        )

        drawText(
            textLayoutResult = percentageLabel,
            brush = SolidColor(percentColor),
            topLeft = Offset(
                x = adjustedPercentageX,
                y = adjustedPercentageY
            )
        )

        currentStartAngle += sweepAngle
    }
}