package com.vaudibert.canidrive.data.repository

import com.vaudibert.canidrive.domain.drivelaw.DriveLaw
import com.vaudibert.canidrive.domain.drivelaw.ProfessionalLimit
import com.vaudibert.canidrive.domain.drivelaw.YoungLimit
import org.json.JSONArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class DriveLawsTest {

    /** Reads drive_laws.json from the module's assets folder without Android Context. */
    private fun loadDriveLawsFromAssets(): List<DriveLaw> {
        val assetsFile = File("src/main/assets/drive_laws.json")
        val jsonString = assetsFile.readText()
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<DriveLaw>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val code = obj.getString("countryCode")
            val limit = obj.getDouble("limit")
            var youngLimit: YoungLimit? = null
            if (!obj.isNull("youngLimit")) {
                val yObj = obj.getJSONObject("youngLimit")
                youngLimit = YoungLimit(yObj.getDouble("limit"), yObj.getString("explanationName"))
            }
            var professionalLimit: ProfessionalLimit? = null
            if (!obj.isNull("professionalLimit")) {
                val pObj = obj.getJSONObject("professionalLimit")
                professionalLimit = ProfessionalLimit(pObj.getDouble("limit"))
            }
            list.add(DriveLaw(code, limit, youngLimit, professionalLimit))
        }
        return list
    }

    @Test
    fun `check drive laws BAC limits`() {
        // These values should all be in g/L.
        val expectedLimits =
            mapOf(
                "AL" to 0.1, "AT" to 0.5, "BY" to 0.3, "BE" to 0.5, "BA" to 0.3, "BG" to 0.5,
                "HR" to 0.5, "CY" to 0.5, "CZ" to 0.0, "DK" to 0.5, "EE" to 0.19, "FI" to 0.5,
                "FR" to 0.5, "GE" to 0.2, "DE" to 0.5, "GI" to 0.5, "GR" to 0.5, "HU" to 0.0,
                "IS" to 0.2, "IE" to 0.5, "IT" to 0.5, "LV" to 0.5, "LT" to 0.4, "LU" to 0.5,
                "MT" to 0.8, "MD" to 0.3, "ME" to 0.3, "NL" to 0.5, "NO" to 0.2, "PL" to 0.2,
                "PT" to 0.5, "RO" to 0.0, "RU" to 0.356, "RS" to 0.2, "SK" to 0.0, "SI" to 0.5,
                "ES" to 0.5, "SE" to 0.2, "CH" to 0.5, "UA" to 0.2, "GB" to 0.8, "CA" to 0.8,
                "US" to 0.8, "CN" to 0.2, "HK" to 0.5, "JP" to 0.3, "KR" to 0.3, "TW" to 0.3,
                "IN" to 0.3, "NP" to 0.0, "PK" to 0.0, "LK" to 0.6, "ID" to 0.0, "LA" to 0.8,
                "MY" to 0.8, "PH" to 0.5, "SG" to 0.8, "TH" to 0.5, "VN" to 0.0, "AM" to 0.4,
                "IR" to 0.0, "IL" to 0.24, "JO" to 0.5, "KW" to 0.0, "SA" to 0.0, "AE" to 0.0,
                "TR" to 0.5,
            )

        for (law in loadDriveLawsFromAssets()) {
            val countryCode = law.countryCode
            if (countryCode.isEmpty()) continue

            val expected = expectedLimits[countryCode]
            requireNotNull(expected) { "Missing expected limit for country $countryCode" }

            assertEquals(expected, law.limit, 0.001, "Incorrect limit for country $countryCode")
        }
    }
}
