package com.example.final_project.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.datastore.SettingsDataStore
import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.core.mvi.BaseViewModel
import com.example.final_project.core.mvi.UiEffect
import com.example.final_project.core.mvi.UiEvent
import com.example.final_project.core.mvi.UiState
import com.example.final_project.ui.theme.ThemeMode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MainUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    /** False until the first DataStore language value is read. */
    val languageReady: Boolean = false,
) : UiState

sealed interface MainUiEvent : UiEvent {
    data class ThemeModeChanged(val themeMode: ThemeMode) : MainUiEvent
    data class LanguageChanged(val language: AppLanguage) : MainUiEvent
}

sealed interface MainUiEffect : UiEffect

class MainViewModel(
    private val settingsDataStore: SettingsDataStore,
) : BaseViewModel<MainUiState, MainUiEvent, MainUiEffect>(
    initialState = MainUiState(),
) {
    init {
        settingsDataStore.themeMode
            .onEach { themeStr ->
                val theme = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
                setState { copy(themeMode = theme) }
            }
            .launchIn(viewModelScope)

        settingsDataStore.language
            .onEach { langStr ->
                val lang = AppLanguage.fromTag(langStr)
                setState { copy(language = lang, languageReady = true) }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.ThemeModeChanged -> {
                viewModelScope.launch {
                    settingsDataStore.setThemeMode(event.themeMode.name)
                }
            }
            is MainUiEvent.LanguageChanged -> {
                // Apply immediately so Compose resources refresh without waiting for Flow round-trip.
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(event.language.tag),
                )
                setState { copy(language = event.language, languageReady = true) }
                viewModelScope.launch {
                    settingsDataStore.setLanguage(event.language.tag)
                }
            }
        }
    }
}
