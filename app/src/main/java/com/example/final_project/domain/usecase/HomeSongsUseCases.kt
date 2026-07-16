package com.example.final_project.domain.usecase

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class GetTrendingSongsUseCase(
    private val songRepository: SongRepository,
    private val dispatchers: DispatchersProvider,
) {
    operator fun invoke(limit: Int = 20): Flow<Resource<List<Song>>> =
        songRepository.getTrendingSongs(limit).flowOn(dispatchers.io)
}

class GetNewReleasesUseCase(
    private val songRepository: SongRepository,
    private val dispatchers: DispatchersProvider,
) {
    operator fun invoke(limit: Int = 20): Flow<Resource<List<Song>>> =
        songRepository.getNewReleases(limit).flowOn(dispatchers.io)
}

class GetLocalSongsUseCase(
    private val songRepository: SongRepository,
    private val dispatchers: DispatchersProvider,
) {
    operator fun invoke(): Flow<Resource<List<Song>>> =
        songRepository.getLocalSongs().flowOn(dispatchers.io)
}
