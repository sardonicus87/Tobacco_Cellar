package com.sardonicus.tobaccocellar.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

/** Fade effect boxes. If you want a set height and scrolling effect, add scroll modifier to
 * the content itself. Use spacers in the content to inset/pad from the edges (glow extends
 *  inward from the edge) if you want a fade effect on scroll.*/
@Composable
fun GlowBox(
    color: GlowColor,
    size: GlowSize,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier,
        contentAlignment = contentAlignment
    ) {
        Column (
            modifier = modifier,
            content = content
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawGlow(color, size)
                }
        )
    }
}

fun DrawScope.drawGlow(glowColor: GlowColor, glowSize: GlowSize) {
    val topSize: Dp
    val bottomSize: Dp
    val startSize: Dp
    val endSize: Dp

    val topColor: Color
    val bottomColor: Color
    val startColor: Color
    val endColor: Color

    when (glowSize) {
        is GlowSize.All -> {
            topSize = glowSize.size
            bottomSize = glowSize.size
            startSize = glowSize.size
            endSize = glowSize.size
        }

        is GlowSize.HorizontalVertical -> {
            topSize = glowSize.vertical
            bottomSize = glowSize.vertical
            startSize = glowSize.horizontal
            endSize = glowSize.horizontal
        }

        is GlowSize.Edges -> {
            topSize = glowSize.top
            bottomSize = glowSize.bottom
            startSize = glowSize.start
            endSize = glowSize.end
        }
    }

    when (glowColor) {
        is GlowColor.All -> {
            topColor = glowColor.color
            bottomColor = glowColor.color
            startColor = glowColor.color
            endColor = glowColor.color
        }

        is GlowColor.HorizontalVertical -> {
            topColor = glowColor.vertical
            bottomColor = glowColor.vertical
            startColor = glowColor.horizontal
            endColor = glowColor.horizontal
        }

        is GlowColor.Edges -> {
            topColor = glowColor.top
            bottomColor = glowColor.bottom
            startColor = glowColor.start
            endColor = glowColor.end
        }
    }

    val topFade = topSize > 0.dp
    val bottomFade = bottomSize > 0.dp
    val startFade = startSize > 0.dp
    val endFade = endSize > 0.dp

    val fadeOffsetX = size.width - endSize.toPx()
    val fadeOffsetY = size.height - bottomSize.toPx()

    if (topFade) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    topColor,
                    Color.Transparent,
                ),
                startY = 0f,
                endY = topSize.toPx(),
            ),
            topLeft = Offset(0f, 0f),
            size = Size(size.width, topSize.toPx())
        )
    }
    if (bottomFade) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    bottomColor,
                ),
                startY = fadeOffsetY,
                endY = size.height,
            ),
            topLeft = Offset(0f, fadeOffsetY),
            size = Size(size.width, bottomSize.toPx())
        )
    }
    if (startFade) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    startColor,
                    Color.Transparent,
                ),
                startX = 0f,
                endX = startSize.toPx(),
            ),
            topLeft = Offset(0f, 0f),
            size = Size(startSize.toPx(), size.height)
        )
    }
    if (endFade) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    endColor,
                ),
                startX = fadeOffsetX,
                endX = size.width,
            ),
            topLeft = Offset(fadeOffsetX, 0f),
            size = Size(endSize.toPx(), size.height)
        )
    }
}

sealed class GlowColor {
    data class All(
        val color: Color = Color.Transparent
    ) : GlowColor()

    data class HorizontalVertical(
        val horizontal: Color = Color.Transparent,
        val vertical: Color = Color.Transparent,
    ) : GlowColor()

    data class Edges(
        val top: Color = Color.Transparent,
        val bottom: Color = Color.Transparent,
        val start: Color = Color.Transparent,
        val end: Color = Color.Transparent,
    ) : GlowColor()

    companion object {
        operator fun invoke(
            color: Color = Color.Transparent
        ): All = All(color)

        operator fun invoke(
            horizontal: Color = Color.Transparent,
            vertical: Color = Color.Transparent,
        ): HorizontalVertical = HorizontalVertical(horizontal, vertical)

        operator fun invoke(
            top: Color = Color.Transparent,
            bottom: Color = Color.Transparent,
            start: Color = Color.Transparent,
            end: Color = Color.Transparent
        ): Edges = Edges(top, bottom, start, end)
    }
}

sealed class GlowSize {
    data class All(
        val size: Dp = 0.dp
    ) : GlowSize()

    data class HorizontalVertical(
        val horizontal: Dp = 0.dp,
        val vertical: Dp = 0.dp
    ) : GlowSize()

    data class Edges(
        val top: Dp = 0.dp,
        val bottom: Dp = 0.dp,
        val start: Dp = 0.dp,
        val end: Dp = 0.dp,
    ) : GlowSize()

    companion object {
        operator fun invoke(
            size: Dp = 0.dp
        ): All = All(size)

        operator fun invoke(
            horizontal: Dp = 0.dp,
            vertical: Dp = 0.dp
        ): HorizontalVertical = HorizontalVertical(horizontal, vertical)

        operator fun invoke(
            top: Dp = 0.dp,
            bottom: Dp = 0.dp,
            start: Dp = 0.dp,
            end: Dp = 0.dp
        ): Edges = Edges(top, bottom, start, end)
    }
}


/** Full screen loading indicator */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Transparent,
    center: Boolean = false,
) {
    var topWeight: Float
    var bottomWeight: Float

    when (center) {
        true -> {
            topWeight = 1f
            bottomWeight = 1f
        }
        false -> {
            topWeight = 1.5f
            bottomWeight = 2f
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor)
    ) {
        Spacer(
            modifier = Modifier
                .weight(topWeight)
        )
        CircularProgressIndicator(
            modifier = Modifier
                .padding(0.dp)
                .size(48.dp),
        )
        Spacer(
            modifier = Modifier
                .weight(bottomWeight)
        )
    }
}


/** Increase/decrease buttons */
@Composable
fun IncreaseDecrease(
    increaseClick: () -> Unit,
    decreaseClick: () -> Unit,
    modifier: Modifier = Modifier,
    increaseEnabled: Boolean = true,
    decreaseEnabled: Boolean = true,
    tint: Color = LocalContentColor.current
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.triangle_arrow_up),
            contentDescription = "Increase",
            modifier = Modifier
                .align(Alignment.Top)
                .clickable(
                    enabled = increaseEnabled,
                    indication = LocalIndication.current,
                    interactionSource = null
                ) { increaseClick() }
                .padding(start = 8.dp, end = 2.dp, top = 4.dp, bottom = 4.dp)
                .offset(x = 1.dp, y = 2.dp),
            tint = tint.copy(alpha = if (increaseEnabled) 1f else 0.38f)
        )
        Icon(
            painter = painterResource(id = R.drawable.triangle_arrow_down),
            contentDescription = "Decrease",
            modifier = Modifier
                .align(Alignment.Bottom)
                .clickable(
                    enabled = decreaseEnabled,
                    indication = LocalIndication.current,
                    interactionSource = null
                ) { decreaseClick() }
                .padding(start = 2.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                .offset(x = (-1).dp, y = (-2).dp),
            tint = tint.copy(alpha = if (decreaseEnabled) 1f else 0.38f)
        )
    }
}


/** TextField with inner-padding access */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues = contentPaddingWithoutLabel(),
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
    ) { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = value,
            visualTransformation = visualTransformation,
            innerTextField = innerTextField,
            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            interactionSource = interactionSource,
            colors = colors.copy(
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = shape,
            contentPadding = contentPadding,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
        )
    }
}


/** TextField with AutoComplete suggestions */
@Composable
fun AutoCompleteText(
    value: String,
    onValueChange: ((String) -> Unit)?,
    allItems: List<String>,
    onOptionSelected: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current,
    componentField: Boolean = false,
) {
    var suggestionsState by remember { mutableStateOf<List<String>>(emptyList()) }
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(value)) }
    var override by remember { mutableStateOf(false) }
    var expandedState by remember { mutableStateOf(false) }

    var fieldY by remember { mutableFloatStateOf(0f) }
    var menuY by remember { mutableFloatStateOf(0f) }
    var typeCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(value) {
        expandedState = if (value.length >= 2 && typeCount >= 2) {
            suggestionsState.isNotEmpty() && !override
        } else {
            false
        }
    }

    LaunchedEffect(override) {
        if (override) {
            delay(250)
            override = false
            if (componentField) {
                suggestionsState = emptyList()
            }
        }
    }

    BackHandler(enabled = expandedState) { expandedState = false }

    Box(modifier = modifier) {
        TextField(
            value = textFieldValueState.copy(text = value),
            onValueChange = { textFieldValue ->
                textFieldValueState = textFieldValue
                val text = textFieldValue.text
                onValueChange?.invoke(text)

                val input =
                    if (componentField && text.contains(", ")) {
                        val cursorPosition = textFieldValueState.selection.start
                        val delimiterIndices = text.mapIndexedNotNull { index, char ->
                            when (char) {
                                ',' -> index
                                ' ' -> index
                                else -> null
                            }
                        }.toMutableList().apply {
                            add(-1)
                            add(text.length)
                        }.sorted()
                        val startIndex = delimiterIndices.last { it < cursorPosition }
                        val endIndex = delimiterIndices.first { it >= cursorPosition }

                        text.substring(startIndex + 1, endIndex).trim()
                    } else { text }

                if (input.isNotBlank()) typeCount++

                if (input.length >= 2 && typeCount >= 2) {
                    val startsWith = allItems.filter { it.startsWith(input, ignoreCase = true) }
                    val otherWordsStartsWith = allItems.filter { string ->
                        string.split(" ").drop(1)
                            .any {
                                it.startsWith(input, ignoreCase = true) }
                                && !startsWith.contains(string)
                    }
                    val contains = allItems.filter {
                        it.contains(input, ignoreCase = true)
                                && !startsWith.contains(it) && !otherWordsStartsWith.contains(it)
                    }

                    val selectedInput =
                        if (componentField) {
                            if (text.isNotBlank()) {
                                text.split(", ").map { it.trim() }.filter { it.isNotBlank() } }
                            else { emptyList() } }
                        else { listOf(input) }
                    val selected = allItems.filter {
                        if (componentField) { selectedInput.contains(it) }
                        else { it.equals(input, ignoreCase = false) } }.toSet()

                    suggestionsState = (startsWith + otherWordsStartsWith + contains) - selected
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .onFocusChanged {
                    if (!it.isFocused) {
                        expandedState = false
                        typeCount = 0
                    }
                }
                .onGloballyPositioned { fieldY = it.positionOnScreen().y },
            enabled = enabled,
            trailingIcon = trailingIcon,
            singleLine = true,
            placeholder = placeholder,
            keyboardOptions = keyboardOptions,
            textStyle = textStyle,
            colors = colors.copy(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.50f),
            ),
            shape = MaterialTheme.shapes.extraSmall,
            label = label,
            maxLines = maxLines,
            minLines = minLines,
            supportingText = supportingText,
        )

        val yOffset = if (fieldY > menuY) 8.dp else (-8).dp

        DropdownMenu(
            expanded = expandedState,
            onDismissRequest = { /**/ },
            modifier = Modifier
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                .height(intrinsicSize = IntrinsicSize.Max)
                .onGloballyPositioned { menuY = it.positionOnScreen().y },
            properties = PopupProperties(focusable = false),
            offset = DpOffset(32.dp, yOffset),
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            suggestionsState.take(3).forEach { label ->
                CustomDropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            modifier = Modifier
                                .padding(0.dp)
                                .focusable(false),
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        val currentText = textFieldValueState.text
                        val cursorPosition = textFieldValueState.selection.start

                        val newValue =
                            if (componentField && currentText.contains(", ")) {
                                val delimiterIndices = currentText.mapIndexedNotNull { index, char ->
                                    when (char) {
                                        ',' -> index
                                        ' ' -> index
                                        else -> null
                                    }
                                }.toMutableList().apply {
                                    add(-1)
                                    add(currentText.length)
                                }.sorted()
                                val startIndex = delimiterIndices.last { it < cursorPosition }
                                val endIndex = delimiterIndices.first { it >= cursorPosition }

                                val updatedText = currentText.replaceRange(startIndex + 1, endIndex, " $label, ")
                                    .trimStart().replace("  ", " ").replace(", ,", ",")
                                val finalCursorPos = if (startIndex == -1) {
                                    label.length + 2
                                } else if (endIndex == currentText.length) {
                                    updatedText.length
                                }
                                else { (startIndex + 1) + label.length + 1 }

                                TextFieldValue(
                                    text = updatedText,
                                    selection = TextRange(finalCursorPos.coerceIn(0, updatedText.length))
                                )
                            } else {
                                val updatedText = if (componentField) { "$label, " } else { label }
                                TextFieldValue(
                                    text = updatedText,
                                    selection = TextRange(updatedText.length)
                                )
                            }

                        textFieldValueState = newValue

                        override = true
                        typeCount = 0
                        expandedState = false
                        onOptionSelected?.invoke(newValue.text)
                    },
                    enabled = true,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp)
                        .offset(0.dp, 0.dp)
                        .fillMaxWidth(),
                    colors = MenuDefaults.itemColors(),
                )
            }
        }
    }
}

@Composable
fun CustomDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = false,
    modifier: Modifier,
    colors: MenuItemColors = MenuDefaults.itemColors(textColor = LocalContentColor.current),
) {
    Box(
        modifier = modifier
            .clickable(
                indication = LocalIndication.current,
                interactionSource = null
            ) { onClick() }
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
    ) {
        text()
        if (enabled) {
            colors.copy(
                textColor = colors.textColor
            )
        }
    }
}


/** Replacement stuff for ContextualFlowRow */
@Composable
fun OverflowRow(
    itemCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
    overflowIndicator: @Composable (
        overflowCount: Int,
        enabledOverflowCount: Int,
        isOverflowEnabled: Boolean,
    ) -> Unit,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    enabledAtIndex: ((index: Int) -> Boolean)? = null,
) {
    val density = LocalDensity.current
    val spacingPx =
        remember(itemSpacing, density) { with(density) { itemSpacing.toPx() } }.toInt()

    SubcomposeLayout(modifier = modifier) { constraints ->
        val maxWidth = constraints.maxWidth
        if (itemCount == 0) {
            return@SubcomposeLayout layout(0, 0) {}
        }

        // measure indicator
        val maxPossibleOverflow = itemCount
        val overflowMeasurable = subcompose("overflow_max") { overflowIndicator(maxPossibleOverflow, maxPossibleOverflow, true) }.firstOrNull()
        val overflowPlaceable = overflowMeasurable?.measure(Constraints())
        val overflowIndicatorWidth = overflowPlaceable?.width ?: 0
        val overflowIndicatorHeight = overflowPlaceable?.height ?: 0

        // measure items and check fit in row
        var currentItemsWidth = 0
        var visibleItemCount = 0
        val placeables = mutableListOf<Placeable>()
        var lineMaxHeight = 0

        val allItemMeasurables = subcompose("items_measure") {
            for (i in 0 until itemCount) itemContent(i)
        }

        for (i in 0 until itemCount) {
            val itemMeasurable = allItemMeasurables[i]
            val itemPlaceable = itemMeasurable.measure(Constraints())
            val itemWidth = itemPlaceable.width + if (visibleItemCount > 0) spacingPx else 0

            // checking item fit by adding items until over width
            if (currentItemsWidth + itemWidth <= maxWidth) {
                placeables.add(itemPlaceable)
                currentItemsWidth += itemWidth
                lineMaxHeight = maxOf(lineMaxHeight, itemPlaceable.height)
                visibleItemCount++
            } else {
                val widthVisibleSoFar = currentItemsWidth
                val widthWithOverflow =
                    widthVisibleSoFar + (if (visibleItemCount > 0 && overflowIndicatorWidth > 0) spacingPx else 0) + overflowIndicatorWidth

                if (widthWithOverflow <= maxWidth) {
                    // everything fits,  no op
                } else {
                    // remove additional items as needed until overflow indicator fits
                    while (visibleItemCount > 0) {
                        visibleItemCount--
                        currentItemsWidth = placeables.take(visibleItemCount)
                            .sumOf { it.width } + if (visibleItemCount > 0) (visibleItemCount - 1) * spacingPx else 0

                        val newWidthWithOverflow =
                            currentItemsWidth + (if (visibleItemCount > 0 && overflowIndicatorWidth > 0) spacingPx else 0) + overflowIndicatorWidth
                        if (newWidthWithOverflow <= maxWidth) {
                            break // overflow indicator fits
                        }
                    }

                    if (visibleItemCount == 0 && overflowIndicatorWidth > 0 && overflowIndicatorWidth > maxWidth) {
                        return@SubcomposeLayout layout(0, 0) {} // even overflow indicator doesn't fit
                    }
                }
                break
            }
        }

        val actualOverCount = itemCount - visibleItemCount
        val showOver = actualOverCount > 0 && overflowIndicatorWidth > 0

        var enabledOverflowCount = 0
        var anyOverflowedEnabled = false
        if (showOver && enabledAtIndex != null) {
            for (i in visibleItemCount until itemCount) {
                if (enabledAtIndex(i)) {
                    enabledOverflowCount++
                    anyOverflowedEnabled = true
                }
            }
        } else if (showOver) {
            enabledOverflowCount = actualOverCount
            anyOverflowedEnabled = true
        }

        // final composition
        val finalPlaceables = subcompose("final_render") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                for (i in 0 until visibleItemCount) { itemContent(i) }
                if (showOver) { overflowIndicator(actualOverCount, enabledOverflowCount, anyOverflowedEnabled) }
            }
        }.map { it.measure(constraints) }

        val finalWidth = finalPlaceables.firstOrNull()?.width ?: 0
        val finalHeight = finalPlaceables.firstOrNull()?.height ?: maxOf(
            lineMaxHeight, if (showOver) overflowIndicatorHeight else 0
        )

        layout(finalWidth, finalHeight) {
            finalPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}


/** pager indicator **/
data class IndicatorSizes(val current: Dp, val other: Dp)

@Composable
fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    indicatorSize: IndicatorSizes = IndicatorSizes(current = 8.dp, other = 7.dp),
) {
    val animationScope = rememberCoroutineScope()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) {
            val color = if (pagerState.currentPage == it) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
            }
            val size =
                if (pagerState.currentPage == it) indicatorSize.current else indicatorSize.other

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(size)
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { animationScope.launch { pagerState.animateScrollToPage(it) } }
            )
        }
    }
}


/** Rating Row */
@Composable
fun RatingRow(
    rating: Double?,
    modifier: Modifier = Modifier,
    starSize: Dp = Dp.Unspecified,
    showEmpty: Boolean = false,
    starColor: Color = LocalCustomColors.current.starRating,
    emptyColor: Color = LocalCustomColors.current.starRating,
    emptyAlpha: Float = 0.38f,
) {
    if (starSize != Dp.Unspecified) {
        RatingRowImpl(
            rating = rating,
            modifier = modifier,
            starSize = starSize,
            maxColor = starColor,
            minColor = starColor,
            emptyColor = emptyColor,
            showEmpty = showEmpty,
            showDivider = false,
            dividerColor = Color.Transparent,
            maxAlpha = 1f,
            minAlpha = 1f,
            emptyAlpha = emptyAlpha,
            range = Pair(null, null),
            showRating = true,
            showRange = false
        )
    } else if (rating != null) {
        BoxWithConstraints(modifier = modifier) {
            val dynamicSize = if (maxHeight > 0.dp && maxWidth > 0.dp) {
                if (showEmpty) {
                    min(maxHeight, maxWidth / 5)
                } else {
                    min(maxHeight, maxWidth * rating.toFloat())
                }
            } else {
                0.dp
            }
            if (dynamicSize > 0.dp) {
                RatingRowImpl(
                    rating = rating,
                    modifier = modifier,
                    starSize = dynamicSize,
                    maxColor = starColor,
                    minColor = starColor,
                    emptyColor = emptyColor,
                    showEmpty = showEmpty,
                    showDivider = false,
                    dividerColor = Color.Transparent,
                    maxAlpha = 1f,
                    minAlpha = 1f,
                    emptyAlpha = emptyAlpha,
                    range = Pair(null, null),
                    showRating = true,
                    showRange = false
                )
            }
        }
    }
}

// Range //
@Composable
fun RatingRow(
    range: Pair<Double?, Double?>,
    modifier: Modifier = Modifier,
    starSize: Dp = Dp.Unspecified,
    showDivider: Boolean = false,
    dividerColor: Color = MaterialTheme.colorScheme.onBackground,
    minColor: Color = LocalCustomColors.current.starRating,
    maxColor: Color = LocalCustomColors.current.starRating,
    emptyColor: Color = LocalContentColor.current,
    minAlpha: Float = 1f,
    maxAlpha: Float = 1f,
    emptyAlpha: Float = 0.38f,
) {
    if (starSize != Dp.Unspecified) {
        RatingRowImpl(
            rating = 5.0,
            modifier = modifier,
            starSize = starSize,
            maxColor = maxColor,
            minColor = minColor,
            emptyColor = emptyColor,
            showEmpty = true,
            showDivider = showDivider,
            dividerColor = dividerColor,
            maxAlpha = maxAlpha,
            minAlpha = minAlpha,
            emptyAlpha = emptyAlpha,
            range = range,
            showRating = false,
            showRange = true
        )
    } else {
        BoxWithConstraints(modifier = modifier) {
            val dynamicSize = if (maxHeight > 0.dp && maxWidth > 0.dp) {
                min(maxHeight, maxWidth / 5)
            } else {
                0.dp
            }
            if (dynamicSize > 0.dp) {
                RatingRowImpl(
                    rating = 5.0,
                    modifier = modifier,
                    starSize = dynamicSize,
                    maxColor = maxColor,
                    minColor = minColor,
                    emptyColor = emptyColor,
                    showEmpty = true,
                    showDivider = showDivider,
                    dividerColor = dividerColor,
                    maxAlpha = maxAlpha,
                    minAlpha = minAlpha,
                    emptyAlpha = emptyAlpha,
                    range = range,
                    showRating = false,
                    showRange = true
                )
            }
        }
    }
}

@Composable
private fun RatingRowImpl(
    rating: Double?,
    modifier: Modifier = Modifier,
    starSize: Dp,
    showEmpty: Boolean,
    showDivider: Boolean,
    dividerColor: Color,
    maxColor: Color,
    minColor: Color,
    emptyColor: Color,
    maxAlpha: Float,
    minAlpha: Float,
    emptyAlpha: Float,
    range: Pair<Double?, Double?>,
    showRating: Boolean,
    showRange: Boolean,
) {
    @Composable
    fun star(color: Color, alignment: Alignment = Alignment.CenterStart, alpha: Float = 1f) {
        Image(
            painter = painterResource(id = R.drawable.star_filled),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier
                .height(starSize),
            alignment = alignment,
            contentScale = ContentScale.FillHeight,
            alpha = alpha,
        )
    }

    @Composable
    fun fractionalStar(
        fractional: Double,
        startColor: Color = maxColor,
        endColor: Color = emptyColor,
        startAlpha: Float = 1f,
        endAlpha: Float = 1f
    ) {
        val startWidth = starSize * fractional.toFloat()
        val endWidth = starSize - startWidth
        Box(
            modifier = Modifier
                .height(starSize)
                .width(startWidth)
                .clip(RectangleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            star(startColor, alpha = startAlpha)
        }
        if (showEmpty) {
            Box(
                modifier = Modifier
                    .height(starSize)
                    .width(endWidth)
                    .clip(RectangleShape),
                contentAlignment = Alignment.CenterStart
            ) {
                star(endColor, Alignment.CenterEnd, endAlpha)
            }
        }
    }

    Row(
        modifier = modifier
            .height(starSize),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showRange) {
            val nullRange = range.first == null && range.second == null

            if (nullRange) {
                repeat(5) {
                    star(emptyColor, alpha = emptyAlpha)
                }
            } else {
                val min = range.first ?: 0.0
                val max = range.second ?: range.first ?: 5.0
                val minWhole = (0.0 + min).toInt()
                val fractionalMin = min - minWhole
                val entireMin = ceil(minWhole + fractionalMin).toInt()
                val maxWhole = (max - entireMin).toInt()
                val fractionalMax = if (max != min) (max - (maxWhole + entireMin)) else 0.0
                val emptyEnd = (5.0 - max).toInt()

                val fractionalMinAlphaRemap = if (range.second == null || range.second == range.first) emptyAlpha else maxAlpha
                val fractionalMinColorRemap = if (range.second == null || range.second == range.first) emptyColor else maxColor

                Box {
                    Row {
                        repeat(minWhole) {
                            star(minColor, alpha = minAlpha)
                        }
                        if (fractionalMin > 0.0) {
                            fractionalStar(
                                fractionalMin,
                                minColor,
                                fractionalMinColorRemap,
                                minAlpha,
                                fractionalMinAlphaRemap
                            )
                        }
                        repeat(maxWhole) {
                            star(maxColor, alpha = maxAlpha)
                        }
                        if (fractionalMax > 0.0) {
                            fractionalStar(
                                fractionalMax,
                                maxColor,
                                emptyColor,
                                maxAlpha,
                                emptyAlpha
                            )
                        }
                        repeat(emptyEnd) {
                            star(emptyColor, alpha = emptyAlpha)
                        }
                    }
                    if (showDivider) {
                        Box(
                            modifier = Modifier
                                .matchParentSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            range.toList().forEach {
                                if (it != null && (it != 0.0 && it != 5.0)) {
                                    val padding: Dp = starSize * it.toFloat()

                                    Row {
                                        Spacer(modifier = Modifier.width(padding))
                                        VerticalDivider(thickness = 1.5.dp, color = dividerColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showRating) {
            if (rating != null) {
                val whole = rating.toInt()
                val remainder = rating - whole
                val empty = (5.0 - rating).toInt()

                repeat(whole) {
                    star(maxColor)
                }
                if (remainder > 0.0) {
                    fractionalStar(
                        remainder,
                        maxColor,
                        emptyColor,
                        1f,
                        emptyAlpha
                    )
                }
                if (showEmpty && empty > 0) {
                    repeat(empty) {
                        star(emptyColor, alpha = emptyAlpha)
                    }
                }
            } else {
                if (showEmpty) {
                    repeat(5) {
                        star(emptyColor, alpha = emptyAlpha)
                    }
                }
            }
        }
    }
}


@Composable
fun CustomCheckBox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    checkedIcon: Int,
    uncheckedIcon: Int,
    modifier: Modifier = Modifier,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
    enabled: Boolean = true,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange?.invoke(it) },
        modifier = modifier
            .size(34.dp),
        colors = colors,
        enabled = enabled
    ) {
        Icon(
            imageVector = if (checked) {
                ImageVector.vectorResource(id = checkedIcon)
            } else ImageVector.vectorResource(id = uncheckedIcon),
            contentDescription = null,
            modifier = Modifier.Companion
        )
    }
}

