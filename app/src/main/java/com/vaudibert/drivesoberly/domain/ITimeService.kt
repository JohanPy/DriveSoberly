package com.vaudibert.drivesoberly.domain

interface ITimeService {
    fun nowInMillis(): Long

    fun isSaintPatrick(): Boolean
}
