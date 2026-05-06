package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.CutRequest

/**
 * Validates a [CutRequest] before it is handed to a calculator.
 *
 * Rules:
 *  - Pipe `diameterMm` must be > 0.
 *  - Cut `tiltDeg` α must be in `[0, 90)` — 90° is geometrically degenerate.
 *  - Cut `clockingDeg` β must be in `[-360, 360]` — `cos` is periodic so the
 *    calculator does not need a tighter range; values outside this window are
 *    almost certainly user mistakes.
 *  - Cut `offsetMm` must be ≥ 0.
 *  - When `saddle != null`:
 *      - partner diameter > 0,
 *      - intersection angle in `(0, 90]`,
 *      - saddle clocking in `[-360, 360]`.
 *
 * All rules are checked; the result accumulates every violation.
 */
object CutRequestValidator {

    fun validate(request: CutRequest): Validated<CutRequest> {
        val errors = mutableListOf<ValidationError>()

        if (request.pipe.diameterMm <= 0.0) {
            errors += ValidationError.DiameterMustBePositive
        }

        val cut = request.cut
        if (cut.tiltDeg < 0.0 || cut.tiltDeg >= 90.0) {
            errors += ValidationError.TiltOutOfRange(cut.tiltDeg)
        }
        if (cut.clockingDeg < -360.0 || cut.clockingDeg > 360.0) {
            errors += ValidationError.ClockingOutOfRange(cut.clockingDeg)
        }
        if (cut.offsetMm < 0.0) {
            errors += ValidationError.OffsetMustBeNonNegative(cut.offsetMm)
        }

        request.saddle?.let { saddle ->
            if (saddle.partnerDiameterMm <= 0.0) {
                errors += ValidationError.PartnerDiameterMustBePositive
            }
            if (saddle.intersectionAngleDeg <= 0.0 || saddle.intersectionAngleDeg > 90.0) {
                errors += ValidationError.IntersectionAngleOutOfRange(saddle.intersectionAngleDeg)
            }
            if (saddle.clockingDeg < -360.0 || saddle.clockingDeg > 360.0) {
                errors += ValidationError.SaddleClockingOutOfRange(saddle.clockingDeg)
            }
        }

        return if (errors.isEmpty()) Validated.Valid(request) else Validated.Invalid(errors)
    }
}
