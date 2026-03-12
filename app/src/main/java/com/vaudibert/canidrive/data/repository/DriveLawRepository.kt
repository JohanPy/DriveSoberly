package com.vaudibert.canidrive.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vaudibert.canidrive.R
import com.vaudibert.canidrive.domain.drivelaw.DriveLaw
import com.vaudibert.canidrive.domain.drivelaw.DriveLawService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.*

class DriveLawRepository(private val context: Context) {

    val driveLawService =
        DriveLawService(
            { code: String -> Locale("", code).displayCountry },
            context.getString(R.string.other),
            DriveLaws.loadLaws(context),
            DriveLaws.default
        )

    private val _liveDriveLaw = MutableLiveData<DriveLaw>()
    private val _liveIsYoung = MutableLiveData<Boolean>()
    private val _liveIsProfessional = MutableLiveData<Boolean>()
    private val _liveCustomCountryLimit = MutableLiveData<Double>()


    val liveDriveLaw: LiveData<DriveLaw>
            get() = _liveDriveLaw
    val liveIsYoung: LiveData<Boolean>
            get() = _liveIsYoung
    val liveIsProfessional: LiveData<Boolean>
            get() = _liveIsProfessional
    val liveCustomCountryLimit: LiveData<Double>
            get() = _liveCustomCountryLimit

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val sharedPref = EncryptedSharedPreferences.create(
            context,
            context.getString(R.string.user_preferences),
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Initiate drive law service
        driveLawService.isYoung = sharedPref.getBoolean(context.getString(R.string.user_young_driver), false)
        driveLawService.isProfessional = sharedPref.getBoolean(context.getString(R.string.user_professional_driver), false)

        driveLawService.select(
            sharedPref.getString(context.getString(R.string.countryCode), "") ?: ""
        )
        driveLawService.customCountryLimit= sharedPref.getFloat(context.getString(R.string.customCountryLimit), 0.0F).toDouble()

        // Set collectors once drive law service initialized
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                driveLawService.driveLawFlow.drop(1).collect { law ->
                    sharedPref.edit()
                        .putString(context.getString(R.string.countryCode), law.countryCode)
                        .apply()
                    _liveDriveLaw.postValue(law)
                }
            }

            launch {
                driveLawService.isYoungFlow.drop(1).collect { isYoung ->
                    sharedPref.edit()
                        .putBoolean(context.getString(R.string.user_young_driver), isYoung)
                        .apply()
                    _liveIsYoung.postValue(isYoung)
                    _liveDriveLaw.postValue(driveLawService.driveLaw)
                }
            }

            launch {
                driveLawService.isProfessionalFlow.drop(1).collect { isProfessional ->
                    sharedPref.edit()
                        .putBoolean(context.getString(R.string.user_professional_driver), isProfessional)
                        .apply()
                    _liveIsProfessional.postValue(isProfessional)
                    _liveDriveLaw.postValue(driveLawService.driveLaw)
                }
            }

            launch {
                driveLawService.customCountryLimitFlow.drop(1).collect { newLimit ->
                    sharedPref.edit()
                        .putFloat(context.getString(R.string.customCountryLimit), newLimit.toFloat())
                        .apply()
                    _liveCustomCountryLimit.postValue(newLimit)
                }
            }
        }

        // Initialize live datas with initial state from flows
        _liveDriveLaw.value = driveLawService.driveLaw
        _liveIsYoung.value = driveLawService.isYoung
        _liveIsProfessional.value = driveLawService.isProfessional
        _liveCustomCountryLimit.value = driveLawService.customCountryLimit
    }

}