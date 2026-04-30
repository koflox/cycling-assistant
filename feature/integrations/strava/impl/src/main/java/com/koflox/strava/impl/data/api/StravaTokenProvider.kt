package com.koflox.strava.impl.data.api

import com.koflox.concurrent.suspendRunCatching
import com.koflox.strava.impl.data.mapper.TokenMapper
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import io.ktor.client.plugins.auth.providers.BearerTokens
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges the Strava token storage to Ktor's `Auth` plugin. Methods are wired into the
 * authenticated [HttpClientProvider.createAuthenticated] as `loadTokens` / `refreshTokens`
 * callbacks.
 *
 * Extracting this from the DI module's inline lambdas keeps the refresh policy unit-testable —
 * specifically the rule that a failed refresh must wipe the local token so the user is forced
 * to re-authorize (otherwise an invalid refresh token would loop forever).
 */
@Singleton
internal class StravaTokenProvider @Inject constructor(
    private val authApi: StravaAuthApi,
    private val tokenLocalDataSource: StravaTokenLocalDataSource,
    private val tokenMapper: TokenMapper,
) {

    suspend fun loadTokens(): BearerTokens? {
        val entity = tokenLocalDataSource.get() ?: return null
        return BearerTokens(entity.accessToken, entity.refreshToken)
    }

    suspend fun refreshTokens(refreshToken: String): BearerTokens? = suspendRunCatching {
        val response = authApi.refreshToken(refreshToken)
        val fallbackName = tokenLocalDataSource.get()?.athleteName
        tokenLocalDataSource.upsert(tokenMapper.toEntity(response, fallbackName))
        BearerTokens(response.accessToken, response.refreshToken)
    }.getOrElse {
        tokenLocalDataSource.delete()
        null
    }
}
