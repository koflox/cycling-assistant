package com.koflox.strava.impl.di

import com.koflox.strava.api.usecase.ObserveStravaSyncStatusUseCase
import com.koflox.strava.api.usecase.StravaSyncUseCase
import com.koflox.strava.impl.data.mapper.TokenMapper
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import com.koflox.strava.impl.data.source.remote.StravaAuthRemoteDataSource
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import com.koflox.strava.impl.domain.repository.StravaUploadRepository
import com.koflox.strava.impl.domain.usecase.ObserveStravaSyncStatusUseCaseImpl
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCase
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCaseImpl
import com.koflox.strava.impl.domain.usecase.StravaSyncUseCaseImpl
import com.koflox.strava.impl.work.StravaWorkScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object StravaDomainHiltModule {

    @Provides
    @Singleton
    fun provideStravaSyncUseCase(
        authRepository: StravaAuthRepository,
        syncRepository: StravaSyncRepository,
        uploadRepository: StravaUploadRepository,
        workScheduler: StravaWorkScheduler,
    ): StravaSyncUseCase = StravaSyncUseCaseImpl(
        authRepository = authRepository,
        syncRepository = syncRepository,
        uploadRepository = uploadRepository,
        workScheduler = workScheduler,
    )

    @Provides
    fun provideObserveStravaSyncStatusUseCase(
        repository: StravaSyncRepository,
    ): ObserveStravaSyncStatusUseCase = ObserveStravaSyncStatusUseCaseImpl(
        repository = repository,
    )

    @Provides
    @Singleton
    fun provideStravaAuthUseCase(
        authRepository: StravaAuthRepository,
        remoteDataSource: StravaAuthRemoteDataSource,
        tokenLocalDataSource: StravaTokenLocalDataSource,
        tokenMapper: TokenMapper,
    ): StravaAuthUseCase = StravaAuthUseCaseImpl(
        authRepository = authRepository,
        remoteDataSource = remoteDataSource,
        tokenLocalDataSource = tokenLocalDataSource,
        tokenMapper = tokenMapper,
    )
}
