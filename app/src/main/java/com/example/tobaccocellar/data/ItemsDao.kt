package com.example.tobaccocellar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tobaccocellar.ui.stats.BrandCount
import com.example.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemsDao {

    /* TODO organize DAO functions */

    /** Insertion functions **/
    // Add item to database //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Items)

    // Batch add items to database from CSV //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultiple(items: List<Items>): LongArray

    /** Editing functions **/
    // Update item in database //
    @Update
    suspend fun update(item: Items)

    // Delete item from database //
    @Delete
    suspend fun delete(item: Items)

    /** Getting functions **/
    // Get item by id //
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItem(id: Int): Flow<Items>

    // Get all items flow //
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItems(): Flow<List<Items>>

    // Get all items list //
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItemsExport(): List<Items>

    // Get all brands flow //
    @Query("SELECT DISTINCT brand FROM items ORDER BY brand ASC")
    fun getAllBrands(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT * FROM items WHERE brand = :brand AND blend = :blend)")
    suspend fun exists(brand: String, blend: String): Boolean

    /** Getting by value functions **/
    @Query("SELECT id FROM items WHERE brand = :brand AND blend = :blend")
    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    @Query("SELECT * FROM items WHERE brand = :brand")
    fun getItemsByBrand(brand: String): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE blend = :blend")
    fun getItemsByBlend(blend: String): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE type = :type")
    fun getItemsByType(type: String): Flow<List<Items>>

    /** Getting count functions **/
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

    // total quantity //
    @Query("SELECT SUM(quantity) FROM items")
    fun getTotalQuantity(): Flow<Int>

    // total favorite //
    @Query("SELECT SUM(favorite) FROM items")
    fun getTotalFavorite(): Flow<Int>

    // total dislike //
    @Query("SELECT SUM(disliked) FROM items")
    fun getTotalDislike(): Flow<Int>

    // total zero quantity //
    @Query("SELECT COUNT(*) FROM items WHERE quantity = 0")
    fun getTotalZeroQuantity(): Flow<Int>


    /** Getting by favorite functions **/
    @Query("SELECT * FROM items WHERE favorite = 1")
    fun getItemsByFavorite(): Flow<List<Items>>


    @Query("SELECT * FROM items WHERE disliked = 1")
    fun getItemsByDisliked(): Flow<List<Items>>
}