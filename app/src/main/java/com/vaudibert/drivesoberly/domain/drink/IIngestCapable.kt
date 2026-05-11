package com.vaudibert.drivesoberly.domain.drink

import java.util.*

interface IIngestCapable<Preset : IPresetDrink> {
    fun ingest(
        preset: Preset,
        ingestionTime: Date,
    )
}
