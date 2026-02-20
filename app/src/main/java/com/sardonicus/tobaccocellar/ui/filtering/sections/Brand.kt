package com.sardonicus.tobaccocellar.ui.filtering.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
    val excludeSwitch by filterViewModel.sheetSelectedExcludeBrandSwitch.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        // Search bar and brand include/exclude button //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandFilterSearch(
                filterViewModel = filterViewModel,
                modifier = Modifier
                    .weight(1f, false)
            )

            IncludeExcludeSwitch(
                excluded = { excludeSwitch },
                onClick = filterViewModel::updateSelectedExcludeBrandsSwitch,
                modifier = Modifier
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
    var hasFocus by remember { mutableStateOf(false) }
    val showCursor by remember(hasFocus) { mutableStateOf(hasFocus) }

    BasicTextField(
        value = brandSearchText,
        onValueChange = filterViewModel::updateBrandSearchText,
        modifier = modifier
            .background(color = LocalCustomColors.current.textField, RoundedCornerShape(6.dp))
            .height(48.dp)
            .onFocusChanged { focusState ->
                hasFocus = focusState.hasFocus
                if (!focusState.hasFocus) {
                    focusManager.clearFocus()
                }
            }
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
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (brandSearchText.isEmpty() && !hasFocus) {
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
            .background(
                LocalCustomColors.current.textField,
                RoundedCornerShape(8.dp)
            )
            .border(
                Dp.Hairline,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp)
            .combinedClickable(
                onClick = { onClick() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Include",
            modifier = Modifier
                .padding(0.dp)
                .offset(y = 3.dp),
            color = if (!excluded()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = if (!excluded()) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Text(
            text = "Exclude",
            modifier = Modifier
                .padding(0.dp)
                .offset(y = (-3).dp),
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
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return (Offset(x = available.x, y = 0f))
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(x = available.x, y = 0f)
            }
        }
    }

    val excludeSwitch by filterViewModel.sheetSelectedExcludeBrandSwitch.collectAsState()
    val filteredBrands by filterViewModel.filteredBrands.collectAsState()
    val unselectedBrands by filterViewModel.unselectedBrands.collectAsState()
    val brandEnabled by filterViewModel.brandEnabled.collectAsState()

    val clickAction = remember {
        { brand: String ->
            if (excludeSwitch) {
                updateSelectedExcludedBrands(brand, true)
            } else {
                updateSelectedBrands(brand, true)
            }
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
                val enabled by remember(brand) { derivedStateOf { brandEnabled[brand] ?: true } }

                BrandTextButton(
                    brand = { brand },
                    onClickAction = { clickAction(brand) },
                    enabled = { enabled },
                    modifier = Modifier
                )
            }
        }

        LaunchedEffect(filteredBrands) { lazyListState.scrollToItem(0) }
        LaunchedEffect(brandEnabled) { lazyListState.scrollToItem(0) }
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
    ) {
        Text(
            text = brand(),
            modifier = Modifier
        )
    }
}

@Composable
private fun SelectedBrandChipBox(
    filterViewModel: FilterViewModel,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    updateSelectedBrands: (String, Boolean) -> Unit,
    clearAllSelectedBrands: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val excludeSwitch by filterViewModel.sheetSelectedExcludeBrandSwitch.collectAsState()
    val selectedBrands by filterViewModel.selectedBrand.collectAsState()
    val showBrandChipOverflow by filterViewModel.showBrandChipOverflow.collectAsState()
    val maxWidth by filterViewModel.chipMaxWidth.collectAsState()

    val density = LocalDensity.current

    Box (
        modifier = Modifier
            .onGloballyPositioned {
                filterViewModel.updateChipBoxWidth(with(density) { it.size.width.toDp() })
            }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            Dp.Hairline,
                            LocalCustomColors.current.sheetBoxBorder.copy(alpha = .8f),
                            RoundedCornerShape(8.dp)
                        )
                        .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                        .height(96.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                ) {
                    val overflowCount by remember { derivedStateOf { selectedBrands.size - 5 } }

                    selectedBrands.take(5).forEach { brand ->
                        val onRemoved = remember(brand, excludeSwitch) {
                            {
                                if (excludeSwitch) {
                                    updateSelectedExcludedBrands(brand, false)
                                } else {
                                    updateSelectedBrands(brand, false)
                                }
                            }
                        }

                        Chip(
                            text = brand,
                            onChipClicked = { },
                            onChipRemoved = onRemoved,
                            trailingIcon = true,
                            iconSize = 20.dp,
                            trailingTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxWidth = maxWidth,
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
                    }

                    if (overflowCount > 0) {
                        Chip(
                            text = "+$overflowCount",
                            onChipClicked = { filterViewModel.showBrandOverflow() },
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
                    }
                }

                Box {
                    val nothingSelected by remember { derivedStateOf { selectedBrands.isNotEmpty() } }
                    //   if (selectedBrands.isEmpty())
                    Text(
                        text = if (nothingSelected) "" else if (excludeSwitch) "Excluded Brands" else "Included Brands",
                        modifier = Modifier
                            .padding(0.dp),
                        color = if (nothingSelected) Color.Transparent else if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (showBrandChipOverflow) {
                SelectedBrandOverflow(
                    onDismiss = filterViewModel::showBrandOverflow,
                    excludeSwitch = { excludeSwitch },
                    selectedBrands = { selectedBrands },
                    updateSelectedExcludedBrands = updateSelectedExcludedBrands,
                    updateSelectedBrands = updateSelectedBrands,
                    clearAllSelectedBrands = clearAllSelectedBrands,
                    modifier = Modifier
                )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                GlowBox(
                    color = GlowColor(MaterialTheme.colorScheme.background),
                    size = GlowSize(vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .heightIn(min = 0.dp, max = 280.dp),
                        userScrollEnabled = true,
                        contentPadding = PaddingValues(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy((-6).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(selectedBrands()) { brand ->
                            val onRemoved = remember(brand, excludeSwitch) {
                                {
                                    if (excludeSwitch()) {
                                        updateSelectedExcludedBrands(brand, false)
                                    } else {
                                        updateSelectedBrands(brand,  false)
                                    }
                                }
                            }
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            clearAllSelectedBrands()
                            onDismiss()
                        },
                        modifier = Modifier
                            .offset(x = (-4).dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(end = 3.dp)
                                .size(20.dp)
                        )
                        Text(
                            text = "Clear All",
                            modifier = Modifier,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
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
                    modifier = Modifier
                        .width(25.dp),
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
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = fontSize,
                            minFontSize = 9.sp,
                            stepSize = .2.sp
                        )
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
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { onChipRemoved() }
                        .size(iconSize),
                    tint = trailingTint
                )
            } else { /** do nothing */ }
        },
        modifier = modifier
            .widthIn(max = maxWidth)
            .padding(0.dp),
        enabled = enabled,
        colors = colors,
        border = border
    )
}