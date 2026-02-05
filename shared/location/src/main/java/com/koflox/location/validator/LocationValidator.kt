package com.koflox.location.validator

import com.koflox.location.model.Location

interface LocationValidator {
    fun isAccuracyValid(location: Location): Boolean
}

internal class LocationValidatorImpl : LocationValidator {

    companion object {
        private const val MAX_ACCURACY_METERS = 20f
    }

    override fun isAccuracyValid(location: Location): Boolean {
        val accuracy = location.accuracyMeters ?: return true
        return accuracy <= MAX_ACCURACY_METERS
    }
}
