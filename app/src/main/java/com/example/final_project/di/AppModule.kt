package com.example.final_project.di

import com.example.final_project.core.dispatchers.DefaultDispatchersProvider
import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.player.PlayerController
import com.example.final_project.ui.MainViewModel
import com.example.final_project.ui.screens.home.HomeViewModel
import com.example.final_project.ui.screens.playlists.PlaylistsViewModel
import com.example.final_project.ui.screens.playlists.PlaylistDetailViewModel
import com.example.final_project.ui.screens.profile.ProfileViewModel
import com.example.final_project.ui.screens.profile.follows.FollowsViewModel
import com.example.final_project.ui.screens.home.liked.LikedSongsViewModel
import com.example.final_project.ui.screens.home.recent.RecentlyPlayedViewModel
import com.example.final_project.ui.screens.downloads.DownloadsViewModel
import com.example.final_project.ui.screens.social.ExternalProfileViewModel
import com.example.final_project.ui.screens.social.UserSearchViewModel
import com.example.final_project.ui.screens.chat.ChatListViewModel
import com.example.final_project.ui.screens.chat.ChatConversationViewModel
import com.example.final_project.ui.screens.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DispatchersProvider> { DefaultDispatchersProvider() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single { com.example.final_project.core.datastore.SettingsDataStore(androidContext()) }
    single {
        PlayerController(
            context = androidContext(),
            scope = get(),
            downloadsRepository = get(),
            songRepository = get(),
        )
    }
    viewModel { MainViewModel(settingsDataStore = get()) }
    viewModel {
        HomeViewModel(
            getSongsUseCase = get(),
            getTrendingSongsUseCase = get(),
            getNewReleasesUseCase = get(),
            getLocalSongsUseCase = get(),
            playerController = get(),
            settingsDataStore = get(),
            downloadsRepository = get(),
        )
    }
    viewModel { PlaylistsViewModel(get(), get(), get(), get()) }
    viewModel { PlaylistDetailViewModel(get(), get(), get()) }
    single {
        com.example.final_project.domain.favorites.FavoritesStore(
            songRepository = get(),
            dispatchers = get(),
            appScope = get(),
        )
    }
    viewModel { LikedSongsViewModel(get(), get()) }
    viewModel { RecentlyPlayedViewModel(get(), get()) }
    viewModel { com.example.final_project.ui.screens.home.artists.TopArtistsViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { FollowsViewModel(get()) }
    viewModel { DownloadsViewModel(get()) }
    
    viewModel { ExternalProfileViewModel(get(), get()) }
    viewModel { UserSearchViewModel(get()) }
    viewModel { ChatListViewModel(get(), get(), get()) }
    viewModel { ChatConversationViewModel(get(), get(), get(), get()) }
    viewModel { AuthViewModel(get(), get()) }
}
