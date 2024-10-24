@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tobaccocellar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
import com.example.tobaccocellar.ui.theme.onPrimaryLight
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
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            dragHandle = { },
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        ) {
            FilterBottomSheet(
                filterViewModel = filterViewModel,
                onDismiss = { filterViewModel.closeBottomSheet() },
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
    currentDestination: NavigationDestination? = null,
    navigateUp: () -> Unit = {},
    navigateToCsvImport: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
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
        title = { Text(title) },
        modifier = modifier,
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
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier,
                        containerColor = LocalCustomColors.current.textField,
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.import_csv)) },
                            onClick = {
                                expanded = false
                                navigateToCsvImport()
                            },
                            modifier = Modifier.padding(0.dp),
                            enabled = currentDestination == HomeDestination,
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
                            enabled = currentDestination == HomeDestination && exportCsvHandler != null,
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
        },
        expandedHeight = 56.dp,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primaryLight,
            scrolledContainerColor = primaryLight,
            navigationIconContentColor = onPrimaryLight,
            actionIconContentColor = onPrimaryLight,
            titleContentColor = onPrimaryLight,
        ),
        scrollBehavior = scrollBehavior,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = primaryLight,
        contentColor = LocalCustomColors.current.navIcon,
        contentPadding = PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var clickToAdd by remember { mutableStateOf(false) }

            // Cellar //
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
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
                        if (currentDestination == HomeDestination && !clickToAdd) {
                            onPrimaryLight
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
                    fontWeight =
                    if (currentDestination == HomeDestination && !clickToAdd) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal },
                    color =
                    if (currentDestination == HomeDestination && !clickToAdd) {
                        onPrimaryLight
                    } else {
                        LocalContentColor.current
                    },
                )
            }

            // Stats //
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center,
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
                        if (currentDestination == StatsDestination && !clickToAdd) {
                            onPrimaryLight
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
                    fontWeight =
                    if (currentDestination == StatsDestination && !clickToAdd) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal },
                    color =
                    if (currentDestination == StatsDestination && !clickToAdd) {
                        onPrimaryLight
                    } else {
                        LocalContentColor.current
                    },
                )
            }

            // Filter //
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center,
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
                        tint = if (filterViewModel.isBottomSheetOpen) {
                            onPrimaryLight
                        } else { LocalContentColor.current },
                    )
                }
                Text(
                    text = stringResource(R.string.filter_items),
                    modifier = Modifier
                        .offset(y = 13.dp),
                    fontSize = 11.sp,
                    fontWeight =
                    if (filterViewModel.isBottomSheetOpen) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    color = if (filterViewModel.isBottomSheetOpen) {
                        onPrimaryLight
                    } else { LocalContentColor.current }
                )
            }

            // Add //
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center,
                //  verticalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.Bottom),
                //  horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = {
                        clickToAdd = true
                        navigateToAddEntry()
                    },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = stringResource(R.string.add),
                        modifier = Modifier
                            .size(26.dp)
                            .offset(y = (-8).dp),
                        tint = if (clickToAdd) {
                            onPrimaryLight
                        } else { LocalContentColor.current },
                    )
                }
                Text(
                    text = stringResource(R.string.add),
                    modifier = Modifier
                        .offset(y = 13.dp),
                    fontSize = 11.sp,
                    fontWeight =
                    if (clickToAdd) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    color = if (clickToAdd) {
                        onPrimaryLight
                    } else { LocalContentColor.current }
                )
            }
        }
    }
}


@Composable
fun FilterBottomSheet(
    filterViewModel: FilterViewModel,
    onDismiss: () -> Unit,
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
        Spacer(
            modifier = Modifier
                .height(4.dp)
        )
        BrandFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
        )
        Spacer(
            modifier = Modifier
                .height(4.dp)
        )
        TypeFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier,
        )
        Spacer(
            modifier = Modifier
                .height(4.dp)
        )
        OtherFiltersSection(
            filterViewModel = filterViewModel,
            modifier = Modifier,
        )
        Spacer(
            modifier = Modifier
                .height(4.dp)
        )
    }
}

@Composable
fun OtherFiltersSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val favorites by filterViewModel.selectedFavorites.collectAsState()
    val dislikeds by filterViewModel.selectedDislikeds.collectAsState()
    val neutral by filterViewModel.selectedNeutral.collectAsState()
    val nonNeutral by filterViewModel.selectedNonNeutral.collectAsState()
    val inStock by filterViewModel.selectedInStock.collectAsState()
    val outOfStock by filterViewModel.selectedOutOfStock.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .width(intrinsicSize = IntrinsicSize.Max)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    CheckboxWithLabel(
                        text = "Favorites",
                        checked = favorites,
                        onCheckedChange = { filterViewModel.updateSelectedFavorites(it) }
                    )
                    CheckboxWithLabel(
                        text = "Both",
                        checked = nonNeutral,
                        onCheckedChange = { filterViewModel.updateSelectedNonNeutral(it) },
                    )
                }
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    CheckboxWithLabel(
                        text = "Dislikes",
                        checked = dislikeds,
                        onCheckedChange = { filterViewModel.updateSelectedDislikeds(it) }
                    )
                    CheckboxWithLabel(
                        text = "Neither",
                        checked = neutral,
                        onCheckedChange = { filterViewModel.updateSelectedNeutral(it) },
                    )
                }
                Spacer(
                        modifier = Modifier
                            .width(4.dp)
                    )
            }
        }
//        Spacer(
//            modifier = Modifier
//                .weight(1f)
//        )
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .width(intrinsicSize = IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            CheckboxWithLabel(
                text = "In-stock",
                checked = inStock,
                onCheckedChange = { filterViewModel.updateSelectedInStock(it) }
            )
            CheckboxWithLabel(
                text = "Out",
                checked = outOfStock,
                onCheckedChange = { filterViewModel.updateSelectedOutOfStock(it) }
            )
        }
    }
}

@Composable
fun CheckboxWithLabel(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    fontColor: Color = LocalContentColor.current,
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .padding(0.dp)
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(end = 8.dp),
            color = fontColor,
            fontSize = 15.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val availableTypes = listOf("Aromatic", "English", "Burley", "Virginia", "Other")
    val selectedTypes by filterViewModel.selectedTypes.collectAsState()
    val selectedUnassigned by filterViewModel.selectedUnassigned.collectAsState()

    Column(
        modifier = modifier
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
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
                    shape = MaterialTheme.shapes.small,
                )
            }
            FilterChip(
                selected = selectedUnassigned,
                onClick = { filterViewModel.updateSelectedUnassigned(!selectedUnassigned) },
                label = {
                    Text(
                        "Unassigned",
                        fontSize = 14.sp,
                    )
                },
                modifier = Modifier
                    .padding(0.dp),
                shape = MaterialTheme.shapes.small,
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
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

    Column(
        modifier = modifier
    ) {
        CustomFilterTextField(
            value = brandSearchText,
            onValueChange = { text ->
                brandSearchText = text
                filteredBrands = if (text.isBlank()) {
                    allBrands
                } else {
                    allBrands.filter { it.contains(text, ignoreCase = true) }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = "Search Brands",
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
                .padding(end = 1.dp)
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val unselectedBrands = filteredBrands.filterNot { selectedBrands.contains(it) }
            items(unselectedBrands.size, key = { index -> unselectedBrands[index] }) { index ->
                val brand = unselectedBrands[index]
                TextButton(
                    onClick = {
                        filterViewModel.updateSelectedBrands(brand, true)
                        brandSearchText = ""
                        filteredBrands = allBrands
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
                        title = {
                            Text(
                                text = "Selected Brands",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(0.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                            ) {
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(
                                        onClick = {
                                            filterViewModel.clearAllSelectedBrands()
                                            showOverflowPopup = false
                                        },
                                        modifier = Modifier,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "",
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .size(20.dp)
                                        )
                                        Text(
                                            text = "Clear All",
                                            modifier = Modifier,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showOverflowPopup = false }) {
                                Text("Close")
                            }
                        },
                        containerColor = LocalCustomColors.current.darkNeutral,
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomFilterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int. MAX_VALUE,
) {
    var showCursor by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(color = LocalCustomColors.current.textField, RoundedCornerShape(4.dp))
            .height(48.dp)
            .onFocusChanged { focusState ->
                hasFocus = focusState.hasFocus
                showCursor = focusState.hasFocus
                if (!focusState.hasFocus) {
                    focusManager.clearFocus()
                }
            }
            .padding(horizontal = 16.dp),
        textStyle = LocalTextStyle.current.copy(
            color = LocalContentColor.current,
            fontSize = TextUnit.Unspecified,
            lineHeight = TextUnit.Unspecified,
        ),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        maxLines = maxLines,
        cursorBrush = if (showCursor) { SolidColor(MaterialTheme.colorScheme.primary) }
        else { SolidColor(Color.Transparent) },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && !hasFocus) {
                        Text(
                            text = placeholder,
                            style = LocalTextStyle.current.copy(
                                color = LocalContentColor.current.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
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



