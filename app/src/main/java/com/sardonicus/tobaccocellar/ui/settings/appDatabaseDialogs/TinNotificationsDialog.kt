package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialogDefaults.DisplayModeToggle
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.sardonicus.tobaccocellar.CellarApplication
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinNotificationsDialog (
    onDismiss: () -> Unit,
    tinNotifications: Boolean,
    notificationTime: Int,
    onSave: (Boolean, Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val notificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onSave(it, notificationTime) }
    var launchedFromApp by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        if (launchedFromApp) {
            val isReady = checkNotificationPermission(notificationManager, context)
            if (isReady) { onSave(true, notificationTime) }
            launchedFromApp = false
        }
        onPauseOrDispose { }
    }

    DisposableEffect(Unit) { onDispose { focusManager.clearFocus() } }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        ),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Show a notification whenever tins are ready. Notifications are local, " +
                            "they do not require internet access. Check that notifications for " +
                            "the app are enabled in your device settings.",
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notifications:",
                        modifier = Modifier,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                        Switch(
                            checked = tinNotifications,
                            onCheckedChange = {
                                if (it) {
                                    if (requestNotificationPermission(
                                            context, notificationManager, permissionLauncher)
                                        ) { onSave(true, notificationTime) }
                                    else {
                                        val isRuntime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                        } else true
                                        if (isRuntime) { launchedFromApp = true }
                                    }
                                }
                                else onSave(false, notificationTime)
                            },
                            modifier = Modifier.scale(.6f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val formattedTime = formatTime(notificationTime, context)
                    Text(
                        text = "Notification time:",
                        modifier = Modifier,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formattedTime.ifBlank { "Set Time" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(25))
                            .clickable { showTimePicker = true }
                            .padding(8.dp, 3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showTimePicker) {
                val pickerState = rememberTimePickerState(
                    initialHour = notificationTime / 60,
                    initialMinute = notificationTime % 60,
                    is24Hour = android.text.format.DateFormat.is24HourFormat(context)
                )
                var displayMode by remember { mutableStateOf(TimePickerDisplayMode.Picker) }

                CustomTimePickerDialog(
                    onDismiss = { showTimePicker = false },
                    pickerState = pickerState,
                    displayMode = displayMode,
                    onTimeChange = { onSave(tinNotifications, it) },
                    onDisplayModeChange = { displayMode = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) { Text("Done") }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}


fun requestNotificationPermission(
    context: Context,
    notificationManager: NotificationManager,
    permissionLauncher: ActivityResultLauncher<String>
): Boolean {
    val isRuntime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isRuntime) {
        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        return false
    } else {
        val appDisabled = !notificationManager.areNotificationsEnabled() // overall notifications disabled
        val channelDisabled = notificationManager.getNotificationChannel(CellarApplication.TIN_NOTIFICATION)?.importance == NotificationManager.IMPORTANCE_NONE

        if (appDisabled) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName) }
            context.startActivity(intent)
            return false
        } else if (channelDisabled) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, CellarApplication.TIN_NOTIFICATION) }
            context.startActivity(intent)
            return false
        }
    }
    return true
}

fun checkNotificationPermission(
    notificationManager: NotificationManager,
    context: Context
): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            return false
    }
    if (!notificationManager.areNotificationsEnabled()) return false

    val channel = notificationManager.getNotificationChannel(CellarApplication.TIN_NOTIFICATION)

    return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
}

private fun formatTime(time: Int?, context: Context): String {
    if (time == null) return ""

    val raw = LocalTime.ofSecondOfDay(time.toLong() * 60)
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)

    val skeleton = if (is24Hour) "Hm" else "hm"
    val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

    return raw.format(formatter)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTimePickerDialog(
    onDismiss: () -> Unit,
    pickerState: TimePickerState,
    displayMode: TimePickerDisplayMode,
    onTimeChange: (Int) -> Unit,
    onDisplayModeChange: (TimePickerDisplayMode) -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.width(IntrinsicSize.Min),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier, Alignment.Center) {
                    if (displayMode == TimePickerDisplayMode.Input) {
                        TimeInput(
                            state = pickerState,
                            colors = TimePickerDefaults.colors()
                        )
                    } else {
                        TimePicker(
                            state = pickerState,
                            colors = TimePickerDefaults.colors()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DisplayModeToggle(
                        onDisplayModeChange = {
                            onDisplayModeChange(
                                if (displayMode == TimePickerDisplayMode.Picker) { TimePickerDisplayMode.Input }
                                else TimePickerDisplayMode.Picker
                            )
                        },
                        displayMode = displayMode,
                        modifier = Modifier
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.Top
                    ) {
                        TextButton(
                            onClick = { onDismiss() },
                            contentPadding = PaddingValues(12.dp, 4.dp),
                            modifier = Modifier
                                .heightIn(32.dp, 32.dp)
                        ) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                onTimeChange((pickerState.hour * 60) + pickerState.minute)
                                onDismiss()
                            },
                            contentPadding = PaddingValues(12.dp, 4.dp),
                            modifier = Modifier.heightIn(32.dp, 32.dp)
                        ) { Text("OK") }
                    }
                }
            }
        }
    }
}