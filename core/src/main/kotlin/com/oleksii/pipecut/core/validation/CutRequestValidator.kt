package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.SaddleSpec

/**
 * Validates a [CutRequest] in a single pass and returns either the original
 * request as [Validated.Valid] or every error found as [Validated.Invalid].
 *
 * Rules applied (every Double field is also required to be finite — NaN and
 * ±Infinity are rejected via the same error variants):
 *
 *  - Pipe diameter: finite and > 0 mm.
 *  - Cut tilt: finite and 0 ≤ α ≤ 89 degrees.
 *  - Cut clocking: finite and -360 ≤ β ≤ 360 degrees.
 *  - Cut offset: finite and ≥ 0 mm.
 *  - When a saddle is present:
 *      - Partner diameter: finite and > 0 mm.
 *      - Intersection angle: finite and 0 < θ < 180 degrees.
 *      - Saddle clocking: finite and -360 ≤ ψ ≤ 360 degrees.
 *      - Saddle offset: finite and ≥ 0 mm.
 *
 * No cross-field rules in this PR.
 */
object CutRequestValidator {

    fun validate(request: CutRequest): Validated<CutRequest> {
        val errors = buildList {
            addAll(validatePipe(request.pipe))
            addAll(validateCutPlane(request.cut))
            request.saddle?.let { addAll(validateSaddle(it)) }
        }
        return if (errors.isEmpty()) Validated.valid(request) else Validated.invalid(errors)
    }

    private fun validatePipe(pipe: PipeSpec): List<ValidationError> = buildList {
        if (!pipe.diameterMm.isFinite() || pipe.diameterMm <= 0.0) {
            add(ValidationError.DiameterMustBePositive(pipe.diameterMm))
        }
    }

    private fun validateCutPlane(cut: CutPlane): List<ValidationError> = buildList {
        if (!cut.tiltDeg.isFinite() || cut.tiltDeg < 0.0 || cut.tiltDeg > 89.0) {
            add(ValidationError.TiltOutOfRange(cut.tiltDeg))
        }
        if (!cut.clockingDeg.isFinite() || cut.clockingDeg < -360.0 || cut.clockingDeg > 360.0) {
            add(ValidationError.ClockingOutOfRange(cut.clockingDeg))
        }
        if (!cut.offsetMm.isFinite() || cut.offsetMm < 0.0) {
            add(ValidationError.CutOffsetNegative(cut.offsetMm))
        }
    }

    private fun validateSaddle(saddle: SaddleSpec): List<ValidationError> = buildList {
        if (!saddle.partnerDiameterMm.isFinite() || saddle.partnerDiameterMm <= 0.0) {
            add(ValidationError.PartnerDiameterMustBePositive(saddle.partnerDiameterMm))
        }
        if (!saddle.intersectionAngleDeg.isFinite() ||
            saddle.intersectionAngleDeg <= 0.0 ||
            saddle.intersectionAngleDeg >= 180.0
        ) {
            add(ValidationError.IntersectionAngleOutOfRange(saddle.intersectionAngleDeg))
        }
        if (!saddle.clockingDeg.isFinite() ||
            saddle.clockingDeg < -360.0 ||
            saddle.clockingDeg > 360.0
        ) {
            add(ValidationError.SaddleClockingOutOfRange(saddle.clockingDeg))
        }
        if (!saddle.offsetMm.isFinite() || saddle.offsetMm < 0.0) {
            add(ValidationError.SaddleOffsetNegative(saddle.offsetMm))
        }
    }
}
