package com.johanpy.drivesoberly.di

import com.johanpy.drivesoberly.data.repository.MainRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        // Singletons for Repositories
        single { MainRepository(androidContext()) }
        single { get<MainRepository>().drinkRepository }
        single { get<MainRepository>().digestionRepository }
        single { get<MainRepository>().driveLawRepository }

        // ViewModels
        viewModel { com.johanpy.drivesoberly.ui.fragment.AddDrinkViewModel(get()) }
        viewModel { com.johanpy.drivesoberly.ui.fragment.DrinkerViewModel(get()) }
        viewModel { com.johanpy.drivesoberly.ui.fragment.DriveViewModel(get()) }
        viewModel { com.johanpy.drivesoberly.ui.fragment.EditPresetViewModel(get()) }
    }
