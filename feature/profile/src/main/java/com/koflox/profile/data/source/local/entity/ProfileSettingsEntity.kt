package com.koflox.profile.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_settings")
data class ProfileSettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val riderWeightKg: Double? = null,
)
