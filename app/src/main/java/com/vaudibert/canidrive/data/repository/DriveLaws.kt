package com.vaudibert.canidrive.data.repository

import android.content.Context
import com.vaudibert.canidrive.domain.drivelaw.DriveLaw
import com.vaudibert.canidrive.domain.drivelaw.ProfessionalLimit
import com.vaudibert.canidrive.domain.drivelaw.YoungLimit
import org.json.JSONArray

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
        
        return list
    }
}