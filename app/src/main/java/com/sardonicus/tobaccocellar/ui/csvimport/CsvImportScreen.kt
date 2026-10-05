package com.sardonicus.tobaccocellar.ui.csvimport

import android.annotation.SuppressLint
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.navigation.CsvImportDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch

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
    viewModel: CsvImportViewModel = viewModel(key = navKey.id)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val csvImportState = viewModel.csvImportState.value
    val csvUiState = viewModel.csvUiState

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                navigateToCsvHelp = navigateToCsvHelp,
                navigateToImportResults = navigateToImportResults,
                navigateToHome = navigateToHome,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun CsvImportBody(
    navigateToImportResults: (Int, Int, Int, Int, Int, Boolean, Boolean) -> Unit,
    navigateToHome: () -> Unit,
    navigateToCsvHelp: () -> Unit,
    csvImportState: CsvImportState,
    csvUiState: CsvUiState,
    viewModel: CsvImportViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val showErrorDialog by viewModel.showErrorDialog.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val context = LocalContext.current
    val csvHelper = CsvHelper()
    val csvEmpty = stringResource(R.string.csv_empty)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            try {
                uri?.let {
                    val contentResolver = context.contentResolver
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        when (val result = csvHelper.csvFileReader(inputStream)) {
                            is CsvResult.Success -> {
                                viewModel.onCsvLoaded(uri, result.header, result.firstFullRecord, result.recordCount)
                                viewModel.generateColumns(result.columnCount)
                            }

                            is CsvResult.Error -> {
                                viewModel.onShowError(true)
                                viewModel.onCsvError(result.exception.message)
                            }

                            is CsvResult.Empty -> {
                                viewModel.onShowError(true)
                                viewModel.onCsvError(csvEmpty)
                            }
                        }

                    }
                }
            } catch (_: Exception) { viewModel.onShowError(true) }
        }
    }
    val onSelectFile = {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "text/*" }
        launcher.launch(intent)
    }

    if (showErrorDialog) {
        LoadErrorDialog(
            viewModel = viewModel,
            confirmError = { viewModel.onShowError(false) },
        )
    }

    LaunchedEffect(viewModel.navigateToResults) {
        viewModel.navigateToResults.collect { results ->
            navigateToImportResults(
                results.totalRecords,
                results.successfulConversions,
                results.successfulInsertions,
                results.successfulUpdates,
                results.successfulTins,
                results.updateFlag,
                results.tinFlag,
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
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
                        onClick = onSelectFile,
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
                        text = stringResource(R.string.to_see_help),
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
                    CsvLoadedBody(
                        viewModel = viewModel,
                        csvImportState = csvImportState,
                        csvUiState = csvUiState,
                        onSelectFile = onSelectFile,
                        navigateToCsvHelp = navigateToCsvHelp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@Composable
fun CsvLoadedBody(
    viewModel: CsvImportViewModel,
    csvImportState: CsvImportState,
    csvUiState: CsvUiState,
    onSelectFile: () -> Unit,
    navigateToCsvHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val importOption by viewModel.importOption.collectAsState()
    val overwriteSelections by viewModel.overwriteSelections.collectAsState()
    val mappingOptions = viewModel.mappingOptions

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) { // Screen after CSV loaded //
        Spacer(Modifier.height(12.dp))
        // Select CSV and Help buttons //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onSelectFile,
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.help_button),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.help_outline),
                        contentDescription = "Help?",
                        modifier = Modifier.size(16.dp),
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
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.possible_header),
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = csvImportState.header.joinToString(", "),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                )
                Text(
                    text = stringResource(R.string.possible_record),
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                )
                val parseTest =
                    if (csvImportState.firstFullRecord.isEmpty()) stringResource(R.string.parse_error)
                    else csvImportState.firstFullRecord.joinToString(", ")

                Text(
                    text = parseTest,
                    modifier = Modifier.padding(start = 8.dp),
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
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        LabeledCheckbox(
                            text = stringResource(R.string.has_header),
                            width = 96.dp,
                            checked = mappingOptions.hasHeader,
                            onCheckedChange = viewModel::updateHeaderOption
                        )
                    }
                    // record count //
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text =
                                if (mappingOptions.hasHeader) {
                                    stringResource(R.string.record_count, "${csvImportState.recordCount - 1}")
                                } else { stringResource(R.string.record_count, "${csvImportState.recordCount}") },
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
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                    ) {
                        LabeledCheckbox(
                            text = stringResource(R.string.collate_tins),
                            maxLines = 1,
                            width = 96.dp,
                            checked = mappingOptions.collateTins,
                            onCheckedChange = viewModel::updateCollateTinsOption
                        )
                        LabeledCheckbox(
                            text = stringResource(R.string.sync_tins_q),
                            maxLines = 1,
                            width = 96.dp,
                            checked = mappingOptions.syncTins,
                            enabled = mappingOptions.collateTins,
                            onCheckedChange = viewModel::updateSyncTinsOption,
                        )
                    }
                    // Warning //
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = if (mappingOptions.collateTins && importOption == ImportOption.OVERWRITE) {
                                stringResource(R.string.overwrite_warning) } else { "" },
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.heightIn(max = 48.dp),
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
                    Text(stringResource(R.string.existing_entries))
                    Row(
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(25))
                                .clickable(null, LocalIndication.current) { viewModel.updateImportOption(ImportOption.SKIP) }
                                .padding(horizontal = 8.dp)
                                .width(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.skip),
                                modifier = Modifier,
                                color =
                                    if (importOption == ImportOption.SKIP) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                fontWeight =
                                    if (importOption == ImportOption.SKIP) FontWeight.SemiBold
                                    else FontWeight.Normal,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 13.sp,
                                    maxFontSize = 16.sp,
                                    stepSize = .25.sp,
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(25))
                                .clickable(null, LocalIndication.current) { viewModel.updateImportOption(ImportOption.UPDATE) }
                                .padding(horizontal = 8.dp)
                                .width(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.update),
                                color =
                                    if (importOption == ImportOption.UPDATE) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                fontWeight =
                                    if (importOption == ImportOption.UPDATE) FontWeight.SemiBold
                                    else FontWeight.Normal,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 13.sp,
                                    maxFontSize = 16.sp,
                                    stepSize = .25.sp,
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(25))
                                .clickable(null, LocalIndication.current) { viewModel.updateImportOption(ImportOption.OVERWRITE) }
                                .padding(horizontal = 8.dp)
                                .width(76.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.overwrite),
                                color =
                                    if (importOption == ImportOption.OVERWRITE) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                fontWeight =
                                    if (importOption == ImportOption.OVERWRITE) FontWeight.SemiBold
                                    else FontWeight.Normal,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 13.sp,
                                    maxFontSize = 16.sp,
                                    stepSize = .25.sp,
                                )
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
                    Text(
                        text = stringResource(R.string.db_field),
                        style = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            lineBreak = LineBreak.Paragraph,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f),
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 13.sp,
                            maxFontSize = 14.sp,
                            stepSize = .25.sp,
                        )
                    )
                    Spacer(Modifier.weight(1.1f))
                    Text(
                        text = stringResource(R.string.csv_column),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph),
                        maxLines = 2,
                        modifier = Modifier.weight(1f),
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 13.sp,
                            maxFontSize = 14.sp,
                            stepSize = .25.sp,
                        )
                    )
                    Spacer(Modifier.weight(.9f))
                    Text(
                        text = stringResource(R.string.overwrite_allowed),
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

                val brand = stringResource(R.string.brand_label)
                val blend = stringResource(R.string.blend_label)
                val required = stringResource(R.string.required)
                val type = stringResource(R.string.type_label)
                val subgenre = stringResource(R.string.subgenre_label)
                val cut = stringResource(R.string.cut_label)
                val components = stringResource(R.string.component_label)
                val flavoring = stringResource(R.string.flavoring_label)
                val noOfTins = stringResource(R.string.no_of_tins_label)
                val rating = stringResource(R.string.rating_label)
                val favorite = stringResource(R.string.favorite_label)
                val disliked = stringResource(R.string.disliked_label)
                val production = stringResource(R.string.production_status_label2)
                val notes = stringResource(R.string.notes_label)

                val mainFields = remember(mappingOptions.syncTins) {
                    listOf(
                        FieldConfig(CsvField.Brand, brand, showCheckbox = false, placeholder = required),
                        FieldConfig(CsvField.Blend, blend, showCheckbox = false, placeholder = required),
                        FieldConfig(CsvField.Type, type),
                        FieldConfig(CsvField.SubGenre, subgenre),
                        FieldConfig(CsvField.Cut, cut),
                        FieldConfig(CsvField.Components, components),
                        FieldConfig(CsvField.Flavoring, flavoring),
                        FieldConfig(CsvField.Quantity, noOfTins, enabled = !mappingOptions.syncTins),
                        FieldConfig(CsvField.Rating, rating),
                        FieldConfig(CsvField.Favorite, favorite),
                        FieldConfig(CsvField.Disliked, disliked),
                        FieldConfig(CsvField.Production, production, maxLines = 2),
                        FieldConfig(CsvField.Notes, notes)
                    )
                }

                mainFields.forEach { config ->
                    MappingField(
                        label = config.label,
                        selectedColumn = mappingOptions.columnMap[config.field] ?: "",
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { viewModel.updateFieldMapping(config.field, it) },
                        enabled = config.enabled,
                        showCheckbox = config.showCheckbox,
                        overwriteSelected = overwriteSelections[config.field] ?: false,
                        onOverwrite = { viewModel.updateOverwriteSelection(config.field, it) },
                        importOption = importOption,
                        placeholder = config.placeholder,
                        maxLines = config.maxLines
                    )

                    if (config.field == CsvField.Rating) {
                        val ratingNotBlank = mappingOptions.columnMap[CsvField.Rating]?.isNotBlank() == true

                        MaxValueField(
                            maxValue = mappingOptions.maxValueString,
                            onMaxValueChange = viewModel::updateMaxValue,
                            enabled = ratingNotBlank,
                            error = ratingNotBlank && mappingOptions.maxValue == null,
                        )
                    }
                }
            }

            // Tins mapping options
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, end = 20.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val dateFormatSelected = remember(mappingOptions.dateFormat) { mappingOptions.dateFormat.isNotBlank() }
                Text(
                    text = stringResource(R.string.tins_mapping),
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = if (mappingOptions.collateTins) LocalContentColor.current
                    else LocalContentColor.current.copy(alpha = 0.5f)
                )
                MappingField(
                    label = stringResource(R.string.container_label),
                    selectedColumn = mappingOptions.columnMap[CsvField.Container] ?: "",
                    csvColumns = csvUiState.columns,
                    onColumnSelected = { viewModel.updateFieldMapping(CsvField.Container, it) },
                    enabled = mappingOptions.collateTins
                )
                MappingField(
                    label = stringResource(R.string.quantity_label),
                    selectedColumn = mappingOptions.columnMap[CsvField.TinQuantity] ?: "",
                    csvColumns = csvUiState.columns,
                    onColumnSelected = { viewModel.updateFieldMapping(CsvField.TinQuantity, it) },
                    enabled = mappingOptions.collateTins
                )

                DateFormatField(
                    selectedFormat = mappingOptions.dateFormat,
                    onFormatSelected = viewModel::updateDateFormat,
                    enabled = mappingOptions.collateTins,
                    modifier = Modifier
                )

                Box(contentAlignment = Alignment.Center) {
                    Column {
                        MappingField(
                            label = stringResource(R.string.csv_manuf_label),
                            selectedColumn = mappingOptions.columnMap[CsvField.ManufactureDate] ?: "",
                            csvColumns = csvUiState.columns,
                            onColumnSelected = { viewModel.updateFieldMapping(CsvField.ManufactureDate, it) },
                            enabled = mappingOptions.collateTins && dateFormatSelected,
                            maxLines = 2
                        )
                        MappingField(
                            label = stringResource(R.string.cellar_date_label),
                            selectedColumn = mappingOptions.columnMap[CsvField.CellarDate] ?: "",
                            csvColumns = csvUiState.columns,
                            onColumnSelected = { viewModel.updateFieldMapping(CsvField.CellarDate, it) },
                            enabled = mappingOptions.collateTins && dateFormatSelected
                        )
                        MappingField(
                            label = stringResource(R.string.open_date_label),
                            selectedColumn = mappingOptions.columnMap[CsvField.OpenDate] ?: "",
                            csvColumns = csvUiState.columns,
                            onColumnSelected = { viewModel.updateFieldMapping(CsvField.OpenDate, it) },
                            enabled = mappingOptions.collateTins && dateFormatSelected,
                        )
                    }
                    if (mappingOptions.collateTins && !dateFormatSelected) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(4.dp))
                                .matchParentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.must_date_format),
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.33f)),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                MappingField(
                    label = stringResource(R.string.finished_label),
                    selectedColumn = mappingOptions.columnMap[CsvField.Finished] ?: "",
                    csvColumns = csvUiState.columns,
                    onColumnSelected = { viewModel.updateFieldMapping(CsvField.Finished, it) },
                    enabled = mappingOptions.collateTins
                )
            }

            // Confirm and  Import button //
            Button(
                onClick = { coroutineScope.launch { viewModel.confirmImport(context.contentResolver) } },
                enabled = csvUiState.isFormValid,
                modifier = Modifier
                    .padding(8.dp)
                    .height(48.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(R.string.confirm_import),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}


/** Body Elements */
@Composable
private fun LoadErrorDialog(
    viewModel: CsvImportViewModel,
    confirmError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorMessage by viewModel.csvErrorMessage.collectAsState()

    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = {
            Column(verticalArrangement = Arrangement.Top) {
                Text(
                    text = stringResource(R.string.csv_import_error),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(stringResource(R.string.reported_error), fontSize = 15.sp)
                Text(errorMessage, fontSize = 15.sp)
            }
        },
        modifier = modifier,
        confirmButton = { TextButton(onClick = confirmError) { Text(stringResource(R.string.ok)) } },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground
    )
}


/** Other body conditions */
@Composable
private fun ImportError(
    onTryAgain: () -> Unit,
    navigateToHome: () -> Unit,
    exception: Throwable,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1.5f))
        Text(
            text = stringResource(R.string.csv_import_error),
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        Text(
            text = stringResource(R.string.csv_try_again),
            modifier = Modifier.padding(bottom = 16.dp),
            fontSize = 18.sp,
        )
        Text(
            text = stringResource(R.string.error_for_reporting),
            fontSize = 14.sp
        )
        SelectionContainer {
            Text(
                text = stringResource(R.string.error_cause, exception.message ?: "unknown error", exception.cause?.message ?: "unknown cause"),
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
            shape = MaterialTheme.shapes.small,
        ) { Text(stringResource(R.string.reset_form), fontSize = 18.sp) }
        TextButton(
            onClick = { navigateToHome() },
            modifier = Modifier,
            shape = MaterialTheme.shapes.small,
        ) { Text(stringResource(R.string.go_back_cellar), fontSize = 18.sp) }
        Spacer(Modifier.weight(2f))
    }
}


/** Custom composables */
// Custom checkbox //
@Composable
private fun LabeledCheckbox(
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
            .height(24.dp)
            .clickable(
                indication = null,
                interactionSource = null,
                enabled = enabled
            ) { onCheckedChange?.invoke(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = text,
            modifier = Modifier.width(width),
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
                    onClick = { if (onCheckedChange != null) { run { onCheckedChange(!checked) } } },
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
private fun DateFormatField(
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

    Column(modifier.padding(vertical = 4.dp)) {
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
                    text = stringResource(R.string.csv_date_format),
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
                    .weight(1f)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFormat.ifBlank { "" },
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        placeholder = { Text(placeholder) },
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
                            text = { Text("") },
                            onClick = {
                                onFormatSelected("")
                                expanded = false
                            }
                        )
                        formats.forEach { format ->
                            DropdownMenuItem(
                                text = { Text(format) },
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
                modifier = Modifier.width(54.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                if (showCheckbox) {
                    Checkbox(
                        checked = overwriteSelected,
                        onCheckedChange = onOverwrite,
                        modifier = Modifier.offset(x = 6.dp),
                        enabled = importOption == ImportOption.OVERWRITE && enabled,
                    )
                }
            }
        }
    }
}


@Composable
private fun MaxValueField(
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
    Column(modifier.padding(vertical = 4.dp)) {
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
                    text = stringResource(R.string.csv_max_rating),
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
            Box(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = maxValue,
                    onValueChange = onMaxValueChange,
                    modifier = Modifier.fillMaxWidth(),
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
                            text = stringResource(R.string.required_scaling),
                            color = if (enabled) LocalContentColor.current.copy(alpha = 0.5f) else Color.Transparent,
                            fontSize = 14.sp
                        )
                    }
                )
            }
            Column(
                modifier = Modifier.width(54.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                if (showCheckbox) {
                    Checkbox(
                        checked = overwriteSelected,
                        onCheckedChange = onOverwrite,
                        modifier = Modifier.offset(x = 6.dp),
                        enabled = importOption == ImportOption.OVERWRITE && enabled,
                    )
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingField(
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

    Column(modifier.padding(vertical = 4.dp)) {
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
                    modifier = Modifier.wrapContentHeight(),
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp, stepSize = .02.sp),
                    maxLines = maxLines,
                )
            }
            Box(Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedColumn.ifBlank { "" },
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        placeholder = { Text(placeholder) },
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
                        modifier = Modifier.heightIn(max = maxHeight)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.blank),
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
                modifier = Modifier.width(54.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                if (showCheckbox) {
                    Checkbox(
                        checked = overwriteSelected,
                        onCheckedChange = onOverwrite,
                        modifier = Modifier.offset(x = 6.dp),
                        enabled = importOption == ImportOption.OVERWRITE && enabled,
                    )
                }
            }
        }
    }
}

private data class FieldConfig(
    val field: CsvField,
    val label: String,
    val showCheckbox: Boolean = true,
    val placeholder: String = "",
    val maxLines: Int = 1,
    val enabled: Boolean = true
)