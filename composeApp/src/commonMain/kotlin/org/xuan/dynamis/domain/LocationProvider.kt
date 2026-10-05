package org.xuan.dynamis.domain

import org.xuan.dynamis.domain.model.GeoCoordinates

interface LocationProvider {
    suspend fun currentLocation(): GeoCoordinates
    suspend fun locality(coordinates: GeoCoordinates): String?
}

enum class LocationFailure {
    PermissionDenied,
    PermissionDeniedForever,
    Timeout,
    Unavailable,
}

class LocationException(val reason: LocationFailure, detail: String? = null, cause: Throwable? = null) :
    Exception(listOfNotNull("Location request failed: $reason", detail).joinToString(" - "), cause)
