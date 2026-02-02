package com.koflox.settings.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.settings.api.LocaleProvider
import com.koflox.settings.api.RiderProfileProvider
import com.koflox.settings.api.ThemeProvider
import com.koflox.settings.data.repository.SettingsRepositoryImpl
import com.koflox.settings.data.source.SettingsDataStore
import com.koflox.settings.data.source.SettingsLocalDataSource
import com.koflox.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val dataModule = module {
    single<SettingsLocalDataSource> {
        SettingsDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
    single {
        SettingsRepositoryImpl(localDataSource = get())
    }
    single<SettingsRepository> { get<SettingsRepositoryImpl>() }
    single<ThemeProvider> { get<SettingsRepositoryImpl>() }
    single<LocaleProvider> { get<SettingsRepositoryImpl>() }
    single<RiderProfileProvider> { get<SettingsRepositoryImpl>() }
}
