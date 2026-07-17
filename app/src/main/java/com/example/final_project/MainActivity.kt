package com.example.final_project

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.ui.MainUiEvent
import com.example.final_project.ui.MainViewModel
import com.example.final_project.ui.navigation.MelodifyApp
import com.example.final_project.ui.theme.FinalProjectTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by mainViewModel.state.collectAsStateWithLifecycle()

            // Only apply after DataStore has emitted so we don't overwrite FA with default EN.
            LaunchedEffect(state.languageReady, state.language) {
                if (!state.languageReady) return@LaunchedEffect
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(state.language.tag),
                )
            }

            FinalProjectTheme(themeMode = state.themeMode) {
                MelodifyApp(
                    themeMode = state.themeMode,
                    language = state.language,
                    onThemeModeChange = {
                        mainViewModel.onEvent(MainUiEvent.ThemeModeChanged(it))
                    },
                    onLanguageChange = {
                        mainViewModel.onEvent(MainUiEvent.LanguageChanged(it))
                    },
                )
            }
        }
    }
}
