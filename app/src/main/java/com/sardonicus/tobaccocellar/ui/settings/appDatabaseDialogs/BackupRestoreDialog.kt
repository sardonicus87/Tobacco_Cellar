package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import androidx.activity.compose.BackHandler
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
    viewmodel: SettingsViewModel
) {
    var option: BackupRestoreOption? by rememberSaveable { mutableStateOf(null) }
    val updateOption: (BackupRestoreOption?) -> Unit = { option = it }

    val backupState by viewmodel.backupState.collectAsState()
    val restoreState by viewmodel.restoreState.collectAsState()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        text = {
            when (option) {
                BackupRestoreOption.BACKUP -> BackupDialog(backupState, viewmodel) { updateOption(null) }
                BackupRestoreOption.RESTORE -> RestoreDialog(restoreState, viewmodel) { updateOption(null) }
                null -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Would you like to Backup or Restore?",
                            fontSize = 15.sp,
                            color = LocalContentColor.current
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { updateOption(BackupRestoreOption.BACKUP) },
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = Modifier.heightIn(28.dp, 28.dp)
                            ) {
                                Text(
                                    text = "Backup",
                                    fontSize = 15.sp
                                )
                            }
                            TextButton(
                                onClick = { updateOption(BackupRestoreOption.RESTORE) },
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = Modifier.heightIn(28.dp, 28.dp)
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
        dismissButton = { TextButton({ onDismiss() }) { Text(stringResource(R.string.cancel)) } },
        confirmButton = {
            when (option) {
                BackupRestoreOption.BACKUP -> {
                    TextButton(
                        onClick = {
                            val suggestedFilename = backupState.suggestedFilename
                            onSave(suggestedFilename)
                        },
                        enabled = backupState.databaseChecked || backupState.settingsChecked
                    ) { Text(stringResource(R.string.save)) }
                }
                BackupRestoreOption.RESTORE -> {
                    TextButton(
                        onClick = { onRestore() },
                        enabled = restoreState.databaseChecked || restoreState.settingsChecked
                    ) { Text("Open") }
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
    viewModel: SettingsViewModel,
    resetState: () -> Unit,
) {
    BackHandler {
        viewModel.onBackupOptionChanged(backupState.copy(databaseChecked = false, settingsChecked = false))
        resetState()
    }
    Column (
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Select what to backup. If you check both boxes, a single file will " +
                    "be created that holds both. The restore function can optionally " +
                    "restore either the database or the settings from the joint backup " +
                    "file.",
            modifier = Modifier.padding(bottom = 12.dp),
            fontSize = 15.sp,
            color = LocalContentColor.current
        )
        CheckboxWithLabel(
            text = "Database",
            checked = backupState.databaseChecked,
            onCheckedChange = { viewModel.onBackupOptionChanged(backupState.copy(databaseChecked = !backupState.databaseChecked)) }
        )
        CheckboxWithLabel(
            text = "Settings",
            checked = backupState.settingsChecked,
            onCheckedChange = { viewModel.onBackupOptionChanged(backupState.copy(settingsChecked = !backupState.settingsChecked)) }
        )
    }
}

@Composable
private fun RestoreDialog(
    restoreState: RestoreState,
    viewModel: SettingsViewModel,
    resetState: () -> Unit,
) {
    BackHandler {
        viewModel.onRestoreOptionChanged(restoreState.copy(databaseChecked = false, settingsChecked = false))
        resetState()
    }
    Column (
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Select what to restore.\n\nWARNING: Restore will overwrite any existing " +
                    "settings and/or database data (depending on which is selected). " +
                    "Either or both can be restored from a joint database/settings backup " +
                    "file.",
            modifier = Modifier.padding(bottom = 12.dp),
            fontSize = 15.sp,
            color = LocalContentColor.current
        )
        CheckboxWithLabel(
            text = "Database",
            checked = restoreState.databaseChecked,
            onCheckedChange = { viewModel.onRestoreOptionChanged(restoreState.copy(databaseChecked = !restoreState.databaseChecked)) }
        )
        CheckboxWithLabel(
            text = "Settings",
            checked = restoreState.settingsChecked,
            onCheckedChange = { viewModel.onRestoreOptionChanged(restoreState.copy(settingsChecked = !restoreState.settingsChecked)) }
        )
    }
}

enum class BackupRestoreOption { BACKUP, RESTORE }