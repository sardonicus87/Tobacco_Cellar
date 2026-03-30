package com.sardonicus.tobaccocellar.ui.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarBottomAppBar
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.navigation.HomeDestination
import com.sardonicus.tobaccocellar.ui.settings.ChangelogEntryData
import com.sardonicus.tobaccocellar.ui.settings.changelogEntries
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.launch

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
    navigateToChangelog: (List<ChangelogEntryData>, Int?) -> Unit,
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val homeUiState by viewModel.homeUiState.collectAsState()

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

    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        onDispose {
            if (activity?.isChangingConfigurations == false) {
                viewModel.snackbarShown()
                viewModel.onDismissMenu()
            }
        }
    }

    HomeBackHandler(viewModel, filterViewModel)

    // Important Alert stuff
    val importantAlertState by viewModel.importantAlertState.collectAsState()
    if (importantAlertState.show) {
        ImportantAlertDialog(
            importantAlertState = importantAlertState,
            viewModel = viewModel
        )
    }

    // Release Notes
    val releaseNotesState by viewModel.releaseNotesState.collectAsState()

    if (releaseNotesState.show && !importantAlertState.show) {
        ReleaseNotesDialog(
            releaseNotesState = releaseNotesState,
            viewModel = viewModel,
            onNavigateToChangelog = {
                viewModel.saveReleaseNotesSeen()
                navigateToChangelog(changelogEntries, it)
            }
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
                filterViewModel = filterViewModel,
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
                shouldScrollUp = filterViewModel::shouldScrollUp,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(0.dp),
            )
        }
    }
}


@Composable
private fun HomeBackHandler(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
) {
    val menuState by viewModel.menuState.collectAsState()
    val searchState by filterViewModel.searchState.collectAsState()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler(menuState.activeMenuId != null) { viewModel.onDismissMenu() }

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
}