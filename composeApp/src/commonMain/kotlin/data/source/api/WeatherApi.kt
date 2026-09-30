package data.source.api

import data.source.api.dto.ForecastResponse

interface WeatherApi {
    suspend fun getWeather(latitude: Double, longitude: Double): ForecastResponse
}
