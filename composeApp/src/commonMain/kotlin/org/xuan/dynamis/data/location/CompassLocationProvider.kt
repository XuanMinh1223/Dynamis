package org.xuan.dynamis.data.location

import co.touchlab.kermit.Logger
import dev.jordond.compass.Coordinates
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import org.xuan.dynamis.domain.LocationException
import org.xuan.dynamis.domain.LocationFailure
import org.xuan.dynamis.domain.LocationProvider
import org.xuan.dynamis.domain.model.GeoCoordinates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

class CompassLocationProvider(
    private val geolocator: Geolocator,
    private val geocoder: Geocoder,
) : LocationProvider {
    private val log = Logger.withTag("Location")

    override suspend fun currentLocation(): GeoCoordinates {
        try {
            return when (val result = withTimeoutOrNull(20_000) { geolocator.current() }) {
                null -> throw LocationException(LocationFailure.Timeout, "no fix within 20s")
                is GeolocatorResult.Success -> GeoCoordinates(
                    result.data.coordinates.latitude,
                    result.data.coordinates.longitude,
                )
                is GeolocatorResult.PermissionDenied -> throw LocationException(
                    if (result.forever) LocationFailure.PermissionDeniedForever
                    else LocationFailure.PermissionDenied,
                )
                else -> throw LocationException(LocationFailure.Unavailable, "geolocator returned $result")
            }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: LocationException) {
            throw cause
        } catch (cause: Exception) {
            throw LocationException(LocationFailure.Unavailable, cause = cause)
        }
    }

    override suspend fun locality(coordinates: GeoCoordinates): String? {
        return try {
            val place = withTimeoutOrNull(5_000) {
                geocoder.placeOrNull(Coordinates(coordinates.latitude, coordinates.longitude))
            }
            if (place == null) {
                log.w { "Reverse geocoding found no place for $coordinates (timeout or no result)" }
                return null
            }
            listOfNotNull(place.locality, place.administrativeArea)
                .filter { it.isNotBlank() }.distinct().joinToString(", ").ifBlank { null }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Exception) {
            // Reverse geocoding is optional; weather remains usable without a place name.
            log.w(cause) { "Reverse geocoding failed for $coordinates" }
            null
        }
    }
}
