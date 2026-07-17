package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.data.mapper.toDomain
import com.example.final_project.data.remote.SongApiService
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Live Supabase-backed implementation of [SongRepository].
 *
 * Bound by Koin when [com.example.final_project.core.constants.AppConstants.USE_FAKE_SONGS]
 * is **false** (set in build.gradle.kts once Task A keys are available).
 *
 * so the network call never touches the Main thread.
 */
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.final_project.data.remote.dto.SongDto

@Serializable
private data class RecentlyPlayedInsertDto(
    @SerialName("user_id") val userId: String,
    @SerialName("song_id") val songId: String,
)

class SongRepositoryImpl(
    private val api: SongApiService,
    private val dispatchers: DispatchersProvider,
    private val supabase: SupabaseClient
) : SongRepository {

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Wraps a suspend [block] in Loading → Success | Error emissions. */
    private fun <T> safeFlow(block: suspend () -> T): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        // Only block() goes in the try: emit() itself throws AbortFlowException
        // when a collector (e.g. .first{}) finishes early, and catching that and
        // emitting again violates flow exception transparency (fatal crash).
        val result = try {
            Resource.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(message = e.message, throwable = e)
        }
        emit(result)
    }

    // ── SongRepository ────────────────────────────────────────────────────────

    override fun getSongs(): Flow<Resource<List<Song>>> =
        safeFlow { api.fetchSongs().toDomain() }
            .flowOn(dispatchers.io)

    override fun getTrendingSongs(limit: Int): Flow<Resource<List<Song>>> =
        safeFlow { api.fetchTrendingSongs(limit).toDomain() }
            .flowOn(dispatchers.io)

    override fun getNewReleases(limit: Int): Flow<Resource<List<Song>>> =
        safeFlow { api.fetchNewReleases(limit).toDomain() }
            .flowOn(dispatchers.io)

    override fun getLocalSongs(): Flow<Resource<List<Song>>> =
        safeFlow { api.fetchLocalSongs().toDomain() }
            .flowOn(dispatchers.io)

    override fun searchSongs(query: String): Flow<Resource<List<Song>>> =
        safeFlow { api.searchSongs(query).toDomain() }
            .flowOn(dispatchers.io)

    override suspend fun getSongById(id: String): Resource<Song> {
        return try {
            Resource.Success(api.fetchSongById(id).toDomain())
        } catch (e: Exception) {
            Resource.Error(message = e.message, throwable = e)
        }
    }

    override suspend fun incrementPlayCount(songId: String) {
        // Fire-and-forget: swallow exceptions so playback is never interrupted.
        runCatching { api.incrementPlayCount(songId) }
    }

    override suspend fun recordRecentlyPlayed(songId: String) {
        runCatching {
            val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return
            supabase.postgrest["recently_played"].insert(
                RecentlyPlayedInsertDto(userId = currentUserId, songId = songId),
            )
        }
    }

    override suspend fun getLikedSongs(): Flow<Resource<List<Song>>> = safeFlow {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
        val likedSongs = supabase.postgrest["liked_songs"]
            .select { filter { eq("user_id", currentUserId) } }
            .decodeList<Map<String, String>>()
            
        if (likedSongs.isEmpty()) return@safeFlow emptyList<Song>()
        
        val songIds = likedSongs.mapNotNull { it["song_id"] }
        val songsDto = supabase.postgrest["songs"]
            .select { filter { isIn("id", songIds) } }
            .decodeList<SongDto>()
            
        songsDto.toDomain()
    }.flowOn(dispatchers.io)

    override suspend fun toggleLike(songId: String): Resource<Boolean> = try {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
        val result = supabase.postgrest.rpc(
            "toggle_like",
            buildJsonObject {
                put("p_user_id", currentUserId)
                put("p_song_id", songId)
            },
        )
        val raw = result.data.trim().removeSurrounding("\"")
        val liked = raw.equals("true", ignoreCase = true)
        Resource.Success(liked)
    } catch (e: Exception) {
        Resource.Error(e.message, e)
    }

    override suspend fun getRecentlyPlayed(): Flow<Resource<List<Song>>> = safeFlow {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
        val recentRows = supabase.postgrest["recently_played"]
            .select { 
                filter { eq("user_id", currentUserId) }
                order("played_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<Map<String, String>>()
            
        if (recentRows.isEmpty()) return@safeFlow emptyList<Song>()
        
        val songIds = recentRows.mapNotNull { it["song_id"] }
        val songsDto = supabase.postgrest["songs"]
            .select { filter { isIn("id", songIds) } }
            .decodeList<SongDto>()
            
        // Maintain the recent order
        val songMap = songsDto.toDomain().associateBy { it.id }
        songIds.mapNotNull { songMap[it] }.distinct()
    }.flowOn(dispatchers.io)

    override suspend fun removeRecentlyPlayed(songId: String): Resource<Unit> = try {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
        supabase.postgrest["recently_played"].delete {
            filter {
                eq("user_id", currentUserId)
                eq("song_id", songId)
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message, e)
    }
}
