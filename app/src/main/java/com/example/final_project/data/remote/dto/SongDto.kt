package com.example.final_project.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Matches the Supabase `songs` table (Task A schema).
 * Fields: id, title, artist_name, cover_image_url, audio_url
 */
@Serializable
data class SongDto(
    val id: String,
    val title: String,
    @SerialName("artist_name") val artistName: String,
    @SerialName("cover_image_url") val coverImageUrl: String,
    @SerialName("audio_url") val audioUrl: String,
)
