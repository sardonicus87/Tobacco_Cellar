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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.CsvResult
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
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
    navigateToImportResults: (Int, Int, Int, Int) -> Unit,
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
                mappingOptions = mappingOptions,
                importOption = importOption,
                onHeaderChange = { isChecked -> viewModel.updateHeaderOptions(isChecked) },
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
    navigateToImportResults: (Int, Int, Int, Int) -> Unit,
    navigateToHome: () -> Unit,
    onHeaderChange: (isChecked: Boolean) -> Unit,
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
                println("Error parsing CSV: ${e.message}")
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
                success.successfulUpdates
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
            is ImportStatus.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1.5f)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(0.dp)
                            .size(48.dp)
                            .weight(0.5f),
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(2f)
                    )
                }
            }
            is ImportStatus.Error -> {
                ImportError(
                    onTryAgain = {
                        coroutineScope.launch {
                            viewModel.resetImportState()
                        }
                    },
                    navigateToHome = { navigateToHome() },
                    modifier = Modifier,
                )
            }
            is ImportStatus.Success -> {}
            else -> {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 0.dp)
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
                            Spacer(
                                modifier = Modifier
                                    .height(12.dp)
                            )
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

                            AnimatedVisibility(
                                visible = showHelp,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 300))
                            ) {
                                CsvHelpBody(
                                    modifier = Modifier
                                )
                            }
                            if (!showHelp) {
                                // CSV parse test //
                                Column(
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(
                                            top = 8.dp,
                                            bottom = 8.dp,
                                            start = 12.dp,
                                            end = 12.dp
                                        ),
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
                                            .padding(
                                                start = 8.dp,
                                                top = 0.dp,
                                                end = 0.dp,
                                                bottom = 8.dp
                                            ),
                                    )
                                    Text(
                                        text = stringResource(R.string.possible_record),
                                        modifier = Modifier
                                            .padding(0.dp)
                                            .fillMaxWidth(),
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = csvImportState.firstFullRecord.joinToString(", "),
                                        modifier = Modifier
                                            .padding(
                                                start = 8.dp,
                                                top = 0.dp,
                                                end = 0.dp,
                                                bottom = 0.dp
                                            ),
                                    )
                                }

                                // Import options //
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .padding(
                                            start = 8.dp,
                                            top = 8.dp,
                                            end = 8.dp,
                                            bottom = 8.dp
                                        ),
                                ) {
                                    // Has header option and record count //
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 0.dp,
                                                top = 0.dp,
                                                end = 0.dp,
                                                bottom = 8.dp
                                            ),
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
                                                checked = mappingOptions.hasHeader,
                                                onCheckedChange = { isChecked ->
                                                    onHeaderChange(isChecked)
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
                                    // Existing Records options //
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 0.dp,
                                                top = 0.dp,
                                                end = 0.dp,
                                                bottom = 0.dp
                                            ),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            8.dp,
                                            Alignment.Start
                                        ),
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

                                // column mapping options //
                                Column(
                                    modifier = Modifier
                                        .padding(
                                            start = 24.dp,
                                            top = 8.dp,
                                            end = 24.dp,
                                            bottom = 16.dp
                                        )
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    MappingField(
                                        label = "Brand: ",
                                        selectedColumn = mappingOptions.brandColumn,
                                        csvColumns = csvUiState.columns,
                                        onColumnSelected = { selectedColumn ->
                                            viewModel.updateMappingOptions(
                                                CsvImportViewModel.CsvField.Brand, selectedColumn
                                            )
                                        },
                                        placeholder = "Required",
                                        showCheckbox = false
                                    )
                                    MappingField(
                                        label = "Blend: ",
                                        selectedColumn = mappingOptions.blendColumn,
                                        csvColumns = csvUiState.columns,
                                        onColumnSelected = { selectedColumn ->
                                            viewModel.updateMappingOptions(
                                                CsvImportViewModel.CsvField.Blend, selectedColumn
                                            )
                                        },
                                        placeholder = "Required",
                                    )
                                    MappingField(
                                        label = "Type: ",
                                        selectedColumn = mappingOptions.typeColumn,
                                        csvColumns = csvUiState.columns,
                                        onColumnSelected = { selectedColumn ->
                                            viewModel.updateMappingOptions(
                                                CsvImportViewModel.CsvField.Type, selectedColumn
                                            )
                                        },
                                        overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Type]
                                            ?: false,
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
                                        label = "Quantity: ",
                                        selectedColumn = mappingOptions.quantityColumn,
                                        csvColumns = csvUiState.columns,
                                        onColumnSelected = { selectedColumn ->
                                            viewModel.updateMappingOptions(
                                                CsvImportViewModel.CsvField.Quantity, selectedColumn
                                            )
                                        },
                                        overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Quantity]
                                            ?: false,
                                        onOverwrite = {
                                            viewModel.updateOverwriteSelection(
                                                CsvImportViewModel.CsvField.Quantity,
                                                it
                                            )
                                        },
                                        importOption = importOption,
                                        showCheckbox = true
                                    )
                                    MappingField(
                                        label = "Favorite: ",
                                        selectedColumn = mappingOptions.favoriteColumn,
                                        csvColumns = csvUiState.columns,
                                        onColumnSelected = { selectedColumn ->
                                            viewModel.updateMappingOptions(
                                                CsvImportViewModel.CsvField.Favorite, selectedColumn
                                            )
                                        },
                                        overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Favorite]
                                            ?: false,
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
                                        label = "Disliked: ",
                                        selectedColumn = mappingOptions.dislikedColumn,
                                        csvColumns = csvUiState.columns,
                                        onColumnSelected = { selectedColumn ->
                                            viewModel.updateMappingOptions(
                                                CsvImportViewModel.CsvField.Disliked, selectedColumn
                                            )
                                        },
                                        overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Disliked]
                                            ?: false,
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
                                        label = "Notes: ",
                                        selectedColumn = mappingOptions.notesColumn,
                                        csvColumns = csvUiState.columns,
                                        onColumnSelected = { selectedColumn ->
                                            viewModel.updateMappingOptions(
                                                CsvImportViewModel.CsvField.Notes, selectedColumn
                                            )
                                        },
                                        overwriteSelected = overwriteSelections[CsvImportViewModel.CsvField.Notes]
                                            ?: false,
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
){
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "When you import a file, the box at the top of the screen...",
            modifier = modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp),
            softWrap = true,
        )
        Image(
            painter = painterResource(id = R.drawable.csvhelp_parse_test),
            contentDescription = "Parse test image",
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .padding(0.dp),
            contentScale = ContentScale.Inside
        )
        Text(
            text = "...will show you the first line of the file (labeled \"Possible header\") " +
                    "and another line after (labeled \"Record parse test\"). This is to verify " +
                    "whether or not there is a header and what a record looks like.\n\nIf there " +
                    "is no header, the \"Possible header\" line will look like a record. If the " +
                    "record parse test looks wrong, try reformatting your CSV file to " +
                    "RFC 4180 formatting to ensure a successful read.\n\nBelow this are " +
                    "the import options.",
            modifier = modifier
                .align(Alignment.Start)
                .padding(top = 12.dp, bottom = 8.dp),
            softWrap = true,
        )
        Image(
            painter = painterResource(id = R.drawable.csvhelp_import_options),
            contentDescription = "Import options image",
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .padding(0.dp),
            contentScale = ContentScale.Inside
        )
        Text(
            text = "Use the \"Has header?\" checkbox to skip the first line if it is a header. " +
                    "The record count on the right will show the number of records found in the " +
                    "CSV (minus the header if the option is selected for that). Use this as well " +
                    "to ensure the CSV file is being properly read.\n\nThe next line contains " +
                    "options for handling records that already match entries in the database:",
            modifier = modifier
                .align(Alignment.Start)
                .padding(top = 12.dp, bottom = 0.dp),
            softWrap = true,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
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
                        text = "\"Update\" will fill-in/update only the empty fields in the " +
                                "database.",
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
                                "entry data with blank/empty values.\n",
                        modifier = modifier
                            .align(Alignment.Start),
                        softWrap = true,
                    )
                }
            }
        }
        Text(
            text = "All three options always import new records, they only affect records that " +
                    "match existing entries in the database.\n\nFinally, the import mapping is " +
                    "the last step.",
            modifier = modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp),
            softWrap = true,
        )
        Image(
            painter = painterResource(id = R.drawable.csvhelp_mapping_options),
            contentDescription = "Import mapping image",
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .padding(0.dp),
            contentScale = ContentScale.Inside
        )
        Text(
            text = "The labels on the left represent the fields in the database. " +
                    "Please use the dropdown boxes in the center to select which column in the " +
                    "CSV file to map to the given database field. All fields other than \"Brand\"" +
                    " and \"Blend\" are optional.",
            modifier = modifier
                .align(Alignment.Start)
                .padding(top = 12.dp, bottom = 8.dp),
            softWrap = true,
        )
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
    enabled: Boolean = true,
    fontColor: Color = LocalContentColor.current,
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
            modifier = Modifier,
            color = fontColor,
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
fun MappingField (
    label: String,
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showCheckbox: Boolean = false,
    overwriteSelected: Boolean = false,
    onOverwrite: (Boolean) -> Unit = {},
    importOption: ImportOption = ImportOption.SKIP,
    placeholder: String = "",
) {
    var expanded by remember { mutableStateOf(false) }


    Column {
        Row (
            modifier = modifier
                .padding(0.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .padding(0.dp)
                    //    .weight(1f)
                    .width(90.dp),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    //  .weight(2f)
                    .weight(1f)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = modifier
                ) {
                    OutlinedTextField(
                        value = selectedColumn.ifBlank { "" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        trailingIcon =
                        {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        placeholder = { Text(text = placeholder) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField,
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = LocalCustomColors.current.textField,
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "") },
                            onClick = {
                                onColumnSelected("")
                                expanded = false
                            }
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
                //    .weight(.5f)
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
                        enabled = importOption == ImportOption.OVERWRITE,
                    )
                }
            }
        }
    }
}



