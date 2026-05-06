package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.Development

/**
 * Validates a [Development] produced by a calculator.
 *
 * Rules:
 *  - Non-empty.
 *  - Every `phiDeg` is in `[0, 360)`.
 *  - `phiDeg` values are strictly ascending (which also rules out duplicates).
 *  - Every `lengthMm` is ≥ 0.
 */
object DevelopmentValidator {

    fun validate(development: Development): Validated<Development> {
        val errors = mutableListOf<ValidationError>()
        val points = development.points

        if (points.isEmpty()) {
            errors += ValidationError.DevPointsEmpty
            return Validated.Invalid(errors)
        }

        for ((i, p) in points.withIndex()) {
            if (p.phiDeg < 0.0 || p.phiDeg >= 360.0) {
                errors += ValidationError.DevPointPhiOutOfRange(p.phiDeg)
            }
            if (p.lengthMm < 0.0) {
                errors += ValidationError.DevPointLengthNegative(p.phiDeg, p.lengthMm)
            }
            if (i > 0) {
                val prev = points[i - 1].phiDeg
                if (p.phiDeg == prev) {
                    errors += ValidationError.DevPointPhiDuplicate(p.phiDeg)
                } else if (p.phiDeg < prev) {
                    errors += ValidationError.DevPointPhiNotAscending(prev, p.phiDeg)
                }
            }
        }

        return if (errors.isEmpty()) Validated.Valid(development) else Validated.Invalid(errors)
    }
}
