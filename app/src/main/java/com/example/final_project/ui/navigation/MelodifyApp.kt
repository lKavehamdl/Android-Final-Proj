package com.example.final_project.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.final_project.core.datastore.FontScale
import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.domain.repository.AuthRepository
import com.example.final_project.player.PlayerController
import com.example.final_project.ui.components.MelodifyTopBar
import com.example.final_project.ui.player.MiniPlayer
import com.example.final_project.ui.player.NowPlayingScreen
import com.example.final_project.ui.screens.downloads.DownloadsScreen
import com.example.final_project.ui.screens.home.HomeScreen
import com.example.final_project.ui.screens.playlists.PlaylistsScreen
import com.example.final_project.ui.screens.profile.ProfileScreen
import com.example.final_project.ui.screens.search.SearchScreen
import com.example.final_project.ui.screens.chat.ChatListScreen
import com.example.final_project.ui.screens.home.liked.LikedSongsScreen
import com.example.final_project.ui.screens.home.recent.RecentlyPlayedScreen
import com.example.final_project.ui.screens.profile.follows.FollowsScreen
import com.example.final_project.ui.screens.settings.SettingsScreen
import com.example.final_project.ui.screens.auth.AuthScreen
import com.example.final_project.ui.theme.ThemeMode
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

val LocalShareSong = compositionLocalOf<(String) -> Unit> { error("No ShareSong provided") }

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MelodifyApp(
    themeMode: ThemeMode,
    language: AppLanguage,
    fontScale: FontScale,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFontScaleChange: (FontScale) -> Unit,
    modifier: Modifier = Modifier,
) {
    var songToShareId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playerController: PlayerController = koinInject()
    val playerState by playerController.state.collectAsStateWithLifecycle()
    
    val supabaseClient: SupabaseClient = koinInject()
    val sessionStatus by supabaseClient.auth.sessionStatus.collectAsStateWithLifecycle(
        initialValue = SessionStatus.Initializing,
    )

    when (sessionStatus) {
        SessionStatus.Initializing,
        is SessionStatus.RefreshFailure,
        -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return
        }
        is SessionStatus.NotAuthenticated -> {
            AuthScreen()
            return
        }
        is SessionStatus.Authenticated -> Unit
    }

    CompositionLocalProvider(
        LocalShareSong provides { songId -> songToShareId = songId }
    ) {
        SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = playerState.isExpanded && playerState.currentSong != null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "playerExpand",
        ) { expanded ->
            if (expanded && playerState.currentSong != null) {
                BackHandler { playerController.setExpanded(false) }
                NowPlayingScreen(
                    song = playerState.currentSong!!,
                    isPlaying = playerState.isPlaying,
                    isBuffering = playerState.isBuffering,
                    positionMs = playerState.positionMs,
                    durationMs = playerState.durationMs,
                    playbackSpeed = playerState.playbackSpeed,
                    sleepTimerMinutesLeft = playerState.sleepTimerMinutesLeft,
                    isShuffleOn = playerState.isShuffleOn,
                    repeatMode = playerState.repeatMode,
                    audioSessionId = playerState.audioSessionId,
                    onCollapse = { playerController.setExpanded(false) },
                    onPlayPause = playerController::togglePlayPause,
                    onSkipNext = playerController::skipNext,
                    onSkipPrevious = playerController::skipPrevious,
                    onSeek = playerController::seekTo,
                    onSpeedChange = playerController::setPlaybackSpeed,
                    onSleepTimerSet = playerController::setSleepTimer,
                    onToggleShuffle = playerController::toggleShuffle,
                    onCycleRepeat = playerController::cycleRepeatMode,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val showAppTopBar = currentRoute in ROOT_TABS_WITH_APP_BAR
                        if (showAppTopBar) {
                            MelodifyTopBar(
                                onProfileClick = {
                                    navController.navigateToTab(Screen.Profile.route)
                                },
                                onNotificationsClick = {
                                    navController.navigate(Screen.Notifications.route)
                                },
                                onSettingsClick = {
                                    navController.navigate(Screen.Settings.route)
                                },
                            )
                        }
                    },
                    bottomBar = {
                        Column {
                            val song = playerState.currentSong
                            if (song != null) {
                                MiniPlayer(
                                    song = song,
                                    isPlaying = playerState.isPlaying,
                                    positionMs = playerState.positionMs,
                                    durationMs = playerState.durationMs,
                                    onExpand = { playerController.setExpanded(true) },
                                    onPlayPause = playerController::togglePlayPause,
                                    onSkipNext = playerController::skipNext,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedContent,
                                )
                            }
                            MelodifyBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route -> navController.navigateToTab(route) },
                            )
                        }
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        MelodifyNavHost(
                            navController = navController,
                            themeMode = themeMode,
                            language = language,
                            fontScale = fontScale,
                            onThemeModeChange = onThemeModeChange,
                            onLanguageChange = onLanguageChange,
                            onFontScaleChange = onFontScaleChange,
                        )
                    }
                }
            }
        }
    }
    }
    
    if (songToShareId != null) {
        com.example.final_project.ui.screens.social.ShareSongBottomSheet(
            songId = songToShareId!!,
            onDismissRequest = { songToShareId = null }
        )
    }
}

@Composable
private fun MelodifyBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        Screen.bottomNavItems.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = stringResource(screen.titleRes),
                    )
                },
                label = { Text(stringResource(screen.titleRes)) },
            )
        }
    }
}

@Composable
private fun MelodifyNavHost(
    navController: NavHostController,
    themeMode: ThemeMode,
    language: AppLanguage,
    fontScale: FontScale,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFontScaleChange: (FontScale) -> Unit,
    modifier: Modifier = Modifier,
) {
    val authRepository: AuthRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onOpenPlaylistsTab = {
                    navController.navigateToTab(Screen.Playlists.route)
                },
                onNavigateToLiked = {
                    navController.navigate(Screen.LikedSongs.route)
                },
                onNavigateToRecent = {
                    navController.navigate(Screen.RecentlyPlayed.route)
                },
                onNavigateToArtists = {
                    navController.navigate(Screen.TopArtists.route)
                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen()
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen()
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onOpenPlaylist = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(androidx.navigation.navArgument("playlistId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            com.example.final_project.ui.screens.playlists.PlaylistDetailScreen(
                playlistId = playlistId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToFollows = {
                    navController.navigate(Screen.Follows.route)
                },
                onNavigateToLiked = {
                    navController.navigate(Screen.LikedSongs.route)
                },
                onNavigateToMessages = {
                    navController.navigate(Screen.ChatList.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                themeMode = themeMode,
                language = language,
                fontScale = fontScale,
                onThemeModeChange = onThemeModeChange,
                onLanguageChange = onLanguageChange,
                onFontScaleChange = onFontScaleChange,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    scope.launch {
                        authRepository.signOut()
                    }
                }
            )
        }
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.RecentlyPlayed.route) {
            RecentlyPlayedScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.TopArtists.route) {
            com.example.final_project.ui.screens.home.artists.TopArtistsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Follows.route) {
            FollowsScreen(
                onNavigateBack = { navController.popBackStack() },
                onUserClick = { userId ->
                    navController.navigate(Screen.ExternalProfile.createRoute(userId))
                }
            )
        }
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onChatClick = { userId ->
                    navController.navigate(Screen.ChatConversation.createRoute(userId))
                },
                onSearchClick = {
                    navController.navigate(Screen.UserSearch.route)
                },
            )
        }
        composable(Screen.Notifications.route) {
            com.example.final_project.ui.screens.notifications.NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.ChatConversation.route,
            arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            com.example.final_project.ui.screens.chat.ChatConversationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ExternalProfile.route,
            arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            com.example.final_project.ui.screens.social.ExternalProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onMessageClick = {
                    navController.navigate(Screen.ChatConversation.createRoute(userId))
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
            )
        }
        composable(Screen.UserSearch.route) {
            com.example.final_project.ui.screens.social.UserSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onUserClick = { userId ->
                    navController.navigate(Screen.ExternalProfile.createRoute(userId))
                }
            )
        }
    }
}

private val ROOT_TABS_WITH_APP_BAR = setOf(
    Screen.Home.route,
    Screen.Search.route,
    Screen.Downloads.route,
    Screen.Playlists.route,
    Screen.Profile.route,
)

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
