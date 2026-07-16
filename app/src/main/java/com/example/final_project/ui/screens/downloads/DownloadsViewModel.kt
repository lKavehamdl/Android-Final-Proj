package com.example.final_project.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for [DownloadsScreen].
 *
 * Phase 2 hook: Replace the empty stub with a Flow from the Room
 * [DownloadsRepository] once the database module is wired up.
 *
 * Until then, [uiState] exposes an empty list so the screen renders
 * the correct empty state.
 */
class DownloadsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    // Phase 2: inject DownloadsRepository here and collect from Room
    // init {
    //     viewModelScope.launch {
    //         repository.getDownloads().collect { list ->
    //             _uiState.update { it.copy(songs = list) }
    //         }
    //     }
    // }

    // ── Intents ───────────────────────────────────────────────────────────────

    fun onSortChanged(order: DownloadSortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    /**
     * Removes a download record from the list.
     * Phase 2: also deletes from Room DB and removes the local file via WorkManager.
     */
    fun onDelete(songId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(songs = state.songs.filter { it.id != songId })
            }
        }
    }
}
