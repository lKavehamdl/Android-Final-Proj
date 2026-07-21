package com.example.final_project.ui.screens.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.domain.model.Playlist
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalProfileScreen(
    onNavigateBack: () -> Unit,
    onMessageClick: (String) -> Unit, // userId
    onPlaylistClick: (String) -> Unit, // playlistId
    viewModel: ExternalProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            com.example.final_project.ui.components.MelodifyBackTopBar(
                title = state.profile?.username ?: stringResource(R.string.tab_profile),
                onBack = onNavigateBack,
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.error_with_message, state.error ?: ""),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            val profile = state.profile
            if (profile != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MelodifySpacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            if (profile.avatarUrl != null) {
                                AsyncImage(
                                    model = com.example.final_project.ui.components.avatarModel(profile.avatarUrl),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(MelodifySpacing.sm))

                            Text(
                                text = profile.displayName ?: profile.username,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(MelodifySpacing.md))

                            // Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(count = profile.followersCount, label = stringResource(R.string.profile_followers))
                                StatItem(count = profile.followingCount, label = stringResource(R.string.profile_following))
                            }

                            Spacer(modifier = Modifier.height(MelodifySpacing.lg))

                            // Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = viewModel::onToggleFollow,
                                    enabled = !state.isTogglingFollow,
                                    modifier = Modifier.weight(1f).padding(end = MelodifySpacing.sm)
                                ) {
                                    Icon(
                                        imageVector = if (profile.isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(MelodifySpacing.xs))
                                    Text(
                                        stringResource(
                                            if (profile.isFollowing) R.string.unfollow else R.string.follow,
                                        ),
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { onMessageClick(profile.id) },
                                    modifier = Modifier.weight(1f).padding(start = MelodifySpacing.sm)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Message,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(MelodifySpacing.xs))
                                    Text(stringResource(R.string.profile_message_button))
                                }
                            }
                        }
                    }

                    if (state.publicPlaylists.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.public_playlists),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = MelodifySpacing.md,
                                    vertical = MelodifySpacing.sm
                                )
                            )
                        }

                        items(state.publicPlaylists, key = { it.id }) { playlist ->
                            PublicPlaylistRow(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicPlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(MelodifySpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            playlist.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
