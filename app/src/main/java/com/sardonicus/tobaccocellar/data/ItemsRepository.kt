package com.sardonicus.tobaccocellar.data

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
    fun getItemStream(id: Int): Flow<Items?>

    fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins?>

    fun getComponentsForItemStream(id: Int): Flow<List<Components>>

    fun getFlavoringForItemStream(id: Int): Flow<List<Flavoring>>

    fun getTinsForItemStream(id: Int): Flow<List<Tins>>

    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    suspend fun getComponentIdByName(name: String): Int?

    suspend fun getFlavoringIdByName(name: String): Int?


    /** Checks **/
    suspend fun exists(brand: String, blend: String): Boolean


    /** Get any by value **/
    fun getItemByIndex(brand: String, blend: String): Items?

}