package com.example.final_project.ui.screens.social

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExternalProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val publicPlaylists: List<Playlist> = emptyList(),
    val error: String? = null,
    val isTogglingFollow: Boolean = false
)

class ExternalProfileViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(ExternalProfileUiState())
    val uiState: StateFlow<ExternalProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Fetch profile
            when (val result = socialRepository.getUserProfile(userId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(profile = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                    return@launch
                }
                is Resource.Loading -> {}
            }
            
            // Fetch playlists
            when (val result = socialRepository.getUserPublicPlaylists(userId)) {
                is Resource.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    _uiState.update { it.copy(publicPlaylists = result.data as? List<Playlist> ?: emptyList()) }
                }
                is Resource.Error -> {
                    // Ignore for now
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onToggleFollow() {
        val currentProfile = _uiState.value.profile ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingFollow = true) }
            when (val result = socialRepository.toggleFollow(userId)) {
                is Resource.Success -> {
                    val isNowFollowing = result.data
                    _uiState.update { state -> 
                        state.copy(
                            isTogglingFollow = false,
                            profile = state.profile?.copy(
                                isFollowing = isNowFollowing,
                                followersCount = state.profile.followersCount + if (isNowFollowing) 1 else -1
                            )
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isTogglingFollow = false) } // optionally show error
                }
                is Resource.Loading -> {}
            }
        }
    }
}
