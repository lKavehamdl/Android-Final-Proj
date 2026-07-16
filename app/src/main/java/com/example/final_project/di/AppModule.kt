package com.example.final_project.di

import com.example.final_project.core.dispatchers.DefaultDispatchersProvider
import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.ui.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DispatchersProvider> { DefaultDispatchersProvider() }
    viewModel { MainViewModel() }
}
