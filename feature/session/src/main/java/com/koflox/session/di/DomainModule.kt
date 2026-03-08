package com.koflox.session.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCaseImpl
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCaseImpl
import com.koflox.session.domain.usecase.CheckLocationEnabledUseCase
import com.koflox.session.domain.usecase.CheckLocationEnabledUseCaseImpl
import com.koflox.session.domain.usecase.CreateSessionUseCase
import com.koflox.session.domain.usecase.CreateSessionUseCaseImpl
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.domain.usecase.GetAllSessionsUseCaseImpl
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCaseImpl
import com.koflox.session.domain.usecase.ObserveActiveSessionRouteUseCase
import com.koflox.session.domain.usecase.ObserveActiveSessionRouteUseCaseImpl
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCaseImpl
import com.koflox.session.domain.usecase.PowerReadingBuffer
import com.koflox.session.domain.usecase.PowerReadingBufferImpl
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCaseImpl
import com.koflox.session.domain.usecase.UpdateSessionPowerUseCase
import com.koflox.session.domain.usecase.UpdateSessionPowerUseCaseImpl
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCaseImpl
import com.koflox.session.domain.usecase.UpdateStatsDisplayConfigUseCase
import com.koflox.session.domain.usecase.UpdateStatsDisplayConfigUseCaseImpl
import kotlinx.coroutines.sync.Mutex
import org.koin.dsl.module

internal val domainModule = module {
    single<PowerReadingBuffer> {
        PowerReadingBufferImpl()
    }
    single<Mutex>(SessionQualifier.SessionMutex) { Mutex() }
    single<ActiveSessionUseCase> {
        ActiveSessionUseCaseImpl(
            sessionRepository = get(),
        )
    }
    factory<CreateSessionUseCase> {
        CreateSessionUseCaseImpl(
            sessionRepository = get(),
            idGenerator = get(),
            getUserLocationUseCase = get(),
            locationValidator = get(),
            currentTimeProvider = get(),
        )
    }
    single<UpdateSessionStatusUseCase> {
        UpdateSessionStatusUseCaseImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            sessionMutex = get(SessionQualifier.SessionMutex),
            activeSessionUseCase = get(),
            sessionRepository = get(),
            currentTimeProvider = get(),
        )
    }
    // single: holds stateful speedBuffer and locationSmoother that must not be
    // duplicated across injection sites. State is reset per segment in saveSegmentStartPoint().
    single<UpdateSessionLocationUseCase> {
        UpdateSessionLocationUseCaseImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            sessionMutex = get(SessionQualifier.SessionMutex),
            activeSessionUseCase = get(),
            sessionRepository = get(),
            distanceCalculator = get(),
            altitudeCalculator = get(),
            locationValidator = get(),
            locationSmoother = get(),
            powerReadingBuffer = get(),
        )
    }
    // single: holds stateful lastReadingTimestampMs and powerBuffer for energy delta and max power filtering.
    single<UpdateSessionPowerUseCase> {
        UpdateSessionPowerUseCaseImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            sessionMutex = get(SessionQualifier.SessionMutex),
            activeSessionUseCase = get(),
            sessionRepository = get(),
            powerReadingBuffer = get(),
        )
    }
    factory<CalculateSessionStatsUseCase> {
        CalculateSessionStatsUseCaseImpl(
            getSessionByIdUseCase = get(),
            riderProfileUseCase = get(),
        )
    }
    factory<GetAllSessionsUseCase> {
        GetAllSessionsUseCaseImpl(
            sessionRepository = get(),
        )
    }
    factory<GetSessionByIdUseCase> {
        GetSessionByIdUseCaseImpl(
            sessionRepository = get(),
        )
    }
    factory<CheckLocationEnabledUseCase> {
        CheckLocationEnabledUseCaseImpl(
            locationSettingsDataSource = get(),
        )
    }
    factory<ObserveActiveSessionRouteUseCase> {
        ObserveActiveSessionRouteUseCaseImpl(
            activeSessionUseCase = get(),
        )
    }
    factory<ObserveStatsDisplayConfigUseCase> {
        ObserveStatsDisplayConfigUseCaseImpl(
            repository = get(),
        )
    }
    factory<UpdateStatsDisplayConfigUseCase> {
        UpdateStatsDisplayConfigUseCaseImpl(
            repository = get(),
        )
    }
}
