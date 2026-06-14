package com.dcs.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DcsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
