package org.xuan.dynamis.ui.screen.home

import kotlinx.datetime.LocalDateTime
import org.xuan.dynamis.domain.model.WeatherForecast
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherUiMapperTest {
    @Test
    fun formatsLocalTimeAndRoundsTemperaturesWithoutChangingTheDate() {
        val state = forecast().toUiState("San Francisco")
        assertEquals("23:05", state.time)
        assertEquals("18°C", state.currentTemperature)
        assertEquals("22°C", state.todayHigh)
        assertEquals("-2°C", state.todayLow)
        assertEquals("San Francisco", state.locality)
    }

    @Test
    fun missingDailyReadingsDisplayAsUnavailable() {
        val state = forecast().copy(todayHigh = null, todayLow = null).toUiState()
        assertEquals("—", state.todayHigh)
        assertEquals("—", state.todayLow)
        assertEquals("", state.locality)
    }

    private fun forecast() =
        WeatherForecast(
            observedAt = LocalDateTime(2026, 9, 30, 23, 5),
            timeZone = "America/Los_Angeles",
            temperature = 18.4,
            temperatureUnit = "°C",
            weatherCode = 3,
            todayHigh = 21.6,
            todayLow = -1.8,
        )
}
