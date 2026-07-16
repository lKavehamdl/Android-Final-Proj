package com.example.final_project.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.final_project.core.datastore.SettingsDataStore

/**
 * ViewModel for [ProfileScreen].
 *
 * Follows strict UDF / MVI:
 *  - State is exposed via [StateFlow<ProfileUiState>]
 *  - Mutations arrive as intent functions (onUpgrade, onAvatarChanged, etc.)
 *
 * Phase 2 note: isPremium / avatar URI will be read from DataStore once
 * the DataStore module is wired. Until then, defaults are used.
 */
class ProfileViewModel(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Observe premium status from DataStore
        viewModelScope.launch {
            settingsDataStore.isPremium.collect { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }
    }

    // ── Intents ───────────────────────────────────────────────────────────────

    /**
     * Demo-grade upgrade: flips isPremium flag locally.
     * Phase 2 will persist this to DataStore + call Supabase RPC.
     */
    fun onUpgrade() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpgrading = true) }
            // Simulate a brief "purchase" animation delay
            delay(300)
            settingsDataStore.setPremiumStatus(true)
            _uiState.update { it.copy(isUpgrading = false) }
        }
    }

    fun onDowngrade() {
        viewModelScope.launch {
            settingsDataStore.setPremiumStatus(false)
        }
    }

    /**
     * Called by the image picker result handler in the composable.
     * Saves uri string; Phase 2 will persist to DataStore.
     */
    fun onAvatarChanged(uriString: String?) {
        _uiState.update { it.copy(avatarUri = uriString) }
    }
}

/**
 * Immutable UI state for the Profile tab — single source of truth.
 */
data class ProfileUiState(
    val username: String = "melodify_user",
    val displayName: String = "Melodify User",
    val avatarUri: String? = null,
    val isPremium: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val likedSongsCount: Int = 0,
    /** True while the upgrade animation/fake-purchase is running. */
    val isUpgrading: Boolean = false,
)
