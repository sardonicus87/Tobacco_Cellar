package com.sardonicus.tobaccocellar.ui.filtering.sections

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.CheckboxWithLabel
import com.sardonicus.tobaccocellar.ui.composables.RatingRow
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

@Composable
fun OtherFiltersSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val favDisExist by filterViewModel.favDisExist.collectAsState()
    val ratingsExist by filterViewModel.ratingsExist.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            // Ratings //
            Column(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        LocalCustomColors.current.sheetBoxBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                    .width(229.dp)
                    .padding(vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                FavoriteDislikeFilters(filterViewModel, { favDisExist }, { ratingsExist })

                StarRatingFilters(filterViewModel, { favDisExist }, { ratingsExist })
            }
            if (!favDisExist && !ratingsExist) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            Dp.Hairline,
                            LocalCustomColors.current.sheetBoxBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                            RoundedCornerShape(8.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ratings assigned.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // In Stock
        Box {
            InStockSection(filterViewModel)
        }
    }
}

@Composable
private fun FavoriteDislikeFilters(
    filterViewModel: FilterViewModel,
    favDisExist: () -> Boolean,
    ratingsExist: () -> Boolean
) {
    val favoritesSelection by filterViewModel.favoriteSelection.collectAsState()
    val dislikedsSelection by filterViewModel.dislikedSelection.collectAsState()
    val favoritesEnabled by filterViewModel.favoritesEnabled.collectAsState()
    val dislikedsEnabled by filterViewModel.dislikedsEnabled.collectAsState()

    Box {
        Row {
            TriStateCheckWithLabel(
                text = { "Favorites" },
                state = { favoritesSelection },
                onClick = filterViewModel::updateFavSelection,
                colors = {
                    CheckboxDefaults.colors(
                        checkedColor =
                            when (favoritesSelection) {
                                ToggleableState.On -> MaterialTheme.colorScheme.primary
                                ToggleableState.Indeterminate -> MaterialTheme.colorScheme.error
                                else -> Color.Transparent
                            },
                    )
                },
                enabled = { favoritesEnabled },
                maxLines = { 1 },
            )

            TriStateCheckWithLabel(
                text = { "Dislikes" },
                state = { dislikedsSelection },
                onClick = filterViewModel::updateDisSelection,
                colors = {
                    CheckboxDefaults.colors(
                        checkedColor =
                            when (dislikedsSelection) {
                                ToggleableState.On -> MaterialTheme.colorScheme.primary
                                ToggleableState.Indeterminate -> MaterialTheme.colorScheme.error
                                else -> Color.Transparent
                            },
                    )
                },
                enabled = { dislikedsEnabled },
                maxLines = { 1 },
            )
        }
        if (!favDisExist() && ratingsExist()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                        RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)
                    )
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorites/dislikes assigned.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun StarRatingFilters(
    filterViewModel: FilterViewModel,
    favDisExist: () -> Boolean,
    ratingsExist: () -> Boolean
) {
    val rangeEnabled by filterViewModel.rangeEnabled.collectAsState()
    val ratingLowEnabled by filterViewModel.ratingLowEnabled.collectAsState()
    val ratingHighEnabled by filterViewModel.ratingHighEnabled.collectAsState()
    val unratedEnabled by filterViewModel.unratedEnabled.collectAsState()

    val unchosen by filterViewModel.rangeUnchosen.collectAsState()
    val unrated by filterViewModel.sheetSelectedUnrated.collectAsState()
    val ratingLow by filterViewModel.sheetSelectedRatingLow.collectAsState()
    val ratingHigh by filterViewModel.sheetSelectedRatingHigh.collectAsState()

    val lowText by filterViewModel.rangeLowText.collectAsState()
    val highText by filterViewModel.rangeHighText.collectAsState()
    val lowTextAlpha by filterViewModel.rangeLowTextAlpha.collectAsState()
    val highTextAlpha by filterViewModel.rangeHighTextAlpha.collectAsState()
    val ratingRowEmptyAlpha by filterViewModel.ratingRowEmptyAlpha.collectAsState()

    val showRatingPop by filterViewModel.showRatingPop.collectAsState()

    Box {
        Row(
            modifier = Modifier
                .height(36.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .alpha(if (rangeEnabled) 1f else .38f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Rating:",
                modifier = Modifier
                    .padding(end = 8.dp),
                fontSize = 15.sp
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        enabled = rangeEnabled,
                        onClick = filterViewModel::onShowRatingPop
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
            ) {
                Box (
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .width(20.dp)
                ) {
                    Text(
                        text = lowText,
                        modifier = Modifier,
                        fontSize = 13.sp,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        color = LocalContentColor.current.copy(alpha = lowTextAlpha)
                    )
                }
                RatingRow(
                    range = Pair(ratingLow, ratingHigh),
                    modifier = Modifier
                        .padding(horizontal = 5.dp),
                    starSize = 18.dp,
                    showDivider = true,
                    minColor = LocalContentColor.current,
                    minAlpha = .38f,
                    emptyColor = if (unchosen || (ratingLow != null && ratingHigh == null)) LocalCustomColors.current.starRating else LocalContentColor.current,
                    emptyAlpha = ratingRowEmptyAlpha
                )
                Box (
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .width(20.dp)
                ) {
                    Text(
                        text = highText,
                        modifier = Modifier,
                        fontSize = 13.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Start,
                        color = LocalContentColor.current.copy(alpha = highTextAlpha)
                    )
                }
            }
        }

        if (favDisExist() && !ratingsExist()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        LocalCustomColors.current.sheetBox.copy(alpha = .85f),
                        RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)
                    )
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ratings assigned.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }

    if (showRatingPop) {
        RatingRangePop(
            unrated = { unrated },
            unratedEnabled = { unratedEnabled },
            ratingLow = { ratingLow },
            ratingLowEnabled = { ratingLowEnabled },
            ratingHigh = { ratingHigh },
            ratingHighEnabled = { ratingHighEnabled },
            updateSelectedUnrated = filterViewModel::updateSelectedUnrated,
            updateSelectedRatingRange = filterViewModel::updateSelectedRatingRange,
            onDismiss = filterViewModel::onShowRatingPop,
            modifier = Modifier,
        )
    }
}

@Composable
private fun RatingRangePop(
    unrated: () -> Boolean,
    unratedEnabled: () -> Boolean,
    ratingLow: () -> Double?,
    ratingLowEnabled: () -> Double?,
    ratingHigh: () -> Double?,
    ratingHighEnabled: () -> Double?,
    updateSelectedUnrated: (Boolean) -> Unit,
    updateSelectedRatingRange: (Double?, Double?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var ratingLowString by rememberSaveable { mutableStateOf(formatDecimal(ratingLow())) }
    var selectedLow by rememberSaveable { mutableStateOf(ratingLow()) }
    val minRating by rememberSaveable { mutableStateOf(ratingLowEnabled()) }

    var ratingHighString by rememberSaveable { mutableStateOf(formatDecimal(ratingHigh())) }
    var selectedHigh by rememberSaveable { mutableStateOf(ratingHigh()) }
    val maxRating by rememberSaveable { mutableStateOf(ratingHighEnabled()) }

    val compMin = maxOf((selectedLow ?: 0.0), (minRating ?: 0.0))
    val compMax = minOf((selectedHigh ?: 5.0), (maxRating ?: 5.0))

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
    val decimalSeparator = symbols.decimalSeparator.toString()
    val allowedPattern = remember(decimalSeparator) {
        val ds = Regex.escape(decimalSeparator)
        Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .wrapContentHeight(),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = "Rating",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
            ) {
                CheckboxWithLabel(
                    text = "Unrated",
                    checked = unrated(),
                    onCheckedChange = {
                        updateSelectedUnrated(it)
                        updateSelectedRatingRange(selectedLow, selectedHigh)
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                    enabled = unratedEnabled(),
                    fontColor = if (!unratedEnabled()) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current,
                )
                // Rating Range //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Low Rating //
                    TextField(
                        value = ratingLowString,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                ratingLowString = it

                                try {
                                    if (it.isNotBlank()) {
                                        val preNumber = if (it.startsWith(decimalSeparator)) {
                                            "0$it"
                                        } else it
                                        val number = numberFormat.parse(preNumber)?.toDouble()

                                        selectedLow = when {
                                            number == null -> null
                                            number < (minRating ?: 0.0) -> (minRating ?: 0.0)
                                            number > compMax -> compMax
                                            else -> number
                                        }
                                    } else {
                                        selectedLow = null
                                    }

                                } catch (_: ParseException) {
                                    selectedLow = null
                                }
                            }
                        },
                        modifier = Modifier
                            .width(70.dp)
                            .padding(end = 8.dp)
                            .onFocusChanged {
                                if (!it.isFocused) {
                                    if (ratingLowString != formatDecimal(selectedLow))
                                        ratingLowString = formatDecimal(selectedLow)
                                    updateSelectedRatingRange(selectedLow, selectedHigh)
                                }
                            },
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                updateSelectedRatingRange(selectedLow, selectedHigh)
                                this.defaultKeyboardAction(ImeAction.Done)
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField,
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )

                    // Selected Range Display //
                    val lowField = ratingLowString.toDoubleOrNull()?.coerceIn(0.0, compMax)
                    val highField = ratingHighString.toDoubleOrNull()?.coerceIn(compMin, 5.0)

                    val emptyColor = if (ratingLowString.isNotBlank() && ratingHighString.isBlank()) LocalCustomColors.current.starRating else LocalContentColor.current
                    val emptyAlpha = if (ratingLowString.isNotBlank() && ratingHighString.isBlank()) 1f else .5f

                    RatingRow(
                        range = Pair(lowField, highField),
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                        starSize = 20.dp,
                        showDivider = true,
                        minColor = LocalContentColor.current,
                        maxColor = LocalCustomColors.current.starRating,
                        emptyColor = emptyColor,
                        minAlpha = .5f,
                        maxAlpha = 1f,
                        emptyAlpha = emptyAlpha
                    )

                    // High Rating //
                    TextField(
                        value = ratingHighString,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                ratingHighString = it

                                try {
                                    if (it.isNotBlank()) {
                                        val preNumber = if (it.startsWith(decimalSeparator)) {
                                            "0$it"
                                        } else it
                                        val number = numberFormat.parse(preNumber)?.toDouble()

                                        selectedHigh = when {
                                            number == null -> null
                                            number < compMin -> compMin
                                            number > (maxRating ?: 5.0) -> (maxRating ?: 5.0)
                                            else -> number
                                        }
                                    } else {
                                        selectedHigh = null
                                    }

                                } catch (_: ParseException) {
                                    selectedHigh = null
                                }
                            }
                        },
                        modifier = Modifier
                            .width(70.dp)
                            .padding(start = 8.dp)
                            .onFocusChanged {
                                if (!it.isFocused) {
                                    if (ratingHighString != formatDecimal(selectedHigh))
                                        ratingHighString = formatDecimal(selectedHigh)
                                    updateSelectedRatingRange(selectedLow, selectedHigh)
                                }
                            },
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                updateSelectedRatingRange(selectedLow, selectedHigh)
                                this.defaultKeyboardAction(ImeAction.Done)
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField,
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                }

                // Clear buttons and available range //
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 29.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lowAlpha = if (ratingLowString.isNotBlank()) .75f else 0.38f
                    val highAlpha = if (ratingHighString.isNotBlank()) .75f else 0.38f

                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = ratingLowString.isNotBlank()
                            ) {
                                ratingLowString = ""
                                selectedLow = null
                                updateSelectedRatingRange(null, selectedHigh)
                            }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(lowAlpha)
                    )
                    Text(
                        text = "(Limits: ${formatDecimal(minRating).ifBlank { "0.0" }} - ${formatDecimal(maxRating).ifBlank { "5.0" }})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier,
                        color = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = ratingHighString.isNotBlank()
                            ) {
                                ratingHighString = ""
                                selectedHigh = null
                                updateSelectedRatingRange(selectedLow, null)
                            }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(highAlpha)
                    )
                }

                // Clear all button //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = {
                            ratingLowString = ""
                            selectedLow = null
                            ratingHighString = ""
                            selectedHigh = null
                            updateSelectedUnrated(false)
                            updateSelectedRatingRange(null, null)
                        },
                        enabled = ratingLowString.isNotBlank() || ratingHighString.isNotBlank() || unrated(),
                        modifier = Modifier
                            .offset(x = (-4).dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(end = 3.dp)
                                .size(20.dp)
                        )
                        Text(
                            text = "Clear All",
                            modifier = Modifier,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    updateSelectedRatingRange(selectedLow, selectedHigh)
                    onDismiss()
                },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
private fun InStockSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val inStock by filterViewModel.sheetSelectedInStock.collectAsState()
    val outOfStock by filterViewModel.sheetSelectedOutOfStock.collectAsState()
    val inStockEnabled by filterViewModel.inStockEnabled.collectAsState()
    val outOfStockEnabled by filterViewModel.outOfStockEnabled.collectAsState()

    Column(
        modifier = modifier
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.Start
    ) {
        LocalCheckboxWithLabel(
            text = { "In-stock" },
            checked = { inStock },
            onCheckedChange = filterViewModel::updateSelectedInStock,
            modifier = Modifier,
            enabled = { inStockEnabled },
            allowResize = { true }
        )
        LocalCheckboxWithLabel(
            text = { "Out" },
            checked = { outOfStock },
            onCheckedChange = filterViewModel::updateSelectedOutOfStock,
            modifier = Modifier,
            enabled = { outOfStockEnabled },
            allowResize = { false }
        )
    }
}


@Composable
private fun TriStateCheckWithLabel(
    text: () -> String,
    state: () -> ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: () -> Boolean = { true },
    maxLines: () -> Int = { 1 },
    colors: @Composable () -> CheckboxColors = { CheckboxDefaults.colors() },
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() }
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(36.dp)
            .offset(x = (-2).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box {
            TriStateCheckbox(
                state = state(),
                onClick = onClick,
                modifier = Modifier
                    .padding(0.dp),
                enabled = enabled(),
                colors = colors(),
                interactionSource = interactionSource
            )
        }
        Text(
            text = text(),
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
            color = if (enabled()) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f),
            fontSize = 15.sp,
            maxLines = maxLines(),
        )
    }
}


@Composable
fun LocalCheckboxWithLabel(
    text: () -> String,
    checked: () -> Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: () -> Boolean = { true },
    allowResize: () -> Boolean = { false },
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() }
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(36.dp)
            .offset(x = (-2).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            Checkbox(
                checked = checked(),
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .padding(0.dp),
                enabled = enabled(),
                colors = CheckboxDefaults.colors(),
                interactionSource = interactionSource
            )
        }
        Text(
            text = text(),
            style = LocalTextStyle.current.copy(
                color = if (enabled()) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f),
                lineHeight = TextUnit.Unspecified
            ),
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
            maxLines = 1,
            fontSize = if (!allowResize()) 15.sp else TextUnit.Unspecified,
            autoSize = if (!allowResize()) { null } else {
                TextAutoSize.StepBased(
                    maxFontSize = 15.sp,
                    minFontSize = 9.sp,
                    stepSize = .2.sp
                )
            }
        )
    }
}