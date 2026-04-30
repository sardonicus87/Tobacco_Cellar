package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ItemInputForm(
    twoColumnTabs: () -> Boolean,
    selectedTabIndex: () -> Int,
    currentLeftTab: () -> Int,
    updateSelectedTab: (Int) -> Unit,
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    componentUiState: ComponentList,
    flavoringUiState: FlavoringList,
    tinDetailsList: List<TinDetails>,
    tabErrorState: TabErrorState,
    syncedTins: Int,
    isEditEntry: Boolean,
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
    onValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    onComponentChange: (String) -> Unit,
    onFlavoringChange: (String) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    showRatingPop: Boolean,
    onShowRatingPop: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var tooltipVisible by remember { mutableStateOf(false) }
    var textFieldFocused by remember { mutableStateOf(false) }
    val fieldFocused: (Boolean) -> Unit = { textFieldFocused = it }
    val twoColumn = twoColumnTabs()
    val currentLeftTab = currentLeftTab()
    val selectedTabIndex = remember(selectedTabIndex()) { selectedTabIndex().coerceIn(0, 2) }

    val largePagerState = rememberPagerState(initialPage = currentLeftTab()) { 2 }
    val narrowPagerState = rememberPagerState(initialPage = selectedTabIndex()) { 3 }

    val fieldInteractionSource = remember { MutableInteractionSource() }
    val unfocusedFieldScroll by fieldInteractionSource.collectIsDraggedAsState()

    if (twoColumn) {
        LaunchedEffect(largePagerState.currentPage) {
            if (largePagerState.currentPage == largePagerState.targetPage) {
                if (largePagerState.currentPage != currentLeftTab()) {
                    updateSelectedTab(largePagerState.currentPage)
                }
            }
        }
        LaunchedEffect(currentLeftTab()) {
            if (largePagerState.currentPage != currentLeftTab()) {
                largePagerState.animateScrollToPage(currentLeftTab())
            }
        }
    } else {
        LaunchedEffect(narrowPagerState.currentPage) {
            if (narrowPagerState.currentPage == narrowPagerState.targetPage) {
                if (narrowPagerState.currentPage != selectedTabIndex()) {
                    updateSelectedTab(narrowPagerState.currentPage)
                }
            }
        }
        LaunchedEffect(selectedTabIndex()) {
            if (narrowPagerState.currentPage != selectedTabIndex()) {
                narrowPagerState.animateScrollToPage(selectedTabIndex())
            }
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { fieldFocused(it.hasFocus) },
        verticalArrangement = Arrangement.Top
    ) {
        AdaptiveTabRow(
            twoColumnTabs = twoColumn,
            selectedTabIndex = selectedTabIndex,
            tabErrorState = tabErrorState,
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
                        userScrollEnabled = !textFieldFocused,
                        verticalAlignment = Alignment.Top
                    ) { targetIndex ->
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState(), !tooltipVisible)
                                .onFocusChanged {
                                    if (it.hasFocus && selectedTabIndex == 2) updateSelectedTab(
                                        currentLeftTab
                                    )
                                }
                                .pointerInput(currentLeftTab, selectedTabIndex) {
                                    if (selectedTabIndex() == 2) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event =
                                                    awaitPointerEvent(pass = PointerEventPass.Initial)
                                                if (event.changes.any { it.changedToDown() }) {
                                                    updateSelectedTab(currentLeftTab)
                                                }
                                            }
                                        }
                                    }
                                }
                        ) {
                            if (targetIndex == 0) {
                                DetailsEntry(
                                    itemDetails = itemDetails,
                                    itemUiState = itemUiState,
                                    syncedTins = syncedTins,
                                    isEditEntry = isEditEntry,
                                    onValueChange = onValueChange,
                                    componentList = componentUiState,
                                    onComponentChange = onComponentChange,
                                    flavoringList = flavoringUiState,
                                    onFlavoringChange = onFlavoringChange,
                                    showRatingPop = showRatingPop,
                                    onShowRatingPop = onShowRatingPop,
                                    fieldInteractionSource = fieldInteractionSource,
                                    tooltipVisible = { tooltipVisible = it },
                                    modifier = Modifier
                                )
                            } else {
                                NotesEntry(
                                    itemDetails = itemDetails,
                                    onValueChange = onValueChange,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }

                VerticalDivider(
                    thickness = Dp.Hairline,
                    color = DividerDefaults.color.copy(alpha = .5f)
                )

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
                                if (it.hasFocus && selectedTabIndex != 2) updateSelectedTab(
                                    2
                                )
                            }
                            .pointerInput(selectedTabIndex) {
                                if (selectedTabIndex != 2) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event =
                                                awaitPointerEvent(pass = PointerEventPass.Initial)
                                            if (event.changes.any { it.changedToDown() }) {
                                                updateSelectedTab(2)
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        TinsEntry(
                            tinDetailsList = tinDetailsList,
                            onTinValueChange = onTinValueChange,
                            isTinLabelValid = isTinLabelValid,
                            addTin = addTin,
                            removeTin = removeTin,
                            itemUiState = itemUiState,
                            validateDates = validateDates,
                            modifier = Modifier
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else {
            BackHandler(narrowPagerState.currentPage != 0 && !textFieldFocused) {
                updateSelectedTab(0)
            }

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
                                    itemUiState = itemUiState,
                                    syncedTins = syncedTins,
                                    isEditEntry = isEditEntry,
                                    onValueChange = onValueChange,
                                    componentList = componentUiState,
                                    onComponentChange = onComponentChange,
                                    flavoringList = flavoringUiState,
                                    onFlavoringChange = onFlavoringChange,
                                    showRatingPop = showRatingPop,
                                    onShowRatingPop = onShowRatingPop,
                                    fieldInteractionSource = fieldInteractionSource,
                                    tooltipVisible = { tooltipVisible = it },
                                    modifier = Modifier
                                )

                            1 ->
                                NotesEntry(
                                    itemDetails = itemDetails,
                                    onValueChange = onValueChange,
                                    modifier = Modifier
                                )

                            2 ->
                                TinsEntry(
                                    tinDetailsList = tinDetailsList,
                                    onTinValueChange = onTinValueChange,
                                    isTinLabelValid = isTinLabelValid,
                                    addTin = addTin,
                                    removeTin = removeTin,
                                    itemUiState = itemUiState,
                                    validateDates = validateDates,
                                    fieldInteractionSource = fieldInteractionSource,
                                    modifier = Modifier
                                )

                            else ->
                                DetailsEntry(
                                    itemDetails = itemDetails,
                                    itemUiState = itemUiState,
                                    syncedTins = syncedTins,
                                    isEditEntry = isEditEntry,
                                    onValueChange = onValueChange,
                                    componentList = componentUiState,
                                    onComponentChange = onComponentChange,
                                    flavoringList = flavoringUiState,
                                    onFlavoringChange = onFlavoringChange,
                                    showRatingPop = showRatingPop,
                                    onShowRatingPop = onShowRatingPop,
                                    fieldInteractionSource = fieldInteractionSource,
                                    tooltipVisible = { tooltipVisible = it },
                                    modifier = Modifier
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
    updateSelectedTab: (Int) -> Unit,
) {
    val titles = listOf("Details", "Notes", "Tins")
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val showAdditional = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 2) {
            scope.launch {
                delay(50)
                showAdditional.value = true
            }
        } else {
            scope.launch {
                delay(5)
                showAdditional.value = false
            }
        }
    }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        if (twoColumnTabs) {
            Column {
                Box(Modifier.fillMaxWidth()) {
                    SecondaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier
                            .fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = LocalContentColor.current,
                        indicator = {
                            val offset = if (selectedTabIndex == 2) 3 else selectedTabIndex
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(offset),
                                color = MaterialTheme.colorScheme.inversePrimary
                            )

                            if (showAdditional.value) {
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
                                focusManager.clearFocus()
                                updateSelectedTab(0)
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
                                focusManager.clearFocus()
                                updateSelectedTab(1)
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
                            onClick = {
                                focusManager.clearFocus()
                                updateSelectedTab(2)
                            },
                            text = { },
                            modifier = Modifier
                                .background(
                                    if (selectedTabIndex == 2) MaterialTheme.colorScheme.background
                                    else LocalCustomColors.current.backgroundUnselected
                                )
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = {
                                focusManager.clearFocus()
                                updateSelectedTab(2)
                            },
                            text = { },
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
                                focusManager.clearFocus()
                                updateSelectedTab(2)
                            },
                            text = {
                                Text(
                                    text = titles[2],
                                    fontWeight =
                                        if (selectedTabIndex == 2) FontWeight.Bold
                                        else FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                )
                            },
                            modifier = Modifier,
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
                        modifier = Modifier
                            .tabIndicatorOffset(selectedTabIndex),
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
                            focusManager.clearFocus()
                            updateSelectedTab(index)
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