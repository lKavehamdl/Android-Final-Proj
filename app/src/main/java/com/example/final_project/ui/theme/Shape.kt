package com.example.final_project.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Melodify Shape System
 *
 * extraSmall → chips, badges, tiny indicators
 * small      → buttons, input fields, small cards
 * medium     → song cards, search results
 * large      → album art cards, bottom sheets
 * extraLarge → full-rounded panels, mini-player
 *
 * DO NOT use hardcoded corner radius values elsewhere in the codebase.
 * Always reference MaterialTheme.shapes.*
 */
val MelodifyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
