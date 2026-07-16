package com.example.final_project.player

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.final_project.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property — creates one DataStore per application
private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "player_state")

/**
 * Persists the minimum set of playback state to survive process death and
 * app close/reopen. Backed by Jetpack [DataStore].
 *
 * Saved fields:
 *  - [KEY_SONG_ID]     : id of the currently playing song
 *  - [KEY_POSITION_MS] : last known seek position in ms
 *  - [KEY_QUEUE_IDS]   : comma-separated song ids representing the current queue
 *
 * Usage:
 * ```
 * // save on every position tick (throttled externally):
 * store.save(currentSong, positionMs, queue)
 *
 * // restore on controller init:
 * store.savedState.collect { snapshot -> ... }
 * ```
 */
class PlayerStateStore(context: Context) {

    private val store = context.applicationContext.playerDataStore

    // ── Keys ──────────────────────────────────────────────────────────────────
    companion object {
        val KEY_SONG_ID = stringPreferencesKey("song_id")
        val KEY_POSITION_MS = longPreferencesKey("position_ms")
        val KEY_QUEUE_IDS = stringPreferencesKey("queue_ids")
        val KEY_SPEED = intPreferencesKey("playback_speed_x10") // stored as int × 10 to avoid float key
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    data class SavedPlayerSnapshot(
        val songId: String?,
        val positionMs: Long,
        val queueIds: List<String>,
        val playbackSpeed: Float,
    )

    val savedState: Flow<SavedPlayerSnapshot> = store.data.map { prefs ->
        SavedPlayerSnapshot(
            songId = prefs[KEY_SONG_ID],
            positionMs = prefs[KEY_POSITION_MS] ?: 0L,
            queueIds = prefs[KEY_QUEUE_IDS]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
            playbackSpeed = (prefs[KEY_SPEED] ?: 10) / 10f,
        )
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Persist the current state. Call this from a coroutine on [Dispatchers.IO].
     * [PlayerController] throttles calls to every 5 s to avoid excessive writes.
     */
    suspend fun save(
        currentSong: Song?,
        positionMs: Long,
        queue: List<Song>,
        playbackSpeed: Float,
    ) {
        store.edit { prefs ->
            if (currentSong != null) prefs[KEY_SONG_ID] = currentSong.id else prefs.remove(KEY_SONG_ID)
            prefs[KEY_POSITION_MS] = positionMs
            prefs[KEY_QUEUE_IDS] = queue.joinToString(",") { it.id }
            prefs[KEY_SPEED] = (playbackSpeed * 10).toInt()
        }
    }

    /** Called when the user explicitly stops playback or logs out. */
    suspend fun clear() {
        store.edit { it.clear() }
    }
}
