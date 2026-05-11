package com.johanpy.drivesoberly.ui.fragment

import androidx.lifecycle.ViewModel
import com.johanpy.drivesoberly.data.repository.MainRepository
import com.johanpy.drivesoberly.domain.digestion.FoodState

class DriveViewModel(
    val mainRepository: MainRepository,
) : ViewModel() {
    val digestionRepository = mainRepository.digestionRepository
    val drinkRepository = mainRepository.drinkRepository
    val driveLawRepository = mainRepository.driveLawRepository
    val driveLawService = driveLawRepository.driveLawService
    val isInit = mainRepository.init

    fun updateFoodState(state: FoodState) {
        digestionRepository.body.foodState = state
    }
}
