package com.example.tobaccocellar.data

import kotlinx.coroutines.flow.Flow

interface ItemsRepository {
    fun getAllItemsStream(): Flow<List<Items>>

    fun getItemStream(id: Int): Flow<Items?>

    suspend fun insertItem(item: Items)

    suspend fun deleteItem(item: Items)

    suspend fun updateItem(item: Items)

    suspend fun exists(brand: String, blend: String): Boolean

    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    fun getAllBrands(): Flow<List<String>>

//    fun getAllBrands(): Flow<List<BrandState>>

//    suspend fun getItemsCount(): Int
//
//    suspend fun getItemsByType(type: String): List<Items>
//
//    fun getItemsByBrand(brand: String): List<Items>

}