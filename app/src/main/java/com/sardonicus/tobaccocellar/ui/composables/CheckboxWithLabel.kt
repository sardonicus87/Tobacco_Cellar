package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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