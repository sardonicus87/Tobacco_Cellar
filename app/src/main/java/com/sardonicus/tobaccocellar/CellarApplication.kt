package com.sardonicus.tobaccocellar

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.sardonicus.tobaccocellar.data.AppContainer
import com.sardonicus.tobaccocellar.data.AppDataContainer
import com.sardonicus.tobaccocellar.data.CsvHelper
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val VIEW_PREFERENCE_NAME = "view_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = VIEW_PREFERENCE_NAME
)

class CellarApplication : Application() {
    lateinit var container: AppContainer
    lateinit var preferencesRepo: PreferencesRepo
    lateinit var csvHelper: CsvHelper
    val filterViewModel: FilterViewModel by lazy {
        FilterViewModel(container.itemsRepository, preferencesRepo)
    }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        preferencesRepo = PreferencesRepo(dataStore, applicationScope)
        csvHelper = CsvHelper()

        migrateSyncSettings()
    }

    private fun migrateSyncSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            if (preferencesRepo.syncSettingsMigrated.first()) {
                return@launch
            }

            val itemsRepo = container.itemsRepository
            val allItemIds = itemsRepo.getAllItemIds()

            for (id in allItemIds) {
                val oldSyncState = preferencesRepo.getItemSyncState(id).first()
                val item = itemsRepo.getItemDetailsStream(id).first()?.items
                if (item != null && item.syncTins != oldSyncState) {
                    itemsRepo.updateItem(item.copy(syncTins = oldSyncState))
                }
            }
            preferencesRepo.setSyncSettingsMigrated()
        }
    }
}
