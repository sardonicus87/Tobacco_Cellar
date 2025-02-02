package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object EditEntryDestination : NavigationDestination {
    override val route = "edit_entry_title"
    override val titleRes = R.string.edit_entry_title
    @Suppress("ConstPropertyName")
    const val itemsIdArg = "itemsId"
    val routeWithArgs = "$route/{$itemsIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: EditEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val filterViewModel = LocalCellarApplication.current.filterViewModel

//    LaunchedEffect(key1 = viewModel.itemUiState) {
//        viewModel.updateUiState(viewModel.itemUiState.itemDetails)
//    }

    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(EditEntryDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp,
                showMenu = false,
                modifier = Modifier
                    .noRippleClickable(onClick = { focusManager.clearFocus() })
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
                itemUiState = viewModel.itemUiState,
                componentUiState = viewModel.componentList,
                tinDetails = viewModel.tinDetailsState,
                tinDetailsList = viewModel.tinDetailsList,
                syncedTins = viewModel.calculateSyncTins(),
                existState = ExistState(),
                tinConversion = viewModel.tinConversion.value,
                onItemValueChange = viewModel::updateUiState,
                onTinValueChange = viewModel::updateTinDetails,
                onComponentChange = viewModel::updateComponentList,
                onTinConverterChange = viewModel::updateTinConversion,
                addTin = viewModel::addTin,
                removeTin = viewModel::removeTin,
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
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxSize()
            )
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

