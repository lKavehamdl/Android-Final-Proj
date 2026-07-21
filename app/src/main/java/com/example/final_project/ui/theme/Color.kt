package com.example.final_project.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  Brand palette — Melodify (MX Player-inspired)
//  Cool, neutral, dark-first UI with a single bright blue accent.
//  Primary  : Blue    — play actions / accents / selection
//  Secondary: Steel   — secondary chips / containers
//  Tertiary : Cyan    — highlights / premium
// ─────────────────────────────────────────────────────────────────────────────

val MxBlue        = Color(0xFF2979FF)
val MxBlueLight   = Color(0xFF4C8DFF)
val MxCyan        = Color(0xFF29B6F6)
val MxSteel       = Color(0xFF565E71)

/** Prefer [MaterialTheme.colorScheme.tertiary] for premium accents. */
val GoldPremium = MxCyan

// ── Dark Color Scheme (primary MX Player look) ───────────────────────────────

val DarkPrimary               = Color(0xFF4C8DFF)
val DarkOnPrimary             = Color(0xFF002E6B)
val DarkPrimaryContainer      = Color(0xFF00429B)
val DarkOnPrimaryContainer    = Color(0xFFD8E2FF)

val DarkSecondary             = Color(0xFFBBC7DB)
val DarkOnSecondary           = Color(0xFF253141)
val DarkSecondaryContainer    = Color(0xFF3B4758)
val DarkOnSecondaryContainer  = Color(0xFFD7E3F8)

val DarkTertiary              = Color(0xFF7FD0E8)
val DarkOnTertiary            = Color(0xFF00363F)
val DarkTertiaryContainer     = Color(0xFF164E5B)
val DarkOnTertiaryContainer   = Color(0xFFA6ECFF)

// Cool near-black neutrals — MX Player's dark charcoal UI.
val DarkBackground            = Color(0xFF0E1113)
val DarkOnBackground          = Color(0xFFE3E2E6)
val DarkSurface               = Color(0xFF16191C)
val DarkOnSurface             = Color(0xFFE3E2E6)
val DarkSurfaceVariant        = Color(0xFF262A2F)
val DarkOnSurfaceVariant      = Color(0xFFC3C7CE)
val DarkOutline               = Color(0xFF8D9199)
val DarkOutlineVariant        = Color(0xFF43474E)
val DarkScrim                 = Color(0xFF000000)

val DarkError                 = Color(0xFFFFB4AB)
val DarkOnError               = Color(0xFF690005)
val DarkErrorContainer        = Color(0xFF93000A)
val DarkOnErrorContainer      = Color(0xFFFFDAD6)

// ── Light Color Scheme ───────────────────────────────────────────────────────

val LightPrimary              = Color(0xFF175CD3)
val LightOnPrimary            = Color(0xFFFFFFFF)
val LightPrimaryContainer     = Color(0xFFD8E2FF)
val LightOnPrimaryContainer   = Color(0xFF001A43)

val LightSecondary            = Color(0xFF565E71)
val LightOnSecondary          = Color(0xFFFFFFFF)
val LightSecondaryContainer   = Color(0xFFDAE2F9)
val LightOnSecondaryContainer = Color(0xFF131C2B)

val LightTertiary             = Color(0xFF00687A)
val LightOnTertiary           = Color(0xFFFFFFFF)
val LightTertiaryContainer    = Color(0xFFABEDFF)
val LightOnTertiaryContainer  = Color(0xFF001F27)

// Cool off-white neutrals.
val LightBackground           = Color(0xFFFAFAFD)
val LightOnBackground         = Color(0xFF1A1C1E)
val LightSurface              = Color(0xFFFFFFFF)
val LightOnSurface            = Color(0xFF1A1C1E)
val LightSurfaceVariant       = Color(0xFFE0E2EC)
val LightOnSurfaceVariant     = Color(0xFF43474E)
val LightOutline              = Color(0xFF74777F)
val LightOutlineVariant       = Color(0xFFC4C6D0)
val LightScrim                = Color(0xFF000000)

val LightError                = Color(0xFFBA1A1A)
val LightOnError              = Color(0xFFFFFFFF)
val LightErrorContainer       = Color(0xFFFFDAD6)
val LightOnErrorContainer     = Color(0xFF410002)
