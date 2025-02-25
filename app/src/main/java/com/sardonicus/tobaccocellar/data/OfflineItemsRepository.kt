package com.sardonicus.tobaccocellar.data

import androidx.sqlite.db.SimpleSQLiteQuery
import com.sardonicus.tobaccocellar.ui.items.formatLongDate
import com.sardonicus.tobaccocellar.ui.stats.BrandCount
import com.sardonicus.tobaccocellar.ui.stats.TypeCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class OfflineItemsRepository(private val itemsDao: ItemsDao) : ItemsRepository {

    /** Database operations **/
    // Items //
    override suspend fun insertItem(item: Items): Long {
        return itemsDao.insert(item)
    }

    override suspend fun insertMultipleItems(items: List<Items>): List<Long> {
        return itemsDao.insertMultipleItems(items).toList()
    }

    override suspend fun updateItem(item: Items) = itemsDao.update(item)

    override suspend fun updateMultipleItems(items: List<Items>) {
        for (item in items) {
            itemsDao.update(item)
        }
    }

    override suspend fun deleteItem(item: Items) = itemsDao.delete(item)

    override suspend fun deleteAllItems() = itemsDao.deleteAllItems()

    override suspend fun optimizeDatabase() {
        itemsDao.deleteOrphanedComponents()
        itemsDao.vacuumDatabase(SimpleSQLiteQuery("VACUUM"))
    }


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

    override suspend fun deleteComponentsCrossRefByItemId(itemId: Int) {
        itemsDao.deleteComponentsCrossRefByItemId(itemId)
    }

    override suspend fun insertMultipleComponents(components: List<Components>): List<Long> {
        return itemsDao.insertMultipleComponents(components).toList()
    }

    override suspend fun insertMultipleComponentsCrossRef(crossRefs: List<ItemsComponentsCrossRef>) {
        itemsDao.insertMultipleComponentsCrossRef(crossRefs)
    }


    // Tins //
    override suspend fun insertTin(tin: Tins): Long {
        return itemsDao.insertTin(tin)
    }

    override suspend fun updateTin(tin: Tins) {
        itemsDao.update(tin)
    }

    override suspend fun deleteTin(tinId: Int) {
        itemsDao.deleteTin(tinId)
    }

    override suspend fun insertMultipleTins(tins: List<Tins>): List<Long> {
        return itemsDao.insertMultipleTins(tins).toList()
    }

    override suspend fun deleteAllTinsForItem(itemId: Int) {
        itemsDao.deleteAllTinsForItem(itemId)
    }

    override suspend fun getTinExportData(): List<TinExportData> {
        val items = itemsDao.getAllItemsExport()
        val tinExportData = mutableListOf<TinExportData>()

        for (item in items) {
            val components = itemsDao.getComponentsForItemStream(item.id).first().joinToString(", ") { it.componentName }
            val tins = itemsDao.getTinsForItemStream(item.id).first()

            if (tins.isNotEmpty()) {
                for (tin in tins) {
                    val tinExport = TinExportData(
                        brand = item.brand,
                        blend = item.blend,
                        type = item.type,
                        subGenre = item.subGenre,
                        cut = item.cut,
                        favorite = item.favorite,
                        disliked = item.disliked,
                        inProduction = item.inProduction,
                        notes = item.notes,
                        components = components,
                        container = tin.container,
                        quantity = "${tin.tinQuantity} ${tin.unit}",
                        manufactureDate = formatLongDate(tin.manufactureDate),
                        cellarDate = formatLongDate(tin.cellarDate),
                        openDate = formatLongDate(tin.openDate)
                    )
                    tinExportData.add(tinExport)
                }
            } else {
                val tinExport = TinExportData(
                    brand = item.brand,
                    blend = item.blend,
                    type = item.type,
                    subGenre = item.subGenre,
                    cut = item.cut,
                    favorite = item.favorite,
                    disliked = item.disliked,
                    inProduction = item.inProduction,
                    notes = item.notes,
                    components = components,
                    container = "",
                    quantity = "",
                    manufactureDate = "",
                    cellarDate = "",
                    openDate = ""
                )
                tinExportData.add(tinExport)
            }
        }
        return tinExportData
    }


    /** Get all items **/
    override fun getAllItemsStream(): Flow<List<Items>> = itemsDao.getAllItemsStream()

    override fun getAllItemIds(): List<Int> = itemsDao.getAllItemIds()

    override fun getAllItemsExport(): List<Items> = itemsDao.getAllItemsExport()

    override suspend fun getAllItemsWithComponents(): List<ItemsWithComponents> {
        val items = itemsDao.getAllItemsExport()
        return items.map {
            val components = itemsDao.getComponentsForItemStream(it.id).first()
            ItemsWithComponents(it, components)
        }
    }

    override fun getAllComponentsStream(): Flow<List<Components>> = itemsDao.getAllComponents()

    override fun getAllTinsStream(): Flow<List<Tins>> = itemsDao.getAllTins()

    override fun getAllItemsComponentsCrossRefStream(): Flow<List<ItemsComponentsCrossRef>> = itemsDao.getAllItemsComponentsCrossRef()

    override fun getEverythingStream(): Flow<List<ItemsComponentsAndTins>> = itemsDao.getEverythingStream()


    /** Get single item **/
    override fun getItemStream(id: Int): Flow<Items?> = itemsDao.getItemStream(id)

    override fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins?> = itemsDao.getItemDetailsStream(id)

    override fun getComponentsForItemStream(id: Int): Flow<List<Components>> = itemsDao.getComponentsForItemStream(id)

    override fun getTinsForItemStream(id: Int): Flow<List<Tins>> = itemsDao.getTinsForItemStream(id)

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

    override fun getAllTinContainersStream(): Flow<List<String>> = itemsDao.getAllTinContainers()

    override fun getAllSubgenresStream(): Flow<List<String>> = itemsDao.getAllSubgenres()


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

    override fun getComponentsByName(components: List<String>): Flow<List<Components>> = itemsDao.getComponentsByName(components)
}