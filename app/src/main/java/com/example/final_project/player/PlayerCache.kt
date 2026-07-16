package com.example.final_project.player

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Application-wide ExoPlayer [SimpleCache] singleton.
 *
 * Stores up to 200 MB of audio data in `context.cacheDir/exo_cache`.
 * Least-Recently-Used eviction is used to stay within the quota.
 *
 * Must be released by calling [release] when the app terminates.
 * In practice this is handled by [MelodifyPlaybackService.onDestroy].
 */
object PlayerCache {

    private const val MAX_BYTES = 200L * 1024 * 1024 // 200 MB

    @Volatile
    private var instance: SimpleCache? = null

    fun get(context: Context): SimpleCache {
        return instance ?: synchronized(this) {
            instance ?: buildCache(context).also { instance = it }
        }
    }

    fun release() {
        synchronized(this) {
            instance?.release()
            instance = null
        }
    }

    private fun buildCache(context: Context): SimpleCache {
        val cacheDir = File(context.cacheDir, "exo_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(MAX_BYTES)
        val databaseProvider = StandaloneDatabaseProvider(context)
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }
}
