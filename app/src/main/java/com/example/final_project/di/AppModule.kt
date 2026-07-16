package com.example.final_project.di

import com.example.final_project.core.dispatchers.DefaultDispatchersProvider
import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.player.PlayerController
import com.example.final_project.ui.MainViewModel
import com.example.final_project.ui.screens.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DispatchersProvider> { DefaultDispatchersProvider() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single {
        PlayerController(
            context = androidContext(),
            scope = get(),
        )
    }
    viewModel { MainViewModel() }
    viewModel {
        HomeViewModel(
            getSongsUseCase = get(),
            getTrendingSongsUseCase = get(),
            getNewReleasesUseCase = get(),
            getLocalSongsUseCase = get(),
            playerController = get(),
        )
    }
    viewModel { PlaylistsViewModel(get(), get()) }
}
