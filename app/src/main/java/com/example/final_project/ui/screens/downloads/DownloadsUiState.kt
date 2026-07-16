package com.example.final_project.ui.screens.downloads

import com.example.final_project.domain.model.DownloadedSong
import com.example.final_project.domain.model.Song

/** Sort order options for the Downloads list. */
enum class DownloadSortOrder {
    BY_NAME,
    BY_DATE,
    BY_SIZE,
}

/**
 * Immutable UI state for [DownloadsScreen].
 */
data class DownloadsUiState(
    val songs: List<DownloadedSong> = emptyList(),
    val sortOrder: DownloadSortOrder = DownloadSortOrder.BY_DATE,
    val isLoading: Boolean = false,
) {
    /** Sorted view of songs — derived from [songs] + [sortOrder], no extra state. */
    val sortedSongs: List<DownloadedSong>
        get() = when (sortOrder) {
            DownloadSortOrder.BY_NAME -> songs.sortedBy { it.song.title.lowercase() }
            DownloadSortOrder.BY_DATE -> songs.sortedByDescending { it.downloadedAt }
            DownloadSortOrder.BY_SIZE -> songs.sortedByDescending { it.fileSizeBytes }
        }
}
