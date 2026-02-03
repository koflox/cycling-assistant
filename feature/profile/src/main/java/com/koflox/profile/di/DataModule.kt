package com.koflox.profile.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.profile.data.repository.ProfileRepositoryImpl
import com.koflox.profile.data.source.ProfileDataStore
import com.koflox.profile.data.source.ProfileLocalDataSource
import com.koflox.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val dataSourceModule = module {
    single<ProfileLocalDataSource> {
        ProfileDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<ProfileRepository> {
        ProfileRepositoryImpl(localDataSource = get())
    }
}

internal val dataModules = listOf(dataSourceModule, repoModule)
