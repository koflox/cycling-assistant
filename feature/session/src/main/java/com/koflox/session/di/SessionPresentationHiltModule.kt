package com.koflox.session.di

import android.content.Context
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.di.DefaultDispatcher
import com.koflox.di.IoDispatcher
import com.koflox.di.SessionErrorMapper
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.presentation.error.SessionErrorMessageMapper
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.mapper.SessionUiMapperImpl
import com.koflox.session.presentation.session.timer.SessionTimerFactory
import com.koflox.session.presentation.session.timer.SessionTimerImpl
import com.koflox.session.presentation.sessionslist.SessionsListUiMapper
import com.koflox.session.presentation.sessionslist.SessionsListUiMapperImpl
import com.koflox.session.presentation.share.GpxShareErrorMapper
import com.koflox.session.presentation.share.GpxShareErrorMapperImpl
import com.koflox.session.presentation.share.SessionGpxSharer
import com.koflox.session.presentation.share.SessionGpxSharerImpl
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.SessionImageSharerImpl
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.ShareErrorMapperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideSessionUiMapper(
        localizedContextProvider: com.koflox.designsystem.context.LocalizedContextProvider,
    ): SessionUiMapper = SessionUiMapperImpl(
        localizedContextProvider = localizedContextProvider,
    )

    @Provides
    @Singleton
    fun provideSessionTimerFactory(
        currentTimeProvider: CurrentTimeProvider,
    ): SessionTimerFactory = SessionTimerFactory { scope ->
        SessionTimerImpl(scope, currentTimeProvider)
    }

    @Provides
    @Singleton
    fun provideSessionsListUiMapper(
        localizedContextProvider: com.koflox.designsystem.context.LocalizedContextProvider,
    ): SessionsListUiMapper = SessionsListUiMapperImpl(
        localizedContextProvider = localizedContextProvider,
    )

    @Provides
    @Singleton
    fun provideSessionImageSharer(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): SessionImageSharer = SessionImageSharerImpl(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideShareErrorMapper(): ShareErrorMapper = ShareErrorMapperImpl()

    @Provides
    @Singleton
    fun provideSessionGpxSharer(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): SessionGpxSharer = SessionGpxSharerImpl(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideGpxShareErrorMapper(): GpxShareErrorMapper = GpxShareErrorMapperImpl()
}
