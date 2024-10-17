package com.example.tobaccocellar.ui.stats


import android.icu.text.DecimalFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarBottomAppBar
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import com.example.tobaccocellar.ui.theme.LocalCustomColors
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
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
            StatsBody(
                rawStats = rawStats,
                filteredStats = filteredStats,
                viewModel = viewmodel,
                modifier = modifier.fillMaxSize(),
                contentPadding = innerPadding,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsBody(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var rawStatsExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        // Raw Stats
        Row(
            modifier = Modifier
                .clickable(onClick = { rawStatsExpanded = !rawStatsExpanded }),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.quick_stats),
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp, bottom = 2.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            )
            Icon(
                painter = if (rawStatsExpanded) painterResource(id = R.drawable.arrow_down) else painterResource(id = R.drawable.arrow_up),
                contentDescription = null,
                modifier = Modifier
                    .padding(0.dp)
                    .size(24.dp),
                tint = LocalContentColor.current
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            thickness = 1.dp,
        )
            AnimatedVisibility(
                visible = rawStatsExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                QuickStats(
                    rawStats = rawStats,
                )
                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )
        }
        Spacer(
            modifier = Modifier
                .height(20.dp)
        )

        // Charts
        Text(
            text = "Charts",
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 2.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            thickness = 1.dp,
        )
        ChartsSection(
            rawStats = rawStats,
            filteredStats = filteredStats,
            viewModel = viewModel
        )

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickStats(
    rawStats: RawStats,
) {
    val totalByType = rawStats.totalByType.toList().sortedBy {
        when (it.first) {
            "Burley" -> 0
            "Virginia" -> 1
            "English" -> 2
            "Aromatic" -> 3
            "Other" -> 4
            else -> 5
        }
    }.joinToString(separator = ", ") { "${it.second} ${it.first}" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 0.dp)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {

        Text(
            text = "${rawStats.itemsCount} blends, ${rawStats.brandsCount} brands\n" +
                    "${rawStats.favoriteCount} favorites, ${rawStats.dislikedCount} disliked\n" +
                    "${rawStats.totalQuantity} total \"quantity\", " +
                    "${rawStats.totalZeroQuantity} out of stock",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 0.dp, bottom = 12.dp),
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            softWrap = true,
        )

        Text(
            text = totalByType,
            modifier = Modifier
                .fillMaxWidth(fraction = 0.75f)
                .padding(0.dp),
            textAlign = TextAlign.Start,
            softWrap = true
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartsSection(
    rawStats: RawStats,
    filteredStats: FilteredStats,
    viewModel: StatsViewModel
) {
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
    var rawCharts by remember { mutableStateOf(false) }
    var filterCharts by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 0.dp)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Select charts to view:",
            modifier = Modifier
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { filterCharts = false
                    rawCharts = !rawCharts
                },
                modifier = Modifier,
                colors = ButtonDefaults.textButtonColors(
                    contentColor =
                        if (rawCharts) MaterialTheme.colorScheme.onBackground
                        else Color.Unspecified,
                    containerColor =
                        if (rawCharts) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                )
            )
            {
                Text(text = "Raw Stats")
            }
            TextButton(
                onClick = { rawCharts = false
                    filterCharts = !filterCharts
                },
                modifier = Modifier,
                colors = ButtonDefaults.textButtonColors(
                    contentColor =
                        if (filterCharts) MaterialTheme.colorScheme.onBackground
                        else Color.Unspecified,
                    containerColor =
                        if (filterCharts) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                )
            )
            {
                Text(text = "Filterable")
            }
        }
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )

        if (rawCharts) {
            Box(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Blends by Type",
                        modifier = Modifier,
                        fontSize = 15.sp,
                    )
                    PieChart(
                        data = rawStats.totalByType,
                        colors = pieColors,
                        showLabels = true,
                        showPercentages = true,
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 32.dp)
                            .size(250.dp),
                        onSliceLabelPosition = 0.6f,
                        outsideSliceLabelPosition = 0.65f,
                        outsideLabelThreshold = 20f,
                        rotationOffset = 225f,
                        textColor = Color.Black,
                        labelBackground = Color.White.copy(alpha = 0.45f),
                        sortData = true
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                        thickness = 1.dp,
                    )
                    Text(
                        text = "Top Brands (by number of entries)",
                        modifier = Modifier,
                        fontSize = 15.sp,
                    )
                    val topTenBrands = viewModel.getTopTenBrands(rawStats)
                    PieChart(
                        data = topTenBrands,
                        showLabels = true,
                        showPercentages = true,
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 32.dp)
                            .fillMaxWidth(fraction = 0.7f),
                        onSliceLabelPosition = 0.6f,
                        outsideSliceLabelPosition = 0.65f,
                        outsideLabelThreshold = 25f,
                        rotationOffset = 225f,
                        textColor = Color.Black,
                        labelBackground = Color.White.copy(alpha = 0.55f),
                        sortData = true
                    )
                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                    )
                }
            }
        }

        if (filterCharts) {
            Box(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Blends by Type",
                        modifier = Modifier,
                        fontSize = 15.sp,
                    )
                    PieChart(
                        data = filteredStats.entriesByType,
                        colors = pieColors,
                        showLabels = true,
                        showPercentages = true,
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 32.dp)
                            .size(250.dp),
                        onSliceLabelPosition = 0.6f,
                        outsideSliceLabelPosition = 0.65f,
                        outsideLabelThreshold = 20f,
                        rotationOffset = 225f,
                        textColor = Color.Black,
                        labelBackground = Color.White.copy(alpha = 0.45f),
                        sortData = true
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                        thickness = 1.dp,
                    )
                    Text(
                        text = "Top Brands (by number of entries)",
                        modifier = Modifier,
                        fontSize = 15.sp,
                    )
                    PieChart(
                        data = filteredStats.topBrands,
                        showLabels = true,
                        showPercentages = true,
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 32.dp)
                            .fillMaxWidth(fraction = 0.7f),
                        onSliceLabelPosition = 0.6f,
                        outsideSliceLabelPosition = 0.65f,
                        outsideLabelThreshold = 25f,
                        rotationOffset = 225f,
                        textColor = Color.Black,
                        labelBackground = Color.White.copy(alpha = 0.55f),
                        sortData = true
                    )
                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                    )
                }
            }
        }
    }
}


/** Pie Chart stuff*/

@Composable
private fun PieChart(
    data: Map<String, Int>,
    showLabels: Boolean = true,
    showPercentages: Boolean = false,
    modifier: Modifier = Modifier,
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
    onSliceLabelPosition: Float = 0.5f,
    outsideSliceLabelPosition: Float = 0.5f,
    outsideLabelThreshold: Float = 20f,
    rotationOffset: Float = 270f,
    textColor: Color = Color.Black,
    labelBackground: Color = Color.Unspecified,
    sortData: Boolean = true,
) {
    val total = data.values.sum()
    val sortedData = if (sortData) { data.toList().sortedByDescending { it.second }.toMap() } else { data }
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radiusSlice = min(centerX, centerY) * 0.8f // float is padding between canvas bounds and chart
        val insideLabel = min(centerX, centerY) * onSliceLabelPosition
        val outsideLabel = min(centerX, centerY) * (1f + (outsideSliceLabelPosition * 0.3f))

        drawSlices(
            sortedData, colors, total, rotationOffset
        )

        drawLabels(
            sortedData, total, rotationOffset, showLabels, showPercentages, textMeasurer, textColor, labelBackground,
            centerX, centerY, insideLabel, outsideLabel, outsideLabelThreshold,
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
    textMeasurer: TextMeasurer,
    textColor: Color,
    backgroundColor: Color,
    centerX: Float,
    centerY: Float,
    insideRadius: Float,
    outsideRadius: Float,
    outsideLabelThreshold: Float,
) {
    var currentStartAngle = startAngle
    var outsideLabelCount = 0

    data.forEach { (label, value) ->
        val sweepAngle = (value.toFloat() / total) * 360f
        val midpointAngle = currentStartAngle + sweepAngle / 2

        val radius = if (sweepAngle < outsideLabelThreshold) {
            outsideLabelCount++
            outsideRadius
        } else {
            outsideLabelCount = 0
            insideRadius
        }

        val labelColor = if (showLabels) { textColor } else { Color.Transparent }
        val percentColor = if (showLabels && showPercentages) { textColor } else { Color.Transparent }
        val labelBg = if (showLabels) { backgroundColor } else { Color.Transparent }
        val percentBg = if (showLabels && showPercentages) { backgroundColor } else { Color.Transparent }
        val labelPad = " $label "
        val decimalFormat = DecimalFormat(" #.##% ")
        val percentageCal = decimalFormat.format(value.toDouble() / total)

        val textLabel = textMeasurer.measure(
            text = AnnotatedString(labelPad),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                background = labelBg,
                textAlign = TextAlign.Center,
            ),
            constraints = Constraints(maxWidth = 250),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        val percentageLabel = textMeasurer.measure(
            text = AnnotatedString(percentageCal),
            style = TextStyle(
                fontSize = 12.sp,
                background = percentBg
            )
        )

        val labelWidth = textLabel.size.width.toFloat()
        val labelHeight = textLabel.size.height.toFloat()
        val percentageWidth = percentageLabel.size.width.toFloat()
        val percentageHeight = percentageLabel.size.height.toFloat()
        val combinedHeight =
            if (showLabels && showPercentages) { labelHeight + (percentageHeight * .2f)
            } else {
                if (showLabels) { labelHeight } else { percentageHeight }
            }

        val labelX = centerX + radius * cos(Math.toRadians(midpointAngle.toDouble())).toFloat()
        val labelY = centerY + radius * sin(Math.toRadians(midpointAngle.toDouble())).toFloat() - combinedHeight / 2
        val adjustedLabelX = labelX - labelWidth / 2
        val adjustedLabelY = labelY - labelHeight / 2

        val percentageX = adjustedLabelX + (labelWidth - percentageWidth) / 2
        val percentageY = adjustedLabelY + labelHeight

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
                x = percentageX,
                y = percentageY
            )
        )

        currentStartAngle += sweepAngle
    }
}


/** Bar graph stuff */

