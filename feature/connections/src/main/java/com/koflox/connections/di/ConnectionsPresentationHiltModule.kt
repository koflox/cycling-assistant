package com.koflox.connections.di

import com.koflox.connections.presentation.error.ConnectionsErrorMessageMapper
import com.koflox.di.ConnectionsErrorMapper
import com.koflox.di.DefaultDispatcher
import com.koflox.error.mapper.ErrorMessageMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ConnectionsPresentationHiltModule {

    @Provides
    @Singleton
    @ConnectionsErrorMapper
    fun provideConnectionsErrorMessageMapper(
        defaultMapper: ErrorMessageMapper,
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
    ): ErrorMessageMapper = ConnectionsErrorMessageMapper(
        defaultMapper = defaultMapper,
        dispatcherDefault = dispatcherDefault,
    )
}
