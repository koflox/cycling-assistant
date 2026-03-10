package com.koflox.profile.di

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.di.IoDispatcher
import com.koflox.di.ProfileDaoFactory
import com.koflox.profile.data.repository.ProfileRepositoryImpl
import com.koflox.profile.data.source.ProfileLocalDataSource
import com.koflox.profile.data.source.ProfileRoomDataSource
import com.koflox.profile.data.source.local.dao.ProfileDao
import com.koflox.profile.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ProfileDataHiltModule {

    @Provides
    @Singleton
    fun provideProfileLocalDataSource(
        @ProfileDaoFactory daoFactory: ConcurrentFactory<ProfileDao>,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): ProfileLocalDataSource = ProfileRoomDataSource(
        daoFactory = daoFactory,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideProfileRepository(
        localDataSource: ProfileLocalDataSource,
    ): ProfileRepository = ProfileRepositoryImpl(
        localDataSource = localDataSource,
    )
}
