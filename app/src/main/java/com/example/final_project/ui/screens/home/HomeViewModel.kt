package com.example.final_project.ui.screens.home

import com.example.final_project.core.mvi.BaseViewModel
import com.example.final_project.core.mvi.UiEffect
import com.example.final_project.core.mvi.UiEvent
import com.example.final_project.core.mvi.UiState
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.usecase.GetSongsUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val errorMessage: String? = null,
) : UiState

sealed interface HomeUiEvent : UiEvent {
    data object LoadSongs : HomeUiEvent
    data object Retry : HomeUiEvent
}

sealed interface HomeUiEffect : UiEffect {
    data class ShowError(val message: String) : HomeUiEffect
}

class HomeViewModel(
    private val getSongsUseCase: GetSongsUseCase,
) : BaseViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(HomeUiState()) {

    init {
        onEvent(HomeUiEvent.LoadSongs)
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.LoadSongs,
            HomeUiEvent.Retry,
            -> loadSongs()
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            getSongsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> setState {
                        copy(isLoading = true, errorMessage = null)
                    }
                    is Resource.Success -> setState {
                        copy(isLoading = false, songs = resource.data, errorMessage = null)
                    }
                    is Resource.Error -> {
                        val message = resource.message.orEmpty()
                        setState {
                            copy(isLoading = false, errorMessage = message)
                        }
                        sendEffect(HomeUiEffect.ShowError(message))
                    }
                }
            }
        }
    }
}
