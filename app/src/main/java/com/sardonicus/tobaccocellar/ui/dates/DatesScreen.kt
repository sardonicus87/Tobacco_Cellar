package com.sardonicus.tobaccocellar.ui.dates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarBottomAppBar
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.navigation.DatesDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatesScreen(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    navigateToStats: () -> Unit,
    navigateToAddEntry: () -> Unit,
    navigateToDetails: (Int) -> Unit,
    isTwoPane: Boolean,
    viewModel: DatesViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val datesUiState by viewModel.datesUiState.collectAsState()
    val selectionFocused by viewModel.selectionFocused.collectAsState()
    val selectionKey by viewModel.selectionKey.collectAsState()

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
            .clickable(indication = null, interactionSource = null) { viewModel.resetSelection() },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.dates_title),
                scrollBehavior = scrollBehavior,
                showMenu = false,
                canNavigateBack = false,
                modifier = Modifier,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier
                    .padding(0.dp),
                navigateToHome = { viewModel.resetSelection(); navigateToHome() },
                navigateToStats = { viewModel.resetSelection(); navigateToStats() },
                navigateToAddEntry = { viewModel.resetSelection(); navigateToAddEntry() },
                currentDestination = DatesDestination,
                isTwoPane = isTwoPane
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DatesBody(
                navigateToDetails = navigateToDetails,
                datesUiState = datesUiState,
                selectionKey = { selectionKey },
                updateSelectionFocused = viewModel::updateFocused,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun DatesBody(
    navigateToDetails: (Int) -> Unit,
    datesUiState: DatesUiState,
    selectionKey: () -> Int,
    updateSelectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val separatorColor = colorScheme.secondary

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (datesUiState.loading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // disclaimer
                item {
                    Text(
                        text = "*Excluding \"Aging Tracker\", all date information on this screen is filter-reactive.",
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(.9f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = LocalContentColor.current.copy(alpha = .75f),
                    )
                }

                // Aging Tracker
                item {
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = LocalCustomColors.current.backgroundVariant)
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Aging Tracker",
                                modifier = Modifier
                                    .padding(start = 8.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Start,
                                color = colorScheme.onBackground
                            )
                        }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .drawBehind {
                                    val strokeWidth = Dp.Hairline.toPx()
                                    val yOffset = size.height - strokeWidth / 2

                                    drawLine(
                                        color = separatorColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, strokeWidth),
                                        strokeWidth = strokeWidth
                                    )

                                    drawLine(
                                        color = separatorColor,
                                        start = Offset(0f, yOffset),
                                        end = Offset(size.width, yOffset),
                                        strokeWidth = strokeWidth
                                    )
                                }
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        if (datesUiState.agingExists) {
                            datesUiState.agingSection.forEach {
                                if (it.agingDue.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = it.exists,
                                        modifier = Modifier
                                            .padding(bottom = 1.dp),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    AgingSection(
                                        items = it.agingDue,
                                        navigateToDetails = navigateToDetails,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        showTime = true
                                    )
                                }
                                else {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = it.empty,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Start,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        } else {
                            Text(
                                text = "No tins coming of age this week or month.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }


                if (datesUiState.datesExist) {
                    // Quick Stats
                    item {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = LocalCustomColors.current.backgroundVariant)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Quick Date Stats",
                                    modifier = Modifier
                                        .padding(start = 8.dp),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Start,
                                    color = colorScheme.onBackground
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .drawBehind {
                                        val strokeWidth = Dp.Hairline.toPx()
                                        val yOffset = size.height - strokeWidth / 2

                                        drawLine(
                                            color = separatorColor,
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, strokeWidth),
                                            strokeWidth = strokeWidth
                                        )

                                        drawLine(
                                            color = separatorColor,
                                            start = Offset(0f, yOffset),
                                            end = Offset(size.width, yOffset),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                            )
                        }
                    }

                    item {
                        QuickStatsSection(
                            averagesExist = { datesUiState.averageAgeExists },
                            averageAgeSection = datesUiState.averageAgeSection,
                            selectionKey = selectionKey,
                            updateSelectionFocused = updateSelectionFocused,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Oldest Tins
                    item {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = LocalCustomColors.current.backgroundVariant)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Oldest Tins",
                                    modifier = Modifier
                                        .padding(start = 8.dp),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Start,
                                    color = colorScheme.onBackground
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .drawBehind {
                                        val strokeWidth = Dp.Hairline.toPx()
                                        val yOffset = size.height - strokeWidth / 2

                                        drawLine(
                                            color = separatorColor,
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, strokeWidth),
                                            strokeWidth = strokeWidth
                                        )

                                        drawLine(
                                            color = separatorColor,
                                            start = Offset(0f, yOffset),
                                            end = Offset(size.width, yOffset),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                            )
                        }
                    }

                    item {
                        OldestTinsSection(
                            oldestExist = { datesUiState.oldestTinsExists },
                            oldestSection = datesUiState.oldestTinsSection,
                            navigateToDetails = navigateToDetails,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    //  Future tins
                    item {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = LocalCustomColors.current.backgroundVariant)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Future Tins",
                                    modifier = Modifier
                                        .padding(start = 8.dp),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Start,
                                    color = colorScheme.onBackground
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .drawBehind {
                                        val strokeWidth = Dp.Hairline.toPx()
                                        val yOffset = size.height - strokeWidth / 2

                                        drawLine(
                                            color = separatorColor,
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, strokeWidth),
                                            strokeWidth = strokeWidth
                                        )

                                        drawLine(
                                            color = separatorColor,
                                            start = Offset(0f, yOffset),
                                            end = Offset(size.width, yOffset),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                            )
                        }
                    }

                    item {
                        FutureTinsSection(
                            futureExists = { datesUiState.futureTinsExists },
                            futureSection = datesUiState.futureTinsSection,
                            navigateToDetails = navigateToDetails,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        HorizontalDivider(
                            Modifier.fillMaxWidth(),
                            Dp.Hairline,
                            colorScheme.secondary
                        )
                    }

                    item {
                        val offset = with(LocalDensity.current) { Dp.Hairline.toPx() / 2 }
                        HorizontalDivider(
                            Modifier
                                .fillMaxWidth()
                                .offset(y = -offset.dp),
                            Dp.Hairline,
                            LocalCustomColors.current.backgroundVariant
                        )
                    }

                    item { Spacer(Modifier.weight(1f)) }

                    item {
                        Text(
                            text = "No date information found within filtered entries.",
                            modifier = Modifier
                                .fillMaxWidth(.75f),
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                        )
                    }

                    item { Spacer(Modifier.weight(1.25f)) }
                }
            }
        }
    }
}


@Composable
private fun AgingSection(
    items: List<DateInfoItem>,
    navigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    showTime: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        items.forEach {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { navigateToDetails(it.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "• ${it.brand}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                    )
                    Text(
                        text = ", ${it.blend}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, false)
                    )
                    Text(
                        text = " - ${it.tinLabel}",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                    )
                    if (showTime) {
                        Text(
                            text = " (${it.time})",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                        )
                    }
                    if (showDate) {
                        Text(
                            text = " (${it.date})",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun QuickStatsSection(
    averagesExist: () -> Boolean,
    averageAgeSection: List<AverageAgeSection>,
    selectionKey: () -> Int,
    updateSelectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
        ) {
            Spacer(Modifier.height(10.dp))
            if (averagesExist()) {
                key(selectionKey()) {
                    SelectionContainer(
                        Modifier.onFocusChanged { updateSelectionFocused(it.isFocused) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start)
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                                    .padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                                horizontalAlignment = Alignment.Start
                            ) {
                                averageAgeSection.forEach {
                                    Text(
                                        text = it.title,
                                        modifier = Modifier,
                                        fontSize = 15.sp,
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(end = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                                horizontalAlignment = Alignment.Start
                            ) {
                                averageAgeSection.forEach {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically

                                    ) {
                                        Text(
                                            text = it.averageAge,
                                            modifier = Modifier,
                                            maxLines = 1,
                                            style = LocalTextStyle.current.copy(
                                                color = LocalContentColor.current
                                            ),
                                            autoSize = TextAutoSize.StepBased(
                                                minFontSize = 13.sp,
                                                maxFontSize = 15.sp,
                                                stepSize = .25.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            } else {
                Text(
                    text = "No relevant date stats found in entries.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                )
            }
        }
    }
}


@Composable
private fun OldestTinsSection(
    oldestExist: () -> Boolean,
    oldestSection: List<OldestTinsSection>,
    navigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        if (oldestExist()) {
            oldestSection.forEach {
                Spacer(Modifier.height(10.dp))
                DatesSection(
                    label = it.title,
                    items = it.oldestTins,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            Spacer(Modifier.height(20.dp))
        } else {
            Text(
                text = "No tin dates found.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FutureTinsSection(
    futureExists: () -> Boolean,
    futureSection: List<FutureTinsSection>,
    navigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        if (futureExists()) {
            Spacer(Modifier.height(10.dp))
            futureSection.forEach {
                DatesSection(
                    label = it.title,
                    items = it.futureTins,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            Text(
                text = "No future tin dates found.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}


@Composable
fun DatesSection(
    label: String,
    items: List<DateInfoItem>,
    navigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    var expandEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(items) {
        expandEnabled = items.isNotEmpty()
        if (items.isEmpty()) {
            expanded = true
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = expandEnabled,
                    indication = LocalIndication.current,
                    interactionSource = null
                ) { expanded = !expanded },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$label Date",
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.tertiary,
                modifier = Modifier
                    .padding(bottom = 1.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.TopEnd
            ) {
                val icon = if (expanded) R.drawable.arrow_up else R.drawable.arrow_down
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Expand/collapse",
                    modifier = Modifier
                        .offset(y = (-2).dp)
                        .size(22.dp),
                    tint = if (expandEnabled) colorScheme.onBackground.copy(alpha = 0.8f) else Color.Transparent
                )
            }
        }
        if (expanded) {
            if (items.isNotEmpty()) {
                items.forEach {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { navigateToDetails(it.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(.9f)
                        ) {
                            Text(
                                text = "• ${it.brand}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                            )
                            Text(
                                text = ", ${it.blend}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, false)
                            )
                            Text(
                                text = " - ${it.tinLabel}",
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "${it.date} (${it.time})",
                                fontSize = 14.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Text(
                    text = "No ${label.lowercase()} dates found.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 10.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    color = colorScheme.primary.copy(alpha= .5f),
                )
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

