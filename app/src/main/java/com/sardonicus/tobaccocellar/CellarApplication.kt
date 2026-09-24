package com.sardonicus.tobaccocellar

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
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
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.checkNotificationPermission
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.ShowToast
import com.sardonicus.tobaccocellar.ui.utilities.TinNotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

private const val VIEW_PREFERENCE_NAME = "view_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(VIEW_PREFERENCE_NAME)

class CellarApplication : Application(), Application.ActivityLifecycleCallbacks {
    lateinit var container: AppContainer
    lateinit var preferencesRepo: PreferencesRepo
    lateinit var csvHelper: CsvHelper
    val filterViewModel: FilterViewModel by lazy { FilterViewModel(container.itemsRepository, preferencesRepo) }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var verificationJob: Job? = null
    private var lastSyncVerification: Long = 0

    companion object {
        const val SYNC_NOTIFICATION = "sync_channel"
        const val TIN_NOTIFICATION = "tin_notification"
    }
    private val activityResumedTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        container = AppDataContainer(this)
        preferencesRepo = PreferencesRepo(dataStore, applicationScope)
        csvHelper = CsvHelper()
        createNotificationChannel()

        applicationScope.launch(Dispatchers.Default) {
            if (!preferencesRepo.syncSettingsMigrated.first()) { migrateSyncSettings() }
            filterViewModel
        }

        // Check Network Flow and trigger upload if there are pending ops, schedule tin notification
        applicationScope.launch(Dispatchers.Default) {
            val networkMonitor = container.networkMonitor
            val notificationManager = getSystemService(NotificationManager::class.java)
            launch {
                preferencesRepo.crossDeviceSync.collectLatest { enabled ->
                    if (enabled) {
                        launch {
                            combine(
                                networkMonitor.isWifi,
                                networkMonitor.isConnected,
                                preferencesRepo.allowMobileData
                            ) { isWifi, isConnected, allowMobile ->
                                isWifi || (isConnected && allowMobile)
                            }.distinctUntilChanged().collectLatest {
                                delay(500.milliseconds)
                                if (it && container.itemsRepository.hasPendingOperations()) {
                                    container.itemsRepository.triggerUploadWorker()
                                }
                            }
                        }
                        launch {
                            preferencesRepo.allowMobileData.distinctUntilChanged()
                                .collect { enqueuePeriodicSync(it) }
                        }
                    } else { cancelPeriodicSync() }
                }
            }
            launch {
                combine(
                    preferencesRepo.tinNotifications,
                    preferencesRepo.tinNotifyTime,
                    activityResumedTrigger.onStart { emit(Unit) }
                ) { notify, time, _ ->
                    notify to time
                }.collectLatest { (notify, time) ->
                    delay(500.milliseconds)
                    val systemEnabled = checkNotificationPermission(notificationManager, this@CellarApplication)
                    if (notify && systemEnabled) { scheduleTinNotificationWorker(time) }
                    else { cancelTinNotificationWorker() }
                }
            }
        }
    }

    private fun enqueuePeriodicSync(mobileEnabled: Boolean) {
        val networkType = if (mobileEnabled) NetworkType.CONNECTED else NetworkType.UNMETERED

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val data = Data.Builder()
            .putString(DownloadSyncWorker.SYNC_TYPE_KEY, DownloadSyncWorker.SYNC_TYPE_PERIODIC)
            .build()

        val downloadWorkRequest =
            PeriodicWorkRequestBuilder<DownloadSyncWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag("periodic worker")
                .setInputData(data)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "download_sync_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            downloadWorkRequest
        )
    }

    fun periodicDownloadSetup() {
        applicationScope.launch(Dispatchers.Default) {
            val mobileEnabled = preferencesRepo.allowMobileData.first()
            enqueuePeriodicSync(mobileEnabled)
        }
    }

    fun cancelPeriodicSync() { WorkManager.getInstance(this).cancelUniqueWork("download_sync_work") }

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

    private suspend fun verifySyncStatus(context: Context) {
        val userEmail = preferencesRepo.signedInUserEmail.first()
        if (userEmail != null) {
            withContext(Dispatchers.IO) {
                try {
                    val driveService = GoogleDriveServiceHelper.getDriveService(context, userEmail)

                    driveService.files().list()
                        .setSpaces("appDataFolder")
                        .setPageSize(1)
                        .setFields("files(id)")
                        .execute()
                } catch (e: Exception) {
                    val authError = when (e) {
                        // server responds, handshake failure or rejected
                        is GoogleJsonResponseException -> e.statusCode == 401 || e.statusCode == 403
                        // Google Play Services/Auth Service says no (potentially user-unlinked app
                        // through Google account settings
                        is GoogleAuthIOException -> e is UserRecoverableAuthIOException
                        // any other failure (e.g. no internet connection)
                        else -> false
                    }

                    if (authError) { resetSyncState() }
                }
            }
        }
    }

    private suspend fun resetSyncState() {
        preferencesRepo.saveCrossDeviceSync(false)
        preferencesRepo.clearLogin()

        val workManager = WorkManager.getInstance(this@CellarApplication)
        workManager.cancelUniqueWork("download_sync_work")
        EventBus.emit(ShowToast("Sync disabled, please sign in again."))
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val syncChannel = NotificationChannel(SYNC_NOTIFICATION, "Background sync", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Quiet background notification indicating sync working in the background."
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
        }
        val tinReadyChannel = NotificationChannel(TIN_NOTIFICATION, "Tins ready", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Show a notification when a tin is ready to open."
            setShowBadge(true)
            enableVibration(true)
            enableLights(true)
        }

        notificationManager.createNotificationChannel(syncChannel)
        notificationManager.createNotificationChannel(tinReadyChannel)
    }

    fun scheduleTinNotificationWorker(time: Int) {
        val now = LocalTime.now()
        val targetTime = LocalTime.ofSecondOfDay(time.toLong() * 60)

        var delay = ChronoUnit.SECONDS.between(now, targetTime)
        if (delay <= 0) { delay += 86400 }

        val tinReadyRequest = PeriodicWorkRequestBuilder<TinNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .addTag("tin_notification")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_check_for_tins",
            ExistingPeriodicWorkPolicy.UPDATE,
            tinReadyRequest
        )
    }

    fun cancelTinNotificationWorker() {
        WorkManager.getInstance(this).cancelUniqueWork("daily_check_for_tins")
    }



    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // cold/warm start check for new files
        applicationScope.launch(Dispatchers.Default) {
            delay(500.milliseconds)
            val syncEnabled = preferencesRepo.crossDeviceSync.first()
            val syncInProgress = SyncStateManager.isSyncing.first()
            val tinsReady = activity.intent?.getBooleanExtra("FILTER_READY_TINS", false) == true

            if (syncEnabled && !syncInProgress && !tinsReady) {
                val workManager = WorkManager.getInstance(this@CellarApplication)
                val allowMobile = preferencesRepo.allowMobileData.first()
                val networkType = if (allowMobile) NetworkType.CONNECTED else NetworkType.UNMETERED

                val data = Data.Builder()
                    .putString(DownloadSyncWorker.SYNC_TYPE_KEY, "other")
                    .build()

                val onStartWorkRequest = OneTimeWorkRequestBuilder<DownloadSyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(networkType)
                            .build()
                    )
                    .addTag("cold/warm start check")
                    .setInputData(data)
                    .build()

                workManager.enqueue(onStartWorkRequest)
            }
        }
    }
    override fun onActivityStarted(activity: Activity) { // hot starts
        if (verificationJob?.isActive != true) {
            verificationJob = applicationScope.launch(Dispatchers.Default) {
                if (preferencesRepo.crossDeviceSync.first()) {
                    val hour: Long = 60 * 60 * 1000
                    if ((System.currentTimeMillis() - lastSyncVerification) > hour) {
                        lastSyncVerification = System.currentTimeMillis()
                        verifySyncStatus(activity)
                    }
                }
            }
        }
    }
    override fun onActivityResumed(activity: Activity) { activityResumedTrigger.tryEmit(Unit) }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

}
