package com.example.final_project.ui.screens.profile.follows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowsScreen(
    onNavigateBack: () -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_following)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
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
                        icon = Icons.Default.Person,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.following.isEmpty() -> {
                    EmptyState(
                        titleRes = R.string.empty_state_generic_title,
                        subtitleRes = R.string.empty_state_generic_subtitle,
                        icon = Icons.Default.Person,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(MelodifySpacing.md),
                        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.sm)
                    ) {
                        items(state.following, key = { it.id }) { user ->
                            FollowItem(
                                user = user,
                                onClick = { onUserClick(user.id) },
                                onUnfollow = { viewModel.onUnfollow(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowItem(
    user: UserProfile,
    onClick: () -> Unit,
    onUnfollow: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MelodifySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.avatarUrl != null) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        } else {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(MelodifySpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName ?: user.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onUnfollow) {
            Icon(
                imageVector = Icons.Default.PersonRemove,
                contentDescription = "Unfollow",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
