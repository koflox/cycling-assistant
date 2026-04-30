package com.koflox.strava.impl.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UploadResponse(
    val id: Long,
    @SerialName("external_id")
    val externalId: String? = null,
    @SerialName("activity_id")
    val activityId: Long? = null,
    val status: String? = null,
    val error: String? = null,
)
