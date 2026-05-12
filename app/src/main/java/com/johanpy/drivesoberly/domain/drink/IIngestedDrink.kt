package com.johanpy.drivesoberly.domain.drink

import java.util.*

interface IIngestedDrink {
    val name: String
    val volume: Double
    val degree: Double
    val ingestionTime: Date
    val emoji: String

    fun alcoholMass(): Double
}
