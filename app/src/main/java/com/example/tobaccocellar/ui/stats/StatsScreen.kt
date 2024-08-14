package com.example.tobaccocellar.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarBottomAppBar
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination

object StatsDestination : NavigationDestination {
    override val route = "stats"
    override val titleRes = R.string.stats_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigateToHome: () -> Unit,
//    navigateToStats: () -> Unit,
    navigateToAddEntry: () -> Unit,
//    navigateToSettings: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val statsUiState by viewmodel.statsUiState.collectAsState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                navigateToHome = navigateToHome,
//                navigateToStats = navigateToStats,
                navigateToAddEntry = navigateToAddEntry,
            )
        },
    ) { innerPadding ->
        StatsBody(
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun StatsBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.stats_nothing),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(contentPadding),
        )
    }
}