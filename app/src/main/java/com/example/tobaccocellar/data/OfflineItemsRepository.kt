package com.example.tobaccocellar.data

import com.example.tobaccocellar.ui.stats.BrandCount
import com.example.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow

class OfflineItemsRepository(private val itemsDao: ItemsDao) : ItemsRepository {

    /* TODO organize OfflineItemsRepo functions */

    override fun getAllItemsStream(): Flow<List<Items>> = itemsDao.getAllItems()

    override fun getAllItemsExport(): List<Items> = itemsDao.getAllItemsExport()

    override fun getItemStream(id: Int): Flow<Items?> = itemsDao.getItem(id)

    // Exist check //
    override suspend fun exists(brand: String, blend: String): Boolean {
        return itemsDao.exists(brand, blend)
    }

    override fun getAllBrandsStream(): Flow<List<String>> = itemsDao.getAllBrands()

    override suspend fun getItemIdByIndex(brand: String, blend: String) = itemsDao.getItemIdByIndex(brand, blend)

    override suspend fun insertItem(item: Items) = itemsDao.insert(item)

    override suspend fun insertMultiple(items: List<Items>): List<Long> {
        return itemsDao.insertMultiple(items).toList()
    }

    override fun getItemsCount(): Flow<Int> = itemsDao.getItemsCount()

    override fun getBrandsCount(): Flow<Int> = itemsDao.getBrandsCount()

    override fun getTotalByBrand(): Flow<List<BrandCount>> = itemsDao.getTotalByBrand()

    override fun getTotalByType(): Flow<List<TypeCount>> = itemsDao.getTotalByType()

    override fun getTotalQuantity(): Flow<Int> = itemsDao.getTotalQuantity()

    override fun getTotalFavorite(): Flow<Int> = itemsDao.getTotalFavorite()

    override fun getTotalDislike(): Flow<Int> = itemsDao.getTotalDislike()

    override fun getTotalZeroQuantity(): Flow<Int> = itemsDao.getTotalZeroQuantity()



    override suspend fun deleteItem(item: Items) = itemsDao.delete(item)

    override suspend fun updateItem(item: Items) = itemsDao.update(item)



    override fun getItemsByBrand(brand: String): Flow<List<Items>> = itemsDao.getItemsByBrand(brand)

    override fun getItemsByType(type: String): Flow<List<Items>> = itemsDao.getItemsByType(type)

    override fun getItemsByFavorite(): Flow<List<Items>> = itemsDao.getItemsByFavorite()

    override fun getItemsByDisliked(): Flow<List<Items>> = itemsDao.getItemsByDisliked()

}