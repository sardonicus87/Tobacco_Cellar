package com.sardonicus.tobaccocellar.ui.plaintext

import android.annotation.SuppressLint
import android.content.Context
import android.print.PrintManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.columns
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.rows
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.PrintHelper
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaintextScreen(
    onNavigateUp: () -> Unit,
    twoColumnTabs: Boolean,
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    viewModel: PlaintextViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current
    var anyFocused by remember { mutableStateOf(false) }
    var selectionFocused by remember { mutableStateOf(false) }
    var selectionKey by remember { mutableIntStateOf(0) }

    var actionRowToggleBounds by remember { mutableStateOf<Rect?>(null) }
    var actionButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var saveLoadBounds by remember { mutableStateOf<Rect?>(null) }
    var scaffoldCords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    BackHandler(selectionFocused || anyFocused) {
        if (selectionFocused) { selectionKey++ }
        else { focusManager.clearFocus() }
    }

    DisposableEffect(Unit) { onDispose { selectionKey++ } }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .onGloballyPositioned { scaffoldCords = it }
            .pointerInput(selectionFocused, anyFocused) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    val tapRoot =
                        scaffoldCords?.localToRoot(down.position) ?: return@awaitEachGesture
                    val actionToggle = actionRowToggleBounds?.contains(tapRoot) ?: false
                    val isOtherTap = listOfNotNull(actionButtonBounds, saveLoadBounds)
                        .any { it.contains(tapRoot) }

                    when {
                        actionToggle -> if (anyFocused && !selectionFocused) {
                            focusManager.clearFocus(); down.consume() }
                        selectionFocused -> { selectionKey++; down.consume() }
                        anyFocused -> { focusManager.clearFocus(); if (isOtherTap) down.consume() }
                    }
                }
            },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.plaintext_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateUp = {
                    if (selectionFocused) { selectionKey++ } else { focusManager.clearFocus() }
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
                .onFocusChanged { anyFocused = it.hasFocus }
                .padding(innerPadding)
        ) {
            PlaintextBody(
                viewModel = viewModel,
                filterViewModel = filterViewModel,
                twoColumnTabs = twoColumnTabs,
                selectionKey = selectionKey,
                resetSelection = { selectionKey++ },
                anyFocused = anyFocused,
                selectionFocused = selectionFocused,
                updateSelectionFocused = { selectionFocused = it },
                actionRowBounds = { actionRowToggleBounds = it.boundsInRoot() },
                actionButtonBounds = { actionButtonBounds = it.boundsInRoot() },
                saveLoadBounds = { saveLoadBounds = it.boundsInRoot() },
                focusManager = focusManager,
                savePrintOptions = viewModel::savePrintOptions
            )
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun PlaintextBody(
    viewModel: PlaintextViewModel,
    filterViewModel: FilterViewModel,
    twoColumnTabs: Boolean,
    selectionKey: Int,
    resetSelection: () -> Unit,
    anyFocused: Boolean,
    selectionFocused: Boolean,
    updateSelectionFocused: (Boolean) -> Unit,
    actionRowBounds: (LayoutCoordinates) -> Unit,
    actionButtonBounds: (LayoutCoordinates) -> Unit,
    saveLoadBounds: (LayoutCoordinates) -> Unit,
    focusManager: FocusManager,
    savePrintOptions: (Float, Double) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = tabIndex) { 2 }

    // pager and tab synchronizing
    SideEffect(pagerState.currentPage) {
        if (pagerState.currentPage == pagerState.targetPage) {
            if (pagerState.currentPage != tabIndex) { tabIndex = pagerState.currentPage }
        }
    }
    LaunchedEffect(tabIndex) {
        if (pagerState.currentPage != tabIndex) { pagerState.animateScrollToPage(tabIndex) }
    }

    val fieldInteractionSource = remember { MutableInteractionSource() }
    val unfocusedFieldScroll by fieldInteractionSource.collectIsDraggedAsState()

    val plainList by viewModel.plainList.collectAsState()
    var actionRowExpanded by remember { mutableStateOf(false) }
    val formatString by viewModel.formatStringEntry.collectAsState()
    val delimiter by viewModel.delimiter.collectAsState()
    val listAs by viewModel.listAsTins.collectAsState()

    val context = LocalContext.current
    var printDialog by remember { mutableStateOf(false) }
    val printOptions by viewModel.printOptions.collectAsState()

    BackHandler(actionRowExpanded) { actionRowExpanded = false }

    if (twoColumnTabs) {
        Row(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.hasFocus && tabIndex == 1) tabIndex = 0 }
                    .pointerInput(tabIndex) {
                        if (tabIndex == 1) {
                            awaitEachGesture {
                                awaitFirstDown(pass = PointerEventPass.Initial); tabIndex = 0
                            }
                        }
                    }
            ) {
                GlowBox(
                    color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                    size = GlowSize(top = 3.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    PlaintextList(
                        viewModel = viewModel,
                        filterViewModel = filterViewModel,
                        context = context,
                        plainList = plainList,
                        actionRowBounds = actionRowBounds,
                        actionButtonBounds = actionButtonBounds,
                        actionRowExpanded = actionRowExpanded,
                        toggleActionRow = { actionRowExpanded = !actionRowExpanded },
                        showPrintDialog = { printDialog = true },
                        formatString = formatString,
                        selectionKey = selectionKey,
                        updateSelectionFocused = updateSelectionFocused
                    )
                }
            }
            VerticalDivider()

            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                size = GlowSize(top = 3.dp),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.hasFocus && tabIndex == 0) tabIndex = 1 }
                    .pointerInput(tabIndex) {
                        if (tabIndex == 0) {
                            awaitEachGesture {
                                awaitFirstDown(pass = PointerEventPass.Initial); tabIndex = 1
                            }
                        }
                    }
                    .padding(horizontal = 12.dp)
            ) {
                PlaintextFormatting(
                    viewModel = viewModel,
                    saveLoadBounds = saveLoadBounds,
                    twoColumnTabs = twoColumnTabs,
                    formatString = formatString,
                    delimiter = delimiter,
                    listAs = listAs,
                    selectionKey = selectionKey,
                    updateSelectionFocused = updateSelectionFocused
                )
            }
        }
    } else {
        BackHandler(pagerState.currentPage != 0 && (!anyFocused && !selectionFocused)) { tabIndex = 0 }

        LaunchedEffect(actionRowExpanded) {
            if (actionRowExpanded) {
                snapshotFlow { pagerState.isScrollInProgress }.first { it }
                actionRowExpanded = false
            }
        }

        SecondaryTabRow(
            selectedTabIndex = tabIndex,
            modifier = Modifier.padding(bottom = 1.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = LocalContentColor.current,
            indicator = {
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabIndex),
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            },
            divider = { HorizontalDivider(thickness = Dp.Hairline, color = DividerDefaults.color) }
        ) {
            listOf("List", "Format").forEachIndexed { index, title ->
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    Tab(
                        selected = tabIndex == index,
                        onClick = {
                            if (selectionFocused) { resetSelection() }
                            else if (anyFocused) { focusManager.clearFocus() }
                            else { tabIndex = index }
                        },
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
            userScrollEnabled = !anyFocused && !selectionFocused && !unfocusedFieldScroll,
            beyondViewportPageCount = 1,
            verticalAlignment = Alignment.Top
        ) { targetIndex ->
            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = 0.3f)),
                size = GlowSize(top = 3.dp)
            ) {
                when (targetIndex) {
                    0 ->
                        PlaintextList(
                            viewModel = viewModel,
                            filterViewModel = filterViewModel,
                            context = context,
                            plainList = plainList,
                            actionRowBounds = actionRowBounds,
                            actionButtonBounds = actionButtonBounds,
                            actionRowExpanded = actionRowExpanded,
                            toggleActionRow = { actionRowExpanded = !actionRowExpanded },
                            showPrintDialog = { printDialog = true },
                            formatString = formatString,
                            selectionKey = selectionKey,
                            updateSelectionFocused = updateSelectionFocused,
                            modifier = Modifier.fillMaxWidth()
                        )
                    1 ->
                        PlaintextFormatting(
                            viewModel = viewModel,
                            saveLoadBounds = saveLoadBounds,
                            twoColumnTabs = twoColumnTabs,
                            formatString = formatString,
                            delimiter = delimiter,
                            listAs = listAs,
                            selectionKey = selectionKey,
                            updateSelectionFocused = updateSelectionFocused,
                            fieldInteractionSource = fieldInteractionSource,
                            modifier = Modifier.fillMaxWidth()
                        )
                    else ->
                        PlaintextList(
                            viewModel = viewModel,
                            filterViewModel = filterViewModel,
                            context = context,
                            plainList = plainList,
                            actionRowBounds = actionRowBounds,
                            actionButtonBounds = actionButtonBounds,
                            actionRowExpanded = actionRowExpanded,
                            toggleActionRow = { actionRowExpanded = !actionRowExpanded },
                            showPrintDialog = { printDialog = true },
                            formatString = formatString,
                            selectionKey = selectionKey,
                            updateSelectionFocused = updateSelectionFocused,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                printManager?.print("Plaintext Output", PrintHelper("Plaintext Output", plainList, font, margin), null)

                savePrintOptions(font, margin);printDialog = false
            },
            onPrintCancel = { font, margin -> savePrintOptions(font, margin); printDialog = false }
        )
    }
}




@Composable
private fun PlaintextList(
    viewModel: PlaintextViewModel,
    filterViewModel: FilterViewModel,
    context: Context,
    plainList: String,
    actionRowBounds: (LayoutCoordinates) -> Unit,
    actionButtonBounds: (LayoutCoordinates) -> Unit,
    actionRowExpanded: Boolean,
    toggleActionRow: () -> Unit,
    showPrintDialog: () -> Unit,
    formatString: String,
    selectionKey: Int,
    updateSelectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val loading by viewModel.loading.collectAsState()
    var showLoading by remember { mutableStateOf(true) }

    LaunchedEffect(loading) {
        if (!loading) {
            delay(25.milliseconds)
            showLoading = false
        }
    }

    LaunchedEffect(actionRowExpanded) {
        if (actionRowExpanded) {
            snapshotFlow { scrollState.isScrollInProgress }.first { !it }
            snapshotFlow { scrollState.isScrollInProgress }.first { it }
            toggleActionRow()
        }
    }

    Box {
        if (showLoading) { Column (Modifier.fillMaxSize()) { LoadingIndicator() } }
        else {
            Column(
                modifier = modifier
                    .padding(horizontal = 8.dp)
                    .verticalScroll(scrollState),
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
                    key(selectionKey) {
                        SelectionContainer(
                            Modifier.onFocusChanged { updateSelectionFocused(it.isFocused) }
                        ) {
                            Text(
                                text = plainList,
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 32.dp)
                            )
                        }
                    }
                }
            }
            PlaintextActionRow(
                viewModel = viewModel,
                filterViewModel = filterViewModel,
                actionRowBounds = actionRowBounds,
                otherBounds = actionButtonBounds,
                expanded = actionRowExpanded,
                toggleActionRow = toggleActionRow,
                showPrintDialog = showPrintDialog,
                plainList = plainList,
                context = context,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp)
            )
        }
    }
}


@OptIn(ExperimentalGridApi::class)
@Composable
private fun PlaintextFormatting(
    viewModel: PlaintextViewModel,
    saveLoadBounds: (LayoutCoordinates) -> Unit,
    twoColumnTabs: Boolean,
    formatString: String,
    delimiter: String,
    listAs: Boolean,
    selectionKey: Int,
    updateSelectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    fieldInteractionSource: MutableInteractionSource? = null
) {
    val formatPreview by viewModel.formatPreview.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val tinsEnabled by viewModel.listAsTinsEnabled.collectAsState()

    SideEffect(tinsEnabled) { if (!tinsEnabled) { viewModel.saveFormatting(listAsTins = false) } }

    var saveDialog by remember { mutableStateOf(false) }
    var loadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Top,
    ) {
        if (twoColumnTabs) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) { Text("Format Output:", fontWeight = FontWeight.SemiBold) }
        } else {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Format Output:",
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
        }

        Grid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 24.dp),
            config = {
                columns(GridTrackSize.Auto, GridTrackSize.Flex(1.fr))
                rows(GridTrackSize.Auto, GridTrackSize.Auto, GridTrackSize.Auto, GridTrackSize.Auto)
                gap(row = 8.dp, column = 20.dp)
            }
        ) {
            Text(
                text = "String:",
                modifier = Modifier
                    .gridItem(row = 1, column = 1, alignment = Alignment.CenterStart),
                maxLines = 1
            )
            Text(
                text = "Delimiter:",
                modifier = Modifier
                    .gridItem(row = 2, column = 1, alignment = Alignment.CenterStart),
                maxLines = 1
            )
            Text(
                text = "List as:",
                modifier = Modifier
                    .gridItem(row = 3, column = 1, alignment = Alignment.CenterStart),
                maxLines = 1
            )

            // format string
            TextField(
                value = formatString,
                onValueChange = { viewModel.saveFormatting(format = it) },
                modifier = Modifier
                    .gridItem(row = 1, column = 2, alignment = Alignment.CenterStart)
                    .fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (formatString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(null, LocalIndication.current) {
                                    viewModel.saveFormatting(format = "")
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
                shape = MaterialTheme.shapes.extraSmall,
                interactionSource = fieldInteractionSource
            )

            // Delimiter
            TextField(
                value = delimiter,
                onValueChange = { viewModel.saveFormatting(delimiter = it) },
                modifier = Modifier
                    .gridItem(row = 2, column = 2, alignment = Alignment.CenterStart)
                    .fillMaxWidth(),
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
                shape = MaterialTheme.shapes.extraSmall,
                interactionSource = fieldInteractionSource
            )

            // list as tins
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                modifier = Modifier
                    .gridItem(row = 3, column = 2, alignment = Alignment.CenterStart)
            ) {
                Text(
                    text = "Entries",
                    fontWeight = if (!listAs) FontWeight.Bold else FontWeight.Normal,
                    color = LocalContentColor.current.copy(alpha = if (!listAs) 1f else .5f),
                    fontSize = 14.sp,
                    lineHeight = 1.em,
                    modifier = Modifier
                        .clip(RoundedCornerShape(25))
                        .clickable(null, LocalIndication.current) {
                            viewModel.saveFormatting(listAsTins = false)
                        }
                        .padding(8.dp, 4.dp)
                )
                Text(
                    text = "Tins",
                    fontWeight = if (listAs) FontWeight.Bold else FontWeight.Normal,
                    color = LocalContentColor.current.copy(alpha = if (listAs) 1f else .5f),
                    fontSize = 14.sp,
                    lineHeight = 1.em,
                    modifier = Modifier
                        .clip(RoundedCornerShape(25))
                        .clickable(null, LocalIndication.current, tinsEnabled) {
                            viewModel.saveFormatting(listAsTins = true)
                        }
                        .padding(8.dp, 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .gridItem(row = 4, column = 1, columnSpan = 2, alignment = Alignment.Center)
                    .onGloballyPositioned { saveLoadBounds(it) },
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { saveDialog = true },
                    enabled = formatString.isNotBlank(),
                    modifier = Modifier.heightIn(40.dp, 40.dp),
                    contentPadding = PaddingValues(12.dp, 2.dp),
                ) { Text("Save Preset") }
                TextButton(
                    onClick = { loadDialog = true },
                    enabled = presets.any { it.formatString.isNotBlank() },
                    modifier = Modifier.heightIn(40.dp, 40.dp),
                    contentPadding = PaddingValues(12.dp, 2.dp),
                ) { Text("Load Preset") }
            }
        }

        // Preview
        if (!twoColumnTabs) {
            Text(
                text = "Preview:",
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 30.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .background(LocalCustomColors.current.whiteBlack, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) { Text(formatPreview, minLines = 6, maxLines = 6, fontSize = 14.sp) }
        }

        // Formatting Guide
        Text(
            text = "Formatting Guide",
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold,
        )

        // Formatting Options
        key(selectionKey) { SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
                .onFocusChanged { updateSelectionFocused(it.isFocused) }
        ) { FormattingGuide() } }

        var expanded by remember { mutableStateOf(false) }

        AnimatedVisibility (
            visible = expanded,
            enter = expandVertically(tween(250), Alignment.Top) + fadeIn(tween(250)),
            exit = shrinkVertically(tween(250), Alignment.Top) + fadeOut(tween(250))
        ) {
            Column {
                Text(
                    text = "Formatting Help",
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                FormattingHelp()
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(horizontal = 12.dp)
                .clickable(null, LocalIndication.current) { expanded = !expanded }
        ) {
            HorizontalDivider(Modifier.weight(1f), 1.dp)
            Text(
                text = if (!expanded) "Click for Formatting Help" else "Click to Hide",
                fontSize = 14.sp,
                color = LocalContentColor.current.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(Modifier.weight(1f), 1.dp)
        }

        Spacer(Modifier.height(24.dp))

        if (saveDialog) {
            SaveDialog(
                savedPresets = presets,
                formatString = formatString,
                delimiter = delimiter,
                onSaveConfirm = { slot, string, delimiter -> viewModel.savePreset(slot, string, delimiter) },
                onDeleteConfirm = { viewModel.savePreset(it, "", "") },
                onSaveCancel = { saveDialog = false },
            )
        }
        if (loadDialog) {
            LoadDialog(
                savedPresets = presets,
                formatString = formatString,
                delimiter = delimiter,
                onLoadConfirm = { string, delimiter -> viewModel.saveFormatting(format = string, delimiter = delimiter) },
                onDeleteConfirm = { viewModel.savePreset(it, "", "") },
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
        modifier = modifier.fillMaxWidth(),
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
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier
                    .width(IntrinsicSize.Min)
                    .padding(end = 8.dp)) {
                    firstHalf.forEach {
                        Box(Modifier
                            .fillMaxWidth()
                            .height(height), Alignment.CenterStart) {
                            Text(
                                text = "${it.key}:",
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
                Column {
                    firstHalf.forEach {
                        Box(Modifier.height(height), Alignment.CenterStart) {
                            Text(
                                text = it.value,
                                modifier = Modifier,
                                style = TextStyle(color = LocalContentColor.current),
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
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier
                    .width(IntrinsicSize.Min)
                    .padding(end = 8.dp)) {
                    secondHalf.forEach {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "${it.key}:",
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
                Column {
                    secondHalf.forEach {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = it.value,
                                style = TextStyle(color = LocalContentColor.current),
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
private fun FormattingHelp(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Anything typed in the format string will show in the text. To reference " +
                    "specific fields, use the placeholders above. Setting the \"List as\" to " +
                    "tins will explode the tins into individual entries in the text list. The " +
                    "delimiter separates entries and is automatically removed from the end.",
        )
        Text(
            text = "Use the delimiter line for how to separate records in the generated string. " +
                    "Anything typed here will show up in-between each record. So, to separate " +
                    "each record by a blank line, you would need to enter \"_n_\". When tins " +
                    "are passed as a sublist, mark the start of the tins sublist delimiter with " +
                    "a tilde (~) at the end of the tins-sublist formatting, inside the closing " +
                    "tins as sublist bracket (e.g.: {@label~, } or {@label~_n_}.",
        )
        Text(
            text = "The \"@rating_0_0\" tag is to be used in a specific way. The first zero should " +
                    "be replaced with the desired max rating (for scaling). The second \"_0\" is " +
                    "optional for the number of decimal places to be rounded to (max of 2, enter " +
                    "0 to round to the nearest whole number). For example, to pass the rating on " +
                    "a scale of 1-4 with whole number rounding, enter \"@rating_4_0\" into the " +
                    "formatting. More advanced examples might be:\n" +
                    "\"[@rating_10_0 stars]\" (of 10, whole number) or \"[@rating_4_2/4]\" (of 4, " +
                    "two places).",
            modifier = Modifier
        )
        Text(
            text = "\"Number\" is a special tag that counts each record in the given sort order " +
                    "(use multiple # to include leading 0's).",
        )
        Text(
            text = "In order to output raw text rather than special characters, escape the " +
                    "special character with the escape character. For example, to output # in the " +
                    "string, enter: '#. Likewise for example, to output brackets around a field, " +
                    "escape the first bracket (e.g. '[@type]). The escape character itself doesn't " +
                    "need to be escaped unless you're trying to use it before an escapable " +
                    "character (e.g. to render: '01' you would need to input ''##').",
        )
        Text(
            text = "Use the square brackets ([ ]) when you conditionally want the text within them " +
                    "to appear only if one or more placeholders (also inside the brackets) are " +
                    "found. For instance, if you want the type shown on a new line, but " +
                    "don't want an extra line for a blank type, enter: [_n_@type]. These " +
                    "conditionals can also be nested (e.g. [ @type[ - @subgenre]]).",
        )
        Text(
            text = "When sorting by items, if you want the tins organized as a sublist " +
                    "per each item, use the curly braces around the formatting you want for " +
                    "tins (e.g. {@label (@T_qty)~, }). Conditional brackets can also be used " +
                    "inside the curly braces.",
        )
        Text(
            text = "To set a delimiter for tins as a sublist, at the very end of the tin line " +
                    "formatting, still inside the tins as sublist brackets, place a tilde (~) " +
                    "just before the desired delimiter, followed by delimiter. For example, to " +
                    "separate each tin in the sublist by a new line, enter: {@label~_n_}.",
        )
        Text(
            text = "A more advanced example might be to pass the list of tins only if tins exist " +
                    "for that blend and passing the quantity in brackets. For example, entering...",
        )
        Text(
            text = "@brand - \"@blend\"[_n_{    - @label '[@T_qty']~_n_}]",
            fontSize = 14.sp,
        )
        Text(
            text = "... would result in:",
        )
        Box {
            Text(
                text = "Lane Limited - \"Very Cherry\"\n        - Lot 1 [2 oz]\n        - Lot 2 [50 grams]",
                fontSize = 14.sp,
            )
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
    var fontSize by remember { mutableFloatStateOf(savedFontSize) }
    var margins by remember { mutableDoubleStateOf(savedMargin) }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
    val decimalSeparator = symbols.decimalSeparator.toString()

    val focusManager = LocalFocusManager.current
    DisposableEffect(Unit) {
        onDispose { focusManager.clearFocus() }
    }

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
                        var fontSizeString by remember { mutableStateOf(fontSize.toInt().toString()) }
                        var marginsString by remember { mutableStateOf(formatDecimal(margins)) }

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
    var selectedSlot by remember { mutableIntStateOf(-1) }
    var confirmDelete by remember { mutableStateOf(false) }
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
                                width = if (isSelected) 1.dp else Dp.Hairline,
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
    var selectedSlot by remember { mutableIntStateOf(-1) }
    var confirmDelete by remember { mutableStateOf(false) }
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
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
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
                                width = if (isSelected) 1.dp else Dp.Hairline,
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