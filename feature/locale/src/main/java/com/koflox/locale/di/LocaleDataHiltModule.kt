package com.koflox.locale.di

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.di.IoDispatcher
import com.koflox.di.LocaleDaoFactory
import com.koflox.locale.data.repository.LocaleRepositoryImpl
import com.koflox.locale.data.source.LocaleLocalDataSource
import com.koflox.locale.data.source.LocaleRoomDataSource
import com.koflox.locale.data.source.local.dao.LocaleDao
import com.koflox.locale.domain.repository.LocaleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocaleDataHiltModule {

    @Provides
    @Singleton
    fun provideLocaleLocalDataSource(
        @LocaleDaoFactory daoFactory: ConcurrentFactory<LocaleDao>,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): LocaleLocalDataSource = LocaleRoomDataSource(
        daoFactory = daoFactory,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideLocaleRepository(
        localDataSource: LocaleLocalDataSource,
    ): LocaleRepository = LocaleRepositoryImpl(
        localDataSource = localDataSource,
    )
}
