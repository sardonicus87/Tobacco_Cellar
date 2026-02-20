package com.sardonicus.tobaccocellar.ui.filtering.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.ui.ClearAll
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.filtering.OverflowFilterSection
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun ContainerFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.containerAvailable.collectAsState()
    val selected by filterViewModel.sheetSelectedContainer.collectAsState()
    val enabled by filterViewModel.containerEnabled.collectAsState()
    val tinsExist by filterViewModel.tinsExist.collectAsState()
    val nothingLabel by remember(tinsExist) {
        derivedStateOf {
            if (tinsExist) "No containers assigned to any tins." else "No tins assigned to any blends."
        }
    }
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(Unassigned)" } } }

    OverflowFilterSection(
        label = { "Tin Containers" },
        nothingLabel = { nothingLabel },
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedContainer,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = { nothingAssigned },
        clearAll = { filterViewModel.clearAllSelected(ClearAll.CONTAINER) },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}