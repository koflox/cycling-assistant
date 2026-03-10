package com.koflox.observability.initializer

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.startup.Initializer
import timber.log.Timber

internal class TimberInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val isDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
