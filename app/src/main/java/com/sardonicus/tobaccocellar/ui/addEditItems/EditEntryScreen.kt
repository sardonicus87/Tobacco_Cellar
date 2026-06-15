package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    navigateBackSkip: () -> Unit,
    canNavigateBack: Boolean = true,
    twoColumnTabs: Boolean = false,
    viewModel: EditEntryViewModel = viewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val currentLeftTab by viewModel.currentLeftTab.collectAsState()
    val showRatingPop by viewModel.showRatingPop.collectAsState()

    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        onDispose {
            if (activity?.isChangingConfigurations == false) {
                focusManager.clearFocus()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.edit_entry_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                navigateUp = navigateBack,
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
                    twoColumnTabs = { twoColumnTabs },
                    selectedTabIndex = { selectedTabIndex },
                    currentLeftTab = { currentLeftTab },
                    updateSelectedTab = viewModel::updateSelectedTab,
                    itemUiState = viewModel.itemUiState,
                    autoComplete = viewModel.autoCompleteData,
                    tabErrorState = viewModel.tabErrorState,
                    existState = ExistState(),
                    onItemValueChange = viewModel::updateUiState,
                    onTinValueChange = viewModel::updateTinDetails,
                    addTin = viewModel::addTin,
                    removeTin = viewModel::removeTin,
                    showRatingPop = showRatingPop,
                    onShowRatingPop = viewModel::onShowRatingPop,
                    onSaveClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.Main) {
                                viewModel.checkItemExistsOnUpdate()
                                if (!viewModel.existState.value.exists) {
                                    viewModel.updateItem()
                                    navigateBack()
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        coroutineScope.launch {
                            viewModel.deleteItem()
                            navigateBackSkip()
                        }
                    },
                    isEditEntry = true,
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize()
                )
                if (viewModel.loading.value) {
                    LoadingIndicator(
                        scrimColor = Color.Black.copy(alpha = 0.33f),
                    )
                }
            }

            if (viewModel.existState.value.exists) {
                ItemExistsEditDialog(
                    onItemExistsConfirm = {
                        viewModel.resetExistState()
                    },
                )
            }
        }
    }
}

