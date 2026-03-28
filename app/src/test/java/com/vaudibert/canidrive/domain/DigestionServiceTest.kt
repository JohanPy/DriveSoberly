package com.vaudibert.canidrive.domain

import com.vaudibert.canidrive.domain.digestion.DigestionService
import com.vaudibert.canidrive.domain.digestion.FoodState
import com.vaudibert.canidrive.domain.digestion.PhysicalBody
import com.vaudibert.canidrive.domain.digestion.Sex
import com.vaudibert.canidrive.domain.drink.IngestedDrink
import com.vaudibert.canidrive.domain.drink.IngestionService
import com.vaudibert.canidrive.domain.drink.PresetDrink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class DigestionServiceTest {
    /** Tolerance for g/L comparisons that are expected to be exact (linear-phase tests). */
    private val precision = 0.001

    private lateinit var body: PhysicalBody
    private lateinit var ingestionService: IngestionService<PresetDrink, IngestedDrink>
    private lateinit var digestionService: DigestionService

    private fun ingestBeer(at: Date) {
        ingestionService.ingest(PresetDrink("beer", 500.0, 5.0), at)
    }

    /** Returns milliseconds offset from epoch as a [Date]. */
    private fun minutesLater(minutes: Long): Date = Date(Date().time + minutes * 60_000L)
    private fun hoursLater(hours: Double): Date = Date(Date().time + (hours * 3_600_000).toLong())

    @BeforeEach
    fun before() {
        body = PhysicalBody()
        body.sex = Sex.MALE
        body.weight = 100.0
        // Default food state is EMPTY (absorptionRate=2.0, absorptionDelay=0.25h)
        ingestionService =
            IngestionService { preset: PresetDrink, ingestionTime: Date ->
                IngestedDrink(preset.name, preset.volume, preset.degree, ingestionTime)
            }
        digestionService = DigestionService(body, ingestionService)
    }

    // ── Basic sanity ──────────────────────────────────────────────────────────

    @Test
    fun `Sober user has zero alcohol`() {
        assertEquals(0.0, digestionService.alcoholRateAt(Date()))
    }

    @Test
    fun `User that drank is no longer sober after absorption lag`() {
        val now = Date()
        ingestBeer(now)
        // Check at 30 min: past the 15-min lag of EMPTY stomach, absorption well underway.
        val thirtyMinLater = Date(now.time + 30 * 60_000L)
        assertTrue(digestionService.alcoholRateAt(thirtyMinLater) > 0.0)
    }

    @Test
    fun `BAC is zero during absorption lag`() {
        val now = Date()
        ingestBeer(now)
        // 1 second after ingestion: still within 15-min lag window → no alcohol in blood yet.
        assertEquals(0.0, digestionService.alcoholRateAt(Date(now.time + 1_000L)))
    }

    // ── Linearity ─────────────────────────────────────────────────────────────

    @Test
    fun `BAC increases with number of simultaneous drinks`() {
        val now = Date()
        ingestBeer(now)
        val atOneHour = Date(now.time + 3_600_000L)
        val singleBeerBac = digestionService.alcoholRateAt(atOneHour)

        // Add identical second beer at the same time.
        ingestBeer(now)
        val twoBeerBac = digestionService.alcoholRateAt(atOneHour)

        // More alcohol → higher BAC.
        assertTrue(twoBeerBac > singleBeerBac)

        // With zero-order elimination (constant β, independent of dose), the exact relationship is:
        //   BAC_2(t) = 2·BAC_1(t) + β·(t − t_lag)
        // because elimination does not double but absorption does.
        val effectiveH = 1.0 - body.absorptionDelay // t_check - t_lag = 0.75 h for EMPTY
        val expectedTwoBeerBac = 2 * singleBeerBac + body.decreaseFactor * effectiveH
        assertEquals(expectedTwoBeerBac, twoBeerBac, precision)
    }

    // ── Elimination rate ──────────────────────────────────────────────────────

    @Test
    fun `Alcohol rate decreases at the body decrease-factor once stomach is empty`() {
        val now = Date()
        // 4 beers to ensure BAC is still positive at 5–6 h.
        repeat(4) { ingestBeer(now) }

        // At 5 h and 6 h, the stomach is essentially empty for EMPTY food state
        // (k_a=2.0 → >99.9% absorbed within 4 h). Pure linear elimination then applies.
        val rate5h = digestionService.alcoholRateAt(Date(now.time + 5 * 3_600_000L))
        val rate6h = digestionService.alcoholRateAt(Date(now.time + 6 * 3_600_000L))

        assertEquals(body.decreaseFactor, rate5h - rate6h, precision)
    }

    @Test
    fun `BAC is never negative`() {
        val now = Date()
        ingestBeer(now)
        val farFuture = Date(now.time + 48 * 3_600_000L)
        assertTrue(digestionService.alcoholRateAt(farFuture) >= 0.0)
    }

    // ── Food state effect ─────────────────────────────────────────────────────

    @Test
    fun `Full meal produces a lower peak BAC than empty stomach`() {
        val now = Date()
        ingestBeer(now)

        // Measure approximate peak for EMPTY stomach (around 45 min).
        body.foodState = FoodState.EMPTY
        val emptyPeak = (15L..180L step 5L)
            .map { digestionService.alcoholRateAt(Date(now.time + it * 60_000L)) }
            .max()

        // Reset with same drink under FULL_MEAL.
        ingestionService.remove(ingestionService.getDrinks().filterIsInstance<IngestedDrink>().first())
        ingestBeer(now)
        body.foodState = FoodState.FULL_MEAL
        val fullMealPeak = (15L..240L step 5L)
            .map { digestionService.alcoholRateAt(Date(now.time + it * 60_000L)) }
            .max()

        assertTrue(
            fullMealPeak < emptyPeak,
            "Full meal peak ($fullMealPeak) should be lower than empty stomach peak ($emptyPeak)",
        )
    }

    @Test
    fun `Full meal delays the BAC peak compared to empty stomach`() {
        val now = Date()

        body.foodState = FoodState.EMPTY
        ingestBeer(now)
        val emptyPeakTime = (15L..180L step 5L)
            .maxByOrNull { digestionService.alcoholRateAt(Date(now.time + it * 60_000L)) }!!

        ingestionService.remove(ingestionService.getDrinks().filterIsInstance<IngestedDrink>().first())
        body.foodState = FoodState.FULL_MEAL
        ingestBeer(now)
        val fullMealPeakTime = (15L..300L step 5L)
            .maxByOrNull { digestionService.alcoholRateAt(Date(now.time + it * 60_000L)) }!!

        assertTrue(
            fullMealPeakTime > emptyPeakTime,
            "Full meal peak time ($fullMealPeakTime min) should be later than empty peak ($emptyPeakTime min)",
        )
    }

    @Test
    fun `BAC converges to the same value long after ingestion regardless of food state`() {
        val now = Date()
        ingestBeer(now)

        // At 8 h, all food states should yield the same BAC (stomach fully emptied for all levels).
        val atEightHours = Date(now.time + 8 * 3_600_000L)

        body.foodState = FoodState.EMPTY
        val bacEmpty = digestionService.alcoholRateAt(atEightHours)

        body.foodState = FoodState.LIGHT_MEAL
        val bacLight = digestionService.alcoholRateAt(atEightHours)

        body.foodState = FoodState.FULL_MEAL
        val bacFull = digestionService.alcoholRateAt(atEightHours)

        // All three should be identical (within simulation step rounding ≈ 0.001 g/L).
        assertEquals(bacEmpty, bacLight, precision)
        assertEquals(bacEmpty, bacFull, precision)
    }
}
