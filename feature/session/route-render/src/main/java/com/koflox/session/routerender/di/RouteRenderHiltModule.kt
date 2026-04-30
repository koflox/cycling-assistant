package com.koflox.session.routerender.di

import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.mapper.SessionUiMapperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RouteRenderHiltModule {

    @Provides
    @Singleton
    fun provideSessionUiMapper(
        localizedContextProvider: LocalizedContextProvider,
    ): SessionUiMapper = SessionUiMapperImpl(
        localizedContextProvider = localizedContextProvider,
    )
}
