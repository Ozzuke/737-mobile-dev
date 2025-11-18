package com.example.project.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing app preferences using DataStore
 */
class PreferencesRepository(private val context: Context) {

    private val ACTIVE_DATASET_ID_KEY = stringPreferencesKey("active_dataset_id")
    private val PREFERRED_UNIT_KEY = stringPreferencesKey("preferred_unit")
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")

    /**
     * Get the active dataset ID as a Flow
     */
    fun getActiveDatasetId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACTIVE_DATASET_ID_KEY]
        }
    }

    /**
     * Set the active dataset ID
     */
    suspend fun setActiveDatasetId(datasetId: String) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_DATASET_ID_KEY] = datasetId
        }
    }

    /**
     * Clear the active dataset ID
     */
    suspend fun clearActiveDatasetId() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACTIVE_DATASET_ID_KEY)
        }
    }

    /**
     * Get the preferred unit as a Flow
     */
    fun getPreferredUnit(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PREFERRED_UNIT_KEY]
        }
    }

    /**
     * Set the preferred unit
     */
    suspend fun setPreferredUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_UNIT_KEY] = unit
        }
    }

    /**
     * Get the dark mode preference as a Flow
     */
    fun getDarkModeEnabled(): Flow<Boolean?> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY]
        }
    }

    /**
     * Set the dark mode preference
     */
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
}
