package org.xuan.dynamis.ui.screen.home

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

class MoonPhaseTest {
    @Test
    fun newAndFullMoonHaveExpectedCycleFractions() {
        val newMoon = moonPhaseFraction(LocalDateTime(2024, 1, 11, 11, 57), "UTC")
        val fullMoon = moonPhaseFraction(LocalDateTime(2024, 1, 25, 17, 54), "UTC")

        assertTrue(newMoon < 0.01f)
        assertTrue(fullMoon in 0.45f..0.55f)
    }

    @Test
    fun usesTheForecastLocationTimeZone() {
        val utc = moonPhaseFraction(LocalDateTime(2024, 1, 11, 11, 57), "UTC")
        val losAngeles =
            moonPhaseFraction(
                LocalDateTime(2024, 1, 11, 3, 57),
                "America/Los_Angeles",
            )

        assertTrue(kotlin.math.abs(utc - losAngeles) < 0.0001f)
    }
}
