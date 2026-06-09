package com.sardonicus.tobaccocellar.data.multiDeviceSync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

object SyncStateManager {
    var loggingPaused = false
    var schedulingPaused = false

    private val _activeSyncTasks = MutableStateFlow(0)
    val isSyncing = _activeSyncTasks.map { it > 0 }
    fun started() { _activeSyncTasks.value++ }
    fun finished() {
        val current = _activeSyncTasks.value
        if (current > 0) _activeSyncTasks.value--
    }
}