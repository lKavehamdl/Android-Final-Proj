package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.repository.PlaylistRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** In-memory fake for previews/testing when USE_FAKE_SONGS = true. */
class FakePlaylistRepository(
    private val dispatchers: DispatchersProvider,
) : PlaylistRepository {

    private val fakePlaylists = listOf(
        Playlist("1", null, "Top Global Hits", "The biggest tracks",
            "https://picsum.photos/seed/tophits/300", true, false, ""),
        Playlist("2", null, "Best of Iranian Music", "Golden era of Persian music",
            "https://picsum.photos/seed/iranianmusic/300", true, true, ""),
        Playlist("3", null, "Chill Vibes", "Relax and unwind",
            "https://picsum.photos/seed/chillvibes/300", true, false, ""),
        Playlist("4", null, "Rock Essentials", "Iconic rock anthems",
            "https://picsum.photos/seed/rockessentials/300", true, false, ""),
    )

    override fun getPublicPlaylists(): Flow<Resource<List<Playlist>>> = flow {
        emit(Resource.Loading)
        delay(600)
        emit(Resource.Success(fakePlaylists))
    }.flowOn(dispatchers.io)

    override fun getUserPlaylists(userId: String): Flow<Resource<List<Playlist>>> = flow {
        emit(Resource.Loading)
        delay(400)
        emit(Resource.Success(emptyList<Playlist>()))
    }.flowOn(dispatchers.io)
}