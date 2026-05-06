package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DevelopmentTest {

    @Test
    fun `maxLengthMm and minLengthMm return correct values for a known list`() {
        val dev = Development(
            points = listOf(
                DevPoint(phiDeg = 0.0, lengthMm = 100.0),
                DevPoint(phiDeg = 90.0, lengthMm = 175.5),
                DevPoint(phiDeg = 180.0, lengthMm = 200.0),
                DevPoint(phiDeg = 270.0, lengthMm = 50.25)
            )
        )
        assertEquals(200.0, dev.maxLengthMm!!, 1e-9)
        assertEquals(50.25, dev.minLengthMm!!, 1e-9)
    }

    @Test
    fun `maxLengthMm and minLengthMm return null for an empty list`() {
        val dev = Development(points = emptyList())
        assertNull(dev.maxLengthMm)
        assertNull(dev.minLengthMm)
    }

    @Test
    fun `equality holds when point lists are identical`() {
        val points = listOf(
            DevPoint(phiDeg = 0.0, lengthMm = 100.0),
            DevPoint(phiDeg = 180.0, lengthMm = 200.0)
        )
        val a = Development(points = points)
        val b = Development(points = points.toList())
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
