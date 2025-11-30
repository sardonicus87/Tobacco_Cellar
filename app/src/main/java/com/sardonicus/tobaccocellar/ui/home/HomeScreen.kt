package com.sardonicus.tobaccocellar.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onLayoutRectChanged
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarBottomAppBar
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.CheckboxWithLabel
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.navigation.HomeDestination
import com.sardonicus.tobaccocellar.ui.settings.TypeGenreOption
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    modifier: Modifier = Modifier,
    viewmodel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val preferencesRepo = LocalCellarApplication.current.preferencesRepo
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val onDismissMenu = viewmodel::onDismissMenu

    val searchFocused by filterViewModel.searchFocused.collectAsState()
    val searchPerformed by filterViewModel.searchPerformed.collectAsState()
    val isTinSearch by filterViewModel.isTinSearch.collectAsState()
    val searchText by filterViewModel.searchTextDisplay.collectAsState()
    val emptyDatabase by filterViewModel.emptyDatabase.collectAsState()

    val resetLoading by viewmodel.resetLoading.collectAsState()
    val activeMenuId by remember { viewmodel.activeMenuId }
    val isMenuShown by remember { viewmodel.itemMenuShown }
    val emptyMessage by viewmodel.emptyMessage.collectAsState()
    val showTins by filterViewModel.showTins.collectAsState()
    val columnState = rememberLazyListState()

    val homeUiState by viewmodel.homeUiState.collectAsState()
    val columnVisibility by viewmodel.tableColumnVisibility.collectAsState()
    val columnVisibilityEnablement by viewmodel.columnVisibilityEnablement.collectAsState()

    val showSnackbar by viewmodel.showSnackbar.collectAsState()
    if (showSnackbar) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "CSV Exported",
                duration = SnackbarDuration.Short
            )
            viewmodel.snackbarShown()
        }
    }

    BackHandler(searchFocused) {
        if (searchFocused) {
            focusManager.clearFocus()
            filterViewModel.updateSearchFocused(false)
        }
    }
    BackHandler(searchPerformed) {
        if (!searchFocused) {
            filterViewModel.updateSearchText("")
            filterViewModel.onSearch("")
            filterViewModel.updateSearchIconOpacity(0.5f)

            if (searchPerformed) {
                coroutineScope.launch {
                    EventBus.emit(SearchClearedEvent)
                }
            }
        }
    }

    var lastClickedItemId by remember { mutableIntStateOf(-1) }
    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(lastClickedItemId) {
        if (lastClickedItemId != -1 && !isNavigating) {
            isNavigating = true
            navigateToBlendDetails(lastClickedItemId)
        }
    }
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            delay(200)
            isNavigating = false
        }
    }


    // Important Alert stuff
    val lastAlertShown by preferencesRepo.lastAlertFlow.collectAsState(initial = 999)
    var showImportantAlert by remember { mutableStateOf(false) }
    val onShowImportant: (Boolean) -> Unit = { showImportantAlert = it }
    var currentAlert: OneTimeAlert? by remember { mutableStateOf(null) }
    var unseenPastAlerts by remember { mutableStateOf(listOf<OneTimeAlert>()) }
    var pastAlertIndex by remember { mutableIntStateOf(0) }
    var remainingUnseen by remember { mutableIntStateOf(0) }
    var currentPastAlertId by remember { mutableIntStateOf(-1) }

    @Suppress("KotlinConstantConditions")
    LaunchedEffect(lastAlertShown) {
        if (lastAlertShown < OneTimeAlerts.CURRENT_ALERT_VERSION) {
            currentAlert = OneTimeAlerts.alerts.find { it.id == OneTimeAlerts.CURRENT_ALERT_VERSION }
            val unseenPastAlertCount = OneTimeAlerts.CURRENT_ALERT_VERSION - lastAlertShown - 1
            unseenPastAlerts = if (unseenPastAlertCount > 0) {
                val startId = lastAlertShown + 1
                val endId = OneTimeAlerts.CURRENT_ALERT_VERSION - 1
                OneTimeAlerts.alerts.filter { it.id in startId..endId }.sortedBy { it.id }
            } else {
                emptyList()
            }

            remainingUnseen = unseenPastAlerts.size

            if (unseenPastAlerts.isNotEmpty()) {
                pastAlertIndex = 0
                currentPastAlertId = unseenPastAlerts[0].id
                showImportantAlert = true
            } else if (currentAlert != null) {
                currentPastAlertId = -1
                showImportantAlert = true
            } else {
                remainingUnseen = 0
                currentPastAlertId = -1
                showImportantAlert = false
            }
        } else {
            showImportantAlert = false
            currentAlert = null
            remainingUnseen = 0
            currentPastAlertId = -1
        }
    }

    if (showImportantAlert && currentAlert != null) {
        val alert = currentAlert!!
        val scrollState = rememberScrollState()
        var enabled by rememberSaveable { mutableStateOf(false) }
        val atBottom by remember {
            derivedStateOf {
                scrollState.value == scrollState.maxValue
            }
        }
        var countdown by remember { mutableIntStateOf(5) }
        var canScroll by remember { mutableStateOf(false) }
        val updateScroll: (Boolean) -> Unit = { canScroll = it }

        AlertDialog(
            onDismissRequest = { /* Not dismissible */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
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
                            if (unseenPastAlerts.isNotEmpty() && pastAlertIndex < unseenPastAlerts.size) {
                                Column(
                                    modifier = Modifier
                                ) {
                                    Text(
                                        text = "Missed Alert:",
                                        modifier = Modifier,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${unseenPastAlerts[pastAlertIndex].date}\n(v ${unseenPastAlerts[pastAlertIndex].appVersion})",
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
                                    unseenPastAlerts[pastAlertIndex].message()
                                }
                            } else {
                                Column {
                                    if (canScroll) {
                                        Text(
                                            text = "You must scroll to the bottom to be able to acknowledge and " +
                                                    "dismiss this dialog.",
                                            modifier = Modifier
                                                .padding(bottom = 16.dp)
                                        )
                                    }
                                    alert.message()
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                val isScrollable by remember { derivedStateOf { scrollState.maxValue > 0 } }
                LaunchedEffect(isScrollable) {
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
                LaunchedEffect(isScrollable, atBottom, currentPastAlertId) {
                    if (isScrollable && atBottom) {
                        enabled = true
                    }
                }
                val buttonText = remember(isScrollable, enabled, countdown, remainingUnseen) {
                    if (remainingUnseen > 0) {
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
                        coroutineScope.launch {
                            if (remainingUnseen > 0) {
                                enabled = false
                                updateScroll(false)
                                countdown = 5
                                preferencesRepo.saveAlertShown(currentPastAlertId)
                            } else {
                                preferencesRepo.saveAlertShown(alert.id)
                                onShowImportant(false)
                                currentAlert = null
                            }
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

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) {
                focusManager.clearFocus()
                onDismissMenu()
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
                exportCsvHandler = viewmodel,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier,
                navigateToDates = navigateToDates,
                navigateToStats = navigateToStats,
                navigateToAddEntry = navigateToAddEntry,
                currentDestination = HomeDestination,
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
            val glowSize = if (homeUiState.isTableView) 0.dp else 3.dp
            HomeHeader(
                modifier = Modifier,
                homeUiState = homeUiState,
                isTableView = homeUiState.isTableView,
                emptyDatabase = emptyDatabase,
                searchPerformed = searchPerformed,
                onShowColumnPop = viewmodel::showColumnMenuToggle,
                selectView = viewmodel::selectView,
                filterViewModel = filterViewModel,
                searchText = searchText,
                saveListSorting = viewmodel::saveListSorting,
                listSorting = homeUiState.listSorting,
                filteredItems = homeUiState.sortedItems,
            )
            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                size = GlowSize(top = glowSize)
            ) {
                HomeBody(
                    isLoading = homeUiState.isLoading,
                    resetLoading = resetLoading,
                    emptyDb = emptyDatabase,
                    isTableView = homeUiState.isTableView,
                    showRating = homeUiState.showRating,
                    typeGenreOption = homeUiState.typeGenreOption,
                    emptyMessage = emptyMessage,
                    columnState = columnState,
                    sortedItems = homeUiState.sortedItems,
                    tins = homeUiState.filteredTins,
                    formattedQuantity = homeUiState.formattedQuantities,
                    coroutineScope = coroutineScope,
                    showTins = showTins,
                    filterViewModel = filterViewModel,
                    onDetailsClick = { if (lastClickedItemId != it) lastClickedItemId = it },
                    onEditClick = navigateToEditEntry,
                    isMenuShown = isMenuShown,
                    activeMenuId = activeMenuId,
                    onShowMenu = viewmodel::onShowMenu,
                    onDismissMenu = onDismissMenu,
                    getPositionTrigger = filterViewModel::getPositionTrigger,
                    searchFocused = searchFocused,
                    searchPerformed = searchPerformed,
                    isTinSearch = isTinSearch,
                    tableSorting = homeUiState.tableSorting,
                    updateSorting = viewmodel::updateSorting,
                    showColumnPop = viewmodel.showColumnMenu.value,
                    hideColumnPop = viewmodel::showColumnMenuToggle,
                    columnVisibility = columnVisibility,
                    columnVisibilityEnablement = columnVisibilityEnablement,
                    onVisibilityChange = viewmodel::updateColumnVisibility,
                    shouldScrollUp = filterViewModel::shouldScrollUp,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                )
            }
        }
    }
}


@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    isTableView: Boolean,
    emptyDatabase: Boolean,
    onShowColumnPop: () -> Unit,
    selectView: (Boolean) -> Unit,
    filterViewModel: FilterViewModel,
    searchPerformed: Boolean,
    searchText: String,
    saveListSorting: (ListSortOption) -> Unit,
    listSorting: ListSorting,
    filteredItems: List<ItemsComponentsAndTins>
) {
    val tinsExist by filterViewModel.tinsExist.collectAsState()
    val notesExist by filterViewModel.notesExist.collectAsState()

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
        Row(
            modifier = Modifier
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

        Spacer(Modifier.width(8.dp))

        // Search field
        Box(
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .weight(1f, false),
        ) {
            val coroutineScope = rememberCoroutineScope()
            val currentSetting by LocalCellarApplication.current.preferencesRepo.searchSetting.collectAsState(initial = SearchSetting.Blend)

            CustomBlendSearch(
                value = searchText,
                onValueChange = {
                    filterViewModel.updateSearchText(it)
                    if (it.isEmpty()) {
                        filterViewModel.onSearch(it)
                        filterViewModel.updateSearchIconOpacity(0.5f)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) filterViewModel.updateSearchFocused(true)
                        else filterViewModel.updateSearchFocused(false)
                    },
                onImeAction = {
                    coroutineScope.launch {
                        if (searchText.isNotBlank()) {
                            if (!searchPerformed) { filterViewModel.getPositionTrigger() }
                            delay(15)
                            EventBus.emit(SearchPerformedEvent)
                            filterViewModel.onSearch(searchText)
                        }
                    }
                },
                leadingIcon = {
                    val iconAlpha = filterViewModel.searchIconOpacity.collectAsState().value
                    var expanded by rememberSaveable { mutableStateOf(false) }

                    LaunchedEffect(expanded, searchPerformed) {
                        if (searchPerformed) {
                            filterViewModel.updateSearchIconOpacity(1f)
                        } else {
                            filterViewModel.updateSearchIconOpacity(0.5f)
                        }
                        if (!searchPerformed) {
                            if (expanded) { filterViewModel.updateSearchIconOpacity(1f) }
                            if (!expanded) { filterViewModel.updateSearchIconOpacity(0.5f) }
                        }
                    }

                    val blendSearch = SearchSetting.Blend
                    val notesSearch = if (notesExist) SearchSetting.Notes else null
                    val tinsSearch = if (tinsExist) SearchSetting.TinLabel else null
                    val settingList = listOfNotNull(blendSearch, notesSearch, tinsSearch)
                    val enabled = settingList.size > 1

                    LaunchedEffect(enabled) {
                        if (!enabled) {
                            filterViewModel.saveSearchSetting(blendSearch.value)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(0.dp)
                            .clickable(
                                enabled = enabled && !emptyDatabase,
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { expanded = !expanded }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .size(20.dp),
                            tint = LocalContentColor.current.copy(alpha = iconAlpha)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.triangle_arrow_down),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 7.dp, y = 0.dp)
                                .padding(0.dp)
                                .size(16.dp),
                            tint = if (enabled && !emptyDatabase) LocalContentColor.current.copy(alpha = iconAlpha) else Color.Transparent
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier,
                        containerColor = LocalCustomColors.current.textField,
                        offset = DpOffset((-2).dp, 2.dp)
                    ) {
                        settingList.forEach {
                            DropdownMenuItem(
                                text = { Text(text = it.value) },
                                onClick = {
                                    filterViewModel.saveSearchSetting(it.value)
                                    expanded = false
                                },
                                modifier = Modifier
                                    .padding(0.dp),
                                enabled = true,
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.clear_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) {
                                    filterViewModel.updateSearchText("")
                                    filterViewModel.onSearch("")
                                    filterViewModel.updateSearchIconOpacity(0.5f)

                                    if (searchPerformed) {
                                        coroutineScope.launch {
                                            EventBus.emit(SearchClearedEvent)
                                        }
                                    }
                                }

                                .padding(0.dp),
                        )
                    }
                },
                placeholder = "${currentSetting.value} Search",
            )
        }

        Spacer(Modifier.width(8.dp))

        // total items & list sorting or column hiding
        Row(
            modifier = Modifier
                .padding(0.dp)
                .width(68.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            var sortingMenu by rememberSaveable { mutableStateOf(false) }

            Box {
                // List Sorting
                if (!isTableView) {
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
                        modifier = Modifier
                            .width(94.dp),
                        containerColor = LocalCustomColors.current.textField,
                    ) {
                        homeUiState.sortingOptions.forEach {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier,
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                    ) {
                                        Text(
                                            text = it.value,
                                            fontWeight = FontWeight.Normal,
                                            modifier = Modifier
                                                .padding(end = 2.dp)
                                        )
                                        if (listSorting.option.value == it.value) {
                                            val icon = listSorting.listIcon
                                            Box(
                                                modifier = Modifier
                                            ) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Image(
                                                    painter = painterResource(id = icon),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .padding(0.dp),
                                                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    saveListSorting(it)
                                    filterViewModel.shouldScrollUp()
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
                } // column popup button
            }

            Spacer(Modifier.width(6.dp))

            // Total Items //
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
                    text = "${filteredItems.size}",
                    modifier = Modifier,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    maxLines = 1,
                )
            }
        }
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
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    onImeAction: () -> Unit = {},
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
            }
        }
    )
}


@Composable
private fun HomeBody(
    isLoading: Boolean,
    resetLoading: Boolean,
    emptyDb: Boolean,
    isTableView: Boolean,
    showRating: Boolean,
    typeGenreOption: TypeGenreOption,
    emptyMessage: String,
    columnState: LazyListState,
    sortedItems: List<ItemsComponentsAndTins>,
    tins: List<Tins>,
    formattedQuantity: Map<Int, String>,
    coroutineScope: CoroutineScope,
    showTins: Boolean,
    filterViewModel: FilterViewModel,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    isMenuShown: Boolean,
    activeMenuId: Int?,
    onShowMenu: (Int) -> Unit,
    onDismissMenu: () -> Unit,
    getPositionTrigger: () -> Unit,
    searchFocused: Boolean,
    searchPerformed: Boolean,
    isTinSearch: Boolean,
    tableSorting: TableSorting,
    updateSorting: (Int) -> Unit,
    showColumnPop: Boolean,
    hideColumnPop: () -> Unit,
    columnVisibility: Map<TableColumn, Boolean>,
    columnVisibilityEnablement: Map<TableColumn, Boolean>,
    onVisibilityChange: (TableColumn, Boolean) -> Unit,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayedMessage by remember { mutableStateOf(emptyMessage) }
    var searchWasPerformed by remember { mutableStateOf(searchPerformed) }

    var showLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading, resetLoading, emptyDb, emptyMessage, sortedItems) {
        if (isLoading || resetLoading) {
            showLoading = true
        } else {
            if (!emptyDb && sortedItems.isNotEmpty()) {
                snapshotFlow { columnState.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
                showLoading = false
            } else {
                if (emptyDb) {
                    showLoading = false
                } else {
                    snapshotFlow { emptyMessage.isNotBlank() }.first { it }
                    showLoading = false
                }
            }
        }
    }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            if (sortedItems.isEmpty()) {
                LaunchedEffect(emptyMessage, searchPerformed, searchWasPerformed) {
                    if (searchWasPerformed && !searchPerformed) {
                        delay(20L)
                        displayedMessage = emptyMessage
                    } else {
                        displayedMessage = emptyMessage
                    }
                    searchWasPerformed = searchPerformed
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = displayedMessage,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(0.dp),
                    )
                    Spacer(Modifier.weight(1.25f))
                }
            } else {
                if (isTableView) {
                    TableViewMode(
                        sortedItems = sortedItems,
                        columnState = columnState,
                        typeGenreOption = typeGenreOption,
                        showTins = showTins,
                        filteredTins = tins,
                        formattedQty = formattedQuantity,
                        searchPerformed = searchPerformed,
                        isTinSearch = isTinSearch,
                        searchFocused = searchFocused,
                        onDetailsClick = { onDetailsClick(it.id) },
                        onEditClick = { onEditClick(it.id) },
                        getPositionTrigger = { getPositionTrigger() },
                        activeMenuId = activeMenuId,
                        onShowMenu = onShowMenu,
                        onDismissMenu = onDismissMenu,
                        isMenuShown = isMenuShown,
                        sorting = tableSorting,
                        updateSorting = updateSorting,
                        columnVisibility = columnVisibility,
                        shouldScrollUp = shouldScrollUp,
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                    )
                } else {
                    ListViewMode(
                        sortedItems = sortedItems,
                        columnState = columnState,
                        showRating = showRating,
                        typeGenreOption = typeGenreOption,
                        showTins = showTins,
                        tinsList = tins,
                        formattedQuantity = formattedQuantity,
                        searchPerformed = searchPerformed,
                        isTinSearch = isTinSearch,
                        searchFocused = searchFocused,
                        onDetailsClick = { onDetailsClick(it.id) },
                        onEditClick = { onEditClick(it.id) },
                        getPositionTrigger = { getPositionTrigger() },
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

        if (showLoading) { LoadingIndicator() }

        if (showColumnPop) {
            ColumnVisibilityPopup(
                visibilityMap = columnVisibility,
                columnVisibilityEnablement = columnVisibilityEnablement,
                onVisibilityChange = { column, visibility ->
                    onVisibilityChange(column, visibility)
                },
                onDismiss = { hideColumnPop() }
            )
        }


        // jump to button
        JumpToButton(
            columnState = columnState,
            itemCount = sortedItems.size,
            coroutineScope = coroutineScope,
            onScrollToTop = { coroutineScope.launch { columnState.scrollToItem(0) } },
            onScrollToBottom = { coroutineScope.launch { columnState.scrollToItem(sortedItems.lastIndex) } },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
        )

        val currentItemsList by rememberUpdatedState(sortedItems)
        val currentPosition by filterViewModel.currentPosition.collectAsState()
        val shouldScrollUp by filterViewModel.shouldScrollUp.collectAsState()
        val savedItemId by filterViewModel.savedItemId.collectAsState()
        val savedItemIndex = sortedItems.indexOfFirst { it.items.id == savedItemId }
        val shouldReturn by filterViewModel.shouldReturn.collectAsState()
        val getPosition by filterViewModel.getPosition.collectAsState()

        // Scroll to Positions //
        LaunchedEffect(currentItemsList) {
            while (columnState.layoutInfo.visibleItemsInfo.isEmpty()) { delay(2) }
            if (savedItemIndex != -1) {
                withFrameNanos {
                    coroutineScope.launch {
                        if (savedItemIndex > 0 && savedItemIndex < (sortedItems.size - 1)) {
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
        }

        // Save positions //
        LaunchedEffect(getPosition) {
            if (getPosition > 0 && !searchPerformed) {
                val layoutInfo = columnState.layoutInfo
                val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()

                if (firstVisibleItem != null) {
                    filterViewModel.updateScrollPosition(firstVisibleItem.index, firstVisibleItem.offset * -1)
                }
            }
        }
    }
}


/** JumpTo Button **/
@Stable
@Composable
private fun JumpToButton(
    columnState: LazyListState,
    itemCount: Int,
    coroutineScope: CoroutineScope,
    onScrollToTop: () -> Unit,
    onScrollToBottom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (isVisibleState, scrollDirectionState) = rememberJumpToState(columnState)

    val isVisible by remember { isVisibleState }
    val scrollDirection by remember { scrollDirectionState }

    val icon = if (scrollDirection == ScrollDirection.DOWN) R.drawable.double_down else R.drawable.double_up
    val direction = if (scrollDirection == ScrollDirection.DOWN) "bottom" else "top"

    AnimatedVisibility(
        visible = isVisible && (itemCount > 75),
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    if (scrollDirection == ScrollDirection.DOWN) {
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
                painter = painterResource(id = icon),
                contentDescription = "Scroll to $direction",
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
    searchPerformed: Boolean,
    getPositionTrigger: () -> Unit,
    item: ItemsComponentsAndTins,
    onEditClick: (Items) -> Unit,
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
                if (!searchPerformed) {
                    getPositionTrigger()
                }
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
    }
}


/** List View Mode **/
@Stable
@Composable
fun ListViewMode(
    sortedItems: List<ItemsComponentsAndTins>,
    columnState: LazyListState,
    showRating: Boolean,
    typeGenreOption: TypeGenreOption,
    showTins: Boolean,
    tinsList: List<Tins>,
    formattedQuantity: Map<Int, String>,
    searchPerformed: Boolean,
    isTinSearch: Boolean,
    searchFocused: Boolean,
    onDetailsClick: (Items) -> Unit,
    onEditClick: (Items) -> Unit,
    getPositionTrigger: () -> Unit,
    activeMenuId: Int?,
    onShowMenu: (Int) -> Unit,
    isMenuShown: Boolean,
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
                .padding(0.dp),
            state = columnState,
        ) {
            items(items = sortedItems, key = { it.items.id }) { item ->
                val haptics = LocalHapticFeedback.current
                val focusManager = LocalFocusManager.current

                BackHandler(isMenuShown && activeMenuId == item.items.id) { onDismissMenu() }

                CellarListItem(
                    item = item,
                    showRating = showRating,
                    typeGenreOption = typeGenreOption,
                    showTins = showTins,
                    filteredTins = tinsList,
                    formattedQuantity = formattedQuantity[item.items.id] ?: "--",
                    searchPerformed = searchPerformed,
                    isTinSearch = isTinSearch,
                    onEditClick = { onEditClick(item.items) },
                    modifier = Modifier
                        .padding(0.dp)
                        .combinedClickable(
                            onClick = {
                                if (searchFocused) {
                                    focusManager.clearFocus()
                                } else {
                                    if (isMenuShown && activeMenuId == item.items.id) {
                                        // do nothing
                                    } else {
                                        if (isMenuShown) {
                                            onDismissMenu()
                                        } else {
                                            if (!searchPerformed) {
                                                getPositionTrigger()
                                            }
                                            onDetailsClick(item.items)
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!searchPerformed) {
                                    getPositionTrigger()
                                }
                                onShowMenu(item.items.id)
                            },
                            indication = null,
                            interactionSource = null
                        ),
                    onMenuDismiss = { onDismissMenu() },
                    getPositionTrigger = { getPositionTrigger() },
                    showMenu = isMenuShown && activeMenuId == item.items.id
                )
            }
        }
    }
}


@Stable
@Composable
private fun CellarListItem(
    modifier: Modifier = Modifier,
    item: ItemsComponentsAndTins,
    showRating: Boolean,
    typeGenreOption: TypeGenreOption,
    showTins: Boolean,
    searchPerformed: Boolean,
    isTinSearch: Boolean,
    filteredTins: List<Tins>,
    formattedQuantity: String,
    getPositionTrigger: () -> Unit,
    onMenuDismiss: () -> Unit,
    showMenu: Boolean,
    onEditClick: (Items) -> Unit,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = modifier
                .padding(0.dp)
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
                            // brand & type and/or subgenre
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 8.dp)
                                    .offset(y = (-4).dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    10.dp,
                                    alignment = Alignment.Start
                                ),
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
                                // Type and/or Subgenre //
                                val text = when (typeGenreOption) {
                                    TypeGenreOption.TYPE -> item.items.type
                                    TypeGenreOption.SUBGENRE -> item.items.subGenre
                                    TypeGenreOption.BOTH -> {
                                        item.items.type +
                                        if (item.items.type.isNotEmpty() && item.items.subGenre.isNotEmpty()) { " - " } else { "" } +
                                        item.items.subGenre
                                    }
                                    TypeGenreOption.TYPE_FALLBACK -> item.items.type.ifBlank { item.items.subGenre }
                                    TypeGenreOption.SUB_FALLBACK -> item.items.subGenre.ifBlank { item.items.type }
                                }
                                val shown = when (typeGenreOption) {
                                    TypeGenreOption.TYPE -> item.items.type.isNotEmpty()
                                    TypeGenreOption.SUBGENRE -> item.items.subGenre.isNotEmpty()
                                    TypeGenreOption.BOTH -> item.items.type.isNotEmpty() || item.items.subGenre.isNotEmpty()
                                    TypeGenreOption.TYPE_FALLBACK -> item.items.type.isNotEmpty() || item.items.subGenre.isNotEmpty()
                                    TypeGenreOption.SUB_FALLBACK -> item.items.subGenre.isNotEmpty() || item.items.type.isNotEmpty()
                                }

                                if (shown) {
                                    Text(
                                        text = text,
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            textDecoration = TextDecoration.None
                                        ),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.sp,
                                    )
                                }

                                if (showRating && item.items.rating != null) {
                                    Row (
                                        modifier = Modifier,
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.items.rating.toString(),
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
                        val zeroQuantity = Regex("[1-9]")
                        val outOfStock = !formattedQuantity.contains(zeroQuantity)

                        Text(
                            text = formattedQuantity,
                            modifier = Modifier,
                            style =
                                if (outOfStock) (MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    textDecoration = TextDecoration.None
                                ))
                                else (MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textDecoration = TextDecoration.None
                                )),
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                }

                // Tins
                if (filteredTins.isNotEmpty() && (showTins || (searchPerformed && isTinSearch))) { //item.tins.isNotEmpty()
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
                            Column(
                                modifier = Modifier
                                    .background(
                                        LocalCustomColors.current.sheetBox,
                                        RoundedCornerShape(bottomStart = 8.dp)
                                    )
                                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                val tins = item.tins.filter { it in filteredTins }
                                tins.forEach {
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
                                                val quantity = formatDecimal(it.tinQuantity)
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
                    }
                }
            }

            if (showMenu) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(0.dp)
                ) {
                    ItemMenu(
                        searchPerformed = searchPerformed,
                        getPositionTrigger = { getPositionTrigger() },
                        item = item,
                        onMenuDismiss = { onMenuDismiss() },
                        onEditClick = { onEditClick(it) },
                        modifier = Modifier
                            .height(54.dp)
                    )
                }
            }
        }
    }

    Spacer(Modifier
        .height(1.dp)
        .background(LocalCustomColors.current.backgroundVariant)
    )
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
    visibilityMap: Map<TableColumn, Boolean>,
    columnVisibilityEnablement: Map<TableColumn, Boolean>,
    onVisibilityChange: (TableColumn, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = TableColumn.entries.filter { it != TableColumn.BRAND && it != TableColumn.BLEND }

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
    sortedItems: List<ItemsComponentsAndTins>,
    columnState: LazyListState,
    typeGenreOption: TypeGenreOption,
    showTins: Boolean,
    filteredTins: List<Tins>,
    formattedQty: Map<Int, String>,
    searchPerformed: Boolean,
    isTinSearch: Boolean,
    searchFocused: Boolean,
    onDetailsClick: (Items) -> Unit,
    onEditClick: (Items) -> Unit,
    getPositionTrigger: () -> Unit,
    activeMenuId: Int?,
    onShowMenu: (Int) -> Unit,
    isMenuShown: Boolean,
    onDismissMenu: () -> Unit,
    sorting: TableSorting,
    updateSorting: (Int) -> Unit,
    columnVisibility: Map<TableColumn, Boolean>,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp }
    val horizontalScroll = rememberScrollState()

    val columnOrder = listOf(
        TableColumn.BRAND,
        TableColumn.BLEND,
        TableColumn.TYPE,
        TableColumn.SUBGENRE,
        TableColumn.RATING,
        TableColumn.FAV_DIS,
        TableColumn.NOTE,
        TableColumn.QTY
    )

    val columnMinWidths = columnOrder.map {
        val visible = columnVisibility[it] ?: true
        if (visible) {
            when (it) {
                TableColumn.BRAND -> 180.dp
                TableColumn.BLEND -> 300.dp
                TableColumn.TYPE -> 108.dp
                TableColumn.SUBGENRE -> 120.dp
                TableColumn.RATING -> 64.dp
                TableColumn.FAV_DIS -> 64.dp
                TableColumn.NOTE -> 64.dp
                TableColumn.QTY -> 98.dp
            }
        } else {
            0.dp
        }
    }

    val fallbackType = typeGenreOption == TypeGenreOption.TYPE_FALLBACK && columnVisibility[TableColumn.SUBGENRE] == false
    val fallbackGenre = typeGenreOption == TypeGenreOption.SUB_FALLBACK && columnVisibility[TableColumn.TYPE] == false

    val columnMapping = columnOrder.map {
        when (it) {
            TableColumn.BRAND -> { item: Items -> item.brand }
            TableColumn.BLEND -> { item: Items -> item.blend }
            TableColumn.TYPE -> { item: Items -> item.type.ifBlank { if (fallbackType) item.subGenre else "" } }
            TableColumn.SUBGENRE -> { item: Items -> item.subGenre.ifBlank { if (fallbackGenre) item.type else "" } }
            TableColumn.RATING -> { item: Items -> item.rating }
            TableColumn.FAV_DIS -> { item: Items ->
                when {
                    item.favorite -> 1
                    item.disliked -> 2
                    else -> 0
                }
            }

            TableColumn.NOTE -> { item: Items -> item.notes }
            TableColumn.QTY -> { item: Items -> item.id }
        }
    }

    val focusManager = LocalFocusManager.current
    var tableWidth by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .onLayoutRectChanged{
                    tableWidth = it.width.dp
                },
            horizontalAlignment = Alignment.CenterHorizontally
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
                            3 -> Alignment.Center // subgenre
                            4 -> Alignment.Center // rating
                            5 -> Alignment.Center // fav/dis
                            6 -> Alignment.Center // notes
                            7 -> Alignment.Center // quantity
                            else -> Alignment.CenterStart
                        }
                        val headerText = when (columnIndex) {
                            0 -> "Brand"
                            1 -> "Blend"
                            2 -> "Type"
                            3 -> "Subgenre"
                            4 -> "" // rating
                            5 -> "" // favorite/dislike
                            6 -> "Note"
                            7 -> "Qty"
                            else -> ""
                        }
                        val onSortChange: (Int) -> Unit = { newSortColumn: Int ->
                            updateSorting(newSortColumn)
                            shouldScrollUp()
                        }
                        val headerOverride = if (columnMinWidths[columnIndex] == 0.dp) { "" } else { headerText }
                        when (columnIndex) {
                            0, 1, 2, 3, 4, 7 -> { // sortable columns
                                HeaderCell(
                                    text = headerOverride,
                                    onClick = {
                                        if (searchFocused) {
                                            focusManager.clearFocus()
                                        } else {
                                            if (isMenuShown && activeMenuId != null) {
                                                onDismissMenu()
                                            } else {
                                                onSortChange(columnIndex)
                                            }
                                        }
                                    },
                                    primarySort = sorting.columnIndex == columnIndex,
                                    sorting = sorting,
                                    icon1 = if (columnIndex == 4)
                                        painterResource(id = R.drawable.star_filled) else null,
                                    color1 = LocalCustomColors.current.starRating,
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .matchParentSize()
                                        .align(alignment),
                                    contentAlignment = alignment
                                )
                            } // sortable columns
                            else -> { // not sortable 5-6
                                HeaderCell(
                                    text = headerOverride,
                                    primarySort = sorting.columnIndex == columnIndex,
                                    sorting = sorting,
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .align(alignment),
                                    icon1 = if (columnIndex == 5)
                                        painterResource(id = R.drawable.heart_filled_24) else null,
                                    color1 = LocalCustomColors.current.favHeart,
                                    size1 = 20.dp,
                                    icon2 = if (columnIndex == 5)
                                        painterResource(id = R.drawable.question_mark_24) else null,
                                    color2 = Color.Black,
                                    size2 = 12.dp,
                                    contentAlignment = alignment
                                )
                            } // not sortable
                        }
                    }
                }
            }

            // Items
            val haptics = LocalHapticFeedback.current

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                state = columnState
            ) {
                items(items = sortedItems, key = { it.items.id }) { item ->
                    val showMenu = isMenuShown && activeMenuId == item.items.id
                    BackHandler(isMenuShown && activeMenuId == item.items.id) { onDismissMenu() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(intrinsicSize = IntrinsicSize.Min)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Box {
                            // item
                            Row(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            if (searchFocused) {
                                                focusManager.clearFocus()
                                            } else {
                                                if (isMenuShown && activeMenuId == item.items.id) {
                                                    // do nothing
                                                } else {
                                                    if (isMenuShown) {
                                                        onDismissMenu()
                                                    } else {
                                                        if (!searchPerformed) {
                                                            getPositionTrigger()
                                                        }
                                                        onDetailsClick(item.items)
                                                    }
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                            )
                                            if (!searchPerformed) {
                                                getPositionTrigger()
                                            }
                                            onShowMenu(item.items.id)
                                        },
                                        indication = null,
                                        interactionSource = null
                                    )
                            ) {
                                for (columnIndex in columnMinWidths.indices) {
                                    Box(
                                        modifier = Modifier
                                            .width(columnMinWidths[columnIndex])
                                            .fillMaxHeight()
                                            .align(Alignment.CenterVertically)
                                            .border(
                                                Dp.Hairline,
                                                color = LocalCustomColors.current.tableBorder
                                            )
                                    ) {
                                        val cellValue = columnMapping[columnIndex](item.items)
                                        val alignment = when (columnIndex) {
                                            0 -> Alignment.CenterStart // brand
                                            1 -> Alignment.CenterStart // blend
                                            2 -> Alignment.Center // type
                                            3 -> Alignment.Center // subgenre
                                            4 -> Alignment.Center // rating
                                            5 -> Alignment.Center // fav/dis
                                            6 -> Alignment.Center // notes
                                            7 -> Alignment.Center // quantity
                                            else -> Alignment.CenterStart
                                        }
                                        when (columnIndex) {
                                            0, 1, 2, 3, 4 -> { // brand, blend, type, rating
                                                TableCell(
                                                    value = cellValue,
                                                    modifier = Modifier
                                                        .align(alignment),
                                                    contentAlignment = alignment,
                                                )
                                            } // brand, blend, type, subgenre, rating
                                            5 -> { // fav/disliked
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
                                            6 -> { // notes
                                                if (cellValue != "") {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.notes_24),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .align(alignment),
                                                        colorFilter = ColorFilter.tint(
                                                            MaterialTheme.colorScheme.tertiary
                                                        ),
                                                    )
                                                } else {
                                                    TableCell(
                                                        value = "",
                                                        contentAlignment = alignment,
                                                    )
                                                }
                                            } // notes
                                            7 -> { // quantity
                                                val formattedQuantity = formattedQty[item.items.id] ?: "--"
                                                val zeroQuantity = Regex("[1-9]")
                                                val outOfStock = !formattedQuantity.contains(zeroQuantity)

                                                TableCell(
                                                    value = formattedQuantity,
                                                    modifier = Modifier
                                                        .align(alignment),
                                                    contentAlignment = alignment,
                                                    color = if (outOfStock) MaterialTheme.colorScheme.error
                                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                                )
                                            } // quantity
                                            else -> {
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
                            if (showMenu) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding(0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                       //     .background(LocalCustomColors.current.listMenuScrim),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val currentScrollOffset = horizontalScroll.value
                                        val switch = screenWidth.dp >= tableWidth
                                        val width = if (switch) tableWidth else screenWidth.dp

                                        Box (
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(width)
                                                .offset {
                                                    IntOffset(
                                                        x = if (!switch) currentScrollOffset else 0,
                                                        y = 0
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ItemMenu(
                                                searchPerformed = searchPerformed,
                                                getPositionTrigger = { getPositionTrigger() },
                                                item = item,
                                                onMenuDismiss = { onDismissMenu() },
                                                onEditClick = { onEditClick(it) },
                                                modifier = Modifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // tins
                    if (item.tins.isNotEmpty() && (showTins || (searchPerformed && isTinSearch))) {
                        Row(
                            modifier = Modifier
                                .width(814.dp)
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        LocalCustomColors.current.sheetBox,
                                        RoundedCornerShape(bottomStart = 8.dp)
                                    )
                                    .padding(
                                        start = 12.dp,
                                        top = 4.dp,
                                        bottom = 4.dp,
                                        end = 8.dp
                                    )
                                    .fillMaxWidth()
                            ) {
                                val tins =
                                    item.tins.filter { it in filteredTins }
                                tins.forEach {
                                    Row(
                                        modifier = Modifier,
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
                                        if (tins.indexOf(it) != tins.lastIndex) {
                                            Text(
                                                text = ", ",
                                                modifier = Modifier,
                                                fontWeight = FontWeight.Normal,
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
    }
}


@Stable
@Composable
fun HeaderCell(
    sorting: TableSorting,
    primarySort: Boolean,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon1: Painter? = null,
    color1: Color = LocalContentColor.current,
    size1: Dp = 20.dp,
    icon2: Painter? = null,
    color2: Color = LocalContentColor.current,
    size2: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    contentAlignment: Alignment = Alignment.Center,
) {
    Box(
        modifier = modifier
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
                indication = null, // LocalIndication.current
                interactionSource = null
            )
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = contentAlignment
    ) {
        if (primarySort) {
            Box(contentAlignment = Alignment.Center) {
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
                        colorFilter = ColorFilter.tint(color1),
                        modifier = Modifier
                            .size(size1)
                    )
                }
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
                    colorFilter = ColorFilter.tint(color1),
                    modifier = Modifier
                        .size(size1)
                )
            }
            if (icon2 != null) {
                Image(
                    painter = icon2,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color2),
                    modifier = Modifier
                        .size(size2)
                        .offset(x = 0.dp, y = 0.dp)
                )
            }
        }
    }
}


@Stable
@Composable
fun TableCell(
    value: Any?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    contentAlignment: Alignment = Alignment.Center,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Box(
        modifier = modifier
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
