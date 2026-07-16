package com.example.final_project.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.final_project.R
import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.ui.theme.ThemeMode

/**
 * Profile placeholder with temporary theme/language controls for Phase 0 verification.
 * Full profile UI comes in Phase 3; settings move to Settings screen in Phase 7.
 */
@Composable
fun ProfileScreen(
    themeMode: ThemeMode,
    language: AppLanguage,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.tab_profile),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = themeMode == mode,
                onClick = { onThemeModeChange(mode) },
                label = {
                    Text(
                        text = when (mode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                            ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                        },
                    )
                },
            )
        }

        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )
        AppLanguage.entries.forEach { appLanguage ->
            FilterChip(
                selected = language == appLanguage,
                onClick = { onLanguageChange(appLanguage) },
                label = {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> stringResource(R.string.settings_language_en)
                            AppLanguage.PERSIAN -> stringResource(R.string.settings_language_fa)
                        },
                    )
                },
            )
        }
    }
}
