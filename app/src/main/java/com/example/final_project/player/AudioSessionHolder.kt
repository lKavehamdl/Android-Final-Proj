package com.example.final_project.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-process singleton that bridges the [MelodifyPlaybackService]
 * (which owns the ExoPlayer) to [PlayerController] (which runs in the UI process).
 *
 * The service writes [audioSessionId] immediately after the ExoPlayer is created.
 * Task B's audio visualizer reads it to hook into the hardware audio pipeline.
 *
 * Audio session IDs are valid integers ≥ 0; 0 means "not yet assigned".
 */
object AudioSessionIdHolder {
    private val _audioSessionId = MutableStateFlow(0)

    /** Observed by Task B's visualizer composable to hook into the audio hardware session. */
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    /** Called by [MelodifyPlaybackService] right after ExoPlayer is built. */
    fun set(id: Int) {
        _audioSessionId.value = id
    }
}
