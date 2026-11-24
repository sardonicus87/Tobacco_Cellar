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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.text.TextAutoSize.Companion.StepBased
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckbox
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.exclusiveScroll
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState
import com.sardonicus.tobaccocellar.ui.utilities.localizedToDouble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormatSymbols
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ItemMenu(
    viewModel: HomeViewModel,
    activeItemId: Int,
    onEditClick: () -> Unit,
    onMenuDismiss: () -> Unit
) {
    val quickEditState by viewModel.quickEditState.collectAsState()
    val originalState by viewModel.originalState.collectAsState()
    val changed by viewModel.quickChanges.collectAsState()

    var quickEdit by remember { mutableStateOf(false) }

    var showRatingPop by remember { mutableStateOf(false) }
    var showNotePop by remember { mutableStateOf(false) }
    var showQtyPop by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150.milliseconds)
        viewModel.setQuickEditItem(activeItemId)
    }

    BackHandler(quickEdit) { if (quickEdit) { quickEdit = false } }

    val density = LocalDensity.current
    var rowWidth by remember { mutableStateOf(0.dp) }

    AnimatedContent(
        targetState = quickEdit,
        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.listMenuScrim)
    ) { quickMenu ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .onGloballyPositioned { rowWidth = with(density) { it.size.width.toDp() } }
                .pointerInput(Unit) { detectDragGestures(onDrag = { change, _ -> change.consume() }) },
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!quickMenu) {
                TextButton({ onEditClick(); onMenuDismiss() }) {
                    Text(
                        text = stringResource(R.string.edit_item_v2),
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = .8f),
                                RoundedCornerShape(25)
                            )
                            .padding(4.dp, 2.dp),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.1.em,
                        maxLines = 1,
                        autoSize = StepBased(12.sp, 15.sp),
                        fontSize = 14.sp
                    )
                }

                TextButton({ quickEdit = !quickEdit }) {
                    Text(
                        text = stringResource(R.string.quick_edit_v2),
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = .8f),
                                RoundedCornerShape(25)
                            )
                            .padding(4.dp, 2.dp),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.1.em,
                        maxLines = 1,
                        autoSize = StepBased(12.sp, 15.sp),
                        fontSize = 14.sp
                    )
                }
            } else {
                // go back
                Box (
                    modifier = Modifier
                        .padding(3.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { quickEdit = !quickEdit }
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = .25f).compositeOver(MaterialTheme.colorScheme.onBackground)),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Fit
                    )
                }

                val scrollState = rememberScrollState()
                val startShine = scrollState.canScrollBackward
                val endShine = scrollState.canScrollForward
                val primaryColor = MaterialTheme.colorScheme.secondaryContainer
                val edgeFloat = (32.dp / rowWidth).coerceIn(0.01f, 0.49f)

                val shineBrush = remember(startShine, endShine) {
                    Brush.horizontalGradient(
                        0.0f to (if (startShine) primaryColor else Color.Transparent),
                        edgeFloat to Color.Transparent,
                        (1f - edgeFloat) to Color.Transparent,
                        1.0f to (if (endShine) primaryColor else Color.Transparent)
                    )
                }

                GlowBox(
                    GlowColor(MaterialTheme.colorScheme.background.copy(alpha = .75f)), GlowSize(horizontal = 16.dp),
                    modifier = Modifier
                        .heightIn(max = 40.dp)
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, shineBrush, RoundedCornerShape(50))
                ) { Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .exclusiveScroll()
                        .horizontalScroll(scrollState, null)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(50))
                        .padding(3.dp, 8.dp)
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
                                    .clickable { showRatingPop = true }
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = rating.ifBlank { "--" },
                                    autoSize = StepBased(10.sp, 14.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 1.em,
                                    maxLines = 1
                                )

                                Image(
                                    painter = painterResource(R.drawable.star_filled),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(
                                        if (rating.isNotBlank()) LocalCustomColors.current.starRating
                                        else LocalContentColor.current
                                    ),
                                    alignment = Alignment.Center,
                                    contentScale = ContentScale.FillHeight
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
                                    .clickable { showNotePop = true }
                                    .padding(horizontal = 8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = if (quickEditState.notes.isNotBlank()) R.drawable.notes_24 else R.drawable.notes_outline_24),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(if (quickEditState.notes.isNotBlank()) MaterialTheme.colorScheme.tertiary else LocalContentColor.current),
                                    alignment = Alignment.Center,
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier.padding(vertical = 1.dp)
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
                                        .clickable { showQtyPop = true }
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "Qty: ${quickEditState.quantity}",
                                        autoSize = StepBased(10.sp, 14.sp),
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 1.em,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } }

                TextButton(
                    onClick = viewModel::saveQuickEdits,
                    enabled = quickEditState.saveEnabled,
                    modifier = Modifier.heightIn(min = 1.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save_v2),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.1.em,
                        maxLines = 1,
                        autoSize = StepBased(12.sp, 14.sp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        val editRatingState = rememberTextFieldState(formatDecimal(quickEditState.rating))

        if (showRatingPop) {
            EditRatingPop(
                textFieldState = editRatingState,
                undoEnabled = editRatingState.text.toString() != formatDecimal(originalState.rating),
                updateTextField = { editRatingState.edit { replace(0, length, it) } },
                onDismiss = { showRatingPop = false },
                onCancel = {
                    editRatingState.edit { replace(0, length, formatDecimal(originalState.rating)) }
                    viewModel.updateQuickRating(originalState.rating)
                },
                onRatingEdited = { viewModel.updateQuickRating(it); showRatingPop = false }
            )

        }

        val editNoteState = rememberTextFieldState(quickEditState.notes)

        if (showNotePop) {
            EditNotePop(
                textFieldState = editNoteState,
                undoEnabled = editNoteState.text.toString() != originalState.notes,
                updateTextField = { editNoteState.edit { replace(0, length, it) } },
                onDismiss = { showNotePop = false },
                onCancel = {
                    editNoteState.edit { replace(0, length, originalState.notes) }
                    viewModel.updateQuickNotes(originalState.notes)
                },
                onNoteEdited = { viewModel.updateQuickNotes(it); showNotePop = false }
            )
        }

        val qtyState = rememberTextFieldState(quickEditState.quantity.toString())

        if (showQtyPop) {
            EditQuantityPop(
                textFieldState = qtyState,
                undoEnabled = qtyState.text.toString() != originalState.quantity.toString(),
                updateTextField = { qtyState.edit { replace(0, length, it) } },
                onDismiss = { showQtyPop = false },
                onCancel = {
                    qtyState.edit { replace(0, length, originalState.quantity.toString()) }
                    viewModel.updateQuickQuantity(originalState.quantity)
                },
                onQtyEdited = { viewModel.updateQuickQuantity(it); showQtyPop = false }
            )
        }
    }
}

@Composable
private fun QuickOption(
    edited: Boolean,
    content: @Composable () -> Unit
) {
    Box (contentAlignment = Alignment.Center) {
        if (edited) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(6.dp)
                    .offset((-1).dp, (-8).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
        content()
    }
}

@Composable
private fun EditRatingPop(
    textFieldState: TextFieldState,
    undoEnabled: Boolean,
    updateTextField: (String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onRatingEdited: (Double?) -> Unit
) {
    var parsedDouble by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(textFieldState, Dispatchers.Default) {
        snapshotFlow { textFieldState.text.toString() }.collectLatest {
            updateTextField(textFieldState.text.toString())
            parsedDouble = textFieldState.text.toString().localizedToDouble(max = 5.0)
        }
    }

    val symbols = remember { DecimalFormatSymbols.getInstance(LanguageState.locale) }
    val decimalSeparator = symbols.decimalSeparator.toString()
    val pattern = remember(decimalSeparator) {
        val ds = Regex.escape(decimalSeparator)
        Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.wrapContentHeight(),
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = stringResource(R.string.rating_v2),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.rating_dialog_v2),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(6.dp))
                    TextField(
                        state = textFieldState,
                        inputTransformation = { if (!this.asCharSequence().toString().matches(pattern)) revertAllChanges() },
                        modifier = Modifier
                            .width(80.dp)
                            .padding(end = 8.dp),
                        enabled = true,
                        lineLimits = TextFieldLineLimits.SingleLine,
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
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.End,
                            textDirection = TextDirection.Ltr
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    val alpha = if (textFieldState.text.isNotBlank()) .75f else 0.38f
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                null,
                                LocalIndication.current,
                                textFieldState.text.isNotBlank()
                            ) {
                                updateTextField("")
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
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.done_v2)) }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = undoEnabled,
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.undo_v2)) }
        }
    )
}

@Composable
private fun EditNotePop(
    textFieldState: TextFieldState,
    undoEnabled: Boolean,
    updateTextField: (String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onNoteEdited: (String) -> Unit
) {
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collectLatest {
            updateTextField(textFieldState.text.toString())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.wrapContentHeight(),
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = stringResource(R.string.notes_v2),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            var lastTime = remember { 0L }
            TextField(
                state = textFieldState,
                inputTransformation = {
                    val text = this.asCharSequence().toString()
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastTime < 50L) {
                        val lines = text.lines()
                        if (lines.size > 1) {
                            val lastLine = lines[lines.size - 2]
                            val currentLine = lines.last()
                            val lastWord = lastLine.substringAfterLast(" ")

                            if (currentLine.isNotEmpty() && currentLine == lastWord) {
                                delete(length - lastWord.length, length)
                            }
                        }
                    }
                    lastTime = currentTime
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
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
                shape = MaterialTheme.shapes.extraSmall,
                lineLimits = TextFieldLineLimits.MultiLine(8, 8),
                textStyle = LocalTextStyle.current.copy(
                    textDirection = if (LanguageState.isRtl) TextDirection.Rtl else TextDirection.Ltr
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onNoteEdited(textFieldState.text.toString()) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.done_v2)) }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = undoEnabled,
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.undo_v2)) }
        }
    )
}

@Composable
private fun EditQuantityPop(
    textFieldState: TextFieldState,
    undoEnabled: Boolean,
    updateTextField: (String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onQtyEdited: (Int) -> Unit
) {
    LaunchedEffect(textFieldState, Dispatchers.Default) {
        snapshotFlow { textFieldState.text.toString() }.collectLatest {
            updateTextField(textFieldState.text.toString())
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
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
                text = stringResource(R.string.no_of_tins_v2),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
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

                val pattern = remember { Regex("^(\\s*|\\d{0,2})$") }
                TextField(
                    state = textFieldState,
                    inputTransformation = {
                        if (!this.toString().matches(pattern)) { revertAllChanges() }
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    textStyle = LocalTextStyle.current.copy(
                        textDirection = TextDirection.Ltr,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.width(54.dp),
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
                        if (textFieldState.text.isEmpty()) {
                            textFieldState.edit { replace(0, length, "1") }
                        } else {
                            val current = textFieldState.text.toString().toInt()
                            val next = (current + 1).coerceAtMost(99)
                            textFieldState.edit { replace(0, length, next.toString()) }
                        }
                    },
                    decreaseClick = {
                        if (textFieldState.text.isEmpty()) {
                            textFieldState.edit { replace(0, length, "0") }
                        } else {
                            val current = textFieldState.text.toString().toInt()
                            val next = (current - 1).coerceAtLeast(0)
                            textFieldState.edit { replace(0, length, next.toString()) }
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onQtyEdited(textFieldState.text.toString().toIntOrNull() ?: 1) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.done_v2)) }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = undoEnabled,
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier.heightIn(32.dp, 32.dp)
            ) { Text(stringResource(R.string.undo_v2)) }
        }
    )
}