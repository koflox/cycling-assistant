package com.koflox.error.di

import com.koflox.di.DefaultDispatcher
import com.koflox.error.mapper.DefaultErrorMessageMapper
import com.koflox.error.mapper.ErrorMessageMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ErrorHiltModule {

    @Provides
    @Singleton
    fun provideErrorMessageMapper(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
    ): ErrorMessageMapper = DefaultErrorMessageMapper(
        dispatcherDefault = dispatcherDefault,
    )
}
