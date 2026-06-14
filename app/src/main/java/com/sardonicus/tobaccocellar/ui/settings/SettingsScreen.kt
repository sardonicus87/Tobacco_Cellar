package com.sardonicus.tobaccocellar.ui.settings

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.BackupRestoreDialog
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.DbOperationsDialog
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.DeleteAllDialog
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.DeviceSyncDialog
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.TinRatesDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: SettingsViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarState by viewModel.snackbarState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val loading by viewModel.loading.collectAsState()


    if (snackbarState.show) {
        LaunchedEffect(snackbarState) {
            snackbarHostState.showSnackbar(
                message = snackbarState.message,
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
                viewModel.dismissDialog()
            }
        }
    }

    DialogManager(viewModel = viewModel)

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.settings_title),
                scrollBehavior = scrollBehavior,
                navigateUp = onNavigateUp,
                canNavigateBack = canNavigateBack,
                showMenu = false,
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
                            .padding(bottom = 20.dp)
                    )
                }
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.Top),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box {
                SettingsBody(
                    viewModel = viewModel,
                    modifier = modifier
                        .fillMaxSize(),
                )
                if (loading) {
                    LoadingIndicator(
                        scrimColor = Color.Black.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingsBody(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val displaySettings by viewModel.displaySettings.collectAsState()
    val databaseSettings by viewModel.databaseSettings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        item { Spacer(Modifier.height(20.dp)) }

        item {
            Text(
                text = "Display Settings",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 4.dp),
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(displaySettings, key = { it.title }) {
            SettingsRow(
                item = it,
                onClick = viewModel::showDialog,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        item {
            Text(
                text = "App & Database Settings",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 4.dp),
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(databaseSettings, key = { it.title }) {
            SettingsRow(
                item = it,
                onClick = viewModel::showDialog,
                color =
                    if (it.dialogType == DialogType.DeleteAll) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRow(
    item: SettingsDialog,
    onClick: (DialogType) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .indication(
                interactionSource = interactionSource,
                indication = if (item.currentSetting != null) LocalIndication.current else null
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.title,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    indication = if (item.currentSetting == null) LocalIndication.current else null,
                    interactionSource = interactionSource,
                    onClickLabel = item.description,
                ) { onClick(item.dialogType) }
                .padding(8.dp, 5.dp)
                .width(IntrinsicSize.Max),
            fontSize = 16.sp,
            maxLines = 1,
            color = color
        )

        if (item.currentSetting != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = interactionSource,
                        onClickLabel = item.description,
                    ) { onClick(item.dialogType) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ){
                Text(
                    text = item.currentSetting,
                    modifier = Modifier,
                    fontSize = 14.sp,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 11.sp,
                        maxFontSize = 14.sp,
                        stepSize = 0.1.sp
                    ),
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}


@Composable
private fun DialogManager(viewModel: SettingsViewModel) {
    val openDialog by viewModel.openDialog.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) {
        it?.let {
            viewModel.saveBackup(context, it)
        }
    }
    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) {
        it?.let {
            viewModel.restoreBackup(context, it)
        }
    }

    when (openDialog) {
        // Display settings
        DialogType.Theme -> {
            val themeSetting by viewModel.themeSetting.collectAsState()
            SelectionDialog(
                onDismiss = viewModel::dismissDialog,
                options = ThemeSetting.entries,
                selectedOption = themeSetting,
                onOptionSelected = { viewModel.saveThemeSetting(it.value) },
                optionLabel = { it.value }
            )
        }
        DialogType.Ratings -> {
            val showRatings by viewModel.showRatings.collectAsState()
            ToggleDialog(
                description = "Display ratings on cellar screen:",
                onDismiss = viewModel::dismissDialog,
                checked = showRatings,
                onCheckedChange = viewModel::saveShowRatingOption
            )
        }
        DialogType.TypeGenre -> {
            val typeGenreOption by viewModel.typeGenreOption.collectAsState()
            val typeGenreEnablement by viewModel.typeGenreOptionEnablement.collectAsState()
            SelectionDialog(
                description = "This option sets the display of Type, Subgenre or both on the " +
                        "Cellar screen. Fallback options display the option and if it's unused " +
                        "on an entry, fallback to the other (e.g. Subgenre (fallback) would " +
                        "display the subgenre but if an entry has no subgenre, will instead show " +
                        "the type value in parenthesis). This also affects table view when only " +
                        "one or the other of Type or Subgenre columns are shown.",
                onDismiss = viewModel::dismissDialog,
                options = TypeGenreOption.entries,
                selectedOption = typeGenreOption,
                onOptionSelected = { viewModel.saveTypeGenreOption(it.value) },
                optionLabel = { it.value },
                optionEnabled = { typeGenreEnablement[it] ?: false }
            )
        }
        DialogType.QuantityDisplay -> {
            val quantityOption by viewModel.quantityOption.collectAsState()
            SelectionDialog(
                description = "Displayed quantities for ounces and grams are based on the summed " +
                        "quantities of tins. If no tins are present, \"No. of Tins\" value will " +
                        "be converted and displayed with an asterisk.",
                onDismiss = viewModel::dismissDialog,
                options = QuantityOption.entries,
                selectedOption = quantityOption,
                onOptionSelected = { viewModel.saveQuantityOption(it.value) },
                optionLabel = { it.value }
            )
        }
        DialogType.ParseLinks -> {
            val parseLinks by viewModel.parseLinks.collectAsState()
            ToggleDialog(
                description = "Parse links in notes:",
                onDismiss = viewModel::dismissDialog,
                checked = parseLinks,
                onCheckedChange = viewModel::saveParseLinksOption
            )
        }
        DialogType.GlobalTwoPane -> {
            val globalTwoPane by viewModel.globalTwoPane.collectAsState()
            val landscapeTwoPane by viewModel.landscapeTwoPane.collectAsState()
            val twoColumnTabs by viewModel.twoColumnTabs.collectAsState()
            BaseSettingsDialog(
                onDismiss = viewModel::dismissDialog
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Adaptive layout options for large screens. The dual-pane and " +
                                "expand tabs are separate options (disabling one will not affect " +
                                "the other). \"Restrict to landscape\" affects both.",
                        modifier = Modifier.padding(bottom = 10.dp),
                        fontSize = 15.sp
                    )
                    ToggleRow("Dual pane layouts:", globalTwoPane, viewModel::saveGlobalTwoPane)
                    ToggleRow("Expand tabs to two columns:", twoColumnTabs, viewModel::saveTwoColumnTabs)
                    ToggleRow("Restrict to landscape only:", landscapeTwoPane, viewModel::saveLandscapeTwoPane)
                }
            }
        }

        // App/Database Settings
        DialogType.DeviceSync -> {
            val acknowledgement by viewModel.deviceSyncAcknowledgement.collectAsState()
            val email by viewModel.userEmail.collectAsState()
            val hasScope by viewModel.hasScope.collectAsState()
            val connectionEnabled by viewModel.networkEnabled.collectAsState()
            val deviceSync by viewModel.crossDeviceSync.collectAsState()
            val signingIn by viewModel.signingIn.collectAsState()
            val allowMobileData by viewModel.allowMobileData.collectAsState()

            if (!loading) {
                DeviceSyncDialog(
                    onDismiss = viewModel::dismissDialog,
                    acknowledgement = acknowledgement,
                    connectionEnabled = connectionEnabled,
                    confirmAcknowledgement = viewModel::saveCrossDeviceAcknowledged,
                    deviceSync = deviceSync,
                    signingIn = signingIn,
                    onDeviceSync = viewModel::saveCrossDeviceSync,
                    email = email,
                    hasScope = hasScope,
                    allowMobileData = allowMobileData,
                    onAllowMobileData = viewModel::saveAllowMobileData,
                    onManualSync = viewModel::manualSync,
                    clearRemoteData = viewModel::clearRemoteData,
                    clearLoginState = viewModel::clearLoginState,
                    modifier = Modifier
                )
            }
        }
        DialogType.TinRates -> {
            val ozRate by viewModel.tinOzConversionRate.collectAsState()
            val gramsRate by viewModel.tinGramsConversionRate.collectAsState()

            TinRatesDialog(
                onDismiss = viewModel::dismissDialog,
                ozRate = ozRate,
                gramsRate = gramsRate,
                onSave = viewModel::setTinConversionRates,
                modifier = Modifier
            )
        }
        DialogType.TinSyncDefault -> {
            val defaultSyncOption by viewModel.defaultSyncOption.collectAsState()
            ToggleDialog(
                description = "Set \"Sync Tins\" default on or off when adding new entries:",
                onDismiss = viewModel::dismissDialog,
                checked = defaultSyncOption,
                onCheckedChange = viewModel::setDefaultSyncOption
            )
        }
        DialogType.DbOperations -> {
            DbOperationsDialog(
                onDismiss = viewModel::dismissDialog,
                updateTinSync = viewModel::updateTinSync,
                optimizeDatabase = viewModel::optimizeDatabase,
                modifier = Modifier
            )
        }
        DialogType.BackupRestore -> {
            BackupRestoreDialog(
                onDismiss = viewModel::dismissDialog,
                onSave = {
                    launcher.launch(it)
                    viewModel.dismissDialog()
                },
                onRestore = {
                    openLauncher.launch(arrayOf("application/octet-stream"))
                    viewModel.dismissDialog()
                },
                viewmodel = viewModel,
                modifier = Modifier
            )
        }
        DialogType.DeleteAll -> {
            DeleteAllDialog(
                onDeleteConfirm = {
                    viewModel.deleteAllItems()
                    viewModel.dismissDialog()
                },
                onDeleteCancel = viewModel::dismissDialog,
                modifier = Modifier
                    .padding(0.dp)
            )
        }
        null -> { }
    }
}


@Composable
private fun BaseSettingsDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = content,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun <T> SelectionDialog(
    description: String? = null,
    onDismiss: () -> Unit,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    optionEnabled: (T) -> Boolean = { true }
) {
    BaseSettingsDialog(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (description != null) {
                Text(
                    text = description,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
            }
            options.forEach { option ->
                val enabled = optionEnabled(option)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(25))
                        .clickable(enabled = enabled) { onOptionSelected(option) }
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                ) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = null,
                        enabled = enabled,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = optionLabel(option),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .alpha(if (enabled) 1f else .38f),
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleDialog(
    description: String,
    onDismiss: () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    BaseSettingsDialog(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = description, fontSize = 15.sp, color = LocalContentColor.current)
            SwitchToggle(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SwitchToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val offAlpha = if (!checked) 1f else .5f
        val onAlpha = if (checked) 1f else .5f
        Text(
            text = "Off",
            fontSize = 14.sp,
            fontWeight = if (!checked) FontWeight.SemiBold else FontWeight.Normal,
            color = LocalContentColor.current.copy(alpha = offAlpha)
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .requiredHeight(20.dp)
                    .scale(.6f)
            )
        }
        Text(
            text = "On",
            fontSize = 14.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            color = LocalContentColor.current.copy(alpha = onAlpha)
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
            Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier
                .scale(.6f)
                .padding(start = 10.dp))
        }
    }
}