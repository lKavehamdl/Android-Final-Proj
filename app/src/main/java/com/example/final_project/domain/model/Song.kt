package com.example.final_project.domain.model

/**
 * Domain model for a Song — single source of truth across all app layers.
 *
 * Mapping:
 *   SongDto  (data layer) → Song (domain) → UI composables
 *
 * Rules:
 *  - Never expose DTOs or Room entities to the UI.
 *  - No Android / framework imports here (pure Kotlin only).
 */
data class Song(
    val id: String,
    val title: String,
    val artistName: String,
    val albumName: String?,
    val coverImageUrl: String?,
    val audioUrl: String,
    val durationSeconds: Int?,
    val genre: String?,
    val isLocal: Boolean,       // true = Iranian / local music
    val playCount: Long,
    val createdAt: String,
)
