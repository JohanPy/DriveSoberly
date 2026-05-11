package com.vaudibert.drivesoberly.ui

import android.app.Application
import com.vaudibert.drivesoberly.di.appModule
import com.vaudibert.drivesoberly.domain.ITimeService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class DriveSoberlyApp : Application() {
    val time: ITimeService = TimeServiceAndroid()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@DriveSoberlyApp)
            modules(appModule)
        }
    }
}
