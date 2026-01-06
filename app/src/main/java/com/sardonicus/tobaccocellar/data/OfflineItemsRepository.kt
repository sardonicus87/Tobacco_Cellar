package com.sardonicus.tobaccocellar.data

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sardonicus.tobaccocellar.data.multiDeviceSync.PendingSyncOperation
import com.sardonicus.tobaccocellar.data.multiDeviceSync.PendingSyncOperationDao
import com.sardonicus.tobaccocellar.data.multiDeviceSync.SyncStateManager
import com.sardonicus.tobaccocellar.data.multiDeviceSync.UploadSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class OfflineItemsRepository(
    private var itemsDao: ItemsDao,
    private val pendingSyncOperationDao: PendingSyncOperationDao,
    private val preferencesRepo: PreferencesRepo,
    private val context: Context
) : ItemsRepository {

    private val dbVersion = TobaccoDatabase.getDatabaseVersion(context)

    /** Database operations **/
    // Items //
    override suspend fun insertItem(item: Items): Long {
        val itemId = itemsDao.insert(item)

        val operation = PendingSyncOperation(
            operationType = "INSERT",
            entityType = "Items",
            entityId = itemId.toString(), // just for debugging, not used meaningfully
            payload = Json.encodeToString(item.copy(id = itemId.toInt())),
            dbVersion = dbVersion
        )
        logOperation(operation)
        scheduleSyncUpload()
        return itemId
    }

    override suspend fun insertMultipleItems(items: List<Items>): List<Long> {
        val itemIds = itemsDao.insertMultipleItems(items).toList()

        val itemsInserted = items.zip(itemIds)

        for ((item, id) in itemsInserted) {
            val operation = PendingSyncOperation(
                operationType = "INSERT",
                entityType = "Items",
                entityId = id.toString(),
                payload = Json.encodeToString(item.copy(id = id.toInt())),
                dbVersion = dbVersion
            )
            logOperation(operation)
        }

        scheduleSyncUpload()
        return itemIds
    }

    override suspend fun updateItem(item: Items) {
        itemsDao.update(item)

        val operation = PendingSyncOperation(
            operationType = "UPDATE",
            entityType = "Items",
            entityId = item.id.toString(),
            payload = Json.encodeToString(item),
            dbVersion = dbVersion
        )

        logOperation(operation)
        scheduleSyncUpload()
    }

    override suspend fun updateMultipleItems(items: List<ItemsComponentsAndTins>) {
        for (item in items) {
            itemsDao.updateICT(item.items, item.components, item.tins)

            val operation = PendingSyncOperation(
                operationType = "UPDATE",
                entityType = "Items",
                entityId = item.items.id.toString(),
                payload = Json.encodeToString(item.items),
                dbVersion = dbVersion
            )
            logOperation(operation)
        }

        scheduleSyncUpload()
    }

    override suspend fun deleteItem(item: Items) {
        val operation = PendingSyncOperation(
            operationType = "DELETE",
            entityType = "Items",
            entityId = item.id.toString(),
            payload = Json.encodeToString(item),
            dbVersion = dbVersion
        )

        logOperation(operation)
        itemsDao.delete(item)
        scheduleSyncUpload()
    }

    override suspend fun deleteAllItems() {
        itemsDao.deleteAllItems()
        itemsDao.deleteOrphanedComponents()
        itemsDao.deleteOrphanedFlavoring()
    }

    override suspend fun optimizeDatabase() {
        itemsDao.deleteOrphanedComponents()
        itemsDao.deleteOrphanedFlavoring()
        itemsDao.vacuumDatabase(SimpleSQLiteQuery("VACUUM"))
    }


    // Components //
    override suspend fun insertComponent(component: Components): Long {
        val componentId = itemsDao.insertComponent(component)
        if (componentId != -1L) {
            val operation = PendingSyncOperation(
                operationType = "INSERT",
                entityType = "Components",
                entityId = componentId.toString(),
                payload = Json.encodeToString(component),
                dbVersion = dbVersion
            )
            logOperation(operation)
            scheduleSyncUpload()
        }

        return componentId
    }

    override suspend fun insertComponentsCrossRef(crossRef: ItemsComponentsCrossRef) {
        itemsDao.insertComponentsCrossRef(crossRef)

        val parentItem = itemsDao.getItemById(crossRef.itemId) ?: return
        val component = itemsDao.getComponentById(crossRef.componentId) ?: return
        val syncPayload = CrossRefSyncPayload(
            itemBrand = parentItem.brand,
            itemBlend = parentItem.blend,
            relatedEntityName = component.componentName
        )
        val operation = PendingSyncOperation(
            operationType = "INSERT",
            entityType = "ItemsComponentsCrossRef",
            entityId = "${crossRef.itemId}-${crossRef.componentId}",
            payload = Json.encodeToString(syncPayload),
            dbVersion = dbVersion
        )
        logOperation(operation)
        scheduleSyncUpload()
    }

    override suspend fun deleteComponentsCrossRef(crossRef: ItemsComponentsCrossRef) {
        val parentItem = itemsDao.getItemById(crossRef.itemId) ?: return
        val component = itemsDao.getComponentById(crossRef.componentId) ?: return

        itemsDao.deleteComponentsCrossRef(crossRef)

        val syncPayload = CrossRefSyncPayload(
            itemBrand = parentItem.brand,
            itemBlend = parentItem.blend,
            relatedEntityName = component.componentName
        )
        val operation = PendingSyncOperation(
            operationType = "DELETE",
            entityType = "ItemsComponentsCrossRef",
            entityId = "${crossRef.itemId}-${crossRef.componentId}",
            payload = Json.encodeToString(syncPayload),
            dbVersion = dbVersion
        )
        logOperation(operation)
        scheduleSyncUpload()
    }

    override suspend fun deleteComponentsCrossRefByItemId(itemId: Int) {
        val parentItem = itemsDao.getItemById(itemId) ?: return
        val components = itemsDao.getComponentsForItemStream(itemId).first()
        for (component in components) {
            val syncPayload = CrossRefSyncPayload(
                itemBrand = parentItem.brand,
                itemBlend = parentItem.blend,
                relatedEntityName = component.componentName
            )
            val operation = PendingSyncOperation(
                operationType = "DELETE",
                entityType = "ItemsComponentsCrossRef",
                entityId = "${itemId}-${component.componentId}",
                payload = Json.encodeToString(syncPayload),
                dbVersion = dbVersion
            )
            logOperation(operation)
        }

        itemsDao.deleteComponentsCrossRefByItemId(itemId)
        scheduleSyncUpload()

    }


    // Flavoring //
    override suspend fun insertFlavoring(flavoring: Flavoring): Long {
        val flavoringId = itemsDao.insertFlavoring(flavoring)
        if (flavoringId != -1L) {
            val operation = PendingSyncOperation(
                operationType = "INSERT",
                entityType = "Flavoring",
                entityId = flavoringId.toString(),
                payload = Json.encodeToString(flavoring),
                dbVersion = dbVersion
            )
            logOperation(operation)
            scheduleSyncUpload()
        }

        return flavoringId
    }

    override suspend fun insertFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef) {
        itemsDao.insertFlavoringCrossRef(crossRef)

        val parentItem = itemsDao.getItemById(crossRef.itemId) ?: return
        val flavoring = itemsDao.getFlavoringById(crossRef.flavoringId) ?: return
        val syncPayload = CrossRefSyncPayload(
            itemBrand = parentItem.brand,
            itemBlend = parentItem.blend,
            relatedEntityName = flavoring.flavoringName
        )
        val operation = PendingSyncOperation(
            operationType = "INSERT",
            entityType = "ItemsFlavoringCrossRef",
            entityId = "${crossRef.itemId}-${crossRef.flavoringId}",
            payload = Json.encodeToString(syncPayload),
            dbVersion = dbVersion
        )
        logOperation(operation)
        scheduleSyncUpload()
    }

    override suspend fun deleteFlavoringCrossRef(crossRef: ItemsFlavoringCrossRef) {
        val parentItem = itemsDao.getItemById(crossRef.itemId) ?: return
        val flavoring = itemsDao.getFlavoringById(crossRef.flavoringId) ?: return

        itemsDao.deleteFlavoringCrossRef(crossRef)

        val syncPayload = CrossRefSyncPayload(
            itemBrand = parentItem.brand,
            itemBlend = parentItem.blend,
            relatedEntityName = flavoring.flavoringName
        )
        val operation = PendingSyncOperation(
            operationType = "DELETE",
            entityType = "ItemsFlavoringCrossRef",
            entityId = "${crossRef.itemId}-${crossRef.flavoringId}",
            payload = Json.encodeToString(syncPayload),
            dbVersion = dbVersion
        )
        logOperation(operation)
        scheduleSyncUpload()
    }

    override suspend fun deleteFlavoringCrossRefByItemId(itemId: Int) {
        val parentItem = itemsDao.getItemById(itemId) ?: return
        val flavorings = itemsDao.getFlavoringForItemStream(itemId).first()
        for (flavoring in flavorings) {
            val syncPayload = CrossRefSyncPayload(
                itemBrand = parentItem.brand,
                itemBlend = parentItem.blend,
                relatedEntityName = flavoring.flavoringName
            )
            val operation = PendingSyncOperation(
                operationType = "DELETE",
                entityType = "ItemsFlavoringCrossRef",
                entityId = "${itemId}-${flavoring.flavoringId}",
                payload = Json.encodeToString(syncPayload),
                dbVersion = dbVersion
            )
            logOperation(operation)
        }

        itemsDao.deleteFlavoringCrossRefByItemId(itemId)
        scheduleSyncUpload()
    }


    // Tins //
    override suspend fun insertTin(tin: Tins): Long {
        val tinId = itemsDao.insertTin(tin)
        val parentItem = itemsDao.getItemById(tin.itemsId) ?: return tinId
        val syncPayload = TinSyncPayload(
            itemBrand = parentItem.brand,
            itemBlend = parentItem.blend,
            tin = tin.copy(tinId = tinId.toInt())
        )
        val operation = PendingSyncOperation(
            operationType = "INSERT",
            entityType = "Tins",
            entityId = tinId.toString(),
            payload = Json.encodeToString(syncPayload),
            dbVersion = dbVersion
        )

        logOperation(operation)
        scheduleSyncUpload()

        return tinId
    }

    override suspend fun updateTin(tin: Tins) {
        itemsDao.update(tin)

        val parentItem = itemsDao.getItemById(tin.itemsId) ?: return
        val syncPayload = TinSyncPayload(
            itemBrand = parentItem.brand,
            itemBlend = parentItem.blend,
            tin = tin
        )
        val operation = PendingSyncOperation(
            operationType = "UPDATE",
            entityType = "Tins",
            entityId = tin.tinId.toString(),
            payload = Json.encodeToString(syncPayload),
            dbVersion = dbVersion
        )

        logOperation(operation)
        scheduleSyncUpload()
    }

    override suspend fun deleteTin(tinId: Int) {
        val tin = itemsDao.getTinByTinId(tinId) ?: return
        val parentItem = itemsDao.getItemById(tin.itemsId) ?: return
        val syncPayload = TinSyncPayload(
            itemBrand = parentItem.brand,
            itemBlend = parentItem.blend,
            tin = tin
        )
        itemsDao.deleteTin(tinId)

        val operation = PendingSyncOperation(
            operationType = "DELETE",
            entityType = "Tins",
            entityId = tin.tinId.toString(),
            payload = Json.encodeToString(syncPayload),
            dbVersion = dbVersion
        )
        logOperation(operation)
        scheduleSyncUpload()
    }

    override suspend fun insertMultipleTins(tins: List<Tins>): List<Long> {
        val tinIds = itemsDao.insertMultipleTins(tins).toList()
        val parentItem = itemsDao.getItemById(tins.first().itemsId) ?: return tinIds
        val newTins = tins.zip(tinIds)
        for ((tin, tinId) in newTins) {
            val syncPayload = TinSyncPayload(
                itemBrand = parentItem.brand,
                itemBlend = parentItem.blend,
                tin = tin.copy(tinId = tinId.toInt())
            )
            val operation = PendingSyncOperation(
                operationType = "INSERT",
                entityType = "Tins",
                entityId = tinId.toString(),
                payload = Json.encodeToString(syncPayload),
                dbVersion = dbVersion
            )
            logOperation(operation)
        }

        scheduleSyncUpload()
        return tinIds
    }

    override suspend fun deleteAllTinsForItem(itemId: Int) {
        val tinsToDelete = itemsDao.getTinsForItemStream(itemId).first()
        for (tin in tinsToDelete) {
            val parentItem = itemsDao.getItemById(tin.itemsId) ?: continue
            val syncPayload = TinSyncPayload(
                itemBrand = parentItem.brand,
                itemBlend = parentItem.blend,
                tin = tin
            )
            val operation = PendingSyncOperation(
                operationType = "DELETE",
                entityType = "Tins",
                entityId = tin.tinId.toString(),
                payload = Json.encodeToString(syncPayload),
                dbVersion = dbVersion
            )
            logOperation(operation)
        }
        itemsDao.deleteAllTinsForItem(itemId)
        scheduleSyncUpload()
    }


    /** Get all items **/
    override fun getAllItemIds(): List<Int> = itemsDao.getAllItemIds()

    override fun getAllComponentsStream(): Flow<List<Components>> = itemsDao.getAllComponents()

    override fun getAllFlavoringStream(): Flow<List<Flavoring>> = itemsDao.getAllFlavoring()

    override fun getEverythingStream(): Flow<List<ItemsComponentsAndTins>> = itemsDao.getEverythingStream()


    /** Get single item **/
    override fun getItemDetailsStream(id: Int): Flow<ItemsComponentsAndTins?> = itemsDao.getItemDetailsStream(id)

    override suspend fun getItemById(id: Int): Items? = itemsDao.getItemById(id)

    override fun getComponentsForItemStream(id: Int): Flow<List<Components>> = itemsDao.getComponentsForItemStream(id)

    override suspend fun getComponentById(id: Int): Components? = itemsDao.getComponentById(id)

    override fun getFlavoringForItemStream(id: Int): Flow<List<Flavoring>> = itemsDao.getFlavoringForItemStream(id)

    override suspend fun getFlavoringById(id: Int): Flavoring? = itemsDao.getFlavoringById(id)

    override fun getTinsForItemStream(id: Int): Flow<List<Tins>> = itemsDao.getTinsForItemStream(id)

    override fun getTinDetailsStream(tinId: Int): Flow<Tins?> = itemsDao.getTinByTinIdStream(tinId)

    override suspend fun getTinById(tinId: Int): Tins? = itemsDao.getTinByTinId(tinId)

    override suspend fun getTinByLabel(itemsId: Int, tinLabel: String): Tins? = itemsDao.getTinByLabel(itemsId, tinLabel)

    override suspend fun getItemIdByIndex(brand: String, blend: String) = itemsDao.getItemIdByIndex(brand, blend)

    override suspend fun getComponentIdByName(name: String): Int? = itemsDao.getComponentIdByName(name)

    override suspend fun getFlavoringIdByName(name: String): Int? = itemsDao.getFlavoringIdByName(name)


    /** Checks **/
    override suspend fun exists(brand: String, blend: String): Boolean {
        return itemsDao.exists(brand, blend)
    }


    /** Get any by value **/
    override fun getItemByIndex(brand: String, blend: String): Items? = itemsDao.getItemByIndex(brand, blend)



    /** Cloud-sync **/
    override fun getPendingSyncOperationDao(): PendingSyncOperationDao = this.pendingSyncOperationDao

    override suspend fun triggerUploadWorker() {
        scheduleSyncUpload()
    }

    private suspend fun logOperation(operation: PendingSyncOperation) {
        if (SyncStateManager.loggingPaused) {
            return
        }

        pendingSyncOperationDao.insertOperation(operation)
    }

    private suspend fun scheduleSyncUpload() {
        if (SyncStateManager.schedulingPaused) return

        val allowMobileData = preferencesRepo.allowMobileData.first()

        val networkType = if (allowMobileData) NetworkType.CONNECTED else NetworkType.UNMETERED

        val workManager = WorkManager.getInstance(context)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadSyncWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "upload_sync_work",
            ExistingWorkPolicy.REPLACE,
            uploadWorkRequest
        )
    }

    override suspend fun hasPendingOperations(): Boolean {
        return pendingSyncOperationDao.hasPendingOperations()
    }

}