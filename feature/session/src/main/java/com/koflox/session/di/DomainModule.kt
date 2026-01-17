package com.koflox.session.di

import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCaseImpl
import com.koflox.session.domain.usecase.CreateSessionUseCase
import com.koflox.session.domain.usecase.CreateSessionUseCaseImpl
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
        )
    }
    factory<UpdateSessionStatusUseCase> {
        UpdateSessionStatusUseCaseImpl(
            activeSessionUseCase = get(),
            sessionRepository = get(),
        )
    }
    factory<UpdateSessionLocationUseCase> {
        UpdateSessionLocationUseCaseImpl(
            activeSessionUseCase = get(),
            sessionRepository = get(),
            distanceCalculator = get(),
        )
    }
}
