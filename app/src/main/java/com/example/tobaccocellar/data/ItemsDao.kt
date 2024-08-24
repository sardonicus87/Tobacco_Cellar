package com.example.tobaccocellar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Items)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultiple(items: List<Items>): LongArray

    @Update
    suspend fun update(item: Items)

    @Delete
    suspend fun delete(item: Items)

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItem(id: Int): Flow<Items>

    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItems(): Flow<List<Items>>

    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItemsExport(): List<Items>

    @Query("SELECT DISTINCT brand FROM items ORDER BY brand ASC")
    fun getAllBrands(): Flow<List<String>>

//    @Query("SELECT DISTINCT brand FROM items ORDER BY brand ASC")
//    fun getAllBrands(): Flow<List<BrandState>>

    @Query("SELECT EXISTS(SELECT * FROM items WHERE brand = :brand AND blend = :blend)")
    suspend fun exists(brand: String, blend: String): Boolean

    @Query("SELECT id FROM items WHERE brand = :brand AND blend = :blend")
    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    @Query("SELECT * FROM items WHERE brand = :brand")
    fun getItemsByBrand(brand: String): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE blend = :blend")
    fun getItemsByBlend(blend: String): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE type = :type")
    fun getItemsByType(type: String): Flow<List<Items>>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemsCount(): Int


}