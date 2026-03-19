package com.koflox.observability.initializer

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.startup.Initializer
import com.koflox.observability.FirebaseProvider

internal class FirebaseInitializer : Initializer<Unit> {

    companion object {
        private const val COLLECTION_ENABLED_KEY = "com.koflox.observability.collection_enabled"
    }

    override fun create(context: Context) {
        val metaData = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData
        val collectionEnabled = metaData.getBoolean(COLLECTION_ENABLED_KEY, false)
        FirebaseProvider.initialize(
            application = context.applicationContext as Application,
            collectionEnabled = collectionEnabled,
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        TimberInitializer::class.java,
    )
}
