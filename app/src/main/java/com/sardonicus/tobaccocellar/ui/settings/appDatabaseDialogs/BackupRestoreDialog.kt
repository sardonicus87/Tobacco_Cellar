package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.composables.CheckboxWithLabel
import com.sardonicus.tobaccocellar.ui.settings.BackupState
import com.sardonicus.tobaccocellar.ui.settings.RestoreState
import com.sardonicus.tobaccocellar.ui.settings.SettingsViewModel

@Composable
fun BackupRestoreDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onRestore: () -> Unit,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var option: String? by rememberSaveable { mutableStateOf(null) }
    val updateOption: (String?) -> Unit = { option = it }

    val backupState by viewmodel.backupState.collectAsState()
    val restoreState by viewmodel.restoreState.collectAsState()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        text = {
            when (option) {
                "Backup" -> BackupDialog(backupState, viewmodel)
                "Restore" -> RestoreDialog(restoreState, viewmodel)
                null -> {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Would you like to Backup or Restore?",
                            fontSize = 15.sp,
                            color = LocalContentColor.current
                        )
                        Column(
                            modifier = Modifier
                                // .padding(start = 16.dp)
                                .fillMaxWidth()
                            ,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { updateOption("Backup") },
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = modifier
                                    .heightIn(28.dp, 28.dp)
                            ) {
                                Text(
                                    text = "Backup",
                                    fontSize = 15.sp
                                )
                            }
                            TextButton(
                                onClick = { updateOption("Restore") },
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = modifier
                                    .heightIn(28.dp, 28.dp)
                            ) {
                                Text(
                                    text = "Restore",
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            when (option) {
                "Backup" -> {
                    TextButton(
                        onClick = {
                            val suggestedFilename = backupState.suggestedFilename
                            onSave(suggestedFilename)
                        },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = backupState.databaseChecked || backupState.settingsChecked
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
                "Restore" -> {
                    TextButton(
                        onClick = { onRestore() },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = restoreState.databaseChecked || restoreState.settingsChecked
                    ) {
                        Text(text = "Open")
                    }
                }
                null -> { }
            }

        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )

}

@Composable
private fun BackupDialog(
    backupState: BackupState,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    Column (
        modifier = modifier
            .padding(bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Select what to backup. If you check both boxes, a single file will " +
                    "be created that holds both. The restore function can optionally " +
                    "restore either the database or the settings from the joint backup " +
                    "file.",
            modifier = Modifier
                .padding(bottom = 12.dp),
            fontSize = 15.sp,
            color = LocalContentColor.current
        )
        CheckboxWithLabel(
            text = "Database",
            checked = backupState.databaseChecked,
            onCheckedChange = {
                viewmodel.onBackupOptionChanged(
                    backupState.copy(databaseChecked = it)
                )
            },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
        CheckboxWithLabel(
            text = "Settings",
            checked = backupState.settingsChecked,
            onCheckedChange = {
                viewmodel.onBackupOptionChanged(
                    backupState.copy(settingsChecked = it)
                )
            },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
    }
}

@Composable
private fun RestoreDialog(
    restoreState: RestoreState,
    viewmodel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    Column (
        modifier = modifier
            .padding(bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Select what to restore.\n\nWARNING: Restore will overwrite any existing " +
                    "settings and/or database data (depending on which is selected). " +
                    "Either or both can be restored from a joint database/settings backup " +
                    "file.",
            modifier = Modifier
                .padding(bottom = 12.dp),
            fontSize = 15.sp,
            color = LocalContentColor.current
        )
        CheckboxWithLabel(
            text = "Database",
            checked = restoreState.databaseChecked,
            onCheckedChange = {
                viewmodel.onRestoreOptionChanged(
                    restoreState.copy(databaseChecked = it)
                ) },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
        CheckboxWithLabel(
            text = "Settings",
            checked = restoreState.settingsChecked,
            onCheckedChange = {
                viewmodel.onRestoreOptionChanged(
                    restoreState.copy(settingsChecked = it)
                )
            },
            modifier = Modifier
                .padding(bottom = 0.dp),
        )
    }
}