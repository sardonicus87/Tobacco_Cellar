package com.sardonicus.tobaccocellar.ui.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class NetworkMonitor(context: Context, scope: CoroutineScope) {
    private val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkStatus: Flow<NetworkCapabilities?> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities) }
            override fun onLost(network: Network) { trySend(null) }
        }

        trySend(manager.getNetworkCapabilities(manager.activeNetwork))
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged().conflate().shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)

    val isConnected: Flow<Boolean> = networkStatus
        .map { it != null }
        .distinctUntilChanged()

    val isWifi: Flow<Boolean> = networkStatus
        .map { it?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true }
        .distinctUntilChanged()
}