package com.oleksii.pipecut.core.validation

import com.oleksii.pipecut.core.model.CutPlane
import com.oleksii.pipecut.core.model.CutRequest
import com.oleksii.pipecut.core.model.PipeSpec
import com.oleksii.pipecut.core.model.SaddleSpec

/**
 * Validates a [CutRequest] in a single pass and returns either the original
 * request as [Validated.Valid] or every error found as [Validated.Invalid].
 *
 * Rules applied:
 *
 *  - Pipe diameter: > 0 mm.
 *  - Cut tilt: 0 ≤ α ≤ 89 degrees.
 *  - Cut clocking: -360 ≤ β ≤ 360 degrees.
 *  - Cut offset: ≥ 0 mm.
 *  - When a saddle is present:
 *      - Partner diameter: > 0 mm.
 *      - Intersection angle: 0 < θ < 180 degrees (open interval).
 *      - Saddle clocking: -360 ≤ ψ ≤ 360 degrees.
 *      - Saddle offset: ≥ 0 mm.
 *
 * No cross-field rules in this PR (e.g., partner-vs-pipe diameter, or
 * angle-vs-offset interactions). They can be added in later PRs.
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
        if (pipe.diameterMm <= 0.0) {
            add(ValidationError.DiameterMustBePositive(pipe.diameterMm))
        }
    }

    private fun validateCutPlane(cut: CutPlane): List<ValidationError> = buildList {
        if (cut.tiltDeg < 0.0 || cut.tiltDeg > 89.0) {
            add(ValidationError.TiltOutOfRange(cut.tiltDeg))
        }
        if (cut.clockingDeg < -360.0 || cut.clockingDeg > 360.0) {
            add(ValidationError.ClockingOutOfRange(cut.clockingDeg))
        }
        if (cut.offsetMm < 0.0) {
            add(ValidationError.CutOffsetNegative(cut.offsetMm))
        }
    }

    private fun validateSaddle(saddle: SaddleSpec): List<ValidationError> = buildList {
        if (saddle.partnerDiameterMm <= 0.0) {
            add(ValidationError.PartnerDiameterMustBePositive(saddle.partnerDiameterMm))
        }
        if (saddle.intersectionAngleDeg <= 0.0 || saddle.intersectionAngleDeg >= 180.0) {
            add(ValidationError.IntersectionAngleOutOfRange(saddle.intersectionAngleDeg))
        }
        if (saddle.clockingDeg < -360.0 || saddle.clockingDeg > 360.0) {
            add(ValidationError.SaddleClockingOutOfRange(saddle.clockingDeg))
        }
        if (saddle.offsetMm < 0.0) {
            add(ValidationError.SaddleOffsetNegative(saddle.offsetMm))
        }
    }
}
