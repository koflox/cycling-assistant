package com.koflox.poi.di

import android.content.Context
import com.koflox.di.IoDispatcher
import com.koflox.poi.data.repository.PoiRepositoryImpl
import com.koflox.poi.data.source.PoiLocalDataSource
import com.koflox.poi.data.source.PoiPreferencesDataStore
import com.koflox.poi.domain.repository.PoiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PoiDataHiltModule {

    @Provides
    @Singleton
    fun providePoiLocalDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): PoiLocalDataSource = PoiPreferencesDataStore(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun providePoiRepository(
        localDataSource: PoiLocalDataSource,
    ): PoiRepository = PoiRepositoryImpl(
        localDataSource = localDataSource,
    )
}
