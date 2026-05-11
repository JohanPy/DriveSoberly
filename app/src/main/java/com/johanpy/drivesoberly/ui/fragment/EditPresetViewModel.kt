package com.johanpy.drivesoberly.ui.fragment

import androidx.lifecycle.ViewModel
import com.johanpy.drivesoberly.data.repository.DrinkRepository
import com.johanpy.drivesoberly.data.repository.MainRepository

class EditPresetViewModel(
    val mainRepository: MainRepository,
) : ViewModel() {
    val drinkRepository: DrinkRepository = mainRepository.drinkRepository
}
