package com.sardonicus.tobaccocellar.ui.stats

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R

@Composable
fun QuickStatsSection(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    availableSections: AvailableSections,
    selectionKey: () -> Int,
    selectionFocused: (Boolean) -> Unit,
    contracted: (Boolean) -> Unit,
    expanded: Boolean,
    updateExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Raw Stats",
                modifier = Modifier
                    .weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                color = colorScheme.onBackground
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Filtered Stats",
                modifier = Modifier
                    .weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                color = colorScheme.onBackground,
            )
        }
        // First Section basic counts
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            key(selectionKey()) { SelectionContainer(Modifier
                .weight(1f)
                .onFocusChanged { selectionFocused(it.isFocused) }
            ) {
                Text(
                    text = "${rawStats.blendsCount} blends, ${rawStats.brandsCount} brands\n" +
                            if (rawStats.averageRating.isNotBlank()) { "${rawStats.averageRating} average rating\n" } else { "" } +
                            "${rawStats.favoriteCount} favorites, ${rawStats.dislikedCount} disliked\n" +
                            "${rawStats.totalQuantity} total \"No. of Tins\"\n" +
                            "${rawStats.estimatedWeight} (estimated)\n" +
                            "${rawStats.totalZeroQuantity} out of stock" +
                            if (rawStats.totalOpened != null) "\n${rawStats.totalOpened} opened" else "",
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                )
            } }
            Spacer(Modifier.width(8.dp))
            key(selectionKey()) { SelectionContainer(Modifier
                .weight(1f)
                .onFocusChanged { selectionFocused(it.isFocused) }
            ) {
                Text(
                    text = "${filteredStats.blendsCount} blends, ${filteredStats.brandsCount} brands\n" +
                            if (rawStats.averageRating.isNotBlank()) { "${filteredStats.averageRating} average rating\n" } else { "" } +
                            "${filteredStats.favoriteCount} favorites, " + "${filteredStats.dislikedCount} disliked\n" +
                            "${filteredStats.totalQuantity} total \"No. of Tins\"\n" +
                            "${filteredStats.estimatedWeight} (estimated)\n" +
                            "${filteredStats.totalZeroQuantity} out of stock" +
                            if (rawStats.totalOpened != null)"\n${filteredStats.totalOpened} opened" else "",
                    modifier = Modifier
                        .weight(1f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                )
            } }
        }

        // Second Section counts per type
        if (availableSections.type) {
            StatSubSection(
                label = "Blend Type",
                rawField = rawStats.totalByType,
                filteredField = filteredStats.totalByType,
                selectionKey = selectionKey,
                selectionFocused = selectionFocused,
                modifier = Modifier
            )
        }
        if (availableSections.anyAvailable) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (expanded) {
                    availableSections.available.forEach { (label, raw, filtered) ->
                        StatSubSection(
                            label = label,
                            rawField = raw,
                            filteredField = filtered,
                            selectionKey = selectionKey,
                            selectionFocused = selectionFocused,
                            modifier = Modifier
                        )
                    }
                }
            }

            // Expand/collapse
            if (expanded) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .padding(end = 24.dp)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { contracted(true) }
                ) {
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                    Icon(
                        painter = painterResource(id = R.drawable.double_up),
                        contentDescription = "Collapse",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(18.dp),
                        tint = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .padding(end = 24.dp)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { updateExpanded(true) }
                ) {
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                    Icon(
                        painter = painterResource(id = R.drawable.double_down),
                        contentDescription = "Expand",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(18.dp),
                        tint = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatSubSection(
    label: String,
    rawField: Map<String, Int>,
    filteredField: Map<String, Int>,
    selectionKey: () -> Int,
    selectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box (
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            modifier = Modifier
                .padding(start = 2.dp)
                .offset(y = (-2).dp),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            color = colorScheme.onBackground.copy(alpha = 0.4f)
        )
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Raw Stats
            Column (
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                HorizontalDivider(Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth(.65f), 1.dp)
                key(selectionKey()) { SelectionContainer(
                    Modifier
                        .fillMaxWidth()
                        .onFocusChanged { selectionFocused(it.isFocused) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        // Value
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .padding(end = 6.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                        ) {
                            rawField.forEach {
                                Text(
                                    text = "${it.value} ",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 1.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                        // Key
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                        ) {
                            val height: Dp = with(LocalDensity.current) { 24.sp.toDp() }
                            rawField.forEach {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(.8f)
                                        .height(height),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = it.key,
                                        style = TextStyle(
                                            color = LocalContentColor.current,
                                        ),
                                        maxLines = 2,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 9.sp,
                                            maxFontSize = 15.sp,
                                            stepSize = .02.sp
                                        ),
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } }
            }

            Spacer(Modifier.width(8.dp))

            // Filtered Stats
            Column (
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                HorizontalDivider(Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth(.65f), 1.dp)
                key(selectionKey()) { SelectionContainer(
                    Modifier.onFocusChanged { selectionFocused(it.isFocused) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        if (filteredField.any { it.value > 0 } ) {
                            // Value
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .padding(end = 12.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Top,
                            ) {
                                filteredField.forEach {
                                    if (it.value == 0) {
                                        Text(
                                            text = "",
                                            modifier = Modifier,
                                            textAlign = TextAlign.Start,
                                            fontSize = 15.sp,
                                        )
                                    } else {
                                        Text(
                                            text = "${it.value} ",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 1.dp),
                                            textAlign = TextAlign.Start,
                                            fontSize = 15.sp,
                                        )
                                    }
                                }
                            }

                            // Key
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Top,
                            ) {
                                val height: Dp = with(LocalDensity.current) { 24.sp.toDp() }
                                filteredField.forEach {
                                    if (it.value == 0) {
                                        Text(
                                            text = "--",
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .alpha(.5f),
                                            textAlign = TextAlign.Start,
                                            fontSize = 15.sp,
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(.8f)
                                                .height(height),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = it.key,
                                                style = TextStyle(
                                                    color = LocalContentColor.current,
                                                ),
                                                maxLines = 2,
                                                autoSize = TextAutoSize.StepBased(
                                                    minFontSize = 9.sp,
                                                    maxFontSize = 15.sp,
                                                    stepSize = .02.sp
                                                ),
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                val bottom = with(LocalDensity.current) { 13.5.sp.toDp() }
                                Text(
                                    text = "nothing found",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(.6f)
                                        .padding(start = 12.dp, bottom = bottom),
                                    textAlign = TextAlign.Start,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                } }
            }
        }
    }
}