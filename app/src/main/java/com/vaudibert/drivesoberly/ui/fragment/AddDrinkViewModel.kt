package com.vaudibert.drivesoberly.ui.fragment

import androidx.lifecycle.ViewModel
import com.vaudibert.drivesoberly.data.repository.DrinkRepository
import com.vaudibert.drivesoberly.data.repository.MainRepository

class AddDrinkViewModel(
    val mainRepository: MainRepository,
) : ViewModel() {
    val drinkRepository: DrinkRepository = mainRepository.drinkRepository
}
