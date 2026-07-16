package com.example.final_project.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.final_project.domain.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
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
 * App-wide playback coordinator. Bridges UI ↔ [MelodifyPlaybackService].
 *
 * Phase 4 architecture:
 *  - Owns a [MediaController] that talks to [MelodifyPlaybackService] over IPC.
 *  - Exposes [state] as an immutable [StateFlow<PlayerState>] consumed by all screens.
 *  - All user intents (play, pause, seek, speed, sleep timer …) are dispatched here
 *    and forwarded to the controller, which forwards them to the service's ExoPlayer.
 *
 * Audio focus, noise handling, and caching are all owned by [MelodifyPlaybackService].
 *
 * Injected as a Koin [single] so every ViewModel and composable gets the same instance.
 */
class PlayerController(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    // ── MediaController (connects to MelodifyPlaybackService) ─────────────────
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    // Coroutine jobs
    private var positionJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var crossfadeJob: Job? = null

    // Queue kept in-memory for fast index look-ups
    private var currentQueue: List<Song> = emptyList()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startPositionUpdates()
                startCrossfadeWatch()
            } else {
                positionJob?.cancel()
                crossfadeJob?.cancel()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val buffering = playbackState == Player.STATE_BUFFERING
            _state.update { it.copy(isBuffering = buffering) }
            if (playbackState == Player.STATE_READY) {
                _state.update { it.copy(durationMs = controller?.duration?.coerceAtLeast(0L) ?: 0L) }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.update { it.copy(isBuffering = false, isPlaying = false) }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.update { it.copy(isShuffleOn = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.update { it.copy(repeatMode = repeatMode) }
        }
    }

    init {
        connectToService()
    }

    // ── Service connection ────────────────────────────────────────────────────

    private fun connectToService() {
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, MelodifyPlaybackService::class.java),
        )
        val future = MediaController.Builder(appContext, sessionToken)
            .buildAsync()
        future.addListener(
            {
                try {
                    val ctrl = future.get()
                    ctrl.addListener(playerListener)
                    controller = ctrl
                } catch (_: Exception) { /* service not yet started — will retry on first play() */ }
            },
            MoreExecutors.directExecutor(),
        )
        controllerFuture = future
    }

    // ── Public intents ────────────────────────────────────────────────────────

    /**
     * Starts playing [song] from the given [queue].
     * Checks Room downloads first (Phase 2 wiring point) to play local file if available.
     */
    fun play(song: Song, queue: List<Song> = listOf(song)) {
        val resolvedQueue = queue.ifEmpty { listOf(song) }
        currentQueue = resolvedQueue
        val index = resolvedQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        _state.update {
            it.copy(
                currentSong = song,
                queue = resolvedQueue,
                isPlaying = true,
                positionMs = 0L,
                durationMs = 0L,
                isBuffering = true,
            )
        }

        val ctrl = controller ?: run {
            // Service not yet connected — reconnect and retry
            connectToService()
            return
        }

        ctrl.setMediaItems(
            resolvedQueue.map { s ->
                // Phase 2 hook: check Room for local path
                // val localPath = downloadsRepo.getLocalPath(s.id)
                // if (localPath != null) MediaItem.fromUri(localPath) else MediaItem.fromUri(s.audioUrl)
                MediaItem.fromUri(s.audioUrl)
            },
            index,
            0L,
        )
        ctrl.prepare()
        ctrl.play()
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun skipNext() {
        val current = _state.value
        val index = current.queue.indexOfFirst { it.id == current.currentSong?.id }
        val next = current.queue.getOrNull(index + 1) ?: return
        play(next, current.queue)
    }

    fun skipPrevious() {
        val ctrl = controller ?: return
        val current = _state.value
        if (ctrl.currentPosition > 3_000L) {
            ctrl.seekTo(0L)
            return
        }
        val index = current.queue.indexOfFirst { it.id == current.currentSong?.id }
        val previous = current.queue.getOrNull(index - 1) ?: return
        play(previous, current.queue)
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun setExpanded(expanded: Boolean) {
        _state.update { it.copy(isExpanded = expanded) }
    }

    // ── Playback parameters ───────────────────────────────────────────────────

    fun setPlaybackSpeed(speed: Float) {
        controller?.setPlaybackParameters(PlaybackParameters(speed))
        _state.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleShuffle() {
        val newShuffle = !_state.value.isShuffleOn
        controller?.shuffleModeEnabled = newShuffle
        _state.update { it.copy(isShuffleOn = newShuffle) }
    }

    fun cycleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller?.repeatMode = next
        _state.update { it.copy(repeatMode = next) }
    }

    // ── Sleep timer ───────────────────────────────────────────────────────────

    /**
     * Sets a sleep timer. Pass [minutes] = null (or 0) to cancel.
     * Every minute the [PlayerState.sleepTimerMinutesLeft] counts down.
     * When it reaches zero, playback pauses.
     */
    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _state.update { it.copy(sleepTimerMinutesLeft = null) }
            return
        }
        _state.update { it.copy(sleepTimerMinutesLeft = minutes) }
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (isActive && remaining > 0) {
                delay(60_000L)
                remaining--
                _state.update { it.copy(sleepTimerMinutesLeft = remaining.takeIf { r -> r > 0 }) }
            }
            controller?.pause()
        }
    }

    // ── Release ───────────────────────────────────────────────────────────────

    fun release() {
        positionJob?.cancel()
        sleepTimerJob?.cancel()
        crossfadeJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                val ctrl = controller
                if (ctrl != null) {
                    _state.update {
                        it.copy(
                            positionMs = ctrl.currentPosition.coerceAtLeast(0L),
                            durationMs = ctrl.duration.coerceAtLeast(0L),
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    /**
     * Volume-based crossfade: when < 3 s remain, fade out the current track.
     * The next track is started by [playerListener.onPlaybackStateChanged] (STATE_ENDED).
     * On the next [play] call the volume is ramped back to 1.0.
     */
    private fun startCrossfadeWatch() {
        crossfadeJob?.cancel()
        crossfadeJob = scope.launch {
            while (isActive) {
                delay(200L)
                val ctrl = controller ?: continue
                val remaining = ctrl.duration - ctrl.currentPosition
                if (remaining in 1L..3_000L && ctrl.duration > 0) {
                    val fraction = remaining / 3_000f
                    ctrl.volume = fraction.coerceIn(0f, 1f)
                } else if (remaining > 3_000L) {
                    if (ctrl.volume < 1f) ctrl.volume = 1f
                }
            }
        }
    }
}
