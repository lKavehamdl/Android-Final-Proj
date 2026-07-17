package com.example.final_project.ui.screens.profile.follows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FollowsUiState(
    val isLoading: Boolean = false,
    val following: List<UserProfile> = emptyList(),
    val error: String? = null
)

class FollowsViewModel(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowsUiState())
    val uiState: StateFlow<FollowsUiState> = _uiState.asStateFlow()

    init {
        loadFollowing()
    }

    private fun loadFollowing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val currentUserId = socialRepository.getCurrentUserId()
            if (currentUserId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Not logged in") }
                return@launch
            }
            
            when (val result = socialRepository.getFollowing(currentUserId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, following = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }
    
    fun onUnfollow(userId: String) {
        viewModelScope.launch {
            socialRepository.toggleFollow(userId)
            loadFollowing() // reload list
        }
    }
}
