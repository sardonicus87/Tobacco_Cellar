package com.sardonicus.tobaccocellar.ui.addEditItems

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.AutoCompleteData
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEntryScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    canNavigateBack: Boolean = true,
    twoColumnTabs: Boolean = false,
    viewModel: AddEntryViewModel = viewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val imeOpen = WindowInsets.isImeVisible
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val shouldCollapse = !twoColumnTabs && isLandscape && imeOpen
    val topBarHeight by animateDpAsState(if (shouldCollapse) 0.dp else 56.dp, tween(250))

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) { onDispose { focusManager.clearFocus() } }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() },
        topBar = {
            Box(Modifier
                .fillMaxWidth()
                .height(topBarHeight)
                .graphicsLayer { translationY = topBarHeight.toPx() - 56.dp.toPx() }
            ) {
                CellarTopAppBar(
                    title = stringResource(R.string.add_entry_title),
                    scrollBehavior = scrollBehavior,
                    canNavigateBack = canNavigateBack,
                    modifier = Modifier,
                    navigateUp = onNavigateUp,
                    showMenu = false,
                )
            }
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
                twoColumnTabs = { twoColumnTabs },
                itemUiState = viewModel.itemUiState,
                autoComplete = viewModel.autoCompleteData,
                tabErrorState = viewModel.tabErrorState,
                existState = viewModel.existState.value,
                resetExistState = viewModel::resetExistState,
                onItemValueChange = viewModel::updateUiState,
                onTinValueChange = viewModel::updateTinDetails,
                addTin = viewModel::addTin,
                removeTin = viewModel::removeTin,
                onSaveClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.Main) {
                            viewModel.checkItemExistsOnSave()
                            if (!viewModel.existState.value.exists) {
                                viewModel.saveItem(); navigateBack()
                            }
                        }
                    }
                },
                isEditEntry = false,
                navigateToEditEntry = navigateToEditEntry,
                focusManager = focusManager,
                modifier = modifier
                    .padding(0.dp)
                    .fillMaxSize(),
            )
        }
    }
}


@Composable
fun AddEntryBody(
    twoColumnTabs: () -> Boolean,
    itemUiState: ItemUiState,
    autoComplete: AutoCompleteData,
    tabErrorState: TabErrorState,
    existState: ExistState,
    onItemValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    onSaveClick: () -> Unit,
    isEditEntry: Boolean,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit = { },
    navigateToEditEntry: (Int) -> Unit = {},
    resetExistState: () -> Unit = {}
) {
    var deleteConfirm by remember { mutableStateOf(false) }
    var anythingFocused by remember { mutableStateOf(false) }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    BackHandler(enabled = anythingFocused) { focusManager.clearFocus() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { anythingFocused = it.hasFocus }
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ItemInputForm(
            twoColumnTabs = twoColumnTabs,
            itemDetails = itemUiState.itemDetails,
            tinDetailsList = itemUiState.itemDetails.tinDetailsList,
            autoComplete = autoComplete,
            tabErrorState = tabErrorState,
            onValueChange = onItemValueChange,
            onTinValueChange = onTinValueChange,
            addTin = addTin,
            removeTin = removeTin,
            isEditEntry = isEditEntry,
            focusManager = focusManager,
            modifier = Modifier
                .weight(1f)
        )
        Column(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = if (!twoColumnTabs() && landscape) 12.dp else 40.dp),
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete_forever),
                        contentDescription = null
                    )
                    Text(text = stringResource(R.string.delete))
                }
                if (deleteConfirm) {
                    DeleteConfirmationDialog(
                        onDeleteConfirm = { deleteConfirm = false; onDeleteClick() },
                        onDeleteCancel = { deleteConfirm = false },
                        modifier = Modifier.padding(0.dp)
                    )
                }
            } else { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (existState.exists) {
        ItemExistsDialog(
            onItemExistsConfirm = { resetExistState(); navigateToEditEntry(existState.transferId) },
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
        dismissButton = { TextButton(onItemExistsCancel) { Text(stringResource(R.string.cancel)) } },
        confirmButton = { TextButton(onItemExistsConfirm) { Text(stringResource(R.string.yes)) } }
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
                "An entry already exists with this combination of Brand and Blend (the " +
                        "combination of Brand and Blend must be unique for each entry).",
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        confirmButton = { TextButton(onItemExistsConfirm) { Text(stringResource(R.string.ok)) } }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.delete_entry)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        dismissButton = { TextButton(onDeleteCancel) { Text(stringResource(R.string.cancel)) } },
        confirmButton = { TextButton(onDeleteConfirm) { Text(stringResource(R.string.yes)) } }
    )
}