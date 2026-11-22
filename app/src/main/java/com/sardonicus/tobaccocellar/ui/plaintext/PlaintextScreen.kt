package com.sardonicus.tobaccocellar.ui.plaintext

import android.content.Context
import android.print.PrintManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize.Companion.StepBased
import androidx.compose.foundation.text.contextmenu.data.TextContextMenuKeys.CopyKey
import androidx.compose.foundation.text.contextmenu.modifier.filterTextContextMenuComponents
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import com.sardonicus.tobaccocellar.ui.utilities.localizedToDouble
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.text.DecimalFormatSymbols
import kotlin.time.Duration.Companion.milliseconds

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
                            focusManager.clearFocus(); down.consume()
                        }

                        selectionFocused -> {
                            selectionKey++; down.consume()
                        }

                        anyFocused -> {
                            focusManager.clearFocus(); if (isOtherTap) down.consume()
                        }
                    }
                }
            },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.plaintext_title_v2),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateUp = {
                    if (selectionFocused) { selectionKey++ } else { focusManager.clearFocus() }
                    onNavigateUp()
                },
                showMenu = false
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

    val listScroll = rememberScrollState()
    val formatScroll = rememberScrollState()
    var listShadow by remember { mutableStateOf(listScroll.canScrollBackward) }
    var formatShadow by remember { mutableStateOf(formatScroll.canScrollBackward) }
    SideEffect(listScroll.canScrollBackward) { listShadow = listScroll.canScrollBackward }
    SideEffect(formatScroll.canScrollBackward) { formatShadow = formatScroll.canScrollBackward }

    var helpExpanded by remember { mutableStateOf(false) }

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
                    color = GlowColor(Color.Black.copy(alpha = if (listShadow) 0.3f else 0f)),
                    size = GlowSize(top = 3.dp),
                    modifier = Modifier
                ) {
                    PlaintextList(
                        viewModel = viewModel,
                        filterViewModel = filterViewModel,
                        context = context,
                        scrollState = listScroll,
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
                color = GlowColor(Color.Black.copy(alpha = if (formatShadow) 0.3f else 0f)),
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
            ) {
                PlaintextFormatting(
                    viewModel = viewModel,
                    scrollState = formatScroll,
                    saveLoadBounds = saveLoadBounds,
                    twoColumnTabs = twoColumnTabs,
                    formatString = formatString,
                    delimiter = delimiter,
                    listAs = listAs,
                    selectionKey = selectionKey,
                    updateSelectionFocused = updateSelectionFocused,
                    helpExpanded = helpExpanded,
                    updateHelpExpanded = { helpExpanded = it },
                    modifier = Modifier.padding(horizontal = 12.dp)
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
            listOf(stringResource(R.string.list_v2), stringResource(R.string.format_v2)).forEachIndexed { index, title ->
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
            val shadow = if (targetIndex == 0) listShadow else formatShadow
            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = if (shadow) 0.3f else 0f)),
                size = GlowSize(top = 3.dp)
            ) {
                when (targetIndex) {
                    0 ->
                        PlaintextList(
                            viewModel = viewModel,
                            filterViewModel = filterViewModel,
                            context = context,
                            scrollState = listScroll,
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
                            scrollState = formatScroll,
                            saveLoadBounds = saveLoadBounds,
                            twoColumnTabs = twoColumnTabs,
                            formatString = formatString,
                            delimiter = delimiter,
                            listAs = listAs,
                            selectionKey = selectionKey,
                            updateSelectionFocused = updateSelectionFocused,
                            helpExpanded = helpExpanded,
                            updateHelpExpanded = { helpExpanded = it },
                            fieldInteractionSource = fieldInteractionSource,
                            modifier = Modifier.fillMaxWidth()
                        )
                    else ->
                        PlaintextList(
                            viewModel = viewModel,
                            filterViewModel = filterViewModel,
                            context = context,
                            scrollState = listScroll,
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
            onPrint = { font, margin, confirm ->
                savePrintOptions(font, margin)
                if (confirm) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    printManager?.print("Plaintext Output", PrintHelper("Plaintext Output", plainList, font, margin), null)
                }
                printDialog = false
            }
        )
    }
}


@Composable
private fun PlaintextList(
    viewModel: PlaintextViewModel,
    filterViewModel: FilterViewModel,
    context: Context,
    scrollState: ScrollState,
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
    val loading by viewModel.loading.collectAsState()
    var showLoading by remember { mutableStateOf(true) }

    LaunchedEffect(loading) { if (!loading) { delay(25.milliseconds); showLoading = false } }

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
                    .padding(horizontal = 12.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                if (formatString.isBlank()) {
                    Text(
                        text = stringResource(R.string.no_format_set_v2),
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
                                    .padding(top = 6.dp, bottom = 32.dp),
                                style = LocalTextStyle.current.copy(
                                    textDirection = if (LanguageState.isRtl) TextDirection.Rtl else TextDirection.Ltr
                                )
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
                    .padding(top = 8.dp, end = 8.dp)
            )
        }
    }
}


@Composable
private fun PlaintextFormatting(
    viewModel: PlaintextViewModel,
    scrollState: ScrollState,
    saveLoadBounds: (LayoutCoordinates) -> Unit,
    twoColumnTabs: Boolean,
    formatString: String,
    delimiter: String,
    listAs: Boolean,
    selectionKey: Int,
    updateSelectionFocused: (Boolean) -> Unit,
    helpExpanded: Boolean,
    updateHelpExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    fieldInteractionSource: MutableInteractionSource? = null
) {
    val formatPreview by viewModel.formatPreview.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val tinsEnabled by viewModel.listAsTinsEnabled.collectAsState()

    SideEffect(tinsEnabled) { if (!tinsEnabled) { viewModel.saveFormatting(listAsTins = false) } }

    var saveDialog by remember { mutableStateOf(false) }
    var loadDialog by remember { mutableStateOf(false) }

    val rtlDialogSeen by viewModel.rtlDialogSeen.collectAsState()
    var showRtlDialog by remember { mutableStateOf(false) }
    if (showRtlDialog) {
        AlertDialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            confirmButton = {
                TextButton(onClick = { viewModel.saveRtlDialogSeen(); showRtlDialog = false }) {
                    Text(stringResource(R.string.ok_v2))
                }
            },
            title = { },
            text = { Text(stringResource(R.string.rtl_user_dialog_v2)) },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            textContentColor = MaterialTheme.colorScheme.onBackground
        )
    }

    Column(modifier
        .verticalScroll(scrollState)
        .padding(horizontal = 12.dp)
        .imePadding()
    ) {
        if (twoColumnTabs) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) { Text(stringResource(R.string.format_output_v2), fontWeight = FontWeight.SemiBold) }
        } else {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.format_output_v2),
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
        }

        val isRtl = LanguageState.isRtl
        val label = if (isRtl) 2 else 1
        val field = if (isRtl) 1 else 2

        Grid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            config = {
                if (isRtl) { columns(GridTrackSize.MinMax(0.dp, .98.fr), GridTrackSize.MinMax(78.dp, .02.fr)) }
                else { columns(GridTrackSize.MinMax(78.dp, .02.fr), GridTrackSize.MinMax(0.dp, .98.fr)) }
                rows(GridTrackSize.Auto, GridTrackSize.Auto, GridTrackSize.Auto)
                gap(8.dp)
            }
        ) {
            Text(
                text = stringResource(R.string.pt_string_v2),
                modifier = Modifier.gridItem(1, label, 1, 1, Alignment.CenterStart),
                autoSize = StepBased(14.sp, 16.sp),
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.pt_delimiter_v2),
                modifier = Modifier.gridItem(2, label, 1, 1, Alignment.CenterStart),
                autoSize = StepBased(14.sp, 16.sp),
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.pt_list_as_v2),
                modifier = Modifier.gridItem(3, label, 1, 1, Alignment.CenterStart),
                autoSize = StepBased(14.sp, 16.sp),
                maxLines = 1
            )

            // format string
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TextField(
                    value = formatString,
                    onValueChange = { viewModel.saveFormatting(format = it) },
                    modifier = Modifier
                        .gridItem(1, field, 1, 1, Alignment.CenterStart)
                        .onFocusChanged {
                            if (isRtl && it.isFocused && !rtlDialogSeen) showRtlDialog = true
                        }
                        .fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (formatString.length > 4) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clickable(
                                        null,
                                        LocalIndication.current
                                    ) { viewModel.saveFormatting(format = "") }
                                    .alpha(0.66f)
                                    .size(20.dp)
                                    .focusable(false)
                            )
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = LocalCustomColors.current.textField,
                        unfocusedContainerColor = LocalCustomColors.current.textField,
                        disabledContainerColor = LocalCustomColors.current.textField
                    ),
                    shape = MaterialTheme.shapes.extraSmall,
                    interactionSource = fieldInteractionSource
                )
            }

            // Delimiter
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TextField(
                    value = delimiter,
                    onValueChange = { viewModel.saveFormatting(delimiter = it) },
                    modifier = Modifier
                        .gridItem(2, field, 1, 1, Alignment.CenterStart)
                        .onFocusChanged {
                            if (isRtl && it.isFocused && !rtlDialogSeen) showRtlDialog = true
                        }
                        .fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = LocalCustomColors.current.textField,
                        unfocusedContainerColor = LocalCustomColors.current.textField,
                        disabledContainerColor = LocalCustomColors.current.textField
                    ),
                    shape = MaterialTheme.shapes.extraSmall,
                    interactionSource = fieldInteractionSource
                )
            }

            // list as tins
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                modifier = Modifier
                    .gridItem(3, field, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.entries_v2),
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
                    text = stringResource(R.string.tins_v2),
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
        }
        // save/load preset
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 24.dp)
                .onGloballyPositioned { saveLoadBounds(it) },
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { saveDialog = true },
                enabled = formatString.isNotBlank(),
                modifier = Modifier.heightIn(40.dp, 40.dp),
                contentPadding = PaddingValues(12.dp, 2.dp),
            ) { Text(stringResource(R.string.save_preset_v2)) }
            TextButton(
                onClick = { loadDialog = true },
                enabled = presets.any { it.formatString.isNotBlank() },
                modifier = Modifier.heightIn(40.dp, 40.dp),
                contentPadding = PaddingValues(12.dp, 2.dp),
            ) { Text(stringResource(R.string.load_preset_v2)) }
        }

        // Preview
        if (!twoColumnTabs) {
            Text(
                text = stringResource(R.string.preview_v2),
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
            ) {
                Text(
                    text = formatPreview,
                    minLines = 6,
                    maxLines = 6,
                    fontSize = 14.sp,
                    style = LocalTextStyle.current.copy(
                        textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                    )
                )
            }
        }

        // Formatting Guide
        Text(
            text = stringResource(R.string.formatting_placeholders_v2),
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )

        // Formatting Options
        key(selectionKey) { SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
                .onFocusChanged { updateSelectionFocused(it.isFocused) }
                .filterTextContextMenuComponents { it.key == CopyKey }
        ) { PlaceholderGuide() } }

        AnimatedVisibility (
            visible = helpExpanded,
            enter = expandVertically(tween(250), Alignment.Top) + fadeIn(tween(250)),
            exit = shrinkVertically(tween(250), Alignment.Top) + fadeOut(tween(250))
        ) {
            Column {
                Text(
                    text = stringResource(R.string.formatting_help_v2),
                    modifier = Modifier.padding(bottom = 8.dp, top = 24.dp),
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
                .clickable(null, LocalIndication.current) { updateHelpExpanded(!helpExpanded) }
        ) {
            HorizontalDivider(Modifier.weight(1f), 1.dp)
            Text(
                text = if (!helpExpanded) stringResource(R.string.click_for_help_v2) else stringResource(R.string.click_hide_v2),
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
                onSaveCancel = { saveDialog = false }
            )
        }
        if (loadDialog) {
            LoadDialog(
                savedPresets = presets,
                formatString = formatString,
                delimiter = delimiter,
                onLoadConfirm = { string, delimiter -> viewModel.saveFormatting(format = string, delimiter = delimiter) },
                onDeleteConfirm = { viewModel.savePreset(it, "", "") },
                onLoadCancel = { loadDialog = false }
            )
        }
    }
}

@Composable
private fun PlaceholderGuide(modifier: Modifier = Modifier) {
    val formatGuide = mapOf(
        stringResource(R.string.brand_label_v2) to "@brand",
        stringResource(R.string.blend_label_v2) to "@blend",
        stringResource(R.string.type_label_v2) to "@type",
        stringResource(R.string.subgenre_label_v2) to "@subgenre",
        stringResource(R.string.cut_label_v2) to "@cut",
        stringResource(R.string.components_label_v2) to "@comps",
        stringResource(R.string.flavoring_label_v2) to "@flavors",
        stringResource(R.string.quantity_label_v2) to "@qty",
        stringResource(R.string.rating_label_v2) to "@rating_0_0",
        stringResource(R.string.production_label_v2) to "@prod",
        stringResource(R.string.tin_label_label_v2) to "@label",
        stringResource(R.string.tin_container_label_v2) to "@container",
        stringResource(R.string.tin_quantity_label_v2) to "@T_qty",
        stringResource(R.string.manufacture_label_v2) to "@manufacture",
        stringResource(R.string.cellar_date_label_v2) to "@cellar",
        stringResource(R.string.open_date_label_v2) to "@open",
        stringResource(R.string.finished_label_v2) to "@finished",
        stringResource(R.string.new_line_v2) to "_n_",
        stringResource(R.string.number_v2) to "#",
        stringResource(R.string.escape_char_v2) to "'",
        stringResource(R.string.conditional_v2) to "[...]",
        stringResource(R.string.tin_sublist_v2) to "{...}",
        stringResource(R.string.sublist_delim_v2) to "~"
    )

    val density = LocalDensity.current
    val rowHeight = with(density) { 24.sp.toDp() }
    val textMeasure = rememberTextMeasurer()
    val textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    val idealWidths = remember(formatGuide) {
        formatGuide.map { (key, value) ->
            key to (textMeasure.measure(key.trim(), textStyle).size.width to
                    textMeasure.measure(value, textStyle).size.width) }.toMap() }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columnCount = when {
            this.maxWidth >= 1000.dp -> 5
            this.maxWidth >= 800.dp -> 4
            this.maxWidth >= 600.dp -> 3
            else -> 2
        }

        val itemsPerColumn = (formatGuide.size + columnCount - 1) / columnCount
        val chunks = formatGuide.entries.chunked(itemsPerColumn)
        val rowCount = chunks.maxOf { it.size }

        val fixedSpacers = (8.dp * columnCount) + (36.dp * (columnCount - 1))
        val availableWidth = this.maxWidth - fixedSpacers

        val chunkIdeals = remember(chunks, idealWidths) {
            chunks.map { chunk ->
                val maxKey = chunk.maxOf { idealWidths[it.key]!!.first }
                val maxValue = chunk.maxOf { idealWidths[it.key]!!.second }
                maxKey to maxValue
            }
        }
        val totalIdeal = with(density) { chunkIdeals.sumOf { (k, v) -> k + v }.toDp() }
        val scale = (availableWidth / totalIdeal).coerceAtMost(1f)

        val isRtl = LanguageState.isRtl

        Grid (
            modifier = Modifier.fillMaxWidth(),
            config = {
                val columnSpecs = {
                    val specs = mutableListOf<GridTrackSize>()
                    specs.add(GridTrackSize.MinMax(0.dp, .25.fr)) // outer flex spacer
                    for (i in 0 until columnCount) {
                        specs.add(GridTrackSize.Auto) // Key
                        specs.add(GridTrackSize.Fixed(8.dp)) // key-value fixed padding
                        specs.add(GridTrackSize.Auto) // Value
                        if (i < columnCount - 1) { specs.add(GridTrackSize.MinMax(36.dp, .5.fr)) } // between chunk flex spacer
                    }
                    specs.add(GridTrackSize.MinMax(0.dp, .25.fr)) // outer flex spacer
                    specs.toTypedArray()
                }
                val rowSpecs = { Array(rowCount) { GridTrackSize.Auto } }

                columns(*columnSpecs())
                rows(*rowSpecs())
                gap(1.dp, 0.dp)
            }
        ) {
            Spacer(Modifier
                .gridItem(1, 1, rowCount, 1, Alignment.CenterStart)
                .fillMaxSize())

            val reChunk = if (isRtl) chunks.reversed() else chunks
            reChunk.forEachIndexed { columnIndex, chunk ->
                val (idealKey, idealValue) = chunkIdeals[columnIndex]
                val maxKeyWidth = with(density) { (idealKey.toDp() * scale) }
                val maxValueWidth = with(density) { (idealValue.toDp() * scale) }

                chunk.forEachIndexed { rowIndex, entry ->
                    val column = (columnIndex * 4) + 2 // +2 b/c indexing 0-based, grid is 1-based,
                    val row = rowIndex + 1             // and 1 column is outer flex spacer

                    val keyColumn = if (isRtl) column + 2 else column
                    val valueColumn = if (isRtl) column else column + 2

                    // KEYS (chunk column 1)
                    Box(
                        modifier = Modifier
                            .gridItem(row, keyColumn, alignment = Alignment.CenterStart)
                            .height(rowHeight)
                            .widthIn(max = maxKeyWidth),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        DisableSelection { Text(
                            text = entry.key.trim(),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            autoSize = StepBased(10.sp, 14.sp, 0.25.sp)
                        ) }
                    }

                    // VALUES (chunk column 3; column 2 is 8.dp padding)
                    Box(
                        modifier = Modifier
                            .gridItem(row, valueColumn, alignment = Alignment.CenterStart)
                            .height(rowHeight)
                            .widthIn(max = maxValueWidth),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = entry.value,
                            maxLines = 1,
                            autoSize = StepBased(10.sp, 14.sp, 0.25.sp)
                        )
                    }

                    // between chunk divider and flex spacer
                    if (columnIndex < chunks.size - 1) {
                        VerticalDivider(Modifier
                            .gridItem(1, column + 3, rowCount, 1, Alignment.Center)
                            .fillMaxHeight(.9f)
                        )
                    }
                }
            }
            // for total column count, inner count = (columnCount * 4) - 1
            Spacer(Modifier
                .gridItem(1, (columnCount * 4) + 1, rowCount, 1, Alignment.Center)
                .fillMaxSize())
        }
    }
}

@Composable
private fun FormattingHelp(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(stringResource(R.string.formatting_help1_v2))
        Text(stringResource(R.string.formatting_help2_v2, "_n_", "~", "{@label~, }", "{@label~_n_}"))
        Text(stringResource(R.string.formatting_help3_v2, "@rating_0_0", "_0", "0", "@rating_4_0", "@rating_10_0", "@rating_4_2/4"))
        Text(stringResource(R.string.formatting_help4_v2, "#"))
        Text(stringResource(R.string.formatting_help5_v2, "#", " '# ", " '[@type] ", " '01' ", " ''##' "))
        Text(stringResource(R.string.formatting_help6_v2, "[ ]", " [_n_@type] ", " [ @type[ - @subgenre]] "))
        Text(stringResource(R.string.formatting_help7_v2, " {@label (@T_qty)~, } "))
        Text(stringResource(R.string.formatting_help8_v2, "~", " {@label~_n_} "))
        Text(stringResource(R.string.formatting_help9_v2))
        Text("    @brand - \"@blend\"[_n_{    - @label '[@T_qty]~_n_}]", fontSize = 14.sp)
        Text(stringResource(R.string.formatting_help10_v2))
        Box {
            Text("    Lane Limited - \"Very Cherry\"\n            - Lot 1 [2 oz]\n            - Lot 2 [50 grams]",
                fontSize = 14.sp)
        }
    }
}

/** Dialogs **/
@Composable
private fun PrintDialog(
    savedFontSize: Float,
    savedMargin: Double,
    onPrint: (Float, Double, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var fontSize by remember { mutableFloatStateOf(savedFontSize) }
    var margins by remember { mutableDoubleStateOf(savedMargin) }

    val symbols = remember { DecimalFormatSymbols.getInstance(LanguageState.locale) }
    val decimalSeparator = symbols.decimalSeparator.toString()

    val focusManager = LocalFocusManager.current
    DisposableEffect(Unit) { onDispose { focusManager.clearFocus() } }

    AlertDialog(
        onDismissRequest = { onPrint(fontSize, margins, false) },
        confirmButton = { TextButton({ onPrint(fontSize, margins, true) }) { Text(stringResource(R.string.print_v2)) } },
        dismissButton = { TextButton({ onPrint(fontSize, margins, false) }) { Text(stringResource(R.string.cancel_v2)) } },
        title = { Text(stringResource(R.string.print_settings_v2)) },
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
                Text(stringResource(R.string.print_dialog_v2), Modifier.padding(bottom = 8.dp))
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
                        Box(Modifier.weight(1f), Alignment.CenterStart) { Text(stringResource(R.string.font_size_v2)) }
                        Box(Modifier.weight(1f), Alignment.CenterStart) { Text(stringResource(R.string.margins_v2)) }
                    }

                    // Text fields
                    Column(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        val fontPattern = remember { Regex("^(\\s*|\\d{0,2})$") }
                        val marginPattern = remember(decimalSeparator) {
                            val ds = Regex.escape(decimalSeparator)
                            Regex("^(\\s*|(\\d?)?($ds\\d{0,2})?)$")
                        }

                        // font
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val fontState = rememberTextFieldState(savedFontSize.toInt().toString())
                            LaunchedEffect(fontState) {
                                snapshotFlow { fontState.text.toString() }.collectLatest {
                                    fontSize = if (it.isNotBlank()) it.toFloatOrNull() ?: fontSize
                                        else 12f
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                CustomTextField(
                                    state = fontState,
                                    inputTransformation = {
                                        if (!this.toString().matches(fontPattern)) {
                                            revertAllChanges()
                                        }
                                    },
                                    suffix = {
                                        Text(
                                            "pt",
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(0.75.dp),
                                            color = LocalContentColor.current.copy(alpha = .8f)
                                        )
                                    },
                                    modifier = Modifier
                                        .width(68.dp)
                                        .padding(vertical = 4.dp),
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    textStyle = LocalTextStyle.current.copy(
                                        textDirection = TextDirection.Ltr,
                                        textAlign = TextAlign.End
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
                                        disabledContainerColor = LocalCustomColors.current.textField
                                    ),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    contentPadding = PaddingValues(
                                        vertical = 6.dp,
                                        horizontal = 12.dp
                                    ),
                                )
                            }

                            // increase/decrease font buttons
                            IncreaseDecrease(
                                increaseClick = {
                                    if (fontState.text.isEmpty()) {
                                        fontState.edit { replace(0, length, "1") }
                                        fontSize = 1f
                                    } else {
                                        val current = fontState.text.toString().toIntOrNull() ?: 1
                                        val next = (current + 1).coerceAtMost(99)
                                        fontState.edit { replace(0, length, next.toString()) }
                                        fontSize = next.toFloat()
                                    }
                                },
                                decreaseClick = {
                                    if (fontState.text.isEmpty()) {
                                        fontState.edit { replace(0, length, "1") }
                                        fontSize = 1f
                                    } else {
                                        val current = fontState.text.toString().toIntOrNull() ?: 1
                                        val next = (current - 1).coerceAtLeast(1)
                                        fontState.edit { replace(0, length, next.toString()) }
                                        fontSize = next.toFloat()
                                    }
                                },
                                modifier = Modifier.fillMaxHeight()
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
                            val marginState = rememberTextFieldState(formatDecimal(savedMargin))
                            LaunchedEffect(marginState) {
                                snapshotFlow { marginState.text.toString() }.collectLatest {
                                    margins = it.localizedToDouble(1.0, max = 3.0)
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                CustomTextField(
                                    state = marginState,
                                    inputTransformation = {
                                        if (!this.toString().matches(marginPattern)) {
                                            revertAllChanges()
                                        }
                                    },
                                    modifier = Modifier
                                        .width(68.dp)
                                        .padding(vertical = 4.dp)
                                        .onFocusChanged {
                                            if (!it.hasFocus) {
                                                marginState.edit { replace(0, length, formatDecimal(margins)) }
                                            }
                                        },
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    suffix = {
                                        Text(
                                            text = "×",
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(start = 0.75.dp),
                                            color = LocalContentColor.current.copy(alpha = .8f)
                                        )
                                    },
                                    textStyle = LocalTextStyle.current.copy(
                                        textDirection = TextDirection.Ltr,
                                        textAlign = TextAlign.End
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
                                        disabledContainerColor = LocalCustomColors.current.textField
                                    ),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    contentPadding = PaddingValues(
                                        vertical = 6.dp,
                                        horizontal = 12.dp
                                    )
                                )
                            }

                            // increase/decrease margin buttons
                            IncreaseDecrease(
                                increaseClick = {
                                    if (marginState.text.isEmpty()) {
                                        margins = 0.25
                                        marginState.edit { replace(0, length, "0.25") }
                                    } else {
                                        val current = marginState.text.toString().localizedToDouble(1.0, max = 3.0)
                                        val next = (current + 0.25).coerceAtMost(3.0)
                                        margins = next
                                        marginState.edit { replace(0, length, formatDecimal(next)) }
                                    }
                                },
                                decreaseClick = {
                                    if (marginState.text.isEmpty()) {
                                        margins = 0.0
                                        marginState.edit { replace(0, length, "0") }
                                    } else {
                                        val current = marginState.text.toString().localizedToDouble(1.0, max = 3.0)
                                        val next = (current -0.25).coerceAtLeast(0.0)
                                        margins = next
                                        marginState.edit { replace(0, length, formatDecimal(next)) }
                                    }
                                },
                                modifier = Modifier.fillMaxHeight()
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
                onClick = { onSaveConfirm(selectedSlot, formatString, delimiter); onSaveCancel() },
                enabled = selectedSlot != -1
            ) { Text(stringResource(R.string.save_v2)) }
        },
        dismissButton = { TextButton({ onSaveCancel() }) { Text(stringResource(R.string.cancel_v2)) } },
        title = { Text(stringResource(R.string.save_preset_v2)) },
        modifier = modifier
            .fillMaxWidth(.9f)
            .clickable(null, null) { selectedSlot = -1 },
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
                                if (isSelected) selectedColor else LocalCustomColors.current.darkNeutral,
                                RoundedCornerShape(4.dp)
                            )
                            .combinedClickable(
                                null, null,
                                onClick = { selectedSlot = if (isSelected) -1 else it },
                                onLongClick = {
                                    if (preset.formatString.isNotBlank()) {
                                        selectedSlot = it; onConfirm(true)
                                    }
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box (
                            modifier = Modifier.weight(1f),
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
                                    modifier = Modifier.weight(1f),
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
                                        text = preset.delimiter.ifBlank{ stringResource(R.string.n_a_v2) },
                                        modifier = Modifier
                                            .width(44.dp)
                                            .padding(start = 2.dp),
                                        color = if (preset.delimiter.isBlank()) color2 else color,
                                        maxLines = 1,
                                        fontStyle = if (preset.delimiter.isBlank()) FontStyle.Italic else FontStyle.Normal,
                                        textAlign = if (preset.delimiter.isBlank()) TextAlign.Center else TextAlign.Start
                                    )
                                }
                            }
                            if (isSelected && preset.formatString.isNotBlank()) {
                                Text(
                                    text = stringResource(R.string.overwrite_q_v2),
                                    modifier = Modifier.matchParentSize(),
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
                TextButton({ onDeleteConfirm(selectedSlot); selectedSlot = -1; onConfirm(false) }
                ) { Text(stringResource(R.string.delete_v2)) }
            },
            dismissButton = { TextButton({ onConfirm(false) }) { Text(stringResource(R.string.cancel_v2)) } },
            title = { Text(stringResource(R.string.delete_preset_v2)) },
            text = { Text(stringResource(R.string.delete_preset_dialog_v2, "${selectedSlot + 1}")) },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            textContentColor = MaterialTheme.colorScheme.onBackground
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
                    onLoadCancel() },
                enabled = selectedSlot != -1
            ) { Text(stringResource(R.string.load_v2)) }
        },
        dismissButton = { TextButton({ onLoadCancel() }) { Text(stringResource(R.string.cancel_v2)) } },
        title = { Text(stringResource(R.string.load_preset_v2)) },
        modifier = modifier
            .fillMaxWidth(.9f)
            .clickable(null, null) { selectedSlot = -1 },
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
                                null, null, preset.formatString.isNotBlank(),
                                onClick = { selectedSlot = if (isSelected) -1 else it },
                                onLongClick = { selectedSlot = it; onConfirm(true) }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${it + 1}:",
                            modifier = Modifier.padding(end = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (disabled) LocalContentColor.current.copy(alpha = .38f) else LocalContentColor.current
                        )
                        Text(
                            text = preset.formatString,
                            modifier = Modifier.weight(1f),
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
                                text = preset.delimiter.ifBlank{ stringResource(R.string.n_a_v2) },
                                modifier = Modifier
                                    .width(44.dp)
                                    .padding(start = 2.dp),
                                color = if (preset.delimiter.isBlank()) LocalContentColor.current.copy(alpha = .5f) else LocalContentColor.current,
                                maxLines = 1,
                                fontStyle = if (preset.delimiter.isBlank()) FontStyle.Italic else FontStyle.Normal,
                                textAlign = if (preset.delimiter.isBlank()) TextAlign.Center else TextAlign.Start
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
                TextButton({ onDeleteConfirm(selectedSlot); selectedSlot = -1; onConfirm(false) }) {
                    Text(stringResource(R.string.delete_v2)) }
            },
            dismissButton = { TextButton({ onConfirm(false) }) { Text(stringResource(R.string.cancel_v2)) } },
            title = { Text(stringResource(R.string.delete_preset_v2)) },
            text = { Text(stringResource(R.string.delete_preset_dialog_v2, "${selectedSlot + 1}")) },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            textContentColor = MaterialTheme.colorScheme.onBackground
        )
    }
}