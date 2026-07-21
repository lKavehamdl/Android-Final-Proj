package com.example.final_project.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.final_project.R
import com.example.final_project.domain.model.Song
import com.example.final_project.player.PlayerController
import com.example.final_project.ui.player.AddToPlaylistSheet
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.compose.koinInject

/**
 * Bottom-sheet context menu for a song: Add to queue / Add to playlist. Shown from a
 * bottom sheet (not a dropdown) so it always appears in a sensible place regardless
 * of where on screen the row was long-pressed.
 *
 * [onConsumed] fires after the song is added to the queue or a playlist — callers
 * that show a rolling recommendation list use it to drop-and-replace the item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionSheet(
    song: Song,
    visible: Boolean,
    onDismiss: () -> Unit,
    onConsumed: () -> Unit = {},
) {
    val playerController: PlayerController = koinInject()
    val context = LocalContext.current
    var showAddToPlaylist by remember { mutableStateOf(false) }

    if (visible) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = MelodifySpacing.md),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CoverArt(imageUrl = song.coverImageUrl, contentDescription = song.title, size = 48.dp)
                    Spacer(modifier = Modifier.width(MelodifySpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
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
                }
                HorizontalDivider()
                ActionRow(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    label = stringResource(R.string.player_add_to_queue),
                ) {
                    playerController.addToQueue(song)
                    Toast.makeText(context, context.getString(R.string.player_added_to_queue), Toast.LENGTH_SHORT).show()
                    onConsumed()
                    onDismiss()
                }
                ActionRow(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    label = stringResource(R.string.player_add_to_playlist),
                ) {
                    onDismiss()
                    showAddToPlaylist = true
                }
            }
        }
    }

    if (showAddToPlaylist) {
        AddToPlaylistSheet(
            songId = song.id,
            onDismiss = { showAddToPlaylist = false },
            onResult = { success, messageRes, messageText ->
                val msg = messageRes?.let { context.getString(it) } ?: messageText
                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) onConsumed()
            },
        )
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
