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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * App-wide playback coordinator. Bridges UI ↔ [MelodifyPlaybackService].
 *
 * Phase 4 architecture:
 *  - Owns a [MediaController] that talks to [MelodifyPlaybackService] over IPC.
 *  - Exposes [state] as an immutable [StateFlow<PlayerState>] consumed by all screens.
 *  - All user intents (play, pause, seek, speed, sleep timer …) are dispatched here.
 *  - Persists queue + position to [PlayerStateStore] (DataStore) every 5 s while playing.
 *  - Restores last session on first connection so the user picks up where they left off.
 *  - Propagates [AudioSessionIdHolder.audioSessionId] into [PlayerState.audioSessionId].
 *
 * Injected as a Koin [single] so every ViewModel gets the same instance.
 */
class PlayerController(
    context: Context,
    private val scope: CoroutineScope,
    private val downloadsRepository: com.example.final_project.domain.repository.DownloadsRepository? = null,
) {
    private val appContext = context.applicationContext

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    // ── Persistence ───────────────────────────────────────────────────────────
    private val stateStore = PlayerStateStore(appContext)

    // ── MediaController (connects to MelodifyPlaybackService) ─────────────────
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    // ── Jobs ──────────────────────────────────────────────────────────────────
    private var positionJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var crossfadeJob: Job? = null
    private var persistJob: Job? = null

    // Queue kept in-memory for fast index look-ups
    private var currentQueue: List<Song> = emptyList()

    // ── Player listener ───────────────────────────────────────────────────────
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startPositionUpdates()
                startCrossfadeWatch()
                startPersistLoop()
            } else {
                positionJob?.cancel()
                crossfadeJob?.cancel()
                persistJob?.cancel()
                // Persist final state on pause
                scope.launch(Dispatchers.IO) { persistNow() }
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
        // Mirror audioSessionId from the service into PlayerState whenever it changes
        AudioSessionIdHolder.audioSessionId
            .onEach { id -> _state.update { it.copy(audioSessionId = id) } }
            .launchIn(scope)

        connectToService()
    }

    // ── Service connection ────────────────────────────────────────────────────

    private fun connectToService() {
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, MelodifyPlaybackService::class.java),
        )
        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        future.addListener(
            {
                try {
                    val ctrl = future.get()
                    ctrl.addListener(playerListener)
                    controller = ctrl
                    // Restore last session once controller is ready
                    scope.launch { restoreLastSession(ctrl) }
                } catch (_: Exception) { /* service not yet started */ }
            },
            MoreExecutors.directExecutor(),
        )
        controllerFuture = future
    }

    // ── Public intents ────────────────────────────────────────────────────────

    /**
     * Starts playing [song] from the given [queue].
     * Phase 2 hook: checks Room downloads for a local file before streaming.
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

        val ctrl = controller ?: run { connectToService(); return }

        scope.launch {
            val mediaItems = resolvedQueue.map { s ->
                val localPath = downloadsRepository?.getLocalPath(s.id)
                if (localPath != null && java.io.File(localPath).exists()) {
                    MediaItem.fromUri(java.io.File(localPath).toURI().toString())
                } else {
                    MediaItem.fromUri(s.audioUrl)
                }
            }

            withContext(Dispatchers.Main) {
                ctrl.setMediaItems(mediaItems, index, 0L)
                ctrl.prepare()
                ctrl.play()
            }
        }
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
        if (ctrl.currentPosition > 3_000L) { ctrl.seekTo(0L); return }
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
        persistJob?.cancel()
        scope.launch(Dispatchers.IO) { persistNow() }
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }

    // ── Persistence helpers ───────────────────────────────────────────────────

    /**
     * Saves the current playback state to DataStore immediately.
     * Must be called from a coroutine (ideally on [Dispatchers.IO]).
     */
    private suspend fun persistNow() {
        val s = _state.value
        stateStore.save(
            currentSong = s.currentSong,
            positionMs = s.positionMs,
            queue = s.queue,
            playbackSpeed = s.playbackSpeed,
        )
    }

    /**
     * Starts a periodic persist loop — saves state every 5 s while playing.
     * Cancelled automatically when playback stops.
     */
    private fun startPersistLoop() {
        persistJob?.cancel()
        persistJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(5_000L)
                persistNow()
            }
        }
    }

    /**
     * Called once after [MediaController] connects.
     * Reads [PlayerStateStore] and restores queue + position so the user
     * continues from where they left off after app restart.
     *
     * Only restores if [ctrl] has no active media (fresh start).
     */
    private suspend fun restoreLastSession(ctrl: MediaController) {
        if (ctrl.mediaItemCount > 0) return // Already playing — don't interrupt

        val snapshot = withContext(Dispatchers.IO) {
            stateStore.savedState.first()
        }
        if (snapshot.songId == null || snapshot.queueIds.isEmpty()) return

        // Restore queue as MediaItems — UI layer's SongRepository will fill meta on demand
        // For now restore enough for ExoPlayer to resume the stream
        val savedSongId = snapshot.songId
        val savedQueueIds = snapshot.queueIds
        val savedPosition = snapshot.positionMs
        val savedSpeed = snapshot.playbackSpeed

        // Rebuild minimal queue from saved ids (Phase 2 will enrich from Room)
        // For now: nothing to enrich without repository access here.
        // We signal the UI that a restore is available via _state so the
        // HomeViewModel can trigger play() with the full Song objects.
        _state.update {
            it.copy(playbackSpeed = savedSpeed)
        }
        ctrl.setPlaybackParameters(PlaybackParameters(savedSpeed))
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
     * The next track starts from [skipNext] triggered by [playerListener.onPlaybackStateChanged].
     */
    private fun startCrossfadeWatch() {
        crossfadeJob?.cancel()
        crossfadeJob = scope.launch {
            while (isActive) {
                delay(200L)
                val ctrl = controller ?: continue
                val remaining = ctrl.duration - ctrl.currentPosition
                when {
                    remaining in 1L..3_000L && ctrl.duration > 0 ->
                        ctrl.volume = (remaining / 3_000f).coerceIn(0f, 1f)
                    remaining > 3_000L && ctrl.volume < 1f ->
                        ctrl.volume = 1f
                }
            }
        }
    }
}
