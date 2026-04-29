package com.koflox.strava.impl.data.repository

import com.koflox.strava.impl.data.api.dto.UploadResponse
import com.koflox.strava.impl.data.mapper.StravaErrorMapper
import com.koflox.strava.impl.data.mapper.UploadStatusMapper
import com.koflox.strava.impl.data.source.remote.StravaUploadRemoteDataSource
import com.koflox.strava.impl.domain.model.StravaError
import com.koflox.strava.impl.domain.model.UploadStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class StravaUploadRepositoryImplTest {

    companion object {
        private const val SESSION_ID = "session-1"
        private const val UPLOAD_ID = 42L
    }

    private val remoteDataSource: StravaUploadRemoteDataSource = mockk()
    private val uploadStatusMapper: UploadStatusMapper = mockk()
    private val errorMapper: StravaErrorMapper = mockk()

    private fun createRepository() = StravaUploadRepositoryImpl(
        remoteDataSource = remoteDataSource,
        uploadStatusMapper = uploadStatusMapper,
        errorMapper = errorMapper,
    )

    @Test
    fun `uploadGpx maps response via UploadStatusMapper`() = runTest {
        val response = UploadResponse(id = UPLOAD_ID, status = "processing")
        val expected = UploadStatus.Processing(uploadId = UPLOAD_ID)
        coEvery { remoteDataSource.uploadGpx(any(), SESSION_ID, "ride") } returns response
        every { uploadStatusMapper.toDomain(response) } returns expected

        val result = createRepository().uploadGpx(byteArrayOf(), SESSION_ID, "ride")

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `uploadGpx maps Ktor exception via StravaErrorMapper`() = runTest {
        val original = IOException("offline")
        val mapped = StravaError.Network()
        coEvery { remoteDataSource.uploadGpx(any(), any(), any()) } throws original
        every { errorMapper.map(original) } returns mapped

        val result = createRepository().uploadGpx(byteArrayOf(), SESSION_ID, "ride")

        assertTrue(result.isFailure)
        assertEquals(mapped, result.exceptionOrNull())
    }

    @Test
    fun `getUploadStatus maps response`() = runTest {
        val response = UploadResponse(id = UPLOAD_ID, activityId = 99L)
        val expected = UploadStatus.Ready(uploadId = UPLOAD_ID, activityId = 99L)
        coEvery { remoteDataSource.getUploadStatus(UPLOAD_ID) } returns response
        every { uploadStatusMapper.toDomain(response) } returns expected

        val result = createRepository().getUploadStatus(UPLOAD_ID)

        assertEquals(expected, result.getOrThrow())
    }
}
