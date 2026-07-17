package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.PlaylistRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

/** In-memory fake for previews/testing when USE_FAKE_SONGS = true. */
class FakePlaylistRepository(
    private val dispatchers: DispatchersProvider,
) : PlaylistRepository {

    private val fakePlaylists = mutableListOf(
        Playlist("1", null, "Top Global Hits", "The biggest tracks",
            "https://picsum.photos/seed/tophits/300", true, false, ""),
        Playlist("2", null, "Best of Iranian Music", "Golden era of Persian music",
            "https://picsum.photos/seed/iranianmusic/300", true, true, ""),
        Playlist("3", null, "Chill Vibes", "Relax and unwind",
            "https://picsum.photos/seed/chillvibes/300", true, false, ""),
        Playlist("4", null, "Rock Essentials", "Iconic rock anthems",
            "https://picsum.photos/seed/rockessentials/300", true, false, ""),
    )

    private val playlistSongs = mutableMapOf<String, MutableList<String>>()

    override fun getPublicPlaylists(): Flow<Resource<List<Playlist>>> = flow {
        emit(Resource.Loading)
        delay(600)
        emit(Resource.Success(fakePlaylists.filter { it.isPublic }))
    }.flowOn(dispatchers.io)

    override fun getUserPlaylists(userId: String): Flow<Resource<List<Playlist>>> = flow {
        emit(Resource.Loading)
        delay(400)
        emit(Resource.Success(fakePlaylists.filter { it.userId == userId }))
    }.flowOn(dispatchers.io)

    override fun getPlaylistSongs(playlistId: String): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        delay(400)
        emit(Resource.Success(emptyList()))
    }.flowOn(dispatchers.io)

    override suspend fun createPlaylist(
        userId: String,
        name: String,
        description: String?,
        isPublic: Boolean,
    ): Resource<Playlist> = withContext(dispatchers.io) {
        delay(300)
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return@withContext Resource.Error(message = "Playlist name is required")
        if (fakePlaylists.any { it.userId == userId && it.name.equals(trimmed, ignoreCase = true) }) {
            return@withContext Resource.Error(message = "A playlist with this name already exists")
        }
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = trimmed,
            description = description,
            coverImageUrl = null,
            isPublic = isPublic,
            isLocal = false,
            createdAt = "",
        )
        fakePlaylists.add(0, playlist)
        Resource.Success(playlist)
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String): Resource<Unit> =
        withContext(dispatchers.io) {
            delay(200)
            val songs = playlistSongs.getOrPut(playlistId) { mutableListOf() }
            if (songs.contains(songId)) {
                return@withContext Resource.Error(message = "Song is already in this playlist")
            }
            songs.add(songId)
            Resource.Success(Unit)
        }
}
