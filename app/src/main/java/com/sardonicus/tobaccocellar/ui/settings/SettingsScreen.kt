package com.sardonicus.tobaccocellar.ui.settings

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
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
import com.sardonicus.tobaccocellar.ui.composables.FullScreenLoading
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewmodel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val ozRate by viewmodel.tinOzConversionRate.collectAsState()
    val gramsRate by viewmodel.tinGramsConversionRate.collectAsState()
    val snackbarState = viewmodel.snackbarState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val loading by viewmodel.loading.collectAsState()

    if (snackbarState.value.show) {
        LaunchedEffect(snackbarState) {
            snackbarHostState.showSnackbar(
                message = snackbarState.value.message,
                duration = SnackbarDuration.Short
            )
            viewmodel.snackbarShown()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(SettingsDestination.titleRes),
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
                    viewmodel = viewmodel,
                    onDeleteAllClick = {
                        coroutineScope.launch {
                            viewmodel.deleteAllItems()
                        }
                    },
                    optimizeDatabase = { viewmodel.optimizeDatabase() },
                    saveTheme = { viewmodel.saveThemeSetting(it) },
                    saveQuantity = { viewmodel.saveQuantityOption(it) },
                    preferencesRepo = viewmodel.preferencesRepo,
                    tinOzConversionRate = ozRate,
                    tinGramsConversionRate = gramsRate,
                    onSetTinConversionRates = { ozRate, gramsRate ->
                        viewmodel.setTinConversionRates(ozRate, gramsRate)
                    },
                    modifier = modifier
                        .fillMaxSize(),
                )
                if (loading) {
                    FullScreenLoading(
                        scrimColor = Color.Black.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingsBody(
    viewmodel: SettingsViewModel,
    onDeleteAllClick: () -> Unit,
    optimizeDatabase: () -> Unit,
    saveTheme: (String) -> Unit,
    saveQuantity: (String) -> Unit,
    tinOzConversionRate: Double,
    tinGramsConversionRate: Double,
    onSetTinConversionRates: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    preferencesRepo: PreferencesRepo,
) {
    var deleteAllConfirm by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showTinRates by rememberSaveable { mutableStateOf(false) }
    var showQuantityDialog by rememberSaveable { mutableStateOf(false) }
    var backup by rememberSaveable { mutableStateOf(false) }
    var restore by rememberSaveable { mutableStateOf(false) }
    var showChangelog by rememberSaveable { mutableStateOf(false) }

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val windowWidth = LocalWindowInfo.current.containerSize.width.dp.value * density.density
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = tween(durationMillis = 300),
        label = "Animated Drag Offset"
    )

    LaunchedEffect(showChangelog) {
        if (!showChangelog) {
            dragOffset = 0f
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource) : Offset {
                return if (isDragging) {
                    available
                } else {
                    Offset.Zero
                }
            }
        }
    }

    BackHandler(showChangelog) { showChangelog = false }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) {
        it?.let {
            viewmodel.saveBackup(context, it)
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) {
        it?.let {
            viewmodel.restoreBackup(context, it)
        }
    }

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(0.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )

            DisplaySettings(
                showThemeDialog = { showThemeDialog = it },
                showQuantityDialog = { showQuantityDialog = it },
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

            DatabaseSettings(
                showTinRates = { showTinRates = it },
                deleteAllConfirm = { deleteAllConfirm = it },
                showBackup = { backup = it },
                showRestore = { restore = it },
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

            AboutSection(
                showChangelog = { showChangelog = it },
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

            // Popup settings dialogs
            if (showThemeDialog) {
                ThemeDialog(
                    onThemeSelected = { newTheme ->
                        saveTheme(newTheme)
                    },
                    preferencesRepo = preferencesRepo,
                    onClose = { showThemeDialog = false }
                )
            }
            if (showQuantityDialog) {
                QuantityDialog(
                    onDismiss = { showQuantityDialog = false },
                    preferencesRepo = preferencesRepo,
                    modifier = Modifier,
                    onQuantityOption = { saveQuantity(it) }
                )
            }
            if (showTinRates) {
                TinRatesDialog(
                    onDismiss = { showTinRates = false },
                    ozRate = tinOzConversionRate,
                    gramsRate = tinGramsConversionRate,
                    onSet = { ozRate, gramsRate ->
                        onSetTinConversionRates(ozRate, gramsRate)
                        showTinRates = false
                    },
                    modifier = Modifier
                )
            }
            if (deleteAllConfirm) {
                DeleteAllDialog(
                    onDeleteConfirm = {
                        deleteAllConfirm = false
                        onDeleteAllClick()
                    },
                    onDeleteCancel = { deleteAllConfirm = false },
                    modifier = Modifier
                        .padding(0.dp)
                )
            }
            if (backup) {
                BackupDialog(
                    onDismiss = { backup = false },
                    onSave = {
                        backup = false
                        launcher.launch(it)
                    },
                    viewmodel = viewmodel,
                    modifier = Modifier
                )
            }
            if (restore) {
                RestoreDialog(
                    onDismiss = { restore = false },
                    onRestore = {
                        restore = false
                        openLauncher.launch(arrayOf("application/octet-stream"))
                    },
                    viewmodel = viewmodel,
                    modifier = Modifier
                )
            }

        }

        AnimatedVisibility(
            visible = showChangelog,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            ChangelogDialog(
                changelogEntries = changelogEntries,
                showChangelog = {
                    coroutineScope.launch {
                        if (!it) { dragOffset = 0f }
                        showChangelog = it
                    }
                },
                modifier = Modifier
                    .offset { IntOffset(animatedDragOffset.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                if (showChangelog) {
                                    isDragging = true
                                    if (dragAmount.x > 0 && dragOffset < windowWidth) {
                                        dragOffset = min(dragOffset + dragAmount.x, windowWidth)
                                    } else if (dragAmount.x < 0 && dragOffset > 0) {
                                        dragOffset = max(dragOffset + dragAmount.x, 0f)
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onDragEnd = {
                                isDragging = false
                                if (dragOffset > density.run { 100.dp.toPx() }) {
                                    dragOffset = windowWidth
                                    showChangelog = false
                                } else {
                                    dragOffset = 0f
                                }
                            }
                        )
                    }
            )
        }
    }
}

@Composable
fun DisplaySettings(
    showThemeDialog: (Boolean) -> Unit,
    showQuantityDialog: (Boolean) -> Unit,
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
        TextButton(
            onClick = { showThemeDialog(true) },
            contentPadding = PaddingValues(8.dp, 3.dp),
            modifier = Modifier
                .heightIn(28.dp, 28.dp)
        ) {
            Text(
                text = "Theme",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }
        TextButton(
            onClick = { showQuantityDialog(true) },
            contentPadding = PaddingValues(8.dp, 3.dp),
            modifier = Modifier
                .heightIn(28.dp, 28.dp)
        ) {
            Text (
                text = "Cellar Quantity Display",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
fun DatabaseSettings(
    showTinRates: (Boolean) -> Unit,
    deleteAllConfirm: (Boolean) -> Unit,
    showBackup: (Boolean) -> Unit,
    showRestore: (Boolean) -> Unit,
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
        TextButton(
            onClick = { showTinRates(true) },
            contentPadding = PaddingValues(8.dp, 3.dp),
            modifier = Modifier
                .heightIn(28.dp, 28.dp)
        ){
            Text(
                text = "Tin Conversion Rates",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }
        TextButton(
            onClick = { optimizeDatabase() },
            contentPadding = PaddingValues(8.dp, 3.dp),
            modifier = Modifier
                .heightIn(28.dp, 28.dp)
        ) {
            Text(
                text = "Clean & Optimize Database",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }
        TextButton(
            onClick = { showBackup(true) },
            contentPadding = PaddingValues(8.dp, 3.dp),
            modifier = Modifier
                .heightIn(28.dp, 28.dp)
        ) {
            Text(
                text = "Backup",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }

        TextButton(
            onClick = { showRestore(true) },
            contentPadding = PaddingValues(8.dp, 3.dp),
            modifier = Modifier
                .heightIn(28.dp, 28.dp)
        ) {
            Text(
                text = "Restore",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }
        TextButton(
            onClick = { deleteAllConfirm(true) },
            contentPadding = PaddingValues(8.dp, 3.dp),
            modifier = Modifier
                .heightIn(28.dp, 28.dp)
        ) {
            Text(
                text = "Delete Database",
                modifier = Modifier,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun AboutSection(
    showChangelog: (Boolean) -> Unit,
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
                    ) { showChangelog(true) }
                    .padding(vertical = 1.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChangelogDialog(
    changelogEntries: List<ChangelogEntryData>,
    showChangelog: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenHeight = LocalWindowInfo.current.containerSize.height.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .heightIn(max = screenHeight)
            .padding(0.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = LocalCustomColors.current.backgroundVariant),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Column (
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                IconButton(
                    onClick = { showChangelog(false) },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(0.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Text(
                text = "Changelog",
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
        }
        // log entries
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            state = rememberLazyListState(),
            userScrollEnabled = true,
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .height(12.dp)
                )
            }
            items(items = changelogEntries, key = { it.versionNumber }
            ) {
                if (it.versionNumber.isNotBlank()) {
                    ChangeLogEntryLayout(
                        versionNumber = it.versionNumber,
                        buildDate = it.buildDate,
                        changes = it.changes,
                        improvements = it.improvements,
                        bugFixes = it.bugFixes,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun ChangeLogEntryLayout(
    versionNumber: String,
    buildDate: String,
    modifier: Modifier = Modifier,
    changes: List<String> = emptyList(),
    improvements: List<String> = emptyList(),
    bugFixes: List<String> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Version $versionNumber  ($buildDate)",
            modifier = Modifier,
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        HorizontalDivider(
            modifier = Modifier
                .padding(bottom = 8.dp),
            thickness = 1.dp,
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            if (changes.isNotEmpty()) {
                Text(
                    text = "Changes:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    changes.forEach {
                        Row {
                            Column {
                                Text(
                                    text = "•  ",
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            Column {
                                Text(
                                    text = it,
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    softWrap = true,
                                )
                            }
                        }
                    }
                }
            }
            if (improvements.isNotEmpty()) {
                Text(
                    text = "Improvements:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    improvements.forEach {
                        Row {
                            Column {
                                Text(
                                    text = "•  ",
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            Column {
                                Text(
                                    text = it,
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    softWrap = true,
                                )
                            }
                        }
                    }
                }
            }
            if (bugFixes.isNotEmpty()) {
                Text(
                    text = "Bug Fixes:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    bugFixes.forEach {
                        Row {
                            Column {
                                Text(
                                    text = "•  ",
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            Column {
                                Text(
                                    text = it,
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    softWrap = true,
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
    }
}


/** Dialogs **/
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
                modifier = modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                listOf(ThemeSetting.LIGHT, ThemeSetting.DARK, ThemeSetting.SYSTEM).forEach { theme ->
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { onThemeSelected(theme.value) }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                    ) {
                        RadioButton(
                            selected = currentTheme == theme.value,
                            onClick = null,
                            modifier = Modifier
                                .size(36.dp)
                        )
                        Text(
                            text = theme.value,
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
                Text(stringResource(R.string.save))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
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
                    color = LocalContentColor.current.copy(alpha = 0.75f)
                )
                listOf(QuantityOption.TINS, QuantityOption.OUNCES, QuantityOption.GRAMS).forEach {
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
                Text(stringResource(R.string.save))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinRatesDialog(
    onDismiss: () -> Unit,
    ozRate: Double,
    gramsRate: Double,
    onSet: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var tinOzRate by rememberSaveable { mutableStateOf(ozRate.toString()) }
    var tinGramsRate by rememberSaveable { mutableStateOf(gramsRate.toString()) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Tin Conversion Rates",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 16.dp),
                    fontSize = 18.sp,
                    softWrap = false,
                    color = Color.Transparent
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .offset(x = (-8).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
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
                            )
                            Text(
                                text = "grams",
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                    ) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier
                                .padding(0.dp)
                        )
                    }
                    TextButton(
                        onClick = {
                            onSet(
                                tinOzRate.toDoubleOrNull() ?: 1.75,
                                tinGramsRate.toDoubleOrNull() ?: 50.0
                            )
                        },
                        modifier = Modifier
                    ) {
                        Text(
                            text = "Save",
                            modifier = Modifier
                                .padding(0.dp)
                        )
                    }
                }
            }
        }
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
                    color = LocalContentColor.current.copy(alpha = 0.75f)
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
                    color = LocalContentColor.current.copy(alpha = 0.75f)
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
    )
}