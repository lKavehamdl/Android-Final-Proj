package com.example.final_project.domain.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for song data.
 *
 * Implementations:
 * - [com.example.final_project.data.repository.FakeSongRepository] (default, Phase 1)
 * - [com.example.final_project.data.repository.SongRepositoryImpl] (live Supabase)
 */
interface SongRepository {
    fun getSongs(): Flow<Resource<List<Song>>>
}
