package com.example.final_project.domain.model

data class Artist(
    val name: String,
    val songCount: Int,
    val coverImageUrl: String?,
    val totalPlayCount: Long,
)
