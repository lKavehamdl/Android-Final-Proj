package com.example.final_project.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.flow.first

/**
 * Pages search / song results in [pageSize] chunks for Paging 3.
 * Fetches the full filtered set once per query refresh, then slices locally
 * (Supabase search RPC returns a flat list).
 */
class SongSearchPagingSource(
    private val songRepository: SongRepository,
    private val query: String,
    private val filter: SearchFilter,
    private val pageSize: Int,
) : PagingSource<Int, Song>() {

    enum class SearchFilter { ALL, SONGS, ARTISTS, ALBUMS }

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        return try {
            val page = params.key ?: 0
            val all = fetchFiltered()
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

    private suspend fun fetchFiltered(): List<Song> {
        val resource = if (query.isBlank()) {
            songRepository.getSongs().first { it !is Resource.Loading }
        } else {
            songRepository.searchSongs(query).first { it !is Resource.Loading }
        }
        val songs = (resource as? Resource.Success)?.data.orEmpty()
        val q = query.trim().lowercase()
        return when (filter) {
            SearchFilter.ALL, SearchFilter.SONGS -> songs
            SearchFilter.ARTISTS -> {
                if (q.isEmpty()) songs
                else songs.filter { it.artistName.lowercase().contains(q) }
            }
            SearchFilter.ALBUMS -> {
                if (q.isEmpty()) songs
                else songs.filter { it.albumName?.lowercase()?.contains(q) == true }
            }
        }
    }
}
