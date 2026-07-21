package com.example.final_project

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.core.notification.NotificationHelper
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

            // Request both permissions in one call — launching two separate
            // RequestPermission contracts back-to-back makes Android drop the
            // second ("Can request only one set of permissions at a time"),
            // which was leaving POST_NOTIFICATIONS ungranted → no message alerts.
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { /* notifications + visualizer both degrade gracefully if denied */ }

            LaunchedEffect(Unit) {
                val needed = buildList {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !NotificationHelper.hasPostNotificationsPermission(this@MainActivity)
                    ) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO,
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        add(Manifest.permission.RECORD_AUDIO)
                    }
                }
                if (needed.isNotEmpty()) {
                    permissionLauncher.launch(needed.toTypedArray())
                }
            }

            LaunchedEffect(state.languageReady, state.language) {
                if (!state.languageReady) return@LaunchedEffect
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(state.language.tag),
                )
            }

            FinalProjectTheme(themeMode = state.themeMode, fontScale = state.fontScale) {
                MelodifyApp(
                    themeMode = state.themeMode,
                    language = state.language,
                    fontScale = state.fontScale,
                    onThemeModeChange = {
                        mainViewModel.onEvent(MainUiEvent.ThemeModeChanged(it))
                    },
                    onLanguageChange = {
                        mainViewModel.onEvent(MainUiEvent.LanguageChanged(it))
                    },
                    onFontScaleChange = {
                        mainViewModel.onEvent(MainUiEvent.FontScaleChanged(it))
                    },
                )
            }
        }
    }
}
