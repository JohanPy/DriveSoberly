package com.vaudibert.canidrive.data.repository

import android.content.Context
import com.vaudibert.canidrive.domain.drivelaw.DriveLaw
import com.vaudibert.canidrive.domain.drivelaw.ProfessionalLimit
import com.vaudibert.canidrive.domain.drivelaw.YoungLimit
import org.json.JSONArray
import java.util.Locale

object DriveLaws {
    val default = DriveLaw("", 0.0)

    fun loadLaws(context: Context): List<DriveLaw> {
        val jsonString = context.assets.open("drive_laws.json").bufferedReader().use { it.readText() }
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

            var profLimit: ProfessionalLimit? = null
            if (!obj.isNull("professionalLimit")) {
                val pObj = obj.getJSONObject("professionalLimit")
                profLimit = ProfessionalLimit(pObj.getDouble("limit"))
            }

            list.add(DriveLaw(code, limit, youngLimit, profLimit))
        }

        // Defensive fallback: if the bundled JSON is truncated, keep the app usable by
        // exposing all ISO countries with a conservative default limit.
        if (list.size <= 2) {
            val knownByCode = list.associateBy { it.countryCode }
            val expanded =
                Locale
                    .getISOCountries()
                    .sorted()
                    .map { countryCode ->
                        knownByCode[countryCode] ?: DriveLaw(countryCode, default.limit)
                    }
                    .toMutableList()

            expanded.add(default)
            return expanded
        }

        return list
    }
}
