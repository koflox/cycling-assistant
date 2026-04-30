package com.koflox.session.init.di

import com.koflox.di.DefaultDispatcher
import com.koflox.di.SessionErrorMapper
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.presentation.error.SessionErrorMessageMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SessionPresentationHiltModule {

    @Provides
    @Singleton
    @SessionErrorMapper
    fun provideSessionErrorMessageMapper(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        defaultMapper: ErrorMessageMapper,
    ): ErrorMessageMapper = SessionErrorMessageMapper(
        dispatcherDefault = dispatcherDefault,
        defaultMapper = defaultMapper,
    )
}
