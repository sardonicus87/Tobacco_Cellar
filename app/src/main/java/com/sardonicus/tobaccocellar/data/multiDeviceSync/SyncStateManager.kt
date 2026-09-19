package com.sardonicus.tobaccocellar.data.multiDeviceSync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SyncStateManager {
    var loggingPaused = false
    var schedulingPaused = false

    private val _activeSyncTasks = MutableStateFlow(0)
    private val _isBusy = MutableStateFlow(false)
    val isSyncing = _isBusy.asStateFlow()

    fun started() { _activeSyncTasks.value++; _isBusy.value = true }
    fun finished() {
        if (_activeSyncTasks.value > 0) _activeSyncTasks.value--
        _isBusy.value = _activeSyncTasks.value > 0
    }
}