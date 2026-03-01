package com.koflox.ble.model

import java.util.UUID

/**
 * Events emitted by a BLE GATT connection.
 *
 * Typical event sequence:
 * ```
 * ConnectionStateChanged(CONNECTED)
 *   → ServicesDiscovered
 *     → CharacteristicChanged(...)   // repeated per notification
 *       → ...
 * ConnectionStateChanged(DISCONNECTED)
 * ```
 */
sealed interface BleGattEvent {

    /**
     * A subscribed characteristic sent a new value via BLE notification.
     *
     * Emitted after [BleGattManager.enableNotifications] is called for the target characteristic.
     * The [data] byte array is protocol-specific and must be parsed by the consumer.
     *
     * Example — Cycling Power Measurement (0x2A63):
     * ```
     * data = [0x20, 0x00,   // flags: crank revolution data present
     *         0xC8, 0x00,   // instantaneous power: 200 W (little-endian)
     *         0x08, 0x00,   // cumulative crank revolutions: 8
     *         0x00, 0x04]   // last crank event time: 1024 (1/1024s units)
     * ```
     *
     * @property serviceUuid UUID of the GATT service (e.g., `0x1818` for Cycling Power).
     * @property characteristicUuid UUID of the characteristic (e.g., `0x2A63` for Power Measurement).
     * @property data raw bytes received from the device.
     */
    data class CharacteristicChanged(
        val serviceUuid: UUID,
        val characteristicUuid: UUID,
        val data: ByteArray,
    ) : BleGattEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CharacteristicChanged) return false
            return serviceUuid == other.serviceUuid &&
                characteristicUuid == other.characteristicUuid &&
                data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            var result = serviceUuid.hashCode()
            result = 31 * result + characteristicUuid.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    /**
     * GATT service discovery completed successfully.
     *
     * Emitted once after connection. After this event it is safe
     * to call [BleGattManager.enableNotifications].
     */
    data object ServicesDiscovered : BleGattEvent

    /**
     * The connection state of the peripheral changed.
     *
     * @property state the new [BleConnectionState].
     */
    data class ConnectionStateChanged(
        val state: BleConnectionState,
    ) : BleGattEvent
}
