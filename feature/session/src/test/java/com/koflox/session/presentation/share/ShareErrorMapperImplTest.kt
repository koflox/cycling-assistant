package com.koflox.session.presentation.share

import android.content.Intent
import com.koflox.designsystem.text.UiText
import com.koflox.session.R
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ShareErrorMapperImplTest {

    private lateinit var mapper: ShareErrorMapperImpl

    @Before
    fun setup() {
        mapper = ShareErrorMapperImpl()
    }

    @Test
    fun `maps Success to null`() {
        val result = mapper.map(ShareResult.Success(mockk<Intent>()))

        assertNull(result)
    }

    @Test
    fun `maps CannotProcessTheImage to image processing error UiText`() {
        val result = mapper.map(ShareResult.CannotProcessTheImage)

        assertEquals(UiText.Resource(R.string.share_image_processing_error), result)
    }

    @Test
    fun `maps NoAppAvailable to no app available UiText`() {
        val result = mapper.map(ShareResult.NoAppAvailable)

        assertEquals(UiText.Resource(R.string.share_no_app_available), result)
    }
}
