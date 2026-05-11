package com.johanpy.drivesoberly.domain

import com.johanpy.drivesoberly.domain.digestion.DigestionService
import com.johanpy.drivesoberly.domain.drivelaw.DriveLawService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

private fun anyDate(): Date = any(Date::class.java) ?: Date(0)

class DrinkerStatusServiceTest {
    @Test
    fun `status returns can drive when alcohol rate is below limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)
        val now = Date(1_000L)
        val projection =
            DigestionService.BacProjection(
                currentRate = 0.4,
                peakRate = 0.4,
                peakTime = now,
                exceedsLimit = false,
                returnBelowLimitTime = null,
                soberTime = Date(2_000L),
            )

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(digestionService.projectionForLimit(anyDouble(), anyDate())).thenReturn(projection)

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertTrue(status.canDrive)
        assertEquals(0.4, status.alcoholRate, 0.01)
    }

    @Test
    fun `status returns cannot drive when alcohol rate is above limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)
        val now = Date(1_000L)
        val projection =
            DigestionService.BacProjection(
                currentRate = 0.8,
                peakRate = 0.8,
                peakTime = now,
                exceedsLimit = true,
                returnBelowLimitTime = Date(3_000L),
                soberTime = Date(5_000L),
            )

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(digestionService.projectionForLimit(anyDouble(), anyDate())).thenReturn(projection)

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertFalse(status.canDrive)
        assertEquals(0.8, status.alcoholRate, 0.01)
    }

    @Test
    fun `status returns can drive when alcohol rate equals limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)
        val now = Date(1_000L)
        val projection =
            DigestionService.BacProjection(
                currentRate = 0.5,
                peakRate = 0.5,
                peakTime = now,
                exceedsLimit = false,
                returnBelowLimitTime = null,
                soberTime = Date(3_000L),
            )

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(digestionService.projectionForLimit(anyDouble(), anyDate())).thenReturn(projection)

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertTrue(status.canDrive)
        assertEquals(0.5, status.alcoholRate, 0.01)
    }
}
