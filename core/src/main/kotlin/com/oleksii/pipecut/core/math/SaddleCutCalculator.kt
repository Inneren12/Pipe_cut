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
        // For a full 360° development the entire branch must stay inside the
        // partner's perpendicular reach. With eccentric offset e ≥ 0 (PR3
        // validator rejects negative e) this collapses to r1 + e ≤ r2.
        require(r1 + e <= r2) {
            "Invalid eccentric saddle: branch radius $r1 mm plus offset $e mm " +
                "exceeds partner radius $r2 mm; full 360° development has no " +
                "intersection for every phi."
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

    /**
     * Closed-form solution of the branch-main intersection for the
     * eccentric case.
     *
     * Working in w = z - l0 (so the main axis passes through (0, e, 0)
     * in the shifted frame), the squared distance from the branch
     * surface point at angle phi at axial coordinate z to the main axis
     * is a quadratic in w:
     *
     *   sin²θ · w²
     *     − 2·R₁·cos(φ−ψ)·sinθ·cosθ · w
     *     + ( R₁²·cos²(φ−ψ)·cos²θ + (R₁·sin(φ−ψ) − e)² − R₂² )
     *     = 0
     *
     * Both roots are real under the calculator's preconditions
     * (`r1 + e ≤ r2` ensures every branch generatrix intersects the
     * main cylinder). The lower root corresponds to the cut profile
     * that sits between the open end face and the axes' intersection
     * point — the same branch that the centered analytic formula
     * picks. The upper root is the second intersection on the far side
     * of the main pipe.
     *
     * Validator (PR3) forbids θ ≤ 0 and θ ≥ 180°, so sin²θ > 0 for
     * every accepted request. Guarded explicitly so the failure mode is
     * obvious if validators ever loosen.
     */
    private fun offsetZ(
        phiRad: Double,
        psiRad: Double,
        thetaRad: Double,
        r1: Double,
        r2: Double,
        e: Double,
        l0: Double,
    ): Double {
        val angle = phiRad - psiRad
        val cosPhi = cos(angle)
        val sinPhi = sin(angle)
        val sinT = sin(thetaRad)
        val cosT = cos(thetaRad)
        val a = sinT * sinT
        require(a > 0.0) {
            "Invalid saddle geometry at phi=${Math.toDegrees(phiRad)}: " +
                "sin²θ is zero. Validator should have rejected θ=${Math.toDegrees(thetaRad)}°."
        }
        val b = -2.0 * r1 * cosPhi * sinT * cosT
        val perpY = r1 * sinPhi - e
        val c = r1 * r1 * cosPhi * cosPhi * cosT * cosT + perpY * perpY - r2 * r2
        val discriminant = b * b - 4.0 * a * c
        require(discriminant >= 0.0) {
            "Invalid saddle geometry at phi=${Math.toDegrees(phiRad)}: " +
                "no real intersection (discriminant=$discriminant). " +
                "This should be unreachable given r1+e ≤ r2."
        }
        val sqrtDisc = sqrt(discriminant)
        // Lower root in w (equivalently, lower z): coincides with the
        // -sqrt branch of the centered formula and remains continuous as
        // e → 0. Picking "nearest to l0" looks tempting but disagrees
        // with `centeredZ` whenever cos(φ-ψ) < 0.
        val w1 = (-b - sqrtDisc) / (2.0 * a)
        return l0 + w1
    }
}
