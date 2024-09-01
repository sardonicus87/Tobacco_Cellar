package com.example.tobaccocellar.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarBottomAppBar
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.FilterBottomSheet
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.BottomSheetState
import com.example.tobaccocellar.ui.FilterViewModel
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import eu.wewox.lazytable.LazyTable
import eu.wewox.lazytable.LazyTableDefaults.dimensions
import eu.wewox.lazytable.LazyTableDefaults.pinConfiguration
import eu.wewox.lazytable.LazyTableDimensions
import eu.wewox.lazytable.LazyTableItem
import eu.wewox.lazytable.LazyTablePinConfiguration
import eu.wewox.lazytable.lazyTableDimensions
import eu.wewox.lazytable.lazyTablePinConfiguration

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToStats: () -> Unit,
    navigateToAddEntry: () -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    navigateToCsvImport: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val homeUiState by viewmodel.homeUiState.collectAsState()
    val isTableView = homeUiState.isTableView
    val snackbarHostState = remember { SnackbarHostState() }
    val showSnackbar = viewmodel.showSnackbar.collectAsState()
    val filterViewModel = LocalCellarApplication.current.filterViewModel

    if (showSnackbar.value) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "CSV Exported",
                duration = SnackbarDuration.Short
            )
            viewmodel.snackbarShown()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = false,
                navigateToCsvImport = navigateToCsvImport,
                showMenu = true,
                exportCsvHandler = viewmodel,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier
                    .padding(0.dp),
                navigateToStats = navigateToStats,
                navigateToAddEntry = navigateToAddEntry,
                filterViewModel = filterViewModel,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(0.dp),
                snackbar = { Snackbar(it) }
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, bottom = 66.dp, start = 0.dp, end = 0.dp)
        ) {
            HomeHeader(
                homeUiState = homeUiState,
                selectView = viewmodel::selectView,
                isTableView = isTableView,
            )
            HomeBody(
                items = homeUiState.items,
                isTableView = isTableView,
                onItemClick = navigateToEditEntry,
                isLoading = homeUiState.isLoading,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    selectView: (Boolean) -> Unit,
    isTableView: Boolean,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Switch table/list view //
        Text(
            text = "View:",
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(0.dp)
        )
        IconButton(
            onClick = { selectView(!isTableView) },
            modifier = Modifier
                .padding(4.dp)
                .size(28.dp)
        ) {
            Icon(
                painter = painterResource(homeUiState.toggleIcon),
                contentDescription = stringResource(homeUiState.toggleContentDescription),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(24.dp)
                    .padding(0.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .width(24.dp)
                .padding(0.dp)
        )

        /* TODO Finish Search widget */

        TextField(
            value = "",
            onValueChange = { /* do nothing */ },
            modifier = Modifier
                .fillMaxWidth(fraction = .7f)
                .height(24.dp),
            enabled = false,
            leadingIcon = { Icon(painterResource(id = R.drawable.search), null) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
    }
}


@Composable
private fun HomeBody(
    modifier: Modifier = Modifier,
    items: List<Items>,
    isLoading: Boolean,
    isTableView: Boolean,
    onItemClick: (Int) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptics = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        if (isLoading) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1.5f)
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(0.dp)
                        .size(48.dp)
                        .weight(0.5f),
                )
                Spacer(
                    modifier = Modifier
                        .weight(2f)
                )
            }
        } else {
            if (items.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.no_items),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(0.dp),
                        )
                }
            }
            else {
                if (isTableView) {
                    TableViewMode(
                        itemsList = items,
                        contentPadding = contentPadding,
                        modifier = Modifier
                            .padding(0.dp),
                        )
                }
                else {
                    ListViewMode(
                        itemsList = items,
                        onItemClick = { onItemClick(it.id) },
                        contentPadding = contentPadding,
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                        )
                }
            }
        }
    }
}



/** List View Mode **/
@Composable
fun ListViewMode(
    modifier: Modifier = Modifier,
    itemsList: List<Items>,
    onItemClick: (Items) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
    ){
        items(items = itemsList, key = { it.id }) { item ->
            CellarListItem(
                item = item,
                modifier = Modifier
                    .padding(0.dp)
                    .clickable { onItemClick(item) }
            )
        }
    }
}

// Individual Items in List View //
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CellarListItem(
    item: Items,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp, top = 0.dp, bottom = 1.dp, end = 0.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer),
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
                Column(
                    modifier = Modifier
                        .padding(0.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .padding(0.dp)
                    ) {
                        Row (
                            modifier = Modifier
                                .padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 0.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            if (item.favorite) {
                                Icon(
                                    painter = painterResource(id = R.drawable.favorite_heart_filled_18),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(0.dp)
                                        .graphicsLayer {
                                            rotationZ = (-45f)
                                        },
                                    tint = Color(0x60FF0000)
                                )
                            }
                        }
// Blend Name //
                        Column {
                            Text(
                                text =
                                if (item.disliked) (item.blend + " ")
                                else (item.blend),
                                modifier = Modifier
                                    .padding(0.dp)
                                    .fillMaxWidth(fraction = .9f)
                                    .basicMarquee(
                                        iterations = Int.MAX_VALUE,
                                        delayMillis = 250,
                                        spacing = MarqueeSpacing(100.dp)
                                    ),
                                style =
                                if (item.quantity == 0 && !item.disliked) (
                                        MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        )
                                else if (item.disliked && item.quantity > 0) (
                                        MaterialTheme.typography.titleMedium.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        )
                                else if (item.disliked && item.quantity == 0) (
                                        MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.tertiary,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        )
                                else (MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textDecoration = TextDecoration.None
                                )
                                        ),
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 8.dp)
                                    .offset(y = (-4).dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
// Brand Name //
                                Column(
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .weight(.95f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = item.brand,
                                        modifier = Modifier,
                                        style =
                                        if (item.quantity == 0) (
                                                MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    textDecoration = TextDecoration.None
                                                )
                                                )
                                        else (MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            textDecoration = TextDecoration.None
                                        )
                                                ),
                                        fontStyle = Italic,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
// Type //
                                Column(
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .weight(0.5f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = item.type,
                                        modifier = Modifier,
                                        style =
                                        if (item.quantity == 0) (
                                                MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    textDecoration = TextDecoration.None
                                                )
                                                )
                                        else (MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            textDecoration = TextDecoration.None
                                        )
                                                ),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                    )
                                }
                                Spacer(
                                    modifier = Modifier
                                        .width(0.dp)
                                        .padding(0.dp)
                                        .weight(1f)
                                )
                            }
                        }
                    }

            }
            Column(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(0.1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.End
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "x" + item.quantity,
                        modifier = Modifier,
                        style =
                        if (item.quantity == 0) (
                                MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    textDecoration = TextDecoration.None)
                                )
                        else (MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textDecoration = TextDecoration.None)
                                ),
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


/** Table View Mode **/
@Composable
fun TableViewMode(
    itemsList: List<Items>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
){
    CellarLazyTable(
        itemsTbl = itemsList,
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp)
    )

}

@Suppress("RedundantLambdaArrow")
@Composable
fun CellarLazyTable(
    itemsTbl: List<Items>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val columns = 4
    val rows = itemsTbl.size

    LazyTable(
        pinConfiguration = pinConfiguration(),
        dimensions = dimensions(),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        items(
            count = rows * columns,
            layoutInfo = {
                LazyTableItem(
                    column = it % columns,
                    row = it / columns + 1,
                    )
            },
            ) {
                Cell(
                    items = itemsTbl[it / columns],
                    column = it % columns,
                )
                items(
                    count = columns,
                    layoutInfo = { it ->
                        LazyTableItem(
                            column = it % columns,
                            row = 0,
                        )
                    },
                ) { it ->
                    HeaderCell(column = it)
                }

        }
    }
}

private fun pinConfiguration(): LazyTablePinConfiguration =
    lazyTablePinConfiguration(
        columns = 0,
        rows = 1,
    )

private fun dimensions(): LazyTableDimensions =
    lazyTableDimensions(
        columnSize = {
            when (it) {
                0 -> 144.dp
                1 -> 288.dp
                2 -> 96.dp
                3 -> 48.dp
                else -> error("Unknown column index: $it")
            }
        },
        rowSize = {
            if (it == 0) {
                32.dp
            } else {
                32.dp
            }
        }
    )

@Suppress("ComplexMethod")
@Composable
private fun Cell(
    items: Items,
    column: Int,
) {
    val content = when (column) {
        0 -> items.brand
        1 -> items.blend
        2 -> items.type
        3 -> items.quantity.toString()
        else -> error("Unknown column index: $column")
    }

    Box(
        contentAlignment = when (column) {
            0 -> Alignment.CenterStart
            1 -> Alignment.CenterStart
            2 -> Alignment.Center
            3 -> Alignment.CenterEnd
            else -> error("Unknown column index: $column")
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface)
            .padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 8.dp)
    ) {
        Text(
            text = content,
        )
    }
}


/* TODO Add sorting to HeaderCell */
@Composable
private fun HeaderCell(column: Int) {
    val content = when (column) {
        0 -> "Brand"
        1 -> "Blend"
        2 -> "Type"
        3 -> "Qty"
        else -> error("")
    }

    Box(
        contentAlignment = when (column) {
            0 -> Alignment.Center
            1 -> Alignment.Center
            2 -> Alignment.Center
            3 -> Alignment.Center
            else -> error("Unknown column index: $column")
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .border(Dp.Hairline, MaterialTheme.colorScheme.onPrimary)
            .clickable { }
    ) {
        Text(text = content)

    }
}











//// Previews ////

@Preview(showBackground = true)
@Composable
fun HomeHeaderPreview() {
    HomeHeader(
        homeUiState = HomeUiState(),
        selectView = {},
        isTableView = true,
    )
}


@Preview(showBackground = true)
@Composable
fun HomeBodyListPreview() {
    ListViewMode(itemsList = listOf(
        Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Virginia", 2, disliked = false, favorite = true, notes = null.toString()),
        Items(1, "Sutliff", "Maple Shadows", "Aromatic", 0, disliked = false, favorite = false, notes = null.toString()),
        Items(2, "Cornell & Diehl", "Pegasus", "Burley", 2, disliked = true, favorite = false, notes = null.toString()),
        Items(3, "Cornell & Diehl", "Some super long blend name to test the basic marquee effect if that ever happens", "Burley", 0, disliked = true, favorite = true, notes = null.toString())
    ), onItemClick = {}, contentPadding = PaddingValues(0.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun CellarItemPreview() {
    CellarListItem(
        Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Burley", 2,
            disliked = false,
            favorite = true,
            notes = null.toString(),
        ),
    )
}


@Preview(showBackground = true)
@Composable
fun HomeBodyTablePreview() {
    TableViewMode(itemsList = listOf(
        Items(0, "Cornell & Diehl", "Sun Bear Tupelo (2023)", "Burley", 2,
            disliked = false,
            favorite = true,
            notes = null.toString(),
        ),
        Items(1, "Sutliff", "Maple Shadows", "Aromatic", 0, disliked = false, favorite = true, notes = null.toString()),
        Items(2, "Cornell & Diehl", "Pegasus", "Burley", 2, disliked = true, favorite = false, notes = null.toString()),
        Items(3, "Warped", "Midsommar", "Burley", 1, disliked = false, favorite = false, notes = null.toString())
    ), contentPadding = PaddingValues(0.dp)
    )
}