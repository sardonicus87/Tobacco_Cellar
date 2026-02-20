package com.sardonicus.tobaccocellar.ui.filtering.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.CheckboxWithLabel
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun ProductionFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val production by filterViewModel.sheetSelectedProduction.collectAsState()
    val outOfProduction by filterViewModel.sheetSelectedOutOfProduction.collectAsState()

    val productionEnabled by filterViewModel.productionEnabled.collectAsState()
    val outOfProductionEnabled by filterViewModel.outOfProductionEnabled.collectAsState()

    Row(
        modifier = modifier
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .width(IntrinsicSize.Max),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        CheckboxWithLabel(
            text = "In Production",
            checked = production,
            onCheckedChange = filterViewModel::updateSelectedProduction,
            modifier = Modifier,
            enabled = productionEnabled
        )
        CheckboxWithLabel(
            text = "Discontinued",
            checked = outOfProduction,
            onCheckedChange = filterViewModel::updateSelectedOutOfProduction,
            modifier = Modifier,
            enabled = outOfProductionEnabled
        )
    }
}