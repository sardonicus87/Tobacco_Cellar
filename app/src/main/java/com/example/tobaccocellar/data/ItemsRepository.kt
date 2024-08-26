package com.example.tobaccocellar.data

import com.example.tobaccocellar.ui.stats.BrandCount
import com.example.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow

interface ItemsRepository {

    /* TODO organize ItemsRepo functions */

    /** Get all items **/
    fun getAllItemsStream(): Flow<List<Items>>

    fun getAllItemsExport(): List<Items>

    /** Get single item **/
    fun getItemStream(id: Int): Flow<Items?>

    suspend fun getItemIdByIndex(brand: String, blend: String): Int

    /** Database operations **/
    suspend fun insertItem(item: Items)

    suspend fun insertMultiple(items: List<Items>): List<Long>

    suspend fun deleteItem(item: Items)

    suspend fun updateItem(item: Items)

    /** Checks **/
    suspend fun exists(brand: String, blend: String): Boolean

    fun getAllBrandsStream(): Flow<List<String>>

    /** Get  **/

    fun getItemsCount(): Flow<Int>

    fun getBrandsCount(): Flow<Int>

    fun getTotalByBrand(): Flow<List<BrandCount>>

    fun getTotalByType(): Flow<List<TypeCount>>

    fun getTotalFavorite(): Flow<Int>

    fun getTotalDislike(): Flow<Int>

    fun getTotalQuantity(): Flow<Int>

    fun getTotalZeroQuantity(): Flow<Int>


    /** Get items BY functions (sort and filter) **/
    fun getItemsByBrand(brand: String): Flow<List<Items>>

    fun getItemsByType(type: String): Flow<List<Items>>

    fun getItemsByFavorite(): Flow<List<Items>>

    fun getItemsByDisliked(): Flow<List<Items>>





}