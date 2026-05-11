package com.vaudibert.drivesoberly.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vaudibert.drivesoberly.domain.drink.IIngestedDrink
import com.vaudibert.drivesoberly.domain.drink.IngestedDrink
import java.util.*

@Entity
class IngestedDrinkEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long,
    ingestionTime: Date,
    volume: Double,
    name: String,
    degree: Double,
) : IIngestedDrink, IngestedDrink(name, volume, degree, ingestionTime) {
    constructor(uid: Long, ingested: IngestedDrink) : this(
        uid,
        ingested.ingestionTime,
        ingested.volume,
        ingested.name,
        ingested.degree,
    )
}
