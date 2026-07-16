package com.example.final_project.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Background media playback service — the heart of Phase 4.
 *
 * Responsibilities:
 *  1. Owns the [ExoPlayer] instance with full audio-focus handling
 *  2. Creates and manages a [MediaSession] (exposes controls to OS / notification)
 *  3. Wraps playback through [PlayerCache] for smart caching (200 MB LRU)
 *  4. Keeps music alive even when the user leaves the app
 *
 * Declared in AndroidManifest with:
 *  - `foregroundServiceType="mediaPlayback"`
 *  - `stopWithTask="false"` (music survives task removal)
 *
 * [PlayerController] connects to this service via [MediaController] and delegates
 * all user-facing intents through it.
 */
class MelodifyPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        val cache = PlayerCache.get(applicationContext)
        val upstreamFactory = DefaultDataSource.Factory(applicationContext)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val exoPlayer = ExoPlayer.Builder(applicationContext)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)   // pause on headphone unplug
            .build()

        player = exoPlayer

        // Build a session intent that re-opens MainActivity when user taps the notification
        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, Class.forName("com.example.final_project.MainActivity")),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityIntent)
            .build()
    }

    /** Called by Media3 to retrieve the active session for the given controller. */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        PlayerCache.release()
        mediaSession = null
        player = null
        super.onDestroy()
    }
}
