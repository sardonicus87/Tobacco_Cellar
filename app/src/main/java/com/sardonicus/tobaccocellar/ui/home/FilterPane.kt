package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import com.sardonicus.tobaccocellar.FilterLayout
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.FilterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPane(
    modifier: Modifier = Modifier,
    filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current

    val brandsData by filterViewModel.brandsData.collectAsState()
    val typeData by filterViewModel.typeData.collectAsState()
    val otherData by filterViewModel.otherData.collectAsState()
    val subgenreData by filterViewModel.subgenreData.collectAsState()
    val cutData by filterViewModel.cutData.collectAsState()
    val componentData by filterViewModel.componentData.collectAsState()
    val flavoringData by filterViewModel.flavoringData.collectAsState()
    val tinsData by filterViewModel.tinsFilterData.collectAsState()
    val containerData by filterViewModel.containerData.collectAsState()
    val productionData by filterViewModel.productionData.collectAsState()

    val favDisExist by filterViewModel.favDisExist.collectAsState()
    val tins by filterViewModel.tinsExist.collectAsState()
    val hasContainer = remember(containerData) { containerData.selected.isNotEmpty() }
    val filtersApplied by filterViewModel.isFilterApplied.collectAsState()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) {
                focusManager.clearFocus()
            }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FilterLayout(
                sheetLayout = false,
                filterViewModel = filterViewModel,
                filtersApplied = filtersApplied,
                brandsData = brandsData,
                typeData = typeData,
                otherData = otherData,
                favDisExist = favDisExist,
                subgenreData = subgenreData,
                cutData = cutData,
                componentData = componentData,
                flavoringData = flavoringData,
                containerData = containerData,
                tinsFilterData = tinsData,
                updateSelectedTins = { string, it -> filterViewModel.updateSelectedTins(string, it) },
                hasContainer = hasContainer,
                tins = tins,
                productionData = productionData,
                modifier = Modifier
            )
        }
    }
}