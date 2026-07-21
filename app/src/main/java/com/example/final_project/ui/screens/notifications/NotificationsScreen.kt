package com.example.final_project.ui.screens.notifications

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.domain.model.AppNotification
import com.example.final_project.domain.model.NotificationType
import com.example.final_project.ui.components.EmptyState
import com.example.final_project.ui.components.avatarModel
import com.example.final_project.ui.components.relativeTimeLabel
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenChat: (String) -> Unit = {},
    onOpenProfile: (String) -> Unit = {},
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            com.example.final_project.ui.components.MelodifyBackTopBar(
                title = stringResource(R.string.notifications_title),
                onBack = onNavigateBack,
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = viewModel::onClearAll) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.notif_clear_all),
                            )
                        }
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    titleRes = R.string.notifications_empty_title,
                    subtitleRes = R.string.notifications_empty_subtitle,
                    icon = Icons.Default.NotificationsNone,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = MelodifySpacing.sm),
            ) {
                items(notifications, key = { it.id }) { notification ->
                    SwipeableNotification(
                        notification = notification,
                        onDelete = { viewModel.onDelete(notification.id) },
                        onClick = {
                            when (notification.type) {
                                NotificationType.MESSAGE -> onOpenChat(notification.actorId)
                                NotificationType.FOLLOW,
                                NotificationType.UNFOLLOW -> onOpenProfile(notification.actorId)
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNotification(
    notification: AppNotification,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = MelodifySpacing.lg),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        NotificationRow(notification = notification, onClick = onClick)
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onClick: () -> Unit) {
    val text = when (notification.type) {
        NotificationType.MESSAGE -> stringResource(R.string.notification_message, notification.actorName)
        NotificationType.FOLLOW -> stringResource(R.string.notification_follow, notification.actorName)
        NotificationType.UNFOLLOW -> stringResource(R.string.notification_unfollow, notification.actorName)
    }
    val fallbackIcon: ImageVector = when (notification.type) {
        NotificationType.FOLLOW -> Icons.Default.PersonAdd
        NotificationType.UNFOLLOW -> Icons.Default.PersonRemove
        NotificationType.MESSAGE -> Icons.Default.Person
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Unread rows get a subtle tinted background.
            .background(
                if (notification.isRead) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (notification.actorAvatarUrl != null) {
            AsyncImage(
                model = avatarModel(notification.actorAvatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
            )
        } else {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(MelodifySpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!notification.preview.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(MelodifySpacing.sm))

        Text(
            text = relativeTimeLabel(notification.createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
