package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.DevPoint
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.validation.CutRequestValidator
import com.oleksii.pipecut.core.validation.DevelopmentValidator
import com.oleksii.pipecut.core.validation.Validated
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calculates the developed cut curve for a **saddle** cut: this pipe (the
 * branch) is fitted onto a partner pipe (the main).
 *
 * For the centered case (saddle.offsetMm == 0) the calculator uses the
 * analytic formula
 *
 * ```
 * z(φ) = L₀ + ( R₁ · cos(φ − ψ) · cos(θ)
 *               − √( R₂² − (R₁ · sin(φ − ψ))² ) ) / sin(θ)
 * ```
 *
 * For the offset case (saddle.offsetMm != 0) it uses bisection on the
 * branch–main distance equation per sampled φ.
 *
 * The calculator handles **only** the saddle case (`request.saddle != null`).
 * Plane cuts go to `PlaneCutCalculator`.
 */
object SaddleCutCalculator : CutCalculator {

    private const val BISECTION_ITERATIONS = 60
    private const val ECCENTRIC_TOLERANCE_MM = 1e-12

    override fun calculate(request: CutRequest): Development {
        val saddle = request.saddle
        require(saddle != null) {
            "SaddleCutCalculator requires a non-null SaddleSpec. Use PlaneCutCalculator for plane cuts."
        }

        when (val v = CutRequestValidator.validate(request)) {
            is Validated.Invalid -> throw IllegalArgumentException(
                "Invalid CutRequest: ${v.errors}"
            )
            is Validated.Valid -> Unit
        }

        // PR5 saddle calculator does not implement combined plane-trim
        // geometry. If the user wants a tilted/clocked plane on top of a
        // saddle, that is a separate calculator and a separate model decision.
        // Until then, fail loudly instead of silently dropping the parameters.
        require(request.cut.tiltDeg == 0.0) {
            "SaddleCutCalculator currently supports only pure saddle cuts: " +
                "cut.tiltDeg must be 0 for saddle requests, got ${request.cut.tiltDeg}."
        }
        require(request.cut.clockingDeg == 0.0) {
            "SaddleCutCalculator currently supports only pure saddle cuts: " +
                "cut.clockingDeg must be 0 for saddle requests, got ${request.cut.clockingDeg}."
        }

        val r1 = request.pipe.radiusMm
        val r2 = saddle.partnerDiameterMm / 2.0
        val thetaRad = Math.toRadians(saddle.intersectionAngleDeg)
        val psiRad = Math.toRadians(saddle.clockingDeg)
        val e = saddle.offsetMm
        val l0 = request.cut.offsetMm
        val n = request.pointCount.value
        val step = 360.0 / n

        require(r2 >= r1) {
            "Invalid saddle: partner radius $r2 mm must be ≥ branch radius $r1 mm."
        }
        require(e + r2 >= r1) {
            "Invalid saddle: eccentric offset $e mm leaves the branch outside the partner reach."
        }

        val centered = abs(e) < ECCENTRIC_TOLERANCE_MM
        val points = (0 until n).map { i ->
            val phiDeg = i * step
            val phiRad = Math.toRadians(phiDeg)
            val z = if (centered) {
                centeredZ(phiRad, psiRad, thetaRad, r1, r2, l0)
            } else {
                offsetZ(phiRad, psiRad, thetaRad, r1, r2, e, l0)
            }
            DevPoint(phiDeg = phiDeg, lengthMm = z)
        }

        // Plane-specific precondition equivalent: any negative length means
        // L₀ is too small for the geometry. Domain error, not a math bug.
        val minLength = points.minOf { it.lengthMm }
        require(minLength >= 0.0) {
            "Invalid saddle cut: offsetMm=$l0 is too small for this geometry; " +
                "minimum generated length would be $minLength mm. Increase offsetMm."
        }

        val development = Development(points)
        when (val v = DevelopmentValidator.validate(development)) {
            is Validated.Invalid -> throw IllegalStateException(
                "SaddleCutCalculator produced an invalid Development. " +
                    "This is a bug. Errors: ${v.errors}"
            )
            is Validated.Valid -> Unit
        }
        return development
    }

    private fun centeredZ(
        phiRad: Double,
        psiRad: Double,
        thetaRad: Double,
        r1: Double,
        r2: Double,
        l0: Double,
    ): Double {
        val angle = phiRad - psiRad
        val sinPart = r1 * sin(angle)
        val cosPart = r1 * cos(angle)
        val discriminant = r2 * r2 - sinPart * sinPart
        require(discriminant >= 0.0) {
            "Invalid saddle geometry at phi=${Math.toDegrees(phiRad)}: branch exceeds partner reach."
        }
        return l0 + (cosPart * cos(thetaRad) - sqrt(discriminant)) / sin(thetaRad)
    }

    private fun distanceSquaredToMainAxis(
        phiRad: Double,
        psiRad: Double,
        thetaRad: Double,
        r1: Double,
        e: Double,
        l0: Double,
        z: Double,
    ): Double {
        val angle = phiRad - psiRad
        val px = r1 * cos(angle)
        val py = r1 * sin(angle) - e
        val pz = z - l0
        val dx = sin(thetaRad)
        val dy = 0.0
        val dz = cos(thetaRad)
        val dot = px * dx + py * dy + pz * dz
        val ex = px - dot * dx
        val ey = py - dot * dy
        val ez = pz - dot * dz
        return ex * ex + ey * ey + ez * ez
    }

    private fun offsetZ(
        phiRad: Double,
        psiRad: Double,
        thetaRad: Double,
        r1: Double,
        r2: Double,
        e: Double,
        l0: Double,
    ): Double {
        val zMax = r1 + r2 + abs(e)
        var lo = l0 - zMax
        var hi = l0

        fun signedGap(z: Double): Double =
            distanceSquaredToMainAxis(phiRad, psiRad, thetaRad, r1, e, l0, z) - r2 * r2

        val gapLo = signedGap(lo)
        val gapHi = signedGap(hi)
        require(gapLo * gapHi <= 0.0) {
            "Invalid saddle geometry at phi=${Math.toDegrees(phiRad)}: " +
                "no intersection in the expected half-bracket. Bracket signs: " +
                "lo=$gapLo, hi=$gapHi."
        }

        repeat(BISECTION_ITERATIONS) {
            val mid = (lo + hi) / 2.0
            val gapMid = signedGap(mid)
            if (gapMid * gapLo <= 0.0) {
                hi = mid
            } else {
                lo = mid
            }
        }
        return (lo + hi) / 2.0
    }
}
