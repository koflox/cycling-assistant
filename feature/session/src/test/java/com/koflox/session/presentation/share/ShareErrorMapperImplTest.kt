package com.koflox.session.presentation.share

import android.content.Context
import android.content.Intent
import com.koflox.session.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ShareErrorMapperImplTest {

    companion object {
        private const val IMAGE_PROCESSING_ERROR = "Cannot process image"
        private const val NO_APP_AVAILABLE = "No app available"
    }

    private val context: Context = mockk()
    private lateinit var mapper: ShareErrorMapperImpl

    @Before
    fun setup() {
        every { context.getString(R.string.share_image_processing_error) } returns IMAGE_PROCESSING_ERROR
        every { context.getString(R.string.share_no_app_available) } returns NO_APP_AVAILABLE
        mapper = ShareErrorMapperImpl(context)
    }

    @Test
    fun `maps Success to null`() {
        val result = mapper.map(ShareResult.Success(mockk<Intent>()))

        assertNull(result)
    }

    @Test
    fun `maps CannotProcessTheImage to image processing error`() {
        val result = mapper.map(ShareResult.CannotProcessTheImage)

        assertEquals(IMAGE_PROCESSING_ERROR, result)
    }

    @Test
    fun `maps NoAppAvailable to no app available error`() {
        val result = mapper.map(ShareResult.NoAppAvailable)

        assertEquals(NO_APP_AVAILABLE, result)
    }
}
