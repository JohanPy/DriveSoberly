package com.vaudibert.canidrive.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.domain.digestion.DigestionService
import com.vaudibert.canidrive.domain.digestion.FoodState
import com.vaudibert.canidrive.domain.digestion.PhysicalBody
import com.vaudibert.canidrive.domain.digestion.Sex
import com.vaudibert.canidrive.domain.drink.IIngestedDrinkProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repository holding the drinker and driveLaw instances.
 *
 * It establishes the link between the drinker absolute state and the drive law limits that depend
 * on the country selection.
 *
 * It is responsible for retrieving all saved values with the given :
 *  - sharedPreferences instance for :
 *      - weight
 *      - sex
 *      - country code (= drive law)
 *      - init flag (= user configuration already validated once)
 *  - drinkDao for past consumed drinks.
 */
class DigestionRepository(context: Context, drinkProvider: IIngestedDrinkProvider) {
    // Main instance to link
    val body = PhysicalBody()

    val digestionService = DigestionService(body, drinkProvider)

    val toleranceLevels =
        listOf(
            context.getString(R.string.alcohol_tolerance_low),
            context.getString(R.string.alcohol_tolerance_medium),
            context.getString(R.string.alcohol_tolerance_high),
        )

    private val _liveDrinker = MutableLiveData<PhysicalBody>()
    val liveDrinker: LiveData<PhysicalBody>
        get() = _liveDrinker

    init {
        val masterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        val sharedPref =
            EncryptedSharedPreferences.create(
                context,
                context.getString(R.string.user_preferences),
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

        val weight = sharedPref.getFloat(context.getString(R.string.user_weight), 70F).toDouble()
        val sex =
            Sex.fromString(
                sharedPref.getString(context.getString(R.string.user_sex), "OTHER") ?: "OTHER",
            )
        val tolerance = sharedPref.getFloat(context.getString(R.string.user_tolerance), 0.0F).toDouble()
        val foodState =
            FoodState.fromString(
                sharedPref.getString("FOOD_STATE", "EMPTY") ?: "EMPTY",
            )

        body.sex = sex
        body.weight = weight
        body.alcoholTolerance = tolerance
        body.foodState = foodState

        CoroutineScope(Dispatchers.IO).launch {
            body.bodyState.collect { state ->
                sharedPref
                    .edit()
                    .putString(context.getString(R.string.user_sex), state.sex.name)
                    .putFloat(context.getString(R.string.user_weight), state.weight.toFloat())
                    .putFloat("USER_TOLERANCE", state.alcoholTolerance.toFloat())
                    .putString("FOOD_STATE", state.foodState.name)
                    .apply()
                _liveDrinker.postValue(body)
            }
        }

        _liveDrinker.value = body
    }
}
