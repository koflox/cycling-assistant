package com.koflox.theme.di

import android.content.Context
import com.koflox.di.IoDispatcher
import com.koflox.theme.data.repository.ThemeRepositoryImpl
import com.koflox.theme.data.source.ThemeDataStore
import com.koflox.theme.data.source.ThemeLocalDataSource
import com.koflox.theme.domain.repository.ThemeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ThemeDataHiltModule {

    @Provides
    @Singleton
    fun provideThemeLocalDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): ThemeLocalDataSource = ThemeDataStore(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideThemeRepository(
        localDataSource: ThemeLocalDataSource,
    ): ThemeRepository = ThemeRepositoryImpl(
        localDataSource = localDataSource,
    )
}
