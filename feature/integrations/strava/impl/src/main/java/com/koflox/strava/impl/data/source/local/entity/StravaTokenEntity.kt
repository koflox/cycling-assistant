package com.koflox.strava.impl.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strava_tokens")
data class StravaTokenEntity(
    @PrimaryKey
    val id: Int,
    val accessToken: String,
    val refreshToken: String,
    val expiresAtSeconds: Long,
    val athleteId: Long,
    val athleteName: String,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
