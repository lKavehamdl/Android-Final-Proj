package com.example.final_project.di

import com.example.final_project.data.repository.SongRepositoryImpl
import com.example.final_project.domain.repository.SongRepository
import com.example.final_project.domain.usecase.GetSongsUseCase
import org.koin.dsl.module

val repositoryModule = module {
    single<SongRepository> { SongRepositoryImpl() }
    factory { GetSongsUseCase(get(), get()) }
}
