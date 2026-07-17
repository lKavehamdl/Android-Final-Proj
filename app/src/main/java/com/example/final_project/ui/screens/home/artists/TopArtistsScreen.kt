package com.example.final_project.ui.screens.home.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.R
import com.example.final_project.domain.model.Artist
import com.example.final_project.ui.components.CoverArt
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopArtistsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TopArtistsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.artists_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    EmptyState(
                        titleRes = R.string.error_network,
                        subtitleRes = R.string.error_network_subtitle,
                        icon = Icons.Outlined.Person,
                        actionLabelRes = R.string.retry,
                        onActionClick = { viewModel.load() },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                state.artists.isEmpty() -> {
                    EmptyState(
                        titleRes = R.string.empty_state_generic_title,
                        subtitleRes = R.string.empty_state_generic_subtitle,
                        icon = Icons.Outlined.Person,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(MelodifySpacing.md),
                        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.sm),
                    ) {
                        items(state.artists, key = { it.name }) { artist ->
                            ArtistRow(artist = artist)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistRow(
    artist: Artist,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MelodifySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
    ) {
        CoverArt(
            imageUrl = artist.coverImageUrl,
            contentDescription = artist.name,
            size = MelodifySpacing.songRowCover,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.playlists_songs_count, artist.songCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
