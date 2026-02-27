package com.koflox.map.intent

import android.app.Application
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.koflox.map.intent.GoogleMapsIntentHelper.Companion.GOOGLE_MAPS_PACKAGE
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GoogleMapsIntentHelperTest {

    private val application: Application = mockk()
    private val packageManager: PackageManager = mockk()
    private lateinit var helper: GoogleMapsIntentHelper

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { application.packageManager } returns packageManager
        helper = GoogleMapsIntentHelper(application)
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `isInstalled returns true when package exists`() {
        every { packageManager.getPackageInfo(GOOGLE_MAPS_PACKAGE, 0) } returns PackageInfo()

        assertTrue(helper.isInstalled())
    }

    @Test
    fun `isInstalled returns false when NameNotFoundException`() {
        every { packageManager.getPackageInfo(GOOGLE_MAPS_PACKAGE, 0) } throws
            PackageManager.NameNotFoundException()

        assertFalse(helper.isInstalled())
    }

    @Test
    fun `buildNavigationUri returns correct URI format`() {
        val expectedUri: Uri = mockk()
        every { Uri.parse("google.navigation:q=52.52,13.405&mode=b") } returns expectedUri

        val result = helper.buildNavigationUri(latitude = 52.52, longitude = 13.405)

        assertEquals(expectedUri, result)
    }

    @Test
    fun `buildSearchUri encodes query correctly`() {
        val expectedUri: Uri = mockk()
        every { Uri.encode("coffee shop") } returns "coffee%20shop"
        every { Uri.parse("geo:52.52,13.405?q=coffee%20shop") } returns expectedUri

        val result = helper.buildSearchUri(latitude = 52.52, longitude = 13.405, query = "coffee shop")

        assertEquals(expectedUri, result)
    }

    @Test
    fun `createViewIntent sets ACTION_VIEW and package`() {
        mockkConstructor(Intent::class)
        try {
            val uri: Uri = mockk()
            every { anyConstructed<Intent>().setPackage(GOOGLE_MAPS_PACKAGE) } returns mockk()

            val intent = helper.createViewIntent(uri)

            verify { anyConstructed<Intent>().setPackage(GOOGLE_MAPS_PACKAGE) }
        } finally {
            unmockkConstructor(Intent::class)
        }
    }
}
