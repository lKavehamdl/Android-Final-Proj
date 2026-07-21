package com.example.final_project.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.datastore.SettingsDataStore
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.repository.SocialRepository
import com.example.final_project.domain.social.CurrentUserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileEffect {
    data class ShowMessage(val message: String) : ProfileEffect
}

/**
 * ViewModel for [ProfileScreen].
 *
 * Follows strict UDF / MVI:
 *  - State is exposed via [StateFlow<ProfileUiState>]
 *  - Mutations arrive as intent functions (onUpgrade, onAvatarChanged, etc.)
 */
class ProfileViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val socialRepository: SocialRepository,
    private val currentUserStore: CurrentUserStore,
    private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect

    init {
        viewModelScope.launch {
            settingsDataStore.isPremium.collect { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }
        viewModelScope.launch {
            currentUserStore.refresh()
            currentUserStore.profile.collect { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            username = profile.username,
                            displayName = profile.displayName ?: profile.username,
                            avatarUri = profile.avatarUrl,
                            followersCount = profile.followersCount,
                            followingCount = profile.followingCount,
                        )
                    }
                }
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

    /** Re-fetch the profile (incl. follower/following counts) — call when the screen re-appears. */
    fun refresh() {
        viewModelScope.launch { currentUserStore.refresh() }
    }

    /**
     * Downscales + compresses the picked image and persists it directly in
     * `profiles.avatar_url` as a `data:` URI. This deliberately avoids Supabase
     * Storage (which needs a manually-created bucket + RLS policies) so avatars
     * work with zero server-side setup — the profile row UPDATE is the same
     * proven path used by username editing.
     */
    fun onAvatarChanged(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true) }
            try {
                val dataUri = withContext(Dispatchers.IO) { encodeAvatar(uri) }
                    ?: throw IllegalStateException("Couldn't read image")

                when (val result = socialRepository.updateProfile(avatarUrl = dataUri)) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(avatarUri = dataUri) }
                        // Re-fetch so counts (which updateProfile returns as 0) stay correct.
                        currentUserStore.refresh()
                    }
                    is Resource.Error -> _effect.emit(ProfileEffect.ShowMessage(result.message ?: "Couldn't update avatar"))
                    Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Avatar update failed", e)
                _effect.emit(ProfileEffect.ShowMessage(e.message ?: "Couldn't update avatar"))
            } finally {
                _uiState.update { it.copy(isUploadingAvatar = false) }
            }
        }
    }

    /** Reads [uri], downscales to <=512px, JPEG-compresses, returns a data URI (or null). */
    private fun encodeAvatar(uri: Uri): String? {
        val resolver = appContext.contentResolver

        // Decode bounds first to compute a sane downsample factor.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val maxDim = 512
        var sample = 1
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) {
            sample *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: return null

        return try {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
            val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } finally {
            bitmap.recycle()
        }
    }

    fun onUsernameChanged(newUsername: String) {
        val trimmed = newUsername.trim()
        if (trimmed.isEmpty() || trimmed == _uiState.value.username) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingUsername = true) }
            when (val result = socialRepository.updateProfile(username = trimmed)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(username = result.data.username, isSavingUsername = false) }
                    // Re-fetch so counts (which updateProfile returns as 0) stay correct.
                    currentUserStore.refresh()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSavingUsername = false) }
                    _effect.emit(ProfileEffect.ShowMessage(result.message ?: "Couldn't update username"))
                }
                Resource.Loading -> Unit
            }
        }
    }
}

/**
 * Immutable UI state for the Profile tab — single source of truth.
 */
data class ProfileUiState(
    val username: String = "",
    val displayName: String = "",
    val avatarUri: String? = null,
    val isPremium: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    /** True while the upgrade animation/fake-purchase is running. */
    val isUpgrading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val isSavingUsername: Boolean = false,
)
