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
            text = "Tobacco Cellar is intended to be a basic inventory tracker and \'tobacco " +
                    "passport\", combined with the ease of searching and filtering and " +
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
                content = { CellarView() }
            )
            HelpSection(
                title = "Stats Screen",
                content = { StatsPage() }
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
private fun CellarView(
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
            text = "Also found in the header is a \"Quick Search\" for blends. This search only " +
                    "returns results based on the \"Blend\" field, and works independently of " +
                    "filtering. To return to the full list/table, click the clear button or erase " +
                    "the search text. Any previously chosen filters will be re-applied.",
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
                    "columns. The default sorting is by the order the items were entered into the " +
                    "database. The first tap will sort the column ascending, the second " +
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
                    "for comparing stats between the unfiltered stats on the left and stats ." +
                    "based on your chosen filters on the right.",
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
            text = "The \"Filter Sheet\" opens on top of the Cellar and Stats screens. Any " +
                    "filters persist through navigation around the app and affect both the " +
                    "Cellar and Stats screens. Filters have no effect on the quick Blend Search.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Multiple filters can be combined and the Cellar and Stats screens " +
                    "will react instantly to changes. Once you have selected filters, " +
                    "the sheet can be dismissed by tapping outside of it, tapping the close " +
                    "icon, or swiping it away.",
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
            text = "The filter sheet has two pages, swipe left/right to swap between pages.",
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
                    "overflow. To see the full list of brand selections, tap the overflow button. " +
                    "A popup will show the ful list of selected brands. The \"Clear All\" button " +
                    "here only removes the selected brands.",
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
            text = "The second page contains three sections that all work mostly the same for " +
                    "Subgenres, cuts and components. The overflow chip will be highlighted if " +
                    "any selected filters in this section aren't shown on the screen. Tapping " +
                    "the overflow chip will show a full list of available selections for that " +
                    "section.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "One key difference is the additional option in Components filtering for " +
                    "matching. Selecting \"Any\" will return blends that contain any of the " +
                    "selected components. Selecting \"All\" will return only those blends that " +
                    "contain all of the selected components (though they may contain other " +
                    "components in addition to the selected ones).",
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
                    "leaving the \"No. of Tins\" field blank will presume the quantity to be 1." +
                    "Next to this field, there are \"increase/decrease\" buttons for quickly " +
                    "updating the number of tins.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The \"Sync?\" check box allows you to synchronize the No. of Tins field with " +
                    "the total quantities of individual tins/containers. The total tins are " +
                    "calculated based on the aforementioned conversion rate.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "When adding components, please separate each component with a comma and a " +
                    "space. For example: \"virginia, burley, perique\".",
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
            text = "Items can be edited by long-pressing the entry in the cellar screen, by " +
                    "using the bulk edit option in the top bar overflow menu, or through a CSV " +
                    "import and selecting the \"Update\" or \"Overwrite\" option (see help file " +
                    "on CSV Import for more information).",
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
                    "it blank will result in erasing that field for the selected entries.",
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
            text = "Expand the details of any given tin using the icon in the top left, or the " +
                    "\"expand\" text at the bottom (if collapsed). The same top left icon can " +
                    "be used to collapse a tin. The tin list will be scrollable.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The date fields on this entry screen will show the chosen date as MM/YY if " +
                    "the field is not wide enough to show the full date. Otherwise, it will show " +
                    "the chosen date based on your locale settings. Regardless of how it is " +
                    "displayed here, the full selected day, month and year are saved.",
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
                    "have much of an effect beyond cleaning up orphaned data.",
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
//    BasicAlertDialog(
//        onDismissRequest = onDismiss,
//        modifier = modifier
//            .fillMaxSize(),
//        properties = DialogProperties(
//            dismissOnBackPress = true,
//            dismissOnClickOutside = true,
//            usePlatformDefaultWidth = false
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.5f))
//                .clickable(onClick = onDismiss),
//            contentAlignment = Alignment.Center
//        ) {
//            Image(
//                painter = imagePainter,
//                contentDescription = null,
//                modifier = Modifier
//                    .align(Alignment.Center),
//                //    .fillMaxWidth(),
//                alignment = Alignment.Center,
//                contentScale = ContentScale.Fit,
//            )
//        }
//    }

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