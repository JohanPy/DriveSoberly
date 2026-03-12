package com.vaudibert.canidrive.domain.drivelaw

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

class DriveLawService(
    private val countryNamer: (countryCode: String) -> String,
    private val defaultName: String,
    countryList: List<DriveLaw>,
    private val defaultDriveLaw: DriveLaw
) {
    val defaultLimit = 0.0

    private val _customCountryLimitFlow = MutableStateFlow(0.0)
    val customCountryLimitFlow: StateFlow<Double> = _customCountryLimitFlow.asStateFlow()

    var customCountryLimit: Double
        get() = _customCountryLimitFlow.value
        set(value) {
            _customCountryLimitFlow.value = value
        }

    private val _isYoungFlow = MutableStateFlow(false)
    val isYoungFlow: StateFlow<Boolean> = _isYoungFlow.asStateFlow()

    var isYoung: Boolean
        get() = _isYoungFlow.value
        set(value) {
            _isYoungFlow.value = value
        }

    private val _isProfessionalFlow = MutableStateFlow(false)
    val isProfessionalFlow: StateFlow<Boolean> = _isProfessionalFlow.asStateFlow()

    var isProfessional: Boolean
        get() = _isProfessionalFlow.value
        set(value) {
            _isProfessionalFlow.value = value
        }

    private val countryLaws = countryList
        .sortedBy { law -> countryNamer(law.countryCode) }

    private val _driveLawFlow = MutableStateFlow(defaultDriveLaw)
    val driveLawFlow: StateFlow<DriveLaw> = _driveLawFlow.asStateFlow()

    var driveLaw: DriveLaw
        get() = _driveLawFlow.value
        private set(value) {
            _driveLawFlow.value = value
        }

    fun getListOfCountriesWithFlags(): List<String> {
        return countryLaws.map { law ->
            if (law.countryCode == "")
                defaultName
            else
                stringToFlagEmoji(law.countryCode) + " " + countryNamer(law.countryCode)
        }
    }


    fun getIndexOfCurrent() = countryLaws
        .indexOfFirst {
                law -> law.countryCode == driveLaw.countryCode
        }.coerceAtLeast(0)

    fun select(countryCode: String) {
        driveLaw = countryLaws.find { law -> law.countryCode == countryCode } ?: defaultDriveLaw
    }

    fun select(position: Int) {
        driveLaw = if (position !in countryLaws.indices)
            defaultDriveLaw
        else
            countryLaws[position]
    }

    fun driveLimit() : Double {
        if (driveLaw == defaultDriveLaw) return customCountryLimit

        val regularLimit = driveLaw.limit

        val youngLimit = if (isYoung)
            driveLaw.youngLimit?.limit ?: regularLimit
        else
            regularLimit

        val professionalLimit = if (isProfessional)
            driveLaw.professionalLimit?.limit ?: regularLimit
        else
            regularLimit

        return min(youngLimit, professionalLimit)
    }

    /**
     * This method is to change the country code like "us" into 🇺🇸
     * Stolen from https://stackoverflow.com/a/35849652/75579
     * 1. It first checks if the string consists of only 2 characters: ISO 3166-1 alpha-2 two-letter country codes (https://en.wikipedia.org/wiki/Regional_Indicator_Symbol).
     * 2. It then checks if both characters are alphabet
     * do nothing if it doesn't fulfil the 2 checks
     * caveat: if you enter an invalid 2 letter country code, say "XX", it will pass the 2 checks, and it will return unknown result
     */
    private fun stringToFlagEmoji(twoCharString: String): String {
        // 1. It first checks if the string consists of only 2 characters: ISO 3166-1 alpha-2 two-letter country codes (https://en.wikipedia.org/wiki/Regional_Indicator_Symbol).
        if (twoCharString.length != 2) {
            return twoCharString
        }

        val countryCodeCaps = twoCharString.uppercase(java.util.Locale.ROOT) // upper case is important because we are calculating offset
        val firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6

        // 2. It then checks if both characters are alphabet
        if (!countryCodeCaps[0].isLetter() || !countryCodeCaps[1].isLetter()) {
            return twoCharString
        }

        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
}