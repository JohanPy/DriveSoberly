package com.vaudibert.canidrive.ui

import com.vaudibert.canidrive.domain.ITimeService
import java.util.*

class TimeServiceAndroid : ITimeService {
    override fun nowInMillis() = System.currentTimeMillis()

    override fun isSaintPatrick(): Boolean {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) // 0-indexed: March = 2
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return month == Calendar.MARCH && day in 16..18
    }
}