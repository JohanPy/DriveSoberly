package com.vaudibert.canidrive.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ConvertersTest {

    @Test
    fun `fromTimestamp converts timestamp to correct Date`() {
        val timestamp = 1678615200000L // 2023-03-12 10:00:00 UTC
        val date = Converters.fromTimestamp(timestamp)
        assertEquals(timestamp, date.time)
    }

    @Test
    fun `dateToTimestamp converts Date to correct timestamp`() {
        val timestamp = 1678615200000L
        val date = Date(timestamp)
        val result = Converters.dateToTimestamp(date)
        assertEquals(timestamp, result)
    }
}
