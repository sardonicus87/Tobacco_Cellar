package com.example.tobaccocellar.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarBottomAppBar
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.FilterViewModel
import com.example.tobaccocellar.ui.navigation.NavigationDestination

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
                showMenu = false,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier
                    .padding(0.dp),
                navigateToHome = navigateToHome,
                navigateToAddEntry = navigateToAddEntry,
                filterViewModel = filterViewModel,
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, bottom = 66.dp, start = 0.dp, end = 0.dp)
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
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.quick_stats),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = "These stats are based on totals without regards to quantities"
        )
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
        ){
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Total Blends: ${rawStats.itemsCount}",
                )
                Text(
                    text = "Total Brands: ${rawStats.brandsCount}",
                )
                Text(
                    text = "Total favorites: ${rawStats.favoriteCount}",
                )
                Text(
                    text = "Total disliked: ${rawStats.dislikedCount}",
                )
                Text(
                    text = "Total quantity: ${rawStats.totalQuantity}",
                )
                Text(
                    text = "Total out of stock: ${rawStats.totalZeroQuantity}",
                )
                Text(
                    text = "Totals by type: " + rawStats.totalByType.toList(),
                )
            }
        }
    }
}

