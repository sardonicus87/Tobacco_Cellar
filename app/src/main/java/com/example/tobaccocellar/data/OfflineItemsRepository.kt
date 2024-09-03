package com.example.tobaccocellar.data

import android.util.Log
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.example.tobaccocellar.ui.stats.BrandCount
import com.example.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow

class OfflineItemsRepository(private val itemsDao: ItemsDao) : ItemsRepository {

    /* TODO organize OfflineItemsRepo functions */

    /** Database operations **/
    override suspend fun insertItem(item: Items) = itemsDao.insert(item)

    override suspend fun insertMultiple(items: List<Items>): List<Long> {
        return itemsDao.insertMultiple(items).toList()
    }

    override suspend fun updateItem(item: Items) = itemsDao.update(item)

    override suspend fun deleteItem(item: Items) = itemsDao.delete(item)


    /** Get all items **/
    override fun getAllItemsStream(): Flow<List<Items>> = itemsDao.getAllItems()

    override fun getAllItemsExport(): List<Items> = itemsDao.getAllItemsExport()


    /** Get single item **/
    override fun getItemStream(id: Int): Flow<Items?> = itemsDao.getItem(id)

    override suspend fun getItemIdByIndex(brand: String, blend: String) =
        itemsDao.getItemIdByIndex(brand, blend)


    /** Checks **/
    override suspend fun exists(brand: String, blend: String): Boolean {
        return itemsDao.exists(brand, blend)
    }


    /** Get all column/value functions **/
    override fun getAllBrandsStream(): Flow<List<String>> = itemsDao.getAllBrands()

    override fun getAllTypesStream(): Flow<List<String>> = itemsDao.getAllTypes()

    override fun getAllFavoritesStream(): Flow<List<Boolean>> = itemsDao.getAllFavorites()

    override fun getAllDislikeStream(): Flow<List<Boolean>> = itemsDao.getAllDislikeds()

    override fun getAllZeroQuantityStream(): Flow<List<Boolean>> = itemsDao.getAllZeroQuantity()


    /** Get  **/
    override fun getItemsCount(): Flow<Int> = itemsDao.getItemsCount()

    override fun getBrandsCount(): Flow<Int> = itemsDao.getBrandsCount()

    override fun getTotalByBrand(): Flow<List<BrandCount>> = itemsDao.getTotalByBrand()

    override fun getTotalByType(): Flow<List<TypeCount>> = itemsDao.getTotalByType()

    override fun getTotalFavorite(): Flow<Int> = itemsDao.getTotalFavorite()

    override fun getTotalDislike(): Flow<Int> = itemsDao.getTotalDislike()

    override fun getTotalQuantity(): Flow<Int> = itemsDao.getTotalQuantity()

    override fun getTotalZeroQuantity(): Flow<Int> = itemsDao.getTotalZeroQuantity()


    /** Get items BY functions (sort and filter) **/
    override fun getItemsByBrand(brand: String): Flow<List<Items>> = itemsDao.getItemsByBrand(brand)

    override fun getItemsByType(type: String): Flow<List<Items>> = itemsDao.getItemsByType(type)

    override fun getItemsByFavorite(): Flow<List<Items>> = itemsDao.getItemsByFavorite()

    override fun getItemsByDisliked(): Flow<List<Items>> = itemsDao.getItemsByDisliked()

    override fun getItemsByZeroQuantity(): Flow<List<Items>> = itemsDao.getItemsByZeroQuantity()


    /** Filtering function **/
    override fun getFilteredItems(
        brands: List<String>?,
        types: List<String>?,
        favorites: Boolean?,
        dislikeds: Boolean?,
        outOfStock: Boolean?,
    ): Flow<List<Items>> {
        val queryBuilder = SupportSQLiteQueryBuilder.builder("items")
        val args = mutableListOf<Any>()
        val whereClauses = mutableListOf<String>()

        if (!brands.isNullOrEmpty()) {
            val brandClause = StringBuilder()
            brandClause.append("brand IN (")
            for (i in brands.indices) {
                brandClause.append("?")
                if (i < brands.size - 1) {
                    brandClause.append(", ")
                }
                args.add(brands[i])
            }
            brandClause.append(")")
            whereClauses.add(brandClause.toString())
        }

        if (!types.isNullOrEmpty()) {
            val typeClause = StringBuilder()
            typeClause.append("type IN (")
            for (i in types.indices) {
                typeClause.append("?")
                if (i < types.size - 1) {
                    typeClause.append(", ")
                }
                args.add(types[i])
            }
            typeClause.append(")")
            whereClauses.add(typeClause.toString())
        }

        if (favorites != null) {
            if (favorites) {
                whereClauses.add("favorite = ?")
                args.add(1)
            }
        }

        if (dislikeds != null) {
            if (dislikeds) {
                whereClauses.add("disliked = ?")
                args.add(1)
            }
        }

        if (outOfStock != null) {
            if (outOfStock) {
                whereClauses.add("quantity = 0")
            }
        }

        if (whereClauses.isNotEmpty()) {
            val whereClause = whereClauses.joinToString(" AND ")
            queryBuilder.selection(whereClause, args.toTypedArray())
        }

        val query = queryBuilder.create()

        // Log the query details
        Log.d("OfflineItemsRepository", "Query: ${query.sql}")
        for (i in 0 until query.argCount) {
            Log.d("OfflineItemsRepository", "Arg $i: ${args[i]}")
        }

        return itemsDao.getFilteredItems(query)
    }


//    (",") { "?" }})")


//    override fun getFilteredItems(
//        brands: List<String>?,
//        types: List<String>?,
//        favorites: Boolean?,
//        dislikeds: Boolean?,
//        outOfStock: Boolean?,
//    ): Flow<List<Items>> =
//        itemsDao.getFilteredItems(
//            brands,
//            types,
//            favorites,
//            dislikeds,
//            outOfStock,
//        )

//    if (favorites != null) {
//        whereClauses.add("favorite = ?")
//        args.add(if (favorites) 1 else 0)
//    }
//
//    if (dislikeds != null) {
//        whereClauses.add("disliked = ?")
//        args.add(if (dislikeds) 1 else 0)
//    }

    override fun getItemsByQuantity(): Flow<List<Items>> = itemsDao.getItemsByQuantity()






}