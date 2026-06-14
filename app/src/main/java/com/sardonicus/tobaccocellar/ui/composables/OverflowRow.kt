package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp

/** Replacement stuff for ContextualFlowRow */
@Composable
fun <T> OverflowRow(
    items: List<T>,
    itemContent: @Composable (item: T) -> Unit,
    overflowIndicator: @Composable (
        visibleCount: Int,
        overflowCount: Int,
        enabledOverflowCount: Int,
        isOverflowEnabled: Boolean,
    ) -> Unit,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    enabledAtIndex: ((index: Int) -> Boolean)? = null,
) {
    val density = LocalDensity.current
    val spacingPx = remember(itemSpacing, density) { with(density) { itemSpacing.toPx() }.toInt() }

    SubcomposeLayout(modifier = modifier) { constraints ->
        val itemCount = items.size
        if (itemCount == 0) return@SubcomposeLayout layout(0, 0) {}
        val maxWidth = constraints.maxWidth

        // measure indicator
        val overflowMeasurable = subcompose("overflow") { overflowIndicator(0, itemCount, itemCount, true) }
            .firstOrNull()

        val overflowPlaceable = overflowMeasurable?.measure(constraints.copy(minWidth = 0, minHeight = 0))
        val indicatorWidth = overflowPlaceable?.width ?: 0

        // measure items and check fit in row
        val itemMeasurables = subcompose("items_measure") { items.forEach { itemContent(it) } }

        val placeables = arrayOfNulls<Placeable>(itemCount)
        var currentWidth = 0
        var visibleCount = 0
        var rowMaxHeight = 0


        for (i in 0 until itemCount) {
            val placeable = itemMeasurables[i].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val spaceNeeded = if (visibleCount > 0) spacingPx else 0

            // checking item fit by adding items until over width
            if (currentWidth + spaceNeeded + placeable.width <= maxWidth) {
                placeables[i] = placeable
                currentWidth += spaceNeeded + placeable.width // itemWidth
                rowMaxHeight = maxOf(rowMaxHeight, placeable.height)
                visibleCount++
            } else { break }
        }

        val showIndicator = visibleCount < itemCount
        if (showIndicator && indicatorWidth > 0) {
            val indicatorSpaceNeeded = if (visibleCount > 0) spacingPx else 0

            while (visibleCount > 0 && currentWidth + indicatorSpaceNeeded + indicatorWidth > maxWidth) {
                visibleCount--
                val removedPlaceable = placeables[visibleCount]!!
                val removedSpace = if (visibleCount > 0) spacingPx else 0
                currentWidth -= (removedPlaceable.width + removedSpace)
                placeables[visibleCount] = null
            }
        }

        val overflowCount = itemCount - visibleCount
        val enabledOverflowCount = if (enabledAtIndex != null) {
            (visibleCount until itemCount).count { enabledAtIndex(it) }
        } else overflowCount

        val showOver = (itemCount - visibleCount) > 0

        val finalIndicatorPlaceable = if (showIndicator) {
            subcompose("final_overflow") {
                overflowIndicator(visibleCount, overflowCount, enabledOverflowCount, showOver)
            }.firstOrNull()?.measure(constraints.copy(minWidth = 0, minHeight = 0))
        } else null

        val contentWidth = currentWidth +
                (if (showOver && visibleCount > 0) spacingPx else 0) +
                (finalIndicatorPlaceable?.width ?: 0)
        val contentHeight = maxOf(rowMaxHeight, finalIndicatorPlaceable?.height ?: 0)

        val resolvedWidth = constraints.constrainWidth(contentWidth)
        val resolvedHeight = constraints.constrainHeight(contentHeight)

        layout(resolvedWidth, resolvedHeight) {
            var xPosition = 0
            for (i in 0 until visibleCount) {
                val placeable = placeables[i] ?: continue
                placeable.placeRelative(xPosition, (resolvedHeight - placeable.height) / 2)
                xPosition += placeable.width + spacingPx
            }
            finalIndicatorPlaceable?.let { it.placeRelative(xPosition, (resolvedHeight - it.height) / 2) }
        }
    }
}