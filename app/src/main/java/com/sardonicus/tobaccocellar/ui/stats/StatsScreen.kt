package com.sardonicus.tobaccocellar.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarBottomAppBar
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.navigation.StatsDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigateToHome: () -> Unit,
    navigateToDates: () -> Unit,
    navigateToAddEntry: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val rawStats by viewmodel.rawStats.collectAsState()
    val filteredStats by viewmodel.filteredStats.collectAsState()

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() },
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.stats_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = false,
                showMenu = false,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier
                    .padding(0.dp),
                navigateToHome = navigateToHome,
                navigateToDates = navigateToDates,
                navigateToAddEntry = navigateToAddEntry,
                currentDestination = StatsDestination,
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
            if (rawStats.rawLoading || filteredStats.filteredLoading) {
                LoadingIndicator()
            } else {
                StatsBody(
                    rawStats = rawStats,
                    filteredStats = filteredStats,
                    viewmodel = viewmodel,
                    modifier = modifier
                        .fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun StatsBody(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    viewmodel: StatsViewModel,
    modifier: Modifier = Modifier,
) {
    val separatorColor = colorScheme.secondary
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val expanded = viewmodel.expanded

    val contract = {
        scope.launch {
            scrollState.scrollTo(0)
            while (scrollState.value > 0) {
                delay(5)
            }
            viewmodel.updateExpanded(false)
            delay(10)
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(1.dp))

        // Quick Stats //
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
                    text = stringResource(R.string.quick_stats),
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

        Spacer(Modifier.height(10.dp))

        QuickStatsSection(
            rawStats = rawStats,
            filteredStats = filteredStats,
            contracted = { if (it) contract() },
            expanded = expanded,
            updateExpanded = { viewmodel.updateExpanded(it) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 24.dp),
        )

        Spacer(Modifier.height(10.dp))

        // Charts //
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = LocalCustomColors.current.backgroundVariant)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Charts",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
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

        ChartsSection(
            filteredStats = filteredStats
        )

    }
}

@Composable
fun QuickStatsSection(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    contracted: (Boolean) -> Unit,
    expanded: Boolean,
    updateExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Raw Stats",
                modifier = Modifier
                    .weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                color = colorScheme.onBackground
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Filtered Stats",
                modifier = Modifier
                    .weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                color = colorScheme.onBackground,
            )
        }
        // First Section basic counts
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            SelectionContainer(Modifier.weight(1f)) {
                Text(
                    text = "${rawStats.itemsCount} blends, ${rawStats.brandsCount} brands\n" +
                            if (rawStats.averageRating.isNotBlank()) { "${rawStats.averageRating} average rating\n" } else { "" } +
                            "${rawStats.favoriteCount} favorites, ${rawStats.dislikedCount} disliked\n" +
                            "${rawStats.totalQuantity} total \"No. of Tins\"\n" +
                            "${rawStats.estimatedWeight} (estimated)\n" +
                            "${rawStats.totalZeroQuantity} out of stock" +
                            if (rawStats.totalOpened != null) "\n${rawStats.totalOpened} opened" else "",
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                )
            }
            Spacer(Modifier.width(8.dp))
            SelectionContainer(Modifier.weight(1f)) {
                Text(
                    text = "${filteredStats.itemsCount} blends, ${filteredStats.brandsCount} brands\n" +
                            if (rawStats.averageRating.isNotBlank()) { "${filteredStats.averageRating} average rating\n" } else { "" } +
                            "${filteredStats.favoriteCount} favorites, " + "${filteredStats.dislikedCount} disliked\n" +
                            "${filteredStats.totalQuantity} total \"No. of Tins\"\n" +
                            "${filteredStats.estimatedWeight} (estimated)\n" +
                            "${filteredStats.totalZeroQuantity} out of stock" +
                            if (rawStats.totalOpened != null)"\n${filteredStats.totalOpened} opened" else "",
                    modifier = Modifier
                        .weight(1f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                )
            }
        }

        // Second Section counts per type
        if (rawStats.totalByType.any { it.key != "Unassigned" }) {
            StatSubSection(
                label = "Blend Type",
                rawField = rawStats.totalByType,
                filteredField = filteredStats.totalByType,
                modifier = Modifier
            )
        }
        if (
            (rawStats.totalBySubgenre.any { it.key != "Unassigned" }) ||
            (rawStats.totalByCut.any { it.key != "Unassigned" }) ||
            (rawStats.totalByComponent.any { it.key != "None Assigned" }) ||
            (rawStats.totalByFlavoring.any { it.key != "None Assigned" }) ||
            (rawStats.totalByContainer.any { it.key != "Unassigned" })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (expanded) {
                    // Third section counts by subgenre
                    if (rawStats.totalBySubgenre.any { it.key != "Unassigned" }) {
                        StatSubSection(
                            label = "Subgenres",
                            rawField = rawStats.totalBySubgenre,
                            filteredField = filteredStats.totalBySubgenre,
                            modifier = Modifier
                        )
                    }

                    // Fourth section counts by cuts
                    if (rawStats.totalByCut.any { it.key != "Unassigned" }) {
                        StatSubSection(
                            label = "Cuts",
                            rawField = rawStats.totalByCut,
                            filteredField = filteredStats.totalByCut,
                            modifier = Modifier
                        )
                    }

                    // Fifth section counts by components
                    if (rawStats.totalByComponent.any { it.key != "None Assigned" }) {
                        StatSubSection(
                            label = "Components",
                            rawField = rawStats.totalByComponent,
                            filteredField = filteredStats.totalByComponent,
                            modifier = Modifier
                        )
                    }

                    // Sixth section counts by flavorings
                    if (rawStats.totalByFlavoring.any { it.key != "None Assigned" }) {
                        StatSubSection(
                            label = "Flavoring",
                            rawField = rawStats.totalByFlavoring,
                            filteredField = filteredStats.totalByFlavoring,
                            modifier = Modifier
                        )
                    }

                    // Seventh section counts by container
                    if (rawStats.totalByContainer.any { it.key != "Unassigned" }) {
                        StatSubSection(
                            label = "Tin Containers",
                            rawField = rawStats.totalByContainer,
                            filteredField = filteredStats.totalByContainer,
                            modifier = Modifier
                        )
                    }
                }
            }

            // Expand/collapse
            if (expanded) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .padding(end = 24.dp)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { contracted(true) }
                ) {
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                    Icon(
                        painter = painterResource(id = R.drawable.double_up),
                        contentDescription = "Collapse",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(18.dp),
                        tint = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .padding(end = 24.dp)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { updateExpanded(true) }
                ) {
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                    Icon(
                        painter = painterResource(id = R.drawable.double_down),
                        contentDescription = "Expand",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(18.dp),
                        tint = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    HorizontalDivider(Modifier.weight(1f), 1.dp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatSubSection(
    label: String,
    rawField: Map<String, Int>,
    filteredField: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Box (
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            modifier = Modifier
                .padding(start = 2.dp)
                .offset(y = (-2).dp),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            color = colorScheme.onBackground.copy(alpha = 0.4f)
        )
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Raw Stats
            Column (
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                HorizontalDivider(Modifier.padding(bottom = 20.dp).fillMaxWidth(.65f), 1.dp)
                SelectionContainer(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        // Value
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .padding(end = 6.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                        ) {
                            rawField.forEach {
                                Text(
                                    text = "${it.value} ",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 1.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                        // Key
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                        ) {
                            val height: Dp = with(LocalDensity.current) { 24.sp.toDp() }
                            rawField.forEach {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(.8f)
                                        .height(height),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = it.key,
                                        style = TextStyle(
                                            color = LocalContentColor.current,
                                        ),
                                        maxLines = 2,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 9.sp,
                                            maxFontSize = 15.sp,
                                            stepSize = .02.sp
                                        ),
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Filtered Stats
            Column (
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                HorizontalDivider(Modifier.padding(bottom = 20.dp).fillMaxWidth(.65f), 1.dp)
                SelectionContainer {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        if (filteredField.any { it.value > 0 } ) {
                            // Value
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .padding(end = 12.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Top,
                            ) {
                                filteredField.forEach {
                                    if (it.value == 0) {
                                        Text(
                                            text = "",
                                            modifier = Modifier,
                                            textAlign = TextAlign.Start,
                                            fontSize = 15.sp,
                                        )
                                    } else {
                                        Text(
                                            text = "${it.value} ",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 1.dp),
                                            textAlign = TextAlign.Start,
                                            fontSize = 15.sp,
                                        )
                                    }
                                }
                            }

                            // Key
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Top,
                            ) {
                                val height: Dp = with(LocalDensity.current) { 24.sp.toDp() }
                                filteredField.forEach {
                                    if (it.value == 0) {
                                        Text(
                                            text = "--",
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .alpha(.5f),
                                            textAlign = TextAlign.Start,
                                            fontSize = 15.sp,
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(.8f)
                                                .height(height),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = it.key,
                                                style = TextStyle(
                                                    color = LocalContentColor.current,
                                                ),
                                                maxLines = 2,
                                                autoSize = TextAutoSize.StepBased(
                                                    minFontSize = 9.sp,
                                                    maxFontSize = 15.sp,
                                                    stepSize = .02.sp
                                                ),
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                val bottom = with(LocalDensity.current) { 13.5.sp.toDp() }
                                Text(
                                    text = "nothing found",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(.6f)
                                        .padding(start = 12.dp, bottom = bottom),
                                    textAlign = TextAlign.Start,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartsSection(
    filteredStats: FilteredStats,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 0.dp)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            text = "*Charts are filter-reactive. Some charts may be redundant/irrelevant " +
                    "depending on the chosen filters.",
            color = LocalContentColor.current.copy(alpha = 0.75f),
            modifier = Modifier
                .fillMaxWidth(),
            fontSize = 13.sp,
            textAlign = TextAlign.Start,
            softWrap = true,
        )
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChartsFormat(
                label = "Brands by Number of Entries",
                chartData = filteredStats.brandsByEntries
            )
            if (filteredStats.brandsByQuantity.count() > 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Brands by \"No. of Tins\"",
                    chartData = filteredStats.brandsByQuantity
                )
            }
            if (filteredStats.typesByEntries.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Types by Entries",
                    chartData = filteredStats.typesByEntries
                )
            }
            if (filteredStats.typesByQuantity.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Types by \"No. of Tins\"",
                    chartData = filteredStats.typesByQuantity
                )
            }

            if (filteredStats.ratingsDistribution.distribution.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Ratings Distribution",
                    histogramData = filteredStats.ratingsDistribution,
                    showHistogram = true
                )
            }

            if (filteredStats.favDisByEntries.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Fav/Dislike by Entries",
                    chartData = filteredStats.favDisByEntries
                )
            }
            if (filteredStats.subgenresByEntries.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Subgenres by Entries",
                    chartData = filteredStats.subgenresByEntries
                )
            }
            if (filteredStats.subgenresByQuantity.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Subgenres by \"No. of Tins\"",
                    chartData = filteredStats.subgenresByQuantity
                )
            }
            if (filteredStats.cutsByEntries.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Cuts by Entries",
                    chartData = filteredStats.cutsByEntries
                )
            }
            if (filteredStats.cutsByQuantity.count() > 1) {
                HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp), 1.dp)
                ChartsFormat(
                    label = "Cuts by \"No. of Tins\"",
                    chartData = filteredStats.cutsByQuantity
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChartsFormat(
    label: String,
    chartData: Map<String, Int> = mapOf(),
    histogramData: RatingsDistribution = RatingsDistribution(),
    showHistogram: Boolean = false
) {
    val countVal = if (!showHistogram) chartData.values.sum() else histogramData.distribution.values.sum()
    val showValue = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            modifier = Modifier,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "(Data Total: $countVal - ",
                modifier = Modifier,
                fontSize = 12.sp,
                textAlign = TextAlign.Start
            )
            Text(
                text = if (!showValue.value) "Show Values" else "Hide Values",
                modifier = Modifier
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { showValue.value = !showValue.value }
                    .width(75.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = colorScheme.primary,
            )
            Text(
                text = ")",
                modifier = Modifier,
                fontSize = 12.sp,
                textAlign = TextAlign.Start
            )
        }
        if (!showHistogram) {
            PieChart(
                data = chartData,
                showLabels = true,
                showPercentages = true,
                showValues = showValue.value,
                modifier = Modifier
                    .padding(top = 28.dp, bottom = 44.dp)
                    .fillMaxWidth(0.7f),
                onSliceLabelPosition = 0.6f,
                outsideSliceLabelPosition = 0.6f,
                outsideLabelThreshold = 25f,
                rotationOffset = 270f,
                textColor = Color.Black,
                labelBackground = Color.White.copy(alpha = 0.55f),
                sortData = false
            )
        } else {
            HistogramChart(
                data = histogramData,
                showValues = showValue.value,
                modifier = Modifier
                    .padding(top = 28.dp, bottom = 44.dp)
                    .fillMaxWidth(0.7f)
            )
        }
    }
}


/** Pie Chart stuff*/
@Composable
private fun PieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showPercentages: Boolean = true,
    showValues: Boolean = false,
    colors: List<Color> = listOf(
        LocalCustomColors.current.pieOne,
        LocalCustomColors.current.pieTwo,
        LocalCustomColors.current.pieThree,
        LocalCustomColors.current.pieFour,
        LocalCustomColors.current.pieFive,
        LocalCustomColors.current.pieSix,
        LocalCustomColors.current.pieSeven,
        LocalCustomColors.current.pieEight,
        LocalCustomColors.current.pieNine,
        LocalCustomColors.current.pieTen),
    onSliceLabelPosition: Float = .5f,
    outsideSliceLabelPosition: Float = 0.5f,
    outsideLabelThreshold: Float = 20f,
    rotationOffset: Float = 270f,
    textColor: Color = Color.Black,
    labelBackground: Color = Color.Unspecified,
    sortData: Boolean = false,
) {
    val total = data.values.sum()
    val sortedData = if (sortData) { data.toList().sortedByDescending { it.second }.toMap() } else { data }
    val textMeasurer = rememberTextMeasurer()

    val isLightTheme = LocalCustomColors.current.isLightTheme
    val background = colorScheme.background

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val insideLabel = min(centerX, centerY) * onSliceLabelPosition
        val outsideLabel = (min(centerX, centerY) * (1f)) + ((20.dp.toPx() * outsideSliceLabelPosition))

        drawSlices(
            sortedData, colors, total, rotationOffset
        )

        drawLabels(
            sortedData, total, rotationOffset, showLabels, showPercentages, showValues, textMeasurer, textColor, colors, labelBackground,
            centerX, centerY, insideLabel, outsideLabel, outsideLabelThreshold, isLightTheme, background
        )
    }
}

private fun DrawScope.drawSlices(
    data: Map<String, Int>,
    colors: List<Color>,
    total: Int,
    startAngle: Float,
) {
    var currentStartAngle = startAngle

    data.forEach { (label, value) ->
        val sweepAngle = (value.toFloat() / total) * 360f

        drawArc(
            color = colors[data.keys.indexOf(label) % colors.size],
            startAngle = currentStartAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            style = Fill,
        )
        currentStartAngle += sweepAngle
    }
}

private fun DrawScope.drawLabels(
    data: Map<String, Int>,
    total: Int,
    startAngle: Float,
    showLabels: Boolean,
    showPercentages: Boolean,
    showValues: Boolean,
    textMeasurer: TextMeasurer,
    textColor: Color,
    colors: List<Color>,
    backgroundColor: Color,
    centerX: Float,
    centerY: Float,
    insideRadius: Float,
    outsideRadius: Float,
    outsideLabelThreshold: Float,
    isLightTheme: Boolean,
    pageBackground: Color,
) {
    var currentStartAngle = startAngle
    var outsideLabelCount = 0
    val sliceCount = data.size
    val totalOutsideLabels = data.values.count { (it.toFloat() / total) * 360f < outsideLabelThreshold }
    val totalThinPercent = data.values.count {
        val normalizedMidpointAngle = (currentStartAngle + (it.toFloat() / total) * 360f) % 360f
        (it.toFloat() / total) * 360f < 10f && normalizedMidpointAngle > 245f
    }
    var thinCount = 0

    var firstOutsideLabelAngle = -1f
    var tempAngle = startAngle
    for ((_, value) in data) {
        val sweep = (value.toFloat() / total) * 360f
        if (sweep < outsideLabelThreshold) {
            firstOutsideLabelAngle = (tempAngle + sweep / 2f) % 360f
            break
        }
        tempAngle += sweep
    }

    val moveToBottom = firstOutsideLabelAngle in 180f..225f

    val thinPercentMeasures = mutableMapOf<String, Float>()
    var tempMeasureAngle = startAngle
    // measure percentage pre-processing loop
    data.forEach { (label, value) ->
        val sweep = (value.toFloat() / total) * 360f
        val midpoint = tempMeasureAngle + sweep / 2f
        val normalizedMidpoint = (midpoint + 360f) % 360f

        if (sweep < 10f && normalizedMidpoint > 245f) {
            val valuePad = if (showValues) "($value) " else ""
            val percent = (value.toDouble() / total) * 100
            val percentCal = " ${formatDecimal(percent)}% " + valuePad
            val measuredWidth = textMeasurer.measure(
                text = AnnotatedString(percentCal),
                style = TextStyle(fontSize = 11.sp)
            ).size.width.toFloat()
            thinPercentMeasures[label] = measuredWidth
        }
        tempMeasureAngle += sweep
    }
    val spacing = 3.dp.toPx()
    val totalThinWidth = thinPercentMeasures.values.sum() + (spacing * (thinPercentMeasures.size - 1).coerceAtLeast(0))
    val orderedThinLabels = data.keys.filter { it in thinPercentMeasures.keys }

    data.forEach { (label, value) ->
        val sweepAngle = (value.toFloat() / total) * 360f
        val midpointAngle = currentStartAngle + (sweepAngle / 2)
        val normalizedMidpointAngle = (midpointAngle) % 360f
        val isOther = label == "(Other)"
        if (sweepAngle < outsideLabelThreshold && !isOther) { outsideLabelCount++ }
        if (sweepAngle < 10f && normalizedMidpointAngle > 245f) { thinCount++ }

        val radius = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            outsideRadius
        } else {
            if (sliceCount > 5 && isOther) {
                insideRadius * .65f
            } else {
                insideRadius
            }
        }

        /** label coloring */
        val targetColor = listOf(colors[3], colors[4], colors[5], colors[6])

        val labelColor = if (showLabels) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    colors[data.keys.indexOf(label) % colors.size]
                } else {
                    textColor
                }
            } else {
                textColor
            }
        } else {
            Color.Transparent
        }
        val percentColor = if (showLabels && showPercentages) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    colors[data.keys.indexOf(label) % colors.size]
                } else {
                    textColor
                }
            } else {
                textColor
            }
        } else {
            Color.Transparent
        }
        val labelBg = if (showLabels) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    if (isLightTheme && labelColor in targetColor) {
                        Color.DarkGray.copy(alpha = 0.65f)
                    } else {
                        pageBackground.copy(alpha = 0.75f)
                    }
                } else {
                    backgroundColor
                }
            } else {
                backgroundColor
            }
        } else {
            Color.Transparent
        }
        val percentBg = if (showLabels && showPercentages) {
            if (totalOutsideLabels > 1) {
                if (outsideLabelCount >= 1 && !isOther) {
                    if (isLightTheme && percentColor in targetColor) {
                        Color.DarkGray.copy(alpha = 0.65f)
                    } else {
                        pageBackground.copy(alpha = 0.75f)
                    }
                }  else {
                    backgroundColor
                }
            } else {
                backgroundColor
            }
        } else {
            Color.Transparent
        }


        /** label text formatting */
        val labelPad = " $label "
        val valuePad = if (showValues) "($value) " else ""
        val percent = (value.toDouble() / total) * 100
        val percentageCal = " ${formatDecimal(percent)}% " + valuePad

        val textLabel = textMeasurer.measure(
            text = AnnotatedString(labelPad),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                background = labelBg,
                textAlign = TextAlign.Start,
            ),
            constraints = Constraints(maxWidth = 250),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        val percentageLabel = textMeasurer.measure(
            text = AnnotatedString(percentageCal),
            style = TextStyle(
                fontSize = 11.sp,
                background = percentBg,
                textAlign = TextAlign.Center,
            )
        )


        /** label positioning */
        val labelWidth = textLabel.size.width.toFloat()
        val labelHeight = textLabel.size.height.toFloat()
        val percentageWidth = percentageLabel.size.width.toFloat()
        val percentageHeight = percentageLabel.size.height.toFloat()
        val combinedHeight = if (showLabels && showPercentages) {
            labelHeight + percentageHeight // * .2f)
        } else {
            if (showLabels) {
                labelHeight
            } else {
                percentageHeight
            }
        }

        val labelX = centerX + radius * cos(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val labelY = centerY + radius * sin(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val percentageX = centerX + radius * cos(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val percentageY = centerY + radius * sin(Math.toRadians(midpointAngle.toDouble())).toFloat()

        val listSpacing = 3.dp.toPx()

        val labelList = (labelHeight + listSpacing) * (outsideLabelCount - 1)
        val listHeight = (labelHeight + listSpacing) * totalOutsideLabels
        val listX = (centerX - outsideRadius) - 125

        val listY = (centerY - outsideRadius) - (percentageHeight * 1.5f)


        /** additional adjustment based on quadrant */
        val xOffsetFactor = when (normalizedMidpointAngle) {
            in 0f..90f -> (normalizedMidpointAngle - 0f) / (180f) // 0 to 0.5
            in 90f..180f -> (normalizedMidpointAngle - 0f) / (180f) // 0.5 to 1
            in 180f..270f -> (360f - normalizedMidpointAngle) / (180f)  // 1 to 0.5
            in 270f..360f -> (360f - normalizedMidpointAngle) / (180f) // 0.5 to 0
            else -> 0f
        }
        val yOffsetFactor = when (normalizedMidpointAngle) {
            in 0f..90f -> (90f - normalizedMidpointAngle) / (180f) // 0.5 to 0
            in 90f..180f -> (normalizedMidpointAngle - 90f) / (180f) // 0 to 0.5
            in 180f..270f -> (normalizedMidpointAngle - 90f) / (180f)  // 0.5 to 1
            in 270f..360f -> (450f - normalizedMidpointAngle) / (180f) // 1 to 0.5
            else -> 0f
        }


        /** final adjustment */
        val adjustedLabelX = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            // list placement
            listX
        } else {
            // normal on slice labels
            labelX - (labelWidth * xOffsetFactor)
        }
        val adjustedLabelY = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            // list placement
            val listBase = if (moveToBottom) { size.height - listHeight } else { 0f }
            listBase + labelList
        } else {
            // normal on slice labels
            labelY - (combinedHeight * yOffsetFactor)
        }

        val adjustedPercentageX = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            // very thin slices extra adjustment
            if (sweepAngle < 10f && normalizedMidpointAngle > 245f && totalThinPercent > 0) {
                // list
                val start = centerY - (totalThinWidth / 2)
                val index = orderedThinLabels.indexOf(label)
                val precedingWidth = orderedThinLabels.take(index).sumOf {
                    thinPercentMeasures[it]?.toDouble() ?: 0.0
                }.toFloat()
                val precedingSpace = listSpacing * index

                start + precedingWidth + precedingSpace
//                if (totalThinPercent > 2) {
//                    val right = (totalThinPercent - thinCount)
//                    (percentageX - (percentageWidth * xOffsetFactor)) - ((right * (percentageHeight * 1.15f)) * cos(Math.toRadians(midpointAngle.toDouble())).toFloat())
//                } else {
//                    // alternating
//                    val additionalOffset = if (outsideLabelCount % 2 == 0) {
//                        // evens up
//                        (-1 * ((percentageHeight * 0.25f) + (alternatingOffsetMax * alternatingOffsetFactor)))
//                    } else {
//                        // odds down
//                        (percentageHeight * 0.75f) + ((alternatingOffsetMax * 3) * alternatingOffsetFactor)
//                    }
//                    (percentageX - (percentageWidth * xOffsetFactor)) - (additionalOffset * cos(Math.toRadians(midpointAngle.toDouble())).toFloat())
//                }
            } else {
                // normal outside slice placement
                percentageX - (percentageWidth * xOffsetFactor)
            }
        } else {
            // normal inside slice placement
            adjustedLabelX + (labelWidth - percentageWidth) / 2
        }
        val adjustedPercentageY = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            if (sweepAngle < 10f && normalizedMidpointAngle > 245f && totalThinPercent > 1) {
                // list placement
                listY

//                // very thin slices at the top of the chart
//                if (totalThinPercent > 2) {
//                    val down = (totalThinPercent - thinCount)
//                    ((centerY - radius) - percentageHeight) + (down * (percentageHeight * 1.15f))
//                }
//                else {
//                    // alternating
//                    val additionalOffset = if (outsideLabelCount % 2 == 0) {
//                        // evens up
//                        (-1 * ((percentageHeight * 0.25f) + (alternatingOffsetMax * alternatingOffsetFactor)))
//                    } else {
//                        // odds down
//                        (percentageHeight * 0.75f) + ((alternatingOffsetMax * 3) * alternatingOffsetFactor)
//                    }
//                    (percentageY - (percentageHeight * yOffsetFactor)) + (additionalOffset)
//                }
            } else {
                // outside slice placement
                percentageY - (percentageHeight * yOffsetFactor)
            }
        } else {
            // normal inside slice placement
            adjustedLabelY + labelHeight
        }

        drawText(
            textLayoutResult = textLabel,
            brush = SolidColor(labelColor),
            topLeft = Offset(
                x = adjustedLabelX,
                y = adjustedLabelY
            ),
        )

        drawText(
            textLayoutResult = percentageLabel,
            brush = SolidColor(percentColor),
            topLeft = Offset(
                x = adjustedPercentageX,
                y = adjustedPercentageY
            )
        )

        currentStartAngle += sweepAngle
    }
}


@Composable
private fun HistogramChart(
    data: RatingsDistribution,
    modifier: Modifier = Modifier,
    showValues: Boolean = false,
) {
    val distribution = data.distribution
    val unratedCount = data.unratedCount

    val maxCount = (distribution.values.maxOrNull() ?: 0).coerceAtLeast(1)

    val density = LocalDensity.current
    var currentWidth by remember { mutableStateOf(0.dp) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                currentWidth = with(density) { it.size.width.toDp() }
            }
    ) {
        val width: Dp = (currentWidth) / 11
        val ratingSteps = List(11) { it / 2.0 }

        Box(
            modifier = Modifier
        ) {
            // chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.33f)
                    .padding(bottom = 16.dp)
            ) {
                ratingSteps.forEach {
                    val count = distribution.getOrDefault(it, 0)
                    val barHeight = count.toFloat() / maxCount

                    Column(
                        modifier = Modifier
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(width)
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (showValues && count == 0) {
                                Text(
                                    text = "(0)",
                                    color = LocalContentColor.current,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    autoSize = TextAutoSize.StepBased(
                                        minFontSize = 9.sp,
                                        maxFontSize = 12.sp,
                                        stepSize = .02.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(start = 4.dp, end = 4.dp, bottom = 1.dp)
                                )
                            }
                            // actual bar
                            val borderColor = colorScheme.background
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 2.dp)
                                    .fillMaxHeight(barHeight)
                                    .background(LocalCustomColors.current.pieOne)
                                    .drawBehind {
                                        val strokeWidth = Dp.Hairline.toPx()
                                        val xOffset = size.width - strokeWidth / 2

                                        drawLine(
                                            color = borderColor,
                                            start = Offset((strokeWidth / 2), 0f),
                                            end = Offset((strokeWidth / 2), size.height),
                                            strokeWidth = strokeWidth
                                        )

                                        drawLine(
                                            color = borderColor,
                                            start = Offset(xOffset, 0f),
                                            end = Offset(xOffset, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                contentAlignment = Alignment.TopCenter
                            ) {
                                val offset = with(density) { 6.sp.toDp() }
                                if (showValues && count > 0) {
                                    Text(
                                        text = "$count",
                                        color = LocalContentColor.current,
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .offset(y = -offset)
                                            .background(colorScheme.background.copy(alpha = 0.75f))
                                            .padding(start = 4.dp, end = 4.dp, bottom = 0.5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // null count
            if (unratedCount > 0) {
                Text(
                    text = "(Unrated: $unratedCount)",
                    color = LocalContentColor.current,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(colorScheme.background.copy(alpha = 0.75f))
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                )
            }
            // count label
            Text(
                text = "Frequency",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .rotate(-90f)
                    .align(Alignment.CenterStart)
                    .offset(24.dp, -(width + 32.dp))
            )
        }
        // x-axis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
                .height(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = colorScheme.outline,
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    repeat(11) {
                        Box(
                            modifier = Modifier
                                .width(width),
                            contentAlignment = Alignment.TopStart
                        ) {
                            val fraction = if (it % 2 == 0) 1f else 0.5f
                            val thickness = if (it % 2 == 0) 2.dp else 0.75.dp
                            VerticalDivider(Modifier.fillMaxHeight(fraction), thickness, colorScheme.outline)
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = -(width / 2)),
            verticalAlignment = Alignment.Top
        ) {
            ratingSteps.forEachIndexed { index, it ->
                if (index % 2 == 0) {
                    Text(
                        text = formatDecimal(it, 0),
                        modifier = Modifier
                            .width(width),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    val offset = with(density) { 9.sp.toDp() }
                    Text(
                        text = formatDecimal(it, 1),
                        modifier = Modifier
                            .width(width)
                            .offset(y = -offset),
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rating (ranges)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}