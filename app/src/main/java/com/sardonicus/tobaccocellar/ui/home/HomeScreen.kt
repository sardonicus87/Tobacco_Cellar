package com.sardonicus.tobaccocellar.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarBottomAppBar
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.CheckboxWithLabel
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.HomeScrollState
import com.sardonicus.tobaccocellar.ui.SearchState
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.navigation.HomeDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ScrollDirection { UP, DOWN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToStats: () -> Unit,
    navigateToDates: () -> Unit,
    navigateToAddEntry: () -> Unit,
    navigateToBlendDetails: (Int) -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    navigateToBulkEdit: () -> Unit,
    navigateToCsvImport: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToHelp: () -> Unit,
    navigateToPlaintext: () -> Unit,
    filterViewModel: FilterViewModel,
    isTwoPane: Boolean,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val homeUiState by viewModel.homeUiState.collectAsState()
    val searchState by filterViewModel.searchState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    val showSnackbar by viewModel.showSnackbar.collectAsState()
    if (showSnackbar) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "CSV Exported",
                duration = SnackbarDuration.Short
            )
            viewModel.snackbarShown()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.snackbarShown()
            viewModel.onDismissMenu()
        }
    }

    BackHandler(searchState.searchFocused || searchState.searchPerformed) {
        if (searchState.searchFocused) {
            focusManager.clearFocus()
            filterViewModel.updateSearchFocused(false)
        } else {
            filterViewModel.updateSearchText("")
            filterViewModel.onSearch("")
            if (searchState.searchPerformed) {
                coroutineScope.launch {
                    EventBus.emit(SearchClearedEvent)
                }
            }
        }
    }

    // Important Alert stuff
    val importantAlertState by viewModel.importantAlertState.collectAsState()
    if (importantAlertState.show) {
        ImportantAlertDialog(
            importantAlertState = importantAlertState,
            viewModel = viewModel
        )
    }


    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) {
                focusManager.clearFocus()
                viewModel.onDismissMenu()
            },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.home_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = false,
                navigateToBulkEdit = navigateToBulkEdit,
                navigateToCsvImport = navigateToCsvImport,
                navigateToSettings = navigateToSettings,
                navigateToHelp = navigateToHelp,
                navigateToPlaintext = navigateToPlaintext,
                showMenu = true,
                currentDestination = HomeDestination,
                exportCsvHandler = viewModel,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier,
                navigateToDates = navigateToDates,
                navigateToStats = navigateToStats,
                navigateToAddEntry = navigateToAddEntry,
                currentDestination = HomeDestination,
                isTwoPane = isTwoPane
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(0.dp),
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeHeader(
                viewModel = viewModel,
                searchState = searchState,
                updateSearchText = filterViewModel::updateSearchText,
                onSearch = filterViewModel::onSearch,
                updateSearchFocused = filterViewModel::updateSearchFocused,
                getPositionTrigger = filterViewModel::getPositionTrigger,
                saveSearchSetting = filterViewModel::saveSearchSetting,
                onExpandSearchMenu = filterViewModel::setSearchMenuExpanded,
                onShowColumnPop = viewModel::showColumnMenuToggle,
                saveListSorting = viewModel::saveListSorting,
                shouldScrollUp = filterViewModel::shouldScrollUp,
                modifier = Modifier,
            )
                HomeBody(
                    viewModel = viewModel,
                    filterViewModel = filterViewModel,
                    showLoading = { homeUiState.isLoading },
                    isTableView = { homeUiState.isTableView },
                    coroutineScope = { coroutineScope },
                    onDetailsClick = navigateToBlendDetails,
                    onEditClick = navigateToEditEntry,
                    isMenuShown = { menuState.isMenuShown },
                    activeMenuId = { menuState.activeMenuId },
                    getPositionTrigger = filterViewModel::getPositionTrigger,
                    searchFocused = { searchState.searchFocused },
                    searchPerformed = { searchState.searchPerformed },
                    shouldScrollUp = filterViewModel::shouldScrollUp,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                )
          //  }
        }
    }
}


/** Header stuff **/
@Composable
private fun HomeHeader(
    viewModel: HomeViewModel,
    searchState: SearchState,
    updateSearchText: (String) -> Unit,
    onSearch: (String) -> Unit,
    updateSearchFocused: (Boolean) -> Unit,
    getPositionTrigger: () -> Unit,
    saveSearchSetting: (String) -> Unit,
    onExpandSearchMenu: (Boolean) -> Unit,
    onShowColumnPop: () -> Unit,
    saveListSorting: (ListSortOption) -> Unit,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(LocalCustomColors.current.homeHeaderBg)
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Select view
        ViewSelect(
            viewModel = viewModel,
        //    selectView = selectView,
            modifier = Modifier
                .width(74.dp)
        )

        Spacer(Modifier.width(8.dp))

        // Search field
        Box(
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .weight(1f, false),
        ) {
            SearchField(
                state = searchState,
                updateSearchText = updateSearchText,
                onSearch = onSearch,
                updateSearchFocused = updateSearchFocused,
                getPositionTrigger = getPositionTrigger,
                saveSearchSetting = saveSearchSetting,
                onExpandSearchMenu = onExpandSearchMenu,
                modifier = Modifier
            )
        }

        Spacer(Modifier.width(8.dp))

        // total items & list sorting or column hiding
        Row(
            modifier = modifier
                .width(68.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            ListColumnMenu(
                viewModel = viewModel,
                shouldScrollUp = shouldScrollUp,
                saveListSorting = saveListSorting,
                onShowColumnPop = onShowColumnPop,
                modifier = Modifier
            )
            Spacer(Modifier.width(6.dp))
            TotalCount(viewModel)
        }
    }
}

@Composable
private fun ViewSelect(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.viewSelect.collectAsState()

    Row(
        modifier = modifier
            .padding(0.dp)
            .width(74.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "View:",
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(0.dp)
        )
        IconButton(
            onClick = viewModel::selectView, // { viewModel.selectView(!state.isTableView) },
            modifier = Modifier
                .padding(4.dp)
                .size(22.dp)
        ) {
            Icon(
                painter = painterResource(state.toggleIcon),
                contentDescription = stringResource(state.toggleContentDescription),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(20.dp)
                    .padding(0.dp)
            )
        }
    }
}

@Composable
private fun SearchField (
    state: SearchState,
    updateSearchText: (String) -> Unit,
    onSearch: (String) -> Unit,
    updateSearchFocused: (Boolean) -> Unit,
    getPositionTrigger: () -> Unit,
    saveSearchSetting: (String) -> Unit,
    onExpandSearchMenu: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    CustomBlendSearch(
        value = { state.searchText },
        onValueChange = {
            updateSearchText(it)
            if (it.isEmpty()) {
                onSearch(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused) updateSearchFocused(true)
                else updateSearchFocused(false)
            },
        onImeAction = {
            coroutineScope.launch {
                if (state.searchText.isNotBlank()) {
                    if (!state.searchPerformed) { getPositionTrigger() }
                    delay(15)
                    EventBus.emit(SearchPerformedEvent)
                    onSearch(state.searchText)
                }
            }
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .clickable(
                        enabled = state.settingsEnabled && !state.emptyDatabase,
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { onExpandSearchMenu(!state.searchMenuExpanded) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .size(20.dp),
                    tint = LocalContentColor.current.copy(alpha = state.searchIconOpacity)
                )
                Icon(
                    painter = painterResource(id = R.drawable.triangle_arrow_down),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 7.dp, y = 0.dp)
                        .padding(0.dp)
                        .size(16.dp),
                    tint = if (state.settingsEnabled && !state.emptyDatabase) LocalContentColor.current.copy(alpha = state.searchIconOpacity) else Color.Transparent
                )
            }
            DropdownMenu(
                expanded = state.searchMenuExpanded,
                onDismissRequest = { onExpandSearchMenu(false) },
                modifier = Modifier,
                containerColor = LocalCustomColors.current.textField,
                offset = DpOffset((-2).dp, 2.dp)
            ) {
                state.settingsList.settings.forEach {
                    DropdownMenuItem(
                        text = { Text(text = it.value) },
                        onClick = {
                            saveSearchSetting(it.value)
                            onExpandSearchMenu(false)
                        },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = true,
                    )
                }
            }
        },
        trailingIcon = {
            if (state.searchText.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.clear_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) {
                            updateSearchText("")
                            onSearch("")

                            if (state.searchPerformed) {
                                coroutineScope.launch {
                                    EventBus.emit(SearchClearedEvent)
                                }
                            }
                        }

                        .padding(0.dp),
                )
            }
        },
        placeholder = "${state.currentSetting.value} Search",
    )
}

@Composable
private fun ListColumnMenu(
    viewModel: HomeViewModel,
    shouldScrollUp: () -> Unit,
    saveListSorting: (ListSortOption) -> Unit,
    onShowColumnPop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.listSortingMenuState.collectAsState()

    Box(modifier = modifier) {
        // List Sorting
        if (!state.isTableView) {
            var sortingMenu by rememberSaveable { mutableStateOf(false) }

            IconButton(
                onClick = { sortingMenu = !sortingMenu },
                modifier = Modifier
                    .padding(4.dp)
                    .size(22.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sort_bars),
                    contentDescription = "List sorting",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(0.dp),
                )
            }
            DropdownMenu(
                expanded = sortingMenu,
                onDismissRequest = { sortingMenu = false },
                shadowElevation = 4.dp,
                modifier = Modifier,
                containerColor = LocalCustomColors.current.textField,
            ) {
                state.sortingOptions.options.forEach {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = it.value,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier
                                )
                                Box(
                                    modifier = Modifier
                                        .width(14.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (state.listSorting.option.value == it.value) {
                                        val icon = state.listSorting.listIcon
                                        Image(
                                            painter = painterResource(id = icon),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .offset(x = (-6).dp)
                                                .padding(0.dp),
                                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            saveListSorting(it)
                            shouldScrollUp()
                        },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = true,
                    )
                }
            }
        } else {
            IconButton(
                onClick = { onShowColumnPop() },
                modifier = Modifier
                    .padding(4.dp)
                    .size(22.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.table_edit),
                    contentDescription = "Column Visibility",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(0.dp),
                )
            }
        }
    }
}

@Composable
private fun TotalCount(
    viewModel: HomeViewModel,
) {
    val count by viewModel.itemsCount.collectAsState()

    Box (contentAlignment = Alignment.CenterEnd) {
        Text(
            text = "999",
            modifier = Modifier,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            maxLines = 1,
            color = Color.Transparent
        )
        Text(
            text = "$count",
            modifier = Modifier,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            maxLines = 1,
        )
    }
}

data object SearchClearedEvent
data object SearchPerformedEvent

@Composable
private fun CustomBlendSearch(
    value: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Blend Search",
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    onImeAction: () -> Unit = {},
) {
    var showCursor by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value(),
        onValueChange = onValueChange,
        modifier = modifier
            .background(LocalCustomColors.current.textField, RoundedCornerShape(100f))
            .height(30.dp)
            .onFocusChanged { focusState ->
                hasFocus = focusState.hasFocus
                showCursor = focusState.hasFocus
                if (!focusState.hasFocus) {
                    focusManager.clearFocus()
                }
            }
            .padding(horizontal = 8.dp),
        textStyle = LocalTextStyle.current.copy(
            color = LocalContentColor.current,
            fontSize = TextUnit.Unspecified,
            lineHeight = 16.sp
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onImeAction()
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        cursorBrush = if (showCursor) {
            SolidColor(MaterialTheme.colorScheme.primary)
        } else {
            SolidColor(Color.Transparent)
        },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                leadingIcon()
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value().isEmpty() && !hasFocus) {
                        Text(
                            text = placeholder,
                            style = LocalTextStyle.current.copy(
                                color = LocalContentColor.current.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
                trailingIcon()
            }
        }
    )
}


/** Body stuff **/
@Composable
private fun HomeBody(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
    showLoading: () -> Boolean,
    isTableView: () -> Boolean,
    coroutineScope: () -> CoroutineScope,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    isMenuShown: () -> Boolean,
    activeMenuId: () -> Int?,
    getPositionTrigger: () -> Unit,
    shouldScrollUp: () -> Unit,
    searchFocused: () -> Boolean,
    searchPerformed: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val columnState = rememberLazyListState()
    val scrollState by filterViewModel.homeScrollState.collectAsState()
    val sortedItems by viewModel.itemsListState.collectAsState()
    val itemsCount by viewModel.itemsCount.collectAsState()

    LaunchedEffect(columnState) {
        snapshotFlow { columnState.layoutInfo.visibleItemsInfo.isNotEmpty() }
            .distinctUntilChanged()
            .collect { viewModel.updateListRendered(it) }
    }

    Box {
        BodyContent(
            viewModel, isTableView, columnState, sortedItems, onDetailsClick, onEditClick,
            isMenuShown, activeMenuId, getPositionTrigger, searchFocused,
            searchPerformed, shouldScrollUp, modifier
        )

        if (showLoading()) { LoadingIndicator() }

        if (viewModel.showColumnMenu.value) {
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
            coroutineScope = coroutineScope(),
            onScrollToTop = { coroutineScope().launch { columnState.scrollToItem(0) } },
            onScrollToBottom = { coroutineScope().launch { columnState.scrollToItem(sortedItems.list.lastIndex) } },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
        )

        HomeScrollHandler(
            columnState, sortedItems, { itemsCount }, scrollState, filterViewModel,
            searchPerformed(), coroutineScope(),
        )
    }
}

@Composable
private fun BodyContent(
    viewModel: HomeViewModel,
    isTableView: () -> Boolean,
    columnState: LazyListState,
    sortedItems: ItemsList,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    isMenuShown: () -> Boolean,
    activeMenuId: () -> Int?,
    getPositionTrigger: () -> Unit,
    searchFocused: () -> Boolean,
    searchPerformed: () -> Boolean,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            val tableSorting by viewModel.tableSorting.collectAsState()
            val tableLayoutData by viewModel.tableLayoutData.collectAsState()
            val tableShadow by viewModel.tableShadow.collectAsState()

            TableViewMode(
                sortedItems = sortedItems,
                columnState = columnState,
                shadowAlpha = { tableShadow },
                tableLayoutData = tableLayoutData,
                sorting = tableSorting,
                updateSorting = viewModel::updateSorting,
                searchFocused = searchFocused,
                searchPerformed = searchPerformed,
                onDetailsClick = onDetailsClick,
                onEditClick = onEditClick,
                getPositionTrigger = getPositionTrigger,
                shouldScrollUp = shouldScrollUp,
                onShowMenu = viewModel::onShowMenu,
                onDismissMenu = viewModel::onDismissMenu,
                isMenuShown = isMenuShown,
                activeMenuId = activeMenuId,
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
            )
        } else {
            val listShadow by viewModel.listShadow.collectAsState()

            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                size = GlowSize(top =  listShadow)
            ) {
                ListViewMode(
                    sortedItems = sortedItems,
                    columnState = columnState,
                    searchFocused = searchFocused,
                    searchPerformed = searchPerformed,
                    onDetailsClick = onDetailsClick,
                    onEditClick = onEditClick,
                    getPositionTrigger = getPositionTrigger,
                    onShowMenu = viewModel::onShowMenu,
                    onDismissMenu = viewModel::onDismissMenu,
                    isMenuShown = isMenuShown,
                    activeMenuId = activeMenuId,
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HomeScrollHandler(
    columnState: LazyListState,
    sortedItems: ItemsList,
    itemsCount: () -> Int,
    scrollState: HomeScrollState,
    filterViewModel: FilterViewModel,
    searchPerformed: Boolean,
    coroutineScope: CoroutineScope,
) {
    val currentItemsList by rememberUpdatedState(sortedItems.list)
    val savedItemIndex = remember(sortedItems.list, scrollState.savedItemId) {
        sortedItems.list.indexOfFirst { it.itemId == scrollState.savedItemId }
    }

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


/** JumpTo Button **/
@Composable
private fun JumpToButton(
    columnState: LazyListState,
    itemCountPass: () -> Boolean,
    coroutineScope: CoroutineScope,
    onScrollToTop: () -> Unit,
    onScrollToBottom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val jumpToState = rememberJumpToState(columnState)

    AnimatedVisibility(
        visible = jumpToState.first.value && itemCountPass(),
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    jumpToState.second.value
                    if (jumpToState.second.value == ScrollDirection.DOWN) {
                        onScrollToBottom()
                    } else {
                        onScrollToTop()
                    }
                }
            },
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            containerColor = LocalCustomColors.current.whiteBlack.copy(alpha = 0.45f),
            contentColor = LocalCustomColors.current.whiteBlackInverted.copy(alpha = 0.45f),
            modifier = modifier
                .border(Dp.Hairline, LocalCustomColors.current.whiteBlackInverted.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = if (jumpToState.second.value == ScrollDirection.DOWN) R.drawable.double_down else R.drawable.double_up),
                contentDescription = if (jumpToState.second.value == ScrollDirection.DOWN) "Scroll to bottom" else "Scroll to top",
                modifier = Modifier
                    .size(36.dp),
            )
        }
    }
}

@Composable
fun rememberJumpToState(
    lazyListState: LazyListState,
): Pair<State<Boolean>, State<ScrollDirection>> {
    val scrollDirection = produceState(initialValue = ScrollDirection.UP, key1 = lazyListState) {
        var previousIndex = 0
        val updatePrevious: (Int) -> Unit = { previousIndex = it }
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex > previousIndex) {
                    value = ScrollDirection.DOWN
                } else if (currentIndex < previousIndex) {
                    value = ScrollDirection.UP
                }
                updatePrevious(currentIndex)
            }
    }

    val isVisible = produceState(initialValue = false, key1 = lazyListState, key2 = scrollDirection.value) {
        var delayJob: Job? = null
        val updateJob: (Job) -> Unit = { delayJob = it }

        snapshotFlow { Triple(
            lazyListState.isScrollInProgress,
            !lazyListState.canScrollBackward,
            !lazyListState.canScrollForward
        ) }.collect { (isScrolling, atTop, atBottom) ->
            delayJob?.cancel()

            val overScroll = (atTop && scrollDirection.value == ScrollDirection.UP) ||
                    (atBottom && scrollDirection.value == ScrollDirection.DOWN)

            if (isScrolling && !overScroll) {
                if (!value) {
                    delay(25)
                    value = true
                }
            } else {
                updateJob(
                    launch {
                        val delayMillis = if (atTop || atBottom) 0 else 1500L
                        delay(delayMillis)
                        value = false
                    }
                )
            }
        }
    }
    return Pair(isVisible, scrollDirection)
}


/** Item Menu **/
@Composable
private fun ItemMenu(
    onEditClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.listMenuScrim),
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = {
                onEditClick()
                onMenuDismiss()
            },
            modifier = Modifier,
        ) {
            Text(
                text = "Edit Item",
                modifier = Modifier,
                color = LocalContentColor.current,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}


/** List View Mode **/
@Composable
fun ListViewMode(
    sortedItems: ItemsList,
    columnState: LazyListState,
    searchPerformed: () -> Boolean,
    searchFocused: () -> Boolean,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    getPositionTrigger: () -> Unit,
    onShowMenu: (Int) -> Unit,
    isMenuShown: () -> Boolean,
    activeMenuId: () -> Int?,
    onDismissMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .background(LocalCustomColors.current.backgroundVariant)
                .padding(0.dp),
            state = columnState,
        ) {
            items(items = sortedItems.list, key = { it.itemId }) { item ->
                val haptics = LocalHapticFeedback.current
                val focusManager = LocalFocusManager.current

                BackHandler(isMenuShown() && activeMenuId() == item.itemId) { onDismissMenu() }

                ListItem(
                    brand = { item.item.items.brand },
                    blend = { item.item.items.blend },
                    favorite = { item.item.items.favorite },
                    disliked = { item.item.items.disliked },
                    notes = { item.item.items.notes },
                    typeGenreText = { item.formattedTypeGenre },
                    formattedQuantity = { item.formattedQuantity },
                    outOfStock = { item.outOfStock },
                    rating = { item.rating },
                    onEditClick = { onEditClick(item.itemId) },
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                if (searchFocused()) {
                                    focusManager.clearFocus()
                                } else {
                                    if (isMenuShown() && activeMenuId() == item.itemId) {
                                        // do nothing
                                    } else {
                                        if (isMenuShown()) {
                                            onDismissMenu()
                                        } else {
                                            if (!searchPerformed()) {
                                                getPositionTrigger()
                                            }
                                            onDetailsClick(item.itemId)
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!searchPerformed()) {
                                    getPositionTrigger()
                                }
                                onShowMenu(item.itemId)
                            },
                            indication = null,
                            interactionSource = null
                        ),
                    onMenuDismiss = onDismissMenu,
                    showMenu = { isMenuShown() && activeMenuId() == item.itemId },
                    filteredTins = item.tins,
                )
            }
        }
    }
}


@Composable
private fun ListItem(
    brand: () -> String,
    blend: () -> String,
    favorite: () -> Boolean,
    disliked: () -> Boolean,
    notes: () -> String,
    typeGenreText: () -> String,
    formattedQuantity: () -> String,
    outOfStock: () -> Boolean,
    rating: () -> String,
    filteredTins: TinsList,
    onMenuDismiss: () -> Unit,
    showMenu: () -> Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = modifier
                .padding(bottom = 1.dp)
        ) {
            // main details
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(start = 8.dp, top = 4.dp, bottom = 2.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Entry info
                    MainDetails(
                        brand = brand,
                        blend = blend,
                        favorite = favorite,
                        disliked = disliked,
                        notes = notes,
                        rating = rating,
                        typeGenreText = typeGenreText,
                        modifier = Modifier
                            .weight(1f, false)
                    )

                    // Quantity
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .padding(0.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        QuantityColumn(
                            formattedQuantity = formattedQuantity,
                            outOfStock = outOfStock,
                            modifier = Modifier
                        )
                    }
                }

                // Tins
                if (filteredTins.tins.isNotEmpty()) {
                    GlowBox(
                        color = GlowColor(Color.Black.copy(alpha = .5f)),
                        size = GlowSize(top = 3.dp),
                        modifier = Modifier
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            TinList(
                                filteredTins = filteredTins,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }

            if (showMenu()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(0.dp)
                ) {
                    ItemMenu(
                        onMenuDismiss = onMenuDismiss,
                        onEditClick = onEditClick,
                        modifier = Modifier
                            .height(54.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainDetails(
    blend: () -> String,
    brand: () -> String,
    favorite: () -> Boolean,
    disliked: () -> Boolean,
    notes: () -> String,
    rating: () -> String,
    typeGenreText: () -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(fraction = .95f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = blend(),
                modifier = Modifier
                    .weight(1f, false)
                    .padding(end = 4.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textDecoration = TextDecoration.None
                ),
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconRow(favorite, disliked, notes)
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .offset(y = (-4).dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = brand(),
                modifier = Modifier,
                fontStyle = Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textDecoration = TextDecoration.None
                )
            )
            if (typeGenreText().isNotEmpty()){
                Text (
                    text = typeGenreText(),
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textDecoration = TextDecoration.None
                    ),
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
            if (rating().isNotEmpty()) {
                RatingLabel(rating)
            }
        }
    }
}

@Composable
private fun QuantityColumn(
    formattedQuantity: () -> String,
    outOfStock: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = formattedQuantity(),
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium.copy(
            color = if (outOfStock()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
            textDecoration = TextDecoration.None
        ),
        fontWeight = FontWeight.Normal,
        maxLines = 1,
        fontSize = 16.sp
    )
}

@Composable
private fun IconRow(
    favorite: () -> Boolean,
    disliked: () -> Boolean,
    notes: () -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (favorite()) {
            Icon(
                painter = painterResource(id = R.drawable.heart_filled_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(17.dp),
                tint = LocalCustomColors.current.favHeart
            )
        }
        if (disliked()) {
            Icon(
                painter = painterResource(id = R.drawable.heartbroken_filled_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(17.dp),
                tint = LocalCustomColors.current.disHeart
            )
        }
        if (notes().isNotBlank()) {
            Icon(
                painter = painterResource(id = R.drawable.notes_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun RatingLabel(
    rating: () -> String,
    modifier: Modifier = Modifier
) {
    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rating(),
            modifier = Modifier,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
        )
        Image(
            painter = painterResource(id = R.drawable.star_filled),
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalCustomColors.current.starRating),
            alignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 2.dp)
                .size(12.dp),
        )
    }
}

@Composable
private fun TinList(
    filteredTins: TinsList,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                LocalCustomColors.current.sheetBox,
                RoundedCornerShape(bottomStart = 8.dp)
            )
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
            .fillMaxWidth()
    ) {
        filteredTins.tins.forEach {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = it.tinLabel,
                    modifier = Modifier,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                if (it.container.isNotEmpty() || it.unit.isNotEmpty()) {
                    val tinInfo = remember(it.container, it.tinQuantity, it.unit) {
                        buildString {
                            append(" (")
                            if (it.container.isNotEmpty()) {
                                append(it.container)
                            }
                            if (it.container.isNotEmpty() && it.unit.isNotEmpty()) {
                                append(" - ")
                            }
                            if (it.unit.isNotEmpty()) {
                                val quantity = formatDecimal(it.tinQuantity)
                                val unit = if (it.unit == "grams") "g" else it.unit
                                append("$quantity $unit")
                            }
                            append(")")
                        }
                    }

                    Text(
                        text = tinInfo,
                        modifier = Modifier,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
                if (it.finished) {
                    Text(
                        text = " (Finished)",
                        modifier = Modifier,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        fontStyle = Italic,
                        color = LocalContentColor.current.copy(alpha = .5f)
                    )
                }
            }
        }
    }

}


/** Table View Mode **/
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

@Composable
fun ColumnVisibilityPopup(
    viewModel: HomeViewModel,
    onVisibilityChange: (TableColumn, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = TableColumn.entries.filter { it != TableColumn.BRAND && it != TableColumn.BLEND }
    val visibilityMap by viewModel.tableColumnVisibility.collectAsState()
    val columnVisibilityEnablement by viewModel.columnVisibilityEnablement.collectAsState()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        text = {
            LazyColumn (
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(columns) { column ->
                    Row (
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        CheckboxWithLabel(
                            text = column.title,
                            checked = visibilityMap[column] ?: true,
                            onCheckedChange = {
                                val visible = visibilityMap[column] ?: true
                                onVisibilityChange(column, !visible)
                            },
                            enabled = columnVisibilityEnablement[column] ?: true,
                            modifier = Modifier
                        )
                    }
                }
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        confirmButton = {
            TextButton(onClick = { onDismiss() }
            ) {
                Text(
                    text = "Done"
                )
            }
        }
    )
}

@Composable
fun TableViewMode(
    sortedItems: ItemsList,
    columnState: LazyListState,
    shadowAlpha: () -> Float,
    tableLayoutData: TableLayoutData,
    sorting: TableSorting,
    searchPerformed: () -> Boolean,
    searchFocused: () -> Boolean,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    getPositionTrigger: () -> Unit,
    activeMenuId: () -> Int?,
    onShowMenu: (Int) -> Unit,
    isMenuShown: () -> Boolean,
    onDismissMenu: () -> Unit,
    updateSorting: (Int) -> Unit,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Items
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll, overscrollEffect = null),
            state = columnState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            stickyHeader {
                TableHeaderRow(
                    tableLayoutData,
                    updateSorting,
                    sorting,
                    shouldScrollUp,
                    isMenuShown,
                    activeMenuId,
                    onDismissMenu,
                    searchFocused,
                    Modifier
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

                BackHandler(isMenuShown() && activeMenuId() == item.itemId) { onDismissMenu() }

                val haptics = LocalHapticFeedback.current
                val focusManager = LocalFocusManager.current

                TableItem(
                    item = item,
                    layoutData = tableLayoutData,
                    horizontalScroll = horizontalScroll,
                    showMenu = { isMenuShown() && activeMenuId() == item.itemId },
                    onEditClick = { onEditClick(item.itemId) },
                    onDismissMenu = onDismissMenu,
                    modifier
                        .fillMaxWidth()
                        .height(intrinsicSize = IntrinsicSize.Min)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .combinedClickable(
                            onClick = {
                                if (searchFocused()) {
                                    focusManager.clearFocus()
                                } else {
                                    if (isMenuShown() && activeMenuId() == item.itemId) {
                                        // do nothing
                                    } else {
                                        if (isMenuShown()) {
                                            onDismissMenu()
                                        } else {
                                            if (!searchPerformed()) {
                                                getPositionTrigger()
                                            }
                                            onDetailsClick(item.itemId)
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                                if (!searchPerformed()) {
                                    getPositionTrigger()
                                }
                                onShowMenu(item.itemId)
                            },
                            indication = null,
                            interactionSource = null
                        )
                )

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
fun TableHeaderRow(
    layoutData: TableLayoutData,
    updateSorting: (Int) -> Unit,
    sorting: TableSorting,
    shouldScrollUp: () -> Unit,
    isMenuShown: () -> Boolean,
    activeMenuId: () -> Int?,
    onDismissMenu: () -> Unit,
    searchFocused: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(intrinsicSize = IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        for (columnIndex in layoutData.columnMinWidths.values.indices) {
            val focusManager = LocalFocusManager.current

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
                                    enabled = layoutData.columnMinWidths.values[columnIndex] > 0.dp,
                                    onClick = {
                                        if (searchFocused()) {
                                            focusManager.clearFocus()
                                        } else {
                                            if (isMenuShown() && activeMenuId() != null) {
                                                onDismissMenu()
                                            } else {
                                                updateSorting(columnIndex)
                                                shouldScrollUp()
                                            }
                                        }
                                    },
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
                                    enabled = layoutData.columnMinWidths.values[4] > 0.dp,
                                    onClick = {
                                        if (searchFocused()) {
                                            focusManager.clearFocus()
                                        } else {
                                            if (isMenuShown() && activeMenuId() != null) {
                                                onDismissMenu()
                                            } else {
                                                updateSorting(columnIndex)
                                                shouldScrollUp()
                                            }
                                        }
                                    },
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
fun TableItem(
    item: ItemsListState,
    layoutData: TableLayoutData,
    horizontalScroll: ScrollState,
    showMenu: () -> Boolean,
    onEditClick: () -> Unit,
    onDismissMenu: () -> Unit,
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
        if (showMenu()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(0.dp)
            ) {
                val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp }
            //    val currentScrollOffset by remember { derivedStateOf { horizontalScroll.value } }
                val switch = remember(screenWidth, layoutData.totalWidth) { screenWidth.dp >= layoutData.totalWidth }
            //    val width = remember(switch) { if (switch) layoutData.totalWidth.value.dp else screenWidth.dp }

                Box (
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(if (switch) layoutData.totalWidth.value.dp else screenWidth.dp)
                        .offset { IntOffset(if (switch) 0 else horizontalScroll.value, 0) },
                    contentAlignment = Alignment.Center
                ) {
                    ItemMenu(
                        onMenuDismiss = onDismissMenu,
                        onEditClick = onEditClick,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun TableTinsList(
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


@Composable
fun ImportantAlertDialog(
    importantAlertState: ImportantAlertState,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val alert = importantAlertState.alertToDisplay!!
    val isCurrent = importantAlertState.isCurrentAlert

    val scrollState = rememberSaveable(alert.id, saver = ScrollState.Saver) { ScrollState(0) } // rememberScrollState()
    var enabled by rememberSaveable(alert.id) { mutableStateOf(false) }
    val atBottom by remember(alert.id) { derivedStateOf { !scrollState.canScrollForward } }
    var countdown by remember(alert.id) { mutableIntStateOf(5) }
    var canScroll by remember(alert.id) { mutableStateOf(false) }
    val updateScroll: (Boolean) -> Unit = { canScroll = it }

    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        modifier = modifier,
        containerColor = LocalCustomColors.current.darkNeutral,
        title = {
            Text(
                text = "Important Alert!",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
            )
        },
        text = {
            Column {
                Text(
                    text = "This is a one-time alert.",
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                GlowBox(
                    color = GlowColor(LocalCustomColors.current.darkNeutral),
                    size = GlowSize(vertical = 6.dp),
                    modifier = Modifier
                        .height(175.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState),
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                        ) {
                            if (!isCurrent) {
                                Text(
                                    text = "Missed Alert:",
                                    modifier = Modifier,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "${alert.date}\n(v ${alert.appVersion})",
                                modifier = Modifier
                                    .padding(bottom = 16.dp),
                                fontSize = 14.sp
                            )
                            if (canScroll) {
                                Text(
                                    text = "You must scroll to the bottom to be able to acknowledge and " +
                                            "dismiss this dialog.",
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                )
                            }
                            alert.message(this)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            val isScrollable by remember(alert.id) { derivedStateOf { scrollState.maxValue > 0 } }
            LaunchedEffect(isScrollable, alert.id) {
                if (!isScrollable) {
                    while (countdown > 0) {
                        delay(1000)
                        countdown--
                    }
                    enabled = true
                } else {
                    updateScroll(true)
                }
            }
            LaunchedEffect(isScrollable, atBottom, alert.id) {
                if (isScrollable && atBottom) {
                    enabled = true
                }
            }
            val buttonText = remember(isScrollable, enabled, countdown, isCurrent) {
                if (!isCurrent) {
                    if (isScrollable) {
                        "Next"
                    } else {
                        if (enabled) "Next" else "( $countdown )"
                    }
                } else {
                    if (isScrollable) {
                        "Confirm"
                    } else {
                        if (enabled) "Confirm" else "( $countdown )"
                    }
                }
            }

            Button(
                onClick = {
                    if (!isCurrent) {
                        enabled = false
                        updateScroll(false)
                        countdown = 5
                        viewModel.saveAlertSeen(alert.id)
                    } else {
                        viewModel.saveAlertSeen(alert.id)
                    }
                },
                enabled = enabled
            ) {
                Box (contentAlignment = Alignment.Center) {
                    Text(
                        text = "Confirm",
                        color = Color.Transparent
                    )
                    Text(
                        text = buttonText
                    )
                }
            }
        }
    )
}