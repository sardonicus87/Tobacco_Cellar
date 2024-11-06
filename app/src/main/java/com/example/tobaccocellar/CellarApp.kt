@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("SameParameterValue")

package com.example.tobaccocellar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TriStateCheckbox
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
import androidx.compose.ui.state.ToggleableState
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
import com.example.tobaccocellar.ui.navigation.CellarNavHost
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import com.example.tobaccocellar.ui.stats.StatsDestination
import com.example.tobaccocellar.ui.theme.LocalCustomColors
import com.example.tobaccocellar.ui.theme.onPrimaryLight
import com.example.tobaccocellar.ui.theme.primaryLight
import com.example.tobaccocellar.ui.utilities.ExportCsvHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarApp(
    navController: NavHostController = rememberNavController()
) {
    CellarNavHost(navController = navController)

    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val bottomSheetState by filterViewModel.bottomSheetState.collectAsState()
    val navigationHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    if (bottomSheetState == BottomSheetState.OPENED) {
        ModalBottomSheet(
            onDismissRequest = {
            //    filterViewModel.applyFilter()
                filterViewModel.closeBottomSheet()
            },
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
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
                modifier = Modifier
            )
        }
    }
}


/** App bars **/
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


/** Filter sheet stuff **/
@Composable
fun FilterBottomSheet(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val filtersApplied by filterViewModel.isFilterApplied.collectAsState()

    Column (
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 0.dp)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
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
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
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
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }

        BrandFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
        )
        TypeFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(start = 6.dp, end = 6.dp, top = 0.dp, bottom = 6.dp),
        )
        OtherFiltersSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(start = 6.dp, end = 6.dp, top = 0.dp, bottom = 6.dp),
        )
        TextButton(
            onClick = { filterViewModel.resetFilter() },
            modifier = Modifier
                .offset(x = (-4).dp),
            enabled = filtersApplied,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 3.dp)
                    .size(20.dp)
            )
            Text(
                text = "Clear All",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(
            modifier = Modifier
                .height(12.dp)
        )
    }
}

@Composable
fun OtherFiltersSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
//    val favorites by filterViewModel.selectedFavorites.collectAsState()
//    val dislikeds by filterViewModel.selectedDislikeds.collectAsState()
//    val neutral by filterViewModel.selectedNeutral.collectAsState()
//    val nonNeutral by filterViewModel.selectedNonNeutral.collectAsState()
//    val inStock by filterViewModel.selectedInStock.collectAsState()
//    val outOfStock by filterViewModel.selectedOutOfStock.collectAsState()

    val favorites by filterViewModel.sheetSelectedFavorites.collectAsState()
    val dislikeds by filterViewModel.sheetSelectedDislikeds.collectAsState()
    val neutral by filterViewModel.sheetSelectedNeutral.collectAsState()
    val nonNeutral by filterViewModel.sheetSelectedNonNeutral.collectAsState()
    val inStock by filterViewModel.sheetSelectedInStock.collectAsState()
    val outOfStock by filterViewModel.sheetSelectedOutOfStock.collectAsState()

    // tristate checkbox state
    val (state) = mutableStateOf(nonNeutral)
    val (state2) = mutableStateOf(neutral)
    val triState =
        if (state) ToggleableState.On
        else if (state2) ToggleableState.Indeterminate
        else ToggleableState.Off


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    RoundedCornerShape(8.dp)
                )
                .width(intrinsicSize = IntrinsicSize.Max)
                .padding(vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box {
                        TriStateCheckWithLabel(
                            text = "",
                            state = triState,
                            onClick = { },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                if (state) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else if (state2) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else Color.Transparent,
                                checkmarkColor =
                                if (state) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else if (state2) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else Color.Transparent
                            )
                        )
                        CheckboxWithLabel(
                            text = "Favorites",
                            checked = favorites,
                            onCheckedChange = { filterViewModel.updateSelectedFavorites(it) },
                            modifier = Modifier
                                .padding(end = 4.dp)
                        )
                    }
                    CheckboxWithLabel(
                        text = "Both",
                        checked = nonNeutral,
                        onCheckedChange = { filterViewModel.updateSelectedNonNeutral(it) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                }
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box {
                        TriStateCheckWithLabel(
                            text = "",
                            state = triState,
                            onClick = { },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                if (state) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else if (state2) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else Color.Transparent,
                                checkmarkColor =
                                if (state) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else if (state2) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else Color.Transparent
                            )
                        )
                        CheckboxWithLabel(
                            text = "Dislikes",
                            checked = dislikeds,
                            onCheckedChange = { filterViewModel.updateSelectedDislikeds(it) },
                            modifier = Modifier
                                .padding(end = 8.dp)
                        )
                    }
                    CheckboxWithLabel(
                        text = "Neutral",
                        checked = neutral,
                        onCheckedChange = { filterViewModel.updateSelectedNeutral(it) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                }
                Spacer(
                        modifier = Modifier
                            .width(4.dp)
                    )
            }
        }
        Column(
            modifier = Modifier
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    RoundedCornerShape(8.dp)
                )
                .width(intrinsicSize = IntrinsicSize.Max)
                .padding(vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            CheckboxWithLabel(
                text = "In-stock",
                checked = inStock,
                onCheckedChange = { filterViewModel.updateSelectedInStock(it) },
                modifier = Modifier
                    .padding(end = 8.dp)
            )
            CheckboxWithLabel(
                text = "Out",
                checked = outOfStock,
                onCheckedChange = { filterViewModel.updateSelectedOutOfStock(it) },
                modifier = Modifier
                    .padding(end = 8.dp)
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
    enabled: Boolean = true,
    fontColor: Color = LocalContentColor.current,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .padding(0.dp),
                enabled = enabled,
                colors = colors,
            )
        }
        Text(
            text = text,
            modifier = Modifier,
            color = fontColor,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun TriStateCheckWithLabel(
    text: String,
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontColor: Color = LocalContentColor.current,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box {
            TriStateCheckbox(
                state = state,
                onClick = onClick,
                modifier = Modifier
                    .padding(0.dp),
                enabled = enabled,
                colors = colors,
            )
        }
        Text(
            text = text,
            modifier = Modifier,
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
//    val selectedTypes by filterViewModel.selectedTypes.collectAsState()
//    val selectedUnassigned by filterViewModel.selectedUnassigned.collectAsState()

    val selectedTypes by filterViewModel.sheetSelectedTypes.collectAsState()
    val selectedUnassigned by filterViewModel.sheetSelectedUnassigned.collectAsState()

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
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
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
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun BrandFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
//    val selectedBrands by filterViewModel.selectedBrands.collectAsState()

    val selectedBrands by filterViewModel.sheetSelectedBrands.collectAsState()
    val selectedExcludedBrands by filterViewModel.sheetSelectedExcludedBrands.collectAsState()
    val excluded by filterViewModel.sheetSelectedExcludeSwitch.collectAsState()
//    val included = !excluded

    val allBrands by filterViewModel.availableBrands.collectAsState()

    var brandSearchText by remember { mutableStateOf("") }
    var filteredBrands by remember { mutableStateOf(allBrands) }
    var showOverflowPopup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
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
                    .weight(1f, false),
                placeholder = "Search Brands",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None,
                ),
                singleLine = true,
                maxLines = 1,
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 1.dp)
                    .width(52.dp)
                    .combinedClickable(
                        onClick = {
                            filterViewModel.updateSelectedExcludeSwitch(isSelected = !excluded)
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Include",
                    modifier = Modifier
                        .padding(0.dp)
                        .offset(y = 1.dp)
//                        .combinedClickable(
//                            onClick = {
//                                filterViewModel.updateSelectedExcludeSwitch(isSelected = false)
//                            },
//                            indication = null,
//                            interactionSource = remember { MutableInteractionSource() }
//                        )
                    ,
                    color = if (!excluded) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = if (excluded) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Exclude",
                    modifier = Modifier
                        .padding(0.dp)
                        .offset(y = (-1).dp)
//                        .combinedClickable(
//                            onClick = {
//                                filterViewModel.updateSelectedExcludeSwitch(isSelected = true)
//                            },
//                            indication = null,
//                            interactionSource = remember { MutableInteractionSource() }
//                        )
                    ,
                    color = if (excluded) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = if (excluded) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 2.dp)
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val unselectedBrands = filteredBrands.filterNot {
                if((selectedExcludedBrands.isEmpty())) {
                selectedBrands.contains(it) } else {
                    selectedExcludedBrands.contains(it) }
            }
            items(unselectedBrands.size, key = { index -> unselectedBrands[index] }) { index ->
                val brand = unselectedBrands[index]
                TextButton(
                    onClick = {
                        if (excluded) {
                            filterViewModel.updateSelectedExcludedBrands(brand, true)
                        } else {
                            filterViewModel.updateSelectedBrands(brand, true)
                        }
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
            val maxWidth = (maxWidth * 0.32f) - 4.dp
            val chipCountToShow = 5
            val overflowCount =
                if (excluded) { selectedExcludedBrands.size - chipCountToShow }
                else { selectedBrands.size - chipCountToShow }
            val chips = if (excluded) selectedExcludedBrands else selectedBrands

            Column(
                modifier = Modifier
            ) {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = Dp.Hairline,
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            //        .heightIn(min = 48.dp),
                            .height(96.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 6.dp, alignment = Alignment.CenterHorizontally
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                    ) {
                        chips.take(chipCountToShow).forEach { brand ->
                            Chip(
                                text = brand,
                                onChipClicked = {},
                                onChipRemoved = {
                                    if (excluded) {
                                        filterViewModel.updateSelectedExcludedBrands(brand, false)
                                    } else {
                                        filterViewModel.updateSelectedBrands(brand, false)
                                    }
                                },
                                trailingIcon = true,
                                iconSize = 20.dp,
                                trailingTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxWidth = maxWidth,
                                modifier = Modifier,
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = if (excluded) MaterialTheme.colorScheme.error.copy(alpha = 0.07f) else MaterialTheme.colorScheme.background,
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = if (excluded) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else
                                        MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        if (overflowCount > 0) {
                            Chip(
                                text = "+$overflowCount",
                                onChipClicked = { showOverflowPopup = true },
                                onChipRemoved = { },
                                trailingIcon = false,
                                modifier = Modifier,
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = if (excluded) MaterialTheme.colorScheme.error.copy(alpha = 0.07f) else MaterialTheme.colorScheme.background,
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = if (excluded) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else
                                        MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    Box {
                        if (selectedBrands.isEmpty() && selectedExcludedBrands.isEmpty())
                        Text(
                            text = if (excluded) "Excluded Brands" else "Included Brands",
                            modifier = Modifier
                                .padding(0.dp),
                            color = if (excluded) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (showOverflowPopup) {
                    AlertDialog(
                        onDismissRequest = { showOverflowPopup = false },
                        title = {
                            Text(
                                text = if (excluded) "Excluded Brands" else "Included Brands",
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
                                    items(chips) { brand ->
                                        Chip(
                                            text = brand,
                                            onChipClicked = { },
                                            onChipRemoved = {
                                                if (excluded) {
                                                    filterViewModel.updateSelectedExcludedBrands(brand, false)
                                                } else {
                                                    filterViewModel.updateSelectedBrands(brand, false)
                                                }
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
                                        modifier = Modifier
                                            .offset(x = (-4).dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "",
                                            modifier = Modifier
                                                .padding(end = 3.dp)
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
//    colors: TextFieldColors = TextFieldDefaults.colors(),
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
            .background(color = LocalCustomColors.current.textField, RoundedCornerShape(6.dp))
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
    onChipClicked: (String) -> Unit,
    onChipRemoved: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    trailingIcon: Boolean = true,
    iconSize: Dp = 24.dp,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.outline),
    maxWidth: Dp = Dp.Infinity,
    trailingTint: Color = LocalContentColor.current
) {
    AssistChip(
        onClick = { onChipClicked(text) },
        label = {
            if (text.startsWith("+")) {
                Text(
                    text = text,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(25.dp)
                )
            }
            else {
                Text(
                    text = text,
                    fontSize = fontSize,
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
                    modifier = Modifier
                        .clickable { onChipRemoved() }
                        .size(iconSize),
                    tint = trailingTint
                )
            } else { /** do nothing */ }
        },
        modifier = modifier
            .widthIn(max = maxWidth)
            .padding(0.dp),
        colors = colors,
        border = border
    )
}



