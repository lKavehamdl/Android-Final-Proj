package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.data.mapper.toDomain
import com.example.final_project.data.remote.SongApiService
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Live Supabase-backed implementation of [SongRepository].
 *
 * Bound by Koin when [com.example.final_project.core.constants.AppConstants.USE_FAKE_SONGS]
 * is **false** (set in build.gradle.kts once Task A keys are available).
 *
 * Thread safety: every Flow is switched to [DispatchersProvider.io] via [flowOn],
 * so the network call never touches the Main thread.
 */
class SongRepositoryImpl(
    private val api: SongApiService,
    private val dispatchers: DispatchersProvider,
) : SongRepository {

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Wraps a suspend [block] in Loading → Success | Error emissions. */
    private fun <T> safeFlow(block: suspend () -> T): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        try {
            emit(Resource.Success(block()))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message, throwable = e))
        }
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
}
