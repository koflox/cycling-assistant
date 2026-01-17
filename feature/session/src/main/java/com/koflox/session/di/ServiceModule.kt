package com.koflox.session.di

import com.koflox.session.service.SessionNotificationManager
import com.koflox.session.service.SessionNotificationManagerImpl
import com.koflox.session.service.SessionServiceController
import com.koflox.session.service.SessionServiceControllerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val serviceModule = module {
    single<SessionNotificationManager> {
        SessionNotificationManagerImpl(
            context = androidContext(),
            sessionUiMapper = get(),
        )
    }
    single<SessionServiceController> {
        SessionServiceControllerImpl(
            context = androidContext(),
        )
    }
}
