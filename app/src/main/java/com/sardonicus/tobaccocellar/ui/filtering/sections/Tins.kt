package com.sardonicus.tobaccocellar.ui.filtering.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.CheckboxWithLabel
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun TinsFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val hasTins by filterViewModel.selectedHasTins.collectAsState()
    val implicitHas by filterViewModel.implicitHasTins.collectAsState()
    val noTins by filterViewModel.selectedNoTins.collectAsState()
    val opened by filterViewModel.selectedOpened.collectAsState()
    val unopened by filterViewModel.selectedUnopened.collectAsState()
    val finished by filterViewModel.selectedFinished.collectAsState()
    val unfinished by filterViewModel.selectedUnfinished.collectAsState()

    val hasEnabled by filterViewModel.hasTinsEnabled.collectAsState()
    val noEnabled by filterViewModel.noTinsEnabled.collectAsState()
    val openedEnabled by filterViewModel.openedEnabled.collectAsState()
    val unopenedEnabled by filterViewModel.unopenedEnabled.collectAsState()
    val finishedEnabled by filterViewModel.finishedEnabled.collectAsState()
    val unfinishedEnabled by filterViewModel.unfinishedEnabled.collectAsState()

    val tinsExist by filterViewModel.tinsExist.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.Top)
    ) {
        Box{
            Row(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        LocalCustomColors.current.sheetBoxBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                    .fillMaxWidth()
                    .padding(4.dp, 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val textMeasurer = rememberTextMeasurer()
                val baseStyle = LocalTextStyle.current.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal)
                val pad = with(LocalDensity.current) { 42.dp.toPx() }

                val hasText = stringResource(R.string.has_tins)
                val noText = stringResource(R.string.no_tins)
                val openText = stringResource(R.string.opened)
                val unopenText = stringResource(R.string.unopened)
                val finishedText = stringResource(R.string.finished)
                val unfinishedText = stringResource(R.string.Unfinished)

                val hasColumn = remember(hasText, noText) {
                    maxOf(
                        textMeasurer.measure(hasText, baseStyle).size.width.toFloat(),
                        textMeasurer.measure(noText, baseStyle).size.width.toFloat()
                    ) + pad
                }
                val openColumn = remember(openText, unopenText) {
                    maxOf(
                        textMeasurer.measure(openText, baseStyle).size.width.toFloat(),
                        textMeasurer.measure(unopenText, baseStyle).size.width.toFloat()
                    ) + pad
                }
                val finishedColumn = remember(finishedText, unfinishedText) {
                    maxOf(
                        textMeasurer.measure(finishedText, baseStyle).size.width.toFloat(),
                        textMeasurer.measure(unfinishedText, baseStyle).size.width.toFloat()
                    ) + pad
                }
                val total = (hasColumn + openColumn + finishedColumn).coerceAtLeast(1f)

                val hasWeight = hasColumn/total
                val openWeight = openColumn/total
                val finishedWeight = finishedColumn/total

                // Has Tins
                Column(Modifier.weight(hasWeight)) {
                    Box {
                        CheckboxWithLabel(
                            text = "",
                            checked = implicitHas,
                            onCheckedChange = { },
                            modifier = Modifier,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                uncheckedColor = Color.Transparent,
                                disabledUncheckedColor = Color.Transparent
                            ),
                            allowResize = true
                        )
                        CheckboxWithLabel(
                            text = stringResource(R.string.has_tins),
                            checked = hasTins,
                            onCheckedChange = filterViewModel::updateSelectedHasTins,
                            modifier = Modifier,
                            enabled = tinsExist && (hasEnabled || hasTins),
                            fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                            allowResize = true
                        )
                    }
                    CheckboxWithLabel(
                        text = stringResource(R.string.no_tins),
                        checked = noTins,
                        onCheckedChange = filterViewModel::updateSelectedNoTins,
                        modifier = Modifier,
                        enabled = tinsExist && (noEnabled || noTins),
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        allowResize = true
                    )
                }

                // Opened
                Column(Modifier.weight(openWeight)) {
                    CheckboxWithLabel(
                        text = stringResource(R.string.opened),
                        checked = opened,
                        onCheckedChange = filterViewModel::updateSelectedOpened,
                        modifier = Modifier,
                        enabled = tinsExist && (openedEnabled || opened),
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        allowResize = true
                    )
                    CheckboxWithLabel(
                        text = stringResource(R.string.unopened),
                        checked = unopened,
                        onCheckedChange = filterViewModel::updateSelectedUnopened,
                        modifier = Modifier,
                        enabled = tinsExist && (unopenedEnabled || unopened),
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        allowResize = true
                    )
                }

                // Finished
                Column(Modifier.weight(finishedWeight)) {
                    CheckboxWithLabel(
                        text = stringResource(R.string.finished),
                        checked = finished,
                        onCheckedChange = filterViewModel::updateSelectedFinished,
                        modifier = Modifier,
                        enabled = tinsExist && (finishedEnabled || finished),
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        allowResize = true
                    )
                    CheckboxWithLabel(
                        text = stringResource(R.string.Unfinished),
                        checked = unfinished,
                        onCheckedChange = filterViewModel::updateSelectedUnfinished,
                        modifier = Modifier,
                        enabled = tinsExist && (unfinishedEnabled || unfinished),
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        allowResize = true
                    )
                }
            }
            if (!tinsExist) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            Dp.Hairline,
                            LocalCustomColors.current.sheetBoxBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                            RoundedCornerShape(8.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.no_tins_containers),
                            autoSize = TextAutoSize.StepBased(11.sp, 14.sp),
                            maxLines = 1,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}