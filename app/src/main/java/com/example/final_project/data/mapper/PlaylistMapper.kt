package com.example.final_project.data.mapper

import com.example.final_project.data.remote.dto.PlaylistDto
import com.example.final_project.domain.model.Playlist

/** Maps a single [PlaylistDto] to the domain [Playlist] model. */
fun PlaylistDto.toDomain(): Playlist = Playlist(
    id            = id,
    userId        = userId,
    name          = name,
    description   = description,
    coverImageUrl = coverImageUrl,
    isPublic      = isPublic,
    isLocal       = isLocal,
    createdAt     = createdAt,
)

/** Bulk mapper for list responses. */
fun List<PlaylistDto>.toDomain(): List<Playlist> = map { it.toDomain() }