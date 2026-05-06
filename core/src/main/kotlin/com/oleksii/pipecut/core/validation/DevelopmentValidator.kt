package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.Development
import kotlin.math.abs

/**
 * Validates a [Development] produced by a calculator.
 *
 * Rules applied:
 *
 *  - Points list is non-empty.
 *  - Every phi is finite and in [0, 360).
 *  - Every length is finite and ≥ 0.
 *  - Phi values are strictly ascending (which also implies uniqueness).
 *
 * NaN and ±Infinity are reported via [ValidationError.DevPointPhiOutOfRange]
 * and [ValidationError.DevPointLengthNegative] (no separate variants).
 *
 * Duplicate detection is reported separately from order errors: when two
 * adjacent points share a phi value, [ValidationError.DevPointsDuplicatePhi]
 * is emitted *instead of* [ValidationError.DevPointsNotAscending] for that
 * pair.
 */
object DevelopmentValidator {

    private const val PHI_TOLERANCE = 1e-9

    fun validate(development: Development): Validated<Development> {
        val errors = mutableListOf<ValidationError>()
        val points = development.points

        if (points.isEmpty()) {
            errors.add(ValidationError.DevelopmentEmpty)
            return Validated.invalid(errors)
        }

        points.forEachIndexed { index, p ->
            if (!p.phiDeg.isFinite() || p.phiDeg < 0.0 || p.phiDeg >= 360.0) {
                errors.add(ValidationError.DevPointPhiOutOfRange(index, p.phiDeg))
            }
            if (!p.lengthMm.isFinite() || p.lengthMm < 0.0) {
                errors.add(ValidationError.DevPointLengthNegative(index, p.lengthMm))
            }
        }

        for (i in 1 until points.size) {
            val prev = points[i - 1].phiDeg
            val curr = points[i].phiDeg
            // If either side is non-finite, the per-point check above already
            // recorded an error and we skip the order check for this pair.
            if (!prev.isFinite() || !curr.isFinite()) continue

            val delta = curr - prev
            when {
                abs(delta) <= PHI_TOLERANCE ->
                    errors.add(ValidationError.DevPointsDuplicatePhi(i - 1, i, curr))

                delta < 0.0 ->
                    errors.add(ValidationError.DevPointsNotAscending(i, prev, curr))
            }
        }

        return if (errors.isEmpty()) Validated.valid(development) else Validated.invalid(errors)
    }
}
