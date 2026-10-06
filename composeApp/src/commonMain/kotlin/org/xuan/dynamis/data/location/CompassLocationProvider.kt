package org.xuan.dynamis.data.location

import co.touchlab.kermit.Logger
import dev.jordond.compass.Coordinates
import dev.jordond.compass.Location
import dev.jordond.compass.Priority
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import org.xuan.dynamis.domain.LocationException
import org.xuan.dynamis.domain.LocationFailure
import org.xuan.dynamis.domain.LocationProvider
import org.xuan.dynamis.domain.model.GeoCoordinates
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

class CompassLocationProvider(
    private val geolocator: Geolocator,
    private val geocoder: Geocoder,
) : LocationProvider {
    private val log = Logger.withTag("Location")

    private var cachedFix: CachedFix? = null

    override suspend fun currentLocation(): GeoCoordinates {
        try {
            // The home and radar screens both ask; share one fix rather than waking the GPS twice.
            cachedFix?.takeIf { it.isFresh(MEMORY_CACHE_MILLIS) }?.let {
                log.d { "Using in-memory location fix" }
                return it.coordinates
            }

            // A forecast only needs city-level accuracy, so the OS's last known fix is plenty and
            // arrives instantly. Only fall back to a fresh fix (slow indoors) when there isn't one.
            val result = lastKnownFix() ?: freshFix()
            return when (result) {
                is GeolocatorResult.Success -> remember(result)

                is GeolocatorResult.PermissionDenied -> throw LocationException(
                    if (result.forever) {
                        LocationFailure.PermissionDeniedForever
                    } else {
                        LocationFailure.PermissionDenied
                    },
                )

                null -> throw LocationException(LocationFailure.Timeout, "no fix within ${FRESH_FIX_TIMEOUT_MILLIS}ms")

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

    /** The OS's cached fix if it is recent enough, or a permission denial; otherwise null. */
    private suspend fun lastKnownFix(): GeolocatorResult? {
        val result = withTimeoutOrNull(LAST_KNOWN_TIMEOUT_MILLIS.milliseconds) { geolocator.lastLocation(Priority.LowPower) }
        return when (result) {
            is GeolocatorResult.PermissionDenied -> {
                result
            }

            is GeolocatorResult.Success -> {
                if (result.data.isRecent()) {
                    log.d { "Using the OS's last known location" }
                    result
                } else {
                    log.d { "No usable last known location (too old); requesting a fresh fix" }
                    null
                }
            }

            else -> {
                log.d { "No usable last known location (${result?.let { it::class.simpleName } ?: "timed out"}); requesting a fresh fix" }
                null
            }
        }
    }

    private suspend fun freshFix(): GeolocatorResult? =
        withTimeoutOrNull(FRESH_FIX_TIMEOUT_MILLIS.milliseconds) { geolocator.current(Priority.Balanced) }
            .also { log.d { "Fresh location fix: ${it?.let { r -> r::class.simpleName } ?: "timed out"}" } }

    private fun remember(result: GeolocatorResult.Success): GeoCoordinates {
        val coordinates = GeoCoordinates(result.data.coordinates.latitude, result.data.coordinates.longitude)
        cachedFix = CachedFix(coordinates, Clock.System.now().toEpochMilliseconds())
        return coordinates
    }

    private fun Location.isRecent() = Clock.System.now().toEpochMilliseconds() - timestampMillis <= LAST_KNOWN_MAX_AGE_MILLIS

    private class CachedFix(
        val coordinates: GeoCoordinates,
        val savedAtMillis: Long,
    ) {
        fun isFresh(maxAgeMillis: Long) = Clock.System.now().toEpochMilliseconds() - savedAtMillis <= maxAgeMillis
    }

    private companion object {
        const val MEMORY_CACHE_MILLIS = 5 * 60_000L
        const val LAST_KNOWN_MAX_AGE_MILLIS = 30 * 60_000L
        const val LAST_KNOWN_TIMEOUT_MILLIS = 3_000L
        const val FRESH_FIX_TIMEOUT_MILLIS = 20_000L
    }

    override suspend fun locality(coordinates: GeoCoordinates): String? {
        return try {
            val place =
                withTimeoutOrNull(5_000.milliseconds) {
                    geocoder.placeOrNull(Coordinates(coordinates.latitude, coordinates.longitude))
                }
            if (place == null) {
                log.w { "Reverse geocoding found no place for $coordinates (timeout or no result)" }
                return null
            }
            listOfNotNull(place.locality, place.administrativeArea)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(", ")
                .ifBlank { null }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Exception) {
            // Reverse geocoding is optional; weather remains usable without a place name.
            log.w(cause) { "Reverse geocoding failed for $coordinates" }
            null
        }
    }
}
