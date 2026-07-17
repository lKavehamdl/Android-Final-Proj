package com.example.final_project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.example.final_project.domain.model.Song
import com.example.final_project.ui.theme.MelodifySpacing

/**
 * Vertical song tile for horizontal [androidx.compose.foundation.lazy.LazyRow]s (Home sections).
 *
 * **Locked signature — do not change without team agreement.**
 */
@Composable
fun SongCard(
    song: Song,
    onClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(MelodifySpacing.songCardCover)
            .clickable { onClick(song) },
        verticalArrangement = Arrangement.spacedBy(MelodifySpacing.sm),
    ) {
        Box {
            CoverArt(
                imageUrl = song.coverImageUrl,
                contentDescription = song.title,
                size = MelodifySpacing.songCardCover,
            )
        }
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = song.artistName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = MelodifySpacing.xs),
        )
    }
}
