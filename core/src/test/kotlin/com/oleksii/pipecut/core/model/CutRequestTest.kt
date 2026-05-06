package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CutRequestTest {

    private val pipe = PipeSpec(diameterMm = 219.1)
    private val cut = CutPlane(tiltDeg = 28.0, clockingDeg = 12.0, offsetMm = 150.0)
    private val saddle = SaddleSpec(
        partnerDiameterMm = 914.4,
        intersectionAngleDeg = 90.0,
        clockingDeg = 0.0,
        offsetMm = 0.0
    )

    @Test
    fun `equality without saddle`() {
        val a = CutRequest(pipe = pipe, cut = cut, saddle = null, pointCount = PointCount.P36)
        val b = CutRequest(pipe = pipe, cut = cut, saddle = null, pointCount = PointCount.P36)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `equality with saddle`() {
        val a = CutRequest(pipe = pipe, cut = cut, saddle = saddle, pointCount = PointCount.P36)
        val b = CutRequest(pipe = pipe, cut = cut, saddle = saddle, pointCount = PointCount.P36)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality between flat and saddle requests`() {
        val flat = CutRequest(pipe = pipe, cut = cut, saddle = null, pointCount = PointCount.P36)
        val withSaddle = CutRequest(pipe = pipe, cut = cut, saddle = saddle, pointCount = PointCount.P36)
        assertNotEquals(flat, withSaddle)
    }

    @Test
    fun `flat and saddle requests can both be constructed explicitly`() {
        val flat = CutRequest(
            pipe = pipe,
            cut = cut,
            saddle = null,
            pointCount = PointCount.P36
        )
        val withSaddle = CutRequest(
            pipe = pipe,
            cut = cut,
            saddle = saddle,
            pointCount = PointCount.P36
        )
        assertNull(flat.saddle)
        assertNotNull(withSaddle.saddle)
        assertEquals(914.4, withSaddle.saddle!!.partnerDiameterMm, 1e-9)
    }
}
