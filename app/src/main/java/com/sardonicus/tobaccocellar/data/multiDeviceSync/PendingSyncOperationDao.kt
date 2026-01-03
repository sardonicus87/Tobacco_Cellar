package com.sardonicus.tobaccocellar.data.multiDeviceSync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingSyncOperationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: PendingSyncOperation)

    @Query("SELECT * FROM pending_sync_operations ORDER BY timestamp ASC")
    suspend fun getAllOperations(): List<PendingSyncOperation>

    @Query("DELETE FROM pending_sync_operations WHERE id IN (:ids)")
    suspend fun deleteOperation(ids: List<Long>)

    @Query("SELECT EXISTS(SELECT 1 FROM pending_sync_operations LIMIT 1)")
    suspend fun hasPendingOperations(): Boolean
}