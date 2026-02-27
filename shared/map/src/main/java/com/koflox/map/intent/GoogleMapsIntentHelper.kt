package com.koflox.map.intent

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.net.toUri

class GoogleMapsIntentHelper(private val application: Application) {

    companion object {
        internal const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }

    fun isInstalled(): Boolean = try {
        application.packageManager.getPackageInfo(GOOGLE_MAPS_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun buildNavigationUri(latitude: Double, longitude: Double): Uri =
        "google.navigation:q=$latitude,$longitude&mode=b".toUri()

    fun buildSearchUri(latitude: Double, longitude: Double, query: String): Uri {
        val encodedQuery = Uri.encode(query)
        return "geo:$latitude,$longitude?q=$encodedQuery".toUri()
    }

    fun createViewIntent(uri: Uri): Intent =
        Intent(Intent.ACTION_VIEW, uri).apply { setPackage(GOOGLE_MAPS_PACKAGE) }
}
