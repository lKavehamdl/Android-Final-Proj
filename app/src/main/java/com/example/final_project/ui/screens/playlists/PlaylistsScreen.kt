package com.example.final_project.ui.screens.playlists

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.domain.model.Playlist
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.SectionHeader
import com.example.final_project.ui.components.ShimmerEffect   
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistsScreen(
    onOpenPlaylist: (String) -> Unit = {},
    modifier: Modifier = Modifier,
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
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.isEmpty -> PlaylistsShimmer()

            state.errorMessage != null && state.isEmpty -> {
                EmptyState(
                    titleRes = R.string.error_network,
                    subtitleRes = R.string.error_network_subtitle,
                    actionLabelRes = R.string.retry,
                    onActionClick = { viewModel.onEvent(PlaylistsUiEvent.Retry) },
                )
            }

            state.isEmpty -> {
                EmptyState(
                    titleRes = R.string.playlists_empty_title,
                    subtitleRes = R.string.playlists_empty_subtitle,
                )
            }

            else -> {
                PlaylistsContent(
                    state = state,
                    onPlaylistClick = { viewModel.onEvent(PlaylistsUiEvent.PlaylistClick(it)) },
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PlaylistsContent(
    state: PlaylistsUiState,
    onPlaylistClick: (Playlist) -> Unit,
) {
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
        playlistSection(
            titleRes = R.string.playlists_section_world,
            playlists = state.worldPlaylists,
            onPlaylistClick = onPlaylistClick,
        )
        playlistSection(
            titleRes = R.string.playlists_section_local,
            playlists = state.localPlaylists,
            onPlaylistClick = onPlaylistClick,
        )
        playlistSection(
            titleRes = R.string.playlists_section_user,
            playlists = state.userPlaylists,
            onPlaylistClick = onPlaylistClick,
        )
    }
}

/** Adds a full-width header + 2-column playlist cards for one section. */
private fun androidx.compose.foundation.lazy.grid.LazyGridScope.playlistSection(
    @StringRes titleRes: Int,
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
) {
    if (playlists.isEmpty()) return

    item(span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(titleRes = titleRes)
    }
    items(playlists, key = { it.id }) { playlist ->
        PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = playlist.coverImageUrl,
            contentDescription = playlist.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.70f),
                        ),
                    ),
                ),
        )
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(MelodifySpacing.sm),
        )
    }
}

@Composable
private fun PlaylistsShimmer() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MelodifySpacing.md),
        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
    ) {
        items(6) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = MaterialTheme.shapes.large,
            )
        }
    }
}