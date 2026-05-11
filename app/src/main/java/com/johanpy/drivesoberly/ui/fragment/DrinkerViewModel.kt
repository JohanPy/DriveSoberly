package com.johanpy.drivesoberly.ui.fragment

import androidx.lifecycle.ViewModel
import com.johanpy.drivesoberly.data.repository.DigestionRepository
import com.johanpy.drivesoberly.data.repository.DriveLawRepository
import com.johanpy.drivesoberly.data.repository.MainRepository

class DrinkerViewModel(
    val mainRepository: MainRepository,
) : ViewModel() {
    val digestionRepository: DigestionRepository = mainRepository.digestionRepository
    val driveLawRepository: DriveLawRepository = mainRepository.driveLawRepository
    val driveLawService = driveLawRepository.driveLawService

    fun updateTolerance(
        progress: Int,
        levelCount: Int,
    ) {
        val count = levelCount.coerceAtLeast(1)
        digestionRepository.body.alcoholTolerance = progress.toDouble() / count.toDouble()
    }
}
