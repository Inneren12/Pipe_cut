package com.oleksii.pipecut.ui.input

import com.oleksii.pipecut.core.model.PointCount
import com.oleksii.pipecut.core.validation.ValidationError

/**
 * Snapshot of the input form, used by both the ViewModel and the Composable.
 *
 * Numeric fields are kept as raw strings so the user's typing experience is
 * preserved (trailing dots, leading minuses, etc.). Validation converts them
 * to Double on demand.
 */
data class InputUiState(
    val diameterMm: String = "",
    val tiltDeg: String = "",
    val clockingDeg: String = "",
    val offsetMm: String = "",
    val pointCount: PointCount = PointCount.P36,
    val saddleEnabled: Boolean = false,
    val partnerDiameterMm: String = "",
    val intersectionAngleDeg: String = "",
    val saddleClockingDeg: String = "",
    val saddleOffsetMm: String = "",
    /** Per-field touched flags. A field is touched once the user has edited it. */
    val touched: Set<FieldKey> = emptySet(),
    /** Per-field error map. Empty when the form is currently valid. */
    val errors: Map<FieldKey, FieldError> = emptyMap(),
)

/** Identifier for every input field, including the saddle group. */
enum class FieldKey {
    DIAMETER,
    TILT,
    CLOCKING,
    OFFSET,
    /** Included for uniform touched tracking; the dropdown cannot produce a parse or validation error. */
    POINT_COUNT,
    PARTNER_DIAMETER,
    INTERSECTION_ANGLE,
    SADDLE_CLOCKING,
    SADDLE_OFFSET,
}

/**
 * UI-side error union. Either the user typed something un-parseable, or
 * `CutRequestValidator` rejected the value. `String`-formatted messages live
 * in [InputFieldStrings]; this layer stays type-safe.
 */
sealed interface FieldError {
    object NotANumber : FieldError
    object Required : FieldError
    data class Domain(val cause: ValidationError) : FieldError
}
