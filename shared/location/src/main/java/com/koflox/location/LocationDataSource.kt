package com.koflox.location

import com.koflox.location.model.Location

interface LocationDataSource {
    suspend fun getCurrentLocation(): Result<Location>
}
