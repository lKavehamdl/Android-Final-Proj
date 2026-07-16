package com.example.final_project.ui.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.final_project.core.locale.AppLanguage
import com.example.final_project.ui.screens.downloads.DownloadsScreen
import com.example.final_project.ui.screens.home.HomeScreen
import com.example.final_project.ui.screens.playlists.PlaylistsScreen
import com.example.final_project.ui.screens.profile.ProfileScreen
import com.example.final_project.ui.screens.search.SearchScreen
import com.example.final_project.ui.theme.ThemeMode

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

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
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
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
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
}
