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
import com.example.final_project.domain.usecase.GetPublicPlaylistsUseCase
import com.example.final_project.domain.usecase.GetUserPlaylistsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlaylistsUiState(
    val isLoading: Boolean = false,
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
}

sealed interface PlaylistsUiEffect : UiEffect {
    data class OpenPlaylist(val playlistId: String) : PlaylistsUiEffect
    data class ShowMessage(@StringRes val messageRes: Int) : PlaylistsUiEffect
}

class PlaylistsViewModel(
    private val getPublicPlaylistsUseCase: GetPublicPlaylistsUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
) : BaseViewModel<PlaylistsUiState, PlaylistsUiEvent, PlaylistsUiEffect>(PlaylistsUiState()) {

    // TODO: replace with real logged-in user id once Auth (Task C) is wired.
    private val currentUserId: String? = null

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
                sendEffect(PlaylistsUiEffect.ShowMessage(R.string.coming_soon))
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