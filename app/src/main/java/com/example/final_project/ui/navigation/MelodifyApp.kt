package com.example.final_project.ui.navigation

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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.ui.components.MelodifyTopBar
import com.example.final_project.ui.screens.downloads.DownloadsScreen
import com.example.final_project.ui.screens.home.HomeScreen
import com.example.final_project.ui.screens.playlists.PlaylistsScreen
import com.example.final_project.ui.screens.profile.ProfileScreen
import com.example.final_project.ui.screens.search.SearchScreen
import com.example.final_project.ui.theme.ThemeMode

/**
 * Root UI shell for Melodify.
 *
 * Step 1 foundation:
 * - [MelodifyTopBar] on all main tabs
 * - 5-tab bottom [NavigationBar] + [NavHost]
 * - [miniPlayer] slot reserved above the bottom nav (Step 2 wires this)
 */
@Composable
fun MelodifyApp(
    themeMode: ThemeMode,
    language: AppLanguage,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    miniPlayer: @Composable (() -> Unit)? = null,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MelodifyTopBar(
                onProfileClick = {
                    navController.navigateToTab(Screen.Profile.route)
                },
                onNotificationsClick = {
                    // Phase 6 / secondary — notifications screen not yet built
                },
                onSettingsClick = {
                    // Phase 7 — Settings route; Profile hosts temp theme/language for now
                    navController.navigateToTab(Screen.Profile.route)
                },
            )
        },
        bottomBar = {
            Column {
                // Step 2: MiniPlayer floats here above the nav bar
                miniPlayer?.invoke()
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
            HomeScreen()
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
            ProfileScreen(
                themeMode = themeMode,
                language = language,
                onThemeModeChange = onThemeModeChange,
                onLanguageChange = onLanguageChange,
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
