package org.xuan.dynamis.data.location

import dev.jordond.compass.Priority
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.NotSupportedPlatformGeocoder
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.LocationRequest
import dev.jordond.compass.geolocation.TrackingStatus
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.xuan.dynamis.domain.LocationException
import org.xuan.dynamis.domain.LocationFailure
import org.xuan.dynamis.domain.model.GeoCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CompassLocationProviderTest {
    @Test
    fun preservesTemporaryAndPermanentPermissionDenials() =
        runTest {
            listOf(false, true).forEach { forever ->
                val exception =
                    assertFailsWith<LocationException> {
                        provider { GeolocatorResult.PermissionDenied(forever) }.currentLocation()
                    }
                assertEquals(
                    if (forever) LocationFailure.PermissionDeniedForever else LocationFailure.PermissionDenied,
                    exception.reason,
                )
            }
        }

    @Test
    fun mapsMissingLocationToRecoverableFailure() =
        runTest {
            val exception =
                assertFailsWith<LocationException> {
                    provider { GeolocatorResult.NotFound }.currentLocation()
                }
            assertEquals(LocationFailure.Unavailable, exception.reason)
        }

    @Test
    fun locationTimeoutDoesNotLeaveScreenLoadingForever() =
        runTest {
            val exception =
                assertFailsWith<LocationException> {
                    provider { awaitCancellation() }.currentLocation()
                }
            assertEquals(LocationFailure.Timeout, exception.reason)
        }

    @Test
    fun unsupportedGeocodingReturnsNoPlaceName() =
        runTest {
            assertNull(provider { GeolocatorResult.NotFound }.locality(GeoCoordinates(37.7, -122.4)))
        }

    private fun provider(current: suspend () -> GeolocatorResult) =
        CompassLocationProvider(
            geolocator =
                object : Geolocator {
                    override val trackingStatus = emptyFlow<TrackingStatus>()

                    override suspend fun isAvailable() = true

                    override suspend fun current(priority: Priority) = current()

                    override suspend fun current(request: LocationRequest) = current()

                    override suspend fun lastLocation(priority: Priority) = current()

                    override suspend fun lastLocation(request: LocationRequest) = current()

                    override fun track(request: LocationRequest) = emptyFlow<TrackingStatus>()

                    override fun stopTracking() = Unit
                },
            geocoder = Geocoder(NotSupportedPlatformGeocoder),
        )
}
