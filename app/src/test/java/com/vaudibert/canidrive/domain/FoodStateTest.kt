package com.vaudibert.canidrive.domain

import com.vaudibert.canidrive.domain.digestion.FoodState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FoodStateTest {

    @Test
    fun `fromString returns EMPTY for unknown values`() {
        assertEquals(FoodState.EMPTY, FoodState.fromString(""))
        assertEquals(FoodState.EMPTY, FoodState.fromString("unknown"))
        assertEquals(FoodState.EMPTY, FoodState.fromString("EMPTY"))
    }

    @Test
    fun `fromString is case-insensitive`() {
        assertEquals(FoodState.LIGHT_MEAL, FoodState.fromString("light_meal"))
        assertEquals(FoodState.LIGHT_MEAL, FoodState.fromString("LIGHT_MEAL"))
        assertEquals(FoodState.FULL_MEAL, FoodState.fromString("full_meal"))
        assertEquals(FoodState.FULL_MEAL, FoodState.fromString("FULL_MEAL"))
    }

    @Test
    fun `fromString round-trips for all values`() {
        FoodState.entries.forEach { state ->
            assertEquals(state, FoodState.fromString(state.name))
        }
    }

    @Test
    fun `absorption rate decreases from EMPTY to FULL_MEAL`() {
        assertTrue(FoodState.EMPTY.absorptionRate > FoodState.LIGHT_MEAL.absorptionRate)
        assertTrue(FoodState.LIGHT_MEAL.absorptionRate > FoodState.FULL_MEAL.absorptionRate)
    }

    @Test
    fun `absorption delay increases from EMPTY to FULL_MEAL`() {
        assertTrue(FoodState.EMPTY.absorptionDelay < FoodState.LIGHT_MEAL.absorptionDelay)
        assertTrue(FoodState.LIGHT_MEAL.absorptionDelay < FoodState.FULL_MEAL.absorptionDelay)
    }

    @Test
    fun `EMPTY parameters match calibration values`() {
        assertEquals(2.0, FoodState.EMPTY.absorptionRate)
        assertEquals(0.25, FoodState.EMPTY.absorptionDelay)
    }

    @Test
    fun `FULL_MEAL parameters match calibration values`() {
        assertEquals(0.6, FoodState.FULL_MEAL.absorptionRate)
        assertEquals(0.75, FoodState.FULL_MEAL.absorptionDelay)
    }
}
