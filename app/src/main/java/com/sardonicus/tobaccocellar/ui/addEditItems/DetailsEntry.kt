package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.columns
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.RichTooltipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition.Companion.Above
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.AutoCompleteData
import com.sardonicus.tobaccocellar.ui.BlendTypes
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.AutoCompleteText
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckbox
import com.sardonicus.tobaccocellar.ui.composables.CustomDropDown
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.composables.RatingPopup
import com.sardonicus.tobaccocellar.ui.composables.RatingRow
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.getLocalizedContext
import com.sardonicus.tobaccocellar.ui.utilities.getLocalizedType
import com.sardonicus.tobaccocellar.ui.utilities.safeString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGridApi::class)
@Composable
fun DetailsEntry(
    itemDetails: ItemDetails,
    autoComplete: AutoCompleteData,
    isEditEntry: Boolean,
    onValueChange: (ItemDetails) -> Unit,
    showRatingPop: Boolean,
    onShowRatingPop: (Boolean) -> Unit,
    fieldInteractionSource: MutableInteractionSource?,
    tooltipVisible: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Spacer(Modifier.height(20.dp))
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Grid(
            modifier = Modifier.fillMaxWidth(),
            config = {
                columns(GridTrackSize.MinMax(85.dp, .02.fr), GridTrackSize.MinMax(0.dp, .98.fr))
                repeat(9) { row(GridTrackSize.Auto) }
                gap(8.dp)
            },
        ) {
            Text(stringResource(R.string.brand_label), Modifier.gridItem(1, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))
            Text(stringResource(R.string.blend_label), Modifier.gridItem(2, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))
            Text(stringResource(R.string.type_label), Modifier.gridItem(3, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))
            Text(stringResource(R.string.subgenre_label), Modifier.gridItem(4, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))
            Text(stringResource(R.string.cut_label), Modifier.gridItem(5, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))
            Text(stringResource(R.string.component_label), Modifier.gridItem(6, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))
            Text(stringResource(R.string.flavoring_label), Modifier.gridItem(7, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))
            Text(stringResource(R.string.no_of_tins_label), Modifier.gridItem(8, 1, 1, 1, Alignment.CenterStart), maxLines = 2, autoSize = TextAutoSize.StepBased(12.sp, 16.sp),
                style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph))
            Text(stringResource(R.string.rating_label), Modifier.gridItem(9, 1, 1, 1, Alignment.CenterStart), maxLines = 1, autoSize = TextAutoSize.StepBased(12.sp, 16.sp))

            AutoCompleteText(
                value = itemDetails.brand,
                allItems = autoComplete.brands,
                onValueChange = { onValueChange(itemDetails.copy(brand = it)) },
                onOptionSelected = { onValueChange(itemDetails.copy(brand = it)) },
                modifier = Modifier
                    .gridItem(1, 2, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth(),
                placeholder = {
                    if (isEditEntry) {
                        val text = if (itemDetails.originalItem.brand.isNotEmpty()) {
                            "(${itemDetails.originalItem.brand})" } else ""
                        Text(
                            text = text,
                            modifier = Modifier.alpha(0.66f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.required),
                            modifier = Modifier.alpha(0.66f),
                            fontSize = 14.sp,
                            lineHeight = 1.em,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    if (itemDetails.brand.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(null, LocalIndication.current) {
                                    onValueChange(itemDetails.copy(brand = ""))
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                interactionSource = fieldInteractionSource
            )

            TextField(
                value = itemDetails.blend,
                onValueChange = { onValueChange(itemDetails.copy(blend = it)) },
                modifier = Modifier
                    .gridItem(2, 2, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth(),
                enabled = true,
                singleLine = true,
                placeholder = {
                    if (isEditEntry) {
                        val text = if (itemDetails.originalItem.blend.isNotEmpty()) {
                            "(${itemDetails.originalItem.blend})" } else ""
                        Text(
                            text = text,
                            modifier = Modifier.alpha(0.66f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.required),
                            modifier = Modifier.alpha(0.66f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    if (itemDetails.blend.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(null, LocalIndication.current) {
                                    onValueChange(itemDetails.copy(blend = ""))
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = LocalCustomColors.current.textField,
                    unfocusedContainerColor = LocalCustomColors.current.textField,
                    disabledContainerColor = LocalCustomColors.current.textField,
                ),
                shape = MaterialTheme.shapes.extraSmall,
                interactionSource = fieldInteractionSource
            )

            val context = getLocalizedContext(LocalCellarApplication.current)
            val typeMap = remember { BlendTypes.entries.filterNot { it == BlendTypes.UNASSIGNED }.associate { context.safeString(it.resId) to it.name } }

            CustomDropDown(
                selectedValue = getLocalizedType(itemDetails.type, false),
                onValueChange = { onValueChange(itemDetails.copy(type = typeMap[it] ?: "")) },
                options = listOf("") + typeMap.keys.toList(),
                modifier = Modifier
                    .gridItem(3, 2, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth()
            )

            AutoCompleteText(
                value = itemDetails.subGenre,
                onValueChange = { onValueChange(itemDetails.copy(subGenre = it)) },
                allItems = autoComplete.subgenres,
                onOptionSelected = { onValueChange(itemDetails.copy(subGenre = it)) },
                modifier = Modifier
                    .gridItem(4, 2, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.subGenre.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(null, LocalIndication.current) {
                                    onValueChange(itemDetails.copy(subGenre = ""))
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                interactionSource = fieldInteractionSource
            )

            AutoCompleteText(
                value = itemDetails.cut,
                onValueChange = { onValueChange(itemDetails.copy(cut = it)) },
                onOptionSelected = { onValueChange(itemDetails.copy(cut = it)) },
                allItems = autoComplete.cuts,
                modifier = Modifier
                    .gridItem(5, 2, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.cut.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(null, LocalIndication.current) {
                                    onValueChange(itemDetails.copy(cut = ""))
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                interactionSource = fieldInteractionSource
            )

            AutoCompleteText(
                value = itemDetails.componentString,
                allItems = autoComplete.components,
                onValueChange = { onValueChange(itemDetails.copy(componentString = it)) },
                componentField = true,
                onOptionSelected = { onValueChange(itemDetails.copy(componentString = it)) },
                modifier = Modifier
                    .gridItem(6, 2, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.componentString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(null, LocalIndication.current) {
                                    onValueChange(itemDetails.copy(componentString = ""))
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                maxLines = 1,
                placeholder = {
                    Text(
                        text = stringResource(R.string.separate_comma),
                        autoSize = TextAutoSize.StepBased(11.sp, 13.sp, .1.sp),
                        lineHeight = 1.em,
                        maxLines = 1,
                        modifier = Modifier
                            .alpha(0.66f)
                            .basicMarquee(
                                repeatDelayMillis = 1500,
                                spacing = MarqueeSpacing.fractionOfContainer(.5f)
                            )
                    )
                },
                interactionSource = fieldInteractionSource
            )

            AutoCompleteText(
                value = itemDetails.flavoringString,
                onValueChange = { onValueChange(itemDetails.copy(flavoringString = it)) },
                componentField = true,
                onOptionSelected = { onValueChange(itemDetails.copy(flavoringString = it)) },
                allItems = autoComplete.flavorings,
                modifier = Modifier
                    .gridItem(7, 2, 1, 1, Alignment.CenterStart)
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.flavoringString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(null, LocalIndication.current) {
                                    onValueChange(itemDetails.copy(flavoringString = ""))
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = if (itemDetails.syncTins) ImeAction.Done else ImeAction.Next,
                ),
                maxLines = 1,
                placeholder = {
                    Text(
                        text = stringResource(R.string.separate_comma),
                        autoSize = TextAutoSize.StepBased(11.sp, 13.sp, .1.sp),
                        lineHeight = 1.em,
                        maxLines = 1,
                        modifier = Modifier
                            .alpha(0.66f)
                            .basicMarquee(
                                repeatDelayMillis = 1500,
                                spacing = MarqueeSpacing.fractionOfContainer(.5f)
                            )
                    )
                },
                interactionSource = fieldInteractionSource
            )

            Row(
                modifier = Modifier
                    .gridItem(8, 2, 1, 1, Alignment.CenterStart)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val pattern = remember { Regex("^(\\s*|\\d+)$") }

                TextField(
                    value = itemDetails.quantityString,
                    onValueChange = {
                        if (!itemDetails.syncTins) {
                            if (it.matches(pattern) && it.length <= 2) {
                                onValueChange(
                                    itemDetails.copy(quantityString = it, quantity = it.toIntOrNull() ?: 1)
                                )
                            }
                        }
                    },
                    modifier = Modifier.width(54.dp),
                    visualTransformation = VisualTransformation.None,
                    enabled = !itemDetails.syncTins,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = LocalCustomColors.current.textField,
                        unfocusedContainerColor = LocalCustomColors.current.textField,
                        disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.66f),
                        disabledTextColor = LocalContentColor.current.copy(alpha = 0.66f),
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                )

                // Tin field options //
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Increase/Decrease Buttons //
                    IncreaseDecrease(
                        increaseClick = {
                            if (itemDetails.quantityString.isEmpty()) {
                                onValueChange(
                                    itemDetails.copy(quantityString = "1", quantity = 1)
                                )
                            } else {
                                if (itemDetails.quantityString.toInt() < 99) {
                                    onValueChange(
                                        itemDetails.copy(
                                            quantityString = (itemDetails.quantityString.toInt() + 1).toString(),
                                            quantity = itemDetails.quantityString.toInt() + 1
                                        )
                                    )
                                } else {
                                    onValueChange(
                                        itemDetails.copy(quantityString = "99", quantity = 99)
                                    )
                                }
                            }
                        },
                        decreaseClick = {
                            if (itemDetails.quantityString.isEmpty()) {
                                onValueChange(
                                    itemDetails.copy(quantityString = "0", quantity = 0)
                                )
                            } else {
                                if (itemDetails.quantityString.toInt() > 0) {
                                    onValueChange(
                                        itemDetails.copy(
                                            quantityString = (itemDetails.quantityString.toInt() - 1).toString(),
                                            quantity = itemDetails.quantity - 1
                                        )
                                    )
                                } else if (itemDetails.quantityString.toInt() == 0) {
                                    onValueChange(
                                        itemDetails.copy(quantityString = "0", quantity = 0)
                                    )
                                }
                            }
                        },
                        increaseEnabled = !itemDetails.syncTins,
                        decreaseEnabled = !itemDetails.syncTins,
                        modifier = Modifier.fillMaxHeight()
                    )

                    // Sync Tins? //
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(2.dp, Alignment.CenterHorizontally)
                    ) {
                        val tooltipState = rememberTooltipState(isPersistent = true)
                        val interactionSource = remember { MutableInteractionSource() }
                        val hovered by interactionSource.collectIsHoveredAsState()
                        val pressed by interactionSource.collectIsPressedAsState()
                        val width = LocalWindowInfo.current.containerDpSize.width

                        LaunchedEffect(tooltipState.isVisible) {
                            tooltipVisible(tooltipState.isVisible)

                            if (tooltipState.isVisible) {
                                withTimeoutOrNull(30000.milliseconds) { // 30-second overall timeout, dismiss no matter what
                                    delay(5000.milliseconds) // 5 seconds minimum visibility
                                    val held = hovered || pressed // check at 5 sec if we're holding
                                    snapshotFlow { hovered || pressed }.first { !it } // wait for release
                                    if (held) { delay(250.milliseconds) } // add 1/4 second after release if was held
                                }
                                tooltipState.dismiss()
                            }
                        }

                        BackHandler(tooltipState.isVisible) { tooltipState.dismiss() }

                        TooltipBox(
                            positionProvider = rememberTooltipPositionProvider(Above, 3.dp),
                            tooltip = {
                                RichTooltip(
                                    maxWidth = width * .55f,
                                    colors = RichTooltipColors(
                                        containerColor = LocalCustomColors.current.darkNeutral,
                                        contentColor = LocalContentColor.current,
                                        titleContentColor = Color.Transparent,
                                        actionContentColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .border(Dp.Hairline, MaterialTheme.colorScheme.outlineVariant, TooltipDefaults.richTooltipContainerShape)
                                ) { Text(stringResource(R.string.sync_tooltip)) }
                            },
                            state = tooltipState
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = spacedBy(2.dp, Alignment.CenterHorizontally),
                                modifier = Modifier
                                    .hoverable(interactionSource)
                                    .clickable(
                                        interactionSource,
                                        null
                                    ) { onValueChange(itemDetails.copy(syncTins = !itemDetails.syncTins)) }
                            ) {
                                Text(
                                    text = stringResource(R.string.sync_q),
                                    modifier = Modifier
                                        .offset(x = 0.dp, y = 1.dp)
                                        .widthIn(max = 40.dp),
                                    lineHeight = 1.em,
                                    autoSize = TextAutoSize.StepBased(11.sp, 14.sp),
                                    maxLines = 1
                                )
                                CustomCheckbox(
                                    checked = itemDetails.syncTins,
                                    onCheckedChange = { onValueChange(itemDetails.copy(syncTins = it)) },
                                    size = 34.dp,
                                    checkedIcon = R.drawable.check_box_24,
                                    uncheckedIcon = R.drawable.check_box_outline_24
                                )
                                val coroutineScope = rememberCoroutineScope()
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(40.dp)
                                        .clickable(interactionSource, null) {
                                            coroutineScope.launch { tooltipState.show() }
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.help_outline),
                                        contentDescription = "Help",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = .75f).compositeOver(MaterialTheme.colorScheme.onBackground)),
                                        modifier = Modifier
                                            .size(15.dp)
                                            .offset((-3).dp, (-9).dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .gridItem(9, 2, 1, 1, Alignment.CenterStart)
                    .heightIn(min = 42.dp)
                    .clickable(null, null) { onShowRatingPop(true) },
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RatingRow(
                    rating = itemDetails.rating,
                    showEmpty = true,
                    starSize = 24.dp,
                    emptyColor = LocalContentColor.current,
                )
                if (itemDetails.rating != null) {
                    Text(
                        text = "(${formatDecimal(itemDetails.rating)})",
                        fontSize = 13.sp,
                        modifier = Modifier.alpha(0.75f)
                    )
                }
            }
        }

        // Favorite or Disliked? //
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable(null, null) { onValueChange(itemDetails.copy(favorite = !itemDetails.favorite)) },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.favorite),
                    modifier = Modifier.offset(x = 0.dp, y = 1.dp)
                )
                CustomCheckbox(
                    checked = itemDetails.favorite,
                    onCheckedChange = {
                        if (itemDetails.favorite) { onValueChange(itemDetails.copy(favorite = it)) }
                        else { onValueChange(itemDetails.copy(favorite = it, disliked = false)) }
                    },
                    checkedIcon = R.drawable.heart_filled_24,
                    uncheckedIcon = R.drawable.heart_outline_24,
                    size = 34.dp,
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContentColor = LocalCustomColors.current.favHeart,
                    )
                )
            }
            Row(
                modifier = Modifier
                    .clickable(null, null) { onValueChange(itemDetails.copy(disliked = !itemDetails.disliked)) },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.disliked),
                    modifier = Modifier.offset(x = 0.dp, y = 1.dp)
                )
                CustomCheckbox(
                    checked = itemDetails.disliked,
                    onCheckedChange = {
                        if (itemDetails.disliked) { onValueChange(itemDetails.copy(disliked = it)) }
                        else { onValueChange(itemDetails.copy(disliked = it, favorite = false)) }
                    },
                    checkedIcon = R.drawable.heartbroken_filled_24,
                    uncheckedIcon = R.drawable.heartbroken_outlined_24,
                    size = 34.dp,
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContentColor = LocalCustomColors.current.disHeart,
                    )
                )
            }
        }

        // Production Status //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable(null, null) { onValueChange(itemDetails.copy(inProduction = !itemDetails.inProduction)) },
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.in_production),
                modifier = Modifier.offset(x = 0.dp, y = 1.dp)
            )
            CustomCheckbox(
                checked = itemDetails.inProduction,
                onCheckedChange = { onValueChange(itemDetails.copy(inProduction = it)) },
                checkedIcon = R.drawable.check_box_24,
                size = 34.dp,
                uncheckedIcon = R.drawable.check_box_outline_24
            )
        }
    }

    if (showRatingPop) {
        RatingPopup(
            currentRating = itemDetails.rating,
            onDismiss = { onShowRatingPop(false) },
            onRatingSelected = { onValueChange(itemDetails.copy(rating = it)); onShowRatingPop(false) }
        )
    }
}