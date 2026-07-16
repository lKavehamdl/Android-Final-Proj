package com.example.final_project.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Melodify spacing scale — use these instead of hardcoded dp paddings/margins.
 *
 * Example: `Modifier.padding(MelodifySpacing.md)`
 */
object MelodifySpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp

    /** Cover size for [com.example.final_project.ui.components.SongRow]. */
    val songRowCover = 56.dp

    /** Cover size for [com.example.final_project.ui.components.SongCard]. */
    val songCardCover = 140.dp

    /** Avatar in [com.example.final_project.ui.components.MelodifyTopBar]. */
    val topBarAvatar = 36.dp
}
