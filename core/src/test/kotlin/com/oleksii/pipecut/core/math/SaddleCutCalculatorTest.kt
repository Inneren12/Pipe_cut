package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import com.oleksii.pipecut.core.validation.DevelopmentValidator
import com.oleksii.pipecut.core.validation.Validated
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.tan

private const val EPS = 1e-9

private fun saddleRequest(
    diameterMm: Double = 100.0,
    partnerDiameterMm: Double = 200.0,
    intersectionAngleDeg: Double = 90.0,
    saddleClockingDeg: Double = 0.0,
    eccentricOffsetMm: Double = 0.0,
    cutOffsetMm: Double = 200.0,
    pointCount: PointCount = PointCount.P36,
) = CutRequest(
    pipe = PipeSpec(diameterMm = diameterMm),
    cut = CutPlane(tiltDeg = 0.0, clockingDeg = 0.0, offsetMm = cutOffsetMm),
    saddle = SaddleSpec(
        partnerDiameterMm = partnerDiameterMm,
        intersectionAngleDeg = intersectionAngleDeg,
        clockingDeg = saddleClockingDeg,
        offsetMm = eccentricOffsetMm,
    ),
    pointCount = pointCount,
)

class SaddleCutCalculatorTest {

    // -----------------------------------------------------------------------
    // Group 1 — Reference real-world case (centered)
    // -----------------------------------------------------------------------

    @Test
    fun `reference drawing — branch 114_3 main 914_4 theta 62`() {
        // L₀ needs to clear R₂/sinθ ≈ 518 mm of fishmouth depth plus the
        // R₁·cot θ ≈ 30 mm hip extension. 600 mm gives ~52 mm of headroom.
        val req = saddleRequest(
            diameterMm = 114.3,
            partnerDiameterMm = 914.4,
            intersectionAngleDeg = 62.0,
            saddleClockingDeg = 0.0,
            eccentricOffsetMm = 0.0,
            cutOffsetMm = 600.0,
            pointCount = PointCount.P36,
        )
        val result = SaddleCutCalculator.calculate(req)

        assertEquals(36, result.points.size)
        for (i in 0 until 36) {
            assertEquals(i * 10.0, result.points[i].phiDeg, EPS)
        }

        val maxIdx = result.points.indices.maxBy { result.points[it].lengthMm }
        val minIdx = result.points.indices.minBy { result.points[it].lengthMm }
        assertEquals(0.0, result.points[maxIdx].phiDeg, EPS)
        assertEquals(180.0, result.points[minIdx].phiDeg, EPS)

        result.points.forEach { assertTrue(it.lengthMm >= 0.0, "negative length: ${it.lengthMm}") }
        assertTrue(DevelopmentValidator.validate(result) is Validated.Valid)

        val r1 = 114.3 / 2.0
        val expectedRange = 2.0 * r1 / tan(Math.toRadians(62.0))
        val actualRange = result.maxLengthMm!! - result.minLengthMm!!
        assertEquals(expectedRange, actualRange, expectedRange * 0.01)
    }

    // -----------------------------------------------------------------------
    // Group 2 — Perpendicular tee (θ = 90°)
    // -----------------------------------------------------------------------

    @Test
    fun `perpendicular tee analytic targets`() {
        val req = saddleRequest(
            diameterMm = 100.0,
            partnerDiameterMm = 200.0,
            intersectionAngleDeg = 90.0,
            saddleClockingDeg = 0.0,
            eccentricOffsetMm = 0.0,
            cutOffsetMm = 150.0,
            pointCount = PointCount.P36,
        )
        val result = SaddleCutCalculator.calculate(req)

        // φ = 0° → 150 − 100 = 50
        assertEquals(50.0, result.points[0].lengthMm, EPS)
        // φ = 90° → 150 − √(100² − 50²)
        val expected90 = 150.0 - sqrt(100.0 * 100.0 - 50.0 * 50.0)
        assertEquals(expected90, result.points[9].lengthMm, EPS)
        // φ = 180° → 50
        assertEquals(50.0, result.points[18].lengthMm, EPS)
        // φ = 270° symmetric to 90°
        assertEquals(expected90, result.points[27].lengthMm, EPS)
    }

    // -----------------------------------------------------------------------
    // Group 3 — Clocking
    // -----------------------------------------------------------------------

    @Test
    fun `clocking shifts the maximum to phi closest to psi`() {
        // Oblique θ < 90° is required: for the perpendicular case the max
        // sits at φ = ψ ± 90° because cos θ = 0 kills the term that pulls
        // the peak to ψ.
        val req = saddleRequest(
            diameterMm = 100.0,
            partnerDiameterMm = 300.0,
            intersectionAngleDeg = 60.0,
            saddleClockingDeg = 45.0,
            eccentricOffsetMm = 0.0,
            cutOffsetMm = 250.0,
            pointCount = PointCount.P72,
        )
        val result = SaddleCutCalculator.calculate(req)
        assertEquals(72, result.points.size)

        val maxIdx = result.points.indices.maxBy { result.points[it].lengthMm }
        assertEquals(45.0, result.points[maxIdx].phiDeg, EPS)
    }

    // -----------------------------------------------------------------------
    // Group 4 — Eccentric offset
    // -----------------------------------------------------------------------

    @Test
    fun `offset solver matches centered solver in the limit`() {
        val base = saddleRequest(
            diameterMm = 100.0,
            partnerDiameterMm = 300.0,
            intersectionAngleDeg = 90.0,
            saddleClockingDeg = 0.0,
            eccentricOffsetMm = 0.0,
            cutOffsetMm = 200.0,
            pointCount = PointCount.P36,
        )
        val centered = SaddleCutCalculator.calculate(base)

        val tinyOffset = base.copy(
            saddle = base.saddle!!.copy(offsetMm = 1e-6),
        )
        val almost = SaddleCutCalculator.calculate(tinyOffset)

        for (i in 0 until 36) {
            assertEquals(
                centered.points[i].lengthMm,
                almost.points[i].lengthMm,
                1e-6,
                "i=$i centered=${centered.points[i].lengthMm} almost=${almost.points[i].lengthMm}",
            )
        }
    }

    @Test
    fun `eccentric offset 20 mm produces non-trivial difference`() {
        val base = saddleRequest(
            diameterMm = 100.0,
            partnerDiameterMm = 300.0,
            intersectionAngleDeg = 90.0,
            saddleClockingDeg = 0.0,
            eccentricOffsetMm = 0.0,
            cutOffsetMm = 200.0,
            pointCount = PointCount.P36,
        )
        val centered = SaddleCutCalculator.calculate(base)

        val offset = SaddleCutCalculator.calculate(
            base.copy(saddle = base.saddle!!.copy(offsetMm = 20.0))
        )

        offset.points.forEach { assertTrue(it.lengthMm >= 0.0) }
        assertTrue(DevelopmentValidator.validate(offset) is Validated.Valid)

        val maxDelta = (0 until 36).maxOf {
            abs(offset.points[it].lengthMm - centered.points[it].lengthMm)
        }
        assertTrue(maxDelta > 1.0, "expected > 1 mm difference, got $maxDelta")
    }

    // -----------------------------------------------------------------------
    // Group 5 — Output structure
    // -----------------------------------------------------------------------

    @Test
    fun `point count matches PointCount value for every entry`() {
        for (pc in PointCount.entries) {
            val result = SaddleCutCalculator.calculate(
                saddleRequest(pointCount = pc)
            )
            assertEquals(pc.value, result.points.size, "pc=$pc")
        }
    }

    @Test
    fun `phi values are evenly spaced and start at zero`() {
        for (pc in PointCount.entries) {
            val result = SaddleCutCalculator.calculate(saddleRequest(pointCount = pc))
            val step = 360.0 / pc.value
            assertEquals(0.0, result.points[0].phiDeg, EPS)
            result.points.forEachIndexed { i, p ->
                assertEquals(i * step, p.phiDeg, EPS, "pc=$pc i=$i")
            }
        }
    }

    @Test
    fun `phi values stay strictly under 360`() {
        for (pc in PointCount.entries) {
            val result = SaddleCutCalculator.calculate(saddleRequest(pointCount = pc))
            assertTrue(
                result.points.last().phiDeg < 360.0,
                "last phi must be < 360, was ${result.points.last().phiDeg} for $pc",
            )
        }
    }

    @Test
    fun `output passes DevelopmentValidator for every happy-path case`() {
        val cases = listOf(
            saddleRequest(
                diameterMm = 114.3,
                partnerDiameterMm = 914.4,
                intersectionAngleDeg = 62.0,
                cutOffsetMm = 600.0,
            ),
            saddleRequest(
                diameterMm = 100.0,
                partnerDiameterMm = 200.0,
                intersectionAngleDeg = 90.0,
                cutOffsetMm = 150.0,
            ),
            saddleRequest(
                diameterMm = 100.0,
                partnerDiameterMm = 300.0,
                intersectionAngleDeg = 90.0,
                saddleClockingDeg = 45.0,
                cutOffsetMm = 200.0,
                pointCount = PointCount.P72,
            ),
            saddleRequest(
                diameterMm = 100.0,
                partnerDiameterMm = 300.0,
                intersectionAngleDeg = 90.0,
                eccentricOffsetMm = 20.0,
                cutOffsetMm = 200.0,
            ),
        )
        for (req in cases) {
            val result = SaddleCutCalculator.calculate(req)
            assertTrue(
                DevelopmentValidator.validate(result) is Validated.Valid,
                "DevelopmentValidator must accept output for $req",
            )
        }
    }

    // -----------------------------------------------------------------------
    // Group 6 — Contract enforcement
    // -----------------------------------------------------------------------

    @Test
    fun `rejects a plane request`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = 100.0),
            cut = CutPlane(tiltDeg = 10.0, clockingDeg = 0.0, offsetMm = 200.0),
            saddle = null,
            pointCount = PointCount.P36,
        )
        val ex = assertThrows<IllegalArgumentException> { SaddleCutCalculator.calculate(req) }
        assertTrue(
            ex.message!!.contains("saddle", ignoreCase = true),
            "message must mention saddle, was: ${ex.message}",
        )
    }

    @Test
    fun `rejects an invalid request`() {
        val req = saddleRequest(diameterMm = -10.0)
        val ex = assertThrows<IllegalArgumentException> { SaddleCutCalculator.calculate(req) }
        assertTrue(
            ex.message!!.contains("DiameterMustBePositive"),
            "message must mention DiameterMustBePositive, was: ${ex.message}",
        )
    }

    @Test
    fun `rejects partner smaller than branch`() {
        val req = saddleRequest(
            diameterMm = 200.0,
            partnerDiameterMm = 100.0,
            intersectionAngleDeg = 90.0,
            cutOffsetMm = 200.0,
        )
        val ex = assertThrows<IllegalArgumentException> { SaddleCutCalculator.calculate(req) }
        assertTrue(
            ex.message!!.contains("partner radius", ignoreCase = true),
            "message must mention partner radius, was: ${ex.message}",
        )
    }

    @Test
    fun `rejects offset too small for the geometry`() {
        val req = saddleRequest(
            diameterMm = 100.0,
            partnerDiameterMm = 200.0,
            intersectionAngleDeg = 90.0,
            cutOffsetMm = 10.0,
        )
        val ex = assertThrows<IllegalArgumentException> { SaddleCutCalculator.calculate(req) }
        val message = ex.message ?: ""
        assertTrue(message.contains("offsetMm", ignoreCase = true), "Got: $message")
        assertTrue(message.contains("too small", ignoreCase = true), "Got: $message")
        // Sanity: invariant assertion to silence unused-import warnings.
        assertNotEquals("", message)
    }

    @Test
    fun `reference drawing geometry with L0 = 250 is rejected as offset too small`() {
        // The user's drawing pipe is 1079 mm long, so 250 mm is a plausible
        // user input but it is geometrically below the fishmouth depth for
        // partner Ø914.4 at θ=62°. Document the real bound: this case must
        // fail loudly, not return garbage.
        val req = saddleRequest(
            diameterMm = 114.3,
            partnerDiameterMm = 914.4,
            intersectionAngleDeg = 62.0,
            cutOffsetMm = 250.0,
            pointCount = PointCount.P36,
        )
        val ex = assertThrows<IllegalArgumentException> {
            SaddleCutCalculator.calculate(req)
        }
        val message = ex.message ?: ""
        assertTrue(message.contains("offsetMm", ignoreCase = true), "Got: $message")
        assertTrue(message.contains("too small", ignoreCase = true), "Got: $message")
    }

    @Test
    fun `rejects saddle request with non-zero cut tilt`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = 100.0),
            cut = CutPlane(tiltDeg = 5.0, clockingDeg = 0.0, offsetMm = 200.0),
            saddle = SaddleSpec(
                partnerDiameterMm = 200.0,
                intersectionAngleDeg = 90.0,
                clockingDeg = 0.0,
                offsetMm = 0.0,
            ),
            pointCount = PointCount.P36,
        )
        val ex = assertThrows<IllegalArgumentException> {
            SaddleCutCalculator.calculate(req)
        }
        assertTrue(
            ex.message!!.contains("tiltDeg", ignoreCase = false),
            "message must mention tiltDeg, was: ${ex.message}",
        )
    }

    @Test
    fun `rejects eccentric saddle when offset plus branch radius exceeds partner radius`() {
        // r1 = 50, r2 = 60, e = 20  →  r1 + e = 70 > r2 = 60.
        val req = saddleRequest(
            diameterMm = 100.0,
            partnerDiameterMm = 120.0,
            intersectionAngleDeg = 90.0,
            eccentricOffsetMm = 20.0,
            cutOffsetMm = 200.0,
        )
        val ex = assertThrows<IllegalArgumentException> {
            SaddleCutCalculator.calculate(req)
        }
        assertTrue(
            ex.message!!.contains("full 360", ignoreCase = true),
            "message must explain the 360° reach failure, was: ${ex.message}",
        )
    }

    @Test
    fun `rejects saddle request with non-zero cut clocking`() {
        val req = CutRequest(
            pipe = PipeSpec(diameterMm = 100.0),
            cut = CutPlane(tiltDeg = 0.0, clockingDeg = 12.0, offsetMm = 200.0),
            saddle = SaddleSpec(
                partnerDiameterMm = 200.0,
                intersectionAngleDeg = 90.0,
                clockingDeg = 0.0,
                offsetMm = 0.0,
            ),
            pointCount = PointCount.P36,
        )
        val ex = assertThrows<IllegalArgumentException> {
            SaddleCutCalculator.calculate(req)
        }
        assertTrue(
            ex.message!!.contains("clockingDeg", ignoreCase = false),
            "message must mention clockingDeg, was: ${ex.message}",
        )
    }
}
