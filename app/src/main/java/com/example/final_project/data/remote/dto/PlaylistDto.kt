package com.example.final_project.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the `playlists` table in Supabase.
 * snake_case → camelCase via @SerialName. Nullable fields default to null
 * so column-filtered SELECTs don't break deserialization.
 */
@Serializable
data class PlaylistDto(
    val id: String,
    @SerialName("user_id")          val userId: String?         = null,
    val name: String,
    val description: String?                                    = null,
    @SerialName("cover_image_url")  val coverImageUrl: String?  = null,
    @SerialName("is_public")        val isPublic: Boolean       = false,
    @SerialName("is_local")         val isLocal: Boolean        = false,
    @SerialName("created_at")       val createdAt: String       = "",
)