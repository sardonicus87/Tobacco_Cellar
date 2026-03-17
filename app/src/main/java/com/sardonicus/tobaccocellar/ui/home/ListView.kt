package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun ListViewMode(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
    sortedItems: ItemsList,
    columnState: LazyListState,
    onDetailsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onShowMenu: (Int) -> Unit,
    onDismissMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current

    val activeMenuId by viewModel.activeMenuId.collectAsState()

    val onClick = remember {
        { itemId: Int ->
            if (filterViewModel.searchFocused.value) {
                focusManager.clearFocus()
            } else {
                if (activeMenuId == itemId) { // currentMenuId
                    // do nothing
                }
                else if (activeMenuId != null) {
                    onDismissMenu()
                }
                else {
                    if (!filterViewModel.searchPerformed.value) {
                        filterViewModel.getPositionTrigger()
                    }
                    onDetailsClick(itemId)
                }
            }
        }
    }
    val onLongClick = remember {
        { itemId: Int ->
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (!filterViewModel.searchPerformed.value) {
                filterViewModel.getPositionTrigger()
            }
            onShowMenu(itemId)
        }
    }

    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .background(LocalCustomColors.current.backgroundVariant)
                .padding(0.dp),
            state = columnState,
        ) {
            items(items = sortedItems.list, key = { it.itemId }) { item ->
                val openMenu by rememberSaveable(item.itemId, activeMenuId) { mutableStateOf(activeMenuId == item.itemId) }

                ListItem(
                    brand = { item.item.items.brand },
                    blend = { item.item.items.blend },
                    favorite = { item.item.items.favorite },
                    disliked = { item.item.items.disliked },
                    notes = { item.item.items.notes },
                    typeGenreText = { item.formattedTypeGenre },
                    formattedQuantity = { item.formattedQuantity },
                    outOfStock = { item.outOfStock },
                    rating = { item.rating },
                    onEditClick = { onEditClick(item.itemId) },
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onClick(item.itemId) },
                            onLongClick = { onLongClick(item.itemId) },
                            indication = null,
                            interactionSource = null
                        ),
                    onMenuDismiss = onDismissMenu,
                    showMenu = { openMenu },
                    filteredTins = item.tins,
                )
            }
        }
    }
}


@Composable
private fun ListItem(
    brand: () -> String,
    blend: () -> String,
    favorite: () -> Boolean,
    disliked: () -> Boolean,
    notes: () -> String,
    typeGenreText: () -> String,
    formattedQuantity: () -> String,
    outOfStock: () -> Boolean,
    rating: () -> String,
    filteredTins: TinsList,
    onMenuDismiss: () -> Unit,
    showMenu: () -> Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = modifier
                .padding(bottom = 1.dp)
        ) {
            // main details
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(start = 8.dp, top = 4.dp, bottom = 2.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Entry info
                    MainDetails(
                        brand = brand,
                        blend = blend,
                        favorite = favorite,
                        disliked = disliked,
                        notes = notes,
                        rating = rating,
                        typeGenreText = typeGenreText,
                        modifier = Modifier
                            .weight(1f, false)
                    )

                    // Quantity
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .padding(0.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        QuantityColumn(
                            formattedQuantity = formattedQuantity,
                            outOfStock = outOfStock,
                            modifier = Modifier
                        )
                    }
                }

                // Tins
                if (filteredTins.tins.isNotEmpty()) {
                    GlowBox(
                        color = GlowColor(Color.Black.copy(alpha = .5f)),
                        size = GlowSize(top = 3.dp),
                        modifier = Modifier
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            TinList(
                                filteredTins = filteredTins,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }

            if (showMenu()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(0.dp)
                ) {
                    ItemMenu(
                        onMenuDismiss = onMenuDismiss,
                        onEditClick = onEditClick,
                        modifier = Modifier
                            .height(54.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainDetails(
    blend: () -> String,
    brand: () -> String,
    favorite: () -> Boolean,
    disliked: () -> Boolean,
    notes: () -> String,
    rating: () -> String,
    typeGenreText: () -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(fraction = .95f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = blend(),
                modifier = Modifier
                    .weight(1f, false)
                    .padding(end = 4.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textDecoration = TextDecoration.None
                ),
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconRow(favorite, disliked, notes)
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .offset(y = (-4).dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Start),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = brand(),
                modifier = Modifier,
                fontStyle = Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textDecoration = TextDecoration.None
                )
            )
            if (typeGenreText().isNotEmpty()){
                Text (
                    text = typeGenreText(),
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textDecoration = TextDecoration.None
                    ),
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
            if (rating().isNotEmpty()) {
                RatingLabel(rating)
            }
        }
    }
}

@Composable
private fun QuantityColumn(
    formattedQuantity: () -> String,
    outOfStock: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = formattedQuantity(),
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium.copy(
            color = if (outOfStock()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
            textDecoration = TextDecoration.None
        ),
        fontWeight = FontWeight.Normal,
        maxLines = 1,
        fontSize = 16.sp
    )
}

@Composable
private fun IconRow(
    favorite: () -> Boolean,
    disliked: () -> Boolean,
    notes: () -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (favorite()) {
            Icon(
                painter = painterResource(id = R.drawable.heart_filled_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(17.dp),
                tint = LocalCustomColors.current.favHeart
            )
        }
        if (disliked()) {
            Icon(
                painter = painterResource(id = R.drawable.heartbroken_filled_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(17.dp),
                tint = LocalCustomColors.current.disHeart
            )
        }
        if (notes().isNotBlank()) {
            Icon(
                painter = painterResource(id = R.drawable.notes_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun RatingLabel(
    rating: () -> String,
    modifier: Modifier = Modifier
) {
    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rating(),
            modifier = Modifier,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
        )
        Image(
            painter = painterResource(id = R.drawable.star_filled),
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalCustomColors.current.starRating),
            alignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 2.dp)
                .size(12.dp),
        )
    }
}

@Composable
private fun TinList(
    filteredTins: TinsList,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                LocalCustomColors.current.sheetBox,
                RoundedCornerShape(bottomStart = 8.dp)
            )
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
            .fillMaxWidth()
    ) {
        filteredTins.tins.forEach {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = it.tinLabel,
                    modifier = Modifier,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                if (it.container.isNotEmpty() || it.unit.isNotEmpty()) {
                    val tinInfo = remember(it.container, it.tinQuantity, it.unit) {
                        buildString {
                            append(" (")
                            if (it.container.isNotEmpty()) {
                                append(it.container)
                            }
                            if (it.container.isNotEmpty() && it.unit.isNotEmpty()) {
                                append(" - ")
                            }
                            if (it.unit.isNotEmpty()) {
                                val quantity = formatDecimal(it.tinQuantity)
                                val unit = if (it.unit == "grams") "g" else it.unit
                                append("$quantity $unit")
                            }
                            append(")")
                        }
                    }

                    Text(
                        text = tinInfo,
                        modifier = Modifier,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
                if (it.finished) {
                    Text(
                        text = " (Finished)",
                        modifier = Modifier,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        fontStyle = Italic,
                        color = LocalContentColor.current.copy(alpha = .5f)
                    )
                }
            }
        }
    }
}