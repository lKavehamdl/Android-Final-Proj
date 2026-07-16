package com.example.final_project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// ─────────────────────────────────────────────────────────────────────────────
//  Theme Mode
//  SYSTEM  → follow system dark/light setting  (default)
//  LIGHT   → always light
//  DARK    → always dark
//
//  ThemeMode is persisted in DataStore and injected here via the composable
//  parameter. ViewModels read DataStore and pass ThemeMode down to this call
//  site in MainActivity (Phase 2 wires this up).
// ─────────────────────────────────────────────────────────────────────────────

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Composition local so any composable can read the current ThemeMode. */
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

// ── Color Schemes ─────────────────────────────────────────────────────────────

private val MelodifyDarkColorScheme = darkColorScheme(
    primary               = DarkPrimary,
    onPrimary             = DarkOnPrimary,
    primaryContainer      = DarkPrimaryContainer,
    onPrimaryContainer    = DarkOnPrimaryContainer,
    secondary             = DarkSecondary,
    onSecondary           = DarkOnSecondary,
    secondaryContainer    = DarkSecondaryContainer,
    onSecondaryContainer  = DarkOnSecondaryContainer,
    tertiary              = DarkTertiary,
    onTertiary            = DarkOnTertiary,
    tertiaryContainer     = DarkTertiaryContainer,
    onTertiaryContainer   = DarkOnTertiaryContainer,
    background            = DarkBackground,
    onBackground          = DarkOnBackground,
    surface               = DarkSurface,
    onSurface             = DarkOnSurface,
    surfaceVariant        = DarkSurfaceVariant,
    onSurfaceVariant      = DarkOnSurfaceVariant,
    outline               = DarkOutline,
    outlineVariant        = DarkOutlineVariant,
    scrim                 = DarkScrim,
    error                 = DarkError,
    onError               = DarkOnError,
    errorContainer        = DarkErrorContainer,
    onErrorContainer      = DarkOnErrorContainer,
)

private val MelodifyLightColorScheme = lightColorScheme(
    primary               = LightPrimary,
    onPrimary             = LightOnPrimary,
    primaryContainer      = LightPrimaryContainer,
    onPrimaryContainer    = LightOnPrimaryContainer,
    secondary             = LightSecondary,
    onSecondary           = LightOnSecondary,
    secondaryContainer    = LightSecondaryContainer,
    onSecondaryContainer  = LightOnSecondaryContainer,
    tertiary              = LightTertiary,
    onTertiary            = LightOnTertiary,
    tertiaryContainer     = LightTertiaryContainer,
    onTertiaryContainer   = LightOnTertiaryContainer,
    background            = LightBackground,
    onBackground          = LightOnBackground,
    surface               = LightSurface,
    onSurface             = LightOnSurface,
    surfaceVariant        = LightSurfaceVariant,
    onSurfaceVariant      = LightOnSurfaceVariant,
    outline               = LightOutline,
    outlineVariant        = LightOutlineVariant,
    scrim                 = LightScrim,
    error                 = LightError,
    onError               = LightOnError,
    errorContainer        = LightErrorContainer,
    onErrorContainer      = LightOnErrorContainer,
)

// ── Composable Theme Entry Point ──────────────────────────────────────────────

/**
 * Root theme composable for the Melodify app.
 *
 * @param themeMode  Overrides the system dark/light setting.
 *                   Pass ThemeMode.SYSTEM to respect system preference.
 *                   In Phase 2, this will be driven by DataStore.
 * @param content    The composable content tree.
 */
@Composable
fun FinalProjectTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = if (isDark) MelodifyDarkColorScheme else MelodifyLightColorScheme

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = MelodifyTypography,
            shapes      = MelodifyShapes,
            content     = content,
        )
    }
}