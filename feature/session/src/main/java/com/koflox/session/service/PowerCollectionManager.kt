package com.koflox.session.service

import com.koflox.connectionsession.bridge.usecase.PowerConnectionException
import com.koflox.connectionsession.bridge.usecase.SessionPowerMeterUseCase
import com.koflox.session.domain.usecase.UpdateSessionPowerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal interface PowerCollectionManager {
    fun start(scope: CoroutineScope)
    fun stop()
}

internal class PowerCollectionManagerImpl(
    private val sessionPowerMeterUseCase: SessionPowerMeterUseCase,
    private val updateSessionPowerUseCase: UpdateSessionPowerUseCase,
    private val powerConnectionStatePublisher: PowerConnectionStatePublisher,
) : PowerCollectionManager {

    companion object {
        internal val RETRY_INITIAL_DELAY = 2.seconds
        internal val RETRY_MAX_DELAY = 5.minutes
        internal const val RETRY_FACTOR = 2
        private val COUNTDOWN_TICK = 1.seconds
    }

    private var job: Job? = null

    override fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return
        job = scope.launch {
            val device = sessionPowerMeterUseCase.getSessionPowerDevice() ?: return@launch
            publishState(device.name, PowerConnectionState.Connecting)
            collectWithRetry(device.macAddress, device.name)
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
        sessionPowerMeterUseCase.disconnect()
        powerConnectionStatePublisher.updateState(null)
    }

    private suspend fun collectWithRetry(macAddress: String, deviceName: String) {
        var retryDelay = RETRY_INITIAL_DELAY
        while (true) {
            publishState(deviceName, PowerConnectionState.Connecting)
            try {
                sessionPowerMeterUseCase.observePowerReadings(macAddress).collect { reading ->
                    retryDelay = RETRY_INITIAL_DELAY
                    publishState(deviceName, PowerConnectionState.Connected(reading.powerWatts))
                    updateSessionPowerUseCase.update(
                        powerWatts = reading.powerWatts,
                        timestampMs = reading.timestampMs,
                    )
                }
            } catch (_: PowerConnectionException) {
                // Connection error — retry with backoff
            }
            awaitRetryWithCountdown(deviceName, retryDelay)
            retryDelay = (retryDelay.times(RETRY_FACTOR)).coerceAtMost(RETRY_MAX_DELAY)
        }
    }

    private suspend fun awaitRetryWithCountdown(deviceName: String, retryDelay: Duration) {
        var remaining = retryDelay
        while (remaining > Duration.ZERO) {
            publishState(deviceName, PowerConnectionState.Reconnecting(remaining))
            val tick = remaining.coerceAtMost(COUNTDOWN_TICK)
            delay(tick)
            remaining -= tick
        }
    }

    private fun publishState(deviceName: String, state: PowerConnectionState) {
        powerConnectionStatePublisher.updateState(DeviceConnectionInfo(deviceName, state))
    }
}
