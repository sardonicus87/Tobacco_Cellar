package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
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
    val focusManager = LocalFocusManager.current

    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .noRippleClickable(onClick = { focusManager.clearFocus() }),
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
            BlendDetailsBody(
                blendDetails = viewModel.blendDetails,
                viewModel = viewModel,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}


@Composable
fun BlendDetailsBody(
    blendDetails: BlendDetails,
    viewModel: BlendDetailsViewModel,
    modifier: Modifier = Modifier
) {
    fun buildString(title: String, value: String): AnnotatedString {
        val string = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)) { append(title) }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) { append(value) }
        }
        return string
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Blend Name
        SelectionContainer {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = blendDetails.blend,
                    modifier = Modifier
                        .padding(bottom = 2.dp),
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                            )
                        ) { append("by ") }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp,
                            )
                        ) { append(blendDetails.brand) }
                    },
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
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
                .background(LocalCustomColors.current.darkNeutral, RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Details",
                    modifier = Modifier
                        .padding(bottom = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )
                if (blendDetails.favorite || blendDetails.disliked) {
                    val icon =
                        if (blendDetails.favorite) R.drawable.heart_filled_24 else R.drawable.heartbroken_filled_24
                    val tint =
                        if (blendDetails.favorite) LocalCustomColors.current.favHeart else LocalCustomColors.current.disHeart
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(20.dp),
                        tint = tint
                    )
                }
            }
            SelectionContainer {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                ) {
                    val type = if (blendDetails.type.isBlank()) "Unassigned" else blendDetails.type
                    Text(
                        text = buildString("Type: ", type),
                        modifier = Modifier,
                    )
                    if (blendDetails.subGenre.isNotBlank()) {
                        Text(
                            text = buildString("Subgenre: ", blendDetails.subGenre),
                            modifier = Modifier,
                        )
                    }
                    if (blendDetails.cut.isNotBlank()) {
                        Text(
                            text = buildString("Cut: ", blendDetails.cut),
                            modifier = Modifier,
                        )
                    }
                    if (blendDetails.componentList.isNotBlank()) {
                        Text(
                            text = buildString("Components: ", blendDetails.componentList),
                            modifier = Modifier,
                        )
                    }
                    val productionStatus =
                        if (blendDetails.inProduction) "in production" else "not in production"
                    val productionStatusColor =
                        if (blendDetails.inProduction) LocalContentColor.current else MaterialTheme.colorScheme.error
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            ) { append("Production Status: ") }
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    color = productionStatusColor,
                                    fontSize = 14.sp
                                )
                            ) { append(productionStatus) }
                        },
                        modifier = Modifier,
                    )
                    Text(
                        text = buildString(
                            "No. of Tins: ",
                            blendDetails.quantity.toString(),
                        ),
                        modifier = Modifier,
                    )
                }
            }
        }


        // Notes
        if (blendDetails.notes.isNotBlank()) {
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
                        .padding(bottom = 6.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                SelectionContainer {
                    NotesText(
                        notes = blendDetails.notes,
                        modifier = Modifier
                            .padding(start = 12.dp, bottom = 8.dp)
                    )
                }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Tins",
                        modifier = Modifier
                            .padding(bottom = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                    if (blendDetails.tins.any{ it.tinQuantity > 0}) {
                        Text(
                            text = "(${blendDetails.tinsTotal})",
                            modifier = Modifier,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                SelectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        blendDetails.tins.forEach {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = it.tinLabel,
                                        modifier = Modifier,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                    )
                                    if (it.container.isNotEmpty() || it.unit.isNotEmpty() || it.manufactureDate != null || it.cellarDate != null || it.openDate != null) {
                                        Column(
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.spacedBy(
                                                0.dp,
                                                Alignment.Top
                                            ),
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                        ) {
                                            if (it.container.isNotEmpty()) {
                                                Text(
                                                    text = buildString(
                                                        "Container: ",
                                                        it.container,
                                                    ),
                                                    modifier = Modifier,
                                                )
                                            }
                                            if (it.unit.isNotEmpty()) {
                                                val quantity = viewModel.formatDecimal(it.tinQuantity)
                                                Text(
                                                    text = buildString(
                                                        "Quantity: ",
                                                        "$quantity ${it.unit}"
                                                    ),
                                                    modifier = Modifier,
                                                )
                                            }
                                            if (it.manufactureDate != null) {
                                                Text(
                                                    text = buildString(
                                                        "Manufacture Date: ",
                                                        formatLongDate(it.manufactureDate),
                                                    ),
                                                    modifier = Modifier,
                                                )
                                                Text(
                                                    text = "(${viewModel.calculateAge(it.manufactureDate, "manufacture")})",
                                                    modifier = Modifier
                                                        .padding(start = 16.dp),
                                                    fontSize = 12.sp,
                                                    lineHeight = 12.sp,
                                                )
                                            }
                                            if (it.cellarDate != null) {
                                                Text(
                                                    text = buildString(
                                                        "Cellar Date: ",
                                                        formatLongDate(it.cellarDate),
                                                    ),
                                                    modifier = Modifier,
                                                )
                                                Text(
                                                    text = "(${viewModel.calculateAge(it.cellarDate, "cellar")})",
                                                    modifier = Modifier
                                                        .padding(start = 16.dp),
                                                    fontSize = 12.sp,
                                                    lineHeight = 12.sp,
                                                )
                                            }
                                            if (it.openDate != null) {
                                                Text(
                                                    text = buildString(
                                                        "Opened Date: ",
                                                        formatLongDate(it.openDate),
                                                    ),
                                                    modifier = Modifier,
                                                )
                                                Text(
                                                    text = "(${viewModel.calculateAge(it.openDate, "open")})",
                                                    modifier = Modifier
                                                        .padding(start = 16.dp),
                                                    fontSize = 12.sp,
                                                    lineHeight = 12.sp,
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.spacedBy(
                                                0.dp,
                                                Alignment.Top
                                            ),
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                        ) {
                                            Text(
                                                text = "No details available.",
                                                modifier = Modifier,
                                                fontSize = 14.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .height(6.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
    }
}


@Composable
fun NotesText(
    notes: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
) {
    val lines = notes.split("\n")
    val blankLine = 10.sp
    val blankLineHeight: Dp = with(LocalDensity.current) { blankLine.toDp() }

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
                    fontSize = fontSize,
                )
            }
        }
    }
}