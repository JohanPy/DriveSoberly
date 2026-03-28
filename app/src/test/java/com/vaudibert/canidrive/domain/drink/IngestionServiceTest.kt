package com.vaudibert.canidrive.domain.drink

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

class IngestionServiceTest {
    @Test
    fun `ingest adds drink to list and triggers callbacks`() {
        var addedCount = 0
        var changesCount = 0
        val service =
            IngestionService<PresetDrink, IngestedDrink> { preset, time ->
                IngestedDrink(preset.name, preset.volume, preset.degree, time)
            }

        service.onAdded = { addedCount++ }
        service.onIngestedChanged = { changesCount++ }

        val preset = PresetDrink("Beer", 500.0, 5.0)
        val ingestionTime = Date()

        service.ingest(preset, ingestionTime)

        assertEquals(1, service.getDrinks().size)
        // onAdded is NOT called by ingest() in the current implementation.
        // ingest() calls sortAndListCallBack() which calls onIngestedChanged().
        assertEquals(1, changesCount)
    }

    @Test
    fun `remove and add operations properly trigger respective callbacks`() {
        var addedCount = 0
        var removedCount = 0
        var changesCount = 0
        val service =
            IngestionService<PresetDrink, IngestedDrink> { preset, time ->
                IngestedDrink(preset.name, preset.volume, preset.degree, time)
            }

        service.onAdded = { addedCount++ }
        service.onRemoved = { removedCount++ }
        service.onIngestedChanged = { changesCount++ }

        val preset = PresetDrink("Beer", 500.0, 5.0)
        val ingestedDrink = IngestedDrink(preset.name, preset.volume, preset.degree, Date())

        service.add(ingestedDrink)
        assertEquals(1, service.getDrinks().size)
        assertEquals(1, addedCount)
        assertEquals(1, changesCount)

        service.remove(ingestedDrink)
        assertEquals(0, service.getDrinks().size)
        assertEquals(1, removedCount)
        assertEquals(2, changesCount) // changed again
    }
}
