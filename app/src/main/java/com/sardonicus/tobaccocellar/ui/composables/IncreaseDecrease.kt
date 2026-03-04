package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.R

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