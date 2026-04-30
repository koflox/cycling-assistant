package com.koflox.strava.impl.data.source.remote

import com.koflox.di.IoDispatcher
import com.koflox.strava.impl.data.api.StravaAuthApi
import com.koflox.strava.impl.data.api.dto.TokenResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal interface StravaAuthRemoteDataSource {
    suspend fun exchangeAuthorizationCode(code: String): TokenResponse
    suspend fun refreshToken(refreshToken: String): TokenResponse
}

internal class StravaAuthRemoteDataSourceImpl @Inject constructor(
    private val api: StravaAuthApi,
    @param:IoDispatcher private val dispatcherIo: CoroutineDispatcher,
) : StravaAuthRemoteDataSource {

    override suspend fun exchangeAuthorizationCode(code: String): TokenResponse =
        withContext(dispatcherIo) { api.exchangeAuthorizationCode(code) }

    override suspend fun refreshToken(refreshToken: String): TokenResponse =
        withContext(dispatcherIo) { api.refreshToken(refreshToken) }
}
