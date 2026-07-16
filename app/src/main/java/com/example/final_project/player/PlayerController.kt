package com.example.final_project.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.final_project.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * App-wide playback controller (Koin single).
 * Step 2: in-app play/pause/next + UI state.
 * Phase 4: MediaSession service, cache, audio focus, crossfade.
 */
class PlayerController(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val player: ExoPlayer = ExoPlayer.Builder(appContext).build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var positionJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startPositionUpdates() else positionJob?.cancel()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _state.update { it.copy(durationMs = player.duration.coerceAtLeast(0L)) }
            }
            if (playbackState == Player.STATE_ENDED) {
                skipNext()
            }
        }
    }

    init {
        player.addListener(listener)
    }

    fun play(song: Song, queue: List<Song> = listOf(song)) {
        val resolvedQueue = queue.ifEmpty { listOf(song) }
        val index = resolvedQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _state.update {
            it.copy(
                currentSong = song,
                queue = resolvedQueue,
                isPlaying = true,
                positionMs = 0L,
                durationMs = 0L,
            )
        }
        player.setMediaItems(
            resolvedQueue.map { MediaItem.fromUri(it.audioUrl) },
            index,
            0L,
        )
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipNext() {
        val current = _state.value
        if (current.queue.isEmpty()) return
        val index = current.queue.indexOfFirst { it.id == current.currentSong?.id }
        val next = current.queue.getOrNull(index + 1) ?: return
        play(next, current.queue)
    }

    fun skipPrevious() {
        val current = _state.value
        if (current.queue.isEmpty()) return
        if (player.currentPosition > 3_000L) {
            player.seekTo(0L)
            return
        }
        val index = current.queue.indexOfFirst { it.id == current.currentSong?.id }
        val previous = current.queue.getOrNull(index - 1) ?: return
        play(previous, current.queue)
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun setExpanded(expanded: Boolean) {
        _state.update { it.copy(isExpanded = expanded) }
    }

    fun release() {
        positionJob?.cancel()
        player.removeListener(listener)
        player.release()
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                _state.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L),
                    )
                }
                delay(500L)
            }
        }
    }
}
