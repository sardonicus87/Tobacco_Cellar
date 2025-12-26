package com.sardonicus.tobaccocellar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_operations")
data class PendingSyncOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationType: String,
    val entityType: String,
    val entityId: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)