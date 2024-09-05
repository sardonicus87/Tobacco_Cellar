package com.example.tobaccocellar.ui.csvimport

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.CsvHelper
import com.example.tobaccocellar.data.CsvResult
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object CsvImportDestination : NavigationDestination {
    override val route = "csv_import"
    override val titleRes = R.string.csv_import_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportScreen(
    modifier: Modifier = Modifier,
    navigateToImportResults: (Int, Int, Int) -> Unit,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: CsvImportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val csvImportState = viewModel.csvImportState.value
    val csvUiState = viewModel.csvUiState
    val mappingOptions = viewModel.mappingOptions

    LaunchedEffect(key1 = csvImportState){ // Add LaunchedEffect here
        Log.d("CsvImportScreen", "State updated: $csvImportState")
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(CsvImportDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp,
                navigateToCsvImport = {},
                navigateToSettings = {},
                showMenu = false,
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
        ) {
            CsvImportBody(
                csvImportState = csvImportState,
                viewModel = viewModel,
                csvUiState = csvUiState,
                mappingOptions = mappingOptions,
                onHeaderChange = { isChecked -> viewModel.updateHeaderOptions(isChecked) },
                navigateToImportResults = navigateToImportResults,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                contentPadding = innerPadding
            )
        }
    }
}

@Composable
fun CsvImportBody(
    modifier: Modifier = Modifier,
    navigateToImportResults: (Int, Int, Int) -> Unit,
    onHeaderChange: (isChecked: Boolean) -> Unit,
    csvImportState: CsvImportState,
    csvUiState: CsvUiState,
    mappingOptions: MappingOptions,
    viewModel: CsvImportViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val coroutineScope = rememberCoroutineScope()
    val importStatus by viewModel.importStatus.collectAsState()
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
                                    result.firstRecord,
                                    result.allRecords,
                                    result.recordCount
                                )
                                viewModel.generateColumns(
                                    result.columnCount
                                )
                            }

                            is CsvResult.Error -> {
                                /*TODO show error dialog*/
                            }

                            is CsvResult.Empty -> {
                                /*TODO show error dialog*/
                            }
                        }

                    }
                }
            } catch (e: Exception) {
                println("Error parsing CSV: ${e.message}")
            }
        }
    }

    if (importStatus is ImportStatus.Loading) {
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
    else {
        Column(
            modifier = modifier
                .padding(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 0.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(
                modifier = modifier
                    .height(12.dp)
            )
            Text(
                text = stringResource(R.string.csv_import_instructions),
                modifier = modifier
                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 8.dp),
                softWrap = true,
                textAlign = TextAlign.Justify,
            )
            Button(
                onClick = { launcher.launch(intent) },
                enabled = true,
                modifier = modifier
                    .padding(8.dp)
                    .height(40.dp)
            ) {
                Text(text = stringResource(R.string.select_csv))
            }
            Spacer(
                modifier = modifier
                    .height(8.dp)
            )
// Imported CSV data //
            if (csvUiState.columns.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.possible_header),
                    modifier = modifier
                        .padding(0.dp)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                )
                Log.d("header in CsvImportBody", "header: ${csvImportState.header}")
                Text(
                    text = csvImportState.header.joinToString(", "),
                    modifier = modifier
                        .padding(start = 8.dp, top = 0.dp, end = 0.dp, bottom = 8.dp),
                )
                Text(
                    text = stringResource(R.string.possible_record),
                    modifier = modifier
                        .padding(0.dp)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                )
                Log.d(
                    "default format record in CsvImportBody",
                    "firstRecord: ${csvImportState.firstRecord}"
                )
                Text(
                    text = csvImportState.firstRecord.joinToString(", "),
                    modifier = modifier
                        .padding(start = 8.dp, top = 0.dp, end = 0.dp, bottom = 16.dp),
                )
                Text(
                    text = stringResource(R.string.csv_import_mapping),
                    modifier = modifier
                        .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 8.dp),
                    softWrap = true,
                    textAlign = TextAlign.Justify,
                )
                // has header option //
                Row (
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // checkbox
                    Spacer(
                        modifier = modifier
                            .weight(.1f)
                    )
                    Column (
                        modifier = modifier
                            .padding(0.dp)
                            .weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Row(
                            modifier = modifier
                                .padding(0.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Has header?",
                                modifier = modifier
                            )
                            Checkbox(
                                checked = mappingOptions.hasHeader,
                                onCheckedChange = { isChecked ->
                                    onHeaderChange(isChecked)
                                },
                                modifier = modifier
                            )
                        }
                    }
                    Spacer(
                        modifier = modifier
                            .weight(.5f)
                    )
                    // record count
                    Column (
                        modifier = modifier
                            .padding(0.dp)
                            .weight(1.5f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text =
                            if (mappingOptions.hasHeader) {
                                "Record count: ${csvImportState.recordCount - 1}"
                            } else {
                                "Record count: ${csvImportState.recordCount}"
                            },
                            modifier = modifier
                        )
                    }
                    Spacer(
                        modifier = modifier
                            .weight(.1f)
                    )
                }
                // column mapping options //
                Column (
                    modifier = modifier
                        .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandField(
                        selectedColumn = mappingOptions.brandColumn,
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { selectedColumn -> viewModel.updateMappingOptions(
                            CsvImportViewModel.CsvField.Brand, selectedColumn
                        ) },
                    )
                    BlendField(
                        selectedColumn = mappingOptions.blendColumn,
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { selectedColumn -> viewModel.updateMappingOptions(
                            CsvImportViewModel.CsvField.Blend, selectedColumn
                        ) },
                    )
                    TypeField(
                        selectedColumn = mappingOptions.typeColumn,
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { selectedColumn -> viewModel.updateMappingOptions(
                            CsvImportViewModel.CsvField.Type, selectedColumn
                        ) },
                    )
                    QuantityField(
                        selectedColumn = mappingOptions.quantityColumn,
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { selectedColumn -> viewModel.updateMappingOptions(
                            CsvImportViewModel.CsvField.Quantity, selectedColumn
                        ) },
                    )
                    FavoriteField(
                        selectedColumn = mappingOptions.favoriteColumn,
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { selectedColumn -> viewModel.updateMappingOptions(
                            CsvImportViewModel.CsvField.Favorite, selectedColumn
                        ) },
                    )
                    DislikedField(
                        selectedColumn = mappingOptions.dislikedColumn,
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { selectedColumn -> viewModel.updateMappingOptions(
                            CsvImportViewModel.CsvField.Disliked, selectedColumn
                        ) },
                    )
                    NotesField(
                        selectedColumn = mappingOptions.notesColumn,
                        csvColumns = csvUiState.columns,
                        onColumnSelected = { selectedColumn -> viewModel.updateMappingOptions(
                            CsvImportViewModel.CsvField.Notes, selectedColumn
                        ) },
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
                    modifier = modifier
                        .padding(8.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Confirm and Import")
                }
                Spacer(
                    modifier = modifier
                        .height(12.dp)
                )
                when (importStatus) {
                    is ImportStatus.Success -> {
                        val success = importStatus as ImportStatus.Success
                        navigateToImportResults(
                            success.totalRecords,
                            success.successfulConversions,
                            success.successfulInsertions
                        )
                    }
                    is ImportStatus.Error -> Text(text = "Import failed")
                    else -> {}
                }
            }
        }
    }
}

// Field mapping composables //

@Composable
fun BrandField (
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row (
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(fraction = .7f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Brand: ",
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(2f)
            ) {
                OutlinedTextField(
                    value = selectedColumn.ifBlank { "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable{ expanded = !expanded }
                        )
                    },
                    placeholder = { Text(text = "Required") },
                    modifier = Modifier
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
    }
}

@Composable
fun BlendField (
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row (
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(fraction = .7f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Blend: ",
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(2f)
            ) {
                OutlinedTextField(
                    value = selectedColumn.ifBlank { "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                        )
                    },
                    placeholder = { Text(text = "Required") },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
    }
}

@Composable
fun TypeField (
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row (
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(fraction = .7f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Type: ",
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(2f)
            ) {
                OutlinedTextField(
                    value = selectedColumn.ifBlank { "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                        )
                    },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
    }
}

@Composable
fun QuantityField (
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row (
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(fraction = .7f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Quantity: ",
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(2f)
            ) {
                OutlinedTextField(
                    value = selectedColumn.ifBlank { "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                        )
                    },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
    }
}

@Composable
fun FavoriteField (
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row (
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(fraction = .7f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Favorite: ",
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(2f)
            ) {
                OutlinedTextField(
                    value = selectedColumn.ifBlank { "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                        )
                    },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
    }
}

@Composable
fun DislikedField (
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row (
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(fraction = .7f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Disliked: ",
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(2f)
            ) {
                OutlinedTextField(
                    value = selectedColumn.ifBlank { "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                        )
                    },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
    }
}

@Composable
fun NotesField (
    selectedColumn: String,
    csvColumns: List<String>,
    onColumnSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row (
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(fraction = .7f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Notes: ",
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f),
            )
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(2f)
            ) {
                OutlinedTextField(
                    value = selectedColumn.ifBlank { "" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                        )
                    },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
    }
}

@Preview(showBackground = true)
@Composable
fun CsvImportScreenPreview() {
    CsvImportScreen(
        navigateBack = {},
        onNavigateUp = {},
        navigateToImportResults = { totalRecords, successCount, successfulInsertions -> {} },
        modifier = Modifier
    )
}