package com.koflox.cyclingassistant.app

import android.app.Application
import com.koflox.concurrent.enableStrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CyclingAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        enableStrictMode(this)
    }
}
