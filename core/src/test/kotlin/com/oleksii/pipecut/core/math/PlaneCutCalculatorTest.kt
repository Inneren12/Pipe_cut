package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import com.oleksii.pipecut.core.validation.DevelopmentValidator
import com.oleksii.pipecut.core.validation.Validated
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.cos
import kotlin.math.tan

private const val EPS = 1e-9

private fun request(
    diameterMm: Double = 100.0,
    tiltDeg: Double = 0.0,
    clockingDeg: Double = 0.0,
    offsetMm: Double = 200.0,
    pointCount: PointCount = PointCount.P36,
    saddle: SaddleSpec? = null,
) = CutRequest(
    pipe = PipeSpec(diameterMm = diameterMm),
    cut = CutPlane(tiltDeg = tiltDeg, clockingDeg = clockingDeg, offsetMm = offsetMm),
    saddle = saddle,
    pointCount = pointCount,
)

class PlaneCutCalculatorTest {

    // -----------------------------------------------------------------------
    // Group 1 — Happy paths with controlled values
    // -----------------------------------------------------------------------

    @Test
    fun `case A — straight cut alpha 0 yields constant length`() {
        val result = PlaneCutCalculator.calculate(
            request(diameterMm = 100.0, tiltDeg = 0.0, clockingDeg = 0.0, offsetMm = 200.0, pointCount = PointCount.P36)
        )
        assertEquals(36, result.points.size)
        result.points.forEachIndexed { i, p ->
            assertEquals(i * 10.0, p.phiDeg, EPS)
            assertEquals(200.0, p.lengthMm, EPS)
        }
        assertEquals(200.0, result.maxLengthMm!!, EPS)
        assertEquals(200.0, result.minLengthMm!!, EPS)
    }

    @Test
    fun `case B — classic non-clocked cut beta 0`() {
        val result = PlaneCutCalculator.calculate(
            request(diameterMm = 100.0, tiltDeg = 30.0, clockingDeg = 0.0, offsetMm = 200.0, pointCount = PointCount.P12)
        )
        assertEquals(12, result.points.size)
        val amplitude = 50.0 * tan(Math.toRadians(30.0))

        // φ = 0 → max
        assertEquals(0.0, result.points[0].phiDeg, EPS)
        assertEquals(200.0 + amplitude, result.points[0].lengthMm, EPS)
        // φ = 90
        assertEquals(90.0, result.points[3].phiDeg, EPS)
        assertEquals(200.0, result.points[3].lengthMm, EPS)
        // φ = 180 → min
        assertEquals(180.0, result.points[6].phiDeg, EPS)
        assertEquals(200.0 - amplitude, result.points[6].lengthMm, EPS)
        // φ = 270
        assertEquals(270.0, result.points[9].phiDeg, EPS)
        assertEquals(200.0, result.points[9].lengthMm, EPS)

        val maxIdx = result.points.indices.maxBy { result.points[it].lengthMm }
        val minIdx = result.points.indices.minBy { result.points[it].lengthMm }
        assertEquals(0.0, result.points[maxIdx].phiDeg, EPS)
        assertEquals(180.0, result.points[minIdx].phiDeg, EPS)
    }

    @Test
    fun `case C — reference drawing alpha 28 beta 12`() {
        val result = PlaneCutCalculator.calculate(
            request(diameterMm = 114.3, tiltDeg = 28.0, clockingDeg = 12.0, offsetMm = 100.0, pointCount = PointCount.P36)
        )
        assertEquals(36, result.points.size)
        // Phi step exactly 10°.
        for (i in 0 until 36) {
            assertEquals(i * 10.0, result.points[i].phiDeg, EPS)
        }
        // Numeric max on sampled grid: φ = 10° (closer to β = 12° than 20°).
        val maxIdx = result.points.indices.maxBy { result.points[it].lengthMm }
        assertEquals(10.0, result.points[maxIdx].phiDeg, EPS)
        // L(10°) ≈ 130.37 within 1e-2 — formula-derived expected value.
        val r = 114.3 / 2.0
        val amp = r * tan(Math.toRadians(28.0))
        val expectedAt10 = 100.0 + amp * cos(Math.toRadians(10.0 - 12.0))
        assertEquals(expectedAt10, result.points[1].lengthMm, EPS)
        // L(190°) symmetric.
        val expectedAt190 = 100.0 + amp * cos(Math.toRadians(190.0 - 12.0))
        assertEquals(expectedAt190, result.points[19].lengthMm, EPS)
        // Spec's hand-rounded reference values within 1e-2.
        assertEquals(130.37, result.points[1].lengthMm, 1e-2)
        assertEquals(69.63, result.points[19].lengthMm, 1e-2)
        // (max + min) / 2 ≈ L₀.
        val mid = (result.maxLengthMm!! + result.minLengthMm!!) / 2.0
        assertEquals(100.0, mid, 1e-3)
    }

    @Test
    fun `case D — extreme tilt alpha 60 beta 45`() {
        val result = PlaneCutCalculator.calculate(
            request(diameterMm = 200.0, tiltDeg = 60.0, clockingDeg = 45.0, offsetMm = 500.0, pointCount = PointCount.P72)
        )
        assertEquals(72, result.points.size)
        // 5° step → φ = 45° is exact.
        val maxIdx = result.points.indices.maxBy { result.points[it].lengthMm }
        assertEquals(45.0, result.points[maxIdx].phiDeg, EPS)

        // L(45°) ≈ 500 + 100·tan(60°)
        val amplitude = 100.0 * tan(Math.toRadians(60.0))
        assertEquals(500.0 + amplitude, result.points[9].lengthMm, 1e-9)
        // L(225°) ≈ 500 − amplitude
        assertEquals(500.0 - amplitude, result.points[45].lengthMm, 1e-9)

        // All lengths positive.
        result.points.forEach { assertTrue(it.lengthMm > 0.0, "length must be positive: ${it.lengthMm}") }
    }

    // -----------------------------------------------------------------------
    // Group 2 — Output structure
    // -----------------------------------------------------------------------

    @Test
    fun `point count matches PointCount value for every entry`() {
        for (pc in PointCount.entries) {
            val result = PlaneCutCalculator.calculate(
                request(diameterMm = 100.0, tiltDeg = 10.0, clockingDeg = 0.0, offsetMm = 200.0, pointCount = pc)
            )
            assertEquals(pc.value, result.points.size, "pc=$pc")
        }
    }

    @Test
    fun `phi values are evenly spaced and start at zero`() {
        for (pc in PointCount.entries) {
            val result = PlaneCutCalculator.calculate(
                request(tiltDeg = 10.0, pointCount = pc)
            )
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
            val result = PlaneCutCalculator.calculate(request(tiltDeg = 10.0, pointCount = pc))
            assertTrue(
                result.points.last().phiDeg < 360.0,
                "last phi must be < 360, was ${result.points.last().phiDeg} for $pc",
            )
        }
    }

    @Test
    fun `output passes DevelopmentValidator for every happy-path case`() {
        val cases = listOf(
            request(diameterMm = 100.0, tiltDeg = 0.0, clockingDeg = 0.0, offsetMm = 200.0, pointCount = PointCount.P36),
            request(diameterMm = 100.0, tiltDeg = 30.0, clockingDeg = 0.0, offsetMm = 200.0, pointCount = PointCount.P12),
            request(diameterMm = 114.3, tiltDeg = 28.0, clockingDeg = 12.0, offsetMm = 100.0, pointCount = PointCount.P36),
            request(diameterMm = 200.0, tiltDeg = 60.0, clockingDeg = 45.0, offsetMm = 500.0, pointCount = PointCount.P72),
        )
        for (req in cases) {
            val result = PlaneCutCalculator.calculate(req)
            assertTrue(
                DevelopmentValidator.validate(result) is Validated.Valid,
                "DevelopmentValidator must accept output for $req",
            )
        }
    }

    // -----------------------------------------------------------------------
    // Group 3 — Invariants over many parameter combinations
    // -----------------------------------------------------------------------

    @Test
    fun `invariants over alpha beta L0 grid`() {
        val alphas = listOf(0.0, 10.0, 28.0, 45.0, 60.0)
        val betas = listOf(-180.0, -45.0, 0.0, 12.0, 90.0, 180.0)
        val diameter = 200.0
        val r = diameter / 2.0

        for (alpha in alphas) {
            for (beta in betas) {
                val l0 = r * tan(Math.toRadians(alpha)) + 50.0
                val req = request(
                    diameterMm = diameter,
                    tiltDeg = alpha,
                    clockingDeg = beta,
                    offsetMm = l0,
                    pointCount = PointCount.P36,
                )
                val result = PlaneCutCalculator.calculate(req)
                val ctx = "α=$alpha β=$beta L₀=$l0"

                // 1. size
                assertEquals(36, result.points.size, ctx)
                // 2. non-negative
                result.points.forEach {
                    assertTrue(it.lengthMm >= 0.0, "$ctx length=${it.lengthMm}")
                }
                // 3. (max + min) / 2 ≈ L₀
                val mid = (result.maxLengthMm!! + result.minLengthMm!!) / 2.0
                assertEquals(l0, mid, 1e-6, ctx)
                // 4. periodicity: shifting β by ±360 → identical lengths.
                // Direction chosen to keep β in the validator range [-360, 360].
                val shift = if (beta > 0.0) -360.0 else 360.0
                val shifted = PlaneCutCalculator.calculate(
                    req.copy(cut = req.cut.copy(clockingDeg = beta + shift))
                )
                for (i in 0 until 36) {
                    assertEquals(
                        result.points[i].lengthMm,
                        shifted.points[i].lengthMm,
                        EPS,
                        "$ctx periodicity i=$i",
                    )
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Group 4 — Contract enforcement
    // -----------------------------------------------------------------------

    @Test
    fun `rejects a saddle request`() {
        val req = request(
            saddle = SaddleSpec(
                partnerDiameterMm = 80.0,
                intersectionAngleDeg = 90.0,
                clockingDeg = 0.0,
                offsetMm = 0.0,
            ),
        )
        val ex = assertThrows<IllegalArgumentException> { PlaneCutCalculator.calculate(req) }
        assertTrue(
            ex.message!!.contains("saddle", ignoreCase = true),
            "message must mention saddle support, was: ${ex.message}",
        )
    }

    @Test
    fun `rejects an invalid request`() {
        val req = request(diameterMm = -10.0, tiltDeg = 10.0)
        val ex = assertThrows<IllegalArgumentException> { PlaneCutCalculator.calculate(req) }
        assertTrue(
            ex.message!!.contains("DiameterMustBePositive"),
            "message must mention DiameterMustBePositive, was: ${ex.message}",
        )
    }

    @Test
    fun `does not throw on a valid request`() {
        PlaneCutCalculator.calculate(request(tiltDeg = 15.0, clockingDeg = 30.0, offsetMm = 100.0))
    }

    @Test
    fun `rejects plane cut when offset is too small for tilted plane`() {
        // D=200 → R=100; α=60° → amplitude = 100·tan(60°) ≈ 173.2;
        // L₀ = 10 < amplitude → cut would extend past the end face.
        val req = request(
            diameterMm = 200.0,
            tiltDeg = 60.0,
            clockingDeg = 0.0,
            offsetMm = 10.0,
            pointCount = PointCount.P36,
        )
        val ex = assertThrows<IllegalArgumentException> {
            PlaneCutCalculator.calculate(req)
        }
        val message = ex.message ?: ""
        assertTrue(message.contains("offsetMm", ignoreCase = true), "Got: $message")
        assertTrue(message.contains("too small", ignoreCase = true), "Got: $message")
    }
}
