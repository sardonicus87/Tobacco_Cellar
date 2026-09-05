package com.sardonicus.tobaccocellar.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.sardonicus.tobaccocellar.ui.utilities.DismissSnackbar
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToBlendDetails: (Int) -> Unit,
    navigateToStats: () -> Unit,
    navigateToDates: () -> Unit,
    navigateToAddEntry: () -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    navigateToBulkEdit: () -> Unit,
    navigateToCsvImport: () -> Unit,
    navigateToPlaintext: () -> Unit,
    navigateToHelp: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToChangelog: (Int?) -> Unit,
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val homeUiState by viewModel.homeUiState.collectAsState()
    var showColumnMenu by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            EventBus.tryEmit(DismissSnackbar); showColumnMenu = false
            viewModel.onDismissMenu(); focusManager.clearFocus()
        }
    }

    HomeBackHandler(
        viewModel = viewModel,
        filterViewModel = filterViewModel,
        showColumnMenu = showColumnMenu,
        searchFocused = searchFocused,
        dismissColumnMenu = { showColumnMenu = false },
        clearFocus = { focusManager.clearFocus() }
    )

    // Important Alert stuff
    val importantAlertState by viewModel.importantAlertState.collectAsState()
    if (importantAlertState.show) { ImportantAlertDialog(importantAlertState, viewModel) }

    // Release Notes
    val releaseNotesState by viewModel.releaseNotesState.collectAsState()
    if (releaseNotesState.show && !importantAlertState.show) {
        ReleaseNotesDialog(releaseNotesState, viewModel) { viewModel.saveReleaseNotesSeen()
            navigateToChangelog(it) }
    }


    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(null, null) { focusManager.clearFocus(); viewModel.onDismissMenu() },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.home_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = false,
                navigateToBulkEdit = navigateToBulkEdit,
                navigateToCsvImport = navigateToCsvImport,
                navigateToPlaintext = navigateToPlaintext,
                navigateToHelp = navigateToHelp,
                navigateToAbout = navigateToAbout,
                navigateToSettings = navigateToSettings,
                showMenu = true,
                currentDestination = HomeDestination,
                exportCsvHandler = viewModel
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                navigateToDates = navigateToDates,
                navigateToStats = navigateToStats,
                navigateToAddEntry = navigateToAddEntry,
                currentDestination = HomeDestination
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
                onSearch = { viewModel.onDismissMenu(); filterViewModel.onSearch(it) },
                updateSearchFocused = { searchFocused = it },
                getPositionTrigger = filterViewModel::getPositionTrigger,
                onShowColumnPop = { showColumnMenu = !showColumnMenu },
                saveListSorting = viewModel::saveListSorting,
                shouldScrollUp = filterViewModel::shouldScrollUp,
                modifier = Modifier,
            )
            HomeBody(
                viewModel = viewModel,
                filterViewModel = filterViewModel,
                showLoading = homeUiState.isLoading,
                isTableView = homeUiState.isTableView,
                columnMenu = { showColumnMenu },
                searchFocused = { searchFocused },
                showColumnMenuToggle = { showColumnMenu = !showColumnMenu },
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
    showColumnMenu: Boolean,
    searchFocused: Boolean,
    dismissColumnMenu: () -> Unit,
    clearFocus: () -> Unit,
) {
    val menuState by viewModel.menuState.collectAsState()
    val searchState by filterViewModel.searchState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    BackHandler(searchFocused || searchState.searchPerformed) {
        if (searchFocused) { clearFocus() }
        else {
            filterViewModel.updateSearchText(""); filterViewModel.onSearch("")
            if (searchState.searchPerformed) {
                coroutineScope.launch { EventBus.emit(SearchClearedEvent) }
            }
        }
    }

    BackHandler(menuState.activeMenuId != null) { viewModel.onDismissMenu() }

    BackHandler(showColumnMenu) { dismissColumnMenu() }
}