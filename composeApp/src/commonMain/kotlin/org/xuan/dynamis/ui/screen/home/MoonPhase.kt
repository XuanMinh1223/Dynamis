package org.xuan.dynamis.ui.screen.home

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** Fraction of a lunar cycle since the 11 January 2024 new moon (0 = new, 0.5 = full). */
internal fun moonPhaseFraction(observedAt: LocalDateTime, timeZone: String): Float {
    val observationMillis = observedAt.toInstant(TimeZone.of(timeZone)).toEpochMilliseconds()
    val daysSinceNewMoon = (observationMillis - 1_704_974_220_000L) / 86_400_000.0
    val cycles = daysSinceNewMoon / 29.530588853
    return ((cycles % 1.0 + 1.0) % 1.0).toFloat()
}
