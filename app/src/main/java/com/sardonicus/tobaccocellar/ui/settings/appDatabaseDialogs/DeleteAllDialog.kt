package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R

@Composable
fun DeleteAllDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.delete_all)) },
        text = {
            Text(
                text = stringResource(R.string.delete_all_question),
                fontSize = 15.sp
            )
        },
        dismissButton = { TextButton(onClick = onDeleteCancel) { Text(stringResource(R.string.cancel)) } },
        confirmButton = { TextButton(onClick = onDeleteConfirm) { Text(stringResource(R.string.yes)) } },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}