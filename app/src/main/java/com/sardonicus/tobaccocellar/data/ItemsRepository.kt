package com.sardonicus.tobaccocellar.data

import com.sardonicus.tobaccocellar.ui.stats.BrandCount
import com.sardonicus.tobaccocellar.ui.stats.TypeCount
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

    suspend fun deleteComponentsCrossRef(itemId: Int, componentId: Int)

    suspend fun deleteComponentsCrossRefByItemId(itemId: Int)

    suspend fun insertMultipleComponents(components: List<Components>): List<Long>

    suspend fun insertMultipleComponentsCrossRef(crossRefs: List<ItemsComponentsCrossRef>)

    // Tins //
    suspend fun insertTin(tin: Tins): Long

    suspend fun updateTin(tin: Tins)

    suspend fun deleteTin(tinId: Int)

    suspend fun insertMultipleTins(tins: List<Tins>): List<Long>

    suspend fun deleteAllTinsForItem(itemId: Int)

    suspend fun getTinExportData(): List<TinExportData>


    /** Get all items **/
    fun getAllItemsStream(): Flow<List<Items>>

    fun getAllItemIds(): List<Int>

    fun getAllItemsExport(): List<Items>

    suspend fun getAllItemsWithComponents(): List<ItemsWithComponents>

    fun getAllComponentsStream(): Flow<List<Components>>

    fun getAllTinsStream(): Flow<List<Tins>>

    fun getAllItemsComponentsCrossRefStream(): Flow<List<ItemsComponentsCrossRef>>

    fun getEverythingStream(): Flow<List<ItemsComponentsAndTins>>


    /** Get single item **/
    fun getItemStream(id: Int): Flow<Items?>

    fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins?>

    fun getComponentsForItemStream(id: Int): Flow<List<Components>>

    fun getTinsForItemStream(id: Int): Flow<List<Tins>>

    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    suspend fun getComponentIdByName(name: String): Int?


    /** Checks **/
    suspend fun exists(brand: String, blend: String): Boolean


    /** Get all column/value functions **/
    fun getAllBrandsStream(): Flow<List<String>>

    fun getAllBlendsStream(): Flow<List<String>>

    fun getAllTypesStream(): Flow<List<String>>

    fun getAllSubGenresStream(): Flow<List<String>>

    fun getAllCutsStream(): Flow<List<String>>

    fun getAllFavoritesStream(): Flow<List<Boolean>>

    fun getAllDislikeStream(): Flow<List<Boolean>>

    fun getAllZeroQuantityStream(): Flow<List<Boolean>>

    fun getAllCompNamesStream(): Flow<List<String>>

    fun getAllTinContainersStream(): Flow<List<String>>

    fun getAllSubgenresStream(): Flow<List<String>>


    /** Get counts **/
    fun getItemsCount(): Flow<Int>

    fun getBrandsCount(): Flow<Int>

    fun getTotalByBrand(): Flow<List<BrandCount>>

    fun getTotalByType(): Flow<List<TypeCount>>

    fun getTotalFavorite(): Flow<Int>

    fun getTotalDislike(): Flow<Int>

    fun getTotalQuantity(): Flow<Int>

    fun getTotalZeroQuantity(): Flow<Int>


    /** Get any by value **/
    fun getItemsByBrand(brand: String): Flow<List<Items>>

    fun getItemsByBlend(blend: String): Flow<List<Items>>

    fun getItemsByType(type: String): Flow<List<Items>>

    fun getItemsByQuantity(): Flow<List<Items>>

    fun getItemsByFavorite(): Flow<List<Items>>

    fun getItemsByDisliked(): Flow<List<Items>>

    fun getItemsByZeroQuantity(): Flow<List<Items>>

    fun getItemByIndex(brand: String, blend: String): Items?

    fun getComponentsByName(components: List<String>): Flow<List<Components>>

}