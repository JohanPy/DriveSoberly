package com.vaudibert.canidrive.domain.digestion

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the person drinking.
 * The parameters such as weight and sex may change as the user adjusts the inputs.
 */

class PhysicalBody {
    companion object {
        private const val MALE_SEX_FACTOR = 0.7
        private const val MALE_MIN_DECREASE = 0.1
        private const val MALE_MAX_DECREASE = 0.15

        private const val FEMALE_SEX_FACTOR = 0.6
        private const val FEMALE_MIN_DECREASE = 0.085
        private const val FEMALE_MAX_DECREASE = 0.1
    }

    data class BodyState(
        val sex: Sex,
        val weight: Double,
        val alcoholTolerance: Double,
        val foodState: FoodState,
    )

    private val _bodyState = MutableStateFlow(BodyState(Sex.OTHER, 80.0, 0.0, FoodState.EMPTY))
    val bodyState: StateFlow<BodyState> = _bodyState.asStateFlow()

    var sex: Sex = Sex.OTHER
        set(value) {
            field = value
            effectiveWeight = weight * (if (sex == Sex.MALE) MALE_SEX_FACTOR else FEMALE_SEX_FACTOR)
            decreaseFactor = decreaseFactorWith(value, alcoholTolerance)
            _bodyState.value = BodyState(sex, weight, alcoholTolerance, foodState)
        }

    var weight = 80.0
        set(value) {
            field = value
            effectiveWeight = weight * (if (sex == Sex.MALE) MALE_SEX_FACTOR else FEMALE_SEX_FACTOR)
            _bodyState.value = BodyState(sex, weight, alcoholTolerance, foodState)
        }

    var alcoholTolerance = 0.0
        set(value) {
            if (value in 0.0..1.0) {
                field = value
                decreaseFactor = decreaseFactorWith(sex, value)
                _bodyState.value = BodyState(sex, weight, alcoholTolerance, foodState)
            }
        }

    var foodState: FoodState = FoodState.EMPTY
        set(value) {
            field = value
            _bodyState.value = BodyState(sex, weight, alcoholTolerance, foodState)
        }

    /** Absorption rate constant k_a (h⁻¹), derived from current [foodState]. */
    val absorptionRate: Double get() = foodState.absorptionRate

    /** Lag time before absorption begins (h), derived from current [foodState]. */
    val absorptionDelay: Double get() = foodState.absorptionDelay

    private fun decreaseFactorWith(
        sex: Sex,
        tolerance: Double,
    ): Double {
        return if (sex == Sex.MALE) {
            tolerance * MALE_MAX_DECREASE + (1 - tolerance) * MALE_MIN_DECREASE
        } else {
            tolerance * FEMALE_MAX_DECREASE + (1 - tolerance) * FEMALE_MIN_DECREASE
        }
    }

    var decreaseFactor: Double = FEMALE_MIN_DECREASE

    var effectiveWeight: Double = weight * FEMALE_SEX_FACTOR
}
