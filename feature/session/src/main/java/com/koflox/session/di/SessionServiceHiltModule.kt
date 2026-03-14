package com.koflox.session.di

import android.content.Context
import com.koflox.altitude.AltitudeCalculator
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.connectionsession.bridge.usecase.SessionPowerMeterUseCase
import com.koflox.di.IoDispatcher
import com.koflox.distance.DistanceCalculator
import com.koflox.location.smoother.LocationSmoother
import com.koflox.location.usecase.CheckLocationEnabledUseCase
import com.koflox.location.usecase.ObserveUserLocationUseCase
import com.koflox.location.validator.LocationValidator
import com.koflox.nutritionsession.bridge.usecase.NutritionReminderUseCase
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionPowerUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.domain.usecase.comparison.ComparisonSessionManager
import com.koflox.session.domain.usecase.comparison.ComparisonSessionManagerImpl
import com.koflox.session.domain.usecase.comparison.V1RawComparisonLocationProcessor
import com.koflox.session.domain.usecase.comparison.V2FilteredComparisonLocationProcessor
import com.koflox.session.domain.usecase.comparison.V3KalmanComparisonLocationProcessor
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.service.LocationCollectionManager
import com.koflox.session.service.LocationCollectionManagerImpl
import com.koflox.session.service.NutritionReminderManager
import com.koflox.session.service.NutritionReminderManagerImpl
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
import com.koflox.theme.domain.usecase.ObserveThemeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
internal object SessionServiceHiltModule {

    @Provides
    @Singleton
    fun providePendingSessionActionImpl(): PendingSessionActionImpl = PendingSessionActionImpl()

    @Provides
    @Singleton
    fun providePendingSessionAction(
        impl: PendingSessionActionImpl,
    ): PendingSessionAction = impl

    @Provides
    @Singleton
    fun providePendingSessionActionConsumer(
        impl: PendingSessionActionImpl,
    ): PendingSessionActionConsumer = impl

    @Provides
    @Singleton
    fun provideSessionNotificationManager(
        @ApplicationContext context: Context,
        localizedContextProvider: com.koflox.designsystem.context.LocalizedContextProvider,
        sessionUiMapper: SessionUiMapper,
        observeThemeUseCase: ObserveThemeUseCase,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): SessionNotificationManager = SessionNotificationManagerImpl(
        context = context,
        localizedContextProvider = localizedContextProvider,
        sessionUiMapper = sessionUiMapper,
        observeThemeUseCase = observeThemeUseCase,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideSessionServiceController(
        @ApplicationContext context: Context,
    ): SessionServiceController = SessionServiceControllerImpl(
        context = context,
    )

    @Provides
    @Singleton
    fun providePowerConnectionStateHolderImpl(): PowerConnectionStateHolderImpl = PowerConnectionStateHolderImpl()

    @Provides
    @Singleton
    fun providePowerConnectionStateHolder(
        impl: PowerConnectionStateHolderImpl,
    ): PowerConnectionStateHolder = impl

    @Provides
    @Singleton
    fun providePowerConnectionStatePublisher(
        impl: PowerConnectionStateHolderImpl,
    ): PowerConnectionStatePublisher = impl

    @Provides
    @Singleton
    fun provideComparisonSessionManager(
        distanceCalculator: DistanceCalculator,
        altitudeCalculator: AltitudeCalculator,
        locationValidator: LocationValidator,
        locationSmoother: LocationSmoother,
        localDataSource: SessionLocalDataSource,
        mapper: SessionMapper,
    ): ComparisonSessionManager {
        val processors = listOf(
            V1RawComparisonLocationProcessor(distanceCalculator),
            V2FilteredComparisonLocationProcessor(distanceCalculator, altitudeCalculator, locationValidator),
            V3KalmanComparisonLocationProcessor(distanceCalculator, altitudeCalculator, locationValidator, locationSmoother),
        )
        return ComparisonSessionManagerImpl(processors, localDataSource, mapper)
    }

    @Provides
    fun provideLocationCollectionManager(
        observeUserLocationUseCase: ObserveUserLocationUseCase,
        updateSessionLocationUseCase: UpdateSessionLocationUseCase,
        checkLocationEnabledUseCase: CheckLocationEnabledUseCase,
        updateSessionStatusUseCase: UpdateSessionStatusUseCase,
        currentTimeProvider: CurrentTimeProvider,
        comparisonSessionManager: ComparisonSessionManager,
        activeSessionUseCase: ActiveSessionUseCase,
    ): LocationCollectionManager = LocationCollectionManagerImpl(
        observeUserLocationUseCase = observeUserLocationUseCase,
        updateSessionLocationUseCase = updateSessionLocationUseCase,
        checkLocationEnabledUseCase = checkLocationEnabledUseCase,
        updateSessionStatusUseCase = updateSessionStatusUseCase,
        currentTimeProvider = currentTimeProvider,
        comparisonSessionManager = comparisonSessionManager,
        activeSessionUseCase = activeSessionUseCase,
    )

    @Provides
    fun provideNutritionReminderManager(
        nutritionReminderUseCase: NutritionReminderUseCase,
    ): NutritionReminderManager = NutritionReminderManagerImpl(
        nutritionReminderUseCase = nutritionReminderUseCase,
    )

    @Provides
    fun providePowerCollectionManager(
        sessionPowerMeterUseCase: SessionPowerMeterUseCase,
        updateSessionPowerUseCase: UpdateSessionPowerUseCase,
        powerConnectionStatePublisher: PowerConnectionStatePublisher,
    ): PowerCollectionManager = PowerCollectionManagerImpl(
        sessionPowerMeterUseCase = sessionPowerMeterUseCase,
        updateSessionPowerUseCase = updateSessionPowerUseCase,
        powerConnectionStatePublisher = powerConnectionStatePublisher,
    )

    @Provides
    fun provideSessionTracker(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        activeSessionUseCase: ActiveSessionUseCase,
        updateSessionStatusUseCase: UpdateSessionStatusUseCase,
        locationCollectionManager: LocationCollectionManager,
        powerCollectionManager: PowerCollectionManager,
        nutritionReminderManager: NutritionReminderManager,
        currentTimeProvider: CurrentTimeProvider,
        comparisonSessionManager: ComparisonSessionManager,
    ): SessionTracker = SessionTrackerImpl(
        dispatcherIo = dispatcherIo,
        activeSessionUseCase = activeSessionUseCase,
        updateSessionStatusUseCase = updateSessionStatusUseCase,
        locationCollectionManager = locationCollectionManager,
        powerCollectionManager = powerCollectionManager,
        nutritionReminderManager = nutritionReminderManager,
        currentTimeProvider = currentTimeProvider,
        comparisonSessionManager = comparisonSessionManager,
    )
}
