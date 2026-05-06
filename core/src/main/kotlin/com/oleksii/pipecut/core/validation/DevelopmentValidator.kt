package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.Development

/**
 * Validates a [Development] produced by a calculator. Used by PR4/PR5 in tests
 * and may be used by the UI to guard against malformed results.
 *
 * Rules applied:
 *
 *  - Points list is non-empty.
 *  - Every phi is in [0, 360).
 *  - Every length is ≥ 0.
 *  - Phi values are strictly ascending (which also implies uniqueness).
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
            if (p.phiDeg < 0.0 || p.phiDeg >= 360.0) {
                errors.add(ValidationError.DevPointPhiOutOfRange(index, p.phiDeg))
            }
            if (p.lengthMm < 0.0) {
                errors.add(ValidationError.DevPointLengthNegative(index, p.lengthMm))
            }
        }

        for (i in 1 until points.size) {
            val prev = points[i - 1].phiDeg
            val curr = points[i].phiDeg
            val delta = curr - prev
            when {
                kotlin.math.abs(delta) <= PHI_TOLERANCE -> {
                    errors.add(ValidationError.DevPointsDuplicatePhi(i - 1, i, curr))
                }
                delta < 0.0 -> {
                    errors.add(ValidationError.DevPointsNotAscending(i, prev, curr))
                }
            }
        }

        return if (errors.isEmpty()) Validated.valid(development) else Validated.invalid(errors)
    }
}
