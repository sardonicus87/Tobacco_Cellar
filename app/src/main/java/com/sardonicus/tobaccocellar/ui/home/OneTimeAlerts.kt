package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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