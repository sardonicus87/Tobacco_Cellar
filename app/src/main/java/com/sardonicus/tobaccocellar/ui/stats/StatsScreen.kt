package com.sardonicus.tobaccocellar.ui.stats


import android.icu.text.DecimalFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.composables.FullScreenLoading
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object StatsDestination : NavigationDestination {
    override val route = "stats"
    override val titleRes = R.string.stats_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigateToHome: () -> Unit,
    navigateToDates: () -> Unit,
    navigateToAddEntry: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val rawStats by viewmodel.rawStats.collectAsState()
    val filteredStats by viewmodel.filteredStats.collectAsState()
    val filterViewModel = LocalCellarApplication.current.filterViewModel

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(StatsDestination.titleRes),
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
                filterViewModel = filterViewModel,
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
                FullScreenLoading()
            } else {
                StatsBody(
                    rawStats = rawStats,
                    filteredStats = filteredStats,
                    modifier = modifier
                        .fillMaxSize(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsBody(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    modifier: Modifier = Modifier,
) {
    val separatorColor = colorScheme.secondary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(
            modifier = Modifier
                .height(1.dp)
        )

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

        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        QuickStatsSection(
            rawStats = rawStats,
            filteredStats = filteredStats,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 24.dp),
        )

        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickStatsSection(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val totalByType = rawStats.totalByType.toList().sortedBy {
        when (it.first) {
            "Burley" -> 0
            "Virginia" -> 1
            "English" -> 2
            "Aromatic" -> 3
            "Other" -> 4
            "Unassigned" -> 5
            else -> 6
        }
    }.joinToString(separator = "\n") { "${it.second} ${it.first}" }
    val totalBySubgenre = rawStats.totalBySubgenre.toList().sortedWith(
        compareBy<Pair<String, Int>> { if (it.first == "Unassigned") 1 else 0 }
            .thenBy { if (it.first != "Unassigned") it.first.lowercase() else "" }
    ).joinToString(separator = "\n") { "${it.second} ${it.first}" }
    val totalByCut = rawStats.totalByCut.toList().sortedWith(
        compareBy<Pair<String, Int>> { if (it.first == "Unassigned") 1 else 0 }
            .thenBy { if (it.first != "Unassigned") it.first.lowercase() else "" }
    ).joinToString(separator = "\n") { "${it.second} ${it.first}" }
    val totalByContainer = rawStats.totalByContainer.toList().sortedWith(
        compareBy<Pair<String, Int>> { if (it.first == "Unassigned") 1 else 0 }
            .thenBy { if (it.first != "Unassigned") it.first.lowercase() else "" }
    ).joinToString(separator = "\n") { "${it.second} ${it.first}" }

    val totalByTypeFiltered = filteredStats.totalByType.toList().sortedBy {
        when (it.first) {
            "Burley" -> 0
            "Virginia" -> 1
            "English" -> 2
            "Aromatic" -> 3
            "Other" -> 4
            "Unassigned" -> 5
            else -> 6
        }
    }.joinToString(separator = "\n") { "${it.second} ${it.first}" }
    val totalBySubgenreFiltered = filteredStats.totalBySubgenre.toList().sortedWith(
        compareBy<Pair<String, Int>> { if (it.first == "Unassigned") 1 else 0 }
            .thenBy { if (it.first != "Unassigned") it.first.lowercase() else "" }
    ).joinToString(separator = "\n") { "${it.second} ${it.first}" }
    val totalByCutFiltered = filteredStats.totalByCut.toList().sortedWith(
        compareBy<Pair<String, Int>> { if (it.first == "Unassigned") 1 else 0 }
            .thenBy { if (it.first != "Unassigned") it.first.lowercase() else "" }
    ).joinToString(separator = "\n") { "${it.second} ${it.first}" }
    val totalByContainerFiltered = filteredStats.totalByContainer.toList().sortedWith(
        compareBy<Pair<String, Int>> { if (it.first == "Unassigned") 1 else 0 }
            .thenBy { if (it.first != "Unassigned") it.first.lowercase() else "" }
    ).joinToString(separator = "\n") { "${it.second} ${it.first}" }

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
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
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
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "${rawStats.itemsCount} blends, ${rawStats.brandsCount} brands\n" +
                        "${rawStats.favoriteCount} favorites, ${rawStats.dislikedCount} disliked\n" +
                        "${rawStats.totalQuantity} total \"No. of Tins\"\n" +
                        "${rawStats.totalOpened} opened\n" +
                        "${rawStats.totalZeroQuantity} out of stock",
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 12.dp),
                fontSize = 15.sp,
                textAlign = TextAlign.Start,
                softWrap = true,
            )
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
            Text(
                text = "${filteredStats.itemsCount} blends, ${filteredStats.brandsCount} brands\n" +
                        "${filteredStats.favoriteCount} favorites, " + "${filteredStats.dislikedCount} disliked\n" +
                        "${filteredStats.totalQuantity} total \"No. of Tins\"\n" +
                        "${filteredStats.totalOpened} opened\n" +
                        "${filteredStats.totalZeroQuantity} out of stock",
                modifier = Modifier
                    .weight(1f),
                fontSize = 15.sp,
                textAlign = TextAlign.Start,
                softWrap = true,
            )
        }

        // Second Section counts per type
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            if (rawStats.totalByType.any { it.key != "Unassigned" }) {
                Text(
                    text = totalByType,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = totalByType.toString() }
                        .padding(bottom = 12.dp),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                )
            }
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
            if (filteredStats.totalByType.any { it.key != "Unassigned" }) {
                Text(
                    text = totalByTypeFiltered,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = totalByTypeFiltered.toString() },
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    softWrap = true
                )
            }
        }
        if (
            (rawStats.totalBySubgenre.any { it.key != "Unassigned" }) ||
            (rawStats.totalByCut.any { it.key != "Unassigned" }) ||
            (rawStats.totalByContainer.any { it.key != "Unassigned" })
        ) {
            Column {
                if (expanded) {
                    // Third section counts by subgenre
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        if (rawStats.totalBySubgenre.any { it.key != "Unassigned" }) {
                            Text(
                                text = totalBySubgenre,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 12.dp)
                                    .semantics { contentDescription = totalBySubgenre.toString() },
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                softWrap = true,
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .width(8.dp)
                        )
                        if (filteredStats.totalBySubgenre.any { it.key != "Unassigned" }) {
                            Text(
                                text = totalBySubgenreFiltered,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = totalBySubgenreFiltered.toString()
                                    },
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                softWrap = true,
                            )
                        }
                    }

                    // Fourth section counts by cuts
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        if (rawStats.totalByCut.any { it.key != "Unassigned" }) {
                            Text(
                                text = totalByCut,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 12.dp)
                                    .semantics { contentDescription = totalByCut.toString() },
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                softWrap = true,
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .width(8.dp)
                        )
                        if (filteredStats.totalByCut.any { it.key != "Unassigned" }) {
                            Text(
                                text = totalByCutFiltered,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = totalByCutFiltered.toString()
                                    },
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                softWrap = true,
                            )
                        }
                    }

                    // Fifth section container counts
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        if (rawStats.totalByContainer.any { it.key != "Unassigned" }) {
                            Text(
                                text = totalByContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 12.dp)
                                    .semantics { contentDescription = totalByContainer.toString() },
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                softWrap = true,
                            )
                        }
                        if (filteredStats.totalByContainer.any { it.key != "Unassigned" }) {
                            Spacer(
                                modifier = Modifier
                                    .width(8.dp)
                            )
                            Text(
                                text = totalByContainerFiltered,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = totalByContainerFiltered.toString() },
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                softWrap = true,
                            )
                        }
                    }
                }
            }
            if (expanded) {
                Text(
                    text = "Collapse",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            expanded = false
                        }
                        .padding(vertical = 1.dp)
                        .fillMaxWidth()
                )
            } else {
                Text(
                    text = "Expand",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            expanded = true
                        }
                        .padding(vertical = 1.dp)
                        .fillMaxWidth()
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(8.dp)
        )
    }

}

@OptIn(ExperimentalLayoutApi::class)
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
        Spacer(
            modifier = Modifier
                .height(10.dp)
        )
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
        Spacer(
            modifier = Modifier
                .height(24.dp)
        )
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
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                thickness = 1.dp,
            )
            ChartsFormat(
                label = "Brands by Tins",
                chartData = filteredStats.brandsByQuantity
            )
            if (filteredStats.typesByEntries.count() > 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Types by Entries",
                    chartData = filteredStats.typesByEntries
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Types by Tins",
                    chartData = filteredStats.typesByQuantity
                )
            }
            if (filteredStats.ratingsByEntries.count() > 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Ratings by Entries",
                    chartData = filteredStats.ratingsByEntries
                )
            }
            if (filteredStats.subgenresByEntries.count() > 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Subgenres by Entries",
                    chartData = filteredStats.subgenresByEntries
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                )
                ChartsFormat(
                    label = "Subgenres by Tins",
                    chartData = filteredStats.subgenresByQuantity
                )
            }
            if (filteredStats.cutsByEntries.count() > 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Cuts by Entries",
                    chartData = filteredStats.cutsByEntries
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                    thickness = 1.dp,
                )
                ChartsFormat(
                    label = "Cuts by Tins",
                    chartData = filteredStats.cutsByQuantity
                )
            }
            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )
        }
    }
}

@Composable
private fun ChartsFormat(
    label: String,
    chartData: Map<String, Int>,
) {
    val countVal = chartData.values.sum()
    var showValue = remember { mutableStateOf(false) }
    val pieColors = listOf(
        LocalCustomColors.current.pieOne,
        LocalCustomColors.current.pieTwo,
        LocalCustomColors.current.pieThree,
        LocalCustomColors.current.pieFour,
        LocalCustomColors.current.pieFive,
        LocalCustomColors.current.pieSix,
        LocalCustomColors.current.pieSeven,
        LocalCustomColors.current.pieEight,
        LocalCustomColors.current.pieNine,
        LocalCustomColors.current.pieTen,
    )

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
            modifier = Modifier,
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
                        onClick = { showValue.value = !showValue.value }
                    )
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
        PieChart(
            data = chartData,
            showLabels = true,
            showPercentages = true,
            showValues = showValue.value,
            modifier = Modifier
                .padding(top = 28.dp, bottom = 44.dp)
                .fillMaxWidth(fraction = 0.7f),
            onSliceLabelPosition = 0.6f,
            outsideSliceLabelPosition = 0.6f,
            outsideLabelThreshold = 25f,
            rotationOffset = 270f,
            textColor = Color.Black,
            labelBackground = Color.White.copy(alpha = 0.55f),
            sortData = false
        )
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
            centerX, centerY, insideLabel, outsideLabel, outsideLabelThreshold, isLightTheme
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
) {
    var currentStartAngle = startAngle
    var outsideLabelCount = 0
    val sliceCount = data.size
    val totalOutsideLabels = data.values.count { (it.toFloat() / total) * 360f < outsideLabelThreshold }

    data.forEach { (label, value) ->
        val sweepAngle = (value.toFloat() / total) * 360f
        val midpointAngle = currentStartAngle + (sweepAngle / 2)
        val isOther = label == "(Other)"
        if (sweepAngle < outsideLabelThreshold && !isOther) {
            outsideLabelCount++
        }

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
                    if (isLightTheme) {
                        Color.Black.copy(alpha = 0.80f)
                    } else {
                        Color(0xFF0F1511).copy(alpha = 0.75f)
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
                    if (isLightTheme) {
                        Color.Black.copy(alpha = 0.80f)
                    } else {
                        Color(0xFF0F1511).copy(alpha = 0.75f)
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
        val valuePad = "($value) "
        val decimalFormat = DecimalFormat(" #.##% ")
        val percentageCal = if (showValues) {
                decimalFormat.format(value.toDouble() / total) + valuePad
            } else {
                decimalFormat.format(value.toDouble() / total)
            }

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
        val labelY = centerY + radius * sin(Math.toRadians(midpointAngle.toDouble())).toFloat() // - combinedHeight / 2
        val percentageX = centerX + radius * cos(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val percentageY = centerY + radius * sin(Math.toRadians(midpointAngle.toDouble())).toFloat()

        val outsideList = (labelHeight * 1.25) * (outsideLabelCount - 1)
        val listHeight = (labelHeight * 1.25) * totalOutsideLabels
        val listX = outsideRadius * (-0.4f) // multiplying by negative puts it on the left of the chart
        val listY = outsideList


        /** additional adjustment based on quadrant */
        val normalizedMidpointAngle = (midpointAngle) % 360f

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

        val topOffsetSwitch = when (normalizedMidpointAngle) {
            in 225f..270f -> 1  // originally 255-270
            else -> 0
        }

        val alternatingOffsetMax = 20.dp.toPx()
        val alternatingOffsetFactor = when (normalizedMidpointAngle) {
            in 180f..270f -> (270f - normalizedMidpointAngle) / (90f) // 1 to 0
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
            if (normalizedMidpointAngle > 180f && totalOutsideLabels > 2) {
                listY.toFloat() + ((centerY * 2) - listHeight.toFloat()) + (labelHeight * .25f)
            }
            else {
                listY.toFloat()
            }
        } else {
            // normal on slice labels
            labelY - (combinedHeight * yOffsetFactor)
        }

        val adjustedPercentageX = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            // outside slice placement
            percentageX - (percentageWidth * xOffsetFactor)
        } else {
            // normal inside slice placement
            adjustedLabelX + (labelWidth - percentageWidth) / 2
        }
        val adjustedPercentageY = if (sweepAngle < outsideLabelThreshold && totalOutsideLabels > 1) {
            if (sweepAngle < 10f && normalizedMidpointAngle > 225f) {
                // very thin slices at the top of the chart
                val additionalOffset = if (outsideLabelCount % 2 == 0) {
                    (-1 * ((percentageHeight * 0.25f) + (alternatingOffsetMax * alternatingOffsetFactor))) // (-1 * (percentageHeight * 0.25f))
                    } else {
                    (percentageHeight * 0.75f) + ((alternatingOffsetMax * 3) * alternatingOffsetFactor)
                }
                (percentageY - (percentageHeight * yOffsetFactor)) + (additionalOffset)
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

