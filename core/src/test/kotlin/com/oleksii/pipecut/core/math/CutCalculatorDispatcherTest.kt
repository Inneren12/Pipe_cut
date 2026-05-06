package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val EPS = 1e-12

class CutCalculatorDispatcherTest {

    @Test
    fun `dispatches plane request to PlaneCutCalculator`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = 100.0),
            cut = CutPlane(tiltDeg = 30.0, clockingDeg = 12.0, offsetMm = 200.0),
            saddle = null,
            pointCount = PointCount.P36,
        )
        val viaDispatcher = CutCalculatorDispatcher.calculate(req)
        val direct = PlaneCutCalculator.calculate(req)

        assertEquals(direct.points.size, viaDispatcher.points.size)
        for (i in direct.points.indices) {
            assertEquals(direct.points[i].phiDeg, viaDispatcher.points[i].phiDeg, EPS)
            assertEquals(direct.points[i].lengthMm, viaDispatcher.points[i].lengthMm, EPS)
        }
    }

    @Test
    fun `dispatches saddle request to SaddleCutCalculator`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = 114.3),
            cut = CutPlane(tiltDeg = 0.0, clockingDeg = 0.0, offsetMm = 600.0),
            saddle = SaddleSpec(
                partnerDiameterMm = 914.4,
                intersectionAngleDeg = 62.0,
                clockingDeg = 0.0,
                offsetMm = 0.0,
            ),
            pointCount = PointCount.P36,
        )
        val viaDispatcher = CutCalculatorDispatcher.calculate(req)
        val direct = SaddleCutCalculator.calculate(req)

        assertEquals(direct.points.size, viaDispatcher.points.size)
        for (i in direct.points.indices) {
            assertEquals(direct.points[i].phiDeg, viaDispatcher.points[i].phiDeg, EPS)
            assertEquals(direct.points[i].lengthMm, viaDispatcher.points[i].lengthMm, EPS)
        }
    }

    @Test
    fun `propagates IllegalArgumentException from underlying calculator`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = -10.0),
            cut = CutPlane(tiltDeg = 10.0, clockingDeg = 0.0, offsetMm = 200.0),
            saddle = null,
            pointCount = PointCount.P36,
        )
        val ex = assertThrows<IllegalArgumentException> {
            CutCalculatorDispatcher.calculate(req)
        }
        assertTrue(
            ex.message!!.contains("DiameterMustBePositive"),
            "message must mention DiameterMustBePositive, was: ${ex.message}",
        )
    }
}
