package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Offline-friendly fake data so Task C can finish without waiting for Task A/B.
 * Swap to [SongRepositoryImpl] by setting `USE_FAKE_SONGS=false` in app/build.gradle.kts.
 *
 * Audio samples: SoundHelix (royalty-free demos).
 * Covers: picsum.photos seeded images.
 */
class FakeSongRepository(
    private val dispatchers: DispatchersProvider,
) : SongRepository {

    override fun getSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        // Simulate network latency so Loading state is visible in UI.
        delay(FAKE_NETWORK_DELAY_MS)
        emit(Resource.Success(FAKE_SONGS))
    }.flowOn(dispatchers.io)

    override fun getTrendingSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        delay(FAKE_NETWORK_DELAY_MS)
        emit(Resource.Success(FAKE_SONGS.take(limit)))
    }.flowOn(dispatchers.io)

    override fun getNewReleases(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        delay(FAKE_NETWORK_DELAY_MS)
        emit(Resource.Success(FAKE_SONGS.take(limit)))
    }.flowOn(dispatchers.io)

    override fun getLocalSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        delay(FAKE_NETWORK_DELAY_MS)
        emit(Resource.Success(FAKE_SONGS.filter { it.isLocal }))
    }.flowOn(dispatchers.io)

    override fun searchSongs(query: String): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        delay(FAKE_NETWORK_DELAY_MS)
        val lowerQuery = query.lowercase()
        val filtered = FAKE_SONGS.filter {
            it.title.lowercase().contains(lowerQuery) || it.artistName.lowercase().contains(lowerQuery)
        }
        emit(Resource.Success(filtered))
    }.flowOn(dispatchers.io)

    override suspend fun getSongById(id: String): Resource<Song> {
        delay(FAKE_NETWORK_DELAY_MS)
        val song = FAKE_SONGS.find { it.id == id }
        return if (song != null) Resource.Success(song) else Resource.Error(message = "Song not found")
    }

    override suspend fun getRecommendations(seedSong: Song, limit: Int): Resource<List<Song>> {
        delay(FAKE_NETWORK_DELAY_MS)
        val related = FAKE_SONGS.filter {
            it.id != seedSong.id && (it.genre == seedSong.genre || it.artistName == seedSong.artistName)
        }
        val recs = (related + FAKE_SONGS.filter { it.id != seedSong.id })
            .distinctBy { it.id }
            .take(limit)
        return Resource.Success(recs)
    }

    override suspend fun incrementPlayCount(songId: String) {
        delay(100)
    }

    override suspend fun recordRecentlyPlayed(songId: String) {
        delay(50)
        recentIds.remove(songId)
        recentIds.add(0, songId)
        if (recentIds.size > 20) recentIds.removeAt(recentIds.lastIndex)
    }

    override suspend fun getLikedSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        delay(FAKE_NETWORK_DELAY_MS)
        emit(Resource.Success(FAKE_SONGS.filter { it.id in likedIds }))
    }.flowOn(dispatchers.io)

    override suspend fun toggleLike(songId: String): Resource<Boolean> {
        delay(FAKE_NETWORK_DELAY_MS)
        val nowLiked = if (songId in likedIds) {
            likedIds.remove(songId)
            false
        } else {
            likedIds.add(songId)
            true
        }
        return Resource.Success(nowLiked)
    }

    override suspend fun getRecentlyPlayed(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        delay(FAKE_NETWORK_DELAY_MS)
        val songs = recentIds.mapNotNull { id -> FAKE_SONGS.find { it.id == id } }
        emit(Resource.Success(songs))
    }.flowOn(dispatchers.io)

    override suspend fun removeRecentlyPlayed(songId: String): Resource<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        recentIds.remove(songId)
        return Resource.Success(Unit)
    }

    companion object {
        private const val FAKE_NETWORK_DELAY_MS = 400L

        private val likedIds = mutableSetOf("fake-1", "fake-2")
        private val recentIds = mutableListOf("fake-5", "fake-4", "fake-3")

        val FAKE_SONGS: List<Song> = listOf(
            Song(
                id = "fake-1",
                title = "Midnight Drive",
                artistName = "SoundHelix",
                albumName = "Demos",
                coverImageUrl = "https://picsum.photos/seed/melodify1/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                durationSeconds = 240,
                genre = "Electronic",
                isLocal = false,
                playCount = 1000L,
                createdAt = "2024-05-01T00:00:00Z"
            ),
            Song(
                id = "fake-2",
                title = "Ocean Waves",
                artistName = "SoundHelix",
                albumName = "Demos",
                coverImageUrl = "https://picsum.photos/seed/melodify2/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                durationSeconds = 180,
                genre = "Ambient",
                isLocal = false,
                playCount = 2000L,
                createdAt = "2024-05-02T00:00:00Z"
            ),
            Song(
                id = "fake-3",
                title = "Neon Lights",
                artistName = "SoundHelix",
                albumName = "Demos",
                coverImageUrl = "https://picsum.photos/seed/melodify3/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                durationSeconds = 210,
                genre = "Pop",
                isLocal = false,
                playCount = 3000L,
                createdAt = "2024-05-03T00:00:00Z"
            ),
            Song(
                id = "fake-4",
                title = "Forest Path",
                artistName = "SoundHelix",
                albumName = "Demos",
                coverImageUrl = "https://picsum.photos/seed/melodify4/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                durationSeconds = 260,
                genre = "Folk",
                isLocal = true,
                playCount = 4000L,
                createdAt = "2024-05-04T00:00:00Z"
            ),
            Song(
                id = "fake-5",
                title = "City Rain",
                artistName = "SoundHelix",
                albumName = "Demos",
                coverImageUrl = "https://picsum.photos/seed/melodify5/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                durationSeconds = 200,
                genre = "Lo-Fi",
                isLocal = true,
                playCount = 5000L,
                createdAt = "2024-05-05T00:00:00Z"
            ),
        )
    }
}
