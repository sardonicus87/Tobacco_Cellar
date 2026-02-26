package com.sardonicus.tobaccocellar.ui.csvimport

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.CellarTopAppBar
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
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = "CSV Import Help",
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
            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = 0.68f)),
                size = GlowSize(top = 4.dp)
            ) {
                CsvHelpBody(
                    modifier = Modifier
                        .fillMaxSize(),
                    scrollState = scrollState,
                )
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
            text = "Verifying Data before Import",
            modifier = modifier
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        Text(
            text = "After selecting a file, a box at the top will show you the first line " +
                    "file and label it \"Possible header\" (if there is a header, it is " +
                    "always the first line), followed by another line labelled \"Record parse " +
                    "test\". This is a preview of the file being read, use it to determine if " +
                    "there is a header or not and that the records are being read correctly.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "If there is no header, \"Possible header\" will look like a record. " +
                    "If the record parse test looks wrong, try reformatting your CSV file to " +
                    "RFC 4180 formatting to ensure a successful read.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "In the import options section, there is also a \"Record count\" that shows " +
                    "the number of lines (records) in the file. If you know roughly how many " +
                    "there should be, this can also confirm the file is being read correctly.",
            modifier = modifier,
            softWrap = true,
        )


        // Import options //
        Text(
            text = "Import Options",
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )

        Text(
            text = "Use the \"Has header?\" checkbox to skip the first line if it is a header. " +
                    "The record count on the right will decrease by 1 if the \"Has header?\" " +
                    "option is chosen.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "Use the \"Collate tins?\" option to attempt to parse the individual records " +
                    "as tins if your CSV has a separate line for each tin (individual tins are " +
                    "handled separately in the database and attached to entries). Each line " +
                    "for any given Brand/Blend combination will generate a tin for that entry.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "Use the \"Sync tins?\" option to synchronize the \"No. of Tins\" field " +
                    "with the quantities of the imported tins (if you wish the \"No of Tins\" to " +
                    "be reflective of quantity for stats). This will be determined by adding up " +
                    "the total quantities of the tins and dividing by the tin conversion rates " +
                    "set on the settings screen (default is 1 tin = 1.75 oz or 50 grams).",
            modifier = modifier,
            softWrap = true,
        )

        // Existing entries options
        Text(
            text = "Existing Entries Options",
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "These options are for how to handle records in the CSV file that already " +
                    "exist in the app database. Each entry in the database must be a unique " +
                    "combination of Brand + Blend. For all options, any new entries will always " +
                    "be imported, these are only for entries that already exist. The options are:",
            modifier = modifier,
            softWrap = true,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        ) {
            Row {
                Column {
                    Text(
                        text = "• ",
                        modifier = modifier,
                        softWrap = true,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(
                        text = "Skip: Skips importing the records that match existing entries " +
                                "(based solely on Brand + Blend).",
                        modifier = modifier,
                        softWrap = true,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "• ",
                        modifier = modifier,
                        softWrap = true,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(
                        text = "Update: Updates only the blank fields in the database with " +
                                "values in the CSV.",
                        modifier = modifier,
                        softWrap = true,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "• ",
                        modifier = modifier,
                        softWrap = true,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(
                        text = "Overwrite: replaces only the selected fields (checkbox on " +
                                "the right) with the values in the CSV file, including " +
                                "overwriting entry data with blank/empty values.",
                        modifier = modifier,
                        softWrap = true,
                    )
                }
            }
        }

        Text(
            text = "WARNING: Using \"Overwrite\" with \"Collate tins\" will always delete all " +
                    "existing tins and replace them with tins generated from CSV records. If you " +
                    "wish to update entries while maintaining your existing tins, do not use " +
                    "Collate with Overwrite.",
            modifier = modifier,
            softWrap = true,
        )


        // Import mapping //
        Text(
            text = "Import Mapping",
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "The labels on the left represent the fields in the database. Use the " +
                    "dropdown boxes to select which CSV column to map to which field. " +
                    "All fields other than \"Brand\" and \"Blend\" are optional.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "The checkboxes to the right are for the \"Overwrite\" option. " +
                    "Only those fields that are checked will be allowed to be overwritten.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "If you select the tin collation option, the \"No. of Tins\" field can " +
                    "take a different value than the tin quantity field if you wish to keep the " +
                    "number of tins separate (only whole numbers will map). This field will " +
                    "be disabled if you select the \"Sync Tins?\" option.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "For the rating, select the column that contains the rating and in the \"Max " +
                    "Value\" field, enter the highest possible rating you could give (used for " +
                    "re-scaling your rating system to the one used in the app).",
            modifier = modifier,
            softWrap = true,
        )


        // Tins mapping //
        Text(
            text = "Tins Mapping",
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "\"Tins\" here represents any container, it doesn't have to actually be " +
                    "in a tin.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "The tins mapping fields are enabled by selecting the \"collate tins\" " +
                    "option. When collating, the CSV is iterated through and 1 tin is added per " +
                    "each line that contains the same Brand + Blend (a tin label will be " +
                    "automatically generated). Upon import, the tins will be linked to " +
                    "their parent Brand + Blend entries.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "Tin collation will only work properly if each individual tin has its " +
                    "own line in the CSV. The \"Skip\" option with tin collation will only " +
                    "create tins for new entries. The \"Update\" option will only create " +
                    "tins for entries that have no attached tins, and the \"Overwrite\" " +
                    "option will erase all existing tins and will create tins for all items " +
                    "regardless of whether or not they had tins before.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "The quantity field will attempt to separate the numerical quantity from " +
                    "the unit and map them to the respective tin fields of \"quantity\" and " +
                    "\"unit\". For instance, if the column containing your tin quantities has " +
                    "a value of \"12 oz\", the tin quantity field will be \"12\" and the unit " +
                    "field will be \"oz\".",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "Date mapping is not guaranteed. Select the date format that was used in " +
                    "the CSV column that contains the date. Several date formats are " +
                    "currently supported. Date formats will work regardless of month/day/year " +
                    "delimiter (/, -, .), or if days have leading 0's. The date format " +
                    "applies to all three fields.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "Only map dates if your manufacture, cellar or open date values each have " +
                    "their own columns and that the dates themselves are in one column (month/" +
                    "day/year in same column—date parsing where day/month/years are split across " +
                    "different columns is not supported.",
            modifier = modifier,
            softWrap = true,
        )
        Text(
            text = "For dates that have two digit years, all years are presumed to be after " +
                    "1900 and before the current year. If the two digit year value is " +
                    "greater than the current year, it's presumed to be 19__; if it is less " +
                    "than or equal to the current year, it is presumed to be 20__.",
            modifier = modifier,
            softWrap = true,
        )

        Spacer(Modifier.height(24.dp))
    }
}