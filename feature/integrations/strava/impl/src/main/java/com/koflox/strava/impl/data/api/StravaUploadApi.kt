package com.koflox.strava.impl.data.api

import com.koflox.strava.impl.data.api.dto.UploadResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

internal interface StravaUploadApi {
    suspend fun uploadGpx(
        gpxBytes: ByteArray,
        externalId: String,
        name: String,
    ): UploadResponse

    suspend fun getUploadStatus(uploadId: Long): UploadResponse

    suspend fun activityExists(activityId: Long): Boolean
}

internal class StravaUploadApiImpl @Inject constructor(
    @param:StravaAuthenticatedClient private val client: HttpClient,
) : StravaUploadApi {

    private companion object {
        const val UPLOADS_URL = "https://www.strava.com/api/v3/uploads"
        const val ACTIVITIES_URL = "https://www.strava.com/api/v3/activities"
        const val FIELD_DATA_TYPE = "data_type"
        const val FIELD_ACTIVITY_TYPE = "activity_type"
        const val FIELD_EXTERNAL_ID = "external_id"
        const val FIELD_NAME = "name"
        const val FIELD_TRAINER = "trainer"
        const val FIELD_COMMUTE = "commute"
        const val FIELD_FILE = "file"
        const val DATA_TYPE_GPX = "gpx"
        const val ACTIVITY_TYPE_RIDE = "ride"
        const val BOOL_FALSE = "0"
    }

    override suspend fun uploadGpx(
        gpxBytes: ByteArray,
        externalId: String,
        name: String,
    ): UploadResponse {
        val multipart = MultiPartFormDataContent(
            formData {
                append(FIELD_DATA_TYPE, DATA_TYPE_GPX)
                append(FIELD_ACTIVITY_TYPE, ACTIVITY_TYPE_RIDE)
                append(FIELD_EXTERNAL_ID, externalId)
                append(FIELD_NAME, name)
                append(FIELD_TRAINER, BOOL_FALSE)
                append(FIELD_COMMUTE, BOOL_FALSE)
                append(
                    key = FIELD_FILE,
                    value = gpxBytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$externalId.gpx\"")
                        append(HttpHeaders.ContentType, "application/gpx+xml")
                    },
                )
            },
        )
        return client.post(UPLOADS_URL) { setBody(multipart) }.body()
    }

    override suspend fun getUploadStatus(uploadId: Long): UploadResponse =
        client.get("$UPLOADS_URL/$uploadId").body()

    override suspend fun activityExists(activityId: Long): Boolean = try {
        client.get("$ACTIVITIES_URL/$activityId")
        true
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.NotFound) {
            false
        } else {
            throw e
        }
    }

}
