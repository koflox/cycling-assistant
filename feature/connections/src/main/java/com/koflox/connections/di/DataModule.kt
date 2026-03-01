package com.koflox.connections.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.connections.data.mapper.PairedDeviceMapper
import com.koflox.connections.data.mapper.PairedDeviceMapperImpl
import com.koflox.connections.data.repository.PairedDeviceRepositoryImpl
import com.koflox.connections.data.source.PairedDeviceLocalDataSource
import com.koflox.connections.data.source.PairedDeviceLocalDataSourceImpl
import com.koflox.connections.domain.repository.PairedDeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module
import org.koin.dsl.module

private val dataModule = module {
    single<PairedDeviceMapper> { PairedDeviceMapperImpl() }
}

private val dataSourceModule = module {
    single<PairedDeviceLocalDataSource> {
        PairedDeviceLocalDataSourceImpl(
            daoFactory = get(ConnectionsQualifier.DaoFactory),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<PairedDeviceRepository> {
        PairedDeviceRepositoryImpl(
            localDataSource = get(),
            mapper = get(),
        )
    }
}

internal val dataModules: List<Module> = listOf(
    dataModule,
    dataSourceModule,
    repoModule,
)
