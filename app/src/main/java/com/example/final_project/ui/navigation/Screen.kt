package com.example.final_project.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.final_project.R

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    data object Home : Screen("home", R.string.tab_home, Icons.Filled.Home)
    data object Search : Screen("search", R.string.tab_search, Icons.Filled.Search)
    data object Downloads : Screen("downloads", R.string.tab_downloads, Icons.Filled.Download)
    data object Playlists : Screen("playlists", R.string.tab_playlists, Icons.AutoMirrored.Filled.PlaylistPlay)
    data object Profile : Screen("profile", R.string.tab_profile, Icons.Filled.Person)
    data object Settings : Screen("settings", R.string.settings_title, Icons.Filled.Person)

    companion object {
        val bottomNavItems = listOf(Home, Search, Downloads, Playlists, Profile)
    }
}
