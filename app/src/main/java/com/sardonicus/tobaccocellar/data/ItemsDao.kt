package com.sardonicus.tobaccocellar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
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
    suspend fun insert(item: Items)

    // Batch add items //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultiple(items: List<Items>): LongArray

    // Update item in database //
    @Update
    suspend fun update(item: Items)

    // Delete item //
    @Delete
    suspend fun delete(item: Items)

    // Delete all items //
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()


    /** Get all items **/
    // Get all items flow //
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItems(): Flow<List<Items>>

    // Get all items list //
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItemsExport(): List<Items>


    /** Get single item **/
    // Get item by id //
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItem(id: Int): Flow<Items>

    @Query("SELECT id FROM items WHERE brand = :brand AND blend = :blend")
    suspend fun getItemIdByIndex(brand: String, blend: String): Int


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

    // Get all favorites flow //
    @Query("SELECT favorite FROM items")
    fun getAllFavorites(): Flow<List<Boolean>>

    // Get all dislikeds flow //
    @Query("SELECT disliked FROM items")
    fun getAllDislikeds(): Flow<List<Boolean>>

    // Get all zero quantities flow //
    @Query("SELECT quantity FROM items WHERE quantity = 0")
    fun getAllZeroQuantity(): Flow<List<Boolean>>


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


    /** Special functions **/
    // Filter function //
    @RawQuery(observedEntities = [Items::class])
    fun getFilteredItems(query: SupportSQLiteQuery): Flow<List<Items>>


}