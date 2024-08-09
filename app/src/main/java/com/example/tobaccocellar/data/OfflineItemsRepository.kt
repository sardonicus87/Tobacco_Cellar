package com.example.tobaccocellar.data

import kotlinx.coroutines.flow.Flow

class OfflineItemsRepository(private val itemsDao: ItemsDao) : ItemsRepository {
    override fun getAllItemsStream(): Flow<List<Items>> = itemsDao.getAllItems()

    override fun getItemStream(id: Int): Flow<Items?> = itemsDao.getItem(id)

//    override suspend fun insertItem(item: Items) {
//        itemsDao.insertWithSnackbar(item)
//    }

    override suspend fun exists(brand: String, blend: String): Boolean {
        return itemsDao.exists(brand, blend)
    }

    override suspend fun getItemIdByIndex(brand: String, blend: String) = itemsDao.getItemIdByIndex(brand, blend)

    override suspend fun insertItem(item: Items) = itemsDao.insert(item)

    override suspend fun deleteItem(item: Items) = itemsDao.delete(item)

    override suspend fun updateItem(item: Items) = itemsDao.update(item)

//    override fun isEntryExist(id: Int): Boolean = itemsDao.isEntryExist(id)

//    override suspend fun checkItemExists(brand: String, blend: String): Flow<Items?> = itemsDao.checkItemExists(brand, blend)

}