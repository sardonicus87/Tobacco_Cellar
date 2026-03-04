package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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