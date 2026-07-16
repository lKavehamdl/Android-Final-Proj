package com.example.final_project.ui.screens.downloads

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.final_project.R
import com.example.final_project.ui.components.EmptyState

/** Step 1 placeholder — downloads list lands with WorkManager (Phase 5). */
@Composable
fun DownloadsScreen(modifier: Modifier = Modifier) {
    EmptyState(
        titleRes = R.string.downloads_empty_title,
        subtitleRes = R.string.downloads_empty_subtitle,
        modifier = modifier,
        icon = Icons.Outlined.Download,
    )
}
