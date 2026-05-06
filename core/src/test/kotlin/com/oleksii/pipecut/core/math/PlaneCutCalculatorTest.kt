package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.model.SaddleSpec
import com.oleksii.pipecut.core.validation.DevelopmentValidator
import com.oleksii.pipecut.core.validation.Validated
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import kotlin.math.cos
import kotlin.math.tan

class PlaneCutCalculatorTest {

    // ── Group 1 — happy paths with controlled values ────────────────────────

    @Test
    fun `case A — straight cut alpha 0 produces flat lengths`() {
        val request = planeRequest(diameter = 100.0, tilt = 0.0, clocking = 0.0, offset = 200.0, n = PointCount.P36)
        val dev = PlaneCutCalculator.calculate(request)

        assertEquals(36, dev.points.size)
        for ((i, p) in dev.points.withIndex()) {
            assertEquals(i * 10.0, p.phiDeg, 1e-9, "phi at index $i")
            assertEquals(200.0, p.lengthMm, 1e-9, "length at index $i")
        }
        assertNotNull(dev.maxLengthMm)
        assertNotNull(dev.minLengthMm)
        assertEquals(200.0, dev.maxLengthMm!!, 1e-9)
        assertEquals(200.0, dev.minLengthMm!!, 1e-9)
        assertTrue(DevelopmentValidator.validate(dev) is Validated.Valid)
    }

    @Test
    fun `case B — classic non-clocked cut beta 0 hits extremes at 0 and 180`() {
        val request = planeRequest(diameter = 100.0, tilt = 30.0, clocking = 0.0, offset = 200.0, n = PointCount.P12)
        val dev = PlaneCutCalculator.calculate(request)

        assertEquals(12, dev.points.size)

        val r = 50.0
        val a = r * tan(Math.toRadians(30.0))

        val byPhi = dev.points.associateBy { it.phiDeg }
        assertEquals(200.0 + a, byPhi.getValue(0.0).lengthMm, 1e-9, "max at 0°")
        assertEquals(200.0 - a, byPhi.getValue(180.0).lengthMm, 1e-9, "min at 180°")
        assertEquals(200.0, byPhi.getValue(90.0).lengthMm, 1e-9, "L₀ at 90°")
        assertEquals(200.0, byPhi.getValue(270.0).lengthMm, 1e-9, "L₀ at 270°")

        assertEquals(200.0 + a, dev.maxLengthMm!!, 1e-9)
        assertEquals(200.0 - a, dev.minLengthMm!!, 1e-9)
        assertTrue(DevelopmentValidator.validate(dev) is Validated.Valid)
    }

    @Test
    fun `case C — reference drawing alpha 28 beta 12 P36 hits expected extremes near beta`() {
        val request = planeRequest(diameter = 114.3, tilt = 28.0, clocking = 12.0, offset = 100.0, n = PointCount.P36)
        val dev = PlaneCutCalculator.calculate(request)

        assertEquals(36, dev.points.size)
        for ((i, p) in dev.points.withIndex()) {
            assertEquals(i * 10.0, p.phiDeg, 1e-9, "phi step at index $i")
        }

        val byPhi = dev.points.associateBy { it.phiDeg }
        // L(φ) = 100 + 57.15·tan(28°)·cos(φ − 12°). Spec lists hand-rounded
        // approximations (≈130.372, ≈69.628) at 1e-3 tolerance, but rounds A
        // to 30.391 vs. its exact value 30.3872, which puts the spec value
        // ~3·10⁻³ off the formula. We assert against the formula directly to
        // keep the test exact rather than against the spec's rounded numbers.
        val a = 57.15 * tan(Math.toRadians(28.0))
        val expectedAt10 = 100.0 + a * cos(Math.toRadians(10.0 - 12.0))
        val expectedAt190 = 100.0 + a * cos(Math.toRadians(190.0 - 12.0))
        assertEquals(expectedAt10, byPhi.getValue(10.0).lengthMm, 1e-9, "L(10°)")
        assertEquals(expectedAt190, byPhi.getValue(190.0).lengthMm, 1e-9, "L(190°)")
        // Sanity: matches the spec's hand-rounded values within 5·10⁻³.
        assertTrue(kotlin.math.abs(byPhi.getValue(10.0).lengthMm - 130.372) < 5e-3)
        assertTrue(kotlin.math.abs(byPhi.getValue(190.0).lengthMm - 69.628) < 5e-3)

        // The grid maximum lies on the sample closest to β=12°, which is φ=10°.
        val maxPoint = dev.points.maxBy { it.lengthMm }
        val minPoint = dev.points.minBy { it.lengthMm }
        assertEquals(10.0, maxPoint.phiDeg, 1e-9, "argmax phi")
        assertEquals(190.0, minPoint.phiDeg, 1e-9, "argmin phi")

        // Mean of max and min is L₀ for a sinusoid sampled at antipodal φs.
        assertEquals(100.0, (dev.maxLengthMm!! + dev.minLengthMm!!) / 2.0, 1e-3)
        assertTrue(DevelopmentValidator.validate(dev) is Validated.Valid)
    }

    @Test
    fun `case D — extreme tilt alpha 60 beta 45 P72 stays positive and hits expected extremes`() {
        val request = planeRequest(diameter = 200.0, tilt = 60.0, clocking = 45.0, offset = 500.0, n = PointCount.P72)
        val dev = PlaneCutCalculator.calculate(request)

        assertEquals(72, dev.points.size)

        val byPhi = dev.points.associateBy { it.phiDeg }
        val a = 100.0 * tan(Math.toRadians(60.0))
        assertEquals(500.0 + a, byPhi.getValue(45.0).lengthMm, 1e-9, "L(45°)")
        assertEquals(500.0 - a, byPhi.getValue(225.0).lengthMm, 1e-9, "L(225°)")

        val maxPoint = dev.points.maxBy { it.lengthMm }
        val minPoint = dev.points.minBy { it.lengthMm }
        assertEquals(45.0, maxPoint.phiDeg, 1e-9, "argmax phi")
        assertEquals(225.0, minPoint.phiDeg, 1e-9, "argmin phi")

        for (p in dev.points) {
            assertTrue(p.lengthMm > 0.0, "length at ${p.phiDeg}° must be positive")
        }
        assertTrue(DevelopmentValidator.validate(dev) is Validated.Valid)
    }

    // ── Group 2 — output structure ─────────────────────────────────────────

    @TestFactory
    fun `point count matches PointCount value for every enum entry`(): List<DynamicTest> =
        PointCount.entries.map { pc ->
            DynamicTest.dynamicTest("size == ${pc.value} for $pc") {
                val request = planeRequest(diameter = 100.0, tilt = 10.0, clocking = 0.0, offset = 200.0, n = pc)
                val dev = PlaneCutCalculator.calculate(request)
                assertEquals(pc.value, dev.points.size)
                assertTrue(DevelopmentValidator.validate(dev) is Validated.Valid)
            }
        }

    @Test
    fun `phi values are evenly spaced and start at zero`() {
        val request = planeRequest(diameter = 100.0, tilt = 10.0, clocking = 0.0, offset = 200.0, n = PointCount.P72)
        val dev = PlaneCutCalculator.calculate(request)
        val n = dev.points.size
        val step = 360.0 / n
        assertEquals(0.0, dev.points[0].phiDeg, 1e-9)
        for (i in dev.points.indices) {
            assertEquals(i * step, dev.points[i].phiDeg, 1e-9, "phi[$i]")
        }
    }

    @Test
    fun `phi values stay strictly under 360`() {
        for (pc in PointCount.entries) {
            val request = planeRequest(diameter = 100.0, tilt = 10.0, clocking = 0.0, offset = 200.0, n = pc)
            val dev = PlaneCutCalculator.calculate(request)
            assertTrue(dev.points.last().phiDeg < 360.0, "${pc.name}: last phi must be < 360, was ${dev.points.last().phiDeg}")
        }
    }

    @Test
    fun `output passes DevelopmentValidator on every happy path case`() {
        val cases = listOf(
            planeRequest(100.0, 0.0, 0.0, 200.0, PointCount.P36),
            planeRequest(100.0, 30.0, 0.0, 200.0, PointCount.P12),
            planeRequest(114.3, 28.0, 12.0, 100.0, PointCount.P36),
            planeRequest(200.0, 60.0, 45.0, 500.0, PointCount.P72)
        )
        assertAll(
            cases.map { req ->
                Executable {
                    val dev = PlaneCutCalculator.calculate(req)
                    val v = DevelopmentValidator.validate(dev)
                    assertTrue(v is Validated.Valid, "validator must accept output for $req, got $v")
                }
            }
        )
    }

    // ── Group 3 — invariants over a (α, β, L₀) grid ────────────────────────

    @TestFactory
    fun `invariants over alpha beta L0 grid`(): List<DynamicTest> {
        val alphas = listOf(0.0, 10.0, 28.0, 45.0, 60.0)
        val betas = listOf(-180.0, -45.0, 0.0, 12.0, 90.0, 180.0)
        val diameter = 200.0
        val r = diameter / 2.0
        val tests = mutableListOf<DynamicTest>()

        for (alpha in alphas) {
            for (beta in betas) {
                val l0 = r * tan(Math.toRadians(alpha)) + 50.0
                tests += DynamicTest.dynamicTest("α=$alpha β=$beta L₀=${"%.4f".format(l0)}") {
                    val request = planeRequest(diameter, alpha, beta, l0, PointCount.P36)
                    val dev = PlaneCutCalculator.calculate(request)

                    assertEquals(36, dev.points.size, "size")

                    for (p in dev.points) {
                        assertTrue(p.lengthMm >= 0.0, "length at ${p.phiDeg}° must be ≥ 0, was ${p.lengthMm}")
                    }

                    val mid = (dev.maxLengthMm!! + dev.minLengthMm!!) / 2.0
                    assertEquals(l0, mid, 1e-6, "(max+min)/2 should equal L₀")

                    // Periodicity: shifting β by ±360° leaves lengths unchanged.
                    // Pick the sign that keeps β within the validator's
                    // [-360, 360] range.
                    val shiftedBeta = if (beta <= 0.0) beta + 360.0 else beta - 360.0
                    val shifted = PlaneCutCalculator.calculate(
                        planeRequest(diameter, alpha, shiftedBeta, l0, PointCount.P36)
                    )
                    for (i in dev.points.indices) {
                        assertEquals(
                            dev.points[i].lengthMm,
                            shifted.points[i].lengthMm,
                            1e-9,
                            "periodicity at index $i"
                        )
                    }
                }
            }
        }
        return tests
    }

    // ── Group 4 — contract enforcement ─────────────────────────────────────

    @Test
    fun `rejects a saddle request`() {
        val saddle = SaddleSpec(
            partnerDiameterMm = 50.0,
            intersectionAngleDeg = 90.0,
            clockingDeg = 0.0,
            offsetMm = 0.0
        )
        val request = CutRequest(
            pipe = PipeSpec(diameterMm = 100.0),
            cut = CutPlane(tiltDeg = 10.0, clockingDeg = 0.0, offsetMm = 200.0),
            saddle = saddle,
            pointCount = PointCount.P36
        )
        val ex = assertThrows<IllegalArgumentException> { PlaneCutCalculator.calculate(request) }
        assertTrue(
            ex.message!!.contains("saddle", ignoreCase = true),
            "message should mention saddle, was: ${ex.message}"
        )
    }

    @Test
    fun `rejects an invalid request with negative diameter`() {
        val request = CutRequest(
            pipe = PipeSpec(diameterMm = -10.0),
            cut = CutPlane(tiltDeg = 10.0, clockingDeg = 0.0, offsetMm = 200.0),
            saddle = null,
            pointCount = PointCount.P36
        )
        val ex = assertThrows<IllegalArgumentException> { PlaneCutCalculator.calculate(request) }
        assertTrue(
            ex.message!!.contains("DiameterMustBePositive"),
            "message should contain DiameterMustBePositive, was: ${ex.message}"
        )
    }

    @Test
    fun `does not throw on a valid request`() {
        val request = planeRequest(100.0, 10.0, 0.0, 200.0, PointCount.P36)
        // Just make sure no exception is thrown.
        val dev = PlaneCutCalculator.calculate(request)
        assertEquals(36, dev.points.size)
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private fun planeRequest(
        diameter: Double,
        tilt: Double,
        clocking: Double,
        offset: Double,
        n: PointCount
    ): CutRequest = CutRequest(
        pipe = PipeSpec(diameterMm = diameter),
        cut = CutPlane(tiltDeg = tilt, clockingDeg = clocking, offsetMm = offset),
        saddle = null,
        pointCount = n
    )
}
