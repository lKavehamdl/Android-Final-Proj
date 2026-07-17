package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.data.mapper.toDomain
import com.example.final_project.data.remote.PlaylistApiService
import com.example.final_project.data.remote.dto.PlaylistDto
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.PlaylistRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Live Supabase-backed implementation of [PlaylistRepository].
 * Every Flow switched to Dispatchers.IO via flowOn — no Main-thread network calls.
 */
class PlaylistRepositoryImpl(
    private val api: PlaylistApiService,
    private val dispatchers: DispatchersProvider,
    private val supabase: SupabaseClient,
) : PlaylistRepository {

    // Only block() goes in the try: emit() itself throws AbortFlowException
    // when a collector (e.g. .first{}) finishes early, and catching that and
    // emitting again violates flow exception transparency (fatal crash).
    private fun <T> safeFlow(block: suspend () -> T): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        val result = try {
            Resource.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(message = e.message, throwable = e)
        }
        emit(result)
    }

    override fun getPublicPlaylists(): Flow<Resource<List<Playlist>>> =
        safeFlow { api.fetchPublicPlaylists().toDomain() }
            .flowOn(dispatchers.io)

    override fun getUserPlaylists(userId: String): Flow<Resource<List<Playlist>>> =
        safeFlow { api.fetchUserPlaylists(userId).toDomain() }
            .flowOn(dispatchers.io)

    override fun getPlaylistSongs(playlistId: String): Flow<Resource<List<Song>>> =
        safeFlow { api.fetchPlaylistSongs(playlistId).map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun createPlaylist(
        userId: String,
        name: String,
        description: String?,
        isPublic: Boolean,
    ): Resource<Playlist> = withContext(dispatchers.io) {
        try {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                return@withContext Resource.Error(message = "Playlist name is required")
            }
            val existing = api.fetchUserPlaylists(userId)
            if (existing.any { it.name.equals(trimmed, ignoreCase = true) }) {
                return@withContext Resource.Error(message = "A playlist with this name already exists")
            }
            val created = supabase.postgrest["playlists"]
                .insert(
                    PlaylistInsertDto(
                        userId = userId,
                        name = trimmed,
                        description = description,
                        isPublic = isPublic,
                    ),
                ) {
                    select()
                }
                .decodeSingle<PlaylistDto>()
            Resource.Success(created.toDomain())
        } catch (e: Exception) {
            Resource.Error(message = e.message, throwable = e)
        }
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String): Resource<Unit> =
        withContext(dispatchers.io) {
            try {
                val existing = supabase.postgrest["playlist_songs"]
                    .select {
                        filter {
                            eq("playlist_id", playlistId)
                            eq("song_id", songId)
                        }
                    }
                    .decodeList<PlaylistSongRowDto>()
                if (existing.isNotEmpty()) {
                    return@withContext Resource.Error(message = "Song is already in this playlist")
                }
                val maxPos = supabase.postgrest["playlist_songs"]
                    .select {
                        filter { eq("playlist_id", playlistId) }
                    }
                    .decodeList<PlaylistSongRowDto>()
                    .maxOfOrNull { it.position } ?: -1
                supabase.postgrest["playlist_songs"].insert(
                    PlaylistSongInsertDto(
                        playlistId = playlistId,
                        songId = songId,
                        position = maxPos + 1,
                    ),
                )
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(message = e.message, throwable = e)
            }
        }
}

@Serializable
private data class PlaylistInsertDto(
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String? = null,
    @SerialName("is_public") val isPublic: Boolean = false,
)

@Serializable
private data class PlaylistSongInsertDto(
    @SerialName("playlist_id") val playlistId: String,
    @SerialName("song_id") val songId: String,
    val position: Int,
)

@Serializable
private data class PlaylistSongRowDto(
    @SerialName("playlist_id") val playlistId: String = "",
    @SerialName("song_id") val songId: String = "",
    val position: Int = 0,
)
