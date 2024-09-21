@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tobaccocellar

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.BottomSheetState
import com.example.tobaccocellar.ui.FilterViewModel
import com.example.tobaccocellar.ui.home.HomeDestination
import com.example.tobaccocellar.ui.interfaces.ExportCsvHandler
import com.example.tobaccocellar.ui.navigation.CellarNavHost
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import com.example.tobaccocellar.ui.stats.StatsDestination
import com.example.tobaccocellar.ui.theme.LocalCustomColors
import com.example.tobaccocellar.ui.theme.onBackgroundDark
import com.example.tobaccocellar.ui.theme.onPrimaryLight
import com.example.tobaccocellar.ui.theme.primaryDark
import com.example.tobaccocellar.ui.theme.primaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarApp(
    navController: NavHostController = rememberNavController()
) {
    CellarNavHost(navController = navController)

    val application = LocalCellarApplication.current

    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val bottomSheetState by filterViewModel.bottomSheetState.collectAsState()

    if (bottomSheetState == BottomSheetState.OPENED) {
        ModalBottomSheet(
            onDismissRequest = { filterViewModel.closeBottomSheet() },
            modifier = Modifier
                .statusBarsPadding(),
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            /* TODO new window insets stuff */
            //        windowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
            dragHandle = { },
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarTopAppBar(
    title: String,
    showMenu: Boolean,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
    navigateToCsvImport: () -> Unit,
    navigateToSettings: () -> Unit,
    exportCsvHandler: ExportCsvHandler? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
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
            containerColor = primaryLight,
            scrolledContainerColor = primaryLight,
            navigationIconContentColor = onPrimaryLight,
            actionIconContentColor = onPrimaryLight,
            titleContentColor = onPrimaryLight,
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
                            onClick = {
                                expanded = false
                                navigateToSettings()
                            },
                            modifier = Modifier.padding(0.dp),
                            enabled = true,
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun CellarBottomAppBar(
    currentDestination: NavigationDestination?,
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit = {},
    navigateToStats: () -> Unit = {},
    navigateToAddEntry: () -> Unit = {},
    filterViewModel: FilterViewModel,
) {
    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(0.dp),
        containerColor = primaryLight,  // BottomAppBarDefaults.containerColor
        contentColor = onBackgroundDark,
        tonalElevation = 10.dp,
        contentPadding = PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cellar //
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth(.55f)
//                        .background(if (currentDestination == HomeDestination) MaterialTheme.colorScheme.inversePrimary else Color.Transparent, CircleShape),
//                    contentAlignment = Alignment.Center,
//                ) {
                    IconButton(
                        onClick = navigateToHome,
                        modifier = Modifier
                            .padding(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.table_view_old),
                            contentDescription = stringResource(R.string.home_title),
                            modifier = Modifier
                                .size(26.dp)
                                .offset(y = (-8).dp),
                            tint =
                            if (currentDestination == HomeDestination) {
                                primaryDark
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }
                    Text(
                        text = stringResource(R.string.home_title),
                        modifier = Modifier
                            .padding(0.dp)
                            .offset(y = 13.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color =
                        if (currentDestination == HomeDestination) {
                            primaryDark
                        } else {
                            LocalContentColor.current
                        },
                    )
              //  }
            }

            // Stats //
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center,
                //  verticalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.Bottom),
                //  horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = navigateToStats,
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bar_chart),
                        contentDescription = stringResource(R.string.stats_title),
                        modifier = Modifier
                            .size(26.dp)
                            .offset(y = (-8).dp),
                        tint =
                        if (currentDestination == StatsDestination) {
                            primaryDark
                        } else {
                            LocalContentColor.current
                        },

                        )
                }
                Text(
                    text = stringResource(R.string.stats_title),
                    modifier = Modifier
                        .offset(y = 13.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color =
                    if (currentDestination == StatsDestination) {
                        primaryDark
                    } else {
                        LocalContentColor.current
                    },
                )
            }

            // Filter //
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center,
                //  verticalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.Bottom),
                //  horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = { filterViewModel.openBottomSheet() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filter_24),
                        contentDescription = stringResource(R.string.filter_items),
                        modifier = Modifier
                            .size(26.dp)
                            .offset(y = (-8).dp),
                        tint = LocalContentColor.current,
                    )
                }
                Text(
                    text = stringResource(R.string.filter_items),
                    modifier = Modifier
                        .offset(y = 13.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                )
            }

            // Add //
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center,
                //  verticalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.Bottom),
                //  horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = navigateToAddEntry,
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = stringResource(R.string.add),
                        modifier = Modifier
                            .size(26.dp)
                            .offset(y = (-8).dp),
                        tint = LocalContentColor.current,
                    )
                }
                Text(
                    text = stringResource(R.string.add),
                    modifier = Modifier
                        .offset(y = 13.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}


@Composable
fun FilterBottomSheet(
    filterViewModel: FilterViewModel,
    onDismiss: () -> Unit,
    onApplyFilters: () -> Unit,
) {
//    val navigationHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
//    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
            Text(
                text = "Select Filters",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
            )
            Column (
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = { filterViewModel.closeBottomSheet() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .padding(0.dp)
                    )
                }
            }
        }
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
        Spacer(
            modifier = Modifier
                .height(8.dp)
        )
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
        modifier = Modifier
            .fillMaxWidth(),
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
        Spacer(
            modifier = Modifier
                .width(8.dp)
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
            fontSize = 14.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val availableTypes = listOf("Aromatic", "English", "Burley", "Virginia", "Other")
    val selectedTypes by filterViewModel.selectedTypes.collectAsState()

    Column(
        modifier = Modifier
    ) {
        Text("Type:")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            availableTypes.forEach { type ->
                FilterChip(
                    selected = selectedTypes.contains(type),
                    onClick = { filterViewModel.updateSelectedTypes(type, !selectedTypes.contains(type)) },
                    label = {
                        Text(
                            type,
                            fontSize = 14.sp,
                        )
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    shape = MaterialTheme.shapes.small
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
        Spacer(
            modifier = Modifier
                .height(4.dp)
        )
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
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.None,
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField,
            ),
            singleLine = true,
            maxLines = 1,
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

        BoxWithConstraints {

            val maxWidth = maxWidth * 0.32f
            val chipCountToShow = 5
            val overflowCount = selectedBrands.size - chipCountToShow

            Column(
                modifier = Modifier
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                        .heightIn(min = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 6.dp, alignment = Alignment.CenterHorizontally
                    ),
                ) {
                    selectedBrands.take(chipCountToShow).forEach { brand ->
                        Chip(
                            text = brand,
                            isSelected = true,
                            onChipClicked = {},
                            onChipRemoved = { filterViewModel.updateSelectedBrands(brand, false) },
                            trailingIcon = true,
                            maxWidth = maxWidth,
                            modifier = Modifier
                        )
                    }
                    if (overflowCount > 0) {
                        Chip(
                            text = "+$overflowCount",
                            isSelected = true,
                            onChipClicked = { showOverflowPopup = true },
                            onChipRemoved = { },
                            trailingIcon = false,
                            modifier = Modifier
                        )
                    }
                }

                if (showOverflowPopup) {
                    AlertDialog(
                        onDismissRequest = { showOverflowPopup = false },
                        title = { Text("Selected Brands:") },
                        text = {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy((-6).dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
}

@Composable
fun Chip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onChipClicked: (String) -> Unit,
    onChipRemoved: () -> Unit,
    trailingIcon: Boolean = true,
    maxWidth: Dp = Dp.Infinity
) {
    AssistChip(
        onClick = { onChipClicked(text) },
        label = {
            if (text.startsWith("+")) {
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(25.dp)
                )
            }
            else {
                Text(
                    text = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
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
        },
        modifier = Modifier
            .widthIn(max = maxWidth)
            .padding(0.dp),
    )
}



