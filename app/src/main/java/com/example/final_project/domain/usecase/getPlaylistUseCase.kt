package com.example.final_project.domain.usecase

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class GetPublicPlaylistsUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    operator fun invoke(): Flow<Resource<List<Playlist>>> =
        playlistRepository.getPublicPlaylists()
}

class GetUserPlaylistsUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    operator fun invoke(userId: String): Flow<Resource<List<Playlist>>> =
        playlistRepository.getUserPlaylists(userId)
}