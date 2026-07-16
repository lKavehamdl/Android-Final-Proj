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
 * Live repository — talks to Supabase via [SongApiService].
 * Bound by Koin when [com.example.final_project.core.constants.AppConstants.USE_FAKE_SONGS] is false.
 */
class SongRepositoryImpl(
    private val api: SongApiService,
    private val dispatchers: DispatchersProvider,
) : SongRepository {

    override fun getSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        try {
            val songs = api.fetchSongs().toDomain()
            emit(Resource.Success(songs))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message, throwable = e))
        }
    }.flowOn(dispatchers.io)
}
