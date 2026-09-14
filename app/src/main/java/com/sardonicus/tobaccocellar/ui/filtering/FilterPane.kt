package com.sardonicus.tobaccocellar.ui.filtering

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sardonicus.tobaccocellar.ui.FilterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPane(
    filterViewModel: FilterViewModel,
    twoPane: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) { FilterLayout(filterViewModel, twoPane, paginateLayout = false) }
    }
}