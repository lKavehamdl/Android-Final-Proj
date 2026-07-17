package com.example.final_project.ui.screens.home.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Artist
import com.example.final_project.domain.usecase.GetSongsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TopArtistsUiState(
    val isLoading: Boolean = false,
    val artists: List<Artist> = emptyList(),
    val error: String? = null,
)

class TopArtistsViewModel(
    private val getSongsUseCase: GetSongsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopArtistsUiState())
    val uiState: StateFlow<TopArtistsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = TopArtistsUiState(isLoading = true)
            when (val result = getSongsUseCase().first { it !is Resource.Loading }) {
                is Resource.Success -> {
                    val artists = result.data
                        .groupBy { it.artistName }
                        .map { (name, songs) ->
                            Artist(
                                name = name,
                                songCount = songs.size,
                                coverImageUrl = songs.firstOrNull()?.coverImageUrl,
                                totalPlayCount = songs.sumOf { it.playCount },
                            )
                        }
                        .sortedByDescending { it.totalPlayCount }
                    _uiState.value = TopArtistsUiState(artists = artists)
                }
                is Resource.Error -> {
                    _uiState.value = TopArtistsUiState(error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
