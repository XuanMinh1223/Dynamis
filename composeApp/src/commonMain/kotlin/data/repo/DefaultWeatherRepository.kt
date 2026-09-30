package data.repo

import data.source.api.WeatherApi
import domain.WeatherRepository
import domain.model.WeatherForecast

class DefaultWeatherRepository(
    private val api: WeatherApi,
) : WeatherRepository {
    override suspend fun getWeather(latitude: Double, longitude: Double): WeatherForecast =
        api.getWeather(latitude, longitude).toWeatherForecast()
}
