package com.example.final_project.data.mapper

import com.example.final_project.data.remote.dto.SongDto
import com.example.final_project.domain.model.Song

fun SongDto.toDomain(): Song = Song(
    id = id,
    title = title,
    artistName = artistName,
    coverImageUrl = coverImageUrl,
    audioUrl = audioUrl,
)

fun List<SongDto>.toDomain(): List<Song> = map { it.toDomain() }
