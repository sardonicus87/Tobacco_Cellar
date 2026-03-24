package com.sardonicus.tobaccocellar.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TableViewMode(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
    sortedItems: ItemsList,
    columnState: LazyListState,
    shadowAlpha: () -> Float,
    tableLayoutData: TableLayoutData,
    sorting: TableSorting,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onShowMenu: (Int) -> Unit,
    onDismissMenu: () -> Unit,
    updateSorting: (Int) -> Unit,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScroll = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val switch by remember(screenWidth, tableLayoutData.totalWidth) { derivedStateOf { screenWidth.dp >= tableLayoutData.totalWidth } }

    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current

    val activeMenuId by viewModel.activeMenuId.collectAsState()

    val onClick = remember {
        { itemId: Int ->
            if (filterViewModel.searchFocused.value) {
                focusManager.clearFocus()
            } else {
                when {
                    activeMenuId == itemId -> { }
                    activeMenuId != null -> { onDismissMenu() }
                    else -> {
                        if (!filterViewModel.searchPerformed.value) {
                            filterViewModel.getPositionTrigger()
                        }
                        onDetailsClick(itemId)
                    }
                }
            }
        }
    }
    val onLongClick = remember {
        { itemId: Int ->
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (!filterViewModel.searchPerformed.value) {
                filterViewModel.getPositionTrigger()
            }
            onShowMenu(itemId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Items
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll, overscrollEffect = null),
            state = columnState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            stickyHeader {
                TableHeaderRow(
                    viewModel = viewModel,
                    filterViewModel = filterViewModel,
                    layoutData = tableLayoutData,
                    updateSorting = updateSorting,
                    sorting = sorting,
                    shouldScrollUp = shouldScrollUp,
                    onDismissMenu = onDismissMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                        .dropShadow(
                            shape = RectangleShape,
                            shadow = Shadow(
                                radius = 3.dp,
                                spread = 1.dp,
                                offset = DpOffset(0.dp, 3.dp),
                                alpha = shadowAlpha()
                            )
                        )
                )
            }

            items(items = sortedItems.list, key = { it.itemId }) { item ->
                val openMenu by remember(item.itemId) { derivedStateOf { activeMenuId == item.itemId } }
                val view = LocalView.current

                Box {
                    TableItem(
                        item = item,
                        layoutData = tableLayoutData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(intrinsicSize = IntrinsicSize.Min)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .combinedClickable(
                                onClick = { onClick(item.itemId) },
                                onLongClick = {
                                    view.isHapticFeedbackEnabled = !openMenu
                                    onLongClick(item.itemId)
                                },
                                indication = null,
                                interactionSource = null
                            )
                    )

                    Box (
                        modifier = Modifier
                            .matchParentSize()
                    ) {
                        AnimatedVisibility(
                            visible = openMenu,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(if (switch) tableLayoutData.totalWidth.value.dp else screenWidth.dp)
                                .offset { IntOffset(if (switch) 0 else horizontalScroll.value, 0) },
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(150))
                        ) {
                            ItemMenu(
                                viewModel = viewModel,
                                activeItemId = { item.itemId },
                                onMenuDismiss = onDismissMenu,
                                onEditClick = { onEditClick(item.itemId) },
                                modifier = Modifier,
                            )
                        }
                    }
                }

                // tins
                if (item.tins.tins.isNotEmpty()) {
                    TableTinsList(
                        item.tins,
                        modifier
                            .width(tableLayoutData.totalWidth)
                            .padding(start = 12.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun TableItem(
    item: ItemsListState,
    layoutData: TableLayoutData,
    modifier: Modifier = Modifier
) {
    // item
    Box(Modifier) {
        Row(
            modifier = modifier
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            for (columnIndex in layoutData.columnMinWidths.values.indices) {
                Box(
                    modifier = Modifier
                        .width(layoutData.columnMinWidths.values[columnIndex])
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .border(Dp.Hairline, LocalCustomColors.current.tableBorder)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = layoutData.alignment.values[columnIndex]
                ) {
                    when (columnIndex) {
                        0, 1, 2, 3, 4 -> { // brand, blend, type, subgenre, rating
                            Text(
                                text = layoutData.columnMapping.values[columnIndex](item.item.items)?.toString() ?: "",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        } // brand, blend, type, subgenre, rating
                        5 -> { // fav/disliked
                            val favDisValue = layoutData.columnMapping.values[columnIndex](item.item.items) as Int
                            if (favDisValue != 0) {
                                Image(
                                    painter = painterResource(if (favDisValue == 1) R.drawable.heart_filled_24 else R.drawable.heartbroken_filled_24),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp),
                                    colorFilter = ColorFilter.tint(if (favDisValue == 1) LocalCustomColors.current.favHeart else LocalCustomColors.current.disHeart)
                                )
                            }
                        } // fav/disliked
                        6 -> { // notes
                            if (layoutData.columnMapping.values[columnIndex](item.item.items)?.toString()?.isNotEmpty() == true) {
                                Image(
                                    painter = painterResource(id = R.drawable.notes_24),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp),
                                    colorFilter =  ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                                )
                            }
                        } // notes
                        7 -> { // quantity
                            Text(
                                text = item.formattedQuantity,
                                color = if (item.outOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        } // quantity
                        else -> {
                            Text(
                                text = layoutData.columnMapping.values[columnIndex](item.item.items)?.toString() ?: "",
                                modifier = Modifier,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderRow(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
    layoutData: TableLayoutData,
    updateSorting: (Int) -> Unit,
    sorting: TableSorting,
    shouldScrollUp: () -> Unit,
    onDismissMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val searchFocused by filterViewModel.searchFocused.collectAsState()
    val activeMenuId by viewModel.activeMenuId.collectAsState()

    val onClick = remember {
        { columnIndex: Int ->
            if (searchFocused) {
                focusManager.clearFocus()
            } else {
                if (activeMenuId != null) {
                    onDismissMenu()
                } else {
                    updateSorting(columnIndex)
                    shouldScrollUp()
                }
            }
        }
    }

    Row(
        modifier = modifier
            .height(intrinsicSize = IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        for (columnIndex in layoutData.columnMinWidths.values.indices) {
            Box(
                modifier = Modifier
                    .width(layoutData.columnMinWidths.values[columnIndex])
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .border(Dp.Hairline, LocalCustomColors.current.tableBorder)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = layoutData.alignment.values[columnIndex]
            ) {
                when (columnIndex) {
                    0, 1, 2, 3, 7 -> { // brand, blend, type, subgenre, quantity
                        Box(
                            modifier = Modifier
                                .clickable(
                                    onClick = { onClick(columnIndex) },
                                    indication = null,
                                    interactionSource = null
                                )
                                .matchParentSize(),
                            contentAlignment = layoutData.alignment.values[columnIndex]
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = layoutData.headerText.values[columnIndex],
                                    modifier = Modifier,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (sorting.columnIndex == columnIndex) {
                                    Image(
                                        painter = painterResource(id = sorting.sortIcon),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .align(Alignment.CenterEnd)
                                            .offset(x = 20.dp),
                                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                                    )
                                }
                            }
                        }
                    }
                    4 -> { // rating
                        Box(
                            modifier = Modifier
                                .clickable(
                                    onClick = { onClick(columnIndex) },
                                    indication = null,
                                    interactionSource = null
                                )
                                .matchParentSize(),
                            contentAlignment = layoutData.alignment.values[columnIndex]
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.star_filled),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(LocalCustomColors.current.starRating),
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                if (sorting.columnIndex == columnIndex) {
                                    Image(
                                        painter = painterResource(id = sorting.sortIcon),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .align(Alignment.CenterEnd)
                                            .offset(x = 20.dp),
                                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                                    )
                                }
                            }
                        }
                    }
                    5 -> { // fav/dislike
                        Box(contentAlignment = layoutData.alignment.values[columnIndex]) {
                            Image(
                                painter = painterResource(id = R.drawable.heart_filled_24),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(LocalCustomColors.current.favHeart),
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.question_mark_24),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.Black),
                                modifier = Modifier
                                    .size(12.dp)
                            )
                        }
                    }
                    6 -> { // notes
                        Box(contentAlignment = layoutData.alignment.values[columnIndex]) {
                            Text(
                                text = layoutData.headerText.values[columnIndex],
                                modifier = Modifier,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun TableTinsList(
    filteredTins: TinsList,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier
                .background(
                    LocalCustomColors.current.sheetBox,
                    RoundedCornerShape(bottomStart = 8.dp)
                )
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                .fillMaxWidth()
        ) {
            filteredTins.tins.forEachIndexed { index, it ->
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = it.tinLabel,
                        modifier = Modifier,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    if (it.container.isNotEmpty() || it.unit.isNotEmpty()) {
                        Text(
                            text = " (",
                            modifier = Modifier,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp
                        )
                        if (it.container.isNotEmpty()) {
                            Text(
                                text = it.container,
                                modifier = Modifier,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                        if (it.container.isNotEmpty() && it.unit.isNotEmpty()) {
                            Text(
                                text = " - ",
                                modifier = Modifier,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                        if (it.unit.isNotEmpty()) {
                            val quantity =
                                formatDecimal(it.tinQuantity)
                            val unit = when (it.unit) {
                                "grams" -> "g"
                                else -> it.unit
                            }
                            Text(
                                text = "$quantity $unit",
                                modifier = Modifier,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = ")",
                            modifier = Modifier,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                    if (index != filteredTins.tins.lastIndex) {
                        VerticalDivider(Modifier
                            .fillMaxHeight(.85f)
                            .padding(horizontal = 12.dp), thickness = 2.dp, color = LocalCustomColors.current.tableBorder)
                    }
                }
            }
        }
    }
}

enum class TableColumn(val title: String) {
    BRAND("Brand"),
    BLEND("Blend"),
    TYPE("Type"),
    SUBGENRE("Subgenre"),
    RATING("Rating"),
    FAV_DIS("Fav/Dis"),
    NOTE("Notes"),
    QTY("Quantity")
}