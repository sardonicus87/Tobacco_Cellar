package com.sardonicus.tobaccocellar.ui.dates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarBottomAppBar
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.FullScreenLoading
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

object DatesDestination : NavigationDestination {
    override val route = "dates_title"
    override val titleRes = R.string.dates_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatesScreen(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    navigateToStats: () -> Unit,
    navigateToAddEntry: () -> Unit,
    navigateToDetails: (Int) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: DatesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current

    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

    val datesUiState by viewModel.datesUiState.collectAsState()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .noRippleClickable(onClick = { focusManager.clearFocus() }),
        topBar = {
            CellarTopAppBar(
                title = stringResource(DatesDestination.titleRes),
                scrollBehavior = scrollBehavior,
                showMenu = false,
                canNavigateBack = false,
                modifier = Modifier,
            )
        },
        bottomBar = {
            CellarBottomAppBar(
                modifier = Modifier
                    .padding(0.dp),
                navigateToHome = navigateToHome,
                navigateToStats = navigateToStats,
                navigateToAddEntry = navigateToAddEntry,
                currentDestination = DatesDestination,
                filterViewModel = filterViewModel,
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DatesBody(
                loading = datesUiState.loading,
                items = datesUiState.items,
                navigateToDetails = navigateToDetails,
                datesUiState = datesUiState,
                filterViewModel = filterViewModel,
                modifier = Modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatesBody(
    loading: Boolean,
    items: List<ItemsComponentsAndTins>,
    navigateToDetails: (Int) -> Unit,
    datesUiState: DatesUiState,
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val filtering = filterViewModel.isFilterApplied.collectAsState()
    val filtered: String = if (filtering.value) { " filtered" } else { "" }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (loading) {
            FullScreenLoading()
        } else {
            if (datesUiState.datesExist) {
                DateInfo(
                    items = items,
                    navigateToDetails = navigateToDetails,
                    datesUiState = datesUiState,
                    filtered = filtered,
                    modifier = Modifier
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No date information found within filtered entries.",
                        modifier = Modifier
                            .fillMaxWidth(.75f),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.weight(1.25f))
                }
            }
        }
    }
}


@Composable
fun DateInfo(
    items: List<ItemsComponentsAndTins>,
    navigateToDetails: (Int) -> Unit,
    datesUiState: DatesUiState,
    filtered: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        val separatorColor = colorScheme.secondary
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "*All date information on this screen is filter-reactive.",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(.9f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = LocalContentColor.current.copy(alpha= .75f),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Aging ready
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = LocalCustomColors.current.backgroundVariant)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Aging Tracker",
                    modifier = Modifier
                        .padding(start = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    color = colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val strokeWidth = Dp.Hairline.toPx()
                        val yOffset = size.height - strokeWidth / 2

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, strokeWidth),
                            strokeWidth = strokeWidth
                        )

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, yOffset),
                            end = Offset(size.width, yOffset),
                            strokeWidth = strokeWidth
                        )
                    }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            if (datesUiState.agedDueThisWeek.isNotEmpty() || datesUiState.agedDueThisMonth.isNotEmpty()) {
                if (datesUiState.agedDueThisWeek.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tins ready in the next 7 days:",
                        modifier = Modifier
                            .padding(bottom = 1.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    AgingSection(
                        items = datesUiState.agedDueThisWeek,
                        navigateToDetails = navigateToDetails,
                        modifier = Modifier
                            .fillMaxWidth(),
                        showTime = true
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tins ready this week in$filtered entries.",
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                if (datesUiState.agedDueThisMonth.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Other tins ready this month:",
                        modifier = Modifier
                            .padding(bottom = 1.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    AgingSection(
                        items = datesUiState.agedDueThisMonth,
                        navigateToDetails = navigateToDetails,
                        showDate = true,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "No more tins ready this month in$filtered entries.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Start,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Text(
                    text = "No tins coming of age this week or month in$filtered entries.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Quick stats
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = LocalCustomColors.current.backgroundVariant)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Quick Date Stats",
                    modifier = Modifier
                        .padding(start = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    color = colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val strokeWidth = Dp.Hairline.toPx()
                        val yOffset = size.height - strokeWidth / 2

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, strokeWidth),
                            strokeWidth = strokeWidth
                        )

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, yOffset),
                            end = Offset(size.width, yOffset),
                            strokeWidth = strokeWidth
                        )
                    }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            if (datesUiState.averageAgeManufacture.isNotBlank() || datesUiState.averageAgeCellar.isNotBlank() || datesUiState.averageAgeOpen.isNotBlank() || datesUiState.averageWaitTime.isNotBlank()) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (datesUiState.averageAgeManufacture.isNotBlank()) {
                            Text(
                                text = "Average age of tins: ${datesUiState.averageAgeManufacture}",
                                modifier = Modifier,
                                fontSize = 15.sp,
                            )
                        }
                        if (datesUiState.averageAgeCellar.isNotBlank()) {
                            Text(
                                text = "Average time in cellar: ${datesUiState.averageAgeCellar}",
                                modifier = Modifier,
                                fontSize = 15.sp,
                            )
                        }
                        if (datesUiState.averageAgeOpen.isNotBlank()) {
                            Text(
                                text = "Average opened time: ${datesUiState.averageAgeOpen}",
                                modifier = Modifier,
                                fontSize = 15.sp,
                            )
                        }
                        if (datesUiState.averageWaitTime.isNotBlank()) {
                            Text(
                                text = "Average wait time: ${datesUiState.averageWaitTime}",
                                modifier = Modifier,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Text(
                    text = "No relevant date stats found in$filtered entries.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                )
            }
        }

        // Oldest Tins
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = LocalCustomColors.current.backgroundVariant)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Oldest Tins",
                    modifier = Modifier
                        .padding(start = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    color = colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val strokeWidth = Dp.Hairline.toPx()
                        val yOffset = size.height - strokeWidth / 2

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, strokeWidth),
                            strokeWidth = strokeWidth
                        )

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, yOffset),
                            end = Offset(size.width, yOffset),
                            strokeWidth = strokeWidth
                        )
                    }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            if (datesUiState.pastManufacture.isNotEmpty() || datesUiState.pastCellared.isNotEmpty() || datesUiState.pastOpened.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                DatesSection(
                    label = "Manufacture",
                    items = datesUiState.pastManufacture,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                DatesSection(
                    label = "Cellared",
                    items = datesUiState.pastCellared,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                DatesSection(
                    label = "Opened",
                    items = datesUiState.pastOpened,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Text(
                    text = "No tin dates found.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Future tins
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = LocalCustomColors.current.backgroundVariant)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Future Tins",
                    modifier = Modifier
                        .padding(start = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    color = colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val strokeWidth = Dp.Hairline.toPx()
                        val yOffset = size.height - strokeWidth / 2

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, strokeWidth),
                            strokeWidth = strokeWidth
                        )

                        drawLine(
                            color = separatorColor,
                            start = Offset(0f, yOffset),
                            end = Offset(size.width, yOffset),
                            strokeWidth = strokeWidth
                        )
                    }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            if (datesUiState.futureManufacture.isNotEmpty() || datesUiState.futureCellared.isNotEmpty() || datesUiState.futureOpened.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                DatesSection(
                    label = "Manufacture",
                    items = datesUiState.futureManufacture,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                DatesSection(
                    label = "Cellared",
                    items = datesUiState.futureCellared,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                DatesSection(
                    label = "Opened",
                    items = datesUiState.futureOpened,
                    navigateToDetails = navigateToDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Text(
                    text = "No future tin dates found.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@Composable
fun DatesSection(
    label: String,
    items: List<DateInfoItem>,
    navigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    var expandEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(items) {
        expandEnabled = items.isNotEmpty()
        if (items.isEmpty()) {
            expanded = true
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = expandEnabled) { expanded = !expanded },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$label Date",
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.tertiary,
                modifier = Modifier
                    .padding(bottom = 1.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = "Expand/collapse",
                    modifier = Modifier
                        .offset(y = (-2).dp)
                        .size(22.dp),
                    tint = if (expandEnabled) colorScheme.onBackground.copy(alpha = 0.8f) else Color.Transparent
                )
            }
        }
        if (expanded) {
            if (items.isNotEmpty()) {
                items.forEach {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable(onClick = { navigateToDetails(it.id) })
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(.9f)
                        ) {
                            Text(
                                text = "• ${it.brand}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                            )
                            Text(
                                text = ", ${it.blend}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, false)
                            )
                            Text(
                                text = " - ${it.tinLabel}",
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "${it.date} (${it.time})",
                                fontSize = 14.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Text(
                    text = "No ${label.lowercase()} dates found.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 10.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    color = colorScheme.primary.copy(alpha= .5f),
                )
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}


@Composable
fun AgingSection(
    items: List<DateInfoItem>,
    navigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    showTime: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        items.forEach {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable(onClick = { navigateToDetails(it.id) })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "• ${it.brand}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                    )
                    Text(
                        text = ", ${it.blend}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, false)
                    )
                    Text(
                        text = " - ${it.tinLabel}",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                    )
                    if (showTime) {
                        Text(
                            text = " (${it.time})",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                        )
                    }
                    if (showDate) {
                        Text(
                            text = " (${it.date})",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                        )
                    }
                }
            }
        }
    }
}