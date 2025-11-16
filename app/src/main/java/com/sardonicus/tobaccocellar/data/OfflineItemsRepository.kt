package com.sardonicus.tobaccocellar.data

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow

class OfflineItemsRepository(
    private var itemsDao: ItemsDao,
) : ItemsRepository {

    /** Database operations **/
    // Items //
    override suspend fun insertItem(item: Items): Long {
        return itemsDao.insert(item)
    }

    override suspend fun insertMultipleItems(items: List<Items>): List<Long> {
        return itemsDao.insertMultipleItems(items).toList()
    }

    override suspend fun updateItem(item: Items) = itemsDao.update(item)

    override suspend fun updateMultipleItems(items: List<ItemsComponentsAndTins>) {
        for (item in items) {
            itemsDao.updateICT(item.items, item.components, item.tins)
        }
    }

    override suspend fun deleteItem(item: Items) = itemsDao.delete(item)

    override suspend fun deleteAllItems() {
        itemsDao.deleteAllItems()
        itemsDao.deleteOrphanedComponents()
        itemsDao.deleteOrphanedFlavoring()
    }

    override suspend fun optimizeDatabase() {
        itemsDao.deleteOrphanedComponents()
        itemsDao.deleteOrphanedFlavoring()
        itemsDao.vacuumDatabase(SimpleSQLiteQuery("VACUUM"))
    }


    // Components //
    override suspend fun insertComponent(component: Components): Long {
        return itemsDao.insertComponent(component)
    }

    override suspend fun insertComponentsCrossRef(crossRef: ItemsComponentsCrossRef) {
        itemsDao.insertComponentsCrossRef(crossRef)
    }

    override suspend fun deleteComponentsCrossRef(crossRef: ItemsComponentsCrossRef) {
        itemsDao.deleteComponentsCrossRef(crossRef)
    }

    override suspend fun deleteComponentsCrossRefByItemId(itemId: Int) {
        itemsDao.deleteComponentsCrossRefByItemId(itemId)
    }


    // Flavoring //
    override suspend fun insertFlavoring(flavoring: Flavoring): Long {
        return itemsDao.insertFlavoring(flavoring)
    }

    override suspend fun insertFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef) {
        itemsDao.insertFlavoringCrossRef(crossRef)
    }

    override suspend fun deleteFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef) {
        itemsDao.deleteFlavoringCrossRef(crossRef)
    }

    override suspend fun deleteFlavoringCrossRefByItemId(itemId: Int) {
        itemsDao.deleteFlavoringCrossRefByItemId(itemId)
    }


    // Tins //
    override suspend fun insertTin(tin: Tins): Long {
        return itemsDao.insertTin(tin)
    }

    override suspend fun updateTin(tin: Tins) {
        itemsDao.update(tin)
    }

    override suspend fun deleteTin(tinId: Int) {
        itemsDao.deleteTin(tinId)
    }

    override suspend fun insertMultipleTins(tins: List<Tins>): List<Long> {
        return itemsDao.insertMultipleTins(tins).toList()
    }

    override suspend fun deleteAllTinsForItem(itemId: Int) {
        itemsDao.deleteAllTinsForItem(itemId)
    }


    /** Get all items **/
    override fun getAllItemIds(): List<Int> = itemsDao.getAllItemIds()

    override fun getAllComponentsStream(): Flow<List<Components>> = itemsDao.getAllComponents()

    override fun getAllFlavoringStream(): Flow<List<Flavoring>> = itemsDao.getAllFlavoring()

    override fun getEverythingStream(): Flow<List<ItemsComponentsAndTins>> = itemsDao.getEverythingStream()


    /** Get single item **/
    override fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins?> = itemsDao.getItemDetailsStream(id)

    override fun getComponentsForItemStream(id: Int): Flow<List<Components>> = itemsDao.getComponentsForItemStream(id)

    override fun getFlavoringForItemStream(id: Int): Flow<List<Flavoring>> = itemsDao.getFlavoringForItemStream(id)

    override fun getTinsForItemStream(id: Int): Flow<List<Tins>> = itemsDao.getTinsForItemStream(id)

    override suspend fun getItemIdByIndex(brand: String, blend: String) =
        itemsDao.getItemIdByIndex(brand, blend)

    override suspend fun getComponentIdByName(name: String): Int? = itemsDao.getComponentIdByName(name)

    override suspend fun getFlavoringIdByName(name: String): Int? = itemsDao.getFlavoringIdByName(name)


    /** Checks **/
    override suspend fun exists(brand: String, blend: String): Boolean {
        return itemsDao.exists(brand, blend)
    }


    /** Get any by value **/
    override fun getItemByIndex(brand: String, blend: String): Items? = itemsDao.getItemByIndex(brand, blend)
}