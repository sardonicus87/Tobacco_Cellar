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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.navigation3.runtime.NavKey
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.BottomSheetState
import com.sardonicus.tobaccocellar.ui.BrandsSectionData
import com.sardonicus.tobaccocellar.ui.FilterSectionData
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.OtherSectionData
import com.sardonicus.tobaccocellar.ui.ProductionSectionData
import com.sardonicus.tobaccocellar.ui.TinsFilterData
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
import com.sardonicus.tobaccocellar.ui.settings.ExportRating
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
    navigationState: NavigationState = rememberNavigationState(
        startRoute = HomeDestination,
        topLevelRoutes = setOf(HomeDestination, StatsDestination, DatesDestination)
    ),
    navigator: Navigator = remember { Navigator(navigationState) }
) {

    CellarNavigation(
        navigator = navigator,
        navigationState = navigationState,
    )

    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val bottomSheetState by filterViewModel.bottomSheetState.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 3 })

    val brandsData by filterViewModel.brandsData.collectAsState()
    val typeData by filterViewModel.typeData.collectAsState()
    val otherData by filterViewModel.otherData.collectAsState()
    val subgenreData by filterViewModel.subgenreData.collectAsState()
    val cutData by filterViewModel.cutData.collectAsState()
    val componentData by filterViewModel.componentData.collectAsState()
    val flavoringData by filterViewModel.flavoringData.collectAsState()
    val tinsData by filterViewModel.tinsFilterData.collectAsState()
    val containerData by filterViewModel.containerData.collectAsState()
    val productionData by filterViewModel.productionData.collectAsState()

    val favDisExist by filterViewModel.favDisExist.collectAsState()
    val tins by filterViewModel.tinsExist.collectAsState()
    val hasContainer = remember(containerData) { containerData.selected.isNotEmpty() }
    val filtersApplied by filterViewModel.isFilterApplied.collectAsState()

    if (bottomSheetState == BottomSheetState.OPENED) {
        ModalBottomSheet(
            onDismissRequest = { filterViewModel.closeBottomSheet() },
            modifier = Modifier
                .statusBarsPadding(),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            dragHandle = { },
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        ) {
            Box {
                FilterBottomSheet(
                    filterViewModel = filterViewModel,
                    closeSheet = { filterViewModel.closeBottomSheet() },
                    pagerState = pagerState,
                    filtersApplied = filtersApplied,
                    brandsData = brandsData,
                    typeData = typeData,
                    otherData = otherData,
                    favDisExist = favDisExist,
                    subgenreData = subgenreData,
                    cutData = cutData,
                    componentData = componentData,
                    flavoringData = flavoringData,
                    containerData = containerData,
                    tinsFilterData = tinsData,
                    updateSelectedTins = { string, it -> filterViewModel.updateSelectedTins(string, it) },
                    hasContainer = hasContainer,
                    tins = tins,
                    productionData = productionData,
                    modifier = Modifier
                )
                Box (Modifier.matchParentSize()) {
                    val navigation = WindowInsets.navigationBars.getBottom(LocalDensity.current).times(1f)

                    Canvas(Modifier.fillMaxSize()) {
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
    preferencesRepo: PreferencesRepo = LocalCellarApplication.current.preferencesRepo,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var menuState by rememberSaveable { mutableStateOf(MenuState.MAIN) }

    var exportCsvPopup by rememberSaveable { mutableStateOf(false) }
    var exportType by remember { mutableStateOf<ExportType?>(null) }
    val previouslySavedExportRating by preferencesRepo.exportRating.collectAsState(ExportRating())

    var allItems by rememberSaveable { mutableStateOf(true) }
    val selectAll: (Boolean) -> Unit = { allItems = it }
    var exportRating by remember { mutableStateOf(previouslySavedExportRating) }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            exportCsvHandler?.onExportCsvClick(uri, allItems, exportRating)
        }
    }
    val exportCsvIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv"
        putExtra(Intent.EXTRA_TITLE, "tobacco_cellar.csv")
    }

    val exportAsTinsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            exportCsvHandler?.onTinsExportCsvClick(uri, allItems, exportRating)
        }
    }
    val exportAsTinsIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv"
        putExtra(Intent.EXTRA_TITLE, "tobacco_cellar_as_tins.csv")
    }

    val coroutineScope = rememberCoroutineScope()
    val expanded: (Boolean) -> Unit = { menuExpanded = it }
    val changeMenuState: (MenuState) -> Unit = { menuState = it }
    val showExportCsv: (Boolean) -> Unit = { exportCsvPopup = it }


    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (showMenu) {
                IconButton(
                    onClick = {
                        expanded(!menuExpanded)
                        filterViewModel.getPositionTrigger()
                    },
                    modifier = Modifier
                        .size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vert),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { expanded(false) },
                        modifier = Modifier,
                        containerColor = LocalCustomColors.current.textField,
                    ) {
                        when (menuState) {
                            MenuState.MAIN -> {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.bulk_edit_title)) },
                                    onClick = {
                                        expanded(false)
                                        navigateToBulkEdit()
                                    },
                                    modifier = Modifier
                                        .padding(0.dp),
                                    enabled = currentDestination == HomeDestination,
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.import_csv)) },
                                    onClick = {
                                        expanded(false)
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
                                        changeMenuState(MenuState.EXPORT_CSV)
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
                                        expanded(false)
                                        navigateToPlaintext()
                                    },
                                    modifier = Modifier
                                        .padding(0.dp),
                                    enabled = currentDestination == HomeDestination,
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.help_faq)) },
                                    onClick = {
                                        expanded(false)
                                        navigateToHelp()
                                    },
                                    modifier = Modifier
                                        .padding(0.dp),
                                    enabled = true,
                                )

                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.settings)) },
                                    onClick = {
                                        expanded(false)
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
                                    onClick = { changeMenuState(MenuState.MAIN) }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Normal") },
                                    onClick = {
                                        exportType = ExportType.ITEMS
                                        showExportCsv(true)
                                        expanded(false)
                                        changeMenuState(MenuState.MAIN)
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
                                        exportType = ExportType.TINS
                                        showExportCsv(true)
                                        expanded(false)
                                        changeMenuState(MenuState.MAIN)
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
        val options = listOf("All", "Filtered")
        val selectedIndex = if (allItems) 0 else 1

        var currentMaxString by rememberSaveable { mutableStateOf(exportRating.maxRating.toString()) }
        var currentRoundingString by rememberSaveable { mutableStateOf(exportRating.rounding.toString()) }
        val allowedMax = remember { Regex("^(\\s*|\\d{0,3})$") }

        AlertDialog(
            onDismissRequest = { showExportCsv(false) },
            title = {
                Text(
                    text = "Export CSV Options"
                )
            },
            text = {
                Column(
                    modifier = Modifier
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
                                selected = index == selectedIndex,
                                onClick = { selectAll(index == 0) },
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
                                value = currentMaxString,
                                onValueChange = {
                                    if (it.matches(allowedMax)) {
                                        currentMaxString = it
                                        exportRating =
                                            exportRating.copy(maxRating = it.toIntOrNull() ?: 5)
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
                                selectedValue = currentRoundingString,
                                onValueChange = {
                                    currentRoundingString = it
                                    exportRating = exportRating.copy(rounding = it.toIntOrNull() ?: 2)
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
                TextButton(onClick = { showExportCsv(false) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        preferencesRepo.saveExportRating(exportRating.maxRating, exportRating.rounding)
                    }
                    when (exportType) {
                        ExportType.ITEMS -> exportCsvLauncher.launch(exportCsvIntent)
                        ExportType.TINS -> exportAsTinsLauncher.launch(exportAsTinsIntent)
                        null -> { }
                    }
                    showExportCsv(false)
                }
                ) {
                    Text(
                        text = "Confirm"
                    )
                }
            }
        )
    }
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
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel,
) {
    val sheetState by filterViewModel.bottomSheetState.collectAsState()
    val sheetOpen = sheetState == BottomSheetState.OPENED
    val filteringApplied by filterViewModel.isFilterApplied.collectAsState()
    val searchPerformed by filterViewModel.searchPerformed.collectAsState()
    val datesExist by filterViewModel.datesExist.collectAsState()
    val databaseEmpty by filterViewModel.emptyDatabase.collectAsState()

    val tinsReady by filterViewModel.tinsReady.collectAsState()

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
                            FontWeight.Normal
                        },
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
                        onClick = {
                            filterViewModel.getPositionTrigger()
                            navigateToStats()
                        },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = !databaseEmpty
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
                                if (databaseEmpty) {
                                    LocalContentColor.current.copy(alpha = 0.5f)
                                } else LocalContentColor.current
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
                            FontWeight.Normal
                        },
                        color =
                        if (currentDestination == StatsDestination && !clickToAdd) {
                            onPrimaryLight
                        } else {
                            if (databaseEmpty) {
                                LocalContentColor.current.copy(alpha = 0.5f)
                            } else LocalContentColor.current
                        },
                    )
                }

                // Dates //
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val borderColor =
                        if (tinsReady) { // && !datesSeen
                            if (currentDestination == DatesDestination && !clickToAdd) {
                                onPrimaryLight }
                            else LocalContentColor.current }
                        else Color.Transparent
                    val indicatorColor =
                        if (tinsReady) { // && !datesSeen
                            LocalCustomColors.current.indicatorCircle }
                        else Color.Transparent

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = 13.dp, y = (-17).dp)
                            .clip(CircleShape)
                            .border(1.5.dp, borderColor, CircleShape)
                            .background(indicatorColor)
                    )
                    IconButton(
                        onClick = {
                                filterViewModel.getPositionTrigger()
                                navigateToDates()
                            },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = datesExist
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar_month),
                            contentDescription = stringResource(R.string.dates_title),
                            modifier = Modifier
                                .size(26.dp)
                                .offset(y = (-8).dp),
                            tint =
                                if (currentDestination == DatesDestination && !clickToAdd) {
                                    onPrimaryLight
                                } else {
                                    LocalContentColor.current
                                },
                        )
                    }

                    Text(
                        text = stringResource(R.string.dates_title),
                        modifier = Modifier
                            .padding(0.dp)
                            .offset(y = 13.dp),
                        fontSize = 11.sp,
                        fontWeight =
                            if (currentDestination == DatesDestination && !clickToAdd) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                        color =
                            if (currentDestination == DatesDestination && !clickToAdd) {
                                onPrimaryLight
                            } else {
                                if (datesExist) {
                                LocalContentColor.current} else {
                                    LocalContentColor.current.copy(alpha = 0.5f)
                                }
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
                    val borderColor =
                        if (filteringApplied) {
                            if (searchPerformed && currentDestination == HomeDestination) {
                                LocalCustomColors.current.indicatorBorderCorrection
                            } else {
                                if (sheetOpen) {
                                    onPrimaryLight
                                } else LocalContentColor.current
                            }
                        } else { Color.Transparent }

                    val indicatorColor =
                        if (filteringApplied) {
                            if (searchPerformed && currentDestination == HomeDestination) {
                                LocalCustomColors.current.indicatorCircle.copy(alpha = 0.5f)
                            } else LocalCustomColors.current.indicatorCircle
                        } else Color.Transparent

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = 13.dp, y = (-17).dp)
                            .clip(CircleShape)
                            .border(1.5.dp, borderColor, CircleShape)
                            .background(indicatorColor)
                    )
                    IconButton(
                        onClick = { filterViewModel.openBottomSheet() },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = if (currentDestination == HomeDestination && !databaseEmpty) !searchPerformed else !databaseEmpty
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter_24),
                            contentDescription = stringResource(R.string.filter_items),
                            modifier = Modifier
                                .size(26.dp)
                                .offset(y = (-8).dp),
                            tint = if (sheetOpen) {
                                onPrimaryLight
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }

                    Text(
                        text = stringResource(R.string.filter_items),
                        modifier = Modifier
                            .offset(y = 13.dp),
                        fontSize = 11.sp,
                        fontWeight =
                        if (sheetOpen) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        },
                        color = if (sheetOpen) {
                            onPrimaryLight
                        } else {
                            if ((searchPerformed && currentDestination == HomeDestination) || databaseEmpty) {
                                LocalContentColor.current.copy(alpha = 0.5f)
                            } else LocalContentColor.current
                        }
                    )
                }

                // Add //
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = {
                            clickToAdd = true
                            filterViewModel.getPositionTrigger()
                            navigateToAddEntry()
                        },
                        modifier = Modifier
                            .padding(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_circle),
                            contentDescription = stringResource(R.string.add),
                            modifier = Modifier
                                .size(26.dp)
                                .offset(y = (-8).dp),
                            tint =
                            if (clickToAdd) {
                                onPrimaryLight
                            } else {
                                LocalContentColor.current
                            },
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
                        } else {
                            LocalContentColor.current
                        }
                    )
                }
            }
        }
    }
}


/** Filter sheet stuff **/
@Composable
fun FilterBottomSheet(
    filterViewModel: FilterViewModel,
    closeSheet: () -> Unit,
    pagerState: PagerState,
    filtersApplied: Boolean,
    brandsData: BrandsSectionData,
    typeData: FilterSectionData,
    otherData: OtherSectionData,
    favDisExist: Boolean,
    subgenreData: FilterSectionData,
    cutData: FilterSectionData,
    componentData: FilterSectionData,
    flavoringData: FilterSectionData,
    containerData: FilterSectionData,
    tinsFilterData: TinsFilterData,
    updateSelectedTins: (String, Boolean) -> Unit,
    hasContainer: Boolean,
    tins: Boolean,
    productionData: ProductionSectionData,
    modifier: Modifier = Modifier,
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
        // Header //
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp), // top
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
                    .weight(1f)
                    .padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column (
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

        // Pager //
        var innerScrolling by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .pointerInput(pagerState) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            totalDrag += amount
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
                .fillMaxWidth(),
            userScrollEnabled = !innerScrolling,
            beyondViewportPageCount = 2,
            verticalAlignment = Alignment.Top,
        ) {
            when (it) {
                // brand, type, rating, stock filters //
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BrandFilterSection(
                            brandsData = brandsData,
                            brandSwitch = { filterViewModel.updateSelectedExcludeBrandsSwitch(it) },
                            reorderChips = { list, map -> filterViewModel.reorderChips(list, map) },
                            updateSelectedBrands = { string, boolean -> filterViewModel.updateSelectedBrands(string, boolean) },
                            updateSelectedExcludedBrands = { string, boolean -> filterViewModel.updateSelectedExcludedBrands(string, boolean) },
                            clearAll = { filterViewModel.clearAllSelectedBrands() },
                            modifier = Modifier
                                .padding(6.dp),
                            innerScrolling = { innerScrolling = it }
                        )
                        TypeFilterSection(
                            typeData = typeData,
                            updateSelectedTypes = { string, boolean -> filterViewModel.updateSelectedTypes(string, boolean) },
                            modifier = Modifier
                                .padding(start = 6.dp, end = 6.dp, top = 0.dp, bottom = 6.dp),
                        )
                        OtherFiltersSection(
                            otherData = otherData,
                            favDisExist = favDisExist,
                            updateSelectedFavorites = { filterViewModel.updateSelectedFavorites(it) },
                            updateSelectedExcludeFavorites = { filterViewModel.updateSelectedExcludeFavorites(it) },
                            updateSelectedDislikeds = { filterViewModel.updateSelectedDislikeds(it) },
                            updateSelectedExcludeDislikeds = { filterViewModel.updateSelectedExcludeDislikeds(it) },
                            updateSelectedInStock = { filterViewModel.updateSelectedInStock(it) },
                            updateSelectedOutOfStock = { filterViewModel.updateSelectedOutOfStock(it) },
                            updateSelectedUnrated = { filterViewModel.updateSelectedUnrated(it) },
                            updateSelectedRatingRange = { (min, max) -> filterViewModel.updateSelectedRatingRange(min, max) },
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp),
                        )
                    }
                }

                // subgenre, cuts, components, flavoring filters //
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FlowFilterSection(
                            label = "Subgenre",
                            nothingLabel = "No subgenres assigned to any blends.",
                            displayData = subgenreData,
                            updateSelectedOptions = { string, boolean -> filterViewModel.updateSelectedSubgenre(string, boolean) },
                            overflowCheck = { selected, avail, int ->
                                filterViewModel.overflowCheck(selected, avail, int) },
                            noneField = "(Unassigned)",
                            clearAll = { filterViewModel.clearAllSelected("Subgenre") },
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = LocalCustomColors.current.sheetBoxBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    LocalCustomColors.current.sheetBox,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )

                        FlowFilterSection(
                            label = "Cut",
                            nothingLabel = "No cuts assigned to any blends.",
                            displayData = cutData,
                            updateSelectedOptions = { string, boolean -> filterViewModel.updateSelectedCut(string, boolean) },
                            overflowCheck = { selected, avail, int ->
                                filterViewModel.overflowCheck(selected, avail, int) },
                            noneField = "(Unassigned)",
                            clearAll = { filterViewModel.clearAllSelected("Cut") },
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = LocalCustomColors.current.sheetBoxBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    LocalCustomColors.current.sheetBox,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                        )

                        FlowFilterSection(
                            label = "Components",
                            nothingLabel = "No components assigned to any blends.",
                            displayData = componentData,
                            updateSelectedOptions = { string, boolean -> filterViewModel.updateSelectedComponent(string, boolean) },
                            overflowCheck = { selected, avail, int ->
                                filterViewModel.overflowCheck(selected, avail, int) },
                            noneField = "(None Assigned)",
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = LocalCustomColors.current.sheetBoxBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    LocalCustomColors.current.sheetBox,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            clearAll = { filterViewModel.clearAllSelected("Components") },
                            onMatchOptionChange = { filterViewModel.updateCompMatching(it) },
                        )

                        FlowFilterSection(
                            label = "Flavorings",
                            nothingLabel = "No flavorings assigned to any blends.",
                            displayData = flavoringData,
                            updateSelectedOptions = { string, boolean -> filterViewModel.updateSelectedFlavoring(string, boolean) },
                            overflowCheck = { selected, avail, int ->
                                filterViewModel.overflowCheck(selected, avail, int) },
                            noneField = "(None Assigned)",
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = LocalCustomColors.current.sheetBoxBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    LocalCustomColors.current.sheetBox,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            clearAll = { filterViewModel.clearAllSelected("Flavorings") },
                            onMatchOptionChange = { filterViewModel.updateFlavorMatching(it) },
                        )
                    }
                }

                // tin filtering, containers, production //
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(383.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TinsFilterSection(
                            tinsFilterData = tinsFilterData,
                            hasContainer = hasContainer,
                            tins = tins,
                            updateSelectedTins = { string, boolean -> updateSelectedTins(string, boolean) },
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                        )
                        FlowFilterSection(
                            label = "Tin Containers",
                            nothingLabel = if (tins) "No containers assigned to any tins." else "No tins assigned to any blends.",
                            displayData = containerData,
                            updateSelectedOptions = { string, boolean ->
                                filterViewModel.updateSelectedContainer(string, boolean)
                            },
                            overflowCheck = { selected, avail, int ->
                                filterViewModel.overflowCheck(selected, avail, int) },
                            noneField = "(Unassigned)",
                            clearAll = { filterViewModel.clearAllSelected("Container") },
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = LocalCustomColors.current.sheetBoxBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    LocalCustomColors.current.sheetBox,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                        )

                        // Production
                        Row(
                            modifier = Modifier
                                .border(
                                    width = Dp.Hairline,
                                    color = LocalCustomColors.current.sheetBoxBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    LocalCustomColors.current.sheetBox,
                                    RoundedCornerShape(8.dp)
                                )
                                .width(intrinsicSize = IntrinsicSize.Max),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CheckboxWithLabel(
                                text = "In Production",
                                checked = productionData.production,
                                onCheckedChange = { filterViewModel.updateSelectedProduction(it) },
                                modifier = Modifier,
                                enabled = productionData.productionEnabled
                            )
                            CheckboxWithLabel(
                                text = "Discontinued",
                                checked = productionData.outOfProduction,
                                onCheckedChange = { filterViewModel.updateSelectedOutOfProduction(it) },
                                modifier = Modifier,
                                enabled = productionData.outOfProductionEnabled
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }

        TextButton(
            onClick = { filterViewModel.resetFilter() },
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(top = 6.dp),
            enabled = filtersApplied,
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
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}


@Composable
fun BrandFilterSection(
    brandsData: BrandsSectionData,
    brandSwitch: (Boolean) -> Unit,
    reorderChips: (List<String>, Map<String, Boolean>) -> List<String>,
    updateSelectedBrands: (String, Boolean) -> Unit,
    updateSelectedExcludedBrands: (String, Boolean) -> Unit,
    clearAll: () -> Unit,
    innerScrolling: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBrands by remember(brandsData) { mutableStateOf(brandsData.selectedBrands) }
    val selectedExcludedBrands by remember(brandsData) { mutableStateOf(brandsData.excludeBrands) }
    val excluded by remember(brandsData) { mutableStateOf(brandsData.excludeBrandSwitch) }
    val allBrands by remember(brandsData) { mutableStateOf(brandsData.allBrands) }
    val includeEnabled by remember(brandsData) { mutableStateOf(brandsData.includeBrandsEnabled) }
    val excludeEnabled by remember(brandsData) { mutableStateOf(brandsData.excludeBrandsEnabled) }

    val brandEnabled = if (excluded) excludeEnabled else includeEnabled

    var brandSearchText by remember { mutableStateOf("") }
    var filteredBrands by remember { mutableStateOf(allBrands) }
    var showOverflowPopup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        // Search bar and brand include/exclude button //
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

                        val startsWith = allBrands.filter { brand ->
                            brand.startsWith(text, ignoreCase = true)
                        }
                        val otherWordsStartsWith = allBrands.filter { brand ->
                            brand.split(" ").drop(1).any { word ->
                                word.startsWith(text, ignoreCase = true)
                            } && !brand.startsWith(text, ignoreCase = true)
                        }
                        val contains = allBrands.filter { brand ->
                            brand.contains(text, ignoreCase = true)
                                    && !brand.startsWith(text, ignoreCase = true) &&
                                    !otherWordsStartsWith.contains(brand)
                        }
                        startsWith + otherWordsStartsWith + contains
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
            // Brand include/exclude button
            Column(
                modifier = Modifier
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
                        onClick = {
                            brandSwitch(!excluded)
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
                        .offset(y = 3.dp),
                    color = if (!excluded) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.5f
                    ),
                    fontWeight = if (excluded) FontWeight.Normal else FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Exclude",
                    modifier = Modifier
                        .padding(0.dp)
                        .offset(y = (-3).dp),
                    color = if (excluded) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.5f
                    ),
                    fontWeight = if (excluded) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }

        // Selectable brands row //
        val lazyListState = rememberLazyListState()
        val preSortUnselected = filteredBrands.filterNot {
            if (!excluded) { selectedBrands.contains(it) }
            else { selectedExcludedBrands.contains(it) }
        }
        val unselectedBrands = reorderChips(preSortUnselected, brandEnabled)

        GlowBox(
            color = GlowColor(MaterialTheme.colorScheme.background),
            size = GlowSize(horizontal = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
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
                items(unselectedBrands.size, key = { index -> unselectedBrands[index] }) { index ->
                    val brand = unselectedBrands[index]
                    TextButton(
                        onClick = {
                            if (excluded) {
                                updateSelectedExcludedBrands(brand, true)
                            } else {
                                updateSelectedBrands(brand, true)
                            }
                            brandSearchText = ""
                            filteredBrands = allBrands
                        },
                        modifier = Modifier
                            .wrapContentSize(),
                        enabled = brandEnabled[brand] ?: false,
                    ) {
                        Text(
                            text = brand,
                            modifier = Modifier
                                .wrapContentSize()
                        )
                    }
                }
            }

            LaunchedEffect(filteredBrands) { lazyListState.scrollToItem(0) }
            LaunchedEffect(brandEnabled) { lazyListState.scrollToItem(0) }
        }

        // Selected brands chip box //
        var chipBoxWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current
        Box (
            modifier = Modifier
                .onGloballyPositioned { chipBoxWidth = with(density) { it.size.width.toDp() } }
        ) {
            val maxWidth = (chipBoxWidth * 0.32f) - 4.dp
            val chipCountToShow = 5
            val overflowCount =
                if (excluded) { selectedExcludedBrands.size - chipCountToShow }
                else { selectedBrands.size - chipCountToShow }
            val chips = if (excluded) selectedExcludedBrands else selectedBrands

            Column(
                modifier = Modifier
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
                                width = Dp.Hairline,
                                color = LocalCustomColors.current.sheetBoxBorder.copy(alpha = .8f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                LocalCustomColors.current.sheetBox,
                                RoundedCornerShape(8.dp)
                            )
                            .height(96.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                    ) {
                        chips.take(chipCountToShow).forEach { brand ->
                            Chip(
                                text = brand,
                                onChipClicked = { },
                                onChipRemoved = {
                                    if (excluded) {
                                        updateSelectedExcludedBrands(brand, false)
                                    } else {
                                        updateSelectedBrands(brand, false)
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
                        modifier = Modifier,
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
                                        items(chips) { brand ->
                                            Chip(
                                                text = brand,
                                                onChipClicked = { },
                                                onChipRemoved = {
                                                    if (excluded) {
                                                        updateSelectedExcludedBrands(brand, false)
                                                    } else {
                                                        updateSelectedBrands(brand, false)
                                                    }
                                                },
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
                                            clearAll()
                                            showOverflowPopup = false
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
                            Button(onClick = { showOverflowPopup = false }) {
                                Text("Close")
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        textContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}


@Composable
fun TypeFilterSection(
    typeData: FilterSectionData,
    updateSelectedTypes: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedTypes = typeData.selected
    val availableTypes = typeData.available
    val enabledTypes = typeData.enabled
    val nothingAssigned = !typeData.available.any { it != "(Unassigned)"}

    Column(
        modifier = modifier
            .height(96.dp)
    ) {
        if (nothingAssigned) {
            Row(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .fillMaxSize(),
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
                    .height(96.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 6.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                availableTypes.forEach {
                    FilterChip(
                        selected = selectedTypes.contains(it),
                        onClick = { updateSelectedTypes(it, !selectedTypes.contains(it)) },
                        label = { Text(it, fontSize = 14.sp) },
                        modifier = Modifier
                            .padding(0.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        enabled = enabledTypes[it] ?: false
                    )
                }
            }
        }
    }
}

@Composable
fun OtherFiltersSection(
    otherData: OtherSectionData,
    favDisExist: Boolean,
    updateSelectedFavorites: (Boolean) -> Unit,
    updateSelectedExcludeFavorites: (Boolean) -> Unit,
    updateSelectedDislikeds: (Boolean) -> Unit,
    updateSelectedExcludeDislikeds: (Boolean) -> Unit,
    updateSelectedInStock: (Boolean) -> Unit,
    updateSelectedOutOfStock: (Boolean) -> Unit,
    updateSelectedUnrated: (Boolean) -> Unit,
    updateSelectedRatingRange: (Pair<Double?, Double?>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favorites by remember(otherData) { mutableStateOf(otherData.favorites) }
    val excludeFavorites by remember(otherData) { mutableStateOf(otherData.excludeFavorites) }
    val favoritesSelection =
        if (favorites) ToggleableState.On
        else if (excludeFavorites) ToggleableState.Indeterminate
        else ToggleableState.Off

    val favoritesEnabled by remember(otherData) { mutableStateOf(otherData.favoritesEnabled) }
    val favoritesExcludeEnabled by remember(otherData) { mutableStateOf(otherData.excludeFavoritesEnabled) }
    val favoritesEnabledTristate = when (favoritesSelection) {
        ToggleableState.Off -> favoritesEnabled || favoritesExcludeEnabled
        ToggleableState.On -> true
        ToggleableState.Indeterminate -> true
    }

    val dislikeds by remember(otherData) { mutableStateOf(otherData.dislikeds) }
    val excludeDislikeds by remember(otherData) { mutableStateOf(otherData.excludeDislikeds) }
    val dislikedsSelection =
        if (dislikeds) ToggleableState.On
        else if (excludeDislikeds) ToggleableState.Indeterminate
        else ToggleableState.Off

    val dislikedsEnabled by remember(otherData) { mutableStateOf(otherData.dislikedsEnabled) }
    val dislikedsExcludeEnabled by remember(otherData) { mutableStateOf(otherData.excludeDislikedsEnabled) }
    val dislikedsEnabledTristate = when (dislikedsSelection) {
        ToggleableState.Off -> dislikedsEnabled || dislikedsExcludeEnabled
        ToggleableState.On -> true
        ToggleableState.Indeterminate -> true
    }

    var showRatingPop by rememberSaveable { mutableStateOf(false) }

    val inStock by remember(otherData) { mutableStateOf(otherData.inStock) }
    val inStockEnabled by remember(otherData) { mutableStateOf(otherData.inStockEnabled) }
    val outOfStock by remember(otherData) { mutableStateOf(otherData.outOfStock) }
    val outOfStockEnabled by remember(otherData) { mutableStateOf(otherData.outOfStockEnabled) }

    // Overall ratings and stock filters //
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
                        width = Dp.Hairline,
                        color = LocalCustomColors.current.sheetBoxBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        LocalCustomColors.current.sheetBox,
                        RoundedCornerShape(8.dp)
                    )
                    .width(intrinsicSize = IntrinsicSize.Max)
                    .padding(vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Fav/Dis //
                Box {
                    Row {
                        TriStateCheckWithLabel(
                            text = "Favorites",
                            state = favoritesSelection,
                            onClick = {
                                when (favoritesSelection) {
                                    ToggleableState.Off -> {
                                        if (favoritesEnabled) updateSelectedFavorites(true)
                                        else if (favoritesExcludeEnabled) updateSelectedExcludeFavorites(true)
                                        else {
                                            updateSelectedFavorites(false)
                                            updateSelectedExcludeFavorites(false)
                                        }
                                    }
                                    ToggleableState.On ->
                                        if (favoritesExcludeEnabled) updateSelectedExcludeFavorites(true)
                                        else updateSelectedFavorites(false)
                                    ToggleableState.Indeterminate -> {
                                        updateSelectedFavorites(false)
                                        updateSelectedExcludeFavorites(false)
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                    if (favorites) MaterialTheme.colorScheme.primary
                                    else if (excludeFavorites) MaterialTheme.colorScheme.error
                                    else Color.Transparent,
                            ),
                            enabled = favoritesEnabledTristate && favDisExist,
                            maxLines = 1
                        )

                        TriStateCheckWithLabel(
                            text = "Dislikes",
                            state = dislikedsSelection,
                            onClick = {
                                when (dislikedsSelection) {
                                    ToggleableState.Off -> {
                                        if (dislikedsEnabled) updateSelectedDislikeds(true)
                                        else if (dislikedsExcludeEnabled) updateSelectedExcludeDislikeds(true)
                                        else {
                                            updateSelectedDislikeds(false)
                                            updateSelectedExcludeDislikeds(false)
                                        }
                                    }
                                    ToggleableState.On -> {
                                        if (dislikedsExcludeEnabled) updateSelectedExcludeDislikeds(true)
                                        else {
                                            updateSelectedDislikeds(false)
                                            updateSelectedExcludeDislikeds(true)
                                        }
                                    }
                                    ToggleableState.Indeterminate -> {
                                        updateSelectedDislikeds(false)
                                        updateSelectedExcludeDislikeds(false)
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                    if (dislikeds) MaterialTheme.colorScheme.primary
                                    else if (excludeDislikeds) MaterialTheme.colorScheme.error
                                    else Color.Transparent,
                            ),
                            enabled = dislikedsEnabledTristate && favDisExist,
                            maxLines = 1
                        )
                    }
                    if (!favDisExist && otherData.ratingsExist) {
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

                // Star Rating //
                Box {
                    val rangeEnabled = otherData.ratingLowEnabled != null && otherData.ratingHighEnabled != null
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .width(229.dp)
                            .padding(horizontal = 12.dp)
                            .alpha(if (!rangeEnabled) .38f else 1f),
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
                                .clickable(
                                    indication = null,
                                    interactionSource = null,
                                    enabled = rangeEnabled,
                                ) { showRatingPop = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
                        ) {
                            val low = otherData.ratingLow
                            val high = otherData.ratingHigh
                            val unchosen = low == null && high == null
                            val emptyColor = if (unchosen || (low != null && high == null)) LocalCustomColors.current.starRating else LocalContentColor.current
                            val emptyAlpha = if (unchosen || (low != null && high == null)) 1f else .38f
                            val lowTextAlpha = if (unchosen || low == null) .7f else 1f
                            val highTextAlpha = if (unchosen || high == null) .7f else 1f
                            Box (contentAlignment = Alignment.CenterEnd) {
                                Text(
                                    text = "4.5",
                                    modifier = Modifier,
                                    fontSize = 13.sp,
                                    color = Color.Transparent
                                )
                                Text(
                                    text = formatDecimal(otherData.ratingLow, 1).ifBlank { "0" },
                                    modifier = Modifier,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = LocalContentColor.current.copy(alpha = lowTextAlpha)
                                )
                            }
                            RatingRow(
                                range = Pair(otherData.ratingLow, otherData.ratingHigh),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp),
                                starSize = 18.dp,
                                showDivider = true,
                                minColor = LocalContentColor.current,
                                minAlpha = .38f,
                                emptyColor = emptyColor,
                                emptyAlpha = emptyAlpha
                            )
                            Box (contentAlignment = Alignment.CenterStart) {
                                Text(
                                    text = "4.5",
                                    modifier = Modifier,
                                    fontSize = 13.sp,
                                    color = Color.Transparent
                                )
                                Text(
                                    text = formatDecimal(otherData.ratingHigh, 1).ifBlank { "5" },
                                    modifier = Modifier,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = LocalContentColor.current.copy(alpha = highTextAlpha)
                                )
                            }
                        }
                    }
                    if (favDisExist && !otherData.ratingsExist) {
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
            }
            if (!favDisExist && !otherData.ratingsExist) {
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

        if (showRatingPop) {
            RatingRangePop(
                unrated = otherData.unrated,
                unratedEnabled = otherData.unratedEnabled,
                selectedRange = Pair(otherData.ratingLow, otherData.ratingHigh),
                ratingRangeEnabled = Pair(otherData.ratingLowEnabled, otherData.ratingHighEnabled),
                updateSelectedUnrated = updateSelectedUnrated,
                updateSelectedRatingRange = { updateSelectedRatingRange(it) },
                onDismiss = { showRatingPop = false },
                modifier = Modifier,
            )
        }

        // In Stock
        Column(
            modifier = Modifier
                .background(
                    LocalCustomColors.current.sheetBox,
                    RoundedCornerShape(8.dp)
                )
                .border(
                    width = Dp.Hairline,
                    color = LocalCustomColors.current.sheetBoxBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .width(intrinsicSize = IntrinsicSize.Max)
                .padding(vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            CheckboxWithLabel(
                text = "In-stock",
                checked = inStock,
                onCheckedChange = { updateSelectedInStock(it) },
                modifier = Modifier,
                enabled = inStockEnabled
            )
            CheckboxWithLabel(
                text = "Out",
                checked = outOfStock,
                onCheckedChange = { updateSelectedOutOfStock(it) },
                modifier = Modifier,
                enabled = outOfStockEnabled
            )
        }
    }
}

@Composable
fun FlowFilterSection(
    label: String,
    nothingLabel: String,
    displayData: FilterSectionData,
    updateSelectedOptions: (String, Boolean) -> Unit,
    overflowCheck: (List<String>, List<String>, Int) -> Boolean,
    noneField: String,
    modifier: Modifier = Modifier,
    clearAll: () -> Unit = {},
    onMatchOptionChange: (String) -> Unit = {},
) {
    var showOverflowPopup by remember { mutableStateOf(false) }
    val enableMatchOption = !displayData.matching.isNullOrBlank()
    val nothingAssigned = !displayData.available.any { it != noneField }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {

        // Header and Match options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier,
                color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.4f) else LocalContentColor.current
            )
            if (enableMatchOption) {
                Row(
                    modifier = Modifier
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Match: ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier,
                        color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.4f) else LocalContentColor.current
                    )
                    Box(
                        modifier = Modifier
                            .padding(0.dp)
                            .clickable(
                                enabled = !nothingAssigned,
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { onMatchOptionChange("Any") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Any",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Transparent,
                        )
                        Text(
                            text = "Any",
                            fontSize = 14.sp,
                            fontWeight = if (displayData.matching == "Any" && !nothingAssigned) FontWeight.Medium else FontWeight.Normal,
                            color = if (displayData.matching == "Any" && !nothingAssigned) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = .6f),
                            modifier = Modifier
                        )
                    }
                    Text(
                        text = " / ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier,
                        color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
                    )
                    Box(
                        modifier = Modifier
                            .padding(0.dp)
                            .clickable(
                                enabled = !nothingAssigned,
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { onMatchOptionChange("All") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Transparent,
                        )
                        Text(
                            text = "All",
                            fontSize = 14.sp,
                            fontWeight = if (displayData.matching == "All") FontWeight.Medium else FontWeight.Normal,
                            color = if (displayData.matching == "All") MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                                alpha = .6f
                            ),
                            modifier = Modifier
                        )
                    }
                    Text(
                        text = " / ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier,
                        color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
                    )
                    Box(
                        modifier = Modifier
                            .padding(0.dp)
                            .clickable(
                                enabled = !nothingAssigned,
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { onMatchOptionChange("Only") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Only",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Transparent,
                        )
                        Text(
                            text = "Only",
                            fontSize = 14.sp,
                            fontWeight = if (displayData.matching == "Only") FontWeight.Medium else FontWeight.Normal,
                            color = if (displayData.matching == "Only") MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                                alpha = .6f
                            ),
                            modifier = Modifier
                        )
                    }
                }
            }
        }

        if (nothingAssigned) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nothingLabel,
                    modifier = Modifier
                        .padding(0.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        } else {
            OverflowRow(
                itemCount = displayData.available.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top)
                    .padding(horizontal = 4.dp),
                itemSpacing = 6.dp,
                itemContent = {
                    val option = displayData.available[it]

                    FilterChip(
                        selected = displayData.selected.contains(option),
                        onClick = { updateSelectedOptions(option, !displayData.selected.contains(option)) },
                        label = { Text(text = option, fontSize = 14.sp) },
                        modifier = Modifier
                            .padding(0.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        enabled = displayData.enabled[option] ?: false,
                    )
                },
                enabledAtIndex = { displayData.enabled[displayData.available[it]] ?: true },
                overflowIndicator = { overflowCount, enabledCount, overflowEnabled -> // overflowEnabled means count > 0
                    val overflowedSelected = overflowCheck(displayData.selected, displayData.available, displayData.available.size - overflowCount)

                    val labelColor =
                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.onSecondaryContainer
                        else if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else if (!overflowEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    val containerColor =
                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
                        else if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer
                        else if (!overflowEnabled) MaterialTheme.colorScheme.background
                        else MaterialTheme.colorScheme.background
                    val borderColor =
                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
                        else if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer
                        else if (!overflowEnabled) MaterialTheme.colorScheme.outlineVariant
                        else MaterialTheme.colorScheme.outlineVariant


                    Chip(
                        text = "+$enabledCount",  // "+$overflowCount"
                        onChipClicked = { showOverflowPopup = true },
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

            if (showOverflowPopup) {
                AlertDialog(
                    onDismissRequest = { showOverflowPopup = false },
                    title = {
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier
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
                                .padding(0.dp)
                                .heightIn(min = 0.dp, max = 280.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top)
                        ) {
                            GlowBox(
                                color = GlowColor(MaterialTheme.colorScheme.background),
                                size = GlowSize(vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, false),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    // match options
                                    if (enableMatchOption) {
                                        Row(
                                            modifier = Modifier
                                                .padding(bottom = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Match: ",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                            )
                                            // Any
                                            Box(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .clickable(
                                                        indication = LocalIndication.current,
                                                        interactionSource = null
                                                    ) { onMatchOptionChange("Any") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Any",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Transparent,
                                                )
                                                Text(
                                                    text = "Any",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (displayData.matching == "Any") FontWeight.Medium else FontWeight.Normal,
                                                    color = if (displayData.matching == "Any") MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                                                        alpha = .7f
                                                    ),
                                                    modifier = Modifier
                                                )
                                            }
                                            Text(
                                                text = " / ",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                modifier = Modifier
                                            )
                                            // All
                                            Box(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .clickable(
                                                        indication = LocalIndication.current,
                                                        interactionSource = null
                                                    ) { onMatchOptionChange("All") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "All",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Transparent,
                                                )
                                                Text(
                                                    text = "All",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (displayData.matching == "All") FontWeight.Medium else FontWeight.Normal,
                                                    color = if (displayData.matching == "All") MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                                                        alpha = .7f
                                                    ),
                                                    modifier = Modifier
                                                )
                                            }
                                            Text(
                                                text = " / ",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                modifier = Modifier
                                            )
                                            // Only
                                            Box(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .clickable(
                                                        indication = LocalIndication.current,
                                                        interactionSource = null
                                                    ) { onMatchOptionChange("Only") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Only",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Transparent,
                                                )
                                                Text(
                                                    text = "Only",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (displayData.matching == "Only") FontWeight.Medium else FontWeight.Normal,
                                                    color = if (displayData.matching == "Only") MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                                                        alpha = .7f
                                                    ),
                                                    modifier = Modifier
                                                )
                                            }
                                        }
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
                                        displayData.available.forEach {
                                            FilterChip(
                                                selected = displayData.selected.contains(it),
                                                onClick = {
                                                    updateSelectedOptions(
                                                        it, !displayData.selected.contains(it)
                                                    )
                                                },
                                                label = { Text(text = it, fontSize = 14.sp) },
                                                modifier = Modifier
                                                    .padding(0.dp),
                                                shape = MaterialTheme.shapes.small,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = MaterialTheme.colorScheme.background
                                                ),
                                                enabled = displayData.enabled[it] ?: false
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
                                        showOverflowPopup = false
                                    },
                                    modifier = Modifier
                                        .offset(x = (-4).dp),
                                    enabled = displayData.selected.isNotEmpty()
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
                        Button(onClick = { showOverflowPopup = false }) {
                            Text("Close")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    textContentColor = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun TinsFilterSection(
    tinsFilterData: TinsFilterData,
    hasContainer: Boolean,
    tins: Boolean,
    updateSelectedTins: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
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
                val implicitHas = !tinsFilterData.hasTins && (tinsFilterData.opened || tinsFilterData.unopened || tinsFilterData.finished || tinsFilterData.unfinished || hasContainer)

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
                            checked = tinsFilterData.hasTins,
                            onCheckedChange = { updateSelectedTins("has", it) },
                            modifier = Modifier,
                            enabled = tins && tinsFilterData.hasEnabled,
                            fontColor = if (!tins) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        )
                    }
                    CheckboxWithLabel(
                        text = "No tins",
                        checked = tinsFilterData.noTins,
                        onCheckedChange = { updateSelectedTins("no", it) },
                        modifier = Modifier,
                        enabled = tins && tinsFilterData.noEnabled,
                        fontColor = if (!tins) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
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
                        checked = tinsFilterData.opened,
                        onCheckedChange = { updateSelectedTins("opened", it) },
                        modifier = Modifier,
                        enabled = tins && tinsFilterData.openedEnabled,
                        fontColor = if (!tins) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                    CheckboxWithLabel(
                        text = "Unopened",
                        checked = tinsFilterData.unopened,
                        onCheckedChange = { updateSelectedTins("unopened", it) },
                        modifier = Modifier,
                        enabled = tins && tinsFilterData.unopenedEnabled,
                        fontColor = if (!tins) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
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
                        checked = tinsFilterData.finished,
                        onCheckedChange = { updateSelectedTins("finished", it) },
                        modifier = Modifier,
                        enabled = tins && tinsFilterData.finishedEnabled,
                        fontColor = if (!tins) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                    CheckboxWithLabel(
                        text = "Unfinished",
                        checked = tinsFilterData.unfinished,
                        onCheckedChange = { updateSelectedTins("unfinished", it) },
                        modifier = Modifier,
                        enabled = tins && tinsFilterData.unfinishedEnabled,
                        fontColor = if (!tins) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                }
            }
            if (!tins) {
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


/** Custom composables for sheet **/
@Composable
fun RatingRangePop(
    unrated: Boolean,
    unratedEnabled: Boolean,
    selectedRange: Pair<Double?, Double?>,
    ratingRangeEnabled: Pair<Double?, Double?>,
    updateSelectedUnrated: (Boolean) -> Unit,
    updateSelectedRatingRange: (Pair<Double?, Double?>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var ratingLowString by rememberSaveable { mutableStateOf(formatDecimal(selectedRange.first)) }
    var selectedLow by rememberSaveable { mutableStateOf(selectedRange.first) }
    val minRating by rememberSaveable { mutableStateOf(ratingRangeEnabled.first) }

    var ratingHighString by rememberSaveable { mutableStateOf(formatDecimal(selectedRange.second)) }
    var selectedHigh by rememberSaveable { mutableStateOf(selectedRange.second) }
    val maxRating by rememberSaveable { mutableStateOf(ratingRangeEnabled.second) }

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
                    checked = unrated,
                    onCheckedChange = {
                        updateSelectedUnrated(it)
                        updateSelectedRatingRange(Pair(selectedLow, selectedHigh))
                                      },
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                    enabled = unratedEnabled,
                    fontColor = if (!unratedEnabled) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current,
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
                                    updateSelectedRatingRange(Pair(selectedLow, selectedHigh))
                                }
                            },
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
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
                                    updateSelectedRatingRange(Pair(selectedLow, selectedHigh))
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
                                updateSelectedRatingRange(Pair(selectedLow, selectedHigh))
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
                                updateSelectedRatingRange(Pair(null, selectedHigh))
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
                                updateSelectedRatingRange(Pair(selectedLow, null))
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
                            updateSelectedRatingRange(Pair(null, null))
                        },
                        enabled = ratingLowString.isNotBlank() || ratingHighString.isNotBlank() || unrated,
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
                    updateSelectedRatingRange(Pair(selectedLow, selectedHigh))
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
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(height)
            .offset(x = (-2).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .padding(0.dp),
            enabled = enabled,
            colors = colors,
            interactionSource = remember { MutableInteractionSource() }
        )
        Box(
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = LocalTextStyle.current.copy(
                    color = if (enabled) fontColor else fontColor.copy(alpha = 0.5f),
                    lineHeight = lineHeight
                ),
                modifier = Modifier,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = fontSize,
                    minFontSize = 9.sp,
                    stepSize = .2.sp
                )
            )
        }
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
    colors: CheckboxColors = CheckboxDefaults.colors()
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
                interactionSource = remember { MutableInteractionSource() }
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
private fun CustomFilterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
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