package com.vaudibert.canidrive.di

import com.vaudibert.canidrive.data.repository.DigestionRepository
import com.vaudibert.canidrive.data.repository.DrinkRepository
import com.vaudibert.canidrive.data.repository.DriveLawRepository
import com.vaudibert.canidrive.data.repository.MainRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Singletons for Repositories
    single { MainRepository(androidContext()) }
    single { get<MainRepository>().drinkRepository }
    single { get<MainRepository>().digestionRepository }
    single { get<MainRepository>().driveLawRepository }

    // ViewModels
    viewModel { com.vaudibert.canidrive.ui.fragment.AddDrinkViewModel(get()) }
    viewModel { com.vaudibert.canidrive.ui.fragment.DrinkerViewModel(get()) }
    viewModel { com.vaudibert.canidrive.ui.fragment.DriveViewModel(get()) }
    viewModel { com.vaudibert.canidrive.ui.fragment.EditPresetViewModel(get()) }
}
