package com.example.final_project.domain.favorites

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Single source of truth for liked-song state across the app.
 * [LikeButton], Liked Songs screen, and Now Playing all observe the same flows.
 */
class FavoritesStore(
    private val songRepository: SongRepository,
    private val dispatchers: DispatchersProvider,
    private val appScope: CoroutineScope,
) {
    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())
    val likedIds: StateFlow<Set<String>> = _likedIds.asStateFlow()

    private val _likedSongs = MutableStateFlow<Resource<List<Song>>>(Resource.Loading)
    val likedSongs: StateFlow<Resource<List<Song>>> = _likedSongs.asStateFlow()

    init {
        appScope.launch { refresh() }
    }

    fun isLiked(songId: String): Boolean = songId in _likedIds.value

    suspend fun refresh() {
        withContext(dispatchers.io) {
            _likedSongs.value = Resource.Loading
            when (val result = songRepository.getLikedSongs().first { it !is Resource.Loading }) {
                is Resource.Success -> {
                    _likedSongs.value = result
                    _likedIds.value = result.data.map { it.id }.toSet()
                }
                is Resource.Error -> {
                    _likedSongs.value = result
                }
                Resource.Loading -> Unit
            }
        }
    }

    suspend fun toggle(songId: String): Resource<Boolean> {
        val wasLiked = songId in _likedIds.value
        val optimistic = !wasLiked
        _likedIds.update { ids ->
            if (optimistic) ids + songId else ids - songId
        }
        if (!optimistic) {
            _likedSongs.update { current ->
                if (current is Resource.Success) {
                    Resource.Success(current.data.filterNot { it.id == songId })
                } else current
            }
        }

        return when (val result = withContext(dispatchers.io) { songRepository.toggleLike(songId) }) {
            is Resource.Success -> {
                val nowLiked = result.data
                _likedIds.update { ids ->
                    if (nowLiked) ids + songId else ids - songId
                }
                appScope.launch { refresh() }
                Resource.Success(nowLiked)
            }
            is Resource.Error -> {
                _likedIds.update { ids ->
                    if (wasLiked) ids + songId else ids - songId
                }
                appScope.launch { refresh() }
                result
            }
            Resource.Loading -> Resource.Error(message = "Unexpected loading state")
        }
    }
}
