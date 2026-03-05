package com.koflox.sensorprotocol.power

import java.util.UUID

/**
 * Constants for the Bluetooth Cycling Power Service and its characteristics.
 *
 * UUIDs are derived from 16-bit Bluetooth SIG assigned numbers using the Bluetooth Base UUID:
 * `0000XXXX-0000-1000-8000-00805f9b34fb`.
 *
 * @see [Bluetooth Assigned Numbers](https://www.bluetooth.com/specifications/assigned-numbers/)
 * @see [GATT Specification Supplement – Cycling Power Measurement](https://www.bluetooth.com/specifications/specs/gatt-specification-supplement/)
 */
object CyclingPowerConstants {

    /** Cycling Power Service (assigned number `0x1818`). */
    val SERVICE_UUID: UUID = UUID.fromString("00001818-0000-1000-8000-00805f9b34fb")

    /** Cycling Power Measurement characteristic (assigned number `0x2A63`). */
    val MEASUREMENT_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a63-0000-1000-8000-00805f9b34fb")

    /** Bit 0 — Pedal Power Balance field is present (1 byte: `uint8`). */
    const val FLAG_PEDAL_POWER_BALANCE_PRESENT = 0x0001

    /** Bit 2 — Accumulated Torque field is present (2 bytes: `uint16`). */
    const val FLAG_ACCUMULATED_TORQUE_PRESENT = 0x0004

    /** Bit 4 — Wheel Revolution Data is present (6 bytes: `uint32` + `uint16`). */
    const val FLAG_WHEEL_REVOLUTION_DATA_PRESENT = 0x0010

    /** Bit 5 — Crank Revolution Data is present (4 bytes: `uint16` + `uint16`). */
    const val FLAG_CRANK_REVOLUTION_DATA_PRESENT = 0x0020

    /** Size of Pedal Power Balance field in bytes. */
    const val PEDAL_POWER_BALANCE_SIZE = 1

    /** Size of Accumulated Torque field in bytes. */
    const val ACCUMULATED_TORQUE_SIZE = 2

    /** Size of Wheel Revolution Data fields in bytes (cumulative revolutions `uint32` + last event time `uint16`). */
    const val WHEEL_REVOLUTION_DATA_SIZE = 6
}
