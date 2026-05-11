package com.vaudibert.drivesoberly.di

import com.vaudibert.drivesoberly.data.repository.MainRepository
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
        viewModel { com.vaudibert.drivesoberly.ui.fragment.AddDrinkViewModel(get()) }
        viewModel { com.vaudibert.drivesoberly.ui.fragment.DrinkerViewModel(get()) }
        viewModel { com.vaudibert.drivesoberly.ui.fragment.DriveViewModel(get()) }
        viewModel { com.vaudibert.drivesoberly.ui.fragment.EditPresetViewModel(get()) }
    }
