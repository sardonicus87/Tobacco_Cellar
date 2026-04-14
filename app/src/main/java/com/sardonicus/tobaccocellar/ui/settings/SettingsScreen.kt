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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.TinSyncDefaultDialog
import com.sardonicus.tobaccocellar.ui.settings.displayDialogs.GlobalTwoPaneDialog
import com.sardonicus.tobaccocellar.ui.settings.displayDialogs.ParseLinksDialog
import com.sardonicus.tobaccocellar.ui.settings.displayDialogs.QuantityDialog
import com.sardonicus.tobaccocellar.ui.settings.displayDialogs.RatingsDialog
import com.sardonicus.tobaccocellar.ui.settings.displayDialogs.ThemeDialog
import com.sardonicus.tobaccocellar.ui.settings.displayDialogs.TypeGenreDialog

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
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 4.dp),
                fontSize = 18.sp
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 4.dp),
                fontSize = 18.sp
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
                .padding(8.dp, 6.dp)
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
                    fontSize = 15.sp,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 12.sp,
                        maxFontSize = 15.sp,
                        stepSize = 0.1.sp
                    ),
                    fontWeight = FontWeight.Medium,
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

            ThemeDialog(
                onDismiss = viewModel::dismissDialog,
                themeSetting = themeSetting,
                onThemeSelected = viewModel::saveThemeSetting
            )
        }
        DialogType.Ratings -> {
            val showRatings by viewModel.showRatings.collectAsState()

            RatingsDialog(
                onDismiss = viewModel::dismissDialog,
                showRatings = showRatings,
                onRatingsOption = viewModel::saveShowRatingOption,
                modifier = Modifier,
            )
        }
        DialogType.TypeGenre -> {
            val typeGenreOption by viewModel.typeGenreOption.collectAsState()
            val typeGenreEnablement by viewModel.typeGenreOptionEnablement.collectAsState()

            TypeGenreDialog(
                onDismiss = viewModel::dismissDialog,
                typeGenreOption = typeGenreOption,
                optionEnablement = typeGenreEnablement,
                onTypeGenreOption = viewModel::saveTypeGenreOption,
                modifier = Modifier
            )
        }
        DialogType.QuantityDisplay -> {
            val quantityOption by viewModel.quantityOption.collectAsState()

            QuantityDialog(
                onDismiss = viewModel::dismissDialog,
                quantityOption = quantityOption,
                onQuantityOption = viewModel::saveQuantityOption,
                modifier = Modifier
            )
        }
        DialogType.ParseLinks -> {
            val parseLinks by viewModel.parseLinks.collectAsState()

            ParseLinksDialog(
                onDismiss = viewModel::dismissDialog,
                parseLinks = parseLinks,
                onParseLinksOption = viewModel::saveParseLinksOption,
                modifier = Modifier
            )
        }
        DialogType.GlobalTwoPane -> {
            val globalTwoPane by viewModel.globalTwoPane.collectAsState()
            val twoColumnTabs by viewModel.twoColumnTabs.collectAsState()

            GlobalTwoPaneDialog(
                onDismiss = viewModel::dismissDialog,
                globalTwoPane = globalTwoPane,
                twoColumnTabs = twoColumnTabs,
                onGlobalTwoPane = viewModel::saveGlobalTwoPane,
                onTwoColumnTabs = viewModel::saveTwoColumnTabs,
                modifier = Modifier
            )
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

            TinSyncDefaultDialog(
                onDismiss = viewModel::dismissDialog,
                defaultSyncOption = defaultSyncOption,
                onDefaultSync = viewModel::setDefaultSyncOption,
                modifier = Modifier
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