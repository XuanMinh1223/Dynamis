package domain

import domain.model.WeatherForecast

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherForecast
}
