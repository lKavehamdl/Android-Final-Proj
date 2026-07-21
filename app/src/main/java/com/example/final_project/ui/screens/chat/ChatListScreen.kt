package com.example.final_project.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.domain.model.MessageType
import com.example.final_project.ui.theme.MelodifySpacing
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.ShimmerBox
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: () -> Unit = {},
    onChatClick: (String) -> Unit, // userId
    onSearchClick: () -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.user_search_title))
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(MelodifySpacing.md),
                verticalArrangement = Arrangement.spacedBy(MelodifySpacing.md)
            ) {
                repeat(6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(
                            width = 56.dp,
                            height = 56.dp,
                            shape = CircleShape
                        )
                        Spacer(modifier = Modifier.width(MelodifySpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            ShimmerBox(width = 120.dp, height = 16.dp)
                            Spacer(modifier = Modifier.height(MelodifySpacing.xs))
                            ShimmerBox(width = 200.dp, height = 14.dp)
                        }
                    }
                }
            }
        } else if (state.conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                EmptyState(
                    titleRes = R.string.empty_chats_title,
                    subtitleRes = R.string.empty_chats_subtitle,
                    icon = Icons.Default.Message
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.conversations, key = { it.otherUser.id }) { item ->
                    ConversationRow(
                        item = item,
                        onClick = { onChatClick(item.otherUser.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationRow(item: ConversationItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.otherUser.avatarUrl != null) {
            AsyncImage(
                model = com.example.final_project.ui.components.avatarModel(item.otherUser.avatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        } else {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(MelodifySpacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = item.otherUser.displayName ?: item.otherUser.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                val timeString = try {
                    formatter.format(Instant.parse(item.lastMessage.createdAt))
                } catch (e: Exception) {
                    ""
                }
                
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = if (item.lastMessage.messageType == MessageType.SONG) "🎵 Shared a song" else item.lastMessage.content ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
