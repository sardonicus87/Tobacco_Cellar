package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay

data class OneTimeAlert(
    val id: Int,
    val message: @Composable ColumnScope.() -> Unit,
    val date: String,
    val appVersion: String
)

object OneTimeAlerts {
    const val CURRENT_ALERT_VERSION = 1

    val alerts = listOf(
        OneTimeAlert(
            id = 0,
            date = "",
            appVersion = "",
            message = {}
        ),

        OneTimeAlert(
            id = 1,
            date = "27 Mar, 2025",
            appVersion = "2.7.0",
            message = {
                Text(
                    text = "I have employed code and resource shrinking, which makes the app " +
                            "much smaller and should improve performance. However, this " +
                            "could result in some bugs. I have attempted to thoroughly test " +
                            "the app, but may have missed some things.\n\nAdditionally, the " +
                            "backup/restore function has been changed due to a potential for " +
                            "database corruption upon RESTORE. From this version on, any " +
                            "previously created backups will not work, you must create a new " +
                            "backup file. See the changelog for more information.",
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "PLEASE contact me if you encounter any issues:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                )
                SelectionContainer {
                    Text(
                        text = "sardonicus.notadev@gmail.com",
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "(Email address can be found on the app settings screen)",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }
        )
    )
}

@Composable
fun ImportantAlertDialog(
    importantAlertState: ImportantAlertState,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val alert = importantAlertState.alertToDisplay!!
    val isCurrent = importantAlertState.isCurrentAlert

    val scrollState = rememberSaveable(alert.id, saver = ScrollState.Saver) { ScrollState(0) } // rememberScrollState()
    var enabled by rememberSaveable(alert.id) { mutableStateOf(false) }
    val atBottom by remember(alert.id) { derivedStateOf { !scrollState.canScrollForward } }
    var countdown by remember(alert.id) { mutableIntStateOf(5) }
    var canScroll by remember(alert.id) { mutableStateOf(false) }
    val updateScroll: (Boolean) -> Unit = { canScroll = it }

    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        modifier = modifier,
        containerColor = LocalCustomColors.current.darkNeutral,
        title = {
            Text(
                text = "Important Alert!",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
            )
        },
        text = {
            Column {
                Text(
                    text = "This is a one-time alert.",
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                GlowBox(
                    color = GlowColor(LocalCustomColors.current.darkNeutral),
                    size = GlowSize(vertical = 6.dp),
                    modifier = Modifier
                        .height(175.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState),
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                        ) {
                            if (!isCurrent) {
                                Text(
                                    text = "Missed Alert:",
                                    modifier = Modifier,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "${alert.date}\n(v ${alert.appVersion})",
                                modifier = Modifier
                                    .padding(bottom = 16.dp),
                                fontSize = 14.sp
                            )
                            if (canScroll) {
                                Text(
                                    text = "You must scroll to the bottom to be able to acknowledge and " +
                                            "dismiss this dialog.",
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                )
                            }
                            alert.message(this)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            val isScrollable by remember(alert.id) { derivedStateOf { scrollState.maxValue > 0 } }
            LaunchedEffect(isScrollable, alert.id) {
                if (!isScrollable) {
                    while (countdown > 0) {
                        delay(1000)
                        countdown--
                    }
                    enabled = true
                } else {
                    updateScroll(true)
                }
            }
            LaunchedEffect(isScrollable, atBottom, alert.id) {
                if (isScrollable && atBottom) {
                    enabled = true
                }
            }
            val buttonText = remember(isScrollable, enabled, countdown, isCurrent) {
                if (!isCurrent) {
                    if (isScrollable) {
                        "Next"
                    } else {
                        if (enabled) "Next" else "( $countdown )"
                    }
                } else {
                    if (isScrollable) {
                        "Confirm"
                    } else {
                        if (enabled) "Confirm" else "( $countdown )"
                    }
                }
            }

            Button(
                onClick = {
                    if (!isCurrent) {
                        enabled = false
                        updateScroll(false)
                        countdown = 5
                        viewModel.saveAlertSeen(alert.id)
                    } else {
                        viewModel.saveAlertSeen(alert.id)
                    }
                },
                enabled = enabled
            ) {
                Box (contentAlignment = Alignment.Center) {
                    Text(
                        text = "Confirm",
                        color = Color.Transparent
                    )
                    Text(
                        text = buttonText
                    )
                }
            }
        }
    )
}