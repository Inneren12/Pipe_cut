package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DevPointTest {

    @Test
    fun `equality and hashCode work for identical values`() {
        val a = DevPoint(phiDeg = 90.0, lengthMm = 175.5)
        val b = DevPoint(phiDeg = 90.0, lengthMm = 175.5)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `copy with overridden lengthMm preserves phiDeg`() {
        val original = DevPoint(phiDeg = 90.0, lengthMm = 175.5)
        val copied = original.copy(lengthMm = 200.25)
        assertEquals(90.0, copied.phiDeg, 1e-9)
        assertEquals(200.25, copied.lengthMm, 1e-9)
    }
}
