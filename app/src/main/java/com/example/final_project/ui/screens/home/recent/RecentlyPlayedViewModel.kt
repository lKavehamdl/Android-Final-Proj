package com.example.final_project.ui.screens.home.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import com.example.final_project.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

data class RecentlyPlayedUiState(
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

class RecentlyPlayedViewModel(
    private val songRepository: SongRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentlyPlayedUiState())
    val uiState: StateFlow<RecentlyPlayedUiState> = _uiState.asStateFlow()

    init {
        loadRecentlyPlayed()
    }

    private fun loadRecentlyPlayed() {
        viewModelScope.launch {
            songRepository.getRecentlyPlayed().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, songs = result.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    
    fun onRemoveRecent(songId: String) {
        viewModelScope.launch {
            songRepository.removeRecentlyPlayed(songId)
            loadRecentlyPlayed()
        }
    }

    fun playAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            playerController.play(songs.first(), songs)
        }
    }

    fun playSong(song: Song) {
        playerController.play(song, _uiState.value.songs)
    }
}
