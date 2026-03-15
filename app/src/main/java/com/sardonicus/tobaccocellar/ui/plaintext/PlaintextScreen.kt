package com.sardonicus.tobaccocellar.ui.plaintext

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.print.PrintManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.data.PrintHelper
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaintextScreen(
    onNavigateUp: () -> Unit,
    isLargeScreen: Boolean,
    modifier: Modifier = Modifier,
    viewModel: PlaintextViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current
    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val selectionFocused by viewModel.selectionFocused.collectAsState()
    val tabIndex by viewModel.tabIndex.collectAsState()

    BackHandler(selectionFocused) {
        if (selectionFocused) {
            viewModel.resetSelection()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetSelection()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) {
                if (selectionFocused) viewModel.resetSelection()
                else focusManager.clearFocus()
            },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.plaintext_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateUp = {
                    viewModel.resetSelection()
                    onNavigateUp()
                },
                showMenu = false,
                modifier = Modifier,
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PlaintextBody(
                viewModel = viewModel,
                filterViewModel = filterViewModel,
                largeScreen = isLargeScreen,
                selectionFocused = selectionFocused,
                tabIndex = tabIndex,
                onTabChange = viewModel::updateTabIndex,
                saveFormatString = viewModel::saveFormatString,
                savePrintOptions = viewModel::savePrintOptions,
                savePreset = viewModel::savePreset,
                updateSelectionFocused = viewModel::updateFocused,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(indication = null, interactionSource = null) {
                        if (selectionFocused) viewModel.resetSelection()
                        else focusManager.clearFocus()
                    }

            )
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PlaintextBody(
    viewModel: PlaintextViewModel,
    filterViewModel: FilterViewModel,
    largeScreen: Boolean,
    selectionFocused: Boolean,
    tabIndex: Int,
    onTabChange: (Int) -> Unit,
    saveFormatString: (String, String) -> Unit,
    savePrintOptions: (Float, Double) -> Unit,
    savePreset: (Int, String, String) -> Unit,
    updateSelectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var anythingFocused by remember { mutableStateOf(false) }
    val updateFocused: (Boolean) -> Unit = { anythingFocused = it }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = anythingFocused && !selectionFocused) { focusManager.clearFocus() }

    val pagerState = rememberPagerState(initialPage = tabIndex) { 2 }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == pagerState.targetPage) {
            if (pagerState.currentPage != tabIndex) {
                onTabChange(pagerState.currentPage)
            }
        }
    }
    LaunchedEffect(tabIndex) {
        if (pagerState.currentPage != tabIndex) {
            pagerState.animateScrollToPage(tabIndex)
        }
    }

    val plainList by viewModel.plainList.collectAsState()
    val selectionKey by viewModel.selectionKey.collectAsState()
    val formatString by viewModel.formatStringEntry.collectAsState()
    val delimiter by viewModel.delimiter.collectAsState()

    val context = LocalContext.current
    val printDialog by viewModel.printDialog.collectAsState()
    val printOptions by viewModel.printOptions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .onFocusChanged { updateFocused(it.hasFocus) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val titles = listOf("List", "Format")

        if (largeScreen) {
            Row(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            if (it.hasFocus && tabIndex == 1) {
                                onTabChange(0)
                            }
                        }
                        .pointerInput(tabIndex) {
                            if (tabIndex == 1) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event =
                                            awaitPointerEvent(pass = PointerEventPass.Initial)
                                        if (event.changes.any { it.changedToDown() }) {
                                            onTabChange(0)
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    GlowBox(
                        color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                        size = GlowSize(top = 3.dp),
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                    ) {
                        PlaintextList(
                            viewModel = viewModel,
                            filterViewModel = filterViewModel,
                            context = context,
                            plainList = plainList,
                            formatString = formatString,
                            selectionKey = selectionKey,
                            updateSelectionFocused = updateSelectionFocused,
                            modifier = Modifier
                        )
                    }
                }
                VerticalDivider()

                GlowBox(
                    color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                    size = GlowSize(top = 3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            if (it.hasFocus && tabIndex == 0) {
                                onTabChange(1)
                            }
                        }
                        .pointerInput(tabIndex) {
                            if (tabIndex == 0) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event =
                                            awaitPointerEvent(pass = PointerEventPass.Initial)
                                        if (event.changes.any { it.changedToDown() }) {
                                            onTabChange(1)
                                        }
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 12.dp)
                ) {
                    PlaintextFormatting(
                        viewModel = viewModel,
                        largeScreen = largeScreen,
                        formatString = formatString,
                        delimiter = delimiter,
                        saveFormatString = saveFormatString,
                        savePreset = savePreset,
                        selectionKey = selectionKey,
                        updateSelectionFocused = updateSelectionFocused,
                        modifier = Modifier
                    )
                }

            }
        } else {
            SecondaryTabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier
                    .padding(bottom = 1.dp),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = LocalContentColor.current,
                indicator = {
                    SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabIndex),
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                },
                divider = {
                    HorizontalDivider(
                        modifier = Modifier,
                        thickness = Dp.Hairline,
                        color = DividerDefaults.color,
                    )
                }
            ) {
                titles.forEachIndexed { index, title ->
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        Tab(
                            selected = tabIndex == index,
                            onClick = { onTabChange(index) },
                            modifier = Modifier
                                .background(
                                    if (tabIndex == index) MaterialTheme.colorScheme.background
                                    else LocalCustomColors.current.backgroundUnselected
                                ),
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.SemiBold,
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.onBackground,
                            unselectedContentColor = MaterialTheme.colorScheme.outline,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !anythingFocused,
                beyondViewportPageCount = 1,
                verticalAlignment = Alignment.Top
            ) { targetIndex ->
                GlowBox(
                    color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                    size = GlowSize(top = 3.dp),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                ) {
                    when (targetIndex) {
                        0 ->
                            PlaintextList(
                                viewModel = viewModel,
                                filterViewModel = filterViewModel,
                                context = context,
                                plainList = plainList,
                                formatString = formatString,
                                selectionKey = selectionKey,
                                updateSelectionFocused = updateSelectionFocused,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        1 ->
                            PlaintextFormatting(
                                viewModel = viewModel,
                                largeScreen = largeScreen,
                                formatString = formatString,
                                delimiter = delimiter,
                                saveFormatString = saveFormatString,
                                savePreset = savePreset,
                                selectionKey = selectionKey,
                                updateSelectionFocused = updateSelectionFocused,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        else ->
                            PlaintextList(
                                viewModel = viewModel,
                                filterViewModel = filterViewModel,
                                context = context,
                                plainList = plainList,
                                formatString = formatString,
                                selectionKey = selectionKey,
                                updateSelectionFocused = updateSelectionFocused,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                    }
                }
            }
        }
    }


    if (printDialog) {
        PrintDialog(
            savedFontSize = printOptions.font,
            savedMargin = printOptions.margin,
            onPrintConfirm = { font, margin ->
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "Plaintext Output"

                printManager?.print(jobName, PrintHelper(jobName, plainList, font, margin), null)
                savePrintOptions(font, margin)
                viewModel.showPrintDialog(false)
            },
            onPrintCancel = { font, margin ->
                savePrintOptions(font, margin)
                viewModel.showPrintDialog(false)
            }
        )
    }

}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun PlaintextActionRow(
    viewModel: PlaintextViewModel,
    filterViewModel: FilterViewModel,
    plainList: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    val sortState by viewModel.sortState.collectAsState()
    val sortOptions by viewModel.sortOptions.collectAsState()
    val sortMenuState by viewModel.sortMenuState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    BackHandler(sortMenuState.mainMenu || sortMenuState.subMenu) {
        if (sortMenuState.mainMenu && !sortMenuState.subMenu) {
            viewModel.updateSortMenuState(sortMenuState.copy(mainMenu = false))
        }
        if (sortMenuState.subMenu) {
            viewModel.updateSortMenuState(sortMenuState.copy(subMenu = false))
        }
    }

    Row(
        modifier = modifier
            .background(LocalCustomColors.current.homeHeaderBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Open filter sheet
        Box(contentAlignment = Alignment.Center) {
            val filteringApplied by filterViewModel.isFilterApplied.collectAsState()
            val borderColor = if (filteringApplied) MaterialTheme.colorScheme.primary else Color.Transparent
            val indicatorColor = if (filteringApplied) LocalCustomColors.current.indicatorCircle else Color.Transparent

            IconButton (
                onClick = filterViewModel::openBottomSheet,
                enabled = sortOptions.isNotEmpty(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
                ),
                modifier = Modifier
                    .padding(0.dp)
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.filter_24),
                    contentDescription = "Filter"
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(7.dp)
                    .offset((-5).dp, (-9).dp)
                    .clip(CircleShape)
                    .border(0.5.dp, borderColor, CircleShape)
                    .background(indicatorColor)
            )
        }

        // Sorting
        Box {
            var reverse: Boolean

            val hasSubOptions = listOf(
                PlaintextSortOption.TIN_LABEL.value,
                PlaintextSortOption.TIN_CONTAINER.value,
                PlaintextSortOption.TIN_QUANTITY.value
            )
            val subOptionsList = listOf(
                PlaintextSortOption.DEFAULT.value,
                PlaintextSortOption.TIN_DEFAULT.value,
                PlaintextSortOption.BRAND.value,
                PlaintextSortOption.BLEND.value
            )

            val density = LocalDensity.current
            var yPositions by remember { mutableStateOf(mapOf<String, Dp>()) }
            var mainWidth by remember { mutableStateOf(0.dp) }
            var mainPosition by remember { mutableStateOf(0.dp) }
            val alteredColor = Color.Black.copy(alpha = .1f).compositeOver(LocalCustomColors.current.textField)
            val color = if (sortMenuState.subMenu) alteredColor else LocalCustomColors.current.textField

            IconButton (
                onClick = { viewModel.updateSortMenuState(sortMenuState.copy(mainMenu = !sortMenuState.mainMenu)) },
                enabled = sortOptions.isNotEmpty(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
                ),
                modifier = Modifier
                    .padding(0.dp)
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sort_bars),
                    contentDescription = "Sorting"
                )
            }

            // Main options
            DropdownMenu(
                expanded = sortMenuState.mainMenu,
                onDismissRequest = { viewModel.updateSortMenuState(sortMenuState.copy(mainMenu = false)) },
                modifier = Modifier
                    .onGloballyPositioned {
                        mainWidth = with(density) { it.size.width.toDp() }
                        mainPosition = with(density) { it.positionOnScreen().x.toDp() }
                    },
                containerColor = color,
                shadowElevation = 6.dp
            ) {
                sortOptions.forEach { option ->
                    var yPosition by remember { mutableStateOf(0.dp) }

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                Text(
                                    text = option.value,
                                    modifier = Modifier
                                        .padding(end = 2.dp),
                                    color = LocalContentColor.current.copy(alpha = if (sortMenuState.subMenu) 0.85f else 1.0f)
                                )
                                // Sort indicator and/or submenu
                                if (sortState.value == option.value) {
                                    Box {
                                        Image(
                                            painter = painterResource(id = sortState.icon),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .padding(0.dp),
                                            colorFilter = ColorFilter.tint(
                                                LocalContentColor.current
                                            )
                                        )
                                    }
                                } else if (option.value in hasSubOptions) {
                                    Box {
                                        Image(
                                            painter = painterResource(R.drawable.arrow_right),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp),
                                            colorFilter = ColorFilter.tint(
                                                LocalContentColor.current.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.width(20.dp))
                                }
                            }
                        },
                        onClick = {
                            if (option.value in hasSubOptions) {
                                viewModel.updateSortMenuState(
                                    sortMenuState.copy(
                                        subMenu = true,
                                        mainSelection = option.value,
                                    )
                                )
                            }
                            else { viewModel.updateSorting(option.value, true) }
                        },
                        modifier = Modifier
                            .onGloballyPositioned {
                                yPosition = with(density) { (it.positionInParent().y).toDp() }
                            }
                    )

                    yPositions = yPositions + (option.value to yPosition)

                }
            }

            // Sub sorting menu
            var subWidth by remember { mutableStateOf(0.dp) }
            val yOffset = yPositions[sortMenuState.mainSelection]
            val mainRightEdge = mainPosition + mainWidth
            val remainingSpace = screenWidth - mainRightEdge
            val xOffset = if (subWidth < (remainingSpace * 1.05f)) mainWidth else -(subWidth)

            DropdownMenu(
                expanded = sortMenuState.subMenu,
                onDismissRequest = { viewModel.updateSortMenuState(sortMenuState.copy(subMenu = false)) },
                containerColor = LocalCustomColors.current.textField,
                modifier = Modifier
                    .onGloballyPositioned{
                        subWidth = with(density) { it.size.width.toDp() }
                    },
                offset = DpOffset(xOffset, yOffset ?: 0.dp),
                shadowElevation = 6.dp
            ) {
                subOptionsList.forEach {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                Text(
                                    text = it,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                )
                                val color = if (sortMenuState.subSelection == it && sortState.value == sortMenuState.mainSelection) {
                                    LocalContentColor.current } else Color.Transparent
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        },
                        onClick = {
                            reverse = sortMenuState.subSelection == it && sortState.value == sortMenuState.mainSelection
                            viewModel.updateSortMenuState(sortMenuState.copy(subSelection = it))
                            viewModel.updateSorting(sortMenuState.mainSelection, reverse)
                            viewModel.updateSubSorting(it)
                        }
                    )
                }
            }
        }

        // Copy
        IconButton(
            onClick = {
                coroutineScope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Plaintext", plainList)))
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = plainList.isNotBlank(),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
            ),
            modifier = Modifier
                .padding(0.dp)
                .size(40.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.copy_icon),
                contentDescription = "Copy",
                modifier = Modifier
                    .padding(0.dp),
            )
        }

        // Print
        IconButton(
            onClick = { viewModel.showPrintDialog(true) },
            enabled = plainList.isNotBlank(),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
            ),
            modifier = Modifier
                .padding(0.dp)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.print_icon),
                contentDescription = "Print",
                modifier = Modifier
                    .padding(0.dp),
            )
        }
    }
}

@Composable
fun PlaintextList(
    viewModel: PlaintextViewModel,
    filterViewModel: FilterViewModel,
    context: Context,
    plainList: String,
    formatString: String,
    selectionKey: Int,
    updateSelectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        if (formatString.isBlank()) {
            Text(
                text = "Please set a format string.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp)
            )
        } else {
            PlaintextActionRow(
                viewModel = viewModel,
                filterViewModel = filterViewModel,
                plainList = plainList,
                context = context,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )
            key(selectionKey) { SelectionContainer(
                modifier = Modifier
                    .onFocusChanged{
                        if (it.isFocused) {
                            updateSelectionFocused(true)
                        } else {
                            updateSelectionFocused(false)
                        }
                    }
            ) {
                Text(
                    text = plainList,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                )
            } }
        }
    }
}


@Composable
fun PlaintextFormatting(
    viewModel: PlaintextViewModel,
    largeScreen: Boolean,
    formatString: String,
    delimiter: String,
    saveFormatString: (String, String) -> Unit,
    savePreset: (Int, String, String) -> Unit,
    selectionKey: Int,
    updateSelectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatPreview by viewModel.formatPreview.collectAsState()
    val presets by viewModel.presets.collectAsState()

    var saveDialog by rememberSaveable { mutableStateOf(false) }
    var loadDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
    ) {
        // Format

        if (largeScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Format Output:",
                    modifier = Modifier,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        else {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Format Output:",
                modifier = Modifier
                    .padding(bottom = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min)
            ) {
                Box (
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "String:",
                        modifier = Modifier
                            .padding(end = 12.dp),
                        maxLines = 1
                    )
                }
                Box (
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Delimiter:",
                        modifier = Modifier
                            .padding(end = 12.dp),
                        maxLines = 1
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
            ) {
                // String
                TextField(
                    value = formatString,
                    onValueChange = { saveFormatString(it, delimiter) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 8.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (formatString.length > 4) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clickable(
                                        indication = LocalIndication.current,
                                        interactionSource = null
                                    ) {
                                        saveFormatString("", delimiter)
                                    }
                                    .alpha(0.66f)
                                    .size(20.dp)
                                    .focusable(false)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Text,
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

                // Delimiter
                TextField (
                    value = delimiter,
                    onValueChange = { saveFormatString(formatString, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Text,
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
            }
        }

        // Load/save presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(.2f))
            TextButton(
                onClick = { saveDialog = true },
                enabled = formatString.isNotBlank(),
                modifier = Modifier
                    .heightIn(40.dp, 40.dp),
                contentPadding = PaddingValues(8.dp, 2.dp),
            ) {
                Text(
                    text = "Save Preset",
                    modifier = Modifier
                )
            }
            Spacer(Modifier.weight(.2f))
            TextButton(
                onClick = { loadDialog = true },
                enabled = presets.any { it.formatString.isNotBlank() },
                modifier = Modifier
                    .heightIn(40.dp, 40.dp),
                contentPadding = PaddingValues(8.dp, 2.dp),
            ) {
                Text(
                    text = "Load Preset",
                    modifier = Modifier
                )
            }
            Spacer(Modifier.weight(.2f))
        }

        Spacer(Modifier.height(16.dp))

        // Preview
        Text(
            text = "Preview:",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(8.dp)
                )
                .background(
                    LocalCustomColors.current.whiteBlack.copy(alpha = .2f),
                    RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Text(
                text = formatPreview,
                modifier = Modifier,
                minLines = 6,
                maxLines = 6,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(30.dp))

        // Formatting Guide
        Text(
            text = "Formatting Guide",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold,
        )
        // Formatting Options
        key(selectionKey) { SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
                .onFocusChanged {
                    if (it.isFocused) {
                        updateSelectionFocused(true)
                    } else {
                        updateSelectionFocused(false)
                    }
                }
        ) {
            FormattingGuide()
        } }

        Text(
            text = "Formatting Help",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        var expanded by remember { mutableStateOf(false) }

        if (!expanded) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 12.dp)
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { expanded = true }
            ) {
                HorizontalDivider(Modifier.weight(1f), 1.dp)
                Text(
                    text = "Click to Expand",
                    fontSize = 14.sp,
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
                HorizontalDivider(Modifier.weight(1f), 1.dp)
            }

        } else {
            FormattingHelp({expanded = false})
        }


        Spacer(Modifier.height(24.dp))

        if (saveDialog) {
            SaveDialog(
                savedPresets = presets,
                formatString = formatString,
                delimiter = delimiter,
                onSaveConfirm = { slot, formatString, delimiter ->
                    savePreset(slot, formatString, delimiter)
                },
                onDeleteConfirm = { savePreset(it, "", "") },
                onSaveCancel = { saveDialog = false },
            )
        }
        if (loadDialog) {
            LoadDialog(
                savedPresets = presets,
                formatString = formatString,
                delimiter = delimiter,
                onLoadConfirm = { string, delimiter ->
                    saveFormatString(string, delimiter)
                },
                onDeleteConfirm = { savePreset(it, "", "") },
                onLoadCancel = { loadDialog = false },
            )
        }
    }
}


@Composable
private fun FormattingGuide(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        val formatGuide = mapOf(
            "Brand" to "@brand",
            "Blend" to "@blend",
            "Type" to "@type",
            "Subgenre" to "@subgenre",
            "Cut" to "@cut",
            "Components" to "@comps",
            "Flavoring" to "@flavors",
            "Quantity" to "@qty",
            "Rating" to "@rating_0_0",
            "Production" to "@prod",
            "Tin Label" to "@label",
            "Tin Container" to "@container",
            "Tin Quantity" to "@T_qty",
            "Manufacture" to "@manufacture",
            "Cellar Date" to "@cellar",
            "Open Date" to "@open",
            "Finished" to "@finished",
            "New Line" to "_n_",
            "Number" to "#",
            "Escape char" to "'",
            "Conditional" to "[...]",
            "Tin sublist" to "{...}",
            "Sublist delim." to "~"
        )

        val firstHalf = formatGuide.entries.take((formatGuide.size / 2.0).roundToInt())
        val secondHalf = formatGuide.entries.drop(firstHalf.size)
        val height: Dp = with(LocalDensity.current) { 24.sp.toDp() }

        // first half
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .padding(end = 8.dp),
                ) {
                    firstHalf.forEach {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "${it.key}:",
                                modifier = Modifier,
                                style = TextStyle(
                                    color = LocalContentColor.current,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 10.sp,
                                    maxFontSize = 14.sp,
                                    stepSize = 0.25.sp
                                )
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier,
                ) {
                    firstHalf.forEach {
                        Box(
                            modifier = Modifier
                                .height(height),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = it.value,
                                modifier = Modifier,
                                style = TextStyle(
                                    color = LocalContentColor.current,
                                ),
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 10.sp,
                                    maxFontSize = 14.sp,
                                    stepSize = 0.25.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(36.dp))

        // second half
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .padding(end = 8.dp),
                ) {
                    secondHalf.forEach {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "${it.key}:",
                                modifier = Modifier,
                                style = TextStyle(
                                    color = LocalContentColor.current,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 10.sp,
                                    maxFontSize = 14.sp,
                                    stepSize = 0.25.sp
                                )
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier,
                ) {
                    secondHalf.forEach {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = it.value,
                                modifier = Modifier,
                                style = TextStyle(
                                    color = LocalContentColor.current,
                                ),
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 10.sp,
                                    maxFontSize = 14.sp,
                                    stepSize = 0.25.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattingHelp(
    hide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Anything typed in the format string will show in the text. To reference " +
                    "specific fields, use the placeholders above. Sorting options are generated " +
                    "based on the format string placeholders (set format string before sorting). " +
                    "Using the delimiter field will automatically remove the delimiter from the " +
                    "last line.",
            modifier = Modifier
        )
        Text(
            text = "Use the delimiter line for how to separate records in the generated string. " +
                    "Anything typed here will show up in-between each record. So, to separate " +
                    "each record by a blank line, you would need to enter \"_n__n_\". When tins " +
                    "are passed as a sublist, mark the start of the tins sublist delimiter with " +
                    "a tilde (~) at the end of the tins-sublist formatting, inside the closing " +
                    "tins as sublist bracket (e.g.: {@label~, } or {@label~_n_}.",
            modifier = Modifier
        )
        Text(
            text = "The \"@rating_0_0\" tag is to be used in a specific way. The first zero should " +
                    "be replaced with the desired max rating (for scaling). The second \"_0\" is " +
                    "optional for the number of decimal places to be rounded to (max of 2, enter " +
                    "0 to round to the nearest whole number). For example, to pass the rating on " +
                    "a scale of 1-4 with whole number rounding, enter \"@rating_4_0\" into the " +
                    "formatting. More advanced examples might be:\n" +
                    "\"[@rating_10_0 stars]\" (of 10, whole number) or \"[@rating_4_2/4]\" (of 4, " +
                    "two places)",
            modifier = Modifier
        )
        Text(
            text = "\"Number\" is a special tag that counts each record in the given sort order " +
                    "(use multiple # to include leading 0's).",
            modifier = Modifier
        )
        Text(
            text = "In order to output raw text rather than special characters, escape the " +
                    "special character with the escape character. For example, to output # in the " +
                    "string, enter: '#. Likewise for example, to output brackets around a field, " +
                    "escape each bracket (e.g. '[@type']). The escape character itself doesn't " +
                    "need to be escaped unless you're trying to use it before an escapable " +
                    "character (e.g. to render: '01' you would need to input ''##').",
            modifier = Modifier
        )
        Text(
            text = "Use the square brackets ([ ]) when you only want the text within them " +
                    "to appear if one or more placeholders (also inside the brackets) are " +
                    "found. For instance, if you want the type shown on a new line, but " +
                    "don't want an extra line for a blank type, enter: [_n_@type]. These " +
                    "conditional brackets can also be nested.",
            modifier = Modifier
        )
        Text(
            text = "When sorting by items, if you want the tins organized as a sublist " +
                    "per each item, use the curly braces around the formatting you want for " +
                    "tins (e.g. {@label (@T_qty)}). Conditional brackets can also be used " +
                    "inside the curly braces.",
            modifier = Modifier
        )
        Text(
            text = "To set a delimiter for tins as a sublist, at the very end of the tin line " +
                    "formatting, still inside the tins as sublist brackets, place a tilde (~) " +
                    "just before the desired delimiter, followed by delimiter. For example, to " +
                    "separate each tin in the sublist by a new line, enter: {@label~_n_}.",
            modifier = Modifier
        )
        Text(
            text = "A more advanced example might be to pass the list of tins only if tins exist " +
                    "for that blend and passing the quantity in brackets. For example, entering...",
            modifier = Modifier
        )
        Text(
            text = "@brand - \"@blend\"[_n_{    - @label '[@T_qty']~_n_}]",
            modifier = Modifier,
            fontSize = 14.sp,
        )
        Text(
            text = "... would result in:",
            modifier = Modifier
        )
        Box {
            Text(
                text = "Lane Limited - \"Very Cherry\"\n        - Lot 1 [2 oz]\n        - Lot 2 [50 grams]",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(horizontal = 12.dp)
                .clickable(
                    indication = LocalIndication.current,
                    interactionSource = null
                ) { hide() }
        ) {
            HorizontalDivider(Modifier.weight(1f), 1.dp)
            Text(
                text = "Click to Hide",
                fontSize = 14.sp,
                color = LocalContentColor.current.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
            HorizontalDivider(Modifier.weight(1f), 1.dp)
        }
    }
}

/** Dialogs **/
@Composable
private fun PrintDialog(
    savedFontSize: Float,
    savedMargin: Double,
    onPrintConfirm: (Float, Double) -> Unit,
    onPrintCancel: (Float, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var fontSize by rememberSaveable { mutableFloatStateOf(savedFontSize) }
    var margins by rememberSaveable { mutableDoubleStateOf(savedMargin) }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
    val decimalSeparator = symbols.decimalSeparator.toString()

    AlertDialog(
        onDismissRequest = { onPrintCancel(fontSize, margins) },
        confirmButton = {
            TextButton(
                onClick = { onPrintConfirm(fontSize, margins) },
            ) {
                Text(text = "Print")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onPrintCancel(fontSize, margins) },
            ) {
                Text(text = "Cancel")
            }
        },
        title = { Text(text = "Print Settings") },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Font size is standard point-font size. Margin value is a multiplier " +
                            "of 1 inch with a range of 0-3 (including decimals, for example 0.5).",
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(Modifier.weight(.5f))
                    // Labels
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .fillMaxHeight()
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("Font Size:")
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("Margins:")
                        }
                    }

                    // Text fields
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        val fontPattern = remember { Regex("^(\\s*|\\d{0,2})$") }
                        val marginPattern = remember(decimalSeparator) {
                            val ds = Regex.escape(decimalSeparator)
                            Regex("^(\\s*|(\\d?)?($ds\\d{0,2})?)$")
                        }
                        var fontSizeString by rememberSaveable { mutableStateOf(fontSize.toInt().toString()) }
                        var marginsString by rememberSaveable { mutableStateOf(formatDecimal(margins)) }

                        // font
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTextField(
                                value = fontSizeString,
                                onValueChange = {
                                    if (it.matches(fontPattern)) {
                                        fontSizeString = it
                                        fontSize =
                                            if (it.isNotBlank()) {
                                                it.toFloatOrNull() ?: fontSize
                                            } else { 12f }
                                    }
                                },
                                suffix = {
                                    Text(
                                        "pt",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .padding(0.75.dp),
                                        color = LocalContentColor.current.copy(alpha = .8f)
                                    )
                                },
                                modifier = Modifier
                                    .width(68.dp)
                                    .padding(vertical = 4.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.End,
                                    color = LocalContentColor.current
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = LocalCustomColors.current.textField,
                                    unfocusedContainerColor = LocalCustomColors.current.textField,
                                    disabledContainerColor = LocalCustomColors.current.textField.copy(
                                        alpha = 0.66f
                                    ),
                                    disabledTextColor = LocalContentColor.current.copy(alpha = 0.66f),
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                            )

                            // increase/decrease font buttons
                            IncreaseDecrease(
                                increaseClick = {
                                    if (fontSizeString.isEmpty()) {
                                        fontSize = 1f
                                        fontSizeString = "1"
                                    } else {
                                        if (fontSize < 99f) {
                                            fontSize += 1
                                            fontSizeString = fontSize.toInt().toString()
                                        } else {
                                            fontSize = 99f
                                            fontSizeString = "99"
                                        }
                                    }
                                },
                                decreaseClick = {
                                    if (fontSizeString.isEmpty()) {
                                        fontSize = 6f
                                        fontSizeString = "6"
                                    } else {
                                        if (fontSize > 1f) {
                                            fontSize -= 1
                                            fontSizeString = fontSize.toInt().toString()
                                        } else if (fontSize <= 1f) {
                                            fontSize = 1f
                                            fontSizeString = "1"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight(),
                            )
                        }

                        // margins
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTextField(
                                value = marginsString,
                                onValueChange = {
                                    if (it.matches(marginPattern)) {
                                        marginsString = it
                                        try {
                                            var parsedDouble: Double?

                                            if (it.isNotBlank()) {
                                                val preNumber =
                                                    if (it.startsWith(decimalSeparator)) {
                                                        "0$it"
                                                    } else it
                                                val number = numberFormat.parse(preNumber)

                                                parsedDouble = number?.toDouble() ?: 1.0
                                            } else {
                                                parsedDouble = 1.0
                                            }

                                            margins = if (parsedDouble <= 3.0) parsedDouble else 3.0
                                        } catch (e: ParseException) {
                                            Log.e("Print dialog", "Input: $it", e)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .width(68.dp)
                                    .padding(vertical = 4.dp)
                                    .onFocusChanged {
                                        if (!it.hasFocus) marginsString = formatDecimal(margins)
                                    },
                                singleLine = true,
                                suffix = {
                                    Text(
                                        text = "x",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .padding(start = 0.75.dp),
                                        color = LocalContentColor.current.copy(alpha = .8f)
                                    )
                                         },
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.End,
                                    color = LocalContentColor.current
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = LocalCustomColors.current.textField,
                                    unfocusedContainerColor = LocalCustomColors.current.textField,
                                    disabledContainerColor = LocalCustomColors.current.textField.copy(
                                        alpha = 0.66f
                                    ),
                                    disabledTextColor = LocalContentColor.current.copy(alpha = 0.66f),
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                            )

                            // increase/decrease margin buttons
                            IncreaseDecrease(
                                increaseClick = {
                                    if (marginsString.isEmpty()) {
                                        margins = 0.25
                                        marginsString = "0.25"
                                    } else {
                                        if (margins < 3.0) {
                                            margins += 0.25
                                            marginsString = formatDecimal(margins)
                                        } else {
                                            margins = 3.0
                                            marginsString = "3"
                                        }
                                    }
                                },
                                decreaseClick = {
                                    if (marginsString.isEmpty()) {
                                        margins = 0.0
                                        marginsString = formatDecimal(margins)
                                    } else {
                                        if (margins > 0.25) {
                                            margins -= 0.25
                                            marginsString = formatDecimal(margins)
                                        } else if (margins <= 0.0) {
                                            margins = 0.0
                                            marginsString = formatDecimal(margins)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight(),
                            )
                        }
                    }
                    Spacer(Modifier.weight(.5f))
                }
            }
        },
    )
}


@Composable
private fun SaveDialog(
    savedPresets: List<PlaintextPreset>,
    formatString: String,
    delimiter: String,
    onSaveConfirm: (Int, String, String) -> Unit,
    onDeleteConfirm: (Int) -> Unit,
    onSaveCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSlot by rememberSaveable { mutableIntStateOf(-1) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val onConfirm: (Boolean) -> Unit = { confirmDelete = it }

    AlertDialog(
        onDismissRequest = { onSaveCancel() },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveConfirm(selectedSlot, formatString, delimiter)
                    onSaveCancel()
                },
                enabled = selectedSlot != -1
            ) { Text(text = "Save") }
        },
        dismissButton = {
            TextButton(
                onClick = { onSaveCancel() },
            ) { Text(text = "Cancel") }
        },
        title = { Text(text = "Save Preset") },
        modifier = modifier
            .fillMaxWidth(.9f)
            .clickable(
                indication = null,
                interactionSource = null
            ) { selectedSlot = -1 },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        shape = MaterialTheme.shapes.small,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                (0..4).forEach {
                    val preset = savedPresets[it]
                    val isSelected = selectedSlot == it
                    val presetExists = preset.formatString == formatString && preset.delimiter == delimiter
                    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = .07f).compositeOver(LocalCustomColors.current.darkNeutral)

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else if (presetExists) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (isSelected) selectedColor else
                                    LocalCustomColors.current.darkNeutral,
                                RoundedCornerShape(4.dp)
                            )
                            .combinedClickable(
                                indication = null,
                                interactionSource = null,
                                onClick = { selectedSlot = if (isSelected) -1 else it },
                                onLongClick = {
                                    if (preset.formatString.isNotBlank()) {
                                        selectedSlot = it
                                        onConfirm(true)
                                    }
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Box (
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row (
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val color = if (isSelected) LocalContentColor.current.copy(alpha = .2f) else LocalContentColor.current
                                val color2 = if (isSelected) LocalContentColor.current.copy(alpha = .2f) else LocalContentColor.current.copy(alpha = .5f)
                                Text(
                                    text = "${it + 1}:",
                                    modifier = Modifier
                                        .width(IntrinsicSize.Max)
                                        .padding(end = 8.dp),
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                                Text(
                                    text = preset.formatString,
                                    modifier = Modifier
                                        .weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = color
                                )
                                if (preset.formatString.isNotBlank()) {
                                    VerticalDivider(
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp)
                                            .fillMaxHeight(),
                                        color = color,
                                        thickness = 1.5.dp
                                    )
                                    Text(
                                        text = preset.delimiter.ifBlank{ "n/a" },
                                        modifier = Modifier
                                            .width(44.dp)
                                            .padding(start = 2.dp),
                                        color = if (preset.delimiter.isBlank()) color2 else color,
                                        maxLines = 1,
                                        fontStyle = if (preset.delimiter.isBlank()) FontStyle.Italic else FontStyle.Normal,
                                        textAlign = if (preset.delimiter.isBlank()) TextAlign.Center else TextAlign.Start,
                                    )
                                }
                            }
                            if (isSelected && preset.formatString.isNotBlank()) {
                                Text(
                                    text = "Overwrite?",
                                    modifier = Modifier
                                        .matchParentSize(),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        },
    )
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { onConfirm(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirm(selectedSlot)
                        selectedSlot = -1
                        onConfirm(false)
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onConfirm(false) },
                ) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = "Delete Preset") },
            text = { Text(text = "Are you sure you want to delete the preset in Slot ${selectedSlot + 1}?") },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            textContentColor = MaterialTheme.colorScheme.onBackground,
        )
    }
}


@Composable
private fun LoadDialog(
    savedPresets: List<PlaintextPreset>,
    formatString: String,
    delimiter: String,
    onLoadConfirm: (String, String) -> Unit,
    onLoadCancel: () -> Unit,
    onDeleteConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSlot by rememberSaveable { mutableIntStateOf(-1) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val onConfirm: (Boolean) -> Unit = { confirmDelete = it }

    AlertDialog(
        onDismissRequest = { onLoadCancel() },
        confirmButton = {
            TextButton(
                onClick = {
                    onLoadConfirm(savedPresets[selectedSlot].formatString, savedPresets[selectedSlot].delimiter)
                    onLoadCancel()
                },
                enabled = selectedSlot != -1
            ) { Text(text = "Load") }
        },
        dismissButton = {
            TextButton(
                onClick = { onLoadCancel() },
            ) { Text(text = "Cancel") }
        },
        title = { Text(text = "Load Preset") },
        modifier = modifier
            .fillMaxWidth(.9f)
            .clickable(
                indication = null,
                interactionSource = null
            ) { selectedSlot = -1 },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                (0..4).forEach {
                    val preset = savedPresets[it]
                    val isSelected = selectedSlot == it
                    val presetLoaded = preset.formatString == formatString && preset.delimiter == delimiter
                    val disabled = preset.formatString.isBlank()
                    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = .07f).compositeOver(LocalCustomColors.current.darkNeutral)

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                width = Dp.Hairline,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else if (presetLoaded) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (isSelected) selectedColor
                                else if (disabled) LocalCustomColors.current.darkNeutral.copy(alpha = .38f)
                                else LocalCustomColors.current.darkNeutral,
                                RoundedCornerShape(4.dp)
                            )
                            .combinedClickable(
                                enabled = preset.formatString.isNotBlank(),
                                indication = null,
                                interactionSource = null,
                                onClick = { selectedSlot = if (isSelected) -1 else it },
                                onLongClick = {
                                    selectedSlot = it
                                    onConfirm(true)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "${it + 1}:",
                            modifier = Modifier
                                .padding(end = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (disabled) LocalContentColor.current.copy(alpha = .38f) else LocalContentColor.current
                        )
                        Text(
                            text = preset.formatString,
                            modifier = Modifier
                                .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Normal,
                            color = LocalContentColor.current
                        )
                        if (preset.formatString.isNotBlank()) {
                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .fillMaxHeight(),
                                color = LocalContentColor.current,
                                thickness = 1.5.dp
                            )
                            Text(
                                text = preset.delimiter.ifBlank{ "n/a" },
                                modifier = Modifier
                                    .width(44.dp)
                                    .padding(start = 2.dp),
                                color = if (preset.delimiter.isBlank()) LocalContentColor.current.copy(alpha = .5f) else LocalContentColor.current,
                                maxLines = 1,
                                fontStyle = if (preset.delimiter.isBlank()) FontStyle.Italic else FontStyle.Normal,
                                textAlign = if (preset.delimiter.isBlank()) TextAlign.Center else TextAlign.Start,
                            )
                        }
                    }
                }
            }
        },
    )
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { onConfirm(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirm(selectedSlot)
                        selectedSlot = -1
                        onConfirm(false)
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onConfirm(false) },
                ) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = "Delete Preset") },
            text = { Text(text = "Are you sure you want to delete the preset in Slot ${selectedSlot + 1}?") },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            textContentColor = MaterialTheme.colorScheme.onBackground,
        )
    }
}