package com.example.final_project.ui

import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.core.mvi.BaseViewModel
import com.example.final_project.core.mvi.UiEffect
import com.example.final_project.core.mvi.UiEvent
import com.example.final_project.core.mvi.UiState
import com.example.final_project.ui.theme.ThemeMode

data class MainUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
) : UiState

sealed interface MainUiEvent : UiEvent {
    data class ThemeModeChanged(val themeMode: ThemeMode) : MainUiEvent
    data class LanguageChanged(val language: AppLanguage) : MainUiEvent
}

sealed interface MainUiEffect : UiEffect

class MainViewModel : BaseViewModel<MainUiState, MainUiEvent, MainUiEffect>(
    initialState = MainUiState(),
) {
    override fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.ThemeModeChanged -> setState { copy(themeMode = event.themeMode) }
            is MainUiEvent.LanguageChanged -> setState { copy(language = event.language) }
        }
    }
}
