package com.example.final_project.domain.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    /** All public/global playlists (world + local). */
    fun getPublicPlaylists(): Flow<Resource<List<Playlist>>>

    /** Playlists owned by a specific user. */
    fun getUserPlaylists(userId: String): Flow<Resource<List<Playlist>>>

    /** Get songs for a specific playlist. */
    fun getPlaylistSongs(playlistId: String): Flow<Resource<List<Song>>>

    /** Create a new playlist owned by [userId]. */
    suspend fun createPlaylist(
        userId: String,
        name: String,
        description: String? = null,
        isPublic: Boolean = false,
    ): Resource<Playlist>

    /** Add [songId] to [playlistId]. Returns error if duplicate. */
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Resource<Unit>
}
