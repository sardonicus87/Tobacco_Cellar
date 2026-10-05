package com.sardonicus.tobaccocellar.ui.csvimport

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportResultsScreen (
    totalRecords: Int,
    successfulConversions: Int,
    successfulInsertions: Int,
    successfulUpdates: Int,
    successfulTins: Int,
    updateFlag: Boolean,
    tinFlag: Boolean,
    navigateToHome: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(true) { navigateToHome() }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.import_results_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
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
                .padding(innerPadding)
        ) {
            ImportResultsBody(
                totalRecords = totalRecords,
                successfulConversions = successfulConversions,
                successfulInsertions = successfulInsertions,
                successfulUpdates = successfulUpdates,
                successfulTins = successfulTins,
                updateFlag = updateFlag,
                tinFlag = tinFlag,
                navigateToHome = navigateToHome,
                modifier = modifier
                    .fillMaxSize()
                    .padding(0.dp),
                contentPadding = innerPadding,
            )
        }
    }
}

@SuppressLint("AutoboxingStateCreation")
@Composable
fun ImportResultsBody(
    totalRecords: Int,
    successfulConversions: Int,
    successfulInsertions: Int,
    successfulUpdates: Int,
    successfulTins: Int,
    updateFlag: Boolean,
    tinFlag: Boolean,
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var visibleItemIndex by remember { mutableStateOf(0) }
    val fadeMillis = 500
    var indexCondition by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val totalIndex = when {
            updateFlag && tinFlag -> 7
            tinFlag || updateFlag -> 6
            else -> 5
        }
        indexCondition = totalIndex

        while (visibleItemIndex < totalIndex) {
            delay(850.milliseconds)
            visibleItemIndex++
        }
    }


    Column(
        modifier = modifier
            .padding(contentPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(1.5f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val resultsTitle = stringResource(R.string.import_results_title)
            // Results header
            if (visibleItemIndex <= 0) {
                Text(
                    text = resultsTitle,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Transparent
                )
            }
            AnimatedVisibility(
                visible = visibleItemIndex > 0,
                enter = fadeIn(animationSpec = tween(durationMillis = 350))
            ) {
                Text(
                    text = resultsTitle,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Data
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                // Labels
                Column(
                    modifier = Modifier.padding(0.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val totalFound = stringResource(R.string.total_found)
                    val conversions = stringResource(R.string.successful_conversions)
                    val totalImported = stringResource(R.string.total_imported)

                    if (visibleItemIndex <= 1) {
                        Text(totalFound, fontSize = 18.sp, color = Color.Transparent)
                    }
                    AnimatedVisibility(
                        visible = visibleItemIndex > 1,
                        enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                    ) { Text(totalFound, fontSize = 18.sp) }

                    if (visibleItemIndex <= 2) {
                        Text(conversions, fontSize = 18.sp, color = Color.Transparent)
                    }
                    AnimatedVisibility(
                        visible = visibleItemIndex > 2,
                        enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                    ) { Text(conversions, fontSize = 18.sp) }

                    if (visibleItemIndex <= 3) {
                        Text(totalImported, fontSize = 18.sp, color = Color.Transparent)
                    }
                    AnimatedVisibility(
                        visible = visibleItemIndex > 3,
                        enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                    ) { Text(totalImported, fontSize = 18.sp) }

                    // Updates AND Tins
                    if (indexCondition == 7) {
                        val totalUpdated = stringResource(R.string.total_updated)
                        val totalTinsChange = stringResource(R.string.total_tins_change)

                        // Successful Updates
                        if (visibleItemIndex <= 4) {
                            Text(totalUpdated, Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                        }
                        AnimatedVisibility(
                            visible = visibleItemIndex > 4,
                            enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                        ) { Text(totalUpdated, Modifier.padding(0.dp), fontSize = 18.sp) }

                        // Successful Tins
                        if (visibleItemIndex <= 5) {
                            Text(totalTinsChange, Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                        }
                        AnimatedVisibility(
                            visible = visibleItemIndex > 5,
                            enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                        ) { Text(totalTinsChange, Modifier.padding(0.dp), fontSize = 18.sp) }
                    }

                    // Updates OR Tins
                    if (indexCondition == 6) {
                        val label = if (updateFlag) stringResource(R.string.total_updated) else stringResource(R.string.total_tins_change)
                        if (visibleItemIndex <= 4) {
                            Text(label, Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                        }
                        AnimatedVisibility(
                            visible = visibleItemIndex > 4,
                            enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                        ) { Text(label, Modifier.padding(0.dp), fontSize = 18.sp) }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Counts
                Column(
                    modifier = Modifier.padding(0.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Total Records
                    if (visibleItemIndex <= 1) {
                        Text("$totalRecords", Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                    }
                    AnimatedVisibility(
                        visible = visibleItemIndex > 1,
                        enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                    ) { Text("$totalRecords", Modifier.padding(bottom = 0.dp), fontSize = 18.sp) }

                    // Successful Conversions
                    if (visibleItemIndex <= 2) {
                        Text("$successfulConversions", Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                    }
                    AnimatedVisibility(
                        visible = visibleItemIndex > 2,
                        enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                    ) { Text("$successfulConversions", Modifier.padding(0.dp), fontSize = 18.sp) }

                    // Records Imported
                    if (visibleItemIndex <= 3) {
                        Text("$successfulInsertions", Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                    }
                    AnimatedVisibility(
                        visible = visibleItemIndex > 3,
                        enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                    ) { Text("$successfulInsertions", Modifier.padding(0.dp), fontSize = 18.sp) }

                    // Updates AND Tins
                    if (indexCondition == 7) {
                        // Successful Updates
                        if (visibleItemIndex <= 4) {
                            Text("$successfulUpdates", Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                        }
                        AnimatedVisibility(
                            visible = visibleItemIndex > 4,
                            enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                        ) { Text("$successfulUpdates", Modifier.padding(0.dp), fontSize = 18.sp) }

                        // Successful Tins
                        if (visibleItemIndex <= 5) {
                            Text("$successfulTins", Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                        }
                        AnimatedVisibility(
                            visible = visibleItemIndex > 5,
                            enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                        ) { Text("$successfulTins", Modifier.padding(0.dp), fontSize = 18.sp) }
                    }

                    // Updates OR Tins
                    if (indexCondition == 6) {
                        val count = if (updateFlag) successfulUpdates else successfulTins

                        if (visibleItemIndex <= 4) {
                            Text("$count", Modifier.padding(bottom = 0.dp), fontSize = 18.sp, color = Color.Transparent)
                        }
                        AnimatedVisibility(
                            visible = visibleItemIndex > 4,
                            enter = fadeIn(animationSpec = tween(durationMillis = fadeMillis))
                        ) { Text("$count", Modifier.padding(0.dp), fontSize = 18.sp) }
                    }
                }
            }

            // Navigate to Cellar
            val finalIndex =
                when (indexCondition) {
                    7 -> 6
                    6 -> 5
                    else -> 4
                }

            val backToCellar = stringResource(R.string.back_to_cellar)
            if (visibleItemIndex <= finalIndex) {
                TextButton(
                    onClick = {  },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                ) { Text(backToCellar, fontSize = 25.sp, color = Color.Transparent) }
            }
            AnimatedVisibility(
                visible = visibleItemIndex > finalIndex,
                enter = fadeIn(animationSpec = tween(durationMillis = 350))
            ) {
                TextButton(
                    onClick = { navigateToHome() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                ) { Text(backToCellar, fontSize = 25.sp) }
            }
        }

        Spacer(Modifier.weight(2f))
    }
}