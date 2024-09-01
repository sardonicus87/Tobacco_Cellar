@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tobaccocellar

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetDefaults.ExpandedShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.BottomSheetState
import com.example.tobaccocellar.ui.FilterViewModel
import com.example.tobaccocellar.ui.interfaces.ExportCsvHandler
import com.example.tobaccocellar.ui.navigation.CellarNavHost
import com.example.tobaccocellar.ui.theme.primaryLight
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.chip.Chip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarApp(
    navController: NavHostController = rememberNavController()
) {
    val application = LocalCellarApplication.current
    val systemBars = rememberSystemUiController()

    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val bottomSheetState by filterViewModel.bottomSheetState.collectAsState()
    if (bottomSheetState == BottomSheetState.OPENED) {
        ModalBottomSheet(
            onDismissRequest = { filterViewModel.closeBottomSheet() },
            modifier = Modifier
                .offset(y = (-66).dp)
                .systemBarsPadding()
                .navigationBarsPadding(),
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            dragHandle = { },
            windowInsets = WindowInsets(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
            ),
            properties = ModalBottomSheetDefaults.properties(
                shouldDismissOnBackPress = true
            ),
        ) {
            FilterBottomSheet(
                filterViewModel = filterViewModel,
                onDismiss = { filterViewModel.closeBottomSheet() },
                onApplyFilters = {
                    filterViewModel.closeBottomSheet()
                },
            )
        }
    }

    CellarNavHost(
            navController = navController,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    showMenu: Boolean,
    modifier: Modifier = Modifier,
    exportCsvHandler: ExportCsvHandler? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    navigateToCsvImport: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            exportCsvHandler?.onExportCsvClick(uri)
        }
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primaryLight
        ),
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (showMenu) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.import_csv)) },
                            onClick = {
                                expanded = false
                                navigateToCsvImport()
                            },
                            modifier = Modifier.padding(0.dp),
                            enabled = true,
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.export_csv)) },
                            onClick = {
                                expanded = false
                                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_TITLE, "tobacco_cellar.csv")
                                }
                                launcher.launch(intent)
                            },
                            modifier = Modifier.padding(0.dp),
                            enabled = exportCsvHandler != null,
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.settings)) },
                            onClick = { expanded = false },
                            modifier = Modifier.padding(0.dp),
                            enabled = false,
                        )
                    }
                }
            }
        }
    )
}


/* TODO add filter sheet */

@Composable
fun FilterBottomSheet(
    filterViewModel: FilterViewModel,
    onDismiss: () -> Unit,
    onApplyFilters: () -> Unit,
) {

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Select Filters: "
        )
        BrandFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
        )
        TypeFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
        )
        OtherFiltersSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { filterViewModel.closeBottomSheet() },
                modifier = Modifier
            ) {
                Text(text = "Apply Filtering")
            }
        }
    }
}

@Composable
fun OtherFiltersSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by filterViewModel.selectedFavorites.collectAsState()
    val dislikeds by filterViewModel.selectedDislikeds.collectAsState()
    val outOfStock by filterViewModel.selectedOutOfStock.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CheckboxWithLabel(
            text = "Favorites",
            checked = favorites,
            onCheckedChange = { filterViewModel.updateSelectedFavorites(it) }
        )
        CheckboxWithLabel(
            text = "Dislikes",
            checked = dislikeds,
            onCheckedChange = { filterViewModel.updateSelectedDislikeds(it) }
        )
        CheckboxWithLabel(
            text = "Out of Stock",
            checked = outOfStock,
            onCheckedChange = { filterViewModel.updateSelectedOutOfStock(it) }
        )
    }
}

@Composable
fun CheckboxWithLabel(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            fontSize = 12.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
  //  val allTypes by filterViewModel.availableTypes.collectAsState()
    val availableTypes = listOf("Aromatic", "English", "Burley", "Virginia", "Other")
    val selectedTypes by filterViewModel.selectedTypes.collectAsState()

    Column {
        Text("Type:")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            availableTypes.forEach { type ->
                FilterChip(
                    selected = selectedTypes.contains(type),
                    onClick = { filterViewModel.updateSelectedTypes(type, !selectedTypes.contains(type)) },
                    label = { Text(type) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrandFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val selectedBrands by filterViewModel.selectedBrands.collectAsState()
    val allBrands by filterViewModel.availableBrands.collectAsState()
    var brandSearchText by remember { mutableStateOf("") }
    var filteredBrands by remember { mutableStateOf(allBrands) }
    var showOverflowPopup by remember { mutableStateOf(false) }

    Column {
        Text("Brand:")

        TextField(
            value = brandSearchText,
            onValueChange = { text ->
                brandSearchText = text
                filteredBrands = if (text.isBlank()) {
                    allBrands
                } else {
                    allBrands.filter { it.contains(text, ignoreCase = true) }
                }
            },
            label = { Text("Search brands") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val unselectedBrands = filteredBrands.filterNot { selectedBrands.contains(it) }
            items(unselectedBrands.size, key = {index -> unselectedBrands[index]}) { index ->
                val brand = unselectedBrands[index]
                TextButton(
                    onClick = {
                        filterViewModel.updateSelectedBrands(brand, true)
                    },
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    Text(
                        text = brand,
                        modifier = Modifier
                            .wrapContentSize()
                    )
                }
            }
        }

        val chipCountToShow = 4
        val overflowCount = selectedBrands.size - chipCountToShow

        Column() {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally),
            ) {
                selectedBrands.take(chipCountToShow).forEach { brand ->
                    Chip(
                        text = brand,
                        isSelected = true,
                        onChipClicked = {},
                        onChipRemoved = { filterViewModel.updateSelectedBrands(brand, false) },
                        trailingIcon = true
                    )
                }
                if (overflowCount > 0) {
                    Chip(
                        text = "+$overflowCount",
                        isSelected = true,
                        onChipClicked = { showOverflowPopup = true },
                        onChipRemoved = { },
                        trailingIcon = false,
                    )
                }
            }

            if (showOverflowPopup) {
                AlertDialog(
                    onDismissRequest = { showOverflowPopup = false },
                    title = { Text("Selected Brands") },
                    text = {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(selectedBrands) { brand ->
                                Chip(
                                    text = brand,
                                    isSelected = true,
                                    onChipClicked = { },
                                    onChipRemoved = {
                                        filterViewModel.updateSelectedBrands(
                                            brand,
                                            false
                                        )
                                    }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showOverflowPopup = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Chip(
    text: String,
    isSelected: Boolean,
    onChipClicked: (String) -> Unit,
    onChipRemoved: () -> Unit,
    trailingIcon: Boolean = true
) {
    AssistChip(
        onClick = { onChipClicked(text) },
        label = { Text(text)},
        trailingIcon = {
            if (trailingIcon) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove Chip",
                    modifier = Modifier.clickable { onChipRemoved() }
                )
            }
            else { // do nothing
            }
        }
    )
}


@Composable
fun CellarBottomAppBar(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit = {},
    navigateToStats: () -> Unit = {},
    navigateToAddEntry: () -> Unit = {},
    filterViewModel: FilterViewModel,
) {
    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp)
            .height(66.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        /* TODO add tinting for current page/option */
        Row (
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .height(66.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Cellar //
            Column (
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                IconButton(
                    onClick = navigateToHome,
                    modifier = Modifier
                        .padding(0.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.table_view_old),
                        contentDescription = stringResource(R.string.home_title),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.home_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .offset(y = (-8).dp)
                )
            }

            // Stats //
            Column (
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = navigateToStats,
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bar_chart),
                        contentDescription = stringResource(R.string.stats_title),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.stats_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .offset(y = (-8).dp)
                )
            }

            // Filter //
            Column (
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = { filterViewModel.openBottomSheet() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = stringResource(R.string.filter_items),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.filter_items),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .offset(y = (-8).dp)
                )
            }

            // Add //
            Column (
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = navigateToAddEntry,
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = stringResource(R.string.add),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.add),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .offset(y = (-8).dp)
                )
            }
        }
    }
}

@Composable
fun ErrorDialog(
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
        }
    )
}

//@Preview
//@Composable
//fun BottomBarPreview(
//    modifier: Modifier = Modifier,
//    navigateToHome: () -> Unit = {},
//    navigateToStats: () -> Unit = {},
//    navigateToAddEntry: () -> Unit = {},
//    showBackground: Boolean = true,
//) {
//    CellarBottomAppBar () {
//    }
//}
