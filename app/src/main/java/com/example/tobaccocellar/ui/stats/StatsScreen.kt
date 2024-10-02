package com.example.tobaccocellar.ui.stats


import android.icu.text.DecimalFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
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
import java.io.File.separator
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
    navigateToCsvImport: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val rawStats by viewmodel.rawStats.collectAsState()
    val filterViewModel = LocalCellarApplication.current.filterViewModel

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(StatsDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = false,
                navigateToCsvImport = navigateToCsvImport,
                navigateToSettings = navigateToSettings,
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
                .padding(top = 64.dp, bottom = 52.dp, start = 0.dp, end = 0.dp)
        ) {
            StatsBody(
                rawStats = rawStats,
                viewModel = viewmodel,
                modifier = modifier.fillMaxSize(),
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun StatsBody(
    rawStats: RawStats,
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 0.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.quick_stats),
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
        )
        Text(
            text = "${rawStats.itemsCount} blends • ${rawStats.brandsCount} brands • " +
                    "${rawStats.favoriteCount} favorites • ${rawStats.dislikedCount} disliked\n" +
                    "${rawStats.totalQuantity} total quantity • ${rawStats.totalZeroQuantity} out of stock\n",
              //      + rawStats.totalByType.toList().sortedByDescending { it.second }.joinToString(separator = ", ") { "${it.second} ${it.first}" },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 0.dp),
            textAlign = TextAlign.Center,
            softWrap = true,
        )
        Text(
            text = "Charts:",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PieChart(
                    data = rawStats.totalByType,
                    colors = listOf(LocalCustomColors.current.disHeart, Color.Cyan, Color.Green, Color.Yellow, Color.Red),
                    showLabels = true,
                    showPercentages = true,
                    onSliceLabelPosition = 0.6f,
                    outsideSliceLabelPosition = 0.65f,
                    outsideLabelThreshold = 20f,
                    modifier = Modifier
                        .padding(32.dp)
                        .size(250.dp),
                    rotationOffset = 225f,
                    textColor = Color.Black,
                    labelBackground = Color.White.copy(alpha = 0.45f),
                    sortData = true
                )
            }
        }
    }
}

@Composable
private fun PieChart(
    data: Map<String, Int>,
    colors: List<Color>,
    showLabels: Boolean = true,
    showPercentages: Boolean = false,
    onSliceLabelPosition: Float,
    outsideSliceLabelPosition: Float,
    outsideLabelThreshold: Float = 20f,
    modifier: Modifier = Modifier,
    rotationOffset: Float = 0f,
    textColor: Color = Color.Black,
    labelBackground: Color = Color.Unspecified,
    sortData: Boolean = true,
) {
    val total = data.values.sum()
    val sortedData = if (sortData) { data.toList().sortedByDescending { it.second }.toMap() } else { data }
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radiusSlice = min(centerX, centerY) * 0.8f // float is padding between canvas bounds and chart
        val insideLabel = min(centerX, centerY) * onSliceLabelPosition
        val outsideLabel = min(centerX, centerY) * (1f + (outsideSliceLabelPosition * 0.25f))

        drawSlices(
            sortedData, colors, total, rotationOffset,
            centerX, centerY, radiusSlice
        )

        drawLabels(
            sortedData, total, rotationOffset, showLabels, showPercentages, textMeasurer, textColor, labelBackground,
            centerX, centerY, insideLabel, outsideLabel, outsideLabelThreshold
        )
    }
}

private fun DrawScope.drawSlices(
    data: Map<String, Int>,
    colors: List<Color>,
    total: Int,
    startAngle: Float,
    centerX: Float,
    centerY: Float,
    radius: Float
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
    outsideLabelThreshold: Float
) {
    var currentStartAngle = startAngle
    var outsideLabelCount = 0

    data.forEach { (label, value) ->
        val sweepAngle = (value.toFloat() / total) * 360f
        val midpointAngle = currentStartAngle + sweepAngle / 2

        val radius = if (sweepAngle < outsideLabelThreshold) {
            outsideLabelCount++
            if (outsideLabelCount % 2 == 1) { outsideRadius + 1f } else { outsideRadius }
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
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                background = labelBg
            )
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
            if (showLabels && showPercentages) { labelHeight + (percentageHeight / 2)
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
            )
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

