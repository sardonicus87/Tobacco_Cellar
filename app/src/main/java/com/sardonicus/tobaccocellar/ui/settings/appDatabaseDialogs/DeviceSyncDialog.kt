package com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
    var atBottom by rememberSaveable { mutableStateOf(false) }
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

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .padding(0.dp)
            .heightIn(max = 350.dp),
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!acknowledgement) {
                    Text(
                        text = "About Multi Device Sync",
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
                            text = "(You must scroll to the bottom to accept)",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 13.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "To auto-synchronize collection changes across devices, you " +
                                    "must enable this option and sign-in with the same Google " +
                                    "account on each device you want sync'ed. This will also " +
                                    "authorize the app to have Google Drive access (you do not " +
                                    "need the Google Drive app for this functionality to work). " +
                                    "This feature also requires all synced devices to be " +
                                    "running a version of the app with the same Database Version.",
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Developer and/or any third parties will not have access to " +
                                    "your login or drive. This authorization just allows the app " +
                                    "to use your Google Drive as a cloud location for storing and " +
                                    "retrieving data changes between devices. The app will create " +
                                    "a hidden folder on your Google Drive, and this folder is the " +
                                    "only part of your Drive that the app can access. Login and " +
                                    "remote sync data can be cleared at any time in this setting " +
                                    "dialog (clear remote data before clearing login). If you " +
                                    "wish to revoke Drive authorization, this must be done in " +
                                    "your Google Account settings (Services → Connected Apps).",
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Sync data does not count toward your Google Drive storage " +
                                    "quota. Data is checked once at every app start (including " +
                                    "\"cold starts\" if it has been at least one hour since " +
                                    "initial app start), and cyclically once every 12 hours as " +
                                    "long as the device is powered on and connected. All data " +
                                    "checks respect your settings regarding mobile data or WIFI " +
                                    "only (will never use mobile data unless enabled).",
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "It is recommended to create a manual database backup of the " +
                                    "device with the most up-to-date information, then transfer " +
                                    "to and restore on the other device(s) before enabling this " +
                                    "setting.",
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
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
                                        text = "Multi-Device Sync:",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LocalContentColor.current.copy(alpha = if (debouncedLoading) .38f else 1f)
                                    )
                                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                                        Switch(
                                            checked = deviceSync || signingIn || disconnectFailure,
                                            onCheckedChange = {
                                                if (!connectionEnabled && !deviceSync) {
                                                    disconnectFailure = it
                                                } else {
                                                    onDeviceSync(it)
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
                                        text = "(Check Connection)",
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
                                    text = "Allow Mobile Data:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalContentColor.current.copy(alpha = alpha)
                                )
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                                    Switch(
                                        checked = allowMobileData,
                                        onCheckedChange = { onAllowMobileData(it) },
                                        enabled = deviceSync && !debouncedLoading,
                                        modifier = Modifier
                                            .scale(.6f)
                                            .padding(start = 10.dp),
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
                                    modifier = Modifier
                                        .width(IntrinsicSize.Max)
                                ) {
                                    TextButton(
                                        onClick = { onManualSync() },
                                        enabled = deviceSync && accountLinked && connectionEnabled && !debouncedLoading,
                                        contentPadding = PaddingValues(8.dp, 3.dp),
                                        modifier = Modifier.heightIn(28.dp, 28.dp)
                                    ) {
                                        Text(
                                            text = "Manual Sync",
                                            fontSize = 15.sp,
                                        )
                                    }

                                    // Clear remote data
                                    TextButton(
                                        onClick = { clearRemoteData() },
                                        enabled = accountLinked && connectionEnabled && !debouncedLoading,
                                        contentPadding = PaddingValues(8.dp, 3.dp),
                                        modifier = Modifier.heightIn(28.dp, 28.dp)
                                    ) {
                                        Text(
                                            text = "Clear Remote Data",
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Clear Login
                            TextButton(
                                onClick = {
                                    clearLoginState()
                                    onDeviceSync(false)
                                },
                                enabled = accountLinked && !debouncedLoading,
                                contentPadding = PaddingValues(8.dp, 3.dp),
                                modifier = Modifier.heightIn(28.dp, 28.dp)
                            ) {
                                Text(
                                    text = "Sign-Out",
                                    fontSize = 15.sp,
                                    modifier = Modifier.alpha(if (accountLinked) 1f else 0f)
                                )
                            }
                        }
                        if (debouncedLoading) { LoadingIndicator(center = true, modifier = Modifier.matchParentSize()) }
                    }
                }
            }
        },
        confirmButton = {
            if (!acknowledgement) {
                TextButton(
                    onClick = { confirmAcknowledgement() },
                    enabled = atBottom
                ) { Text("Agree") }
            }
            else { TextButton({ onDismiss() }, enabled = !debouncedLoading) { Text("Done") } }
        },
        dismissButton =
            if (!acknowledgement) { { TextButton({ onDismiss() }) { Text("Cancel") } } }
            else null,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}