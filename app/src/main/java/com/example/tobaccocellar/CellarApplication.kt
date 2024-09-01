package com.example.tobaccocellar

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.tobaccocellar.data.AppContainer
import com.example.tobaccocellar.data.AppDataContainer
import com.example.tobaccocellar.data.CsvHelper
import com.example.tobaccocellar.data.PreferencesRepo
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.FilterViewModel

private const val VIEW_PREFERENCE_NAME = "view_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = VIEW_PREFERENCE_NAME
)

class CellarApplication : Application() {
    lateinit var container: AppContainer
    lateinit var preferencesRepo: PreferencesRepo
    lateinit var csvHelper: CsvHelper
    val filterViewModel: FilterViewModel by lazy {
        FilterViewModel(container.itemsRepository)
    }

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        preferencesRepo = PreferencesRepo(dataStore)
        csvHelper = CsvHelper()
    }
}
