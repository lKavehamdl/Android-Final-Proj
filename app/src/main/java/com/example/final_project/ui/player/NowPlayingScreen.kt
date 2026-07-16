package com.example.final_project.ui.player

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.final_project.R
import com.example.final_project.domain.model.Song
import com.example.final_project.player.PlayerSharedKeys
import com.example.final_project.ui.components.CoverArt
import com.example.final_project.ui.theme.MelodifySpacing
import kotlin.math.sin

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val rotation = rememberInfiniteTransition(label = "cd")
    val angle by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cdAngle",
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val background = MaterialTheme.colorScheme.background

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.35f),
                            background,
                            secondary.copy(alpha = 0.2f),
                        ),
                    ),
                )
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MelodifySpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Text(
                        text = stringResource(R.string.player_now_playing),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.size(MelodifySpacing.xl))
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.xl))

                CoverArt(
                    imageUrl = song.coverImageUrl,
                    contentDescription = song.title,
                    size = MelodifySpacing.nowPlayingCover,
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(
                                key = PlayerSharedKeys.COVER,
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .clip(CircleShape)
                        .rotate(if (isPlaying) angle else 0f),
                )

                Spacer(modifier = Modifier.height(MelodifySpacing.xl))

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(MelodifySpacing.lg))

                AudioVisualizer(
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MelodifySpacing.xxl),
                )

                Spacer(modifier = Modifier.height(MelodifySpacing.md))

                val durationSafe = durationMs.coerceAtLeast(1L)
                var sliderPosition by remember(positionMs, durationMs) {
                    mutableFloatStateOf(positionMs.toFloat() / durationSafe.toFloat())
                }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = {
                        onSeek((sliderPosition * durationSafe).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatMs(positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatMs(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onSkipPrevious) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = stringResource(R.string.cd_previous_track),
                            modifier = Modifier.size(MelodifySpacing.xl),
                        )
                    }
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.cd_play_pause),
                            modifier = Modifier.size(MelodifySpacing.xxl),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onSkipNext) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = stringResource(R.string.cd_next_track),
                            modifier = Modifier.size(MelodifySpacing.xl),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val barColor = MaterialTheme.colorScheme.tertiary
    val transition = rememberInfiniteTransition(label = "viz")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "vizPhase",
    )

    Canvas(modifier = modifier) {
        val bars = 24
        val gap = size.width / bars
        val barWidth = gap * 0.55f
        for (i in 0 until bars) {
            val wave = if (isPlaying) {
                (0.25f + 0.75f * ((sin(phase + i * 0.45f) + 1f) / 2f))
            } else {
                0.2f
            }
            val barHeight = size.height * wave
            drawRoundRect(
                color = barColor,
                topLeft = Offset(i * gap, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = (ms / 1000L).toInt()
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return "%d:%02d".format(minutes, seconds)
}
