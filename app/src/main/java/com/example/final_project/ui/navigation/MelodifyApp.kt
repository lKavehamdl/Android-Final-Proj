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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.player.PlayerController
import com.example.final_project.ui.components.MelodifyTopBar
import com.example.final_project.ui.player.MiniPlayer
import com.example.final_project.ui.player.NowPlayingScreen
import com.example.final_project.ui.screens.downloads.DownloadsScreen
import com.example.final_project.ui.screens.home.HomeScreen
import com.example.final_project.ui.screens.playlists.PlaylistsScreen
import com.example.final_project.ui.screens.profile.ProfileScreen
import com.example.final_project.ui.screens.search.SearchScreen
import com.example.final_project.ui.screens.settings.SettingsScreen
import com.example.final_project.ui.theme.ThemeMode
import org.koin.compose.koinInject

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MelodifyApp(
    themeMode: ThemeMode,
    language: AppLanguage,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playerController: PlayerController = koinInject()
    val playerState by playerController.state.collectAsStateWithLifecycle()

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
                        MelodifyTopBar(
                            onProfileClick = {
                                navController.navigateToTab(Screen.Profile.route)
                            },
                            onNotificationsClick = {},
                            onSettingsClick = {
                                navController.navigate(Screen.Settings.route)
                            },
                        )
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
                            onThemeModeChange = onThemeModeChange,
                            onLanguageChange = onLanguageChange,
                        )
                    }
                }
            }
        }
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
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            )
        }
        composable(Screen.Search.route) {
            SearchScreen()
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen()
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                themeMode = themeMode,
                language = language,
                onThemeModeChange = onThemeModeChange,
                onLanguageChange = onLanguageChange,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = { /* Will be handled in Phase 7 */ }
            )
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
