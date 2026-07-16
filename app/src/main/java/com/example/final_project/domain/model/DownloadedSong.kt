package com.example.final_project.domain.model

/**
 * Domain model for a song that has been downloaded for offline playback.
 *
 * Mapping: Room [DownloadEntity] → [DownloadedSong] (via mapper) → UI
 *
 * Note: The actual Room entity and mapper are introduced in Phase 2.
 * This model is defined early so Phase 3 UI can compile independently.
 */
data class DownloadedSong(
    val id: String,
    val song: Song,
    /** Absolute path to the cached file on-device. */
    val localPath: String,
    /** File size in bytes — used for "By Size" sort. */
    val fileSizeBytes: Long,
    /** ISO-8601 timestamp string — used for "By Date" sort. */
    val downloadedAt: String,
)
