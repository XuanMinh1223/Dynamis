package org.xuan.dynamis.data.source.api

object ApiConstants {
    const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
    object ParameterNames {
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CURRENT = "current"
        const val DAILY = "daily"
        const val TEMPERATURE_UNIT = "temperature_unit"
        const val TIMEZONE = "timezone"
    }
    object ParameterValues {
        const val CURRENT_VALUES =
            "temperature_2m,weather_code"
        const val DAILY_VALUES =
            "temperature_2m_max,temperature_2m_min"
        const val DEFAULT_TIMEZONE = "auto"
        const val DEFAULT_TEMPERATURE_UNIT = "celsius"
    }
}
