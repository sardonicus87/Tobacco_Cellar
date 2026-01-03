package com.sardonicus.tobaccocellar.data

import android.content.Context
import com.sardonicus.tobaccocellar.CellarApplication

interface AppContainer {
    val itemsRepository: ItemsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val itemsRepository: ItemsRepository by lazy {
        val database = TobaccoDatabase.getDatabase(context)
        val preferences = (context.applicationContext as CellarApplication).preferencesRepo
        OfflineItemsRepository(database.itemsDao(), database.pendingSyncOperationDao(), preferences, context)
    }
}