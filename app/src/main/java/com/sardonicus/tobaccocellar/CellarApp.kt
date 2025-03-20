@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("SameParameterValue")

package com.sardonicus.tobaccocellar

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
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.BottomSheetState
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.IndicatorSizes
import com.sardonicus.tobaccocellar.ui.composables.PagerIndicator
import com.sardonicus.tobaccocellar.ui.home.HomeDestination
import com.sardonicus.tobaccocellar.ui.navigation.CellarNavHost
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.stats.StatsDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.theme.onPrimaryLight
import com.sardonicus.tobaccocellar.ui.utilities.ExportCsvHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarApp(
    navController: NavHostController = rememberNavController()
) {
    CellarNavHost(navController = navController)

    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val bottomSheetState by filterViewModel.bottomSheetState.collectAsState()

    if (bottomSheetState == BottomSheetState.OPENED) {
        ModalBottomSheet(
            onDismissRequest = {
                filterViewModel.closeBottomSheet()
            },
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
    navigateToBulkEdit: () -> Unit = {},
    navigateToCsvImport: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToHelp: () -> Unit = {},
    exportCsvHandler: ExportCsvHandler? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel,
) {
    val coroutineScope = rememberCoroutineScope()

    var expanded by rememberSaveable { mutableStateOf(false) }
    var menuState by rememberSaveable { mutableStateOf(MenuState.MAIN) }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            exportCsvHandler?.onExportCsvClick(uri)
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
            exportCsvHandler?.onTinsExportCsvClick(uri)
        }
    }
    val exportAsTinsIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv"
        putExtra(Intent.EXTRA_TITLE, "tobacco_cellar_as_tins.csv")
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
                IconButton(
                    onClick = {
                        expanded = !expanded
                        filterViewModel.getPositionTrigger()
                    }
                ) {
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
                        when (menuState) {
                            MenuState.MAIN -> {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.bulk_edit_title)) },
                                    onClick = {
                                        expanded = false
                                        navigateToBulkEdit()
                                    },
                                    modifier = Modifier
                                        .padding(0.dp),
                                    enabled = currentDestination == HomeDestination,
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.import_csv)) },
                                    onClick = {
                                        expanded = false
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
                                                Icons.AutoMirrored.Default.KeyboardArrowRight,
                                                "Export Options",
                                            modifier = Modifier.size(20.dp),
                                            tint = LocalContentColor.current.copy(alpha = 0.75f))
                                        }
                                    },
                                    onClick = {
                                        menuState = MenuState.EXPORT_CSV
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
                                    text = { Text(text = stringResource(R.string.help_faq)) },
                                    onClick = {
                                        expanded = false
                                        navigateToHelp()
                                    },
                                    modifier = Modifier
                                        .padding(0.dp),
                                    enabled = true,
                                )

                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.settings)) },
                                    onClick = {
                                        expanded = false
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
                                            Icons.AutoMirrored.Default.KeyboardArrowLeft,
                                            "Back",
                                            modifier = Modifier.size(20.dp),
                                            tint = LocalContentColor.current.copy(alpha = 0.75f)
                                        )
                                    },
                                    onClick = { menuState = MenuState.MAIN }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Normal Export") },
                                    onClick = {
                                        expanded = false
                                        menuState = MenuState.MAIN
                                        exportCsvLauncher.launch(exportCsvIntent)
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
                                    text = { Text(text = "Export as Tins") },
                                    onClick = {
                                        expanded = false
                                        menuState = MenuState.MAIN
                                        exportAsTinsLauncher.launch(exportAsTinsIntent)
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
}

enum class MenuState {
    MAIN,
    EXPORT_CSV
}


@Composable
fun CellarBottomAppBar(
    currentDestination: NavigationDestination?,
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit = {},
    navigateToStats: () -> Unit = {},
    navigateToAddEntry: () -> Unit = {},
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel,
) {
    val sheetOpen by filterViewModel.bottomSheetState.collectAsState()

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
                            FontWeight.Normal
                        },
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
                            tint = if (sheetOpen == BottomSheetState.OPENED) {
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
                        if (sheetOpen == BottomSheetState.OPENED) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        },
                        color = if (sheetOpen == BottomSheetState.OPENED) {
                            onPrimaryLight
                        } else {
                            LocalContentColor.current
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
                            painter = painterResource(id = R.drawable.add),
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
    modifier: Modifier = Modifier,
) {
    val filtersApplied by filterViewModel.isFilterApplied.collectAsState()

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
                .padding(top = 6.dp, bottom = 6.dp),
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
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable { filterViewModel.closeBottomSheet() }
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }
        }

        // Pager //
        val pagerState = rememberPagerState(pageCount = { 2 })
        var innerScrolling by remember { mutableStateOf(false) }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .padding(0.dp),
                indicatorSize = IndicatorSizes(7.dp, 6.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth(),
            userScrollEnabled = !innerScrolling,
            beyondViewportPageCount = 1,
            verticalAlignment = Alignment.Top
        ) {
            when (it) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BrandFilterSection(
                            filterViewModel = filterViewModel,
                            modifier = Modifier
                                .padding(6.dp),
                            innerScrolling = { innerScrolling = it }
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
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GenreFilterSection(
                            filterViewModel = filterViewModel,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.7f
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        CutFilterSection(
                            filterViewModel = filterViewModel,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)

                        )
                        ComponentFilterSection(
                            filterViewModel = filterViewModel,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                                .border(
                                    width = Dp.Hairline,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        ProductionFilterSection(
                            filterViewModel = filterViewModel,
                            modifier = Modifier
                                .padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
                        )
                    }
                }
            }
        }

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
    val favorites by filterViewModel.sheetSelectedFavorites.collectAsState()
    val excludeFavorites by filterViewModel.sheetSelectedExcludeLikes.collectAsState()
    val dislikeds by filterViewModel.sheetSelectedDislikeds.collectAsState()
    val excludeDislikes by filterViewModel.sheetSelectedExcludeDislikes.collectAsState()
    val neutral by filterViewModel.sheetSelectedNeutral.collectAsState()
    val nonNeutral by filterViewModel.sheetSelectedNonNeutral.collectAsState()
    val inStock by filterViewModel.sheetSelectedInStock.collectAsState()
    val outOfStock by filterViewModel.sheetSelectedOutOfStock.collectAsState()

    // state for monitoring neutral/nonNeutral filters //
    val (nonNeutralState) = mutableStateOf(nonNeutral)
    val (neutralState) = mutableStateOf(neutral)
    val dualSelectionIndicatorState =
        if (nonNeutralState) ToggleableState.On
        else if (neutralState) ToggleableState.Indeterminate
        else ToggleableState.Off

    val (favoritesSelected) = mutableStateOf(favorites)
    val (favoritesExcluded) = mutableStateOf(excludeFavorites)
    val favoritesSelection =
        if (favoritesSelected) ToggleableState.On
        else if (favoritesExcluded) ToggleableState.Indeterminate
        else ToggleableState.Off

    val (dislikedsSelected) = mutableStateOf(dislikeds)
    val (dislikedsExcluded) = mutableStateOf(excludeDislikes)
    val dislikedsSelection =
        if (dislikedsSelected) ToggleableState.On
        else if (dislikedsExcluded) ToggleableState.Indeterminate
        else ToggleableState.Off


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
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
                            state = dualSelectionIndicatorState,
                            onClick = { },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                if (nonNeutralState) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else if (neutralState) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else Color.Transparent,
                                checkmarkColor =
                                if (nonNeutralState) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else if (neutralState) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else Color.Transparent
                            )
                        )
                        TriStateCheckWithLabel(
                            text = "Favorites",
                            state = favoritesSelection,
                            onClick = {
                                if (favoritesSelection == ToggleableState.Off) {
                                    filterViewModel.updateSelectedFavorites(true)
                                } else if (favoritesSelection == ToggleableState.On) {
                                    filterViewModel.updateSelectedExcludeLikes(true)
                                } else if (favoritesSelection == ToggleableState.Indeterminate) {
                                    filterViewModel.updateSelectedFavorites(false)
                                    filterViewModel.updateSelectedExcludeLikes(false)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                if (favoritesSelected) MaterialTheme.colorScheme.primary
                                else if (favoritesExcluded) MaterialTheme.colorScheme.error
                                else Color.Transparent,
                            )
                        )
                    }
                    CheckboxWithLabel(
                        text = "Rated",
                        checked = nonNeutral,
                        onCheckedChange = { filterViewModel.updateSelectedNonNeutral(it) },
                        modifier = Modifier
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
                            state = dualSelectionIndicatorState,
                            onClick = { },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                if (nonNeutralState) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else if (neutralState) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else Color.Transparent,
                                checkmarkColor =
                                if (nonNeutralState) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else if (neutralState) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else Color.Transparent
                            )
                        )
                        TriStateCheckWithLabel(
                            text = "Dislikes",
                            state = dislikedsSelection,
                            onClick = {
                                if (dislikedsSelection == ToggleableState.Off) {
                                    filterViewModel.updateSelectedDislikeds(true)
                                } else if (dislikedsSelection == ToggleableState.On) {
                                    filterViewModel.updateSelectedExcludeDislikes(true)
                                } else if (dislikedsSelection == ToggleableState.Indeterminate) {
                                    filterViewModel.updateSelectedDislikeds(false)
                                    filterViewModel.updateSelectedExcludeDislikes(false)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor =
                                if (dislikedsSelected) MaterialTheme.colorScheme.primary
                                else if (dislikedsExcluded) MaterialTheme.colorScheme.error
                                else Color.Transparent,
                            )
                        )
                    }
                    CheckboxWithLabel(
                        text = "Unrated",
                        checked = neutral,
                        onCheckedChange = { filterViewModel.updateSelectedNeutral(it) },
                        modifier = Modifier
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
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
                modifier = Modifier,
            )
            CheckboxWithLabel(
                text = "Out",
                checked = outOfStock,
                onCheckedChange = { filterViewModel.updateSelectedOutOfStock(it) },
                modifier = Modifier
            )
        }
    }
}

@Composable
fun ProductionFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val inProduction by filterViewModel.sheetSelectedProduction.collectAsState()
    val outOfProduction by filterViewModel.sheetSelectedOutOfProduction.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .width(intrinsicSize = IntrinsicSize.Max)
                .padding(vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                CheckboxWithLabel(
                    text = "In Production",
                    checked = inProduction,
                    onCheckedChange = { filterViewModel.updateSelectedProduction(it) },
                    modifier = Modifier,
                )
                CheckboxWithLabel(
                    text = "Out of Production",
                    checked = outOfProduction,
                    onCheckedChange = { filterViewModel.updateSelectedOutOfProduction(it) },
                    modifier = Modifier
                )
            }
        }
    }
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
        )
        Text(
            text = text,
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
            color = fontColor,
            fontSize = fontSize,
            lineHeight = lineHeight,
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
            )
        }
        Text(
            text = text,
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
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
    val selectedTypes by filterViewModel.sheetSelectedTypes.collectAsState()
    val selectedUnassigned by filterViewModel.sheetSelectedUnassigned.collectAsState()

    val availableTypes = listOf("Aromatic", "English", "Burley", "Virginia", "Other")

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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun BrandFilterSection(
    filterViewModel: FilterViewModel,
    innerScrolling: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBrands by filterViewModel.sheetSelectedBrands.collectAsState()
    val selectedExcludedBrands by filterViewModel.sheetSelectedExcludeBrands.collectAsState()
    val excluded by filterViewModel.sheetSelectedExcludeBrandSwitch.collectAsState()
    val allBrands by filterViewModel.availableBrands.collectAsState()

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
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp)
                    .combinedClickable(
                        onClick = {
                            filterViewModel.updateSelectedExcludeBrandsSwitch(isSelected = !excluded)
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
                val unselectedBrands = filteredBrands.filterNot {
                    if ((selectedExcludedBrands.isEmpty())) {
                        selectedBrands.contains(it)
                    } else {
                        selectedExcludedBrands.contains(it)
                    }
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

            LaunchedEffect(filteredBrands) {
                lazyListState.scrollToItem(0)
            }
        }

        // Selected brands chip box //
        BoxWithConstraints {
            val boxWithConstraintsScope = this
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
                                onChipClicked = { },
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
                                                        filterViewModel.updateSelectedExcludedBrands(brand, false)
                                                    } else {
                                                        filterViewModel.updateSelectedBrands(brand, false)
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
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        textContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun GenreFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {
        val availableGenres by filterViewModel.availableSubgenres.collectAsState()
        val selectedGenres by filterViewModel.sheetSelectedSubgenres.collectAsState()
        val nothingAssigned = !availableGenres.any { it != "(Unassigned)" }

        var showOverflowPopup by remember { mutableStateOf(false) }
        var overflowCount by remember { mutableIntStateOf(0) }
        var shownItems by remember { mutableIntStateOf(0) }

        Text(
            text = "Subgenres:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .align(Alignment.Start),
            color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
        )

        if (nothingAssigned) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No subgenres assigned to any blends.",
                    modifier = Modifier,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        } else {
            ContextualFlowRow(
                itemCount = availableGenres.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                maxLines = 1,
                overflow = ContextualFlowRowOverflow.expandIndicator {
                    shownItems = shownItemCount
                    overflowCount = availableGenres.size - shownItems

                    val overflowedSelected = filterViewModel.overflowCheck(
                        selectedGenres,
                        availableGenres,
                        shownItemCount
                    )

                    Chip(
                        text = "+$overflowCount",
                        onChipClicked = { showOverflowPopup = true },
                        onChipRemoved = { },
                        trailingIcon = false,
                        modifier = Modifier,
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer else
                                MaterialTheme.colorScheme.background,
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer else
                                MaterialTheme.colorScheme.outline
                        ),
                    )
                },
            ) {
                val genre = availableGenres[it]

                FilterChip(
                    selected = selectedGenres.contains(genre),
                    onClick = {
                        filterViewModel.updateSelectedSubgenre(
                            genre,
                            !selectedGenres.contains(genre)
                        )
                    },
                    label = {
                        Text(
                            text = genre,
                            fontSize = 14.sp,
                        )
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            }
            if (showOverflowPopup) {
                AlertDialog(
                    onDismissRequest = { showOverflowPopup = false },
                    title = {
                        Text(
                            text = "Subgenres",
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
                                .heightIn(min = 0.dp, max = 280.dp)
                                .padding(0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                2.dp,
                                Alignment.CenterVertically
                            )
                        ) {
                            GlowBox(
                                color = GlowColor(MaterialTheme.colorScheme.background),
                                size = GlowSize(vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 0.dp, max = 280.dp),
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
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            4.dp,
                                            Alignment.Start
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(
                                            0.dp,
                                            Alignment.Top
                                        )
                                    ) {
                                        availableGenres.forEach {
                                            FilterChip(
                                                selected = selectedGenres.contains(it),
                                                onClick = {
                                                    filterViewModel.updateSelectedSubgenre(
                                                        it, !selectedGenres.contains(it)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = it,
                                                        fontSize = 14.sp,
                                                    )
                                                },
                                                modifier = Modifier
                                                    .padding(0.dp),
                                                shape = MaterialTheme.shapes.small,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                ),
                                            )
                                        }
                                    }
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CutFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {
        var showOverflowPopup by remember { mutableStateOf(false) }
        var overflowCount by remember { mutableIntStateOf(0) }
        var shownItems by remember { mutableIntStateOf(0) }

        val availableCuts by filterViewModel.availableCuts.collectAsState()
        val selectedCuts by filterViewModel.sheetSelectedCuts.collectAsState()
        val nothingAssigned = !availableCuts.any { it != "(Unassigned)" }

        Text(
            text = "Cuts:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .align(Alignment.Start),
            color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
        )

        if (nothingAssigned) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No cuts assigned to any blends.",
                    modifier = Modifier
                        .padding(0.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        } else {
            ContextualFlowRow(
                itemCount = availableCuts.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                maxLines = 1,
                overflow = ContextualFlowRowOverflow.expandIndicator {
                    shownItems = shownItemCount
                    overflowCount = availableCuts.size - shownItems

                    val overflowedSelected =
                        filterViewModel.overflowCheck(selectedCuts, availableCuts, shownItemCount)

                    Chip(
                        text = "+$overflowCount",
                        onChipClicked = { showOverflowPopup = true },
                        onChipRemoved = { },
                        trailingIcon = false,
                        modifier = Modifier,
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer else
                                MaterialTheme.colorScheme.background,
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer else
                                MaterialTheme.colorScheme.outline
                        ),
                    )
                },
            ) {
                val cut = availableCuts[it]

                FilterChip(
                    selected = selectedCuts.contains(cut),
                    onClick = {
                        filterViewModel.updateSelectedCut(
                            cut,
                            !selectedCuts.contains(cut)
                        )
                    },
                    label = {
                        Text(
                            text = cut,
                            fontSize = 14.sp,
                        )
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            }
            if (showOverflowPopup) {
                AlertDialog(
                    onDismissRequest = { showOverflowPopup = false },
                    title = {
                        Text(
                            text = "Cuts",
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
                                .heightIn(min = 0.dp, max = 280.dp)
                                .padding(0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                2.dp,
                                Alignment.CenterVertically
                            )
                        ) {
                            GlowBox(
                                color = GlowColor(MaterialTheme.colorScheme.background),
                                size = GlowSize(vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 0.dp, max = 280.dp),
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
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            4.dp,
                                            Alignment.Start
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(
                                            0.dp,
                                            Alignment.Top
                                        )
                                    ) {
                                        availableCuts.forEach {
                                            FilterChip(
                                                selected = selectedCuts.contains(it),
                                                onClick = {
                                                    filterViewModel.updateSelectedCut(
                                                        it,
                                                        !selectedCuts.contains(it)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = it,
                                                        fontSize = 14.sp,
                                                    )
                                                },
                                                modifier = Modifier
                                                    .padding(0.dp),
                                                shape = MaterialTheme.shapes.small,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                ),
                                            )
                                        }
                                    }
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ComponentFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {
        var showOverflowPopup by remember { mutableStateOf(false) }
        var overflowCount by remember { mutableIntStateOf(0) }
        var shownItems by remember { mutableIntStateOf(0) }

        val availableComps by filterViewModel.availableComponents.collectAsState()
        val selectedComps by filterViewModel.sheetSelectedComponents.collectAsState()
        val matchAll by filterViewModel.sheetSelectedComponentMatchAll.collectAsState()
        val nothingAssigned = !availableComps.any { it != "(None Assigned)" }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Components:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier,
                color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
            )
            // Match options
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
                    color = if (nothingAssigned) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
                )
                Box(
                    modifier = Modifier
                        .padding(0.dp)
                        .clickable(
                            enabled = !nothingAssigned,
                            onClick = { filterViewModel.updateCompMatchAll(false) }
                        ),
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
                        fontWeight = if (!matchAll) FontWeight.Medium else FontWeight.Normal,
                        color = if (!matchAll) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                            alpha = .7f
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
                            onClick = { filterViewModel.updateCompMatchAll(true) }
                        ),
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
                        fontWeight = if (matchAll) FontWeight.Medium else FontWeight.Normal,
                        color = if (matchAll) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                            alpha = .7f
                        ),
                        modifier = Modifier
                    )
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
                    text = "No components assigned to any blends.",
                    modifier = Modifier
                        .padding(0.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        } else {
            ContextualFlowRow(
                itemCount = availableComps.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                maxLines = 1,
                overflow = ContextualFlowRowOverflow.expandIndicator {
                    shownItems = shownItemCount
                    overflowCount = availableComps.size - shownItems

                    val overflowedSelected =
                        filterViewModel.overflowCheck(selectedComps, availableComps, shownItemCount)

                    Chip(
                        text = "+$overflowCount",
                        onChipClicked = { showOverflowPopup = true },
                        onChipRemoved = { },
                        trailingIcon = false,
                        modifier = Modifier,
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer else
                                MaterialTheme.colorScheme.background,
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer else
                                MaterialTheme.colorScheme.outline
                        ),
                    )
                },
            ) {
                val comp = availableComps[it]

                FilterChip(
                    selected = selectedComps.contains(comp),
                    onClick = {
                        filterViewModel.updateSelectedComponent(
                            comp,
                            !selectedComps.contains(comp)
                        )
                    },
                    label = {
                        Text(
                            text = comp,
                            fontSize = 14.sp,
                        )
                    },
                    modifier = Modifier
                        .padding(0.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                    enabled = !matchAll || (matchAll && !comp.contains("(None Assigned)"))
                )
            }
            if (showOverflowPopup) {
                AlertDialog(
                    onDismissRequest = { showOverflowPopup = false },
                    title = {
                        Text(
                            text = "Components",
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
                                    .heightIn(min = 0.dp, max = 280.dp),
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
                                        Box(
                                            modifier = Modifier
                                                .padding(0.dp)
                                                .clickable(onClick = {
                                                    filterViewModel.updateCompMatchAll(
                                                        false
                                                    )
                                                }),
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
                                                fontWeight = if (!matchAll) FontWeight.Medium else FontWeight.Normal,
                                                color = if (!matchAll) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
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
                                        Box(
                                            modifier = Modifier
                                                .padding(0.dp)
                                                .clickable(onClick = {
                                                    filterViewModel.updateCompMatchAll(
                                                        true
                                                    )
                                                }),
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
                                                fontWeight = if (matchAll) FontWeight.Medium else FontWeight.Normal,
                                                color = if (matchAll) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                                                    alpha = .7f
                                                ),
                                                modifier = Modifier
                                            )
                                        }
                                    }
                                    // Chips
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            4.dp,
                                            Alignment.Start
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(
                                            0.dp,
                                            Alignment.Top
                                        )
                                    ) {
                                        availableComps.forEach {
                                            FilterChip(
                                                selected = selectedComps.contains(it),
                                                onClick = {
                                                    filterViewModel.updateSelectedComponent(
                                                        it, !selectedComps.contains(it)
                                                    )
                                                },
                                                label = { Text(text = it, fontSize = 14.sp,) },
                                                modifier = Modifier
                                                    .padding(0.dp),
                                                shape = MaterialTheme.shapes.small,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                ),
                                                enabled = !matchAll || (matchAll && !it.contains("(None Assigned)"))
                                            )
                                        }
                                    }
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


/** Custom composable fields **/
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