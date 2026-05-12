package com.johanpy.drivesoberly.domain.drink

interface IPresetDrink {
    var name: String
    var volume: Double
    var degree: Double
    var count: Int
    var emoji: String
    var isBuiltIn: Boolean
}
