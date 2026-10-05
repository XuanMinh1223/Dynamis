package org.xuan.dynamis.ui.screen.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val weather: WeatherUiState) : HomeUiState
    data class Error(val reason: HomeError) : HomeUiState
}

enum class HomeError {
    PermissionDenied,
    PermissionDeniedForever,
    LocationUnavailable,
    LocationTimeout,
    WeatherUnavailable,
    WeatherServerError,
    WeatherDataInvalid,
}
