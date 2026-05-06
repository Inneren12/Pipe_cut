package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class CutPlaneTest {

    @Test
    fun `equality holds when tilt, rotation, offset all match`() {
        val a = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 150.0)
        val b = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 150.0)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality on tilt difference`() {
        val a = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 150.0)
        val b = CutPlane(tiltDeg = 30.0, rotationDeg = 12.0, offsetMm = 150.0)
        assertNotEquals(a, b)
    }

    @Test
    fun `inequality on rotation difference`() {
        val a = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 150.0)
        val b = CutPlane(tiltDeg = 28.0, rotationDeg = 0.0, offsetMm = 150.0)
        assertNotEquals(a, b)
    }

    @Test
    fun `inequality on offset difference`() {
        val a = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 150.0)
        val b = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 200.0)
        assertNotEquals(a, b)
    }

    @Test
    fun `copy preserves other fields`() {
        val original = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 150.0)
        val copied = original.copy(rotationDeg = 45.0)
        assertEquals(28.0, copied.tiltDeg, 1e-9)
        assertEquals(45.0, copied.rotationDeg, 1e-9)
        assertEquals(150.0, copied.offsetMm, 1e-9)
    }
}
