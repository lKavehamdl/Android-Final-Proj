package com.example.final_project.ui.screens.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.final_project.R
import com.example.final_project.ui.components.MelodifyBackTopBar
import com.example.final_project.ui.components.SongRow
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    viewModel: PlaylistDetailViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.songs.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddSong by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlaylistDetailEffect.Deleted -> onNavigateBack()
                is PlaylistDetailEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (showAddSong) {
        AddSongToPlaylistSheet(
            playlistId = playlistId,
            onDismiss = { showAddSong = false },
            onSongAdded = { viewModel.onSongsChanged() },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.playlist_delete)) },
            text = { Text(stringResource(R.string.playlist_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deletePlaylist()
                    },
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            MelodifyBackTopBar(
                title = state.playlistName.ifBlank { stringResource(R.string.tab_playlists) },
                onBack = onNavigateBack,
                actions = {
                    if (state.isOwner) {
                        IconButton(onClick = { showAddSong = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = stringResource(R.string.playlist_add_song_action),
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.playlist_delete)) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirm = true
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.songCount > 0) {
                FloatingActionButton(onClick = { viewModel.playAll() }) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.play_all),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading && pagingItems.itemCount == 0 -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null && pagingItems.itemCount == 0 -> {
                    Text(
                        text = stringResource(R.string.error_with_message, state.error ?: ""),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                    pagingItems.itemCount == 0 -> {
                    Text(
                        text = stringResource(R.string.playlist_detail_empty),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                    ) {
                        items(
                            count = pagingItems.itemCount,
                            key = { index -> pagingItems.peek(index)?.id ?: index },
                        ) { index ->
                            val song = pagingItems[index] ?: return@items
                            if (state.isOwner) {
                                com.example.final_project.ui.components.RemovableSongRow(
                                    song = song,
                                    onClick = { viewModel.playSong(it) },
                                    removeLabel = stringResource(R.string.playlist_remove_song),
                                    onRemove = { viewModel.removeSong(it) },
                                )
                            } else {
                                SongRow(song = song, onClick = { viewModel.playSong(song) })
                            }
                        }
                    }
                }
            }
        }
    }
}
