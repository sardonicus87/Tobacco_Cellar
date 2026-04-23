package com.sardonicus.tobaccocellar.ui.addEditItems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.AutoCompleteText
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckbox
import com.sardonicus.tobaccocellar.ui.composables.CustomDropDown
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.composables.RatingPopup
import com.sardonicus.tobaccocellar.ui.composables.RatingRow
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsEntry(
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    syncedTins: Int,
    isEditEntry: Boolean,
    componentList: ComponentList,
    onComponentChange: (String) -> Unit,
    flavoringList: FlavoringList,
    onFlavoringChange: (String) -> Unit,
    onValueChange: (ItemDetails) -> Unit,
    showRatingPop: Boolean,
    onShowRatingPop: (Boolean) -> Unit,
    fieldInteractionSource: MutableInteractionSource?,
    tooltipVisible: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 0.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Brand //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Brand:",
                modifier = Modifier
                    .width(80.dp)
            )

            AutoCompleteText(
                value = itemDetails.brand,
                allItems = itemUiState.autoBrands,
                onValueChange = { onValueChange(itemDetails.copy(brand = it)) },
                onOptionSelected = { onValueChange(itemDetails.copy(brand = it)) },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    if (isEditEntry) {
                        val text = if (itemDetails.originalBrand.isNotEmpty()) {
                            "(" + itemDetails.originalBrand + ")" } else ""
                        Text(
                            text = text,
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    } else {
                        Text(
                            text = "Required",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
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
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(brand = "")) }
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
        }

        // Blend //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Blend:",
                modifier = Modifier
                    .width(80.dp)
            )
            TextField(
                value = itemDetails.blend,
                onValueChange = { onValueChange(itemDetails.copy(blend = it)) },
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = true,
                singleLine = true,
                placeholder = {
                    if (isEditEntry) {
                        val text = if (itemDetails.originalBlend.isNotEmpty()) {
                            "(" + itemDetails.originalBlend + ")" } else ""
                        Text(
                            text = text,
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    } else {
                        Text(
                            text = "Required",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
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
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(blend = "")) }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
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
        }

        // Type //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Type:",
                modifier = Modifier
                    .width(80.dp)
            )
            CustomDropDown(
                selectedValue = itemDetails.type,
                onValueChange = { onValueChange(itemDetails.copy(type = it)) },
                options = listOf("", "Aromatic", "English", "Burley", "Virginia", "Other"),
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }

        // Subgenre //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subgenre:",
                modifier = Modifier
                    .width(80.dp)
            )

            AutoCompleteText(
                value = itemDetails.subGenre,
                onValueChange = { onValueChange(itemDetails.copy(subGenre = it)) },
                allItems = itemUiState.autoGenres,
                onOptionSelected = { onValueChange(itemDetails.copy(subGenre = it)) },
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.subGenre.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(subGenre = "")) }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                interactionSource = fieldInteractionSource
            )
        }

        // Cut //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cut:",
                modifier = Modifier
                    .width(80.dp)
            )

            AutoCompleteText(
                value = itemDetails.cut,
                onValueChange = { onValueChange(itemDetails.copy(cut = it)) },
                onOptionSelected = { onValueChange(itemDetails.copy(cut = it)) },
                allItems = itemUiState.autoCuts,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.cut.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(cut = "")) }
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
        }

        // Components //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Components: ",
                style = TextStyle(
                    color = LocalContentColor.current
                ),
                modifier = Modifier
                    .width(80.dp)
                    .align(Alignment.CenterVertically),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 16.sp,
                    stepSize = .02.sp,
                ),
                maxLines = 1,
            )

            AutoCompleteText(
                value = componentList.componentString,
                allItems = componentList.autoComps,
                onValueChange = { onComponentChange(it) },
                componentField = true,
                onOptionSelected = { onComponentChange(it) },
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (componentList.componentString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onComponentChange("") }
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
                maxLines = 1,
                placeholder = {
                    Text(
                        text = "(Separate with comma + space)",
                        modifier = Modifier
                            .alpha(0.66f),
                        fontSize = 13.sp,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                },
                interactionSource = fieldInteractionSource
            )
        }

        // Flavoring //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Flavoring: ",
                style = TextStyle(
                    color = LocalContentColor.current
                ),
                modifier = Modifier
                    .width(80.dp)
                    .align(Alignment.CenterVertically),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 16.sp,
                    stepSize = .02.sp,
                ),
                maxLines = 1,
            )

            AutoCompleteText(
                value = flavoringList.flavoringString,
                onValueChange = { onFlavoringChange(it) },
                componentField = true,
                onOptionSelected = { onFlavoringChange(it) },
                allItems = flavoringList.autoFlavors,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (flavoringList.flavoringString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onFlavoringChange("") }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                maxLines = 1,
                placeholder = {
                    Text(
                        text = "(Separate with comma + space)",
                        modifier = Modifier
                            .alpha(0.66f),
                        fontSize = 13.sp,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                },
                interactionSource = fieldInteractionSource
            )
        }

        // No. of Tins //
        Row(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No. of\nTins:",
                style = TextStyle(
                    color = LocalContentColor.current
                ),
                modifier = Modifier
                    .width(80.dp)
                    .heightIn(max = 48.dp)
                    .wrapContentHeight()
                    .align(Alignment.CenterVertically),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 16.sp,
                    stepSize = .02.sp,
                ),
                maxLines = 2,
            )
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val pattern = remember { Regex("^(\\s*|\\d+)$") }

                TextField(
                    value =
                        if (itemDetails.syncTins) syncedTins.toString()
                        else itemDetails.quantityString,
                    onValueChange = {
                        if (!itemDetails.syncTins) {
                            if (it.matches(pattern) && it.length <= 2) {
                                onValueChange(
                                    itemDetails.copy(
                                        quantityString = it,
                                        quantity = it.toIntOrNull() ?: 1
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .width(54.dp)
                        .padding(0.dp),
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
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Increase/Decrease Buttons //
                    IncreaseDecrease(
                        increaseClick = {
                            if (itemDetails.quantityString.isEmpty()) {
                                onValueChange(
                                    itemDetails.copy(
                                        quantityString = "1",
                                        quantity = 1
                                    )
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
                                        itemDetails.copy(
                                            quantityString = "99",
                                            quantity = 99
                                        )
                                    )
                                }
                            }
                        },
                        decreaseClick = {
                            if (itemDetails.quantityString.isEmpty()) {
                                onValueChange(
                                    itemDetails.copy(
                                        quantityString = "0",
                                        quantity = 0
                                    )
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
                                        itemDetails.copy(
                                            quantityString = "0",
                                            quantity = 0
                                        )
                                    )
                                }
                            }
                        },
                        increaseEnabled = !itemDetails.syncTins,
                        decreaseEnabled = !itemDetails.syncTins,
                        modifier = Modifier
                            .fillMaxHeight()
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
                                // 30-second overall timeout, dismiss no matter what
                                withTimeoutOrNull(30000) {
                                    // 5 seconds minimum of visibility (if user hasn't dismissed manually)
                                    delay(5000)

                                    // check now if we're holding to add 1 second after release
                                    val held = hovered || pressed

                                    // wait until hold is released, instant if we let go
                                    // before 5 seconds or opened by clicking tip button
                                    snapshotFlow { hovered || pressed }.first { !it }

                                    // dismiss 1 second after release, will be false if we weren't
                                    // holding/hovering at 5 seconds
                                    if (held) { delay(250) }
                                }
                                tooltipState.dismiss()
                            }
                        }

                        BackHandler(tooltipState.isVisible) {
                            tooltipState.dismiss()
                        }

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
                                ) {
                                    Text("Synchronize the \"No. of Tins\" field with the total " +
                                            "quantities of unfinished tins in the Tins tab.")
                                }
                            },
                            state = tooltipState,
                            modifier = Modifier
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = spacedBy(2.dp, Alignment.CenterHorizontally),
                                modifier = Modifier
                                    .hoverable(interactionSource)
                                    .clickable(interactionSource, null) { /* do nothing, press listener */ }
                            ) {
                                Text(
                                    text = "Sync?",
                                    modifier = Modifier
                                        .offset(x = 0.dp, y = 1.dp),
                                    fontSize = 14.sp,
                                )
                                CustomCheckbox(
                                    checked = itemDetails.syncTins,
                                    onCheckedChange = {
                                        onValueChange(itemDetails.copy(syncTins = it))
                                    },
                                    size = 34.dp,
                                    checkedIcon = R.drawable.check_box_24,
                                    uncheckedIcon = R.drawable.check_box_outline_24,
                                    modifier = Modifier
                                )
                                val coroutineScope = rememberCoroutineScope()
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(40.dp)
                                        .clickable(indication = null, interactionSource = interactionSource) {
                                            coroutineScope.launch {
                                                tooltipState.show()
                                            }
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
        }

        // Rating //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Rating:",
                modifier = Modifier
                    .width(80.dp)
            )
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = { onShowRatingPop(true) }
                    ),
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
                        modifier = Modifier
                            .alpha(0.75f)
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
                    .padding(0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Favorite?",
                    modifier = Modifier
                        .offset(x = 0.dp, y = 1.dp)
                )
                CustomCheckbox(
                    checked = itemDetails.favorite,
                    onCheckedChange = {
                        if (itemDetails.favorite) {
                            onValueChange(itemDetails.copy(favorite = it))
                        } else {
                            onValueChange(
                                itemDetails.copy(
                                    favorite = it,
                                    disliked = false
                                )
                            )
                        }
                    },
                    checkedIcon = R.drawable.heart_filled_24,
                    uncheckedIcon = R.drawable.heart_outline_24,
                    size = 34.dp,
                    modifier = Modifier
                        .padding(0.dp),
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContentColor = LocalCustomColors.current.favHeart,
                    )
                )
            }
            Row(
                modifier = Modifier
                    .padding(0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Disliked?",
                    modifier = Modifier
                        .offset(x = 0.dp, y = 1.dp)
                )
                CustomCheckbox(
                    checked = itemDetails.disliked,
                    onCheckedChange = {
                        if (itemDetails.disliked) {
                            onValueChange(itemDetails.copy(disliked = it))
                        } else {
                            onValueChange(
                                itemDetails.copy(
                                    disliked = it,
                                    favorite = false
                                )
                            )
                        }
                    },
                    checkedIcon = R.drawable.heartbroken_filled_24,
                    uncheckedIcon = R.drawable.heartbroken_outlined_24,
                    size = 34.dp,
                    modifier = Modifier
                        .padding(0.dp),
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
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "In Production",
                modifier = Modifier
                    .offset(x = 0.dp, y = 1.dp)
            )
            CustomCheckbox(
                checked = itemDetails.inProduction,
                onCheckedChange = {
                    onValueChange(itemDetails.copy(inProduction = it))
                },
                checkedIcon = R.drawable.check_box_24,
                size = 34.dp,
                uncheckedIcon = R.drawable.check_box_outline_24,
                modifier = Modifier
            )
        }
    }

    if (showRatingPop) {
        RatingPopup(
            currentRating = itemDetails.rating,
            onDismiss = { onShowRatingPop(false) },
            onRatingSelected = {
                onValueChange(itemDetails.copy(rating = it))
                onShowRatingPop(false)
            }
        )
    }
}