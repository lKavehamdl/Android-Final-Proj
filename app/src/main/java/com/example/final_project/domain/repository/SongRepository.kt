package com.example.final_project.domain.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for song data.
 * Implementation lives in [com.example.final_project.data.repository] (Phase 1).
 */
interface SongRepository {
    fun getSongs(): Flow<Resource<List<Song>>>
}
