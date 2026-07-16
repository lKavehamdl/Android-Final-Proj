package com.example.final_project.domain.model

/**
 * Pure Kotlin domain model — no Android dependencies.
 * Populated from backend in Phase 1.
 */
data class Song(
    val id: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String,
)
