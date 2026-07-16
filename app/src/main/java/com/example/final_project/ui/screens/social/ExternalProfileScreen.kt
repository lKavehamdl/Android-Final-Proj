package com.example.final_project.ui.screens.social

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.final_project.domain.model.Playlist
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalProfileScreen(
    onNavigateBack: () -> Unit,
    onMessageClick: (String) -> Unit, // userId
    viewModel: ExternalProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.profile?.username ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error)
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
                                    model = profile.avatarUrl,
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
                                StatItem(count = profile.followersCount, label = "Followers")
                                StatItem(count = profile.followingCount, label = "Following")
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
                                    Text(if (profile.isFollowing) "Unfollow" else "Follow")
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
                                    Text("Message")
                                }
                            }
                        }
                    }

                    if (state.publicPlaylists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Public Playlists",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = MelodifySpacing.md,
                                    vertical = MelodifySpacing.sm
                                )
                            )
                        }

                        items(state.publicPlaylists, key = { it.id }) { playlist ->
                            Text(
                                text = playlist.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm)
                            )
                            // We can use PlaylistCard here if we want later
                        }
                    }
                }
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
