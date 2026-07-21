package com.example.final_project.ui.screens.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.R
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.MelodifyBackTopBar
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Shows only the playlists the signed-in user created — the target of the Home
 * "My Playlists" quick action (distinct from the Playlists tab, which also lists
 * World/Local playlists).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlaylistsScreen(
    onNavigateBack: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    viewModel: PlaylistsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlaylistsUiEffect.OpenPlaylist -> onOpenPlaylist(effect.playlistId)
                is PlaylistsUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                is PlaylistsUiEffect.ShowTextMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (state.showCreateDialog) {
        CreatePlaylistDialog(
            isCreating = state.isCreating,
            onDismiss = { viewModel.onEvent(PlaylistsUiEvent.DismissCreateDialog) },
            onConfirm = { name -> viewModel.onEvent(PlaylistsUiEvent.ConfirmCreatePlaylist(name)) },
        )
    }

    Scaffold(
        topBar = {
            MelodifyBackTopBar(
                title = stringResource(R.string.playlists_section_user),
                onBack = onNavigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onEvent(PlaylistsUiEvent.CreatePlaylistClick) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.playlist_create))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.userPlaylists.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.userPlaylists.isEmpty() -> {
                    EmptyState(
                        titleRes = R.string.playlists_empty_title,
                        subtitleRes = R.string.playlists_empty_subtitle,
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = MelodifySpacing.md,
                            end = MelodifySpacing.md,
                            bottom = MelodifySpacing.lg,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
                        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
                    ) {
                        items(state.userPlaylists, key = { it.id }) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { viewModel.onEvent(PlaylistsUiEvent.PlaylistClick(playlist)) },
                            )
                        }
                    }
                }
            }
        }
    }
}
