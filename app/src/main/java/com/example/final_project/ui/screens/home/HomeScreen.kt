package com.example.final_project.ui.screens.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.domain.model.Song
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.SectionHeader
import com.example.final_project.ui.components.ShimmerBox
import com.example.final_project.ui.components.SongCard
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onOpenPlaylistsTab: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
                HomeUiEffect.OpenPlaylistsTab -> onOpenPlaylistsTab()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.carousel.isEmpty() -> HomeShimmer()
            state.errorMessage != null && state.carousel.isEmpty() -> {
                EmptyState(
                    titleRes = R.string.error_network,
                    subtitleRes = R.string.error_network_subtitle,
                    actionLabelRes = R.string.retry,
                    onActionClick = { viewModel.onEvent(HomeUiEvent.Retry) },
                )
            }
            else -> {
                HomeContent(
                    state = state,
                    onSongClick = { viewModel.onEvent(HomeUiEvent.SongClick(it)) },
                    onQuickAction = { viewModel.onEvent(HomeUiEvent.QuickActionClick(it)) },
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
private fun HomeContent(
    state: HomeUiState,
    onSongClick: (Song) -> Unit,
    onQuickAction: (HomeQuickAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = MelodifySpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.sm),
    ) {
        item {
            FeaturedCarousel(songs = state.carousel, onSongClick = onSongClick)
        }
        item {
            QuickActionsRow(onQuickAction = onQuickAction)
        }
        item {
            SongSection(
                titleRes = R.string.home_section_trending,
                songs = state.trending,
                onSongClick = onSongClick,
            )
        }
        item {
            SongSection(
                titleRes = R.string.home_section_new_releases,
                songs = state.newReleases,
                onSongClick = onSongClick,
            )
        }
        item {
            SongSection(
                titleRes = R.string.home_section_global_playlists,
                songs = state.globalPlaylists,
                onSongClick = onSongClick,
            )
        }
        item {
            SongSection(
                titleRes = R.string.home_section_local_playlists,
                songs = state.localPlaylists,
                onSongClick = onSongClick,
            )
        }
    }
}

@Composable
private fun FeaturedCarousel(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
) {
    if (songs.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { songs.size })
    Column {
        SectionHeader(titleRes = R.string.home_carousel_featured)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = MelodifySpacing.md),
            pageSpacing = MelodifySpacing.md,
            modifier = Modifier
                .fillMaxWidth()
                .height(MelodifySpacing.carouselHeight),
        ) { page ->
            val song = songs[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onSongClick(song) },
            ) {
                AsyncImage(
                    model = song.coverImageUrl,
                    contentDescription = song.title,
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
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                                ),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(MelodifySpacing.md),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(onQuickAction: (HomeQuickAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        QuickAction(
            labelRes = R.string.home_quick_liked,
            icon = Icons.Filled.Favorite,
            onClick = { onQuickAction(HomeQuickAction.LIKED) },
        )
        QuickAction(
            labelRes = R.string.home_quick_recent,
            icon = Icons.Filled.History,
            onClick = { onQuickAction(HomeQuickAction.RECENT) },
        )
        QuickAction(
            labelRes = R.string.home_quick_playlists,
            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
            onClick = { onQuickAction(HomeQuickAction.PLAYLISTS) },
        )
        QuickAction(
            labelRes = R.string.home_quick_artists,
            icon = Icons.Filled.Person,
            onClick = { onQuickAction(HomeQuickAction.ARTISTS) },
        )
    }
}

@Composable
private fun QuickAction(
    @StringRes labelRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(MelodifySpacing.quickActionSize + MelodifySpacing.md)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(MelodifySpacing.quickActionSize)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(labelRes),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(modifier = Modifier.height(MelodifySpacing.xs))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SongSection(
    @StringRes titleRes: Int,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
) {
    if (songs.isEmpty()) return
    Column {
        SectionHeader(titleRes = titleRes)
        LazyRow(
            contentPadding = PaddingValues(horizontal = MelodifySpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
        ) {
            items(songs, key = { "${titleRes}-${it.id}" }) { song ->
                SongCard(song = song, onClick = onSongClick)
            }
        }
    }
}

@Composable
private fun HomeShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MelodifySpacing.md),
        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
    ) {
        ShimmerBox(
            width = MelodifySpacing.xxl * 8,
            height = MelodifySpacing.carouselHeight,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(4) {
                ShimmerBox(
                    width = MelodifySpacing.quickActionSize,
                    height = MelodifySpacing.quickActionSize,
                    shape = MaterialTheme.shapes.medium,
                )
            }
        }
        repeat(2) {
            ShimmerBox(width = MelodifySpacing.xxl * 3, height = MelodifySpacing.lg)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md)) {
                items(4) {
                    ShimmerBox(
                        width = MelodifySpacing.songCardCover,
                        height = MelodifySpacing.songCardCover,
                    )
                }
            }
        }
    }
}
