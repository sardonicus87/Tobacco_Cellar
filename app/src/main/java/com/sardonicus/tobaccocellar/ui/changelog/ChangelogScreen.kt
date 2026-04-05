package com.sardonicus.tobaccocellar.ui.changelog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen (
    onNavigateUp: () -> Unit,
    changelogEntries: List<ChangelogEntryData>,
    targetVersion: Int?,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    var scrolling by remember { mutableStateOf(false) }
    var scrollingFinished by rememberSaveable(targetVersion) { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(targetVersion) {
        val index = changelogEntries.indexOfFirst { it.versionCode == targetVersion }

        if (targetVersion != null && index > 1 && !scrollingFinished) {
            scrolling = true
            val offset = with(density) { -16.dp.roundToPx() }

            delay(600)
            listState.animateScrollToItem(index + 1, offset)

            snapshotFlow { listState.isScrollInProgress }.first { !it }
            delay(50)
            scrollingFinished = true
        }
        scrolling = false
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() },
        topBar = {
            CellarTopAppBar(
                title = "Changelog",
                scrollBehavior = scrollBehavior,
                navigateUp = onNavigateUp,
                canNavigateBack = true,
            )
        },
    ) { innerPadding ->
        Box {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                GlowBox(
                    color = GlowColor(Color.Black.copy(alpha = 0.68f)),
                    size = GlowSize(top = 4.dp)
                ) {
                    // log entries
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        state = listState,
                        userScrollEnabled = true,
                    ) {
                        item { Spacer(Modifier.height(12.dp)) }

                        items(
                            items = changelogEntries, key = { it.versionCode }
                        ) {
                            if (it.versionNumber.isNotBlank()) {
                                ChangeLogEntryLayout(
                                    versionNumber = it.versionNumber,
                                    buildDate = it.buildDate,
                                    changes = it.changes,
                                    improvements = it.improvements,
                                    bugFixes = it.bugFixes,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }
            }
            if (scrolling) {
                LoadingIndicator(
                    scrimColor = Color.Black.copy(alpha = 0.38f),
                )
            }
        }
    }
}

@Composable
fun ChangeLogEntryLayout(
    versionNumber: String,
    buildDate: String,
    modifier: Modifier = Modifier,
    changes: List<String> = emptyList(),
    improvements: List<String> = emptyList(),
    bugFixes: List<String> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Version $versionNumber  ($buildDate)",
            modifier = Modifier,
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        HorizontalDivider(Modifier.padding(bottom = 8.dp), 1.dp)

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            if (changes.isNotEmpty()) {
                Text(
                    text = "Changes:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    changes.forEach {
                        Row {
                            Column {
                                Text(
                                    text = "•  ",
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            Column {
                                Text(
                                    text = it,
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    softWrap = true,
                                )
                            }
                        }
                    }
                }
            }
            if (improvements.isNotEmpty()) {
                Text(
                    text = "Improvements:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    improvements.forEach {
                        Row {
                            Column {
                                Text(
                                    text = "•  ",
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            Column {
                                Text(
                                    text = it,
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    softWrap = true,
                                )
                            }
                        }
                    }
                }
            }
            if (bugFixes.isNotEmpty()) {
                Text(
                    text = "Bug Fixes:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    bugFixes.forEach {
                        Row {
                            Column {
                                Text(
                                    text = "•  ",
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            Column {
                                Text(
                                    text = it,
                                    modifier = Modifier,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    softWrap = true,
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}