package com.sardonicus.tobaccocellar.ui.utilities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CornerRadii(
    val topLeft: Dp = 0.dp,
    val topRight: Dp = 0.dp,
    val bottomRight: Dp = 0.dp,
    val bottomLeft: Dp = 0.dp
)

object DeviceCorners {
    var radii by mutableStateOf(CornerRadii())
        private set

    val topLeft get() = radii.topLeft
    val topRight get() = radii.topRight
    val bottomRight get() = radii.bottomRight
    val bottomLeft get() = radii.bottomLeft

    internal fun update(newRadii: CornerRadii) {
        if (radii != newRadii) { radii = newRadii }
    }
}