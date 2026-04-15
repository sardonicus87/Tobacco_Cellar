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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay

@Composable
fun DeviceSyncDialog(
    onDismiss: () -> Unit,
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
    clearLoginState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accountLinked by remember (email, hasScope) { mutableStateOf(!email.isNullOrBlank() || hasScope) }

    val scrollState = rememberScrollState()
    val atBottom by remember { derivedStateOf { !scrollState.canScrollForward } }
    val density = LocalDensity.current
    val checkOffset = remember { with(density) { 14.sp.toDp() } }

    var disconnectFailure by remember { mutableStateOf(false) }

    LaunchedEffect(disconnectFailure, connectionEnabled) {
        if (disconnectFailure) {
            snapshotFlow { connectionEnabled }.collect {
                if (it) {
                    delay(1000)
                    disconnectFailure = false
                    onDeviceSync(true)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp)
            .heightIn(max = 350.dp),
        text = {
            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!acknowledgement) {
                    Text(
                        text = "About Multi Device Sync",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalContentColor.current
                    )
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "(You must scroll to the bottom to accept)",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            fontSize = 13.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "To auto-synchronize collection changes across devices, you " +
                                    "must enable this option and sign-in with the same Google " +
                                    "account to authorize Google Drive access on each device (you " +
                                    "do not need the Google Drive app for this functionality to " +
                                    "work). This feature also requires all synced devices to be " +
                                    "running a version of the app with the same Database Version.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "I (developer), and any third parties will not have access to " +
                                    "your login or drive, this authorization just allows the app " +
                                    "to use your Google Drive as a cloud location for storing and " +
                                    "retrieving data changes between devices. The app will create " +
                                    "a hidden folder that only this app can access, and this " +
                                    "folder is the only part of your Drive that the app can " +
                                    "access. Login and remote sync data can be cleared at any " +
                                    "time in this setting dialog (clear remote data before " +
                                    "clearing login). If you wish to revoke Drive authorization, " +
                                    "this must be done in your Google Account settings: Services " +
                                    "> Connected Apps).",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Data does not count toward your Google Drive storage quota, " +
                                    "and is checked once at every app start, and cyclically once " +
                                    "every 12 hours as long as the device is powered on. The app " +
                                    "start check and 12-hour cycled checks respect your settings " +
                                    "regarding mobile data or WIFI only.",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                        Text(
                            text = "Before enabling this option on multiple devices, it is " +
                                    "recommended to create a manual database backup of the " +
                                    "device with the most up-to-date data and transfer it to, " +
                                    "and restore on, the other device(s).",
                            modifier = Modifier,
                            fontSize = 14.sp,
                            color = LocalContentColor.current
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        // Enable Sync
                        Box {
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .padding(start = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Multi-Device Sync:",
                                    modifier = Modifier,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalContentColor.current
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
                                                    tint = Color.White,
                                                    modifier = Modifier
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
                            modifier = modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val alpha = if (!deviceSync) .38f else 1f
                            Text(
                                text = "Allow Mobile Data:",
                                modifier = Modifier,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = LocalContentColor.current.copy(alpha = alpha)
                            )
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 20.dp) {
                                Switch(
                                    checked = allowMobileData,
                                    onCheckedChange = { onAllowMobileData(it) },
                                    enabled = deviceSync,
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
                                    enabled = deviceSync && accountLinked && connectionEnabled,
                                    contentPadding = PaddingValues(8.dp, 3.dp),
                                    modifier = modifier
                                        .heightIn(28.dp, 28.dp)
                                ) {
                                    Text(
                                        text = "Manual Sync",
                                        modifier = Modifier,
                                        fontSize = 15.sp,
                                    )
                                }

                                // Clear remote data
                                TextButton(
                                    onClick = { clearRemoteData() },
                                    enabled = accountLinked && connectionEnabled,
                                    contentPadding = PaddingValues(8.dp, 3.dp),
                                    modifier = modifier
                                        .heightIn(28.dp, 28.dp)
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
                            enabled = accountLinked,
                            contentPadding = PaddingValues(8.dp, 3.dp),
                            modifier = modifier
                                .heightIn(28.dp, 28.dp)
                        ) {
                            Text(
                                text = "Sign-Out",
                                fontSize = 15.sp,
                                color = if (accountLinked) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!acknowledgement) {
                TextButton(
                    onClick = { confirmAcknowledgement() },
                    modifier = Modifier
                        .padding(0.dp),
                    enabled = atBottom
                ) {
                    Text("Agree")
                }
            } else {
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = if (!acknowledgement) {
            {
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Text("Cancel")
                }
            }
        } else null,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}