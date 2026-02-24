package com.vaudibert.canidrive.domain.digestion

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

    var sex: Sex = Sex.OTHER
        set(value) {
            field = value
            effectiveWeight = weight * (if (sex == Sex.MALE) MALE_SEX_FACTOR else FEMALE_SEX_FACTOR)
            decreaseFactor = decreaseFactorWith(value, alcoholTolerance)
            onUpdate(sex, weight, alcoholTolerance)
        }

    var weight = 80.0
        set(value) {
            field = value
            effectiveWeight = weight * (if (sex == Sex.MALE) MALE_SEX_FACTOR else FEMALE_SEX_FACTOR)
            onUpdate(sex, weight, alcoholTolerance)
        }

    var alcoholTolerance = 0.0
        set(value) {
            if (value in 0.0..1.0) {
                field = value
                decreaseFactor = decreaseFactorWith(sex, value)
                onUpdate(sex, weight, alcoholTolerance)
            }
        }

    private fun decreaseFactorWith(sex: Sex, tolerance: Double): Double {
        return if (sex == Sex.MALE)
            tolerance * MALE_MAX_DECREASE + (1-tolerance) * MALE_MIN_DECREASE
        else
            tolerance * FEMALE_MAX_DECREASE + (1-tolerance) * FEMALE_MIN_DECREASE
    }

    var onUpdate = { _ : Sex, _ : Double, _ : Double -> }

    var decreaseFactor: Double = FEMALE_MIN_DECREASE

    var effectiveWeight:Double = weight * FEMALE_SEX_FACTOR

}