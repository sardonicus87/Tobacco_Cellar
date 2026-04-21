package com.sardonicus.tobaccocellar.ui.addEditItems

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    canNavigateBack: Boolean = true,
    isLargeScreen: Boolean = false,
    viewModel: AddEntryViewModel = viewModel(),
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
                title = stringResource(R.string.add_entry_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                modifier = Modifier,
                navigateUp = onNavigateUp,
                showMenu = false,
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
            AddEntryBody(
                isLargeScreen = { isLargeScreen },
                selectedTabIndex = { selectedTabIndex },
                currentLeftTab = { currentLeftTab },
                updateSelectedTab = viewModel::updateSelectedTab,
                itemUiState = viewModel.itemUiState,
                componentUiState = viewModel.componentList,
                flavoringUiState = viewModel.flavoringList,
                tinDetailsList = viewModel.tinDetailsList,
                tabErrorState = viewModel.tabErrorState,
                syncedTins = viewModel.calculateSyncTins(),
                existState = viewModel.existState,
                resetExistState = viewModel::resetExistState,
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
                            viewModel.checkItemExistsOnSave()
                            if (!viewModel.existState.exists) {
                                viewModel.saveItem()
                                navigateBack()
                            }
                        }
                    }
                },
                onDeleteClick = { },
                isEditEntry = false,
                validateDates = { _, _, _ -> Triple(true, true, true) },
                navigateToEditEntry = navigateToEditEntry,
                modifier = modifier
                    .padding(0.dp)
                    .fillMaxSize(),
            )
        }
    }
}


@Composable
fun AddEntryBody(
    isLargeScreen: () -> Boolean,
    selectedTabIndex: () -> Int,
    currentLeftTab: () -> Int,
    updateSelectedTab: (Int) -> Unit,
    itemUiState: ItemUiState,
    componentUiState: ComponentList,
    flavoringUiState: FlavoringList,
    tinDetailsList: List<TinDetails>,
    tabErrorState: TabErrorState,
    syncedTins: Int,
    existState: ExistState,
    onItemValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    onComponentChange: (String) -> Unit,
    onFlavoringChange: (String) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    showRatingPop: Boolean,
    onShowRatingPop: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isEditEntry: Boolean,
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
    modifier: Modifier = Modifier,
    navigateToEditEntry: (Int) -> Unit = {},
    resetExistState: () -> Unit = {}
) {
    var deleteConfirm by rememberSaveable { mutableStateOf(false) }
    var anythingFocused by remember { mutableStateOf(false) }
    val updateFocused: (Boolean) -> Unit = { anythingFocused = it }
    val focusManager = LocalFocusManager.current
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    BackHandler(enabled = anythingFocused) { focusManager.clearFocus() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { updateFocused(it.hasFocus) }
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ItemInputForm(
            isLargeScreen = isLargeScreen,
            selectedTabIndex = selectedTabIndex,
            currentLeftTab = currentLeftTab,
            updateSelectedTab = updateSelectedTab,
            itemDetails = itemUiState.itemDetails,
            itemUiState = itemUiState,
            componentUiState = componentUiState,
            flavoringUiState = flavoringUiState,
            tinDetailsList = tinDetailsList,
            tabErrorState = tabErrorState,
            syncedTins = syncedTins,
            onValueChange = onItemValueChange,
            onTinValueChange = onTinValueChange,
            isTinLabelValid = isTinLabelValid,
            onComponentChange = onComponentChange,
            onFlavoringChange = onFlavoringChange,
            addTin = addTin,
            removeTin = removeTin,
            showRatingPop = showRatingPop,
            onShowRatingPop = onShowRatingPop,
            isEditEntry = isEditEntry,
            validateDates = validateDates,
            modifier = Modifier
                .weight(1f)
        )
        Column(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = if (!isLargeScreen() && landscape) 24.dp else 40.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onSaveClick() },
                enabled = itemUiState.isEntryValid,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(text = if (!isEditEntry) stringResource(R.string.save) else stringResource(R.string.update))
            }
            if (isEditEntry) {
                Button(
                    onClick = { deleteConfirm = true },
                    enabled = true,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonColors(
                        containerColor = LocalCustomColors.current.deleteButton,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.onErrorContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete_forever),
                        contentDescription = null
                    )
                    Text(text = stringResource(R.string.delete))
                }
                if (deleteConfirm) {
                    DeleteConfirmationDialog(
                        onDeleteConfirm = {
                            deleteConfirm = false
                            onDeleteClick()
                        },
                        onDeleteCancel = { deleteConfirm = false },
                        modifier = Modifier
                            .padding(0.dp)
                    )
                }
            } else { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (existState.existCheck) {
        ItemExistsDialog(
            onItemExistsConfirm = {
                resetExistState()
                navigateToEditEntry(existState.transferId)
            },
            onItemExistsCancel = { resetExistState() },
        )
    }
}


/** Dialogs **/
@Composable
fun ItemExistsDialog(
    onItemExistsConfirm: () -> Unit,
    onItemExistsCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.item_exists)) },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        dismissButton = {
            TextButton(onClick = onItemExistsCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onItemExistsConfirm) {
                Text(stringResource(R.string.yes))
            }
        }
    )
}

@Composable
fun ItemExistsEditDialog(
    onItemExistsConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.attention)) },
        text = {
            Text(
                text = "An entry already exists with this combination of Brand and Blend—the combination of Brand and Blend must be unique for each entry.",
                softWrap = true,
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        confirmButton = {
            TextButton(onClick = onItemExistsConfirm) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.delete_entry)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(stringResource(R.string.yes))
            }
        }
    )
}