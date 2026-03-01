package com.koflox.ble.connection

import com.koflox.ble.model.BleConnectionState
import com.koflox.ble.model.BleGattEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Manages a BLE GATT (Generic Attribute Profile) connection to a peripheral device.
 *
 * GATT is the BLE protocol for exchanging data via Services and Characteristics.
 * A Service groups related Characteristics (e.g., Cycling Power Service 0x1818).
 * A Characteristic holds a value that can be read, written, or subscribed to
 * for notifications (e.g., Cycling Power Measurement 0x2A63).
 *
 * Each instance manages a single device connection. Create a new instance per device.
 */
interface BleGattManager {

    /**
     * Connects to a BLE peripheral and emits GATT events.
     *
     * Automatically discovers services after connection is established.
     * The flow completes when the device disconnects.
     *
     * @param address MAC address of the target device (e.g., "AA:BB:CC:DD:EE:FF").
     * @return a cold [Flow] of [BleGattEvent]s — connection state changes,
     *   service discovery, and characteristic notifications.
     */
    fun connect(address: String): Flow<BleGattEvent>

    /**
     * Initiates a graceful disconnection from the connected device.
     *
     * Triggers [BleGattEvent.ConnectionStateChanged] with [BleConnectionState.DISCONNECTED]
     * and completes the [connect] flow.
     */
    fun disconnect()

    /**
     * Observes the current connection state of this manager.
     *
     * @return a [StateFlow] that emits [BleConnectionState] updates.
     */
    fun observeConnectionState(): StateFlow<BleConnectionState>

    /**
     * Enables characteristic notifications via the Client Characteristic Configuration Descriptor.
     *
     * After calling this, value changes will be delivered as
     * [BleGattEvent.CharacteristicChanged] events in the [connect] flow.
     *
     * @param serviceUuid UUID of the GATT service containing the characteristic.
     * @param characteristicUuid UUID of the characteristic to subscribe to.
     */
    fun enableNotifications(serviceUuid: UUID, characteristicUuid: UUID)
}
