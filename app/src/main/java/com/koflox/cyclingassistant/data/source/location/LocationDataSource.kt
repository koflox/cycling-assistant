package com.koflox.cyclingassistant.data.source.location

import com.koflox.cyclingassistant.domain.model.Location

// TODO: Should be move to a separate module
interface LocationDataSource {
    suspend fun getCurrentLocation(): Result<Location>
}
