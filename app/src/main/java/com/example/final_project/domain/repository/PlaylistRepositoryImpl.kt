package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.data.mapper.toDomain
import com.example.final_project.data.remote.PlaylistApiService
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Live Supabase-backed implementation of [PlaylistRepository].
 * Every Flow switched to Dispatchers.IO via flowOn — no Main-thread network calls.
 */
class PlaylistRepositoryImpl(
    private val api: PlaylistApiService,
    private val dispatchers: DispatchersProvider,
) : PlaylistRepository {

    private fun <T> safeFlow(block: suspend () -> T): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        try {
            emit(Resource.Success(block()))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message, throwable = e))
        }
    }

    override fun getPublicPlaylists(): Flow<Resource<List<Playlist>>> =
        safeFlow { api.fetchPublicPlaylists().toDomain() }
            .flowOn(dispatchers.io)

    override fun getUserPlaylists(userId: String): Flow<Resource<List<Playlist>>> =
        safeFlow { api.fetchUserPlaylists(userId).toDomain() }
            .flowOn(dispatchers.io)
}