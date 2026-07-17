package com.example.final_project.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.final_project.domain.repository.SongRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LikeButton(
    songId: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    unlikedTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    songRepository: SongRepository = koinInject()
) {
    var isLiked by remember(songId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(songId) {
        songRepository.getLikedSongs().collect { resource ->
            if (resource is com.example.final_project.core.result.Resource.Success) {
                isLiked = resource.data.any { it.id == songId }
            }
        }
    }

    IconButton(
        onClick = {
            val oldState = isLiked
            isLiked = !oldState
            scope.launch {
                try {
                    when (val result = songRepository.toggleLike(songId)) {
                        is com.example.final_project.core.result.Resource.Success -> {
                            isLiked = result.data
                        }
                        else -> isLiked = oldState
                    }
                } catch (e: Exception) {
                    isLiked = oldState // revert
                }
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isLiked) "Unlike" else "Like",
            tint = if (isLiked) tint else unlikedTint
        )
    }
}
