package com.example.final_project.data.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Placeholder implementation — returns empty list until Phase 1 backend is wired.
 */
class SongRepositoryImpl : SongRepository {
    override fun getSongs(): Flow<Resource<List<Song>>> =
        flowOf(Resource.Success(emptyList()))
}
