package org.xuan.dynamis.ui.screen.home

import org.xuan.dynamis.domain.LocationException
import org.xuan.dynamis.domain.LocationFailure
import org.xuan.dynamis.domain.LocationProvider
import org.xuan.dynamis.domain.WeatherRepository
import org.xuan.dynamis.domain.model.GeoCoordinates
import org.xuan.dynamis.domain.model.WeatherForecast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setMainDispatcher() = Dispatchers.setMain(dispatcher)

    @After
    fun resetMainDispatcher() = Dispatchers.resetMain()

    @Test
    fun loadsForecastAndPlaceName() = runTest(dispatcher) {
        val viewModel = HomeViewModel(repository(), locationProvider())
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        val weather = assertIs<HomeUiState.Success>(viewModel.uiState.value).weather
        assertEquals("18°C", weather.currentTemperature)
        assertEquals("San Francisco, California", weather.locality)
    }

    @Test
    fun locationFailuresHaveDistinctRecoveryStates() = runTest(dispatcher) {
        val failures = mapOf(
            LocationFailure.PermissionDenied to HomeError.PermissionDenied,
            LocationFailure.PermissionDeniedForever to HomeError.PermissionDeniedForever,
            LocationFailure.Unavailable to HomeError.LocationUnavailable,
        )
        failures.forEach { (failure, expected) ->
            var weatherRequested = false
            val viewModel = HomeViewModel(
                repository { _, _ -> weatherRequested = true; forecast() },
                locationProvider(current = { throw LocationException(failure) }),
            )
            advanceUntilIdle()
            assertEquals(HomeUiState.Error(expected), viewModel.uiState.value)
            assertEquals(false, weatherRequested)
        }
    }

    @Test
    fun requestFailureCanBeRetriedSuccessfully() = runTest(dispatcher) {
        var attempt = 0
        val viewModel = HomeViewModel(repository { _, _ ->
            if (attempt++ == 0) throw IllegalStateException("Offline")
            forecast()
        }, locationProvider())
        advanceUntilIdle()
        assertEquals(HomeUiState.Error(HomeError.WeatherUnavailable), viewModel.uiState.value)
        viewModel.refresh()
        advanceUntilIdle()
        assertIs<HomeUiState.Success>(viewModel.uiState.value)
        assertEquals(2, attempt)
    }

    @Test
    fun displaysWeatherWhileReverseGeocodingIsPending() = runTest(dispatcher) {
        val place = CompletableDeferred<String?>()
        val viewModel = HomeViewModel(repository(), locationProvider(locality = { place.await() }))
        runCurrent()
        assertEquals("", assertIs<HomeUiState.Success>(viewModel.uiState.value).weather.locality)
        place.complete("San Francisco")
        advanceUntilIdle()
        assertEquals("San Francisco", assertIs<HomeUiState.Success>(viewModel.uiState.value).weather.locality)
    }

    @Test
    fun failedReverseGeocodingKeepsTheForecastUsable() = runTest(dispatcher) {
        val viewModel = HomeViewModel(repository(), locationProvider(locality = { null }))
        advanceUntilIdle()
        assertEquals("18°C", assertIs<HomeUiState.Success>(viewModel.uiState.value).weather.currentTemperature)
    }

    @Test
    fun refreshCancelsPreviousRequestAndPublishesLatestResult() = runTest(dispatcher) {
        var attempt = 0
        var cancelled = false
        val viewModel = HomeViewModel(repository { _, _ ->
            if (attempt++ == 0) {
                try { awaitCancellation() } finally { cancelled = true }
            }
            forecast().copy(temperature = 25.0)
        }, locationProvider())
        runCurrent()
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(cancelled)
        assertEquals("25°C", assertIs<HomeUiState.Success>(viewModel.uiState.value).weather.currentTemperature)
    }

    @Test
    fun cancellationDoesNotBecomeAWeatherError() = runTest(dispatcher) {
        val viewModel = HomeViewModel(repository { _, _ -> throw CancellationException("Leaving") }, locationProvider())
        advanceUntilIdle()
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun forwardsActualLocationCoordinatesToRepository() = runTest(dispatcher) {
        var requested: GeoCoordinates? = null
        HomeViewModel(repository { latitude, longitude ->
            requested = GeoCoordinates(latitude, longitude)
            forecast()
        }, locationProvider())
        advanceUntilIdle()
        assertEquals(GeoCoordinates(37.7, -122.4), requested)
    }

    @Test
    fun viewModelStoreRetainsInstanceAndCancelsWorkWhenCleared() = runTest(dispatcher) {
        var cancelled = false
        val store = ViewModelStore()
        val provider = ViewModelProvider(store, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
                repository { _, _ ->
                    try { awaitCancellation() } finally { cancelled = true }
                },
                locationProvider(),
            ) as T
        })
        val first = provider[HomeViewModel::class.java]
        assertTrue(first === provider[HomeViewModel::class.java])
        runCurrent()
        store.clear()
        advanceUntilIdle()
        assertTrue(cancelled)
        assertEquals(HomeUiState.Loading, first.uiState.value)
    }

    private fun repository(
        load: suspend (Double, Double) -> WeatherForecast = { _, _ -> forecast() },
    ) = object : WeatherRepository {
        override suspend fun getWeather(latitude: Double, longitude: Double) = load(latitude, longitude)
    }

    private fun locationProvider(
        current: suspend () -> GeoCoordinates = { GeoCoordinates(37.7, -122.4) },
        locality: suspend () -> String? = { "San Francisco, California" },
    ) = object : LocationProvider {
        override suspend fun currentLocation() = current()
        override suspend fun locality(coordinates: GeoCoordinates) = locality()
    }

    private fun forecast() = WeatherForecast(
        LocalDateTime(2026, 9, 30, 23, 45), "America/Los_Angeles", 18.4, "°C", 3, 22.0, 12.0,
    )
}
