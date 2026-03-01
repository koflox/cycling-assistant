package com.koflox.connections.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_devices")
data class PairedDeviceEntity(
    @PrimaryKey
    val id: String,
    val macAddress: String,
    val name: String,
    val deviceType: String,
    val isSessionUsageEnabled: Boolean,
)
