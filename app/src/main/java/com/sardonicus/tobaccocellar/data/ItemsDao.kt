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

    // Update item from ItemsComponentsAndTins //
    @Update
    suspend fun updateICT(item: Items, components: List<Components>, tins: List<Tins>)

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

    @Delete
    suspend fun deleteComponentsCrossRef(crossRef: ItemsComponentsCrossRef)

    @Query("DELETE FROM items_components_cross_ref WHERE itemId = :itemId")
    suspend fun deleteComponentsCrossRefByItemId(itemId: Int)

    @Transaction
    @Query("DELETE FROM components WHERE componentId NOT IN (SELECT componentId FROM items_components_cross_ref)")
    suspend fun deleteOrphanedComponents()


    // Flavoring //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFlavoring(flavoring: Flavoring): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef)

    @Delete
    suspend fun deleteFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef)

    @Query("DELETE FROM items_flavoring_cross_ref WHERE itemId = :itemId")
    suspend fun deleteFlavoringCrossRefByItemId(itemId: Int)

    @Query("DELETE FROM flavoring WHERE flavoringId NOT IN (SELECT flavoringId FROM items_flavoring_cross_ref)")
    suspend fun deleteOrphanedFlavoring()


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
    // Get all items ids //
    @Query("SELECT id FROM items ORDER BY id ASC")
    fun getAllItemIds(): List<Int>

    // Get all components flow //
    @Transaction
    @Query("SELECT * FROM components ORDER BY componentName ASC")
    fun getAllComponents(): Flow<List<Components>>

    // Get all flavoring flow //
    @Query("SELECT * FROM flavoring ORDER BY flavoringName ASC")
    fun getAllFlavoring(): Flow<List<Flavoring>>

    // Get all items with components and tins flow //
    @Transaction
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getEverythingStream(): Flow<List<ItemsComponentsAndTins>>


    /** Get single item **/
    // Get item by id //
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemStream(id: Int): Flow<Items?>

    // Get item details by id //
    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins?>

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

    // Get flavoring by item id //
    @Transaction
    @Query(
        """
        SELECT T2.* 
        FROM items_flavoring_cross_ref AS T1 
        INNER JOIN flavoring AS T2
        ON T1.flavoringId = T2.flavoringId
        WHERE T1.itemId = :itemId
        """
    )
    fun getFlavoringForItemStream(itemId: Int): Flow<List<Flavoring>>

    // Get tins by item id //
    @Query("SELECT * FROM tins WHERE itemsId = :itemsId")
    fun getTinsForItemStream(itemsId: Int): Flow<List<Tins>>

    @Query("SELECT id FROM items WHERE brand = :brand AND blend = :blend")
    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    @Query("SELECT componentId FROM components WHERE componentName = :name")
    suspend fun getComponentIdByName(name: String): Int?

    @Transaction
    @Query("SELECT flavoringId FROM flavoring WHERE flavoringName = :name")
    suspend fun getFlavoringIdByName(name: String): Int?


    /** Checks **/
    // Check if item exists //
    @Query("SELECT EXISTS(SELECT * FROM items WHERE brand = :brand AND blend = :blend)")
    suspend fun exists(brand: String, blend: String): Boolean


    /** Get any BY value **/
    @Query("SELECT * FROM items WHERE brand = :brand AND blend = :blend")
    fun getItemByIndex(brand: String, blend: String): Items?

}