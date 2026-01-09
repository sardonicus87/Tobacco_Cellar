package com.sardonicus.tobaccocellar

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.Toast
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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.sardonicus.tobaccocellar.data.AppContainer
import com.sardonicus.tobaccocellar.data.AppDataContainer
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.multiDeviceSync.DownloadSyncWorker
import com.sardonicus.tobaccocellar.data.multiDeviceSync.GoogleDriveServiceHelper
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
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val VIEW_PREFERENCE_NAME = "view_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = VIEW_PREFERENCE_NAME
)

class CellarApplication : Application(), Application.ActivityLifecycleCallbacks {
    lateinit var container: AppContainer
    lateinit var preferencesRepo: PreferencesRepo
    lateinit var csvHelper: CsvHelper
    val filterViewModel: FilterViewModel by lazy { FilterViewModel(container.itemsRepository, preferencesRepo) }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastSyncVerification: Long = 0

//    private var startWorkEnqueued = false


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        container = AppDataContainer(this)
        preferencesRepo = PreferencesRepo(dataStore, applicationScope)
        csvHelper = CsvHelper()

        migrateSyncSettings()

        // Network Flow and trigger upload if can upload
        applicationScope.launch {
            val networkMonitor = NetworkMonitor(this@CellarApplication)
            val itemsRepository = container.itemsRepository
            val syncEnabled = preferencesRepo.crossDeviceSync.first()

            val networkCheckFlow = combine(
                networkMonitor.isWifi,
                networkMonitor.isConnected,
                preferencesRepo.allowMobileData
            ) { isWifi, isConnected, allowMobile ->
                isWifi || (isConnected && allowMobile)
            }

            networkCheckFlow.distinctUntilChanged().collect { canUpload ->
                if (canUpload && syncEnabled) {
                    if (itemsRepository.hasPendingOperations()) {
                        itemsRepository.triggerUploadWorker()
                    }
                }
            }
        }

        // Periodic Worker
        applicationScope.launch {
            val workManager = WorkManager.getInstance(this@CellarApplication)
            val syncEnabled = preferencesRepo.crossDeviceSync.first()

            // Periodic Worker
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downloadWorkRequest = PeriodicWorkRequestBuilder<DownloadSyncWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            if (syncEnabled) {
                workManager.enqueueUniquePeriodicWork(
                    "download_sync_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    downloadWorkRequest
                )
            }

            // Refresh Db flow when there's a download
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

    private fun verifySyncStatus() {
        applicationScope.launch {
            if (preferencesRepo.crossDeviceSync.first()) {
                val userEmail = preferencesRepo.signedInUserEmail.first()
                if (userEmail != null) {
                    withContext(Dispatchers.IO) {
                        try {
                            val driveService = GoogleDriveServiceHelper.getDriveService(this@CellarApplication, userEmail)

                            driveService.files().list()
                                .setSpaces("appDataFolder")
                                .setPageSize(1)
                                .setFields("files(id)")
                                .execute()
                        } catch (e: Exception) {
                            val authError = when (e) {
                                is GoogleJsonResponseException ->
                                    e.statusCode == 401 || e.statusCode == 403
                                is GoogleAuthIOException -> true
                                else -> false
                            }

                            if (authError) {
                                resetSyncState()
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun resetSyncState() {
        preferencesRepo.saveCrossDeviceSync(false)
        preferencesRepo.clearLoginState()

        val workManager = WorkManager.getInstance(this@CellarApplication)
        workManager.cancelUniqueWork("download_sync_work")
        Toast.makeText(this@CellarApplication, "Sync disabled.", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // cold/warm start check for new files
        if (savedInstanceState == null) {  // && !startWorkEnqueued
            applicationScope.launch {
                val workManager = WorkManager.getInstance(this@CellarApplication)
                val syncEnabled = preferencesRepo.crossDeviceSync.first()


                if (syncEnabled) {
                    val allowMobile = preferencesRepo.allowMobileData.first()
                    val networkType =
                        if (allowMobile) NetworkType.CONNECTED else NetworkType.UNMETERED

                    val onStartWorkRequest = OneTimeWorkRequestBuilder<DownloadSyncWorker>()
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .build()
                        )
                        .build()

                    workManager.enqueue(onStartWorkRequest)
                    // startWorkEnqueued
                }
            }
        }
    }
    override fun onActivityStarted(activity: Activity) {
        // hot starts
        val tenMinutes: Long = 10 * 60 * 1000
        if (System.currentTimeMillis() - lastSyncVerification > tenMinutes) {
            lastSyncVerification = System.currentTimeMillis()
            verifySyncStatus()
        }
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

}
