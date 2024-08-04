package com.example.tobaccocellar.data

import kotlinx.coroutines.flow.Flow

interface ItemsRepository {
    fun getAllItemsStream(): Flow<List<Items>>

    fun getItemStream(id: Int): Flow<Items?>

    suspend fun insertItem(item: Items): Result<Unit> {
        return try {
            ItemsDao.insert(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(item: Items)

    suspend fun updateItem(item: Items)

//    fun isEntryExist(id: Int): Boolean

//    suspend fun checkItemExists(brand: String, blend: String): Flow<Items?>

//    suspend fun getItemsCount(): Int
//
//    suspend fun getItemsByType(type: String): List<Items>
//
//    fun getItemsByBrand(brand: String): List<Items>

}