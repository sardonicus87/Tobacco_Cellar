package com.sardonicus.tobaccocellar.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen (
    onNavigateUp: () -> Unit,
    changelogEntries: List<ChangelogEntryData>,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current

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
                    state = rememberLazyListState(),
                    userScrollEnabled = true,
                ) {
                    item { Spacer(Modifier.height(12.dp)) }

                    items(
                        items = changelogEntries, key = { it.versionNumber }
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