package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.ui.AutoCompleteData
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ItemInputForm(
    twoColumnTabs: () -> Boolean,
    itemDetails: ItemDetails,
    tinDetailsList: List<TinDetails>,
    autoComplete: AutoCompleteData,
    tabErrorState: TabErrorState,
    isEditEntry: Boolean,
    onValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    var showRatingPop by remember { mutableStateOf(false) }
    var tooltipVisible by remember { mutableStateOf(false) }
    var textFieldFocused by remember { mutableStateOf(false) }
    val twoColumn = twoColumnTabs()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var currentLeftTab by remember { mutableIntStateOf(0) }
    val updateSelectedTab = remember {
        { it: Int ->
            if (selectedTabIndex != it) {
                selectedTabIndex = it
                if (it < 2) currentLeftTab = it
            }
        }
    }

    val largePagerState = rememberPagerState(initialPage = currentLeftTab) { 2 }
    val narrowPagerState = rememberPagerState(initialPage = selectedTabIndex) { 3 }

    val fieldInteractionSource = remember { MutableInteractionSource() }
    val unfocusedFieldScroll by fieldInteractionSource.collectIsDraggedAsState()

    LaunchedEffect(twoColumn) {
        if (twoColumn) {
            if (largePagerState.currentPage != currentLeftTab) {
                largePagerState.scrollToPage(currentLeftTab)
            }
        } else {
            if (narrowPagerState.currentPage != selectedTabIndex) {
                narrowPagerState.scrollToPage(selectedTabIndex)
            }
        }
    }

    LaunchedEffect(selectedTabIndex, currentLeftTab) {
        if (twoColumn) {
            if (largePagerState.currentPage != currentLeftTab) {
                largePagerState.animateScrollToPage(currentLeftTab)
            }
        } else {
            if (narrowPagerState.currentPage != selectedTabIndex) {
                narrowPagerState.animateScrollToPage(selectedTabIndex)
            }
        }
    }

    val largeDragged by largePagerState.interactionSource.collectIsDraggedAsState()
    val narrowDragged by narrowPagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(largePagerState, narrowPagerState, twoColumn) {
        if (twoColumn) {
            snapshotFlow { largePagerState.currentPage }
                .collect { if (it != currentLeftTab && largeDragged) updateSelectedTab(it) }
        } else {
            snapshotFlow { narrowPagerState.currentPage }
                .collect { if (it != selectedTabIndex && narrowDragged) updateSelectedTab(it) }
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { textFieldFocused = it.hasFocus },
        verticalArrangement = Arrangement.Top
    ) {
        AdaptiveTabRow(
            twoColumnTabs = twoColumn,
            selectedTabIndex = selectedTabIndex,
            tabErrorState = tabErrorState,
            focusManager = focusManager,
            anythingFocused = textFieldFocused || tooltipVisible,
            updateSelectedTab = updateSelectedTab
        )
        if (twoColumn) {
            Row(Modifier.fillMaxHeight()) {
                GlowBox(
                    color = GlowColor(MaterialTheme.colorScheme.background),
                    size = GlowSize(vertical = 3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 1.dp)
                ) {
                    HorizontalPager(
                        state = largePagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !textFieldFocused && !tooltipVisible && !unfocusedFieldScroll,
                        verticalAlignment = Alignment.Top
                    ) { targetIndex ->
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState(), !tooltipVisible)
                                .onFocusChanged {
                                    if (it.hasFocus && selectedTabIndex == 2 && !largePagerState.isScrollInProgress) {
                                        updateSelectedTab(currentLeftTab)
                                    }
                                }
                                .pointerInput(currentLeftTab, selectedTabIndex) {
                                    if (selectedTabIndex == 2) {
                                        awaitEachGesture {
                                            awaitFirstDown(pass = PointerEventPass.Initial)
                                            updateSelectedTab(currentLeftTab)
                                        }
                                    }
                                }
                        ) {
                            if (targetIndex == 0) {
                                DetailsEntry(
                                    itemDetails = itemDetails,
                                    autoComplete = autoComplete,
                                    isEditEntry = isEditEntry,
                                    onValueChange = onValueChange,
                                    showRatingPop = showRatingPop,
                                    onShowRatingPop = { showRatingPop = it },
                                    fieldInteractionSource = fieldInteractionSource,
                                    tooltipVisible = { tooltipVisible = it }
                                )
                            } else {
                                NotesEntry(
                                    itemDetails = itemDetails,
                                    onValueChange = onValueChange
                                )
                            }
                        }
                    }
                }

                VerticalDivider(thickness = Dp.Hairline, color = DividerDefaults.color.copy(alpha = .5f))

                GlowBox(
                    color = GlowColor(MaterialTheme.colorScheme.background),
                    size = GlowSize(vertical = 3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .onFocusChanged {
                                if (it.hasFocus && selectedTabIndex != 2) updateSelectedTab(2)
                            }
                            .pointerInput(selectedTabIndex) {
                                if (selectedTabIndex != 2) {
                                    awaitEachGesture {
                                        awaitFirstDown(pass = PointerEventPass.Initial)
                                        updateSelectedTab(2)
                                    }
                                }
                            }
                    ) {
                        TinsEntry(
                            tinDetailsList = tinDetailsList,
                            onTinValueChange = onTinValueChange,
                            addTin = addTin,
                            removeTin = removeTin,
                            autoComplete = autoComplete
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else {
            BackHandler(narrowPagerState.currentPage != 0 && !textFieldFocused) { updateSelectedTab(0) }

            GlowBox(
                color = GlowColor(MaterialTheme.colorScheme.background),
                size = GlowSize(vertical = 3.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 1.dp)
            ) {
                HorizontalPager(
                    state = narrowPagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !textFieldFocused && !tooltipVisible && !unfocusedFieldScroll,
                    verticalAlignment = Alignment.Top
                ) { targetIndex ->
                    Column(
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState(), !tooltipVisible),
                    ) {
                        when (targetIndex) {
                            0 ->
                                DetailsEntry(
                                    itemDetails = itemDetails,
                                    autoComplete = autoComplete,
                                    isEditEntry = isEditEntry,
                                    onValueChange = onValueChange,
                                    showRatingPop = showRatingPop,
                                    onShowRatingPop = { showRatingPop = it },
                                    fieldInteractionSource = fieldInteractionSource,
                                    tooltipVisible = { tooltipVisible = it }
                                )

                            1 ->
                                NotesEntry(
                                    itemDetails = itemDetails,
                                    onValueChange = onValueChange
                                )

                            2 ->
                                TinsEntry(
                                    tinDetailsList = tinDetailsList,
                                    onTinValueChange = onTinValueChange,
                                    addTin = addTin,
                                    removeTin = removeTin,
                                    autoComplete = autoComplete,
                                    fieldInteractionSource = fieldInteractionSource
                                )

                            else ->
                                DetailsEntry(
                                    itemDetails = itemDetails,
                                    autoComplete = autoComplete,
                                    isEditEntry = isEditEntry,
                                    onValueChange = onValueChange,
                                    showRatingPop = showRatingPop,
                                    onShowRatingPop = { showRatingPop = it },
                                    fieldInteractionSource = fieldInteractionSource,
                                    tooltipVisible = { tooltipVisible = it }
                                )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdaptiveTabRow(
    twoColumnTabs: Boolean,
    selectedTabIndex: Int,
    tabErrorState: TabErrorState,
    focusManager: FocusManager,
    anythingFocused: Boolean,
    updateSelectedTab: (Int) -> Unit,
) {
    val titles = listOf("Details", "Notes", "Tins")
    var showAdditional by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTabIndex) {
        if (twoColumnTabs) {
            if (selectedTabIndex == 2) {
                delay(50.milliseconds)
                showAdditional = true
            } else {
                delay(5.milliseconds)
                showAdditional = false
            }
        }
    }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        if (twoColumnTabs) {
            Column {
                Box(Modifier.fillMaxWidth()) {
                    SecondaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = LocalContentColor.current,
                        indicator = {
                            val offset = if (selectedTabIndex == 2) 3 else selectedTabIndex
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(offset),
                                color = MaterialTheme.colorScheme.inversePrimary
                            )

                            if (showAdditional) {
                                SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(2),
                                    color = MaterialTheme.colorScheme.inversePrimary
                                )
                            }

                        },
                        divider = {
                            HorizontalDivider(
                                modifier = Modifier,
                                thickness = Dp.Hairline,
                                color = DividerDefaults.color,
                            )
                        },
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = {
                                if (anythingFocused) { focusManager.clearFocus() }
                                else updateSelectedTab(0)
                            },
                            text = {
                                Text(
                                    text = titles[0],
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.SemiBold,
                                )
                            },
                            modifier = Modifier
                                .background(
                                    if (selectedTabIndex == 0) MaterialTheme.colorScheme.background
                                    else LocalCustomColors.current.backgroundUnselected
                                ),
                            selectedContentColor = MaterialTheme.colorScheme.onBackground,
                            unselectedContentColor =
                                if (tabErrorState.detailsError) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.outline,
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = {
                                if (anythingFocused) { focusManager.clearFocus() }
                                else updateSelectedTab(1)
                            },
                            text = {
                                Text(
                                    text = titles[1],
                                    fontWeight =
                                        if (selectedTabIndex == 1) FontWeight.Bold
                                        else FontWeight.SemiBold,
                                )
                            },
                            modifier = Modifier
                                .background(
                                    if (selectedTabIndex == 1) MaterialTheme.colorScheme.background
                                    else LocalCustomColors.current.backgroundUnselected
                                ),
                            selectedContentColor = MaterialTheme.colorScheme.onBackground,
                            unselectedContentColor = MaterialTheme.colorScheme.outline,
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { },
                            modifier = Modifier
                                .background(
                                    if (selectedTabIndex == 2) MaterialTheme.colorScheme.background
                                    else LocalCustomColors.current.backgroundUnselected
                                )
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { },
                            modifier = Modifier
                                .background(
                                    if (selectedTabIndex == 2) MaterialTheme.colorScheme.background
                                    else LocalCustomColors.current.backgroundUnselected
                                )
                        )
                    }

                    SecondaryTabRow(
                        selectedTabIndex = if (selectedTabIndex == 2) 0 else -1,
                        modifier = Modifier
                            .fillMaxWidth(.5f)
                            .align(Alignment.CenterEnd),
                        containerColor = Color.Transparent,
                        contentColor = LocalContentColor.current,
                        indicator = { },
                        divider = { }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = {
                                if (anythingFocused) { focusManager.clearFocus() }
                                else updateSelectedTab(2)
                            },
                            text = {
                                Text(
                                    text = titles[2],
                                    fontWeight =
                                        if (selectedTabIndex == 2) FontWeight.Bold
                                        else FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.onBackground,
                            unselectedContentColor =
                                if (tabErrorState.tinsError) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = LocalContentColor.current,
                indicator = {
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                },
                divider = {
                    HorizontalDivider(
                        modifier = Modifier,
                        thickness = Dp.Hairline,
                        color = DividerDefaults.color,
                    )
                },
            ) {
                titles.forEachIndexed { index, title ->
                    val textColor = when (index) {
                        0 ->
                            if (tabErrorState.detailsError) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.outline
                        2 ->
                            if (tabErrorState.tinsError) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.outline
                    }

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            if (anythingFocused) focusManager.clearFocus()
                            else updateSelectedTab(index)
                        },
                        modifier = Modifier
                            .background(
                                if (selectedTabIndex == index) MaterialTheme.colorScheme.background
                                else LocalCustomColors.current.backgroundUnselected
                            ),
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.SemiBold,
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onBackground,
                        unselectedContentColor = textColor,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                }
            }
        }
    }
}