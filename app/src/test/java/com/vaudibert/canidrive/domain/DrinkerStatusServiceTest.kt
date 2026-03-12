package com.vaudibert.canidrive.domain

import com.vaudibert.canidrive.domain.digestion.DigestionService
import com.vaudibert.canidrive.domain.drivelaw.DriveLawService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

class DrinkerStatusServiceTest {

    @Test
    fun `status returns can drive when alcohol rate is below limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(driveLawService.defaultLimit).thenReturn(0.0)
        `when`(digestionService.alcoholRateAt(any(Date::class.java))).thenReturn(0.4)
        `when`(digestionService.timeToReachLimit(anyDouble())).thenReturn(Date(1000L))

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertTrue(status.isSafeToDrive)
        assertEquals(0.4, status.alcoholRate, 0.01)
    }

    @Test
    fun `status returns cannot drive when alcohol rate is above limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(driveLawService.defaultLimit).thenReturn(0.0)
        `when`(digestionService.alcoholRateAt(any(Date::class.java))).thenReturn(0.8)
        `when`(digestionService.timeToReachLimit(anyDouble())).thenReturn(Date(1000L))

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertFalse(status.isSafeToDrive)
        assertEquals(0.8, status.alcoholRate, 0.01)
    }

    @Test
    fun `status returns can drive when alcohol rate equals limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(driveLawService.defaultLimit).thenReturn(0.0)
        `when`(digestionService.alcoholRateAt(any(Date::class.java))).thenReturn(0.5)
        `when`(digestionService.timeToReachLimit(anyDouble())).thenReturn(Date(1000L))

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertTrue(status.isSafeToDrive)
        assertEquals(0.5, status.alcoholRate, 0.01)
    }
}
