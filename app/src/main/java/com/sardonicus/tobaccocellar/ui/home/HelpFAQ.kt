package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
            text = "Tobacco Cellar was originally intended to be a basic inventory tracker and \"" +
                    "tobacco passport\", combined with the ease of searching and filtering and " +
                    "statistical analysis, with varying degrees of verbosity.",
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
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = modifier
//                .fillMaxWidth()
//                .padding(0.dp)
//        ) {
//            Text(
//                text = "FAQ:",
//                modifier = Modifier
//                    .padding(horizontal = 12.dp, vertical = 12.dp),
//                softWrap = true,
//                fontWeight = FontWeight.Bold,
//                fontSize = 20.sp,
//            )
//            HelpSection(
//                title = "Basic Use",
//                content = { BasicUse() }
//            )
//        }
    }
}



/** Sections */
@Composable
private fun BasicUse(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "Tobacco Cellar is intended to be a basic inventory tracker and \'tobacco " +
                    "passport\", combined with the ease of searching and filtering. The future " +
                    "will bring optional detailed cellar tracking (dates and more).",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The \"Filtering\" button at the bottom of the screen applies filtering " +
                    "globally, persisting between the \"Cellar\" and \"Stats\" screens. The " +
                    "\"Blend Search\" bar at the top allows quick searching by Blend name (more " +
                    "details in the \"Cellar Screen\" section).",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Please see the relevant sections for more detailed information.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
    }
}

@Composable
private fun CellarScreen(
    modifier: Modifier = Modifier,
) {
    var showFullscreen by remember { mutableStateOf(false) }
    var fullImage by remember { mutableStateOf<Painter?>(null) }
    val viewMode = painterResource(id = R.drawable.help_view_mode)
    val listMenu = painterResource(id = R.drawable.help_list_menu)
    val tableMenu = painterResource(id = R.drawable.help_table_1)
    val tableMenu2 = painterResource(id = R.drawable.help_table_2)

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Cellar is the starting point of the app. There are two view options, " +
                    "List and Table, which can be switched in the header.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Tapping the view icon in the header switches between list and table modes. " +
                    "Next to this is a \"Quick Search\". This search only returns results based  " +
                    "on the chosen field, and works independently of filtering. To change search " +
                    "fields, tap the search icon on the left for a menu. To return to the full " +
                    "list/table, click the clear button, erase the search text, or tap the system " +
                    "back button. Any previously chosen filters will be re-applied.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Next to the Quick Search is another icon, which is used to change the sorting " +
                    "in the list view mode. The default sorting returns items in the order in " +
                    "which they were added to the database. This applies only to list view (in " +
                    "table view, tapping the table header cell sorts by that field). Finally, the " +
                    "number next to the sort icon shows how many items are currently returned " +
                    "based on your filtering or search value.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Image(
            painter = viewMode,
            contentDescription = "View mode image",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(fraction = 0.5f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .clickable {
                    showFullscreen = true
                    fullImage = viewMode
                }
                .padding(0.dp),
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center
        )
        Spacer(
            modifier = Modifier
                .height(12.dp)
        )
        Text(
            text = "In List View, tapping an entry will navigate to the full details for that " +
                    "entry (if menu overlay is not open on any other item and the blend search " +
                    "is not focused). Long-press to access edit item option.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Image(
            painter = listMenu,
            contentDescription = "List menu image",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(fraction = 0.5f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .clickable {
                    showFullscreen = true
                    fullImage = listMenu
                }
                .padding(0.dp),
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center
        )
        Text(
            text = "In Table View, items can be sorted by tapping the \"Brand\" or \"Blend\" " +
                    "column headers. The default sorting is by the order the items were entered " +
                    "into the database. The first tap will sort the column ascending, the second " +
                    "descending, and the third returns to the default sort order.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Long-pressing the brand or blend cell will take you to the edit screen for " +
                    "that entry. Tapping will take you to the blend details if the blend search " +
                    "field is not focused.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Image(
            painter = tableMenu,
            contentDescription = "Table mode image",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(fraction = 0.5f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .clickable {
                    showFullscreen = true
                    fullImage = tableMenu
                }
                .padding(0.dp),
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center
        )
        Text(
            text = "Scrolling is enabled in both directions, but only one direction can be " +
                    "scrolled at a time. To view notes, tap the icon in the \"Notes\" column.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Image(
            painter = tableMenu2,
            contentDescription = "Table mode image",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(fraction = 0.5f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .clickable {
                    showFullscreen = true
                    fullImage = tableMenu2
                }
                .padding(0.dp),
            contentScale = ContentScale.Inside
        )
        Text(
            text = "The quantity displayed is the \"No. of Tins\" by default. In the settings " +
                    "screen, there is an option to change this quantity display to being oz/lbs " +
                    "or grams. This display value is based on the sum of the quantities entered " +
                    "for each tin. If no tins are given for an entry, the default will revert to " +
                    "the value saved in \"No. of Tins\".",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
    }

    if (showFullscreen) {
        FullscreenImage(
            imagePainter = fullImage!!,
            onDismiss = { showFullscreen = false },
            modifier = Modifier
        )
    }
}

@Composable
private fun StatsPage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "The \"Stats\" screen gives you a \"Quick Stats\" section with two columns " +
                    "for comparing stats between the unfiltered stats on the left and stats " +
                    "based on your chosen filters on the right.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Quick Stats is collapsed by default, tapping where it says \"Expand\" will " +
                    "show all quick stats. Tapping again will collapse this section. The Quick " +
                    "Stats are broken up into subsections for convenience: general stats, counts " +
                    "per type, subgenre, cut, tin containers. Any subsection is only visible if " +
                    "the relevant data is used in any entries.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The \"Charts\" section provides pie charts for visually comparing data " +
                    "by many different metrics. These charts are all populated based on the data " +
                    "that matches your chosen filters.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
    }
}

@Composable
private fun DatesPage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "The \"Dates\" screen contains various date-related information This part is " +
                    "still under construction, but more date-tracking features will be coming soon.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The first section shows blends which are ready to be opened within the next " +
                    "week and month, based on any open dates that were previously set in the " +
                    "future. See the Adding Tins section for more details.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The \"Quick Stats\" are calculated based only on tins which have relevant " +
                    "data added (tins without a date in that field aren't counted). Average age " +
                    "is based on manufacturing date, time in cellar only on the cellar date, and " +
                    "time opened based on tins with open dates that aren't marked as finished. " +
                    "These calculations also do not factor future tins. Average wait time is " +
                    "calculated based on all future dates.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The next section is a list of the 5 oldest tins per each date field, sorted " +
                    "from oldest to youngest. The section after this shows the 5 tins furthest " +
                    "into the future per each date field, sorted by soonest to latest. Any of " +
                    "these sections can be collapsed by tapping the icon/header. Tapping any of " +
                    "the tins in this list will take you to the Details screen for the entry with " +
                    "that tin.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = " " +
                    " " +
                    " " +
                    "",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
    }
}

@Composable
private fun Filtering(
    modifier: Modifier = Modifier,
) {
    var showFullscreen by remember { mutableStateOf(false) }
    var fullImage by remember { mutableStateOf<Painter?>(null) }
    val filterSheet = painterResource(id = R.drawable.help_filter_sheet)

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "The \"Filter Sheet\" opens on top of the Cellar, Stats and Date screens. Any " +
                    "filters persist through navigation around the app and affect all three of " +
                    "these main screens. Filters have no effect on the quick search. If any " +
                    "filters are applied, this icon will have an indicator dot.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Multiple filters can be combined and these main screens will all react " +
                    "instantly to changes. Once you have selected filters, the sheet can be " +
                    "dismissed by tapping outside of it, tapping the close button, or swiping it " +
                    "away.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The \"Clear All\" button at the bottom will clear all chosen filters.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Image(
            painter = filterSheet,
            contentDescription = "Filter sheet image",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(fraction = 0.5f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .clickable {
                    showFullscreen = true
                    fullImage = filterSheet
                }
                .padding(0.dp),
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center
        )
        Text(
            text = "The filter sheet has three pages, swipe left/right to swap between pages.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The first part is brand filtering. Simply tap the name of a brand in the row " +
                    "below the search bar to add it to the brand filter list. The \"Search " +
                    "Brands\" field is just a filter for this row of brands. The button next to " +
                    "the search bar switches the brands filter between include or exclude filtering.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Selected brands are shown in the box below and can be removed by tapping " +
                    "the close icon on each brand. Brands that exceed this space are placed in an " +
                    "overflow. To see the full list of selected brands, tap the overflow button. " +
                    "The \"Clear All\" button here only removes the selected brands.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The next section is for filtering by blend type. Tap the button to add it " +
                    "to the filtering, tap again to remove it. The \"Unassigned\" button will " +
                    "filter for entries that have a blank blend type.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The box below this on the left contains filtering for ratings. Tapping " +
                    "\"Favorites\" or \"Dislikes\" once returns only entries that match" +
                    "the rating, tapping again excludes blends with that rating, tapping again " +
                    "removes that filter. If you would like to see only blends that are rated or " +
                    "unrated, use the appropriate checkboxes below.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The box to the right contains filtering for whether or not a blend is " +
                    "in-stock, and is based on the \"No. of Tins\". For ease of use, this is " +
                    "why leaving the \"Tins\" field blank when adding will presume a quantity of " +
                    "1 for those that wish to use the \"Tins\" field just for quick reference " +
                    "of whether or not they have a given blend on-hand. See the \"Add Items\" " +
                    "section for more details.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The second page contains four sections that all work mostly the same for " +
                    "Subgenres, Cuts, Components and Flavorings. The overflow chip will be " +
                    "highlighted if any selected filters in this section aren't shown on the " +
                    "screen. Tapping the overflow chip will show a full list of available " +
                    "selections for that section.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "One key difference is the additional option in Components and Flavoring for " +
                    "matching. Selecting \"Any\" will return blends that contain any of the " +
                    "selections. Selecting \"All\" will return only those blends that contain " +
                    "all of the field selections (though they may contain other non selected " +
                    "options in addition to the selected ones). Finally, selecting \"Only\" will " +
                    "return those blends that contain only the selections (and no others), an " +
                    "exact match and the strictest filtering.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The third page has filtering for tin-related parameters. The Tin Containers " +
                    "filtering works the same as the boxes on the previous page. However, this " +
                    "will also cause these filtered entries to expand a simplified list of the " +
                    "tins for that item which also match the chosen filter.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The next box with the three check boxes are for filtering items for those " +
                    "with or without assigned tins, those that are opened or unopened (opened " +
                    "only returns tins that have a non-future \"Open Date\" applied and not a " +
                    "\"finished\" tag), and those that are finished or unfinished (though " +
                    "\"unfinished\" only returns tins with a non-future open date). These three " +
                    "filters are mutually-exclusive and do not apply to the Dates screen. And " +
                    "like the containers filter, the opened/unopened and finished/unfinished " +
                    "filters will show the simplified tins expansion with the relevant tins.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The last box is for production status and these two check boxes are mutually " +
                    "exclusive with one another as well.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
    }

    if (showFullscreen) {
        FullscreenImage(
            imagePainter = fullImage!!,
            onDismiss = { showFullscreen = false },
            modifier = Modifier
        )
    }
}

@Composable
private fun AddingItems(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "Items can be added in one of two ways: by tapping the \"Add\" button in the " +
                    "navigation bar, or by importing a CSV file through the top bar overflow " +
                    "menu.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "All entries require a unique combination of \"Brand\" and \"Blend\". Multiple " +
                    "entries can contain the same brand OR blend, but only one entry can contain " +
                    "the same brand AND blend. All other fields are optional.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Most fields that are left blank will remain blank when clicking save, however " +
                    "leaving the \"No. of Tins\" field blank will presume the quantity to be 1. " +
                    "Next to this field, there are \"increase/decrease\" buttons for quickly " +
                    "updating the number of tins.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The \"Sync?\" check box allows you to synchronize the No. of Tins field with " +
                    "the total quantities of individual tins/containers. The total tins are " +
                    "calculated based on the Tin Conversion Rates set in the settings screen (" +
                    "default is 1.75 oz or 50g per tin).",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "When adding components or flavorings, please separate each with a comma and a " +
                    "space. For example: \"virginia, burley, perique\".",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "There are three tabs on the add entry screen that are self-explanatory. " +
                    "Please see the \"Adding Tins\" section for more details on the tins tab.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Entries can also be imported through the \"CSV Import Wizard\" by selecting " +
                    "the \"Import CSV\" option in the top app bar on the \"Cellar\" screen. " +
                    "For more information and help on adding items by CSV, please see the \"Help\" " +
                    "button at the top of the CSV Import Wizard screen.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
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
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The edit entry screen is the same as the add entry screen and functions the " +
                    "same way, just with the existing data pre-loaded in all of the fields.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Batch editing is limited to a select few fields. The screen contains two " +
                    "tabs, one to select items to edit and another to make the edits. Select the " +
                    "checkbox only for the fields you wish to edit. Selecting a field and leaving " +
                    "it blank will result in erasing that field for the selected entries, with " +
                    "exception of the components and flavoring fields.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
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
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
    }
}

@Composable
private fun AddingTins(
    modifier: Modifier = Modifier,
) {
    var showFullscreen by remember { mutableStateOf(false) }
    var fullImage by remember { mutableStateOf<Painter?>(null) }
    val tins = painterResource(id = R.drawable.help_tins_entry)

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "When adding or editing an entry, navigate to the Tins tab on the right and " +
                    "select the \"Add Tin\" button to add a tin. Add additional tins by tapping " +
                    "the \"+\" button below the given tin. To remove a tin, tap the \"-\" icon " +
                    "in the top right corner of the tin.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Image(
            painter = tins,
            contentDescription = "Tins entry image",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(fraction = 0.5f)
                .wrapContentSize(align = Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                .clickable {
                    showFullscreen = true
                    fullImage = tins
                }
                .padding(0.dp),
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center
        )
        Text(
            text = "Each \"tin\" must have a unique label within a given entry. When collating " +
                    "tins in CSV import, the label will be automatically generated as \"Lot __\". " +
                    "The same label can be reused under different entries. All other fields are " +
                    "optional.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Expand/collapse the details of any given tin using the icon in the top left, " +
                    "or \"expand\" text at the bottom (if collapsed). The tin list will be " +
                    "scrollable if it exceeds the height of the given area.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The date fields on this entry screen will show the chosen date as MM/YY if " +
                    "the field is not wide enough, or it will show the date in medium length " +
                    "format based on your locale settings if there is room. Regardless of how it " +
                    "displays here, the full selected day, month and year are saved.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Date selection for any given field is limited by the other fields. If no " +
                    "dates have been entered, then any past or future date can be selected. The " +
                    "first date field to be entered will then start limiting others. The limit " +
                    "schema: Manufacture must be on/before Cellar/Opened, Cellar must be on/" +
                    "between Manufacture and Opened, Opened must be on/after Cellar/Manufacture.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The default view is the calendar picker mode when adding dates. If you would " +
                    "like to clear a previously entered date, you must switch to input mode by " +
                    "tapping the edit icon on the right and erasing the entered date. This is a " +
                    "limitation to the date picker as Google hasn't added a deselect method to " +
                    "the calendar picker mode.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "If an Open date is entered and that date is not in the future, the option to " +
                    "select the \"Finished\" checkbox is enabled. This is relevant for removing a " +
                    "given tin from certain date stats without having to erase the saved date.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "If you would like to keep track of aging, set the open date to a future date " +
                    "of when you'd like to open the tin. The dates screen will have a section for " +
                    "blends that will be ready to open in a given week/month, see the dates " +
                    "section for more details. So for instance, if you would like to age a tin " +
                    "for three years, set an open date for 3 years in the future. In three years, " +
                    "the Dates screen will show the tin is ready to open.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )

        if (showFullscreen) {
            FullscreenImage(
                imagePainter = fullImage!!,
                onDismiss = { showFullscreen = false },
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = "Most settings are self-explanatory, but a few might need further clarification.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Tin Conversion Rates allows you to set the value of what constitutes one " +
                    "\"tin\". You can set the value for ounces and grams separately. This " +
                    "conversion rate is used when calculating the \"No. of Tins\" field when " +
                    "adding individual tins and using the sync option. It is also used on the " +
                    "Statistics screen.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Clean and Optimize Database cleans up the tables of any potentially orphaned " +
                    "data (such as components that are no longer attached to any blends) and " +
                    "also runs the SQL \"vacuum\" command. This option will have its greatest " +
                    "effect if you have updated or deleted a lot of entries, otherwise it won't " +
                    "have much of an effect beyond cleaning up orphaned data. You don't need to " +
                    "use this very often, if ever (except to clean up orphaned components).",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Backup and Restore both give you two options, \"Database\" and \"Settings\". " +
                    "Selecting database will make a copy of the entire database, and settings " +
                    "will save the app settings (like display settings). One backup file is made " +
                    "regardless of whether you select one or both (both backups will be in one).",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "This is an entirely optional process for creating a manual backup. The app " +
                    "does participate in the automatic Google services backup if you have that " +
                    "enabled in your phone's settings, and will save the database and settings.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "These backups are saved as \".tcbu\" files, and are given a suggested name " +
                    "(that you don't have to use) depending on the selected options.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "When restoring, the same options are presented. You can restore the database, " +
                    "settings, or both from a backup file that contains both, but a database " +
                    "backup file will only restore the database, and a settings file will restore " +
                    "only the settings.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Delete Database will erase all entries in the database, and this cannot be " +
                    "undone.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
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
            .clickable { visible = !visible }
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
        ) {
            Spacer(
                modifier = Modifier
                    .height(8.dp)
            )
            content()
            Spacer(
                modifier = Modifier
                    .height(8.dp)
            )
        }
    }
    Spacer(
        modifier = Modifier
            .height(2.dp)
    )
}

// Fullscreen Image //
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenImage(
    imagePainter: Painter,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
        ) {
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                alignment = Alignment.Center,
                contentScale = ContentScale.Inside,
            )
        }
    }
}