package com.example.final_project.data.mapper

import com.example.final_project.data.remote.dto.SongDto
import com.example.final_project.domain.model.Song

/** Maps a single [SongDto] to the domain [Song] model. */
fun SongDto.toDomain(): Song = Song(
    id              = id,
    title           = title,
    artistName      = artistName,
    albumName       = albumName,
    coverImageUrl   = coverImageUrl,
    audioUrl        = audioUrl,
    durationSeconds = durationSeconds,
    genre           = genre,
    isLocal         = isLocal,
    playCount       = playCount,
    createdAt       = createdAt,
)

/** Bulk mapper for list responses. */
fun List<SongDto>.toDomain(): List<Song> = map { it.toDomain() }
