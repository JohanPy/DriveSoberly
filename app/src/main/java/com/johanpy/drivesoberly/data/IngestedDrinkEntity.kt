package com.johanpy.drivesoberly.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.johanpy.drivesoberly.domain.drink.IIngestedDrink
import com.johanpy.drivesoberly.domain.drink.IngestedDrink
import java.util.*

@Entity
class IngestedDrinkEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long,
    ingestionTime: Date,
    volume: Double,
    name: String,
    degree: Double,
    emoji: String,
) : IIngestedDrink, IngestedDrink(name, volume, degree, ingestionTime, emoji) {
    constructor(uid: Long, ingested: IngestedDrink) : this(
        uid,
        ingested.ingestionTime,
        ingested.volume,
        ingested.name,
        ingested.degree,
        ingested.emoji,
    )
}
