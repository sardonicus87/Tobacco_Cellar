package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    checkedIcon: Int,
    uncheckedIcon: Int,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
    enabled: Boolean = true,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange?.invoke(it) },
        modifier = modifier
            .size(size),
        colors = colors,
        enabled = enabled
    ) {
        Icon(
            imageVector =
                if (checked) { ImageVector.vectorResource(id = checkedIcon) }
                else ImageVector.vectorResource(id = uncheckedIcon),
            contentDescription = null,
            modifier = Modifier.Companion
        )
    }
}