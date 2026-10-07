package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DeviceSyncDialog(
    onDismiss: () -> Unit,
    loading: Boolean,
    acknowledgement: Boolean,
    connectionEnabled: Boolean,
    confirmAcknowledgement: () -> Unit,
    deviceSync: Boolean,
    signingIn: Boolean,
    onDeviceSync: (Boolean) -> Unit,
    email: String?,
    hasScope: Boolean,
    allowMobileData: Boolean,
    onAllowMobileData: (Boolean) -> Unit,
    onManualSync: () -> Unit,
    clearRemoteData: () -> Unit,
    clearLoginState: () -> Unit
) {
    val accountLinked by remember (email, hasScope) { mutableStateOf(!email.isNullOrBlank() || hasScope) }

    val scrollState = rememberScrollState()
    var atBottom by remember { mutableStateOf(false) }
    val scrolled by remember { derivedStateOf { !scrollState.canScrollForward } }
    if (scrolled) { atBottom = true }
    val density = LocalDensity.current
    val checkOffset = remember { with(density) { 14.sp.toDp() } }

    var debouncedLoading by remember { mutableStateOf(false) }
    var disconnectFailure by remember { mutableStateOf(false) }

    LaunchedEffect(loading) {
        if (loading) {
            delay(50.milliseconds)
            debouncedLoading = true
        } else { debouncedLoading = false }
    }

    LaunchedEffect(disconnectFailure, connectionEnabled) {
        if (disconnectFailure) {
            snapshotFlow { connectionEnabled }.collect {
                if (it) {
                    delay(1000.milliseconds)
                    disconnectFailure = false
                    onDeviceSync(true)
                }
            }
        }
    }

    val context = LocalContext.current
    val notificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var syncNotificationEnabled by remember { mutableStateOf(checkNotificationPermission(notificationManager, context, CellarApplication.SYNC_NOTIFICATION)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var launchedFromApp by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        if (launchedFromApp) {
            val isReady = checkNotificationPermission(notificationManager, context, CellarApplication.SYNC_NOTIFICATION)
            if (isReady) { syncNotificationEnabled = true }
            launchedFromApp = false
        }
        onPauseOrDispose { }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier.padding(0.dp).heightIn(max = 350.dp),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!acknowledgement) {
                    Text(
                        text = stringResource(R.string.multi_device_acknowledge_title),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalContentColor.current
                    )
                    Column(
                        modifier = Modifier.verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.scroll_to_accept),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 13.sp,
                            color = LocalContentColor.current
                        )
                        Text(stringResource(R.string.multi_device_acknowledge_body1),
                            fontSize = 14.sp, color = LocalContentColor.current)
                        Text(stringResource(R.string.multi_device_acknowledge_body2),
                            fontSize = 14.sp, color = LocalContentColor.current)
                        Text(stringResource(R.string.multi_device_acknowledge_body3),
                            fontSize = 14.sp, color = LocalContentColor.current)
                        Text(stringResource(R.string.multi_device_acknowledge_body4),
                            fontSize = 14.sp, color = LocalContentColor.current)
                    }
                } else {
                    Box {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))
                            // Enable Sync
                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(28.dp)
                                        .padding(start = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.multi_device_sync_label),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LocalContentColor.current.copy(alpha = if (debouncedLoading) .38f else 1f)
                                    )
                                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                                        Switch(
                                            checked = deviceSync || signingIn || disconnectFailure,
                                            onCheckedChange = {
                                                if (!connectionEnabled && !deviceSync) { disconnectFailure = it }
                                                else { onDeviceSync(it) }

                                                if (it && !signingIn && !disconnectFailure && !syncNotificationEnabled) {
                                                    val isRuntime = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                                    if (isRuntime) { launchedFromApp = true }
                                                    requestNotificationPermission(context, notificationManager, permissionLauncher, CellarApplication.SYNC_NOTIFICATION)
                                                }
                                            },
                                            modifier = Modifier
                                                .scale(.6f)
                                                .padding(start = 10.dp),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = if (deviceSync && connectionEnabled) MaterialTheme.colorScheme.onPrimary else if (!connectionEnabled) LocalCustomColors.current.favHeart else Color.Transparent,
                                                checkedTrackColor = if (deviceSync && connectionEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                checkedBorderColor = if ((deviceSync && !connectionEnabled) || disconnectFailure) MaterialTheme.colorScheme.outline else Color.Transparent
                                            ),
                                            enabled = !debouncedLoading,
                                            thumbContent = if (signingIn && !deviceSync) {
                                                {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier
                                                            .padding(0.dp)
                                                            .fillMaxSize()
                                                            .background(
                                                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                                                CircleShape
                                                            ),
                                                        strokeWidth = 3.dp
                                                    )
                                                }
                                            } else if ((deviceSync && !connectionEnabled) || disconnectFailure) {
                                                {
                                                    Icon(
                                                        painter = painterResource(R.drawable.close),
                                                        contentDescription = null,
                                                        tint = Color.White
                                                    )
                                                }
                                            } else null
                                        )
                                    }
                                }
                                if ((deviceSync && !connectionEnabled) || disconnectFailure) {
                                    Text(
                                        text = stringResource(R.string.multi_device_check_connection),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 1.em,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = -(checkOffset + 8.dp)),
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = .75f),
                                    )
                                }
                            }

                            // Allow Mobile
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .padding(start = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val alpha = if (!deviceSync || debouncedLoading) .38f else 1f
                                Text(
                                    text = stringResource(R.string.multi_device_allow_mobile),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalContentColor.current.copy(alpha = alpha)
                                )
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                                    Switch(
                                        checked = allowMobileData,
                                        onCheckedChange = { onAllowMobileData(it) },
                                        enabled = deviceSync && !debouncedLoading,
                                        modifier = Modifier.scale(.6f).padding(start = 10.dp),
                                        colors = SwitchDefaults.colors()
                                    )
                                }
                            }

                            // Manual Sync
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.width(IntrinsicSize.Max)
                                ) {
                                    TextButton(
                                        onClick = { onManualSync() },
                                        enabled = deviceSync && accountLinked && connectionEnabled && !debouncedLoading,
                                        contentPadding = PaddingValues(8.dp, 3.dp),
                                        modifier = Modifier.heightIn(28.dp, 28.dp)
                                    ) { Text(stringResource(R.string.multi_device_manual_sync), fontSize = 15.sp) }

                                    // Clear remote data
                                    TextButton(
                                        onClick = { clearRemoteData() },
                                        enabled = accountLinked && connectionEnabled && !debouncedLoading,
                                        contentPadding = PaddingValues(8.dp, 3.dp),
                                        modifier = Modifier.heightIn(28.dp, 28.dp)
                                    ) { Text(stringResource(R.string.multi_device_clear_remote), fontSize = 15.sp, maxLines = 1) }
                                }
                            }

                            // Clear Login
                            TextButton(
                                onClick = { clearLoginState(); onDeviceSync(false) },
                                enabled = accountLinked && !debouncedLoading,
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = Modifier.heightIn(28.dp, 28.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.multi_device_sign_out),
                                    fontSize = 15.sp,
                                    modifier = Modifier.alpha(if (accountLinked) 1f else 0f)
                                )
                            }

                            // notification
                            if (deviceSync&& !signingIn && !disconnectFailure && !syncNotificationEnabled) {
                                TextButton(
                                    onClick = {
                                        launchedFromApp = true
                                        requestNotificationPermission(context, notificationManager, permissionLauncher, CellarApplication.SYNC_NOTIFICATION) },
                                    enabled = accountLinked && !debouncedLoading,
                                    contentPadding = PaddingValues(8.dp, 3.dp),
                                    modifier = Modifier.heightIn(28.dp, 28.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.multi_device_enable_notification),
                                        fontSize = 15.sp,
                                        modifier = Modifier.alpha(if (accountLinked) 1f else 0f)
                                    )
                                }
                            }
                        }
                        if (debouncedLoading) { LoadingIndicator(Modifier.matchParentSize(), center = true) }
                    }
                }
            }
        },
        confirmButton = {
            if (!acknowledgement) {
                TextButton({ confirmAcknowledgement() }, enabled = atBottom) { Text(stringResource(R.string.agree)) }
            }
            else { TextButton({ onDismiss() }, enabled = !debouncedLoading) { Text(stringResource(R.string.done)) } }
        },
        dismissButton =
            if (!acknowledgement) { { TextButton({ onDismiss() }) { Text(stringResource(R.string.cancel)) } } }
            else null,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}