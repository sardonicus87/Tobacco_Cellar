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
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
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

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        container = AppDataContainer(this)
        preferencesRepo = PreferencesRepo(dataStore, applicationScope)
        csvHelper = CsvHelper()

        applicationScope.launch(Dispatchers.Default) {
            if (!preferencesRepo.syncSettingsMigrated.first()) {
                migrateSyncSettings()
            }

            filterViewModel
        }

        // Check Network Flow and trigger upload if possible
        applicationScope.launch(Dispatchers.Default) {
            if (preferencesRepo.crossDeviceSync.first()) {
                val networkMonitor = NetworkMonitor(this@CellarApplication)
                val itemsRepository = container.itemsRepository

                if (itemsRepository.hasPendingOperations()) {
                    val networkCheckFlow = combine(
                        networkMonitor.isWifi,
                        networkMonitor.isConnected,
                        preferencesRepo.allowMobileData
                    ) { isWifi, isConnected, allowMobile ->
                        isWifi || (isConnected && allowMobile)
                    }

                    networkCheckFlow.distinctUntilChanged().collect { canUpload ->
                        if (canUpload) {
                            itemsRepository.triggerUploadWorker()
                        }
                    }
                }
            }
        }

        // Periodic Worker
        applicationScope.launch(Dispatchers.Default) {
            val syncEnabled = preferencesRepo.crossDeviceSync.first()
            if (syncEnabled) {
                val workManager = WorkManager.getInstance(this@CellarApplication)
                val mobileEnabled = preferencesRepo.allowMobileData.first()
                val networkType =
                    if (mobileEnabled) NetworkType.CONNECTED else NetworkType.UNMETERED

                // Periodic Worker
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(networkType)
                    .build()

                val downloadWorkRequest =
                    PeriodicWorkRequestBuilder<DownloadSyncWorker>(12, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .addTag("periodic worker")
                        .build()

                workManager.enqueueUniquePeriodicWork(
                    "download_sync_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    downloadWorkRequest
                )

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
    }


    private suspend fun migrateSyncSettings() {
        SyncStateManager.loggingPaused = true
        SyncStateManager.schedulingPaused = true

        val itemsRepo = container.itemsRepository
        val allItems = itemsRepo.getEverythingStream().first()

        for (fullItem in allItems) {
            val item = fullItem.items
            val oldSyncState = preferencesRepo.getItemSyncState(item.id).first()
            if (item.syncTins != oldSyncState) {
                itemsRepo.updateItem(item.copy(syncTins = oldSyncState))
            }
        }

        SyncStateManager.loggingPaused = false
        SyncStateManager.schedulingPaused = false
        preferencesRepo.setSyncSettingsMigrated()
    }

    private suspend fun verifySyncStatus() {
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
                        // server responds, handshake failure or rejected, connection safe
                        is GoogleJsonResponseException ->
                            e.statusCode == 401 || e.statusCode == 403
                        // User potentially unlinked app through Google account settings or
                        // something else where Google Play Services/Auth Service says no
                        is GoogleAuthIOException ->
                            e is UserRecoverableAuthIOException
                        else -> false // any other failure (e.g. no internet connection)
                    }

                    if (authError) {
                        resetSyncState()
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
        Toast.makeText(this@CellarApplication, "Sync disabled, please sign in again.", Toast.LENGTH_SHORT).show()
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // cold/warm start check for new files
        if (savedInstanceState == null) {
            applicationScope.launch(Dispatchers.Default) {
                val syncEnabled = preferencesRepo.crossDeviceSync.first()

                if (syncEnabled) {
                    val workManager = WorkManager.getInstance(this@CellarApplication)
                    val allowMobile = preferencesRepo.allowMobileData.first()
                    val networkType =
                        if (allowMobile) NetworkType.CONNECTED else NetworkType.UNMETERED

                    val onStartWorkRequest = OneTimeWorkRequestBuilder<DownloadSyncWorker>()
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .build()
                        )
                        .addTag("cold/warm start check")
                        .build()

                    workManager.enqueue(onStartWorkRequest)
                }
            }
        }
    }
    override fun onActivityStarted(activity: Activity) { // hot starts
        applicationScope.launch(Dispatchers.Default) {
            if (preferencesRepo.crossDeviceSync.first()) {
                val tenMinutes: Long = 10 * 60 * 1000
                if (System.currentTimeMillis() - lastSyncVerification > tenMinutes) {
                    lastSyncVerification = System.currentTimeMillis()
                    verifySyncStatus()
                }
            }
        }
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

}
