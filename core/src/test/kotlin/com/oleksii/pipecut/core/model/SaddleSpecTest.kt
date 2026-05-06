package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class SaddleSpecTest {

    private val base = SaddleSpec(
        partnerDiameterMm = 914.4,
        intersectionAngleDeg = 90.0,
        clockingDeg = 30.0,
        offsetMm = 25.0
    )

    @Test
    fun `equality and hashCode work for identical values`() {
        val a = SaddleSpec(
            partnerDiameterMm = 914.4,
            intersectionAngleDeg = 90.0,
            clockingDeg = 30.0,
            offsetMm = 25.0
        )
        val b = SaddleSpec(
            partnerDiameterMm = 914.4,
            intersectionAngleDeg = 90.0,
            clockingDeg = 30.0,
            offsetMm = 25.0
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality on partnerDiameterMm difference`() {
        assertNotEquals(base, base.copy(partnerDiameterMm = 323.9))
    }

    @Test
    fun `inequality on intersectionAngleDeg difference`() {
        assertNotEquals(base, base.copy(intersectionAngleDeg = 60.0))
    }

    @Test
    fun `inequality on clockingDeg difference`() {
        assertNotEquals(base, base.copy(clockingDeg = 0.0))
    }

    @Test
    fun `inequality on offsetMm difference`() {
        assertNotEquals(base, base.copy(offsetMm = 0.0))
    }

    @Test
    fun `copy preserves other fields`() {
        val copied = base.copy(clockingDeg = 45.0)
        assertEquals(914.4, copied.partnerDiameterMm, 1e-9)
        assertEquals(90.0, copied.intersectionAngleDeg, 1e-9)
        assertEquals(45.0, copied.clockingDeg, 1e-9)
        assertEquals(25.0, copied.offsetMm, 1e-9)
    }
}
