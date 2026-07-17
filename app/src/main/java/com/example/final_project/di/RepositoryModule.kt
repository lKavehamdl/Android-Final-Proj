package com.example.final_project.di

import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.repository.FakeSongRepository
import com.example.final_project.data.repository.SongRepositoryImpl
import com.example.final_project.domain.repository.DownloadsRepository
import com.example.final_project.domain.repository.SongRepository

import com.example.final_project.domain.usecase.GetLocalSongsUseCase
import com.example.final_project.domain.usecase.GetNewReleasesUseCase
import com.example.final_project.domain.usecase.GetSongsUseCase
import com.example.final_project.domain.usecase.GetTrendingSongsUseCase
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import com.example.final_project.data.repository.FakePlaylistRepository
import com.example.final_project.data.repository.PlaylistRepositoryImpl
import com.example.final_project.domain.repository.PlaylistRepository
import com.example.final_project.domain.usecase.GetPublicPlaylistsUseCase
import com.example.final_project.domain.usecase.GetUserPlaylistsUseCase
import com.example.final_project.data.repository.ChatRepositoryImpl
import com.example.final_project.domain.repository.ChatRepository
import com.example.final_project.data.repository.SocialRepositoryImpl
import com.example.final_project.domain.repository.SocialRepository

val repositoryModule = module {
    single<SongRepository> {
        if (AppConstants.USE_FAKE_SONGS) {
            FakeSongRepository(dispatchers = get())
        } else {
            SongRepositoryImpl(api = get(), dispatchers = get(), supabase = get())
        }
    }

    factory { GetSongsUseCase(songRepository = get(), dispatchers = get()) }
    factory { GetTrendingSongsUseCase(songRepository = get(), dispatchers = get()) }
    factory { GetNewReleasesUseCase(songRepository = get(), dispatchers = get()) }
    factory { GetLocalSongsUseCase(songRepository = get(), dispatchers = get()) }

    // Playlists
    single<PlaylistRepository> {
        if (AppConstants.USE_FAKE_SONGS) {
            FakePlaylistRepository(dispatchers = get())
        } else {
            PlaylistRepositoryImpl(api = get(), dispatchers = get(), supabase = get())
        }
    }
    factory { GetPublicPlaylistsUseCase(playlistRepository = get()) }
    factory { GetUserPlaylistsUseCase(playlistRepository = get()) }

    single {
        com.example.final_project.domain.repository.DownloadsRepository(
            context = androidContext(),
            downloadsDao = get()
        )
    }

    single<ChatRepository> {
        ChatRepositoryImpl(
            supabaseClient = get(),
            messagesDao = get(),
            dispatchers = get(),
            appScope = get()
        )
    }

    single<SocialRepository> {
        SocialRepositoryImpl(
            supabase = get()
        )
    }
}
