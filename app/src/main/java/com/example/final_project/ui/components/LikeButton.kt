package com.example.final_project.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.R
import com.example.final_project.domain.favorites.FavoritesStore
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LikeButton(
    songId: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    unlikedTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    favoritesStore: FavoritesStore = koinInject(),
) {
    val likedIds by favoritesStore.likedIds.collectAsStateWithLifecycle()
    val isLiked = songId in likedIds
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                favoritesStore.toggle(songId)
            }
        },
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(
                if (isLiked) R.string.cd_unlike else R.string.cd_like,
            ),
            tint = if (isLiked) tint else unlikedTint,
        )
    }
}
