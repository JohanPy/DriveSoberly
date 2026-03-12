package com.vaudibert.canidrive.domain.drink

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class IngestedDrinkTest {
    @Test
    fun `alcoholMass is correctly calculated`() {
        // formula: degree/100 * volume * ALCOHOL_DENSITY(0.8)

        // 500ml of 5% beer -> 0.05 * 500 * 0.8 = 20g
        val beer = IngestedDrink("Beer", 500.0, 5.0, Date())
        assertEquals(20.0, beer.alcoholMass(), 0.01)

        // 130ml of 12% wine -> 0.12 * 130 * 0.8 = 12.48g
        val wine = IngestedDrink("Wine", 130.0, 12.0, Date())
        assertEquals(12.48, wine.alcoholMass(), 0.01)

        // 30ml of 40% strong alcohol -> 0.4 * 30 * 0.8 = 9.6g
        val strong = IngestedDrink("Strong", 30.0, 40.0, Date())
        assertEquals(9.6, strong.alcoholMass(), 0.01)
    }
}
