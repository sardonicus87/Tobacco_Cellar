package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

object HelpDestination : NavigationDestination {
    override val route = "help"
    override val titleRes = R.string.help_faq_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(HelpDestination.titleRes),
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(0.dp)
    ) {
        Text(
            text = "Tobacco Cellar was originally intended to be a basic inventory list and \"" +
                    "tobacco passport\" combined with the ease of searching and filtering, and " +
                    "interesting statics. This remains the primary gaol, despite any additional " +
                    "features added since launch or possible future features..",
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp),
            softWrap = true,
        )
        Text(
            text = "Please see the relevant sections for more detailed information.",
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            softWrap = true,
        )
        // Help section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
            modifier = modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            Text(
                text = "Help:",
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                softWrap = true,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            HelpSection(
                title = "Cellar Screen",
                content = { CellarScreen() }
            )
            HelpSection(
                title = "Stats Screen",
                content = { StatsPage() }
            )
            HelpSection(
                title = "Dates Screen",
                content = { DatesPage() }
            )
            HelpSection(
                title = "Filtering",
                content = { Filtering() }
            )
            HelpSection(
                title = "Adding Items",
                content = { AddingItems() }
            )
            HelpSection(
                title = "Editing Items",
                content = { EditingItems() }
            )
            HelpSection(
                title = "Adding Tins",
                content = { AddingTins() }
            )
            HelpSection(
                title = "Settings",
                content = { Settings() }
            )
        }
        Spacer(Modifier.height(12.dp))
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
                    "a sort button (only for list view), and a count of the current entries based " +
                    "on chosen filtering or searching.",
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
                    "that entry, long-pressing will show a menu overlay with an option to edit. " +
                    "Clear the menu overlay by tapping outside of it or using the system back " +
                    "navigation.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Items can be sorted in list mode by tapping the sort button in the header. " +
                    "Tapping an option sorts ascending, tapping it again switches to descending. " +
                    "Tap the sort icon again to close the list view sorting menu." +
                    "In Table View, sorting is done by tapping the \"Brand\", \"Blend\", \"Type\"" +
                    ", \"Rating\", or \"Qty\" column headers. Default sorting is the order the " +
                    "entries were entered into the database. The first tap will sort the column " +
                    "ascending, the second descending, and the third returns to the default sort order.",
            modifier = Modifier,
            softWrap = true,
        )

        Text(
            text = "The quantity displayed is the \"No. of Tins\" by default. There is an option " +
                    "on the settings screen to change this being oz/lbs or grams based on the sum " +
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
                    "calculated based on all future dates.",
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
                    "filters persist through navigation around the app and affect all three of " +
                    "these main screens (but have no effect on the quick search). If any filters " +
                    "are applied, this icon will have an indicator dot.",
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
            text = "Multiple filters can be combined and these main screens will all react " +
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
            text = "The filter sheet has three pages, swipe/flick left/right to swap between pages, " +
                    "or tap the page indicator dot at the top to swap to that page.",
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
                    "tap again to remove it. The \"Unassigned\" button will filter for entries " +
                    "that have a blank blend type.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Filter \"Favorites\" or \"Dislikes\" by tapping the check boxes. Unchecked, " +
                    "is no filtering by these, the first tap will return only those marked as " +
                    "favorites or dislikes, tapping again will return all entries that are not in " +
                    "that category. Check both to see only entries that are either favorite or " +
                    "disliked or to exclude all favorites and dislikes.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Below this is the rating range filter, tapping the range display opens a " +
                    "popup for the range options. Unrated will return all entries that haven't " +
                    "been rated. Entering a value in the left field will return all entries that " +
                    "are rated at that amount or more, entering in the right field returns all  " +
                    "entries that are rated at that value and below, while entering both fields " +
                    "returns all entries that are rated between those values. The rating range is " +
                    "not mutually exclusive with unrated (to see all items that are unrated and " +
                    "that are rated below a certain level, tap both the unrated box and enter the " +
                    "maximum rating in the right box).",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Filtering by in-stock/out is based on the \"No. of Tins\" field.",
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
                    "leaving the \"No. of Tins\" field blank will presume the quantity to be 1. " +
                    "Next to this field, there are \"increase/decrease\" buttons for quickly " +
                    "updating the number of tins.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "The \"Sync?\" check box allows you to synchronize the No. of Tins field with " +
                    "the total quantities of individual tins/containers. The total tins are " +
                    "calculated based on the Tin Conversion Rates set in the settings screen (" +
                    "default is 1.75 oz or 50g per tin).",
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
            text = "Items can be edited by long-pressing the entry in the cellar screen, using " +
                    "the bulk edit option in the top bar menu, tapping edit on the details screen, " +
                    "or through a CSV import and selecting the \"Update\" or \"Overwrite\" option " +
                    "(see help file on the CSV Import screen for more information).",
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
                    "checkbox only for the fields you wish to edit. Selecting a field and leaving " +
                    "it blank will result in erasing that field for the selected entries, with " +
                    "exception of the components and flavoring fields.",
            modifier = Modifier,
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
                    "the \"+\" button below the given tin. To remove a tin, tap the \"-\" icon " +
                    "in the top right corner of the tin.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Each \"tin\" must have a unique label within a given entry. When collating " +
                    "tins in CSV import, the label will be automatically generated as \"Lot __\". " +
                    "The same label can be reused under different entries. All other fields are " +
                    "optional.",
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
        Text(
            text = "Tin Conversion Rates allows you to set the value of what constitutes one " +
                    "\"tin\". You can set the value for ounces and grams separately. This " +
                    "conversion rate is used when calculating the \"No. of Tins\" field when " +
                    "adding individual tins and using the sync option. It is also used on the " +
                    "Statistics screen.",
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = "Clean and Optimize Database cleans up the tables of any potentially orphaned " +
                    "data (such as components that are no longer attached to any blends) and " +
                    "also runs the SQL \"vacuum\" command. This option will have its greatest " +
                    "effect if you have updated or deleted a lot of entries, otherwise it won't " +
                    "have much of an effect beyond cleaning up orphaned data. You don't need to " +
                    "use this very often, if ever (except to clean up orphaned components).",
            modifier = Modifier,
            softWrap = true,
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
        Text(
            text = "Delete Database will erase all entries in the database, and this cannot be " +
                    "undone.",
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
    content: @Composable (() -> Unit),
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(
                indication = LocalIndication.current,
                interactionSource = null
            ) { visible = !visible }
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