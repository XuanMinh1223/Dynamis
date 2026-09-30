package org.xuan.dynamis.data.source.api

import org.xuan.dynamis.data.source.api.dto.ForecastResponse

interface WeatherApi {
    suspend fun getWeather(latitude: Double, longitude: Double): ForecastResponse
}
