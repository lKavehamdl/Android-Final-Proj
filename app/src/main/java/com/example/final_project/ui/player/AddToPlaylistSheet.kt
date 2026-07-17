package com.example.final_project.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.final_project.R
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.repository.PlaylistRepository
import com.example.final_project.ui.theme.MelodifySpacing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    songId: String,
    onDismiss: () -> Unit,
    onResult: (success: Boolean, messageRes: Int?, messageText: String?) -> Unit,
    playlistRepository: PlaylistRepository = koinInject(),
    supabase: SupabaseClient = koinInject(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var emptyHint by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            isLoading = false
            emptyHint = true
            return@LaunchedEffect
        }
        when (val result = playlistRepository.getUserPlaylists(userId).first { it !is Resource.Loading }) {
            is Resource.Success -> {
                playlists = result.data
                emptyHint = result.data.isEmpty()
            }
            else -> emptyHint = true
        }
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MelodifySpacing.md)
                .padding(bottom = MelodifySpacing.xl),
        ) {
            Text(
                text = stringResource(R.string.playlist_picker_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MelodifySpacing.md),
            )

            when {
                isLoading -> {
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                emptyHint -> {
                    Text(
                        text = stringResource(R.string.playlist_picker_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    LazyColumn {
                        items(playlists, key = { it.id }) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            when (val result =
                                                playlistRepository.addSongToPlaylist(playlist.id, songId)
                                            ) {
                                                is Resource.Success -> {
                                                    onResult(true, R.string.playlist_add_song_success, null)
                                                    onDismiss()
                                                }
                                                is Resource.Error -> {
                                                    onResult(false, null, result.message)
                                                }
                                                Resource.Loading -> Unit
                                            }
                                        }
                                    }
                                    .padding(vertical = MelodifySpacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.md),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
