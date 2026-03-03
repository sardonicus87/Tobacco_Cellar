package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Replacement stuff for ContextualFlowRow */
@Composable
fun <T> OverflowRow(
    items: List<T>,
    itemContent: @Composable (item: T) -> Unit,
    overflowIndicator: @Composable (
        overflowCount: Int,
        enabledOverflowCount: Int,
        isOverflowEnabled: Boolean,
    ) -> Unit,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    enabledAtIndex: ((index: Int) -> Boolean)? = null,
) {
    val density = LocalDensity.current
    val spacingPx =
        remember(itemSpacing, density) { with(density) { itemSpacing.toPx() } }.toInt()

    SubcomposeLayout(modifier = modifier) { constraints ->
        val itemCount = items.size
        val maxWidth = constraints.maxWidth
        if (itemCount == 0) {
            return@SubcomposeLayout layout(0, 0) {}
        }

        // measure indicator
        val maxPossibleOverflow = itemCount
        val overflowMeasurable = subcompose("overflow_max") { overflowIndicator(maxPossibleOverflow, maxPossibleOverflow, true) }.firstOrNull()
        val overflowPlaceable = overflowMeasurable?.measure(Constraints())
        val overflowIndicatorWidth = overflowPlaceable?.width ?: 0
        val overflowIndicatorHeight = overflowPlaceable?.height ?: 0

        // measure items and check fit in row
        var currentItemsWidth = 0
        var visibleItemCount = 0
        val placeables = mutableListOf<Placeable>()
        var lineMaxHeight = 0

        val allItemMeasurables = subcompose("items_measure") {
            for (i in 0 until itemCount) itemContent(items[i])
        }

        for (i in 0 until itemCount) {
            val itemMeasurable = allItemMeasurables[i]
            val itemPlaceable = itemMeasurable.measure(Constraints())
            val itemWidth = itemPlaceable.width + if (visibleItemCount > 0) spacingPx else 0

            // checking item fit by adding items until over width
            if (currentItemsWidth + itemWidth <= maxWidth) {
                placeables.add(itemPlaceable)
                currentItemsWidth += itemWidth
                lineMaxHeight = maxOf(lineMaxHeight, itemPlaceable.height)
                visibleItemCount++
            } else {
                val widthVisibleSoFar = currentItemsWidth
                val widthWithOverflow =
                    widthVisibleSoFar + (if (visibleItemCount > 0 && overflowIndicatorWidth > 0) spacingPx else 0) + overflowIndicatorWidth

                if (widthWithOverflow <= maxWidth) {
                    // everything fits,  no op
                } else {
                    // remove additional items as needed until overflow indicator fits
                    while (visibleItemCount > 0) {
                        visibleItemCount--
                        currentItemsWidth = placeables.take(visibleItemCount)
                            .sumOf { it.width } + if (visibleItemCount > 0) (visibleItemCount - 1) * spacingPx else 0

                        val newWidthWithOverflow =
                            currentItemsWidth + (if (visibleItemCount > 0 && overflowIndicatorWidth > 0) spacingPx else 0) + overflowIndicatorWidth
                        if (newWidthWithOverflow <= maxWidth) {
                            break // overflow indicator fits
                        }
                    }

                    if (visibleItemCount == 0 && overflowIndicatorWidth > 0 && overflowIndicatorWidth > maxWidth) {
                        return@SubcomposeLayout layout(0, 0) {} // even overflow indicator doesn't fit
                    }
                }
                break
            }
        }

        val actualOverCount = itemCount - visibleItemCount
        val showOver = actualOverCount > 0 && overflowIndicatorWidth > 0

        var enabledOverflowCount = 0
        var anyOverflowedEnabled = false
        if (showOver && enabledAtIndex != null) {
            for (i in visibleItemCount until itemCount) {
                if (enabledAtIndex(i)) {
                    enabledOverflowCount++
                    anyOverflowedEnabled = true
                }
            }
        } else if (showOver) {
            enabledOverflowCount = actualOverCount
            anyOverflowedEnabled = true
        }

        // final composition
        val finalPlaceables = subcompose("final_render") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                repeat(visibleItemCount) { i -> itemContent(items[i]) }

                if (showOver) { overflowIndicator(actualOverCount, enabledOverflowCount, anyOverflowedEnabled) }
            }
        }.map { it.measure(constraints) }

        val finalWidth = finalPlaceables.firstOrNull()?.width ?: 0
        val finalHeight = finalPlaceables.firstOrNull()?.height ?: maxOf(
            lineMaxHeight, if (showOver) overflowIndicatorHeight else 0
        )

        layout(finalWidth, finalHeight) {
            finalPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}