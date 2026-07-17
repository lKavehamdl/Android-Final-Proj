package com.example.final_project.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.first

/**
 * Pages playlist tracks for Paging 3 (spec §3 — long playlist song lists).
 * Loads the playlist once per refresh, then slices by page.
 */
class PlaylistSongsPagingSource(
    private val playlistRepository: PlaylistRepository,
    private val playlistId: String,
    private val pageSize: Int,
) : PagingSource<Int, Song>() {

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        return try {
            val page = params.key ?: 0
            val resource = playlistRepository.getPlaylistSongs(playlistId)
                .first { it !is Resource.Loading }
            if (resource is Resource.Error) {
                return LoadResult.Error(resource.throwable ?: Exception(resource.message))
            }
            val all = (resource as? Resource.Success)?.data.orEmpty()
            val from = page * pageSize
            if (from >= all.size && page > 0) {
                return LoadResult.Page(emptyList(), prevKey = page - 1, nextKey = null)
            }
            val to = (from + pageSize).coerceAtMost(all.size)
            val slice = if (from < all.size) all.subList(from, to) else emptyList()
            LoadResult.Page(
                data = slice,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (to < all.size) page + 1 else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
