package com.example.final_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import com.example.final_project.R

/**
 * Shared cover art loader (Coil). Used by [SongCard] and [SongRow] — do not reimplement.
 */
@Composable
fun CoverArt(
    imageUrl: String?,
    contentDescription: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription ?: stringResource(R.string.cd_cover_art),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(shape),
        loading = {
            ShimmerEffect(
                modifier = Modifier.fillMaxSize(),
                shape = shape,
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
