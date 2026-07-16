package com.example.final_project.player

import com.example.final_project.domain.model.Song

data class PlayerState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isExpanded: Boolean = false,
)
