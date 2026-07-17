package com.example.final_project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSongDto(
    val songs: SongDto
)
