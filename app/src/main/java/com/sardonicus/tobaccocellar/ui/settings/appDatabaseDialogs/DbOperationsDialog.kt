package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DbOperationsDialog(
    onDismiss: () -> Unit,
    loading: Boolean,
    updateTinSync: () -> Unit,
    optimizeDatabase: () -> Unit
) {
    var debouncedLoading by remember { mutableStateOf(false) }

    LaunchedEffect(loading) {
        if (loading) {
            delay(50.milliseconds)
            debouncedLoading = true
        } else { debouncedLoading = false }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        text = {
            Box {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "These options shouldn't be necessary. Fix tin sync quantity corrects " +
                                "the \"No. of Tins\" for entries with quantity sync checked. Optimize " +
                                "database cleans up any potentially orphaned data.",
                        fontSize = 15.sp,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 8.dp)
                            .alpha(if (debouncedLoading) 0.38f else 1f),
                    )
                    TextButton(
                        onClick = { updateTinSync() },
                        enabled = !debouncedLoading,
                        contentPadding = PaddingValues(8.dp, 3.dp),
                        modifier = Modifier.heightIn(28.dp, 28.dp)
                    ) {
                        Text(
                            text = "Fix/Update Tin Sync Quantity",
                            modifier = Modifier,
                            fontSize = 15.sp,
                        )
                    }
                    TextButton(
                        onClick = { optimizeDatabase() },
                        enabled = !debouncedLoading,
                        contentPadding = PaddingValues(8.dp, 3.dp),
                        modifier = Modifier.heightIn(28.dp, 28.dp)
                    ) {
                        Text(
                            text = "Clean and Optimize Database",
                            fontSize = 15.sp,
                        )
                    }
                }
                if (debouncedLoading) { LoadingIndicator(center = true, modifier = Modifier.matchParentSize()) }
            }
        },
        confirmButton = { TextButton({ onDismiss() }, enabled = !debouncedLoading) { Text("Done") } },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}