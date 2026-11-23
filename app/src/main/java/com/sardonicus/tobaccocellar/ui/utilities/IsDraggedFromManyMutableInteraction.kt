package com.sardonicus.tobaccocellar.ui.utilities

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberAnyDragged(vararg sources: MutableInteractionSource): Boolean {
    val states = sources.map { it.collectIsDraggedAsState().value }
    return remember(states) { states.any { it } }
}