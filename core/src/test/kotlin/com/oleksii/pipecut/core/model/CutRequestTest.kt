package com.oleksii.pipecut.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CutRequestTest {

    private val pipe = PipeSpec(diameterMm = 219.1)
    private val cut = CutPlane(tiltDeg = 28.0, rotationDeg = 12.0, offsetMm = 150.0)
    private val saddle = SaddleSpec(partnerDiameterMm = 914.4)

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

    /**
     * Contract: `saddle` has no default value. Calling
     * `CutRequest(pipe, cut, pointCount = PointCount.P36)` would fail to
     * compile in this PR. This test asserts the explicit-null branch instead.
     */
    @Test
    fun `saddle defaults are not provided`() {
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
