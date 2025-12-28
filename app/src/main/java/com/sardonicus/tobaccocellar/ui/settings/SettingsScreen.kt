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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
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
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
                    preferencesRepo = viewModel.preferencesRepo,
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
    preferencesRepo: PreferencesRepo,
    navigateToChangelog: (List<ChangelogEntryData>) -> Unit,
    tinOzConversionRate: Double,
    tinGramsConversionRate: Double,
    updateTinSync: () -> Unit,
    optimizeDatabase: () -> Unit,
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openDialog by viewModel.openDialog.collectAsState()

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
                updateTinSync = updateTinSync,
                optimizeDatabase = optimizeDatabase,
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
                onThemeSelected = { viewModel.saveThemeSetting(it) },
                preferencesRepo = preferencesRepo,
                onClose = { viewModel.dismissDialog() }
            )
        }
        DialogType.Ratings -> {
                RatingsDialog(
                    onDismiss = { viewModel.dismissDialog() },
                    preferencesRepo = preferencesRepo,
                    modifier = Modifier,
                    onRatingsOption = { viewModel.saveShowRatingOption(it) }
                )
            }
        DialogType.TypeGenre -> {
            val enablement by viewModel.typeGenreOptionEnablement.collectAsState()
            TypeGenreDialog(
                onDismiss = { viewModel.dismissDialog() },
                preferencesRepo = preferencesRepo,
                optionEnablement = enablement,
                modifier = Modifier,
                onTypeGenreOption = { viewModel.saveTypeGenreOption(it) }
            )
        }
        DialogType.QuantityDisplay -> {
            QuantityDialog(
                onDismiss = { viewModel.dismissDialog() },
                preferencesRepo = preferencesRepo,
                modifier = Modifier,
                onQuantityOption = { viewModel.saveQuantityOption(it) }
            )
        }
        DialogType.ParseLinks -> {
                ParseLinksDialog(
                    onDismiss = { viewModel.dismissDialog() },
                    preferencesRepo = preferencesRepo,
                    modifier = Modifier,
                    onParseLinksOption = { viewModel.saveParseLinksOption(it) }
                )
            }

        // App/Database Settings
        DialogType.DeviceSync -> {
            val acknowledgement by viewModel.deviceSyncAcknowledgement.collectAsState()

            DeviceSyncDialog(
                onDismiss = { viewModel.dismissDialog() },
                acknowledgement = acknowledgement,
                confirmAcknowledgement = { viewModel.saveCrossDeviceAcknowledged() },
                onDeviceSync = { viewModel.saveCrossDeviceSync(it) },
                preferencesRepo = preferencesRepo,
                modifier = Modifier
            )
        }
        DialogType.DefaultSync -> {
            DefaultSyncDialog(
                onDismiss = { viewModel.dismissDialog() },
                onDefaultSync = { viewModel.setDefaultSyncOption(it) },
                preferencesRepo = preferencesRepo,
                modifier = Modifier
            )
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
        DialogType.Backup -> {
                BackupDialog(
                    onDismiss = { viewModel.dismissDialog() },
                    onSave = {
                        launcher.launch(it)
                        viewModel.dismissDialog()
                    },
                    viewmodel = viewModel,
                    modifier = Modifier
                )
            }
        DialogType.Restore -> {
            RestoreDialog(
                onDismiss = { viewModel.dismissDialog() },
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
        DialogType.Recalculate -> { }
        DialogType.Optimize -> { }
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
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Display Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 4.dp),
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
    updateTinSync: () -> Unit,
    optimizeDatabase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "App & Database Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 4.dp),
            fontSize = 16.sp
        )
        viewModel.databaseSettings.forEach {
            val onClickOverride:  () -> Unit =
                when (it.dialogType) {
                    DialogType.Recalculate -> { { updateTinSync() } }
                    DialogType.Optimize -> { { optimizeDatabase() } }
                    else -> { { viewModel.showDialog(it.dialogType) } }
                }
            val color = if (it.dialogType == DialogType.DeleteAll) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary

            SettingsButton(
                text = it.title,
                onClick = onClickOverride,
                color = color
            )
        }
    }
}

@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(8.dp, 3.dp),
        modifier = Modifier
            .heightIn(28.dp, 28.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier,
            fontSize = 14.sp,
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
    onThemeSelected: (String) -> Unit,
    onClose: () -> Unit,
    preferencesRepo: PreferencesRepo,
    modifier: Modifier = Modifier
) {
    val currentTheme by preferencesRepo.themeSetting.collectAsState(initial = ThemeSetting.SYSTEM.value)

    AlertDialog(
        onDismissRequest = { onClose() },
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
                            selected = currentTheme == it.value,
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
                onClick = { onClose() },
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
    onRatingsOption: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    preferencesRepo: PreferencesRepo
) {
    val currentShowRating by preferencesRepo.showRating.collectAsState(initial = false)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Row(
                modifier = modifier
                    .padding(bottom = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Display ratings on cellar screen:",
                    modifier = Modifier,
                    fontSize = 16.sp,
                    color = LocalContentColor.current
                )
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                    Switch(
                        checked = currentShowRating,
                        onCheckedChange = { onRatingsOption(it) },
                        modifier = Modifier
                            .requiredHeight(20.dp)
                            .scale(.7f)
                            .padding(start = 10.dp),
                        colors = SwitchDefaults.colors(
                        )
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
    onTypeGenreOption: (String) -> Unit,
    optionEnablement: Map<TypeGenreOption, Boolean>,
    preferencesRepo: PreferencesRepo,
    modifier: Modifier = Modifier
) {
    val currentTypeGenre by preferencesRepo.typeGenreOption.collectAsState(initial = TypeGenreOption.TYPE)

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
                    text = "This option sets the display of Type, Subgenre or both in list view " +
                            "on the Cellar screen. Fallback options allow you to select one or " +
                            "the other, but if that field is unused for an entry, try to fallback " +
                            "to the other field.",
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    fontSize = 14.sp,
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
                            selected = currentTypeGenre == it,
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
    onQuantityOption: (String) -> Unit,
    preferencesRepo: PreferencesRepo,
    modifier: Modifier = Modifier
) {
    val currentQuantity by preferencesRepo.quantityOption.collectAsState(initial = QuantityOption.TINS)

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
                            "quantities of tins. If no tins are present, the \"No. of Tins\" " +
                            "value will be displayed instead.",
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
                            selected = currentQuantity == it,
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
    onParseLinksOption: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    preferencesRepo: PreferencesRepo
) {
    val currentParseOption by preferencesRepo.parseLinks.collectAsState(initial = true)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Row(
                modifier = modifier
                    .padding(bottom = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Parse links in notes:",
                    modifier = Modifier,
                    fontSize = 16.sp,
                    color = LocalContentColor.current
                )
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                    Switch(
                        checked = currentParseOption,
                        onCheckedChange = { onParseLinksOption(it) },
                        modifier = Modifier
                            .requiredHeight(20.dp)
                            .scale(.7f)
                            .padding(start = 10.dp),
                        colors = SwitchDefaults.colors(
                        )
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
    confirmAcknowledgement: () -> Unit,
    onDeviceSync: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    preferencesRepo: PreferencesRepo
) {
    val currentOption by preferencesRepo.crossDeviceSync.collectAsState(initial = false)
    val email by preferencesRepo.signedInUserEmail.collectAsState(initial = null)
    val hasScope by preferencesRepo.hasDriveScope.collectAsState(initial = false)
    val enabled by remember (email, hasScope) { mutableStateOf(!email.isNullOrBlank() && hasScope) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!acknowledgement) {
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "About Multi Device Sync",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "To auto synchronize collection changes across devices, you " +
                                    "must enable this option and authorize Google Drive access " +
                                    "on each device. You only need to authorize once per device " +
                                    "(though uninstalling and reinstalling will require " +
                                    "re-authorizing). This also requires all devices to be " +
                                    "running an the same Database Version.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "I (developer) will not have access to your login or drive, " +
                                    "this authorization just allows the app to use your Google " +
                                    "Drive as a cloud location for storing and retrieving data " +
                                    "changes between devices. This login and authorization can be " +
                                    "cleared at any time in this setting dialog.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Data is only uploaded/downloaded when connected to WIFI, and " +
                                    "does not count toward your storage quota. Data is checked " +
                                    "once at every app start, and cyclically once every 12 hours " +
                                    "as long as the device is powered on and connected to WIFI.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Changes are saved locally and scheduled to upload when " +
                                    "connected to WIFI. The uploaded data only includes changes. " +
                                    "The changes are time-stamped and device sync is bi-" +
                                    "directional.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Before enabling this option on multiple devices, it is " +
                                    "recommended to create a manual database backup of the " +
                                    "device with the most up-to-date data and transfer it to " +
                                    "and restore on the other device(s).",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                    }
                } else {
                    Text(
                        text = "Multi-Device Sync:",
                        modifier = Modifier,
                        fontSize = 16.sp,
                        color = LocalContentColor.current
                    )
                    Row(
                        modifier = modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val offAlpha = if (!currentOption) 1f else .5f
                        val onAlpha = if (currentOption) 1f else .5f
                        Text(
                            text = "Off",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            fontWeight = if (!currentOption) FontWeight.SemiBold else FontWeight.Normal,
                            color = LocalContentColor.current.copy(alpha = offAlpha)
                        )
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                            Switch(
                                checked = currentOption,
                                onCheckedChange = { onDeviceSync(it) },
                                modifier = Modifier
                                    .requiredHeight(20.dp)
                                    .scale(.7f)
                                    .padding(start = 10.dp),
                                colors = SwitchDefaults.colors(
                                )
                            )
                        }
                        Text(
                            text = "On",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            fontWeight = if (currentOption) FontWeight.SemiBold else FontWeight.Normal,
                            color = LocalContentColor.current.copy(onAlpha)
                        )
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                preferencesRepo.clearLoginState()
                                onDeviceSync(false)
                            }
                        },
                        enabled = enabled,
                        modifier = Modifier
                            .padding(0.dp)
                    ) {
                        Text(
                            text = "Clear login and authorization"
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!acknowledgement) {
                TextButton(
                    onClick = { confirmAcknowledgement() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Text("Acknowledge")
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
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun DefaultSyncDialog(
    onDismiss: () -> Unit,
    onDefaultSync: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    preferencesRepo: PreferencesRepo
) {
    val currentSyncOption by preferencesRepo.defaultSyncOption.collectAsState(initial = false)

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
                    text = "Set \"Sync Tins\" default on or off when adding new items.",
                    modifier = Modifier,
                    fontSize = 16.sp,
                    color = LocalContentColor.current
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offAlpha = if (!currentSyncOption) 1f else .5f
                    val onAlpha = if (currentSyncOption) 1f else .5f
                    Text(
                        text = "Off",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (!currentSyncOption) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(alpha = offAlpha)
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = currentSyncOption,
                            onCheckedChange = { onDefaultSync(it) },
                            modifier = Modifier
                                .requiredHeight(20.dp)
                                .scale(.7f)
                                .padding(start = 10.dp),
                            colors = SwitchDefaults.colors(
                            )
                        )
                    }
                    Text(
                        text = "On",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (currentSyncOption) FontWeight.SemiBold else FontWeight.Normal,
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
private fun BackupDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val backupState by viewmodel.backupState.collectAsState()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        text = {
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
                    val suggestedFilename = backupState.suggestedFilename
                    onSave(suggestedFilename)
                },
                modifier = Modifier
                    .padding(0.dp),
                enabled = backupState.databaseChecked || backupState.settingsChecked
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
private fun RestoreDialog(
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRestore()
                    onDismiss()
                },
                modifier = Modifier
                    .padding(0.dp),
                enabled = restoreState.databaseChecked || restoreState.settingsChecked
            ) {
                Text(stringResource(R.string.ok))
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
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
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
        text = { Text(stringResource(R.string.delete_all_question)) },
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