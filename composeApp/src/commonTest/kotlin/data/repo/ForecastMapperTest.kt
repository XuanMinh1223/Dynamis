package data.repo

import data.source.api.dto.Current
import data.source.api.dto.CurrentUnits
import data.source.api.dto.Daily
import data.source.api.dto.ForecastResponse
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ForecastMapperTest {
    private fun response() = ForecastResponse(
        timezone = "America/Los_Angeles",
        currentUnits = CurrentUnits("°C"),
        current = Current("2026-09-30T23:45", 18.4, 3),
        daily = Daily(listOf("2026-09-29", "2026-09-30"), listOf(25.0, 22.0), listOf(15.0, 12.0)),
    )

    @Test
    fun matchesDailyTemperaturesUsingForecastLocalDate() {
        val forecast = response().toWeatherForecast()
        assertEquals(LocalDateTime(2026, 9, 30, 23, 45), forecast.observedAt)
        assertEquals("America/Los_Angeles", forecast.timeZone)
        assertEquals(18.4, forecast.temperature)
        assertEquals(22.0, forecast.todayHigh)
        assertEquals(12.0, forecast.todayLow)
    }

    @Test
    fun missingDateLeavesDailyTemperaturesUnavailable() {
        val forecast = response().copy(daily = Daily(listOf("2026-10-01"), listOf(22.0), listOf(12.0)))
            .toWeatherForecast()
        assertNull(forecast.todayHigh)
        assertNull(forecast.todayLow)
    }

    @Test
    fun shortOrNullDailyArraysDoNotCrash() {
        val forecast = response().copy(daily = Daily(listOf("2026-09-30"), listOf(null), emptyList()))
            .toWeatherForecast()
        assertNull(forecast.todayHigh)
        assertNull(forecast.todayLow)
    }

    @Test
    fun missingDailySectionDoesNotInventTemperatures() {
        val forecast = response().copy(daily = null).toWeatherForecast()
        assertNull(forecast.todayHigh)
        assertNull(forecast.todayLow)
    }

    @Test
    fun rejectsMissingCurrentWeather() {
        assertFailsWith<InvalidForecastException> { response().copy(current = null).toWeatherForecast() }
    }

    @Test
    fun rejectsMissingOrNonFiniteTemperature() {
        listOf(null, Double.NaN, Double.POSITIVE_INFINITY).forEach { temperature ->
            assertFailsWith<InvalidForecastException> {
                response().copy(current = Current("2026-09-30T23:45", temperature)).toWeatherForecast()
            }
        }
    }

    @Test
    fun rejectsMalformedTimeAndTimezone() {
        assertFailsWith<InvalidForecastException> {
            response().copy(current = Current("not-a-time", 18.4)).toWeatherForecast()
        }
        assertFailsWith<InvalidForecastException> { response().copy(timezone = "invalid/zone").toWeatherForecast() }
    }

    @Test
    fun rejectsMissingUnitsInsteadOfRenderingNull() {
        assertFailsWith<InvalidForecastException> { response().copy(currentUnits = null).toWeatherForecast() }
    }
}
