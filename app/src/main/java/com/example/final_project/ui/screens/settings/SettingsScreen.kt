package com.example.final_project.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.final_project.BuildConfig
import com.example.final_project.R
import com.example.final_project.core.datastore.FontScale
import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.ui.components.MelodifyBackTopBar
import com.example.final_project.ui.theme.MelodifySpacing
import com.example.final_project.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    language: AppLanguage,
    fontScale: FontScale,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFontScaleChange: (FontScale) -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }

    val themeLabel = themeMode.label()
    val languageLabel = language.label()
    val fontLabel = fontScale.label()

    if (showThemeDialog) {
        SingleChoiceDialog(
            titleRes = R.string.settings_theme,
            options = ThemeMode.entries,
            selected = themeMode,
            labelOf = { it.label() },
            onDismiss = { showThemeDialog = false },
            onSelect = { onThemeModeChange(it); showThemeDialog = false },
        )
    }
    if (showLanguageDialog) {
        SingleChoiceDialog(
            titleRes = R.string.settings_language,
            options = AppLanguage.entries,
            selected = language,
            labelOf = { it.label() },
            onDismiss = { showLanguageDialog = false },
            onSelect = { onLanguageChange(it); showLanguageDialog = false },
        )
    }
    if (showFontDialog) {
        SingleChoiceDialog(
            titleRes = R.string.settings_font_size,
            options = FontScale.entries,
            selected = fontScale,
            labelOf = { it.label() },
            onDismiss = { showFontDialog = false },
            onSelect = { onFontScaleChange(it); showFontDialog = false },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout)) },
            text = { Text(stringResource(R.string.settings_logout_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MelodifyBackTopBar(
                title = stringResource(R.string.settings_title),
                onBack = onBackClick,
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = MelodifySpacing.md),
            verticalArrangement = Arrangement.spacedBy(MelodifySpacing.lg),
        ) {
            Spacer(modifier = Modifier.height(MelodifySpacing.xs))

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.settings_theme),
                    value = themeLabel,
                    onClick = { showThemeDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    value = languageLabel,
                    onClick = { showLanguageDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.FormatSize,
                    title = stringResource(R.string.settings_font_size),
                    value = fontLabel,
                    onClick = { showFontDialog = true },
                )
            }

            SettingsGroup {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = stringResource(R.string.settings_logout),
                    titleColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutDialog = true },
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MelodifySpacing.xl),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(start = MelodifySpacing.xl + MelodifySpacing.md),
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    value: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.height(MelodifySpacing.xl - MelodifySpacing.sm),
        )
        Spacer(modifier = Modifier.width(MelodifySpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun <T> SingleChoiceDialog(
    titleRes: Int,
    options: List<T>,
    selected: T,
    labelOf: @Composable (T) -> String,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = MelodifySpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = { onSelect(option) })
                        Spacer(modifier = Modifier.width(MelodifySpacing.sm))
                        Text(labelOf(option), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun AppLanguage.label(): String = when (this) {
    AppLanguage.ENGLISH -> stringResource(R.string.settings_language_en)
    AppLanguage.PERSIAN -> stringResource(R.string.settings_language_fa)
}

@Composable
private fun FontScale.label(): String = when (this) {
    FontScale.SMALL -> stringResource(R.string.settings_font_small)
    FontScale.MEDIUM -> stringResource(R.string.settings_font_medium)
    FontScale.LARGE -> stringResource(R.string.settings_font_large)
}
