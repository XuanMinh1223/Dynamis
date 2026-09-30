package ui.screen.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.currentLocationOrNull
import domain.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel(
    private val repository: WeatherRepository,
    private val geoLocator: Geolocator,
    private val geocoder: Geocoder,
) : ViewModel() {
    private val _isShowing = MutableStateFlow(false)
    val isShowing: StateFlow<Boolean> = _isShowing.asStateFlow()
    private val _weatherUiState = mutableStateOf(WeatherUiState())
    val weatherUIState: State<WeatherUiState> = _weatherUiState

    init {
        viewModelScope.launch {
            geoLocator.currentLocationOrNull()?.let { location ->
                getWeather(location.coordinates.latitude, location.coordinates.longitude)
                geocoder.placeOrNull(location.coordinates)?.let { place ->
                    _weatherUiState.value = _weatherUiState.value.copy(
                        locality = listOfNotNull(place.locality, place.administrativeArea)
                            .filter { it.isNotBlank() }.distinct().joinToString(", "),
                    )
                }
            }
        }
    }

    private fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                    val forecast = repository.getWeather(latitude, longitude)
                    _weatherUiState.value = _weatherUiState.value.copy(
                        time = forecast.observedAt.time.toString(),
                        currentTemperature = forecast.temperature.format(forecast.temperatureUnit),
                        currentWeatherCode = forecast.weatherCode ?: 0,
                        todayHigh = forecast.todayHigh.format(forecast.temperatureUnit),
                        todayLow = forecast.todayLow.format(forecast.temperatureUnit),
                    )
                    _isShowing.value = true
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Exception) {
                // Screen-level recovery is introduced with the unified UI state.
            }
        }
    }
}

private fun Double?.format(unit: String): String = this?.let { "${it.roundToInt()}$unit" } ?: "—"
