package com.koflox.locale.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locale_settings")
data class LocaleSettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val languageCode: String? = null,
)
