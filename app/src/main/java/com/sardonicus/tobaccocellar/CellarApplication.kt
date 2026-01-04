package com.sardonicus.tobaccocellar

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sardonicus.tobaccocellar.data.AppContainer
import com.sardonicus.tobaccocellar.data.AppDataContainer
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.multiDeviceSync.DownloadSyncWorker
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.settings.SyncDownloadEvent
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val VIEW_PREFERENCE_NAME = "view_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = VIEW_PREFERENCE_NAME
)

class CellarApplication : Application() {
    lateinit var container: AppContainer
    lateinit var preferencesRepo: PreferencesRepo
    lateinit var csvHelper: CsvHelper
    val filterViewModel: FilterViewModel by lazy {
        FilterViewModel(container.itemsRepository, preferencesRepo)
    }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        preferencesRepo = PreferencesRepo(dataStore, applicationScope)
        csvHelper = CsvHelper()

        migrateSyncSettings()

        // Network Flow and trigger upload if can upload
        applicationScope.launch {
            val networkMonitor = NetworkMonitor(this@CellarApplication)
            val itemsRepository = container.itemsRepository

            val networkCheckFlow = combine(
                networkMonitor.isWifi,
                networkMonitor.isConnected,
                preferencesRepo.allowMobileData
            ) { isWifi, isConnected, allowMobile ->
                isWifi || (isConnected && allowMobile)
            }

            networkCheckFlow.distinctUntilChanged().collect { canUpload ->
                if (canUpload) {
                    if (itemsRepository.hasPendingOperations()) {
                        itemsRepository.triggerUploadWorker()
                    }
                }
            }
        }

        // Workers
        applicationScope.launch {
            val workManager = WorkManager.getInstance(this@CellarApplication)

            val allowMobile = preferencesRepo.allowMobileData.first()
            val networkType = if (allowMobile) NetworkType.CONNECTED else NetworkType.UNMETERED
            val onStartWorkRequest = OneTimeWorkRequestBuilder<DownloadSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(networkType)
                        .build()
                )
                .build()

            workManager.enqueue(onStartWorkRequest)

            // Periodic Worker
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downloadWorkRequest = PeriodicWorkRequestBuilder<DownloadSyncWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "download_sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                downloadWorkRequest
            )

            workManager.getWorkInfoByIdFlow(downloadWorkRequest.id)
                .collect { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val result = workInfo.outputData
                            when (result.getString(DownloadSyncWorker.RESULT_KEY)) {
                                DownloadSyncWorker.SYNC_COMPLETE -> {
                                    EventBus.emit(SyncDownloadEvent)
                                }
                            }
                        }
                        else -> {}
                    }
                }
        }
    }

    private fun migrateSyncSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            if (preferencesRepo.syncSettingsMigrated.first()) {
                return@launch
            }

            SyncStateManager.loggingPaused = true
            SyncStateManager.schedulingPaused = true

            val itemsRepo = container.itemsRepository
            val allItemIds = itemsRepo.getAllItemIds()

            for (id in allItemIds) {
                val oldSyncState = preferencesRepo.getItemSyncState(id).first()
                val item = itemsRepo.getItemDetailsStream(id).first()?.items
                if (item != null && item.syncTins != oldSyncState) {
                    itemsRepo.updateItem(item.copy(syncTins = oldSyncState))
                }
            }

            SyncStateManager.loggingPaused = false
            SyncStateManager.schedulingPaused = false
            preferencesRepo.setSyncSettingsMigrated()
        }
    }
}
