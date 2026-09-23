package com.sardonicus.tobaccocellar.data.multiDeviceSync

import android.app.NotificationManager
import android.content.Context
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.CrossRefSyncPayload
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsCrossRef
import com.sardonicus.tobaccocellar.data.ItemsFlavoringCrossRef
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.TinSyncPayload
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.ui.settings.SyncDownloadEvent
import com.sardonicus.tobaccocellar.ui.settings.appDatabaseDialogs.checkNotificationPermission
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

class DownloadSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    companion object {
        const val RESULT_KEY = "message"
        const val SYNC_COMPLETE = "SYNC_COMPLETE"
        const val REMOTE_EMPTY = "REMOTE_EMPTY"
        const val UP_TO_DATE = "ALREADY_UP_TO_DATE"
        const val SKIPPED = "SKIPPED"
        const val NO_ACCOUNT = "NO_ACCOUNT"
        const val NETWORK_ERROR = "NETWORK_ERROR"
        const val AUTH_ERROR = "AUTH_ERROR"
        const val SERVER_ERROR = "SERVER_ERROR"
        const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
        const val SYNC_NOTIFICATION_ID = 1001
        const val SYNC_TYPE_KEY = "sync_type"
        const val SYNC_TYPE_PERIODIC = "periodic"
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun doWork(): Result {
        val syncType = inputData.getString(SYNC_TYPE_KEY)

        val app = applicationContext as CellarApplication
        val itemsRepo = app.container.itemsRepository
        val prefsRepo = app.preferencesRepo

        val allowMobile = prefsRepo.allowMobileData.first()
        if (!allowMobile) {
            val networkMonitor = app.container.networkMonitor
            val isWifi = networkMonitor.isWifi.first()
            if (!isWifi) { return Result.success(workDataOf(RESULT_KEY to NETWORK_ERROR)) }
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        SyncStateManager.started()

        var started = 0L

        try {
            if (checkNotificationPermission(notificationManager, app)) {
                started = SystemClock.elapsedRealtime()
                val title =
                    if (syncType == SYNC_TYPE_PERIODIC) "Tobacco Cellar periodic sync check"
                    else "Tobacco Cellar sync check"
                val notification = NotificationCompat.Builder(applicationContext, CellarApplication.SYNC_NOTIFICATION)
                    .setContentTitle(title)
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setOngoing(true)
                    .setProgress(0, 0, true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSilent(true)
                    .build()

                notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
            }
            val syncEnabled = prefsRepo.crossDeviceSync.first()
            if (!syncEnabled) { return Result.success(workDataOf(RESULT_KEY to SKIPPED)) }

            val userEmail = prefsRepo.signedInUserEmail.first()
            if (userEmail.isNullOrBlank()) { return Result.failure(workDataOf(RESULT_KEY to NO_ACCOUNT)) }

            val driveService = GoogleDriveServiceHelper.getDriveService(applicationContext, userEmail)

            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name, createdTime)")
                .execute()

            if (fileList.files.isNullOrEmpty()) { return Result.success(workDataOf(RESULT_KEY to REMOTE_EMPTY)) }

            val processedFileIds = prefsRepo.processedSyncFiles.first()
            val newFiles = fileList.files.filter { it.id !in processedFileIds }
                .ifEmpty { return Result.success(workDataOf(RESULT_KEY to REMOTE_EMPTY)) }

            val successfullyProcessedFiles = mutableListOf<String>()

            for (file in newFiles) {
                var processSuccess = true

                try {
                    driveService.files().get(file.id).executeMediaAsInputStream()
                        .use { inputStream ->
                            val operations =
                                Json.decodeFromStream<List<PendingSyncOperation>>(inputStream)
                            val db = TobaccoDatabase.getDatabase(applicationContext)

                            db.withTransaction {
                                for (op in operations) {
                                    if (!applyOperation(itemsRepo, op))
                                        processSuccess = false
                                }
                            }
                        }
                    if (processSuccess) { successfullyProcessedFiles.add(file.id) }
                } catch (_: Exception) { continue }
            }

            // find old files and delete
            val oneMonthMillis = 30 * 24 * 60 * 60 * 1000L
            val cutOffTime = System.currentTimeMillis() - oneMonthMillis

            val oldFiles = fileList.files.filter { it.createdTime.value < cutOffTime }
            val failedDelete = mutableSetOf<String>()
            if (oldFiles.isNotEmpty()) {
                for (file in oldFiles) {
                    try { driveService.files().delete(file.id).execute() }
                    catch (_: Exception) { failedDelete.add(file.id); continue }
                }
            }

            val oldFileIds = oldFiles.map { it.id }.toSet() - failedDelete
            val allProcessedIds = processedFileIds + successfullyProcessedFiles - oldFileIds

            prefsRepo.saveProcessedSyncFiles(allProcessedIds.toSet())

            EventBus.emit(SyncDownloadEvent)
            return Result.success(workDataOf(RESULT_KEY to SYNC_COMPLETE))
        } catch (e: Exception) {
            val error = when (e) {
                is GoogleJsonResponseException -> {
                    if (e.statusCode == 401 || e.statusCode == 403) AUTH_ERROR
                    else SERVER_ERROR }
                is UserRecoverableAuthIOException -> AUTH_ERROR
                is IOException -> NETWORK_ERROR
                else -> UNKNOWN_ERROR
            }

            return if (error == AUTH_ERROR || runAttemptCount >= 5) {
                Result.failure(workDataOf(RESULT_KEY to error))
            } else Result.retry()
        } finally {
            withContext(NonCancellable) {
                SyncStateManager.finished()
                if (started > 0) {
                    val elapsed = SystemClock.elapsedRealtime() - started
                    val extend = 3500 - elapsed
                    if (extend > 0) { delay(extend.milliseconds) }
                }
                notificationManager.cancel(SYNC_NOTIFICATION_ID)
            }
        }
    }


    private suspend fun applyOperation(repo: ItemsRepository, op: PendingSyncOperation): Boolean {
        if (op.dbVersion > TobaccoDatabase.DATABASE_VERSION) { return false }

        when (op.entityType) {
            "Items" -> {
                val remoteItem = Json.decodeFromString<Items>(op.payload)
                val localItem = repo.getItemByIndex(remoteItem.brand, remoteItem.blend)

                when (op.operationType) {
                    "INSERT" -> {
                        if (localItem == null) { repo.insertItem(remoteItem) }
                        else if (remoteItem.lastModified > localItem.lastModified) {
                            repo.updateItem(remoteItem.copy(id = localItem.id)) } }
                    "UPDATE" -> {
                        if (localItem != null && remoteItem.lastModified > localItem.lastModified) {
                            repo.updateItem(remoteItem.copy(id = localItem.id)) } }
                    "DELETE" -> { localItem?.let { repo.deleteItem(it) } }
                }
            }
            "Tins" -> {
                val syncPayload = Json.decodeFromString<TinSyncPayload>(op.payload)
                val remoteTin = syncPayload.tin

                val localParentItem =
                    repo.getItemByIndex(syncPayload.itemBrand, syncPayload.itemBlend) ?: return false

                val localTin = repo.getTinByLabel(localParentItem.id, remoteTin.tinLabel)
                val correctedRemoteTin = remoteTin.copy(itemsId = localParentItem.id)

                when (op.operationType) {
                    "INSERT" -> {
                        if (localTin == null) { repo.insertTin(correctedRemoteTin) }
                        else if (correctedRemoteTin.lastModified > localTin.lastModified) {
                            repo.updateTin(correctedRemoteTin.copy(tinId = localTin.tinId)) } }
                    "UPDATE" -> {
                        if (localTin != null && correctedRemoteTin.lastModified > localTin.lastModified) {
                            repo.updateTin(correctedRemoteTin.copy(tinId = localTin.tinId)) } }
                    "DELETE" -> { localTin?.let { repo.deleteTin(it.tinId) } }
                }
            }
            "Components" -> {
                if (op.operationType == "INSERT") {
                    val remoteComponent = Json.decodeFromString<Components>(op.payload)
                    if (repo.getComponentIdByName(remoteComponent.componentName) == null) {
                        repo.insertComponent(Components(componentName = remoteComponent.componentName))
                    }
                }
            }
            "ItemsComponentsCrossRef" -> {
                val payload = Json.decodeFromString<CrossRefSyncPayload>(op.payload)
                val localItemId = repo.getItemIdByIndex(payload.itemBrand, payload.itemBlend)
                val localComponentId = repo.getComponentIdByName(payload.relatedEntityName)

                if (localItemId != null && localComponentId != null) {
                    val crossRef = ItemsComponentsCrossRef(localItemId, localComponentId)
                    if (op.operationType == "INSERT") { repo.insertComponentsCrossRef(crossRef) }
                    else if (op.operationType == "DELETE") { repo.deleteComponentsCrossRef(crossRef) }
                }
            }
            "Flavoring" -> {
                if (op.operationType == "INSERT") {
                    val remoteFlavoring = Json.decodeFromString<Flavoring>(op.payload)
                    if (repo.getFlavoringIdByName(remoteFlavoring.flavoringName) == null) {
                        repo.insertFlavoring(Flavoring(flavoringName = remoteFlavoring.flavoringName))
                    }
                }
            }
            "ItemsFlavoringCrossRef" -> {
                val payload = Json.decodeFromString<CrossRefSyncPayload>(op.payload)
                val localItemId = repo.getItemIdByIndex(payload.itemBrand, payload.itemBlend)
                val localFlavoringId = repo.getFlavoringIdByName(payload.relatedEntityName)

                if (localItemId != null && localFlavoringId != null) {
                    val crossRef = ItemsFlavoringCrossRef(localItemId, localFlavoringId)
                    if (op.operationType == "INSERT") { repo.insertFlavoringCrossRef(crossRef) }
                    else if (op.operationType == "DELETE") { repo.deleteFlavoringCrossRef(crossRef) }
                }
            }
        }
        return true
    }
}