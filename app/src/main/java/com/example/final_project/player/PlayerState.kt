package com.example.final_project.player

import androidx.media3.common.Player
import com.example.final_project.domain.model.Song

/**
 * Immutable snapshot of all playback state — single source of truth for all UI.
 *
 * Exposed as [StateFlow<PlayerState>] from [PlayerController].
 */
data class PlayerState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isExpanded: Boolean = false,
    /** Current playback speed multiplier (1f, 1.5f, 2f). */
    val playbackSpeed: Float = 1f,
    /** Minutes remaining in sleep timer; null means timer is off. */
    val sleepTimerMinutesLeft: Int? = null,
    val isShuffleOn: Boolean = false,
    /** One of [Player.REPEAT_MODE_OFF], [Player.REPEAT_MODE_ONE], [Player.REPEAT_MODE_ALL]. */
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
)
