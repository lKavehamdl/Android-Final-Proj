package com.example.final_project.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.R
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.SectionHeader
import com.example.final_project.ui.components.ShimmerBox
import com.example.final_project.ui.components.SongCard
import com.example.final_project.ui.components.SongRow
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Home tab — Step 1 uses shared components against live song data.
 * Step 2 adds carousel, quick actions, and full section layout.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { /* snackbars in later phases */ }
    }

    when {
        state.isLoading && state.songs.isEmpty() -> {
            HomeShimmer(modifier = modifier)
        }

        state.errorMessage != null && state.songs.isEmpty() -> {
            EmptyState(
                titleRes = R.string.error_network,
                subtitleRes = R.string.error_network_subtitle,
                modifier = modifier,
                actionLabelRes = R.string.retry,
                onActionClick = { viewModel.onEvent(HomeUiEvent.Retry) },
            )
        }

        state.songs.isEmpty() -> {
            EmptyState(
                titleRes = R.string.empty_state_generic_title,
                subtitleRes = R.string.empty_state_generic_subtitle,
                modifier = modifier,
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = MelodifySpacing.lg),
            ) {
                item {
                    SectionHeader(titleRes = R.string.home_section_trending)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = MelodifySpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
                    ) {
                        items(state.songs.take(12), key = { "card-${it.id}" }) { song ->
                            SongCard(
                                song = song,
                                onClick = { /* Step 2: play */ },
                            )
                        }
                    }
                }
                item {
                    SectionHeader(titleRes = R.string.home_section_recommended)
                }
                items(state.songs.take(20), key = { "row-${it.id}" }) { song ->
                    SongRow(
                        song = song,
                        onClick = { /* Step 2: play */ },
                        modifier = Modifier.padding(horizontal = MelodifySpacing.md),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MelodifySpacing.md),
        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
    ) {
        ShimmerBox(
            width = MelodifySpacing.xxl * 3,
            height = MelodifySpacing.lg,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md)) {
            items(4) {
                ShimmerBox(
                    width = MelodifySpacing.songCardCover,
                    height = MelodifySpacing.songCardCover,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MelodifySpacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
