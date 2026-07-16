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

    companion object {
        private const val FAKE_NETWORK_DELAY_MS = 400L

        val FAKE_SONGS: List<Song> = listOf(
            Song(
                id = "fake-1",
                title = "Midnight Drive",
                artistName = "SoundHelix",
                coverImageUrl = "https://picsum.photos/seed/melodify1/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            ),
            Song(
                id = "fake-2",
                title = "Ocean Waves",
                artistName = "SoundHelix",
                coverImageUrl = "https://picsum.photos/seed/melodify2/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            ),
            Song(
                id = "fake-3",
                title = "Neon Lights",
                artistName = "SoundHelix",
                coverImageUrl = "https://picsum.photos/seed/melodify3/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            ),
            Song(
                id = "fake-4",
                title = "Forest Path",
                artistName = "SoundHelix",
                coverImageUrl = "https://picsum.photos/seed/melodify4/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            ),
            Song(
                id = "fake-5",
                title = "City Rain",
                artistName = "SoundHelix",
                coverImageUrl = "https://picsum.photos/seed/melodify5/400/400",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            ),
        )
    }
}
