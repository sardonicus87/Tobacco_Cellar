package com.sardonicus.tobaccocellar.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Fade effect boxes. If you want a set height and scrolling effect, add scroll modifier to
 * the content itself. Use spacers in the content to inset/pad from the edges (glow extends
 *  inward from the edge) if you want a fade effect on scroll.*/
@Composable
fun GlowBox(
    color: GlowColor,
    size: GlowSize,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        Column {
            content.invoke()
        }
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
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Transparent,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor)
    ) {
        Spacer(
            modifier = Modifier
                .weight(1.5f)
        )
        CircularProgressIndicator(
            modifier = Modifier
                .padding(0.dp)
                .size(48.dp)
                .weight(0.5f),
        )
        Spacer(
            modifier = Modifier
                .weight(2f)
        )
    }
}


/** TextField with inner-padding access */
@OptIn(ExperimentalMaterial3Api::class)
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
            leadingIcon = leadingIcon,
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
        )
    }
}


/** TextField with AutoComplete suggestions */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteText(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: ((String) -> Unit)?,
    allItems: List<String>,
    onOptionSelected: ((String) -> Unit)?,
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
    val focusRequester = remember { FocusRequester() }
    var override by remember { mutableStateOf(false) }
    var expandedState by remember { mutableStateOf(false) }

    var fieldY by remember { mutableFloatStateOf(0f) }
    var menuY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(value) {
        expandedState = if (value.length >= 2) {
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
            onValueChange = {
                textFieldValueState = it
                val text = it.text
                onValueChange?.invoke(text)

                val input =
                    if (componentField && text.contains(", ")) {
                        text.substringAfterLast(", ", "") }
                    else { text }

                if (input.length >= 2) {
                    val startsWith = allItems.filter { it.startsWith(input, ignoreCase = true) }
                    val otherWordsStartsWith = allItems.filter {
                        it.split(" ").drop(1)
                            .any {
                                it.startsWith(input, ignoreCase = true) }
                                && !startsWith.contains(it)
                    }
                    val contains = allItems.filter {
                        it.contains(input, ignoreCase = true)
                                && !startsWith.contains(it) && !otherWordsStartsWith.contains(it)
                    }

                    val selectedInput =
                        if (componentField) {
                            val components = text.substringBeforeLast(", ", "").trim()
                            if (components.isNotBlank()) {
                                components.split(", ").map { it.trim() }.filter { it.isNotBlank() } }
                            else { emptyList() } }
                        else { listOf(input) }
                    val selected = allItems.filter {
                        if (componentField) { selectedInput.contains(it) }
                        else { it.equals(input, ignoreCase = false) } }

                    suggestionsState = (startsWith + otherWordsStartsWith + contains) - selected
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { if (!it.isFocused) { expandedState = false } }
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

                        val updatedText =
                            if (componentField && currentText.contains(", ")) {
                                currentText.substringBeforeLast(", ", "") + ", " + label + ", " }
                            else { if (componentField) { "$label, " } else { label } }

                        textFieldValueState = TextFieldValue(
                            text = updatedText,
                            selection = TextRange(updatedText.length)
                        )

                        override = true
                        expandedState = false
                        onOptionSelected?.invoke(updatedText)
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
    overflowIndicator: @Composable (overflowCount: Int) -> Unit,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
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
        val overflowMeasurable = subcompose("overflow_max") { overflowIndicator(maxPossibleOverflow) }.firstOrNull()
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

        // final composition
        val actualOverCount = itemCount - visibleItemCount
        val showOver = actualOverCount > 0 && overflowIndicatorWidth > 0

        val finalPlaceables = subcompose("final_render") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                for (i in 0 until visibleItemCount) { itemContent(i) }
                if (showOver) { overflowIndicator(actualOverCount) }
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
                ImageVector.Companion.vectorResource(id = checkedIcon)
            } else ImageVector.Companion.vectorResource(id = uncheckedIcon),
            contentDescription = null,
            modifier = Modifier.Companion
        )
    }
}

