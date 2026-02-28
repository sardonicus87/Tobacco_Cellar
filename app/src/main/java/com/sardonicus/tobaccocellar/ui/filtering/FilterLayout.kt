package com.sardonicus.tobaccocellar.ui.filtering

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.filtering.sections.BrandFilterSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.ComponentSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.ContainerFilterSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.CutSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.FlavoringSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.OtherFiltersSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.ProductionFilterSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.SubgenreSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.TinsFilterSection
import com.sardonicus.tobaccocellar.ui.filtering.sections.TypeFilterSection
import kotlinx.coroutines.launch

@Composable
fun FilterLayout(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    closeSheet: () -> Unit = {},
    sheetLayout: Boolean = true,
    pagerState: PagerState = rememberPagerState { 3 },
) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 0.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FilterHeader(sheetLayout, closeSheet)

        if (sheetLayout) { PagerLayout(filterViewModel, pagerState) }
        else { PaneLayout(filterViewModel) }

        FilterFooter(filterViewModel)

    }
}


@Composable
private fun FilterHeader(
    sheetLayout: Boolean,
    closeSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sheetLayout) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "Select Filters",
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) { closeSheet() }
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Filters",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                maxLines = 1,
                modifier = Modifier,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun FilterFooter(
    filterViewModel: FilterViewModel,
) {
    val filtersApplied by filterViewModel.isFilterApplied.collectAsState()

    TextButton(
        onClick = filterViewModel::resetFilter,
        modifier = Modifier
            .offset(x = (-4).dp)
            .padding(top = 6.dp),
        enabled = filtersApplied,
    ) {
        Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 3.dp)
                .size(20.dp)
        )
        Text(
            text = "Clear All",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(Modifier.height(12.dp))
}


@Composable
private fun PaneLayout(
    filterViewModel: FilterViewModel
) {
    Spacer(Modifier.height(8.dp))
    PageOne(
        filterViewModel = filterViewModel,
        modifier = Modifier
    )
    PageTwo(
        filterViewModel = filterViewModel,
        modifier = Modifier
            .padding(vertical = 8.dp)
    )
    PageThree(
        filterViewModel = filterViewModel,
        modifier = Modifier
            .padding(top = 4.dp)
    )
}


@Composable
private fun PagerLayout(
    filterViewModel: FilterViewModel,
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .pointerInput(pagerState) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        totalDrag += amount
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (totalDrag < (-50) && pagerState.currentPage < (pagerState.pageCount - 1)) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else if (totalDrag > 50 && pagerState.currentPage > 0) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    }
                )
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(0.dp),
            indicatorSize = IndicatorSizes(6.5.dp, 6.dp)
        )
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(383.dp),
        userScrollEnabled = true,
        verticalAlignment = Alignment.Top,
    ) { page ->
        when (page) {
            // brand, type, rating, stock filters //
            0 -> {
                PageOne(
                    filterViewModel = filterViewModel,
                    modifier = Modifier
                )
            }

            // subgenre, cuts, components, flavoring filters //
            1 -> {
                PageTwo(
                    filterViewModel = filterViewModel,
                    modifier = Modifier
                )
            }

            // tin filtering, containers, production //
            2 -> {
                PageThree(
                    filterViewModel = filterViewModel,
                    modifier = Modifier
                        .height(383.dp)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

data class IndicatorSizes(val current: Dp, val other: Dp)

@Composable
private fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    indicatorSize: IndicatorSizes = IndicatorSizes(current = 8.dp, other = 7.dp),
) {
    val animationScope = rememberCoroutineScope()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) {
            val color = if (pagerState.currentPage == it) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
            }
            val size =
                if (pagerState.currentPage == it) indicatorSize.current else indicatorSize.other

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(size)
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { animationScope.launch { pagerState.animateScrollToPage(it) } }
            )
        }
    }
}


@Composable
private fun PageOne(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(6.dp),
        )
        TypeFilterSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(start = 6.dp, end = 6.dp, top = 0.dp, bottom = 6.dp),
        )
        OtherFiltersSection(
            filterViewModel = filterViewModel,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 0.dp),
        )
    }
}

@Composable
private fun PageTwo(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SubgenreSection(filterViewModel)
        CutSection(filterViewModel)
        ComponentSection(filterViewModel)
        FlavoringSection(filterViewModel)
    }
}

@Composable
private fun PageThree(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TinsFilterSection(filterViewModel)
        ContainerFilterSection(filterViewModel)
        ProductionFilterSection(filterViewModel)
    }
}