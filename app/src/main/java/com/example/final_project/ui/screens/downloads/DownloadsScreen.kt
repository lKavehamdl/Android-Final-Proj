package com.example.final_project.ui.screens.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.R
import com.example.final_project.domain.model.DownloadedSong
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.ShimmerEffect
import com.example.final_project.ui.components.SongRow
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun DownloadsScreen(
    modifier: Modifier = Modifier,
    viewModel: DownloadsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.downloads_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(MelodifySpacing.md)
        )

        AnimatedVisibility(visible = state.songs.isNotEmpty() || state.isLoading) {
            SortBar(
                currentSort = state.sortOrder,
                onSortChanged = viewModel::onSortChanged
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> DownloadsShimmer()
                state.songs.isEmpty() -> {
                    EmptyState(
                        titleRes = R.string.downloads_empty_title,
                        subtitleRes = R.string.downloads_empty_subtitle,
                        icon = Icons.Outlined.Download
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = MelodifySpacing.xl)
                    ) {
                        items(
                            items = state.sortedSongs,
                            key = { it.id }
                        ) { downloadedSong ->
                            SwipeableDownloadRow(
                                downloadedSong = downloadedSong,
                                onDelete = { viewModel.onDelete(downloadedSong.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortBar(
    currentSort: DownloadSortOrder,
    onSortChanged: (DownloadSortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MelodifySpacing.md),
        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.sm)
    ) {
        Text(
            text = stringResource(R.string.downloads_sort),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        DownloadSortOrder.entries.forEach { sort ->
            val labelRes = when (sort) {
                DownloadSortOrder.BY_NAME -> R.string.downloads_sort_name
                DownloadSortOrder.BY_DATE -> R.string.downloads_sort_date
                DownloadSortOrder.BY_SIZE -> R.string.downloads_sort_size
            }
            FilterChip(
                selected = currentSort == sort,
                onClick = { onSortChanged(sort) },
                label = { Text(stringResource(labelRes)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableDownloadRow(
    downloadedSong: DownloadedSong,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { DismissBackground(dismissState) },
        enableDismissFromStartToEnd = false,
        modifier = Modifier
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.xs)
            .clip(RoundedCornerShape(8.dp))
    ) {
        SongRow(
            song = downloadedSong.song,
            onClick = { /* Will play the offline file in Phase 6 */ },
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(MelodifySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun DownloadsShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MelodifySpacing.md),
        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.md)
    ) {
        repeat(8) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerEffect(
                    modifier = Modifier.size(MelodifySpacing.songRowCover),
                    shape = MaterialTheme.shapes.small
                )
                Spacer(modifier = Modifier.size(MelodifySpacing.md))
                Column {
                    ShimmerEffect(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.6f),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    Spacer(modifier = Modifier.height(MelodifySpacing.sm))
                    ShimmerEffect(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.4f),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                }
            }
        }
    }
}
