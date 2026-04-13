package com.sardonicus.tobaccocellar.ui.settings.displayDialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingsDialog(
    onDismiss: () -> Unit,
    showRatings: Boolean,
    onRatingsOption: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Display ratings on cellar screen:",
                    modifier = Modifier,
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offAlpha = if (!showRatings) 1f else .5f
                    val onAlpha = if (showRatings) 1f else .5f
                    Text(
                        text = "Off",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (!showRatings) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(alpha = offAlpha)
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = showRatings,
                            onCheckedChange = { onRatingsOption(it) },
                            modifier = Modifier
                                .requiredHeight(20.dp)
                                .scale(.6f),
                            colors = SwitchDefaults.colors(
                            )
                        )
                    }
                    Text(
                        text = "On",
                        modifier = Modifier,
                        fontSize = 14.sp,
                        fontWeight = if (showRatings) FontWeight.SemiBold else FontWeight.Normal,
                        color = LocalContentColor.current.copy(onAlpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}