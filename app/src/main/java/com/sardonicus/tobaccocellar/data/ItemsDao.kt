package com.sardonicus.tobaccocellar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.sardonicus.tobaccocellar.ui.stats.BrandCount
import com.sardonicus.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemsDao {

    /** Database operations **/
    // Add item //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Items): Long

    // Batch add items //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultipleItems(items: List<Items>): LongArray

    // Update item in database //
    @Update
    suspend fun update(item: Items)

    // Delete item //
    @Delete
    suspend fun delete(item: Items)

    // Delete all items //
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @RawQuery
    suspend fun vacuumDatabase(supportSQLiteQuery: SupportSQLiteQuery): Int


    // Components //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertComponent(component: Components): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertComponentsCrossRef(crossRef: ItemsComponentsCrossRef)

    @Query("DELETE FROM items_components_cross_ref WHERE itemId = :itemId AND componentId = :componentId")
    suspend fun deleteComponentsCrossRef(itemId: Int, componentId: Int)

    @Query("DELETE FROM items_components_cross_ref WHERE itemId = :itemId")
    suspend fun deleteComponentsCrossRefByItemId(itemId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultipleComponents(components: List<Components>): LongArray

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultipleComponentsCrossRef(crossRefs: List<ItemsComponentsCrossRef>)

    @Transaction
    @Query("DELETE FROM components WHERE componentId NOT IN (SELECT componentId FROM items_components_cross_ref)")
    suspend fun deleteOrphanedComponents()

    // Tins //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTin(tin: Tins): Long

    @Update
    suspend fun update(tin: Tins)

    @Query("DELETE FROM tins WHERE tinId = :tinId")
    suspend fun deleteTin(tinId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultipleTins(tins: List<Tins>): LongArray

    @Query("DELETE FROM tins WHERE itemsId = :itemId")
    suspend fun deleteAllTinsForItem(itemId: Int)


    /** Get all items **/
    // Get all items flow //
    @Transaction
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItemsStream(): Flow<List<Items>>

    // Get all items ids //
    @Query("SELECT id FROM items ORDER BY id ASC")
    fun getAllItemIds(): List<Int>

    // Get all items list //
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItemsExport(): List<Items>

    // Get all components flow //
    @Query("SELECT * FROM components ORDER BY componentName ASC")
    fun getAllComponents(): Flow<List<Components>>

    // Get all tins flow //
    @Query("SELECT * FROM tins ORDER BY tinId ASC")
    fun getAllTins(): Flow<List<Tins>>

    // Get all items components cross ref flow //
    @Query("SELECT * FROM items_components_cross_ref ORDER BY itemId ASC")
    fun getAllItemsComponentsCrossRef(): Flow<List<ItemsComponentsCrossRef>>

    // Get all items with components and tins flow //
    @Transaction
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getEverythingStream(): Flow<List<ItemsComponentsAndTins>>


    /** Get single item **/
    // Get item by id //
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemStream(id: Int): Flow<Items>

    // Get item details by id //
    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins>

    // Get components by item id //
    @Transaction
    @Query(
        """
        SELECT T2.* 
        FROM items_components_cross_ref AS T1 
        INNER JOIN components AS T2
        ON T1.componentId = T2.componentId
        WHERE T1.itemId = :itemId
        """
    )
    fun getComponentsForItemStream(itemId: Int): Flow<List<Components>>

    // Get tins by item id //
    @Query("SELECT * FROM tins WHERE itemsId = :itemsId")
    fun getTinsForItemStream(itemsId: Int): Flow<List<Tins>>

    @Query("SELECT id FROM items WHERE brand = :brand AND blend = :blend")
    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    @Query("SELECT componentId FROM components WHERE componentName = :name")
    suspend fun getComponentIdByName(name: String): Int?


    /** Checks **/
    // Check if item exists //
    @Query("SELECT EXISTS(SELECT * FROM items WHERE brand = :brand AND blend = :blend)")
    suspend fun exists(brand: String, blend: String): Boolean


    /** Get all column/value functions **/
    // Get all brands flow //
    @Query("SELECT DISTINCT brand FROM items ORDER BY brand ASC")
    fun getAllBrands(): Flow<List<String>>

    @Query("SELECT blend FROM items ORDER BY blend ASC")
    fun getAllBlends(): Flow<List<String>>

    // Get all types flow //
    @Query("SELECT type FROM items ORDER BY type ASC")
    fun getAllTypes(): Flow<List<String>>

    // Get all subgenres flow //
    @Query("SELECT DISTINCT subGenre FROM items ORDER BY subGenre ASC")
    fun getAllSubGenres(): Flow<List<String>>

    // Get all cuts flow //
    @Query("SELECT DISTINCT cut FROM items ORDER BY cut ASC")
    fun getAllCuts(): Flow<List<String>>

    // Get all favorites flow //
    @Query("SELECT favorite FROM items")
    fun getAllFavorites(): Flow<List<Boolean>>

    // Get all dislikeds flow //
    @Query("SELECT disliked FROM items")
    fun getAllDislikeds(): Flow<List<Boolean>>

    // Get all zero quantities flow //
    @Query("SELECT quantity FROM items WHERE quantity = 0")
    fun getAllZeroQuantity(): Flow<List<Boolean>>

    // Get all component names flow //
    @Query("SELECT componentName FROM components ORDER BY componentName ASC")
    fun getAllCompNames(): Flow<List<String>>

    // Get all tin containers flow //
    @Query("SELECT DISTINCT container FROM tins ORDER BY container ASC")
    fun getAllTinContainers(): Flow<List<String>>

    // Get all subgenres flow //
    @Query("SELECT DISTINCT subGenre FROM items ORDER BY subGenre ASC")
    fun getAllSubgenres(): Flow<List<String>>


    /** Get counts **/
    // total item count //
    @Query("SELECT COUNT(*) FROM items")
    fun getItemsCount(): Flow<Int>

    // total brands //
    @Query("SELECT COUNT(DISTINCT brand) FROM items")
    fun getBrandsCount(): Flow<Int>

    // total items per brand //
    @Query("SELECT DISTINCT brand, COUNT(*) as bcount FROM items GROUP BY brand")
    fun getTotalByBrand(): Flow<List<BrandCount>>

    // total items per type //
    @Query("SELECT type, COUNT(*) as tcount FROM items GROUP BY type")
    fun getTotalByType(): Flow<List<TypeCount>>

    // total favorite //
    @Query("SELECT SUM(favorite) FROM items")
    fun getTotalFavorite(): Flow<Int>

    // total dislike //
    @Query("SELECT SUM(disliked) FROM items")
    fun getTotalDislike(): Flow<Int>

    // total quantity //
    @Query("SELECT SUM(quantity) FROM items")
    fun getTotalQuantity(): Flow<Int>

    // total zero quantity //
    @Query("SELECT COUNT(*) FROM items WHERE quantity = 0")
    fun getTotalZeroQuantity(): Flow<Int>



    /** Get any BY value **/
    @Query("SELECT * FROM items WHERE brand = :brand")
    fun getItemsByBrand(brand: String): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE blend = :blend")
    fun getItemsByBlend(blend: String): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE type = :type")
    fun getItemsByType(type: String): Flow<List<Items>>

    @Query("SELECT * FROM items ORDER BY quantity DESC")
    fun getItemsByQuantity(): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE favorite = 1")
    fun getItemsByFavorite(): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE disliked = 1")
    fun getItemsByDisliked(): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE quantity = 0")
    fun getItemsByZeroQuantity(): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE brand = :brand AND blend = :blend")
    fun getItemByIndex(brand: String, blend: String): Items?

    @Query("SELECT * FROM components WHERE componentName IN (:components)")
    fun getComponentsByName(components: List<String>): Flow<List<Components>>

}