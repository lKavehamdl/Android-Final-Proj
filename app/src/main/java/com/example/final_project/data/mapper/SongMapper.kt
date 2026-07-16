package com.example.final_project.data.mapper

import com.example.final_project.data.remote.dto.SongDto
import com.example.final_project.domain.model.Song

fun SongDto.toDomain(): Song = Song(
    id = id,
    title = title,
    artistName = artist_name,
    coverImageUrl = cover_image_url,
    audioUrl = audio_url,
)

fun List<SongDto>.toDomain(): List<Song> = map { it.toDomain() }
