package com.oleksii.pipecut.core.validation

/**
 * Typed validation errors. Each variant carries the offending value(s) so that
 * the UI can present a precise, actionable message without parsing strings.
 *
 * The error catalog is intentionally explicit — adding a new validation rule
 * adds a new variant here.
 */
sealed interface ValidationError {

    // --- PipeSpec ----------------------------------------------------------

    data class DiameterMustBePositive(val actualMm: Double) : ValidationError

    // --- CutPlane ----------------------------------------------------------

    /** [actualDeg] must be in `[0, 89]`. */
    data class TiltOutOfRange(val actualDeg: Double) : ValidationError {
        val minDeg: Double = 0.0
        val maxDeg: Double = 89.0
    }

    /** [actualDeg] must be in `[-360, 360]`. */
    data class ClockingOutOfRange(val actualDeg: Double) : ValidationError {
        val minDeg: Double = -360.0
        val maxDeg: Double = 360.0
    }

    /** [actualMm] must be `>= 0`. */
    data class CutOffsetNegative(val actualMm: Double) : ValidationError

    // --- SaddleSpec --------------------------------------------------------

    data class PartnerDiameterMustBePositive(val actualMm: Double) : ValidationError

    /** [actualDeg] must be in `(0, 180)`. */
    data class IntersectionAngleOutOfRange(val actualDeg: Double) : ValidationError {
        val minDegExclusive: Double = 0.0
        val maxDegExclusive: Double = 180.0
    }

    /** [actualDeg] must be in `[-360, 360]`. */
    data class SaddleClockingOutOfRange(val actualDeg: Double) : ValidationError {
        val minDeg: Double = -360.0
        val maxDeg: Double = 360.0
    }

    /** [actualMm] must be `>= 0`. Negative eccentric offsets are not yet supported. */
    data class SaddleOffsetNegative(val actualMm: Double) : ValidationError

    // --- Development (output validation) -----------------------------------

    object DevelopmentEmpty : ValidationError {
        override fun toString(): String = "DevelopmentEmpty"
    }

    /** A point's [actualDeg] is outside `[0, 360)`. */
    data class DevPointPhiOutOfRange(val indexInList: Int, val actualDeg: Double) : ValidationError

    /** A point's [actualMm] is negative. */
    data class DevPointLengthNegative(val indexInList: Int, val actualMm: Double) : ValidationError

    /** Two consecutive points have non-ascending phi. */
    data class DevPointsNotAscending(
        val indexInList: Int,
        val previousPhiDeg: Double,
        val currentPhiDeg: Double,
    ) : ValidationError

    /** Two points share the same phi value. */
    data class DevPointsDuplicatePhi(
        val firstIndex: Int,
        val secondIndex: Int,
        val phiDeg: Double,
    ) : ValidationError
}
