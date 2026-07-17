package com.example.final_project.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Email
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
    data object LikedSongs : Screen("liked_songs", R.string.home_quick_liked, Icons.Filled.Person)
    data object RecentlyPlayed : Screen("recently_played", R.string.home_quick_recent, Icons.Filled.Person)
    data object Follows : Screen("follows", R.string.following_title, Icons.Filled.Person)
    data object TopArtists : Screen("top_artists", R.string.artists_title, Icons.Filled.Person)
    data object PlaylistDetail : Screen("playlist_detail/{playlistId}", R.string.tab_playlists, Icons.AutoMirrored.Filled.PlaylistPlay) {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }
    
    // Phase 6 Routes
    data object ChatList : Screen("chat_list", R.string.chat_title, Icons.Filled.Email)
    data object ChatConversation : Screen("chat_conversation/{userId}", R.string.chat_title, Icons.Filled.Person) {
        fun createRoute(userId: String) = "chat_conversation/$userId"
    }
    data object ExternalProfile : Screen("external_profile/{userId}", R.string.tab_profile, Icons.Filled.Person) {
        fun createRoute(userId: String) = "external_profile/$userId"
    }
    data object UserSearch : Screen("user_search", R.string.tab_search, Icons.Filled.Search)

    companion object {
        val bottomNavItems = listOf(Home, Search, Downloads, Playlists, ChatList)
    }
}
