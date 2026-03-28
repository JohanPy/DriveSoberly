package com.vaudibert.canidrive.domain.digestion

/**
 * Represents the user's alimentary state at the time of drinking.
 *
 * Food slows gastric emptying, which delays and reduces peak BAC by:
 *  - Increasing [absorptionDelay] (t_lag): the stomach holds alcohol longer before it
 *    reaches the intestine where fast absorption occurs.
 *  - Decreasing [absorptionRate] (k_a): the alcohol transfer from stomach to bloodstream
 *    is slower and more spread out.
 *
 * Calibration values based on published pharmacokinetic studies:
 *  - EMPTY  → peak at ~30–60 min, absorption mostly complete in ~1.5 h
 *  - LIGHT_MEAL → moderate delay, peak 20–30% lower and ~30–45 min later
 *  - FULL_MEAL  → pronounced delay, peak 30–40% lower and ~60–90 min later
 *
 * @param absorptionRate  Gastric emptying / absorption rate constant k_a (h⁻¹).
 *                        Higher = faster transfer from stomach to blood.
 * @param absorptionDelay Lag time before absorption begins, t_lag (h).
 */
enum class FoodState(val absorptionRate: Double, val absorptionDelay: Double) {

    /** Fasted – empty stomach. Rapid absorption, earlier and higher peak. */
    EMPTY(absorptionRate = 2.0, absorptionDelay = 0.25),

    /** Light snack: chips, fruit, crackers, cheese, etc. */
    LIGHT_MEAL(absorptionRate = 1.2, absorptionDelay = 0.40),

    /** Full meal: pasta, rice, pizza, meat, burger, etc. */
    FULL_MEAL(absorptionRate = 0.6, absorptionDelay = 0.75),
    ;

    companion object {
        fun fromString(value: String): FoodState =
            when (value.uppercase()) {
                "LIGHT_MEAL" -> LIGHT_MEAL
                "FULL_MEAL" -> FULL_MEAL
                else -> EMPTY
            }
    }
}
