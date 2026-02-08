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
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCaseImpl
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    single<ActiveSessionUseCase> {
        ActiveSessionUseCaseImpl(
            sessionRepository = get(),
        )
    }
    factory<CreateSessionUseCase> {
        CreateSessionUseCaseImpl(
            sessionRepository = get(),
            idGenerator = get(),
            locationDataSource = get(),
            locationValidator = get(),
        )
    }
    factory<UpdateSessionStatusUseCase> {
        UpdateSessionStatusUseCaseImpl(
            activeSessionUseCase = get(),
            sessionRepository = get(),
        )
    }
    // single: holds stateful speedBuffer and locationSmoother that must not be
    // duplicated across injection sites. State is reset per segment in saveSegmentStartPoint().
    single<UpdateSessionLocationUseCase> {
        UpdateSessionLocationUseCaseImpl(
            dispatcherDefault = get(DispatchersQualifier.Default),
            activeSessionUseCase = get(),
            sessionRepository = get(),
            distanceCalculator = get(),
            altitudeCalculator = get(),
            locationValidator = get(),
            locationSmoother = get(),
            idGenerator = get(),
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
}
