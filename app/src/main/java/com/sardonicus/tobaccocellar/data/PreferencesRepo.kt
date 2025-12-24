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
import com.sardonicus.tobaccocellar.ui.home.ListSortOption
import com.sardonicus.tobaccocellar.ui.home.SearchSetting
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextPreset
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextSortOption
import com.sardonicus.tobaccocellar.ui.settings.ExportRating
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.ThemeSetting
import com.sardonicus.tobaccocellar.ui.settings.TypeGenreOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

class PreferencesRepo(
    private val dataStore: DataStore<Preferences>,
    applicationScope: CoroutineScope
) {
    private companion object {
        val IS_TABLE_VIEW = booleanPreferencesKey("is_table_view")
        val TABLE_COLUMNS_HIDDEN = stringPreferencesKey("table_columns_hidden")
        val THEME_SETTING = stringPreferencesKey("theme_setting")
        val TIN_OZ_CONVERSION_RATE = doublePreferencesKey("tin_oz_conversion_rate")
        val TIN_GRAMS_CONVERSION_RATE = doublePreferencesKey("tin_grams_conversion_rate")
        val MAX_RATING = intPreferencesKey("max_rating")
        val RATING_ROUND = intPreferencesKey("rating_round")
        val SORT_COLUMN_INDEX = intPreferencesKey("sort_column_index")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
        val QUANTITY_OPTION = stringPreferencesKey("quantity_option")
        val PARSE_LINKS = booleanPreferencesKey("parse_links")

        val SHOW_RATING = booleanPreferencesKey("show_rating")
        val TYPE_GENRE_OPTION = stringPreferencesKey("type_genre_option")
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
        val PLAINTEXT_PRESET_FORMAT1 = stringPreferencesKey("plaintext_preset_format1")
        val PLAINTEXT_PRESET_DELIMITER1 = stringPreferencesKey("plaintext_preset_delimiter1")
        val PLAINTEXT_PRESET_FORMAT2 = stringPreferencesKey("plaintext_preset_format2")
        val PLAINTEXT_PRESET_DELIMITER2 = stringPreferencesKey("plaintext_preset_delimiter2")
        val PLAINTEXT_PRESET_FORMAT3 = stringPreferencesKey("plaintext_preset_format3")
        val PLAINTEXT_PRESET_DELIMITER3 = stringPreferencesKey("plaintext_preset_delimiter3")
        val PLAINTEXT_PRESET_FORMAT4 = stringPreferencesKey("plaintext_preset_format4")
        val PLAINTEXT_PRESET_DELIMITER4 = stringPreferencesKey("plaintext_preset_delimiter4")
        val PLAINTEXT_PRESET_FORMAT5 = stringPreferencesKey("plaintext_preset_format5")
        val PLAINTEXT_PRESET_DELIMITER5 = stringPreferencesKey("plaintext_preset_delimiter5")

        val DEFAULT_SYNC = booleanPreferencesKey("default_sync")
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

    // setting quantity, rating, etc display options //
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

    val parseLinks: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading link preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PARSE_LINKS] ?: true
        }

    suspend fun saveParseLinksOption(parseLinks: Boolean) {
        dataStore.edit {
            it[PARSE_LINKS] = parseLinks
        }
    }

    val showRating: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading rating preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[SHOW_RATING] ?: true
        }

    suspend fun saveShowRatingOption(showRating: Boolean) {
        dataStore.edit {
            it[SHOW_RATING] = showRating
        }
    }

    val typeGenreOption: Flow<TypeGenreOption> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading genre preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            val savedValue = it[TYPE_GENRE_OPTION] ?: TypeGenreOption.TYPE.value
            TypeGenreOption.entries.firstOrNull { it.value == savedValue } ?: TypeGenreOption.TYPE
        }

    suspend fun saveTypeGenreOption(option: String) {
        dataStore.edit {
            it[TYPE_GENRE_OPTION] = option
        }
    }


    /** Setting table and list specific options and sorting **/
    // setting table options //
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

    val tableColumnsHidden: Flow<Set<String>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading table sort preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[TABLE_COLUMNS_HIDDEN]?.split(",")?.toSet() ?: emptySet()
        }

    suspend fun saveTableColumnsHidden(columnsHidden: Set<String>) {
        dataStore.edit {
            it[TABLE_COLUMNS_HIDDEN] = columnsHidden.joinToString(",")
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
            it[LIST_SORTING] ?: ListSortOption.DEFAULT.value
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
    val themeSetting: StateFlow<String> = dataStore.data
        .map {
            it[THEME_SETTING] ?: ThemeSetting.SYSTEM.value
        }.catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading theme preferences.", it)
                emit(ThemeSetting.SYSTEM.value)
            } else {
                throw it
            }
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeSetting.SYSTEM.value
        )

    suspend fun saveThemeSetting(themeSetting: String) {
        dataStore.edit { preferences ->
            preferences[THEME_SETTING] = themeSetting
        }
    }


    /** Setting Data preferences **/
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

    val exportRating: Flow<ExportRating> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading rating preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            val maxRating = it[MAX_RATING] ?: 5
            val rounding = it[RATING_ROUND] ?: 2
            ExportRating(maxRating, rounding)
        }

    suspend fun saveExportRating(rating: Int, rounding: Int) {
        dataStore.edit {
            it[MAX_RATING] = rating
            it[RATING_ROUND] = rounding
            ExportRating(rating, rounding)
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

    // set default sync selected //
    val defaultSyncOption: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[DEFAULT_SYNC] ?: false
        }

    suspend fun saveDefaultSyncOption(sync: Boolean) {
        dataStore.edit {
            it[DEFAULT_SYNC] = sync
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
            it[PLAINTEXT_DELIMITER] ?: ""
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

    val plaintextPresetsFlow: Flow<List<PlaintextPreset>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            (0..4).map {
                val format = preferences[plaintextPresetFormatKey(it)] ?: ""
                val delimiter = preferences[plaintextPresetDelimiterKey(it)] ?: ""
                PlaintextPreset(
                    slot = it,
                    formatString = format,
                    delimiter = delimiter
                )
            }
        }

    suspend fun savePlaintextPreset(slot: Int, format: String, delimiter: String) {
        dataStore.edit {
            it[plaintextPresetFormatKey(slot)] = format
            it[plaintextPresetDelimiterKey(slot)] = delimiter
        }
    }

    private fun plaintextPresetFormatKey(slot: Int): Preferences.Key<String> {
        return when (slot) {
            0 -> PLAINTEXT_PRESET_FORMAT1
            1 -> PLAINTEXT_PRESET_FORMAT2
            2 -> PLAINTEXT_PRESET_FORMAT3
            3 -> PLAINTEXT_PRESET_FORMAT4
            4 -> PLAINTEXT_PRESET_FORMAT5
            else -> throw IllegalArgumentException("Invalid slot: $slot")
        }
    }

    private fun plaintextPresetDelimiterKey(slot: Int): Preferences.Key<String> {
        return when (slot) {
            0 -> PLAINTEXT_PRESET_DELIMITER1
            1 -> PLAINTEXT_PRESET_DELIMITER2
            2 -> PLAINTEXT_PRESET_DELIMITER3
            3 -> PLAINTEXT_PRESET_DELIMITER4
            4 -> PLAINTEXT_PRESET_DELIMITER5
            else -> throw IllegalArgumentException("Invalid slot: $slot")
        }
    }

}
