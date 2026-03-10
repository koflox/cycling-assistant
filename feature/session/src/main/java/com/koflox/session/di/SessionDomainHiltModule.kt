package com.koflox.session.di

import com.koflox.altitude.AltitudeCalculator
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.di.DefaultDispatcher
import com.koflox.di.SessionMutex
import com.koflox.distance.DistanceCalculator
import com.koflox.id.IdGenerator
import com.koflox.location.smoother.LocationSmoother
import com.koflox.location.usecase.GetUserLocationUseCase
import com.koflox.location.validator.LocationValidator
import com.koflox.profilesession.bridge.api.RiderProfileUseCase
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.domain.repository.StatsDisplayRepository
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.ActiveSessionUseCaseImpl
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCaseImpl
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
internal object SessionDomainHiltModule {

    @Provides
    @Singleton
    fun providePowerReadingBuffer(): PowerReadingBuffer = PowerReadingBufferImpl()

    @Provides
    @Singleton
    @SessionMutex
    fun provideSessionMutex(): Mutex = Mutex()

    @Provides
    @Singleton
    fun provideActiveSessionUseCase(
        sessionRepository: SessionRepository,
    ): ActiveSessionUseCase = ActiveSessionUseCaseImpl(
        sessionRepository = sessionRepository,
    )

    @Provides
    fun provideCreateSessionUseCase(
        sessionRepository: SessionRepository,
        idGenerator: IdGenerator,
        getUserLocationUseCase: GetUserLocationUseCase,
        locationValidator: LocationValidator,
        currentTimeProvider: CurrentTimeProvider,
    ): CreateSessionUseCase = CreateSessionUseCaseImpl(
        sessionRepository = sessionRepository,
        idGenerator = idGenerator,
        getUserLocationUseCase = getUserLocationUseCase,
        locationValidator = locationValidator,
        currentTimeProvider = currentTimeProvider,
    )

    @Provides
    @Singleton
    fun provideUpdateSessionStatusUseCase(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        @SessionMutex sessionMutex: Mutex,
        activeSessionUseCase: ActiveSessionUseCase,
        sessionRepository: SessionRepository,
        currentTimeProvider: CurrentTimeProvider,
    ): UpdateSessionStatusUseCase = UpdateSessionStatusUseCaseImpl(
        dispatcherDefault = dispatcherDefault,
        sessionMutex = sessionMutex,
        activeSessionUseCase = activeSessionUseCase,
        sessionRepository = sessionRepository,
        currentTimeProvider = currentTimeProvider,
    )

    // single: holds stateful speedBuffer and locationSmoother that must not be
    // duplicated across injection sites. State is reset per segment in saveSegmentStartPoint().
    @Provides
    @Singleton
    fun provideUpdateSessionLocationUseCase(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        @SessionMutex sessionMutex: Mutex,
        activeSessionUseCase: ActiveSessionUseCase,
        sessionRepository: SessionRepository,
        distanceCalculator: DistanceCalculator,
        altitudeCalculator: AltitudeCalculator,
        locationValidator: LocationValidator,
        locationSmoother: LocationSmoother,
        powerReadingBuffer: PowerReadingBuffer,
    ): UpdateSessionLocationUseCase = UpdateSessionLocationUseCaseImpl(
        dispatcherDefault = dispatcherDefault,
        sessionMutex = sessionMutex,
        activeSessionUseCase = activeSessionUseCase,
        sessionRepository = sessionRepository,
        distanceCalculator = distanceCalculator,
        altitudeCalculator = altitudeCalculator,
        locationValidator = locationValidator,
        locationSmoother = locationSmoother,
        powerReadingBuffer = powerReadingBuffer,
    )

    // single: holds stateful lastReadingTimestampMs and powerBuffer for energy delta and max power filtering.
    @Provides
    @Singleton
    fun provideUpdateSessionPowerUseCase(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        @SessionMutex sessionMutex: Mutex,
        activeSessionUseCase: ActiveSessionUseCase,
        sessionRepository: SessionRepository,
        powerReadingBuffer: PowerReadingBuffer,
    ): UpdateSessionPowerUseCase = UpdateSessionPowerUseCaseImpl(
        dispatcherDefault = dispatcherDefault,
        sessionMutex = sessionMutex,
        activeSessionUseCase = activeSessionUseCase,
        sessionRepository = sessionRepository,
        powerReadingBuffer = powerReadingBuffer,
    )

    @Provides
    fun provideCalculateSessionStatsUseCase(
        getSessionByIdUseCase: GetSessionByIdUseCase,
        riderProfileUseCase: RiderProfileUseCase,
    ): CalculateSessionStatsUseCase = CalculateSessionStatsUseCaseImpl(
        getSessionByIdUseCase = getSessionByIdUseCase,
        riderProfileUseCase = riderProfileUseCase,
    )

    @Provides
    fun provideGetAllSessionsUseCase(
        sessionRepository: SessionRepository,
    ): GetAllSessionsUseCase = GetAllSessionsUseCaseImpl(
        sessionRepository = sessionRepository,
    )

    @Provides
    fun provideGetSessionByIdUseCase(
        sessionRepository: SessionRepository,
    ): GetSessionByIdUseCase = GetSessionByIdUseCaseImpl(
        sessionRepository = sessionRepository,
    )

    @Provides
    fun provideObserveActiveSessionRouteUseCase(
        activeSessionUseCase: ActiveSessionUseCase,
    ): ObserveActiveSessionRouteUseCase = ObserveActiveSessionRouteUseCaseImpl(
        activeSessionUseCase = activeSessionUseCase,
    )

    @Provides
    fun provideObserveStatsDisplayConfigUseCase(
        repository: StatsDisplayRepository,
    ): ObserveStatsDisplayConfigUseCase = ObserveStatsDisplayConfigUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateStatsDisplayConfigUseCase(
        repository: StatsDisplayRepository,
    ): UpdateStatsDisplayConfigUseCase = UpdateStatsDisplayConfigUseCaseImpl(
        repository = repository,
    )
}
