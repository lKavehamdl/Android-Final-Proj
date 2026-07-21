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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
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
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Song
import com.example.final_project.domain.repository.SongRepository
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
private const val SPEED_MIN = 0.5f
private const val SPEED_MAX = 2.0f
private const val SPEED_STEP = 0.25f
private const val SLEEP_MAX_MINUTES = 60f
private const val SLEEP_STEP_MINUTES = 15f

private fun formatSpeed(speed: Float): String {
    val rounded = (speed * 100).toInt() / 100f
    return if (rounded == rounded.toInt().toFloat()) {
        "${rounded.toInt()}x"
    } else {
        "${"%.2f".format(rounded).trimEnd('0').trimEnd('.')}x"
    }
}

private fun Float.snapTo(step: Float): Float = Math.round(this / step) * step

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
    audioSessionId: Int = 0,
    queue: List<Song> = emptyList(),
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit = {},
    onSleepTimerSet: (Int?) -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    onJumpToQueueItem: (Int) -> Unit = {},
    onRemoveFromQueue: (Int) -> Unit = {},
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onPlaySong: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
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
    var showQueue by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showQueue) {
        QueueSheet(
            queue = queue,
            currentSongId = song.id,
            accent = animatedPaletteColor,
            onDismiss = { showQueue = false },
            onJumpToQueueItem = onJumpToQueueItem,
            onRemoveFromQueue = onRemoveFromQueue,
            onMoveQueueItem = onMoveQueueItem,
            onPlaySong = onPlaySong,
            onAddToQueue = { s ->
                onAddToQueue(s)
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.player_added_to_queue)) }
            },
        )
    }

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
        PlayerAdjustSheet(
            title = stringResource(R.string.player_speed),
            accent = animatedPaletteColor,
            initialValue = playbackSpeed,
            range = SPEED_MIN..SPEED_MAX,
            step = SPEED_STEP,
            valueLabel = { formatSpeed(it) },
            onValueChange = { onSpeedChange(it) },
            onDismiss = { showSpeedDialog = false },
        )
    }

    if (showSleepDialog) {
        val offLabel = stringResource(R.string.player_sleep_timer_off)
        PlayerAdjustSheet(
            title = stringResource(R.string.player_sleep_timer),
            accent = animatedPaletteColor,
            initialValue = (sleepTimerMinutesLeft ?: 0).toFloat(),
            range = 0f..SLEEP_MAX_MINUTES,
            step = SLEEP_STEP_MINUTES,
            valueLabel = { if (it <= 0f) offLabel else "${it.toInt()} min" },
            onValueChange = { onSleepTimerSet(if (it <= 0f) null else it.toInt()) },
            onDismiss = { showSleepDialog = false },
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
                    IconButton(onClick = { showQueue = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = stringResource(R.string.player_queue),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
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

                // ── Title + artist (centered) ─────────────────────────────
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(MelodifySpacing.md))

                // ── Action row: like · download · playlist · share ─────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val downloadsRepo: DownloadsRepository = koinInject()
                    val settingsDataStore: com.example.final_project.core.datastore.SettingsDataStore = koinInject()
                    val isPremium by settingsDataStore.isPremium.collectAsStateWithLifecycle(initialValue = false)
                    val downloadedSongs by downloadsRepo.downloadedSongs.collectAsStateWithLifecycle(initialValue = emptyList())
                    val isDownloaded = downloadedSongs.any { it.song.id == song.id }
                    val shareSong = LocalShareSong.current

                    LikeButton(
                        songId = song.id,
                        tint = animatedPaletteColor,
                        unlikedTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (isDownloaded) {
                        IconButton(onClick = {}, enabled = false) {
                            Icon(Icons.Filled.DownloadDone, contentDescription = stringResource(R.string.player_download), tint = animatedPaletteColor)
                        }
                    } else {
                        IconButton(onClick = {
                            if (isPremium) {
                                downloadsRepo.startDownload(song)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.upgrade_needed_message),
                                    )
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = stringResource(R.string.player_download), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                }

                Spacer(modifier = Modifier.height(MelodifySpacing.md))

                // ── Audio Visualizer (Canvas) ──────────────────────────────
                AudioVisualizer(
                    isPlaying = isPlaying,
                    audioSessionId = audioSessionId,
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
                            tint = MaterialTheme.colorScheme.onBackground,
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
                            tint = MaterialTheme.colorScheme.onBackground,
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

                Spacer(modifier = Modifier.height(MelodifySpacing.lg))

                // ── Secondary controls (speed + sleep) ────────────────────
                // Plain text+icon buttons — no chip border or background.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    PlayerTextControl(
                        icon = Icons.Default.Speed,
                        label = formatSpeed(playbackSpeed),
                        active = playbackSpeed != 1f,
                        activeColor = animatedPaletteColor,
                        onClick = { showSpeedDialog = true },
                    )
                    PlayerTextControl(
                        icon = Icons.Default.Timer,
                        label = if (sleepTimerMinutesLeft != null)
                            stringResource(R.string.player_sleep_timer_set, sleepTimerMinutesLeft)
                        else
                            stringResource(R.string.player_sleep_timer),
                        active = sleepTimerMinutesLeft != null,
                        activeColor = animatedPaletteColor,
                        onClick = { showSleepDialog = true },
                    )
                }
            }
        }
        }
    }
}

// ── Speed / Sleep controls ───────────────────────────────────────────────────

/** Borderless, background-free icon+label button used for speed & sleep triggers. */
@Composable
private fun PlayerTextControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
) {
    val tint = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = MelodifySpacing.sm, vertical = MelodifySpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MelodifySpacing.xs),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}

// ── Speed / Sleep adjust sheet ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerAdjustSheet(
    title: String,
    accent: Color,
    initialValue: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    valueLabel: (Float) -> String,
    onValueChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableFloatStateOf(initialValue) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MelodifySpacing.lg)
                .padding(bottom = MelodifySpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(MelodifySpacing.sm))
            Text(
                text = valueLabel(draft),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Spacer(modifier = Modifier.height(MelodifySpacing.md))
            Slider(
                value = draft,
                onValueChange = { draft = it.snapTo(step) },
                onValueChangeFinished = { onValueChange(draft) },
                valueRange = range,
                steps = ((range.endInclusive - range.start) / step).toInt() - 1,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(valueLabel(range.start), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(valueLabel(range.endInclusive), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Audio Visualizer ─────────────────────────────────────────────────────────

@Composable
private fun AudioVisualizer(
    isPlaying: Boolean,
    audioSessionId: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val bars = 32
    val magnitudes = remember { mutableFloatStateArray(bars) }
    val frameTick = remember { mutableIntStateOf(0) }
    var fftActive by remember { mutableStateOf(false) }

    DisposableEffect(audioSessionId, isPlaying) {
        if (audioSessionId <= 0 || !isPlaying) {
            fftActive = false
            onDispose { }
        } else {
            var visualizer: android.media.audiofx.Visualizer? = null
            try {
                visualizer = android.media.audiofx.Visualizer(audioSessionId).apply {
                    captureSize = android.media.audiofx.Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(
                        object : android.media.audiofx.Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(
                                visualizer: android.media.audiofx.Visualizer?,
                                waveform: ByteArray?,
                                samplingRate: Int,
                            ) = Unit

                            override fun onFftDataCapture(
                                visualizer: android.media.audiofx.Visualizer?,
                                fft: ByteArray?,
                                samplingRate: Int,
                            ) {
                                if (fft == null || fft.size < 4) return
                                val n = bars.coerceAtMost(fft.size / 2)
                                for (i in 0 until n) {
                                    val re = fft.getOrNull(i * 2)?.toInt() ?: 0
                                    val im = fft.getOrNull(i * 2 + 1)?.toInt() ?: 0
                                    val mag = kotlin.math.sqrt((re * re + im * im).toDouble()).toFloat()
                                    magnitudes[i] = (mag / 128f).coerceIn(0.05f, 1f)
                                }
                                frameTick.intValue = frameTick.intValue + 1
                            }
                        },
                        android.media.audiofx.Visualizer.getMaxCaptureRate() / 2,
                        false,
                        true,
                    )
                    enabled = true
                }
                fftActive = true
            } catch (_: Exception) {
                visualizer?.release()
                visualizer = null
                fftActive = false
            }
            onDispose {
                fftActive = false
                try {
                    visualizer?.enabled = false
                    visualizer?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    // Fallback animation when the Visualizer is unavailable — genuinely random,
    // uncorrelated per-bar motion: every bar eases toward its own target and
    // occasionally re-rolls that target, so the row shimmers organically instead
    // of reading as one wave travelling across it.
    val simBars = remember { FloatArray(bars) { 0.2f } }
    val simTargets = remember { FloatArray(bars) { kotlin.random.Random.nextFloat() } }
    var simTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(isPlaying, fftActive) {
        if (fftActive || !isPlaying) return@LaunchedEffect
        while (true) {
            for (i in 0 until bars) {
                if (kotlin.random.Random.nextFloat() < 0.18f) {
                    simTargets[i] = 0.12f + kotlin.random.Random.nextFloat() * 0.88f
                }
                simBars[i] += (simTargets[i] - simBars[i]) * 0.28f
            }
            simTick++
            kotlinx.coroutines.delay(45)
        }
    }

    // Read ticks so Canvas recomposes when FFT / simulation updates.
    val tick = frameTick.intValue
    val simFrame = simTick

    Canvas(modifier = modifier) {
        @Suppress("UNUSED_EXPRESSION") simFrame
        val gap = size.width / bars
        val barWidth = gap * 0.55f
        for (i in 0 until bars) {
            val wave = if (fftActive && tick > 0) {
                magnitudes[i].coerceIn(0.08f, 1f)
            } else if (isPlaying) {
                simBars[i].coerceIn(0.08f, 1f)
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

private fun mutableFloatStateArray(size: Int): FloatArray = FloatArray(size) { 0.15f }

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = (ms / 1000L).toInt()
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return "%d:%02d".format(minutes, seconds)
}

// ── Queue + recommendations sheet ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(
    queue: List<Song>,
    currentSongId: String,
    accent: Color,
    onDismiss: () -> Unit,
    onJumpToQueueItem: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onPlaySong: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val songRepository: SongRepository = koinInject()

    val currentIndex = queue.indexOfFirst { it.id == currentSongId }
    val currentSong = queue.getOrNull(currentIndex)
    val upNextStart = if (currentIndex >= 0) currentIndex + 1 else 0
    val upNext = if (currentIndex >= 0) queue.drop(currentIndex + 1) else queue

    // A pool of candidates fetched per current song, from which we always surface
    // exactly 5. A candidate drops out when it enters the queue (added / autoplay)
    // or is dismissed (added to a playlist), and a fresh one takes its place. The
    // whole pool refreshes when the song changes.
    var recPool by remember(currentSongId) { mutableStateOf<List<Song>>(emptyList()) }
    var dismissedRecs by remember(currentSongId) { mutableStateOf<Set<String>>(emptySet()) }
    var recActionSong by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(currentSongId) {
        val seed = currentSong ?: return@LaunchedEffect
        when (val res = songRepository.getRecommendations(seed, 25)) {
            is Resource.Success -> recPool = res.data
            else -> Unit
        }
    }

    val queueIds = queue.mapTo(HashSet()) { it.id }
    val recommendations = recPool
        .filterNot { it.id == currentSongId || it.id in queueIds || it.id in dismissedRecs }
        .take(5)

    // Context sheet for a recommendation (Add to queue / Add to playlist). On
    // "consume" we dismiss it so the 5-slot list refills.
    recActionSong?.let { rec ->
        com.example.final_project.ui.components.SongActionSheet(
            song = rec,
            visible = true,
            onDismiss = { recActionSong = null },
            onConsumed = { dismissedRecs = dismissedRecs + rec.id },
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = MelodifySpacing.md,
                end = MelodifySpacing.md,
                bottom = MelodifySpacing.lg,
            ),
        ) {
            item {
                Text(
                    text = stringResource(R.string.player_queue),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = MelodifySpacing.sm),
                )
            }

            if (currentSong != null) {
                item { QueueSectionLabel(stringResource(R.string.player_now_playing_label)) }
                item {
                    QueueTrackRow(
                        song = currentSong,
                        highlight = true,
                        accent = accent,
                        onClick = {},
                        trailing = {},
                    )
                }
            }

            item { QueueSectionLabel(stringResource(R.string.player_up_next)) }
            if (upNext.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.player_queue_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = MelodifySpacing.sm),
                    )
                }
            } else {
                // Swipe a row away to remove it; long-press the handle to drag-reorder.
                item {
                    ReorderableUpNext(
                        songs = upNext,
                        startRealIndex = upNextStart,
                        accent = accent,
                        onJump = onJumpToQueueItem,
                        onRemove = onRemoveFromQueue,
                        onMove = onMoveQueueItem,
                    )
                }
            }

            if (recommendations.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = MelodifySpacing.sm))
                    QueueSectionLabel(stringResource(R.string.player_recommended))
                }
                items(recommendations, key = { "r_${it.id}" }) { s ->
                    QueueTrackRow(
                        song = s,
                        highlight = false,
                        accent = accent,
                        onClick = { onPlaySong(s) },
                        trailing = {
                            IconButton(onClick = { recActionSong = s }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.player_add_to_queue),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = MelodifySpacing.sm, bottom = MelodifySpacing.xs),
    )
}

@Composable
private fun QueueTrackRow(
    song: Song,
    highlight: Boolean,
    accent: Color,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MelodifySpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(
            imageUrl = song.coverImageUrl,
            contentDescription = song.title,
            size = 48.dp,
        )
        Spacer(modifier = Modifier.width(MelodifySpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
                color = if (highlight) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}

// ── Reorderable + swipeable up-next list ─────────────────────────────────────

private val QUEUE_ROW_HEIGHT = 64.dp

/**
 * Up-next list where each row can be swiped away to remove it, or drag-reordered
 * by long-pressing its handle. Reorder is done with a shared drag offset and
 * step-swaps against the live list (read via [rememberUpdatedState] so the gesture
 * never captures a stale index).
 */
@Composable
private fun ReorderableUpNext(
    songs: List<Song>,
    startRealIndex: Int,
    accent: Color,
    onJump: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
) {
    val density = LocalDensity.current
    val rowHeightPx = with(density) { QUEUE_ROW_HEIGHT.toPx() }
    val songsState = rememberUpdatedState(songs)
    val startState = rememberUpdatedState(startRealIndex)
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        songs.forEach { s ->
            key(s.id) {
                val isDragging = draggingId == s.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer { translationY = if (isDragging) dragOffset else 0f },
                ) {
                    SwipeableQueueRow(
                        song = s,
                        isDragging = isDragging,
                        onClick = {
                            val idx = songsState.value.indexOfFirst { it.id == s.id }
                            if (idx >= 0) onJump(startState.value + idx)
                        },
                        onRemove = {
                            val idx = songsState.value.indexOfFirst { it.id == s.id }
                            if (idx >= 0) onRemove(startState.value + idx)
                        },
                        dragModifier = Modifier.pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggingId = s.id; dragOffset = 0f },
                                onDragEnd = { draggingId = null; dragOffset = 0f },
                                onDragCancel = { draggingId = null; dragOffset = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y
                                    val list = songsState.value
                                    val cur = list.indexOfFirst { it.id == draggingId }
                                    if (cur >= 0) {
                                        val target = (cur + (dragOffset / rowHeightPx).roundToInt())
                                            .coerceIn(0, list.lastIndex)
                                        if (target != cur) {
                                            onMove(startState.value + cur, startState.value + target)
                                            dragOffset -= (target - cur) * rowHeightPx
                                        }
                                    }
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableQueueRow(
    song: Song,
    isDragging: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    dragModifier: Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onRemove()
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
                    contentDescription = stringResource(R.string.player_remove_from_queue),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(QUEUE_ROW_HEIGHT)
                .background(
                    if (isDragging) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface
                )
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(MelodifySpacing.xs))
            CoverArt(imageUrl = song.coverImageUrl, contentDescription = song.title, size = 44.dp)
            Spacer(modifier = Modifier.width(MelodifySpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = stringResource(R.string.player_reorder),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragModifier.padding(MelodifySpacing.md),
            )
        }
    }
}
