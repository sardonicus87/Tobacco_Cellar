package com.example.tobaccocellar.data

import com.example.tobaccocellar.ui.stats.BrandCount
import com.example.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow

interface ItemsRepository {

    /** Database operations **/
    suspend fun insertItem(item: Items)

    suspend fun insertMultiple(items: List<Items>): List<Long>

    suspend fun updateItem(item: Items)

    suspend fun deleteItem(item: Items)

    suspend fun deleteAllItems()


    /** Get all items **/
    fun getAllItemsStream(): Flow<List<Items>>

    fun getAllItemsExport(): List<Items>


    /** Get single item **/
    fun getItemStream(id: Int): Flow<Items?>

    suspend fun getItemIdByIndex(brand: String, blend: String): Int


    /** Checks **/
    suspend fun exists(brand: String, blend: String): Boolean


    /** Get all column/value functions **/
    fun getAllBrandsStream(): Flow<List<String>>

    fun getAllBlendsStream(): Flow<List<String>>

    fun getAllTypesStream(): Flow<List<String>>

    fun getAllFavoritesStream(): Flow<List<Boolean>>

    fun getAllDislikeStream(): Flow<List<Boolean>>

    fun getAllZeroQuantityStream(): Flow<List<Boolean>>


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



    /** Special functions **/
    fun getFilteredItems(
        brands: List<String>?,
        types: List<String>?,
        favorites: Boolean?,
        neutral: Boolean?,
        dislikeds: Boolean?,
        inStock: Boolean?,
        outOfStock: Boolean?,
    ): Flow<List<Items>>

}