package com.example.tobaccocellar.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetDefaults.properties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarBottomAppBar
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import kotlin.math.roundToInt

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToStats: () -> Unit,
    navigateToAddEntry: () -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    navigateToCsvImport: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val homeUiState by viewmodel.homeUiState.collectAsState()
    val sorting by viewmodel.sorting
    val isTableView = homeUiState.isTableView
    val snackbarHostState = remember { SnackbarHostState() }
    val showSnackbar = viewmodel.showSnackbar.collectAsState()
    val filterViewModel = LocalCellarApplication.current.filterViewModel

    if (showSnackbar.value) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "CSV Exported",
                duration = SnackbarDuration.Short
            )
            viewmodel.snackbarShown()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = false,
                navigateToCsvImport = navigateToCsvImport,
                navigateToSettings = navigateToSettings,
                showMenu = true,
                exportCsvHandler = viewmodel,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier
                    .padding(0.dp),
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
                snackbar = { Snackbar(it) }
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, bottom = 66.dp, start = 0.dp, end = 0.dp)
        ) {
            HomeHeader(
                homeUiState = homeUiState,
                selectView = viewmodel::selectView,
                isTableView = isTableView,
            )
            HomeBody(
                items = homeUiState.items,
                isTableView = isTableView,
                onItemClick = navigateToEditEntry,
                sorting = sorting,
                updateSorting = viewmodel::updateSorting,
                isLoading = homeUiState.isLoading,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    selectView: (Boolean) -> Unit,
    isTableView: Boolean,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Switch table/list view //
        Text(
            text = "View:",
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(0.dp)
        )
        IconButton(
            onClick = { selectView(!isTableView) },
            modifier = Modifier
                .padding(4.dp)
                .size(28.dp)
        ) {
            Icon(
                painter = painterResource(homeUiState.toggleIcon),
                contentDescription = stringResource(homeUiState.toggleContentDescription),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(24.dp)
                    .padding(0.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .width(24.dp)
                .padding(0.dp)
        )

        /* TODO Finish Search widget */

        TextField(
            value = "",
            onValueChange = { /* do nothing */ },
            modifier = Modifier
                .fillMaxWidth(fraction = .7f)
                .height(24.dp),
            enabled = false,
            leadingIcon = { Icon(painterResource(id = R.drawable.search), null) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
    }
}


@Composable
private fun HomeBody(
    modifier: Modifier = Modifier,
    items: List<Items>,
    isLoading: Boolean,
    isTableView: Boolean,
    sorting: Sorting,
    updateSorting: (Int) -> Unit,
    onItemClick: (Int) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        if (isLoading) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1.5f)
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(0.dp)
                        .size(48.dp)
                        .weight(0.5f),
                )
                Spacer(
                    modifier = Modifier
                        .weight(2f)
                )
            }
        } else {
            if (items.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.no_items),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(0.dp),
                    )
                }
            } else {
                if (!isTableView) {
                    ListViewMode(
                        itemsList = items,
                        onItemClick = { onItemClick(it.id) },
                        onNoteClick = { item -> noteToDisplay = item.notes
                                      showNoteDialog = true },
                        contentPadding = contentPadding,
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                    )
                }
                if (isTableView) {
                    TableViewMode(
                        itemsList = items,
                        onItemClick = { onItemClick(it.id) },
                        onNoteClick = { item -> noteToDisplay = item.notes
                                      showNoteDialog = true },
                        sorting = sorting,
                        updateSorting = updateSorting,
                        modifier = Modifier
                            .padding(0.dp)
                    )
                } else {
                    ListViewMode(
                        itemsList = items,
                        onItemClick = { onItemClick(it.id) },
                        onNoteClick = { item -> noteToDisplay = item.notes
                            showNoteDialog = true },
                        contentPadding = contentPadding,
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
            .padding(8.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .padding(0.dp)
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        //    elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Unspecified,
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
                        text = "Note",
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
                                modifier = Modifier
                                    .padding(0.dp)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
    modifier: Modifier = Modifier,
    itemsList: List<Items>,
    onItemClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
    ) {
        items(items = itemsList, key = { it.id }) { item ->
            val haptics = LocalHapticFeedback.current
            var showMenu by remember { mutableStateOf(false) }
            var itemBounds by remember { mutableStateOf<Rect?>(null) }
            var menuPosition by remember { mutableStateOf(IntOffset.Zero) }
            CellarListItem(
                item = item,
                modifier = Modifier
                    .padding(0.dp)
                    .onGloballyPositioned { coordinates ->
                        itemBounds = coordinates.boundsInWindow()
                    }
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    )
            )
            if (showMenu) {
                itemBounds?.let {
                    menuPosition = IntOffset(
                        x = it.left.roundToInt() + 50,
                        y = it.top.roundToInt() - 100
                    )
                }
            }
            val density = LocalDensity.current
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = DpOffset(
                    x = density.run { menuPosition.x.toDp() },
                    y = density.run { menuPosition.y.toDp() }
//                    x = with(density) { menuPosition.x.toDp() },
//                    y = with(density) { menuPosition.y.toDp() }
                ),
                properties = PopupProperties(focusable = false)
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Edit Item") },
                    onClick = {
                        onItemClick(item)
                        showMenu = false
                    }
                )
                if (item.notes.isNotEmpty()) {
                    DropdownMenuItem(
                        text = { Text(text = "View Note") },
                        onClick = {
                            onNoteClick(item)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}


// Individual Items in List View //
@Composable
private fun CellarListItem(
    item: Items,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp, top = 0.dp, bottom = 1.dp, end = 0.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer),
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 4.dp, bottom = 3.dp, end = 8.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(0.dp)
                ) {
                    Row (
                        modifier = Modifier
                            .padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 0.dp)
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (item.favorite) {
                            Icon(
                                painter = painterResource(id = R.drawable.favorite_heart_filled_18),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(0.dp)
                                    .graphicsLayer {
                                        rotationZ = (-45f)
                                    },
                                tint = Color(0x60FF0000)
                            )
                        }
                    }
// Blend Name //
                    Column {
                        Text(
                            text =
                            if (item.disliked) (item.blend + " ")
                            else (item.blend),
                            modifier = Modifier
                                .padding(0.dp)
                                .fillMaxWidth(fraction = .9f)
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    repeatDelayMillis = 250,
                                    initialDelayMillis = 250,
                                    spacing = MarqueeSpacing(100.dp)
                                ),
                            style =
                            if (item.quantity == 0 && !item.disliked) (
                                    MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    )
                            else if (item.disliked && item.quantity > 0) (
                                    MaterialTheme.typography.titleMedium.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    )
                            else if (item.disliked && item.quantity == 0) (
                                    MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.tertiary,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    )
                            else (MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textDecoration = TextDecoration.None
                            )
                                    ),
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 8.dp)
                                .offset(y = (-4).dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
// Brand Name //
                            Column(
                                modifier = Modifier
                                    .padding(0.dp)
                                    .weight(.95f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = item.brand,
                                    modifier = Modifier,
                                    style =
                                    if (item.quantity == 0) (
                                            MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                textDecoration = TextDecoration.None
                                            )
                                            )
                                    else (MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        textDecoration = TextDecoration.None
                                    )
                                            ),
                                    fontStyle = Italic,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
// Type //
                            Column(
                                modifier = Modifier
                                    .padding(0.dp)
                                    .weight(0.5f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.type,
                                    modifier = Modifier,
                                    style =
                                    if (item.quantity == 0) (
                                            MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                textDecoration = TextDecoration.None
                                            )
                                            )
                                    else (MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        textDecoration = TextDecoration.None
                                    )
                                            ),
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .width(0.dp)
                                    .padding(0.dp)
                                    .weight(1f)
                            )
                        }
                    }
                }

            }
            Column(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(0.1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.End
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "x" + item.quantity,
                        modifier = Modifier,
                        style =
                        if (item.quantity == 0) (
                                MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    textDecoration = TextDecoration.None)
                                )
                        else (MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textDecoration = TextDecoration.None)
                                ),
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


/** Table View Mode **/
@Composable
fun TableViewMode(
    modifier: Modifier = Modifier,
    itemsList: List<Items>,
    onItemClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
    sorting: Sorting,
    updateSorting: (Int) -> Unit,
) {
    val columnWidths = listOf(
        180.dp, // Brand
        250.dp, // Blend
        108.dp, // Type
        64.dp, // Fav/Dis
        64.dp, // Note
        54.dp // Quantity
    )
    TableLayout(
        items = itemsList,
        columnWidths = columnWidths,
        onItemClick = onItemClick,
        onNoteClick = onNoteClick,
        sorting = sorting,
        updateSorting = updateSorting,
        modifier = modifier
    )
}

@Composable
fun TableLayout(
    items: List<Items>,
    columnWidths: List<Dp>,
    onItemClick: (Items) -> Unit,
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
        { item: Items -> item.quantity }, // 5
    )

    val sortedItems = when (sorting.columnIndex) {
        0 -> items.sortedBy { it.brand }
        1 -> items.sortedBy { it.blend }
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
            for (columnIndex in columnWidths.indices) {
                Box(
                    modifier = Modifier
                        .width(columnWidths[columnIndex])
                        .fillMaxHeight()
                        .padding(0.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(Dp.Hairline, color = Color.Gray)
                ) {
                    val alignment = when (columnIndex) {
                        0 -> Alignment.CenterStart // brand
                        1 -> Alignment.CenterStart // blend
                        2 -> Alignment.Center // type
                        3 -> Alignment.Center // fav/dis
                        4 -> Alignment.Center // notes
                        5 -> Alignment.CenterEnd // quantity
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

                    HeaderCell(
                        text = headerText,
                        columnIndex = columnIndex,
                        primarySort = sorting.columnIndex == columnIndex,
                        onSortChange = onSortChange,
                        sorting = sorting,
                        modifier = Modifier
                            .padding(0.dp)
                            .align(alignment),
                        icon1 = if (columnIndex == 3)
                            painterResource(id = R.drawable.favorite_heart_filled_18) else null,
                        icon2 = if (columnIndex == 3)
                            painterResource(id = R.drawable.question_mark_24) else null,
                        iconUp = painterResource(id = R.drawable.arrow_up),
                        iconDown = painterResource(id = R.drawable.arrow_down)
                    )
                }
            }
        }
// Items
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(items = sortedItems, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(intrinsicSize = IntrinsicSize.Min)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    for (columnIndex in columnWidths.indices) {
                        Box(
                            modifier = Modifier
                                .width(columnWidths[columnIndex])
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                                .border(Dp.Hairline, color = Color.Gray)
                        ) {
                            val cellValue = columnMapping[columnIndex](item)
                            val alignment = when (columnIndex) {
                                0 -> Alignment.CenterStart // brand
                                1 -> Alignment.CenterStart // blend
                                2 -> Alignment.Center // type
                                3 -> Alignment.Center // fav/dis
                                4 -> Alignment.Center // notes
                                5 -> Alignment.CenterEnd // quantity
                                else -> Alignment.CenterStart
                            }
                            when (columnIndex) {
                                3 -> { // fav/disliked
                                    val favDisValue = cellValue as Int
                                    val icon = when (favDisValue) {
                                        1 -> painterResource(id = R.drawable.favorite_heart_filled_18)
                                        2 -> painterResource(id = R.drawable.heartbroken_filled_18)
                                        else -> null
                                    }
                                    if (icon != null) {
                                        val tintColor = when (favDisValue) {
                                            1 -> Color(0xFFAA0000)
                                            2 -> Color(0xFFAA0000)
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
                                }
                                4 -> { // notes
                                    if (cellValue != "") {
                                        Image(
                                            painter = painterResource(id = R.drawable.notes_24),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(alignment)
                                                .clickable { onNoteClick(item) },
                                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                                        )
                                    } else {
                                        TableCell(
                                            value = "",
                                            contentAlignment = alignment,
                                        )
                                    }
                                }
                                else -> {
                                    TableCell(
                                        value = cellValue,
                                        modifier = Modifier
                                            .align(alignment),
                                        contentAlignment = alignment,
                                        onClick = {
                                            when (columnIndex) {
                                                0 -> onItemClick(item)
                                                1 -> onItemClick(item)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
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
    iconUp: Painter? = null,
    iconDown: Painter? = null,
    columnIndex: Int,
    onSortChange: ((Int) -> Unit)?,
    sorting: Sorting,
    primarySort: Boolean,
) {
    Box(
        modifier = modifier
            .clickable {
                onSortChange?.invoke(columnIndex)
            }
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
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
                if (sorting.sortAscending) {
                    iconUp?.let {
                        Image(
                            painter = it,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                            modifier = Modifier
                                .size(22.dp)
                        )
                    }
                } else {
                    iconDown?.let {
                        Image(
                            painter = it,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                            modifier = Modifier
                                .size(22.dp)
                        )
                    }
                }
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
                    colorFilter = ColorFilter.tint(Color(0xFFAA0000)),
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

@Composable
fun TableCell(
    value: Any?,
    modifier: Modifier = Modifier,
//    textColor: Color = Color.Black,
    backgroundColor: Color = Color.Transparent,
    contentAlignment: Alignment = Alignment.Center,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .background(backgroundColor)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = contentAlignment
    ) {
        val text = when (value) {
            null -> ""
            is String -> if (value.isBlank()) "" else value
            else -> value.toString()
        }
        Text(
            text = text,
//            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}




//// Previews ////

//@Preview(showBackground = true)
//@Composable
//fun HomeHeaderPreview() {
//    HomeHeader(
//        homeUiState = HomeUiState(),
//        selectView = {},
//        isTableView = true,
//    )
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun HomeBodyListPreview() {
//    ListViewMode(itemsList = listOf(
//        Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Virginia", 2, disliked = false, favorite = true, notes = null.toString()),
//        Items(1, "Sutliff", "Maple Shadows", "Aromatic", 0, disliked = false, favorite = false, notes = null.toString()),
//        Items(2, "Cornell & Diehl", "Pegasus", "Burley", 2, disliked = true, favorite = false, notes = null.toString()),
//        Items(3, "Cornell & Diehl", "Some super long blend name to test the basic marquee effect if that ever happens", "Burley", 0, disliked = true, favorite = true, notes = null.toString())
//    ), onItemClick = {}, contentPadding = PaddingValues(0.dp)
//    )
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun CellarItemPreview() {
//    CellarListItem(
//        Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Burley", 2,
//            disliked = false,
//            favorite = true,
//            notes = null.toString(),
//        ),
//    )
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun HomeBodyTablePreview() {
//    TableViewMode(itemsList = listOf(
//        Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Burley", 2,
//            disliked = false,
//            favorite = true,
//            notes = null.toString(),
//        ),
//        Items(1, "Sutliff", "Maple Shadows", "Aromatic", 0, disliked = false, favorite = true, notes = null.toString()),
//        Items(2, "Cornell & Diehl", "Pegasus", "Burley", 2, disliked = true, favorite = false, notes = null.toString()),
//        Items(3, "Warped", "Midsommar", "Burley", 1, disliked = false, favorite = false, notes = null.toString())
//    ), contentPadding = PaddingValues(0.dp)
//    )
//}

//@Preview(showBackground = true)
//@Composable
//fun NewTablePreview() {
//    TableLayout(
//        items = listOf(
//            Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Burley", 2,
//                disliked = false,
//                favorite = true,
//                notes = null.toString(),
//            ),
//            Items(1, "Sutliff", "Maple Shadows", "Aromatic", 0, disliked = false, favorite = true, notes = null.toString()),
//            Items(2, "Cornell & Diehl", "Pegasus", "Burley", 2, disliked = true, favorite = false, notes = null.toString()),
//            Items(3, "Warped", "Midsommar", "Burley", 1, disliked = false, favorite = false, notes = null.toString())
//    ),
//        onItemClick = {},
//        onNoteClick = {},
//        columnWidths = listOf(144.dp, 288.dp, 96.dp, 52.dp, 52.dp, 52.dp)
//    )
//
//}