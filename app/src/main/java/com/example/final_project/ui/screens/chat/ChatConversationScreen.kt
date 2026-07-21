package com.example.final_project.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.final_project.ui.components.relativeTimeLabel
import java.time.Duration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.model.MessageStatus
import com.example.final_project.domain.model.MessageType
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
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
    onOpenProfile: () -> Unit = {},
    viewModel: ChatConversationViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.messages.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val palette = rememberChatPalette()

    LaunchedEffect(pagingItems.itemCount) {
        if (pagingItems.itemCount > 0) {
            listState.animateScrollToItem(pagingItems.itemCount - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                otherUser = state.otherUser,
                isTyping = state.isOtherUserTyping,
                onBack = onNavigateBack,
                onOpenProfile = onOpenProfile,
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
                .background(palette.background)
                .padding(innerPadding)
        ) {
            if (pagingItems.itemCount == 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.dateChipBg,
                        contentColor = palette.dateChipText,
                    ) {
                        Text(
                            stringResource(R.string.chat_say_hello),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm),
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MelodifySpacing.sm),
                    contentPadding = PaddingValues(vertical = MelodifySpacing.sm),
                    // Short conversations hug the input bar (Telegram-style); when
                    // the list overflows the viewport this has no effect.
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    items(
                        count = pagingItems.itemCount,
                        key = { index -> pagingItems.peek(index)?.id ?: index },
                    ) { index ->
                        val message = pagingItems[index] ?: return@items
                        val next = if (index < pagingItems.itemCount - 1) pagingItems.peek(index + 1) else null

                        val isLastInGroup = next == null ||
                            next.senderId != message.senderId ||
                            dayKey(next.createdAt) != dayKey(message.createdAt)

                        MessageBubble(
                            message = message,
                            isMine = message.senderId == state.currentUserId,
                            isLastInGroup = isLastInGroup,
                            palette = palette,
                            onDelete = { viewModel.onDeleteMessage(message) },
                        )
                        Spacer(modifier = Modifier.height(if (isLastInGroup) MelodifySpacing.sm else 2.dp))
                    }
                }
            }

            // Floating date pill, pinned to the top of the page. Shows the day of
            // the topmost visible message and updates as you scroll (Telegram-style).
            if (pagingItems.itemCount > 0) {
                val today = stringResource(R.string.chat_date_today)
                val yesterday = stringResource(R.string.chat_date_yesterday)
                val topDateLabel by remember {
                    derivedStateOf {
                        val idx = listState.firstVisibleItemIndex
                        val iso = if (idx in 0 until pagingItems.itemCount) {
                            pagingItems.peek(idx)?.createdAt
                        } else null
                        iso?.let { dateChipLabel(it, today, yesterday) }
                    }
                }
                topDateLabel?.let { label ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = MelodifySpacing.sm),
                    ) {
                        DatePill(label = label, palette = palette)
                    }
                }
            }

            if (state.isOtherUserTyping) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = MelodifySpacing.md, bottom = MelodifySpacing.sm),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.incomingBubble,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = stringResource(R.string.chat_typing_short),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = MelodifySpacing.sm, vertical = 4.dp),
                        color = palette.incomingText.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    otherUser: UserProfile?,
    isTyping: Boolean,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            Spacer(modifier = Modifier.statusBarsPadding())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }

                // Avatar + name/subtitle — the whole block opens the profile.
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onOpenProfile)
                        .padding(end = MelodifySpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (otherUser?.avatarUrl != null) {
                        AsyncImage(
                            model = com.example.final_project.ui.components.avatarModel(otherUser.avatarUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(9.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(MelodifySpacing.sm))

                    // Only render name/subtitle once the profile has loaded, so we
                    // never flash the "Messages" / "last seen recently" fallbacks.
                    if (otherUser != null) {
                        Column {
                            Text(
                                text = otherUser.displayName ?: otherUser.username,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            when {
                                isTyping -> Text(
                                    text = stringResource(R.string.chat_typing_short),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                )
                                otherUser.lastSeen != null -> {
                                    val online = isOnline(otherUser.lastSeen)
                                    Text(
                                        text = if (online) stringResource(R.string.chat_online)
                                        else stringResource(
                                            R.string.chat_last_seen,
                                            relativeTimeLabel(otherUser.lastSeen),
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (online) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                else -> Text(
                                    text = "@${otherUser.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePill(label: String, palette: ChatPalette) {
    Surface(shape = RoundedCornerShape(12.dp), color = palette.dateChipBg, shadowElevation = 1.dp) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.dateChipText,
            modifier = Modifier.padding(horizontal = MelodifySpacing.md, vertical = 4.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    isLastInGroup: Boolean,
    palette: ChatPalette,
    onDelete: () -> Unit = {},
) {
    val backgroundColor = if (isMine) palette.outgoingBubble else palette.incomingBubble
    val textColor = if (isMine) palette.outgoingText else palette.incomingText
    val radius = 18.dp
    val tail = 5.dp
    val shape = if (isMine) {
        RoundedCornerShape(
            topStart = radius, topEnd = radius,
            bottomStart = radius, bottomEnd = if (isLastInGroup) tail else radius,
        )
    } else {
        RoundedCornerShape(
            topStart = radius, topEnd = radius,
            bottomEnd = radius, bottomStart = if (isLastInGroup) tail else radius,
        )
    }

    var menuOpen by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedMsg = stringResource(R.string.chat_message_copied)
    val canCopy = message.messageType == MessageType.TEXT && !message.content.isNullOrBlank()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
      Box {
        Surface(
            shape = shape,
            color = backgroundColor,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { if (canCopy || isMine) menuOpen = true },
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
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
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatClock(message.createdAt),
                        color = textColor.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )

                    if (isMine) {
                        Spacer(modifier = Modifier.width(3.dp))
                        val statusLabel = when (message.status) {
                            MessageStatus.SENDING -> "🕒"
                            MessageStatus.SENT -> "✓"
                            MessageStatus.DELIVERED -> "✓✓"
                            MessageStatus.READ -> "✓✓"
                        }
                        Text(
                            text = statusLabel,
                            color = if (message.status == MessageStatus.READ) {
                                palette.readCheck
                            } else {
                                textColor.copy(alpha = 0.6f)
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            if (canCopy) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_copy)) },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = {
                        clipboard.setText(AnnotatedString(message.content ?: ""))
                        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                        menuOpen = false
                    },
                )
            }
            if (isMine) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete)) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        onDelete()
                        menuOpen = false
                    },
                )
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
                // Union of IME + nav-bar insets: sits right on the keyboard when
                // it's open (no gap) and above the nav bar when it's closed —
                // applying both separately double-counted and left a gap.
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .padding(horizontal = MelodifySpacing.sm, vertical = MelodifySpacing.sm),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = inputText,
                onValueChange = onInputTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = { Text(stringResource(R.string.chat_type_hint)) },
                maxLines = 5,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )

            Spacer(modifier = Modifier.width(MelodifySpacing.sm))

            FilledIconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isSending,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send),
                    )
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
            color = Color.Black.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp),
            onClick = {
                playerController.play(song!!)
            }
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .widthIn(min = 220.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song!!.coverImageUrl,
                    contentDescription = song!!.title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = song!!.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song!!.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    } else {
        Text(stringResource(R.string.chat_song_unavailable), color = textColor)
    }
}

// ── Telegram-ish palette ────────────────────────────────────────────────────

data class ChatPalette(
    val background: Color,
    val incomingBubble: Color,
    val outgoingBubble: Color,
    val incomingText: Color,
    val outgoingText: Color,
    val dateChipBg: Color,
    val dateChipText: Color,
    val readCheck: Color,
)

@Composable
private fun rememberChatPalette(): ChatPalette {
    // Derive from the *applied* theme (respects the app's manual theme override,
    // not just the system setting).
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) {
        ChatPalette(
            background = Color(0xFF0E1621),
            incomingBubble = Color(0xFF182533),
            outgoingBubble = Color(0xFF2B5278),
            incomingText = Color(0xFFECEFF1),
            outgoingText = Color(0xFFECEFF1),
            dateChipBg = Color(0x66000000),
            dateChipText = Color(0xFFE1E8ED),
            readCheck = Color(0xFF5FB0F0),
        )
    } else {
        ChatPalette(
            background = Color(0xFFD9E4EC),
            incomingBubble = Color(0xFFFFFFFF),
            outgoingBubble = Color(0xFFEFFDDE),
            incomingText = Color(0xFF0B0B0B),
            outgoingText = Color(0xFF0B0B0B),
            dateChipBg = Color(0x4D000000),
            dateChipText = Color(0xFFFFFFFF),
            readCheck = Color(0xFF3A8FD6),
        )
    }
}

private val clockFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZoneId.systemDefault())

private fun formatClock(iso: String): String = try {
    clockFormatter.format(Instant.parse(iso))
} catch (e: Exception) {
    ""
}

/** Considered online if active within the last ~70 seconds. */
private fun isOnline(lastSeenIso: String): Boolean = try {
    Duration.between(Instant.parse(lastSeenIso), Instant.now()).seconds < 70
} catch (e: Exception) {
    false
}

/** yyyy-MM-dd key in the device zone, for grouping messages by day. */
private fun dayKey(iso: String): String = try {
    Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate().toString()
} catch (e: Exception) {
    iso
}

private fun dateChipLabel(iso: String, today: String, yesterday: String): String = try {
    val date = Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate()
    val now = LocalDate.now()
    when (date) {
        now -> today
        now.minusDays(1) -> yesterday
        else -> dateFormatter.format(Instant.parse(iso))
    }
} catch (e: Exception) {
    ""
}
