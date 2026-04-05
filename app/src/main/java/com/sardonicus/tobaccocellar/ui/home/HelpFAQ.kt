package com.sardonicus.tobaccocellar.ui.home

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.help_faq_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                showMenu = false,
                modifier = Modifier,
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HelpBody(
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun HelpBody(
    modifier: Modifier = Modifier,
) {
    val columnState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val delayMillis = 210L

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
        state = columnState,
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        item {
            Text(
                text = "Tobacco Cellar was originally intended to be a basic inventory list and \"" +
                        "tobacco passport\" combined with the ease of searching and filtering, and " +
                        "interesting statics. This remains the primary gaol, despite any additional " +
                        "features added since launch or possible future features..",
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp, top = 12.dp),
                softWrap = true,
            )
        }
        item {
            Text(
                text = "Please see the relevant sections for more detailed information.",
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                softWrap = true,
            )
        }
        // Help sections
        item {
            Text(
                text = "Help:",
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                softWrap = true,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }


        item {
            HelpSection(
                title = "Cellar Screen",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(3)
                    }
                },
                content = { CellarScreen() }
            )
        }
        item {
            HelpSection(
                title = "Stats Screen",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(4)
                    }
                },
                content = { StatsPage() }
            )
        }
        item {
            HelpSection(
                title = "Dates Screen",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(5)
                    }
                },
                content = { DatesPage() }
            )
        }
        item {
            HelpSection(
                title = "Filtering",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(6)
                    }
                },
                content = { Filtering() }
            )
        }
        item {
            HelpSection(
                title = "Adding Entries",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(7)
                    }
                },
                content = { AddingItems() }
            )
        }
        item {
            HelpSection(
                title = "Editing Entries",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(8)
                    }
                },
                content = { EditingItems() }
            )
        }
        item {
            HelpSection(
                title = "Adding Tins",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(9)
                    }
                },
                content = { AddingTins() }
            )
        }
        item {
            HelpSection(
                title = "Settings",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(10)
                    }
                },
                content = { Settings() }
            )
        }
        item {
            HelpSection(
                title = "Multi-Device Sync",
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(11)
                    }
                },
                content = { MultiSync() }
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}



/** Sections */
@Composable
private fun CellarScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        modifier = modifier
    ) {
        Text(
            text = "\"Cellar\" is the starting point of the app. Some view options are available " +
                    "in the header, while others are in the settings.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Header",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "The header at the top of the screen contains a view mode switch for changing " +
                    "between list and table modes, a \"Quick Search\" with a search field option, " +
                    "a button to change sorting (list view) or table column visibility (table " +
                    "view), and a count of the current entries based on chosen filtering or searching.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "The search bar icon can be selected to change the field to search. Searching " +
                    "works independently of any chosen filters. To clear search results, tap the " +
                    "\"X\" icon, erase the search text, or use the system back navigation. " +
                    "Previously chosen filters will be re-applied.",
            modifier = Modifier,
            softWrap = true,
        )

        // View Mode
        Text(
            text = "List/Table Views",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "In either view mode, tapping an entry will navigate to the details screen for " +
                    "that entry, long-pressing will show a menu overlay with an option to edit or " +
                    "quick edit. Quick edit swaps the menu to allow editing a select few things " +
                    "without the need to navigate to the full Edit Entry screen. Clear the menu " +
                    "overlay by tapping outside of it or using the system back navigation.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Items can be sorted in list mode by tapping the sort button in the header. " +
                    "Tapping an option sorts ascending, tapping it again switches to descending. " +
                    "Tap the sort icon again to close the list view sorting menu. In Table view, " +
                    "sorting is done by tapping the \"Brand\", \"Blend\", \"Type\", \"Rating\", " +
                    "or \"Qty\" column headers. Default sorting is the order the entries were " +
                    "entered into the database. The first tap will sort the column ascending, " +
                    "the second descending, and the third returns to the default sort order.",
            modifier = Modifier,
            softWrap = true,
        )

        Text(
            text = "The quantity displayed is the \"No. of Tins\" by default. There is an option " +
                    "on the Settings screen to change this to oz/lbs or grams based on the sum " +
                    "of the quantities entered for each tin. If no tins are given for an entry, " +
                    "the \"No. of Tins\" field will be used (converted by the tin conversion rates " +
                    "set on the settings screen.",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun StatsPage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "The \"Stats\" screen gives you an expandable \"Quick Stats\" section (with " +
                    "raw stats on the left and stats based on your filters on the right) and a " +
                    "section of pie charts (which are filter reactive).",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun DatesPage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "The \"Dates\" screen contains various date-related information, all of which " +
                    "is filter-reactive (except the \"Aging Tracker\", which is based on all un-" +
                    "filtered entries). The Dates navigation button will have an indicator dot " +
                    "if any aging tins will be ready within the next 7 days. Viewing the Dates " +
                    "screen will clear this indicator until any other new tins are ready.",
            modifier = Modifier,
            softWrap = true,
        )

        // Aging Tracker
        Text(
            text = "Aging Tracker",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "This section shows blends which are ready to be opened within the next " +
                    "week and month, based on any open dates that were previously set to a " +
                    "future date. See the Adding Tins section for more details.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Tapping on any tin listed on this screen will take you to the details page " +
                    "for the blend that the tin belongs to.",
            modifier = Modifier,
            softWrap = true,
        )

        // Quick Stats
        Text(
            text = "Quick Stats",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "The \"Quick Stats\" are calculated based only on tins which have relevant " +
                    "data added (tins without a date in that field aren't counted). Average age " +
                    "is based on manufacturing date, time in cellar only on the cellar date, and " +
                    "time opened based on tins with open dates that aren't marked as finished. " +
                    "These calculations also do not factor future tins. Average wait time is " +
                    "calculated based on all future set dates.",
            modifier = Modifier,
            softWrap = true,
        )

        // Oldest/Future Tins
        Text(
            text = "Oldest/Future Tins",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "The next two sections list the 5 tins per each date field of past dates from " +
                    "oldest to youngest, and future dates from soonest to latest.",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun Filtering(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "The \"Filter Sheet\" opens on top of the Cellar, Stats and Date screens. Any " +
                    "filters persist through navigation around the app and affect all screens " +
                    "allow filtering (Cellar, Stats, Dates, Bulk Edit, Plaintext), but have no " +
                    "effect on the Cellar screen quick search results. If any filters are " +
                    "applied, this icon will have an indicator dot.",
            modifier = Modifier,
            softWrap = true,
        )

        // Basic Use
        Text(
            text = "Basic Use",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "Multiple filters can be combined and any filterable screen will react " +
                    "instantly to changes. Once you have selected filters, the sheet can be " +
                    "dismissed by tapping outside of it, tapping the close button, or swiping it " +
                    "down.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "The \"Clear All\" button at the bottom will clear all chosen filters.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "The filter sheet has three pages, swipe or flick left/right to swap between " +
                    "pages, or tap the page indicator dot at the top to swap to that page.",
            modifier = Modifier,
            softWrap = true,
        )

        // Filtering
        Text(
            text = "Selecting Filters",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "Filter brands by tapping the name of a brand in the row of brands (row " +
                    "scrolls horizontally). The \"Search Brands\" field is just a filter for this " +
                    "row of brands. Tap the include/exclude button to swap brand filter modes.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Selected brands are shown in the box below and can be removed by tapping " +
                    "the close icon on each brand. Brands that exceed this space are placed in an " +
                    "overflow. To see the full list of selected brands, tap the overflow button. " +
                    "The \"Clear All\" button here only removes the selected brand filters.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "To filter by type, tap one of the types to add it to the filtering, tap it " +
                    "again to remove it. The \"Unassigned\" button will filter for entries that " +
                    "have a blank blend type.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Filter \"Favorites\" or \"Dislikes\" by tapping the check boxes. The first " +
                    "tap will return only those marked as the chosen filter, tapping again will " +
                    "return all entries that are not in that category. Both boxes can be set to " +
                    "include/exclude to see only entries that are marked as being in one or the " +
                    "other, or only entries that aren't marked as being in either.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Below this is the rating range filter, tapping the range display opens a " +
                    "popup for the range options. Unrated will return all entries that haven't " +
                    "been rated. The left and right fields are for min/max rating, respectively. " +
                    "Use one or the other to return all entries that are rated at least/most as " +
                    "the chosen value, or use both to return all entries rated between the max/" +
                    "min. The rating range can also be combined with the unrated option (to see " +
                    "all items that are unrated and only those that are rated within the range).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Filtering by in-stock/out is based on the \"No. of Tins\" field and returns " +
                    "those entries with a value greater than 0 (or 0 for out-of-stock).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Filters for subgenres, cuts, components and flavorings are on the second page. " +
                    "Tap the overflow chip will to show a full list of available selections for " +
                    "that section.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "There is an additional option in components and flavoring for matching type. " +
                    "Selecting \"Any\" will return blends that contain any of the selections, \"" +
                    "All\" will return only those blends that contain all of the selections, and " +
                    "\"Only\" will strictly return those blends that contain only the selections " +
                    "(and no others—exact matching).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Tin-related parameters are on the third page. Some tins filters will also " +
                    "result in a filtered list of matching tins being shown for the relevant " +
                    "blends on the Cellar screen.",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun AddingItems(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "Items can be added in one of two ways: by tapping the \"Add\" button in the " +
                    "navigation bar, or by importing a CSV file through the top bar overflow " +
                    "menu. For more information on adding items by CSV, please see the \"Help\" " +
                    "section on the CSV import screen.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "There are three tabs on the add entry screen that are self-explanatory. " +
                    "Please see the \"Adding Tins\" section for more details on the tins tab.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "All entries require a unique combination of \"Brand\" and \"Blend\". Multiple " +
                    "entries can contain the same brand OR blend, but only one entry can contain " +
                    "the same brand AND blend. All other fields are optional.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Most fields that are left blank will remain blank when clicking save, however " +
                    "leaving the \"No. of Tins\" field blank will presume the quantity to be 1, " +
                    "if you wish this quantity to be 0, you must enter 0. Next to this field, " +
                    "there are \"increase/decrease\" buttons for quickly updating the number of " +
                    "tins.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "The \"Sync?\" check box allows you to synchronize the \"No. of Tins\" field " +
                    "with the total quantities of individual tins/containers (not counting those " +
                    "tins marked as finished). The total tins are calculated based on the Tin " +
                    "Conversion Rates set in the settings screen (default is 1 tin is equal to " +
                    "1.75 oz or 50g).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Tapping the rating bar opens a popup with a text field for entering a rating. " +
                    "The range is 0-5 and does allow fractional numbers (up to 2 decimal places). " +
                    "Clear the text field in order to leave an item unrated.",
            modifier = Modifier,
            softWrap = true,
        )

        Text(
            text = "When adding components or flavorings, please separate each with a comma and a " +
                    "space. For example: \"virginia, burley, perique\".",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun EditingItems(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "Entries can be edited by long-pressing the entry in the cellar screen, using " +
                    "the batch edit option in the top bar menu, tapping edit on the details screen, " +
                    "or through a CSV import and selecting the \"Update\" or \"Overwrite\" option " +
                    "(see help on the CSV Import screen for more information).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "The edit entry screen is the same as the add entry screen and functions the " +
                    "same way, just with the existing data pre-loaded in all of the fields.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Batch editing is limited to a select few fields. The screen contains two " +
                    "tabs, one to select items to edit and another to make the edits. Select the " +
                    "checkbox only for the fields you wish to edit.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "WARNING: Selecting a field and leaving it blank will result in erasing that " +
                    "field for the selected entries (except for the components and flavoring " +
                    "fields)!",
            modifier = Modifier
                .padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.error,
            softWrap = true,
        )
        Text(
            text = "Batch editing components/flavoring works the same as the Add/Edit screens in " +
                    "regards to entering values into the field (separate each with a comma and a " +
                    "space). However here, leaving these blank does not erase anything from " +
                    "entries when you hit save. Any entered components/flavorings will be added " +
                    "(or removed) from the existing values of the selected entries if they aren't " +
                    "already present (or are present in the case of remove). Switch between add " +
                    "and remove by tapping the +/- icon at the end of the field.",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun AddingTins(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        // Basic use
        Text(
            text = "When adding or editing an entry, navigate to the Tins tab on the right and " +
                    "select the \"Add Tin\" button to add a tin. Add additional tins by tapping " +
                    "the \"+\" button below the last tin. To remove a tin, tap the \"-\" icon " +
                    "in the top right corner of the tin.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Each \"tin\" within a given entry must have a unique label. The same label(s) " +
                    "can be reused under different entries. All other fields are optional.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Expand/collapse the details of any given tin using the icon in the top left, " +
                    "or \"expand\" text at the bottom (if collapsed). The tin list will be " +
                    "scrollable if it exceeds the height of the given area.",
            modifier = Modifier,
            softWrap = true,
        )

        // Quantity
        Text(
            text = "Quantity",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "While amount and unit are optional, if you enter an amount, you must also " +
                    "enter a unit. Entering a unit without an amount will default the amount " +
                    "to 0. Do not use grouping separators for 1000 or more, only numbers and " +
                    "your locale-specific decimal separator are allowed.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "This value is used for the \"Cellar Quantity Display\" setting on the main " +
                    "Cellar screen, as well as the total quantity displayed on a Blend Details " +
                    "screen. When using \"Sync\" on the Add/Edit details tab, this quantity is " +
                    "also used with the \"Tin Conversion Rates\" setting to set the \"No. of Tins" +
                    "\" field.",
            modifier = Modifier,
            softWrap = true,
        )

        // Dates
        Text(
            text = "Dates",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "The date fields on this entry screen will show the chosen date as MM/YY if " +
                    "the field is not wide enough, or it will show the date in medium length " +
                    "format based on your locale settings if there is room. Regardless of how it " +
                    "displays here, the full selected day, month and year are saved.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Date selection for any given field is limited by the other fields. If no " +
                    "dates have been entered, then any past or future date can be selected. The " +
                    "first date field to be entered will then start limiting others. The limit " +
                    "schema: Manufacture must be on/before Cellar/Opened, Cellar must be on/" +
                    "between Manufacture and Opened, Opened must be on/after Cellar/Manufacture.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Selecting the \"Finished\" check box will affect some of the stats and dates " +
                    "and the calculated total on Details screens. This checkbox allows you to " +
                    "keep the open date, but exclude the tin from being included in anything " +
                    "related to open dates.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "If you would like to keep track of aging, set the open date to a future date " +
                    "of when you'd like to open the tin. The dates screen will have a section for " +
                    "blends that will be ready to open in a given week/month, see the dates " +
                    "section for more details.",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "Most settings are self-explanatory, but a few might need further clarification.",
            modifier = Modifier,
            softWrap = true,
        )

        // Multi-Device Sync
        Text(
            text = "Multi-Device Sync",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "This option allows you to synchronize changes to your database across " +
                    "multiple devices if you have the app installed on more than one device. " +
                    "Please see the more detailed subsection for this option.",
            modifier = Modifier,
            softWrap = true,
        )

        // Backup/restore
        Text(
            text = "Backup/Restore",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "Backup and Restore both give you two options, \"Database\" and \"Settings\". " +
                    "Selecting database will make a copy of the entire database, and settings " +
                    "will save the app settings (like display settings). One backup file is made " +
                    "regardless of whether you select one or both (both backups will be in one).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "This is an entirely optional process for creating a manual backup. The app " +
                    "does participate in the automatic Google services backup if you have that " +
                    "enabled in your phone's settings, and will save the database and settings. " +
                    "However, a Google backup might not represent the latest data, depending on " +
                    "when scheduled backup was last performed.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "These backups are saved as \".tcbu\" files, and are given a suggested name " +
                    "(that you don't have to use) depending on the selected options.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "When restoring, the same options are presented. You can restore the database, " +
                    "settings, or both from a backup file that contains both, but a database " +
                    "backup file will only restore the database, and a settings file will restore " +
                    "only the settings.",
            modifier = Modifier,
            softWrap = true,
        )

        // Conversion Rates
        Text(
            text = "Tin Conversion Rates",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "Tin Conversion Rates allows you to set the value of what constitutes one " +
                    "\"tin\". You can set the value for ounces and grams separately. This " +
                    "conversion rate is used when calculating the \"No. of Tins\" field when " +
                    "adding individual tins and using the sync option. It is also used on the " +
                    "Statistics screen. Changing the conversion rates will automatically update " +
                    "the \"No. of Tins\" field for all entries with synchronized tins.",
            modifier = Modifier,
            softWrap = true,
        )

        // Default sync
        Text(
            text = "Default \"Sync Tins?\" Option",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "This option sets the default value of the \"Sync?\" option on the Add Entry " +
                    "screen.",
            modifier = Modifier,
            softWrap = true,
        )

        // Other Db operations
        Text(
            text = "Other Db Operations",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "These settings should be unnecessary, but are provided nonetheless. Clean and " +
                    "Optimize database can clear orphaned data, but the database mostly keeps " +
                    "itself clean. The Fix/Update Tin Sync Quantity is provided in the event that " +
                    "these were out of sync for some reason (an older version of the app didn't " +
                    "automatically update the \"No. of Tins\" field for entries with synchronized " +
                    "tins when tin conversion rates were changed for example, though this is " +
                    "automatically done now for all entries when changing rates).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Delete Database will erase all entries in the database, and this cannot be " +
                    "undone.",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

@Composable
private fun MultiSync(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "This option allows you to synchronize changes to your database across " +
                    "multiple devices if you have the app installed on more than one device. " +
                    "This sync is bi-directional, so changes on any device with the option on " +
                    "will be reflected on all other devices.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "For this option to work, each device must be running a version of the app " +
                    "with the same Database Version (app version doesn't matter). The app must " +
                    "also be linked to the same Google account on both devices (even if both " +
                    "devices use a different primary Google account).",
            modifier = Modifier,
            softWrap = true,
        )

        // Explanation of account link/data safety
        Text(
            text = "Data Safety",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "Neither myself nor the app has any access to any of your personal data. " +
                    "Authorization just links the app to your Google Account and grants access " +
                    "to the drive associated with your account for the sole purpose of creating " +
                    "an app-specific folder. The app cannot see anything else in your Google " +
                    "Drive.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "To rescind this linking or drive permission, you must do so from your Google " +
                    "Account settings. Logging out from the app just prevents transfer, but " +
                    "unlinking can ONLY be done from your Google Account settings. This is a " +
                    "Google requirement for safety.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "All data transfer is encrypted and no personal information is even accessed, " +
                    "let alone shared. Database sync data is the only data transferred. I nor " +
                    "any third-parties cannot access any of your data. The app likewise has no " +
                    "access to any of your account or personal data, it has access only to it's " +
                    "own app-specific folder in your Drive.",
            modifier = Modifier,
            softWrap = true,
        )
        val text = "Please see the Privacy Policy↗ and Managing Data↗ for more details."
        val privacy = "Privacy Policy"
        val data = "Managing Data"

        val uriHandler = LocalUriHandler.current
        val hapticFeedback = LocalHapticFeedback.current
        var pressedRange by remember { mutableStateOf<TextRange?>(null) }

        val annotatedString = buildAnnotatedString {
            append(text)
            val privacyStart = text.indexOf(privacy)
            val privacyEnd = privacyStart + privacy.length
            val dataStart = text.indexOf(data)
            val dataEnd = dataStart + data.length

            if (privacyStart != -1) {
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://www.tobacco-cellar.com/privacy-policy",
                    start = privacyStart,
                    end = privacyEnd
                )
                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = privacyStart,
                    end = privacyEnd
                )
            }
            if (dataStart != -1) {
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://www.tobacco-cellar.com/managing-data",
                    start = dataStart,
                    end = dataEnd
                )
                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = dataStart,
                    end = dataEnd
                )
            }

            pressedRange?.let {
                addStyle(
                    style = SpanStyle(
                        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    start = it.start,
                    end = it.end
                )
            }
        }

        var pressedUrl by remember { mutableStateOf<String?>(null) }
        var tooltipPosition by remember { mutableStateOf(IntOffset.Zero) }
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        Box {
            Text(
                text = annotatedString,
                softWrap = true,
                onTextLayout = { layoutResult = it },
                modifier = Modifier
                    .pointerInput(annotatedString) {
                        detectTapGestures(
                            onTap = { offset ->
                                layoutResult?.let { result ->
                                    val index = result.getOffsetForPosition(offset)
                                    annotatedString.getStringAnnotations("URL", index, index).firstOrNull()?.let { range ->
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        uriHandler.openUri(range.item)
                                    }
                                }
                            },
                            onLongPress = { offset ->
                                layoutResult?.let { result ->
                                    val index = result.getOffsetForPosition(offset)
                                    annotatedString.getStringAnnotations("URL", index, index).firstOrNull()?.let { range ->
                                        pressedUrl = range.item
                                        pressedRange = TextRange(range.start, range.end)

                                        val cursorRect = result.getCursorRect(index)
                                        tooltipPosition = IntOffset(offset.x.toInt(), (cursorRect.top - 120).toInt())
                                    }
                                }
                            },
                        )
                    }
            )

            if (pressedUrl != null) {
                LaunchedEffect(Unit) {
                    delay(30000)
                    pressedUrl = null
                    pressedRange = null
                }

                Popup (
                    offset = tooltipPosition,
                    onDismissRequest = {
                        pressedUrl = null
                        pressedRange = null
                    }
                ) {
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val clipboard = LocalClipboard.current

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    uriHandler.openUri(pressedUrl!!)
                                    pressedUrl = null
                                    pressedRange = null
                                },
                                onLongClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "URL",
                                                    pressedUrl!!
                                                )
                                            )
                                        )
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        pressedUrl = null
                                        pressedRange = null
                                    }
                                }
                            )
                    ) {
                        Text(
                            text = pressedUrl!!,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        // How it Works
        Text(
            text = "How It Works",
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "All Google accounts have a Google Drive associated with them (regardless of " +
                    "whether or not you've used it). This device sync option will request access " +
                    "to Google Drive. You do not need the Google Drive app installed for it to " +
                    "work. Granting access allows the app to create an app-specific folder on " +
                    "your Google Drive as a cloud location to transfer between devices. This app-" +
                    "specific folder is the only folder the app will have access to, and this " +
                    "folder will not count toward your Google Drive storage space.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "When you make changes to entries, those changes include a timestamp. The " +
                    "changes will be uploaded to the drive folder according to your data settings " +
                    "(it won't use mobile data unless you turn that option on). If it is unable " +
                    "to upload, it will try again later when it can connect based on your settings.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "The app checks for sync data (if the option is on) whenever it is started, " +
                    "and additionally checks twice per day (12-hour interval). If conflicting " +
                    "data is found (the same entry was edited in both devices before either had " +
                    "a chance to sync), only the option with the latest timestamp will be used. " +
                    "The changes are considered per entry, so if on one device you write a note, " +
                    "then on the other you change a component, the note will be lost (as the " +
                    "timestamp for that note change is earlier than the timestamp for the " +
                    "component change). The timestamp is per entry, not per logged change!",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Remote data can only be cleared within the app. Please clear any data before " +
                    "logging out if you wish to stop using the sync function.",
            modifier = Modifier,
            softWrap = true,
        )
    }
}

/** Components **/
// Section  layout //
@Composable
private fun HelpSection(
    title: String,
    modifier: Modifier = Modifier,
    onExpanded: () -> Unit = {},
    content: @Composable (() -> Unit),
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(
                indication = LocalIndication.current,
                interactionSource = null
            ) {
                visible = !visible
                if (visible) onExpanded()
            }
            .fillMaxWidth()
            .background(color = LocalCustomColors.current.backgroundVariant),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            painter = painterResource(id = if (visible) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up),
            tint = if (visible)MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
            contentDescription = "Expanded icon",
            modifier = Modifier
                .padding(0.dp)
                .size(22.dp)
        )
    }
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 200),
            expandFrom = Alignment.Bottom) + fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 200),
            shrinkTowards = Alignment.Bottom) + fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            content()
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { visible = !visible },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.double_up),
                    contentDescription = "Collapse",
                    modifier = Modifier
                        .size(18.dp),
                    tint = LocalContentColor.current.copy(alpha = 0.75f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}