package com.vaudibert.canidrive.ui.fragment

import androidx.lifecycle.ViewModel
import com.vaudibert.canidrive.data.repository.DrinkRepository
import com.vaudibert.canidrive.data.repository.MainRepository

class EditPresetViewModel(
    val mainRepository: MainRepository,
) : ViewModel() {
    val drinkRepository: DrinkRepository = mainRepository.drinkRepository
}
