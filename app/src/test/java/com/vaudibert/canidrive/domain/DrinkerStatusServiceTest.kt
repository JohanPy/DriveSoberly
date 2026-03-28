package com.vaudibert.canidrive.domain

import com.vaudibert.canidrive.domain.digestion.DigestionService
import com.vaudibert.canidrive.domain.drivelaw.DriveLawService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

/**
 * Kotlin + Mockito fix: `any(Date::class.java)` registers an argument matcher but returns null.
 * The `?: Date(0)` fallback ensures Kotlin's non-null contract is satisfied at call-site,
 * while Mockito still captures the matcher correctly.
 */
private fun anyDate(): Date = any(Date::class.java) ?: Date(0)

class DrinkerStatusServiceTest {
    @Test
    fun `status returns can drive when alcohol rate is below limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(driveLawService.defaultLimit).thenReturn(0.0)
        `when`(digestionService.alcoholRateAt(anyDate())).thenReturn(0.4)
        `when`(digestionService.timeToReachLimit(anyDouble())).thenReturn(Date(1000L))

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertTrue(status.canDrive)
        assertEquals(0.4, status.alcoholRate, 0.01)
    }

    @Test
    fun `status returns cannot drive when alcohol rate is above limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(driveLawService.defaultLimit).thenReturn(0.0)
        `when`(digestionService.alcoholRateAt(anyDate())).thenReturn(0.8)
        `when`(digestionService.timeToReachLimit(anyDouble())).thenReturn(Date(1000L))

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertFalse(status.canDrive)
        assertEquals(0.8, status.alcoholRate, 0.01)
    }

    @Test
    fun `status returns can drive when alcohol rate equals limit`() {
        val digestionService = mock(DigestionService::class.java)
        val driveLawService = mock(DriveLawService::class.java)

        `when`(driveLawService.driveLimit()).thenReturn(0.5)
        `when`(driveLawService.defaultLimit).thenReturn(0.0)
        `when`(digestionService.alcoholRateAt(anyDate())).thenReturn(0.5)
        `when`(digestionService.timeToReachLimit(anyDouble())).thenReturn(Date(1000L))

        val service = DrinkerStatusService(digestionService, driveLawService)
        val status = service.status()

        assertTrue(status.canDrive)
        assertEquals(0.5, status.alcoholRate, 0.01)
    }
}
