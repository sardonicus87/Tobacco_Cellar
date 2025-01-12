package com.sardonicus.tobaccocellar.data

import com.sardonicus.tobaccocellar.ui.stats.BrandCount
import com.sardonicus.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow

class OfflineItemsRepository(private val itemsDao: ItemsDao) : ItemsRepository {

    /** Database operations **/
    // Items //
    override suspend fun insertItem(item: Items): Long {
        return itemsDao.insert(item)
    }

    override suspend fun insertMultiple(items: List<Items>): List<Long> {
        return itemsDao.insertMultiple(items).toList()
    }

    override suspend fun updateItem(item: Items) = itemsDao.update(item)

    override suspend fun deleteItem(item: Items) = itemsDao.delete(item)

    override suspend fun deleteAllItems() = itemsDao.deleteAllItems()

    // Components //
    override suspend fun insertComponent(component: Components): Long {
        return itemsDao.insertComponent(component)
    }

    override suspend fun insertComponentsCrossRef(crossRef: ItemsComponentsCrossRef) {
        itemsDao.insertComponentsCrossRef(crossRef)
    }

    override suspend fun deleteComponentsCrossRef(itemId: Int, componentId: Int) {
        itemsDao.deleteComponentsCrossRef(itemId, componentId)
    }

//    override suspend fun updateComponents(id: Int, components: List<String>) {
//        itemsDao.updateComponents(id, components)
//    }
//
//    override suspend fun updateComponentCrossRef(id: Int, components: List<String>) {
//        itemsDao.updateComponentCrossRef(id, components)
//    }


    /** Get all items **/
    override fun getAllItemsStream(): Flow<List<Items>> = itemsDao.getAllItems()

    override fun getAllItemsExport(): List<Items> = itemsDao.getAllItemsExport()

    override fun getAllComponentsStream(): Flow<List<Components>> = itemsDao.getAllComponents()


    /** Get single item **/
    override fun getItemStream(id: Int): Flow<Items?> = itemsDao.getItem(id)

    override fun getComponentsForItemStream(id: Int): Flow<List<Components>> = itemsDao.getComponentsForItemStream(id)

    override suspend fun getItemIdByIndex(brand: String, blend: String) =
        itemsDao.getItemIdByIndex(brand, blend)

    override suspend fun getComponentIdByName(name: String): Int? = itemsDao.getComponentIdByName(name)


    /** Checks **/
    override suspend fun exists(brand: String, blend: String): Boolean {
        return itemsDao.exists(brand, blend)
    }


    /** Get all column/value functions **/
    override fun getAllBrandsStream(): Flow<List<String>> = itemsDao.getAllBrands()

    override fun getAllBlendsStream(): Flow<List<String>> = itemsDao.getAllBlends()

    override fun getAllTypesStream(): Flow<List<String>> = itemsDao.getAllTypes()

    override fun getAllSubGenresStream(): Flow<List<String>> = itemsDao.getAllSubGenres()

    override fun getAllCutsStream(): Flow<List<String>> = itemsDao.getAllCuts()

    override fun getAllFavoritesStream(): Flow<List<Boolean>> = itemsDao.getAllFavorites()

    override fun getAllDislikeStream(): Flow<List<Boolean>> = itemsDao.getAllDislikeds()

    override fun getAllZeroQuantityStream(): Flow<List<Boolean>> = itemsDao.getAllZeroQuantity()

    override fun getAllCompNamesStream(): Flow<List<String>> = itemsDao.getAllCompNames()


    /** Get counts **/
    override fun getItemsCount(): Flow<Int> = itemsDao.getItemsCount()

    override fun getBrandsCount(): Flow<Int> = itemsDao.getBrandsCount()

    override fun getTotalByBrand(): Flow<List<BrandCount>> = itemsDao.getTotalByBrand()

    override fun getTotalByType(): Flow<List<TypeCount>> = itemsDao.getTotalByType()

    override fun getTotalFavorite(): Flow<Int> = itemsDao.getTotalFavorite()

    override fun getTotalDislike(): Flow<Int> = itemsDao.getTotalDislike()

    override fun getTotalQuantity(): Flow<Int> = itemsDao.getTotalQuantity()

    override fun getTotalZeroQuantity(): Flow<Int> = itemsDao.getTotalZeroQuantity()


    /** Get any by value **/
    override fun getItemsByBrand(brand: String): Flow<List<Items>> = itemsDao.getItemsByBrand(brand)

    override fun getItemsByBlend(blend: String): Flow<List<Items>> = itemsDao.getItemsByBlend(blend)

    override fun getItemsByType(type: String): Flow<List<Items>> = itemsDao.getItemsByType(type)

    override fun getItemsByQuantity(): Flow<List<Items>> = itemsDao.getItemsByQuantity()

    override fun getItemsByFavorite(): Flow<List<Items>> = itemsDao.getItemsByFavorite()

    override fun getItemsByDisliked(): Flow<List<Items>> = itemsDao.getItemsByDisliked()

    override fun getItemsByZeroQuantity(): Flow<List<Items>> = itemsDao.getItemsByZeroQuantity()

    override fun getItemByIndex(brand: String, blend: String): Items? = itemsDao.getItemByIndex(brand, blend)


//    /** Special functions **/
//    // filtering return function //
//    override suspend fun getFilteredItems(
//        brands: List<String>?,
//        types: List<String>?,
//        favorites: Boolean?,
//        dislikeds: Boolean?,
//        neutral: Boolean?,
//        nonNeutral: Boolean?,
//        inStock: Boolean?,
//        outOfStock: Boolean?,
//    ): Flow<List<Items>> {
//        val queryBuilder = SupportSQLiteQueryBuilder.builder("items")
//        val args = mutableListOf<Any>()
//        val whereClauses = mutableListOf<String>()
//
//        if (!brands.isNullOrEmpty()) {
//            val brandClause = StringBuilder()
//            brandClause.append("brand IN (")
//            for (i in brands.indices) {
//                brandClause.append("?")
//                if (i < brands.size - 1) {
//                    brandClause.append(", ")
//                }
//                args.add(brands[i])
//            }
//            brandClause.append(")")
//            whereClauses.add(brandClause.toString())
//        }
//
//        if (!types.isNullOrEmpty()) {
//            val typeClause = StringBuilder()
//            typeClause.append("type IN (")
//            for (i in types.indices) {
//                typeClause.append("?")
//                if (i < types.size - 1) {
//                    typeClause.append(", ")
//                }
//                args.add(types[i])
//            }
//            typeClause.append(")")
//            whereClauses.add(typeClause.toString())
//        }
//
//        if (favorites != null && favorites) {
//            if (favorites) {
//                whereClauses.add("favorite = ?")
//                args.add(1)
//            }
//        }
//
//        if (dislikeds != null && dislikeds) {
//            if (dislikeds) {
//                whereClauses.add("disliked = ?")
//                args.add(1)
//            }
//        }
//
//        if (neutral != null && neutral) {
//            whereClauses.add("favorite = ? AND disliked = ?")
//            args.add(0)
//            args.add(0)
//        }
//
//        if (nonNeutral != null && nonNeutral) {
//            whereClauses.add("favorite = ? OR disliked = ?")
//            args.add(1)
//            args.add(1)
//        }
//
//        if (inStock != null && inStock) {
//            whereClauses.add("quantity > 0")
//        }
//
//        if (outOfStock != null) {
//            if (outOfStock) {
//                whereClauses.add("quantity = 0")
//            }
//        }
//
//        if (whereClauses.isNotEmpty()) {
//            val whereClause = whereClauses.joinToString(" AND ")
//            queryBuilder.selection(whereClause, args.toTypedArray())
//        }
//
//        val query = queryBuilder.create()
//        return itemsDao.getFilteredItems(query)
//    }
}