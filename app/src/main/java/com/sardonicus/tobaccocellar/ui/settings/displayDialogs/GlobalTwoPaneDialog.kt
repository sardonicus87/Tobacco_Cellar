package com.sardonicus.tobaccocellar.ui.settings.displayDialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun GlobalTwoPaneDialog(
    onDismiss: () -> Unit,
    globalTwoPane: Boolean,
    twoColumnTabs: Boolean,
    landscapeTwoPane: Boolean,
    onGlobalTwoPane: (Boolean) -> Unit,
    onTwoColumnTabs: (Boolean) -> Unit,
    onLandscapeTwoPane: (Boolean) -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Adaptive layout options for large screens:",
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dual Pane Layouts:",
                        modifier = Modifier,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = LocalContentColor.current
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = globalTwoPane,
                            onCheckedChange = { onGlobalTwoPane(it) },
                            modifier = Modifier
                                .scale(.6f)
                                .padding(start = 10.dp),
                            colors = SwitchDefaults.colors()
                        )
                    }
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expand tabs to two columns:",
                        modifier = Modifier,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = LocalContentColor.current
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = twoColumnTabs,
                            onCheckedChange = { onTwoColumnTabs(it) },
                            modifier = Modifier
                                .scale(.6f)
                                .padding(start = 10.dp),
                            colors = SwitchDefaults.colors()
                        )
                    }
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Restrict to landscape only:",
                        modifier = Modifier,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = LocalContentColor.current
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = landscapeTwoPane,
                            onCheckedChange = { onLandscapeTwoPane(it) },
                            modifier = Modifier
                                .scale(.6f)
                                .padding(start = 10.dp),
                            colors = SwitchDefaults.colors()
                        )
                    }
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