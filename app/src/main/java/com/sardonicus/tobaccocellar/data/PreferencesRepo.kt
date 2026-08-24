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
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextSorting
import com.sardonicus.tobaccocellar.ui.settings.ExportRating
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.settings.ThemeSetting
import com.sardonicus.tobaccocellar.ui.settings.TypeGenreOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

class PreferencesRepo(
    private val dataStore: DataStore<Preferences>,
    applicationScope: CoroutineScope
) {
    private companion object {
        val NEW_USER = booleanPreferencesKey("new_user")
        val RELEASE_NOTES = intPreferencesKey("release_notes")
        val LAST_ALERT_SHOWN = intPreferencesKey("last_alert_shown")
        val DATES_SEEN_LIST = stringPreferencesKey("dates_seen_list")
        val SEARCH_SETTING = stringPreferencesKey("search_setting")
        val MAX_RATING = intPreferencesKey("max_rating")
        val RATING_ROUND = intPreferencesKey("rating_round")

        val IS_TABLE_VIEW = booleanPreferencesKey("is_table_view")
        val TABLE_COLUMNS_HIDDEN = stringPreferencesKey("table_columns_hidden")
        val SORT_COLUMN_INDEX = intPreferencesKey("sort_column_index")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
        val LIST_SORTING = stringPreferencesKey("list_sorting")
        val LIST_ASCENDING = booleanPreferencesKey("list_ascending")

        val THEME_SETTING = stringPreferencesKey("theme_setting")
        val SHOW_RATING = booleanPreferencesKey("show_rating")
        val TYPE_GENRE_OPTION = stringPreferencesKey("type_genre_option")
        val QUANTITY_OPTION = stringPreferencesKey("quantity_option")
        val PARSE_LINKS = booleanPreferencesKey("parse_links")

        val GLOBAL_TWO_PANE = booleanPreferencesKey("two_pane_global")
        val TWO_COLUMN_TABS = booleanPreferencesKey("two_column_tabs")
        val LANDSCAPE_TWO_PANE = booleanPreferencesKey("two_pane_landscape")

        val CROSS_DEVICE_ACKNOWLEDGED = booleanPreferencesKey("cross_device_acknowledged")
        val CROSS_DEVICE_SYNC = booleanPreferencesKey("cross_device_sync")
        val ALLOW_MOBILE_DATA = booleanPreferencesKey("allow_mobile_data")
        val SIGNED_IN_ACCOUNT = stringPreferencesKey("signed_in_account")
        val HAS_DRIVE_SCOPE = booleanPreferencesKey("has_drive_scope")
        val PROCESSED_SYNC_FILES = stringPreferencesKey("processed_sync_files")
        val TIN_OZ_CONVERSION_RATE = doublePreferencesKey("tin_oz_conversion_rate")
        val TIN_GRAMS_CONVERSION_RATE = doublePreferencesKey("tin_grams_conversion_rate")
        val DEFAULT_SYNC = booleanPreferencesKey("default_sync")

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

        fun itemsSyncKey(itemId: Int) = booleanPreferencesKey("item_sync_$itemId")
        val SYNC_SETTINGS_MIGRATED = booleanPreferencesKey("sync_settings_migrated")

        const val TAG = "PreferencesRepo"
    }

    /** Misc options and saved "last value" **/
    val newUser: Flow<Boolean?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading new user preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[NEW_USER] }

    suspend fun updateToExistingUser() { dataStore.edit { it[NEW_USER] = false } }


    val releaseNotesSeen: Flow<Int?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading release notes preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[RELEASE_NOTES] }

    suspend fun saveReleaseNotes(version: Int) { dataStore.edit { it[RELEASE_NOTES] = version } }

    val lastAlertFlow: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading alert preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[LAST_ALERT_SHOWN] ?: 1 }

    suspend fun saveAlertShown(alertId: Int) { dataStore.edit { it[LAST_ALERT_SHOWN] = alertId } }

    val datesSeen: StateFlow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading dates indicator preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[DATES_SEEN_LIST] ?: "" }
        .stateIn (applicationScope, SharingStarted.WhileSubscribed(5000), "")

    suspend fun setDatesSeen(seen: String) { dataStore.edit { it[DATES_SEEN_LIST] = seen } }

    val searchSetting: Flow<SearchSetting> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading search preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map {
            when (it[SEARCH_SETTING]) {
                SearchSetting.Blend.value -> SearchSetting.Blend
                SearchSetting.Notes.value -> SearchSetting.Notes
                SearchSetting.TinLabel.value -> SearchSetting.TinLabel
                else -> SearchSetting.Blend
            }
        }

    suspend fun setSearchSetting(setting: String) { dataStore.edit { it[SEARCH_SETTING] = setting } }

    val exportRating: Flow<ExportRating> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading rating preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map {
            val maxRating = it[MAX_RATING] ?: 5
            val rounding = it[RATING_ROUND] ?: 2
            ExportRating(maxRating, rounding)
        }

    suspend fun saveExportRating(rating: Int, rounding: Int) {
        dataStore.edit {
            it[MAX_RATING] = rating; it[RATING_ROUND] = rounding; ExportRating(rating, rounding)
        }
    }


    /** Cellar table and list specific options and sorting **/
    val isTableView: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading view preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[IS_TABLE_VIEW] ?: false }

    suspend fun saveView(tableView: Boolean) { dataStore.edit { it[IS_TABLE_VIEW] = tableView } }

    // Cellar table view specific options //
    val tableColumnsHidden: Flow<Set<String>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading table sort preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[TABLE_COLUMNS_HIDDEN]?.split(",")?.toSet() ?: emptySet() }

    suspend fun saveTableColumnsHidden(columnsHidden: Set<String>) {
        dataStore.edit { it[TABLE_COLUMNS_HIDDEN] = columnsHidden.joinToString(",") }
    }

    val sortColumnIndex: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading table sort preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[SORT_COLUMN_INDEX] ?: -1 }

    val sortAscending: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading table sort preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[SORT_ASCENDING] ?: true }

    suspend fun saveTableSorting(index: Int, ascending: Boolean) {
        dataStore.edit { it[SORT_COLUMN_INDEX] = index; it[SORT_ASCENDING] = ascending }
    }

    // Cellar list view specific options //
    val listSorting: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading list sort preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[LIST_SORTING] ?: ListSortOption.DEFAULT.value }

    val listAscending: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading list sort preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[LIST_ASCENDING] ?: true }

    suspend fun saveListSorting(listSorting: String, ascending: Boolean) {
        dataStore.edit { it[LIST_SORTING] = listSorting; it[LIST_ASCENDING] = ascending }
    }


    /** Display Settings **/
    val themeSetting: StateFlow<ThemeSetting> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading theme preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map {
            val savedValue = it[THEME_SETTING] ?: ThemeSetting.SYSTEM.value
            when (savedValue) {
                ThemeSetting.LIGHT.value -> ThemeSetting.LIGHT
                ThemeSetting.DARK.value -> ThemeSetting.DARK
                ThemeSetting.SYSTEM.value -> ThemeSetting.SYSTEM
                else -> ThemeSetting.SYSTEM
            }
        }.stateIn(applicationScope, SharingStarted.Eagerly, ThemeSetting.SYSTEM)

    suspend fun saveTheme(theme: String) { dataStore.edit { it[THEME_SETTING] = theme } }

    val showRating: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading rating preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[SHOW_RATING] ?: true }

    suspend fun saveShowRating(show: Boolean) { dataStore.edit { it[SHOW_RATING] = show } }

    val typeGenreOption: Flow<TypeGenreOption> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading genre preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { setting ->
            val savedValue = setting[TYPE_GENRE_OPTION] ?: TypeGenreOption.TYPE.value
            TypeGenreOption.entries.firstOrNull { it.value == savedValue } ?: TypeGenreOption.TYPE
        }

    suspend fun saveTypeGenre(option: String) { dataStore.edit { it[TYPE_GENRE_OPTION] = option } }

    val quantityOption: Flow<QuantityOption> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading quantity preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map {
            val savedValue = it[QUANTITY_OPTION] ?: QuantityOption.TINS.value
            when (savedValue) {
                QuantityOption.TINS.value -> QuantityOption.TINS
                QuantityOption.OUNCES.value -> QuantityOption.OUNCES
                QuantityOption.GRAMS.value -> QuantityOption.GRAMS
                else -> QuantityOption.TINS
            }
        }

    suspend fun saveQuantity(option: String) { dataStore.edit { it[QUANTITY_OPTION] = option } }

    val parseLinks: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading link preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[PARSE_LINKS] ?: true }

    suspend fun saveParseLinks(parse: Boolean) { dataStore.edit { it[PARSE_LINKS] = parse } }

    val globalTwoPane: StateFlow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading two-pane preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[GLOBAL_TWO_PANE] ?: true }
        .stateIn(applicationScope, SharingStarted.Eagerly, true)

    val twoColumnTabs: StateFlow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading two-pane preferences.", it)
            } else { throw it }
        }.map { it[TWO_COLUMN_TABS] ?: true }
        .stateIn(applicationScope, SharingStarted.WhileSubscribed(5000), true)

    val landscapeTwoPane: StateFlow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading two-pane preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[LANDSCAPE_TWO_PANE] ?: false }
        .stateIn(applicationScope, SharingStarted.Eagerly, false)

    suspend fun saveGlobalTP(enabled: Boolean) { dataStore.edit { it[GLOBAL_TWO_PANE] = enabled } }

    suspend fun saveTwoColumn(enabled: Boolean) { dataStore.edit { it[TWO_COLUMN_TABS] = enabled } }

    suspend fun saveLandscape(enabled: Boolean) { dataStore.edit { it[LANDSCAPE_TWO_PANE] = enabled } }



    /** Database Settings **/
    // multi-device sync settings
    val crossDeviceAcknowledged: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[CROSS_DEVICE_ACKNOWLEDGED] ?: false }

    suspend fun saveCDAcknowledge(yes: Boolean) { dataStore.edit { it[CROSS_DEVICE_ACKNOWLEDGED] = yes } }

    val crossDeviceSync: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[CROSS_DEVICE_SYNC] ?: false }

    suspend fun saveCrossDeviceSync(sync: Boolean) { dataStore.edit { it[CROSS_DEVICE_SYNC] = sync } }

    val allowMobileData: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[ALLOW_MOBILE_DATA] ?: false }

    suspend fun saveAllowMobile(allow: Boolean) { dataStore.edit { it[ALLOW_MOBILE_DATA] = allow } }

    val signedInUserEmail: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[SIGNED_IN_ACCOUNT] }

    val hasDriveScope: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[HAS_DRIVE_SCOPE] ?: false }

    suspend fun saveLoginState(email: String, hasDriveScope: Boolean) {
        dataStore.edit { it[SIGNED_IN_ACCOUNT] = email; it[HAS_DRIVE_SCOPE] = hasDriveScope }
    }

    suspend fun clearLogin() {
        dataStore.edit { it.remove(SIGNED_IN_ACCOUNT); it.remove(HAS_DRIVE_SCOPE) }
    }

    val processedSyncFiles: Flow<Set<String>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[PROCESSED_SYNC_FILES]?.split(",")?.toSet() ?: emptySet() }

    suspend fun saveProcessedSyncFiles(filesIds: Set<String>) {
        dataStore.edit { it[PROCESSED_SYNC_FILES] = filesIds.joinToString(",") }
    }

    val tinOzConversionRate: Flow<Double> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading conversion preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[TIN_OZ_CONVERSION_RATE] ?: 1.75 }

    suspend fun setOzRate(rate: Double) { dataStore.edit { it[TIN_OZ_CONVERSION_RATE] = rate } }

    val tinGramsConversionRate: Flow<Double> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading conversion preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[TIN_GRAMS_CONVERSION_RATE] ?: 50.0 }

    suspend fun setGramRate(rate: Double) { dataStore.edit { it[TIN_GRAMS_CONVERSION_RATE] = rate } }

    val defaultSyncOption: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[DEFAULT_SYNC] ?: false }

    suspend fun saveDefaultSyncOption(sync: Boolean) { dataStore.edit { it[DEFAULT_SYNC] = sync } }



    /** Plaintext Options **/
    val plaintextFormatString: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[PLAINTEXT_FORMAT_STRING] ?: "" }

    suspend fun setPtFormat(format: String) { dataStore.edit { it[PLAINTEXT_FORMAT_STRING] = format } }

    val plaintextDelimiter: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[PLAINTEXT_DELIMITER] ?: "" }

    suspend fun setPtDelimiter(delim: String) { dataStore.edit { it[PLAINTEXT_DELIMITER] = delim } }


    val plaintextSorting: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map {
            it[PLAINTEXT_SORTING] ?: PlaintextSorting.DEFAULT.value
        }

    val plaintextSubSorting: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[PLAINTEXT_SUBSORTING] ?: PlaintextSorting.DEFAULT.value }

    val plaintextSortAscending: Flow<Boolean> = dataStore.data
        .catch{
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[PLAINTEXT_SORT_ASCENDING] ?: true }

    suspend fun setPtSort(sort: String, ascend: Boolean) {
        dataStore.edit { it[PLAINTEXT_SORTING] = sort; it[PLAINTEXT_SORT_ASCENDING] = ascend }
    }

    suspend fun setPtSubSort(subSort: String) { dataStore.edit { it[PLAINTEXT_SUBSORTING] = subSort } }

    val plaintextPrintFontSize: Flow<Float> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[PLAINTEXT_PRINT_FONT] ?: 12f }

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

    suspend fun setPtPrintOptions(font: Float, margin: Double) {
        dataStore.edit { it[PLAINTEXT_PRINT_FONT] = font; it[PLAINTEXT_PRINT_MARGIN] = margin }
    }

    val plaintextPresetsFlow: Flow<List<PlaintextPreset>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading plaintext formatting preferences.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { preferences ->
            (0..4).map {
                PlaintextPreset(
                    slot = it,
                    formatString = preferences[ptFormatKey(it)] ?: "",
                    delimiter = preferences[ptDelimiterKey(it)] ?: ""
                )
            }
        }

    suspend fun savePtPreset(slot: Int, format: String, delimiter: String) {
        dataStore.edit { it[ptFormatKey(slot)] = format; it[ptDelimiterKey(slot)] = delimiter }
    }

    private fun ptFormatKey(slot: Int): Preferences.Key<String> {
        return when (slot) {
            0 -> PLAINTEXT_PRESET_FORMAT1
            1 -> PLAINTEXT_PRESET_FORMAT2
            2 -> PLAINTEXT_PRESET_FORMAT3
            3 -> PLAINTEXT_PRESET_FORMAT4
            4 -> PLAINTEXT_PRESET_FORMAT5
            else -> throw IllegalArgumentException("Invalid slot: $slot")
        }
    }

    private fun ptDelimiterKey(slot: Int): Preferences.Key<String> {
        return when (slot) {
            0 -> PLAINTEXT_PRESET_DELIMITER1
            1 -> PLAINTEXT_PRESET_DELIMITER2
            2 -> PLAINTEXT_PRESET_DELIMITER3
            3 -> PLAINTEXT_PRESET_DELIMITER4
            4 -> PLAINTEXT_PRESET_DELIMITER5
            else -> throw IllegalArgumentException("Invalid slot: $slot")
        }
    }


    /** One-time and migrational stuff **/
    fun getItemSyncState(itemId: Int): Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }
        .map { it[itemsSyncKey(itemId)] ?: false }

    val syncSettingsMigrated: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading sync state.", it)
                emit(emptyPreferences())
            } else { throw it }
        }.map { it[SYNC_SETTINGS_MIGRATED] ?: false }

    suspend fun setSyncSettingsMigrated() { dataStore.edit { it[SYNC_SETTINGS_MIGRATED] = true } }

}
