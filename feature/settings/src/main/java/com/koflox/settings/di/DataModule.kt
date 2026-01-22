package com.koflox.settings.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.settings.api.ThemeProvider
import com.koflox.settings.data.repository.SettingsRepositoryImpl
import com.koflox.settings.data.source.SettingsDataStore
import com.koflox.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val dataModule = module {
    single {
        SettingsDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
    single {
        SettingsRepositoryImpl(dataStore = get())
    }
    single<SettingsRepository> { get<SettingsRepositoryImpl>() }
    single<ThemeProvider> { get<SettingsRepositoryImpl>() }
}
