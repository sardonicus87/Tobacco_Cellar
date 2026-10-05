package com.sardonicus.tobaccocellar.ui.csvimport

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvHelpScreen (
    onNavigateUp: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.import_help),
                scrollBehavior = scrollBehavior,
                navigateUp = onNavigateUp,
                canNavigateBack = true,
            )
        },
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            GlowBox(GlowColor(Color.Black.copy(alpha = 0.68f)), GlowSize(top = 4.dp)) {
                CsvHelpBody(Modifier.fillMaxSize(), scrollState)
            }
        }
    }
}

@Composable
fun CsvHelpBody(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        HorizontalDivider(Modifier.padding(bottom = 12.dp))
        // Verifying data integrity
        Text(
            text = stringResource(R.string.verify_before_import),
            modifier = modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(stringResource(R.string.csv_help1), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help2), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help3), modifier, softWrap = true)


        // Import options //
        Text(
            text = stringResource(R.string.import_options),
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )

        Text(stringResource(R.string.csv_help4), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help5), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help6), modifier, softWrap = true)

        // Existing entries options
        Text(
            text = stringResource(R.string.existing_entries_option),
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(stringResource(R.string.csv_help7), modifier, softWrap = true)
        Column(Modifier.fillMaxWidth().padding(start = 16.dp), Arrangement.spacedBy(0.dp, Alignment.Top)) {
            Row {
                Column(Modifier.width(IntrinsicSize.Max)) {
                    Text(
                        text = "•  ",
                        modifier = modifier,
                        maxLines = 1,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(stringResource(R.string.csv_help8), modifier, softWrap = true)
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.width(IntrinsicSize.Max)) {
                    Text(
                        text = "•  ",
                        modifier = modifier,
                        maxLines = 1,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(stringResource(R.string.csv_help9), modifier, softWrap = true)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.width(IntrinsicSize.Max)) {
                    Text(
                        text = "•  ",
                        modifier = modifier,
                        maxLines = 1,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(stringResource(R.string.csv_help10), modifier, softWrap = true)
                }
            }
        }

        Text(stringResource(R.string.csv_help11), modifier, softWrap = true)


        // Import mapping //
        Text(
            text = stringResource(R.string.import_mapping),
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(stringResource(R.string.csv_help12), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help13), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help14), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help15), modifier, softWrap = true)

        // Tins mapping //
        Text(
            text = stringResource(R.string.tins_mapping),
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(stringResource(R.string.csv_help16), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help17), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help18), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help19), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help20), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help21), modifier, softWrap = true)
        Text(stringResource(R.string.csv_help22), modifier, softWrap = true)

        Spacer(Modifier.height(24.dp))
    }
}