package com.sardonicus.tobaccocellar.data.multiDeviceSync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.model.File
import com.sardonicus.tobaccocellar.CellarApplication
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.util.UUID

class UploadSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val app = applicationContext as CellarApplication
        val preferencesRepo = app.preferencesRepo
        val pendingSyncOperationDao = app.container.itemsRepository.getPendingSyncOperationDao()

        try {
            val syncEnabled = preferencesRepo.crossDeviceSync.first()
            if (!syncEnabled) { return Result.success() }

            val userEmail = preferencesRepo.signedInUserEmail.first() ?: return Result.success()

            val pendingOperations = pendingSyncOperationDao.getAllOperations().ifEmpty { return Result.success() }

            val driveService = GoogleDriveServiceHelper.getDriveService(applicationContext, userEmail)
            val jsonPayload = Json.encodeToString(pendingOperations)
            val filename = "${UUID.randomUUID()}.json"

            val fileMetadata = File().apply {
                name = filename
                parents = listOf("appDataFolder")
            }

            val contentStream = InputStreamContent(
                "application/json",
                ByteArrayInputStream(jsonPayload.toByteArray())
            )

            val uploadedFile = driveService.files().create(fileMetadata, contentStream)
                .setFields("id")
                .execute()

            val fileId = uploadedFile.id
            if (fileId != null) {
                val currentProcessedFiles = preferencesRepo.processedSyncFiles.first()
                val updatedProcessedFiles = currentProcessedFiles + fileId
                preferencesRepo.saveProcessedSyncFiles(updatedProcessedFiles)
            }

            val processedIds = pendingOperations.map { it.id }
            pendingSyncOperationDao.deleteOperation(processedIds)

            return Result.success()
        } catch (e: Exception) {
            println("UploadSyncWorker error uploading to server, $e")
            return Result.retry()
        }
    }
}