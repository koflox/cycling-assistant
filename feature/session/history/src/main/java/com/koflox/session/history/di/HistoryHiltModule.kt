package com.koflox.session.history.di

import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.session.presentation.sessionslist.SessionsListUiMapper
import com.koflox.session.presentation.sessionslist.SessionsListUiMapperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object HistoryHiltModule {

    @Provides
    @Singleton
    fun provideSessionsListUiMapper(
        localizedContextProvider: LocalizedContextProvider,
    ): SessionsListUiMapper = SessionsListUiMapperImpl(
        localizedContextProvider = localizedContextProvider,
    )
}
