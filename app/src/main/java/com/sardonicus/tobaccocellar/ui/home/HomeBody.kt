package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun HomeBody(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
    showLoading: () -> Boolean,
    isTableView: () -> Boolean,
    coroutineScope: () -> CoroutineScope,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val columnState = rememberLazyListState()
    val sortedItems by viewModel.itemsListState.collectAsState()
    val itemsCount by viewModel.itemsCount.collectAsState()
    val showColumnMenu by viewModel.showColumnMenu.collectAsState()

    LaunchedEffect(columnState) {
        snapshotFlow { columnState.layoutInfo.visibleItemsInfo.isNotEmpty() }
            .distinctUntilChanged()
            .collect { viewModel.updateListRendered(it) }
    }

    Box {
        BodyContent(
            viewModel, filterViewModel, isTableView, columnState, sortedItems, onDetailsClick,
            onEditClick, shouldScrollUp, modifier
        )

        if (showLoading()) { LoadingIndicator() }

        if (showColumnMenu) {
            ColumnVisibilityPopup(
                viewModel = viewModel,
                onVisibilityChange = viewModel::updateColumnVisibility,
                onDismiss = viewModel::showColumnMenuToggle
            )
        }

        val itemsCountPass by remember { derivedStateOf { itemsCount > 75 } }

        // jump to button
        JumpToButton(
            columnState = columnState,
            itemCountPass = { itemsCountPass },
            onScrollToTop = { coroutineScope().launch { columnState.scrollToItem(0) } },
            onScrollToBottom = { coroutineScope().launch { columnState.scrollToItem(sortedItems.list.lastIndex) } },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
        )

        HomeScrollHandler(
            columnState, sortedItems, { itemsCount }, filterViewModel, coroutineScope(),
        )
    }
}


@Composable
private fun HomeScrollHandler(
    columnState: LazyListState,
    sortedItems: ItemsList,
    itemsCount: () -> Int,
    filterViewModel: FilterViewModel,
    coroutineScope: CoroutineScope,
) {
    val scrollState by filterViewModel.homeScrollState.collectAsState()
    val currentItemsList by rememberUpdatedState(sortedItems.list)
    val savedItemIndex = remember(sortedItems.list, scrollState.savedItemId) {
        sortedItems.list.indexOfFirst { it.itemId == scrollState.savedItemId }
    }
    val searchPerformed by filterViewModel.searchPerformed.collectAsState()

    // Scroll to Positions //
    LaunchedEffect(currentItemsList) {
        snapshotFlow { columnState.layoutInfo.visibleItemsInfo }.first { it.isNotEmpty() }

        if (savedItemIndex != -1) {
            withFrameNanos {
                coroutineScope.launch {
                    if (savedItemIndex > 0 && savedItemIndex < (itemsCount() - 1)) {
                        val offset =
                            (columnState.layoutInfo.visibleItemsInfo[1].size / 2) * -1
                        columnState.scrollToItem(savedItemIndex, offset)
                    } else {
                        columnState.scrollToItem(savedItemIndex)
                    }
                }
            }
            filterViewModel.resetScroll()
        }
        if (scrollState.shouldScrollUp) {
            columnState.scrollToItem(0)
            filterViewModel.resetScroll()
        }
        if (scrollState.shouldReturn && !searchPerformed && !scrollState.shouldScrollUp) {
            val index = scrollState.currentPosition[0]
            val offset = scrollState.currentPosition[1]

            if (index != null && offset != null) {
                withFrameNanos {
                    coroutineScope.launch {
                        columnState.scrollToItem(index, offset)
                    }
                }
                filterViewModel.resetScroll()
            }
        }
    }

    // Save positions //
    LaunchedEffect(scrollState.getPosition) {
        if (scrollState.getPosition > 0 && !searchPerformed) {
            val layoutInfo = columnState.layoutInfo
            val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()

            if (firstVisibleItem != null) {
                filterViewModel.updateScrollPosition(firstVisibleItem.index, firstVisibleItem.offset * -1)
            }
        }
    }
}




@Composable
private fun BodyContent(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
    isTableView: () -> Boolean,
    columnState: LazyListState,
    sortedItems: ItemsList,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listShadow by viewModel.listShadow.collectAsState()
    val tableSorting by viewModel.tableSorting.collectAsState()
    val tableLayoutData by viewModel.tableLayoutData.collectAsState()
    val tableShadow by viewModel.tableShadow.collectAsState()

    LaunchedEffect(isTableView()) { columnState.scrollToItem(0) }
    LaunchedEffect(columnState.canScrollBackward) { viewModel.updateScrollShadow(columnState.canScrollBackward) }

    if (sortedItems.list.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
        ) {
            val emptyMessage by viewModel.emptyMessage.collectAsState()

            Spacer(Modifier.weight(1f))
            Text(
                text = emptyMessage,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(0.dp),
            )
            Spacer(Modifier.weight(1.25f))
        }
    } else {
        if (isTableView()) {
            TableViewMode(
                viewModel = viewModel,
                filterViewModel = filterViewModel,
                sortedItems = sortedItems,
                columnState = columnState,
                shadowAlpha = { tableShadow },
                tableLayoutData = tableLayoutData,
                sorting = tableSorting,
                updateSorting = viewModel::updateSorting,
                onDetailsClick = onDetailsClick,
                onEditClick = onEditClick,
                shouldScrollUp = shouldScrollUp,
                onShowMenu = viewModel::onShowMenu,
                onDismissMenu = viewModel::onDismissMenu,
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
            )
        } else {
            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                size = GlowSize(top =  listShadow)
            ) {
                ListViewMode(
                    viewModel = viewModel,
                    filterViewModel = filterViewModel,
                    sortedItems = sortedItems,
                    columnState = columnState,
                    onDetailsClick = onDetailsClick,
                    onEditClick = onEditClick,
                    onShowMenu = viewModel::onShowMenu,
                    onDismissMenu = viewModel::onDismissMenu,
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}