package com.vaudibert.canidrive.domain.digestion

/**
 * Biological sex used for BAC calculation (Widmark formula).
 * The sex factor affects the volume of distribution of alcohol in the body.
 */
enum class Sex {
    MALE,
    FEMALE,
    OTHER;

    companion object {
        fun fromString(value: String): Sex {
            return when (value.uppercase()) {
                "MALE" -> MALE
                "FEMALE" -> FEMALE
                else -> OTHER
            }
        }
    }
}
