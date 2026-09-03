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
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun SubgenreSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.subgenreAvailable.collectAsState()
    val selected by filterViewModel.selectedSubgenres.collectAsState()
    val enabled by filterViewModel.subgenresEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(Unassigned)" } } }

    OverflowFilterSection(
        label = "Subgenre",
        nothingLabel = "No subgenres assigned to any blends.",
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedSubgenre,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = nothingAssigned,
        clearAll = { filterViewModel.clearAllSelected(ClearAll.SUBGENRE) },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
fun CutSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.cutAvailable.collectAsState()
    val selected by filterViewModel.selectedCuts.collectAsState()
    val enabled by filterViewModel.cutsEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(Unassigned)" } } }

    OverflowFilterSection(
        label = "Cut",
        nothingLabel = "No cuts assigned to any blends.",
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedCut,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = nothingAssigned,
        clearAll = { filterViewModel.clearAllSelected(ClearAll.CUT) },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}

@Composable
fun ComponentSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.componentAvailable.collectAsState()
    val selected by filterViewModel.selectedComponents.collectAsState()
    val enabled by filterViewModel.componentsEnabled.collectAsState()
    val matching by filterViewModel.compMatching.collectAsState()
    val matchEnablement by filterViewModel.compMatchingEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(None Assigned)" } } }

    OverflowFilterSection(
        label = "Components",
        nothingLabel = "No components assigned to any blends.",
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedComponent,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = nothingAssigned,
        matching = matching,
        matchOptionEnablement = { matchEnablement },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        clearAll = { filterViewModel.clearAllSelected(ClearAll.COMPONENT) },
        onMatchOptionChange = filterViewModel::updateCompMatching,
    )
}

@Composable
fun FlavoringSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.flavoringAvailable.collectAsState()
    val selected by filterViewModel.selectedFlavorings.collectAsState()
    val enabled by filterViewModel.flavoringsEnabled.collectAsState()
    val matching by filterViewModel.flavorMatching.collectAsState()
    val matchEnablement by filterViewModel.flavorMatchingEnabled.collectAsState()
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(None Assigned)" } } }

    OverflowFilterSection(
        label = "Flavorings",
        nothingLabel = "No flavorings assigned to any blends.",
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedFlavoring,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = nothingAssigned,
        matching = matching,
        matchOptionEnablement = { matchEnablement },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        clearAll = { filterViewModel.clearAllSelected(ClearAll.FLAVORING) },
        onMatchOptionChange = filterViewModel::updateFlavorMatching,
    )
}

@Composable
fun ContainerFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val available by filterViewModel.containerAvailable.collectAsState()
    val selected by filterViewModel.selectedContainer.collectAsState()
    val enabled by filterViewModel.containerEnabled.collectAsState()
    val tinsExist by filterViewModel.tinsExist.collectAsState()
    val nothingLabel by remember(tinsExist) {
        derivedStateOf {
            if (tinsExist) "No containers assigned to any tins."
            else "No tins assigned to any blends."
        }
    }
    val nothingAssigned by remember(available) { derivedStateOf { available.none { it != "(Unassigned)" } } }

    OverflowFilterSection(
        label = "Tin Containers",
        nothingLabel = nothingLabel,
        available = { available },
        selected = { selected },
        enabled = { enabled },
        updateSelectedOptions = filterViewModel::updateSelectedContainer,
        overflowCheck = filterViewModel::overflowCheck,
        nothingAssigned = nothingAssigned,
        clearAll = { filterViewModel.clearAllSelected(ClearAll.CONTAINER) },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}