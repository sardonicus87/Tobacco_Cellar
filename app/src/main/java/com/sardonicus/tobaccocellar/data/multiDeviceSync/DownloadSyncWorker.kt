package com.sardonicus.tobaccocellar.data.multiDeviceSync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
import com.sardonicus.tobaccocellar.ui.utilities.NetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

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
        const val NETWORK_ERROR = "NETWORK_ERROR"
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as CellarApplication
        val itemsRepository = app.container.itemsRepository
        val preferencesRepo = app.preferencesRepo

        val allowMobile = preferencesRepo.allowMobileData.first()
        if (!allowMobile) {
            val networkMonitor = NetworkMonitor(applicationContext)
            val isWifi = networkMonitor.isWifi.first()
            if (!isWifi) { return Result.success(workDataOf(RESULT_KEY to NETWORK_ERROR)) }
        }

        try {
            val syncEnabled = preferencesRepo.crossDeviceSync.first()
            if (!syncEnabled) { return Result.success(workDataOf(RESULT_KEY to SKIPPED)) }

            val userEmail = preferencesRepo.signedInUserEmail.first()
                ?: return Result.success(workDataOf(RESULT_KEY to SKIPPED))

            val driveService = GoogleDriveServiceHelper.getDriveService(applicationContext, userEmail)

            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name, createdTime)")
                .execute()

            if (fileList.files.isNullOrEmpty()) { return Result.success(workDataOf(RESULT_KEY to REMOTE_EMPTY)) }

            val processedFileIds = preferencesRepo.processedSyncFiles.first()

            val newFiles = fileList.files.filter { it.id !in processedFileIds }
                .ifEmpty { return Result.success(workDataOf(RESULT_KEY to REMOTE_EMPTY)) }

            val successfullyProcessedFiles = mutableListOf<String>()

            for (file in newFiles) {
                var processSuccess = true

                try {
                    val outputStream = ByteArrayOutputStream()

                    driveService.files().get(file.id).executeMediaAndDownloadTo(outputStream)

                    val fileContent = outputStream.toString()
                    val operations = Json.decodeFromString<List<PendingSyncOperation>>(fileContent)

                    for (op in operations) {
                        if (!applyOperation(itemsRepository, op)) {
                            processSuccess = false
                        }
                    }

                    if (processSuccess) { successfullyProcessedFiles.add(file.id) }

                } catch (e: Exception) {
                    println("DownloadSyncWorker failed to process file ${file.id}, $e")
                    continue
                }
            }

            // find old files and delete
            val oneMonthMillis = 30 * 24 * 60 * 60 * 1000L
            val cutOffTime = System.currentTimeMillis() - oneMonthMillis

            val oldFiles = fileList.files.filter { it.createdTime.value < cutOffTime }
            if (oldFiles.isNotEmpty()) {
                for (file in oldFiles) {
                    try {
                        driveService.files().delete(file.id).execute()
                    } catch (e: Exception) {
                        println("DownloadSyncWorker failed to delete file ${file.id}, $e")
                    }
                }
            }

            val oldFileIds = oldFiles.map { it.id }.toSet()
            val allProcessedIds = processedFileIds + successfullyProcessedFiles - oldFileIds

            preferencesRepo.saveProcessedSyncFiles(allProcessedIds.toSet())

            return Result.success(workDataOf(RESULT_KEY to SYNC_COMPLETE))
        } catch (e: Exception) {
            println("DownloadSyncWorker error during sync download work, $e")
            return Result.retry()
        }
    }

    private suspend fun applyOperation(repo: ItemsRepository, op: PendingSyncOperation): Boolean {
        val localDbVersion = TobaccoDatabase.getDatabaseVersion(applicationContext)
        if (op.dbVersion != localDbVersion) { return false }

        when (op.entityType) {
            "Items" -> {
                val remoteItem = Json.decodeFromString<Items>(op.payload)
                val localItem = repo.getItemByIndex(remoteItem.brand, remoteItem.blend)

                when (op.operationType) {
                    "INSERT" -> {
                        if (localItem == null) {
                            repo.insertItem(remoteItem)
                        } else if (remoteItem.lastModified > localItem.lastModified) {
                            repo.updateItem(remoteItem.copy(id = localItem.id))
                        }
                    }
                    "UPDATE" -> {
                        if (localItem != null && remoteItem.lastModified > localItem.lastModified) {
                            repo.updateItem(remoteItem.copy(id = localItem.id))
                        }
                    }
                    "DELETE" -> {
                        localItem?.let { repo.deleteItem(it) }
                    }
                }
            }
            "Tins" -> {
                val syncPayload = Json.decodeFromString<TinSyncPayload>(op.payload)
                val remoteTin = syncPayload.tin

                val localParentItem =
                    repo.getItemByIndex(syncPayload.itemBrand, syncPayload.itemBlend)
                        ?: return false

                val localTin = repo.getTinByLabel(localParentItem.id, remoteTin.tinLabel)
                val correctedRemoteTin = remoteTin.copy(itemsId = localParentItem.id)

                when (op.operationType) {
                    "INSERT" -> {
                        if (localTin == null) {
                            repo.insertTin(correctedRemoteTin)
                        } else if (correctedRemoteTin.lastModified > localTin.lastModified) {
                            repo.updateTin(correctedRemoteTin.copy(tinId = localTin.tinId))
                        }
                    }
                    "UPDATE" -> {
                        if (localTin != null && correctedRemoteTin.lastModified > localTin.lastModified) {
                            repo.updateTin(correctedRemoteTin.copy(tinId = localTin.tinId))
                        }
                    }
                    "DELETE" -> {
                        localTin?.let { repo.deleteTin(it.tinId) }
                    }
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
                    if (op.operationType == "INSERT") {
                        repo.insertComponentsCrossRef(crossRef)
                    } else if (op.operationType == "DELETE") {
                        repo.deleteComponentsCrossRef(crossRef)
                    }
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
                    if (op.operationType == "INSERT") {
                        repo.insertFlavoringCrossRef(crossRef)
                    } else if (op.operationType == "DELETE") {
                        repo.deleteFlavoringCrossRef(crossRef)
                    }
                }
            }
        }
        return true
    }
}