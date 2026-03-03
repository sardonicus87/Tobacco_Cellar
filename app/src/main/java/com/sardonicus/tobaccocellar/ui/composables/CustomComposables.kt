package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R


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


@Composable
fun CheckboxWithLabel(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    height: Dp = 36.dp,
    enabled: Boolean = true,
    fontColor: Color = LocalContentColor.current,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    allowResize: Boolean = false,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() }
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .height(height)
            .offset(x = (-2).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .padding(0.dp),
                enabled = enabled,
                colors = colors,
                interactionSource = interactionSource
            )
        }
        Text(
            text = text,
            style = LocalTextStyle.current.copy(
                color = if (enabled) fontColor else fontColor.copy(alpha = 0.5f),
                lineHeight = lineHeight
            ),
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(end = 6.dp),
            maxLines = 1,
            fontSize = if (!allowResize) fontSize else TextUnit.Unspecified,
            autoSize = if (!allowResize) { null } else {
                TextAutoSize.StepBased(
                    maxFontSize = fontSize,
                    minFontSize = 9.sp,
                    stepSize = .2.sp
                )
            }
        )
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

