package com.example.final_project.ui.screens.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.final_project.core.result.Resource
import com.example.final_project.data.paging.PlaylistSongsPagingSource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.PlaylistRepository
import com.example.final_project.player.PlayerController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistDetailUiState(
    val playlistName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val songCount: Int = 0,
)

class PlaylistDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private var cachedSongs: List<Song> = emptyList()

    val songs: Flow<PagingData<Song>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = {
            PlaylistSongsPagingSource(
                playlistRepository = playlistRepository,
                playlistId = playlistId,
                pageSize = PAGE_SIZE,
            )
        },
    ).flow.cachedIn(viewModelScope)

    init {
        prefetchForPlayback()
    }

    private fun prefetchForPlayback() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = playlistRepository.getPlaylistSongs(playlistId).first { it !is Resource.Loading }) {
                is Resource.Success -> {
                    cachedSongs = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            songCount = result.data.size,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun playAll() {
        if (cachedSongs.isNotEmpty()) {
            playerController.play(cachedSongs.first(), cachedSongs)
        }
    }

    fun playSong(song: Song) {
        val queue = cachedSongs.ifEmpty { listOf(song) }
        playerController.play(song, queue)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
