package com.example.final_project.di

import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.repository.FakeSongRepository
import com.example.final_project.data.repository.SongRepositoryImpl
import com.example.final_project.domain.repository.SongRepository
import com.example.final_project.domain.usecase.GetLocalSongsUseCase
import com.example.final_project.domain.usecase.GetNewReleasesUseCase
import com.example.final_project.domain.usecase.GetSongsUseCase
import com.example.final_project.domain.usecase.GetTrendingSongsUseCase
import org.koin.dsl.module

val repositoryModule = module {
    single<SongRepository> {
        if (AppConstants.USE_FAKE_SONGS) {
            FakeSongRepository(dispatchers = get())
        } else {
            SongRepositoryImpl(api = get(), dispatchers = get())
        }
    }

    factory { GetSongsUseCase(songRepository = get(), dispatchers = get()) }
    factory { GetTrendingSongsUseCase(songRepository = get(), dispatchers = get()) }
    factory { GetNewReleasesUseCase(songRepository = get(), dispatchers = get()) }
    factory { GetLocalSongsUseCase(songRepository = get(), dispatchers = get()) }
}
