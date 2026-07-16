package com.example.final_project.domain.model

/**
 * Domain model for a Playlist — single source of truth across all layers.
 *
 * Mapping: PlaylistDto (data) → Playlist (domain) → UI composables.
 * Pure Kotlin only — no Android/framework imports.
 */
data class Playlist(
    val id: String,
    val userId: String?,          // null = global/system playlist
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val isPublic: Boolean,
    val isLocal: Boolean,         // true = Iranian/local music playlist
    val createdAt: String,
)