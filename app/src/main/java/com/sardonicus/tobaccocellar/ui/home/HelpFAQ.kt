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
                    "passport\", combined with the ease of searching and filtering. The future " +
                    "will bring optional detailed cellar tracking (dates and more).",
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
        Spacer(
            modifier = Modifier
                .height(12.dp)
        )
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
                    "your chosen filters. It will return all blends containing the search text " +
                    "regardless of whether or not they match any chosen filters. To return to the " +
                    "list or table, click the clear button or erase the search text. Any " +
                    "previously chosen filters will be re-applied.",
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
        Text(
            text = "In List View, long-press any item to open a menu overlay to access options " +
                    "to edit the item, or view notes (if a note is present).",
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
            text = "Tapping anywhere outside of the item or pressing the phone's back button " +
                    "will dismiss this menu.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
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
            text = "Long-pressing the blend name will take you to the edit screen for that item.",
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
            text = "The \"Stats\" screen is currently a work in progress. It gives you a " +
                    "\"Quick Stats\" section with two columns for comparing stats based on your " +
                    "chosen filters on the right, and the unfiltered stats on the left.",
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
        Text(
            text = "This makes comparison quick, easy, and broad through the creative use of " +
                    "filtering and looking at the correct chart. Say for instance, you want to " +
                    "know what percentage of your Aromatic blends come from each brand, or by " +
                    "which brand do you have the most aromatic blends. Simply open the \"Filter\" " +
                    "sheet at the bottom, select \"Aromatic\", and then look at the \"Brands by " +
                    "Number of Entries\" chart. Since the only data fed to the chart are blends " +
                    "tagged as \"Aromatic\", you can see which brand from which you own the most " +
                    "aromatics.",
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
            text = "The \"Filter Sheet\" opens on top of the \"Cellar\" and \"Stats\" screens. " +
                    "Any chosen filters persist through navigation around the app and affect " +
                    "both the \"Cellar\" and \"Stats\" screens. Filters have no effect on the " +
                    "\"Blend Search\" at the top of the \"Cellar Screen\".",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Multiple filters can be combined and the list or table and stats screens " +
                    "will react instantly to filtering changes. Once you have selected filters, " +
                    "the sheet can be dismissed by tapping outside of it, tapping the close " +
                    "icon, or swiping it away.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Image(
            painter = filterSheet,
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
                    fullImage = filterSheet
                }
                .padding(0.dp),
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center
        )
        Text(
            text = "The first part of the sheet shows brand filtering. Simply tap the name of a " +
                    "brand in the row below the search bar to add the brand to the brand filter " +
                    "list. The \"Search Brands\" field is just a filter for this row of brands. " +
                    "There is a button next to the search bar that switches the chosen brand " +
                    "filters to return results that either include or do not include the chosen " +
                    "brands.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Selected brands are shown in the box below. and can be removed by tapping " +
                    "the close icon on each brand. The box has limited space, so brands that " +
                    "exceed this space are placed in an overflow. To see the full list of brand " +
                    "selections, tap the overflow button. This will open a popup with the full " +
                    "list of chosen brands. The \"Clear All\" button in this overflow only " +
                    "removes the selected brands.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The next section is for filtering by blend type. Tap the button to add it " +
                    "to the filtering, tap again to remove it. The \"Unassigned\" button will " +
                    "show only entries that have a blank blend type.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The box below this on the left contains filtering for ratings. Tapping " +
                    "\"Favorites\" or \"Dislikes\" once will return only entries that match" +
                    "the rating, tapping a second time returns only entries that do not match" +
                    "the chosen rating. As \"Favorites\" and \"Dislikes\" are mutually exclusive, " +
                    "if you would like to see only blends that match both or neither, use the " +
                    "checkboxes below. \"Rated\" will return all blends that are either " +
                    "\"Favorites\" or \"Dislikes\", and \"Unrated\" will return only those that " +
                    "are neither.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The box to the right contains filtering for whether or not a blend is " +
                    "in-stock, and is based on the quantity of tins. For ease of use, this is " +
                    "why leaving the \"Tins\" field blank when adding will presume a quantity of " +
                    "one for those that wish to use the \"Tins\" field just for quick reference " +
                    "of whether or not they have a given blend on-hand. See the \"Add Items\" " +
                    "section for more details.",
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
                    "the same brand AND blend (if you want to add individual tins, a future " +
                    "update will allow for this). All other fields are optional, allowing " +
                    "flexibility in use.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "Most fields that are left blank will remain blank when clicking save, however " +
                    "leaving the \"No. of Tins\" field blank will presume the quantity to be one. " +
                    "Next to this field, there are \"increase/decrease\" buttons for quickly " +
                    "updating the quantity. Next to this is a button to open a \"Tin Converter\". " +
                    "The maximum quantity of tins is limited to 99.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "The purpose of the \"Tin Converter\" is to convert quantity amounts of " +
                    "ounces, pounds, or grams into the \"No. of Tins\". The purpose of keeping " +
                    "all entries quantities as the number of tins is to allow for consistency " +
                    "and accuracy with statistics. The default conversion rate is 1 Tin = " +
                    "either 1.75 oz or 50 grams. The conversion rate can be changed in the " +
                    "settings screen, and the conversion rate for oz and grams are independent " +
                    "of one another. So for instance, you might consider 1 Tin to be equivalent " +
                    "to either 2 oz or 50 grams.",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            softWrap = true,
        )
        Text(
            text = "In the future, when support for individual tins is added, there will be an " +
                    "option here to synchronize the quantity of tins with the individual tin counts.",
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
            painter = painterResource(id = if (visible) R.drawable.arrow_down else R.drawable.arrow_up),
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