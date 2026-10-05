package org.xuan.dynamis.ui.screen.home

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import io.ktor.client.plugins.ResponseException
import org.xuan.dynamis.data.repo.InvalidForecastException
import org.xuan.dynamis.domain.model.GeoCoordinates
import androidx.lifecycle.viewModelScope
import org.xuan.dynamis.domain.LocationException
import org.xuan.dynamis.domain.LocationFailure
import org.xuan.dynamis.domain.LocationProvider
import org.xuan.dynamis.domain.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

class HomeViewModel(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private val log = Logger.withTag("Home")

    init {
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            var coordinates: GeoCoordinates? = null
            try {
                coordinates = locationProvider.currentLocation()
                val weather = repository.getWeather(coordinates.latitude, coordinates.longitude).toUiState()
                ensureActive()
                _uiState.value = HomeUiState.Success(weather)
                // Show the forecast immediately; the optional place name can arrive later.
                val locality = locationProvider.locality(coordinates)
                ensureActive()
                _uiState.value = HomeUiState.Success(weather.copy(locality = locality.orEmpty()))
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: LocationException) {
                ensureActive()
                // A denied permission is a normal user choice, not a fault.
                when (cause.reason) {
                    LocationFailure.PermissionDenied, LocationFailure.PermissionDeniedForever ->
                        log.w { "Location unavailable (${cause.reason})" }
                    else -> log.e(cause) { "Could not determine location (${cause.reason})" }
                }
                _uiState.value = HomeUiState.Error(
                    when (cause.reason) {
                        LocationFailure.PermissionDenied -> HomeError.PermissionDenied
                        LocationFailure.PermissionDeniedForever -> HomeError.PermissionDeniedForever
                        LocationFailure.Timeout -> HomeError.LocationTimeout
                        LocationFailure.Unavailable -> HomeError.LocationUnavailable
                    },
                )
            } catch (cause: Exception) {
                ensureActive()
                log.e(cause) { "Weather load failed for ${coordinates ?: "unknown coordinates"}" }
                _uiState.value = HomeUiState.Error(
                    when (cause) {
                        is InvalidForecastException -> HomeError.WeatherDataInvalid
                        is ResponseException -> HomeError.WeatherServerError
                        is SerializationException -> HomeError.WeatherDataInvalid
                        else -> HomeError.WeatherUnavailable
                    },
                )
            }
        }
    }
}
