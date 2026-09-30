package data.location

import dev.jordond.compass.Coordinates
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import domain.LocationException
import domain.LocationFailure
import domain.LocationProvider
import domain.model.GeoCoordinates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

class CompassLocationProvider(
    private val geolocator: Geolocator,
    private val geocoder: Geocoder,
) : LocationProvider {
    override suspend fun currentLocation(): GeoCoordinates {
        try {
            return when (val result = withTimeoutOrNull(20_000) { geolocator.current() }) {
                is GeolocatorResult.Success -> GeoCoordinates(
                    result.data.coordinates.latitude,
                    result.data.coordinates.longitude,
                )
                is GeolocatorResult.PermissionDenied -> throw LocationException(
                    if (result.forever) LocationFailure.PermissionDeniedForever
                    else LocationFailure.PermissionDenied,
                )
                is GeolocatorResult.PermissionError -> throw LocationException(
                    LocationFailure.PermissionDenied, result.cause,
                )
                else -> throw LocationException(LocationFailure.Unavailable)
            }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: LocationException) {
            throw cause
        } catch (cause: Exception) {
            throw LocationException(LocationFailure.Unavailable, cause)
        }
    }

    override suspend fun locality(coordinates: GeoCoordinates): String? {
        return try {
            val place = withTimeoutOrNull(5_000) {
                geocoder.placeOrNull(Coordinates(coordinates.latitude, coordinates.longitude))
            } ?: return null
            listOfNotNull(place.locality, place.administrativeArea)
                .filter { it.isNotBlank() }.distinct().joinToString(", ").ifBlank { null }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Exception) {
            // Reverse geocoding is optional; weather remains usable without a place name.
            null
        }
    }
}
