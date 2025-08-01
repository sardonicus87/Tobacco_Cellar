package com.sardonicus.tobaccocellar.ui.utilities

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier =
    this.clickable(
        indication = null,
        interactionSource = null,
        onClick = { onClick() }
    )