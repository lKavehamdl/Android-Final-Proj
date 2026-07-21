package com.example.final_project.ui.screens.home

import androidx.lifecycle.viewModelScope
import com.example.final_project.R
import com.example.final_project.core.mvi.BaseViewModel
import com.example.final_project.core.mvi.UiEffect
import com.example.final_project.core.mvi.UiEvent
import com.example.final_project.core.mvi.UiState
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.usecase.GetLocalSongsUseCase
import com.example.final_project.domain.usecase.GetNewReleasesUseCase
import com.example.final_project.domain.usecase.GetSongsUseCase
import com.example.final_project.domain.usecase.GetTrendingSongsUseCase
import com.example.final_project.player.PlayerController
import com.example.final_project.core.datastore.SettingsDataStore
import com.example.final_project.domain.repository.DownloadsRepository
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val carousel: List<Song> = emptyList(),
    val recommended: List<Song> = emptyList(),
    val trending: List<Song> = emptyList(),
    val newReleases: List<Song> = emptyList(),
    val globalPlaylists: List<Song> = emptyList(),
    val localPlaylists: List<Song> = emptyList(),
    val errorMessage: String? = null,
    val isPremium: Boolean = false,
    val downloadedIds: Set<String> = emptySet(),
) : UiState

sealed interface HomeUiEvent : UiEvent {
    data object Load : HomeUiEvent
    data object Retry : HomeUiEvent
    data class SongClick(val song: Song) : HomeUiEvent
    data class DownloadClick(val song: Song) : HomeUiEvent
    data class QuickActionClick(val action: HomeQuickAction) : HomeUiEvent
}

enum class HomeQuickAction {
    LIKED,
    RECENT,
    PLAYLISTS,
    ARTISTS,
}

sealed interface HomeUiEffect : UiEffect {
    data class ShowMessage(@androidx.annotation.StringRes val messageRes: Int) : HomeUiEffect
    data object OpenPlaylistsTab : HomeUiEffect
    data object OpenLikedSongs : HomeUiEffect
    data object OpenRecentSongs : HomeUiEffect
    data object OpenArtists : HomeUiEffect
}

class HomeViewModel(
    private val getSongsUseCase: GetSongsUseCase,
    private val getTrendingSongsUseCase: GetTrendingSongsUseCase,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val getLocalSongsUseCase: GetLocalSongsUseCase,
    private val playerController: PlayerController,
    private val settingsDataStore: SettingsDataStore,
    private val downloadsRepository: DownloadsRepository,
    private val songRepository: SongRepository,
) : BaseViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(HomeUiState()) {

    init {
        onEvent(HomeUiEvent.Load)
        
        viewModelScope.launch {
            settingsDataStore.isPremium.collect { isPrem ->
                setState { copy(isPremium = isPrem) }
            }
        }
        viewModelScope.launch {
            downloadsRepository.downloadedSongs.collect { list ->
                setState { copy(downloadedIds = list.map { it.song.id }.toSet()) }
            }
        }
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Load,
            HomeUiEvent.Retry,
            -> loadHome()

            is HomeUiEvent.SongClick -> {
                playerController.play(event.song, buildQueue())
            }
            
            is HomeUiEvent.DownloadClick -> {
                if (currentState().isPremium) {
                    downloadsRepository.startDownload(event.song)
                } else {
                    sendEffect(HomeUiEffect.ShowMessage(R.string.upgrade_needed_message))
                }
            }

            is HomeUiEvent.QuickActionClick -> when (event.action) {
                HomeQuickAction.PLAYLISTS -> sendEffect(HomeUiEffect.OpenPlaylistsTab)
                HomeQuickAction.LIKED -> sendEffect(HomeUiEffect.OpenLikedSongs)
                HomeQuickAction.RECENT -> sendEffect(HomeUiEffect.OpenRecentSongs)
                HomeQuickAction.ARTISTS -> sendEffect(HomeUiEffect.OpenArtists)
            }
        }
    }

    private fun buildQueue(): List<Song> {
        val state = currentState()
        return (state.trending + state.newReleases + state.globalPlaylists + state.localPlaylists)
            .distinctBy { it.id }
            .ifEmpty { state.carousel }
    }

    private fun loadHome() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            try {
                coroutineScope {
                    val trendingRes = async {
                        getTrendingSongsUseCase(20).first { it !is Resource.Loading }
                    }
                    val newRes = async {
                        getNewReleasesUseCase(20).first { it !is Resource.Loading }
                    }
                    val localRes = async {
                        getLocalSongsUseCase().first { it !is Resource.Loading }
                    }
                    val allRes = async {
                        getSongsUseCase().first { it !is Resource.Loading }
                    }

                    val trendingResult = trendingRes.await()
                    val newResult = newRes.await()
                    val localResult = localRes.await()
                    val allResult = allRes.await()

                    val trending = trendingResult.successOrEmpty()
                    val newReleases = newResult.successOrEmpty()
                    val local = localResult.successOrEmpty()
                    val all = allResult.successOrEmpty()

                    val firstError = listOf(trendingResult, newResult, localResult, allResult)
                        .filterIsInstance<Resource.Error>()
                        .firstOrNull()

                    if (trending.isEmpty() && newReleases.isEmpty() && all.isEmpty()) {
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = firstError?.message
                                    ?: "empty",
                            )
                        }
                        return@coroutineScope
                    }

                    setState {
                        copy(
                            isLoading = false,
                            carousel = (trending.ifEmpty { all }).take(6),
                            trending = trending.ifEmpty { all.take(12) },
                            newReleases = newReleases.ifEmpty { all.takeLast(12) },
                            globalPlaylists = all.filter { !it.isLocal }.ifEmpty { all }.take(12),
                            localPlaylists = local.ifEmpty { all.filter { it.isLocal } }.take(12),
                            errorMessage = null,
                        )
                    }

                    // Seed recommendations off the last thing they played, falling
                    // back to the top trending song so the row is populated on a
                    // fresh account too.
                    loadRecommended(fallbackSeed = trending.firstOrNull() ?: all.firstOrNull())
                }
            } catch (e: Exception) {
                setState { copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** Loads "Recommended for you" from the user's most recently played song. */
    private fun loadRecommended(fallbackSeed: Song?) {
        viewModelScope.launch {
            val recent = (songRepository.getRecentlyPlayed().first { it !is Resource.Loading } as? Resource.Success)?.data
            val seed = recent?.firstOrNull() ?: fallbackSeed ?: return@launch
            when (val res = songRepository.getRecommendations(seed, 12)) {
                is Resource.Success -> setState { copy(recommended = res.data) }
                else -> Unit
            }
        }
    }

    private fun Resource<List<Song>>.successOrEmpty(): List<Song> =
        (this as? Resource.Success)?.data.orEmpty()
}
