package com.example.final_project.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.final_project.R
import com.example.final_project.domain.model.Song
import com.example.final_project.ui.theme.MelodifySpacing

/**
 * Horizontal song list item for Search / Downloads / Liked / queues.
 *
 * **Locked signature — do not change without team agreement.**
 *
 * Long-pressing any row opens a context sheet (Add to queue / Add to playlist) so
 * those actions are available everywhere a song is shown.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRow(
    song: Song,
    onClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
    onMoreClick: ((Song) -> Unit)? = null,
) {
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(song) },
                onLongClick = { showActions = true },
            )
            .padding(vertical = MelodifySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
    ) {
        CoverArt(
            imageUrl = song.coverImageUrl,
            contentDescription = song.title,
            size = MelodifySpacing.songRowCover,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MelodifySpacing.xxs),
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onMoreClick != null) {
            IconButton(onClick = { onMoreClick(song) }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.more_options),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    SongActionSheet(song = song, visible = showActions, onDismiss = { showActions = false })
}

/**
 * [SongRow] with a "…" menu that surfaces a single, explicit "Remove" action
 * instead of removing on tap or via a hidden swipe gesture.
 */
@Composable
fun RemovableSongRow(
    song: Song,
    onClick: (Song) -> Unit,
    removeLabel: String,
    onRemove: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        SongRow(
            song = song,
            onClick = onClick,
            onMoreClick = { showMenu = true },
        )
        // Anchor the menu to the right edge (next to the "…" button) rather than
        // the row's top-left, so it drops down from the three-dot icon.
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(removeLabel, color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    onClick = {
                        showMenu = false
                        onRemove(song)
                    },
                )
            }
        }
    }
}
