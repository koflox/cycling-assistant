package com.koflox.cyclingassistant.app

import android.app.Application
import com.koflox.concurrent.enableStrictMode
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CyclingAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        enableStrictMode(this)
        startKoin {
            androidLogger()
            androidContext(this@CyclingAssistantApplication)
            modules(appModule)
        }
    }
}
