package com.vaudibert.canidrive.ui.repository

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.data.DrinkDatabase
import com.vaudibert.canidrive.domain.DrinkerStatusService

class MainRepository(private val context: Context) {

    private val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Add the `name` column with a default value for existing rows.
            //    NOT NULL DEFAULT '' matches the v2 schema (TEXT NOT NULL).
            database.execSQL(
                "ALTER TABLE DrinkEntity ADD COLUMN `name` TEXT NOT NULL DEFAULT ''"
            )
            // 2. Rename the table to its new name.
            database.execSQL(
                "ALTER TABLE DrinkEntity RENAME TO IngestedDrinkEntity"
            )
            // 3. Create the PresetDrinkEntity table that was added in v2.
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `PresetDrinkEntity` " +
                "(`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`volume` REAL NOT NULL, " +
                "`degree` REAL NOT NULL, " +
                "`count` INTEGER NOT NULL)"
            )
        }
    }


    private val drinkDatabase = Room
        .databaseBuilder(context, DrinkDatabase::class.java, "drink-database")
        .addMigrations(migration_1_2)
        .build()

    val drinkRepository = DrinkRepository(context, drinkDatabase)

    val digestionRepository = DigestionRepository(context, drinkRepository.ingestionService)

    val driveLawRepository = DriveLawRepository(context)

    val drinkerStatusService = DrinkerStatusService(
        digestionRepository.digestionService,
        driveLawRepository.driveLawService
    )

    private val sharedPref = EncryptedSharedPreferences.create(
        context,
        context.getString(R.string.user_preferences),
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )


    // Flag for initialization, saved when set.
    var init : Boolean = sharedPref.getBoolean(context.getString(R.string.user_initialized), false)
        set(value) {
            field = value
            sharedPref.edit()
                .putBoolean(context.getString(R.string.user_initialized), value)
                .apply()
        }

}