package com.oleksii.pipecut.core.validation

/**
 * Closed set of validation failures the core domain can produce.
 *
 * Errors are split between `CutRequest` (input) and `Development` (output)
 * concerns. New error types are added here rather than as ad-hoc strings so
 * that callers can pattern-match and produce user-facing messages.
 */
sealed class ValidationError {

    // CutRequest — pipe
    data object DiameterMustBePositive : ValidationError()

    // CutRequest — cut plane
    data class TiltOutOfRange(val tiltDeg: Double) : ValidationError()
    data class ClockingOutOfRange(val clockingDeg: Double) : ValidationError()
    data class OffsetMustBeNonNegative(val offsetMm: Double) : ValidationError()

    // CutRequest — saddle
    data object PartnerDiameterMustBePositive : ValidationError()
    data class IntersectionAngleOutOfRange(val angleDeg: Double) : ValidationError()
    data class SaddleClockingOutOfRange(val clockingDeg: Double) : ValidationError()

    // Development
    data object DevPointsEmpty : ValidationError()
    data class DevPointPhiOutOfRange(val phiDeg: Double) : ValidationError()
    data class DevPointPhiNotAscending(val previousPhiDeg: Double, val currentPhiDeg: Double) : ValidationError()
    data class DevPointPhiDuplicate(val phiDeg: Double) : ValidationError()
    data class DevPointLengthNegative(val phiDeg: Double, val lengthMm: Double) : ValidationError()
}
