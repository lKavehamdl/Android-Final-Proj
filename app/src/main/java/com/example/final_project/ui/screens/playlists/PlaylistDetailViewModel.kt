package com.example.final_project.ui.screens.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.PlaylistRepository
import com.example.final_project.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistDetailUiState(
    val playlistName: String = "Playlist",
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

class PlaylistDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlaylistSongs()
    }

    private fun loadPlaylistSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            playlistRepository.getPlaylistSongs(playlistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, songs = result.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
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
