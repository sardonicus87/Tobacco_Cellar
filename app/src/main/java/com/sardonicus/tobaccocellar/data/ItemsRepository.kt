package com.sardonicus.tobaccocellar.data

import com.sardonicus.tobaccocellar.data.multiDeviceSync.PendingSyncOperationDao
import kotlinx.coroutines.flow.Flow

interface ItemsRepository {

    /** Database operations **/
    // Items //
    suspend fun insertItem(item: Items): Long

    suspend fun insertMultipleItems(items: List<Items>): List<Long>

    suspend fun updateItem(item: Items)

    suspend fun updateMultipleItems(items: List<ItemsComponentsAndTins>)

    suspend fun deleteItem(item: Items)

    suspend fun deleteAllItems()

    suspend fun optimizeDatabase()


    // Components //
    suspend fun insertComponent(component: Components): Long

    suspend fun insertComponentsCrossRef(crossRef: ItemsComponentsCrossRef)

    suspend fun deleteComponentsCrossRef(crossRef: ItemsComponentsCrossRef)

    suspend fun deleteComponentsCrossRefByItemId(itemId: Int)


    // Flavoring //
    suspend fun insertFlavoring(flavoring: Flavoring): Long

    suspend fun insertFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef)

    suspend fun deleteFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef)

    suspend fun deleteFlavoringCrossRefByItemId(itemId: Int)


    // Tins //
    suspend fun insertTin(tin: Tins): Long

    suspend fun updateTin(tin: Tins)

    suspend fun deleteTin(tinId: Int)

    suspend fun insertMultipleTins(tins: List<Tins>): List<Long>

    suspend fun deleteAllTinsForItem(itemId: Int)


    /** Get all items **/
    fun getAllItemIds(): List<Int>

    fun getAllComponentsStream(): Flow<List<Components>>

    fun getAllFlavoringStream(): Flow<List<Flavoring>>

    fun getEverythingStream(): Flow<List<ItemsComponentsAndTins>>


    /** Get single item **/
    fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins?>

    suspend fun getItemById(id: Int): Items?

    fun getComponentsForItemStream(id: Int): Flow<List<Components>>

    suspend fun getComponentById(id: Int): Components?

    fun getFlavoringForItemStream(id: Int): Flow<List<Flavoring>>

    suspend fun getFlavoringById(id: Int): Flavoring?

    fun getTinsForItemStream(id: Int): Flow<List<Tins>>

    fun getTinDetailsStream(tinId: Int): Flow<Tins?>

    suspend fun getTinById(tinId: Int): Tins?

    suspend fun getTinByLabel(itemsId: Int, tinLabel: String): Tins?

    suspend fun getItemIdByIndex(brand: String, blend: String): Int?

    suspend fun getComponentIdByName(name: String): Int?

    suspend fun getFlavoringIdByName(name: String): Int?


    /** Checks **/
    suspend fun exists(brand: String, blend: String): Boolean


    /** Get any by value **/
    fun getItemByIndex(brand: String, blend: String): Items?


    /** Cloud sync **/
    fun getPendingSyncOperationDao(): PendingSyncOperationDao

    fun triggerUploadWorker()

}