package com.example.final_project.ui.player

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.final_project.R
import com.example.final_project.domain.model.Song
import com.example.final_project.player.PlayerSharedKeys
import com.example.final_project.ui.components.CoverArt
import com.example.final_project.ui.components.LikeButton
import com.example.final_project.ui.theme.MelodifySpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.ui.navigation.LocalShareSong
import com.example.final_project.domain.repository.DownloadsRepository
private val SPEED_OPTIONS = listOf(1f, 1.5f, 2f)
private val SLEEP_TIMER_OPTIONS = listOf(null, 15, 30, 45, 60) // null = off

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    isBuffering: Boolean = false,
    positionMs: Long,
    durationMs: Long,
    playbackSpeed: Float = 1f,
    sleepTimerMinutesLeft: Int? = null,
    isShuffleOn: Boolean = false,
    repeatMode: Int = Player.REPEAT_MODE_OFF,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit = {},
    onSleepTimerSet: (Int?) -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    // ── Palette gradient from cover art ──────────────────────────────────────
    val context = LocalContext.current
    val defaultBg = MaterialTheme.colorScheme.background
    val defaultAccent = MaterialTheme.colorScheme.primary
    var paletteColor by remember(song.coverImageUrl) { mutableStateOf(defaultAccent) }

    LaunchedEffect(song.coverImageUrl) {
        val url = song.coverImageUrl ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val req = ImageRequest.Builder(context).data(url).allowHardware(false).build()
                val result = context.imageLoader.execute(req)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val swatch = Palette.from(bitmap).generate().dominantSwatch
                            ?: Palette.from(bitmap).generate().vibrantSwatch
                        swatch?.let { sw ->
                            paletteColor = Color(sw.rgb)
                        }
                    }
                }
            } catch (_: Exception) { /* fall back to theme color */ }
        }
    }

    val animatedPaletteColor by animateColorAsState(
        targetValue = paletteColor,
        animationSpec = tween(800),
        label = "paletteColor",
    )

    // ── Spinning CD animation ────────────────────────────────────────────────
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

    // ── Dialogs state ────────────────────────────────────────────────────────
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showAddToPlaylist) {
        AddToPlaylistSheet(
            songId = song.id,
            onDismiss = { showAddToPlaylist = false },
            onResult = { success, messageRes, messageText ->
                scope.launch {
                    val msg = messageRes?.let { context.getString(it) } ?: messageText
                    if (msg != null) snackbarHostState.showSnackbar(msg)
                }
            },
        )
    }

    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text(stringResource(R.string.player_speed)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(MelodifySpacing.sm)) {
                    SPEED_OPTIONS.forEach { speed ->
                        FilterChip(
                            selected = playbackSpeed == speed,
                            onClick = {
                                onSpeedChange(speed)
                                showSpeedDialog = false
                            },
                            label = {
                                Text(
                                    when (speed) {
                                        1.5f -> stringResource(R.string.player_speed_1_5x)
                                        2f -> stringResource(R.string.player_speed_2x)
                                        else -> stringResource(R.string.player_speed_normal)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }

    if (showSleepDialog) {
        AlertDialog(
            onDismissRequest = { showSleepDialog = false },
            title = { Text(stringResource(R.string.player_sleep_timer)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(MelodifySpacing.sm)) {
                    SLEEP_TIMER_OPTIONS.forEach { minutes ->
                        FilterChip(
                            selected = sleepTimerMinutesLeft == minutes,
                            onClick = {
                                onSleepTimerSet(minutes)
                                showSleepDialog = false
                            },
                            label = {
                                Text(
                                    if (minutes == null) stringResource(R.string.player_sleep_timer_off)
                                    else stringResource(R.string.player_sleep_timer_set, minutes)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepDialog = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }

    with(sharedTransitionScope) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
        ) { scaffoldPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedPaletteColor.copy(alpha = 0.55f),
                            defaultBg,
                            animatedPaletteColor.copy(alpha = 0.15f),
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

                // ── Top bar ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
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
                    // Sleep timer indicator badge
                    if (sleepTimerMinutesLeft != null) {
                        Text(
                            text = "💤 ${sleepTimerMinutesLeft}m",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Spacer(modifier = Modifier.size(MelodifySpacing.xl))
                    }
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.xl))

                // ── Spinning CD cover ─────────────────────────────────────
                Box(contentAlignment = Alignment.Center) {
                    CoverArt(
                        imageUrl = song.coverImageUrl,
                        contentDescription = song.title,
                        size = MelodifySpacing.nowPlayingCover,
                        modifier = Modifier
                            .sharedElement(
                                state = rememberSharedContentState(key = PlayerSharedKeys.COVER),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                            .clip(CircleShape)
                            .rotate(if (isPlaying) angle else 0f),
                    )
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(MelodifySpacing.nowPlayingCover + 8.dp),
                            color = animatedPaletteColor.copy(alpha = 0.6f),
                            strokeWidth = 3.dp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.xl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .sharedElement(
                                    state = rememberSharedContentState(key = PlayerSharedKeys.TITLE),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                        )
                        Text(
                            text = song.artistName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val downloadsRepo: DownloadsRepository = koinInject()
                        val downloadedSongs by downloadsRepo.downloadedSongs.collectAsStateWithLifecycle(initialValue = emptyList())
                        val isDownloaded = downloadedSongs.any { it.song.id == song.id }
                        
                        val shareSong = LocalShareSong.current
                        
                        if (isDownloaded) {
                            IconButton(onClick = {}, enabled = false) {
                                Icon(Icons.Filled.DownloadDone, contentDescription = "Downloaded", tint = animatedPaletteColor)
                            }
                        } else {
                            IconButton(onClick = { downloadsRepo.startDownload(song) }) {
                                Icon(Icons.Filled.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        IconButton(onClick = { showAddToPlaylist = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = stringResource(R.string.player_add_to_playlist),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        IconButton(onClick = { shareSong(song.id) }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.player_share), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        LikeButton(
                            songId = song.id,
                            tint = animatedPaletteColor,
                            unlikedTint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.lg))

                // ── Audio Visualizer (Canvas) ──────────────────────────────
                AudioVisualizer(
                    isPlaying = isPlaying,
                    accentColor = animatedPaletteColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MelodifySpacing.xxl),
                )

                Spacer(modifier = Modifier.height(MelodifySpacing.md))

                // ── Seek bar ──────────────────────────────────────────────
                val durationSafe = durationMs.coerceAtLeast(1L)
                var sliderPosition by remember(positionMs, durationMs) {
                    mutableFloatStateOf(positionMs.toFloat() / durationSafe.toFloat())
                }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = { onSeek((sliderPosition * durationSafe).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = animatedPaletteColor,
                        activeTrackColor = animatedPaletteColor,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(formatMs(positionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatMs(durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.lg))

                // ── Main controls ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Shuffle
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = stringResource(R.string.player_shuffle),
                            tint = if (isShuffleOn) animatedPaletteColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(MelodifySpacing.xl),
                        )
                    }
                    IconButton(onClick = onSkipPrevious) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = stringResource(R.string.cd_previous_track),
                            modifier = Modifier.size(MelodifySpacing.xl),
                        )
                    }
                    // Play/Pause with buffering
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = onPlayPause) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.cd_play_pause),
                                modifier = Modifier.size(MelodifySpacing.xxl),
                                tint = animatedPaletteColor,
                            )
                        }
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = animatedPaletteColor.copy(alpha = 0.5f),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                    IconButton(onClick = onSkipNext) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = stringResource(R.string.cd_next_track),
                            modifier = Modifier.size(MelodifySpacing.xl),
                        )
                    }
                    // Repeat
                    IconButton(onClick = onCycleRepeat) {
                        Icon(
                            imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = stringResource(R.string.player_repeat),
                            tint = if (repeatMode != Player.REPEAT_MODE_OFF) animatedPaletteColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(MelodifySpacing.xl),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.md))

                // ── Secondary controls (speed + sleep) ────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    FilterChip(
                        selected = playbackSpeed != 1f,
                        onClick = { showSpeedDialog = true },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = {
                            Text(
                                when (playbackSpeed) {
                                    1.5f -> stringResource(R.string.player_speed_1_5x)
                                    2f -> stringResource(R.string.player_speed_2x)
                                    else -> stringResource(R.string.player_speed_normal)
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = animatedPaletteColor.copy(alpha = 0.2f),
                        ),
                    )
                    FilterChip(
                        selected = sleepTimerMinutesLeft != null,
                        onClick = { showSleepDialog = true },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = {
                            Text(
                                if (sleepTimerMinutesLeft != null)
                                    stringResource(R.string.player_sleep_timer_set, sleepTimerMinutesLeft)
                                else
                                    stringResource(R.string.player_sleep_timer)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = animatedPaletteColor.copy(alpha = 0.2f),
                        ),
                    )
                }
            }
        }
        }
    }
}

// ── Audio Visualizer ─────────────────────────────────────────────────────────

@Composable
private fun AudioVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
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
        val bars = 32
        val gap = size.width / bars
        val barWidth = gap * 0.55f
        for (i in 0 until bars) {
            val wave = if (isPlaying) {
                0.2f + 0.8f * ((sin(phase.toDouble() + i * 0.45).toFloat() + 1f) / 2f)
            } else {
                0.15f
            }
            val barHeight = size.height * wave
            drawRoundRect(
                color = accentColor.copy(alpha = 0.85f),
                topLeft = Offset(i * gap, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = (ms / 1000L).toInt()
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return "%d:%02d".format(minutes, seconds)
}
