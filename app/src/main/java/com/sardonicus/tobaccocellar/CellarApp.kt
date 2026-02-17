@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("SameParameterValue")

package com.sardonicus.tobaccocellar

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.BottomBarButtonData
import com.sardonicus.tobaccocellar.ui.BottomSheetState
import com.sardonicus.tobaccocellar.ui.ClearAll
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.FlowMatchOption
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.IndicatorSizes
import com.sardonicus.tobaccocellar.ui.composables.OverflowRow
import com.sardonicus.tobaccocellar.ui.composables.PagerIndicator
import com.sardonicus.tobaccocellar.ui.composables.RatingRow
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
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
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

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

    val sheetOpen by remember(sheetState) { derivedStateOf { sheetState == BottomSheetState.OPENED } }

    val navIcon = LocalCustomColors.current.navIcon
    val indicatorCircle = LocalCustomColors.current.indicatorCircle
    val indicatorBorderCorrection = LocalCustomColors.current.indicatorBorderCorrection

    val buttons = remember(currentDestination, clickToAdd, isTwoPane, navIcon,
        indicatorCircle, indicatorBorderCorrection
    ) {
        listOfNotNull(
            BottomBarButtonData(
                title = "Cellar",
                icon = R.drawable.table_view_old,
                destination = HomeDestination,
                onClick = { filterViewModel.getPositionTrigger(); navigateToHome() },
                activeColor = if (currentDestination == HomeDestination && !clickToAdd) onPrimaryLight else navIcon
            ),
            // 2. Stats
            BottomBarButtonData(
                title = "Stats",
                icon = R.drawable.bar_chart,
                destination = StatsDestination,
                onClick = { filterViewModel.getPositionTrigger(); navigateToStats() },
                enabled = !databaseEmpty,
                activeColor = if (currentDestination == StatsDestination && !clickToAdd) onPrimaryLight
                else if (databaseEmpty) navIcon.copy(alpha = 0.5f)
                else navIcon
            ),
            // 3. Dates
            BottomBarButtonData(
                title = "Dates",
                icon = R.drawable.calendar_month,
                destination = DatesDestination,
                onClick = { filterViewModel.getPositionTrigger(); navigateToDates() },
                enabled = datesExist,
                showIndicator = tinsReady,
                indicatorColor = if (tinsReady) indicatorCircle else Color.Transparent,
                borderColor = if (tinsReady) {
                    if (currentDestination == DatesDestination && !clickToAdd) onPrimaryLight else navIcon
                } else Color.Transparent,
                activeColor = if (currentDestination == DatesDestination && !clickToAdd) onPrimaryLight
                else if (datesExist) navIcon
                else navIcon.copy(alpha = 0.5f)
            ),
            // 4. Filtering
            if (!isTwoPane) BottomBarButtonData(
                title = "Filter",
                icon = R.drawable.filter_24,
                onClick = filterViewModel::openBottomSheet,
                enabled = if (currentDestination == HomeDestination && !databaseEmpty) !searchPerformed else !databaseEmpty,
                showIndicator = filteringApplied,
                indicatorColor = if (filteringApplied) {
                    if (searchPerformed && currentDestination == HomeDestination) {
                        indicatorCircle.copy(alpha = 0.5f)
                    } else indicatorCircle
                } else Color.Transparent,
                borderColor = if (filteringApplied) {
                    if (searchPerformed && currentDestination == HomeDestination) {
                        indicatorBorderCorrection
                    } else {
                        if (sheetOpen) {
                            onPrimaryLight
                        } else navIcon
                    }
                } else Color.Transparent,
                activeColor = if (sheetOpen) onPrimaryLight else navIcon
            ) else null,
            // 5. Add
            BottomBarButtonData(
                title = "Add",
                icon = R.drawable.add_circle,
                onClick = { filterViewModel.updateClickToAdd(true); filterViewModel.getPositionTrigger(); navigateToAddEntry() },
                activeColor = if (clickToAdd) onPrimaryLight else navIcon
            )
        )
    }

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
                buttons.forEach { button ->
                    BottomBarButton(button, Modifier.weight(1f))
                }
            }
        }
    }
}

@Stable
@Composable
private fun BottomBarButton(
    data: BottomBarButtonData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        // The Indicator
        if (data.showIndicator) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(x = 13.dp, y = (-17).dp)
                    .clip(CircleShape)
                    .border(1.5.dp, data.borderColor, CircleShape)
                    .background(data.indicatorColor)
            )
        }

        IconButton(
            onClick = data.onClick,
            enabled = data.enabled,
            modifier = Modifier.padding(0.dp)
        ) {
            Icon(
                painter = painterResource(id = data.icon),
                contentDescription = data.title,
                modifier = Modifier
                    .size(26.dp)
                    .offset(y = (-8).dp),
                tint = data.activeColor
            )
        }

        Text(
            text = data.title,
            modifier = Modifier.offset(y = 13.dp),
            fontSize = 11.sp,
            fontWeight = if (data.activeColor == onPrimaryLight) FontWeight.SemiBold else FontWeight.Normal,
            color = data.activeColor
        )
    }
}


/** Filter sheet stuff **/
@Composable
fun FilterSheet(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val bottomSheetState by filterViewModel.bottomSheetState.collectAsState()
    val pagerState = rememberPagerState { 3 }

    if (bottomSheetState == BottomSheetState.OPENED) {
        ModalBottomSheet(
            onDismissRequest = { filterViewModel.closeBottomSheet() },
            modifier = modifier
                .statusBarsPadding(),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            dragHandle = { },
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        ) {
            val view = LocalView.current
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                SideEffect {
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
                }
            }

            Box {
                val density = LocalDensity.current
                val navigation = WindowInsets.navigationBars.getBottom(density).times(1f)
                FilterLayout(
                    filterViewModel = filterViewModel,
                    closeSheet = filterViewModel::closeBottomSheet,
                    pagerState = pagerState,
                    modifier = Modifier
                )

                Canvas(Modifier.matchParentSize()) {
                    drawRect(
                        color = Color.Black.copy(alpha = .9f),
                        topLeft = Offset(0f, (size.height)),
                        size = Size(size.width, navigation)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterLayout(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    closeSheet: () -> Unit = {},
    sheetLayout: Boolean = true,
    pagerState: PagerState = rememberPagerState { 3 },
) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 0.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FilterHeader(sheetLayout, closeSheet)

        if (sheetLayout) { PagerLayout(filterViewModel, pagerState) }
        else { PaneLayout(filterViewModel) }

        FilterFooter(filterViewModel)

    }
}

@Composable
private fun FilterHeader(
    sheetLayout: Boolean,
    closeSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sheetLayout) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "Select Filters",
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { closeSheet() }
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Filters",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                maxLines = 1,
                modifier = Modifier,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun PagerLayout(
    filterViewModel: FilterViewModel,
    pagerState: PagerState
) {
    var innerScrolling by remember { mutableStateOf(false) }
    val updateInnerScrolling: (Boolean) -> Unit = { innerScrolling = it }
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .pointerInput(pagerState) {
                var totalDrag = 0f
                val updateTotalDrag: (Float) -> Unit = { totalDrag = it }

                detectHorizontalDragGestures(
                    onDragStart = { updateTotalDrag(0f) },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        val currentDrag = totalDrag
                        updateTotalDrag(currentDrag + amount)
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (totalDrag < (-50) && pagerState.currentPage < (pagerState.pageCount - 1)) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else if (totalDrag > 50 && pagerState.currentPage > 0) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    }
                )
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(0.dp),
            indicatorSize = IndicatorSizes(6.5.dp, 6.dp)
        )
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(383.dp),
        userScrollEnabled = !innerScrolling,
        verticalAlignment = Alignment.Top,
    ) { page ->
        when (page) {
            // brand, type, rating, stock filters //
            0 -> {
                PageOne(
                    filterViewModel = filterViewModel,
                    innerScrolling = updateInnerScrolling,
                    modifier = Modifier
                )
            }

            // subgenre, cuts, components, flavoring filters //
            1 -> {
                PageTwo(
                    filterViewModel = filterViewModel,
                    modifier = Modifier
                )
            }

            // tin filtering, containers, production //
            2 -> {
                PageThree(
                    filterViewModel = filterViewModel,
                    modifier = Modifier
                        .height(383.dp)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun PaneLayout(
    filterViewModel: FilterViewModel
) {
    Spacer(Modifier.height(8.dp))
    PageOne(
        filterViewModel = filterViewModel,
        modifier = Modifier
    )
    PageTwo(
        filterViewModel = filterViewModel,
        modifier = Modifier
            .padding(vertical = 8.dp)
    )
    PageThree(
        filterViewModel = filterViewModel,
        modifier = Modifier
            .padding(top = 4.dp)
    )
}

@Composable
private fun FilterFooter(
    filterViewModel: FilterViewModel,
) {
    val filtersApplied by filterViewModel.isFilterApplied.collectAsState()

    TextButton(
        onClick = filterViewModel::resetFilter,
        modifier = Modifier
            .offset(x = (-4).dp)
            .padding(top = 6.dp),
        enabled = filtersApplied,
    ) {
        Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = null,
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
    Spacer(Modifier.height(12.dp))
}


@Composable
private fun PageOne(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    innerScrolling: (Boolean) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(6.dp),
            innerScrolling = innerScrolling
        )
        TypeFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(start = 6.dp, end = 6.dp, top = 0.dp, bottom = 6.dp),
        )
        OtherFiltersSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 0.dp),
        )
    }
}

@Composable
private fun PageTwo(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SubgenreSection(filterViewModel)
        CutSection(filterViewModel)
        ComponentSection(filterViewModel)
        FlavoringSection(filterViewModel)
    }
}

@Composable
private fun PageThree(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TinsFilterSection(filterViewModel)

        ContainerFilterSection(filterViewModel)

        ProductionFilterSection(filterViewModel)
    }
}


@Composable
fun BrandFilterSection(
    filterViewModel: FilterViewModel,
    innerScrolling: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val excludeSwitch by filterViewModel.sheetSelectedExcludeBrandSwitch.collectAsState()

    Column(
        modifier = modifier
    ) {
        // Search bar and brand include/exclude button //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandFilterSearch(
                filterViewModel = filterViewModel,
                modifier = Modifier
                    .weight(1f, false)
            )

            IncludeExcludeSwitch(
                excluded = { excludeSwitch },
                onClick = filterViewModel::updateSelectedExcludeBrandsSwitch,
                modifier = Modifier
            )
        }

        // Selectable brands row //
        SelectableBrandsRow(
            filterViewModel = filterViewModel,
            innerScrolling = innerScrolling,
            updateSelectedExcludedBrands = filterViewModel::updateSelectedExcludedBrands,
            updateSelectedBrands = filterViewModel::updateSelectedBrands,
            updateBrandSearchText = filterViewModel::updateBrandSearchText,
            modifier = Modifier.fillMaxWidth()
        )


        // Selected brands chip box //
        SelectedBrandChipBox(
            filterViewModel = filterViewModel,
            updateSelectedExcludedBrands = filterViewModel::updateSelectedExcludedBrands,
            updateSelectedBrands = filterViewModel::updateSelectedBrands,
            clearAllSelectedBrands = filterViewModel::clearAllSelectedBrands,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BrandFilterSearch(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val brandSearchText by filterViewModel.brandSearchText.collectAsState()

    val focusManager = LocalFocusManager.current
    var hasFocus by remember { mutableStateOf(false) }
    val showCursor by remember(hasFocus) { mutableStateOf(hasFocus) }

    BasicTextField(
        value = brandSearchText,
        onValueChange = filterViewModel::updateBrandSearchText,
        modifier = modifier
            .background(color = LocalCustomColors.current.textField, RoundedCornerShape(6.dp))
            .height(48.dp)
            .onFocusChanged { focusState ->
                hasFocus = focusState.hasFocus
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
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.None,
        ),
        singleLine = true,
        maxLines = 1,
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
                    if (brandSearchText.isEmpty() && !hasFocus) {
                        Text(
                            text = "Search Brands",
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
private fun IncludeExcludeSwitch(
    excluded: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(start = 12.dp)
            .width(IntrinsicSize.Max)
            .height(48.dp)
            .background(
                LocalCustomColors.current.textField,
                RoundedCornerShape(8.dp)
            )
            .border(
                Dp.Hairline,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp)
            .combinedClickable(
                onClick = { onClick() },
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
                .offset(y = 3.dp),
            color = if (!excluded()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = if (!excluded()) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Text(
            text = "Exclude",
            modifier = Modifier
                .padding(0.dp)
                .offset(y = (-3).dp),
            color = if (excluded()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = if (excluded()) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SelectableBrandsRow(
    filterViewModel: FilterViewModel,
    innerScrolling: (Boolean) -> Unit,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    updateSelectedBrands: (String, Boolean) -> Unit,
    updateBrandSearchText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    val excludeSwitch by filterViewModel.sheetSelectedExcludeBrandSwitch.collectAsState()
    val filteredBrands by filterViewModel.filteredBrands.collectAsState()
    val unselectedBrands by filterViewModel.unselectedBrands.collectAsState()
    val brandEnabled by filterViewModel.brandEnabled.collectAsState()

    val clickAction = remember(excludeSwitch) {
        { brand: String ->
            if (excludeSwitch) {
                updateSelectedExcludedBrands(brand, true)
            } else {
                updateSelectedBrands(brand, true)
            }
            updateBrandSearchText("")
        }
    }

    GlowBox(
        color = GlowColor(MaterialTheme.colorScheme.background),
        size = GlowSize(horizontal = 15.dp),
        modifier = modifier
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 2.dp)
                .height(36.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.pressed }) {
                                innerScrolling(true)
                            } else {
                                innerScrolling(false)
                            }
                        }
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            state = lazyListState
        ) {
            items(unselectedBrands, key = { it }) { brand ->
                val enabled by remember(brand) { derivedStateOf { brandEnabled[brand] ?: true } }

                BrandTextButton(
                    brand = { brand },
                    onClickAction = { clickAction(brand) },
                    enabled = { enabled },
                    modifier = Modifier
                )
            }
        }

        LaunchedEffect(filteredBrands) { lazyListState.scrollToItem(0) }
        LaunchedEffect(brandEnabled) { lazyListState.scrollToItem(0) }
    }
}

@Composable
private fun BrandTextButton(
    brand: () -> String,
    onClickAction: () -> Unit,
    enabled: () -> Boolean,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClickAction,
        modifier = modifier,
        enabled = enabled(),
    ) {
        Text(
            text = brand(),
            modifier = Modifier
        )
    }
}

@Composable
private fun SelectedBrandChipBox(
    filterViewModel: FilterViewModel,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    updateSelectedBrands: (String, Boolean) -> Unit,
    clearAllSelectedBrands: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val excludeSwitch by filterViewModel.sheetSelectedExcludeBrandSwitch.collectAsState()
    val selectedBrands by filterViewModel.selectedBrand.collectAsState()
    val showBrandChipOverflow by filterViewModel.showBrandChipOverflow.collectAsState()
    val maxWidth by filterViewModel.chipMaxWidth.collectAsState()

    val density = LocalDensity.current

    Box (
        modifier = Modifier
            .onGloballyPositioned {
                filterViewModel.updateChipBoxWidth(with(density) { it.size.width.toDp() })
            }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            Dp.Hairline,
                            LocalCustomColors.current.sheetBoxBorder.copy(alpha = .8f),
                            RoundedCornerShape(8.dp)
                        )
                        .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                        .height(96.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                ) {
                    val overflowCount by remember { derivedStateOf { selectedBrands.size - 5 } }

                    selectedBrands.take(5).forEach { brand ->
                        val onRemoved = remember(brand, excludeSwitch) {
                            {
                                if (excludeSwitch) {
                                    updateSelectedExcludedBrands(brand, false)
                                } else {
                                    updateSelectedBrands(brand, false)
                                }
                            }
                        }

                        Chip(
                            text = brand,
                            onChipClicked = { },
                            onChipRemoved = onRemoved,
                            trailingIcon = true,
                            iconSize = 20.dp,
                            trailingTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxWidth = maxWidth,
                            modifier = Modifier,
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.07f) else MaterialTheme.colorScheme.background,
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else
                                    MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    if (overflowCount > 0) {
                        Chip(
                            text = "+$overflowCount",
                            onChipClicked = { filterViewModel.showBrandOverflow() },
                            onChipRemoved = { },
                            trailingIcon = false,
                            modifier = Modifier,
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.07f) else MaterialTheme.colorScheme.background,
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else
                                    MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                Box {
                    val nothingSelected by remember { derivedStateOf { selectedBrands.isNotEmpty() } }
                    //   if (selectedBrands.isEmpty())
                    Text(
                        text = if (nothingSelected) "" else if (excludeSwitch) "Excluded Brands" else "Included Brands",
                        modifier = Modifier
                            .padding(0.dp),
                        color = if (nothingSelected) Color.Transparent else if (excludeSwitch) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (showBrandChipOverflow) {
                SelectedBrandOverflow(
                    onDismiss = filterViewModel::showBrandOverflow,
                    excludeSwitch = { excludeSwitch },
                    selectedBrands = { selectedBrands },
                    updateSelectedExcludedBrands = updateSelectedExcludedBrands,
                    updateSelectedBrands = updateSelectedBrands,
                    clearAllSelectedBrands = clearAllSelectedBrands,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun SelectedBrandOverflow(
    onDismiss: () -> Unit,
    excludeSwitch: () -> Boolean,
    selectedBrands: () -> List<String>,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    updateSelectedBrands: (String, Boolean) -> Unit,
    clearAllSelectedBrands: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (excludeSwitch()) "Excluded Brands" else "Included Brands",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                GlowBox(
                    color = GlowColor(MaterialTheme.colorScheme.background),
                    size = GlowSize(vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .heightIn(min = 0.dp, max = 280.dp),
                        userScrollEnabled = true,
                        contentPadding = PaddingValues(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy((-6).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(selectedBrands()) { brand ->
                            val onRemoved = remember(brand, excludeSwitch) {
                                {
                                    if (excludeSwitch()) {
                                        updateSelectedExcludedBrands(brand, false)
                                    } else {
                                        updateSelectedBrands(brand,  false)
                                    }
                                }
                            }
                            Chip(
                                text = brand,
                                onChipClicked = { },
                                onChipRemoved = onRemoved,
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = MaterialTheme.colorScheme.background,
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            clearAllSelectedBrands()
                            onDismiss()
                        },
                        modifier = Modifier
                            .offset(x = (-4).dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
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
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
    )
}


@Composable
fun TypeFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    types: List<String> = listOf("Aromatic", "English", "Burley", "Virginia", "Other", "(Unassigned)"),
) {
    val selectedTypes by filterViewModel.sheetSelectedTypes.collectAsState()
    val enabled by filterViewModel.typesEnabled.collectAsState()
    val typesExist by filterViewModel.typesExist.collectAsState()

    val onClick = remember {
        { type: String ->
            filterViewModel.updateSelectedTypes(type, !selectedTypes.contains(type))
        }
    }

    Column(
        modifier = modifier
    ) {
        if (!typesExist) {
            Row(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .heightIn(min = 48.dp, max = 96.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No types assigned to any blends.",
                    modifier = Modifier
                        .padding(0.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            }
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp, max = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                types.forEach {
                    val selected by remember(types) { derivedStateOf { selectedTypes.contains(it) } } // selectedTypes
                    val typeEnabled by remember(types) { derivedStateOf { enabled[it] ?: false } }

                    FilterChip(
                        selected = selected,
                        onClick = { onClick(it) },
                        label = { Text(it, fontSize = 14.sp) },
                        modifier = Modifier
                            .padding(0.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        enabled = typeEnabled
                    )
                }
            }
        }
    }
}


@Composable
fun OtherFiltersSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val favDisExist by filterViewModel.favDisExist.collectAsState()
    val ratingsExist by filterViewModel.ratingsExist.collectAsState()
    val rangeEnabled by filterViewModel.rangeEnabled.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            // Ratings //
            Column(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        LocalCustomColors.current.sheetBoxBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                    .width(229.dp)
                    .padding(vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                FavoriteDislikeFilters(filterViewModel, { favDisExist }, { ratingsExist })

                StarRatingFilters(filterViewModel, rangeEnabled, { favDisExist }, { ratingsExist })
            }
            if (!favDisExist && !ratingsExist) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            Dp.Hairline,
                            LocalCustomColors.current.sheetBoxBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                            RoundedCornerShape(8.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ratings assigned.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // In Stock
        Box {
            InStockSection(filterViewModel)
        }
    }
}

@Composable
private fun FavoriteDislikeFilters(
    filterViewModel: FilterViewModel,
    favDisExist: () -> Boolean,
    ratingsExist: () -> Boolean
) {
    val favoritesSelection by filterViewModel.favoriteSelection.collectAsState()
    val dislikedsSelection by filterViewModel.dislikedSelection.collectAsState()
    val favoritesEnabled by filterViewModel.favoritesEnabled.collectAsState()
    val dislikedsEnabled by filterViewModel.dislikedsEnabled.collectAsState()

    Box {
        Row {
            TriStateCheckWithLabel(
                text = "Favorites",
                state = favoritesSelection,
                onClick = filterViewModel::updateFavSelection,
                colors = CheckboxDefaults.colors(
                    checkedColor =
                        when (favoritesSelection) {
                            ToggleableState.On -> MaterialTheme.colorScheme.primary
                            ToggleableState.Indeterminate -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        },
                ),
                enabled = favoritesEnabled,
                maxLines = 1,
            )

            TriStateCheckWithLabel(
                text = "Dislikes",
                state = dislikedsSelection,
                onClick = filterViewModel::updateDisSelection,
                colors = CheckboxDefaults.colors(
                    checkedColor =
                        when (dislikedsSelection) {
                            ToggleableState.On -> MaterialTheme.colorScheme.primary
                            ToggleableState.Indeterminate -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        },
                ),
                enabled = dislikedsEnabled,
                maxLines = 1,
            )
        }
        if (!favDisExist() && ratingsExist()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                        RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)
                    )
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorites/dislikes assigned.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun StarRatingFilters(
    filterViewModel: FilterViewModel,
    rangeEnabled: Boolean,
    favDisExist: () -> Boolean,
    ratingsExist: () -> Boolean
) {
//    val rangeEnabled by filterViewModel.rangeEnabled.collectAsState()
    val ratingLowEnabled by filterViewModel.ratingLowEnabled.collectAsState()
    val ratingHighEnabled by filterViewModel.ratingHighEnabled.collectAsState()
    val unratedEnabled by filterViewModel.unratedEnabled.collectAsState()

    val unchosen by filterViewModel.rangeUnchosen.collectAsState()
    val unrated by filterViewModel.sheetSelectedUnrated.collectAsState()
    val ratingLow by filterViewModel.sheetSelectedRatingLow.collectAsState()
    val ratingHigh by filterViewModel.sheetSelectedRatingHigh.collectAsState()

    val lowText by filterViewModel.rangeLowText.collectAsState()
    val highText by filterViewModel.rangeHighText.collectAsState()
    val lowTextAlpha by filterViewModel.rangeLowTextAlpha.collectAsState()
    val highTextAlpha by filterViewModel.rangeHighTextAlpha.collectAsState()
    val ratingRowEmptyAlpha by filterViewModel.ratingRowEmptyAlpha.collectAsState()

    val showRatingPop by filterViewModel.showRatingPop.collectAsState()

    Box {
        Row(
            modifier = Modifier
                .height(36.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .alpha(if (rangeEnabled) 1f else .38f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Rating:",
                modifier = Modifier
                    .padding(end = 8.dp),
                fontSize = 15.sp
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        enabled = rangeEnabled,
                        onClick = filterViewModel::onShowRatingPop
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
            ) {
                Box (
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .width(20.dp)
                ) {
                    Text(
                        text = lowText,
                        modifier = Modifier,
                        fontSize = 13.sp,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        color = LocalContentColor.current.copy(alpha = lowTextAlpha)
                    )
                }
                RatingRow(
                    range = Pair(ratingLow, ratingHigh),
                    modifier = Modifier
                        .padding(horizontal = 5.dp),
                    starSize = 18.dp,
                    showDivider = true,
                    minColor = LocalContentColor.current,
                    minAlpha = .38f,
                    emptyColor = if (unchosen || (ratingLow != null && ratingHigh == null)) LocalCustomColors.current.starRating else LocalContentColor.current,
                    emptyAlpha = ratingRowEmptyAlpha
                )
                Box (
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .width(20.dp)
                ) {
                    Text(
                        text = highText,
                        modifier = Modifier,
                        fontSize = 13.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Start,
                        color = LocalContentColor.current.copy(alpha = highTextAlpha)
                    )
                }
            }
        }

        if (favDisExist() && !ratingsExist()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                        RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)
                    )
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ratings assigned.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }

    if (showRatingPop) {
        RatingRangePop(
            unrated = { unrated },
            unratedEnabled = { unratedEnabled },
            ratingLow = { ratingLow },
            ratingLowEnabled = { ratingLowEnabled },
            ratingHigh = { ratingHigh },
            ratingHighEnabled = { ratingHighEnabled },
            updateSelectedUnrated = filterViewModel::updateSelectedUnrated,
            updateSelectedRatingRange = filterViewModel::updateSelectedRatingRange,
            onDismiss = filterViewModel::onShowRatingPop,
            modifier = Modifier,
        )
    }
}

@Composable
private fun RatingRangePop(
    unrated: () -> Boolean,
    unratedEnabled: () -> Boolean,
    ratingLow: () -> Double?,
    ratingLowEnabled: () -> Double?,
    ratingHigh: () -> Double?,
    ratingHighEnabled: () -> Double?,
    updateSelectedUnrated: (Boolean) -> Unit,
    updateSelectedRatingRange: (Double?, Double?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var ratingLowString by rememberSaveable { mutableStateOf(formatDecimal(ratingLow())) }
    var selectedLow by rememberSaveable { mutableStateOf(ratingLow()) }
    val minRating by rememberSaveable { mutableStateOf(ratingLowEnabled()) }

    var ratingHighString by rememberSaveable { mutableStateOf(formatDecimal(ratingHigh())) }
    var selectedHigh by rememberSaveable { mutableStateOf(ratingHigh()) }
    val maxRating by rememberSaveable { mutableStateOf(ratingHighEnabled()) }

    val compMin = maxOf((selectedLow ?: 0.0), (minRating ?: 0.0))
    val compMax = minOf((selectedHigh ?: 5.0), (maxRating ?: 5.0))

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
    val decimalSeparator = symbols.decimalSeparator.toString()
    val allowedPattern = remember(decimalSeparator) {
        val ds = Regex.escape(decimalSeparator)
        Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .wrapContentHeight(),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = "Rating",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
            ) {
                CheckboxWithLabel(
                    text = "Unrated",
                    checked = unrated(),
                    onCheckedChange = {
                        updateSelectedUnrated(it)
                        updateSelectedRatingRange(selectedLow, selectedHigh)
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                    enabled = unratedEnabled(),
                    fontColor = if (!unratedEnabled()) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current,
                )
                // Rating Range //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Low Rating //
                    TextField(
                        value = ratingLowString,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                ratingLowString = it

                                try {
                                    if (it.isNotBlank()) {
                                        val preNumber = if (it.startsWith(decimalSeparator)) {
                                            "0$it"
                                        } else it
                                        val number = numberFormat.parse(preNumber)?.toDouble()

                                        selectedLow = when {
                                            number == null -> null
                                            number < (minRating ?: 0.0) -> (minRating ?: 0.0)
                                            number > compMax -> compMax
                                            else -> number
                                        }
                                    } else {
                                        selectedLow = null
                                    }

                                } catch (e: ParseException) {
                                    Log.e("Rating filter low", "Input: $it", e)
                                }
                            }
                        },
                        modifier = Modifier
                            .width(70.dp)
                            .padding(end = 8.dp)
                            .onFocusChanged {
                                if (!it.isFocused) {
                                    if (ratingLowString != formatDecimal(selectedLow))
                                        ratingLowString = formatDecimal(selectedLow)
                                    updateSelectedRatingRange(selectedLow, selectedHigh)
                                }
                            },
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                updateSelectedRatingRange(selectedLow, selectedHigh)
                                this.defaultKeyboardAction(ImeAction.Done)
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField,
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )

                    // Selected Range Display //
                    val lowField = ratingLowString.toDoubleOrNull()?.coerceIn(0.0, compMax)
                    val highField = ratingHighString.toDoubleOrNull()?.coerceIn(compMin, 5.0)

                    val emptyColor = if (ratingLowString.isNotBlank() && ratingHighString.isBlank()) LocalCustomColors.current.starRating else LocalContentColor.current
                    val emptyAlpha = if (ratingLowString.isNotBlank() && ratingHighString.isBlank()) 1f else .5f

                    RatingRow(
                        range = Pair(lowField, highField),
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                        starSize = 20.dp,
                        showDivider = true,
                        minColor = LocalContentColor.current,
                        maxColor = LocalCustomColors.current.starRating,
                        emptyColor = emptyColor,
                        minAlpha = .5f,
                        maxAlpha = 1f,
                        emptyAlpha = emptyAlpha
                    )

                    // High Rating //
                    TextField(
                        value = ratingHighString,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                ratingHighString = it

                                try {
                                    if (it.isNotBlank()) {
                                        val preNumber = if (it.startsWith(decimalSeparator)) {
                                            "0$it"
                                        } else it
                                        val number = numberFormat.parse(preNumber)?.toDouble()

                                        selectedHigh = when {
                                            number == null -> null
                                            number < compMin -> compMin
                                            number > (maxRating ?: 5.0) -> (maxRating ?: 5.0)
                                            else -> number
                                        }
                                    } else {
                                        selectedHigh = null
                                    }

                                } catch (e: ParseException) {
                                    Log.e("Rating filter high", "Input: $it", e)
                                }
                            }
                        },
                        modifier = Modifier
                            .width(70.dp)
                            .padding(start = 8.dp)
                            .onFocusChanged {
                                if (!it.isFocused) {
                                    if (ratingHighString != formatDecimal(selectedHigh))
                                        ratingHighString = formatDecimal(selectedHigh)
                                    updateSelectedRatingRange(selectedLow, selectedHigh)
                                }
                            },
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                updateSelectedRatingRange(selectedLow, selectedHigh)
                                this.defaultKeyboardAction(ImeAction.Done)
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField,
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                }

                // Clear buttons and available range //
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 29.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lowAlpha = if (ratingLowString.isNotBlank()) .75f else 0.38f
                    val highAlpha = if (ratingHighString.isNotBlank()) .75f else 0.38f

                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = ratingLowString.isNotBlank()
                            ) {
                                ratingLowString = ""
                                selectedLow = null
                                updateSelectedRatingRange(null, selectedHigh)
                            }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(lowAlpha)
                    )
                    Text(
                        text = "(Limits: ${formatDecimal(minRating).ifBlank { "0.0" }} - ${formatDecimal(maxRating).ifBlank { "5.0" }})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier,
                        color = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = ratingHighString.isNotBlank()
                            ) {
                                ratingHighString = ""
                                selectedHigh = null
                                updateSelectedRatingRange(selectedLow, null)
                            }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(highAlpha)
                    )
                }

                // Clear all button //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = {
                            ratingLowString = ""
                            selectedLow = null
                            ratingHighString = ""
                            selectedHigh = null
                            updateSelectedUnrated(false)
                            updateSelectedRatingRange(null, null)
                        },
                        enabled = ratingLowString.isNotBlank() || ratingHighString.isNotBlank() || unrated(),
                        modifier = Modifier
                            .offset(x = (-4).dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
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
            TextButton(
                onClick = {
                    updateSelectedRatingRange(selectedLow, selectedHigh)
                    onDismiss()
                },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
private fun InStockSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val inStock by filterViewModel.sheetSelectedInStock.collectAsState()
    val outOfStock by filterViewModel.sheetSelectedOutOfStock.collectAsState()
    val inStockEnabled by filterViewModel.inStockEnabled.collectAsState()
    val outOfStockEnabled by filterViewModel.outOfStockEnabled.collectAsState()

    Column(
        modifier = modifier
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .width(IntrinsicSize.Max)
            .padding(vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.Start
    ) {
        CheckboxWithLabel(
            text = "In-stock",
            checked = inStock,
            onCheckedChange = filterViewModel::updateSelectedInStock,
            modifier = Modifier,
            enabled = inStockEnabled,
            allowResize = true
        )
        CheckboxWithLabel(
            text = "Out",
            checked = outOfStock,
            onCheckedChange = filterViewModel::updateSelectedOutOfStock,
            modifier = Modifier,
            enabled = outOfStockEnabled,
            allowResize = false
        )
    }
}


@Composable
private fun SubgenreSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.subgenreAvailable.collectAsState()
    val selected by filterViewModel.sheetSelectedSubgenres.collectAsState()
    val enabled by filterViewModel.subgenresEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(Unassigned)" } } }

    FlowFilterSection(
        label = { "Subgenre" },
        nothingLabel = { "No subgenres assigned to any blends." },
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedSubgenre,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = { nothingAssigned },
        clearAll = { filterViewModel.clearAllSelected(ClearAll.SUBGENRE) },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
private fun CutSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.cutAvailable.collectAsState()
    val selected by filterViewModel.sheetSelectedCuts.collectAsState()
    val enabled by filterViewModel.cutsEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(Unassigned)" } } }

    FlowFilterSection(
        label = { "Cut" },
        nothingLabel = { "No cuts assigned to any blends." },
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedCut,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = { nothingAssigned },
        clearAll = { filterViewModel.clearAllSelected(ClearAll.CUT) },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}

@Composable
private fun ComponentSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.componentAvailable.collectAsState()
    val selected by filterViewModel.sheetSelectedComponents.collectAsState()
    val enabled by filterViewModel.componentsEnabled.collectAsState()
    val matching by filterViewModel.sheetSelectedCompMatching.collectAsState()
    val matchEnablement by filterViewModel.compMatchingEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(None Assigned)" } } }

    FlowFilterSection(
        label = { "Components" },
        nothingLabel = { "No components assigned to any blends." },
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedComponent,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = { nothingAssigned },
        matching = { matching },
        matchOptionEnablement = { matchEnablement },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        clearAll = { filterViewModel.clearAllSelected(ClearAll.COMPONENT) },
        onMatchOptionChange = filterViewModel::updateCompMatching,
    )
}

@Composable
private fun FlavoringSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.flavoringAvailable.collectAsState()
    val selected by filterViewModel.sheetSelectedFlavorings.collectAsState()
    val enabled by filterViewModel.flavoringsEnabled.collectAsState()
    val matching by filterViewModel.sheetSelectedFlavorMatching.collectAsState()
    val matchEnablement by filterViewModel.flavorMatchingEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(None Assigned)" } } }

    FlowFilterSection(
        label = { "Flavorings" },
        nothingLabel = { "No flavorings assigned to any blends." },
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedFlavoring,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = { nothingAssigned },
        matching = { matching },
        matchOptionEnablement = { matchEnablement },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        clearAll = { filterViewModel.clearAllSelected(ClearAll.FLAVORING) },
        onMatchOptionChange = filterViewModel::updateFlavorMatching,
    )
}


@Composable
fun FlowFilterSection(
    label: () -> String,
    nothingLabel: () -> String,
    available: () -> List<String>,
    selected: () -> List<String>,
    enabled: () -> Map<String, Boolean>,
    updateSelectedOptions: (String, Boolean) -> Unit,
    overflowCheck: (List<String>, List<String>, Int) -> Boolean,
    nothingAssigned: () -> Boolean,
    modifier: Modifier = Modifier,
    matching: () -> FlowMatchOption? = { null },
    matchOptionEnablement: () -> Map<FlowMatchOption, Boolean> = { mapOf() },
    clearAll: () -> Unit = {},
    onMatchOptionChange: (FlowMatchOption) -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {
        var showOverflowPopup by remember { mutableStateOf(false) }

        // Header and Match options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier,
                color = if (nothingAssigned()) LocalContentColor.current.copy(alpha = 0.4f) else LocalContentColor.current
            )
            if (matching() != null) {
                FlowFilterMatchOptions(
                    nothingAssigned, matching, matchOptionEnablement, { onMatchOptionChange(it) },
                    Modifier, Arrangement.End
                )
            }
        }

        if (nothingAssigned()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nothingLabel(),
                    modifier = Modifier
                        .padding(0.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        } else {
            OverflowWrapper(
                available = available(),
                selected = selected(),
                enabled = enabled(),
                updateSelectedOptions = updateSelectedOptions,
                overflowCheck = overflowCheck,
                showOverflowPopup = { showOverflowPopup = it }
            )

            if (showOverflowPopup) {
                FlowFilterOverflowPopup(
                    onDismiss = { showOverflowPopup = false },
                    label = label,
                    available = available,
                    selected = selected,
                    enabled = enabled,
                    matching = matching,
                    matchOptionEnablement = matchOptionEnablement,
                    enableMatchOption = { matching() != null },
                    onMatchOptionChange = onMatchOptionChange,
                    updateSelectedOptions = updateSelectedOptions,
                    clearAll = clearAll,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun OverflowWrapper(
    available: List<String>,
    selected: List<String>,
    enabled: Map<String, Boolean>,
    updateSelectedOptions: (String, Boolean) -> Unit,
    overflowCheck: (List<String>, List<String>, Int) -> Boolean,
    showOverflowPopup: (Boolean) -> Unit,
) {
    OverflowRow(
        items = available,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Top)
            .padding(horizontal = 4.dp),
        itemSpacing = 6.dp,
        itemContent = {
            val isSelected = selected.contains(it)

            FilterChipWrapper(
                label = { it },
                selected = { isSelected },
                enabled = { enabled[it] ?: false },
                onClick = { updateSelectedOptions(it, !isSelected) },
                modifier = Modifier
                    .widthIn(max = 140.dp)
            )
        },
        enabledAtIndex = { enabled[available[it]] ?: true },
        overflowIndicator = { overflowCount, enabledCount, overflowEnabled -> // overflowEnabled means count > 0
            val overflowedSelected = overflowCheck(selected, available, available.size - overflowCount)

            val labelColor =
                if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
//                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.onSecondaryContainer
//                        else if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer
//                        else if (!overflowEnabled) MaterialTheme.colorScheme.onSurfaceVariant
//                        else MaterialTheme.colorScheme.onSurfaceVariant
            val containerColor =
                if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.background
//                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
//                        else if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer
//                        else if (!overflowEnabled) MaterialTheme.colorScheme.background
//                        else MaterialTheme.colorScheme.background
            val borderColor =
                if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.outlineVariant
//                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
//                        else if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer
//                        else if (!overflowEnabled) MaterialTheme.colorScheme.outlineVariant
//                        else MaterialTheme.colorScheme.outlineVariant


            Chip(
                text = "+$enabledCount",  // "+$overflowCount"
                onChipClicked = { showOverflowPopup(true) },
                onChipRemoved = { },
                enabled = true,
                trailingIcon = false,
                modifier = Modifier,
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = labelColor,
                    containerColor = containerColor
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = borderColor
                ),
            )
        },
    )
}

@Composable
fun FilterChipWrapper(
    label: () -> String,
    selected: () -> Boolean,
    enabled: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected(),
        onClick = onClick,
        label = {
            Text(
                text = label(),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier
            .padding(0.dp),
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        enabled = enabled()
    )
}

@Composable
fun FlowFilterMatchOptions(
    nothingAssigned: () -> Boolean,
    matching: () -> FlowMatchOption?,
    matchOptionEnablement: () -> Map<FlowMatchOption, Boolean>,
    onMatchOptionChange: (FlowMatchOption) -> Unit,
    modifier: Modifier = Modifier,
    arrangement: Arrangement.Horizontal = Arrangement.Center,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Match: ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier,
            color = if (nothingAssigned()) LocalContentColor.current.copy(alpha = 0.4f) else LocalContentColor.current
        )

        FlowMatchOption.entries.forEachIndexed { index, it ->
            val enabled = !nothingAssigned() && (matchOptionEnablement()[it] ?: false)
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .clickable(
                        enabled = enabled,
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { onMatchOptionChange(it) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it.value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Transparent,
                )
                Text(
                    text = it.value,
                    fontSize = 14.sp,
                    fontWeight = if (matching() == it && !nothingAssigned()) FontWeight.Medium else FontWeight.Normal,
                    color = if (matching() == it && !nothingAssigned()) MaterialTheme.colorScheme.primary else if (enabled) LocalContentColor.current.copy(alpha = .6f) else LocalContentColor.current.copy(alpha = .38f),
                    modifier = Modifier
                )
            }
            if (index != FlowMatchOption.entries.lastIndex) {
                Text(
                    text = " / ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier,
                    color = if (nothingAssigned()) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
                )
            }
        }
    }
}

@Composable
fun FlowFilterOverflowPopup(
    onDismiss: () -> Unit,
    label: () -> String,
    available: () -> List<String>,
    selected: () -> List<String>,
    enabled: () -> Map<String, Boolean>,
    enableMatchOption: () -> Boolean,
    matching: () -> FlowMatchOption?,
    matchOptionEnablement: () -> Map<FlowMatchOption, Boolean>,
    onMatchOptionChange: (FlowMatchOption) -> Unit,
    updateSelectedOptions: (String, Boolean) -> Unit,
    clearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = label(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier
            .fillMaxWidth(.9f),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        ),
        shape = MaterialTheme.shapes.medium,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top)
            ) {
                GlowBox(
                    color = GlowColor(MaterialTheme.colorScheme.background),
                    size = GlowSize(vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, 280.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        // match options
                        if (enableMatchOption()) {
                            FlowFilterMatchOptions(
                                { false }, matching, matchOptionEnablement, { onMatchOptionChange(it) },
                                Modifier.padding(bottom = 4.dp), Arrangement.Center
                            )
                        }

                        // Chips
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.spacedBy(
                                4.dp,
                                Alignment.Start
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                0.dp,
                                Alignment.Top
                            )
                        ) {
                            available().forEach {
                                FilterChip(
                                    selected = selected().contains(it),
                                    onClick = {
                                        updateSelectedOptions(it, !selected().contains(it))
                                    },
                                    label = { Text(text = it, fontSize = 14.sp) },
                                    modifier = Modifier
                                        .padding(0.dp),
                                    shape = MaterialTheme.shapes.small,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    ),
                                    enabled = enabled()[it] ?: false
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            clearAll()
                        },
                        modifier = Modifier
                            .offset(x = (-4).dp),
                        enabled = selected().isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
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
            Button(onClick = { onDismiss() }) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
    )
}


@Composable
fun TinsFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val hasTins by filterViewModel.sheetSelectedHasTins.collectAsState()
    val implicitHas by filterViewModel.implicitHasTins.collectAsState()
    val noTins by filterViewModel.sheetSelectedNoTins.collectAsState()
    val opened by filterViewModel.sheetSelectedOpened.collectAsState()
    val unopened by filterViewModel.sheetSelectedUnopened.collectAsState()
    val finished by filterViewModel.sheetSelectedFinished.collectAsState()
    val unfinished by filterViewModel.sheetSelectedUnfinished.collectAsState()

    val hasEnabled by filterViewModel.hasTinsEnabled.collectAsState()
    val noEnabled by filterViewModel.noTinsEnabled.collectAsState()
    val openedEnabled by filterViewModel.openedEnabled.collectAsState()
    val unopenedEnabled by filterViewModel.unopenedEnabled.collectAsState()
    val finishedEnabled by filterViewModel.finishedEnabled.collectAsState()
    val unfinishedEnabled by filterViewModel.unfinishedEnabled.collectAsState()

    val tinsExist by filterViewModel.tinsExist.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.Top)
    ) {
        // Has tins/Opened/Finished
        Box{
            Row(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        LocalCustomColors.current.sheetBoxBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Has Tins
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Box {
                        CheckboxWithLabel(
                            text = "",
                            checked = implicitHas,
                            onCheckedChange = { },
                            modifier = Modifier,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                uncheckedColor = Color.Transparent,
                                disabledUncheckedColor = Color.Transparent
                            ),
                        )
                        CheckboxWithLabel(
                            text = "Has tins",
                            checked = hasTins,
                            onCheckedChange = filterViewModel::updateSelectedHasTins,
                            modifier = Modifier,
                            enabled = hasEnabled,
                            fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        )
                    }
                    CheckboxWithLabel(
                        text = "No tins",
                        checked = noTins,
                        onCheckedChange = filterViewModel::updateSelectedNoTins,
                        modifier = Modifier,
                        enabled = noEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                }

                // Opened
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    CheckboxWithLabel(
                        text = "Opened",
                        checked = opened,
                        onCheckedChange = filterViewModel::updateSelectedOpened,
                        modifier = Modifier,
                        enabled = openedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                    CheckboxWithLabel(
                        text = "Unopened",
                        checked = unopened,
                        onCheckedChange = filterViewModel::updateSelectedUnopened,
                        modifier = Modifier,
                        enabled = unopenedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                }

                // Finished
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    CheckboxWithLabel(
                        text = "Finished",
                        checked = finished,
                        onCheckedChange = filterViewModel::updateSelectedFinished,
                        modifier = Modifier,
                        enabled = finishedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                    CheckboxWithLabel(
                        text = "Unfinished",
                        checked = unfinished,
                        onCheckedChange = filterViewModel::updateSelectedUnfinished,
                        modifier = Modifier,
                        enabled = unfinishedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                }
            }
            if (!tinsExist) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            Dp.Hairline,
                            LocalCustomColors.current.sheetBoxBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                            RoundedCornerShape(8.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No tins assigned to any blends.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ContainerFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.containerAvailable.collectAsState()
    val selected by filterViewModel.sheetSelectedContainer.collectAsState()
    val enabled by filterViewModel.containerEnabled.collectAsState()
    val tinsExist by filterViewModel.tinsExist.collectAsState()
    val nothingLabel by remember(tinsExist) {
        derivedStateOf {
            if (tinsExist) "No containers assigned to any tins." else "No tins assigned to any blends."
        }
    }
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(Unassigned)" } } }

    FlowFilterSection(
        label = { "Tin Containers" },
        nothingLabel = { nothingLabel },
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedContainer,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = { nothingAssigned },
        clearAll = { filterViewModel.clearAllSelected(ClearAll.CONTAINER) },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}


@Composable
private fun ProductionFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val production by filterViewModel.sheetSelectedProduction.collectAsState()
    val outOfProduction by filterViewModel.sheetSelectedOutOfProduction.collectAsState()

    val productionEnabled by filterViewModel.productionEnabled.collectAsState()
    val outOfProductionEnabled by filterViewModel.outOfProductionEnabled.collectAsState()

    Row(
        modifier = modifier
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .width(IntrinsicSize.Max),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        CheckboxWithLabel(
            text = "In Production",
            checked = production,
            onCheckedChange = filterViewModel::updateSelectedProduction,
            modifier = Modifier,
            enabled = productionEnabled
        )
        CheckboxWithLabel(
            text = "Discontinued",
            checked = outOfProduction,
            onCheckedChange = filterViewModel::updateSelectedOutOfProduction,
            modifier = Modifier,
            enabled = outOfProductionEnabled
        )
    }
}

/** Custom composables for sheet **/
@Composable
fun CheckboxWithLabel(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    height: Dp = 36.dp,
    enabled: Boolean = true,
    fontColor: Color = LocalContentColor.current,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    allowResize: Boolean = false,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() }
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(height)
            .offset(x = (-2).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .padding(0.dp),
                enabled = enabled,
                colors = colors,
                interactionSource = interactionSource
            )
        }
        Text(
            text = text,
            style = LocalTextStyle.current.copy(
                color = if (enabled) fontColor else fontColor.copy(alpha = 0.5f),
                lineHeight = lineHeight
            ),
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
            maxLines = 1,
            fontSize = if (!allowResize) fontSize else TextUnit.Unspecified,
            autoSize = if (!allowResize) { null } else {
                TextAutoSize.StepBased(
                    maxFontSize = fontSize,
                    minFontSize = 9.sp,
                    stepSize = .2.sp
                )
            }
        )


//        Checkbox(
//            checked = checked,
//            onCheckedChange = onCheckedChange,
//            modifier = Modifier
//                .padding(0.dp),
//            enabled = enabled,
//            colors = colors,
//            interactionSource = interactionSource
//        )
//        Box(
//            modifier = Modifier
//                .offset(x = (-4).dp)
//                .padding(end = 6.dp),
//            contentAlignment = Alignment.CenterStart
//        ) {
//            Text(
//                text = text,
//                style = LocalTextStyle.current.copy(
//                    color = if (enabled) fontColor else fontColor.copy(alpha = 0.5f),
//                    lineHeight = lineHeight
//                ),
//                modifier = Modifier,
//                maxLines = 1,
//                autoSize = TextAutoSize.StepBased(
//                    maxFontSize = fontSize,
//                    minFontSize = 9.sp,
//                    stepSize = .2.sp
//                )
//            )
//        }
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
    maxLines: Int = Int.MAX_VALUE,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() }
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(36.dp)
            .offset(x = (-2).dp),
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
                interactionSource = interactionSource
            )
        }
        Text(
            text = text,
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
            color = if (enabled) fontColor else fontColor.copy(alpha = 0.5f),
            fontSize = 15.sp,
            maxLines = maxLines,
        )
    }
}


@Composable
fun Chip(
    text: String,
    onChipClicked: (String) -> Unit,
    onChipRemoved: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 14.sp,
    trailingIcon: Boolean = true,
    iconSize: Dp = 24.dp,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(
        enabled = enabled,
        borderColor = MaterialTheme.colorScheme.outline,
    ),
    maxWidth: Dp = Dp.Infinity,
    trailingTint: Color = LocalContentColor.current
) {
    AssistChip(
        onClick = { onChipClicked(text) },
        label = {
            if (text.startsWith("+")) {
                Box (
                    modifier = Modifier
                        .width(25.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        maxLines = 1,
                        modifier = Modifier,
                        style = LocalTextStyle.current.copy(
                            color = LocalContentColor.current
                        ),
                        minLines = 1,
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = fontSize,
                            minFontSize = 9.sp,
                            stepSize = .2.sp
                        )
                    )
                }
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
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Remove Chip",
                    modifier = Modifier
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { onChipRemoved() }
                        .size(iconSize),
                    tint = trailingTint
                )
            } else { /** do nothing */ }
        },
        modifier = modifier
            .widthIn(max = maxWidth)
            .padding(0.dp),
        enabled = enabled,
        colors = colors,
        border = border
    )
}