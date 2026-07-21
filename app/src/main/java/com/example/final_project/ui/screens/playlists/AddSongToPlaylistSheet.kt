package com.example.final_project.ui.screens.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.final_project.R
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.PlaylistRepository
import com.example.final_project.domain.repository.SongRepository
import com.example.final_project.ui.components.SongRow
import com.example.final_project.ui.theme.MelodifySpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Bottom sheet to search for a song and add it into [playlistId] — the reverse flow of [com.example.final_project.ui.player.AddToPlaylistSheet]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongToPlaylistSheet(
    playlistId: String,
    onDismiss: () -> Unit,
    onSongAdded: () -> Unit,
    songRepository: SongRepository = koinInject(),
    playlistRepository: PlaylistRepository = koinInject(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(300)
        when (val result = songRepository.searchSongs(query).first { it !is Resource.Loading }) {
            is Resource.Success -> results = result.data
            else -> results = emptyList()
        }
        isSearching = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MelodifySpacing.md)
                .padding(bottom = MelodifySpacing.xl),
        ) {
            Text(
                text = stringResource(R.string.playlist_add_song_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MelodifySpacing.md),
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = MelodifySpacing.sm),
                )
            }

            Spacer(modifier = Modifier.height(MelodifySpacing.sm))

            when {
                isSearching -> CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MelodifySpacing.lg),
                )
                results.isEmpty() && query.isNotBlank() -> Text(
                    text = stringResource(R.string.search_no_results),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(MelodifySpacing.md),
                )
                else -> LazyColumn(modifier = Modifier.height(360.dp)) {
                    items(results, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            onClick = {
                                scope.launch {
                                    when (val addResult = playlistRepository.addSongToPlaylist(playlistId, song.id)) {
                                        is Resource.Success -> {
                                            onSongAdded()
                                            message = null
                                        }
                                        is Resource.Error -> message = addResult.message
                                        Resource.Loading -> Unit
                                    }
                                }
                            },
                            onMoreClick = null,
                        )
                    }
                }
            }
        }
    }
}
