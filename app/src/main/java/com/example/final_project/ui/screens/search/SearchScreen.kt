package com.example.final_project.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.final_project.R
import com.example.final_project.data.paging.SongSearchPagingSource
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.ShimmerEffect
import com.example.final_project.ui.components.SongRow
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.songs.collectAsLazyPagingItems()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MelodifySpacing.md),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MelodifySpacing.sm),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.sm),
            contentPadding = PaddingValues(vertical = MelodifySpacing.md),
        ) {
            val filters = listOf(
                SongSearchPagingSource.SearchFilter.ALL to R.string.search_filter_all,
                SongSearchPagingSource.SearchFilter.SONGS to R.string.search_filter_songs,
                SongSearchPagingSource.SearchFilter.ARTISTS to R.string.search_filter_artists,
                SongSearchPagingSource.SearchFilter.ALBUMS to R.string.search_filter_albums,
            )
            items(filters.size) { index ->
                val (filter, labelRes) = filters[index]
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.onFilterSelected(filter) },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }

        val showHistory = state.query.isBlank() && state.history.isNotEmpty()
        if (showHistory) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.search_history_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = viewModel::clearHistory) {
                    Text(stringResource(R.string.search_history_clear))
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.history, key = { it }) { query ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onHistoryClick(query) }
                            .padding(vertical = MelodifySpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = query,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = MelodifySpacing.md),
                        )
                        IconButton(onClick = { viewModel.deleteHistory(query) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    }
                }
            }
            return
        }

        when (pagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                Column(verticalArrangement = Arrangement.spacedBy(MelodifySpacing.sm)) {
                    repeat(6) {
                        ShimmerEffect(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(vertical = MelodifySpacing.xs),
                        )
                    }
                }
            }
            is LoadState.Error -> {
                EmptyState(
                    titleRes = R.string.error_network,
                    subtitleRes = R.string.error_network_subtitle,
                    actionLabelRes = R.string.retry,
                    onActionClick = { pagingItems.retry() },
                )
            }
            else -> {
                if (pagingItems.itemCount == 0 && state.query.isNotBlank()) {
                    EmptyState(
                        titleRes = R.string.search_no_results,
                        subtitleRes = R.string.search_no_results_subtitle,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = MelodifySpacing.xl),
                    ) {
                        items(
                            count = pagingItems.itemCount,
                            key = pagingItems.itemKey { it.id },
                        ) { index ->
                            val song = pagingItems[index] ?: return@items
                            SongRow(
                                song = song,
                                onClick = { viewModel.playSong(song) },
                            )
                        }
                    }
                }
            }
        }
    }
}
