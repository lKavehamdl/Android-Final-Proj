package com.example.final_project.domain.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for all song-related data operations.
 *
 * Implementations:
 *  - [com.example.final_project.data.repository.FakeSongRepository]  (dev / offline)
 *  - [com.example.final_project.data.repository.SongRepositoryImpl]  (live Supabase)
 *
 * All functions run on [kotlinx.coroutines.Dispatchers.IO] (enforced by impls).
 * The UI layer depends on this interface only — never on a concrete class.
 */
interface SongRepository {

    /** All songs, alphabetically. Used by initial load. */
    fun getSongs(): Flow<Resource<List<Song>>>

    /** Trending songs ordered by play_count DESC. */
    fun getTrendingSongs(limit: Int = 20): Flow<Resource<List<Song>>>

    /** Newest songs ordered by created_at DESC. */
    fun getNewReleases(limit: Int = 20): Flow<Resource<List<Song>>>

    /** Iranian / local music (is_local = true). */
    fun getLocalSongs(): Flow<Resource<List<Song>>>

    /**
     * Live search. Debounce (300 ms) is applied in the ViewModel via Flow operators;
     * this function just executes the network call.
     */
    fun searchSongs(query: String): Flow<Resource<List<Song>>>

    /** Fetch a single song by its UUID. */
    suspend fun getSongById(id: String): Resource<Song>

    /** Increment server-side play_count. Fire-and-forget — errors are swallowed. */
    suspend fun incrementPlayCount(songId: String)
}
