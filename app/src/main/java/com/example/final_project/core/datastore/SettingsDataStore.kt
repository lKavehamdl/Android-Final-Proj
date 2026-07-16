package com.example.final_project.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.final_project.core.constants.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a single instance of DataStore
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Persists general application settings (e.g., premium status, theme, language).
 */
class SettingsDataStore(context: Context) {

    private val store = context.applicationContext.settingsDataStore

    companion object {
        val KEY_IS_PREMIUM = booleanPreferencesKey(AppConstants.PREF_IS_PREMIUM)
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    val isPremium: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_IS_PREMIUM] ?: false
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun setPremiumStatus(isPremium: Boolean) {
        store.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = isPremium
        }
    }
}
