package com.example.final_project.ui.screens.home.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.favorites.FavoritesStore
import com.example.final_project.domain.model.Song
import com.example.final_project.player.PlayerController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LikedSongsUiState(
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null,
)

class LikedSongsViewModel(
    private val favoritesStore: FavoritesStore,
    private val playerController: PlayerController,
) : ViewModel() {

    val uiState: StateFlow<LikedSongsUiState> = favoritesStore.likedSongs
        .map { resource ->
            when (resource) {
                is Resource.Loading -> LikedSongsUiState(isLoading = true)
                is Resource.Success -> LikedSongsUiState(songs = resource.data)
                is Resource.Error -> LikedSongsUiState(error = resource.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LikedSongsUiState(isLoading = true),
        )

    init {
        viewModelScope.launch { favoritesStore.refresh() }
    }

    fun onUnlike(songId: String) {
        viewModelScope.launch {
            favoritesStore.toggle(songId)
        }
    }

    fun playAll() {
        val songs = uiState.value.songs
        if (songs.isNotEmpty()) {
            playerController.play(songs.first(), songs)
        }
    }

    fun playSong(song: Song) {
        playerController.play(song, uiState.value.songs)
    }
}
