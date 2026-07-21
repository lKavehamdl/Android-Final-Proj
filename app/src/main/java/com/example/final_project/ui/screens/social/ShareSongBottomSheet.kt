package com.example.final_project.ui.screens.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.ChatRepository
import com.example.final_project.domain.repository.SocialRepository
import com.example.final_project.ui.theme.MelodifySpacing
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSongBottomSheet(
    songId: String,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    socialRepository: SocialRepository = koinInject(),
    chatRepository: ChatRepository = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()
    var followingUsers by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var sendingToUserId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val currentUserId = socialRepository.getCurrentUserId()
        if (currentUserId != null) {
            when (val result = socialRepository.getFollowing(currentUserId)) {
                is Resource.Success -> {
                    followingUsers = result.data
                }
                else -> {}
            }
        }
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Share Song",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm)
            )
            
            SnackbarHost(hostState = snackbarHostState)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (followingUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "You aren't following anyone yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(followingUsers, key = { it.id }) { user ->
                        val isSending = sendingToUserId == user.id
                        ListItem(
                            headlineContent = {
                                Text(user.displayName ?: user.username, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Text("@${user.username}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            leadingContent = {
                                if (user.avatarUrl != null) {
                                    AsyncImage(
                                        model = com.example.final_project.ui.components.avatarModel(user.avatarUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        if (isSending) return@IconButton
                                        coroutineScope.launch {
                                            sendingToUserId = user.id
                                            val result = chatRepository.sendMessage(
                                                receiverId = user.id,
                                                content = "Check out this song!",
                                                songId = songId
                                            )
                                            sendingToUserId = null
                                            if (result is Resource.Success) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Sent to ${user.displayName ?: user.username}")
                                                }
                                                // Optional: onDismissRequest()
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Failed to send")
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isSending
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            modifier = Modifier.clickable {
                                // optional ripple
                            }
                        )
                    }
                }
            }
        }
    }
}
