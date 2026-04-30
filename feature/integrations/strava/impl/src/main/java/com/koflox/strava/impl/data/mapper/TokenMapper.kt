package com.koflox.strava.impl.data.mapper

import com.koflox.strava.impl.data.api.dto.TokenResponse
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity

internal interface TokenMapper {
    fun toEntity(response: TokenResponse, fallbackAthleteName: String? = null): StravaTokenEntity
}

internal class TokenMapperImpl : TokenMapper {

    override fun toEntity(response: TokenResponse, fallbackAthleteName: String?): StravaTokenEntity {
        val athleteName = response.athlete?.let { athlete ->
            listOfNotNull(athlete.firstname, athlete.lastname)
                .joinToString(separator = " ")
                .takeIf { it.isNotBlank() }
                ?: athlete.username
        } ?: fallbackAthleteName ?: ""
        return StravaTokenEntity(
            id = StravaTokenEntity.SINGLETON_ID,
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            expiresAtSeconds = response.expiresAtSeconds,
            athleteId = response.athlete?.id ?: 0L,
            athleteName = athleteName,
        )
    }
}
