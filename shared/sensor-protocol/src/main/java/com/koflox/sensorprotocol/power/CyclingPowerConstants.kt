package com.koflox.sensorprotocol.power

import java.util.UUID

object CyclingPowerConstants {

    val SERVICE_UUID: UUID = UUID.fromString("00001818-0000-1000-8000-00805f9b34fb")
    val MEASUREMENT_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a63-0000-1000-8000-00805f9b34fb")

    const val FLAG_CRANK_REVOLUTION_DATA_PRESENT = 0x0020
}
