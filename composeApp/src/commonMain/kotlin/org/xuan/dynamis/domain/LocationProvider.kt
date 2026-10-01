package org.xuan.dynamis.domain

import org.xuan.dynamis.domain.model.GeoCoordinates

interface LocationProvider {
    suspend fun currentLocation(): GeoCoordinates
    suspend fun locality(coordinates: GeoCoordinates): String?
}

enum class LocationFailure {
    PermissionDenied,
    PermissionDeniedForever,
    Unavailable,
}

class LocationException(val reason: LocationFailure, cause: Throwable? = null) :
    Exception("Location request failed: $reason", cause)
