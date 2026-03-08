package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.session.service.PendingSessionAction
import com.koflox.session.service.PendingSessionActionConsumer
import com.koflox.session.service.PendingSessionActionImpl
import com.koflox.session.service.PowerCollectionManager
import com.koflox.session.service.PowerCollectionManagerImpl
import com.koflox.session.service.PowerConnectionStateHolder
import com.koflox.session.service.PowerConnectionStateHolderImpl
import com.koflox.session.service.PowerConnectionStatePublisher
import com.koflox.session.service.SessionNotificationManager
import com.koflox.session.service.SessionNotificationManagerImpl
import com.koflox.session.service.SessionServiceController
import com.koflox.session.service.SessionServiceControllerImpl
import com.koflox.session.service.SessionTracker
import com.koflox.session.service.SessionTrackerImpl
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.binds
import org.koin.dsl.module

internal val serviceModule = module {
    single {
        PendingSessionActionImpl()
    } binds arrayOf(
        PendingSessionAction::class,
        PendingSessionActionConsumer::class,
    )
    single<SessionNotificationManager> {
        SessionNotificationManagerImpl(
            context = androidContext(),
            localizedContextProvider = get(),
            sessionUiMapper = get(),
            observeThemeUseCase = get(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
    single<SessionServiceController> {
        SessionServiceControllerImpl(
            context = androidContext(),
        )
    }
    single {
        PowerConnectionStateHolderImpl()
    } binds arrayOf(
        PowerConnectionStateHolder::class,
        PowerConnectionStatePublisher::class,
    )
    factory<PowerCollectionManager> {
        PowerCollectionManagerImpl(
            sessionPowerMeterUseCase = get(),
            updateSessionPowerUseCase = get(),
            powerConnectionStatePublisher = get(),
        )
    }
    factory<SessionTracker> {
        SessionTrackerImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            activeSessionUseCase = get(),
            updateSessionLocationUseCase = get(),
            updateSessionStatusUseCase = get(),
            locationDataSource = get(),
            locationSettingsDataSource = get(),
            nutritionReminderUseCase = get(),
            powerCollectionManager = get(),
            currentTimeProvider = get(),
        )
    }
}
