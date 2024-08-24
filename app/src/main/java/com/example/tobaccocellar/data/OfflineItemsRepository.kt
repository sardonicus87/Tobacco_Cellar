package com.example.tobaccocellar.data

import kotlinx.coroutines.flow.Flow

class OfflineItemsRepository(private val itemsDao: ItemsDao) : ItemsRepository {
    override fun getAllItemsStream(): Flow<List<Items>> = itemsDao.getAllItems()

    override fun getAllItemsExport(): List<Items> = itemsDao.getAllItemsExport()

    override fun getItemStream(id: Int): Flow<Items?> = itemsDao.getItem(id)

    override suspend fun exists(brand: String, blend: String): Boolean {
        return itemsDao.exists(brand, blend)
    }

//    override fun getAllBrands(): Flow<List<BrandState>> = itemsDao.getAllBrands()

    override fun getAllBrands(): Flow<List<String>> = itemsDao.getAllBrands()

    override suspend fun getItemIdByIndex(brand: String, blend: String) = itemsDao.getItemIdByIndex(brand, blend)

    override suspend fun insertItem(item: Items) = itemsDao.insert(item)

    override suspend fun insertMultiple(items: List<Items>): List<Long> {
        return itemsDao.insertMultiple(items).toList()
    }

    override suspend fun deleteItem(item: Items) = itemsDao.delete(item)

    override suspend fun updateItem(item: Items) = itemsDao.update(item)

}