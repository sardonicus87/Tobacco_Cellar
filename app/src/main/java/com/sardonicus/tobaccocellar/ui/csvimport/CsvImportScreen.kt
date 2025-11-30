package com.sardonicus.tobaccocellar.ui.csvimport

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.navigation.CsvImportDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportScreen(
    navKey: CsvImportDestination,
    modifier: Modifier = Modifier,
    navigateToCsvHelp: () -> Unit,
    navigateToImportResults: (Int, Int, Int, Int, Int, Boolean, Boolean) -> Unit,
    navigateToHome: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: CsvImportViewModel = viewModel(factory = AppViewModelProvider.Factory, key = navKey.id)
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
                title = stringResource(R.string.csv_import_title),
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
                mappingOptions = mappingOptions,
                importOption = importOption,
                onHeaderChange = { viewModel.updateHeaderOption(it) },
                updateCollateTinsOption = { viewModel.updateCollateTinsOption(it) },
                updateSyncTinsOption = { viewModel.updateSyncTinsOption(it) },
                navigateToCsvHelp = navigateToCsvHelp,
                navigateToImportResults = navigateToImportResults,
                navigateToHome = navigateToHome,
                modifier = modifier
                    .fillMaxSize()
                    .padding(0.dp),
            )
        }
    }
}

@Composable
fun CsvImportBody(
    navigateToImportResults: (Int, Int, Int, Int, Int, Boolean, Boolean) -> Unit,
    navigateToHome: () -> Unit,
    navigateToCsvHelp: () -> Unit,
    onHeaderChange: (isChecked: Boolean) -> Unit,
    updateCollateTinsOption: (isChecked: Boolean) -> Unit,
    updateSyncTinsOption: (Boolean) -> Unit,
    csvImportState: CsvImportState,
    csvUiState: CsvUiState,
    mappingOptions: MappingOptions,
    importOption: ImportOption,
    viewModel: CsvImportViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    val onShowError: (Boolean) -> Unit = { showErrorDialog = it }
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
                                onShowError(true)
                            }

                            is CsvResult.Empty -> {
                                onShowError(true)
                            }
                        }

                    }
                }
            } catch (e: Exception) {
                println("Exception: $e")
                onShowError(true)
            }
        }
    }

    if (showErrorDialog) {
        LoadErrorDialog(
            modifier = Modifier,
            confirmError = { onShowError(false) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        when (importStatus) {
            is ImportStatus.Loading -> { LoadingIndicator() }
            is ImportStatus.Error -> {
                ImportError(
                    onTryAgain = { coroutineScope.launch { viewModel.resetImportState() } },
                    navigateToHome = { navigateToHome() },
                    exception = (importStatus as ImportStatus.Error).exception,
                    modifier = Modifier
                        .fillMaxSize(),
                )
            }
            is ImportStatus.Success -> {}
            else -> {
                // Initial screen load before CSV loaded //
                if (csvUiState.columns.isEmpty()) {
                    Spacer(Modifier.weight(1f))
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
                    Text(
                        text = "* To see help, select a CSV file to start\n(import isn't automatic)",
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(.66f)
                            .align(Alignment.CenterHorizontally),
                        fontSize = 12.sp,
                        color = LocalContentColor.current.copy(alpha = .5f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.weight(2.5f))
                }
                else {
                    Column(
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                    ) { // Screen after CSV loaded //
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
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .5f),
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
                                onClick = { navigateToCsvHelp() },
                                enabled = true,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(40.dp),
                            ) {
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
                                        Text(
                                            text = if (mappingOptions.collateTins && importOption == ImportOption.OVERWRITE) {
                                                "Warning: Overwrite will erase existing tins."
                                            } else { "" },
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
                                        text = "Existing entries:",
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
                                                    indication = LocalIndication.current,
                                                    interactionSource = null
                                                ) {
                                                    viewModel.updateImportOption(
                                                        ImportOption.SKIP
                                                    )
                                                }
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
                                                    indication = LocalIndication.current,
                                                    interactionSource = null
                                                ) {
                                                    viewModel.updateImportOption(
                                                        ImportOption.UPDATE
                                                    )
                                                }
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
                                                    indication = LocalIndication.current,
                                                    interactionSource = null
                                                ) {
                                                    viewModel.updateImportOption(
                                                        ImportOption.OVERWRITE
                                                    )
                                                }
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
                                    .padding(
                                        start = 12.dp,
                                        top = 8.dp,
                                        end = 20.dp,
                                        bottom = 16.dp
                                    )
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
                                    Text(
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
                                    Text(
                                        text = "CSV Column",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph),
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
                                    Text(
                                        text = "Overwrite Allowed",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.End,
                                        style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph),
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
                                            CsvImportViewModel.CsvField.Components, selectedColumn
                                        )
                                    },
                                    overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Components] ?: false,
                                    onOverwrite = {
                                        viewModel.updateOverwriteSelection(
                                            CsvImportViewModel.CsvField.Components, it
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
                                            CsvImportViewModel.CsvField.Flavoring, selectedColumn
                                        )
                                    },
                                    overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Flavoring] ?: false,
                                    onOverwrite = {
                                        viewModel.updateOverwriteSelection(
                                            CsvImportViewModel.CsvField.Flavoring, it
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
                                            CsvImportViewModel.CsvField.Quantity, it
                                        )
                                    },
                                    importOption = importOption,
                                    showCheckbox = true,
                                    enabled = !mappingOptions.syncTins
                                )
                                MappingField(
                                    label = "Rating:",
                                    selectedColumn = mappingOptions.ratingColumn,
                                    csvColumns = csvUiState.columns,
                                    onColumnSelected = { selectedColumn ->
                                        viewModel.updateFieldMapping(
                                            CsvImportViewModel.CsvField.Rating,
                                            selectedColumn
                                        )
                                    },
                                    overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Rating] ?: false,
                                    onOverwrite = {
                                        viewModel.updateOverwriteSelection(
                                            CsvImportViewModel.CsvField.Rating,
                                            it
                                        )
                                    }
                                )

                                val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
                                val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
                                val decimalSeparator = symbols.decimalSeparator.toString()
                                val pattern = remember(decimalSeparator) {
                                    val ds = Regex.escape(decimalSeparator)
                                    Regex("^(\\s*|(\\d*)?($ds\\d{0,2})?)$")
                                }

                                MaxValueField(
                                    label = "Max Possible\nCSV Rating:",
                                    maxValue = mappingOptions.maxValueString,
                                    onMaxValueChange = {
                                        if (it.matches(pattern)) {
                                            var parsedDouble: Double?

                                            if (it.isNotBlank()) {
                                                val preNumber = if (it.startsWith(decimalSeparator)) {
                                                    "0$it"
                                                } else it

                                                val number = numberFormat.parse(preNumber)
                                                parsedDouble = number?.toDouble()
                                            } else { parsedDouble = null }

                                            val maxValueDouble = parsedDouble?.takeIf { it > 0.0 }

                                            viewModel.updateMaxValue(it, maxValueDouble)
                                        }
                                    },
                                    enabled = mappingOptions.ratingColumn.isNotBlank(),
                                    error = mappingOptions.ratingColumn.isNotBlank() && mappingOptions.maxValue == null,
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
                                            CsvImportViewModel.CsvField.Favorite, it
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
                                            CsvImportViewModel.CsvField.Disliked, it
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
                                            CsvImportViewModel.CsvField.Production, selectedColumn
                                        )
                                    },
                                    overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Production] ?: false,
                                    onOverwrite = {
                                        viewModel.updateOverwriteSelection(
                                            CsvImportViewModel.CsvField.Production, it
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
                                            CsvImportViewModel.CsvField.Container, selectedColumn
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
                                            CsvImportViewModel.CsvField.TinQuantity, selectedColumn
                                        )
                                    },
                                    enabled = mappingOptions.collateTins
                                )

                                Spacer(Modifier.height(8.dp))

                                var dateFormatSelected by rememberSaveable { mutableStateOf(false) }

                                DateFormatField(
                                    label = "CSV Date\nFormat:",
                                    selectedFormat = mappingOptions.dateFormat,
                                    onFormatSelected = { selectedFormat ->
                                        viewModel.updateDateFormat(selectedFormat)
                                        dateFormatSelected = selectedFormat.isNotBlank()
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
                                                    CsvImportViewModel.CsvField.ManufactureDate, selectedColumn
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
                                                    CsvImportViewModel.CsvField.CellarDate, selectedColumn
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
                                                    CsvImportViewModel.CsvField.OpenDate, selectedColumn
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
                                                    CsvImportViewModel.CsvField.Finished, selectedColumn
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
                                onClick = { coroutineScope.launch { viewModel.confirmImport() } },
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
                            Spacer(Modifier.height(16.dp))
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
fun ImportError(
    onTryAgain: () -> Unit,
    navigateToHome: () -> Unit,
    exception: Throwable,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
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
            fontSize = 30.sp
        )
        Text(
            text = "Please try again or return to cellar.",
            modifier = Modifier
                .padding(bottom = 16.dp),
            fontSize = 18.sp,
        )
        Text(
            text = "Error for reporting:",
            fontSize = 14.sp
        )
        SelectionContainer {
            Text(
                text = "Exception: ${exception.message ?: " unknown error"},\nCause: ${exception.cause?.message ?: " unknown cause"}",
                fontSize = 14.sp,
                softWrap = true,
                modifier = Modifier
                    .fillMaxWidth(.75f)
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }
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
        Spacer(modifier = Modifier.weight(2f))
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
                    style = TextStyle(
                        color = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f),
                        textAlign = TextAlign.Start,
                        lineBreak = LineBreak.Paragraph
                    ),
                    softWrap = false,
                    maxLines = 2,
                    overflow = TextOverflow.Visible,
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp, stepSize = .02.sp),
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
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
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

@Composable
fun MaxValueField(
    label: String,
    maxValue: String,
    onMaxValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    enabled: Boolean = true,
    showCheckbox: Boolean = false,
    overwriteSelected: Boolean = false,
    onOverwrite: (Boolean) -> Unit = {},
    importOption: ImportOption = ImportOption.SKIP,
) {
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
                    softWrap = true,
                    maxLines = 2,
                    style = TextStyle(
                        color = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f),
                        textAlign = TextAlign.Start,
                        lineBreak = LineBreak.Paragraph
                    ),
                    overflow = TextOverflow.Visible,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 8.sp,
                        maxFontSize = 16.sp,
                        stepSize = .02.sp
                    )
                )
            }
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f)
            ) {
                OutlinedTextField(
                    value = maxValue,
                    onValueChange = {
                        onMaxValueChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    enabled = enabled,
                    isError = error,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                        focusedContainerColor = LocalCustomColors.current.textField,
                        unfocusedContainerColor = LocalCustomColors.current.textField,
                        errorContainerColor = LocalCustomColors.current.textField,
                        disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.38f),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    placeholder = {
                        Text(
                            text = "Required for scaling",
                            color = if (enabled) LocalContentColor.current.copy(alpha = 0.5f) else Color.Transparent,
                            fontSize = 14.sp
                        )
                    }
                )
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
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight by remember { mutableStateOf(screenHeight * .67f) }

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
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
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
                        modifier = Modifier
                            .heightIn(max = maxHeight)
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

