package com.sardonicus.tobaccocellar.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.BuildConfig
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch

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

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(SettingsDestination.titleRes),
                scrollBehavior = scrollBehavior,
                navigateUp = onNavigateUp,
                canNavigateBack = canNavigateBack,
                showMenu = false,
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
            SettingsBody(
                onDeleteAllClick = {
                    coroutineScope.launch {
                        viewmodel.deleteAllItems()
                    }
                },
                saveTheme = { viewmodel.saveThemeSetting(it) },
                saveQuantity = { viewmodel.saveQuantityOption(it) },
                preferencesRepo = viewmodel.preferencesRepo,
                tinOzConversionRate = ozRate,
                tinGramsConversionRate = gramsRate,
                onSetTinConversionRates = { ozRate, gramsRate ->
                    viewmodel.setTinConversionRates(ozRate, gramsRate)
                },
                modifier = modifier
                    .fillMaxWidth(),
            )
        }
    }
}


@Composable
private fun SettingsBody(
    onDeleteAllClick: () -> Unit,
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
    var showChangelog by rememberSaveable { mutableStateOf(false) }
    var showQuantityDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showChangelog) {
        showChangelog = false
    }

    if (showChangelog) {
        ChangeLogDialog(
            changeLogEntries = changeLogEntries,
            showChangelog = { showChangelog = it },
            modifier = Modifier
        )
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        if (!showChangelog) {
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
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        LocalCustomColors.current.darkNeutral.copy(alpha = .5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )
//            Spacer(
//                modifier = Modifier
//                    .height(32.dp)
//            )

            DatabaseSettings(
                showTinRates = { showTinRates = it },
                deleteAllConfirm = { deleteAllConfirm = it },
                modifier = Modifier
                    //    .padding(horizontal = 16.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        LocalCustomColors.current.darkNeutral.copy(alpha = .5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )

//            Spacer(
//                modifier = Modifier
//                    .height(32.dp)
//            )

            AboutSection(
                showChangelog = { showChangelog = it },
                modifier = Modifier
                    //    .padding(horizontal = 16.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        LocalCustomColors.current.darkNeutral.copy(alpha = .5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )
//            Spacer(
//                modifier = Modifier
//                    .height(32.dp)
//            )

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
                    onQuantityOption = { saveQuantity(it)}
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
            fontSize = 18.sp
        )
        Text(
            text = "Theme",
            modifier = Modifier
                .clickable { showThemeDialog(true) }
                .padding(vertical = 2.dp)
                .padding(start = 8.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Cellar Quantity Display",
            modifier = Modifier
                .clickable { showQuantityDialog(true) }
                .padding(vertical = 2.dp)
                .padding(start = 8.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DatabaseSettings(
    showTinRates: (Boolean) -> Unit,
    deleteAllConfirm: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Database Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 4.dp),
            fontSize = 18.sp
        )
        Text(
            text = "Change Tin Conversion Rates",
            modifier = Modifier
                .clickable { showTinRates(true) }
                .padding(vertical = 2.dp)
                .padding(start = 8.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Clear Database",
            modifier = Modifier
                .clickable { deleteAllConfirm(true) }
                .padding(vertical = 2.dp)
                .padding(start = 8.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
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
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply{
        data = Uri.parse("mailto:sardonicus.notadev@gmail.com")
        putExtra(Intent.EXTRA_SUBJECT, "Tobacco Cellar Feedback")
    }

    val contactString = buildAnnotatedString {
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal)
        ) { append("Contact me if you experience any bugs: ") }
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
            fontWeight = FontWeight.Normal)
        ) { append(appVersion) }
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold)
        ) { append("\nDatabase Version: ") }
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Normal)
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
            fontSize = 18.sp
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
            Text(
                text = contactString,
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val annotations =
                                contactString.getStringAnnotations("Email", 0, contactString.length)
                            annotations
                                .firstOrNull()
                                ?.let { annotation ->
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse(annotation.item)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            emailIntent,
                                            "Send Email"
                                        )
                                    )
                                }
                        }
                    )
                    .padding(top = 6.dp, bottom = 4.dp),
                fontSize = 14.sp,
                softWrap = true,
            )

            Spacer(
                modifier = Modifier
                    .height(14.dp)
            )
            Text(
                text = versionInfo,
                modifier = Modifier
                    .padding(top = 4.dp),
                fontSize = 14.sp,
                softWrap = true,
            )
            Text(
                text = "Change Log ",
                modifier = Modifier
                    .clickable { showChangelog(true) }
                    .padding(vertical = 1.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChangeLogDialog(
    changeLogEntries: List<ChangeLogEntryData>,
    showChangelog: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
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
                text = "Change Log",
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
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .height(12.dp)
                )
            }
            items(items = changeLogEntries, key = { it.versionNumber }
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
                            .padding(bottom = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                    ) {
                        RadioButton(
                            selected = currentTheme == theme.value,
                            onClick = { onThemeSelected(theme.value) },
                            modifier = Modifier
                                .size(36.dp)
                        )
                        Text(
                            text = theme.value,
                            modifier = Modifier
                                .clickable { onThemeSelected(theme.value) },
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
                            .padding(bottom = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                    ) {
                        RadioButton(
                            selected = currentQuantity == it,
                            onClick = { onQuantityOption(it.value) },
                            modifier = Modifier
                                .size(36.dp)
                        )
                        Text(
                            text = it.value,
                            modifier = Modifier
                                .clickable { onQuantityOption(it.value) },
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
                        val pattern = remember { Regex("^(\\s*|\\d+(\\.\\d{0,2})?)\$") }

                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                        ) {
                            CustomTextField(
                                value = tinOzRate,
                                onValueChange = {
                                    if (it.matches(pattern)) {
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
                                    if (it.matches(pattern)) {
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
                TextButton(
                    onClick = {
                        onSet(
                            tinOzRate.toDoubleOrNull() ?: 1.75,
                            tinGramsRate.toDoubleOrNull() ?: 50.0
                        )
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.End)
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
