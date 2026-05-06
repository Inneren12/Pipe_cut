package com.oleksii.pipecut.core.math

import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.DevPoint
import com.oleksii.pipecut.core.model.Development
import com.oleksii.pipecut.core.validation.CutRequestValidator
import com.oleksii.pipecut.core.validation.DevelopmentValidator
import com.oleksii.pipecut.core.validation.Validated
import kotlin.math.cos
import kotlin.math.tan

/**
 * Calculates the developed cut curve for a **flat plane** cut.
 *
 * The cut plane is parameterized by:
 *
 *  - α (tiltDeg)     — tilt from perpendicular.
 *  - β (clockingDeg) — angular direction of the tilt around the pipe axis.
 *  - L₀ (offsetMm)   — axial position where the plane meets the pipe axis.
 *
 * Length along the pipe surface as a function of angle around the pipe:
 *
 * ```
 * L(φ) = L₀ + R · tan(α) · cos(φ − β)
 * ```
 *
 * Points are emitted at `φᵢ = i · 360 / N` for `i ∈ [0, N)`, where N is
 * `request.pointCount.value`.
 *
 * This calculator handles **only** the plane case (`request.saddle == null`).
 * Saddle math arrives in PR5.
 */
object PlaneCutCalculator : CutCalculator {

    override fun calculate(request: CutRequest): Development {
        require(request.saddle == null) {
            "PlaneCutCalculator does not support saddle requests. Use a saddle calculator instead."
        }

        when (val v = CutRequestValidator.validate(request)) {
            is Validated.Invalid -> throw IllegalArgumentException(
                "Invalid CutRequest: ${v.errors}"
            )
            is Validated.Valid -> Unit
        }

        val r = request.pipe.diameterMm / 2.0
        val alphaRad = Math.toRadians(request.cut.tiltDeg)
        val betaRad = Math.toRadians(request.cut.clockingDeg)
        val l0 = request.cut.offsetMm
        val n = request.pointCount.value
        val step = 360.0 / n
        val amplitude = r * tan(alphaRad)

        val points = (0 until n).map { i ->
            val phiDeg = i * step
            val phiRad = Math.toRadians(phiDeg)
            val length = l0 + amplitude * cos(phiRad - betaRad)
            DevPoint(phiDeg = phiDeg, lengthMm = length)
        }

        val development = Development(points)

        when (val v = DevelopmentValidator.validate(development)) {
            is Validated.Invalid -> throw IllegalStateException(
                "PlaneCutCalculator produced an invalid Development. " +
                    "This is a bug. Errors: ${v.errors}"
            )
            is Validated.Valid -> Unit
        }

        return development
    }
}
