package com.vaudibert.canidrive.ui.fragment

import androidx.lifecycle.ViewModel
import com.vaudibert.canidrive.data.repository.MainRepository

class DriveViewModel(
    val mainRepository: MainRepository
) : ViewModel() {
    val digestionRepository = mainRepository.digestionRepository
    val drinkRepository = mainRepository.drinkRepository
    val driveLawRepository = mainRepository.driveLawRepository
    val driveLawService = driveLawRepository.driveLawService
    val isInit = mainRepository.init
}
