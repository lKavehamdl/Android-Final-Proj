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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
    val isOwner: Boolean = false,
    val isDeleting: Boolean = false,
)

sealed interface PlaylistDetailEffect {
    data object Deleted : PlaylistDetailEffect
    data class ShowMessage(val message: String) : PlaylistDetailEffect
}

class PlaylistDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController,
    private val supabaseClient: SupabaseClient,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PlaylistDetailEffect>()
    val effect: SharedFlow<PlaylistDetailEffect> = _effect

    private var cachedSongs: List<Song> = emptyList()
    private var latestPagingSource: PlaylistSongsPagingSource? = null

    val songs: Flow<PagingData<Song>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = {
            PlaylistSongsPagingSource(
                playlistRepository = playlistRepository,
                playlistId = playlistId,
                pageSize = PAGE_SIZE,
            ).also { latestPagingSource = it }
        },
    ).flow.cachedIn(viewModelScope)

    init {
        loadPlaylistMeta()
        prefetchForPlayback()
    }

    private fun loadPlaylistMeta() {
        viewModelScope.launch {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            when (val result = playlistRepository.getPlaylistById(playlistId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            playlistName = result.data.name,
                            isOwner = currentUserId != null && currentUserId == result.data.userId,
                        )
                    }
                }
                else -> Unit
            }
        }
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

    /** Called after a song was added via the picker sheet, or removed from a row, to refresh the list. */
    fun onSongsChanged() {
        latestPagingSource?.invalidate()
        prefetchForPlayback()
    }

    fun removeSong(song: Song) {
        viewModelScope.launch {
            when (val result = playlistRepository.removeSongFromPlaylist(playlistId, song.id)) {
                is Resource.Success -> onSongsChanged()
                is Resource.Error -> _effect.emit(
                    PlaylistDetailEffect.ShowMessage(result.message ?: "Couldn't remove song"),
                )
                Resource.Loading -> Unit
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            when (val result = playlistRepository.deletePlaylist(playlistId)) {
                is Resource.Success -> _effect.emit(PlaylistDetailEffect.Deleted)
                is Resource.Error -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    _effect.emit(PlaylistDetailEffect.ShowMessage(result.message ?: "Couldn't delete playlist"))
                }
                Resource.Loading -> Unit
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
