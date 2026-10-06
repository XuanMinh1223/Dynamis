package org.xuan.dynamis.ui.component

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkeletonContainerTest {
    @Test
    fun alphaStaysWithinBoundsForEveryTiming() {
        val timings = (0..20).map(::standaloneTiming) + (0..10).map(::listTiming)
        for (timing in timings) {
            for (step in 0..200) {
                assertTrue(pulseAlpha(step * 0.05f, timing) in 0.35f..1f)
            }
        }
    }

    @Test
    fun pulseRepeatsEveryPeriod() {
        val timing = standaloneTiming(5)
        assertEquals(pulseAlpha(0.7f, timing), pulseAlpha(0.7f + timing.periodSeconds, timing), 0.001f)
    }

    @Test
    fun laterListRowsLagEarlierOnes() {
        assertEquals(1f, pulseAlpha(0f, listTiming(0)), 0.0001f)
        // Row 1 reaches the peak row 0 had one lag step earlier.
        val lag = 0.08f * 1.5f
        assertEquals(1f, pulseAlpha(lag, listTiming(1)), 0.0001f)
    }

    @Test
    fun standalonePeriodsStayInRange() {
        assertTrue((0..50).all { standaloneTiming(it).periodSeconds in 1.2f..1.9f })
    }

    @Test
    fun neighbouringStandaloneBlocksStartApart() {
        for (slot in 0..50) {
            val gap = abs(standaloneTiming(slot).offset - standaloneTiming(slot + 1).offset)
            assertTrue(minOf(gap, 1f - gap) > 0.3f)
        }
    }
}
