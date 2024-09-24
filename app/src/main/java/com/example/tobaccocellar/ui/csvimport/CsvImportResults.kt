package com.example.tobaccocellar.ui.csvimport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import kotlinx.coroutines.delay

object CsvImportResultsDestination : NavigationDestination {
    override val route = "import_results"
    override val titleRes = R.string.import_results_title

    const val totalRecordsArg = "total_records"
    const val successCountArg = "success_count"
    const val successfulInsertionsArg = "successful_insertions"

    val routeWithArgs = "$route/{$totalRecordsArg}/{$successCountArg}/{$successfulInsertionsArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportResultsScreen (
    totalRecords: Int,
    successfulConversions: Int,
    successfulInsertions: Int,
    navigateToHome: () -> Unit,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(CsvImportResultsDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateToCsvImport = {},
                navigateToSettings = {},
                navigateUp = onNavigateUp,
                showMenu = false,
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
        ) {
            ImportResultsBody(
                totalRecords = totalRecords,
                successfulConversions = successfulConversions,
                successfulInsertions = successfulInsertions,
                navigateToHome = navigateToHome,
                modifier = modifier
                    .fillMaxSize()
                    .padding(0.dp),
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
fun ImportResultsBody(
    totalRecords: Int,
    successfulConversions: Int,
    successfulInsertions: Int,
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var visibleItemIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (visibleItemIndex < 4) { // (total items to fade in, index 0-3)
            delay(500) // Adjust delay as needed
            visibleItemIndex++
        }
    }

//    AnimatedVisibility(visible = visibleItemIndex > 0, enter = fadeIn()) {
//        Text()
//    }

    Column(
        modifier = modifier
            .padding(contentPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(
            modifier = Modifier
                .weight(1.5f)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "CSV Import Results",
                modifier = Modifier
                    .padding(bottom = 16.dp),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(0.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Total records found: ",
                        modifier = Modifier
                            .padding(0.dp),
                        fontSize = 18.sp,
                    )
                    Text(
                        text = "Successful conversions: ",
                        modifier = Modifier
                            .padding(0.dp),
                        fontSize = 18.sp,
                    )
                    Text(
                        text = "Total records imported: ",
                        modifier = Modifier
                            .padding(0.dp),
                        fontSize = 18.sp,
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(0.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "$totalRecords",
                        modifier = Modifier
                            .padding(bottom = 0.dp),
                        fontSize = 18.sp,
                    )
                    Text(
                        text = "$successfulConversions",
                        modifier = Modifier
                            .padding(0.dp),
                        fontSize = 18.sp,
                    )
                    Text(
                        text = "$successfulInsertions",
                        modifier = Modifier
                            .padding(0.dp),
                        fontSize = 18.sp,
                    )
                }
            }
            TextButton(
                onClick = { navigateToHome() },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = "Back to Cellar",
                    fontSize = 25.sp,
                )
            }
        }
        Spacer(
            modifier = Modifier
                .weight(2f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CsvImportResultsScreenPreview() {
    CellarTopAppBar(
        title = "CSV Import Results",
        canNavigateBack = true,
        navigateToCsvImport = {},
        navigateToSettings = {},
        showMenu = false,
    )
    ImportResultsBody(
        totalRecords = 105, successfulConversions = 104, successfulInsertions = 1, navigateToHome = {},
    )
}