package com.example.tobaccocellar.ui.stats


import android.icu.text.DecimalFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
                modifier = modifier
                    .fillMaxSize(),
            )
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
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
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(
            modifier = Modifier
                .height(10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 24.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            // Raw Stats
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Raw Stats",
                        modifier = Modifier
                            .padding(vertical = 2.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                RawStats(
                    rawStats = rawStats,
                    modifier = Modifier
                )
            }

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            // Filtered Stats
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Filtered Stats",
                        modifier = Modifier
                            .padding(vertical = 2.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                FilteredStats(
                    filteredStats = filteredStats,
                    modifier = Modifier
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        // Charts
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
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        ChartsSection(
            filteredStats = filteredStats,
        //    viewModel = viewModel
        )

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RawStats(
    rawStats: RawStats,
    modifier: Modifier = Modifier
) {
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(
            modifier = Modifier
                .height(2.dp)
        )
        Text(
            text = "${rawStats.itemsCount} blends, ${rawStats.brandsCount} brands\n" +
                    "${rawStats.favoriteCount} favorites, ${rawStats.dislikedCount} disliked\n" +
                    "${rawStats.totalQuantity} total \"quantity\"\n" +
                    "${rawStats.totalZeroQuantity} out of stock",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 0.dp, bottom = 12.dp),
            fontSize = 15.sp,
            textAlign = TextAlign.Start,
            softWrap = true,
        )

        Text(
            text = totalByType,
            modifier = Modifier
                .fillMaxWidth(fraction = 0.75f)
                .padding(0.dp),
            fontSize = 15.sp,
            textAlign = TextAlign.Start,
            softWrap = true
        )
        Spacer(
            modifier = Modifier
                .height(8.dp)
        )
    }
}

@Composable
fun FilteredStats(
    filteredStats: FilteredStats,
    modifier: Modifier = Modifier
) {
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(
            modifier = Modifier
                .height(2.dp)
        )
        Text(
            text = "${filteredStats.itemsCount} blends, ${filteredStats.brandsCount} brands\n" +
                    "${filteredStats.favoriteCount} favorites, " + "${filteredStats.dislikedCount} disliked\n" +
                    "${filteredStats.totalQuantity} total \"quantity\"\n" +
                    "${filteredStats.totalZeroQuantity} out of stock",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 0.dp, bottom = 12.dp),
            fontSize = 15.sp,
            textAlign = TextAlign.Start,
            softWrap = true,
        )

        Text(
            text = totalByTypeFiltered,
            modifier = Modifier
                .fillMaxWidth(fraction = 0.75f)
                .padding(0.dp),
            fontSize = 15.sp,
            textAlign = TextAlign.Start,
            softWrap = true
        )


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
//    viewModel: StatsViewModel
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
            text = "*Charts are filter-reactive and display a maximum of the top 10 results. " +
                    "Some charts may be redundant/irrelevant depending on the chosen filters.",
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
                chartData = filteredStats.topBrands
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                thickness = 1.dp,
            )
            ChartsFormat(
                label = "Brands by Quantity",
                chartData = filteredStats.brandsByQuantity
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                thickness = 1.dp,
            )
            ChartsFormat(
                label = "Blends by Type",
                chartData = filteredStats.entriesByType
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                thickness = 1.dp,
            )
            ChartsFormat(
                label = "Blends by Rating",
                chartData = filteredStats.entriesByRating
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 28.dp),
                thickness = 1.dp,
            )
            ChartsFormat(
                label = "Types by Quantity",
                chartData = filteredStats.typesByQuantity
            )

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
)
{
    val countVal = chartData.values.sum()
    var showValue = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            modifier = Modifier,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "(Data Total: ${countVal} - ",
                modifier = Modifier,
                fontSize = 12.sp,
                textAlign = TextAlign.Start
            )
            Text(
                text = "Show Values",
                modifier = Modifier
                    .clickable(
                    onClick = { showValue.value = !showValue.value }
                ),
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.primary,
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
                .padding(top = 24.dp, bottom = 32.dp)
                .fillMaxWidth(fraction = 0.7f),
            onSliceLabelPosition = 0.75f,
            outsideSliceLabelPosition = 0.65f,
            outsideLabelThreshold = 25f,
            rotationOffset = 225f,
            textColor = Color.Black,
            labelBackground = Color.White.copy(alpha = 0.55f),
            sortData = true
        )
    }
}


/** Pie Chart stuff*/

@Composable
private fun PieChart(
    data: Map<String, Int>,
    showLabels: Boolean = true,
    showPercentages: Boolean = true,
    showValues: Boolean = false,
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
    // Higher float value results in larger slices pushing labels outside
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
    //    val radiusSlice = min(centerX, centerY) * 0.8f // float is padding between canvas bounds and chart
        val insideLabel = min(centerX, centerY) * onSliceLabelPosition
        val outsideLabel = min(centerX, centerY) * (1f + (outsideSliceLabelPosition * 0.3f))

        drawSlices(
            sortedData, colors, total, rotationOffset
        )

        drawLabels(
            sortedData, total, rotationOffset, showLabels, showPercentages, showValues, textMeasurer, textColor, labelBackground,
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
    showValues: Boolean,
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
        val valuePad = "($value) "
        val decimalFormat = DecimalFormat(" #.##% ")
        val percentageCal =
            if (showValues) { decimalFormat.format(value.toDouble() / total) + valuePad } else { decimalFormat.format(value.toDouble() / total) }

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

