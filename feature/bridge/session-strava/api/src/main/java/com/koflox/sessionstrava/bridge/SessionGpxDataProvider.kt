package com.koflox.sessionstrava.bridge

import com.koflox.gpx.GpxInput

interface SessionGpxDataProvider {
    suspend fun getGpxInput(sessionId: String): Result<GpxInput>
}
