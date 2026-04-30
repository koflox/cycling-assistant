package com.koflox.strava.impl.di

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.di.IoDispatcher
import com.koflox.di.StravaSyncDaoFactory
import com.koflox.di.StravaTokenDaoFactory
import com.koflox.strava.impl.BuildConfig
import com.koflox.strava.impl.data.api.HttpClientProvider
import com.koflox.strava.impl.data.api.StravaAuthApi
import com.koflox.strava.impl.data.api.StravaAuthApiImpl
import com.koflox.strava.impl.data.api.StravaAuthenticatedClient
import com.koflox.strava.impl.data.api.StravaClientCredentials
import com.koflox.strava.impl.data.api.StravaTokenProvider
import com.koflox.strava.impl.data.api.StravaUploadApi
import com.koflox.strava.impl.data.api.StravaUploadApiImpl
import com.koflox.strava.impl.data.mapper.SessionSyncStatusMapper
import com.koflox.strava.impl.data.mapper.SessionSyncStatusMapperImpl
import com.koflox.strava.impl.data.mapper.StravaErrorMapper
import com.koflox.strava.impl.data.mapper.StravaErrorMapperImpl
import com.koflox.strava.impl.data.mapper.TokenMapper
import com.koflox.strava.impl.data.mapper.TokenMapperImpl
import com.koflox.strava.impl.data.mapper.UploadStatusMapper
import com.koflox.strava.impl.data.mapper.UploadStatusMapperImpl
import com.koflox.strava.impl.data.repository.StravaAuthRepositoryImpl
import com.koflox.strava.impl.data.repository.StravaSyncRepositoryImpl
import com.koflox.strava.impl.data.repository.StravaUploadRepositoryImpl
import com.koflox.strava.impl.data.source.local.StravaSyncLocalDataSource
import com.koflox.strava.impl.data.source.local.StravaSyncLocalDataSourceImpl
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSourceImpl
import com.koflox.strava.impl.data.source.local.dao.StravaSyncDao
import com.koflox.strava.impl.data.source.local.dao.StravaTokenDao
import com.koflox.strava.impl.data.source.remote.StravaAuthRemoteDataSource
import com.koflox.strava.impl.data.source.remote.StravaAuthRemoteDataSourceImpl
import com.koflox.strava.impl.data.source.remote.StravaUploadRemoteDataSource
import com.koflox.strava.impl.data.source.remote.StravaUploadRemoteDataSourceImpl
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import com.koflox.strava.impl.domain.repository.StravaSyncRepository
import com.koflox.strava.impl.domain.repository.StravaUploadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
internal object StravaDataHiltModule {

    @Provides
    @Singleton
    fun provideSessionSyncStatusMapper(): SessionSyncStatusMapper = SessionSyncStatusMapperImpl()

    @Provides
    @Singleton
    fun provideStravaSyncLocalDataSource(
        @StravaSyncDaoFactory daoFactory: ConcurrentFactory<StravaSyncDao>,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): StravaSyncLocalDataSource = StravaSyncLocalDataSourceImpl(
        daoFactory = daoFactory,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideStravaTokenLocalDataSource(
        @StravaTokenDaoFactory daoFactory: ConcurrentFactory<StravaTokenDao>,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): StravaTokenLocalDataSource = StravaTokenLocalDataSourceImpl(
        daoFactory = daoFactory,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideStravaSyncRepository(
        localDataSource: StravaSyncLocalDataSource,
        mapper: SessionSyncStatusMapper,
        timeProvider: CurrentTimeProvider,
    ): StravaSyncRepository = StravaSyncRepositoryImpl(
        localDataSource = localDataSource,
        mapper = mapper,
        timeProvider = timeProvider,
    )

    @Provides
    @Singleton
    fun provideStravaAuthRepository(
        localDataSource: StravaTokenLocalDataSource,
    ): StravaAuthRepository = StravaAuthRepositoryImpl(
        localDataSource = localDataSource,
    )

    @Provides
    @Singleton
    fun provideStravaClientCredentials(): StravaClientCredentials = StravaClientCredentials(
        clientId = BuildConfig.STRAVA_CLIENT_ID,
        clientSecret = BuildConfig.STRAVA_CLIENT_SECRET,
    )

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClientProvider.createUnauthenticated(isDebug = BuildConfig.DEBUG)

    @Provides
    @Singleton
    @StravaAuthenticatedClient
    fun provideAuthenticatedHttpClient(
        tokenProvider: StravaTokenProvider,
    ): HttpClient = HttpClientProvider.createAuthenticated(
        isDebug = BuildConfig.DEBUG,
        loadTokens = tokenProvider::loadTokens,
        refreshTokens = tokenProvider::refreshTokens,
    )

    @Provides
    @Singleton
    fun provideTokenMapper(): TokenMapper = TokenMapperImpl()

    @Provides
    @Singleton
    fun provideStravaAuthApi(
        client: HttpClient,
        credentials: StravaClientCredentials,
    ): StravaAuthApi = StravaAuthApiImpl(
        client = client,
        clientCredentials = credentials,
    )

    @Provides
    @Singleton
    fun provideStravaAuthRemoteDataSource(
        api: StravaAuthApi,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): StravaAuthRemoteDataSource = StravaAuthRemoteDataSourceImpl(
        api = api,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideUploadStatusMapper(): UploadStatusMapper = UploadStatusMapperImpl()

    @Provides
    @Singleton
    fun provideStravaErrorMapper(): StravaErrorMapper = StravaErrorMapperImpl()

    @Provides
    @Singleton
    fun provideStravaUploadApi(
        @StravaAuthenticatedClient client: HttpClient,
    ): StravaUploadApi = StravaUploadApiImpl(client = client)

    @Provides
    @Singleton
    fun provideStravaUploadRemoteDataSource(
        api: StravaUploadApi,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): StravaUploadRemoteDataSource = StravaUploadRemoteDataSourceImpl(
        api = api,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideStravaUploadRepository(
        remoteDataSource: StravaUploadRemoteDataSource,
        uploadStatusMapper: UploadStatusMapper,
        errorMapper: StravaErrorMapper,
    ): StravaUploadRepository = StravaUploadRepositoryImpl(
        remoteDataSource = remoteDataSource,
        uploadStatusMapper = uploadStatusMapper,
        errorMapper = errorMapper,
    )
}
