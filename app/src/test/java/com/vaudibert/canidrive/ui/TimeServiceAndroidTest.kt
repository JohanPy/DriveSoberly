package com.vaudibert.canidrive.ui

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Calendar

class TimeServiceAndroidTest {
    private fun getServiceForDate(
        year: Int,
        month: Int,
        day: Int,
    ): TimeServiceAndroid {
        return TimeServiceAndroid(calendarProvider = {
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            cal
        })
    }

    @Test
    fun `isSaintPatrick returns true on precisely March 17th`() {
        val timeService = getServiceForDate(2023, Calendar.MARCH, 17)
        assertTrue(timeService.isSaintPatrick())
    }

    @Test
    fun `isSaintPatrick returns true on surrounding days`() {
        val service16 = getServiceForDate(2023, Calendar.MARCH, 16)
        assertTrue(service16.isSaintPatrick())

        val service18 = getServiceForDate(2023, Calendar.MARCH, 18)
        assertTrue(service18.isSaintPatrick())
    }

    @Test
    fun `isSaintPatrick returns false on other days of the year`() {
        // Not St. Patrick
        assertFalse(getServiceForDate(2023, Calendar.FEBRUARY, 17).isSaintPatrick())
        assertFalse(getServiceForDate(2023, Calendar.MARCH, 15).isSaintPatrick())
        assertFalse(getServiceForDate(2023, Calendar.MARCH, 19).isSaintPatrick())
        assertFalse(getServiceForDate(2024, Calendar.DECEMBER, 25).isSaintPatrick())
    }
}
