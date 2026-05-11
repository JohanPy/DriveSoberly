package com.johanpy.drivesoberly.domain

interface ITimeService {
    fun nowInMillis(): Long

    fun isSaintPatrick(): Boolean
}
