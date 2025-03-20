package com.sardonicus.tobaccocellar.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sardonicus.tobaccocellar.ui.home.SearchSetting
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesRepo(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val IS_TABLE_VIEW = booleanPreferencesKey("is_table_view")
        val THEME_SETTING = stringPreferencesKey("theme_setting")
        val TIN_OZ_CONVERSION_RATE = doublePreferencesKey("tin_oz_conversion_rate")
        val TIN_GRAMS_CONVERSION_RATE = doublePreferencesKey("tin_grams_conversion_rate")
        val SORT_COLUMN_INDEX = intPreferencesKey("sort_column_index")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
        val QUANTITY_OPTION = stringPreferencesKey("quantity_option")
        val SEARCH_SETTING = stringPreferencesKey("search_setting")
        fun itemsSyncKey(itemId: Int) = booleanPreferencesKey("item_sync_$itemId")

        const val TAG = "PreferencesRepo"
    }

    /** Setting HomeScreen view options **/
    // setting list/table view //
    val isTableView: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading view preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[IS_TABLE_VIEW] ?: false
        }

    suspend fun saveViewPreference(isTableView: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_TABLE_VIEW] = isTableView
        }
    }

    // setting quantity displayed options //
    val quantityOption: Flow<QuantityOption> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading quantity preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            val savedValue = preferences[QUANTITY_OPTION] ?: QuantityOption.TINS.value
            when (savedValue) {
                QuantityOption.TINS.value -> QuantityOption.TINS
                QuantityOption.OUNCES.value -> QuantityOption.OUNCES
                QuantityOption.GRAMS.value -> QuantityOption.GRAMS
                else -> QuantityOption.TINS
            }
        }

    suspend fun saveQuantityPreference(option: String) {
        dataStore.edit { preferences ->
            preferences[QUANTITY_OPTION] = option
        }
    }

    // setting table sort options //
    val sortColumnIndex: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading table sort preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[SORT_COLUMN_INDEX] ?: -1
        }

    val sortAscending: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading table sort preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[SORT_ASCENDING] ?: true
        }

    suspend fun saveTableSortingPreferences(columnIndex: Int, ascending: Boolean) {
        dataStore.edit { preferences ->
            preferences[SORT_COLUMN_INDEX] = columnIndex
            preferences[SORT_ASCENDING] = ascending
        }
    }


    /** Setting theme options **/
    val themeSetting: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading theme preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[THEME_SETTING] ?: ThemeSetting.SYSTEM.value
        }

    suspend fun saveThemeSetting(themeSetting: String) {
        dataStore.edit { preferences ->
            preferences[THEME_SETTING] = themeSetting
        }
    }


    /** Setting Tin Converter rates **/
    val tinOzConversionRate: Flow<Double> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading conversion preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[TIN_OZ_CONVERSION_RATE] ?: 1.75
        }

    suspend fun setTinOzConversionRate(rate: Double) {
        dataStore.edit { preferences ->
            preferences[TIN_OZ_CONVERSION_RATE] = rate
        }
    }

    val tinGramsConversionRate: Flow<Double> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading conversion preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[TIN_GRAMS_CONVERSION_RATE] ?: 50.0
        }

    suspend fun setTinGramsConversionRate(rate: Double) {
        dataStore.edit { preferences ->
            preferences[TIN_GRAMS_CONVERSION_RATE] = rate
        }
    }


    /** Sync status **/
    suspend fun setItemSyncState(itemId: Int, isSynced: Boolean) {
        dataStore.edit { preferences ->
            preferences[itemsSyncKey(itemId)] = isSynced
        }
    }

    fun getItemSyncState(itemId: Int): Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[itemsSyncKey(itemId)] ?: false
        }

    suspend fun getItemSyncStateString(itemId: Int): String {
        return try {
            getItemSyncState(itemId).first().toString()
        } catch (e: IOException) {
            ""
        }
    }

    /** Home header search setting **/
    val searchSetting: Flow<SearchSetting> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading search preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            when (it[SEARCH_SETTING]) {
                SearchSetting.BLEND.value -> SearchSetting.BLEND
                SearchSetting.NOTES.value -> SearchSetting.NOTES
                else -> SearchSetting.BLEND
            }
        }

    suspend fun setSearchSetting(setting: String) {
        dataStore.edit {
            it[SEARCH_SETTING] = setting
        }
    }

}
