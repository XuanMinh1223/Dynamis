package org.xuan.dynamis.data.repo

import org.xuan.dynamis.data.source.api.WeatherApi
import org.xuan.dynamis.domain.WeatherRepository
import org.xuan.dynamis.domain.model.WeatherForecast

class DefaultWeatherRepository(
    private val api: WeatherApi,
) : WeatherRepository {
    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
    ): WeatherForecast = api.getWeather(latitude, longitude).toWeatherForecast()
}
