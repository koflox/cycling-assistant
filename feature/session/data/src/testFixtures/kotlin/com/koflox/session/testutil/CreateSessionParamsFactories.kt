package com.koflox.session.testutil

import com.koflox.session.domain.usecase.CreateSessionParams

fun createFreeRoamSessionParams(): CreateSessionParams = CreateSessionParams.FreeRoam

fun createDestinationSessionParams(
    destinationId: String = "",
    destinationName: String = "",
    destinationLatitude: Double = 0.0,
    destinationLongitude: Double = 0.0,
) = CreateSessionParams.Destination(
    destinationId = destinationId,
    destinationName = destinationName,
    destinationLatitude = destinationLatitude,
    destinationLongitude = destinationLongitude,
)
