package com.example.tobaccocellar.ui.home

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.tobaccocellar.CellarBottomAppBar
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import com.example.tobaccocellar.ui.theme.LocalCustomColors

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
    val filterViewModel = LocalCellarApplication.current.filterViewModel
//    val scrollBehavior = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
//        TopAppBarDefaults.enterAlwaysScrollBehavior() else TopAppBarDefaults.pinnedScrollBehavior()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
//    val bottomScrollBehavior = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
//        BottomAppBarDefaults.exitAlwaysScrollBehavior() else null
    val homeUiState by viewmodel.homeUiState.collectAsState()
    val showSnackbar = viewmodel.showSnackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sorting by viewmodel.sorting
    val isTableView = homeUiState.isTableView
    val activeItemId by viewmodel.menuItemId
    val isMenuShown by viewmodel.isMenuShown
    val focusManager = LocalFocusManager.current


    if (showSnackbar.value) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "CSV Exported",
                duration = SnackbarDuration.Short
            )
            viewmodel.snackbarShown()
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
                navigateToCsvImport = navigateToCsvImport,
                navigateToSettings = navigateToSettings,
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
                snackbar = { Snackbar(it) }
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
                modifier = Modifier
                    .shadow(2.dp, shape = RectangleShape, clip = false),
                homeUiState = homeUiState,
                selectView = viewmodel::selectView,
                onBlendSearchChanged = viewmodel::onBlendSearchChanged,
       //         searchFieldFocused = searchFieldFocused,
                isTableView = isTableView,
            )
            HomeBody(
                items = homeUiState.items,
                isTableView = isTableView,
                onItemClick = navigateToEditEntry,
                sorting = sorting,
                updateSorting = viewmodel::updateSorting,
                isLoading = homeUiState.isLoading,
                onDismissMenu = viewmodel::onDismissMenu,
                onShowMenu = viewmodel::onShowMenu,
                isMenuShown = isMenuShown,
                activeItemId = activeItemId,
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
    onBlendSearchChanged: (String) -> Unit,
    isTableView: Boolean,
) {
    var blendSearchText by remember { mutableStateOf("") }

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalCustomColors.current.backgroundVariant)
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row (
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
        Box (
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .weight(1f, false)
        ) {
            CustomBlendSearch(
                value = blendSearchText,
                onValueChange = {
                    blendSearchText = it
                    if (it.isEmpty()) {
                        onBlendSearchChanged(it)
                    }
                },
                onImeAction = {
                    onBlendSearchChanged(blendSearchText)
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

@Composable
private fun CustomBlendSearch(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Blend Search",
    leadingIcon: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.search),
            contentDescription = null,
            modifier = Modifier
                .padding(0.dp)
                .size(20.dp),
        )
    },
    onImeAction: () -> Unit = {}
) {
    var showCursor by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
        cursorBrush = if (showCursor) { SolidColor(LocalContentColor.current) }
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
                if (value.isNotEmpty()) {
                    Icon(
                        painter = painterResource(id = R.drawable.clear_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = { onValueChange("") })
                            .padding(0.dp),
                    )
                }
            }
        }
    )
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
    isMenuShown: Boolean,
    activeItemId: Int?,
    onDismissMenu: () -> Unit,
    onShowMenu: (Int) -> Unit,
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
                            .fillMaxWidth()
                    )
                } else {
                    ListViewMode(
                        itemsList = items,
                        onItemClick = { onItemClick(it.id) },
                        onNoteClick = { item -> noteToDisplay = item.notes
                            showNoteDialog = true },
                        menuItemId = activeItemId,
                        onDismissMenu = onDismissMenu,
                        onShowMenu = onShowMenu,
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
        //    elevation = CardDefaults.cardElevation(5.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Unspecified,
            //    contentColor = LocalContentColor.current,
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
    menuItemId: Int?,
    onDismissMenu: () -> Unit,
    onShowMenu: (Int) -> Unit,
    isMenuShown: Boolean,
    itemsList: List<Items>,
    onItemClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(0.dp),
        ) {
            items(items = itemsList, key = { it.id }) { item ->
                val haptics = LocalHapticFeedback.current
                val focusManager = LocalFocusManager.current

                BackHandler(enabled = isMenuShown && menuItemId == item.id) {
                    onDismissMenu()
                }

                CellarListItem(
                    item = item,
                    onItemClick = { onItemClick(item) },
                    onNoteClick = { onNoteClick(item) },
                    modifier = Modifier
                        .padding(0.dp)
                        .combinedClickable(
                            onClick = {
                                if (isMenuShown && menuItemId == item.id) {
                                    // do nothing
                                } else {
                                    onDismissMenu()
                                    focusManager.clearFocus()
                                }
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onShowMenu(item.id)
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    onMenuDissmiss = { onDismissMenu() },
                    showMenu = isMenuShown && menuItemId == item.id,
                )
            }
        }
    }
}

// Entry layout //
@Composable
private fun CellarListItem(
    modifier: Modifier = Modifier,
    item: Items,
    onMenuDissmiss: () -> Unit,
    showMenu: Boolean,
    onItemClick: (Items) -> Unit,
    onNoteClick: (Items) -> Unit,
) {
    Box(
        modifier = modifier
            .padding(0.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 0.dp, top = 0.dp, bottom = 1.dp, end = 0.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp, bottom = 2.dp, end = 8.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(0.dp)
                        .weight(1f, false),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // Blend Name //
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(fraction = .95f),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.blend,
                                modifier = Modifier
                                    .padding(0.dp)
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
                            Spacer(
                                modifier = Modifier
                                    .width(4.dp)
                            )
                            if (item.favorite) {
                                Spacer(
                                    modifier = Modifier
                                        .width(2.dp)
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.favorite_heart_filled_18),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(17.dp),
                                    tint = LocalCustomColors.current.favHeart
                                )
                            }
                            if (item.disliked) {
                                Spacer(
                                    modifier = Modifier
                                        .width(2.dp)
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.heartbroken_filled_18),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(17.dp),
                                    tint = LocalCustomColors.current.disHeart
                                )
                            }
                            if (item.notes.isNotEmpty()) {
                                Spacer(
                                    modifier = Modifier
                                        .width(2.dp)
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.notes_24),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
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
                                text = item.brand,
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
                                text = item.type,
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
                Column(
                    modifier = Modifier
                        .width(18.dp)
                        .padding(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "x" + item.quantity,
                            modifier = Modifier,
                            style =
                            if (item.quantity == 0) (MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.error,
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
        if (showMenu) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(LocalCustomColors.current.listMenuScrim.copy(alpha = 0.80f))
                    .padding(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onItemClick(item)
                            onMenuDissmiss()
                        },
                        modifier = Modifier,
                    ) {
                        Text(
                            text = "Edit item",
                            modifier = Modifier,
                            color = LocalContentColor.current,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    if (item.notes.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                onNoteClick(item)
                                onMenuDissmiss()
                            }
                        ) {
                            Text(
                                text = "View note",
                                modifier = Modifier,
                                color = LocalContentColor.current,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
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
    val columnMinWidths = listOf(
        180.dp, // Brand
        300.dp, // Blend
        108.dp, // Type
        64.dp, // Fav/Dis
        64.dp, // Note
        54.dp // Quantity
    )

    TableLayout(
        items = itemsList,
        columnMinWidths = columnMinWidths,
        onItemClick = onItemClick,
        onNoteClick = onNoteClick,
        sorting = sorting,
        updateSorting = updateSorting,
        modifier = modifier
            .fillMaxWidth()
    )
}

@Composable
fun TableLayout(
    items: List<Items>,
    columnMinWidths: List<Dp>,
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
            for (columnIndex in columnMinWidths.indices) {
                Box(
                    modifier = Modifier
                        //  .width(columnWidths[columnIndex])
                        .width(columnMinWidths[columnIndex])
                        .fillMaxHeight()
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
                    when (columnIndex) {
                        0, 1 -> {
                            HeaderCell(
                                text = headerText,
                                columnIndex = columnIndex,
                                onClick = { onSortChange(columnIndex) },
                                primarySort = sorting.columnIndex == columnIndex,
                                sorting = sorting,
                                modifier = Modifier
                                    .padding(0.dp)
                                    .align(alignment),
                                iconUp = painterResource(id = R.drawable.arrow_up),
                                iconDown = painterResource(id = R.drawable.arrow_down)
                            )
                        }
                        else -> {
                            HeaderCell(
                                text = headerText,
                                columnIndex = columnIndex,
                                primarySort = sorting.columnIndex == columnIndex,
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
                    for (columnIndex in columnMinWidths.indices) {
                        Box(
                            modifier = Modifier
                                .width(columnMinWidths[columnIndex])
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                                .border(Dp.Hairline, color = LocalCustomColors.current.tableBorder)
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
                                1 -> { // blend
                                    TableCell(
                                        value = cellValue,
                                        modifier = Modifier
                                            .align(alignment),
                                        contentAlignment = alignment,
                                        onClick = { onItemClick(item) }
                                    )
                                }
                                3 -> { // fav/disliked
                                    val favDisValue = cellValue as Int
                                    val icon = when (favDisValue) {
                                        1 -> painterResource(id = R.drawable.favorite_heart_filled_18)
                                        2 -> painterResource(id = R.drawable.heartbroken_filled_18)
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
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                                        )
                                    } else {
                                        TableCell(
                                            value = "",
                                            contentAlignment = alignment,
                                        )
                                    }
                                }
                                5 -> { // quantity
                                    TableCell(
                                        value = "x$cellValue",
                                        modifier = Modifier
                                            .align(alignment),
                                        contentAlignment = alignment,
                                    )
                                }
                                else -> { // brand, type
                                    TableCell(
                                        value = cellValue,
                                        modifier = Modifier
                                            .align(alignment),
                                        contentAlignment = alignment,
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
    sorting: Sorting,
    primarySort: Boolean,
    onClick: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .clickable(enabled = onClick != null, onClick = {
                focusManager.clearFocus()
                onClick?.invoke()
            }
            )
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
    onClick: (() -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .background(backgroundColor)
            //  .clickable(enabled = onClick != null) { onClick?.invoke() }
            .combinedClickable(
                enabled = onClick != null,
                onClick = { focusManager.clearFocus() },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick?.invoke()
                },
            ),
        contentAlignment = contentAlignment
    ) {
        val text = when (value) {
            null -> ""
            is String -> value.ifBlank { "" }
            else -> value.toString()
        }
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
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


//@Preview(showBackground = true)
//@Composable
//fun CellarItemPreview() {
//    CellarListItem(
//        modifier = Modifier,
//        Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Burley", 2, false, true, "note",),
//        onItemClick = {},
//        onNoteClick = {},
//        showMenu = false,
//        onMenuDissmiss = {}
//        )
//}


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
//}