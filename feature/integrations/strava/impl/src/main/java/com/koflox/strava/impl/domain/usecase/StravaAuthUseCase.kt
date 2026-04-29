package com.koflox.strava.impl.domain.usecase

import com.koflox.concurrent.suspendRunCatching
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.data.mapper.TokenMapper
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import com.koflox.strava.impl.data.source.remote.StravaAuthRemoteDataSource
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal interface StravaAuthUseCase {
    fun observeAuthState(): Flow<StravaAuthState>
    suspend fun login(authorizationCode: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}

internal class StravaAuthUseCaseImpl @Inject constructor(
    private val authRepository: StravaAuthRepository,
    private val remoteDataSource: StravaAuthRemoteDataSource,
    private val tokenLocalDataSource: StravaTokenLocalDataSource,
    private val tokenMapper: TokenMapper,
) : StravaAuthUseCase {

    override fun observeAuthState(): Flow<StravaAuthState> = authRepository.observeAuthState()

    override suspend fun login(authorizationCode: String): Result<Unit> = suspendRunCatching {
        val response = remoteDataSource.exchangeAuthorizationCode(authorizationCode)
        tokenLocalDataSource.upsert(tokenMapper.toEntity(response))
    }

    override suspend fun logout(): Result<Unit> = suspendRunCatching {
        authRepository.logout()
    }
}
