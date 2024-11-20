package com.sardonicus.tobaccocellar.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sardonicus.tobaccocellar.ui.settings.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesRepo(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val IS_TABLE_VIEW = booleanPreferencesKey("is_table_view")
        val THEME_SETTING = stringPreferencesKey("theme_setting")
        const val TAG = "PreferencesRepo"
    }

    /** Setting Homescreen view options */
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

    /** Setting theme options */
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
}
