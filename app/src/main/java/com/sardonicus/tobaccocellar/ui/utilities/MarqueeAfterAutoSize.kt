package com.sardonicus.tobaccocellar.ui.utilities

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.autoSizeMarquee(overflowing: Boolean, spacing: Float = .5f): Modifier {
    return if (overflowing) {
        basicMarquee(
            iterations = Int.MAX_VALUE, repeatDelayMillis = 1500,
            spacing = MarqueeSpacing.fractionOfContainer(spacing)
        )
    } else Modifier
}