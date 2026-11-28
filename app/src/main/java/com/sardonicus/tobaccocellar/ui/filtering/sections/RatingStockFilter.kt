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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.CheckboxWithLabel
import com.sardonicus.tobaccocellar.ui.composables.RatingRow
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import com.sardonicus.tobaccocellar.ui.utilities.localizedToDouble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import java.text.DecimalFormatSymbols

@Composable
fun OtherFiltersSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val favDisExist by filterViewModel.favDisExist.collectAsState()
    val ratingsExist by filterViewModel.ratingsExist.collectAsState()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.padding(end = 12.dp)) {
            Column(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        LocalCustomColors.current.sheetBoxBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
                    .padding(vertical = 3.dp)
                    .requiredWidthIn(min = 180.dp),
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
                        text = stringResource(R.string.no_ratings_v2),
                        autoSize = TextAutoSize.StepBased(11.sp, 14.sp),
                        maxLines = 1,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
        }
        InStockSection(filterViewModel, Modifier.requiredWidthIn(min = 110.dp))
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
        Row(
            Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TriStateCheckWithLabel(
                text = { stringResource(R.string.favorites_v2) },
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
                enabled = { (favoritesEnabled || favoritesSelection != ToggleableState.Off) && favDisExist() },
                maxLines = { 1 },
            )

            TriStateCheckWithLabel(
                text = { stringResource(R.string.dislikes_v2) },
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
                enabled = { (dislikedsEnabled || dislikedsSelection != ToggleableState.Off) && favDisExist() },
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
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_fav_dis_v2),
                    autoSize = TextAutoSize.StepBased(12.sp, 14.sp),
                    maxLines = 1,
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
    val unrated by filterViewModel.selectedUnrated.collectAsState()
    val ratingLow by filterViewModel.selectedRatingLow.collectAsState()
    val lowLookahead by filterViewModel.lowLookahead.collectAsState()
    val ratingHigh by filterViewModel.selectedRatingHigh.collectAsState()
    val highLookahead by filterViewModel.highLookahead.collectAsState()

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
                .padding(start = 12.dp, end = 10.dp)
                .alpha(if (rangeEnabled) 1f else .38f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.rating_label_v2),
                modifier = Modifier,
                autoSize = TextAutoSize.StepBased(12.sp, 15.sp),
                maxLines = 1
            )
            Row(
                modifier = Modifier.clickable(null, LocalIndication.current, rangeEnabled, onClick = filterViewModel::onShowRatingPop),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
            ) {
                Box (
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier.requiredWidth(20.dp)
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
                    modifier = Modifier.padding(horizontal = 5.dp),
                    starSize = 18.dp,
                    showDivider = true,
                    minColor = LocalContentColor.current,
                    minAlpha = .38f,
                    emptyColor = if (unchosen || (ratingLow != null && ratingHigh == null)) LocalCustomColors.current.starRating else LocalContentColor.current,
                    emptyAlpha = ratingRowEmptyAlpha
                )
                Box (
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.requiredWidth(20.dp)
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
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_ratings_v2),
                    autoSize = TextAutoSize.StepBased(12.sp, 14.sp),
                    maxLines = 1,
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
            lowLookahead = { lowLookahead },
            ratingHigh = { ratingHigh },
            highLookahead = { highLookahead },
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
    lowLookahead: () -> Double?,
    ratingHigh: () -> Double?,
    highLookahead: () -> Double?,
    ratingHighEnabled: () -> Double?,
    updateSelectedUnrated: (Boolean) -> Unit,
    updateSelectedRatingRange: (Double?, Double?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lowString = rememberTextFieldState(formatDecimal(ratingLow()))
    var selectedLow by remember { mutableStateOf(ratingLow()) }
    val minRating by remember { mutableStateOf(ratingLowEnabled()) }

    val highString = rememberTextFieldState(formatDecimal(ratingHigh()))
    var selectedHigh by remember { mutableStateOf(ratingHigh()) }
    val maxRating by remember { mutableStateOf(ratingHighEnabled()) }

    val compMin = maxOf((selectedLow ?: 0.0), (ratingLowEnabled() ?: 0.0))
    val compMax = minOf((selectedHigh ?: 5.0), (ratingHighEnabled() ?: 5.0))

    val symbols = remember { DecimalFormatSymbols.getInstance(LanguageState.locale) }
    val decimalSeparator = symbols.decimalSeparator.toString()
    val allowedPattern = remember(decimalSeparator) {
        val ds = Regex.escape(decimalSeparator)
        Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
    }

    var extendedPop by remember { mutableStateOf(false) }
    var extendedMessage by remember { mutableStateOf<Int?>(null) }

    SideEffect(ratingLow(), ratingHigh()) {
        if (ratingLow() == null && lowString.text.isNotBlank()) lowString.clearText()
        if (ratingHigh() == null && highString.text.isNotBlank()) highString.clearText()
    }

    LaunchedEffect(lowString, highString, Dispatchers.Default) {
        combine(
            snapshotFlow { lowString.text.toString().localizedToDouble(min = minRating ?: 0.0, max = compMax) },
            snapshotFlow { highString.text.toString().localizedToDouble(min = compMin, max = maxRating ?: 5.0) }
        ) { low, high -> low to high }.collectLatest { (low, high) ->
            selectedLow = low; selectedHigh = high
        }
    }

    val lowEdit = {
        if ((selectedLow ?: 0.0) > compMax) selectedLow = compMax
        if (selectedLow != null && lowLookahead() != null) {
            val previousLow = selectedLow
            selectedLow = minOf(selectedLow!!, lowLookahead()!!)
            if (selectedLow != previousLow) {
                extendedMessage = R.string.rating_range_extended_low; extendedPop = true
            }
        }
        lowString.edit { replace(0, length, formatDecimal(selectedLow)) }
        updateSelectedRatingRange(selectedLow, selectedHigh)
    }

    val highEdit = {
        if ((selectedHigh ?: 5.0) < compMin) selectedHigh = compMin
        if (selectedHigh != null && highLookahead() != null) {
            val previousHigh = selectedHigh
            selectedHigh = maxOf(selectedHigh!!, highLookahead()!!)
            if (selectedHigh != previousHigh) {
                extendedMessage = R.string.rating_range_extended_high; extendedPop = true
            }
        }
        highString.edit { replace(0, length, formatDecimal(selectedHigh)) }
        updateSelectedRatingRange(selectedLow, selectedHigh)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.wrapContentHeight(),
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = stringResource(R.string.rating_v2),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 1.em
            )
        },
        text = {
            val focusManager = LocalFocusManager.current
            DisposableEffect(Unit) { onDispose { focusManager.clearFocus(); extendedPop = false } }

            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable(null, null) { focusManager.clearFocus() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
                ) {
                    CheckboxWithLabel(
                        text = stringResource(R.string.unrated_v2),
                        checked = unrated(),
                        onCheckedChange = {
                            updateSelectedUnrated(it)
                            updateSelectedRatingRange(selectedLow, selectedHigh)
                        },
                        modifier = Modifier,
                        enabled = unratedEnabled() || unrated(),
                        fontColor = if (!unratedEnabled()) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current,
                    )
                    // Rating Range //
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Low Rating //
                        TextField(
                            state = lowString,
                            inputTransformation = {
                                if (!this.toString().matches(allowedPattern)) { revertAllChanges() }
                            },
                            modifier = Modifier
                                .width(70.dp)
                                .padding(end = 8.dp)
                                .onFocusChanged { if (!it.isFocused) { lowEdit() } },
                            lineLimits = TextFieldLineLimits.SingleLine,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                textDirection = TextDirection.Ltr,
                                textAlign = TextAlign.End
                            ),
                            onKeyboardAction = { lowEdit(); focusManager.clearFocus() },
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
                        val lowField =
                            lowString.text.toString().localizedToDouble()
                                ?.coerceIn(0.0, compMax)
                        val highField =
                            highString.text.toString().localizedToDouble()
                                ?.coerceIn(compMin, 5.0)

                        val emptyColor =
                            if (lowString.text.isNotBlank() && highString.text.isBlank()) LocalCustomColors.current.starRating else LocalContentColor.current
                        val emptyAlpha =
                            if (lowString.text.isNotBlank() && highString.text.isBlank()) 1f else .5f

                        RatingRow(
                            range = Pair(lowField, highField),
                            modifier = Modifier.padding(horizontal = 4.dp),
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
                            state = highString,
                            inputTransformation = {
                                if (!this.toString().matches(allowedPattern)) { revertAllChanges() }
                            },
                            modifier = Modifier
                                .width(70.dp)
                                .padding(start = 8.dp)
                                .onFocusChanged { if (!it.isFocused) { highEdit() } },
                            lineLimits = TextFieldLineLimits.SingleLine,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                textDirection = TextDirection.Ltr,
                                textAlign = TextAlign.End
                            ),
                            onKeyboardAction = { highEdit(); focusManager.clearFocus() },
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
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(null, LocalIndication.current, lowString.text.isNotBlank()) {
                                    lowString.clearText(); selectedLow = null
                                    updateSelectedRatingRange(null, selectedHigh)
                                }
                                .padding(4.dp)
                                .requiredSize(20.dp)
                                .alpha(if (lowString.text.isNotBlank()) .75f else 0.38f)
                        )
                        Text(
                            text = stringResource(
                                R.string.rating_limits_v2,
                                formatDecimal(minRating).ifBlank { "0.0" },
                                formatDecimal(maxRating).ifBlank { "5.0" }),
                            autoSize = TextAutoSize.StepBased(10.sp, 12.sp),
                            maxLines = 1,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.align(Alignment.Top),
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(null, LocalIndication.current, highString.text.isNotBlank()) {
                                    highString.clearText(); selectedHigh = null
                                    updateSelectedRatingRange(selectedLow, null)
                                }
                                .padding(4.dp)
                                .requiredSize(20.dp)
                                .alpha(if (highString.text.isNotBlank()) .75f else 0.38f)
                        )
                    }

                    // Clear all button //
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = {
                                lowString.clearText(); selectedLow = null
                                highString.clearText(); selectedHigh = null
                                updateSelectedUnrated(false); updateSelectedRatingRange(null, null)
                            },
                            enabled = lowString.text.isNotBlank() || highString.text.isNotBlank() || unrated(),
                            modifier = Modifier.offset(x = (-4).dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(end = 3.dp)
                                    .size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.clear_all_v2),
                                modifier = Modifier,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            if (extendedPop && extendedMessage != null) {
                BasicAlertDialog(
                    onDismissRequest = { extendedPop = false; extendedMessage = null },
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.extraSmall)
                            .width(120.dp)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                    ) {
                        Text(
                            text = if (extendedMessage != null) { stringResource(extendedMessage!!) } else "",
                            fontSize = 15.sp
                        )
                        TextButton(
                            onClick = { updateSelectedRatingRange(selectedLow, selectedHigh); onDismiss() },
                            contentPadding = PaddingValues(12.dp, 4.dp),
                            modifier = Modifier.heightIn(32.dp, 32.dp).align(Alignment.End)
                        ) { Text(stringResource(R.string.ok_v2)) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { updateSelectedRatingRange(selectedLow, selectedHigh); onDismiss() },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.done_v2)) }
        }
    )
}

@Composable
private fun InStockSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val inStock by filterViewModel.selectedInStock.collectAsState()
    val outOfStock by filterViewModel.selectedOutOfStock.collectAsState()
    val inStockEnabled by filterViewModel.inStockEnabled.collectAsState()
    val outOfStockEnabled by filterViewModel.outOfStockEnabled.collectAsState()

    Column(
        modifier = modifier
            .background(LocalCustomColors.current.sheetBox, RoundedCornerShape(8.dp))
            .border(Dp.Hairline, LocalCustomColors.current.sheetBoxBorder, RoundedCornerShape(8.dp))
            .padding(4.dp, 3.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.Start
    ) {
        CheckboxWithLabel(
            text = stringResource(R.string.in_stock_v2),
            checked = inStock,
            onCheckedChange = filterViewModel::updateSelectedInStock,
            modifier = Modifier,
            enabled = inStockEnabled || inStock,
            allowResize = true
        )
        CheckboxWithLabel(
            text = stringResource(R.string.out_v2),
            checked = outOfStock,
            onCheckedChange = filterViewModel::updateSelectedOutOfStock,
            modifier = Modifier,
            enabled = outOfStockEnabled || outOfStock,
            allowResize = true
        )
    }
}


@Composable
private fun TriStateCheckWithLabel(
    text: @Composable () -> String,
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
            .height(36.dp)
            .clickable(null, null, enabled()) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start)
    ) {
        Box {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 30.dp) {
                TriStateCheckbox(
                    state = state(),
                    onClick = onClick,
                    enabled = enabled(),
                    colors = colors(),
                    interactionSource = interactionSource,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        Text(
            text = text(),
            modifier = Modifier.padding(end = 4.dp),
            color = if (enabled()) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f),
            autoSize = TextAutoSize.StepBased(12.sp, 15.sp),
            maxLines = maxLines(),
        )
    }
}