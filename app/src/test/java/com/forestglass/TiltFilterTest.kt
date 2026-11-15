package com.forestglass

import com.forestglass.sensor.TiltFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TiltFilterTest {
    @Test
    fun tinyTremorInsideDeadzoneIsIgnored() {
        val filter = TiltFilter()
        filter.step(0f, 0f, 1f / 60f)
        val out = filter.step(0.004f, -0.005f, 1f / 60f)
        assertEquals(0f, out.x, 0.001f)
        assertEquals(0f, out.y, 0.001f)
    }

    @Test
    fun stepNeverExceedsNormalizedBounds() {
        val filter = TiltFilter()
        filter.step(0f, 0f, 1f / 60f)
        var maxSeen = 0f
        repeat(60) {
            val s = filter.step(2.0f, -2.0f, 1f / 120f)
            maxSeen = maxOf(maxSeen, abs(s.x), abs(s.y))
        }
        assertTrue(maxSeen <= 1.0f)
    }
}
