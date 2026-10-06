package org.xuan.dynamis.data.source.api

object ApiConstants {
    const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
    const val RADAR_MAPS_URL = "https://api.rainviewer.com/public/weather-maps.json"
    const val BASE_MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/positron"

    object RadarTiles {
        // The largest size RainViewer serves; MapLibre treats 512 px tiles as one zoom level sharper.
        const val SIZE = 512
        const val MAX_ZOOM = 7

        // Color scheme 2 is "Universal Blue".
        const val COLOR_SCHEME = 2
    }

    object ParameterNames {
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CURRENT = "current"
        const val HOURLY = "hourly"
        const val DAILY = "daily"
        const val FORECAST_DAYS = "forecast_days"
        const val TEMPERATURE_UNIT = "temperature_unit"
        const val TIMEZONE = "timezone"
    }

    object ParameterValues {
        const val CURRENT_VALUES = "temperature_2m,weather_code"
        const val HOURLY_VALUES = "temperature_2m,weather_code"
        const val DAILY_VALUES =
            "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code"
        const val FORECAST_DAYS = "16"
        const val DEFAULT_TIMEZONE = "auto"
        const val DEFAULT_TEMPERATURE_UNIT = "celsius"
    }
}
