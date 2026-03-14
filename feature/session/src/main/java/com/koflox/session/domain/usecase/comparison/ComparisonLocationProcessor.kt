package com.koflox.session.domain.usecase.comparison

import com.koflox.location.model.Location
import com.koflox.session.domain.model.Session

internal interface ComparisonLocationProcessor {
    val versionTag: String
    val versionLabel: String
    fun initialize(baseSession: Session)
    fun update(location: Location, timestampMs: Long, lastResumedTimeMs: Long)
    fun getSession(): Session?
    fun reset()
}
