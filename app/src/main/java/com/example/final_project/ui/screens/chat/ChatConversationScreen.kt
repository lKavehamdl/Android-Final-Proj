package com.example.final_project.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.model.MessageStatus
import com.example.final_project.domain.model.MessageType
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.compose.koinInject
import com.example.final_project.domain.repository.SongRepository
import com.example.final_project.player.PlayerController
import com.example.final_project.domain.model.Song
import com.example.final_project.core.result.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatConversationViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.otherUser?.avatarUrl != null) {
                            AsyncImage(
                                model = state.otherUser!!.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(MelodifySpacing.sm))
                        Text(
                            text = state.otherUser?.displayName ?: state.otherUser?.username ?: "Chat",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = state.inputText,
                onInputTextChanged = viewModel::onInputTextChanged,
                onSend = viewModel::onSendMessage,
                isSending = state.isSending
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(MelodifySpacing.md))
                        Text(
                            "Say hello!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MelodifySpacing.md),
                    contentPadding = PaddingValues(vertical = MelodifySpacing.md)
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isMine = message.senderId == state.currentUserId
                        )
                        Spacer(modifier = Modifier.height(MelodifySpacing.sm))
                    }
                }
            }
            
            if (state.isOtherUserTyping) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = MelodifySpacing.md, bottom = MelodifySpacing.sm),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = "typing...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = MelodifySpacing.sm, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    val backgroundColor = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isMine) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = shape,
            color = backgroundColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(MelodifySpacing.sm)) {
                if (message.messageType == MessageType.TEXT) {
                    Text(
                        text = message.content ?: "",
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (message.messageType == MessageType.SONG && message.songId != null) {
                    SharedSongCard(
                        songId = message.songId,
                        textColor = textColor
                    )
                }
                
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                    val timeString = try { formatter.format(Instant.parse(message.createdAt)) } catch (e: Exception) { "" }
                    
                    Text(
                        text = timeString,
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when(message.status.name.lowercase()) {
                                "sending" -> "🕒"
                                "sent" -> "✓"
                                "read" -> "✓✓"
                                else -> "✓"
                            },
                            color = if (message.status.name.lowercase() == "read") MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = MelodifySpacing.sm, vertical = MelodifySpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.width(MelodifySpacing.sm))
            
            FilledIconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isSending,
                shape = CircleShape
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun SharedSongCard(
    songId: String,
    textColor: Color,
    songRepository: SongRepository = koinInject(),
    playerController: PlayerController = koinInject()
) {
    var song by remember { mutableStateOf<Song?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(songId) {
        isLoading = true
        when (val result = songRepository.getSongById(songId)) {
            is Resource.Success -> {
                song = result.data
            }
            else -> {}
        }
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = textColor)
    } else if (song != null) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                playerController.play(song!!)
            }
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song!!.coverImageUrl,
                    contentDescription = song!!.title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = song!!.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = song!!.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Text("Song unavailable", color = textColor)
    }
}
