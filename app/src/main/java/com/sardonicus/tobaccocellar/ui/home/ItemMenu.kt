package com.sardonicus.tobaccocellar.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckbox
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

@Composable
fun ItemMenu(
    viewModel: HomeViewModel,
    activeItemId: () -> Int,
    onEditClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickEditState by viewModel.quickEditState.collectAsState()
    val originalState by viewModel.originalState.collectAsState()
    val changed by viewModel.quickChanges.collectAsState()

    var quickEdit by rememberSaveable { mutableStateOf(false) }
    val onQuickEdit: () -> Unit = { quickEdit = !quickEdit }

    var showRatingPop by rememberSaveable { mutableStateOf(false) }
    val onShowRatingPop: (Boolean) -> Unit = { showRatingPop = it }

    var showNotePop by rememberSaveable { mutableStateOf(false) }
    val onShowNotePop: (Boolean) -> Unit = { showNotePop = it }

    var showQtyPop by rememberSaveable { mutableStateOf(false) }
    val onShowQtyPop: (Boolean) -> Unit = { showQtyPop = it }

    LaunchedEffect(Unit) {
        delay(150)
        viewModel.setQuickEditItem(activeItemId())
    }

    BackHandler(quickEdit) {
        if (quickEdit) {
            onQuickEdit()
        }
    }


    AnimatedContent(
        targetState = quickEdit,
        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
        modifier = modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.listMenuScrim)
    ) { quickMenu ->
        Row(
            modifier = Modifier
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!quickMenu) {
                TextButton(
                    onClick = {
                        onEditClick()
                        onMenuDismiss()
                    },
                    modifier = Modifier
                ) {
                    Text(
                        text = "Edit Item",
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(25))
                            .padding(4.dp, 2.dp),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.1.em,
                        fontSize = 14.sp
                    )
                }

                TextButton(
                    onClick = onQuickEdit,
                    modifier = Modifier
                ) {
                    Text(
                        text = "Quick Edit",
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(25))
                            .padding(4.dp, 2.dp),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.1.em,
                        fontSize = 14.sp
                    )
                }
            } else {
                // go back
                Box (
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onQuickEdit() }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = .25f).compositeOver(MaterialTheme.colorScheme.onBackground)),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier
                        .fillMaxHeight()
                        .horizontalScroll(rememberScrollState())
                        .padding(3.dp, 8.dp)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(50))
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.primary.copy(alpha = 0.25f).compositeOver(MaterialTheme.colorScheme.onBackground)
                    ) {
                        // Rating
                        QuickOption(changed.rating) {
                            val rating = remember(quickEditState.rating) { formatDecimal(quickEditState.rating) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onShowRatingPop(true) }
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = rating.ifBlank { "-.-" },
                                    autoSize = TextAutoSize.StepBased(10.sp, 15.sp, 0.1.sp),
                                    modifier = Modifier,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 1.em
                                )

                                Image(
                                    painter = painterResource(R.drawable.star_filled),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(
                                        if (rating.isNotBlank()) LocalCustomColors.current.starRating
                                        else LocalContentColor.current
                                    ),
                                    alignment = Alignment.Center,
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier,
                                )
                            }
                        }

                        // Fav/Dis
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            QuickOption(changed.favorite) {
                                CustomCheckbox(
                                    checked = quickEditState.favorite,
                                    onCheckedChange = viewModel::updateQuickFavorite,
                                    checkedIcon = R.drawable.heart_filled_24,
                                    uncheckedIcon = R.drawable.heart_outline_24,
                                    modifier = Modifier,
                                    colors = IconButtonDefaults.iconToggleButtonColors(
                                        checkedContentColor = LocalCustomColors.current.favHeart,
                                    )
                                )
                            }

                            QuickOption(changed.disliked) {
                                CustomCheckbox(
                                    checked = quickEditState.disliked,
                                    onCheckedChange = viewModel::updateQuickDislike,
                                    checkedIcon = R.drawable.heartbroken_filled_24,
                                    uncheckedIcon = R.drawable.heartbroken_outlined_24,
                                    modifier = Modifier,
                                    colors = IconButtonDefaults.iconToggleButtonColors(
                                        checkedContentColor = LocalCustomColors.current.disHeart,
                                    )
                                )
                            }
                        }

                        // Notes
                        QuickOption(changed.notes) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onShowNotePop(true) }
                                    .padding(horizontal = 8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = if (quickEditState.notes.isNotBlank()) R.drawable.notes_24 else R.drawable.notes_outline_24),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(if (quickEditState.notes.isNotBlank()) MaterialTheme.colorScheme.tertiary else LocalContentColor.current),
                                    alignment = Alignment.Center,
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier
                                        .padding(vertical = 1.dp)
                                )
                            }
                        }

                        // Quantity
                        if (!quickEditState.syncTins) {
                            QuickOption(changed.quantity) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onShowQtyPop(true) }
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "Qty: ${quickEditState.quantity}",
                                        autoSize = TextAutoSize.StepBased(10.sp, 15.sp, 0.1.sp),
                                        modifier = Modifier,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 1.em
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = viewModel::saveQuickEdits,
                    enabled = quickEditState.saveEnabled,
                    modifier = Modifier,
                ) {
                    Text(
                        text = "Save",
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(25))
                            .padding(4.dp, 2.dp),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.1.em,
                    )
                }
            }
        }

        var editRatingState by rememberSaveable { mutableStateOf(formatDecimal(quickEditState.rating)) }

        if (showRatingPop) {
            EditRatingPop(
                textFieldState = editRatingState,
                updateTextField = { editRatingState = it },
                onDismiss = { onShowRatingPop(false) },
                onCancel = {
                    onShowRatingPop(false)
                    editRatingState = formatDecimal(originalState.rating)
                    viewModel.updateQuickRating(originalState.rating)
                },
                onRatingEdited = {
                    viewModel.updateQuickRating(it)
                    onShowRatingPop(false)
                }
            )

        }

        var editNoteState by rememberSaveable { mutableStateOf(quickEditState.notes) }

        if (showNotePop) {
            EditNotePop(
                textFieldState = editNoteState,
                updateTextField = { editNoteState = it },
                onDismiss = { onShowNotePop(false) },
                onCancel = {
                    onShowNotePop(false)
                    editNoteState = originalState.notes
                    viewModel.updateQuickNotes(originalState.notes)
                },
                onNoteEdited = {
                    viewModel.updateQuickNotes(it)
                    onShowNotePop(false)
                }
            )
        }

        var qtyState by rememberSaveable { mutableStateOf(quickEditState.quantity.toString()) }

        if (showQtyPop) {
            EditQuantityPop(
                textFieldState = qtyState,
                updateTextField = { qtyState = it },
                onDismiss = { onShowQtyPop(false) },
                onCancel = {
                    onShowQtyPop(false)
                    qtyState = originalState.quantity.toString()
                    viewModel.updateQuickQuantity(originalState.quantity)
                },
                onQtyEdited = {
                    viewModel.updateQuickQuantity(it)
                    onShowQtyPop(false)
                }
            )
        }
    }
}

@Composable
private fun QuickOption(
    edited: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        if (edited) Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(6.dp)
                .offset((-1).dp, (-8).dp)
                .clip(CircleShape)
                //.border(1.dp, borderColor, CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        content()
    }
}

@Composable
private fun EditRatingPop(
    textFieldState: String,
    updateTextField: (String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onRatingEdited: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var parsedDouble by rememberSaveable { mutableStateOf<Double?>(null) }
    val updateParsedDouble: (Double?) -> Unit = { parsedDouble = it }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
    val decimalSeparator = symbols.decimalSeparator.toString()
    val allowedPattern = remember(decimalSeparator) {
        val ds = Regex.escape(decimalSeparator)
        Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
    }

    fun parseDouble(it: String): Double? {
        var parsed: Double?
        try {
            if (it.isNotBlank()) {
                val preNumber = if (it.startsWith(decimalSeparator)) {
                    "0$it"
                } else it
                val number = numberFormat.parse(preNumber)
                parsed = number?.toDouble() ?: 0.0
                if (parsed > 5.0) {
                    parsed = 5.0
                }
            } else {
                parsed = null
            }

        } catch (_: ParseException) {
            return null
        }
        return parsed
    }

    LaunchedEffect(Unit) {
        updateParsedDouble(parseDouble(textFieldState))
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
            Column {
                Text(
                    text = "Set a rating (maximum 5). To make an item unrated, make the field " +
                            "blank. Supports fractional ratings (up to 2 decimal places).",
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(6.dp))
                    TextField(
                        value = textFieldState,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                updateTextField(it)
                                updateParsedDouble(parseDouble(it))

//                                try {
//                                    if (it.isNotBlank()) {
//                                        val preNumber = if (it.startsWith(decimalSeparator)) {
//                                            "0$it"
//                                        } else it
//                                        val number = numberFormat.parse(preNumber)
//                                        parsedDouble = number?.toDouble() ?: 0.0
//                                        if (parsedDouble!! > 5.0) {
//                                            parsedDouble = 5.0
//                                        }
//                                    } else {
//                                        parsedDouble = null
//                                    }
//
//                                } catch (e: ParseException) {
//                                    Log.e("Rating", "Input: $it", e)
//                                }
                            }
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .padding(end = 8.dp),
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
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
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    val alpha = if (textFieldState.isNotBlank()) .75f else 0.38f
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = textFieldState.isNotBlank()
                            ) {
                                updateTextField("")
                                updateParsedDouble(null)
                            }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(alpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRatingEdited(parsedDouble) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Undo")
            }
        }
    )
}

@Composable
private fun EditNotePop(
    textFieldState: String,
    updateTextField: (String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onNoteEdited: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Notes",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier
            )
        },
        text = {
            TextField(
                value = textFieldState,
                onValueChange = {
                    var updatedText = it
                    if (it.contains("\n")) {
                        val lines = it.lines()
                        if (lines.size > 1) {
                            val lastLine = lines[lines.size - 2]
                            val currentLine = lines.last()
                            val lastWord = lastLine.substringAfterLast(" ")
                            if (currentLine.startsWith(lastWord) && currentLine.length > 1) {
                                updatedText = if (currentLine.length == lastWord.length + 1) {
                                    it.dropLast(lastWord.length + 1)
                                } else {
                                    it.dropLast(lastWord.length)
                                }
                            }
                        }
                    }
                    updateTextField(updatedText)
                },
                modifier = Modifier
                    .fillMaxWidth(),
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
                singleLine = false,
                maxLines = 8,
                minLines = 8,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onNoteEdited(textFieldState) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Undo")
            }
        }
    )
}

@Composable
private fun EditQuantityPop(
    textFieldState: String,
    updateTextField: (String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onQtyEdited: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .wrapContentHeight()
            .width(280.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        ),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = "No. of Tins",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier
            )
        },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
            ) {
                Spacer(Modifier.width(34.dp))

                val pattern = remember { Regex("^(\\s*|\\d+)$") }
                TextField(
                    value = textFieldState,
                    onValueChange = {
                        if (it.matches(pattern) && it.length <= 2) {
                            updateTextField(it)
                        }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    modifier = Modifier
                        .width(54.dp),
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
                        disabledContainerColor = LocalCustomColors.current.textField,
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                )

                IncreaseDecrease(
                    increaseClick = {
                        if (textFieldState.isEmpty()) {
                            updateTextField("1")
                        } else {
                            if (textFieldState.toInt() < 99) {
                                val intQty = textFieldState.toInt()
                                val updatedQty = intQty + 1
                                updateTextField(updatedQty.toString())
                            } else {
                                updateTextField("99")
                            }
                        }
                    },
                    decreaseClick = {
                        if (textFieldState.isEmpty()) {
                            updateTextField("0")
                        } else {
                            if (textFieldState.toInt() > 0) {
                                val intQty = textFieldState.toInt()
                                val updatedQty = intQty - 1
                                updateTextField(updatedQty.toString())
                            } else if (textFieldState.toInt() == 0) {
                                updateTextField("0")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onQtyEdited(textFieldState.toIntOrNull() ?: 1) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Undo")
            }
        }
    )
}