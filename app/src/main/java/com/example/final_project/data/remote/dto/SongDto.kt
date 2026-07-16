package com.example.final_project.data.remote.dto

/**
 * Remote DTO — maps to [com.example.final_project.domain.model.Song] via mapper (Phase 1).
 */
data class SongDto(
    val id: String,
    val title: String,
    val artist_name: String,
    val cover_image_url: String,
    val audio_url: String,
)
