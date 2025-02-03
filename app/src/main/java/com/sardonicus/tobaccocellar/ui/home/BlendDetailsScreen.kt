package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.items.formatLongDate
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

object BlendDetailsDestination : NavigationDestination {
    override val route = "blend_details_title"
    override val titleRes = R.string.blend_details_title
    @Suppress("ConstPropertyName")
    const val itemsIdArg = "itemsId"
    val routeWithArgs = "$route/{$itemsIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlendDetailsScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: BlendDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val blendDetails by viewModel.blendDetails.collectAsState()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(BlendDetailsDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp,
                showMenu = false,
                modifier = Modifier
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
            if (blendDetails.isLoading) {
                Column(
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
            }
            else if (blendDetails.details != null) {
                BlendDetailsBody(
                    blendDetails = blendDetails.details!!,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                )
            }
            else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = "Error loading details.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(0.dp),
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(1.25f)
                    )
                }
            }
        }
    }
}


@Composable
fun BlendDetailsBody(
    blendDetails: ItemsComponentsAndTins,
    modifier: Modifier = Modifier
) {
    fun buildString(title: String, value: String, color: Color = Color.Unspecified): AnnotatedString {
        val string = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = color)) { append(title) }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) { append(value) }
        }
        return string
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier = modifier
            .fillMaxWidth()
//            .padding(vertical = 8.dp, horizontal = 4.dp)
//            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp)
    ) {
        // Blend Name
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text(
                text = blendDetails.items.blend,
                modifier = Modifier
                    .padding(bottom = 2.dp),
                fontSize = 30.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "by ${blendDetails.items.brand}",
                modifier = Modifier
                    .padding(bottom = 12.dp),
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }

        // Blend Details
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(8.dp)
                )
                .background(
                    color = LocalCustomColors.current.darkNeutral,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            val productionStatus = if (blendDetails.items.inProduction) "in production" else "not in production"
            val productionStatusColor = if (blendDetails.items.inProduction) LocalContentColor.current else MaterialTheme.colorScheme.error

            Text(
                text = "Details",
                modifier = Modifier,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = buildString(
                        "Type: ",
                        blendDetails.items.type,
                        MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier,
                )
                Text(
                    text = buildString(
                        "Subgenre: ",
                        blendDetails.items.subGenre,
                        MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier,
                )
                Text(
                    text = buildString(
                        "Cut: ",
                        blendDetails.items.cut,
                        MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier,
                )
                Text(
                    text = buildString(
                        "Components: ",
                        blendDetails.components.joinToString(", ") { it.componentName },
                        MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        ) { append("Production Status: ") }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                color = productionStatusColor
                            )
                        ) { append(productionStatus) }
                    },
                    modifier = Modifier,
                )
            }
        }


        // Notes?
        if (blendDetails.items.notes.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = LocalCustomColors.current.darkNeutral,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = "Notes",
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                NotesText(
                    notes = blendDetails.items.notes,
                    modifier = Modifier
                        .padding(start = 12.dp, bottom = 8.dp)
                )
            }
        }

        // Tins
        if (blendDetails.tins.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = LocalCustomColors.current.darkNeutral,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = "Tins",
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                blendDetails.tins.forEach {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = it.tinLabel,
                            modifier = Modifier,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .padding(start = 12.dp)
                        ) {
                            if (it.container.isNotEmpty()) {
                                Text(
                                    text = buildString(
                                        "Container: ",
                                        it.container,
                                        MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier,
                                )
                            }
                            if (it.unit.isNotEmpty()) {
                                Text(
                                    text = buildString(
                                        "Quantity: ",
                                        "${it.tinQuantity} ${it.unit}",
                                        MaterialTheme.colorScheme.tertiary
                                    ),
                                    modifier = Modifier,
                                )
                            }
                            if (it.manufactureDate != null) {
                                Text(
                                    text = buildString(
                                        "Manufacture Date: ",
                                        formatLongDate(it.manufactureDate),
                                        MaterialTheme.colorScheme.tertiary
                                    ),
                                    modifier = Modifier,
                                )
                            }
                            if (it.cellarDate != null) {
                                Text(
                                    text = buildString(
                                        "Cellar Date: ",
                                        formatLongDate(it.cellarDate),
                                        MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier,
                                )
                            }
                            if (it.openDate != null) {
                                Text(
                                    text = buildString(
                                        "Opened Date: ",
                                        formatLongDate(it.openDate),
                                        MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier,
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .height(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NotesText(
    notes: String,
    modifier: Modifier = Modifier
) {
    val lines = notes.split("\n")
    val blankLineHeight = 9.dp

    Column(
        modifier = modifier
    ) {
        lines.forEach {
            if (it.isEmpty()) {
                Spacer(
                    modifier = Modifier
                        .height(blankLineHeight)
                )
            } else {
                Text(
                    text = it,
                    modifier = Modifier,
                )
            }
        }
    }
}