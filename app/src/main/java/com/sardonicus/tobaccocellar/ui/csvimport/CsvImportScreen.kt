package com.sardonicus.tobaccocellar.ui.csvimport

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.CsvResult
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.composables.FullScreenLoading
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch

object CsvImportDestination : NavigationDestination {
    override val route = "csv_import"
    override val titleRes = R.string.csv_import_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportScreen(
    modifier: Modifier = Modifier,
    navigateToImportResults: (Int, Int, Int, Int, Int, Boolean, Boolean) -> Unit,
    navigateToHome: () -> Unit,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: CsvImportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val csvImportState = viewModel.csvImportState.value
    val csvUiState = viewModel.csvUiState
    val mappingOptions = viewModel.mappingOptions
    val importOption by viewModel.importOption.collectAsState()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(CsvImportDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
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
            CsvImportBody(
                csvImportState = csvImportState,
                viewModel = viewModel,
                csvUiState = csvUiState,
                updateMappingFields = { viewModel::updateFieldMapping },
                mappingOptions = mappingOptions,
                importOption = importOption,
                onHeaderChange = { viewModel.updateHeaderOption(it) },
                updateCollateTinsOption = { viewModel.updateCollateTinsOption(it) },
                updateSyncTinsOption = { viewModel.updateSyncTinsOption(it) },
                updateDateFormat = { viewModel.updateDateFormat(it) },
                navigateToImportResults = navigateToImportResults,
                navigateToHome = navigateToHome,
                modifier = modifier
                    .fillMaxSize()
                    .padding(0.dp),
            )
        }
    }
}

@Suppress("NullableBooleanElvis")
@Composable
fun CsvImportBody(
    navigateToImportResults: (Int, Int, Int, Int, Int, Boolean, Boolean) -> Unit,
    navigateToHome: () -> Unit,
    updateMappingFields: (MappingOptions) -> Unit,
    onHeaderChange: (isChecked: Boolean) -> Unit,
    updateCollateTinsOption: (isChecked: Boolean) -> Unit,
    updateSyncTinsOption: (Boolean) -> Unit,
    updateDateFormat: (dateFormat: String) -> Unit,
    csvImportState: CsvImportState,
    csvUiState: CsvUiState,
    mappingOptions: MappingOptions,
    importOption: ImportOption,
    viewModel: CsvImportViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val importStatus by viewModel.importStatus.collectAsState()
    val overwriteSelections by viewModel.overwriteSelections.collectAsState()
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/*"
    }
    val csvHelper = CsvHelper()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        println("ActivityResult result: $result")
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            try {
                uri?.let {
                    val contentResolver = context.contentResolver
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        when (val result = csvHelper.csvFileReader(inputStream)) {
                            is CsvResult.Success -> {
                                viewModel.onCsvLoaded(
                                    result.header,
                                    result.firstFullRecord,
                                    result.allRecords,
                                    result.recordCount
                                )
                                viewModel.generateColumns(
                                    result.columnCount
                                )
                            }

                            is CsvResult.Error -> {
                                showErrorDialog = true
                            }

                            is CsvResult.Empty -> {
                                showErrorDialog = true
                            }
                        }

                    }
                }
            } catch (e: Exception) {
                println("Exception: $e")
                showErrorDialog = true
            }
        }
    }

    if (showErrorDialog) {
        LoadErrorDialog(
            modifier = Modifier,
            confirmError = { showErrorDialog = false }
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToResults.collect { success ->
            navigateToImportResults(
                success.totalRecords,
                success.successfulConversions,
                success.successfulInsertions,
                success.successfulUpdates,
                success.successfulTins,
                success.updateFlag,
                success.tinFlag,
            )
        }
    }

    BackHandler(enabled = showHelp) {
        showHelp = false
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        when (importStatus) {
            is ImportStatus.Loading -> { FullScreenLoading() }
            is ImportStatus.Error -> {
                ImportError(
                    onTryAgain = { coroutineScope.launch { viewModel.resetImportState() } },
                    navigateToHome = { navigateToHome() },
                    modifier = Modifier,
                )
            }
            is ImportStatus.Success -> {}
            else -> {
                Column(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                ) {

                    // Initial screen load before CSV loaded //
                    if (csvUiState.columns.isEmpty()) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )
                        Button(
                            onClick = { launcher.launch(intent) },
                            enabled = true,
                            modifier = Modifier
                                .padding(8.dp)
                                .height(48.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = stringResource(R.string.select_csv),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .weight(2.5f)
                        )
                    }

                    // Screen after CSV loaded //
                    Column(
                        modifier = Modifier
                            .padding(0.dp)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        if (csvUiState.columns.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            // Select CSV and Help buttons //
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    12.dp,
                                    Alignment.CenterHorizontally
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    onClick = { launcher.launch(intent) },
                                    enabled = !showHelp,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                                        disabledContentColor = MaterialTheme.colorScheme.onPrimary
                                    )

                                ) {
                                    Text(
                                        text = stringResource(R.string.select_csv),
                                        modifier = Modifier,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Button(
                                    onClick = { showHelp = !showHelp },
                                    enabled = true,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(40.dp),
                                ) {
                                    if (showHelp) {
                                        Text(
                                            text = "Hide Help",
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .height(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Hide Help",
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Transparent
                                            )
                                            Row(
                                                modifier = Modifier,
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = "Help ",
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(id = R.drawable.help_outline),
                                                    contentDescription = "Help?",
                                                    modifier = Modifier
                                                        .size(16.dp),
                                                    tint = LocalContentColor.current
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.Start,
                                ) {
                                    // CSV parse test //
                                    Column(
                                        modifier = Modifier
                                            .padding(bottom = 16.dp)
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer.copy(
                                                    alpha = 0.3f
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(vertical = 8.dp, horizontal = 12.dp),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.possible_header),
                                            modifier = modifier
                                                .padding(0.dp)
                                                .fillMaxWidth(),
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = csvImportState.header.joinToString(", "),
                                            modifier = Modifier
                                                .padding(start = 8.dp, bottom = 8.dp),
                                        )
                                        Text(
                                            text = stringResource(R.string.possible_record),
                                            modifier = Modifier
                                                .padding(0.dp)
                                                .fillMaxWidth(),
                                            fontWeight = FontWeight.Bold,
                                        )
                                        val parseTest =
                                            if (csvImportState.firstFullRecord.isEmpty()) "(Parse error)"
                                            else csvImportState.firstFullRecord.joinToString(", ")

                                        Text(
                                            text = parseTest,
                                            modifier = Modifier
                                                .padding(start = 8.dp),
                                        )
                                    }

                                    // Import options //
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                            .padding(8.dp),
                                    ) {
                                        // Has header option and record count //
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .weight(1f),
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.Center,
                                            ) {
                                                LabeledCheckbox(
                                                    text = "Has header?",
                                                    width = 96.dp,
                                                    checked = mappingOptions.hasHeader,
                                                    onCheckedChange = {
                                                        onHeaderChange(it)
                                                    },
                                                    modifier = Modifier,
                                                )
                                            }
                                            // record count //
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(0.dp),
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.Center,
                                            ) {
                                                Text(
                                                    text =
                                                        if (mappingOptions.hasHeader) {
                                                            "Record count: ${csvImportState.recordCount - 1}"
                                                        } else {
                                                            "Record count: ${csvImportState.recordCount}"
                                                        },
                                                    modifier = Modifier,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }
                                        // Collate Tins option and warning //
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .weight(1f),
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                                            ) {
                                                LabeledCheckbox(
                                                    text = "Collate tins?",
                                                    maxLines = 1,
                                                    width = 96.dp,
                                                    checked = mappingOptions.collateTins,
                                                    onCheckedChange = {
                                                        updateCollateTinsOption(it)
                                                    },
                                                    modifier = Modifier,
                                                )
                                                LabeledCheckbox(
                                                    text = "Sync tins?",
                                                    maxLines = 1,
                                                    width = 96.dp,
                                                    checked = mappingOptions.syncTins,
                                                    enabled = mappingOptions.collateTins,
                                                    onCheckedChange = {
                                                        updateSyncTinsOption(it)
                                                    },
                                                )
                                            }
                                            // Warning //
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(0.dp),
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.Center,
                                            ) {
                                                BasicText(
                                                    text = if (mappingOptions.collateTins && importOption == ImportOption.OVERWRITE) {
                                                        "Warning: Overwrite will erase existing tins."
                                                    } else {
                                                        ""
                                                    },
                                                    style = TextStyle(
                                                        color = MaterialTheme.colorScheme.error,
                                                        textAlign = TextAlign.End
                                                    ),
                                                    modifier = Modifier
                                                        .heightIn(max = 48.dp),
                                                    autoSize = TextAutoSize.StepBased(
                                                        minFontSize = 8.sp,
                                                        maxFontSize = 15.sp,
                                                        stepSize = .25.sp,
                                                    ),
                                                    maxLines = 2,
                                                )
                                            }
                                        }
                                        // Existing Records options //
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "Existing records:",
                                                modifier = Modifier
                                                    .padding(end = 8.dp),
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clickable(
                                                            onClick = {
                                                                viewModel.updateImportOption(
                                                                    ImportOption.SKIP
                                                                )
                                                            }
                                                        )
                                                        .width(36.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Skip",
                                                        modifier = Modifier,
                                                        color = if (importOption == ImportOption.SKIP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.7f
                                                        ),
                                                        fontWeight = if (importOption == ImportOption.SKIP) FontWeight.SemiBold else FontWeight.Normal,
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clickable(
                                                            onClick = {
                                                                viewModel.updateImportOption(
                                                                    ImportOption.UPDATE
                                                                )
                                                            }
                                                        )
                                                        .width(56.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Update",
                                                        modifier = Modifier,
                                                        color = if (importOption == ImportOption.UPDATE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.7f
                                                        ),
                                                        fontWeight = if (importOption == ImportOption.UPDATE) FontWeight.SemiBold else FontWeight.Normal,
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clickable(
                                                            onClick = {
                                                                viewModel.updateImportOption(
                                                                    ImportOption.OVERWRITE
                                                                )
                                                            }
                                                        )
                                                        .width(76.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Overwrite",
                                                        modifier = Modifier,
                                                        color = if (importOption == ImportOption.OVERWRITE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.7f
                                                        ),
                                                        fontWeight = if (importOption == ImportOption.OVERWRITE) FontWeight.SemiBold else FontWeight.Normal,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(Modifier.padding(bottom = 16.dp))

                                    // column mapping options //
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 12.dp, top = 8.dp, end = 20.dp, bottom = 16.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                    ) {
                                        // Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            BasicText(
                                                text = "Database Field",
                                                style = LocalTextStyle.current.copy(
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    lineBreak = LineBreak.Paragraph,
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.Start
                                                ),
                                                maxLines = 2,
                                                modifier = Modifier
                                                    .weight(1f),
                                                autoSize = TextAutoSize.StepBased(
                                                    minFontSize = 13.sp,
                                                    maxFontSize = 14.sp,
                                                    stepSize = .25.sp,
                                                )
                                            )
                                            Spacer(Modifier.weight(1.1f))
                                            BasicText(
                                                text = "CSV Column",
                                                style = LocalTextStyle.current.copy(
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    lineBreak = LineBreak.Paragraph,
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.Center
                                                ),
                                                maxLines = 2,
                                                modifier = Modifier
                                                    .weight(1f),
                                                autoSize = TextAutoSize.StepBased(
                                                    minFontSize = 13.sp,
                                                    maxFontSize = 14.sp,
                                                    stepSize = .25.sp,
                                                )
                                            )
                                            Spacer(Modifier.weight(.9f))
                                            BasicText(
                                                text = "Overwrite Allowed",
                                                style = LocalTextStyle.current.copy(
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    lineBreak = LineBreak.Paragraph,
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.End
                                                ),
                                                maxLines = 2,
                                                modifier = Modifier
                                                    .alpha(if (importOption == ImportOption.OVERWRITE) 1f else 0.5f)
                                                    .weight(1f),
                                                autoSize = TextAutoSize.StepBased(
                                                    minFontSize = 13.sp,
                                                    maxFontSize = 14.sp,
                                                    stepSize = .25.sp,
                                                )
                                            )
                                        }

                                        MappingField(
                                            label = "Brand:",
                                            selectedColumn = mappingOptions.brandColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Brand, selectedColumn
                                                )
                                            },
                                            placeholder = "Required",
                                            showCheckbox = false
                                        )
                                        MappingField(
                                            label = "Blend:",
                                            selectedColumn = mappingOptions.blendColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Blend, selectedColumn
                                                )
                                            },
                                            placeholder = "Required",
                                        )
                                        MappingField(
                                            label = "Type:",
                                            selectedColumn = mappingOptions.typeColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Type, selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Type] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Type,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                        MappingField(
                                            label = "Subgenre:",
                                            selectedColumn = mappingOptions.subGenreColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.SubGenre, selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.SubGenre] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.SubGenre,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                        MappingField(
                                            label = "Cut:",
                                            selectedColumn = mappingOptions.cutColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Cut, selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Cut] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Cut,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                        MappingField(
                                            label = "Components:",
                                            selectedColumn = mappingOptions.componentsColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Components,
                                                    selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Components] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Components,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                        MappingField(
                                            label = "Flavoring:",
                                            selectedColumn = mappingOptions.flavoringColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Flavoring,
                                                    selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Flavoring] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Flavoring,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                        MappingField(
                                            label = "No. of Tins:",
                                            selectedColumn = mappingOptions.quantityColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Quantity, selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Quantity] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Quantity,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true,
                                            enabled = !mappingOptions.syncTins
                                        )
                                        MappingField(
                                            label = "Favorite:",
                                            selectedColumn = mappingOptions.favoriteColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Favorite, selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Favorite] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Favorite,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                        MappingField(
                                            label = "Disliked:",
                                            selectedColumn = mappingOptions.dislikedColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Disliked, selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Disliked] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Disliked,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                        MappingField(
                                            label = "Production\nStatus:",
                                            selectedColumn = mappingOptions.productionColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Production,
                                                    selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Production] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Production,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true,
                                            maxLines = 2
                                        )
                                        MappingField(
                                            label = "Notes:",
                                            selectedColumn = mappingOptions.notesColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Notes, selectedColumn
                                                )
                                            },
                                            overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Notes] ?: false,
                                            onOverwrite = {
                                                viewModel.updateOverwriteSelection(
                                                    CsvImportViewModel.CsvField.Notes,
                                                    it
                                                )
                                            },
                                            importOption = importOption,
                                            showCheckbox = true
                                        )
                                    }

                                    // Tins mapping options
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 12.dp, top = 8.dp, end = 20.dp, bottom = 16.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        Text(
                                            text = "Tins Mapping",
                                            modifier = Modifier,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (mappingOptions.collateTins) LocalContentColor.current
                                                else LocalContentColor.current.copy(alpha = 0.5f)
                                        )
                                        MappingField(
                                            label = "Container:",
                                            selectedColumn = mappingOptions.containerColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.Container,
                                                    selectedColumn
                                                )
                                            },
                                            enabled = mappingOptions.collateTins
                                        )
                                        MappingField(
                                            label = "Quantity:",
                                            selectedColumn = mappingOptions.tinQuantityColumn,
                                            csvColumns = csvUiState.columns,
                                            onColumnSelected = { selectedColumn ->
                                                viewModel.updateFieldMapping(
                                                    CsvImportViewModel.CsvField.TinQuantity,
                                                    selectedColumn
                                                )
                                            },
                                            enabled = mappingOptions.collateTins
                                        )

                                        Spacer(
                                            modifier = Modifier
                                                .height(8.dp)
                                        )
                                        var dateFormatSelected by rememberSaveable { mutableStateOf(false) }

                                        DateFormatField(
                                            label = "CSV Date\nFormat:",
                                            selectedFormat = mappingOptions.dateFormat,
                                            onFormatSelected = { selectedFormat ->
                                                viewModel.updateDateFormat(selectedFormat)
                                                dateFormatSelected =
                                                    selectedFormat.isNotBlank()
                                            },
                                            enabled = mappingOptions.collateTins,
                                            modifier = Modifier
                                        )

                                        Box(
                                            modifier = Modifier,
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column {
                                                MappingField(
                                                    label = "Manufacture\nDate:",
                                                    selectedColumn = mappingOptions.manufactureDateColumn,
                                                    csvColumns = csvUiState.columns,
                                                    onColumnSelected = { selectedColumn ->
                                                        viewModel.updateFieldMapping(
                                                            CsvImportViewModel.CsvField.ManufactureDate,
                                                            selectedColumn
                                                        )
                                                    },
                                                    enabled = mappingOptions.collateTins && dateFormatSelected,
                                                    maxLines = 2
                                                )
                                                MappingField(
                                                    label = "Cellar Date:",
                                                    selectedColumn = mappingOptions.cellarDateColumn,
                                                    csvColumns = csvUiState.columns,
                                                    onColumnSelected = { selectedColumn ->
                                                        viewModel.updateFieldMapping(
                                                            CsvImportViewModel.CsvField.CellarDate,
                                                            selectedColumn
                                                        )
                                                    },
                                                    enabled = mappingOptions.collateTins && dateFormatSelected
                                                )
                                                MappingField(
                                                    label = "Open Date:",
                                                    selectedColumn = mappingOptions.openDateColumn,
                                                    csvColumns = csvUiState.columns,
                                                    onColumnSelected = { selectedColumn ->
                                                        viewModel.updateFieldMapping(
                                                            CsvImportViewModel.CsvField.OpenDate,
                                                            selectedColumn
                                                        )
                                                    },
                                                    enabled = mappingOptions.collateTins && dateFormatSelected,
                                                )
                                                MappingField(
                                                    label = "Finished:",
                                                    selectedColumn = mappingOptions.finishedColumn,
                                                    csvColumns = csvUiState.columns,
                                                    onColumnSelected = { selectedColumn ->
                                                        viewModel.updateFieldMapping(
                                                            CsvImportViewModel.CsvField.Finished,
                                                            selectedColumn
                                                        )
                                                    },
                                                    enabled = mappingOptions.collateTins && dateFormatSelected
                                                )
                                            }
                                            if (mappingOptions.collateTins && !dateFormatSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            Color.Black.copy(alpha = 0.50f),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .matchParentSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "(must select date format)",
                                                        modifier = Modifier
                                                            .background(Color.Black.copy(alpha = 0.33f)),
                                                        textAlign = TextAlign.Center,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Confirm and  Import button //
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.confirmImport()
                                            }
                                        },
                                        enabled = csvUiState.isFormValid,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .height(48.dp)
                                            .align(Alignment.CenterHorizontally),
                                    ) {
                                        Text(
                                            text = "Confirm and Import",
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .height(16.dp)
                                    )
                                }
                                Column {
                                    AnimatedVisibility(
                                        visible = showHelp,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 100)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 100))
                                    ) {
                                        CsvHelpBody(
                                            modifier = Modifier
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/** Body Elements */
@Composable
fun LoadErrorDialog(
    confirmError: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.csv_import_error)) },
        modifier = modifier,
        confirmButton = {
            TextButton(onClick = confirmError) {
                Text(stringResource(R.string.ok))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
    )
}


/** Other body conditions */
@Composable
fun CsvHelpBody(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Verifying data integrity
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Verifying Data before Import",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Image(
                painter = painterResource(id = R.drawable.csvhelp_parse_test),
                contentDescription = "Parse test image",
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .wrapContentSize(align = Alignment.Center)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(0.dp),
                contentScale = ContentScale.Inside
            )
            Text(
                text = "After selecting a file, a box will show you the first line of the file " +
                        "(labeled \"Possible header\") and another line (labeled \"Record parse " +
                        "test\"). Use this to verify whether or not there is a header and what " +
                        "a record looks like.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "If there is no header, \"Possible header\" will look like a record. " +
                        "If the record parse test looks wrong, try reformatting your CSV file to " +
                        "RFC 4180 formatting to ensure a successful read.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "In the import options section, there is also a \"Record count\" that shows " +
                        "the number of lines (records) in the file.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp),
                softWrap = true,
            )
        }

        // Import options //
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Import Options",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Image(
                painter = painterResource(id = R.drawable.csvhelp_import_options),
                contentDescription = "Import options image",
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .wrapContentSize(align = Alignment.Center)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(0.dp),
                contentScale = ContentScale.Inside
            )
            Text(
                text = "Use the \"Has header?\" checkbox to skip the first line if it is a header. " +
                        "The record count on the right will decrease by 1 if the \"Has header?\" " +
                        "option is chosen.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "Use the \"Collate tins?\" option to attempt to parse the individual records " +
                        "as tins if your CSV has a separate line for each tin (individual tins are " +
                        "handled separately in the database and attached to entries).",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )

            Text(
                text = "Use the \"Sync tins?\" option to synchronize the \"No. of Tins\" field " +
                        "with the quantities of the imported tins. This will be determined by " +
                        "adding up the total quantity of the tins and dividing it by the tin " +
                        "conversion rates set on the settings screen (default is 1 tin = 1.75 " +
                        "oz or 50 grams).",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )

            Text(
                text = "The next line contains options for handling records that already match " +
                        "entries in the database. Each entry must be a unique combination of Brand " +
                        "+ Blend.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 0.dp),
                softWrap = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "-  ",
                            modifier = modifier
                                .align(Alignment.Start),
                            softWrap = true,
                        )
                    }
                    Column {
                        Text(
                            text = "\"Skip\" will skip matching entries.",
                            modifier = modifier
                                .align(Alignment.Start),
                            softWrap = true,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "-  ",
                            modifier = modifier
                                .align(Alignment.Start),
                            softWrap = true,
                        )
                    }
                    Column {
                        Text(
                            text = "\"Update\" will fill-in only the empty fields in the database " +
                                    "entries.",
                            modifier = modifier
                                .align(Alignment.Start),
                            softWrap = true,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "-  ",
                            modifier = modifier
                                .align(Alignment.Start),
                            softWrap = true,
                        )
                    }
                    Column {
                        Text(
                            text = "\"Overwrite\" will replace the selected fields (checkbox on "+
                                    "the right) with the values in the CSV file, including overwriting " +
                                    "entry data with blank/empty values.",
                            modifier = modifier
                                .align(Alignment.Start),
                            softWrap = true,
                        )
                    }
                }
            }
            Text(
                text = "All three options always import new records, they only affect records that " +
                        "match existing entries in the database.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "If you wish to update tins by existing values with new values from the CSV, " +
                        "use the \"Overwrite\" option, \"Update\" will only fill in values that are " +
                        "blank.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "WARNING: Using \"Overwrite\" with \"Collate tins\" will always delete all " +
                        "existing tins and replace them with tins generated from CSV records. If you " +
                        "wish to update entries while maintaining your existing tins, do not use " +
                        "Collate with Overwrite.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp),
                softWrap = true,
            )
        }

        // Import mapping //
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Import Mapping",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Image(
                painter = painterResource(id = R.drawable.csvhelp_mapping_options),
                contentDescription = "Import mapping image",
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .wrapContentSize(align = Alignment.Center)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(0.dp),
                contentScale = ContentScale.Inside
            )
            Text(
                text = "The labels on the left represent the fields in the database. Use the " +
                        "dropdown boxes to select which CSV column to map to which field. " +
                        "All fields other than \"Brand\" and \"Blend\" are optional.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "The checkboxes to the right are for the \"Overwrite\" option. " +
                        "Only those fields that are checked will be allowed to be overwritten.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "If you select the tin collation option, the Number of Tins field can " +
                        "take a different value than the tin quantity field if you keep the " +
                        "number of tins separate (only whole numbers will map). This field will " +
                        "be disabled if you select the \"Sync Tins?\" option.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp),
                softWrap = true,
            )
        }

        // Tins mapping //
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Tins Mapping",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Text(
                text = "\"Tins\" here represents any container, it doesn't have to actually be " +
                        "in a tin.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "The tins mapping fields are enabled by selecting the \"collate tins\" " +
                        "option. When collating, the CSV is iterated through and 1 tin is added " +
                        "per each line that contains the same Brand and Blend (a tin label will " +
                        "be automatically generated). Upon import, the tins will be linked to " +
                        "their parent Brand + Blend entries.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "Tin collation will only work properly if each individual tin has its " +
                        "own line in the CSV. The \"Skip\" option with tin collation will only " +
                        "create tins for new entries. The \"Update\" option will only create " +
                        "tins for entries that have no attached tins, and the \"Overwrite\" " +
                        "option will erase all existing tins and will create tins for all items" +
                        "regardless of whether or not they had tins before.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "The quantity field will attempt to separate the numerical quantity from " +
                        "the unit and map them to the respective tin fields of \"quantity\" and " +
                        "\"unit\". For instance, if the column containing your tin quantities has " +
                        "a value of \"12 oz\", the database \"quantity\" field will be \"12\" and " +
                        "the \"unit\" field will be oz.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "Date mapping is not guaranteed. Select the date format that was used in " +
                        "the CSV column that contains the date. Several date formats are " +
                        "currently supported. Date formats will work regardless of month/day/year " +
                        "delimiter (/, -, or .), or if days have leading 0's. The date format " +
                        "applies to all three fields.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "Only map dates if your manufacture, cellar or open date values each have " +
                        "their own columns and that the dates themselves are in one column (month " +
                        "day and year). Date tracking where day/month/years are split across " +
                        "different columns is not supported.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
            Text(
                text = "For dates that have two digit years, all years are presumed to be after " +
                        "1900 and before the current year. If the two digit year value is " +
                        "greater than the current year, it's presumed to be 19__; if it is less " +
                        "than or equal to the current year, it is presumed to be 20__.",
                modifier = modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp),
                softWrap = true,
            )
        }
        Spacer(
            modifier = Modifier
                .height(24.dp)
        )
    }
}

@Composable
fun ImportError(
    onTryAgain: () -> Unit,
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .weight(1.5f)
        )
        Text(
            text = "Error importing CSV!",
            modifier = Modifier
                .padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp
        )
        Text(
            text = "Please try again or return to cellar.",
            modifier = Modifier
                .padding(bottom = 16.dp),
            fontSize = 18.sp,
        )
        TextButton(
            onClick = { onTryAgain() },
            modifier = Modifier,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = "Reset form",
                fontSize = 18.sp,
            )
        }
        TextButton(
            onClick = { navigateToHome() },
            modifier = Modifier,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = "Go back to Cellar",
                fontSize = 18.sp,
            )
        }
        Spacer(
            modifier = Modifier
                .weight(2f)
        )
    }
}


/** Custom composables */
// Custom checkbox //
@Composable
fun LabeledCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    enabled: Boolean = true,
    fontColor: Color = LocalContentColor.current,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: CheckboxColors = CheckboxDefaults.colors(),
) {
    Row(
        modifier = modifier
            .padding(start = 1.dp)
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = text,
            modifier = Modifier
                .width(width),
            color = if(!enabled) fontColor.copy(alpha = .5f) else fontColor,
            maxLines = maxLines,
        )
        TriStateCheckbox(
            state = ToggleableState(checked),
            onClick = null,
            interactionSource = interactionSource,
            modifier = Modifier
                .triStateToggleable(
                    state = ToggleableState(checked),
                    onClick = {
                        if (onCheckedChange != null) {
                            run { onCheckedChange(!checked) }
                        }
                    },
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null
                )
                .padding(start = 6.dp, end = 6.dp),
            enabled = enabled,
            colors = colors,
        )
    }
}


// Field mapping composables //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFormatField(
    label: String,
    selectedFormat: String,
    onFormatSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showCheckbox: Boolean = false,
    overwriteSelected: Boolean = false,
    onOverwrite: (Boolean) -> Unit = {},
    importOption: ImportOption = ImportOption.SKIP,
    placeholder: String = "",
) {
    var expanded by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(16.sp) }
    val minFontSize = 11.sp
    var fontMultiplier by remember { mutableFloatStateOf(1f) }
    fun updateFontSize(multiplier: Float) {
        val newSize = fontSize * multiplier
        if (newSize > minFontSize) {
            fontMultiplier = multiplier
        }
    }

    val formats = listOf(
        "01/24 or 01/2024 (MM/YY)",
        "24/01 or 2024/01 (YY/MM)",
        "01/27/24 or 01/27/2024 (MM/DD/YY)",
        "27/01/24 or 27/01/2024 (DD/MM/YY)",
        "24/01/01 or 2024/01/01 (YY/MM/DD)",
        "January 27, 2024 or Jan 27, 2024",
        "27 January, 2024 or 27 Jan, 2024"
    )

    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .height(42.dp)
                    .width(90.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = label,
                    modifier = Modifier,
                    textAlign = TextAlign.Start,
                    softWrap = false,
                    maxLines = 2,
                    overflow = TextOverflow.Visible,
                    style = LocalTextStyle.current.copy(
                        lineHeight = LocalTextStyle.current.lineHeight * fontMultiplier,
                        fontSize = LocalTextStyle.current.fontSize * fontMultiplier,
                    ),
                    onTextLayout = {
                        if (it.hasVisualOverflow) {
                            updateFontSize(fontMultiplier * 0.99f)
                        }
                    },
                    color = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f)
                )
            }
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                ) {
                    OutlinedTextField(
                        value = selectedFormat.ifBlank { "" },
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
                        trailingIcon =
                        {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        placeholder = {
                            Text(text = placeholder)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.38f),
                        ),
                        singleLine = true,
                        enabled = enabled
                        )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = LocalCustomColors.current.textField,
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "") },
                            onClick = {
                                onFormatSelected("")
                                expanded = false
                            }
                        )
                        formats.forEach { format ->
                            DropdownMenuItem(
                                text = { Text(text = format) },
                                onClick = {
                                    onFormatSelected(format)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .width(48.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                if (showCheckbox) {
                    Checkbox(
                        checked = overwriteSelected,
                        onCheckedChange = onOverwrite,
                        modifier = Modifier
                            .offset(x = 6.dp),
                        enabled = importOption == ImportOption.OVERWRITE && enabled,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingField(
    label: String,
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showCheckbox: Boolean = false,
    overwriteSelected: Boolean = false,
    onOverwrite: (Boolean) -> Unit = {},
    importOption: ImportOption = ImportOption.SKIP,
    placeholder: String = "",
    maxLines: Int = 1
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .height(42.dp)
                    .width(90.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicText(
                    text = label,
                    style = TextStyle(
                        color = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f),
                        textAlign = TextAlign.Start,
                        lineBreak = LineBreak.Paragraph
                    ),
                    modifier = Modifier
                        .wrapContentHeight(),
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp, stepSize = .02.sp),
                    maxLines = maxLines,
                )
            }
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedColumn.ifBlank { "" },
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
                        trailingIcon =
                        {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        placeholder = {
                            Text(text = placeholder)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.38f),
                        ),
                        singleLine = true,
                        enabled = enabled
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = LocalCustomColors.current.textField,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "(Blank)",
                                    color = LocalContentColor.current.copy(alpha = 0.5f)
                                )
                            },
                            onClick = {
                                onColumnSelected("")
                                expanded = false
                            },
                        )
                        csvColumns.forEach { column ->
                            DropdownMenuItem(
                                text = { Text(text = column) },
                                onClick = {
                                    onColumnSelected(column)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .width(54.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                if (showCheckbox) {
                    Checkbox(
                        checked = overwriteSelected,
                        onCheckedChange = onOverwrite,
                        modifier = Modifier
                            .offset(x = 6.dp),
                        enabled = importOption == ImportOption.OVERWRITE && enabled,
                    )
                }
            }
        }
    }
}

