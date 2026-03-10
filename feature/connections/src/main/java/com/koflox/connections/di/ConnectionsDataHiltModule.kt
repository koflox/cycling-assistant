package com.koflox.connections.di

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.connections.data.mapper.PairedDeviceMapper
import com.koflox.connections.data.mapper.PairedDeviceMapperImpl
import com.koflox.connections.data.repository.PairedDeviceRepositoryImpl
import com.koflox.connections.data.source.PairedDeviceLocalDataSource
import com.koflox.connections.data.source.PairedDeviceLocalDataSourceImpl
import com.koflox.connections.data.source.local.dao.PairedDeviceDao
import com.koflox.connections.domain.repository.PairedDeviceRepository
import com.koflox.di.ConnectionsDaoFactory
import com.koflox.di.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ConnectionsDataHiltModule {

    @Provides
    @Singleton
    fun providePairedDeviceMapper(): PairedDeviceMapper = PairedDeviceMapperImpl()

    @Provides
    @Singleton
    fun providePairedDeviceLocalDataSource(
        @ConnectionsDaoFactory daoFactory: ConcurrentFactory<PairedDeviceDao>,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): PairedDeviceLocalDataSource = PairedDeviceLocalDataSourceImpl(
        daoFactory = daoFactory,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun providePairedDeviceRepository(
        localDataSource: PairedDeviceLocalDataSource,
        mapper: PairedDeviceMapper,
    ): PairedDeviceRepository = PairedDeviceRepositoryImpl(
        localDataSource = localDataSource,
        mapper = mapper,
    )
}
