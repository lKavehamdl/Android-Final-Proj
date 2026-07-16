package com.example.final_project.ui.screens.playlists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.final_project.R
import com.example.final_project.ui.components.EmptyState

/** Step 1 placeholder — grid + sections in Step 2 / later playlist work. */
@Composable
fun PlaylistsScreen(modifier: Modifier = Modifier) {
    EmptyState(
        titleRes = R.string.playlists_empty_title,
        subtitleRes = R.string.playlists_empty_subtitle,
        modifier = modifier,
        icon = Icons.Outlined.QueueMusic,
    )
}
