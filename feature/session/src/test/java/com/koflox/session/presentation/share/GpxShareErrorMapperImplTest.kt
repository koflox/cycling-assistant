package com.koflox.session.presentation.share

import android.content.Intent
import com.koflox.designsystem.text.UiText
import com.koflox.session.R
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GpxShareErrorMapperImplTest {

    private val mapper: GpxShareErrorMapper = GpxShareErrorMapperImpl()

    @Test
    fun `Success maps to null`() {
        val result = mapper.map(GpxShareResult.Success(mockk<Intent>()))
        assertNull(result)
    }

    @Test
    fun `CannotWriteFile maps to file error string`() {
        val result = mapper.map(GpxShareResult.CannotWriteFile)
        assertEquals(UiText.Resource(R.string.gpx_export_file_error), result)
    }

    @Test
    fun `NoAppAvailable maps to no app string`() {
        val result = mapper.map(GpxShareResult.NoAppAvailable)
        assertEquals(UiText.Resource(R.string.gpx_export_no_app), result)
    }
}
