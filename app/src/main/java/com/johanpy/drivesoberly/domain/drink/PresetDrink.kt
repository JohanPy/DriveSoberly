package com.johanpy.drivesoberly.domain.drink

open class PresetDrink(
    override var name: String,
    override var volume: Double,
    override var degree: Double,
    override var count: Int = 0,
    override var emoji: String = "🍺",
    override var isBuiltIn: Boolean = false,
) : IPresetDrink {
    override fun equals(other: Any?): Boolean {
        if (other !is PresetDrink) return false

        return this.name == other.name &&
            this.volume == other.volume &&
            this.degree == other.degree
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + degree.hashCode()
        return result
    }
}
