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
import androidx.datastore.preferences.core.stringPreferencesKey

// Create a single instance of DataStore
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Persists general application settings (e.g., premium status, theme, language).
 */
class SettingsDataStore(context: Context) {

    private val store = context.applicationContext.settingsDataStore

    companion object {
        val KEY_IS_PREMIUM = booleanPreferencesKey(AppConstants.PREF_IS_PREMIUM)
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_LANGUAGE = stringPreferencesKey("language")
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    val isPremium: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_IS_PREMIUM] ?: false
    }

    val themeMode: Flow<String> = store.data.map { prefs ->
        prefs[KEY_THEME] ?: "SYSTEM"
    }

    val language: Flow<String> = store.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun setPremiumStatus(isPremium: Boolean) {
        store.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = isPremium
        }
    }

    suspend fun setThemeMode(theme: String) {
        store.edit { prefs ->
            prefs[KEY_THEME] = theme
        }
    }

    suspend fun setLanguage(language: String) {
        store.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }
}
