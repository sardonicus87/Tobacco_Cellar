package com.sardonicus.tobaccocellar.ui.details

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
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
import com.sardonicus.tobaccocellar.ui.composables.RatingRow
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlendDetailsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    isTwoPane: Boolean,
    viewModel: BlendDetailsViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val blendDetails by viewModel.blendDetails.collectAsState()
    val selectionFocused by viewModel.selectionFocused.collectAsState()
    val contentVisible by viewModel.contentVisible.collectAsState()

    BackHandler(selectionFocused) {
        if (selectionFocused) {
            viewModel.resetSelection()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetSelection()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null, onClick = viewModel::resetSelection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.blend_details_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateUp = {
                    if (selectionFocused) {
                        viewModel.resetSelection()
                        onNavigateUp()
                    } else { onNavigateUp() }
                },
                showMenu = false,
                overrideBack = isTwoPane,
                modifier = Modifier
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
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(500))
            ) {
                BlendDetailsBody(
                    blendDetails = blendDetails,
                    viewModel = viewModel,
                    navigateToEditEntry = {
                        viewModel.resetSelection()
                        navigateToEditEntry(it)
                    },
                    selectionFocused = viewModel::updateFocused,
                    modifier = Modifier
                )
            }
        }
    }
}


@Composable
fun BlendDetailsBody(
    blendDetails: BlendDetails,
    viewModel: BlendDetailsViewModel,
    navigateToEditEntry: (Int) -> Unit,
    selectionFocused: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectionKey by viewModel.selectionKey.collectAsState()

    LazyColumn (
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        item {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = 16.dp)
            ) {
                Spacer(Modifier.width(24.dp))
                // Blend name
                Box(Modifier.weight(1f)) {
                    key(selectionKey) { SelectionContainer(
                        Modifier.onFocusChanged { selectionFocused(it.isFocused) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = blendDetails.blend,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
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
                    } }
                }
                // Favorite/Disliked
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp)
                        .padding(top = 0.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (blendDetails.favDisIcon != null) {
                        val tint =
                            if (blendDetails.favDisIcon == R.drawable.heart_filled_24) LocalCustomColors.current.favHeart else LocalCustomColors.current.disHeart
                        Icon(
                            painter = painterResource(id = blendDetails.favDisIcon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp),
                            tint = tint
                        )
                    }
                }
            }
        }

        // Blend Details
        item {
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
                    Spacer(Modifier.weight(1f))
                    Icon(
                        painter = painterResource(R.drawable.edit_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .offset(y = 2.dp)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { navigateToEditEntry(blendDetails.id) },
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                    )
                }
                key(selectionKey) { SelectionContainer(
                    Modifier.onFocusChanged { selectionFocused(it.isFocused) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp)
                    ) {
                        blendDetails.itemDetails.forEach {
                            Text(
                                text = it,
                                modifier = Modifier,
                            )
                        }
                        if (blendDetails.rating != null) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                            ) {
                                Text(
                                    text = "Rating: ",
                                    modifier = Modifier,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                                RatingRow(
                                    rating = blendDetails.rating,
                                    modifier = Modifier,
                                    starSize = 17.dp
                                )
                                Text(
                                    text = "(${formatDecimal(blendDetails.rating)})",
                                    modifier = Modifier
                                        .padding(start = 6.dp),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                } }
            }
        }


        // Notes
        if (blendDetails.notes.isNotBlank()) {
            item {
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
                    Text(
                        text = "Notes",
                        modifier = Modifier
                            .padding(bottom = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    key(selectionKey) { SelectionContainer(
                        Modifier.onFocusChanged { selectionFocused(it.isFocused) }
                    ) {
                        NotesText(
                            notes = blendDetails.notes,
                            viewModel = viewModel,
                            modifier = Modifier
                                .padding(start = 12.dp, bottom = 8.dp)
                        )
                    } }
                }
            }
        }

        // Tins
        if (blendDetails.tinsDetails.isNotEmpty()) {
            item {
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

                        Spacer(Modifier.weight(1f))

                        if (blendDetails.tinsTotal.isNotBlank()) {
                            key(selectionKey) { SelectionContainer(
                                Modifier.onFocusChanged { selectionFocused(it.isFocused) }
                            ) {
                                Text(
                                    text = "(${blendDetails.tinsTotal})",
                                    modifier = Modifier,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            } }
                        }
                    }
                    key(selectionKey) { SelectionContainer(
                        Modifier.onFocusChanged { selectionFocused(it.isFocused) }
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            blendDetails.tinsDetails.forEach { (tin, details) ->
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = tin.tinLabel,
                                            modifier = Modifier,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                        )
                                        details?.forEach { detailLine ->
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
                                                    text = detailLine.primary,
                                                    modifier = Modifier,
                                                )
                                                detailLine.secondary?.let {
                                                    Text(
                                                        text = it,
                                                        lineHeight = 12.sp,
                                                        modifier = Modifier
                                                            .padding(start = 16.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}


@Composable
fun NotesText(
    notes: String,
    viewModel: BlendDetailsViewModel,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
) {
    val lines = notes.split("\n")
    val blankLine = 10.sp
    val blankLineHeight: Dp = with(LocalDensity.current) { blankLine.toDp() }

    val parseLinks by viewModel.parseLinks.collectAsState()
    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current
    val linkListener = LinkInteractionListener { link ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        (link as? LinkAnnotation.Url)?.let {
            uriHandler.openUri(it.url)
        }
    }

    Column(
        modifier = modifier
    ) {
        lines.forEach { line ->
            if (line.isEmpty()) {
                Spacer(Modifier.height(blankLineHeight))
            } else {

                Text(
                    text = viewModel.parseHyperlinks(line, MaterialTheme.colorScheme.primary, linkListener, parseLinks),
                    fontSize = fontSize,
                    modifier = Modifier
                )
            }
        }
    }
}