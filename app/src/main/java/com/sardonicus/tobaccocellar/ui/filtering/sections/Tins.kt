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
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.CheckboxWithLabel
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun TinsFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val hasTins by filterViewModel.sheetSelectedHasTins.collectAsState()
    val implicitHas by filterViewModel.implicitHasTins.collectAsState()
    val noTins by filterViewModel.sheetSelectedNoTins.collectAsState()
    val opened by filterViewModel.sheetSelectedOpened.collectAsState()
    val unopened by filterViewModel.sheetSelectedUnopened.collectAsState()
    val finished by filterViewModel.sheetSelectedFinished.collectAsState()
    val unfinished by filterViewModel.sheetSelectedUnfinished.collectAsState()

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
        // Has tins/Opened/Finished
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
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Has Tins
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
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
                        )
                        CheckboxWithLabel(
                            text = "Has tins",
                            checked = hasTins,
                            onCheckedChange = filterViewModel::updateSelectedHasTins,
                            modifier = Modifier,
                            enabled = hasEnabled,
                            fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        )
                    }
                    CheckboxWithLabel(
                        text = "No tins",
                        checked = noTins,
                        onCheckedChange = filterViewModel::updateSelectedNoTins,
                        modifier = Modifier,
                        enabled = noEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                }

                // Opened
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    CheckboxWithLabel(
                        text = "Opened",
                        checked = opened,
                        onCheckedChange = filterViewModel::updateSelectedOpened,
                        modifier = Modifier,
                        enabled = openedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                    CheckboxWithLabel(
                        text = "Unopened",
                        checked = unopened,
                        onCheckedChange = filterViewModel::updateSelectedUnopened,
                        modifier = Modifier,
                        enabled = unopenedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                }

                // Finished
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    CheckboxWithLabel(
                        text = "Finished",
                        checked = finished,
                        onCheckedChange = filterViewModel::updateSelectedFinished,
                        modifier = Modifier,
                        enabled = finishedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                    )
                    CheckboxWithLabel(
                        text = "Unfinished",
                        checked = unfinished,
                        onCheckedChange = filterViewModel::updateSelectedUnfinished,
                        modifier = Modifier,
                        enabled = unfinishedEnabled,
                        fontColor = if (!tinsExist) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
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
                            text = "No tins assigned to any blends.",
                            fontSize = 14.sp,
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