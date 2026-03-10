package com.koflox.session.di

import android.content.Context
import com.koflox.concurrent.ConcurrentFactory
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.di.DefaultDispatcher
import com.koflox.di.IoDispatcher
import com.koflox.di.SessionDaoFactory
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.mapper.SessionMapperImpl
import com.koflox.session.data.repository.SessionFlushDecider
import com.koflox.session.data.repository.SessionFlushDeciderImpl
import com.koflox.session.data.repository.SessionRepositoryImpl
import com.koflox.session.data.repository.StatsDisplayRepositoryImpl
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.data.source.local.SessionLocalDataSourceImpl
import com.koflox.session.data.source.local.StatsDisplayLocalDataSource
import com.koflox.session.data.source.local.StatsDisplayPreferencesDataStore
import com.koflox.session.data.source.runtime.SessionRuntimeDataSource
import com.koflox.session.data.source.runtime.SessionRuntimeDataSourceImpl
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.domain.repository.StatsDisplayRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SessionDataHiltModule {

    @Provides
    @Singleton
    fun provideSessionMapper(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
    ): SessionMapper = SessionMapperImpl(
        dispatcherDefault = dispatcherDefault,
    )

    @Provides
    @Singleton
    fun provideSessionLocalDataSource(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        @SessionDaoFactory daoFactory: ConcurrentFactory<com.koflox.session.data.source.local.dao.SessionDao>,
    ): SessionLocalDataSource = SessionLocalDataSourceImpl(
        dispatcherIo = dispatcherIo,
        daoFactory = daoFactory,
    )

    @Provides
    @Singleton
    fun provideSessionRuntimeDataSource(): SessionRuntimeDataSource = SessionRuntimeDataSourceImpl()

    @Provides
    @Singleton
    fun provideStatsDisplayLocalDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): StatsDisplayLocalDataSource = StatsDisplayPreferencesDataStore(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideSessionFlushDecider(
        currentTimeProvider: CurrentTimeProvider,
    ): SessionFlushDecider = SessionFlushDeciderImpl(
        currentTimeProvider = currentTimeProvider,
    )

    @Provides
    @Singleton
    fun provideSessionRepository(
        localDataSource: SessionLocalDataSource,
        runtimeDataSource: SessionRuntimeDataSource,
        mapper: SessionMapper,
        flushDecider: SessionFlushDecider,
        currentTimeProvider: CurrentTimeProvider,
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
    ): SessionRepository = SessionRepositoryImpl(
        localDataSource = localDataSource,
        runtimeDataSource = runtimeDataSource,
        mapper = mapper,
        flushDecider = flushDecider,
        currentTimeProvider = currentTimeProvider,
        dispatcherDefault = dispatcherDefault,
    )

    @Provides
    @Singleton
    fun provideStatsDisplayRepository(
        localDataSource: StatsDisplayLocalDataSource,
    ): StatsDisplayRepository = StatsDisplayRepositoryImpl(
        localDataSource = localDataSource,
    )
}
