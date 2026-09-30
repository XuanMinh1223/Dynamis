package org.xuan.dynamis.ui.screen.home

import androidx.lifecycle.ViewModel
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

class HomeViewModel(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val coordinates = locationProvider.currentLocation()
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
                _uiState.value = HomeUiState.Error(
                    when (cause.reason) {
                        LocationFailure.PermissionDenied -> HomeError.PermissionDenied
                        LocationFailure.PermissionDeniedForever -> HomeError.PermissionDeniedForever
                        LocationFailure.Unavailable -> HomeError.LocationUnavailable
                    },
                )
            } catch (cause: Exception) {
                ensureActive()
                _uiState.value = HomeUiState.Error(HomeError.WeatherUnavailable)
            }
        }
    }
}
