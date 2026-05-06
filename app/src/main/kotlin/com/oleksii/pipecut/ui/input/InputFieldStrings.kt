package com.oleksii.pipecut.ui.input

import com.oleksii.pipecut.core.validation.ValidationError

/**
 * Maps typed [FieldError]s to human-readable English strings. Localization
 * lands in PR11; for now every message is a plain English literal.
 *
 * Kept as a top-level `object` so it has no Compose dependency and can be
 * unit-tested on the JVM.
 */
object InputFieldStrings {

    fun message(error: FieldError): String = when (error) {
        FieldError.NotANumber -> "Not a number"
        FieldError.Required -> "Required"
        is FieldError.Domain -> domainMessage(error.cause)
    }

    private fun domainMessage(error: ValidationError): String = when (error) {
        is ValidationError.DiameterMustBePositive ->
            "Diameter must be a finite positive number; got ${error.actualMm}"
        is ValidationError.TiltOutOfRange ->
            "Tilt must be in [${error.minDeg}°, ${error.maxDeg}°]; got ${error.actualDeg}°"
        is ValidationError.ClockingOutOfRange ->
            "Clocking must be in [${error.minDeg}°, ${error.maxDeg}°]; got ${error.actualDeg}°"
        is ValidationError.CutOffsetNegative ->
            "Offset must be a finite non-negative number; got ${error.actualMm}"
        is ValidationError.PartnerDiameterMustBePositive ->
            "Partner diameter must be finite positive; got ${error.actualMm}"
        is ValidationError.IntersectionAngleOutOfRange ->
            "Intersection angle must be in (${error.minDegExclusive}°, ${error.maxDegExclusive}°); " +
                "got ${error.actualDeg}°"
        is ValidationError.SaddleClockingOutOfRange ->
            "Saddle clocking must be in [${error.minDeg}°, ${error.maxDeg}°]; got ${error.actualDeg}°"
        is ValidationError.SaddleOffsetNegative ->
            "Saddle offset must be finite non-negative; got ${error.actualMm}"
        ValidationError.DevelopmentEmpty,
        is ValidationError.DevPointPhiOutOfRange,
        is ValidationError.DevPointLengthNegative,
        is ValidationError.DevPointsNotAscending,
        is ValidationError.DevPointsDuplicatePhi -> "Internal error: $error"
    }
}
