package com.koflox.strava.impl.data.mapper

import com.koflox.strava.impl.data.api.dto.UploadResponse
import com.koflox.strava.impl.domain.model.UploadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UploadStatusMapperImplTest {

    private val mapper: UploadStatusMapper = UploadStatusMapperImpl()

    @Test
    fun `toDomain returns Processing when no activityId and no error`() {
        val response = UploadResponse(id = 1L, status = "Your activity is still being processed.")

        val result = mapper.toDomain(response)

        assertEquals(UploadStatus.Processing(uploadId = 1L), result)
    }

    @Test
    fun `toDomain returns Ready when activityId present`() {
        val response = UploadResponse(id = 1L, activityId = 99L, status = "Your activity is ready.")

        val result = mapper.toDomain(response)

        assertEquals(UploadStatus.Ready(uploadId = 1L, activityId = 99L), result)
    }

    @Test
    fun `toDomain treats duplicate-of-activity error as Ready`() {
        val response = UploadResponse(id = 1L, error = "duplicate of activity 555")

        val result = mapper.toDomain(response)

        assertEquals(UploadStatus.Ready(uploadId = 1L, activityId = 555L), result)
    }

    @Test
    fun `toDomain returns Failed for non-duplicate error`() {
        val response = UploadResponse(id = 1L, error = "the gpx file is not valid")

        val result = mapper.toDomain(response)

        assertEquals(UploadStatus.Failed(uploadId = 1L, message = "the gpx file is not valid"), result)
    }

    @Test
    fun `extractDuplicateActivityId parses standard format`() {
        assertEquals(123L, mapper.extractDuplicateActivityId("duplicate of activity 123"))
    }

    @Test
    fun `extractDuplicateActivityId returns null for unrelated message`() {
        assertNull(mapper.extractDuplicateActivityId("the gpx file is not valid"))
    }
}
