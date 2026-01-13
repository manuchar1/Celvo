package com.mtislab.celvo

import android.app.Application
import com.mtislab.celvo.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class CelvoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CelvoApplication)
            androidLogger()
        }
    }
}