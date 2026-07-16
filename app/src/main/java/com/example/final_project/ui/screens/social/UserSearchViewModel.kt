package com.example.final_project.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.SocialRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<UserProfile> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
class UserSearchViewModel(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSearchUiState())
    val uiState: StateFlow<UserSearchUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        searchQueryFlow
            .debounce(300L) // Wait for user to stop typing
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { performSearch(it) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false) }
        } else {
            searchQueryFlow.value = newQuery
        }
    }

    private fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = socialRepository.searchUsers(query)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(results = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
