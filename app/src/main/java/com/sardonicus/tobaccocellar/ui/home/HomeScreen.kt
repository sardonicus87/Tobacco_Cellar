package com.sardonicus.tobaccocellar.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarBottomAppBar
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.FullScreenLoading
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToStats: () -> Unit,
    navigateToAddEntry: () -> Unit,
    navigateToBlendDetails: (Int) -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    navigateToBulkEdit: () -> Unit,
    navigateToCsvImport: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToHelp: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val filterViewModel = LocalCellarApplication.current.filterViewModel
//    val scrollBehavior = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
//        TopAppBarDefaults.enterAlwaysScrollBehavior() else TopAppBarDefaults.pinnedScrollBehavior()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
//    val bottomScrollBehavior = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
//        BottomAppBarDefaults.exitAlwaysScrollBehavior() else null
    val homeUiState by viewmodel.homeUiState.collectAsState()
    val blendSearchText by filterViewModel.blendSearchText.collectAsState()
    val sorting by viewmodel.sorting
    val showSnackbar = viewmodel.showSnackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isTableView = homeUiState.isTableView
    val activeMenuId by viewmodel.activeMenuId
    val isMenuShown by viewmodel.isMenuShown
    val focusManager = LocalFocusManager.current
    val currentPosition by filterViewModel.currentPosition.collectAsState()
    val blendSearchFocused by filterViewModel.blendSearchFocused.collectAsState()
    val searchPerformed by filterViewModel.searchPerformed.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (showSnackbar.value) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "CSV Exported",
                duration = SnackbarDuration.Short
            )
            viewmodel.snackbarShown()
        }
    }

    BackHandler(enabled = searchPerformed) {
        filterViewModel.updateSearchText("")
        filterViewModel.onBlendSearch("")
        filterViewModel.updateSearchIconOpacity(0.5f)

        if (searchPerformed) {
            coroutineScope.launch {
                EventBus.emit(SearchClearedEvent)
            }
        }
    }

    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }


    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .noRippleClickable(onClick = { focusManager.clearFocus() }),
        topBar = {
            CellarTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = false,
                navigateToBulkEdit = navigateToBulkEdit,
                navigateToCsvImport = navigateToCsvImport,
                navigateToSettings = navigateToSettings,
                navigateToHelp = navigateToHelp,
                showMenu = true,
                currentDestination = HomeDestination,
                exportCsvHandler = viewmodel,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier,
                navigateToStats = navigateToStats,
                navigateToAddEntry = navigateToAddEntry,
                currentDestination = HomeDestination,
                filterViewModel = filterViewModel,
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
                            .padding(bottom = 20.dp)
                    )
                }
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeHeader(
                modifier = Modifier,
                homeUiState = homeUiState,
                filterViewModel = filterViewModel,
                blendSearchText = blendSearchText,
                selectView = viewmodel::selectView,
                isTableView = isTableView,
            )
            Box {
                HomeBody(
                    isLoading = homeUiState.isLoading,
                    isTableView = isTableView,
                    items = homeUiState.items,
                    formattedQuantity = homeUiState.formattedQuantities,
                    updateScrollPosition = filterViewModel::updateScrollPosition,
                    currentPosition = currentPosition,
                    filterViewModel = filterViewModel,
                    onDetailsClick = navigateToBlendDetails,
                    onEditClick = navigateToEditEntry,
                    activeMenuId = activeMenuId,
                    onShowMenu = viewmodel::onShowMenu,
                    onDismissMenu = viewmodel::onDismissMenu,
                    isMenuShown = isMenuShown,
                    blendSearchFocused = blendSearchFocused,
                    sorting = sorting,
                    updateSorting = viewmodel::updateSorting,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                )
                Box( // header shadow
                    modifier = Modifier
                        .matchParentSize()
                        .then(
                            Modifier.drawBehind {
                                val glowHeight = 3.dp
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                        ),
                                        startY = 0f,
                                        endY = glowHeight.toPx(),
                                    ),
                                    topLeft = Offset(0f, 0f),
                                    size = Size(size.width, glowHeight.toPx())
                                )
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    filterViewModel: FilterViewModel,
    blendSearchText: String,
    selectView: (Boolean) -> Unit,
    isTableView: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val searchPerformed by filterViewModel.searchPerformed.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(LocalCustomColors.current.homeHeaderBg)
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .padding(0.dp)
                .widthIn(min = 84.dp),
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
                onClick = { selectView(!isTableView) },
                modifier = Modifier
                    .padding(4.dp)
                    .size(22.dp)
            ) {
                Icon(
                    painter = painterResource(homeUiState.toggleIcon),
                    contentDescription = stringResource(homeUiState.toggleContentDescription),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(0.dp)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .width(8.dp)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .weight(1f, false)
        ) {
            CustomBlendSearch(
                value = blendSearchText,
                onValueChange = {
                    filterViewModel.updateSearchText(it)
                    if (it.isEmpty()) {
                        filterViewModel.onBlendSearch(it)
                        filterViewModel.updateSearchIconOpacity(0.5f)
                    }
                    if (it.isNotEmpty()) {
                        filterViewModel.updateSearchIconOpacity(1f)
                    }
                },
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) filterViewModel.updateSearchFocused(true)
                        else filterViewModel.updateSearchFocused(false)
                    },
                onImeAction = {
                    coroutineScope.launch {
                        EventBus.emit(SearchPerformedEvent)
                        filterViewModel.getPositionTrigger()
                        delay(20)
                        filterViewModel.onBlendSearch(blendSearchText)
                    }
                },
                trailingIcon = {
                    if (blendSearchText.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.clear_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    onClick = {
                                        filterViewModel.updateSearchText("")
                                        filterViewModel.onBlendSearch("")
                                        filterViewModel.updateSearchIconOpacity(0.5f)

                                        if (searchPerformed) {
                                            coroutineScope.launch {
                                                EventBus.emit(SearchClearedEvent)
                                            }
                                        }
                                    }
                                )
                                .padding(0.dp),
                        )
                    }
                }
            )
        }
        Spacer(
            modifier = Modifier
                .width(12.dp)
        )
        Text(
            text = "Entries: ${homeUiState.items.size}",
            modifier = Modifier
                .widthIn(min = 84.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            maxLines = 1,
        )
    }
}

data object SearchClearedEvent
data object SearchPerformedEvent

@Composable
private fun CustomBlendSearch(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Blend Search",
    leadingIcon: @Composable () -> Unit = {
        val filterViewModel = LocalCellarApplication.current.filterViewModel
        val iconAlpha = filterViewModel.searchIconOpacity.collectAsState().value
        Icon(
            painter = painterResource(id = R.drawable.search),
            contentDescription = null,
            modifier = Modifier
                .padding(0.dp)
                .size(20.dp),
            tint = LocalContentColor.current.copy(alpha = iconAlpha)
        )
    },
    trailingIcon: @Composable () -> Unit = {},
    onImeAction: () -> Unit = {}
) {
    var showCursor by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    BasicTextField(
        value = value,
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
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && !hasFocus) {
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
//                if (value.isNotEmpty()) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.clear_24),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(20.dp)
//                            .clickable(onClick = {
//                                onValueChange("")
//                                }
//                            )
//                            .padding(0.dp),
//                    )
//                }
            }
        }
    )
}


@Composable
private fun HomeBody(
    isLoading: Boolean,
    isTableView: Boolean,
    items: List<ItemsComponentsAndTins>,
    formattedQuantity: Map<Int, String>,
    updateScrollPosition: (Int, Int) -> Unit,
    currentPosition: Map<Int, Int>,
    filterViewModel: FilterViewModel,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    activeMenuId: Int?,
    onShowMenu: (Int) -> Unit,
    onDismissMenu: () -> Unit,
    isMenuShown: Boolean,
    blendSearchFocused: Boolean,
    sorting: Sorting,
    updateSorting: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteToDisplay by remember { mutableStateOf("") }

    if (showNoteDialog) {
        NoteDialog(
            note = noteToDisplay,
            onDismiss = { showNoteDialog = false },
            modifier = Modifier
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        if (isLoading) {
            FullScreenLoading()
        } else {
            if (items.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.no_items),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(0.dp),
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(1.25f)
                    )
                }
            } else {
                if (isTableView) {
                    TableViewMode(
                        itemsList = items,
                        formattedQuantity = formattedQuantity,
                        updateScrollPosition = updateScrollPosition,
                        currentPosition = currentPosition,
                        filterViewModel = filterViewModel,
                        blendSearchFocused = blendSearchFocused,
                        onDetailsClick = { onDetailsClick(it.id) },
                        onEditClick = { onEditClick(it.id) },
                        onNoteClick = { item -> noteToDisplay = item.notes
                                      showNoteDialog = true },
                        sorting = sorting,
                        updateSorting = updateSorting,
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                    )
                } else {
                    ListViewMode(
                        itemsList = items,
                        formattedQuantity = formattedQuantity,
                        updateScrollPosition = updateScrollPosition,
                        currentPosition = currentPosition,
                        filterViewModel = filterViewModel,
                        blendSearchFocused = blendSearchFocused,
                        onDetailsClick = { onDetailsClick(it.id) },
                        onEditClick = { onEditClick(it.id) },
                        onNoteClick = { item -> noteToDisplay = item.notes
                            showNoteDialog = true },
                        activeMenuId = activeMenuId,
                        onShowMenu = onShowMenu,
                        onDismissMenu = onDismissMenu,
                        isMenuShown = isMenuShown,
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    note: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .padding(0.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .padding(0.dp)
                .wrapContentWidth()
                .wrapContentHeight()
                .border(
                    Dp.Hairline,
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    MaterialTheme.shapes.large
                )
            ,
            shape = MaterialTheme.shapes.large,
        //    elevation = CardDefaults.cardElevation(5.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
        //        contentColor = MaterialTheme.colorScheme.onBackground,
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, top = 0.dp, bottom = 16.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = "Notes",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(0.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LocalContentColor.current.copy(alpha = 0.66f),
                                modifier = Modifier
                                    .padding(0.dp)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = note,
                        modifier = Modifier
                            .padding(0.dp),
                        textAlign = TextAlign.Justify,
                        softWrap = true,
                    )
                }
            }
        }
    }
}


/** List View Mode **/
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListViewMode(
    itemsList: List<ItemsComponentsAndTins>,
    formattedQuantity: Map<Int, String>,
    updateScrollPosition: (Int, Int) -> Unit,
    currentPosition: Map<Int, Int>,
    filterViewModel: FilterViewModel,
    blendSearchFocused: Boolean,
    onDetailsClick: (Items) -> Unit,
    onEditClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
    activeMenuId: Int?,
    onShowMenu: (Int) -> Unit,
    isMenuShown: Boolean,
    onDismissMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val columnState = rememberLazyListState()

    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(0.dp),
            state = columnState,
        ) {
            items(items = itemsList, key = { it.items.id }) { item ->
                val haptics = LocalHapticFeedback.current
                val focusManager = LocalFocusManager.current

                BackHandler(enabled = isMenuShown && activeMenuId == item.items.id) {
                    onDismissMenu()
                }

                CellarListItem(
                    item = item,
                    formattedQuantity = formattedQuantity[item.items.id] ?: "--",
                    filterViewModel = filterViewModel,
                    onDetailsClick = { onDetailsClick(item.items) },
                    onEditClick = { onEditClick(item.items) },
                    onNoteClick = { onNoteClick(item.items) },
                    modifier = Modifier
                        .padding(0.dp)
                        .combinedClickable(
                            onClick = {
                                if (blendSearchFocused) {
                                    focusManager.clearFocus()
                                } else {
                                    if (isMenuShown && activeMenuId == item.items.id) {
                                        // do nothing
                                    } else {
                                        if (isMenuShown) {
                                            onDismissMenu()
                                            focusManager.clearFocus()
                                        } else {
                                            filterViewModel.getPositionTrigger()
                                            onDetailsClick(item.items)
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                filterViewModel.getPositionTrigger()
                                onShowMenu(item.items.id)
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    onMenuDismiss = {
                        onDismissMenu()
                    },
                    showMenu = isMenuShown && activeMenuId == item.items.id,
                )
            }
        }


        val shouldScrollUp by filterViewModel.shouldScrollUp.collectAsState()
        val savedItemId by filterViewModel.savedItemId.collectAsState()
        val savedItemIndex = itemsList.indexOfFirst { it.items.id == savedItemId }
        val shouldReturn by filterViewModel.shouldReturn.collectAsState()
        val getPosition by filterViewModel.getPosition.collectAsState()
        val searchCleared by filterViewModel.searchCleared.collectAsState()
        val searchPerformed by filterViewModel.searchPerformed.collectAsState()

        // Return Positions //
        LaunchedEffect(savedItemIndex) {
            if (savedItemIndex != -1) {
                delay(50)
                withFrameNanos {
                    coroutineScope.launch {
                        if (savedItemIndex > 0 && savedItemIndex < (itemsList.size - 1)) {
                            val offset = (columnState.layoutInfo.visibleItemsInfo[1].size / 2) * -1
                            columnState.scrollToItem(savedItemIndex, offset)
                        } else {
                            columnState.scrollToItem(savedItemIndex)
                        }
                    }
                }
                filterViewModel.resetScroll()
            }
        }

        LaunchedEffect(itemsList) {
            delay(20)
            if (shouldScrollUp) {
                columnState.scrollToItem(0)
                filterViewModel.resetScroll()
            }
            if (shouldReturn && !searchPerformed && !shouldScrollUp) {
                val index = currentPosition[0]
                val offset = currentPosition[1]

                if (index != null && offset != null) {
                    withFrameNanos {
                        coroutineScope.launch {
                            columnState.scrollToItem(index, offset)
                        }
                    }
                    filterViewModel.resetScroll()
                }
            }
            if (searchCleared) {
                val index = currentPosition[0]
                val offset = currentPosition[1]

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

        // Update scroll position //
        LaunchedEffect(getPosition) {
            if (getPosition > 0) {
                val layoutInfo = columnState.layoutInfo
                val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()

                if (firstVisibleItem != null) {
                    updateScrollPosition(firstVisibleItem.index, firstVisibleItem.offset * -1)
                }
            }
        }
    }
}


@Composable
private fun CellarListItem(
    modifier: Modifier = Modifier,
    item: ItemsComponentsAndTins,
    formattedQuantity: String,
    filterViewModel: FilterViewModel,
    onMenuDismiss: () -> Unit,
    showMenu: Boolean,
    onDetailsClick: (Items) -> Unit,
    onEditClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
) {
    Box(
        modifier = modifier
            .padding(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(start = 8.dp, top = 4.dp, bottom = 2.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Entry info
            Column(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f, false),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // Entry Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth(fraction = .95f),
                ) {
                    // blend name and fav/dis/notes icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.items.blend,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .weight(1f, false),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textDecoration = TextDecoration.None
                            ),
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true,
                            maxLines = 1,
                        )
                        if (item.items.favorite) {
                            Icon(
                                painter = painterResource(id = R.drawable.heart_filled_24),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(17.dp),
                                tint = LocalCustomColors.current.favHeart
                            )
                        }
                        if (item.items.disliked) {
                            Icon(
                                painter = painterResource(id = R.drawable.heartbroken_filled_24),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(17.dp),
                                tint = LocalCustomColors.current.disHeart
                            )
                        }
                        if (item.items.notes.isNotEmpty()) {
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
                    // brand and type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 8.dp)
                            .offset(y = (-4).dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Brand Name //
                        Text(
                            text = item.items.brand,
                            modifier = Modifier,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textDecoration = TextDecoration.None
                            ),
                            fontStyle = Italic,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        // Other Info //
                        Text(
                            text = item.items.type,
                            modifier = Modifier,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textDecoration = TextDecoration.None
                            ),
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
            // Quantity
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(0.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                val outOfStock = formattedQuantity == "0 oz" ||
                formattedQuantity == "x0" ||
                formattedQuantity == "0 g"

                Text(
                    text = formattedQuantity,
                    modifier = Modifier,
                    style =
                        if (outOfStock) (MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.error,
                            textDecoration = TextDecoration.None))
                        else (MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textDecoration = TextDecoration.None)),
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }

        if (showMenu) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(0.dp)
                    .background(LocalCustomColors.current.listMenuScrim.copy(alpha = 0.75f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            filterViewModel.getPositionTrigger()
                            onEditClick(item.items)
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
//                    TextButton(
//                        onClick = {
//                            onDetailsClick(item.items)
//                            onMenuDismiss()
//                        },
//                        modifier = Modifier,
//                    ) {
//                        Text(
//                            text = "View Details",
//                            modifier = Modifier,
//                            color = LocalContentColor.current,
//                            fontWeight = FontWeight.SemiBold,
//                            fontSize = 14.sp
//                        )
//                    }
//                    if (item.items.notes.isNotEmpty()) {
//                        TextButton(
//                            onClick = {
//                                onNoteClick(item.items)
//                                onMenuDismiss()
//                            }
//                        ) {
//                            Text(
//                                text = "View Notes",
//                                modifier = Modifier,
//                                color = LocalContentColor.current,
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 14.sp
//                            )
//                        }
//                    }
                }
            }
        }
    }
    Spacer(
        modifier = Modifier
            .height(1.dp)
            .background(LocalCustomColors.current.backgroundVariant)
    )
}



/** Table View Mode **/
@Composable
fun TableViewMode(
    itemsList: List<ItemsComponentsAndTins>,
    formattedQuantity: Map<Int, String>,
    filterViewModel: FilterViewModel,
    blendSearchFocused: Boolean,
    onDetailsClick: (Items) -> Unit,
    onEditClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
    sorting: Sorting,
    updateSorting: (Int) -> Unit,
    updateScrollPosition: (Int, Int) -> Unit,
    currentPosition: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val columnMinWidths = listOf(
        180.dp, // Brand
        300.dp, // Blend
        108.dp, // Type
        64.dp, // Fav/Dis
        64.dp, // Note
        98.dp // Tins
    )

    TableLayout(
        items = itemsList,
        formattedQuantity = formattedQuantity,
        updateScrollPosition = updateScrollPosition,
        currentPosition = currentPosition,
        filterViewModel = filterViewModel,
        blendSearchFocused = blendSearchFocused,
        columnMinWidths = columnMinWidths,
        onDetailsClick = onDetailsClick,
        onEditClick = onEditClick,
        onNoteClick = onNoteClick,
        sorting = sorting,
        updateSorting = updateSorting,
        modifier = modifier
            .fillMaxWidth()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableLayout(
    items: List<ItemsComponentsAndTins>,
    formattedQuantity: Map<Int, String>,
    updateScrollPosition: (Int, Int) -> Unit,
    currentPosition: Map<Int, Int>,
    filterViewModel: FilterViewModel,
    blendSearchFocused: Boolean,
    columnMinWidths: List<Dp>,
    onDetailsClick: (Items) -> Unit,
    onEditClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
    sorting: Sorting,
    updateSorting: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnMapping = listOf(
        { item: Items -> item.brand }, // 0
        { item: Items -> item.blend }, // 1
        { item: Items -> item.type }, // 2
        { item: Items -> // 3
            when {
                item.favorite -> 1
                item.disliked -> 2
                else -> 0
            }
        },
        { item: Items -> item.notes }, // 4
        { item: Items -> item.id }, // 5
    )
    val sortedItems = when (sorting.columnIndex) {
        0 -> items.sortedBy { it.items.brand }
        1 -> items.sortedBy { it.items.blend }
        else -> items
    }.let {
        if (!sorting.sortAscending) it.reversed() else it
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Min)
        ) {
            for (columnIndex in columnMinWidths.indices) {
                Box(
                    modifier = Modifier
                        .width(columnMinWidths[columnIndex])
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .padding(0.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(Dp.Hairline, color = LocalCustomColors.current.tableBorder)
                ) {
                    val alignment = when (columnIndex) {
                        0 -> Alignment.CenterStart // brand
                        1 -> Alignment.CenterStart // blend
                        2 -> Alignment.Center // type
                        3 -> Alignment.Center // fav/dis
                        4 -> Alignment.Center // notes
                        5 -> Alignment.Center // quantity
                        else -> Alignment.CenterStart
                    }
                    val headerText = when (columnIndex) {
                        0 -> "Brand"
                        1 -> "Blend"
                        2 -> "Type"
                        3 -> ""
                        4 -> "Note"
                        5 -> "Qty"
                        else -> ""
                    }
                    val onSortChange: (Int) -> Unit = { newSortColumn: Int ->
                        updateSorting(newSortColumn)
                    }
                    when (columnIndex) {
                        0, 1 -> {
                            HeaderCell(
                                text = headerText,
                                onClick = { onSortChange(columnIndex) },
                                primarySort = sorting.columnIndex == columnIndex,
                                sorting = sorting,
                                modifier = Modifier
                                    .padding(0.dp)
                                    .matchParentSize()
                                    .align(alignment),
                                contentAlignment = alignment
                            )
                        }
                        else -> {
                            HeaderCell(
                                text = headerText,
                                primarySort = sorting.columnIndex == columnIndex,
                                sorting = sorting,
                                modifier = Modifier
                                    .padding(0.dp)
                                    .align(alignment),
                                icon1 = if (columnIndex == 3)
                                    painterResource(id = R.drawable.heart_filled_24) else null,
                                icon2 = if (columnIndex == 3)
                                    painterResource(id = R.drawable.question_mark_24) else null,
                                contentAlignment = alignment
                            )
                        }
                    }
                }
            }
        }

        // Items
        val columnState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val haptics = LocalHapticFeedback.current
        val focusManager = LocalFocusManager.current

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            state = columnState
        ) {
            items(items = sortedItems, key = { it.items.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(intrinsicSize = IntrinsicSize.Min)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    for (columnIndex in columnMinWidths.indices) {
                        Box(
                            modifier = Modifier
                                .width(columnMinWidths[columnIndex])
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                                .border(Dp.Hairline, color = LocalCustomColors.current.tableBorder)
                        ) {
                            val cellValue = columnMapping[columnIndex](item.items)
                            val alignment = when (columnIndex) {
                                0 -> Alignment.CenterStart // brand
                                1 -> Alignment.CenterStart // blend
                                2 -> Alignment.Center // type
                                3 -> Alignment.Center // fav/dis
                                4 -> Alignment.Center // notes
                                5 -> Alignment.Center // quantity
                                else -> Alignment.CenterStart
                            }
                            when (columnIndex) {
                                0 -> { // brand
                                    TableCell(
                                        value = cellValue,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .align(alignment),
                                        contentAlignment = alignment,
                                        onClick = {
                                            if (blendSearchFocused) {
                                                focusManager.clearFocus()
                                            } else {
                                                filterViewModel.getPositionTrigger()
                                                onDetailsClick(item.items)
                                            }
                                        },
                                        onLongClick = {
                                            if (blendSearchFocused) {
                                                focusManager.clearFocus()
                                            } else {
                                                filterViewModel.getPositionTrigger()
                                                onEditClick(item.items)
                                            }
                                        }
                                    )
                                } // brand
                                1 -> { // blend
                                    TableCell(
                                        value = cellValue,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .align(alignment),
                                        contentAlignment = alignment,
                                        onClick = {
                                            if (blendSearchFocused) {
                                                focusManager.clearFocus()
                                            } else {
                                                focusManager.clearFocus()
                                                filterViewModel.getPositionTrigger()
                                                onDetailsClick(item.items)
                                            }
                                        },
                                        onLongClick = {
                                            if (blendSearchFocused) {
                                                focusManager.clearFocus()
                                            } else {
                                                focusManager.clearFocus()
                                                filterViewModel.getPositionTrigger()
                                                onEditClick(item.items)
                                            }
                                        }
                                    )
                                } // blend
                                3 -> { // fav/disliked
                                    val favDisValue = cellValue as Int
                                    val icon = when (favDisValue) {
                                        1 -> painterResource(id = R.drawable.heart_filled_24)
                                        2 -> painterResource(id = R.drawable.heartbroken_filled_24)
                                        else -> null
                                    }
                                    if (icon != null) {
                                        val tintColor = when (favDisValue) {
                                            1 -> LocalCustomColors.current.favHeart
                                            2 -> LocalCustomColors.current.disHeart
                                            else -> Color.Unspecified
                                        }
                                        Image(
                                            painter = icon,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(alignment),
                                            colorFilter = ColorFilter.tint(tintColor)
                                        )
                                    } else {
                                        TableCell(
                                            value = "",
                                            contentAlignment = alignment
                                        )
                                    }
                                } // fav/disliked
                                4 -> { // notes
                                    if (cellValue != "") {
                                        Image(
                                            painter = painterResource(id = R.drawable.notes_24),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(alignment)
                                                .clickable { onNoteClick(item.items) },
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                                        )
                                    } else {
                                        TableCell(
                                            value = "",
                                            contentAlignment = alignment,
                                        )
                                    }
                                } // notes
                                5 -> { // quantity
                                    val formattedQty = formattedQuantity[item.items.id] ?: "--"
                                    val outOfStock = formattedQuantity[item.items.id] == "0 oz" ||
                                    formattedQuantity[item.items.id] == "x0" ||
                                    formattedQuantity[item.items.id] == "0 g"

                                    TableCell(
                                    //    value = "x$cellValue",
                                        value = formattedQty,
                                        modifier = Modifier
                                            .align(alignment),
                                        contentAlignment = alignment,
                                        color = if (outOfStock) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                } // quantity
                                else -> { // [2] type
                                    TableCell(
                                        value = cellValue,
                                        modifier = Modifier
                                            .align(alignment),
                                        contentAlignment = alignment,
                                    )
                                } // [2] type
                            }
                        }
                    }
                }
            }
        }


        val shouldScrollUp by filterViewModel.shouldScrollUp.collectAsState()
        val savedItemId by filterViewModel.savedItemId.collectAsState()
        val savedItemIndex = sortedItems.indexOfFirst { it.items.id == savedItemId }
        val searchPerformed by filterViewModel.searchPerformed.collectAsState()
        val searchCleared by filterViewModel.searchCleared.collectAsState()
        val shouldReturn by filterViewModel.shouldReturn.collectAsState()
        val getPosition by filterViewModel.getPosition.collectAsState()

        // Return Positions //
        LaunchedEffect(savedItemIndex) {
            if (savedItemIndex != -1) {
                delay(50)
                withFrameNanos {
                    coroutineScope.launch {
                        if (savedItemIndex > 1 && savedItemIndex < (sortedItems.size - 1)) {
                            val offset = (columnState.layoutInfo.visibleItemsInfo[1].size / 2) * -1
                            columnState.scrollToItem(savedItemIndex, offset)
                        } else {
                            columnState.scrollToItem(savedItemIndex)
                        }
                    }
                }
                filterViewModel.resetScroll()
            }
        }

        LaunchedEffect(sortedItems) {
            delay(20)
            if (shouldScrollUp) {
                columnState.scrollToItem(0)
                filterViewModel.resetScroll()
            }
            if (shouldReturn && !searchPerformed && !shouldScrollUp) {
                val index = currentPosition[0]
                val offset = currentPosition[1]

                if (index != null && offset != null) {
                    withFrameNanos {
                        coroutineScope.launch {
                            columnState.scrollToItem(index, offset)
                        }
                    }
                    filterViewModel.resetScroll()
                }
            }
            if (searchCleared) {
                val index = currentPosition[0]
                val offset = currentPosition[1]

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

        LaunchedEffect(sorting) {
            columnState.scrollToItem(0)
        }

        // Update positions //
        LaunchedEffect(getPosition) {
            if (getPosition > 0) {
                val layoutInfo = columnState.layoutInfo
                val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()

                if (firstVisibleItem != null) {
                    updateScrollPosition(firstVisibleItem.index, firstVisibleItem.offset * -1)
                }
            }
        }
    }
}

@Composable
fun HeaderCell(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon1: Painter? = null,
    icon2: Painter? = null,
    sorting: Sorting,
    primarySort: Boolean,
    onClick: (() -> Unit)? = null,
    contentAlignment: Alignment = Alignment.Center,
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .clickable(
                enabled = onClick != null,
                onClick = {
                    focusManager.clearFocus()
                    onClick?.invoke()
                }
            )
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = contentAlignment
    ) {
        if (primarySort) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (text != null) {
                    Text(
                        text = text,
                        modifier = Modifier,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Image(
                    painter = painterResource(id = sorting.sortIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp),
                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                )
            }
        } else {
            if (text != null) {
                Text(
                    text = text,
                    modifier = Modifier,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (icon1 != null) {
                Image(
                    painter = icon1,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(LocalCustomColors.current.favHeart),
                    modifier = Modifier
                        .size(20.dp)
                )
            }
            if (icon2 != null) {
                Image(
                    painter = icon2,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF000000)),
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 0.dp, y = 0.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCell(
    value: Any?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    contentAlignment: Alignment = Alignment.Center,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .combinedClickable(
                enabled =  (onLongClick != null) || (onClick != null),
                onClick = {
                    focusManager.clearFocus()
                    onClick?.invoke()
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick?.invoke()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .background(backgroundColor),
        contentAlignment = contentAlignment
    ) {
        val text = when (value) {
            null -> ""
            is String -> value.ifBlank { "" }
            else -> value.toString()
        }
        Text(
            text = text,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
