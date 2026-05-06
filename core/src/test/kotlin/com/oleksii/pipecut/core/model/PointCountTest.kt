package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PointCountTest {

    @Test
    fun `fromInt returns the matching entry for every supported value`() {
        for (entry in PointCount.entries) {
            assertEquals(entry, PointCount.fromInt(entry.value))
        }
    }

    @Test
    fun `fromInt returns null for unsupported values`() {
        for (n in listOf(13, 0, 1000, -1)) {
            assertNull(PointCount.fromInt(n), "expected null for $n")
        }
    }

    @Test
    fun `value equals the documented integer`() {
        assertEquals(12, PointCount.P12.value)
        assertEquals(24, PointCount.P24.value)
        assertEquals(36, PointCount.P36.value)
        assertEquals(72, PointCount.P72.value)
        assertEquals(120, PointCount.P120.value)
        assertEquals(180, PointCount.P180.value)
        assertEquals(360, PointCount.P360.value)
    }
}
