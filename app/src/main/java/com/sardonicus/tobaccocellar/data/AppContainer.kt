package com.sardonicus.tobaccocellar.data

import android.content.Context

interface AppContainer {
    val itemsRepository: ItemsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val itemsRepository: ItemsRepository by lazy {
        val database = TobaccoDatabase.getDatabase(context)
        OfflineItemsRepository(database.itemsDao(), database.pendingSyncOperationDao(), context)
    }
}