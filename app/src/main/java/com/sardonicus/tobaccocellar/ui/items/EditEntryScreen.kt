package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: EditEntryViewModel = viewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val showRatingPop by viewModel.showRatingPop.collectAsState()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLargeScreen by remember(windowSizeClass) { derivedStateOf { windowSizeClass.isAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND) } }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.edit_entry_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp,
                showMenu = false,
                modifier = Modifier
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
            Box {
                AddEntryBody(
                    isLargeScreen = { isLargeScreen },
                    selectedTabIndex = selectedTabIndex,
                    updateSelectedTab = viewModel::updateSelectedTab,
                    itemUiState = viewModel.itemUiState,
                    componentUiState = viewModel.componentList,
                    flavoringUiState = viewModel.flavoringList,
                //    tinDetails = viewModel.tinDetailsState,
                    tinDetailsList = viewModel.tinDetailsList,
                    tabErrorState = viewModel.tabErrorState,
                    syncedTins = viewModel.calculateSyncTins(),
                    existState = ExistState(),
                    onItemValueChange = viewModel::updateUiState,
                    onTinValueChange = viewModel::updateTinDetails,
                    onComponentChange = viewModel::updateComponentList,
                    onFlavoringChange = viewModel::updateFlavoringList,
                    addTin = viewModel::addTin,
                    removeTin = viewModel::removeTin,
                    showRatingPop = showRatingPop,
                    onShowRatingPop = viewModel::onShowRatingPop,
                    isTinLabelValid = viewModel::isTinLabelValid,
                    onSaveClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.Main) {
                                viewModel.checkItemExistsOnUpdate()
                                if (!viewModel.existState.exists) {
                                    viewModel.updateItem()
                                    navigateBack()
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        coroutineScope.launch {
                            viewModel.deleteItem()
                            navigateBack()
                        }
                    },
                    isEditEntry = true,
                    validateDates = viewModel::validateDates,
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize()
                )
                if (viewModel.loading) {
                    LoadingIndicator(
                        scrimColor = Color.Black.copy(alpha = 0.33f),
                    )
                }
            }

            if (viewModel.existState.existCheck) {
                ItemExistsEditDialog(
                    onItemExistsConfirm = {
                        viewModel.resetExistState()
                    },
                )
            }
        }
    }
}

