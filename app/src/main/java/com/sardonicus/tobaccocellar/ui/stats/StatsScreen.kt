package com.sardonicus.tobaccocellar.ui.stats

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
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
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.navigation.StatsDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigateToHome: () -> Unit,
    navigateToDates: () -> Unit,
    navigateToAddEntry: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val rawStats by viewModel.rawStats.collectAsState()
    val filteredStats by viewModel.filteredStats.collectAsState()
    val availableSections by viewModel.availableSections.collectAsState()
    var selectionFocused by remember { mutableStateOf(false) }
    var selectionKey by remember { mutableIntStateOf(0) }

    val showLoading by viewModel.showLoading.collectAsState()

    BackHandler(selectionFocused) { if (selectionFocused) { selectionKey++; selectionFocused = false } }

    DisposableEffect(Unit) { onDispose { selectionKey++; selectionFocused = false } }


    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .pointerInput(selectionFocused) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Main)
                    if (selectionFocused) { selectionKey++; selectionFocused = false; down.consume() }
                }
            },
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
                navigateToHome = { selectionKey++; selectionFocused = false; navigateToHome() },
                navigateToDates = { selectionKey++; selectionFocused = false; navigateToDates() },
                navigateToAddEntry = { selectionKey++; selectionFocused = false; navigateToAddEntry() },
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
            if (showLoading) { LoadingIndicator() }
            else {
                StatsBody(
                    rawStats = rawStats,
                    filteredStats = filteredStats,
                    availableSections = availableSections,
                    selectionKey = { selectionKey },
                    selectionFocused = { selectionFocused = it },
                    modifier = modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun StatsBody(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    availableSections: AvailableSections,
    selectionKey: () -> Int,
    selectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }


    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        item { Header("Quick Stats", Modifier.padding(top = 1.dp)) }

        item { Spacer(Modifier.height(10.dp)) }

        item {
            QuickStatsSection(
                rawStats = rawStats,
                filteredStats = filteredStats,
                availableSections = availableSections,
                selectionKey = selectionKey,
                selectionFocused = selectionFocused,
                expanded = expanded,
                contract = { scope.launch { lazyListState.animateScrollToItem(0); expanded = false } },
                expand = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            )
        }

        item { Spacer(Modifier.height(10.dp)) }

        // Charts //
        item { Header("Charts") }

        // Disclaimer //
        item {
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
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 0.dp)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ChartsFormat(
                    label = "Brands by Number of Entries",
                    chartData = { filteredStats.brandsByEntries },
                )
            }
        }

        if (filteredStats.brandsByQuantity.count() > 1) {
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Brands by \"No. of Tins\"",
                    chartData = { filteredStats.brandsByQuantity },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        if (filteredStats.brandsByRating.count() > 1) {
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                BrandsByRatingSection(
                    brandsByRating = { filteredStats.brandsByRating }
                )
            }
        }
        if (filteredStats.typesByEntries.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Types by Entries",
                    chartData = { filteredStats.typesByEntries },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        if (filteredStats.typesByQuantity.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Types by \"No. of Tins\"",
                    chartData = { filteredStats.typesByQuantity },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

            }
        }
        if (filteredStats.ratingsDistribution.distribution.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Ratings Distribution",
                    histogramData = { filteredStats.ratingsDistribution },
                    showHistogram = true,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        if (filteredStats.favDisByEntries.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Fav/Dislike by Entries",
                    chartData = { filteredStats.favDisByEntries },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        if (filteredStats.subgenresByEntries.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Subgenres by Entries",
                    chartData = { filteredStats.subgenresByEntries },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        if (filteredStats.subgenresByQuantity.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Subgenres by \"No. of Tins\"",
                    chartData = { filteredStats.subgenresByQuantity },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        if (filteredStats.cutsByEntries.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Cuts by Entries",
                    chartData = { filteredStats.cutsByEntries },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

            }
        }
        if (filteredStats.cutsByQuantity.count() > 1) {
            item {
                HorizontalDivider(
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    1.dp
                )
                ChartsFormat(
                    label = "Cuts by \"No. of Tins\"",
                    chartData = { filteredStats.cutsByQuantity },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

    }
}


@Composable
private fun Header(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val separatorColor = colorScheme.secondary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = LocalCustomColors.current.backgroundVariant)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 8.dp),
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


@Composable
private fun BrandsByRatingSection(
    brandsByRating: () -> Map<String, BrandRatingStats>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val countVal = brandsByRating().values.sumOf { it.ratingsCount }
        Text(
            text = "Brands by Average Rating",
            modifier = Modifier,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "(Data Total: $countVal)",
            modifier = Modifier,
            fontSize = 12.sp,
            textAlign = TextAlign.Start
        )
        Column (
            modifier = Modifier
                .padding(top = 20.dp, bottom = 44.dp)
                .fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    Column(Modifier.width(IntrinsicSize.Max)) {
                        brandsByRating().forEach {
                            val index = brandsByRating().keys.indexOf(it.key)
                            Text(
                                text = "${index + 1}.",
                                fontSize = 14.sp,
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                    Column(Modifier.weight(1f, false)) {
                        brandsByRating().forEach {
                            Text(
                                text = it.key,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Column(Modifier.width(IntrinsicSize.Max)) {
                        brandsByRating().forEach {
                            Text(
                                text = formatDecimal(it.value.averageRating, drop = false),
                                fontSize = 14.sp,
                                maxLines = 1,
                            )
                        }
                    }
                    Column(Modifier.width(IntrinsicSize.Max)) {
                        brandsByRating().forEach {
                            Text(
                                text = "(${it.value.ratingsCount})",
                                fontSize = 14.sp,
                                maxLines = 1,
                            )
                        }
                    }
                    Column(Modifier.width(IntrinsicSize.Max)) {
                        brandsByRating().forEach {
                            Text(
                                text = formatDecimal(it.value.weightedRating, drop = false),
                                fontSize = 14.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Text(
                    text = "(Actual - Count - Weighted)",
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.75f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}


@Composable
private fun ChartsFormat(
    label: String,
    modifier: Modifier = Modifier,
    chartData: () -> Map<String, Int> = { mapOf() },
    histogramData: () -> RatingsDistribution = { RatingsDistribution() },
    showHistogram: Boolean = false
) {
    val countVal by remember(showHistogram, chartData(), histogramData()) {
        derivedStateOf {
            if (!showHistogram) chartData().values.sum()
            else histogramData().distribution.values.sum()
        }
    }

    var showValue by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
                text = if (!showValue) "Show Values" else "Hide Values",
                modifier = Modifier
                    .clip(RoundedCornerShape(25))
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { showValue = !showValue }
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
                data = chartData(),
                showValues = showValue,
                modifier = Modifier
                    .padding(top = 28.dp, bottom = 44.dp)
                    .fillMaxWidth(0.7f),
                labelBackground = Color.White.copy(alpha = 0.55f)
            )
        } else {
            HistogramChart(
                data = histogramData(),
                showValues = showValue,
                modifier = Modifier
                    .padding(top = 28.dp, bottom = 44.dp)
                    .fillMaxWidth(0.7f)
            )
        }
    }
}