package com.sardonicus.tobaccocellar.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sardonicus.tobaccocellar.ui.home.ListSorting
import com.sardonicus.tobaccocellar.ui.home.SearchSetting
import com.sardonicus.tobaccocellar.ui.home.plaintext.PlaintextSortOption
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

@Suppress("NullableBooleanElvis")
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
        val LIST_SORTING = stringPreferencesKey("list_sorting")
        val LIST_ASCENDING = booleanPreferencesKey("list_ascending")
        val SEARCH_SETTING = stringPreferencesKey("search_setting")
        val LAST_ALERT_SHOWN = intPreferencesKey("last_alert_shown")
        val DATES_SEEN_LIST = stringPreferencesKey("dates_seen_list")
        val PLAINTEXT_FORMAT_STRING = stringPreferencesKey("plaintext_format_string")
        val PLAINTEXT_DELIMITER = stringPreferencesKey("plaintext_delimiter")
        val PLAINTEXT_SORTING = stringPreferencesKey("plaintext_sorting")
        val PLAINTEXT_SORT_ASCENDING = booleanPreferencesKey("plaintext_sort_ascending")
        val PLAINTEXT_SUBSORTING = stringPreferencesKey("plaintext_subsorting")
        val PLAINTEXT_PRINT_FONT = floatPreferencesKey("plaintext_print_font")
        val PLAINTEXT_PRINT_MARGIN = doublePreferencesKey("plaintext_print_margin")
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
        }.map { preferences ->
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

    // setting list sort options //
    val listSorting: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading list sort preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[LIST_SORTING] ?: ListSorting.DEFAULT.value
        }

    val listAscending: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading list sort preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[LIST_ASCENDING] ?: true
        }

    suspend fun saveListSorting(listSorting: String, ascending: Boolean) {
        dataStore.edit {
            it[LIST_SORTING] = listSorting
            it[LIST_ASCENDING] = ascending
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
            throw e
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
                SearchSetting.Blend.value -> SearchSetting.Blend
                SearchSetting.Notes.value -> SearchSetting.Notes
                SearchSetting.TinLabel.value -> SearchSetting.TinLabel
                else -> SearchSetting.Blend
            }
        }

    suspend fun setSearchSetting(setting: String) {
        dataStore.edit {
            it[SEARCH_SETTING] = setting
        }
    }


    /** Alert shown **/
    val lastAlertFlow: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading alert preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[LAST_ALERT_SHOWN] ?: 1
        }

    suspend fun saveAlertShown(alertId: Int) {
        dataStore.edit {
            it[LAST_ALERT_SHOWN] = alertId
        }
    }


    /** Dates indicator seen **/
    val datesSeen: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading dates indicator preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[DATES_SEEN_LIST] ?: ""
        }

    suspend fun setDatesSeen(seen: String) {
        dataStore.edit {
            it[DATES_SEEN_LIST] = seen
        }
    }


    /** Plaintext options **/
    val plaintextFormatString: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PLAINTEXT_FORMAT_STRING] ?: ""
        }

    val plaintextDelimiter: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PLAINTEXT_DELIMITER] ?: "_n_"
        }

    val plaintextSorting: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PLAINTEXT_SORTING] ?: PlaintextSortOption.DEFAULT.value
        }

    val plaintextSortAscending: Flow<Boolean> = dataStore.data
        .catch{
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PLAINTEXT_SORT_ASCENDING] ?: true
        }

    val plaintextSubSorting: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PLAINTEXT_SUBSORTING] ?: ""
        }

    val plaintextPrintFontSize: Flow<Float> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PLAINTEXT_PRINT_FONT] ?: 12f
        }

    val plaintextPrintMargin: Flow<Double> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PLAINTEXT_PRINT_MARGIN] ?: 1.0
        }

    suspend fun setPlaintextFormatString(format: String) {
        dataStore.edit {
            it[PLAINTEXT_FORMAT_STRING] = format
        }
    }

    suspend fun setPlaintextDelimiter(delimiter: String) {
        dataStore.edit {
            it[PLAINTEXT_DELIMITER] = delimiter
        }
    }

    suspend fun setPlaintextSorting(sorting: String, ascending: Boolean) {
        dataStore.edit {
            it[PLAINTEXT_SORTING] = sorting
            it[PLAINTEXT_SORT_ASCENDING] = ascending
        }
    }

    suspend fun setPlaintextSubSorting(subSorting: String) {
        dataStore.edit {
            it[PLAINTEXT_SUBSORTING] = subSorting
        }
    }

    suspend fun setPlaintextPrintOptions(font: Float, margin: Double) {
        dataStore.edit {
            it[PLAINTEXT_PRINT_FONT] = font
            it[PLAINTEXT_PRINT_MARGIN] = margin
        }
    }

}
