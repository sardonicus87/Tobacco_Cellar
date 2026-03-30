package com.sardonicus.tobaccocellar.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckbox
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.items.RatingPopup
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay

@Composable
fun ItemMenu(
    viewModel: HomeViewModel,
    activeItemId: () -> Int,
    onEditClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickEditState by viewModel.quickEditState.collectAsState()
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
                    modifier = Modifier,
                     //   .padding(end = 8.dp),
                ) {
                    Text(
                        text = "Edit Item",
                        modifier = Modifier,
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                TextButton(
                    onClick = onQuickEdit,
                    modifier = Modifier,
                ) {
                    Text(
                        text = "Quick Edit",
                        modifier = Modifier,
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.SemiBold,
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
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                    )
                }

                val rating = remember(quickEditState.rating) { formatDecimal(quickEditState.rating) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier
                        .fillMaxHeight()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 3.dp)
                ) {
                    // Rating
                    QuickOption(changed.rating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp)
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
                                painter = painterResource(id = R.drawable.star_filled),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(LocalCustomColors.current.starRating),
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
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
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
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
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
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { onShowNotePop(true) }
                                .padding(vertical = 1.dp, horizontal = 8.dp)
                        ) {
                            val icon =
                                if (quickEditState.notes.isNotBlank()) R.drawable.notes_24 else R.drawable.notes_outline_24
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(if (quickEditState.notes.isNotBlank()) MaterialTheme.colorScheme.tertiary else LocalContentColor.current),
                                alignment = Alignment.Center,
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier
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
                                    .padding(vertical = 8.dp)
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

                TextButton(
                    onClick = viewModel::saveQuickEdits,
                    enabled = quickEditState.saveEnabled,
                    modifier = Modifier,
                ) {
                    Text(
                        text = "Save",
                        modifier = Modifier,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (showRatingPop) {
            RatingPopup(
                currentRating = quickEditState.rating,
                onDismiss = { onShowRatingPop(false) },
                onRatingSelected = {
                    viewModel.updateQuickRating(it)
                    onShowRatingPop(false)
                }
            )
        }

        if (showNotePop) {
            EditNotePop(
                currentNote = quickEditState.notes,
                onDismiss = { onShowNotePop(false) },
                onNoteEdited = {
                    viewModel.updateQuickNotes(it)
                    onShowNotePop(false)
                }
            )
        }

        if (showQtyPop) {
            EditQuantityPop(
                currentQty = quickEditState.quantity,
                onDismiss = { onShowQtyPop(false) },
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
                .offset((-1).dp, (-7).dp)
                .clip(CircleShape)
                //.border(1.dp, borderColor, CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        content()
    }
}

@Composable
private fun EditNotePop(
    currentNote: String,
    onDismiss: () -> Unit,
    onNoteEdited: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldState by rememberSaveable { mutableStateOf(currentNote) }
    val updateTextField: (String) -> Unit = { textFieldState = it }

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
private fun EditQuantityPop(
    currentQty: Int,
    onDismiss: () -> Unit,
    onQtyEdited: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldState by rememberSaveable { mutableStateOf(currentQty.toString()) }
    val updateTextField: (String) -> Unit = { textFieldState = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .wrapContentHeight(),
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
                    increaseEnabled = true,
                    decreaseEnabled = true,
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