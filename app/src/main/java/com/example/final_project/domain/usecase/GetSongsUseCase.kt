package com.example.final_project.domain.usecase

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * Example UseCase skeleton — team members follow this pattern for business logic.
 */
class GetSongsUseCase(
    private val songRepository: SongRepository,
    private val dispatchers: DispatchersProvider,
) {
    operator fun invoke(): Flow<Resource<List<Song>>> =
        songRepository.getSongs().flowOn(dispatchers.io)
}
