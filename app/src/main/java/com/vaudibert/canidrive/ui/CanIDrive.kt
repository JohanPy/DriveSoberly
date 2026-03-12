package com.vaudibert.canidrive.ui

import android.app.Application
import com.vaudibert.canidrive.di.appModule
import com.vaudibert.canidrive.domain.ITimeService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CanIDrive : Application() {
    val time: ITimeService = TimeServiceAndroid()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CanIDrive)
            modules(appModule)
        }
    }
}
