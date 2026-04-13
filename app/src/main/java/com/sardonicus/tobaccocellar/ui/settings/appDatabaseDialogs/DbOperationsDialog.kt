package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DbOperationsDialog(
    onDismiss: () -> Unit,
    updateTinSync: () -> Unit,
    optimizeDatabase: () -> Unit,
    modifier: Modifier = Modifier,
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
                    text = "These options shouldn't be necessary. Fix tin sync quantity corrects " +
                            "the \"No. of Tins\" for entries with quantity sync checked. Optimize " +
                            "database cleans up any potentially orphaned data.",
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 8.dp),
                )
                TextButton(
                    onClick = { updateTinSync() },
                    contentPadding = PaddingValues(8.dp, 3.dp),
                    modifier = modifier
                        .heightIn(28.dp, 28.dp)
                ) {
                    Text(
                        text = "Fix/Update Tin Sync Quantity",
                        modifier = Modifier,
                        fontSize = 15.sp,
                    )
                }
                TextButton(
                    onClick = { optimizeDatabase() },
                    contentPadding = PaddingValues(8.dp, 3.dp),
                    modifier = modifier
                        .heightIn(28.dp, 28.dp)
                ) {
                    Text(
                        text = "Clean and Optimize Database",
                        modifier = Modifier,
                        fontSize = 15.sp,
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