package com.vaudibert.drivesoberly.domain

import com.vaudibert.drivesoberly.domain.digestion.DigestionService
import com.vaudibert.drivesoberly.domain.drivelaw.DriveLawService
import java.util.*

class DrinkerStatusService(
    private val digestionService: DigestionService,
    private val driveLawService: DriveLawService,
) {
    fun status(): DrinkerStatus {
        val driveLimit = driveLawService.driveLimit()
        val projection = digestionService.projectionForLimit(driveLimit)
        val ratePresent = projection.currentRate

        val exceedsLimitInProjection = projection.exceedsLimit
        val driveDate = projection.returnBelowLimitTime ?: Date()

        return DrinkerStatus(
            !exceedsLimitInProjection,
            ratePresent,
            projection.peakRate,
            projection.peakTime,
            driveDate,
            projection.soberTime,
            exceedsLimitInProjection,
        )
    }
}
