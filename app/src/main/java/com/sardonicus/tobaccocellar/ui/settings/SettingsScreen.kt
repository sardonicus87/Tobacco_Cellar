package com.sardonicus.tobaccocellar.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.BuildConfig
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.CheckboxWithLabel
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    navigateToChangelog: (List<ChangelogEntryData>) -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: SettingsViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val ozRate by viewModel.tinOzConversionRate.collectAsState()
    val gramsRate by viewModel.tinGramsConversionRate.collectAsState()
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

    DisposableEffect(Unit) {
        onDispose {
            viewModel.snackbarShown()
            viewModel.dismissDialog()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() }
        ,
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
                    navigateToChangelog = { navigateToChangelog(it) },
                    tinOzConversionRate = ozRate,
                    tinGramsConversionRate = gramsRate,
                    updateTinSync = { viewModel.updateTinSync() },
                    optimizeDatabase = { viewModel.optimizeDatabase() },
                    onDeleteAllClick = {
                        coroutineScope.launch {
                            viewModel.deleteAllItems()
                        }
                    },
                    loading = loading,
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
    loading: Boolean,
    navigateToChangelog: (List<ChangelogEntryData>) -> Unit,
    tinOzConversionRate: Double,
    tinGramsConversionRate: Double,
    updateTinSync: () -> Unit,
    optimizeDatabase: () -> Unit,
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

    val openDialog by viewModel.openDialog.collectAsState()
    val connectionEnabled by viewModel.networkEnabled.collectAsState()

    val themeSetting by viewModel.themeSetting.collectAsState()
    val showRatings by viewModel.showRatings.collectAsState()
    val typeGenreOption by viewModel.typeGenreOption.collectAsState()
    val typeGenreEnablement by viewModel.typeGenreOptionEnablement.collectAsState()
    val quantityOption by viewModel.quantityOption.collectAsState()
    val parseLinks by viewModel.parseLinks.collectAsState()

    val acknowledgement by viewModel.deviceSyncAcknowledgement.collectAsState()
    val deviceSync by viewModel.crossDeviceSync.collectAsState()
    val email by viewModel.userEmail.collectAsState()
    val hasScope by viewModel.hasScope.collectAsState()
    val allowMobileData by viewModel.allowMobileData.collectAsState()
    val defaultSyncOption by viewModel.defaultSyncOption.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(0.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        item { Spacer(Modifier.height(16.dp)) }

        item {
            DisplaySettings(
                viewModel = viewModel,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .75f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        LocalCustomColors.current.darkNeutral.copy(alpha = .75f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )
        }

        item {
            DatabaseSettings(
                viewModel = viewModel,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .75f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        LocalCustomColors.current.darkNeutral.copy(alpha = .75f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )
        }

        item {
            AboutSection(
                navigateToChangelog = { navigateToChangelog(it) },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .75f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        LocalCustomColors.current.darkNeutral.copy(alpha = .75f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )
        }
    }

    when (openDialog) {
        // Display settings
        DialogType.Theme -> {
            ThemeDialog(
                onDismiss = { viewModel.dismissDialog() },
                themeSetting = themeSetting,
                onThemeSelected = { viewModel.saveThemeSetting(it) }
            )
        }
        DialogType.Ratings -> {
            RatingsDialog(
                onDismiss = { viewModel.dismissDialog() },
                showRatings = showRatings,
                onRatingsOption = { viewModel.saveShowRatingOption(it) },
                modifier = Modifier,
            )
        }
        DialogType.TypeGenre -> {
            TypeGenreDialog(
                onDismiss = { viewModel.dismissDialog() },
                typeGenreOption = typeGenreOption,
                optionEnablement = typeGenreEnablement,
                onTypeGenreOption = { viewModel.saveTypeGenreOption(it) },
                modifier = Modifier
            )
        }
        DialogType.QuantityDisplay -> {
            QuantityDialog(
                onDismiss = { viewModel.dismissDialog() },
                quantityOption = quantityOption,
                onQuantityOption = { viewModel.saveQuantityOption(it) },
                modifier = Modifier
            )
        }
        DialogType.ParseLinks -> {
            ParseLinksDialog(
                onDismiss = { viewModel.dismissDialog() },
                parseLinks = parseLinks,
                onParseLinksOption = { viewModel.saveParseLinksOption(it) },
                modifier = Modifier
            )
        }

        // App/Database Settings
        DialogType.DeviceSync -> {
            if (!loading) {
                DeviceSyncDialog(
                    onDismiss = { viewModel.dismissDialog() },
                    acknowledgement = acknowledgement,
                    connectionEnabled = connectionEnabled,
                    confirmAcknowledgement = { viewModel.saveCrossDeviceAcknowledged() },
                    deviceSync = deviceSync,
                    onDeviceSync = { viewModel.saveCrossDeviceSync(it) },
                    email = email,
                    hasScope = hasScope,
                    allowMobileData = allowMobileData,
                    onAllowMobileData = { viewModel.saveAllowMobileData(it) },
                    onManualSync = { viewModel.manualSync() },
                    clearRemoteData = { viewModel.clearRemoteData() },
                    clearLoginState = { viewModel.clearLoginState() },
                    modifier = Modifier
                )
            }
        }
        DialogType.TinRates -> {
            TinRatesDialog(
                onDismiss = { viewModel.dismissDialog() },
                ozRate = tinOzConversionRate,
                gramsRate = tinGramsConversionRate,
                onSave = { ozRate, gramsRate ->
                    viewModel.setTinConversionRates(ozRate, gramsRate)
                    viewModel.dismissDialog()
                },
                modifier = Modifier
            )
        }
        DialogType.TinSyncDefault -> {
            TinSyncDefaultDialog(
                onDismiss = { viewModel.dismissDialog() },
                defaultSyncOption = defaultSyncOption,
                onDefaultSync = { viewModel.setDefaultSyncOption(it) },
                modifier = Modifier
            )
        }
        DialogType.DbOperations -> {
            DbOperationsDialog(
                onDismiss = { viewModel.dismissDialog() },
                updateTinSync = updateTinSync,
                optimizeDatabase = optimizeDatabase,
                modifier = Modifier
            )
        }
        DialogType.BackupRestore -> {
            BackupRestoreDialog(
                onDismiss = { viewModel.dismissDialog() },
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
                    onDeleteAllClick()
                    viewModel.dismissDialog()
                },
                onDeleteCancel = { viewModel.dismissDialog() },
                modifier = Modifier
                    .padding(0.dp)
            )
        }
        null -> { }
    }
}


@Composable
fun DisplaySettings(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Display Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 3.dp),
            fontSize = 16.sp
        )
        viewModel.displaySettings.forEach {
            SettingsButton(
                text = it.title,
                onClick = { viewModel.showDialog(it.dialogType) }
            )
        }
    }
}

@Composable
fun DatabaseSettings(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "App & Database Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 3.dp),
            fontSize = 16.sp
        )
        viewModel.databaseSettings.forEach {
            val color = if (it.dialogType == DialogType.DeleteAll) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary

            SettingsButton(
                text = it.title,
                onClick = { viewModel.showDialog(it.dialogType) },
                color = color
            )
        }
    }
}

@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(8.dp, 3.dp),
        modifier = modifier
            .heightIn(28.dp, 28.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier,
            fontSize = 15.sp,
            color = color
        )
    }
}


@Composable
fun AboutSection(
    navigateToChangelog: (List<ChangelogEntryData>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appVersion = BuildConfig.VERSION_NAME
    val dbVersion = TobaccoDatabase.getDatabaseVersion(LocalContext.current).toString()

    val contactString = buildAnnotatedString {
        val emailStart = length
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Normal,
            textDecoration = TextDecoration.Underline
        )) { append("sardonicus.notadev@gmail.com") }
        val emailEnd = length
        addStringAnnotation(
            tag = "Email",
            annotation = "mailto:sardonicus.notadev@gmail.com",
            start = emailStart,
            end = emailEnd
        )
    }

    val versionInfo = buildAnnotatedString {
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold)
        ) { append("App Version: ") }
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Medium)
        ) { append(appVersion) }
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold)
        ) { append("\nDatabase Version: ") }
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Medium)
        ) { append(dbVersion) }
    }


    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "About Tobacco Cellar",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 4.dp),
            fontSize = 16.sp
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Cobbled together by Sardonicus using Kotlin and Jetpack Compose. " +
                        "Uses Apache Commons CSV for reading and writing CSV files.",
                modifier = Modifier
                    .padding(vertical = 2.dp),
                fontSize = 14.sp,
                softWrap = true,
            )
            FlowRow(
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = "Contact me if you experience any bugs: ",
                    modifier = Modifier,
                    fontSize = 14.sp,
                    softWrap = true,
                )
                SelectionContainer {
                    Text(
                        text = contactString,
                        modifier = Modifier
                            .clickable(
                                interactionSource = null,
                                indication = LocalIndication.current,
                            ) {
                                val annotations =
                                    contactString.getStringAnnotations("Email", 0, contactString.length)
                                annotations
                                    .firstOrNull()
                                    ?.let {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = it.item.toUri()
                                            putExtra(Intent.EXTRA_SUBJECT, "Tobacco Cellar Feedback")
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                emailIntent,
                                                "Send Email"
                                            )
                                        )
                                    }
                            },
                        fontSize = 14.sp,
                        softWrap = true,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = versionInfo,
                modifier = Modifier
                    .padding(top = 4.dp),
                fontSize = 14.sp,
                softWrap = true,
            )
            Text(
                text = "Changelog ",
                modifier = Modifier
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { navigateToChangelog(changelogEntries) }
                    .padding(vertical = 1.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


/** Display Settings Dialogs **/
@Composable
fun ThemeDialog(
    themeSetting: ThemeSetting,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column (
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                ThemeSetting.entries.forEach {
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { onThemeSelected(it.value) }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                    ) {
                        RadioButton(
                            selected = themeSetting == it,
                            onClick = null,
                            modifier = Modifier
                                .size(36.dp)
                        )
                        Text(
                            text = it.value,
                            modifier = Modifier,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun RatingsDialog(
    onDismiss: () -> Unit,
    showRatings: Boolean,
    onRatingsOption: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Display ratings on cellar screen:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offAlpha = if (!showRatings) 1f else .5f
                    val onAlpha = if (showRatings) 1f else .5f
                    Text(
                        text = "Off",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (!showRatings) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(alpha = offAlpha)
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = showRatings,
                            onCheckedChange = { onRatingsOption(it) },
                            modifier = Modifier
                                .requiredHeight(20.dp)
                                .scale(.6f),
                            colors = SwitchDefaults.colors(
                            )
                        )
                    }
                    Text(
                        text = "On",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (showRatings) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(onAlpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun TypeGenreDialog(
    onDismiss: () -> Unit,
    typeGenreOption: TypeGenreOption,
    optionEnablement: Map<TypeGenreOption, Boolean>,
    onTypeGenreOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "This option sets the display of Type, Subgenre or both in list view. " +
                            "Fallback options displays the option and if it's unused on an entry, " +
                            "fallback to the other (in parentheses).",
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
                TypeGenreOption.entries.forEach {
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = optionEnablement[it] ?: false
                            ) { onTypeGenreOption(it.value) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                    ) {
                        val alpha = if (optionEnablement[it] == true) 1f else .38f
                        RadioButton(
                            selected = typeGenreOption == it,
                            onClick = null,
                            enabled = optionEnablement[it] ?: false,
                            modifier = Modifier
                                .size(36.dp)
                        )
                        Text(
                            text = it.value,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .alpha(alpha),
                            fontSize = 15.sp,

                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun QuantityDialog(
    onDismiss: () -> Unit,
    quantityOption: QuantityOption,
    onQuantityOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column (
                modifier = modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "Displayed quantities for ounces and grams are based on the summed " +
                            "quantities of tins. If no tins are present, \"No. of Tins\" will " +
                            "value will be converted and displayed with an asterisk.",
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    fontSize = 14.sp,
                    color = LocalContentColor.current
                )
                QuantityOption.entries.forEach {
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { onQuantityOption(it.value) }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                    ) {
                        RadioButton(
                            selected = quantityOption == it,
                            onClick = null,
                            modifier = Modifier
                                .size(36.dp)
                        )
                        Text(
                            text = it.value,
                            modifier = Modifier,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun ParseLinksDialog(
    onDismiss: () -> Unit,
    parseLinks: Boolean,
    onParseLinksOption: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Parse links in notes:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offAlpha = if (!parseLinks) 1f else .5f
                    val onAlpha = if (parseLinks) 1f else .5f
                    Text(
                        text = "Off",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (!parseLinks) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(alpha = offAlpha)
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = parseLinks,
                            onCheckedChange = { onParseLinksOption(it) },
                            modifier = Modifier
                                .requiredHeight(20.dp)
                                .scale(.6f),
                            colors = SwitchDefaults.colors(
                            )
                        )
                    }
                    Text(
                        text = "On",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (parseLinks) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(onAlpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}


/** App/Database Settings Dialogs **/
@Composable
fun DeviceSyncDialog(
    onDismiss: () -> Unit,
    acknowledgement: Boolean,
    connectionEnabled: Boolean,
    confirmAcknowledgement: () -> Unit,
    deviceSync: Boolean,
    onDeviceSync: (Boolean) -> Unit,
    email: String?,
    hasScope: Boolean,
    allowMobileData: Boolean,
    onAllowMobileData: (Boolean) -> Unit,
    onManualSync: () -> Unit,
    clearRemoteData: () -> Unit,
    clearLoginState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accountLinked by remember (email, hasScope) { mutableStateOf(!email.isNullOrBlank() || hasScope) }

    val scrollState = rememberScrollState()
    val atBottom by remember(scrollState.canScrollForward) { mutableStateOf(!scrollState.canScrollForward) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp)
            .heightIn(max = 350.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!acknowledgement) {
                    Text(
                        text = "About Multi Device Sync",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalContentColor.current
                    )
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "(You must scroll to the bottom to accept)",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            fontSize = 13.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "To auto synchronize collection changes across devices, you " +
                                    "must enable this option and sign-in with the same Google " +
                                    "account to authorize Google Drive access on each device (you " +
                                    "do not need the Google Drive app for this functionality to " +
                                    "work). This feature also requires all synced devices to be " +
                                    "running a version of the app with the same Database Version.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "I (developer), and any third parties will not have access to " +
                                    "your login or drive, this authorization just allows the app " +
                                    "to use your Google Drive as a cloud location for storing and " +
                                    "retrieving data changes between devices. The app will create " +
                                    "a hidden folder that only this app can access, and this " +
                                    "folder is the only part of your Drive that the app can " +
                                    "access. Login and remote sync data can be cleared at any " +
                                    "time in this setting dialog (clear remote data before " +
                                    "clearing login). If you wish to revoke Drive authorization, " +
                                    "this must be done in your Google Account settings: Services " +
                                    "> Connected Apps).",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Data does not count toward your Google Drive storage quota, " +
                                    "and is checked once at every app start, and cyclically once " +
                                    "every 12 hours as long as the device is powered on. The app " +
                                    "start check and 12-hour cycled downloads respect your " +
                                    "settings regarding mobile data or WIFI only.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Before enabling this option on multiple devices, it is " +
                                    "recommended to create a manual database backup of the " +
                                    "device with the most up-to-date data and transfer it to, " +
                                    "and restore on, the other device(s).",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        // Enable Sync
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Multi-Device Sync:",
                                modifier = Modifier,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = LocalContentColor.current
                            )
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                                Switch(
                                    checked = deviceSync,
                                    onCheckedChange = { onDeviceSync(it) },
                                    modifier = Modifier
                                        .scale(.6f)
                                        .padding(start = 10.dp),
                                    colors = SwitchDefaults.colors()
                                )
                            }
                        }

                        // Allow Mobile
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val alpha = if (!deviceSync) .38f else 1f
                            Text(
                                text = "Allow Mobile Data:",
                                modifier = Modifier,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = LocalContentColor.current.copy(alpha = alpha)
                            )
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                                Switch(
                                    checked = allowMobileData,
                                    onCheckedChange = { onAllowMobileData(it) },
                                    enabled = deviceSync,
                                    modifier = Modifier
                                        .scale(.6f)
                                        .padding(start = 10.dp),
                                    colors = SwitchDefaults.colors()
                                )
                            }
                        }

                        // Manual Sync
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                            ) {
                                TextButton(
                                    onClick = { onManualSync() },
                                    enabled = deviceSync && accountLinked && connectionEnabled,
                                    contentPadding = PaddingValues(8.dp, 3.dp),
                                    modifier = modifier
                                        .heightIn(28.dp, 28.dp)
                                ) {
                                    Text(
                                        text = "Manual Sync",
                                        modifier = Modifier,
                                        fontSize = 15.sp,
                                    )
                                }

                                // Clear remote data
                                TextButton(
                                    onClick = { clearRemoteData() },
                                    enabled = accountLinked && connectionEnabled,
                                    contentPadding = PaddingValues(8.dp, 3.dp),
                                    modifier = modifier
                                        .heightIn(28.dp, 28.dp)
                                ) {
                                    Text(
                                        text = "Clear Remote Data",
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            if (!connectionEnabled) {
                                Box (
                                    modifier = Modifier
                                        .width(IntrinsicSize.Min)
                                        .padding(end = 10.dp)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = "(Check connection)",
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 12.sp,
                                            maxFontSize = 14.sp,
                                        ),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier,
                                        maxLines = 2,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                                    )
                                }
                            }
                        }

                        // Clear Login
                        TextButton(
                            onClick = {
                                clearLoginState()
                                onDeviceSync(false)
                            },
                            enabled = accountLinked,
                            contentPadding = PaddingValues(8.dp, 3.dp),
                            modifier = modifier
                                .heightIn(28.dp, 28.dp)
                        ) {
                            Text(
                                text = "Clear Login",
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!acknowledgement) {
                TextButton(
                    onClick = { confirmAcknowledgement() },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled = atBottom
                ) {
                    Text("Agree")
                }
            } else {
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = if (!acknowledgement) {
            {
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Text("Cancel")
                }
            }
        } else null,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )

}

@Composable
fun TinRatesDialog(
    onDismiss: () -> Unit,
    ozRate: Double,
    gramsRate: Double,
    onSave: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var tinOzRate by rememberSaveable { mutableStateOf(formatDecimal(ozRate)) }
    var tinGramsRate by rememberSaveable { mutableStateOf(formatDecimal(gramsRate)) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .offset(x = (-8).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "One Tin = ",
                        modifier = Modifier
                            .padding(end = 8.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.Start
                    ) {
                        val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
                        val allowedPattern = remember(symbols.decimalSeparator, symbols.groupingSeparator) {
                            val ds = Regex.escape(symbols.decimalSeparator.toString())
                            Regex("^(\\s*|\\d+($ds\\d{0,2})?)$")
                        }

                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                        ) {
                            CustomTextField(
                                value = tinOzRate,
                                onValueChange = {
                                    if (it.matches(allowedPattern)) {
                                        tinOzRate = it
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.End,
                                    color = LocalContentColor.current,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = LocalCustomColors.current.textField,
                                    unfocusedContainerColor = LocalCustomColors.current.textField,
                                    disabledContainerColor = LocalCustomColors.current.textField,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                ),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                                placeholder = {
                                    Text(
                                        text = "($ozRate)",
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        style = LocalTextStyle.current.copy(
                                            textAlign = TextAlign.End,
                                            fontSize = 13.5.sp,
                                            lineHeight = 20.sp,
                                            color = LocalContentColor.current.copy(alpha = .38f),
                                        )
                                    )
                                }
                            )
                            Text(
                                text = "oz",
                                modifier = Modifier
                            )
                        }
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                        ) {
                            CustomTextField(
                                value = tinGramsRate,
                                onValueChange = {
                                    if (it.matches(allowedPattern)) {
                                        tinGramsRate = it
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.End,
                                    color = LocalContentColor.current,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = LocalCustomColors.current.textField,
                                    unfocusedContainerColor = LocalCustomColors.current.textField,
                                    disabledContainerColor = LocalCustomColors.current.textField,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                ),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                                placeholder = {
                                    Text(
                                        text = "($gramsRate)",
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        style = LocalTextStyle.current.copy(
                                            textAlign = TextAlign.End,
                                            fontSize = 13.5.sp,
                                            lineHeight = 20.sp,
                                            color = LocalContentColor.current.copy(alpha = .38f),
                                        )
                                    )
                                }
                            )
                            Text(
                                text = "grams",
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        tinOzRate.toDoubleOrNull() ?: ozRate,
                        tinGramsRate.toDoubleOrNull() ?: gramsRate
                    )
                },
                modifier = Modifier
            ) {
                Text(stringResource(R.string.save))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun TinSyncDefaultDialog(
    onDismiss: () -> Unit,
    defaultSyncOption: Boolean,
    onDefaultSync: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set \"Sync Tins\" default on or off when adding new entries.",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offAlpha = if (!defaultSyncOption) 1f else .5f
                    val onAlpha = if (defaultSyncOption) 1f else .5f
                    Text(
                        text = "Off",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (!defaultSyncOption) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(alpha = offAlpha)
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = defaultSyncOption,
                            onCheckedChange = { onDefaultSync(it) },
                            modifier = Modifier
                                .requiredHeight(20.dp)
                                .scale(.6f),
                            colors = SwitchDefaults.colors(
                            )
                        )
                    }
                    Text(
                        text = "On",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (defaultSyncOption) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(onAlpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun DbOperationsDialog(
    onDismiss: () -> Unit,
    updateTinSync: () -> Unit,
    optimizeDatabase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "These options shouldn't be necessary. Fix tin sync quantity corrects " +
                            "the \"No. of Tins\" for entries with quantity sync checked. Optimize " +
                            "database cleans up any potentially orphaned data.",
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 8.dp),
                )
                TextButton(
                    onClick = { updateTinSync() },
                    contentPadding = PaddingValues(8.dp, 3.dp),
                    modifier = modifier
                        .heightIn(28.dp, 28.dp)
                ) {
                    Text(
                        text = "Fix/Update Tin Sync Quantity",
                        modifier = Modifier,
                        fontSize = 15.sp,
                    )
                }
                TextButton(
                    onClick = { optimizeDatabase() },
                    contentPadding = PaddingValues(8.dp, 3.dp),
                    modifier = modifier
                        .heightIn(28.dp, 28.dp)
                ) {
                    Text(
                        text = "Clean and Optimize Database",
                        modifier = Modifier,
                        fontSize = 15.sp,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun BackupRestoreDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onRestore: () -> Unit,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var option: String? by rememberSaveable { mutableStateOf(null) }
    val updateOption: (String?) -> Unit = { option = it }

    val backupState by viewmodel.backupState.collectAsState()
    val restoreState by viewmodel.restoreState.collectAsState()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        text = {
            when (option) {
                "Backup" -> BackupDialog(backupState, viewmodel)
                "Restore" -> RestoreDialog(restoreState, viewmodel)
                null -> {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Would you like to Backup or Restore?",
                            fontSize = 15.sp,
                            color = LocalContentColor.current
                        )
                        Column(
                            modifier = Modifier
                               // .padding(start = 16.dp)
                                .fillMaxWidth()
                            ,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { updateOption("Backup") },
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = modifier
                                    .heightIn(28.dp, 28.dp)
                            ) {
                                Text(
                                    text = "Backup",
                                    fontSize = 15.sp
                                )
                            }
                            TextButton(
                                onClick = { updateOption("Restore") },
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = modifier
                                    .heightIn(28.dp, 28.dp)
                            ) {
                                Text(
                                    text = "Restore",
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            when (option) {
                "Backup" -> {
                    TextButton(
                        onClick = {
                            val suggestedFilename = backupState.suggestedFilename
                            onSave(suggestedFilename)
                        },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = backupState.databaseChecked || backupState.settingsChecked
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
                "Restore" -> {
                    TextButton(
                        onClick = { onRestore() },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = restoreState.databaseChecked || restoreState.settingsChecked
                    ) {
                        Text(text = "Open")
                    }
                }
                null -> { }
            }

        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )

}

@Composable
private fun BackupDialog(
    backupState: BackupState,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    Column (
        modifier = modifier
            .padding(bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Select what to backup. If you check both boxes, a single file will " +
                    "be created that holds both. The restore function can optionally " +
                    "restore either the database or the settings from the joint backup " +
                    "file.",
            modifier = Modifier
                .padding(bottom = 12.dp),
            fontSize = 15.sp,
            color = LocalContentColor.current
        )
        CheckboxWithLabel(
            text = "Database",
            checked = backupState.databaseChecked,
            onCheckedChange = {
                viewmodel.onBackupOptionChanged(
                    backupState.copy(databaseChecked = it)
                )
            },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
        CheckboxWithLabel(
            text = "Settings",
            checked = backupState.settingsChecked,
            onCheckedChange = {
                viewmodel.onBackupOptionChanged(
                    backupState.copy(settingsChecked = it)
                )
            },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
    }
}

@Composable
private fun RestoreDialog(
    restoreState: RestoreState,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    Column (
        modifier = modifier
            .padding(bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Select what to restore.\n\nWARNING: Restore will overwrite any existing " +
                    "settings and/or database data (depending on which is selected). " +
                    "Either or both can be restored from a joint database/settings backup " +
                    "file.",
            modifier = Modifier
                .padding(bottom = 12.dp),
            fontSize = 15.sp,
            color = LocalContentColor.current
        )
        CheckboxWithLabel(
            text = "Database",
            checked = restoreState.databaseChecked,
            onCheckedChange = {
                viewmodel.onRestoreOptionChanged(
                    restoreState.copy(databaseChecked = it)
                ) },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
        CheckboxWithLabel(
            text = "Settings",
            checked = restoreState.settingsChecked,
            onCheckedChange = {
                viewmodel.onRestoreOptionChanged(
                    restoreState.copy(settingsChecked = it)
                )
            },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
    }
}

@Composable
private fun DeleteAllDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.delete_all)) },
        text = { Text(
            stringResource(R.string.delete_all_question),
            fontSize = 15.sp
        ) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(stringResource(R.string.yes))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}