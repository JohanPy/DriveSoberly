package com.johanpy.drivesoberly.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.johanpy.drivesoberly.domain.drink.IngestedDrink

@Dao
interface IngestedDrinkDao {
    @Query("SELECT * from ingesteddrinkentity")
    suspend fun getAll(): List<IngestedDrinkEntity>

    @Insert(entity = IngestedDrinkEntity::class)
    suspend fun insert(ingestedDrink: IngestedDrink): Long

    @Delete(entity = IngestedDrinkEntity::class)
    suspend fun remove(ingestedDrink: IngestedDrink)
}
