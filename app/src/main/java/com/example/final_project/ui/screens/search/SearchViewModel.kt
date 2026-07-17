package com.example.final_project.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.local.dao.SearchHistoryDao
import com.example.final_project.data.local.entity.SearchHistoryEntity
import com.example.final_project.data.paging.SongSearchPagingSource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
import com.example.final_project.player.PlayerController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val filter: SongSearchPagingSource.SearchFilter = SongSearchPagingSource.SearchFilter.ALL,
    val history: List<String> = emptyList(),
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val songRepository: SongRepository,
    private val searchHistoryDao: SearchHistoryDao,
    private val playerController: PlayerController,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow(SongSearchPagingSource.SearchFilter.ALL)

    private val historyFlow = searchHistoryDao.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<SearchUiState> = combine(_query, _filter, historyFlow) { q, f, history ->
        SearchUiState(
            query = q,
            filter = f,
            history = history.map { it.term },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    val songs: Flow<PagingData<Song>> = combine(
        _query.debounce(AppConstants.SEARCH_DEBOUNCE_MS).distinctUntilChanged(),
        _filter,
    ) { q, f -> q to f }
        .flatMapLatest { (q, f) ->
            Pager(
                config = PagingConfig(
                    pageSize = AppConstants.DEFAULT_PAGE_SIZE,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    SongSearchPagingSource(
                        songRepository = songRepository,
                        query = q,
                        filter = f,
                        pageSize = AppConstants.DEFAULT_PAGE_SIZE,
                    )
                },
            ).flow
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onFilterSelected(filter: SongSearchPagingSource.SearchFilter) {
        _filter.value = filter
    }

    fun onHistoryClick(query: String) {
        _query.value = query
    }

    fun submitSearch() {
        val q = _query.value.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            searchHistoryDao.upsert(
                SearchHistoryEntity(term = q, searchedAt = System.currentTimeMillis()),
            )
        }
    }

    fun deleteHistory(query: String) {
        viewModelScope.launch {
            searchHistoryDao.delete(
                SearchHistoryEntity(term = query, searchedAt = 0L),
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch { searchHistoryDao.clearAll() }
    }

    fun playSong(song: Song) {
        playerController.play(song)
        submitSearch()
    }
}
