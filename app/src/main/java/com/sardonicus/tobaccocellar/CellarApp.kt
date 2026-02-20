@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("SameParameterValue")

package com.sardonicus.tobaccocellar

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.BottomSheetState
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.filtering.FilterSheet
import com.sardonicus.tobaccocellar.ui.items.CustomDropDown
import com.sardonicus.tobaccocellar.ui.navigation.CellarNavigation
import com.sardonicus.tobaccocellar.ui.navigation.DatesDestination
import com.sardonicus.tobaccocellar.ui.navigation.HomeDestination
import com.sardonicus.tobaccocellar.ui.navigation.NavigationState
import com.sardonicus.tobaccocellar.ui.navigation.Navigator
import com.sardonicus.tobaccocellar.ui.navigation.StatsDestination
import com.sardonicus.tobaccocellar.ui.navigation.rememberNavigationState
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.theme.onPrimaryLight
import com.sardonicus.tobaccocellar.ui.utilities.ExportCsvHandler
import kotlinx.coroutines.launch

@Composable
fun CellarApp(
    isGestureNav: Boolean,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    isLarge: Boolean = remember(windowSizeClass) { windowSizeClass.isAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND) }, // isAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND)  // isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    navigationState: NavigationState = rememberNavigationState(
        startRoute = HomeDestination,
        topLevelRoutes = setOf(HomeDestination, StatsDestination, DatesDestination),
        largeScreen = isLarge
    ),
    navigator: Navigator = remember(navigationState, isLarge) { Navigator(navigationState, isLarge) },
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel,
) {
    CellarNavigation(
        navigator = navigator,
        navigationState = navigationState,
        isGestureNav = isGestureNav,
        largeScreen = isLarge,
        filterViewModel = filterViewModel
    )

    FilterSheet(filterViewModel)
}


/** App bars **/
@Composable
fun CellarTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    showMenu: Boolean = false,
    currentDestination: NavKey? = null,
    navigateUp: () -> Unit = {},
    navigateToBulkEdit: () -> Unit = {},
    navigateToCsvImport: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToHelp: () -> Unit = {},
    navigateToPlaintext: () -> Unit = {},
    exportCsvHandler: ExportCsvHandler? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    overrideBack: Boolean = false,
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel,
) {
    val exportCsvPopup by filterViewModel.exportCsvPopup.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            val data = filterViewModel.exportCsvState.value
            exportCsvHandler?.onExportCsvClick(uri, data.allItems, data.exportRating)
        }
    }
    val exportAsTinsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            val data = filterViewModel.exportCsvState.value
            exportCsvHandler?.onTinsExportCsvClick(uri, data.allItems, data.exportRating)
        }
    }
    val exportCsvIntent = remember {
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "tobacco_cellar.csv")
        }
    }
    val exportAsTinsIntent = remember {
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "tobacco_cellar_as_tins.csv")
        }
    }

    val onConfirmExport: (String, String) -> Unit = remember(exportCsvLauncher, exportAsTinsLauncher) {
        { max, rounding ->
            coroutineScope.launch {
                filterViewModel.saveExportRating(max, rounding)

                val currentType = filterViewModel.exportType.value
                when (currentType) {
                    ExportType.ITEMS -> {
                        exportCsvLauncher.launch(exportCsvIntent)
                    }
                    ExportType.TINS -> exportAsTinsLauncher.launch(exportAsTinsIntent)
                    null -> {}
                }

                filterViewModel.showExportCsv(false)
            }
        }
    }

    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                val icon = if (overrideBack) painterResource(id = R.drawable.arrow_forward) else painterResource(id = R.drawable.arrow_back)

                IconButton(onClick = navigateUp) {
                    Icon(
                        painter = icon,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (showMenu) {
                Box {
                    IconButton(
                        onClick = filterViewModel::toggleMenu,
                        modifier = Modifier
                            .size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.more_vert),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                    TopBarMenu(
                        filterViewModel = filterViewModel,
                        currentDestination = currentDestination,
                        navigateToBulkEdit = navigateToBulkEdit,
                        navigateToCsvImport = navigateToCsvImport,
                        navigateToPlaintext = navigateToPlaintext,
                        navigateToHelp = navigateToHelp,
                        navigateToSettings = navigateToSettings,
                        exportCsvHandler = exportCsvHandler
                    )
                }
            }
        },
        expandedHeight = 56.dp,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LocalCustomColors.current.appBarContainer,
            scrolledContainerColor = LocalCustomColors.current.appBarContainer,
            navigationIconContentColor = onPrimaryLight,
            actionIconContentColor = onPrimaryLight,
            titleContentColor = onPrimaryLight,
        ),
        scrollBehavior = scrollBehavior,
    )

    if (exportCsvPopup) {
        ExportCsvDialog(
            confirm = onConfirmExport,
            filterViewModel = filterViewModel,
            showExportCsv = { filterViewModel.showExportCsv(false) },
            modifier = Modifier
        )
    }
}

@Composable
fun TopBarMenu(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    currentDestination: NavKey? = null,
    navigateToBulkEdit: () -> Unit = {},
    navigateToCsvImport: () -> Unit = {},
    navigateToPlaintext: () -> Unit = {},
    navigateToHelp: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    exportCsvHandler: ExportCsvHandler? = null,
) {
    val menuState by filterViewModel.topAppBarMenuState.collectAsState()

    DropdownMenu(
        expanded = menuState.menuExpanded,
        onDismissRequest = { filterViewModel.showMenu(false) },
        modifier = modifier,
        containerColor = LocalCustomColors.current.textField,
        shadowElevation = 6.dp
    ) {
        when (menuState.menuState) {
            MenuState.MAIN -> {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.bulk_edit_title)) },
                    onClick = {
                        filterViewModel.showMenu(false)
                        navigateToBulkEdit()
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled = currentDestination == HomeDestination,
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.import_csv)) },
                    onClick = {
                        filterViewModel.showMenu(false)
                        navigateToCsvImport()
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled = currentDestination == HomeDestination,
                )
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Export CSV ")
                            Icon(
                                painterResource(R.drawable.arrow_right),
                                "Export Options",
                                modifier = Modifier.size(20.dp),
                                tint = LocalContentColor.current.copy(alpha = 0.75f))
                        }
                    },
                    onClick = {
                        filterViewModel.changeMenuState(MenuState.EXPORT_CSV)
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled =
                        currentDestination == HomeDestination && exportCsvHandler != null,
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 0.dp
                    ),
                )
                DropdownMenuItem(
                    text = { Text(text = "Plaintext") },
                    onClick = {
                        filterViewModel.showMenu(false)
                        navigateToPlaintext()
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled = currentDestination == HomeDestination,
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.help_faq)) },
                    onClick = {
                        filterViewModel.showMenu(false)
                        navigateToHelp()
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled = true,
                )

                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.settings)) },
                    onClick = {
                        filterViewModel.showMenu(false)
                        navigateToSettings()
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled = true,
                )
            }
            MenuState.EXPORT_CSV -> {
                DropdownMenuItem(
                    text = {
                        Icon(
                            painterResource(R.drawable.arrow_left),
                            "Back",
                            modifier = Modifier.size(20.dp),
                            tint = LocalContentColor.current.copy(alpha = 0.75f)
                        )
                    },
                    onClick = { filterViewModel.changeMenuState(MenuState.MAIN) }
                )
                DropdownMenuItem(
                    text = { Text(text = "Normal") },
                    onClick = {
                        filterViewModel.changeExportType(ExportType.ITEMS)
                        filterViewModel.showExportCsv(true)
                        filterViewModel.showMenu(false)
                    //    filterViewModel.changeMenuState(MenuState.MAIN)
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled =
                        currentDestination == HomeDestination && exportCsvHandler != null,
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 0.dp
                    ),
                )
                DropdownMenuItem(
                    text = { Text(text = "As Tins") },
                    onClick = {
                        filterViewModel.showMenu(false)
                        filterViewModel.changeExportType(ExportType.TINS)
                        filterViewModel.showExportCsv(true)
                    //    filterViewModel.changeMenuState(MenuState.MAIN)
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled =
                        currentDestination == HomeDestination && exportCsvHandler != null,
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 0.dp
                    ),
                )
            }
        }
    }
}

@Composable
fun ExportCsvDialog(
    confirm: (String, String) -> Unit,
    filterViewModel: FilterViewModel,
    showExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dialogState by filterViewModel.exportCsvState.collectAsState()

    val options = listOf("All", "Filtered")
    val allowedMax = remember { Regex("^(\\s*|\\d{0,3})$") }

    AlertDialog(
        onDismissRequest = { showExportCsv() },
        title = {
            Text(
                text = "Export CSV Options"
            )
        },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // Items option //
                Text(
                    text = "Choose which entries to export (all or currently filtered) and " +
                            "scaling options for ratings.",
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                ) {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = index == dialogState.selectedIndex,
                            onClick = { filterViewModel.selectAll(index == 0) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            contentPadding = PaddingValues(8.dp, 4.dp),
                            icon = { }
                        ) {
                            Text(label)
                        }
                    }
                }

                // Export Rating options //
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Max Rating:",
                            modifier = Modifier
                                .width(95.dp),
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        TextField(
                            value = dialogState.exportRatingString.first,
                            onValueChange = {
                                if (it.matches(allowedMax)) {
                                    filterViewModel.updateExportRating(it, dialogState.exportRatingString.second)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .width(80.dp),
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
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Decimal Places: ",
                            modifier = Modifier
                                .width(95.dp),
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            maxLines = 2,
                        )
                        CustomDropDown(
                            selectedValue = dialogState.exportRatingString.second,
                            onValueChange = {
                                filterViewModel.updateExportRating(dialogState.exportRatingString.first, it)
                            },
                            options = listOf("0", "1", "2"),
                            modifier = Modifier
                                .width(80.dp),
                        )
                    }
                }
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        dismissButton = {
            TextButton(onClick = { showExportCsv() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirm(dialogState.exportRatingString.first, dialogState.exportRatingString.second) }
            ) {
                Text(
                    text = "Confirm"
                )
            }
        }
    )
}

enum class MenuState { MAIN, EXPORT_CSV }
enum class ExportType { ITEMS, TINS }


@Composable
fun CellarBottomAppBar(
    currentDestination: NavKey?,
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit = {},
    navigateToStats: () -> Unit = {},
    navigateToAddEntry: () -> Unit = {},
    navigateToDates: () -> Unit = {},
    isTwoPane: Boolean = false,
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel,
) {
    LaunchedEffect(Unit) { filterViewModel.updateClickToAdd(false) }

    val sheetState by filterViewModel.bottomSheetState.collectAsState()
    val filteringApplied by filterViewModel.isFilterApplied.collectAsState()
    val searchPerformed by filterViewModel.searchPerformed.collectAsState()
    val datesExist by filterViewModel.datesExist.collectAsState()
    val databaseEmpty by filterViewModel.emptyDatabase.collectAsState()
    val tinsReady by filterViewModel.tinsReady.collectAsState()
    val clickToAdd by filterViewModel.clickToAdd.collectAsState()

    val navIcon = LocalCustomColors.current.navIcon
    val indicatorCircle = LocalCustomColors.current.indicatorCircle
    val indicatorBorderCorrection = LocalCustomColors.current.indicatorBorderCorrection

    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        contentPadding = PaddingValues(0.dp),
        containerColor = LocalCustomColors.current.appBarContainer,
        contentColor = LocalCustomColors.current.navIcon,
        windowInsets = WindowInsets.displayCutout,
    ) {
        GlowBox(
            color = GlowColor(Color.White.copy(alpha = 0.07f)),
            size = GlowSize(top = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Home
                BottomBarButton(
                    title = { "Cellar" },
                    icon = { R.drawable.table_view_old },
                //    destination = { HomeDestination },
                    onClick = { filterViewModel.getPositionTrigger(); navigateToHome() },
                    activeColor = { if (currentDestination == HomeDestination && !clickToAdd) onPrimaryLight else navIcon },
                    modifier = Modifier.weight(1f)
                )

                // Stats
                BottomBarButton(
                    title = { "Stats" },
                    icon = { R.drawable.bar_chart },
                //    destination = StatsDestination,
                    onClick = { filterViewModel.getPositionTrigger(); navigateToStats() },
                    enabled = { !databaseEmpty },
                    activeColor = {
                        if (currentDestination == StatsDestination && !clickToAdd) onPrimaryLight
                        else if (databaseEmpty) navIcon.copy(alpha = 0.5f)
                        else navIcon
                    },
                    modifier = Modifier.weight(1f)
                )

                // 3. Dates
                BottomBarButton(
                    title = { "Dates" },
                    icon = { R.drawable.calendar_month },
                //    destination = DatesDestination,
                    onClick = { filterViewModel.getPositionTrigger(); navigateToDates() },
                    enabled = { datesExist },
                    showIndicator = { tinsReady },
                    indicatorColor = { if (tinsReady) indicatorCircle else Color.Transparent },
                    borderColor =  {
                        if (tinsReady) {
                            if (currentDestination == DatesDestination && !clickToAdd) onPrimaryLight else navIcon
                        } else Color.Transparent
                    },
                    activeColor = {
                        if (currentDestination == DatesDestination && !clickToAdd) onPrimaryLight
                        else if (datesExist) navIcon
                        else navIcon.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.weight(1f)
                )

                // 4. Filtering
                if (!isTwoPane) {
                    BottomBarButton(
                        title = { "Filter" },
                        icon = { R.drawable.filter_24 },
                        onClick = filterViewModel::openBottomSheet,
                        enabled = { if (currentDestination == HomeDestination && !databaseEmpty) !searchPerformed else !databaseEmpty },
                        showIndicator = { filteringApplied },
                        indicatorColor = {
                            if (filteringApplied) {
                                if (searchPerformed && currentDestination == HomeDestination) {
                                    indicatorCircle.copy(alpha = 0.5f)
                                } else indicatorCircle
                            } else Color.Transparent
                        },
                        borderColor = {
                            if (filteringApplied) {
                                if (searchPerformed && currentDestination == HomeDestination) {
                                    indicatorBorderCorrection
                                } else {
                                    if (sheetState == BottomSheetState.OPENED) {
                                        onPrimaryLight
                                    } else navIcon
                                }
                            } else Color.Transparent
                        },
                        activeColor = { if (sheetState == BottomSheetState.OPENED) onPrimaryLight else navIcon },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 5. Add
                BottomBarButton(
                    title = { "Add" },
                    icon = { R.drawable.add_circle },
                    onClick = { filterViewModel.updateClickToAdd(true); filterViewModel.getPositionTrigger(); navigateToAddEntry() },
                    activeColor = { if (clickToAdd) onPrimaryLight else navIcon },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Stable
@Composable
private fun BottomBarButton(
    title: () -> String,
    icon: () -> Int,
    onClick: () -> Unit,
    activeColor: () -> Color,
    modifier: Modifier = Modifier,
    enabled: () -> Boolean = { true },
    showIndicator: () -> Boolean = { false },
    indicatorColor: () -> Color = { Color.Transparent },
    borderColor: () -> Color = { Color.Transparent },
) {
    Box(
        modifier = modifier
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        // The Indicator
        if (showIndicator()) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(x = 13.dp, y = (-17).dp)
                    .clip(CircleShape)
                    .border(1.5.dp, borderColor(), CircleShape)
                    .background(indicatorColor())
            )
        }

        IconButton(
            onClick = onClick,
            enabled = enabled(),
            modifier = Modifier.padding(0.dp)
        ) {
            Icon(
                painter = painterResource(id = icon()),
                contentDescription = title(),
                modifier = Modifier
                    .size(26.dp)
                    .offset(y = (-8).dp),
                tint = activeColor()
            )
        }

        Text(
            text = title(),
            modifier = Modifier.offset(y = 13.dp),
            fontSize = 11.sp,
            fontWeight = if (activeColor() == onPrimaryLight) FontWeight.SemiBold else FontWeight.Normal,
            color = activeColor()
        )
    }
}