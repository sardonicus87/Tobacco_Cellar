package com.sardonicus.tobaccocellar.ui.filtering.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun BrandFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val excludeSwitch by filterViewModel.excludeBrandSwitch.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Search bar and brand include/exclude button //
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandFilterSearch(filterViewModel, Modifier.weight(1f, false))

            IncludeExcludeSwitch(
                excluded = { excludeSwitch },
                onClick = filterViewModel::updateSelectedExcludeBrandsSwitch,
            )
        }

        // Selectable brands row //
        SelectableBrandsRow(
            filterViewModel = filterViewModel,
            updateSelectedExcludedBrands = filterViewModel::updateSelectedExcludedBrands,
            updateSelectedBrands = filterViewModel::updateSelectedBrands,
            updateBrandSearchText = filterViewModel::updateBrandSearchText,
            modifier = Modifier.fillMaxWidth()
        )


        // Selected brands chip box //
        SelectedBrandChipBox(
            filterViewModel = filterViewModel,
            updateSelectedExcludedBrands = filterViewModel::updateSelectedExcludedBrands,
            updateSelectedBrands = filterViewModel::updateSelectedBrands,
            clearAllSelectedBrands = filterViewModel::clearAllSelectedBrands,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BrandFilterSearch(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val brandSearchText by filterViewModel.brandSearchText.collectAsState()

    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    val showCursor by remember(focused) { mutableStateOf(focused) }

    BasicTextField(
        value = brandSearchText,
        onValueChange = filterViewModel::updateBrandSearchText,
        modifier = modifier
            .background(color = LocalCustomColors.current.textField, RoundedCornerShape(6.dp))
            .height(48.dp)
            .onFocusChanged { focused = it.hasFocus; if (!it.hasFocus) { focusManager.clearFocus() } }
            .padding(horizontal = 16.dp),
        textStyle = LocalTextStyle.current.copy(
            color = LocalContentColor.current,
            fontSize = TextUnit.Unspecified,
            lineHeight = TextUnit.Unspecified,
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.None,
        ),
        singleLine = true,
        maxLines = 1,
        cursorBrush = if (showCursor) { SolidColor(MaterialTheme.colorScheme.primary) }
            else { SolidColor(Color.Transparent) },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (brandSearchText.isEmpty() && !focused) {
                        Text(
                            text = "Search Brands",
                            style = LocalTextStyle.current.copy(
                                color = LocalContentColor.current.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun IncludeExcludeSwitch(
    excluded: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(start = 12.dp)
            .width(IntrinsicSize.Max)
            .height(48.dp)
            .background(LocalCustomColors.current.textField, RoundedCornerShape(8.dp))
            .border(
                Dp.Hairline,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp)
            .clickable(remember { MutableInteractionSource() }, null) { onClick() },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Include",
            modifier = Modifier.offset(y = 3.dp),
            color = if (!excluded()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = if (!excluded()) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Text(
            text = "Exclude",
            modifier = Modifier.offset(y = (-3).dp),
            color = if (excluded()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = if (excluded()) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SelectableBrandsRow(
    filterViewModel: FilterViewModel,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    updateSelectedBrands: (String, Boolean) -> Unit,
    updateBrandSearchText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val nestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset { return (Offset(x = available.x, y = 0f)) }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(x = available.x, y = 0f) }
        }
    }

    val excludeSwitch by filterViewModel.excludeBrandSwitch.collectAsState()
    val filteredBrands by filterViewModel.filteredBrands.collectAsState()
    val unselectedBrands by filterViewModel.unselectedBrands.collectAsState()
    val brandEnabled by filterViewModel.brandEnabled.collectAsState()
    val clearTrigger by filterViewModel.clearBrandTrigger.collectAsState()

    val clickAction = remember {
        { brand: String ->
            if (excludeSwitch) { updateSelectedExcludedBrands(brand, true) }
            else { updateSelectedBrands(brand, true) }
            updateBrandSearchText("")
        }
    }

    GlowBox(
        color = GlowColor(MaterialTheme.colorScheme.background),
        size = GlowSize(horizontal = 15.dp),
        modifier = modifier
    ) {
        val lazyListState = rememberLazyListState()

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 2.dp)
                .height(36.dp)
                .nestedScroll(nestedScroll),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            state = lazyListState
        ) {
            items(unselectedBrands, key = { it }) { brand ->
                val enabled by remember(brand) { derivedStateOf { brandEnabled[brand] == true } }

                BrandTextButton(
                    brand = { brand },
                    onClickAction = { clickAction(brand) },
                    enabled = { enabled }
                )
            }
        }

        LaunchedEffect(filteredBrands, brandEnabled, clearTrigger) { lazyListState.scrollToItem(0) }
    }
}

@Composable
private fun BrandTextButton(
    brand: () -> String,
    onClickAction: () -> Unit,
    enabled: () -> Boolean,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClickAction,
        modifier = modifier,
        enabled = enabled(),
    ) { Text(brand()) }
}

@Composable
private fun SelectedBrandChipBox(
    filterViewModel: FilterViewModel,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    updateSelectedBrands: (String, Boolean) -> Unit,
    clearAllSelectedBrands: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val excludeSwitch by filterViewModel.excludeBrandSwitch.collectAsState()
    val selectedBrands by filterViewModel.selectedBrand.collectAsState()
    var showOverflowDialog by remember { mutableStateOf(false) }
    var boxWidth by remember { mutableStateOf(0.dp) }
    val chipMaxWidth by remember { derivedStateOf { (boxWidth * 0.32f) - 4.dp } }

    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { boxWidth = with(density) { it.size.width.toDp() } }
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        Dp.Hairline,
                        LocalCustomColors.current.sheetBoxBorder.copy(alpha = .8f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp)
                    .height(96.dp)
            ) {
                DynamicChipLayout(
                    items = selectedBrands,
                    spacing = 6.dp,
                    itemContent = { brand ->
                        val onRemoved = remember(brand, excludeSwitch) { {
                            if (excludeSwitch) { updateSelectedExcludedBrands(brand, false) }
                            else { updateSelectedBrands(brand, false) }
                        } }

                        Chip(
                            text = brand,
                            onChipClicked = { },
                            onChipRemoved = onRemoved,
                            trailingIcon = true,
                            iconSize = 20.dp,
                            trailingTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxWidth = chipMaxWidth,
                            modifier = Modifier,
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.07f) else MaterialTheme.colorScheme.background,
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else
                                    MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    overflowContent = { overflowCount ->
                        Chip(
                            text = "+$overflowCount",
                            onChipClicked = { showOverflowDialog = true },
                            onChipRemoved = { },
                            trailingIcon = false,
                            modifier = Modifier,
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.07f) else MaterialTheme.colorScheme.background,
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else
                                    MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box {
                val anySelected by remember { derivedStateOf { selectedBrands.isNotEmpty() } }
                Text(
                    text = if (anySelected) "" else if (excludeSwitch) "Excluded Brands" else "Included Brands",
                    color = if (anySelected) Color.Transparent else if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    if (showOverflowDialog) {
        SelectedBrandOverflow(
            onDismiss = { showOverflowDialog = false },
            excludeSwitch = { excludeSwitch },
            selectedBrands = { selectedBrands },
            updateSelectedExcludedBrands = updateSelectedExcludedBrands,
            updateSelectedBrands = updateSelectedBrands,
            clearAllSelectedBrands = clearAllSelectedBrands
        )
    }
}


@Composable
private fun DynamicChipLayout(
    items: List<String>,
    itemContent: @Composable (String) -> Unit,
    overflowContent: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 6.dp
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }.toInt()

    SubcomposeLayout(modifier = modifier) { constraints ->
        val maxWidth = constraints.maxWidth
        val maxHeight = constraints.maxHeight

        val itemMeasurables = subcompose("items") { items.forEach { itemContent(it) } }

        val overflowMeasurable = subcompose("overflow") { overflowContent(items.size) }.firstOrNull()
        val overflowPlaceable = overflowMeasurable?.measure(constraints.copy(minWidth = 0, minHeight = 0))
        val overflowWidth = overflowPlaceable?.width ?: 0

        val rows = mutableListOf<MutableList<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0
        var visibleCount = 0

        for (i in items.indices) {
            val placeable = itemMeasurables[i].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val applySpacing = if (currentRow.isEmpty()) 0 else spacingPx

            if (currentWidth + applySpacing + placeable.width <= maxWidth) {
                currentRow.add(placeable)
                currentWidth += applySpacing + placeable.width
                visibleCount++
            }
            else {
                if (rows.isEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf(placeable)
                    currentWidth = placeable.width
                    visibleCount++
                } else { break }
            }
        }

        if (currentRow.isNotEmpty()) rows.add(currentRow)

        if (visibleCount < items.size) {
            var lastRow = rows.last()
            var lastRowWidth = lastRow.foldIndexed(0) { index, acc, placeable ->
                acc + placeable.width + (if (index > 0) spacingPx else 0)
            }

            while (lastRowWidth + spacingPx + overflowWidth > maxWidth && visibleCount > 0) {
                lastRow.removeAt(lastRow.size - 1)
                visibleCount--
                if (lastRow.isEmpty()) {
                    rows.removeAt(rows.size - 1)
                    if (rows.isEmpty()) break
                    lastRow = rows.last()
                }
                lastRowWidth = lastRow.foldIndexed(0) { index, acc, placeable ->
                    acc + placeable.width + (if (index > 0) spacingPx else 0)
                }
            }
        }

        val finalOverflowCount = items.size - visibleCount
        val finalOverflowPlaceable = if (finalOverflowCount > 0) {
            subcompose("overflow_final") { overflowContent(finalOverflowCount) }
                .firstOrNull()?.measure(constraints.copy(minWidth = 0, minHeight = 0))
        } else null

        val rowHeight = rows.firstOrNull()?.maxOfOrNull { it.height } ?: 0
        val verticalGap = (maxHeight - (2 * rowHeight)) / 3

        layout(maxWidth, maxHeight) {
            rows.forEachIndexed { index, row ->
                val currentY = (verticalGap * (index + 1)) + (rowHeight * index)

                val isLast = index == rows.size - 1
                val rowContentWidth = row.foldIndexed(0) { index, acc, placeable ->
                    acc + placeable.width + (if (index > 0) spacingPx else 0)
                } + if (isLast && finalOverflowPlaceable != null) spacingPx +
                        finalOverflowPlaceable.width else 0

                var currentX = (maxWidth - rowContentWidth) / 2
                row.forEach { placeable ->
                    placeable.placeRelative(currentX, currentY + (rowHeight - placeable.height) / 2)
                    currentX += placeable.width + spacingPx
                }

                if (isLast && finalOverflowPlaceable != null) {
                    finalOverflowPlaceable.placeRelative(currentX, currentY + (rowHeight - finalOverflowPlaceable.height) / 2)
                }
            }
        }
    }
}


@Composable
private fun SelectedBrandOverflow(
    onDismiss: () -> Unit,
    excludeSwitch: () -> Boolean,
    selectedBrands: () -> List<String>,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    updateSelectedBrands: (String, Boolean) -> Unit,
    clearAllSelectedBrands: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (excludeSwitch()) "Excluded Brands" else "Included Brands",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                GlowBox(
                    color = GlowColor(MaterialTheme.colorScheme.background),
                    size = GlowSize(vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .weight(1f, false),
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        userScrollEnabled = true,
                        contentPadding = PaddingValues(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy((-6).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(selectedBrands()) { brand ->
                            val onRemoved = remember(brand, excludeSwitch) { {
                                if (excludeSwitch()) { updateSelectedExcludedBrands(brand, false) }
                                else { updateSelectedBrands(brand,  false) }
                            } }

                            Chip(
                                text = brand,
                                onChipClicked = { },
                                onChipRemoved = onRemoved,
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = MaterialTheme.colorScheme.background,
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { clearAllSelectedBrands(); onDismiss() },
                        modifier = Modifier.offset(x = (-4).dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "",
                            modifier = Modifier.padding(end = 3.dp).size(20.dp)
                        )
                        Text(
                            text = "Clear All",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onDismiss) { Text("Close") } },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun Chip(
    text: String,
    onChipClicked: (String) -> Unit,
    onChipRemoved: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 14.sp,
    trailingIcon: Boolean = true,
    iconSize: Dp = 24.dp,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(
        enabled = enabled,
        borderColor = MaterialTheme.colorScheme.outline,
    ),
    maxWidth: Dp = Dp.Infinity,
    trailingTint: Color = LocalContentColor.current
) {
    AssistChip(
        onClick = { onChipClicked(text) },
        label = {
            if (text.startsWith("+")) {
                Box (
                    modifier = Modifier.width(25.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        maxLines = 1,
                        modifier = Modifier,
                        style = LocalTextStyle.current.copy(
                            color = LocalContentColor.current
                        ),
                        minLines = 1,
                        autoSize = TextAutoSize.StepBased(9.sp, fontSize, 0.2.sp)
                    )
                }
            }
            else {
                Text(
                    text = text,
                    fontSize = fontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        trailingIcon = {
            if (trailingIcon) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Remove Chip",
                    modifier = Modifier
                        .clickable(null, LocalIndication.current) { onChipRemoved() }
                        .size(iconSize),
                    tint = trailingTint
                )
            } else { /** do nothing */ }
        },
        modifier = modifier.widthIn(max = maxWidth).padding(0.dp),
        enabled = enabled,
        colors = colors,
        border = border
    )
}