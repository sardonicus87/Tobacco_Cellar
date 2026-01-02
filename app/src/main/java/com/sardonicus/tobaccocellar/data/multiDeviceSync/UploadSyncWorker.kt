package com.sardonicus.tobaccocellar.data.multiDeviceSync

import android.content.Context
import android.util.Log
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
            if (!syncEnabled) {
                Log.d("UploadSyncWorker", "Cross-device sync is disabled, stopping work")
                return Result.success()
            }

            val userEmail = preferencesRepo.signedInUserEmail.first()

            if (userEmail == null ) {
                Log.w("UploadSyncWorker", "Cannot upload, no user signed in.")
                return Result.success()
            }

            val pendingOperations = pendingSyncOperationDao.getAllOperations()

            if (pendingOperations.isEmpty()) {
                Log.d("UploadSyncWorker", "No pending operations to upload")
                return Result.success()
            }
            Log.d("UploadSyncWorker", "Found ${pendingOperations.size} pending uploads")

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

            Log.d("UploadSyncWorker", "Uploading to server with filename: $filename")
            driveService.files().create(fileMetadata, contentStream)
                .execute()

            val processedIds = pendingOperations.map { it.id }
            pendingSyncOperationDao.deleteOperation(processedIds)

            return Result.success()
        } catch (e: Exception) {
            Log.e("UploadSyncWorker", "Error uploading to server", e)
            return Result.retry()
        }
    }
}