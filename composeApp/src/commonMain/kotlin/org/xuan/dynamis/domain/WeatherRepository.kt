package org.xuan.dynamis.domain

import org.xuan.dynamis.domain.model.WeatherForecast

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherForecast
}
