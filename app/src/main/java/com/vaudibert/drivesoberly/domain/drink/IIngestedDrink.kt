package com.vaudibert.drivesoberly.domain.drink

import java.util.*

interface IIngestedDrink {
    val name: String
    val volume: Double
    val degree: Double
    val ingestionTime: Date

    fun alcoholMass(): Double
}
