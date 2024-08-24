package com.example.tobaccocellar.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    indices = [Index(value = (["brand", "blend"]), unique = true)]
)
data class Items(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brand: String,
    val blend: String,
    val type: String,
    val quantity: Int,
    val disliked: Boolean,
    val favorite: Boolean,
    val notes: String,
)