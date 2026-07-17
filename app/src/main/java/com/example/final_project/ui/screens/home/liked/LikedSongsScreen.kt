package com.example.final_project.ui.screens.home.liked

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.R
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.SongRow
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedSongsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LikedSongsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked Songs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.songs.isNotEmpty()) {
                FloatingActionButton(onClick = { viewModel.playAll() }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play All")
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    EmptyState(
                        titleRes = R.string.error_network,
                        subtitleRes = R.string.error_network_subtitle,
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.songs.isEmpty() -> {
                    EmptyState(
                        titleRes = R.string.empty_state_generic_title,
                        subtitleRes = R.string.empty_state_generic_subtitle,
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // space for FAB
                    ) {
                        items(
                            items = state.songs,
                            key = { it.id }
                        ) { song ->
                            SwipeToDismissBox(
                                state = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.onUnlike(song.id)
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                ),
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = MelodifySpacing.md),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                SongRow(
                                    song = song,
                                    onClick = { viewModel.playSong(song) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
