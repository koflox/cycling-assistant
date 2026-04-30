package com.koflox.strava.impl.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_at")
    val expiresAtSeconds: Long,
    val athlete: AthleteResponse? = null,
)

@Serializable
internal data class AthleteResponse(
    val id: Long,
    val firstname: String? = null,
    val lastname: String? = null,
    val username: String? = null,
)
