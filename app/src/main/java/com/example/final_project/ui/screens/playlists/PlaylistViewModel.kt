package com.example.final_project.ui.screens.playlists

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.example.final_project.R
import com.example.final_project.core.mvi.BaseViewModel
import com.example.final_project.core.mvi.UiEffect
import com.example.final_project.core.mvi.UiEvent
import com.example.final_project.core.mvi.UiState
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.repository.PlaylistRepository
import com.example.final_project.domain.usecase.GetPublicPlaylistsUseCase
import com.example.final_project.domain.usecase.GetUserPlaylistsUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlaylistsUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val worldPlaylists: List<Playlist> = emptyList(),
    val localPlaylists: List<Playlist> = emptyList(),
    val userPlaylists: List<Playlist> = emptyList(),
    val errorMessage: String? = null,
) : UiState {
    val isEmpty: Boolean
        get() = worldPlaylists.isEmpty() &&
                localPlaylists.isEmpty() &&
                userPlaylists.isEmpty()
}

sealed interface PlaylistsUiEvent : UiEvent {
    data object Load : PlaylistsUiEvent
    data object Retry : PlaylistsUiEvent
    data class PlaylistClick(val playlist: Playlist) : PlaylistsUiEvent
    data object CreatePlaylistClick : PlaylistsUiEvent
    data object DismissCreateDialog : PlaylistsUiEvent
    data class ConfirmCreatePlaylist(val name: String) : PlaylistsUiEvent
}

sealed interface PlaylistsUiEffect : UiEffect {
    data class OpenPlaylist(val playlistId: String) : PlaylistsUiEffect
    data class ShowMessage(@StringRes val messageRes: Int) : PlaylistsUiEffect
    data class ShowTextMessage(val message: String) : PlaylistsUiEffect
}

class PlaylistsViewModel(
    private val getPublicPlaylistsUseCase: GetPublicPlaylistsUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
    private val playlistRepository: PlaylistRepository,
    private val supabase: SupabaseClient,
) : BaseViewModel<PlaylistsUiState, PlaylistsUiEvent, PlaylistsUiEffect>(PlaylistsUiState()) {

    private val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    init {
        onEvent(PlaylistsUiEvent.Load)
    }

    override fun onEvent(event: PlaylistsUiEvent) {
        when (event) {
            PlaylistsUiEvent.Load,
            PlaylistsUiEvent.Retry,
            -> loadPlaylists()

            is PlaylistsUiEvent.PlaylistClick ->
                sendEffect(PlaylistsUiEffect.OpenPlaylist(event.playlist.id))

            PlaylistsUiEvent.CreatePlaylistClick ->
                setState { copy(showCreateDialog = true) }

            PlaylistsUiEvent.DismissCreateDialog ->
                setState { copy(showCreateDialog = false) }

            is PlaylistsUiEvent.ConfirmCreatePlaylist ->
                createPlaylist(event.name)
        }
    }

    private fun createPlaylist(name: String) {
        val userId = currentUserId
        if (userId == null) {
            sendEffect(PlaylistsUiEffect.ShowMessage(R.string.error_generic))
            return
        }
        viewModelScope.launch {
            setState { copy(isCreating = true) }
            when (val result = playlistRepository.createPlaylist(userId = userId, name = name)) {
                is Resource.Success -> {
                    setState { copy(isCreating = false, showCreateDialog = false) }
                    sendEffect(PlaylistsUiEffect.ShowMessage(R.string.playlist_created))
                    loadPlaylists()
                }
                is Resource.Error -> {
                    setState { copy(isCreating = false) }
                    sendEffect(
                        PlaylistsUiEffect.ShowTextMessage(
                            result.message ?: "error",
                        ),
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            try {
                coroutineScope {
                    val publicRes = async {
                        getPublicPlaylistsUseCase().first { it !is Resource.Loading }
                    }
                    val userRes = async {
                        currentUserId?.let { id ->
                            getUserPlaylistsUseCase(id).first { it !is Resource.Loading }
                        }
                    }

                    val publicResult = publicRes.await()
                    val userResult = userRes.await()

                    val public = (publicResult as? Resource.Success)?.data.orEmpty()
                    val user = (userResult as? Resource.Success)?.data.orEmpty()

                    val firstError = (publicResult as? Resource.Error)

                    if (public.isEmpty() && user.isEmpty() && firstError != null) {
                        setState {
                            copy(isLoading = false, errorMessage = firstError.message ?: "error")
                        }
                        return@coroutineScope
                    }

                    setState {
                        copy(
                            isLoading = false,
                            worldPlaylists = public.filter { !it.isLocal },
                            localPlaylists = public.filter { it.isLocal },
                            userPlaylists = user,
                            errorMessage = null,
                        )
                    }
                }
            } catch (e: Exception) {
                setState { copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
