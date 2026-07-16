package com.example.final_project.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the `songs` table in Supabase (Task A / Phase 1 schema).
 *
 * Column mapping (snake_case → camelCase via @SerialName):
 *   id               → id
 *   title            → title
 *   artist_name      → artistName
 *   album_name       → albumName
 *   cover_image_url  → coverImageUrl
 *   audio_url        → audioUrl
 *   duration_seconds → durationSeconds
 *   genre            → genre
 *   is_local         → isLocal         (true = Iranian / local music)
 *   play_count       → playCount
 *   created_at       → createdAt
 *
 * All nullable fields use `= null` defaults so PostgREST SELECT with column
 * filtering doesn't break deserialization.
 */
@Serializable
data class SongDto(
    val id: String,
    val title: String,
    @SerialName("artist_name")      val artistName: String,
    @SerialName("album_name")       val albumName: String?      = null,
    @SerialName("cover_image_url")  val coverImageUrl: String?  = null,
    @SerialName("audio_url")        val audioUrl: String,
    @SerialName("duration_seconds") val durationSeconds: Int?   = null,
    val genre: String?                                          = null,
    @SerialName("is_local")         val isLocal: Boolean        = false,
    @SerialName("play_count")       val playCount: Long         = 0L,
    @SerialName("created_at")       val createdAt: String       = "",
)
