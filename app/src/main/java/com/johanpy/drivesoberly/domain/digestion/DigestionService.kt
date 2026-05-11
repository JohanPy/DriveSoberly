package com.johanpy.drivesoberly.domain.digestion

import com.johanpy.drivesoberly.domain.drink.IIngestedDrinkProvider
import java.util.*
import kotlin.math.max

/**
 * Two-compartment pharmacokinetic alcohol digestion model.
 *
 * Each drink enters the **stomach** compartment after an initial lag (absorptionDelay).
 * Alcohol is then progressively transferred to the **body** compartment at rate k_a (h⁻¹),
 * while the liver eliminates it at a constant hepatic rate β (g/L/h).
 *
 * A [PhysicalBody.foodState] controls k_a and the lag time:
 *  - Empty stomach: fast absorption, earlier and higher BAC peak.
 *  - Full meal: slower absorption, peak 30–40% lower and 60–90 min later.
 *
 * The simulation runs forward in time using 5-minute Euler steps.
 * All alcohol is eventually absorbed (conservative — no first-pass reduction applied),
 * so the model never under-estimates total alcohol exposure.
 *
 * Differential equations (food2.md, §"Mise en différence"):
 *   ΔA_stomach = -k_a × A_stomach × Δt
 *   ΔA_body    =  k_a × A_stomach × Δt  −  β_mass × Δt
 *   BAC        =  A_body / effectiveWeight
 */
class DigestionService(
    private val body: PhysicalBody,
    private val drinkProvider: IIngestedDrinkProvider,
) {
    companion object {
        /** Simulation time step: 5 minutes expressed in hours. */
        private const val STEP_HOURS = 1.0 / 12.0
        private const val STEP_MS = (STEP_HOURS * 3_600_000).toLong()

        /** Safety ceiling for forward simulation in [timeToReachLimit] (48 h). */
        private const val MAX_SIMULATE_HOURS = 48.0

        /** Stomach alcohol threshold (g) below which we consider it empty. */
        private const val STOMACH_EMPTY_THRESHOLD = 0.001
    }

    /** Holds the two physical compartments at a given instant. */
    private data class CompartmentState(val stomachAlcohol: Double, val bodyAlcohol: Double)

    data class BacProjection(
        val currentRate: Double,
        val peakRate: Double,
        val peakTime: Date,
        val exceedsLimit: Boolean,
        val returnBelowLimitTime: Date?,
        val soberTime: Date,
    )

    /** Returns the BAC (g/L) at the given [date], never negative. */
    fun alcoholRateAt(date: Date): Double = max(0.0, simulateTo(date).second)

    /**
     * Projects BAC evolution from [now] over the simulation horizon and returns key moments.
     */
    fun projectionForLimit(
        limit: Double,
        now: Date = Date(),
    ): BacProjection {
        val eps = 0.0001
        val maxTime = now.time + (MAX_SIMULATE_HOURS * 3_600_000).toLong()

        var currentRate = alcoholRateAt(now)
        var peakRate = currentRate
        var peakTime = now
        var seenAboveLimit = currentRate > limit + eps
        var returnBelowLimitTime: Date? = null
        var hasFutureAlcohol = currentRate > eps
        var soberTime: Date? = null

        var t = now.time + STEP_MS
        while (t <= maxTime) {
            val r = alcoholRateAt(Date(t))

            if (r > peakRate + eps) {
                peakRate = r
                peakTime = Date(t)
            }

            if (r > eps) {
                hasFutureAlcohol = true
            }

            if (r > limit + eps) {
                seenAboveLimit = true
            }

            if (seenAboveLimit && returnBelowLimitTime == null && r <= limit + eps) {
                returnBelowLimitTime = Date(t)
            }

            if (hasFutureAlcohol && soberTime == null && r <= eps) {
                soberTime = Date(t)
                break
            }

            t += STEP_MS
        }

        // Fallbacks for edge cases near horizon.
        if (soberTime == null) {
            soberTime = if (hasFutureAlcohol) timeToReachLimit(0.0) else now
        }
        if (seenAboveLimit && returnBelowLimitTime == null) {
            returnBelowLimitTime = timeToReachLimit(limit)
        }

        return BacProjection(
            currentRate = currentRate,
            peakRate = peakRate,
            peakTime = peakTime,
            exceedsLimit = peakRate > limit + eps,
            returnBelowLimitTime = returnBelowLimitTime,
            soberTime = soberTime,
        )
    }

    /**
     * Returns the first future [Date] at which BAC will drop to or below [limit].
     * Uses a fast linear path once the stomach is empty.
     */
    fun timeToReachLimit(limit: Double): Date {
        val now = Date()
        val (state, currentBac) = simulateTo(now)
        if (currentBac <= limit) return now

        val ka = body.absorptionRate
        val betaMass = body.decreaseFactor * body.effectiveWeight
        var stomach = state.stomachAlcohol
        var bodyAlcohol = state.bodyAlcohol
        var t = now.time
        val maxTime = now.time + (MAX_SIMULATE_HOURS * 3_600_000).toLong()

        while (t < maxTime) {
            if (stomach < STOMACH_EMPTY_THRESHOLD) {
                // Stomach empty → pure linear elimination; jump directly to target time.
                val gramsToDrop = bodyAlcohol - limit * body.effectiveWeight
                if (gramsToDrop <= 0.0) break
                t += (gramsToDrop / betaMass * 3_600_000).toLong()
                break
            }

            val absorbed = ka * stomach * STEP_HOURS
            stomach = max(0.0, stomach - absorbed)
            bodyAlcohol = max(0.0, bodyAlcohol + absorbed - betaMass * STEP_HOURS)
            t += STEP_MS

            if (bodyAlcohol / body.effectiveWeight <= limit) break
        }

        return if (t <= now.time) now else Date(t)
    }

    /**
     * Simulates the two-compartment model from the first absorption event up to [end].
     *
     * @return Pair of the final [CompartmentState] and BAC (g/L) at [end].
     */
    private fun simulateTo(end: Date): Pair<CompartmentState, Double> {
        val drinks = drinkProvider.getDrinks()
        if (drinks.isEmpty()) return Pair(CompartmentState(0.0, 0.0), 0.0)

        val ka = body.absorptionRate
        val betaMass = body.decreaseFactor * body.effectiveWeight
        val lagMs = (body.absorptionDelay * 3_600_000).toLong()

        // Map drinks to absorption-start events, sorted chronologically.
        data class AbsEvent(val startMs: Long, val mass: Double)
        val events = drinks
            .map { AbsEvent(it.ingestionTime.time + lagMs, it.alcoholMass()) }
            .sortedBy { it.startMs }

        // If the first drink hasn't started absorbing yet, BAC is still zero.
        val simStart = events.first().startMs
        if (simStart >= end.time) return Pair(CompartmentState(0.0, 0.0), 0.0)

        var stomach = 0.0
        var bodyAlcohol = 0.0
        var t = simStart
        var eventIdx = 0

        // Pre-load all events that start at or before simStart.
        while (eventIdx < events.size && events[eventIdx].startMs <= simStart) {
            stomach += events[eventIdx].mass
            eventIdx++
        }

        while (t < end.time) {
            val nextStep = minOf(t + STEP_MS, end.time)

            // Add drinks whose lag has elapsed by the end of this step.
            while (eventIdx < events.size && events[eventIdx].startMs <= nextStep) {
                stomach += events[eventIdx].mass
                eventIdx++
            }

            val stepH = (nextStep - t).toDouble() / 3_600_000.0
            val absorbed = ka * stomach * stepH
            stomach = max(0.0, stomach - absorbed)
            bodyAlcohol = max(0.0, bodyAlcohol + absorbed - betaMass * stepH)

            t = nextStep
        }

        return Pair(
            CompartmentState(stomach, bodyAlcohol),
            bodyAlcohol / body.effectiveWeight,
        )
    }
}
